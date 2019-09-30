package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.uni_passau.visit.compression.network.transaction.DefaultResponse;

/**
 * This interface represents an abstract handler for an API-request received by
 * a client via network.
 * 
 * @author Florian Schlenker
 *
 */
public interface AbstractHandler {

	/**
	 * This method will be called for a API-request received by a client via network
	 * that shall be answered by the class implementing this interface.
	 * 
	 * @param method
	 *            The HTTP-method that has been used for the API-request
	 * @param function
	 *            The collection of tokens used in the URL separated by slashes
	 *            describing the concrete function that shall be called (stripped of
	 *            the first token describing the module that has been addressed)
	 * @param putPostData
	 *            The data sent along with PUT- or POST-requests that is excepted to
	 *            be a valid JSON string.
	 * @return Returns the response that shall be sent to the client
	 * @throws JsonParseException
	 *             if the given PUT-/POST-data is no valid JSON-string
	 * @throws JsonMappingException
	 *             if the given PUT-/POST-data doesn't contain the expected data
	 * @throws IOException
	 *             if another error during the procession of the given data occurs
	 */
	public DefaultResponse handleAction(String method, List<String> function, String putPostData)
			throws JsonParseException, JsonMappingException, IOException;

}
