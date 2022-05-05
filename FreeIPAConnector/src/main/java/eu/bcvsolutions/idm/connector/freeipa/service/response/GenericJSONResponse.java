package eu.bcvsolutions.idm.connector.freeipa.service.response;

import java.util.Map;

public final class GenericJSONResponse extends
		JSONResponse<Map<String, Object>> {

	private Map<String, Object> result;

	@Override
	public Map<String, Object> getResult() {
		return result;
	}

	@Override
	public void setResult(Map<String, Object> result) {
		this.result = result;
	}

}
