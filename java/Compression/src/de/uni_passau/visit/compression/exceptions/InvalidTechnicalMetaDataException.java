package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown after the occurrence of an invalid technical
 * meta data object or JSON-representation. This usually happens when converting
 * technical meta data between object- and JSON-representation.
 * 
 * @author Florian Schlenker
 *
 */
public class InvalidTechnicalMetaDataException extends Exception {

	private static final long serialVersionUID = 2687999322998347468L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public InvalidTechnicalMetaDataException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public InvalidTechnicalMetaDataException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public InvalidTechnicalMetaDataException(Throwable causedBy) {
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
	public InvalidTechnicalMetaDataException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}
