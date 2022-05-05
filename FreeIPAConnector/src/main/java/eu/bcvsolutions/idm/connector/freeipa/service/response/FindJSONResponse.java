package eu.bcvsolutions.idm.connector.freeipa.service.response;

import eu.bcvsolutions.idm.connector.freeipa.service.response.result.FindResponseResult;

public class FindJSONResponse extends JSONResponse<FindResponseResult> {

	private FindResponseResult result;

	@Override
	public FindResponseResult getResult() {
		return result;
	}

	@Override
	public void setResult(FindResponseResult result) {
		this.result = result;
	}

}
