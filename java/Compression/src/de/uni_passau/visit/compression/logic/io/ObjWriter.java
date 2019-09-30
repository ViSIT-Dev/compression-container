package de.uni_passau.visit.compression.logic.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.Normal;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class offers static methods to write a given 3D-model to a specific
 * file.
 * 
 * @author Florian Schlenker
 *
 */
public class ObjWriter {

	/**
	 * This static method writes the given model to a file with the given filename.
	 * The header stored in the model will be used as header for the OBJ-file.
	 * 
	 * @param filename
	 *            The filename of the desired OBJ-file (including relative /
	 *            absolute path)
	 * @param model
	 *            The model that shall be written to the file
	 * @throws IOException
	 *             If there occurs an error while writing the file
	 */
	public static void write(String filename, AbstractModel model) throws IOException {
		write(filename, model, model.getHeader());
	}

	/**
	 * This static method writes the given model to a file with the given filename.
	 * Instead of the header stored in the given model an alternative header passed
	 * as argument will be used for the OBJ-file's header.
	 * 
	 * @param filename
	 *            The filename of the desired OBJ-file (including relative /
	 *            absolute path)
	 * @param model
	 *            The model that shall be written to the file
	 * @param header
	 *            The alternative header that shall be used for the OBJ-file
	 * @throws IOException
	 *             If there occurs an error while writing the file
	 */
	public static void write(String filename, AbstractModel model, String header) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(new File(filename)));

		writer.println(header);

		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.###############", otherSymbols);

		for (Vertex v : model.getVertices()) {
			writer.println(String.format(Locale.US, "v %s %s %s", df.format(v.getCoords()[0]),
					df.format(v.getCoords()[1]), df.format(v.getCoords()[2])));
		}

		for (Normal n : model.getNormals()) {
			writer.println(String.format(Locale.US, "vn %s %s %s", df.format(n.getNormal()[0]),
					df.format(n.getNormal()[1]), df.format(n.getNormal()[2])));
		}

		for (TextureCoords t : model.getTextureCoords()) {
			writer.println(
					String.format(Locale.US, "vt %s %s", df.format(t.getCoords()[0]), df.format(t.getCoords()[1])));
		}

		String currentMaterial = "";

		for (Face f : model.getFaces()) {
			if (!f.getMaterial().equals(currentMaterial)) {
				currentMaterial = f.getMaterial();
				writer.println("usemtl " + f.getMaterial());
			}

			if (f.hasNormals()) {
				if (f.hasTexture()) {
					writer.println(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", f.getVertexIndices()[0] + 1,
							f.getTextureCoordIndices()[0] + 1, f.getNormalIndices()[0] + 1, f.getVertexIndices()[1] + 1,
							f.getTextureCoordIndices()[1] + 1, f.getNormalIndices()[1] + 1, f.getVertexIndices()[2] + 1,
							f.getTextureCoordIndices()[2] + 1, f.getNormalIndices()[2] + 1));
				} else {
					writer.println(String.format("f %d//%d %d//%d %d//%d", f.getVertexIndices()[0] + 1,
							f.getNormalIndices()[0] + 1, f.getVertexIndices()[1] + 1, f.getNormalIndices()[1] + 1,
							f.getVertexIndices()[2] + 1, f.getNormalIndices()[2] + 1));
				}
			} else {
				if (f.hasTexture()) {
					writer.println(String.format("f %d/%d %d/%d %d/%d", f.getVertexIndices()[0] + 1,
							f.getTextureCoordIndices()[0] + 1, f.getVertexIndices()[1] + 1,
							f.getTextureCoordIndices()[1] + 1, f.getVertexIndices()[2] + 1,
							f.getTextureCoordIndices()[2] + 1));
				} else {
					writer.println(String.format("f %d %d %d", f.getVertexIndices()[0] + 1, f.getVertexIndices()[1] + 1,
							f.getVertexIndices()[2] + 1));
				}
			}
		}

		writer.close();
	}

}
