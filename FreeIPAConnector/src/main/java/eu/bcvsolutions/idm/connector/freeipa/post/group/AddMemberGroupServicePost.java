package eu.bcvsolutions.idm.connector.freeipa.post.group;

import java.util.Map;

import com.google.gson.Gson;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAConfiguration;
import eu.bcvsolutions.idm.connector.freeipa.service.response.GenericJSONResponse;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;

public class AddMemberGroupServicePost extends
		GroupServicePost<Map<String, Object>> {

	public AddMemberGroupServicePost(String groupName,
			Map<String, Object> params, boolean debug, FreeIPAConfiguration config) {
		super(groupName, params, debug, config);
	}

	@Override
	protected String getMethod() {
		return "group_add_member";
	}

	@Override
	protected JSONResponse<Map<String, Object>> parse(String responseString) {
		Gson gson = new Gson();
		return gson.fromJson(responseString, GenericJSONResponse.class);
	}

}
