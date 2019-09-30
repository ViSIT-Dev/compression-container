package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class extends the original @see Face implementation by adding some
 * functionality needed for the quadric mesh compression algorithm. The
 * functionality of the base class hasn't been modified, however.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricFace extends Face {

	private static final Logger log = LogManager.getLogger(QuadricFace.class);

	/**
	 * This constructor calls the constructor of the @see Face superclass passing
	 * the given arguments.
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
	public QuadricFace(int index, Vertex[] vertices, Normal[] normals, TextureCoords[] textureCoords,
			String[] additionals, String material) {
		super(index, vertices, normals, textureCoords, additionals, material);
	}

	/**
	 * This constructor calls the constructor of the @see Face superclass passing
	 * the given arguments. As vertices and texture coordinates however the
	 * respective objects in the given lists / maps will be used instead of the
	 * original vertices and texture coordinates used by the original face that is
	 * used as template for this face. These lists / maps will only be used in the
	 * constructor, no reference to them will be kept.
	 * 
	 * @param index
	 *            The index of the new face
	 * @param vertices
	 *            The vertices that have been used by the template face of the new
	 *            face
	 * @param normals
	 *            The normals of the new face in the same order as the vertices or
	 *            null
	 * @param textureCoords
	 *            The texture coordinates that have been used by the template face
	 *            or null, if hasTexture is false
	 * @param additionals
	 *            Additional information for the face which will be passed on
	 *            untouched
	 * @param material
	 *            The material that shall be used for the face
	 * @param vertexIndexAssignment
	 *            A map mapping the vertex indices of the old face used as template
	 *            to the new vertices given in the newVertices-list
	 * @param newVertices
	 *            A list containing all the new vertices that are referenced by the
	 *            indices in vertexIndexAssignment
	 * @param textureIndexAssignment
	 *            A map mapping the texture coordinate indices of the old face used
	 *            as template to the new texture coordinates given in the
	 *            newTextureCoords-list
	 * @param newTextureCoords
	 *            A list containing all the new texture coordinates that are
	 *            referenced by the indices in textureIndexAssignment
	 */
	public QuadricFace(int index, Vertex[] vertices, Normal[] normals, TextureCoords[] textureCoords,
			String[] additionals, String material, HashMap<Integer, Integer> vertexIndexAssignment,
			List<Vertex> newVertices, HashMap<Integer, Integer> textureIndexAssignment,
			List<TextureCoords> newTextureCoords, boolean hasTexture) {
		super(index,
				new Vertex[] { newVertices.get(vertexIndexAssignment.get(vertices[0].getIndex())),
						newVertices.get(vertexIndexAssignment.get(vertices[1].getIndex())),
						newVertices.get(vertexIndexAssignment.get(vertices[2].getIndex())) },
				normals,
				hasTexture
						? new TextureCoords[] {
								newTextureCoords.get(textureIndexAssignment.get(textureCoords[0].getIndex())),
								newTextureCoords.get(textureIndexAssignment.get(textureCoords[1].getIndex())),
								newTextureCoords.get(textureIndexAssignment.get(textureCoords[2].getIndex())) }
						: null,
				additionals, material);
	}

	/**
	 * This constructor creates a copy of the given face, where the arrays
	 * containing all the referenced objects will be shallowly cloned.
	 * 
	 * @param f
	 *            The original face used as template
	 */
	public QuadricFace(Face f) {
		this(f.getIndex(), f.getVertices().clone(), f.getNormals() == null ? null : f.getNormals().clone(),
				f.getTextureCoords() == null ? null : f.getTextureCoords().clone(),
				f.getAdditionals() == null ? null : f.getAdditionals().clone(), f.getMaterial());
	}

	/**
	 * This method has to be called after a edge contraction, so that the reference
	 * to the old vertex will be updated to the new contracted vertex.
	 * 
	 * @param oldIndex
	 *            The index of the old vertex that has been deleted
	 * @param newIndex
	 *            The index of the new contracted vertex
	 * @param newVertex
	 *            The new contracted vertex itself
	 * @return Returns true, if a vertex with the old index has been part of the
	 *         face, otherwise false
	 */
	public boolean registerVertexContraction(int oldIndex, int newIndex, Vertex newVertex) {
		for (int i = 0; i < vertexIndices.length; ++i) {
			if (vertexIndices[i] == oldIndex) {
				vertexIndices[i] = newIndex;
				vertices[i] = newVertex;
				return true;
			}
		}

		return false;
	}

	/**
	 * This method has to be called after a edge contraction on a texturized model,
	 * so that the reference to the old texture coordinate has to be updated to the
	 * new texture coordinate.
	 * 
	 * @param oldIndex
	 *            The index of the old texture coordinate that has been deleted
	 * @param newTex
	 *            The new texture coordinate
	 */
	public void registerTextureContraction(int oldIndex, TextureCoords newTex) {
		for (int i = 0; i < textureCoordIndices.length; ++i) {
			if (textureCoordIndices[i] == oldIndex) {
				textureCoordIndices[i] = newTex.getIndex();
				textureCoords[i] = newTex;
			}
		}
	}

	/**
	 * For performance reasons vertices hold a reference to all faces using this
	 * vertex. This method should be called, when a face gets degenerated or
	 * deleted. The reference to this face will then be untied in all vertices
	 * contained in the given map.
	 * 
	 * @param allVertices
	 *            A map using all vertices as values and their indices as keys
	 */
	public void unregisterFromAdjacentVertices(HashMap<Integer, QuadricVertex> allVertices) {
		for (int i = 0; i < vertexIndices.length; ++i) {
			if (allVertices.get(vertexIndices[i]) != null) {
				allVertices.get(vertexIndices[i]).unregisterAdjacentFace(this);
			} else {
				log.warn("Vertex " + vertexIndices[i] + " could not be found!");
			}
		}

		vertices = null;
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
		QuadricFace other = (QuadricFace) obj;
		if (index != other.index)
			return false;
		return true;
	}

}
