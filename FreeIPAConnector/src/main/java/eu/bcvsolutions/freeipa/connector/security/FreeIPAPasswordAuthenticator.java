package eu.bcvsolutions.freeipa.connector.security;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;

import eu.bcvsolutions.freeipa.connector.httpclient.HttpClient;

public final class FreeIPAPasswordAuthenticator extends FreeIPAAuthenticator {

	@SuppressWarnings("unused")
	private static final Log log = Log
			.getLog(FreeIPAPasswordAuthenticator.class);

	final String endPoint;

	public FreeIPAPasswordAuthenticator(final String endpoint,
			final String login, final GuardedString password) {
		super(login, password);
		this.endPoint = endpoint;
	}

	@Override
	public void authenticate(HttpClient request) {

		FreeIPASessionCookie cookie = FreeIPASessionCookie.getInstace(endPoint, this.userName, this.pwd);
		
		request.addCookie(FreeIPASessionCookie.IPA_SESSION_COOKIE_NAME, cookie.getSessionCookie());
		request.header("referer", endPoint);
		request.header("content-type", "application/json");
		request.header("accept", "application/json");

	}
}
