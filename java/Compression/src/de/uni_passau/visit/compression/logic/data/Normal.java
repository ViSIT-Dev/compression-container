package de.uni_passau.visit.compression.logic.data;

/**
 * This class represents a normal vector of a face or a vertex in a 3D-model. It
 * consists of a index and a 3-dimensional vector representing the normals
 * direction.
 * 
 * @author Florian Schlenker
 *
 */
public class Normal {
	private final int index;
	private final double[] normal;

	/**
	 * This constructor creates a new normal with the given index and the given
	 * direction.
	 * 
	 * @param index
	 *            The index of the new normal
	 * @param normal
	 *            The direction of the new normal as array of length 3
	 */
	public Normal(int index, double[] normal) {
		super();
		this.index = index;
		this.normal = normal;
	}

	/**
	 * This constructor creates a new normal with the given index and the given
	 * direction.
	 * 
	 * @param index
	 *            The index of the new normal
	 * @param n1
	 *            The coordinate of the direction vector with respect to the
	 *            x1-axis.
	 * @param n2
	 *            The coordinate of the direction vector with respect to the
	 *            x2-axis.
	 * @param n3
	 *            The coordinate of the direction vector with respect to the
	 *            x3-axis.
	 */
	public Normal(int index, double n1, double n2, double n3) {
		this(index, new double[] { n1, n2, n3 });
	}

	/**
	 * This method returns the index of the normal.
	 * 
	 * @return Returns the index of the normal
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * This method returns the direction of the normal.
	 * 
	 * @return An array representing the direction of the normal
	 */
	public double[] getNormal() {
		return normal;
	}
}
