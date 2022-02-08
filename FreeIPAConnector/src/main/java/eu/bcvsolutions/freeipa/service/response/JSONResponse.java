package eu.bcvsolutions.freeipa.service.response;


/**
 * 
 * Just a class to encapsulate json response from FreeIPA
 */
public abstract class JSONResponse<E> {

	protected JSONResponseError error;

	protected String id;

	protected String principal;

	protected String version;

	public JSONResponseError getError() {
		return error;
	}

	public void setError(JSONResponseError error) {
		this.error = error;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public abstract E getResult();

	public abstract void setResult(E result);

	public boolean hasErrors() {
		return this.error != null;
	}

	@Override
	public String toString() {
		return "[JSONResponse error=" + error + ", result=" + getResult() + "]";
	}

}
