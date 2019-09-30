package de.uni_passau.visit.compression.exceptions;

/**
 * This exception should be thrown after the try of an illegal modification of the compression components state by a user. 
 * 
 * @author Florian Schlenker
 *
 */
public class IllegalStateModificationException extends Exception {

	private static final long serialVersionUID = -8018463603588037881L;
	
	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public IllegalStateModificationException() {
		super();
	}
	
	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message A message describing the reason for the error
	 */
	public IllegalStateModificationException(String message) {
		super(message);
	}
	
	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public IllegalStateModificationException(Throwable causedBy) {
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
	public IllegalStateModificationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
