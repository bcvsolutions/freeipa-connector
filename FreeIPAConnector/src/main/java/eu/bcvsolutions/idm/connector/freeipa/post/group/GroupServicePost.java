package eu.bcvsolutions.idm.connector.freeipa.post.group;

import java.util.Map;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAConfiguration;
import eu.bcvsolutions.idm.connector.freeipa.post.ServicePost;

public abstract class GroupServicePost<E> extends ServicePost<E> {

	private final String groupName;
	private final Map<String, Object> params;

	public GroupServicePost(String groupName, Map<String, Object> params,
	                        boolean debug, FreeIPAConfiguration config) {
		super(debug, config);
		this.groupName = groupName;
		this.params = params;
	}

	@Override
	protected String[] getPositionalParams() {
		return new String[] { groupName };
	}

	@Override
	protected void fillNamedParams(Map<String, Object> result) {
		result.putAll(params);
	}

}
