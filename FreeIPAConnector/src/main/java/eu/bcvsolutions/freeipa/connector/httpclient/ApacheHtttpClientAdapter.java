package eu.bcvsolutions.freeipa.connector.httpclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ApacheHtttpClientAdapter extends HttpClient {

	private org.apache.http.client.HttpClient cl = HttpClientBuilder.create()
			.build();

	public ApacheHtttpClientAdapter(String uri) {
		super(uri);
	}

	public ApacheHtttpClientAdapter(String uri, HttpClient that) {
		super(uri);
		this.headers = that.headers;
		this.cookies = that.cookies;
	}

	@Override
	public void header(String string, String string2) {
		headers.put(string, string2);
	}

	@Override
	public HttpResponse post(String authPayloadAsString) {
		HttpPost post = new HttpPost(getBaseURI());

		try {
			post.setEntity(new ByteArrayEntity(authPayloadAsString.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			throw new ConnectorException(e1);
		}

		// Add headers
		for (String headerName : headers.keySet()) {
			post.addHeader(headerName, headers.get(headerName));
		}

		// add cookies
		for (String cookieName : cookies.keySet()) {
			post.addHeader("Cookie", cookieName + "=" + cookies.get(cookieName));
		}

		try {
			org.apache.http.HttpResponse res = cl.execute(post);
			return new ApacheHttpResponse(res);

		} catch (IOException e) {
			throw new ConnectorException(e);
		}
	}

	@Override
	public void addCookie(String ipaSessionCookieName, String sessionCookie) {
		cookies.put(ipaSessionCookieName, sessionCookie);
	}

}
