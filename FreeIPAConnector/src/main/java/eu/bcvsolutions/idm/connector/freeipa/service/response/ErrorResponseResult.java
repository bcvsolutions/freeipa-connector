package eu.bcvsolutions.idm.connector.freeipa.service.response;

/**
 * This class serves only for when developer needs to create error json response without actually parsing the json file
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public class ErrorResponseResult<E> extends JSONResponse<E> {

	private final String conde,message,name;

	public ErrorResponseResult(String conde, String message, String name) {
		this.conde = conde;
		this.message = message;
		this.name = name;
	}

	@Override
	public E getResult() {
		return null;
	}

	@Override
	public void setResult(E result) {

	}

	@Override
	public boolean hasErrors() {
		return true;
	}

	@Override
	public JSONResponseError getError() {
		final JSONResponseError err = new JSONResponseError();
		err.setCode(conde);
		err.setMessage(message);
		err.setName(name);
		return err;
	}
}
