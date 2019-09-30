package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.visit.compression.data.CompressionJob;
import de.uni_passau.visit.compression.data.QueueEntryId;
import de.uni_passau.visit.compression.exceptions.QueueException;
import de.uni_passau.visit.compression.models.RootModel;
import de.uni_passau.visit.compression.network.transaction.DefaultResponse;
import de.uni_passau.visit.compression.network.transaction.QueueItemResponse;

/**
 * This class represents a handler for API-requests regarding the job module.
 * This module allows dispatching and retrieval of compression jobs and job
 * cancellation.
 * 
 * @author Florian Schlenker
 *
 */
public class JobHandler implements AbstractHandler {
	private final RootModel root;

	/**
	 * This constructor creates a new handler for API-requests regarding the job
	 * module.
	 * 
	 * @param root
	 *            The root model that shall be used for procession of the request.
	 */
	public JobHandler(RootModel root) {
		this.root = root;
	}

	public DefaultResponse handleAction(String method, List<String> function, String postData)
			throws JsonParseException, JsonMappingException, IOException {
		if (function.size() > 0) {
			switch (function.get(0)) {
			case "dispatch":
				if (method.equals("POST")) {
					return handleJobDispatch(postData);
				} else {
					return new DefaultResponse(false, "Invalid method");
				}
			case "queue":
				if (method.equals("GET")) {
					return handleJobQueue(postData);
				} else {
					return new DefaultResponse(false, "Invalid method");
				}
			case "cancel":
				if (method.equals("DELETE")) {
					return handleJobCancel(function);
				} else {
					return new DefaultResponse(false, "Invalid method");
				}
			default:
				return new DefaultResponse(false, "Invalid action");
			}
		} else {
			return new DefaultResponse(false, "Could not find specified api endpoint.");
		}
	}

	private DefaultResponse handleJobDispatch(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		CompressionJob job = new ObjectMapper().readValue(postData, CompressionJob.class);
		try {
			root.getQueueModel().dispatchJob(job);
		} catch (QueueException e) {
			return new DefaultResponse(false, e.getMessage());
		}

		return new DefaultResponse(true, "Job dispatched.");
	}

	private DefaultResponse handleJobQueue(String postData)
			throws JsonParseException, JsonMappingException, IOException {
		return new QueueItemResponse(root.getQueueModel().getAllUnfinishedJobs());
	}

	private DefaultResponse handleJobCancel(List<String> function)
			throws JsonParseException, JsonMappingException, IOException {
		QueueEntryId deletionEntry = null;
		if (function.size() > 1) {
			try {
				deletionEntry = new QueueEntryId(Integer.parseInt(function.get(1)));
				root.getQueueModel().cancelJob(deletionEntry.getId());
				return new DefaultResponse(true, "Job cancelled.");
			} catch (NumberFormatException ex) {
				return new DefaultResponse(false, "Non-integer id specified for deletion.");
			}
		} else {
			return new DefaultResponse(false, "No id specified for deletion.");
		}
	}
}
