package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown if a non-manifold model is used as input for an algorithm restricted to manifold models. 
 * 
 * @author Florian Schlenker
 *
 */
public class NonManifoldModelException extends UnsupportedModelException {

	private static final long serialVersionUID = 5243632563502379453L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public NonManifoldModelException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public NonManifoldModelException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public NonManifoldModelException(Throwable causedBy) {
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
	public NonManifoldModelException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}
