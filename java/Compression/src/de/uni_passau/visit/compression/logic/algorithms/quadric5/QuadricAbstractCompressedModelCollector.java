package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.Collection;
import java.util.HashMap;

import de.uni_passau.visit.compression.logic.data.TextureCoords;

/**
 * This interface describes a class that can be used to create a new
 * (compressed) model out of given sets of vertices, faces and texture
 * coordinates. The further treatment of the created model is also in the scope
 * of this class, since it is not returned by the method of this interface.
 * 
 * @author Florian Schlenker
 *
 */
public interface QuadricAbstractCompressedModelCollector {

	/**
	 * This method uses the given vertices, faces and texture coordinates to create
	 * a new model and ensures the further treatment of this model.
	 * 
	 * @param vertices
	 *            A map using the vertex indices as keys and the respective vertices
	 *            as values
	 * @param faces
	 *            A collection containing all faces present in the new model
	 * @param textureCoords
	 *            A map using the texture coordinate indices as keys and the
	 *            respective texture coordinates as values
	 * @param vertexCount
	 *            The count of vertices of the new model
	 * @return Returns false, if an error occurred during model creation of further
	 *         treatment, otherwise true
	 */
	public boolean storeCompressedModel(HashMap<Integer, QuadricVertex> vertices, Collection<QuadricFace> faces,
			HashMap<Integer, TextureCoords> textureCoords, int vertexCount);

}
