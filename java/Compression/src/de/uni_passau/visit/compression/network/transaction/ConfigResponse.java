package de.uni_passau.visit.compression.network.transaction;

import de.uni_passau.visit.compression.data.ConfigTransfer;

/**
 * This class extends the @see DefaultResponse class and represents a network
 * response on a configuration retrieval request and therefore holds an object
 * describing the compression system's configuration additionally to the success
 * flag and the message defined by the base class. The latter two will be set to
 * reasonable values automatically by this class' constructor.
 * 
 * @author Florian Schlenker
 * 
 */
public class ConfigResponse extends DefaultResponse {

	private final ConfigTransfer config;

	/**
	 * This constructor creates a new configuration response as a successful
	 * reaction on a configuration retrieval request by a client.
	 * 
	 * @param config
	 *            The current configuration of the compression system that shall be
	 *            carried to the client
	 */
	public ConfigResponse(ConfigTransfer config) {
		super(true, "Configuration retrieval successful.");
		this.config = config;
	}

	/**
	 * This method returns the configuration of the compression system that is
	 * carried by this response.
	 * 
	 * @return Returns the configuration of the compression system
	 */
	public ConfigTransfer getConfig() {
		return config;
	}

}
