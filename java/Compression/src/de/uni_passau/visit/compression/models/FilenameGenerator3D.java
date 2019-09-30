package de.uni_passau.visit.compression.models;

import java.io.File;
import java.nio.file.Paths;

/**
 * This filename generator can be used to create the expected paths and
 * filenames for all files of the model referenced by a specific compression job
 * and for arbitrary compression levels.
 * 
 * @author Florian Schlenker
 *
 */
public class FilenameGenerator3D {

	private static final String FILENAME_SEPARATOR = ".";
	private static final String SUFFIX_SEPARATOR = ".";
	public static final String OBJ_EXTENSION = ".obj";
	public static final String MTL_EXTENSION = ".mtl";

	private final String objectUid, mediaUid, jobSpecificBasePath, originalCompressionLevelIdentifier, mediaFileRoot;

	/**
	 * This constructor initializes the filename generator for the model referenced
	 * by the given compression job information and using the given configuration.
	 * 
	 * @param mediaFileRoot
	 *            The root directory specified in the system's configuration, which
	 *            contains all media files
	 * @param jobSpecificBasePath
	 *            The directory relative to the given mediaFileRoot containing the
	 *            current job's media files
	 * @param objectUid
	 *            The object UID without prefix of the object, for which this
	 *            generator shall create filenames
	 * @param mediaUid
	 *            The media UID without prefix of the media representation, for
	 *            which this generator shall create filenames
	 * @param originalCompressionLevelIdentifier
	 *            The compression level identifier used for original (uncompressed)
	 *            models
	 */
	public FilenameGenerator3D(String mediaFileRoot, String jobSpecificBasePath, String objectUid, String mediaUid,
			String originalCompressionLevelIdentifier) {
		this.mediaFileRoot = mediaFileRoot;
		this.objectUid = objectUid;
		this.mediaUid = mediaUid;
		this.jobSpecificBasePath = jobSpecificBasePath;
		this.originalCompressionLevelIdentifier = originalCompressionLevelIdentifier;
	}

	private String getFilenameWithoutPrefix(String compressionLevelIdentifier) {
		return objectUid + FILENAME_SEPARATOR + mediaUid + FILENAME_SEPARATOR + compressionLevelIdentifier;
	}

	/**
	 * This method creates the filename without path for the model's OBJ-file at the
	 * compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose OBJ-filename shall
	 *            be generated
	 * @return Returns the OBJ-file's name without path
	 */
	public String getObjFilename(String compressionLevelIdentifier) {
		return getFilenameWithoutPrefix(compressionLevelIdentifier) + OBJ_EXTENSION;
	}

	/**
	 * This method creates the filename without path for the model's OBJ-file at the
	 * original (uncompressed) compression level.
	 * 
	 * @return Returns the OBJ-file's name without path
	 */
	public String getObjFilename() {
		return getObjFilename(originalCompressionLevelIdentifier);
	}

	/**
	 * This method creates the filename without path for the model's MTL-file at the
	 * compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose MTL-filename shall
	 *            be generated
	 * @return Returns the MTL-file's name without path
	 */
	public String getMtlFilename(String compressionLevelIdentifier) {
		return getFilenameWithoutPrefix(compressionLevelIdentifier) + MTL_EXTENSION;
	}

	/**
	 * This method creates the filename without path for the model's MTL-file at the
	 * original (uncompressed) compression level.
	 * 
	 * @return Returns the MTL-file's name without path
	 */
	public String getMtlFilename() {
		return getMtlFilename(originalCompressionLevelIdentifier);
	}

	/**
	 * This method creates the filename without path for the model's texture file at
	 * the compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose texture filename
	 *            shall be generated
	 * @param extension
	 *            The texture file's extension without leading dot
	 * @return Returns the texture file's name without path
	 */
	public String getTextureFilename(String compressionLevelIdentifier, String extension) {
		return getFilenameWithoutPrefix(compressionLevelIdentifier) + SUFFIX_SEPARATOR + extension;
	}

	/**
	 * This method creates the filename without path for the model's OBJ-file at the
	 * original (uncompressed) compression level.
	 * 
	 * @param extension
	 *            The texture file's extension without leading dot
	 * @return Returns the OBJ-file's name without path
	 */
	public String getTextureFilename(String extension) {
		return getTextureFilename(originalCompressionLevelIdentifier, extension);
	}

	/**
	 * This method creates the filename including its path for the model's OBJ-file
	 * at the compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose OBJ-filename shall
	 *            be generated
	 * @return Returns the OBJ-file's name including its path
	 */
	public File getObjFilePath(String compressionLevelIdentifier) {
		return Paths.get(mediaFileRoot, jobSpecificBasePath, getObjFilename(compressionLevelIdentifier)).toFile();
	}

	/**
	 * This method creates the filename including its path for the model's OBJ-file
	 * at the original (uncompressed) compression level.
	 * 
	 * @return Returns the OBJ-file's name including its path
	 */
	public File getObjFilePath() {
		return getObjFilePath(originalCompressionLevelIdentifier);
	}

	/**
	 * This method creates the filename including its path for the model's MTL-file
	 * at the compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose MTL-filename shall
	 *            be generated
	 * @return Returns the MTL-file's name including its path
	 */
	public File getMtlFilePath(String compressionLevelIdentifier) {
		return Paths.get(mediaFileRoot, jobSpecificBasePath, getMtlFilename(compressionLevelIdentifier)).toFile();
	}

	/**
	 * This method creates the filename including its path for the model's MTL-file
	 * at the original (uncompressed) compression level.
	 * 
	 * @return Returns the MTL-file's name including its path
	 */
	public File getMtlFilePath() {
		return getMtlFilePath(originalCompressionLevelIdentifier);
	}

	/**
	 * This method creates the filename including its path for the model's texture
	 * file at the compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose texture filename
	 *            shall be generated
	 * @param extension
	 *            The texture file's extension without leading dot
	 * @return Returns the texture file's name including its path
	 */
	public File getTextureFilePath(String compressionLevelIdentifier, String extension) {
		return Paths.get(mediaFileRoot, jobSpecificBasePath, getTextureFilename(compressionLevelIdentifier, extension))
				.toFile();
	}

	/**
	 * This method creates the filename including its path for the model's OBJ-file
	 * at the original (uncompressed) compression level.
	 * 
	 * @param extension
	 *            The texture file's extension without leading dot
	 * @return Returns the OBJ-file's name including its path
	 */
	public File getTextureFilePath(String extension) {
		return getTextureFilePath(originalCompressionLevelIdentifier, extension);
	}

}
