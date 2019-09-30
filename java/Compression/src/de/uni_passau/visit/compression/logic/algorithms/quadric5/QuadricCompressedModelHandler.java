package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.data.EditableTechnicalMetadataCompressionLevel;
import de.uni_passau.visit.compression.data.TechnicalMetadata;
import de.uni_passau.visit.compression.data.TechnicalMetadataCompressionLevel;
import de.uni_passau.visit.compression.data.TechnicalMetadataFileTypeSpecific3D;
import de.uni_passau.visit.compression.exceptions.TextureCompressionException;
import de.uni_passau.visit.compression.logic.algorithms.image.ImageCompressor;
import de.uni_passau.visit.compression.logic.io.AbstractModel;
import de.uni_passau.visit.compression.logic.io.ObjModel;
import de.uni_passau.visit.compression.logic.io.ObjWriter;
import de.uni_passau.visit.compression.models.AbstractCompressionLevelFilter;
import de.uni_passau.visit.compression.models.CompressionModel;
import de.uni_passau.visit.compression.models.FilenameGenerator3D;

/**
 * This class allows the further treatment of new compressed models. It writes
 * both OBJ- and MTL-files and can also be used to update technical meta data.
 * Furthermore it provides a method that triggers the texture compression. This
 * class does not supports models referencing more than one material file and /
 * or more than one texture file. However, since such models are not supported
 * by the ViSIT backend, this functionality is not needed when using the
 * compression system as component. If using the compression system in
 * stand-alone mode, use @see QuadricCompressedModelHandler instead.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricCompressedModelHandler implements QuadricAbstractCompressedModelHandler {

	private static final Logger log = LogManager.getLogger(QuadricCompressedModelHandler.class);
	private final List<Integer> vertexCounts;
	private final Map<Integer, EditableTechnicalMetadataCompressionLevel> newTechMetaCompressionLevels;
	private final String header;
	private final int[] textureCompressionLevelLimits, textureCompressionLevelSizes;
	private final AbstractCompressionLevelFilter filter;
	private final TechnicalMetadata techMeta;
	private final boolean useTechMeta;
	private final FilenameGenerator3D filenameGen;
	private final String textureSuffix;
	private final boolean hasMaterial, hasTexture;

	/**
	 * This constructor initializes the object with the given arguments.
	 * 
	 * @param filter
	 *            A filter that can be used to filter models, which shall not be
	 *            stored (for instance because they already exist)
	 * @param originalHeader
	 *            The header of the original OBJ-file that has been compression
	 * @param textureCompressionLevelLimits
	 *            The vertex count limits between different texture compression
	 *            levels
	 * @param textureCompressionLevelSizes
	 *            The texture image resolution for each texture compression level
	 * @param techMeta
	 *            The technical meta data attached to the object, or null if no
	 *            update of technical meta data shall be performed. The newly
	 *            created compression levels will be added to the hereby referenced
	 *            object
	 * @param filenameGen
	 *            The filename generator that shall be used for reading the input
	 *            files and for writing the output files
	 * @throws FileNotFoundException
	 *             If one of the MTL-files referenced by the OBJ-file could not be
	 *             found
	 */
	public QuadricCompressedModelHandler(AbstractCompressionLevelFilter filter, String originalHeader,
			int[] textureCompressionLevelLimits, int[] textureCompressionLevelSizes, TechnicalMetadata techMeta,
			FilenameGenerator3D filenameGen) throws FileNotFoundException {
		this.header = originalHeader;
		this.vertexCounts = new LinkedList<>();
		this.newTechMetaCompressionLevels = new HashMap<>();
		this.textureCompressionLevelLimits = textureCompressionLevelLimits;
		this.textureCompressionLevelSizes = textureCompressionLevelSizes;
		this.filter = filter;
		this.techMeta = techMeta;
		this.filenameGen = filenameGen;
		this.useTechMeta = techMeta != null;

		boolean hasMaterial = true;
		boolean hasTexture = true;
		String textureSuffix = null;

		try {
			textureSuffix = getTextureFileExtension();
			if (textureSuffix == null) {
				hasTexture = false;
			}
		} catch (FileNotFoundException ex) {
			hasMaterial = false;
			hasTexture = false;
		}

		this.hasTexture = hasTexture;
		this.hasMaterial = hasMaterial;
		this.textureSuffix = textureSuffix;
	}

	/**
	 * This method returns the file extension of the texture file by analyzing the
	 * reference in the MTL-file. The filename of the MTL-file will be retrieved
	 * from the object's filename generator.
	 * 
	 * @return Returns the extension of the texture file referenced by the model's
	 *         material file without the leading dot
	 * @throws FileNotFoundException
	 *             Will be thrown if the MTL-file could not be found
	 */
	private String getTextureFileExtension() throws FileNotFoundException {
		Scanner s = new Scanner(filenameGen.getMtlFilePath());

		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] tokens = line.split(" ");

			if (tokens.length >= 2 && tokens[0].equals(ObjModel.MTL_TEXTURE_IMPORT_PREFIX)) {
				s.close();
				return FilenameUtils.getExtension(tokens[1]);
			}
		}

		s.close();
		return null;
	}

	/**
	 * This method generates and returns the header for the OBJ-file with the given
	 * vertex count, which references the appropriate MTL-file.
	 * 
	 * @param vertexCount
	 *            The vertex count of the model, for which the header shall be
	 *            generated
	 * @return Returns the header for the OBJ-file with the given vertex count
	 */
	private String getModifiedHeader(int vertexCount) {
		Scanner s = new Scanner(header);
		StringBuilder newHeaderBuilder = new StringBuilder();

		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] tokens = line.split(" ");
			if (tokens.length >= 2 && tokens[0].equals(ObjModel.MTL_DECLARATION_PREFIX)) {
				tokens[1] = filenameGen.getMtlFilename(String.valueOf(vertexCount));
				newHeaderBuilder.append(String.join(" ", tokens));
				newHeaderBuilder.append(System.lineSeparator());
			} else {
				newHeaderBuilder.append(line);
				newHeaderBuilder.append(System.lineSeparator());
			}
		}

		s.close();
		return newHeaderBuilder.toString();
	}

	/**
	 * This method writes the MTL-file for a model with the given vertex count,
	 * based on the template file. The filenames and paths for the input and output
	 * files are retrieved from the object's filename generator. All files are
	 * expected to lie in the same folder as the original OBJ-file. Furthermore it
	 * registers the generated MTL-file in this compression level's technical meta
	 * data.
	 * 
	 * @param vertexCount
	 *            The count of vertices of the model, for which the MTL-file shall
	 *            be generated
	 * @throws IOException
	 *             If the MTL-file could not be written
	 */
	private void writeMtlFile(int vertexCount) throws IOException {
		Scanner s = new Scanner(filenameGen.getMtlFilePath());
		StringBuilder mtlBuilder = new StringBuilder();

		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] tokens = line.split(" ");

			if (tokens.length >= 2 && tokens[0].equals(ObjModel.MTL_TEXTURE_IMPORT_PREFIX)) {
				tokens[1] = filenameGen.getTextureFilename(String.valueOf(vertexCount), textureSuffix);
				mtlBuilder.append(String.join(" ", tokens));
			} else {
				mtlBuilder.append(line);
			}

			mtlBuilder.append(System.lineSeparator());
		}

		s.close();
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(filenameGen.getMtlFilePath(String.valueOf(vertexCount))));
		writer.write(mtlBuilder.toString());
		writer.close();

		addTechMetaDataCompressionLevelFile(vertexCount, filenameGen.getMtlFilename(String.valueOf(vertexCount)),
				FileUtils.sizeOf(filenameGen.getMtlFilePath(String.valueOf(vertexCount))));
	}

	@Override
	public boolean handleCompressedModel(AbstractModel model, int vertexCount) {
		if (filter.filterCompressionLevel(String.valueOf(vertexCount))) {
			log.info("Storing compressed version with " + vertexCount + " vertices.");
			try {
				registerTechMetaDataCompressionLevel(model, vertexCount);
				ObjWriter.write(filenameGen.getObjFilePath(String.valueOf(vertexCount)).getAbsolutePath(), model,
						getModifiedHeader(vertexCount));

				vertexCounts.add(vertexCount);
				addTechMetaDataCompressionLevelFile(vertexCount,
						filenameGen.getObjFilename(String.valueOf(vertexCount)),
						FileUtils.sizeOf(filenameGen.getObjFilePath(String.valueOf(vertexCount))));

				if (hasMaterial) {
					writeMtlFile(vertexCount);
				}
				return true;
			} catch (IOException e) {
				log.error("Error while writing compressed model to disk: " + e.getMessage());
				return false;
			}
		} else {
			log.info("Compressed version with " + vertexCount
					+ " vertices won't be stored due to an active filter restriction.");
			return true;
		}
	}

	/**
	 * This method adds the technical meta data for the given model with the given
	 * vertex count to the newTechMetaCompressionLevels-map.
	 * 
	 * @param model
	 *            The compressed model, whose technical meta data shall be added
	 * @param vertexCount
	 *            The vertex count of the compressed model, whose technical meta
	 *            data shall be added
	 */
	private void registerTechMetaDataCompressionLevel(AbstractModel model, int vertexCount) {
		if (useTechMeta) {
			TechnicalMetadataCompressionLevel original = techMeta
					.getCompressionLevel(CompressionModel.ORIGINAL_FILE_INDICATOR);
			String uploadTimestamp = String.valueOf(System.currentTimeMillis() / 1000L);
			EditableTechnicalMetadataCompressionLevel newInfo = new EditableTechnicalMetadataCompressionLevel(
					uploadTimestamp, original.getAccessLevel(), original.getLicense());
			newInfo.setFileTypeSpecificMeta(
					new TechnicalMetadataFileTypeSpecific3D(vertexCount, model.getFaces().size()));
			newTechMetaCompressionLevels.put(vertexCount, newInfo);
		}
	}

	/**
	 * This method registers a file with the given name and size in the technical
	 * meta data for the model with the given vertex count. The compression level
	 * itself has to be registered already in the newTechMetaCompressionLevels-map
	 * to avoid a @see NullPointerException
	 * 
	 * @param vertexCount
	 *            The vertex count of the compressed model the file should be
	 *            registered in
	 * @param filename
	 *            The name of the file that shall be registered in the technical
	 *            meta data (without path)
	 * @param fileSize
	 *            The size of the file that shall be registered in the technical
	 *            meta data
	 */
	private void addTechMetaDataCompressionLevelFile(int vertexCount, String filename, long fileSize) {
		if (useTechMeta) {
			newTechMetaCompressionLevels.get(vertexCount).addFile(filename, fileSize);
		}
	}

	/**
	 * The call to this method triggers the compression of the textures for each of
	 * the created compressed models. Therefore it should be called after all
	 * desired compression levels of the original models have been generated.
	 * 
	 * @return Returns false, if an error during the texture compression occurred,
	 *         otherwise true
	 */
	public boolean compressTextures() {
		boolean success = true;
		int[] textureBounds = new int[vertexCounts.size()];

		if (hasTexture) {
			int index = 0;
			for (Integer currentVertexCount : vertexCounts) {
				int currentSizeIndex = 0;

				while (currentSizeIndex < textureCompressionLevelLimits.length
						&& currentVertexCount > textureCompressionLevelLimits[currentSizeIndex]) {
					currentSizeIndex++;
				}

				textureBounds[index] = textureCompressionLevelSizes[currentSizeIndex];

				++index;
			}

			ImageCompressor compressor = new ImageCompressor();

			File[] filePaths = new File[textureBounds.length];
			String[] fileNames = new String[textureBounds.length];

			index = 0;
			for (Integer currentVertexCount : vertexCounts) {
				filePaths[index] = filenameGen.getTextureFilePath(String.valueOf(currentVertexCount), textureSuffix);
				fileNames[index] = filenameGen.getTextureFilename(String.valueOf(currentVertexCount), textureSuffix);
				++index;
			}

			try {
				compressor.compressTextureFile(filenameGen.getTextureFilePath(textureSuffix), filePaths, textureBounds);
			} catch (TextureCompressionException ex) {
				log.error(ex.getMessage());
				success = false;
			}

			index = 0;
			for (Integer currentVertexCount : vertexCounts) {
				long fileSize = FileUtils.sizeOf(filePaths[index]);
				addTechMetaDataCompressionLevelFile(currentVertexCount, fileNames[index], fileSize);
				++index;
			}
		}

		return success;
	}

	/**
	 * This method writes the technical meta data collected during the compression
	 * process in the @see TechnicalMetadata-object passed to the constructor. After
	 * calling this method no more compressed models must be registered in this @see
	 * QuadricCompressedModelHandler. If doing so the state will be undefined.
	 */
	public void updateTechnicalMetadata() {
		if (useTechMeta) {
			for (Integer vertexCount : vertexCounts) {
				EditableTechnicalMetadataCompressionLevel currentInfo = newTechMetaCompressionLevels.get(vertexCount);
				currentInfo.close();
				techMeta.addCompressionLevel(String.valueOf(vertexCount), currentInfo);
			}
		} else {
			throw new IllegalStateException(
					"This object was constructed without giving technical meta data. Hence no technical meta data can be retrieved.");
		}
	}

}
