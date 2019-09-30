package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class extends the original @see Vertex implementation by adding some
 * functionality needed for the quadric mesh compression algorithm. The
 * functionality of the base class hasn't been modified, however.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricVertex extends Vertex {
	private double[] quadric3;
	private HashSet<QuadricCollapseInfo> collapseCandidates;
	private HashMap<QuadricFace, TextureCoords> faceToTexture;
	private HashMap<TextureCoords, double[]> texToQuadric5;
	private Collection<QuadricFace> allFaces;
	private int adjacentTexturePartitions;
	private boolean isAtBoundary = false;

	/**
	 * This constructor creates a shallow copy of the given vertex regarding the
	 * index, the coordinates and the additional information. The other information
	 * like quadrics are reset.
	 * 
	 * @param v
	 *            The vertex used as tempalte for the new vertex
	 */
	public QuadricVertex(Vertex v) {
		this(v.getIndex(), v.getCoords(), v.getAdditionals());
	}

	/**
	 * This constructor calls the superclass' constructor passing the given
	 * arguments and thus creates a new vertex with the given values.
	 * 
	 * @param index
	 *            The index of the new vertex
	 * @param coords
	 *            The coordinates of the new vertex as array of length 3
	 * @param additionals
	 *            The additional information attached to the vertex or null of not
	 *            used
	 */
	public QuadricVertex(int index, double[] coords, String[] additionals) {
		super(index, coords, additionals);
		collapseCandidates = new HashSet<>();
		faceToTexture = new HashMap<>();
		texToQuadric5 = new HashMap<>();
		allFaces = new LinkedList<>();
		quadric3 = new double[10];
	}

	/**
	 * This constructor creates a shallow copy of all the given object's properties
	 * including the quadrics. The index however will be replaced by the given one.
	 * 
	 * @param original
	 *            The original vertex used as template for the copy
	 * @param newIndex
	 *            The desired vertex index of the vertex's copy
	 */
	public QuadricVertex(QuadricVertex original, int newIndex) {
		super(newIndex, original.getCoords().clone(), original.getAdditionals().clone());
		collapseCandidates = new HashSet<>(original.collapseCandidates);
		faceToTexture = new HashMap<>(original.faceToTexture);
		texToQuadric5 = new HashMap<>(original.texToQuadric5);
		allFaces = new LinkedList<>(original.allFaces);
		quadric3 = original.quadric3.clone();
	}

	/**
	 * This constructor initializes all of the object's fields including the fields
	 * added by this subclass.
	 * 
	 * @param index
	 *            The index of the new vertex
	 * @param coords
	 *            The coordinates of the new vertex as array of length 3
	 * @param additionals
	 *            The additional information attached to the vertex or null of not
	 *            used
	 * @param quadric3
	 *            The 3-dimensional quadric attached to the vertex
	 * @param texToQuadric5
	 *            A map mapping from texture coordinates to the respective
	 *            5-dimensional quadric attached to this combination of vertex and
	 *            texture coordinates
	 * @param faceToTexture
	 *            A map mapping from faces adjacent to the vertex to the respective
	 *            texture coordinates
	 * @param allFaces
	 *            A list containing all the faces adjacent to the vertex
	 * @param adjacentTexturePartitions
	 *            An integer indicating the count of texture partitions adjacent to
	 *            the vertex
	 * @param isAtBoundary
	 *            A boolean indicating if the vertex lies at a boundary of the model
	 *            (true), otherwise false
	 */
	public QuadricVertex(int index, double[] coords, String[] additionals, double[] quadric3,
			HashMap<TextureCoords, double[]> texToQuadric5, HashMap<QuadricFace, TextureCoords> faceToTexture,
			Collection<QuadricFace> allFaces, int adjacentTexturePartitions, boolean isAtBoundary) {
		super(index, coords, additionals);
		collapseCandidates = new HashSet<>();
		this.quadric3 = quadric3;
		this.texToQuadric5 = texToQuadric5;
		this.faceToTexture = faceToTexture;
		this.allFaces = allFaces;
		this.adjacentTexturePartitions = adjacentTexturePartitions;
		this.isAtBoundary = isAtBoundary;
	}

	/**
	 * This method registers the given face as an adjacent face of the vertex. The
	 * corresponding quadrics are also updated by the given values.
	 * 
	 * @param f
	 *            The face that shall be registered as adjacent face
	 * @param tex
	 *            The texture coordinates that have been assigned this vertex of the
	 *            given face or null for an untextured model
	 * @param quadric3
	 *            The 3-dimensional quadric describing the given face
	 * @param quadric5
	 *            The 5-dimensional quadric describing the given face and the given
	 *            texture coordinates or null for an untextured model
	 * @param hasTexture
	 *            A boolean indicating if the model is textured (true) or not
	 *            (false)
	 */
	public void registerAdjacentFace(QuadricFace f, TextureCoords tex, double[] quadric3, double[] quadric5,
			boolean hasTexture) {
		if (hasTexture) {
			faceToTexture.put(f, tex);

			if (!texToQuadric5.containsKey(tex)) {
				texToQuadric5.put(tex, quadric5);
				++adjacentTexturePartitions;
			} else {
				texToQuadric5.put(tex, QuadricUtils.sumQuadrics(texToQuadric5.get(tex), quadric5));
			}
		}

		this.quadric3 = QuadricUtils.sumQuadrics(this.quadric3, quadric3);
		allFaces.add(f);
	}

	/**
	 * This method adds the given vector to the 3-dimensional quadric stored for
	 * this vertex
	 * 
	 * @param summand
	 *            The array of length 10 that shall be added to the 3-dimensional
	 *            quadric
	 */
	public void addToQuadric3(double[] summand) {
		this.quadric3 = QuadricUtils.sumQuadrics(quadric3, summand);
	}

	/**
	 * Calling this methods marks the vertex as boundary vertex and adds the given
	 * penalty quadric to the 3-dimensional quadric
	 * 
	 * @param summand
	 *            The array of length 10 representing the penalty term that shall be
	 *            added to the 3-dimensional quadric
	 */
	public void setBoundaryVertex(double[] summand) {
		addToQuadric3(summand);
		isAtBoundary = true;
	}

	/**
	 * This method returns a boolean indicating if the vertex has been marked as
	 * boundary vertex.
	 * 
	 * @return Returns true, if the vertex has been marked as boundary vertex,
	 *         otherwise false
	 */
	public boolean isAtBoundary() {
		return isAtBoundary;
	}

	/**
	 * This method removes the given face from the list of adjacent faces. No
	 * quadrics will be updated, however.
	 * 
	 * @param f
	 *            The face that shall be removed from the list of adjacent faces
	 */
	public void unregisterAdjacentFace(QuadricFace f) {
		if (faceToTexture != null)
			faceToTexture.remove(f);
		allFaces.remove(f);
	}

	/**
	 * This method returns the count of adjacent texture partitions.
	 * 
	 * @return Returns the count of adjacent texture partitions
	 */
	public int getAdjacentTexturePartitions() {
		return adjacentTexturePartitions;
	}

	/**
	 * This method registers the given collapse candidate for the vertex.
	 * 
	 * @param collapse
	 *            The collapse candidate that shall be registered for the vertex
	 */
	public void addCollapseCandidate(QuadricCollapseInfo collapse) {
		collapseCandidates.add(collapse);
	}

	/**
	 * This method replaces a previously registered collapse candidate by the given
	 * updated collapse candidate. If the given old collapse candidate hasn't been
	 * registered for this vertex, no modification of the vertex will happen.
	 * 
	 * @param oldCollapse
	 *            The old collapse candidate that shall be replaced
	 * @param newCollapse
	 *            The updated collapse candidate that shall be used as replacement
	 */
	public void replaceCollapseCandidate(QuadricCollapseInfo oldCollapse, QuadricCollapseInfo newCollapse) {
		if (collapseCandidates.remove(oldCollapse)) {
			collapseCandidates.add(newCollapse);
		}
	}

	/**
	 * This method returns the set of all collapse candidates registered for this
	 * vertex
	 * 
	 * @return Returns a set containing all collapse candidates registered for this
	 *         vertex
	 */
	public HashSet<QuadricCollapseInfo> getCollapseCandidates() {
		return collapseCandidates;
	}

	/**
	 * This method clears all sets, maps and lists contained in this object to avoid
	 * cyclic references leading to objects that cannot be cleaned up by the garbage
	 * collection. When calling this method the objects looses its integrity, hence
	 * to further calls to this object shall be performed then.
	 */
	public void untieRelations() {
		collapseCandidates.clear();

		if (faceToTexture != null) {
			faceToTexture.clear();
		}

		if (texToQuadric5 != null) {
			texToQuadric5.clear();
		}

		allFaces.clear();
	}

	/**
	 * This method returns a list containing all faces adjacent to the vertex.
	 * 
	 * @return Returns a list containing all faces adjacent to the vertex
	 */
	public Collection<QuadricFace> getAdjacentFaces() {
		return allFaces;
	}

	/**
	 * This method returns a set containing all the texture coordinates touching
	 * this vertex.
	 * 
	 * @return Returns a set of all texture coordinates touching this vertex
	 */
	public Collection<TextureCoords> getAdjacentTextureCoords() {
		HashSet<TextureCoords> adjacentTextureCoords = new HashSet<>();

		for (QuadricFace f : allFaces) {
			adjacentTextureCoords.add(faceToTexture.get(f));
		}

		return adjacentTextureCoords;
	}

	/**
	 * This method returns the texture coordinates that have been registered for the
	 * vertex and the given face.
	 * 
	 * @param f
	 *            The face, whose texture coordinates with respect to this vertex
	 *            shall be returned
	 * @return The texture coordinates registered for the vertex and the given face;
	 *         if the face hasn't been registered as adjacent to this vertex or the
	 *         model has no texture, null will be returned
	 */
	public TextureCoords getTextureCoordForFace(Face f) {
		return faceToTexture.get(f);
	}

	/**
	 * This method returns the 5-dimensional quadric attached to the combination of
	 * this vertex and the given texture coordinates.
	 * 
	 * @param tex
	 *            The texture coordinates, whose quadric with respect to this vertex
	 *            shall be returned
	 * @return Returns the respective 5-dimensional quadric; if no face using the
	 *         given texture coordinates has been registered as an adjacent face for
	 *         this vertex or the model has no texture, a zero quadric will be returned
	 */
	public double[] getQuadric5SumForTextureCoords(TextureCoords tex) {
		double[] quadric = texToQuadric5.get(tex);
		
		if(quadric == null) {
			return new double[21];
		} else {
			return quadric;
		}
	}

	/**
	 * This method returns a map using the texture coordinates that have been used
	 * by faces registered as adjacent to this vertex as keys and the respective
	 * 5-dimensional quadrics as values.
	 * 
	 * @return Returns a map mapping from texture coordinates to the respective
	 *         5-dimensional quadric
	 */
	public HashMap<TextureCoords, double[]> getTexToQuadric5() {
		return texToQuadric5;
	}

	/**
	 * This method returns a map using the faces that have been registered as
	 * adjacent to this vertex as keys and the respective texture coordinates as
	 * values.
	 * 
	 * @return Returns a map mapping from adjacent faces to the respective texture
	 *         coordinates
	 */
	public HashMap<QuadricFace, TextureCoords> getFaceToTexture() {
		return faceToTexture;
	}

	/**
	 * This method returns the total 3-dimensional quadric attached to this vertex
	 * including potential penalty terms.
	 * 
	 * @return Returns an array of length 10 representing the 3-dimensional quadric
	 *         attached to this vertex
	 */
	public double[] getQuadric3() {
		return quadric3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Integer.hashCode(getIndex());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuadricVertex other = (QuadricVertex) obj;
		if (getIndex() != other.getIndex())
			return false;
		return true;
	}
}
