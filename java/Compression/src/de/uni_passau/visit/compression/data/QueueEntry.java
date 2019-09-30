package de.uni_passau.visit.compression.data;

import java.io.Serializable;

/**
 * This class represents a specific entry in the queue of all unfinished
 * compression jobs and therefore particularly contains a reference to this
 * job's @see CompressionJob object. Further data stored along with the job in
 * this object are the job's current state, its id and timestamps of job receipt
 * and last state modification. All properties of this class are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization. Since @see QueueEntry instances are only created at server
 * side, no Jackson annotations for the conversion of json strings to this
 * class' instances are necessary.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueEntry implements Serializable {
	private static final long serialVersionUID = -3377312672235086144L;

	private final CompressionJob job;
	private final long receivedOn;
	private final int id;
	private final JobState state;
	private final long lastStateChange;

	/**
	 * This constructor initializes all fields of the object with the given
	 * arguments
	 * 
	 * @param job
	 *            The compression job that shall be referenced by this queue entry
	 *            item
	 * @param receivedOn
	 *            The UNIX-timestamp representing the moment of job receipt
	 * @param id
	 *            The unique id of this queue entry / its compression job
	 * @param state
	 *            The initial / current state of this queue entry's compression job
	 * @param lastStateChange
	 *            The UNIX-timestamp of the last state modification
	 */
	public QueueEntry(CompressionJob job, long receivedOn, int id, JobState state, long lastStateChange) {
		this.job = job;
		this.receivedOn = receivedOn;
		this.id = id;
		this.state = state;
		this.lastStateChange = lastStateChange;
	}

	/**
	 * This method returns a new instance of the current object. This new instance
	 * contains the given job state as current state and has a refreshed timestamp
	 * of the last state modification. All other properties are kept the same.
	 * 
	 * @param state
	 *            The desired job state of the current objeect's new instance.
	 * @return Returns a duplicate instance of the current object with modified
	 *         state and modified timestamp of last state change
	 */
	public QueueEntry getModifiedState(JobState state) {
		return new QueueEntry(this.job, this.receivedOn, this.id, state, System.currentTimeMillis());
	}

	/**
	 * This method returns the compression job attached to this queue entry.
	 * 
	 * @return Returns the compression job attached to this queue entry.
	 */
	public CompressionJob getJob() {
		return job;
	}

	/**
	 * This method returns the timestamp, at which this object's compression job has been received.
	 * 
	 * @return Returns the UNIX-timestamp of the receipt of this object's compression job. 
	 */
	public long getReceivedOn() {
		return receivedOn;
	}

	/**
	 * This method returns the id of this queue entry / its compression job.
	 * 
	 * @return Returns the integer id of this queue entry
	 */
	public int getId() {
		return id;
	}

	/**
	 * This method returns the current state of this queue entry's compression job.
	 * 
	 * @return Returns the current state of this queue entry's compression job.
	 */
	public JobState getState() {
		return state;
	}

	/**
	 * This method returns the timestamp of last state modification of this queue entry.
	 * 
	 * @return Returns the UNIX-timestamp of last state modification of this queue entry
	 */
	public long getLastStateChange() {
		return lastStateChange;
	}
}
