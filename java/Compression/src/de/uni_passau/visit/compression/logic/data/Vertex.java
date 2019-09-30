package de.uni_passau.visit.compression.logic.data;

import java.util.Arrays;

/**
 * This class represents a vertex of a 3D-model. It consists of an index, the
 * coordinates of the vertex and optional additional information. Strictly
 * speaking this vertices represented by this class are not restricted to three
 * dimensions.
 * 
 * @author Florian Schlenker
 *
 */
public class Vertex {
	protected int index;
	protected final double[] coord;
	private final String[] additionals;

	/**
	 * This constructor creates a new vertex with the given arguments.
	 * 
	 * @param index
	 *            The index of the new vertex
	 * @param coord
	 *            The coordinates of the new vertex as array of length 3
	 * @param additionals
	 *            Optional additional information regarding the vertex or null
	 */
	public Vertex(int index, double[] coord, String[] additionals) {
		super();
		this.index = index;
		this.coord = coord;
		this.additionals = additionals;
	}

	/**
	 * This method returns the index of the vertex.
	 * 
	 * @return Returns the index of the vertex
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * This method returns the coordinates of the vertex.
	 * 
	 * @return Returns the coordinates of the vertex as array of length 3
	 */
	public double[] getCoords() {
		return coord;
	}

	/**
	 * This method returns the optional additional information regarding this
	 * vertex.
	 * 
	 * @return Returns the optional additional information regarding this vertex of
	 *         null, if no such information was given
	 */
	public String[] getAdditionals() {
		return additionals;
	}

	/**
	 * This method returns the squared Euclidean distance between this vertex and
	 * this given vertex.
	 * 
	 * @param w
	 *            The vertex, whose squared distance to the current vertex shall be
	 *            computed
	 * @return Returns the squared Euclidean distance between the two vertices
	 */
	public double getSquaredDistanceTo(Vertex w) {
		double sum = 0.0;

		for (int i = 0; i < coord.length; ++i) {
			sum += (coord[i] - w.coord[i]) * (coord[i] - w.coord[i]);
		}

		return sum;

	}

	/**
	 * This method normalizes the vertex according to the given scale and offset.
	 * After subtracting the offset the vertex will be scaled down by the given
	 * factor.
	 * 
	 * The composition of normalize and anormalize with the same parameters equals
	 * the identity.
	 * 
	 * @param scale
	 *            The scalar value by which the vertex's coordinates will be scaled
	 *            down
	 * @param offset
	 *            The offset defining the translation vector of length 3 that will
	 *            be subtracted from the coordinates before scaling
	 */
	public void normalize(double scale, double[] offset) {
		for (int i = 0; i < coord.length; ++i) {
			coord[i] = (coord[i] - offset[i]) / scale;
		}
	}

	/**
	 * This method undos a previous normalization of the vertex according to the
	 * given scale and offset. After multiplying the vertex's coordinates by the
	 * given scalar the offset vector will be added to the coordinates element-wise.
	 * 
	 * The composition of normalize and anormalize with the same parameters equals
	 * the identity.
	 * 
	 * @param scale
	 *            The scalar value by which the vertex's coordinates will be scaled
	 *            up
	 * @param offset
	 *            The offset defining the translation vector of length 3 that will
	 *            be added to the coordinates after scaling
	 */
	public void anormalize(double scale, double[] offset) {
		for (int i = 0; i < coord.length; ++i) {
			coord[i] = (coord[i] * scale) + offset[i];
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(additionals);
		result = prime * result + Arrays.hashCode(coord);
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (!Arrays.equals(additionals, other.additionals))
			return false;
		if (!Arrays.equals(coord, other.coord))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Vertex [index=" + index + ", coord=" + Arrays.toString(coord) + ", additionals="
				+ Arrays.toString(additionals) + "]";
	}

}
