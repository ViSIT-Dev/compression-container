package de.uni_passau.visit.compression.network.transaction;

/**
 * This class represents a default network response sent by the compression
 * component server to a client and contains a success-flag as well as a message
 * that can be display to the user.
 * 
 * @author Florian Schlenker
 *
 */
public class DefaultResponse {

	private final boolean success;
	private final String message;

	/**
	 * This constructor creates a new default network response with the given values
	 * for the success-flag and the response's message.
	 * 
	 * @param success
	 *            True, if the current process has been successful, otherwise false
	 * @param message
	 *            A message that can be displayed to the user
	 */
	public DefaultResponse(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}

	/**
	 * This method returns true, if the current process has been successful,
	 * otherwise false.
	 * 
	 * @return True, if the current process has been successful, otherwise false.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * This method returns the message that can be displayed to the user contained
	 * in this default response.
	 * 
	 * @return Returns the message contained in this default response
	 */
	public String getMessage() {
		return message;
	}

}
