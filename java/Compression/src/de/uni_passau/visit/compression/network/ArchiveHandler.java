package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.uni_passau.visit.compression.models.RootModel;
import de.uni_passau.visit.compression.network.transaction.DefaultResponse;
import de.uni_passau.visit.compression.network.transaction.QueueItemResponse;

/**
 * This class represents a handler for API-requests regarding the archive
 * module. This module allows the retrieval of the last processed jobs.
 * 
 * @author Florian Schlenker
 *
 */
public class ArchiveHandler implements AbstractHandler {

	private final RootModel root;

	/**
	 * This constructor creates a new handler for API-requests regarding the archive
	 * module.
	 * 
	 * @param root
	 *            The root model that shall be used for processing of the request.
	 */
	public ArchiveHandler(RootModel root) {
		this.root = root;
	}

	public DefaultResponse handleAction(String method, List<String> function, String postData)
			throws JsonParseException, JsonMappingException, IOException {
		if (function.size() > 0 && function.get(0).equals("jobs")) {
			if (method.equals("GET")) {
				return new QueueItemResponse(root.getQueueModel().getProcessedJobs(root.getConfigModel().getArchiveDisplayLength(), 0));
			} else {
				return new DefaultResponse(false, "Invalid method.");
			}
		} else {
			return new DefaultResponse(false, "Could not find specified api endpoint.");
		}
	}

}
