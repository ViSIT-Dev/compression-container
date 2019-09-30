package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.visit.compression.data.ControlStateUpdate;
import de.uni_passau.visit.compression.exceptions.IllegalStateModificationException;
import de.uni_passau.visit.compression.models.RootModel;
import de.uni_passau.visit.compression.network.transaction.ControlStateResponse;
import de.uni_passau.visit.compression.network.transaction.DefaultResponse;

/**
 * This class represents a handler for API-requests regarding the control
 * module. This module allows the retrieval and the update of the compression
 * system's state.
 * 
 * @author Florian Schlenker
 *
 */
public class ControlHandler implements AbstractHandler {
	private final RootModel root;

	/**
	 * This constructor creates a new handler for API-requests regarding the control
	 * module.
	 * 
	 * @param root
	 *            The root model that shall be used for processing of the request.
	 */
	public ControlHandler(RootModel root) {
		this.root = root;
	}

	public DefaultResponse handleAction(String method, List<String> function, String postData)
			throws JsonParseException, JsonMappingException, IOException {
		if (function.size() > 0 && function.get(0).equals("state")) {
			switch (method) {
			case "GET":
				return handleStateQuery(postData);
			case "PUT":
				return handleStateUpdate(postData);
			default:
				return new DefaultResponse(false, "Invalid method.");
			}
		} else {
			return new DefaultResponse(false, "Could not find specified api endpoint.");
		}
	}

	private DefaultResponse handleStateQuery(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		return new ControlStateResponse(root.getControlModel().getCurrentSystemState());
	}

	private DefaultResponse handleStateUpdate(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		ControlStateUpdate update = new ObjectMapper().readValue(postData, ControlStateUpdate.class);
		try {
			switch (update.getState()) {
			case KILL:
				root.getControlModel().kill();
				break;
			case PAUSE:
				root.getControlModel().setPaused();
				break;
			case RUN:
				root.getControlModel().setRunning();
				break;
			case SHUTDOWN_IMMEDIATELY:
				root.getControlModel().shutdown(false);
				break;
			case SHUTDOWN_PROCESS_QUEUE:
				root.getControlModel().shutdown(true);
				break;
			}

			return new DefaultResponse(true, "System state updated to " + update.getState().toString());
		} catch (IllegalStateModificationException ex) {
			return new DefaultResponse(false, "System state update illegal. Current state is "
					+ root.getControlModel().getCurrentSystemState().toString());
		}

	}
}
