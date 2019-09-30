package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown after the user tries to set an invalid
 * configuration for the compression component.
 * 
 * @author Florian Schlenker
 *
 */
public class InvalidConfigurationException extends Exception {

	private static final long serialVersionUID = 4861330354044948374L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public InvalidConfigurationException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public InvalidConfigurationException(String message) {
		super(message);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public InvalidConfigurationException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * This constructor creates an instance of the exception with the given message
	 * and a reference to another exception causing this exception.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public InvalidConfigurationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
