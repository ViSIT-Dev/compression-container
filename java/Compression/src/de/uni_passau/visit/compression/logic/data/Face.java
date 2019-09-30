package de.uni_passau.visit.compression.logic.data;

import java.util.Arrays;

/**
 * This class represents a face of a 3-dimensional model. No restrictions on the
 * order of the face are made. Additional to the face's vertices one can assign
 * normals, texture coordinates and additional information optionally.
 * 
 * @author Florian Schlenker
 *
 */
public class Face {
	protected final int index;
	protected Vertex[] vertices;
	protected final Normal[] normals;
	protected final TextureCoords[] textureCoords;
	private final String[] additionals;
	private final String material;
	protected final int[] vertexIndices, normalIndices, textureCoordIndices;
	private boolean hasTexture = false, hasNormals = false;

	/**
	 * This constructor creates a new face with the given properties.
	 * 
	 * @param index
	 *            The index of the new face
	 * @param vertices
	 *            The vertices used by the new face
	 * @param normals
	 *            The normals of the new face in the same order as the vertices or
	 *            null
	 * @param textureCoords
	 *            The texture coordinates of the new face in the same order as the
	 *            vertices or null
	 * @param additionals
	 *            Additional information for the face which will be passed on
	 *            untouched
	 * @param material
	 *            The material that shall be used for the face
	 */
	public Face(int index, Vertex[] vertices, Normal[] normals, TextureCoords[] textureCoords, String[] additionals,
			String material) {
		super();
		this.index = index;
		this.vertices = vertices;
		this.normals = normals;
		this.textureCoords = textureCoords;
		this.additionals = additionals;
		this.material = material;

		this.vertexIndices = new int[vertices.length];
		for (int i = 0; i < vertices.length; ++i) {
			this.vertexIndices[i] = vertices[i].getIndex();
		}

		if (normals != null) {
			this.normalIndices = new int[normals.length];
			for (int i = 0; i < normals.length; ++i) {
				this.normalIndices[i] = normals[i].getIndex();
			}

			hasNormals = true;
		} else {
			hasNormals = false;
			normalIndices = null;
		}

		if (textureCoords != null) {
			this.textureCoordIndices = new int[textureCoords.length];
			for (int i = 0; i < textureCoords.length; ++i) {
				this.textureCoordIndices[i] = textureCoords[i].getIndex();
			}

			hasTexture = true;
		} else {
			hasTexture = false;
			textureCoordIndices = null;
		}
	}

	/**
	 * This method returns the index of this face.
	 * 
	 * @return Returns the index of this face
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * This method returns the vertices of this face.
	 * 
	 * @return Returns an array containing the vertices of this face
	 */
	public Vertex[] getVertices() {
		return vertices;
	}

	/**
	 * This method returns the normals of this face.
	 * 
	 * @return Returns an array containing the normals of this face or null, if no
	 *         normals have been passed to the constructor
	 */
	public Normal[] getNormals() {
		return normals;
	}

	/**
	 * This method returns the texture coordinates of this face.
	 * 
	 * @return Returns an array containing the texture coordinates of this face or
	 *         null, if no texture coordintaes have been passed to the constructor
	 */
	public TextureCoords[] getTextureCoords() {
		return textureCoords;
	}

	/**
	 * This method returns the additional information of this face.
	 * 
	 * @return Returns an array containing the additional information of this face
	 *         or null, if no such information has been passed to the constructor
	 */
	public String[] getAdditionals() {
		return additionals;
	}

	/**
	 * This method returns the material that has been assigned to the face.
	 * 
	 * @return Returns the material that has been assigned to the face
	 */
	public String getMaterial() {
		return material;
	}

	/**
	 * This method returns the indices of the vertices of this face.
	 * 
	 * @return Returns an array containing the indices of the vertices of this face
	 */
	public int[] getVertexIndices() {
		return vertexIndices;
	}

	/**
	 * This method returns the indices of the normals of this face.
	 * 
	 * @return Returns an array containing the indices of the normals of this face
	 *         or null, if no normals have been passed to the constructor.
	 */
	public int[] getNormalIndices() {
		return normalIndices;
	}

	/**
	 * This method returns the indices of the texture coordinates of this face.
	 * 
	 * @return Returns an array containing the indices of the texture coordinates of
	 *         this face or null, if no texture coordinates have been passed to the
	 *         constructor.
	 */
	public int[] getTextureCoordIndices() {
		return textureCoordIndices;
	}

	/**
	 * This method returns if texture coordinates have been assigned to the face.
	 * 
	 * @return Returns true, if texture coordinates have been assigned, otherwise
	 *         false
	 */
	public boolean hasTexture() {
		return hasTexture;
	}

	/**
	 * This method returns if normals have been assigned to the face.
	 * 
	 * @return Returns true, if normals have been assigned, otherwise false
	 */
	public boolean hasNormals() {
		return hasNormals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Face other = (Face) obj;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Face [index=" + index + ", vertexIndices=" + Arrays.toString(vertexIndices) + ", vertices="
				+ Arrays.toString(vertices) + "]";
	}

}
