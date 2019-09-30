package de.uni_passau.visit.compression.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.data.CompressionJob;
import de.uni_passau.visit.compression.data.JobState;
import de.uni_passau.visit.compression.data.QueueEntry;
import de.uni_passau.visit.compression.exceptions.DatabaseException;
import de.uni_passau.visit.compression.exceptions.QueueCapacityException;
import de.uni_passau.visit.compression.exceptions.QueueClosedException;

/**
 * This class represents the model responsible for the management of the
 * compression job queue. It provides method for job dispatching, retrieval and
 * monitoring.
 * 
 * The queue management uses a SQLite database storing all remaining and
 * finished compression jobs.
 * 
 * @author Florian Schlenker
 *
 */
public class QueueModel {

	private static final String SQL_CURRENT_TIMESTAMP = "strftime('%s', 'now')";
	private static final String DB_PATH = RootModel.DATA_ROOT + "/jobQueue.db";

	private static final Logger log = LogManager.getLogger(QueueModel.class);

	private final Connection con;
	private final ConfigModel config;
	private boolean isShutDown = false;

	/**
	 * This constructor creates a new queue model using the given configuration
	 * model. The database structure will be set up and the state of all unfinished
	 * jobs created by a former instance of this model will be set to "error".
	 * 
	 * @param config
	 *            The configuration model determining parameters for the new queue
	 *            model
	 */
	public QueueModel(ConfigModel config) {
		this.config = config;
		this.con = getDatabaseConnection();
		initDatabase();
	}

	private Connection getDatabaseConnection() {
		final String url = "jdbc:sqlite:" + DB_PATH;
		try {
			return DriverManager.getConnection(url);
		} catch (SQLException e) {
			throw new DatabaseException(
					"Database connection could not be established. Please check access to file '" + DB_PATH + "'");
		}
	}

	private void initDatabase() {
		try {
			// setup tables
			final String sql1 = "CREATE TABLE IF NOT EXISTS jobs (id INTEGER PRIMARY KEY, state INTEGER NOT NULL, "
					+ "receivedOn INTEGER NOT NULL, lastStateChange INTEGER NOT NULL, "
					+ "basePath TEXT NOT NULL, objectUid TEXT NOT NULL, mediaUid TEXT NOT NULL, fileTitle TEXT, "
					+ "mimeType TEXT NOT NULL)";
			con.createStatement().execute(sql1);

			final String sql2 = "CREATE TABLE IF NOT EXISTS jobCompressionLevels (jobId INTEGER NOT NULL, compressionLevel STRING NOT NULL)";
			con.createStatement().execute(sql2);

			// if jobs with state "processing" remain from a forced stop set their state to
			// "error"
			final String sql3 = "UPDATE jobs SET state = 2, lastStateChange = " + SQL_CURRENT_TIMESTAMP
					+ " WHERE state = 1";
			con.createStatement().execute(sql3);

		} catch (SQLException ex) {
			throw new RuntimeException(
					"During database initialization the following error occurred: " + ex.getMessage());
		}
	}

	/**
	 * This method dispatches the given compression job to the queue.
	 * 
	 * @param job
	 *            The compression job that shall be added to the queue
	 * @throws QueueCapacityException
	 *             if the queue contains too many unfinished entries so that this
	 *             job has been rejected
	 * @throws QueueClosedException
	 *             if the queue has been closed due to a scheduled compression
	 *             system shut down
	 */
	public void dispatchJob(CompressionJob job) throws QueueCapacityException, QueueClosedException {
		if (isShutDown) {
			throw new QueueClosedException();
		} else {
			if (config.getQueueMaxLength() > 0 && getEnqueuedJobCount() >= config.getQueueMaxLength()) {
				throw new QueueCapacityException();
			} else {
				try {
					final String sql1 = "INSERT INTO jobs (state, receivedOn, lastStateChange, basePath, objectUid, mediaUid, fileTitle, mimeType) VALUES (?, "
							+ SQL_CURRENT_TIMESTAMP + ", " + SQL_CURRENT_TIMESTAMP + ", ?, ?, ?, ?, ?)";
					final PreparedStatement s1 = con.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
					s1.setInt(1, JobState.ENQUEUED.value());
					s1.setString(2, job.getBasePath());
					s1.setString(3, job.getObjectUid());
					s1.setString(4, job.getMediaUid());
					s1.setString(5, job.getTitle());
					s1.setString(6, job.getMimeType());
					s1.execute();

					ResultSet jobIdSet = s1.getGeneratedKeys();
					if (jobIdSet.next()) {
						int jobId = jobIdSet.getInt(1);

						final String sql2 = "INSERT INTO jobCompressionLevels (jobId, compressionLevel) VALUES (?, ?)";
						final PreparedStatement s2 = con.prepareStatement(sql2);

						for (String level : job.getLevels()) {
							s2.setInt(1, jobId);
							s2.setString(2, level);
							s2.execute();
						}
					} else {
						throw new DatabaseException("Could not retrieve job id of current job.");
					}

					log.info("Dispatched compression job: " + job.getTitle());
				} catch (SQLException ex) {
					throw new DatabaseException(
							"During a job dispatch the following database error occurred: " + ex.getMessage());
				}
			}
		}
	}

	/**
	 * This method cancels the queue entry with the given id if existing. All
	 * information regarding this job will be deleted.
	 * 
	 * @param id
	 *            The id of the queue entry that shall be deleted
	 */
	public void cancelJob(int id) {
		try {
			final String sql1 = "DELETE FROM jobCompressionLevels WHERE jobId = ? AND jobId IN (SELECT id FROM jobs WHERE state = 0)";
			final String sql2 = "DELETE FROM jobs WHERE id = ? AND state = 0";

			final PreparedStatement s1 = con.prepareStatement(sql1);
			s1.setInt(1, id);
			s1.executeUpdate();

			final PreparedStatement s2 = con.prepareStatement(sql2);
			s2.setInt(1, id);
			s2.executeUpdate();

			log.info("Cancelled compression job with id " + id);
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During job deletion the following database error occurred: " + ex.getMessage());
		}
	}

	/**
	 * This method returns next pending compression job of the queue or null, if no
	 * such job exists.
	 * 
	 * @return Returns the next pending compression job of the queue of null, if no
	 *         such job exists
	 */
	public QueueEntry getNextEnqueuedJob() {
		try {
			final String sql1 = "SELECT id, state, receivedOn, lastStateChange, basePath, objectUid, mediaUid, fileTitle, mimeType FROM jobs WHERE state = 0 ORDER BY receivedOn ASC LIMIT 1";
			final Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(sql1);

			if (rs1.next()) {
				int id = rs1.getInt("id");
				String[] compressionLevels = getJobCompressionLevels(id);

				CompressionJob job = new CompressionJob(rs1.getString("basePath"), rs1.getString("objectUid"),
						rs1.getString("mediaUid"), rs1.getString("fileTitle"), rs1.getString("mimeType"),
						compressionLevels);

				return new QueueEntry(job, rs1.getLong("receivedOn"), rs1.getInt("id"),
						JobState.values()[rs1.getInt("state")], rs1.getLong("lastStateChange"));
			} else {
				return null;
			}
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During a job query (getNextEnqueuedJob) the following database error occurred: "
							+ ex.getMessage());
		}
	}

	/**
	 * This method returns a collection containing all unfinished queue entries.
	 * 
	 * @return Returns a collection containing all unfinished queue entries
	 */
	public Collection<QueueEntry> getAllUnfinishedJobs() {
		try {
			final String sql1 = "SELECT id, state, receivedOn, lastStateChange, basePath, objectUid, mediaUid, fileTitle, mimeType FROM jobs WHERE state < 2 ORDER BY receivedOn ASC";
			final Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(sql1);

			ArrayList<QueueEntry> entries = new ArrayList<QueueEntry>();
			JobState[] jobStateValue = JobState.values();
			while (rs1.next()) {
				int id = rs1.getInt("id");
				String[] compressionLevels = getJobCompressionLevels(id);

				CompressionJob job = new CompressionJob(rs1.getString("basePath"), rs1.getString("objectUid"),
						rs1.getString("mediaUid"), rs1.getString("fileTitle"), rs1.getString("mimeType"),
						compressionLevels);

				entries.add(new QueueEntry(job, rs1.getLong("receivedOn"), rs1.getInt("id"),
						jobStateValue[rs1.getInt("state")], rs1.getLong("lastStateChange")));
			}

			return entries;
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During a job query (getAllUnfinishedJobs) the following database error occurred: "
							+ ex.getMessage());
		}
	}

	private String[] getJobCompressionLevels(int id) throws SQLException {
		final String sql2 = "SELECT compressionLevel FROM jobCompressionLevels WHERE jobId = ?";
		final PreparedStatement s2 = con.prepareStatement(sql2);
		s2.setInt(1, id);

		List<String> compressionLevels = new ArrayList<String>();
		ResultSet rs2 = s2.executeQuery();
		while (rs2.next()) {
			compressionLevels.add(rs2.getString("compressionLevel"));
		}
		return compressionLevels.toArray(new String[] {});
	}

	/**
	 * This method returns the count of pending compression jobs in the queue.
	 * 
	 * @return Returns the count of pending compression jobs in the queue.
	 */
	public int getEnqueuedJobCount() {
		try {
			final String sql1 = "SELECT COUNT(*) FROM jobs WHERE state = 0";
			final Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(sql1);

			if (rs1.next()) {
				return rs1.getInt(1);
			} else {
				throw new RuntimeException(
						"During a job query (getEnqueuedJobCount) an invalid empty result set was returned.");
			}
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During a job query (getEnqueuedJobCount) the following database error occurred: "
							+ ex.getMessage());
		}
	}

	/**
	 * This method returns a collection containing successfully of defectively
	 * finished compression jobs. The list's length is limited to at most
	 * {@code limit} elements with an offset of {@see offset}.
	 * 
	 * @param limit
	 *            The limit for the returned collection's length
	 * @param offset
	 *            The offset for the elements in the returned collection
	 * @return Returns a list containing successfully of defectively finished
	 *         compression jobs
	 */
	public Collection<QueueEntry> getProcessedJobs(int limit, int offset) {
		try {
			final String sql1 = "SELECT id, state, receivedOn, lastStateChange, basePath, objectUid, mediaUid, fileTitle, mimeType FROM jobs WHERE state >= 2 ORDER BY lastStateChange DESC LIMIT ? OFFSET ?";
			final PreparedStatement s1 = con.prepareStatement(sql1);
			s1.setInt(1, limit);
			s1.setInt(2, offset);
			ResultSet rs1 = s1.executeQuery();

			ArrayList<QueueEntry> entries = new ArrayList<QueueEntry>();
			JobState[] jobStateValue = JobState.values();
			while (rs1.next()) {
				int id = rs1.getInt("id");
				String[] compressionLevels = getJobCompressionLevels(id);

				CompressionJob job = new CompressionJob(rs1.getString("basePath"), rs1.getString("objectUid"),
						rs1.getString("mediaUid"), rs1.getString("fileTitle"), rs1.getString("mimeType"),
						compressionLevels);

				entries.add(new QueueEntry(job, rs1.getLong("receivedOn"), rs1.getInt("id"),
						jobStateValue[rs1.getInt("state")], rs1.getLong("lastStateChange")));
			}

			return entries;
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During a job query (getProcessedJobs) the following database error occurred: " + ex.getMessage());
		}
	}

	/**
	 * This method returns the count of successfully or defectively finished
	 * compression jobs.
	 * 
	 * @return Returns the count of successfully or defectively finished compression
	 *         jobs
	 */
	public int getProcessedJobCount() {
		try {
			final String sql1 = "SELECT COUNT(*) FROM jobs WHERE state >= 2";
			final Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(sql1);

			if (rs1.next()) {
				return rs1.getInt(1);
			} else {
				throw new RuntimeException(
						"During a job query (getProcessedJobCount) an invalid empty result set was returned.");
			}
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During a job query (getProcessedJobCount) the following database error occurred: "
							+ ex.getMessage());
		}
	}

	/**
	 * This method updates the state of the job referred by the queue entry with the
	 * given id.
	 * 
	 * @param id
	 *            The id of the queue entry whose job's state shall be updated
	 * @param state
	 *            The new state of the queue entry's compression job
	 */
	public void setJobState(int id, JobState state) {
		try {
			final String sql1 = "UPDATE jobs SET state = ?, lastStateChange = " + SQL_CURRENT_TIMESTAMP
					+ " WHERE id = ?";

			final PreparedStatement s1 = con.prepareStatement(sql1);
			s1.setInt(1, state.value());
			s1.setInt(2, id);
			s1.executeUpdate();
		} catch (SQLException ex) {
			throw new DatabaseException(
					"During job state update the following database error occurred: " + ex.getMessage());
		}
	}

	/**
	 * This method shuts down the queue model and in particular closes the queue, so
	 * that all incoming jobs will be rejected.
	 */
	public void shutDown() {
		this.isShutDown = true;
	}

}
