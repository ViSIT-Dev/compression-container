package de.uni_passau.visit.compression.network.transaction;

import java.util.Collection;

import de.uni_passau.visit.compression.data.QueueEntry;

/**
 * This class extends the @see DefaultResponse class and represents a network
 * response on a client request that shall be answered with a collection
 * containing one or several compression job queue items. It holds a collection
 * containing these items additionally to the success flag and the message
 * defined by the base class. The latter two will be set to reasonable values
 * automatically by this class' constructor.
 * 
 * @author Florian Schlenker
 * 
 */
public class QueueItemResponse extends DefaultResponse {

	private final Collection<QueueEntry> items;

	/**
	 * This constructor creates a new queue item response as a successful reaction
	 * on a request by a client that shall be answered with a collection containing
	 * one or several compression job queue items.
	 * 
	 * @param items
	 *            A collection of one or several compression job queue items that
	 *            shall be carried to the client
	 */
	public QueueItemResponse(Collection<QueueEntry> items) {
		super(true, "Data retrieval successful.");
		this.items = items;
	}

	/**
	 * This method returns the collection of compression job queue items that is
	 * carried by this response.
	 * 
	 * @return Returns the collection of compression job queue items that is carried
	 *         by this response
	 */
	public Collection<QueueEntry> getItems() {
		return items;
	}

}
