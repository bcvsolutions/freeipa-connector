package eu.bcvsolutions.idm.connector.freeipa.service.response;

public class JSONResponseError {


	private String code;
	private String message;
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "[JSONResponseError code=" + code + ", message=" + message
				+ ", name=" + name + "]";
	}

}
