package eu.bcvsolutions.idm.connector.freeipa.post.user;

import java.util.Map;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAConfiguration;
import eu.bcvsolutions.idm.connector.freeipa.post.ServicePost;

public abstract class UserServicePost<E> extends ServicePost<E> {

	private final String login;
	private final Map<String, Object> params;

	public UserServicePost(String login, Map<String, Object> params,
			boolean debug, FreeIPAConfiguration config) {
		super(debug, config);
		this.login = login;
		this.params = params;
	}

	@Override
	protected String[] getPositionalParams() {
		return new String[] { login };
	}

	@Override
	protected void fillNamedParams(Map<String, Object> result) {
		result.putAll(params);
	}

}
