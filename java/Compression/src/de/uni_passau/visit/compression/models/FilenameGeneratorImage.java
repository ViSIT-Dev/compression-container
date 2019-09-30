package de.uni_passau.visit.compression.models;

import java.io.File;
import java.nio.file.Paths;

import de.uni_passau.visit.compression.data.CompressionJob;

/**
 * This filename generator can be used to create the expected paths and
 * filenames of an image referenced by a specific compression job for arbitrary
 * compression levels.
 * 
 * @author Florian Schlenker
 *
 */
public class FilenameGeneratorImage {
	private static final String FILENAME_SEPARATOR = ".";
	private static final String SUFFIX_SEPARATOR = ".";

	private final ConfigModel config;
	private final String objectUid, mediaUid, extension, jobSpecificBasePath, originalCompressionLevelIdentifier;

	/**
	 * This constructor initializes the filename generator for the image referenced
	 * by the given compression job and using the given configuration.
	 * 
	 * @param config
	 *            The configuration that shall be used to initialize the filename
	 *            generator
	 * @param job
	 *            The job referencing the image, whose filenames and paths shall be
	 *            generated with this filename generator
	 * @param originalCompressionLevelIdentifier
	 *            The compression level identifier used for original (uncompressed)
	 *            image
	 */
	public FilenameGeneratorImage(ConfigModel config, CompressionJob job, String extension,
			String originalCompressionLevelIdentifier) {
		this.config = config;
		this.objectUid = job.getObjectUid();
		this.mediaUid = job.getMediaUid();
		this.jobSpecificBasePath = job.getBasePath();
		this.extension = extension;
		this.originalCompressionLevelIdentifier = originalCompressionLevelIdentifier;
	}

	/**
	 * This method creates the filename without path for the image file at the
	 * compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose filename shall
	 *            be generated
	 * @return Returns the image's filename without path
	 */
	public String getImageFilename(String compressionLevelIdentifier) {
		return objectUid + FILENAME_SEPARATOR + mediaUid + FILENAME_SEPARATOR + compressionLevelIdentifier
				+ SUFFIX_SEPARATOR + extension;
	}

	/**
	 * This method creates the filename without path for the image file at the
	 * original (uncompressed) compression level.
	 * 
	 * @return Returns the image's filename without path
	 */
	public String getImageFilename() {
		return getImageFilename(originalCompressionLevelIdentifier);
	}

	/**
	 * This method creates the filename including its path for the image file at the
	 * compression level with the given identifier
	 * 
	 * @param compressionLevelIdentifier
	 *            The identifier of the compression level, whose filename shall
	 *            be generated
	 * @return Returns the image's filename including its path
	 */
	public File getImageFilePath(String compressionLevelIdentifier) {
		return Paths.get(config.getMediaFileRoot(), jobSpecificBasePath, getImageFilename(compressionLevelIdentifier))
				.toFile();
	}

	/**
	 * This method creates the filename including its path for the image file at the
	 * original (uncompressed) compression level.
	 * 
	 * @return Returns the image's filename including its path
	 */
	public File getImageFilePath() {
		return getImageFilePath(originalCompressionLevelIdentifier);
	}

}
