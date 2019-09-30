package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.visit.compression.data.ConfigTransfer;
import de.uni_passau.visit.compression.exceptions.InvalidConfigurationException;
import de.uni_passau.visit.compression.models.RootModel;
import de.uni_passau.visit.compression.network.transaction.ConfigResponse;
import de.uni_passau.visit.compression.network.transaction.DefaultResponse;

/**
 * This class represents a handler for API-requests regarding the settings /
 * configuration module. This module allows the update and retrieval of the
 * compression system's configuration.
 * 
 * @author Florian Schlenker
 *
 */
public class SettingsHandler implements AbstractHandler {

	private final RootModel root;

	public SettingsHandler(RootModel root) {
		this.root = root;
	}

	/**
	 * This constructor creates a new handler for API-requests regarding the
	 * settings / configuration module.
	 * 
	 * @param root
	 *            The root model that shall be used for processing of the request.
	 */
	public DefaultResponse handleAction(String method, List<String> function, String postData)
			throws JsonParseException, JsonMappingException, IOException {
		if (function.size() == 1 && function.get(0).equals("config")) {
			switch (method) {
			case "GET":
				return handleConfigQuery(postData);
			case "PUT":
				return handleConfigUpdate(postData);
			default:
				return new DefaultResponse(false, "Invalid method.");
			}
		} else {
			return new DefaultResponse(false, "Could not find specified api endpoint.");
		}
	}

	private DefaultResponse handleConfigQuery(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		return new ConfigResponse(root.getConfigModel().getTransferObject());
	}

	private DefaultResponse handleConfigUpdate(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		ConfigTransfer update = new ObjectMapper().readValue(postData, ConfigTransfer.class);
		try {
			root.getConfigModel().updateConfiguration(update);
			return new DefaultResponse(true, "Configuration updated.");
		} catch (InvalidConfigurationException ex) {
			return new DefaultResponse(false, "Transmitted configuration data is invalid.");
		}

	}

}
