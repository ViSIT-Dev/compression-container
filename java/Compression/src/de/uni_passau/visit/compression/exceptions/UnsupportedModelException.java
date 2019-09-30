package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown if an algorithm is restricted to models with
 * certain properties that are not satisfied for a given model fed to this
 * algorithm.
 * 
 * @author Florian Schlenker
 *
 */
public class UnsupportedModelException extends Exception {

	private static final long serialVersionUID = 1032007018704809354L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public UnsupportedModelException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public UnsupportedModelException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public UnsupportedModelException(Throwable causedBy) {
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
	public UnsupportedModelException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}
}
