package de.uni_passau.visit.compression.logic.io;

import java.util.ArrayList;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This interface represents an 3D-object consisting of a header, vertices,
 * faces and optional texture coordinates and normals.
 * 
 * @author Florian Schlenker
 *
 */
public interface AbstractModel {
	/**
	 * This method returns the list containing all the model's vertices.
	 * 
	 * @return A reference to the model's vertex list
	 */
	public ArrayList<Vertex> getVertices();

	/**
	 * This method returns the list containing all the model's normals.
	 * 
	 * @return A reference to the model's normals or null, if no normals have been
	 *         passed to the constructor
	 */
	public ArrayList<Normal> getNormals();

	/**
	 * This method returns the list containing all the model's texture coordinates.
	 * 
	 * @return A reference to the model's texture coordinates or null, if no texture
	 *         coordinates have been passed to the constructor
	 */
	public ArrayList<TextureCoords> getTextureCoords();

	/**
	 * This method returns the list containing all the model's faces.
	 * 
	 * @return A reference to the model's faces
	 */
	public ArrayList<Face> getFaces();

	/**
	 * This method returns the model's header
	 * 
	 * @return Returns the model's header as string
	 */
	public String getHeader();
}
