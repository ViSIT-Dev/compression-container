package de.uni_passau.visit.compression.data;

/**
 * This enumeration represents a state of the compression system.
 * 
 * - STARTUP: The compression system is starting up and the queue procession
 * hasn't been started yet. 
 * - RUNNING: The compression system is running and the
 * queue is being processed. 
 * - PAUSED: The compression system is running, but
 * queue procession has been paused by the user. 
 * - SHUTTING DOWN: The
 * compression system is being shut down and no new queue entries will be
 * accepted any more. Possibly one or several enqueued compression jobs will be
 * processed before final shut down. 
 * - SHUTDOWN: The compression system has been
 * shut down. Queue processing has been stopped and network interfaces have been
 * closed.
 * 
 * @author Florian Schlenker
 *
 */
public enum SystemState {
	STARTUP, RUNNING, PAUSED, SHUTTINGDOWN, SHUTDOWN
}
