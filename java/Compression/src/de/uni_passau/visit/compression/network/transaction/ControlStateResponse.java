package de.uni_passau.visit.compression.network.transaction;

import de.uni_passau.visit.compression.data.SystemState;

/**
 * This class extends the @see DefaultResponse class and represents a network
 * response on a system state request and therefore holds an object describing
 * the compression system's current state additionally to the success flag and
 * the message defined by the base class. The latter two will be set to
 * reasonable values automatically by this class' constructor.
 * 
 * @author Florian Schlenker
 * 
 */
public class ControlStateResponse extends DefaultResponse {

	private final SystemState state;

	/**
	 * This constructor creates a new state response as a successful reaction on a
	 * state request by a client.
	 * 
	 * @param state
	 *            The current state of the compression system that shall be carried
	 *            to the client
	 */
	public ControlStateResponse(SystemState state) {
		super(true, "State retrieval successful.");
		this.state = state;
	}

	/**
	 * This method returns the current state of the compression system that is
	 * carried by this response.
	 * 
	 * @return Returns the current state of the compression system
	 */
	public SystemState getState() {
		return state;
	}

}
