package de.uni_passau.visit.compression.logic.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import de.uni_passau.visit.compression.exceptions.ModelReadException;
import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class offers a static method to read a specific OBJ-file into a
 * triangular model. Faces of higher degree will be automatically split into
 * several triangles.
 * 
 * @author Florian Schlenker
 *
 */
public class ObjReader {

	private static final int DIM = 3;
	private static final int TEXTURE_DIM = 2;
	private static final int NORMAL_DIM = 3;
	private static final int FACE_ORDER = 3;

	/**
	 * This method can be accessed in a static way and can be used to read a given
	 * OBJ-file into a triangular model, where face of higher degree will be
	 * automatically split into several triangles.
	 * 
	 * @param filename
	 *            The filename including (relative or absolute) its path of the
	 *            OBJ-file that shall be read
	 * @param decimalSeparator
	 *            The char used as decimal separator in the OBJ-file (normally '.'
	 *            or ',')
	 * @return Returns a triangular model of type @see ObjModel
	 * @throws FileNotFoundException
	 *             if the specified file could not be found
	 * @throws ModelReadException
	 *             if there occurred an error while parsing the OBJ-file
	 */
	public static ObjModel read(String filename, String decimalSeparator)
			throws FileNotFoundException, ModelReadException {
		File in = new File(filename);
		Scanner s = new Scanner(in);

		String header = "";
		int lastVertexIndex = 0;
		int lastNormalIndex = 0;
		int lastTextureCoordIndex = 0;
		int lastFaceIndex = 0;

		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Normal> normals = new ArrayList<Normal>();
		ArrayList<TextureCoords> textureCoords = new ArrayList<TextureCoords>();
		ArrayList<Face> faces = new ArrayList<Face>();

		String line;
		String currentMaterial = "";
		int lineNum = 0;
		while (s.hasNextLine()) {
			++lineNum;

			line = s.nextLine();
			int commentStart = line.indexOf("#");

			if (commentStart >= 0) {
				line = line.substring(0, commentStart);
			}

			line = line.trim();

			// OBJ-files use 1-indices, this system/algorithm 0-indices. Hence a conversion
			// is necessary

			if (!line.equals("")) {
				String[] tokens = line.split(" +"); // regex matching also for more than one space
				if (tokens[0].equals(ObjModel.MTL_DECLARATION_PREFIX)) {
					header = header + line + System.lineSeparator();
				} else if (tokens[0].equals("v")) {
					if (tokens.length < DIM + 1) {
						s.close();
						throw new ModelReadException("Too few vertex tokens in line " + lineNum);
					} else {
						String[] additionals = Arrays.copyOfRange(tokens, DIM + 1, tokens.length);
						vertices.add(
								new Vertex(
										lastVertexIndex, new double[] { Double.parseDouble(tokens[1]),
												Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]) },
										additionals));
					}
					++lastVertexIndex;
				} else if (tokens[0].equals("vt")) {
					if (tokens.length < TEXTURE_DIM + 1) {
						s.close();
						throw new ModelReadException("Too few texture tokens in line " + lineNum);
					} else {
						textureCoords.add(new TextureCoords(lastTextureCoordIndex,
								new double[] { Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]) }));
					}
					++lastTextureCoordIndex;
				} else if (tokens[0].equals("vn")) {
					if (tokens.length < NORMAL_DIM + 1) {
						s.close();
						throw new ModelReadException("Too few normal tokens in line " + lineNum);
					} else {
						normals.add(new Normal(lastNormalIndex, new double[] { Double.parseDouble(tokens[1]),
								Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]) }));
					}
					++lastNormalIndex;
				} else if (tokens[0].equals("usemtl")) {
					if (tokens.length >= 2) {
						currentMaterial = tokens[1];
					}
				} else if (tokens[0].equals("f")) {
					if (tokens.length >= FACE_ORDER + 1) {
						int faceOrder = tokens.length - 1;
						String[][] indices = new String[faceOrder][3];
						boolean hasTextureCoords = true;
						boolean hasNormal = true;

						for (int i = 1; i < faceOrder + 1; ++i) {
							String[] currentIndices = tokens[i].split("/");
							indices[i - 1] = currentIndices;

							if (currentIndices.length < 3) {
								hasNormal = false;
							}

							if (currentIndices.length < 2 || currentIndices[1].equals("")) {
								hasTextureCoords = false;
							}
						}
						
						hasNormal = false; // TODO remove

						Vertex[] currentVertices = new Vertex[faceOrder];
						TextureCoords[] currentTextureCoords = null;
						Normal[] currentNormals = null;

						for (int i = 0; i < faceOrder; ++i) {
							currentVertices[i] = vertices.get(Integer.parseInt(indices[i][0]) - 1);
						}

						if (hasTextureCoords) {
							currentTextureCoords = new TextureCoords[faceOrder];
							for (int i = 0; i < faceOrder; ++i) {
								currentTextureCoords[i] = textureCoords.get(Integer.parseInt(indices[i][1]) - 1);
							}
						}

						if (hasNormal) {
							currentNormals = new Normal[faceOrder];
							for (int i = 0; i < faceOrder; ++i) {
								currentNormals[i] = normals.get(Integer.parseInt(indices[i][2]) - 1);
							}
						}

						if (tokens.length == FACE_ORDER + 1) {
							faces.add(new Face(lastFaceIndex, currentVertices, currentNormals, currentTextureCoords,
									new String[1], currentMaterial));
						} else {
							lastFaceIndex = handleHigherDegreeFace(lastFaceIndex, currentVertices, currentNormals,
									currentTextureCoords, new String[1], currentMaterial, faces);
						}
					} else {
						s.close();
						throw new ModelReadException("Given model contains at least one non-triangular face in line "
								+ lineNum + ". Face order: " + (tokens.length - 1) + "; Line: " + line);
					}
					++lastFaceIndex;
				}
			}
		}

		s.close();
		return new ObjModel(vertices, normals, textureCoords, faces, header);
	}

	/**
	 * This method replaces a face with a degree of at least 4 by non-optimized
	 * triangles. The face is assumed to be convex, otherwise inverted triangles can
	 * occur.
	 * 
	 * @param lastFaceIndex
	 *            The index of the last face that has been added to the face
	 *            collection
	 * @param vertices
	 *            The set of vertices defining the high-order face
	 * @param normals
	 *            The set of normals assigned to the high-order face's vertices or
	 *            null, if no normals were given
	 * @param textureCoords
	 *            The set of texture coordinates assigned to the high-order face's
	 *            vertices or null, if no texture coordinates were given
	 * @param additionals
	 *            The additional information stored with the high-order face that
	 *            will be copied to every triangular face or null, if no such
	 *            information was given
	 * @param material
	 *            The material of the high-order face that will be copied to every
	 *            triangular face
	 * @param faces
	 *            The collection of faces the new triangular faces shall be added to
	 * @return Returns the index of the last face that has been added to the
	 *         faces-collection.
	 */
	private static int handleHigherDegreeFace(int lastFaceIndex, Vertex[] vertices, Normal[] normals,
			TextureCoords[] textureCoords, String[] additionals, String material, Collection<Face> faces) {
		final int triangleCount = vertices.length - 2;
		final int len = vertices.length;

		for (int i = 0; i < (triangleCount >> 1); ++i) {
			faces.add(new Face(lastFaceIndex, getVertexSubset(vertices, len - 1 - i, i, i + 1),
					getNormalSubset(normals, len - 1 - i, i, i + 1),
					getTextureCoordsSubset(textureCoords, len - 1 - i, i, i + 1), additionals, material));
			++lastFaceIndex;
			faces.add(new Face(lastFaceIndex, getVertexSubset(vertices, len - 2 - i, len - 1 - i, i + 1),
					getNormalSubset(normals, len - 2 - i, len - 1 - i, i + 1),
					getTextureCoordsSubset(textureCoords, len - 2 - i, len - 1 - i, i + 1), additionals, material));
			++lastFaceIndex;
		}

		if (triangleCount % 2 == 1) {
			faces.add(new Face(lastFaceIndex, getVertexSubset(vertices, len / 2 - 1, len / 2 + 1, len / 2),
					getNormalSubset(normals, len / 2 - 1, len / 2 + 1, len / 2),
					getTextureCoordsSubset(textureCoords, len / 2 - 1, len / 2 + 1, len / 2), additionals, material));
			++lastFaceIndex;
		}

		return lastFaceIndex;
	}

	private static Vertex[] getVertexSubset(Vertex[] vertices, int a, int b, int c) {
		return new Vertex[] { vertices[a], vertices[b], vertices[c] };
	}

	private static TextureCoords[] getTextureCoordsSubset(TextureCoords[] tex, int a, int b, int c) {
		if (tex == null)
			return null;
		return new TextureCoords[] { tex[a], tex[b], tex[c] };
	}

	private static Normal[] getNormalSubset(Normal[] normals, int a, int b, int c) {
		if (normals == null)
			return null;
		return new Normal[] { normals[a], normals[b], normals[c] };
	}

}
