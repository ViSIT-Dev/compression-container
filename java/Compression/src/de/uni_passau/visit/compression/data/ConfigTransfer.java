package de.uni_passau.visit.compression.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a specific configuration of the configuration system.
 * It stores all the parameters that can be set by the user via the web
 * interface. All properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization and contains Jackson-annotations for the conversion of
 * JSON-strings to instances of this class.
 * 
 * @author Florian Schlenker
 * 
 */
public class ConfigTransfer implements Serializable {

	private static final long serialVersionUID = -2638120984313016863L;

	private final int apiPort;
	private final String[] apiAccessWhitelist;
	private final boolean autostart;
	private final int queueMaxLength;
	private final String[] defaultLevels;
	private final int[] textureLevelLimits, textureLevelSizes;
	private final ImageCompressionLevel[] imageCompressionLevels;

	/**
	 * This constructor initializes all of the object's field with the given
	 * arguments.
	 * 
	 * @param apiPort
	 *            Integer between 1 and 65536 defining the port, on which the web
	 *            server and the api endpoint listen for incoming connections.
	 * @param apiAccessWhitelist
	 *            A String array containing ip addresses authorized to use the api
	 *            or the web interface. If the array contains '*', access will be
	 *            granted to all clients
	 * @param autostart
	 *            A boolean indicating if the compression processes shall be started
	 *            automatically after starting the server.
	 * @param queueMaxLength
	 *            The maximum length of the queue of unfinished compression jobs.
	 * @param defaultLevels
	 *            A string array containing all default compression levels when
	 *            compressing 3D models.
	 * @param textureLevelLimits
	 *            An integer array defining the limits between different levels of
	 *            texture compression
	 * @param textureLevelSizes
	 *            An integer array defining the sizes of the different levels of
	 *            texture compression. This array is required to have one element
	 *            more than {@code textureLevelLimits}
	 * @param imageCompressionLevels
	 *            An array of ImageCompressionLevel objects defining the compression
	 *            levels of picture compression jobs
	 */
	@JsonCreator
	public ConfigTransfer(@JsonProperty("apiPort") int apiPort,
			@JsonProperty("apiAccessWhitelist") String[] apiAccessWhitelist,
			@JsonProperty("autostart") boolean autostart, @JsonProperty("queueMaxLength") int queueMaxLength,
			@JsonProperty("defaultLevels") String[] defaultLevels,
			@JsonProperty("textureLevelLimits") int[] textureLevelLimits,
			@JsonProperty("textureLevelSizes") int[] textureLevelSizes,
			@JsonProperty("imageLevels") ImageCompressionLevel[] imageCompressionLevels) {
		super();
		this.apiPort = apiPort;
		this.apiAccessWhitelist = apiAccessWhitelist;
		this.autostart = autostart;
		this.queueMaxLength = queueMaxLength;
		this.defaultLevels = defaultLevels;
		this.textureLevelLimits = textureLevelLimits;
		this.textureLevelSizes = textureLevelSizes;
		this.imageCompressionLevels = imageCompressionLevels;
	}

	/**
	 * This method returns an integer defining the port, on which the web server and
	 * the api endpoint listen for incoming connections.
	 * 
	 * @return Returns an integer between 1 and 65536
	 */
	public int getApiPort() {
		return apiPort;
	}

	/**
	 * This method returns a String array containing ip addresses authorized to use
	 * the api or the web interface. If the array contains '*', access will be
	 * granted to all clients.
	 * 
	 * @return Returns a string array containing authorized ip addresses.
	 */
	public String[] getApiAccessWhitelist() {
		return apiAccessWhitelist;
	}

	/**
	 * This method returns a boolean indicating if the compression processes shall
	 * be started automatically after starting the server.
	 * 
	 * @return Returns a boolean indicating if the compression processes shall be
	 *         started automatically after starting the server.
	 */
	public boolean getAutostart() {
		return autostart;
	}

	/**
	 * This method returns the maximum length of the queue of unfinished compression
	 * jobs.
	 * 
	 * @return Returns an integer defining the maximum queue length.
	 */
	public int getQueueMaxLength() {
		return queueMaxLength;
	}

	/**
	 * This method returns a string array containing all default compression levels
	 * when compressing 3D models.
	 * 
	 * @return Returns a string array containing all default compression levels.
	 */
	public String[] getDefaultLevels() {
		return defaultLevels;
	}

	/**
	 * This method returns an integer array defining the limits between different
	 * levels of texture compression.
	 * 
	 * @return Returns an integer array defining the limits between different levels
	 *         of texture compression.
	 */
	public int[] getTextureLevelLimits() {
		return textureLevelLimits;
	}

	/**
	 * An integer array defining the sizes of the different levels of texture
	 * compression.
	 * 
	 * @return Returns an integer array defining the sizes of the different levels
	 *         of texture compression.
	 */
	public int[] getTextureLevelSizes() {
		return textureLevelSizes;
	}

	/**
	 * This method returns an array of ImageCompressionLevel objects defining the
	 * compression levels of picture compression jobs.
	 * 
	 * @return Returns an array of ImageCompressionLevel objects defining the
	 *         compression levels of picture compression jobs.
	 */
	public ImageCompressionLevel[] getImageCompressionLevels() {
		return imageCompressionLevels;
	}

}
