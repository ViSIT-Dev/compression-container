package de.uni_passau.visit.compression.exceptions;

/**
 * This runtime exception represents an error during an access of the job queue
 * database.
 * 
 * @author Florian Schlenker
 *
 */
public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 3091399470952154633L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public DatabaseException() {
		super();
	}
	
	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message A message describing the reason for the error
	 */
	public DatabaseException(String message) {
		super(message);
	}
	
	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public DatabaseException(Throwable causedBy) {
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
	public DatabaseException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
