package eu.bcvsolutions.freeipa.post.user;

import java.util.Map;

import com.google.gson.Gson;

import eu.bcvsolutions.freeipa.connector.FreeIPAConfiguration;
import eu.bcvsolutions.freeipa.service.response.GenericJSONResponse;
import eu.bcvsolutions.freeipa.service.response.JSONResponse;

public class UpdateUserServicePost extends UserServicePost<Map<String, Object>> {

	public UpdateUserServicePost(String login, Map<String, Object> params,
			boolean debug, FreeIPAConfiguration config) {
		super(login, params, debug, config);
	}

	@Override
	protected String getMethod() {
		return "user_mod";
	}

	@Override
	protected JSONResponse<Map<String, Object>> parse(String responseString) {
		Gson gson = new Gson();
		return gson.fromJson(responseString, GenericJSONResponse.class);
	}

}
