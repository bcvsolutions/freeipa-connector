package eu.bcvsolutions.freeipa.post.group;

import java.util.Map;

import com.google.gson.Gson;

import eu.bcvsolutions.freeipa.connector.FreeIPAConfiguration;
import eu.bcvsolutions.freeipa.connector.FreeIPAConstants;
import eu.bcvsolutions.freeipa.service.response.FindJSONResponse;
import eu.bcvsolutions.freeipa.service.response.JSONResponse;
import eu.bcvsolutions.freeipa.service.response.result.FindResponseResult;

public class FindGroupServicePost extends GroupServicePost<FindResponseResult> {
	
	/**
	 * If this is true, then only primary keys of records will be returned
	 */
	private final boolean onlyPkey;
	
	/**
	 * Time limits in seconds. If time to process query in FreeIPA is longer than this,
	 * "truncated" attribute in result will be set to true and only subset of records will
	 * be returned
	 */
	private final int timeLimit;
	
	/**
	 * If size of result set is bugger than this, "truncated" attribute in result 
	 * will be set to true and only subset of this size of records will be returned
	 */
	private final int sizeLimit;

	public FindGroupServicePost(final String groupName, final Map<String, Object> params,
			final boolean debug, final boolean onlyPkey, int sizeLimit, int timeLimit, FreeIPAConfiguration config) {
		super(groupName, params, debug, config);
		this.onlyPkey = onlyPkey;
		this.timeLimit = timeLimit;
		this.sizeLimit = sizeLimit;
	}
	
	public FindGroupServicePost(final String groupName, final Map<String, Object> params,
			final boolean debug, FreeIPAConfiguration config) {
		super(groupName, params, debug, config);
		this.onlyPkey = false;
		this.timeLimit = FreeIPAConstants.DEFAULT_TIME_LIMIT;
		this.sizeLimit = FreeIPAConstants.DEFAULT_SIZE_LIMIT;
	}

	@Override
	protected String getMethod() {
		return "group_find";
	}

	@Override
	protected JSONResponse<FindResponseResult> parse(String responseString) {
		Gson gson = new Gson();
		return gson.fromJson(responseString, FindJSONResponse.class);
	}

	@Override
	protected void fillNamedParams(Map<String, Object> result) {
		if (onlyPkey) {
			result.put("pkey_only", true);
		} else {
			result.put("all", true);
		}
		
		result.put(FreeIPAConstants.TIME_LIMIT_ATTRIBUTE, timeLimit);
		result.put(FreeIPAConstants.SIZE_LIMIT_ATTRIBUTE, sizeLimit);
		
		super.fillNamedParams(result);
	}
}
