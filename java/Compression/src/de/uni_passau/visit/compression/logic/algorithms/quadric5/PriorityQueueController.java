package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.HashMap;

import org.apache.commons.math3.util.Pair;

import de.uni_passau.visit.compression.logic.util.OptimizedPriorityQueue;
import de.uni_passau.visit.compression.logic.util.QueueEntryPredicate;

/**
 * This class is used to access the heap of possible edge collapses and manages
 * the validity of these edge collapses. A collapse is valid, if and only if the
 * link condition holds (see @see LinkConditionChecker). Other vertex collapses
 * influence this condition for candidates in a certain neighborhood, however.
 * This class manages the necessary updates and therefore abstracts from that
 * difficulty.
 * 
 * A heap (@see OptimizedPriorityQueue) holds all candidates that get added.
 * When polling the next valid candidate, a poll operation will performed on the
 * heap repeatedly, until a valid candidate is returned. All encountered invalid
 * candidates are stored in a map and can get added to the heap later, if an
 * adjacent collapse renders this candidate valid. This reevaluation has to be
 * triggered by calling a specific method. Due to performance these adjacent
 * collapses are only checked for validity. The check for invalidity is only
 * performed when polling.
 * 
 * @author Florian Schlenker
 *
 */
public class PriorityQueueController {

	private final OptimizedPriorityQueue<QuadricCollapseInfo> heap;
	private final HashMap<Pair<Integer, Integer>, QuadricCollapseInfo> invalidCandidates;
	private final QueueEntryPredicate<QuadricCollapseInfo> checker;

	/**
	 * This constructor initializes the object including the collapse candidate
	 * heap. No arguments are required.
	 */
	public PriorityQueueController() {
		heap = new OptimizedPriorityQueue<>();
		invalidCandidates = new HashMap<>();
		checker = new LinkConditionChecker();
	}

	/**
	 * This method adds the given collapse candidate to the heap of possible
	 * collapse candidates.
	 * 
	 * @param candidate
	 *            The candidate that shall be added to the heap
	 */
	public void add(QuadricCollapseInfo candidate) {
		heap.add(candidate);
	}

	/**
	 * When calling this method, the link condition for a potentially existing
	 * collapse candidate between the given set of vertex indices will be
	 * reevaluated. Both arguments can be passed in an arbitrary order.
	 * 
	 * @param vertexIndex1
	 *            The index of one of the two vertices defining the collapse
	 *            candidate that shall be reevaluated
	 * @param vertexIndex2
	 *            The index of one of the two vertices defining the collapse
	 *            candidate that shall be reevaluated
	 */
	public void checkCandidate(int vertexIndex1, int vertexIndex2) {
		Pair<Integer, Integer> indexPair = getPairByIndices(vertexIndex1, vertexIndex2);
		QuadricCollapseInfo candidate = invalidCandidates.get(indexPair);

		if (candidate != null && checker.hasPredicate(candidate)) {
			invalidCandidates.remove(indexPair);
			heap.add(candidate);
		}
	}

	/**
	 * This method returns the next valid collapse candidate in the heap / priority
	 * queue or null, if no such candidate exists.
	 * 
	 * @return The next valid candidate in the heap if existing, otherwise null
	 */
	public QuadricCollapseInfo pollValid() {

		QuadricCollapseInfo candidate = null;
		do {
			if (candidate != null) {
				invalidCandidates.put(
						getPairByIndices(candidate.getVertexA().getIndex(), candidate.getVertexB().getIndex()),
						candidate);
			}
			candidate = heap.poll();
		} while (candidate != null && !checker.hasPredicate(candidate));

		return candidate;
	}

	/**
	 * This method removed the given candidate from the list of possible collapses.
	 * 
	 * @param candidate
	 *            The collapse candidate that shall be removed
	 */
	public void remove(QuadricCollapseInfo candidate) {
		invalidCandidates
				.remove(getPairByIndices(candidate.getVertexA().getIndex(), candidate.getVertexB().getIndex()));
		heap.remove(candidate);
	}

	private Pair<Integer, Integer> getPairByIndices(int vertexIndex1, int vertexIndex2) {
		return vertexIndex1 <= vertexIndex2 ? new Pair<Integer, Integer>(vertexIndex1, vertexIndex2)
				: new Pair<Integer, Integer>(vertexIndex2, vertexIndex1);
	}

}
