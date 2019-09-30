package de.uni_passau.visit.compression.models;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.network.Server;

/**
 * The root model is the main class of the compression system and therefore
 * contains also the main-method. Its only functionality is to set up the
 * remaining models and the server and establish their interconnection.
 * 
 * @author Florian Schlenker
 *
 */
public class RootModel {

	private final ConfigModel configModel;
	private final QueueModel queueModel;
	private final CompressionModel compressionModel;
	private final ControlModel controlModel;
	private Server server;

	private static final Logger log = LogManager.getLogger(RootModel.class);
	//public static final String DATA_ROOT = "/root/compression";
	public static final String DATA_ROOT = "/home/schlenke/projects/visit-compression/compressioncontainer/java/";

	/**
	 * This private constructor creates a new root model instance.
	 */
	private RootModel() {
		configModel = new ConfigModel();
		queueModel = new QueueModel(configModel);
		compressionModel = new CompressionModel(queueModel, configModel);

		try {
			this.server = new Server(this);
			this.server.start(configModel.getApiPort());
		} catch (IOException e) {
			log.fatal("Could not startup web server: " + e.getMessage() + ". Server shut down.");
			System.exit(0);
		}

		controlModel = new ControlModel(configModel, compressionModel, queueModel, server);

		log.info("Server startup finished.");

		compressionModel.start();
	}

	/**
	 * This method returns the queue model created by this root model.
	 * 
	 * @return Returns the queue model created by this root model
	 */
	public QueueModel getQueueModel() {
		return queueModel;
	}

	/**
	 * This model returns the configuration model created by this root model.
	 * 
	 * @return Returns the configuration model created by this root model
	 */
	public ConfigModel getConfigModel() {
		return configModel;
	}

	/**
	 * This model returns the control model created by this root model.
	 * 
	 * @return Returns the control model created by this control model
	 */
	public ControlModel getControlModel() {
		return controlModel;
	}

	/**
	 * This method returns the server component created by this root model.
	 * 
	 * @return Returns the server component created by this root model
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * This method is the main method for the compression system and can be used to
	 * start up the whole compression component.
	 * 
	 * @param args
	 *            Since no command line options are available this parameter doesn't
	 *            matter
	 */
	public static void main(String[] args) {
		new RootModel();
	}

}
