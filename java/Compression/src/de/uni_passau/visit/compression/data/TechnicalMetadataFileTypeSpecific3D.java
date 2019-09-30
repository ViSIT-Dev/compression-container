package de.uni_passau.visit.compression.data;

import java.io.Serializable;

/**
 * This class represents additional technical meta data for 3D models regarding
 * one specific compression level. These contain the vertex and face count. All
 * properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization.
 * 
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadataFileTypeSpecific3D implements Serializable {
	private static final long serialVersionUID = 951575289266257300L;

	private long vertexCount, faceCount;

	/**
	 * This constructor initializes the object with the given arguments.
	 * 
	 * @param vertexCount The vertex count of a compressed version of a model
	 * @param faceCount The face count of a compressed version of a model
	 */
	public TechnicalMetadataFileTypeSpecific3D(long vertexCount, long faceCount) {
		super();
		this.vertexCount = vertexCount;
		this.faceCount = faceCount;
	}

	/**
	 * This method returns the vertex count of the compressed version of a model.
	 * 
	 * @return Returns the vertex count of the compressed version of a model
	 */
	public long getVertexCount() {
		return vertexCount;
	}

	/**
	 * This method returns the face count of the compressed version of a model.
	 * 
	 * @return Returns the face count of the compressed version of a model
	 */
	public long getFaceCount() {
		return faceCount;
	}
}
