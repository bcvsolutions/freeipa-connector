package eu.bcvsolutions.freeipa.service.response.result;

import java.util.Map;

public class FindResponseResult {

	private int count;

	private Map<String, Object>[] result;

	private String summary;

	private boolean truncated;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Map<String, Object>[] getResult() {
		return result;
	}

	public void setResult(Map<String, Object>[] result) {
		this.result = result;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean isTruncated() {
		return truncated;
	}

	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}

}
