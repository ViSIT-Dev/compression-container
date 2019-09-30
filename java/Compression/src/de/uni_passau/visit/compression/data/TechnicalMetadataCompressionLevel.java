package de.uni_passau.visit.compression.data;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the information on one specific compression level in an
 * media object's technical meta data, such as the upload date, the access
 * level, the license and file type specific meta data. Furthermore the names
 * and sizes of all files corresponding with this compression level are stored.
 * All properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization and contains Jackson-annotations for the conversion of
 * JSON-strings to instances of this class.
 * 
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadataCompressionLevel implements Serializable {
	private static final long serialVersionUID = 7982419080342489968L;

	protected final String uploadDate, accessLevel, license;
	protected Object fileTypeSpecificMeta;
	protected long fileSize;
	protected String[] paths;

	/**
	 * This constructor initializes the object with the given arguments.
	 * 
	 * @param uploadDate
	 *            The upload date of this compressed version of the media object
	 * @param accessLevel
	 *            The access level of this compressed version of the media object
	 * @param license
	 *            The license under which this version of the media object is
	 *            published
	 * @param fileTypeSpecificMeta
	 *            Further information about the compressed version of the media
	 *            object depending on the file type
	 * @param fileSize
	 *            A value determined by the summed size of files corresponding with
	 *            this compression level in bytes
	 * @param paths
	 *            An array containing the file names of all files corresponding with
	 *            this compression level in the same order as {@code fileSizes}
	 */
	public TechnicalMetadataCompressionLevel(@JsonProperty("uploadDate") String uploadDate,
			@JsonProperty("accessLevel") String accessLevel, @JsonProperty("license") String license,
			@JsonProperty("fileTypeSpecificMeta") Object fileTypeSpecificMeta, @JsonProperty("fileSize") long fileSize,
			@JsonProperty("paths") String[] paths) {
		this.uploadDate = uploadDate;
		this.accessLevel = accessLevel;
		this.license = license;
		this.fileTypeSpecificMeta = fileTypeSpecificMeta;
		this.fileSize = fileSize;
		this.paths = paths;
	}

	/**
	 * This method returns the upload date of this compressed version of the media
	 * object.
	 * 
	 * @return Returns the upload date of this compressed version of the media
	 *         object
	 */
	public String getUploadDate() {
		return uploadDate;
	}

	/**
	 * This method returns the access level of this compressed version of the media
	 * object.
	 * 
	 * @return Returns a string representing the access level of this compressed
	 *         version of the media object
	 */
	public String getAccessLevel() {
		return accessLevel;
	}

	/**
	 * This method returns the license under which this version of the media object
	 * is published.
	 * 
	 * @return Returns a string representing the title of the license.
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * This method returns the file type specific meta data stored with this version
	 * of the media object.
	 * 
	 * @return Returns an unspecified object containing this file type specific meta
	 *         data.
	 */
	public Object getFileTypeSpecificMeta() {
		return fileTypeSpecificMeta;
	}

	/**
	 * This method returns the sum of the sizes of all files corresponding with this
	 * compression level in bytes.
	 * 
	 * @return Returns the the sum of the sizes of all files corresponding with this
	 *         compression level in bytes
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * This method returns the file name of all files corresponding with this
	 * compression level.
	 * 
	 * @return Returns the file name of all files corresponding with this
	 *         compression level
	 */
	public String[] getPaths() {
		return paths;
	}

	@Override
	public String toString() {
		return "TechnicalMetadataCompressionLevel [uploadDate=" + uploadDate + ", accessLevel=" + accessLevel
				+ ", license=" + license + ", fileTypeSpecificMeta=" + fileTypeSpecificMeta + ", fileSize=" + fileSize
				+ ", filePaths=" + Arrays.toString(paths) + "]";
	}
	
	
}
