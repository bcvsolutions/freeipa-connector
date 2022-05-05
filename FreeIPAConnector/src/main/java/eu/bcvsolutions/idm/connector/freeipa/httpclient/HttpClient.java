package eu.bcvsolutions.idm.connector.freeipa.httpclient;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpClient {

	protected final String baseUri;

	protected Map<String, String> headers = new HashMap<>();
	protected Map<String, String> cookies = new HashMap<>();


	public HttpClient(String baseUri) {
		this.baseUri = baseUri;
	}

	public static HttpClient create(String string) {
		// Factory would be nicer, but maybe overkill
		return new ApacheHtttpClientAdapter(string);
	}

	public abstract void header(String string, String string2);

	public abstract HttpResponse post(String authPayloadAsString);

	public abstract void addCookie(String ipaSessionCookieName,
			String sessionCookie);

	public HttpClient path(String string) {
		return new ApacheHtttpClientAdapter(getBaseURI() + string, this);
	}

	public String getBaseURI() {
		return this.baseUri;
	}

}
