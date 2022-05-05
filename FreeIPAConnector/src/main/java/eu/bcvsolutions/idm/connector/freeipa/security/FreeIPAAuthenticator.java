package eu.bcvsolutions.idm.connector.freeipa.security;

import org.identityconnectors.common.security.GuardedString;

import eu.bcvsolutions.idm.connector.freeipa.httpclient.HttpClient;

public abstract class FreeIPAAuthenticator {

	protected final String userName;
	protected final GuardedString pwd;

	public FreeIPAAuthenticator(final String login, final GuardedString password) {
		userName = login;
		pwd = password;
	}

	/**
	 * 
	 * Adds authentication info into request. In most cases it will only manage
	 * session cookies
	 * 
	 * @param wc
	 */
	public abstract void authenticate(HttpClient wc);

}
