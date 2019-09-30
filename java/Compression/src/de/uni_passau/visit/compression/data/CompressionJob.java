package de.uni_passau.visit.compression.data;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a compression job that can be sent to the compression
 * system. It consists of the file's the base path, the object-UID, the media-
 * UID and MIME-type as well as an arbitrary title and the desired levels of
 * compression in case of 3D-models. All properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization and contains Jackson-annotations for the conversion of
 * JSON-strings to instances of this class.
 * 
 * @author Florian Schlenker
 *
 */
public class CompressionJob implements Serializable {

	private static final long serialVersionUID = -4526653006280076396L;

	private final String basePath, objectUid, mediaUid, title, mimeType;
	private final String[] levels;

	/**
	 * This constructor initializes all object fields with the given arguments.
	 * 
	 * @param basePath
	 *            The directory containing the media file that shall be compressed
	 *            with respect to the root directory specified in the compression
	 *            system's configuration. If the media file is positioned directly
	 *            in the root directory, use an empty string.
	 * @param objectUid
	 *            The object-UID of the media file that shall be compressed
	 * @param mediaUid
	 *            The media-UID of the media file that shall be compressed
	 * @param title
	 *            An arbitrary title describing the compression job
	 * @param mimeType
	 *            The MIME-type of the file that shall be compressed
	 * @param levels
	 *            An array containing the identifiers for all desired compression
	 *            levels.
	 */
	@JsonCreator
	public CompressionJob(@JsonProperty("basePath") String basePath, @JsonProperty("objectUid") String objectUid,
			@JsonProperty("mediaUid") String mediaUid, @JsonProperty("title") String title,
			@JsonProperty("mimeType") String mimeType, @JsonProperty("levels") String[] levels) {
		super();
		this.basePath = basePath;
		this.objectUid = objectUid;
		this.mediaUid = mediaUid;
		this.title = title;
		this.mimeType = mimeType;
		this.levels = levels;
	}

	/**
	 * This method returns the directory containing the media file of this
	 * compression job combined with the given root directory specified in the
	 * compression system's configuration.
	 * 
	 * @param mediaFileRoot
	 *            The media file root directory with respect to which the base path
	 *            has been given
	 * @return Returns the directory containing the media file of this compression
	 *         job.
	 */
	public String getBasePath(String mediaFileRoot) {
		return Paths.get(mediaFileRoot, basePath).toString();
	}

	/**
	 * This method returns the directory containing the media file of this
	 * compression job with respect to the given root directory specified in the
	 * compression system's configuration.
	 * 
	 * @return Returns the directory containing the media file of this compression
	 *         job relative to the root directory specified in the compression
	 *         system's configuration
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * This method returns the object-UID of the media file of this compression job.
	 * 
	 * @return Returns the object-UID of the media file of this compression job.
	 */
	public String getObjectUid() {
		return objectUid;
	}

	/**
	 * This method returns the media-UID of the media file of this compression job.
	 * 
	 * @return Returns the media-UID of the media file of this compression job.
	 */
	public String getMediaUid() {
		return mediaUid;
	}

	/**
	 * This method returns the title that has been given to this compression job.
	 * 
	 * @return Returns the title that has been given to this compression job.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * This method returns the mime type of the media file of this compression job.
	 * 
	 * @return Returns the mime type of the media file of this compression job as
	 *         string.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * This method returns the desired compression levels in case of 3D-models as
	 * String-array.
	 * 
	 * @return A String-array containing the desired compression levels.
	 */
	public String[] getLevels() {
		return levels;
	}

	/**
	 * This method returns String representation of the object containing all
	 * arguments given to the constructor.
	 * 
	 * @return Returns a String representation of the object.
	 */
	@Override
	public String toString() {
		return "CompressionJob [basePath=" + basePath + ", objectUid=" + objectUid + ", mediaUid=" + mediaUid
				+ ", title=" + title + ", mimeType=" + mimeType + ", levels=" + Arrays.toString(levels) + "]";
	}
}
