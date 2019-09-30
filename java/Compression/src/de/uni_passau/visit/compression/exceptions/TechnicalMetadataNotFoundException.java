package de.uni_passau.visit.compression.exceptions;

/**
 * This exception shall be thrown if it was impossible to fetch technical meta
 * data from the meta database, because no data regarding the specified entity
 * could be found.
 * 
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadataNotFoundException extends Exception {

	private static final long serialVersionUID = 3264153341018110047L;

	/**
	 * This constructor creates an instance of the exception without any further
	 * information.
	 */
	public TechnicalMetadataNotFoundException() {
		super();
	}

	/**
	 * This constructor creates an instance of the exception with the given message.
	 * 
	 * @param message
	 *            A message describing the reason for the error
	 */
	public TechnicalMetadataNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * This constructor creates an instance of the exception with a reference to
	 * another exception causing this exception.
	 * 
	 * @param causedBy
	 *            The exception causing this exception
	 */
	public TechnicalMetadataNotFoundException(Throwable causedBy) {
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
	public TechnicalMetadataNotFoundException(String msg, Throwable causedBy) {
		super(msg, causedBy);
	}

}
