package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown when an error occurrs during the read process
 * of a 3D-model.
 * 
 * @author Florian Schlenker
 *
 */
public class ModelReadException extends Exception {

	private static final long serialVersionUID = -2150357656634592851L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public ModelReadException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public ModelReadException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public ModelReadException(Throwable causedBy) {
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
	public ModelReadException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}