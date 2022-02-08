package eu.bcvsolutions.freeipa.post;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.bcvsolutions.freeipa.connector.FreeIPAConfiguration;
import eu.bcvsolutions.freeipa.connector.httpclient.HttpClient;
import eu.bcvsolutions.freeipa.connector.httpclient.HttpResponse;
import eu.bcvsolutions.freeipa.connector.security.FreeIPAAuthenticator;
import eu.bcvsolutions.freeipa.connector.security.FreeIPAPasswordAuthenticator;
import eu.bcvsolutions.freeipa.connector.security.FreeIPASessionCookie;
import eu.bcvsolutions.freeipa.service.response.ErrorResponseResult;
import eu.bcvsolutions.freeipa.service.response.JSONResponse;

/**
 * This class is responsible for building requests to IPA.
 * <p>
 * Example: { "id": 0, "method": "user_show", "params": [ [ "admin" ], { "all":
 * false, "no_members": false, "raw": false, "rights": false, "version": "2.156"
 * } ] }
 */

public abstract class ServicePost<E> {

	private static final Log log = Log.getLog(ServicePost.class);

	public static final String IPA_EXPIRED_SESSION_ERROR_CODE = "";

	public static final int DEFAULT_MAX_REQUESTS_REPEATS = 5;

	public static final int DEFAULT_REQUEST_REPEAT_TIME_OFFSET = 50;

	//TODO coinfigurable max repeats?

	/**
	 * If this variable is true, then each JSON request and response will
	 * be printed in log.
	 */
	private final boolean debug;

	private final FreeIPAConfiguration config;

	public ServicePost(final boolean debug, FreeIPAConfiguration config) {
		this.debug = debug;
		this.config = config;
	}

	/**
	 * Sends post request to FreeIPA end point. This method uses template method pattern
	 * and its purpose is to build POST request, send it and parse response.
	 *
	 * @param wc
	 * @return
	 * @throws ConnectorException if some error occurred while sending request
	 */
	public final JSONResponse<E> post(final HttpClient wc) {
		int repeatCount = 0;

		try {
			boolean repeat = false;
			ResponseResult result;
			do {
				initAuthenticatorAndAuthenticate(wc);
				//
				final HttpClient cl = wc.path(getSubPath());
				String json = prepareRequest(wc, cl);
				//
				HttpResponse r = cl.post(json);
				result = handleResponse(r);
				repeat = result.isRequestRepeatNeeded();
				if (repeat) {
					log.info("Sleeping for {0}ms before trying again", DEFAULT_REQUEST_REPEAT_TIME_OFFSET);
					Thread.sleep(DEFAULT_REQUEST_REPEAT_TIME_OFFSET);
				}
				repeatCount++;

			} while (repeat && repeatCount <= DEFAULT_MAX_REQUESTS_REPEATS);

			if (!result.isOk()) {
				throw new ConnectorException(result.getErrorMessage());
			}

			return result.getResponse();

		} catch (Throwable t) {
			log.error("[FreeIPA connector] Error while posting request");
			throw new ConnectorException(t);
		}

	}

	private void initAuthenticatorAndAuthenticate(HttpClient wc) {
		FreeIPAAuthenticator authenticator = new FreeIPAPasswordAuthenticator(config.getUrl(),
			config.getUsername(), config.getPassword());
		authenticator.authenticate(wc);
	}

	private ResponseResult handleResponse(HttpResponse r) {
		final String responseString = r.getPayload();

		if (debug) {
			log.info("[FreeIPA connector] RESPONSE (HttpStatusCode - {1}) {0}", responseString, r.getStatus());
		}

		// Switch between http response codes (1xx,2xx or 3xx -> SUCCESS)
		switch (r.getGeneralState()) {
			case SUCCESS:
				// success should always return json response
				final JSONResponse<E> response = parse(responseString);
				return handleJsonResponse(response);
			case FAILURE:
				// error can return non-json response (html error page), hence we will address it separately
				return handleFailure(r);
			case UNKNOWN:
			default:
				throw new IllegalStateException("Unknown state");
		}

	}

	/**
	 * Some additional actions on successful json response can be performed here
	 *
	 * @param response Parsed json response
	 * @return
	 */
	private ResponseResult handleJsonResponse(JSONResponse<E> response) {
		if (!response.hasErrors()) {
			// If everything was ok
			return new ResponseResult(response);
		}
		// Here we have to switch according to FreeIPA error response codes
		final String ipaErrorCode = response.getError() == null ? "" : response.getError().getCode();

		switch (ipaErrorCode) {
			// TODO: define special behavior for different error codes
			default:
				return new ResponseResult(response, false, false);
		}
	}

	/**
	 * In case of not successful request, this method is responsible for response handling. First, it tries to extract json
	 * response. If it cannot be extracted, then http response codes are evaluated as fallback
	 *
	 * @param response
	 * @return
	 */
	private ResponseResult handleFailure(HttpResponse response) {

		try {
			final JSONResponse<E> jsonResponse = parse(response.getPayload());
			return handleJsonResponse(jsonResponse);
		} catch (Exception ex) {
			log.warn("Cannot parse json response... falling back to HTTP response codes");
			return handleFailureUsingHttpResponseCode(response);
		}

	}

	/**
	 * Fallback for when connector cannot parse json response.
	 *
	 * @param response
	 * @return
	 */
	private ResponseResult handleFailureUsingHttpResponseCode(HttpResponse response) {
		final String errMessage = "[FreeIPA connector] Http error " + response.getStatus() + " while communicating with FreeIPA endpoint";
		switch (response.getStatus()) {
			case HttpStatus.SC_UNAUTHORIZED:
				log.info("[FreeIPA connector] Invalidating cookie due to unauthorized response from IPA...");
				FreeIPASessionCookie.resetCookie();
				return new ResponseResult(new ErrorResponseResult<E>("Unauthorized", errMessage, "Unauthorized"),
					true, false);
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				log.info("[FreeIPA connector] Invalidating cookie due to Internal Server Error...");
				FreeIPASessionCookie.resetCookie();
				return new ResponseResult(new ErrorResponseResult<E>("Internal Server Error", errMessage, "Internal Server Error"),
					true, false);
			// TODO handle some more http errors?
			default:
				log.info("[FreeIPA connector] Invalidating cookie due to Unknown Error...");
				FreeIPASessionCookie.resetCookie();
				return new ResponseResult(new ErrorResponseResult<E>("Unknown error", errMessage, "Unknown error"),
					false, false);
		}
	}

	private String prepareRequest(HttpClient wc, HttpClient cl) {
		if (cl == null) {
			throw new ConnectorException(
				"[FreeIPA connector] Can not obtain subPath "
					+ getSubPath() + " from url " + wc.getBaseURI());
		}

		final Request req = new Request(getId(), getMethod(),
			getPositionalParams(), getNamedParams());

		// Create JSON representation
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder
			.serializeNulls()
			.create();
		String json = gson.toJson(req);

		if (debug) {
			log.info("[FreeIPA connector] REQUEST {0}", json);
		}
		return json;
	}

	/**
	 * Returns value of id parameter of FreeIPA request. There is probably no reason to
	 * change this implementation and return different value than "0".
	 *
	 * @return Value of id attribute of FreeIPA JSON request
	 */
	protected String getId() {
		return "0";
	}

	/**
	 * By default FreeIPA has its web api published on path /session/json.
	 *
	 * @return relative path to end point of FreeIPA JSON service
	 */
	protected String getSubPath() {
		return "/session/json";
	}

	/**
	 * @return Name of the method to call
	 * @see For available methods go see FreeIPA documentation. You can find
	 * it in IPA Web UI under IPA Server -> API Browser
	 */
	protected abstract String getMethod();

	/**
	 * Each JSON request has this structure:
	 * <p>
	 * { "id": 0, "method": "METHOD_NAME", "params": [ [ POSITIONAL_PARAMS ], {NAMED_PARAMS} ] }
	 * <p>
	 * this method returns value of POSITIONAL_PARAMS in form of a {@link String} array
	 *
	 * @return Positional parameters for concrete method
	 */
	protected abstract String[] getPositionalParams();

	/**
	 * Each JSON request has this structure:
	 * <p>
	 * { "id": 0, "method": "METHOD_NAME", "params": [ [ POSITIONAL_PARAMS ], {NAMED_PARAMS} ] }
	 * <p>
	 * this method returns value of NAMED_PARAMS in form of a {@link Map}, where key is param name
	 * as String and value is {@link Object}
	 * <p>
	 * It uses template method pattern and calls {@link #fillNamedParams(Map)} to add parameters specific to concrete
	 * post request. Some common named attributes can also be added here, such as version.
	 *
	 * @return Named parameters for concrete method
	 */
	protected final Map<String, Object> getNamedParams() {
		final Map<String, Object> result = new HashMap<>();
		fillNamedParams(result);
		// TODO: maybe configurable version??
		result.put("version", "2.156");
		return result;
	}

	protected abstract JSONResponse<E> parse(String responseString);

	/**
	 * This method is called by getNamedParams method and its purpose is to fill
	 * {@link Map}of named params with request-specific parameters.
	 *
	 * @param result
	 * @see #getNamedParams()
	 */
	protected abstract void fillNamedParams(final Map<String, Object> result);

	/**
	 * This class is used to encapsulate FreeIPA request information and is then
	 * used for generating JSON
	 */
	public final class Request {

		public final String id;

		public final String method;

		public final Object[] params;

		public Request(String id, String method, String[] positionalParams,
		               Map<String, Object> namedParams) {
			this.id = id;
			this.method = method;
			this.params = new Object[]{positionalParams, namedParams};
		}

	} // END OF class Request

	/**
	 * This class encapsulates parsed json response (if it could have been parsed) along with information if
	 * the request should be repeated.
	 */
	private class ResponseResult {

		private final JSONResponse<E> response;
		private final boolean requestRepeatNeeded;
		private final boolean ok;


		private ResponseResult(JSONResponse<E> response, boolean requestRepeatNeeded, boolean ok) {
			this.response = response;
			this.requestRepeatNeeded = requestRepeatNeeded;
			this.ok = ok;
		}

		public ResponseResult(JSONResponse<E> response) {
			this.response = response;
			this.requestRepeatNeeded = false;
			this.ok = true;
		}

		public boolean isRequestRepeatNeeded() {
			return requestRepeatNeeded;
		}

		public JSONResponse<E> getResponse() {
			return response;
		}

		public boolean isOk() {
			return ok;
		}

		public String getErrorMessage() {
			if (response != null && response.hasErrors()) {
				return response.getError().getMessage();
			}
			return null;
		}
	} // END OF class ResponseResult

} // END OF class ServicePost
