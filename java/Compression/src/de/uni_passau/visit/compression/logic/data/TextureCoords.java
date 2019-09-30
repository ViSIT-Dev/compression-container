package de.uni_passau.visit.compression.logic.data;

/**
 * This class represents the texture coordinates at one face adjacent to a
 * specific vertex. It consists of the 2-dimensional coordinate vector and an
 * index.
 * 
 * @author Florian Schlenker
 *
 */
public class TextureCoords {
	private final int index;
	private final double[] coords;

	/**
	 * This constructor creates a new texture coordinates object with the given
	 * index and the given coordinates.
	 * 
	 * @param index
	 *            The index of the new texture coordinates
	 * @param coords
	 *            The coordinates as array of length 2
	 */
	public TextureCoords(int index, double[] coords) {
		super();
		this.index = index;
		this.coords = coords;
	}

	/**
	 * This constructor creates a new texture coordinates object with the given
	 * index and the given coordinates.
	 * 
	 * @param index
	 *            The index of the new texture coordinates
	 * @param u
	 *            The u-coordinate of the texture coordinates
	 * @param v
	 *            The v-coordinate of the texture coordinates
	 */
	public TextureCoords(int index, double u, double v) {
		this(index, new double[] { u, v });
	}

	/**
	 * This method returns the index of this texture coordinates.
	 * 
	 * @return Returns the index of this texture coordinates
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * This method returns this object's texture coordinates
	 * 
	 * @return Returns the texture coordinates as array of length 2
	 */
	public double[] getCoords() {
		return coords;
	}
}
