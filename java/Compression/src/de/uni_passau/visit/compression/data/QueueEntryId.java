package de.uni_passau.visit.compression.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the id of a queue entry in the server's compression job
 * queue and contains only one read-only integer value.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization and contains Jackson-annotations for the conversion of
 * JSON-strings to instances of this class.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueEntryId implements Serializable {

	private static final long serialVersionUID = -8316037859094391953L;

	private final int id;

	/**
	 * This constructor initializes the object with the given id argument.
	 * 
	 * @param id
	 *            The integer queue entry id that shall be represented by this
	 *            object
	 */
	@JsonCreator
	public QueueEntryId(@JsonProperty("id") int id) {
		super();
		this.id = id;
	}

	/**
	 * This method returns the queue entry id that is represented by this object.
	 * 
	 * @return Returns the integer queue entry id
	 */
	public int getId() {
		return id;
	}
}
