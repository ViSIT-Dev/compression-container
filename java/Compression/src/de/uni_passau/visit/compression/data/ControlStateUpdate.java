package de.uni_passau.visit.compression.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class contains information on a state update of the compression system.
 * All properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization and contains Jackson-annotations for the conversion of
 * JSON-strings to instances of this class.
 * 
 * @author Florian Schlenker
 *
 */
public class ControlStateUpdate implements Serializable {

	private static final long serialVersionUID = 3744549249494645146L;

	private final SystemStateUpdate state;

	/**
	 * This constructor initializes the object's field with the given argument.
	 * 
	 * @param state
	 *            The state update that shall be applied on the compression system.
	 */
	@JsonCreator
	public ControlStateUpdate(@JsonProperty("state") SystemStateUpdate state) {
		super();
		this.state = state;
	}

	/**
	 * This method returns the state update that shall be applied on the compression
	 * system.
	 * 
	 * @return Returns the state update that shall be applied on the compression
	 *         system.
	 */
	public SystemStateUpdate getState() {
		return state;
	}

}
