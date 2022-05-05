package eu.bcvsolutions.idm.connector.freeipa.post;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAConfiguration;
import eu.bcvsolutions.idm.connector.freeipa.service.response.GenericJSONResponse;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;

/**
 * 
 * This class creates ping request
 * 
 * { "id": 0, "method": "ping", "params": [ [], { "version": "2.156" } ] }
 *
 *
 */
public class PingServicePost extends ServicePost<Map<String, Object>> {

	public PingServicePost(boolean debug, FreeIPAConfiguration config) {
		super(debug, config);
	}

	@Override
	protected String getMethod() {
		return "ping";
	}

	@Override
	protected String[] getPositionalParams() {
		return new String[0];
	}

	@Override
	protected void fillNamedParams(Map<String, Object> result) {

	}

	@Override
	protected JSONResponse<Map<String, Object>> parse(String responseString) {
		Gson gson = new Gson();
		Type fooType = new TypeToken<GenericJSONResponse>() {
		}.getType();
		return gson.fromJson(responseString, fooType);
	}

} // END OF class PingServicePost
