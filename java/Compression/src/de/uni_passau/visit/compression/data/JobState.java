package de.uni_passau.visit.compression.data;

/**
 * This enumeration represents the current state of a compression job. The
 * different options are 
 * 
 * - ENQUEUED (enqueued for processing), 
 * - PROCESSING (the job is currently in processing), 
 * - ERROR (the job has been finished, but errors occurred) and 
 * - COMPLETED (the job has been finished successfully and all 
 * desired compressed versions have been created).
 * 
 * @author Florian Schlenker
 *
 */
public enum JobState {
	ENQUEUED(0), PROCESSING(1), ERROR(2), COMPLETED(3);

	private final int value;

	private JobState(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
