package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.HashSet;

import de.uni_passau.visit.compression.logic.util.QueueEntryPredicate;

/**
 * This implementation of @see QueueEntryPredicate determines, if a given edge
 * collapse operation on a mesh (in a queue of possible edge collapses) retains
 * the link condition. For further information on the link condition confer to
 * Botsch, Mario, et al. Polygon mesh processing. AK Peters/CRC Press, 2010.
 * 
 * @author Florian Schlenker
 *
 */
public class LinkConditionChecker implements QueueEntryPredicate<QuadricCollapseInfo> {

	/**
	 * This method checks if the given edge collapse operation retains the link
	 * condition. The link condition is fulfilled, if the following two conditions
	 * are satisfied: 1) The cardinality of the intersection of the one-rings of the
	 * two vertices involved in the edge collapse is at most two. 2) When both of
	 * the vertices lie on the boundary, also the edge has to be a boundary edge.
	 * 
	 * @param entry
	 *            The edge collapse operation that shall be checked
	 * @return Returns true, if the edge collapse operation retains the link
	 *         condition, otherwise false
	 */
	@Override
	public boolean hasPredicate(QuadricCollapseInfo entry) {
		QuadricVertex a = entry.getVertexA();
		QuadricVertex b = entry.getVertexB();

		// Step 1: Check if the cardinality of the intersection of the one-rings of the
		// two vertices involved in the edge collapse is at most two.
		HashSet<Integer> aNeighbours = new HashSet<>();
		HashSet<Integer> bNeighbours = new HashSet<>();

		for (QuadricFace f : a.getAdjacentFaces()) {
			for (int i = 0; i < f.getVertexIndices().length; ++i) {
				aNeighbours.add(f.getVertexIndices()[i]);
			}
		}

		for (QuadricFace f : b.getAdjacentFaces()) {
			for (int i = 0; i < f.getVertexIndices().length; ++i) {
				bNeighbours.add(f.getVertexIndices()[i]);
			}
		}

		aNeighbours.retainAll(bNeighbours);
		aNeighbours.remove(a.getIndex());
		aNeighbours.remove(b.getIndex());

		// Step 2: If both of the vertices lie at the boundary: Is the edge also a
		// boundary edge?
		boolean conditionA = aNeighbours.size() <= 2;
		boolean conditionB = !entry.getVertexA().isAtBoundary() || !entry.getVertexB().isAtBoundary()
				|| entry.isAtBoundary();

		return conditionA && conditionB;
	}

}
