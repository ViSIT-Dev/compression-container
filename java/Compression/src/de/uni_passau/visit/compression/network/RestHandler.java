package de.uni_passau.visit.compression.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.uni_passau.visit.compression.models.RootModel;
import de.uni_passau.visit.compression.network.transaction.DefaultResponse;

/**
 * This class can be used to handle and answer incoming API-requests. Depending
 * on the module the request is addressing (determined by the first section of
 * the addressed path) the request will be dispatched to a corresponding
 * handler. For handling static web resource requests use @see ResourceHandler
 * instead.
 *
 * @author Kris Raich, Florian Schlenker
 */
public class RestHandler implements HttpHandler {

	private final RootModel root;

	private static final Logger log = LogManager.getLogger(RootModel.class);

	/**
	 * This constructor creates a new rest handler based on the given root model.
	 * 
	 * @param root
	 *            The root model used as base for the new rest handler
	 */
	public RestHandler(final RootModel root) {
		this.root = root;
	}

	@Override
	public void handle(HttpExchange he) {
		try {
			// OutputStream os = he.getResponseBody();
			List<String> whitelist = Arrays.asList(root.getConfigModel().getApiAccessWhitelist());

			if (whitelist.contains(he.getRemoteAddress().getAddress().toString()) || whitelist.contains("*")) {
				// read post data
				InputStream input = he.getRequestBody();
				StringBuilder putPostDataBuilder = new StringBuilder();
				new BufferedReader(new InputStreamReader(input)).lines()
						.forEach((String s) -> putPostDataBuilder.append(s + "\n"));

				String putPostData = putPostDataBuilder.toString();

				// get context data
				List<String> function = new ArrayList<String>(Arrays.asList(he.getRequestURI().toString().split("/")));

				if (function.size() > 1) {
					function.remove(0);
					function.remove(0);
				}

				String method = he.getRequestMethod().toUpperCase();

				// call action specific handler
				DefaultResponse response = handleAction(method, function, putPostData);

				// translate response to json
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(response);
				he.getResponseHeaders().set("Content-Type", "application/json");
				he.getResponseHeaders().set("Connection", "close");
				he.sendResponseHeaders(200, json.getBytes().length);
				he.getResponseBody().write(json.getBytes());
				he.getResponseBody().flush();

				/*
				 * the following code could fix encoding problems, but doesn't seem to be
				 * necessary
				 */
				/*
				 * final String encoding = "UTF-8"; he.getResponseHeaders().set("Content-Type",
				 * "application/json; charset=" + encoding); ByteOutputStream byteStream = new
				 * ByteOutputStream(1); Writer streamWriter = new OutputStreamWriter(byteStream,
				 * encoding); streamWriter.write(json); streamWriter.flush(); byte[] bytes =
				 * byteStream.getBytes(); he.sendResponseHeaders(200, bytes.length);
				 * he.getResponseBody().write(bytes); streamWriter.close();
				 */
			} else {
				he.sendResponseHeaders(403, 0);
				log.info("Access from host " + he.getRemoteAddress().getAddress().toString() + " has been denied.");
			}

			// os.flush();
			// os.close();
		} catch (IOException ex) {
			// assume web server to be shut down
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private DefaultResponse handleAction(String method, List<String> function, String postData) {
		if (function.size() > 0) {
			String module = function.get(0);
			function.remove(0);
			try {
				AbstractHandler requestHandler;

				switch (module) {
				case "jobs":
					requestHandler = new JobHandler(root);
					break;
				case "control":
					requestHandler = new ControlHandler(root);
					break;
				case "archive":
					requestHandler = new ArchiveHandler(root);
					break;
				case "settings":
					requestHandler = new SettingsHandler(root);
					break;
				default:
					return new DefaultResponse(false, "Could not find specified api endpoint.");
				}

				return requestHandler.handleAction(method, function, postData);
			} catch (JsonParseException e) {
				return new DefaultResponse(false, "Could not parse JSON string.");
			} catch (JsonMappingException e) {
				return new DefaultResponse(false, "Could not map JSON string.");
			} catch (IOException e) {
				log.error("Error while handling api request: " + e.getMessage());
				return new DefaultResponse(false, "Error while reading input data.");
			}
		} else {
			return new DefaultResponse(false, "Api endpoint not specified.");
		}
	}

}
