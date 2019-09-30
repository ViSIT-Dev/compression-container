package de.uni_passau.visit.compression.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This class can be used to handle and answer incoming resource requests. It
 * delivers all kinds of static content, in particular HTML-, CSS-, and
 * JS-files. API-requests however are handled by the @see RestHandler class.
 *
 * @author Kris Raich, Florian Schlenker
 */
public class ResourceHandler implements HttpHandler {

	private static final int RESPONSE_STATUS_OK = 200;
	private static final int RESPONSE_STATUS_NOT_FOUND = 404;
	private static final int RESPONSE_STATUS_FORBIDDEN_METHOD = 405;
	private static final String SUPPORTED_METHOD = "GET";
	private static final String RESOURCE_ROOT = "de/uni_passau/visit/compression/website";

	@Override
	public void handle(HttpExchange he) {
		try {
			if (he.getRequestMethod().toUpperCase().equals(SUPPORTED_METHOD)) {
				String reqURI = he.getRequestURI().toString();
				if ("/".equals(reqURI)) {
					reqURI = "/index.html";
				}

				int httpStatus;
				URL url = this.getClass().getClassLoader().getResource(RESOURCE_ROOT + reqURI);

				if (url == null) {
					url = this.getClass().getClassLoader().getResource(RESOURCE_ROOT + "/404.html");
					httpStatus = RESPONSE_STATUS_NOT_FOUND;
				} else {
					httpStatus = RESPONSE_STATUS_OK;
				}

				URLConnection urlCon = url.openConnection();
				InputStream is = urlCon.getInputStream();

				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[1024];

				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}

				he.sendResponseHeaders(httpStatus, buffer.size());
				OutputStream os = he.getResponseBody();
				is.close();
				os.write(buffer.toByteArray());
				os.close();
			} else {
				he.getResponseHeaders().set("Allow", SUPPORTED_METHOD);
				he.getResponseHeaders().set("Connection", "close");
				he.sendResponseHeaders(RESPONSE_STATUS_FORBIDDEN_METHOD, 0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
