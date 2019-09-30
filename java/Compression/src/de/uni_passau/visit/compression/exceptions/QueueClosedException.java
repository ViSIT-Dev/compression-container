package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown if a compression job can't be received, since
 * the compression system is shutting down and thus the queue has been closed.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueClosedException extends QueueException {

	private static final long serialVersionUID = 2044744102210019422L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public QueueClosedException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public QueueClosedException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public QueueClosedException(Throwable causedBy) {
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
	public QueueClosedException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}
}
