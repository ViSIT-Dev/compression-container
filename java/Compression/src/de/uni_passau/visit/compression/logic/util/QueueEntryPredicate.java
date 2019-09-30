package de.uni_passau.visit.compression.logic.util;

/**
 * This interface represents a potential predicate for elements of some data
 * structure. The only method that has to be implemented by classes implementing
 * this interface determines, if a given element contained in the data structure
 * has the predicate defined by the implementing class.
 * 
 * @author Florian Schlenker
 *
 * @param The
 *            type of the elements of the data structure
 */
public interface QueueEntryPredicate<E> {

	/**
	 * This method determines, if a given element of a data structure has the
	 * predicate defined by the implementing class.
	 * 
	 * @param entry
	 *            The element of the data structure
	 * @return Returns true, if the element has the predicate defined by the
	 *         implementing class, otherwise false
	 */
	public boolean hasPredicate(E entry);

}
