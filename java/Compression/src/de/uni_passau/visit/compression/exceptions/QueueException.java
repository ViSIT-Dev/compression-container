package de.uni_passau.visit.compression.exceptions;

/**
 * This exception represents an error during the receipt of a new compression
 * job.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueException extends Exception {

	private static final long serialVersionUID = 342982533819552174L;
	
	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public QueueException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public QueueException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public QueueException(Throwable causedBy) {
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
	public QueueException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}
