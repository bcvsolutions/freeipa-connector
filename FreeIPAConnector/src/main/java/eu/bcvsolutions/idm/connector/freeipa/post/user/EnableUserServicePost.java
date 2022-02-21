package eu.bcvsolutions.idm.connector.freeipa.post.user;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAConfiguration;
import eu.bcvsolutions.idm.connector.freeipa.service.response.GenericJSONResponse;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;

public class EnableUserServicePost extends UserServicePost<Map<String, Object>> {

	public EnableUserServicePost(String login, boolean debug, FreeIPAConfiguration config) {
		super(login, new HashMap<String, Object>(), debug, config);
	}

	@Override
	protected String getMethod() {
		return "user_enable";
	}

	@Override
	protected JSONResponse<Map<String, Object>> parse(String responseString) {
		Gson gson = new Gson();
		return gson.fromJson(responseString, GenericJSONResponse.class);
	}

}
