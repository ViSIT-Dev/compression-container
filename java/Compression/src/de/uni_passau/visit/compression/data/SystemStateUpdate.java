package de.uni_passau.visit.compression.data;

/**
 * This enumeration represents a state update of the compression system.
 * 
 * - RUN: Start compression job queue procession
 * - PAUSE: Pause compression job queue compression
 * - SHUTDOWN_PROCESS_QUEUE: Shut down compression system and reject all incoming jobs, but process the remaining queue
 * - SHUTDOWN_IMMEDIATELY: Shut down compression system, reject all incoming jobs, and finish only the job that is being processed at the moment.
 * - KILL: Shut down compression system immediately and abort the current procession without any consistency guarantees.   
 * 
 * @author Florian Schlenker
 *
 */
public enum SystemStateUpdate {
	RUN, PAUSE, SHUTDOWN_PROCESS_QUEUE, SHUTDOWN_IMMEDIATELY, KILL
}
