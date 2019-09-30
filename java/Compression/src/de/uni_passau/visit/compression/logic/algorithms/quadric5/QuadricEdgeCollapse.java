package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.exceptions.NonManifoldModelException;
import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;
import de.uni_passau.visit.compression.logic.data.VertexIndexComparator;
import de.uni_passau.visit.compression.logic.io.AbstractModel;

/**
 * This is the main class of the quadric edge collapse compression algorithm for
 * 3D-models described in
 * 
 * Garland, Michael, and Paul S. Heckbert. "Surface simplification using quadric
 * error metrics." Proceedings of the 24th annual conference on Computer
 * graphics and interactive techniques. ACM Press/Addison-Wesley Publishing Co.,
 * 1997.
 * 
 * and
 * 
 * Garland, Michael, and Paul S. Heckbert. "Simplifying surfaces with color and
 * texture using quadric error metrics." Proceedings Visualization'98 (Cat. No.
 * 98CB36276). IEEE, 1998.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricEdgeCollapse {

	private static final Logger log = LogManager.getLogger(QuadricEdgeCollapse.class);

	private final QuadricEdgeCollapseConfig config;

	/**
	 * This constructor creates a new quadric edge collapse algorithm compressor
	 * using the settings specified in the given configuration object.
	 * 
	 * @param config The configuration object specifying the settings used for the
	 *               compressor
	 */
	public QuadricEdgeCollapse(QuadricEdgeCollapseConfig config) {
		this.config = config;
	}

	/**
	 * This class initiates the compression of a given model to the given vertex
	 * counts using the quadric edge collapse algorithm. The resulting models are
	 * treated by the given @see QuadricAbstractCompressedModelHandler.
	 * 
	 * @param inputModel             The original model that shall be compressed
	 * @param desiredVertexCount     An array containing the vertex counts of all
	 *                               desired compression levels
	 * @param compressedModelHandler The handler responsible for the treatment of
	 *                               the resulting compressed models
	 * @return Returns false, if an error occurred while storing the resulting
	 *         models
	 * @throws InvalidAlgorithmParameterException if the given model contains
	 *                                            non-triangular faces
	 * @throws NonManifoldModelException          will not be thrown, since the
	 *                                            current implementation supports
	 *                                            non-manifold models
	 */
	public boolean compute(AbstractModel inputModel, Integer[] desiredVertexCount,
			QuadricAbstractCompressedModelHandler compressedModelHandler)
			throws InvalidAlgorithmParameterException, NonManifoldModelException {

		String header = inputModel.getHeader();

		double mins[] = inputModel.getVertices().get(0).getCoords().clone();
		double maxs[] = inputModel.getVertices().get(0).getCoords().clone();
		for (Vertex v : inputModel.getVertices()) {
			double[] coords = v.getCoords();
			for (int i = 0; i < mins.length; ++i) {
				if (coords[i] < mins[i]) {
					mins[i] = coords[i];
				}

				if (coords[i] > maxs[i]) {
					maxs[i] = coords[i];
				}
			}
		}

		double scale = Math.max(maxs[0] - mins[0], Math.max(maxs[1] - mins[1], maxs[2] - mins[2]));
		log.debug("Scaling factor: " + scale);

		log.debug("Initializing vertices...");
		HashMap<Integer, QuadricVertex> vertices = new HashMap<>();
		for (Vertex v : inputModel.getVertices()) {
			QuadricVertex quadricVertex = new QuadricVertex(v);
			quadricVertex.normalize(scale, mins);
			vertices.put(v.getIndex(), quadricVertex);
		}

		log.debug("Initializing faces...");
		boolean hasTexture = true;
		Collection<QuadricFace> faces = new HashSet<>();
		for (Face f : inputModel.getFaces()) {
			faces.add(new QuadricFace(f));
			hasTexture &= f.hasTexture();
		}

		log.debug("Initializing texture coords...");
		HashMap<Integer, TextureCoords> textureCoords = null;
		if (hasTexture) {
			textureCoords = new HashMap<>();
			for (TextureCoords tex : inputModel.getTextureCoords()) {
				textureCoords.put(tex.getIndex(), tex);
			}
		}

		log.debug("Initializing quadrics...");
		computeInitialQuadricEntries(faces, vertices, hasTexture);

		log.debug("Computing boundary...");
		HashMap<Pair<Integer, Integer>, QuadricEdgeBoundaryInfo> edgeMultiplicities = new HashMap<>();
		boolean modelHasBoundary = computeBoundary(edgeMultiplicities, vertices, faces);

		log.debug("Computing valid pairs...");
		PriorityQueueController heap = addEdgePairs(faces, vertices, modelHasBoundary, edgeMultiplicities, hasTexture);
		edgeMultiplicities.clear(); // clear the object, since is isn't needed any more

		log.debug("Decimating mesh...");

		QuadricAbstractCompressedModelCollector compressedModelCollector = new QuadricCompressedModelCollector(header,
				compressedModelHandler, scale, mins, hasTexture);

		System.gc();

		// iteratively remove vertices
		return decimateMesh(vertices, faces, textureCoords, heap, desiredVertexCount, compressedModelCollector,
				hasTexture);
	}

	private boolean computeBoundary(HashMap<Pair<Integer, Integer>, QuadricEdgeBoundaryInfo> edgeMultiplicity,
			HashMap<Integer, QuadricVertex> vertices, Collection<QuadricFace> faces) {

		for (QuadricFace f : faces) {
			QuadricVertex v0 = vertices.get(f.getVertexIndices()[0]);
			QuadricVertex v1 = vertices.get(f.getVertexIndices()[1]);
			QuadricVertex v2 = vertices.get(f.getVertexIndices()[2]);
			increaseEdgeMultiplicity(edgeMultiplicity, v0, v1, v2);
			increaseEdgeMultiplicity(edgeMultiplicity, v0, v2, v1);
			increaseEdgeMultiplicity(edgeMultiplicity, v1, v2, v0);
		}

		boolean modelHasBoundary = false;

		for (QuadricEdgeBoundaryInfo edge : edgeMultiplicity.values()) {
			if (edge.getOccurences() == 1) {
				double[] penaltyTerm = getPenaltyQuadric(edge);
				edge.getEdgeVertexA().setBoundaryVertex(penaltyTerm);
				edge.getEdgeVertexB().setBoundaryVertex(penaltyTerm);
				modelHasBoundary = true;
			}
		}

		return modelHasBoundary;
	}

	private void increaseEdgeMultiplicity(HashMap<Pair<Integer, Integer>, QuadricEdgeBoundaryInfo> edgeMultiplicity,
			QuadricVertex vertexA, QuadricVertex vertexB, QuadricVertex vertexC) {
		int indA = vertexA.getIndex();
		int indB = vertexB.getIndex();

		Pair<Integer, Integer> edge = new Pair<>(indA <= indB ? indA : indB, indA <= indB ? indB : indA);
		if (edgeMultiplicity.containsKey(edge)) {
			edgeMultiplicity.get(edge).incrementOccurences();
		} else {
			edgeMultiplicity.put(edge, new QuadricEdgeBoundaryInfo(vertexA, vertexB, vertexC));
		}
	}

	private boolean isBoundaryEdge(HashMap<Pair<Integer, Integer>, QuadricEdgeBoundaryInfo> edgeMultiplicity,
			int indexA, int indexB) {
		Pair<Integer, Integer> key = indexA <= indexB ? new Pair<Integer, Integer>(indexA, indexB)
				: new Pair<Integer, Integer>(indexB, indexA);
		return edgeMultiplicity.get(key).getOccurences() == 1;
	}

	private double[] getPenaltyQuadric(QuadricEdgeBoundaryInfo edge) {
		Vector3D a = new Vector3D(edge.getEdgeVertexA().getCoords());
		Vector3D b = new Vector3D(edge.getEdgeVertexB().getCoords());
		Vector3D c = new Vector3D(edge.getVertexC().getCoords());

		Vector3D ab = b.subtract(a);
		Vector3D ac = c.subtract(a);

		/*
		 * construct normal vector lying in the plane of the face and orthogonal to the
		 * edge giving rise to a plane containing the edge and being orthogonal to the
		 * face
		 */
		Vector3D nn = ab.crossProduct((ab.crossProduct(ac)));
		if (nn.getNorm() > 1E-20) {
			Vector3D n = nn.normalize();
			double d = -n.dotProduct(a);

			return new ArrayRealVector(new double[] { n.getX() * n.getX(), n.getY() * n.getY(), n.getZ() * n.getZ(),
					d * d, n.getX() * n.getY(), n.getY() * n.getZ(), n.getZ() * d, n.getX() * n.getZ(), n.getY() * d,
					n.getX() * d }).mapMultiply(config.getTargetsizeBoundaryPenalty()).toArray();
		} else {
			return new double[21];
		}
	}

	private boolean decimateMesh(HashMap<Integer, QuadricVertex> vertices, Collection<QuadricFace> faces,
			HashMap<Integer, TextureCoords> textureCoords, PriorityQueueController heap, Integer[] desiredVertexCount,
			QuadricAbstractCompressedModelCollector compressedModelCollector, boolean hasTexture)
			throws NonManifoldModelException {
		int nextVertexIndex = vertices.size();
		int nextTextureIndex = hasTexture ? textureCoords.size() : 0;
		boolean success = true;

		log.debug(Arrays.toString(desiredVertexCount));

		Arrays.sort(desiredVertexCount, Collections.reverseOrder());
		int currentTargetVerticesIndex = 0;

		if (vertices.size() > desiredVertexCount[desiredVertexCount.length - 1]) {
			while (vertices.size() <= desiredVertexCount[currentTargetVerticesIndex]) {
				++currentTargetVerticesIndex;
			}

			for (int i = vertices.size(); i > desiredVertexCount[desiredVertexCount.length - 1]; --i) {
				QuadricCollapseInfo collapse = heap.pollValid();

				if (collapse == null || !(collapse.getCost() < Double.MAX_VALUE)) {
					return success;
				}

				if (i % 10000 == 0) {
					log.debug("Reduced to " + i + " vertices (" + vertices.size() + ", " + collapse.getCost() + ")");
				}

				Collection<QuadricFace> remainingFaces = collapse.getRemainingFaces();

				// create new texture coords
				int newTexturePartitionCount = 0;
				HashMap<TextureCoords, double[]> texToQuadric = null;
				HashMap<QuadricFace, TextureCoords> faceToTexture = null;

				if (hasTexture) {
					collapse.removeDeletedTextureCoords(textureCoords);

					texToQuadric = collapse.getRemainingTexToQuadric();
					newTexturePartitionCount = 1 + texToQuadric.size(); // add 1 as there is at least one new texture
																		// coord

					--newTexturePartitionCount;
					TextureCoords[] newTextureCoords = new TextureCoords[collapse.getNewTextureCoords().length];
					for (int j = 0; j < newTextureCoords.length; ++j) {
						newTextureCoords[j] = new TextureCoords(nextTextureIndex, collapse.getNewTextureCoords()[j]);
						texToQuadric.put(newTextureCoords[j],
								collapse.getQuadricForNewTexture(collapse.getNewTextureCoords()[j]));
						textureCoords.put(nextTextureIndex, newTextureCoords[j]);
						++nextTextureIndex;
						++newTexturePartitionCount;
					}

					faceToTexture = collapse.getRemainingFaceToTexture();
					for (QuadricFace f : remainingFaces) {
						double[] newRemainingTextureCoords = collapse.getTextureCoordForFaceIfNew(f);
						if (newRemainingTextureCoords != null) {
							boolean found = false;
							for (int j = 0; j < collapse.getNewTextureCoords().length && !found; ++j) {
								if (newRemainingTextureCoords == collapse.getNewTextureCoords()[j]) {
									faceToTexture.put(f, newTextureCoords[j]);
									found = true;
								}
							}

							if (!found) {
								throw new IllegalStateException();
							}
						}
					}
				}

				// create new vertex
				QuadricVertex vertexA = collapse.getVertexA();
				QuadricVertex vertexB = collapse.getVertexB();
				QuadricVertex newVertex = new QuadricVertex(nextVertexIndex, collapse.getContractionTarget(),
						vertexA.getAdditionals(), collapse.getQuadric3(), texToQuadric, faceToTexture, remainingFaces,
						newTexturePartitionCount, vertexA.isAtBoundary() || vertexB.isAtBoundary());

				++nextVertexIndex;

				for (QuadricFace edgeFace : collapse.getEdgeFaces()) {
					edgeFace.unregisterFromAdjacentVertices(vertices);
				}

				vertices.put(newVertex.getIndex(), newVertex);

				vertices.remove(vertexA.getIndex());
				vertices.remove(vertexB.getIndex());

				// update faces
				faces.removeAll(collapse.getEdgeFaces());

				for (QuadricFace f : collapse.getRemainingFaces()) {
					f.registerVertexContraction(vertexA.getIndex(), newVertex.getIndex(), newVertex);
					f.registerVertexContraction(vertexB.getIndex(), newVertex.getIndex(), newVertex);

					if (hasTexture) {
						TextureCoords texCoordA = vertexA.getTextureCoordForFace(f);
						TextureCoords texCoordB = vertexB.getTextureCoordForFace(f);

						if (texCoordA != null) {
							f.registerTextureContraction(texCoordA.getIndex(), faceToTexture.get(f));
						} else if (texCoordB != null) {
							f.registerTextureContraction(texCoordB.getIndex(), faceToTexture.get(f));
						} else {
							throw new IllegalStateException();
						}
					}
				}

				List<QuadricCollapseInfo> temp = new LinkedList<>();

				// update collapse candidates
				HashMap<Integer, QuadricCollapseInfo> addedPartners = new HashMap<>();

				for (QuadricCollapseInfo c : vertexA.getCollapseCandidates()) {
					QuadricVertex otherVertex = (vertexA == c.getVertexA()) ? c.getVertexB() : c.getVertexA();
					if (otherVertex != vertexB) {
						QuadricCollapseInfo newCandidate = new QuadricCollapseInfo(otherVertex, newVertex,
								c.isAtBoundary(), hasTexture, config);
						temp.add(newCandidate);
						newVertex.addCollapseCandidate(newCandidate);
						otherVertex.replaceCollapseCandidate(c, newCandidate);

						heap.remove(c);
						c.untieRelations();

						heap.add(newCandidate);
						addedPartners.put(otherVertex.getIndex(), newCandidate);
					}
				}

				for (QuadricCollapseInfo c : vertexB.getCollapseCandidates()) {
					QuadricVertex otherVertex = (vertexB == c.getVertexB()) ? c.getVertexA() : c.getVertexB();
					if (otherVertex != vertexA) {

						QuadricCollapseInfo newCandidate;
						if (!addedPartners.containsKey(otherVertex.getIndex())) {
							newCandidate = new QuadricCollapseInfo(otherVertex, newVertex, c.isAtBoundary(), hasTexture,
									config);
							temp.add(newCandidate);
							heap.add(newCandidate);
						} else {
							newCandidate = addedPartners.get(otherVertex.getIndex());
						}

						otherVertex.replaceCollapseCandidate(c, newCandidate);
						newVertex.addCollapseCandidate(newCandidate);
						heap.remove(c);
						c.untieRelations();
					}
				}

				// if current size is contained in desired compression levels store the current
				// intermediate result
				if (i - 1 == desiredVertexCount[currentTargetVerticesIndex]) {
					success &= compressedModelCollector.storeCompressedModel(vertices, faces, textureCoords,
							desiredVertexCount[currentTargetVerticesIndex]);
					++currentTargetVerticesIndex;
				}

				vertexA.untieRelations();
				vertexB.untieRelations();

				collapse.untieRelations();

				HashSet<Integer> newNeighbourVertices = new HashSet<>();
				for (QuadricFace f : newVertex.getAdjacentFaces()) {
					newNeighbourVertices.add(f.getVertexIndices()[0]);
					newNeighbourVertices.add(f.getVertexIndices()[1]);
					newNeighbourVertices.add(f.getVertexIndices()[2]);
				}

				newNeighbourVertices.remove(newVertex.getIndex());
				for (Integer v1 : newNeighbourVertices) {
					for (Integer v2 : newNeighbourVertices) {
						if (v1 != v2) {
							heap.checkCandidate(v1, v2);
						}
					}
				}
			}
		}

		return success;
	}

	private void computeInitialQuadricEntries(Collection<QuadricFace> faces, HashMap<Integer, QuadricVertex> vertices,
			final boolean hasTexture) throws InvalidAlgorithmParameterException {
		try {
			for (QuadricFace f : faces) {
				double[] quadric5 = hasTexture ? QuadricUtils.computeQuadric5ForFace(f) : null;
				double[] quadric3 = QuadricUtils.computeQuadric3ForFace(f);

				for (int i = 0; i < f.getVertexIndices().length; ++i) {
					int index = f.getVertexIndices()[i];
					QuadricVertex v = vertices.get(index);
					v.registerAdjacentFace(f, hasTexture ? f.getTextureCoords()[i] : null, quadric3, quadric5,
							hasTexture);
				}
			}
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
			throw new InvalidAlgorithmParameterException("Given model contains non-triangular faces");
		}
	}

	private PriorityQueueController addEdgePairs(Collection<QuadricFace> faces,
			HashMap<Integer, QuadricVertex> vertices, boolean modelHasBoundary,
			HashMap<Pair<Integer, Integer>, QuadricEdgeBoundaryInfo> edgeMultiplicities, boolean modelHasTexture)
			throws NonManifoldModelException {

		PriorityQueueController pairs = new PriorityQueueController();
		HashSet<Pair<Integer, Integer>> addedPairs = new HashSet<>();

		int i = 0;
		for (Face f : faces) {
			++i;

			if (i % 10000 == 0) {
				log.debug(i + " / " + faces.size());
			}

			Vertex[] faceVertices = f.getVertices().clone();
			Arrays.sort(faceVertices, new VertexIndexComparator());
			QuadricVertex v0 = vertices.get(faceVertices[0].getIndex());
			QuadricVertex v1 = vertices.get(faceVertices[1].getIndex());
			QuadricVertex v2 = vertices.get(faceVertices[2].getIndex());

			Pair<Integer, Integer> currentPair = new Pair<>(v0.getIndex(), v1.getIndex());
			if (!addedPairs.contains(currentPair)) {
				QuadricCollapseInfo a = new QuadricCollapseInfo(v0, v1,
						modelHasBoundary && isBoundaryEdge(edgeMultiplicities, v0.getIndex(), v1.getIndex()),
						modelHasTexture, config);
				v0.addCollapseCandidate(a);
				v1.addCollapseCandidate(a);
				pairs.add(a);
				addedPairs.add(currentPair);
			}

			currentPair = new Pair<>(v0.getIndex(), v2.getIndex());
			if (!addedPairs.contains(currentPair)) {
				QuadricCollapseInfo b = new QuadricCollapseInfo(v0, v2,
						modelHasBoundary && isBoundaryEdge(edgeMultiplicities, v0.getIndex(), v2.getIndex()),
						modelHasTexture, config);
				v0.addCollapseCandidate(b);
				v2.addCollapseCandidate(b);
				pairs.add(b);
				addedPairs.add(currentPair);
			}

			currentPair = new Pair<>(v1.getIndex(), v2.getIndex());
			if (!addedPairs.contains(currentPair)) {
				QuadricCollapseInfo c = new QuadricCollapseInfo(v1, v2,
						modelHasBoundary && isBoundaryEdge(edgeMultiplicities, v1.getIndex(), v2.getIndex()),
						modelHasTexture, config);
				v1.addCollapseCandidate(c);
				v2.addCollapseCandidate(c);
				pairs.add(c);
				addedPairs.add(currentPair);
			}
		}

		return pairs;
	}
}
