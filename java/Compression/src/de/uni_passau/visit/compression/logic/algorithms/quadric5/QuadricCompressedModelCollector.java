package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;
import de.uni_passau.visit.compression.logic.io.ObjModel;

/**
 * This class implements the @see QuadricAbstractCompressedModelCollector
 * interface and can be used to create a compressed model by the given vertices,
 * faces and texture coordinates, that will be treated by the given @see
 * QuadricAbstractCompressedModelHandler afterwards.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricCompressedModelCollector implements QuadricAbstractCompressedModelCollector {

	private final String header;
	private final QuadricAbstractCompressedModelHandler compressedModelHandler;
	private final double scalingFactor;
	private final double[] offset;
	private final boolean modelHasTexture;

	private static final Logger log = LogManager.getLogger(QuadricCompressedModelCollector.class);

	/**
	 * This constructor initializes the object with several arguments that are
	 * necessary for the model creation process.
	 * 
	 * @param header
	 *            The header that shall be used for the resulting model
	 * @param compressedModelHandler
	 *            The handler used for the treatment of the compressed model
	 * @param scalingFactor
	 *            The factor that was used to scale the initial model for
	 *            geometry-texture-consistency
	 * @param offset
	 *            The offset that was used for the translation of the initial model
	 * @param modelHasTexture
	 *            Should be true, if the initial model is texturized, has to be
	 *            false otherwise
	 */
	public QuadricCompressedModelCollector(String header, QuadricAbstractCompressedModelHandler compressedModelHandler,
			double scalingFactor, double[] offset, boolean modelHasTexture) {
		this.header = header;
		this.compressedModelHandler = compressedModelHandler;
		this.scalingFactor = scalingFactor;
		this.offset = offset;
		this.modelHasTexture = modelHasTexture;
	}

	/**
	 * This method uses the given vertices, faces and texture coordinates to create
	 * a new model and ensures the further treatment of this model by the handler
	 * given to the constructor.
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
			HashMap<Integer, TextureCoords> textureCoords, int vertexCount) {
		// assign a new index to each vertex
		log.debug("Redefining vertices...");
		HashMap<Integer, Integer> vertexIndexAssignment = new HashMap<>();
		ArrayList<Vertex> newVertices = new ArrayList<>();

		int index = 0;
		for (Map.Entry<Integer, QuadricVertex> entry : vertices.entrySet()) {
			Vertex vertexCopy = new Vertex(index, entry.getValue().getCoords().clone(),
					entry.getValue().getAdditionals().clone());
			vertexCopy.anormalize(scalingFactor, offset);
			newVertices.add(vertexCopy);
			vertexIndexAssignment.put(entry.getKey(), index);
			++index;
		}

		ArrayList<TextureCoords> newTextureCoords = new ArrayList<>();
		HashMap<Integer, Integer> textureIndexAssignment = null;
		int textureIndex = 0;
		if (modelHasTexture) {
			log.debug("Redefining texture coords...");
			textureIndexAssignment = new HashMap<>();

			for (Map.Entry<Integer, TextureCoords> entry : textureCoords.entrySet()) {
				TextureCoords textureCopy = new TextureCoords(textureIndex, entry.getValue().getCoords());
				newTextureCoords.add(textureCopy);
				textureIndexAssignment.put(entry.getKey(), textureIndex);
				++textureIndex;
			}
		}

		log.debug("Redefining faces...");
		index = 0;
		ArrayList<Face> newFaces = new ArrayList<>();

		for (QuadricFace f : faces) {
			if (arePairwiseDifferent(f.getVertexIndices())) {
				// this check is necessary for meshes where different vertices share the same
				// texture coords
				if (modelHasTexture) {
					for (int i = 0; i < 3; ++i) {
						if (!textureIndexAssignment.containsKey(f.getTextureCoords()[i].getIndex())) {
							TextureCoords textureCopy = new TextureCoords(textureIndex,
									f.getTextureCoords()[i].getCoords());
							newTextureCoords.add(textureCopy);
							textureIndexAssignment.put(f.getTextureCoords()[i].getIndex(), textureIndex);
							++textureIndex;
						}
					}
				}

				newFaces.add(new QuadricFace(index, f.getVertices(), null, f.getTextureCoords(),
						f.getAdditionals(), f.getMaterial(), vertexIndexAssignment, newVertices, textureIndexAssignment,
						newTextureCoords, modelHasTexture));
				++index;
			} else {
				/* degenerate faces have already been deleted during the compression process */
				throw new IllegalStateException("Illegal branch");
			}
		}

		ObjModel newModel = new ObjModel(newVertices, new ArrayList<Normal>(), newTextureCoords, newFaces, header);
		return compressedModelHandler.handleCompressedModel(newModel, vertexCount);
	}

	private boolean arePairwiseDifferent(int[] newIndices) {
		for (int i = 0; i < newIndices.length; ++i) {
			for (int j = i + 1; j < newIndices.length; ++j) {
				if (newIndices[i] == newIndices[j]) {
					return false;
				}
			}
		}

		return true;
	}

}
