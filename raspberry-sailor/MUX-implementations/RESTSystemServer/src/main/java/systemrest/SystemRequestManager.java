package systemrest;

import http.HTTPServer;
import http.RESTRequestManager;

import java.util.List;

public class SystemRequestManager implements RESTRequestManager {

	private final boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private final RESTImplementation restImplementation;

	private final SystemServer systemServer;

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public SystemRequestManager(SystemServer parent) {
		this.systemServer = parent;
		restImplementation = new RESTImplementation(this);
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return as defined in the {@link RESTImplementation}
	 * @throws UnsupportedOperationException Oops.
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
		if (this.httpVerbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	@Override
	public List<HTTPServer.Operation> getRESTOperationList() {
		return restImplementation.getOperations();
	}

	/*
	 Specific operations
	 */

	protected List<HTTPServer.Operation> getAllOperationList() {
		return systemServer.getAllOperationList();
	}

}
