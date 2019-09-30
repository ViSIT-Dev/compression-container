package de.uni_passau.visit.compression.logic.algorithms.quadric5;

/**
 * This class stores information about an edge in the mesh. Instances of this
 * class can hold the count of faces using this edge. For boundary edges, i.e.
 * edges that appear in one face only, the third vertex of the single face using
 * this edge is also stored.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricEdgeBoundaryInfo {

	private QuadricVertex edgeVertexA, edgeVertexB, vertexC;
	private int occurences = 1;

	/**
	 * This constructor defines a new object for the edge given by the two vertices
	 * edgeVertexA and edgeVertexC.
	 * 
	 * @param edgeVertexA
	 *            The first of the two vertices defining the edge
	 * @param edgeVertexB
	 *            The second of the two vertices defining the edge
	 * @param vertexC
	 *            The third vertex of the face using the edge represented by this
	 *            object
	 */
	public QuadricEdgeBoundaryInfo(QuadricVertex edgeVertexA, QuadricVertex edgeVertexB, QuadricVertex vertexC) {
		super();
		this.edgeVertexA = edgeVertexA;
		this.edgeVertexB = edgeVertexB;
		this.vertexC = vertexC;
	}

	/**
	 * This method returns the first of the two vertices defining the edge.
	 * 
	 * @return Returns the first of the two vertices defining the edge
	 */
	public QuadricVertex getEdgeVertexA() {
		return edgeVertexA;
	}

	/**
	 * This method returns the second of the two vertices defining the edge.
	 * 
	 * @return Returns the second of the two vertices defining the edge
	 */
	public QuadricVertex getEdgeVertexB() {
		return edgeVertexB;
	}

	/**
	 * For boundary edges this method returns the third vertex of the only face
	 * using the edge represented by this object. If more than one face uses this
	 * edge, the return value will represent the third vertex of the face with the
	 * first occurrence of this edge, which was therefore passed to the constructor.
	 * However the the use of this method is intended for boundary edges only.
	 * 
	 * @return The third vertex of the first face using the edge represented by this
	 *         object
	 */
	public QuadricVertex getVertexC() {
		return vertexC;
	}

	/**
	 * This method returns the count of occurrences of this edge in the underlying
	 * model.
	 * 
	 * @return The count of occurrences of this edge in the underlying model
	 */
	public int getOccurences() {
		return occurences;
	}

	/**
	 * This method increments the occurrence counter for the edge represented by
	 * this edge. This method has to be called only starting from the second
	 * occurrence of the edge, since the first occurrence is already registered by
	 * calling the constructor of this object.
	 */
	public void incrementOccurences() {
		occurences += 1;
	}

}
