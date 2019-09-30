package de.uni_passau.visit.compression.exceptions;

/**
 * This exception should be thrown if an error while compression an 3D-model's
 * texture occurs.
 * 
 * @author Florian Schlenker
 *
 */
public class TextureCompressionException extends Exception {

	private static final long serialVersionUID = -7024249155145332321L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public TextureCompressionException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public TextureCompressionException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public TextureCompressionException(Throwable causedBy) {
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
	public TextureCompressionException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}
