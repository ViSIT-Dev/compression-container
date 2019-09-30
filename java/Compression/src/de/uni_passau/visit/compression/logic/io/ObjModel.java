package de.uni_passau.visit.compression.logic.io;

import java.util.ArrayList;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class implements the @see AbstractModel interface and represents a
 * 3D-model read from an OBJ-file.
 * 
 * @author Florian Schlenker
 *
 */
public class ObjModel implements AbstractModel {
	public static final String MTL_DECLARATION_PREFIX = "mtllib";
	public static final String MTL_TEXTURE_IMPORT_PREFIX = "map_Kd";

	private final ArrayList<Vertex> vertices;
	private final ArrayList<Normal> normals;
	private final ArrayList<TextureCoords> textureCoords;
	private final ArrayList<Face> faces;
	private final String header;

	/**
	 * This constructor creates a new OBJ-model with the properties given by the
	 * constructor's arguments. The given arguments will not be cloned, only the
	 * references will be stored.
	 * 
	 * @param vertices
	 *            A list containing all the model's vertices
	 * @param normals
	 *            A list containing all the model's normals or null, if no normals
	 *            were given
	 * @param textureCoords
	 *            A list containing all the model's texture coordinates or null, if
	 *            no normals were given
	 * @param faces
	 *            A list containing all the model's faces
	 * @param header
	 *            The header of the OBJ-file that has been read to create this model
	 */
	public ObjModel(ArrayList<Vertex> vertices, ArrayList<Normal> normals, ArrayList<TextureCoords> textureCoords,
			ArrayList<Face> faces, String header) {
		super();
		this.vertices = vertices;
		this.normals = normals;
		this.textureCoords = textureCoords;
		this.faces = faces;
		this.header = header;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public ArrayList<Normal> getNormals() {
		return normals;
	}

	public ArrayList<TextureCoords> getTextureCoords() {
		return textureCoords;
	}

	public ArrayList<Face> getFaces() {
		return faces;
	}

	public String getHeader() {
		return header;
	}
}
