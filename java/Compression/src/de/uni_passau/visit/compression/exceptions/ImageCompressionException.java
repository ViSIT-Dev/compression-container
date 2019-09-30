package de.uni_passau.visit.compression.exceptions;

/**
 * This exception should be thrown if an error while compression an image
 * occurs.
 * 
 * @author Florian Schlenker
 *
 */
public class ImageCompressionException extends Exception {

	private static final long serialVersionUID = -6228001463986662121L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public ImageCompressionException() {
		super();
	}
	
	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public ImageCompressionException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public ImageCompressionException(String message) {
		super(message);
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
	public ImageCompressionException(String message, Throwable causedBy) {
		super(message, causedBy);
	}
	
}
