package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

import de.uni_passau.visit.compression.models.RootModel;

/**
 * This class represents the server component of the compression system, which
 * is responsible for the whole network communication. Depending on the type of
 * incoming requests (api calls vs. static resources) the treatment of these
 * requests will be delegated to different handlers ( @see RestHandler, @see
 * ResourceHandler ).
 *
 * @author Kris Raich, Florian Schlenkers
 */
public class Server {

	private static final Logger log = LogManager.getLogger(RootModel.class);

	private final RootModel root;
	private HttpServer server;

	/**
	 * This constructor creates a new server instance, which won't be started
	 * automatically however.
	 * 
	 * @param root
	 *            The root model that shall be used as base for the server instance
	 */
	public Server(RootModel root) {
		this.root = root;
	}

	/**
	 * When calling this method the server will be started asynchronously.
	 * 
	 * @param port
	 *            The port on which the server shall listen for requests
	 * @throws IOException
	 *             when there occurs an error during the server startup
	 */
	public void start(final int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new ResourceHandler());
		server.createContext("/api/", new RestHandler(root));
		server.setExecutor(null); // creates a default executor
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				log.info("Web server shutting down.");
				server.stop(0);
			} catch (Throwable e) {
				log.error("Error during web server shutdown: " + e.getMessage());
			}
		}));

		log.info("Web server startup successful.");
	}

	/**
	 * This method stops the server in a synchronous way. All requests currently in
	 * procession will be finished if this happens within five seconds. The method
	 * returns after at most that five seconds.
	 */
	public void stop() {
		if (server != null) {
			server.stop(5);
		}
	}
}
