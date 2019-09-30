package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.logic.data.TextureCoords;

/**
 * This class represents a collapse candidate of two vertices and incorporates
 * all functionality for the computation of the collapse's cost and the position
 * of its resulting target vertex.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricCollapseInfo implements Comparable<QuadricCollapseInfo> {

	private static final Logger log = LogManager.getLogger(QuadricCollapseInfo.class);
	
	private final QuadricVertex a, b;
	private double cost;
	private double[] geoTarget;
	private double[] quadric3;

	private Collection<QuadricFace> edgeFaces;
	private ArrayList<TextureCoords> aTexs, bTexs;
	private double[][] quadric5s;
	private double[][] textureTargets;
	private int edgeTexturePairCount = 0;
	private final boolean isAtBoundary, modelHasTexture;
	private final QuadricEdgeCollapseConfig config;

	/**
	 * This constructor creates a new collapse candidate between the given set of
	 * vertices. The index of the first given vertex has to be smaller than the
	 * index of the second vertex.
	 * 
	 * @param a
	 *            The first of the two vertices that would be collapsed by this
	 *            candidate, with the smaller index
	 * @param b
	 *            The second of the two vertices that would be collapsed by this
	 *            candidate, with the greater index
	 * @param isAtBoundary
	 *            Has to be true, if the edge formed by the two given vertices lies
	 *            at a boundary of the model
	 * @param modelHasTexture
	 *            Should be true, if the model contains texture information, has to
	 *            be false otherwise
	 * @param config
	 *            The configuration object specifying the settings used for the new
	 *            instance
	 */
	public QuadricCollapseInfo(QuadricVertex a, QuadricVertex b, boolean isAtBoundary, boolean modelHasTexture,
			QuadricEdgeCollapseConfig config) {
		this.config = config;
		this.a = a;
		this.b = b;
		this.isAtBoundary = isAtBoundary;
		this.modelHasTexture = modelHasTexture;
		computeCostAndTarget();
	}

	/**
	 * This method computes the cost of the collapse and the contraction target. Two
	 * different penalization systems have to be used for texturized models here.
	 * The 5-dimensional quadric is used, when only one texture partition is
	 * adjacent to the collapse edge and neither of the collapse vertices lies at a
	 * boundary. These two approaches resemble the two different papers used as
	 * basis for this algorithm (see @see QuadricEdgeCollapse for further
	 * information)
	 */
	private void computeCostAndTarget() {
		int edgeFaceCount = getFacesAtEdge();
		if (modelHasTexture)
			edgeTexturePairCount = getTextureCoordsAtEdge();

		if (edgeFaceCount > 2) {
			log.debug("Given model contains non-manifold edge.");
		} else if(edgeFaceCount == 0) {
			log.debug("Given model contains degenerate faces.");
		}

		quadric5s = new double[edgeTexturePairCount][21];
		textureTargets = new double[edgeTexturePairCount][2];

		double cost = 0.0;
		quadric3 = QuadricUtils.sumQuadrics(a.getQuadric3(), b.getQuadric3());
		if (edgeFaceCount == 0 || edgeTexturePairCount > 1 || a.isAtBoundary() || b.isAtBoundary() || !modelHasTexture) {
			/*
			 * use quadric3 for edges along multiple texture partitions and for boundary
			 * vertices, as the penalization quadric is only added to quadric3
			 */
			geoTarget = QuadricUtils.getMinimumForQuadric3(quadric3, a, b).toArray();

			/*
			 * now compute cost for each texture partition separately and sum them up
			 */
			if (modelHasTexture) {
				for (int i = 0; i < aTexs.size(); ++i) {
					cost += computeCostForMultipleTexturePartitions(i, geoTarget);
				}
			} else {
				cost += QuadricUtils.getCostForQuadric3(quadric3, geoTarget);
			}
		} else {
			/* otherwise use quadric5 */
			quadric5s[0] = QuadricUtils.sumQuadrics(a.getQuadric5SumForTextureCoords(aTexs.get(0)),
					b.getQuadric5SumForTextureCoords(bTexs.get(0)));
			RealVector target = QuadricUtils.getMinimumForQuadric5(quadric5s[0], a, b, aTexs.get(0).getCoords(),
					bTexs.get(0).getCoords());
			geoTarget = target.getSubVector(0, 3).toArray();
			textureTargets[0] = target.getSubVector(3, 2).toArray();
			cost += QuadricUtils.getCostOfContraction(quadric5s[0], target);
		}

		if (modelHasTexture) {
			/*
			 * cost for texture partitions, which only touch one of the contracted vertices,
			 * but have no partition border at the contracted edge
			 */
			for (TextureCoords tex : a.getAdjacentTextureCoords()) {
				if (!aTexs.contains(tex)) {
					cost += QuadricUtils.getCostOfContraction(a.getQuadric5SumForTextureCoords(tex), geoTarget,
							tex.getCoords());
				}
			}

			for (TextureCoords tex : b.getAdjacentTextureCoords()) {
				if (!bTexs.contains(tex)) {
					cost += QuadricUtils.getCostOfContraction(b.getQuadric5SumForTextureCoords(tex), geoTarget,
							tex.getCoords());
				}
			}

			/*
			 * penalize contractions at texture borders, since these contractions
			 * necessarily cause inaccuracies regarding the texture segment borders
			 */
			double partitionPenalization = config.getTargetsizePartitionPenalizationFactor()
					* (a.getAdjacentTexturePartitions() * a.getAdjacentTexturePartitions()
							+ b.getAdjacentTexturePartitions() + b.getAdjacentTexturePartitions() - 1); //

			this.cost = cost * getNormalsAndQualityPenalizationFactor() * partitionPenalization;
		} else {
			this.cost = cost * getNormalsAndQualityPenalizationFactor();
		}
		
		if(this.cost < 0) {
			this.cost = Double.MAX_VALUE;
		}

		if(edgeFaceCount == 0) {
			this.cost = 0;
		}
	}

	/**
	 * This method computes the cost that the texture partition with the given index
	 * inflicts on the given geometric target as well as the texture coordinate
	 * target for this partition.
	 * 
	 * @param i
	 *            The index of the texture partition that shall be considered
	 * @param geoTarget
	 *            The geometric target of the contraction
	 * @return Returns the cost inflicted on the geometry target by the considered
	 *         texture partition
	 */
	private double computeCostForMultipleTexturePartitions(int i, double[] geoTarget) {
		TextureCoords texA = aTexs.get(i);
		TextureCoords texB = bTexs.get(i);
		double[] quadric5 = QuadricUtils.sumQuadrics(a.getQuadric5SumForTextureCoords(texA),
				b.getQuadric5SumForTextureCoords(texB));
		RealVector target = QuadricUtils.getGeoContrainedMinimumForQuadric5(quadric5, geoTarget, texA, texB);

		quadric5s[i] = quadric5;
		textureTargets[i] = target.getSubVector(3, 2).toArray();

		return QuadricUtils.getCostOfContraction(quadric5, target);
	}

	/**
	 * This method finds all faces containing the collapse edge and stores them.
	 * 
	 * @return Returns the count of faces containing the collapse edge
	 */
	private int getFacesAtEdge() {
		edgeFaces = new LinkedList<QuadricFace>();

		int count = 0;
		for (QuadricFace f : a.getAdjacentFaces()) {
			if (b.getAdjacentFaces().contains(f)) {
				edgeFaces.add(f);
				++count;
			}
		}

		return count;
	}

	/**
	 * This method finds all the distinct pairs of texture coordinates of faces
	 * along the collapse edge and stores them.
	 * 
	 * @return Returns the count of distinct texture coordinate pairs.
	 */
	private int getTextureCoordsAtEdge() {
		aTexs = new ArrayList<TextureCoords>(edgeFaces.size());
		bTexs = new ArrayList<TextureCoords>(edgeFaces.size());

		for (QuadricFace f : edgeFaces) {
			TextureCoords aTex = a.getTextureCoordForFace(f);
			TextureCoords bTex = b.getTextureCoordForFace(f);

			if (!aTexs.contains(aTex) || !bTexs.contains(bTex)) {
				aTexs.add(aTex);
				bTexs.add(bTex);
			}
		}

		return aTexs.size();
	}

	/**
	 * This method returns the lower-index vertex of the two vertices defining this
	 * collapse candidate.
	 * 
	 * @return Returns the lower-index vertex of the two vertices defining this
	 *         collapse candidate
	 */
	public QuadricVertex getVertexA() {
		return a;
	}

	/**
	 * This method returns the higher-index vertex of the two vertices defining this
	 * collapse candidate.
	 * 
	 * @return Returns the higher-index vertex of the two vertices defining this
	 *         collapse candidate
	 */
	public QuadricVertex getVertexB() {
		return b;
	}

	/**
	 * This method returns a value that indicates, if the collapse edge is a
	 * boundary edge.
	 * 
	 * @return Returns true, if the edge is a boundary edge, otherwise false
	 */
	public boolean isAtBoundary() {
		return isAtBoundary;
	}

	/**
	 * This method returns the geometric contraction target of the edge collapse
	 * described by this candidate.
	 * 
	 * @return Returns the coordinates of the geometric contraction target of this
	 *         candidate
	 */
	public double[] getContractionTarget() {
		return geoTarget;
	}

	/**
	 * This method returns the total cost of the edge contraction described by this
	 * collapse candidate.
	 * 
	 * @return Returns the total cost of this collapse candidate
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * This method returns a collection containing all faces adjacent to at least
	 * one of the collapse's vertices that are still present after performing the
	 * edge collapse operation.
	 * 
	 * @return Returns a collection containing all faces that are still present
	 *         after performing the edge collapse operation
	 */
	public Collection<QuadricFace> getRemainingFaces() {
		Collection<QuadricFace> faces = new HashSet<>();
		faces.addAll(a.getAdjacentFaces());
		faces.addAll(b.getAdjacentFaces());
		faces.removeAll(edgeFaces);
		return faces;
	}

	/**
	 * This method returns a collection consisting of all faces containing the edge
	 * that would be collapsed by this candidate and hence would get degenerated by
	 * the collapse.
	 * 
	 * @return Returns a collection consisting of all faces containing the edge that
	 *         would be collapsed by this candidate
	 */
	public Collection<QuadricFace> getEdgeFaces() {
		return edgeFaces;
	}

	/**
	 * This method returns the texture coordinate targets for each of the texture
	 * partitions adjacent to the collapsed edge.
	 * 
	 * @return Returns a nested array, where each element of the superordinated
	 *         array contains the coordinates of the texture target of one texture
	 *         partition involved in the collapse.
	 */
	public double[][] getNewTextureCoords() {
		return textureTargets;
	}

	/**
	 * This method returns a collection consisting of the texture coordinates at
	 * both vertices with respect to all texture partitions adjacent to the
	 * collapsed edge. These texture coordinates are replaced with one set of target
	 * texture coordinates for each texture partition when performing the collapse.
	 * Thus the coordinates returned by this function can be removed after the edge
	 * collapse.
	 * 
	 * @return Returns a collection consisting of the texture coordinates at both
	 *         vertices with respect to all texture partitions adjacent to the
	 *         collapse edge
	 */
	public Collection<TextureCoords> getDeletedTextureCoords() {
		Collection<TextureCoords> tex = new HashSet<>();

		for (int i = 0; i < aTexs.size(); ++i) {
			tex.add(aTexs.get(i));
		}

		for (int i = 0; i < bTexs.size(); ++i) {
			tex.add(bTexs.get(i));
		}

		return tex;
	}

	/**
	 * If the given face contains the collapse edge, this method returns the texture
	 * target after the edge collapse with respect to this face. Otherwise no
	 * texture target has been computed for this face and null will be returned.
	 * 
	 * @param face
	 *            The texture target with respect to this face will be returned
	 * @return The texture target with respect to the given face, if it contains the
	 *         collapse edge, null otherwise
	 */
	public double[] getTextureCoordForFaceIfNew(QuadricFace face) {
		TextureCoords tex = a.getTextureCoordForFace(face);
		if (tex != null) {
			int index = aTexs.indexOf(tex);
			if (index >= 0) {
				return textureTargets[index];
			} else {
				return null;
			}
		}

		tex = b.getTextureCoordForFace(face);
		if (tex != null) {
			int index = bTexs.indexOf(tex);
			if (index >= 0) {
				return textureTargets[index];
			} else {
				return null;
			}
		}

		throw new IllegalStateException();
	}

	/**
	 * This method returns the updated 5-dimensional quadric of the resulting vertex
	 * after performing the collapse with respect to the given texture coordinates.
	 * 
	 * @param tex
	 *            The texture coordinates, with respect to which one wants to
	 *            retrieve the quadric
	 * @return The updated 5-dimensional quadric of the resulting vertex with
	 *         respect to the given texture coordinates
	 */
	public double[] getQuadricForNewTexture(double[] tex) {
		for (int i = 0; i < textureTargets.length; ++i) {
			if (textureTargets[i] == tex) {
				return quadric5s[i];
			}
		}

		throw new IllegalStateException();
	}

	/**
	 * This method returns the updated 3-dimensional quadric of the resulting vertex
	 * after performing the collapse.
	 * 
	 * @return The updated 3-dimensional quadric of the resulting vertex
	 */
	public double[] getQuadric3() {
		return quadric3;
	}

	/**
	 * This method computes and returns a data structure mapping from texture
	 * coordinates, which are adjacent to one of the two vertices and still present
	 * after performing the collapse, to the respective 5-dimensional quadric.
	 * 
	 * @return Returns a map with texture coordinates as key and the respective
	 *         5-dimensional quadrics as values.
	 */
	public HashMap<TextureCoords, double[]> getRemainingTexToQuadric() {
		HashMap<TextureCoords, double[]> texToQuadric = new HashMap<>(a.getTexToQuadric5());
		texToQuadric.putAll(b.getTexToQuadric5());

		for (TextureCoords tex : getDeletedTextureCoords()) {
			texToQuadric.remove(tex);
		}

		return texToQuadric;
	}

	/**
	 * This method computes and returns a data structure mapping from faces, which
	 * are adjacent to exactly one of the two vertices (and hence are not
	 * degenerated after the collapse), to the respective texture coordinates at the
	 * contracted vertex.
	 * 
	 * @return Returns a map with faces as key and the respective texture
	 *         coordinates as values.
	 */
	public HashMap<QuadricFace, TextureCoords> getRemainingFaceToTexture() {
		HashMap<QuadricFace, TextureCoords> faceToTexture = new HashMap<>(a.getFaceToTexture());
		faceToTexture.putAll(b.getFaceToTexture());

		for (QuadricFace f : edgeFaces) {
			faceToTexture.remove(f);
		}

		return faceToTexture;
	}

	/**
	 * This method removes all texture coordinates, which are dispensable after
	 * performing the collapse, from the given map, which maps these texture
	 * coordinates indices to the texture coordinates itself.
	 * 
	 * @param texs
	 *            The map using texture coordinates as values and their respective
	 *            indices as keys, from which the dispensable texture coordinates
	 *            shall be removed
	 */
	public void removeDeletedTextureCoords(HashMap<Integer, TextureCoords> texs) {
		for (TextureCoords tex : aTexs) {
			texs.remove(tex.getIndex());
		}

		for (TextureCoords tex : bTexs) {
			texs.remove(tex.getIndex());
		}
	}

	/**
	 * This method computes and returns a penalization factor depending on the
	 * resulting face's quality and the deviation of the face's normals.
	 * 
	 * @return Returns the resulting penalization factor as double-value
	 */
	private double getNormalsAndQualityPenalizationFactor() {
		double minNormalDiff = Double.MAX_VALUE;
		double minQuality = Double.MAX_VALUE;

		for (QuadricFace f : getRemainingFaces()) {
			try {
				Vector3D p0Old = new Vector3D(f.getVertices()[0].getCoords());
				Vector3D p1Old = new Vector3D(f.getVertices()[1].getCoords());
				Vector3D p2Old = new Vector3D(f.getVertices()[2].getCoords());
				Vector3D p0New = new Vector3D(
						(f.getVertices()[0] != a && f.getVertices()[0] != b) ? f.getVertices()[0].getCoords()
								: geoTarget);
				Vector3D p1New = new Vector3D(
						(f.getVertices()[1] != a && f.getVertices()[1] != b) ? f.getVertices()[1].getCoords()
								: geoTarget);
				Vector3D p2New = new Vector3D(
						(f.getVertices()[2] != a && f.getVertices()[2] != b) ? f.getVertices()[2].getCoords()
								: geoTarget);

				Vector3D normalOld = p1Old.subtract(p0Old).crossProduct(p2Old.subtract(p0Old)).normalize();
				Vector3D normalNew = p1New.subtract(p0New).crossProduct(p2New.subtract(p0New)).normalize();
				double normalDifference = normalOld.dotProduct(normalNew);

				if (normalDifference <= minNormalDiff) {
					minNormalDiff = normalDifference;
				}

				Vector3D diff10 = p1New.subtract(p0New);
				Vector3D diff20 = p2New.subtract(p0New);
				Vector3D diff12 = p1New.subtract(p2New);
				double area = diff10.crossProduct(diff20).getNorm();
				double maxEdge = diff10.dotProduct(diff10);
				double temp = diff20.dotProduct(diff20);
				if (temp > maxEdge)
					maxEdge = temp;
				temp = diff12.dotProduct(diff12);
				if (temp > maxEdge)
					maxEdge = temp;

				double quality;
				if (maxEdge < 1E-30) {
					quality = 0;
				} else {
					quality = area / maxEdge;
				}

				if (quality <= minQuality) {
					minQuality = quality;
				}
			} catch (MathArithmeticException ex) {
				// ignore degenerate triangles
			}
		}

		if (minQuality > config.getTargetsizeQualityThreshold())
			minQuality = config.getTargetsizeQualityThreshold();

		return (minNormalDiff < config.getTargetsizeNormalDifferenceThreshold()
				? config.getTargetsizeNormalPenalization()
				: 1.0) / minQuality;
	}

	/**
	 * This method unties links to other objects and therefore avoids cyclic
	 * references, which cannot be resolved by the garbage collector. After calling
	 * this method, the object's integrity is not given any more.
	 */
	public void untieRelations() {
		edgeFaces.clear();
		if (aTexs != null)
			aTexs.clear();
		if (bTexs != null)
			bTexs.clear();

		// the following lines should not be necessary, nevertheless keep them here for
		// the moment
		textureTargets = null;
		quadric5s = null;
		quadric3 = null;
		geoTarget = null;
	}

	@Override
	public int compareTo(QuadricCollapseInfo o) {
		return Double.valueOf(cost).compareTo(o.cost);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
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
		QuadricCollapseInfo other = (QuadricCollapseInfo) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		}

		if (b == null) {
			if (other.b != null)
				return false;
		}

		if (a != null && b != null) {
			if ((a.getIndex() == other.a.getIndex() && b.getIndex() == other.b.getIndex())
					|| (a.getIndex() == other.b.getIndex() && b.getIndex() == other.a.getIndex())) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		return "QuadricCollapseInfo [a=" + a.getIndex() + ", b=" + b.getIndex() + ", cost=" + cost + ", geoTarget="
				+ Arrays.toString(geoTarget) + "]";
	}

}
