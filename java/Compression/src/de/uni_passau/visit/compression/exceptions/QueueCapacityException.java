package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown if by receiving a compression job the maximum
 * queue length would be exceeded.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueCapacityException extends QueueException {

	private static final long serialVersionUID = 2288633945553737008L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public QueueCapacityException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public QueueCapacityException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public QueueCapacityException(Throwable causedBy) {
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
	public QueueCapacityException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}
}
