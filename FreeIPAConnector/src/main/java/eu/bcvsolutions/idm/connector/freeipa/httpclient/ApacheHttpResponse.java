package eu.bcvsolutions.idm.connector.freeipa.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ApacheHttpResponse extends HttpResponse {

	private final org.apache.http.HttpResponse response;
	private String payLoad = null;

	public ApacheHttpResponse(org.apache.http.HttpResponse res) {
		response = res;
	}

	@Override
	public List<String> getSetCookieHeaders() {

		List<String> result = new ArrayList<String>();

		for (Header header : response.getAllHeaders()) {
			if ("Set-Cookie".equalsIgnoreCase(header.getName())) {
				result.add(header.getValue());
			}
		}
		System.out.println(result);
		return result;
	}

	@Override
	public String getPayload() {
		if (payLoad == null) {
			StringBuffer result = new StringBuffer();
			String line = "";
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
			} catch (IOException e) {
				throw new ConnectorException(e);
			}
			payLoad = result.toString();
		}
		return payLoad;
	}

	@Override
	public int getStatus() {
		if (response == null) {
			return -1;
		}
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public GeneralState getGeneralState() {

		if (getStatus() < 0) {
			return GeneralState.UNKNOWN;
		}
		int firstDigit = Integer.valueOf(String.valueOf(getStatus()).substring(0, 1));

		if (firstDigit == 1 || firstDigit == 2) {
			return GeneralState.SUCCESS;
		} else {
			return GeneralState.FAILURE;
		}

	}

}
