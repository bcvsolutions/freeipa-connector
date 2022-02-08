package eu.bcvsolutions.freeipa.connector.httpclient;

import java.util.List;

public abstract class HttpResponse {

	public abstract List<String> getSetCookieHeaders();

	public abstract String getPayload();

	public abstract int getStatus();

	public abstract GeneralState getGeneralState();

	public enum GeneralState{
		SUCCESS, FAILURE, UNKNOWN
	}

}
