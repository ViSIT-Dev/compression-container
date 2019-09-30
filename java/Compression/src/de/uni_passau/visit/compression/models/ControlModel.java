package de.uni_passau.visit.compression.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.data.SystemState;
import de.uni_passau.visit.compression.exceptions.IllegalStateModificationException;
import de.uni_passau.visit.compression.network.Server;

/**
 * This class represents the model responsible for the management of the
 * compression system state.
 * 
 * @author Florian Schlenker
 *
 */
public class ControlModel {

	private final CompressionModel compressionModel;
	private final QueueModel queueModel;
	private final Server server;

	private static final Logger log = LogManager.getLogger(ControlModel.class);

	private SystemState currentSystemState = SystemState.STARTUP;

	/**
	 * This constructor creates a new control model managing the state of the given
	 * models.
	 * 
	 * @param configModel
	 *            The configuration model that shall be managed by this control
	 *            model
	 * @param compressionModel
	 *            The compression model that shall be managed by this control model
	 * @param queueModel
	 *            The queue model that shall be managed by this control model
	 * @param server
	 *            The server that shall be managed by this control model
	 */
	public ControlModel(ConfigModel configModel, CompressionModel compressionModel, QueueModel queueModel,
			Server server) {
		this.compressionModel = compressionModel;
		this.queueModel = queueModel;
		this.server = server;

		if (configModel.getAutostart()) {
			try {
				setRunning();
			} catch (IllegalStateModificationException e) {
				// do nothing as setting state to running is always legal when system state is
				// startup.
			}
		}
	}

	/**
	 * This method returns the current system state.
	 * 
	 * @return Returns the current system state.
	 */
	public SystemState getCurrentSystemState() {
		return currentSystemState;
	}

	/**
	 * This method sets the current system state to RUNNING. A call to this method
	 * is legal, if the previous system state is either PAUSED or STARTUP.
	 * 
	 * @throws IllegalStateModificationException
	 *             if the previous system state renders this state change invalid
	 */
	public void setRunning() throws IllegalStateModificationException {
		if (currentSystemState == SystemState.PAUSED || currentSystemState == SystemState.STARTUP) {
			currentSystemState = SystemState.RUNNING;
			log.info("System is running.");
			compressionModel.setPause(false);
		} else {
			throw new IllegalStateModificationException();
		}
	}

	/**
	 * This method sets the current system state to PAUSED. A call to this method is
	 * legal, if the previous system state is RUNNING.
	 * 
	 * @throws IllegalStateModificationException
	 *             if the previous system state renders this state change invalid
	 */
	public void setPaused() throws IllegalStateModificationException {
		if (currentSystemState == SystemState.RUNNING) {
			currentSystemState = SystemState.PAUSED;
			log.info("System is paused.");
			compressionModel.setPause(true);
		} else {
			throw new IllegalStateModificationException();
		}
	}

	/**
	 * This method kills the compression system immediately without any guarantees
	 * regarding data consistency.
	 */
	public void kill() {
		currentSystemState = SystemState.SHUTDOWN;
		log.info("System was killed. Exiting.");
		System.exit(0);
	}

	/**
	 * This method sets the current system state to SHUTTINGDOWN. A call to this
	 * method is legal, if the previous system state is either PAUSED, RUNNING or
	 * STARTUP. Depending on the given parameter either only the current compression
	 * job or the whole job queue will be processed before shutting down.
	 * 
	 * @param processRemainingQueue
	 *            If true, the whole job queue will be processed before shutting
	 *            down, otherwise only the current compression job will be finished
	 * 
	 * @throws IllegalStateModificationException
	 *             if the previous system state renders this state change invalid
	 */
	public void shutdown(boolean processRemainingQueue) throws IllegalStateModificationException {
		if (currentSystemState == SystemState.STARTUP || currentSystemState == SystemState.RUNNING
				|| currentSystemState == SystemState.PAUSED) {
			currentSystemState = SystemState.SHUTTINGDOWN;
			log.info("System was shut down, remaining queue will " + (processRemainingQueue ? "" : "not ")
					+ "be processed");
			queueModel.shutDown();
			compressionModel.shutdown(processRemainingQueue);
			server.stop();
		} else {
			throw new IllegalStateModificationException();
		}
	}

}
