package de.uni_passau.visit.compression.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.visit.compression.data.ConfigTransfer;
import de.uni_passau.visit.compression.data.ImageCompressionLevel;
import de.uni_passau.visit.compression.exceptions.InvalidConfigurationException;
import de.uni_passau.visit.compression.logic.algorithms.quadric5.QuadricEdgeCollapseConfig;

/**
 * This class represents the model responsible for the compression system's
 * configuration. It provides methods to retrieve and update the current
 * configuration.
 * 
 * The current configuration will be stored persistently in an INI-file, which
 * also can be edited by the user directly if necessary. If no such INI-file is
 * given default parameters will be used, which are defined in this class. Some
 * of the parameters can only be modified directly in the INI-file.
 * 
 * @author Florian Schlenker
 *
 */
public class ConfigModel implements QuadricEdgeCollapseConfig {
	// TO ADD
	// docker container specific base path
	// archive length (in ArchiveHandler)
	// API_ENDPOINT_FETCH_URL (TechnicalMetadataCommunicator)
	// API_ENDPOINT_SEND_URL (TechnicalMetadataCommunicator)
	private static final String CONFIG_FILE = RootModel.DATA_ROOT + "/config.ini";

	// properties that can be configured via web interface
	private static final String API_PORT_KEY = "apiPort";
	private static final String API_PORT_VALUE = "1613";
	private static final String API_ACCESS_WHITELIST_KEY = "accessWhiteListIps";
	private static final String API_ACCESS_WHITELIST_VALUE = "[127.0.0.1,*]";
	private static final String AUTOSTART_KEY = "autostart";
	private static final String AUTOSTART_VALUE = "true";
	private static final String QUEUE_MAX_LENGTH_KEY = "queueMaxLength";
	private static final String QUEUE_MAX_LENGTH_VALUE = "5000";
	private static final String DEFAULT_LEVELS_KEY = "defaultLevels";
	private static final String DEFAULT_LEVELS_VALUE = "[500,1000,5000,20000,50000,200000,500000,2000000,5000000,20000000,50000000]";
	private static final String TEXTURE_LEVEL_LIMIT_KEY = "textureLimits";
	private static final String TEXTURE_LEVEL_LIMIT_VALUE = "[5000, 50000]";
	private static final String TEXTURE_LEVEL_SIZE_KEY = "textureSizes";
	private static final String TEXTURE_LEVEL_SIZE_VALUE = "[1024, 2048, 8192]";
	private static final String IMAGE_COMPRESSION_LEVELS_KEY = "imageCompressionLevels";
	private static final String IMAGE_COMPRESSION_LEVELS_VALUE = "[{\"maxWidth\":3840,\"maxHeight\":2160,\"title\":\"UHD\"},{\"maxWidth\":1920,\"maxHeight\":1080,\"title\":\"FullHD\"},{\"maxWidth\":800,\"maxHeight\":600,\"title\":\"Mittel\"},{\"maxWidth\":120,\"maxHeight\":120,\"title\":\"Klein\"}]";

	// properties that can only be modified in the configuration file
	private static final String MEDIA_FILE_ROOT_DIRECTORY_KEY = "mediaFileRootDirectory";
	private static final String MEDIA_FILE_ROOT_DIRECTORY_VALUE = "/var/www/Private";
	private static final String ARCHIVE_DISPLAY_LENGTH_KEY = "archiveDisplayLength";
	private static final String ARCHIVE_DISPLAY_LENGTH_VALUE = "250";
	private static final String METADB_API_ENDPOINT_FETCH_URL_KEY = "metadbApiEndpointFetchUrl";
	private static final String METADB_API_ENDPOINT_FETCH_URL_VALUE = "https://DOMAIN/metadb-rest-api/digrep/media";
	private static final String METADB_API_ENDPOINT_SEND_URL_KEY = "metadbApiEndpointSendUrl";
	private static final String METADB_API_ENDPOINT_SEND_URL_VALUE = "https://DOMAIN/metadb-rest-api/digrep/media";
	private static final String METADB_API_AUTH_STRING_KEY = "metadbApiAuthString";
	private static final String METADB_API_AUTH_STRING_VALUE = "Basic XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==";
	private static final String METADB_API_MEDIA_UID_PREFIX_KEY = "metadbApiMediaUidPrefix";
	private static final String METADB_API_MEDIA_UID_PREFIX_VALUE = "http://DOMAIN/metadb/";
	private static final String TARGETSIZE_BOUNDARY_PENALTY_KEY = "targetSizeBoundaryPenalty";
	private static final String TARGETSIZE_BOUNDARY_PENALTY_VALUE = "100.0";
	private static final String TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_KEY = "targetSizeNormalDifferenceThreshold";
	private static final String TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_VALUE = "0.5";
	private static final String TARGETSIZE_QUALITY_THRESHOLD_KEY = "targetSizeQualityThreshold";
	private static final String TARGETSIZE_QUALITY_THRESHOLD_VALUE = "0.3";
	private static final String TARGETSIZE_NORMAL_PENALIZATION_KEY = "targetSizeNormalDifferencePenalization";
	private static final String TARGETSIZE_NORMAL_PENALIZATION_VALUE = "1000.0";
	private static final String TARGETSIZE_PARTITION_PENALIZATION_FACTOR_KEY = "targetSizePartitionPenalization";
	private static final String TARGETSIZE_PARTITION_PENALIZATION_FACTOR_VALUE = "10.0";

	private static final Logger log = LogManager.getLogger(ConfigModel.class);
	private static final String IMAGE_COMPRESSION_LEVEL_PATTERN = "[A-Za-z0-9_\\-]+";

	private final Properties currentConfiguration;

	/**
	 * This constructor creates a new configuration model and reads the current
	 * configuration from the INI-file.
	 */
	public ConfigModel() {
		File f = new File(CONFIG_FILE);
		currentConfiguration = getDefaultConfiguration();

		if (f.exists()) {
			try {
				FileInputStream in = new FileInputStream(CONFIG_FILE);
				currentConfiguration.load(in);
				in.close();
			} catch (IOException ex) {
				log.error("Logging file could not be read. Using default configuration. " + ex.getMessage());
			}
		} else {
			writeCurrentConfiguration();
		}
	}

	private Properties getDefaultConfiguration() {
		Properties defaultProps = new Properties();
		defaultProps.setProperty(API_PORT_KEY, API_PORT_VALUE);
		defaultProps.setProperty(API_ACCESS_WHITELIST_KEY, API_ACCESS_WHITELIST_VALUE);
		defaultProps.setProperty(AUTOSTART_KEY, AUTOSTART_VALUE);
		defaultProps.setProperty(QUEUE_MAX_LENGTH_KEY, QUEUE_MAX_LENGTH_VALUE);
		defaultProps.setProperty(DEFAULT_LEVELS_KEY, DEFAULT_LEVELS_VALUE);
		defaultProps.setProperty(TEXTURE_LEVEL_LIMIT_KEY, TEXTURE_LEVEL_LIMIT_VALUE);
		defaultProps.setProperty(TEXTURE_LEVEL_SIZE_KEY, TEXTURE_LEVEL_SIZE_VALUE);
		defaultProps.setProperty(IMAGE_COMPRESSION_LEVELS_KEY, IMAGE_COMPRESSION_LEVELS_VALUE);
		defaultProps.setProperty(MEDIA_FILE_ROOT_DIRECTORY_KEY, MEDIA_FILE_ROOT_DIRECTORY_VALUE);
		defaultProps.setProperty(ARCHIVE_DISPLAY_LENGTH_KEY, ARCHIVE_DISPLAY_LENGTH_VALUE);
		defaultProps.setProperty(METADB_API_ENDPOINT_FETCH_URL_KEY, METADB_API_ENDPOINT_FETCH_URL_VALUE);
		defaultProps.setProperty(METADB_API_ENDPOINT_SEND_URL_KEY, METADB_API_ENDPOINT_SEND_URL_VALUE);
		defaultProps.setProperty(METADB_API_AUTH_STRING_KEY, METADB_API_AUTH_STRING_VALUE);
		defaultProps.setProperty(METADB_API_MEDIA_UID_PREFIX_KEY, METADB_API_MEDIA_UID_PREFIX_VALUE);
		defaultProps.setProperty(TARGETSIZE_BOUNDARY_PENALTY_KEY, TARGETSIZE_BOUNDARY_PENALTY_VALUE);
		defaultProps.setProperty(TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_KEY,
				TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_VALUE);
		defaultProps.setProperty(TARGETSIZE_QUALITY_THRESHOLD_KEY, TARGETSIZE_QUALITY_THRESHOLD_VALUE);
		defaultProps.setProperty(TARGETSIZE_NORMAL_PENALIZATION_KEY, TARGETSIZE_NORMAL_PENALIZATION_VALUE);
		defaultProps.setProperty(TARGETSIZE_PARTITION_PENALIZATION_FACTOR_KEY,
				TARGETSIZE_PARTITION_PENALIZATION_FACTOR_VALUE);
		return defaultProps;
	}

	private void writeCurrentConfiguration() {
		try {
			FileOutputStream out = new FileOutputStream(CONFIG_FILE);
			currentConfiguration.store(out, "--- ViSIT Compression Configuration ---");
			out.close();
		} catch (IOException ex) {
			log.error("Logging file could not be written. " + ex.getMessage());
		}
	}

	private void updateConfiguration(int apiPort, String[] apiAccessWhitelist, boolean autostart, int queueMaxLength,
			Object[] defaultLevels, int[] textureLevelLimits, int[] textureLevelSizes,
			ImageCompressionLevel[] imageCompressionLevels) throws JsonProcessingException {
		currentConfiguration.setProperty(API_PORT_KEY, String.valueOf(apiPort));
		currentConfiguration.setProperty(API_ACCESS_WHITELIST_KEY, Arrays.toString(apiAccessWhitelist));
		currentConfiguration.setProperty(AUTOSTART_KEY, String.valueOf(autostart));
		currentConfiguration.setProperty(QUEUE_MAX_LENGTH_KEY, String.valueOf(queueMaxLength));
		currentConfiguration.setProperty(DEFAULT_LEVELS_KEY, Arrays.toString(defaultLevels));
		currentConfiguration.setProperty(TEXTURE_LEVEL_LIMIT_KEY, Arrays.toString(textureLevelLimits));
		currentConfiguration.setProperty(TEXTURE_LEVEL_SIZE_KEY, Arrays.toString(textureLevelSizes));
		currentConfiguration.setProperty(IMAGE_COMPRESSION_LEVELS_KEY,
				new ObjectMapper().writeValueAsString(imageCompressionLevels));
		writeCurrentConfiguration();
	}

	/**
	 * This method updates the current configuration according to the values given
	 * in the transfer object used as argument.
	 * 
	 * @param transferObject
	 *            The transfer object containing the new configuration values
	 * @throws JsonProcessingException
	 *             If a problem during the configuration serialization arises
	 * @throws InvalidConfigurationException
	 *             If the given configuration data are invalid
	 */
	public void updateConfiguration(ConfigTransfer transferObject)
			throws JsonProcessingException, InvalidConfigurationException {
		if (!checkConfigurationValidity(transferObject)) {
			throw new InvalidConfigurationException();
		}

		updateConfiguration(transferObject.getApiPort(), transferObject.getApiAccessWhitelist(),
				transferObject.getAutostart(), transferObject.getQueueMaxLength(), transferObject.getDefaultLevels(),
				transferObject.getTextureLevelLimits(), transferObject.getTextureLevelSizes(),
				transferObject.getImageCompressionLevels());
	}

	private boolean checkConfigurationValidity(ConfigTransfer c) {
		if (!(c.getApiPort() >= 1 && c.getApiPort() <= 65536))
			return false;

		if (!(c.getQueueMaxLength() >= 1))
			return false;

		for (int i = 0; i < c.getDefaultLevels().length; ++i) {
			try {
				Integer.parseInt(c.getDefaultLevels()[i]);
			} catch (NumberFormatException ex) {
				return false;
			}
		}

		if (c.getTextureLevelSizes().length < 1) {
			return false;
		}

		if (c.getTextureLevelLimits().length + 1 != c.getTextureLevelSizes().length) {
			return false;
		}

		for (int current : c.getTextureLevelLimits()) {
			if (current < 1) {
				return false;
			}
		}

		for (int current : c.getTextureLevelSizes()) {
			if (current < 1) {
				return false;
			}
		}

		for (int i = 0; i < c.getImageCompressionLevels().length; ++i) {
			if (c.getImageCompressionLevels()[i].getMaxHeight() < 1)
				return false;
			if (c.getImageCompressionLevels()[i].getMaxWidth() < 1)
				return false;
			if (!isValidImageCompressionLevelTitle(c.getImageCompressionLevels()[i].getTitle()))
				return false;
		}

		return true;
	}

	private boolean isValidImageCompressionLevelTitle(String title) {
		Pattern p = Pattern.compile(IMAGE_COMPRESSION_LEVEL_PATTERN);
		return p.matcher(title).matches() && !title.toLowerCase().equals(CompressionModel.ORIGINAL_FILE_INDICATOR.toLowerCase());
	}

	/**
	 * This method returns the network port on which the server shall listen for
	 * incoming HTTP-requests.
	 * 
	 * @return Returns the network port on which the server shall listen for
	 *         incoming HTTP-requests.
	 */
	public int getApiPort() {
		return getParsedIntOrDefault(API_PORT_KEY, API_PORT_VALUE);
	}

	/**
	 * This method returns the list of IP addresses which are allowed to access the
	 * web interface or the API.
	 * 
	 * @return Returns the list of IP addresses which are allowed to access the web
	 *         interface or the API
	 */
	public String[] getApiAccessWhitelist() {
		String strVal = currentConfiguration.getProperty(API_ACCESS_WHITELIST_KEY);
		strVal = strVal.substring(1, strVal.length() - 1); // crop brackets
		String[] tokens = strVal.split(",");

		for (int i = 0; i < tokens.length; ++i) {
			tokens[i] = tokens[i].trim();
		}

		return tokens;
	}

	/**
	 * This method returns true, if the queue procession shall be started
	 * immediately after the server startup.
	 * 
	 * @return Returns true, if the queue procession shall be started immediately
	 *         after the server startup
	 */
	public boolean getAutostart() {
		return Boolean.parseBoolean(currentConfiguration.getProperty(AUTOSTART_KEY));
	}

	/**
	 * This method returns the maximum length of the queue containing all
	 * unprocessed compression jobs.
	 * 
	 * @return Returns the maximum length of the queue containing all unprocessed
	 *         compression jobs
	 */
	public int getQueueMaxLength() {
		return getParsedIntOrDefault(QUEUE_MAX_LENGTH_KEY, QUEUE_MAX_LENGTH_VALUE);
	}

	/**
	 * This method returns the default list of compression levels for 3D-models.
	 * 
	 * @return Returns the default list of compression levels for 3D-models
	 */
	public String[] getDefaultLevels() {
		String strVal = currentConfiguration.getProperty(DEFAULT_LEVELS_KEY);
		strVal = strVal.substring(1, strVal.length() - 1); // crop brackets
		String[] tokens = strVal.split(",");

		for (int i = 0; i < tokens.length; ++i) {
			tokens[i] = tokens[i].trim();
		}

		return tokens;
	}

	/**
	 * This method returns an array describing the vertex count limits between
	 * different texture compression levels. This array's length is always equal to
	 * the length of the texture level sizes minus one.
	 * 
	 * @return Returns an array describing the vertex count limits between different
	 *         texture compression levels
	 */
	public int[] getTextureLevelLimits() {
		return getParsedIntArrayOrDefault(TEXTURE_LEVEL_LIMIT_KEY, TEXTURE_LEVEL_LIMIT_VALUE);
	}

	/**
	 * This method returns an array describing the texture sizes for all different
	 * texture compression levels. This array's length is always equal to the length
	 * of the texture level limits plus one.
	 * 
	 * @return Returns an array describing the texture sizes for all different
	 *         texture compression levels
	 */
	public int[] getTextureLevelSizes() {
		return getParsedIntArrayOrDefault(TEXTURE_LEVEL_SIZE_KEY, TEXTURE_LEVEL_SIZE_VALUE);
	}

	/**
	 * This method returns the compression levels that shall be generated for
	 * pictures.
	 * 
	 * @return Returns an array containing all compression levels that shall be
	 *         generated for pictures
	 */
	public ImageCompressionLevel[] getImageCompressionLevels() {
		try {
			return new ObjectMapper().readValue(currentConfiguration.getProperty(IMAGE_COMPRESSION_LEVELS_KEY),
					ImageCompressionLevel[].class);
		} catch (IOException e) {
			log.error("Could not parse '" + IMAGE_COMPRESSION_LEVELS_KEY
					+ "' value to image compression levels array. Using default value '"
					+ IMAGE_COMPRESSION_LEVELS_VALUE + "'.");
			try {
				return new ObjectMapper().readValue(IMAGE_COMPRESSION_LEVELS_VALUE, ImageCompressionLevel[].class);
			} catch (IOException e1) {
				throw new RuntimeException(
						"Specified default value could not be parsed to image compression levels array.");
			}
		}

	}

	/**
	 * This method returns the root directory containing all media files at least
	 * recursively. This setting can only be modified directly in the configuration
	 * file.
	 * 
	 * @return Returns the root directory containing all media files at least
	 *         recursively
	 */
	public String getMediaFileRoot() {
		return currentConfiguration.getProperty(MEDIA_FILE_ROOT_DIRECTORY_KEY);
	}

	/**
	 * This method returns the expected count of entries in the compression job
	 * archive. This setting can only be modified directly in the configuration
	 * file.
	 * 
	 * @return Returns the expected count of entries in the compression job archive
	 */
	public int getArchiveDisplayLength() {
		return getParsedIntOrDefault(ARCHIVE_DISPLAY_LENGTH_KEY, ARCHIVE_DISPLAY_LENGTH_VALUE);
	}

	/**
	 * This method returns the API end point that shall be used to fetch technical
	 * meta data from the meta database. This setting can only be modified directly
	 * in the configuration file.
	 * 
	 * @return Returns the API end point that shall be used to fetch technical meta
	 *         data from the meta database
	 */
	public String getMetadbApiEndpointFetchUrl() {
		return currentConfiguration.getProperty(METADB_API_ENDPOINT_FETCH_URL_KEY);
	}

	/**
	 * This method returns the API end point that shall be used to push technical
	 * meta data into the meta database. This setting can only be modified directly
	 * in the configuration file.
	 * 
	 * @return Returns the API end point that shall be used to push technical meta
	 *         data into the meta database
	 */
	public String getMetadbApiEndpointSendUrl() {
		return currentConfiguration.getProperty(METADB_API_ENDPOINT_SEND_URL_KEY);
	}

	/**
	 * This method returns a string that will be used as value for the
	 * "Authorization"-header in all HTTP-requests sent to the meta database. This
	 * setting can only be modified directly in the configuration file.
	 * 
	 * @return Returns a string that will be used as value for the
	 *         "Authorization"-header in all HTTP-requests
	 */
	public String getMetadbApiAuthString() {
		return currentConfiguration.getProperty(METADB_API_AUTH_STRING_KEY);
	}

	/**
	 * This method returns the prefix for the UIDs of all media files stored in the
	 * ViSIT system. Since these UIDs are URLs and therefore contain characters,
	 * which are illegal for filenames, the whole UID can't be used in the file
	 * system. This setting can only be modified directly in the configuration file.
	 * 
	 * @return Returns the prefix for the UIDs of all media files stored in the
	 *         ViSIT system
	 */
	public String getMetadbApiMediaUidPrefix() {
		return currentConfiguration.getProperty(METADB_API_MEDIA_UID_PREFIX_KEY);
	}

	public double getTargetsizeBoundaryPenalty() {
		return getParsedDoubleOrDefault(TARGETSIZE_BOUNDARY_PENALTY_KEY, TARGETSIZE_BOUNDARY_PENALTY_VALUE);
	}

	public double getTargetsizeNormalDifferenceThreshold() {
		return getParsedDoubleOrDefault(TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_KEY,
				TARGETSIZE_NORMAL_DIFFERENCE_THRESHOLD_VALUE);
	}

	public double getTargetsizeQualityThreshold() {
		return getParsedDoubleOrDefault(TARGETSIZE_QUALITY_THRESHOLD_KEY, TARGETSIZE_QUALITY_THRESHOLD_VALUE);
	}

	public double getTargetsizeNormalPenalization() {
		return getParsedDoubleOrDefault(TARGETSIZE_NORMAL_PENALIZATION_KEY, TARGETSIZE_NORMAL_PENALIZATION_VALUE);
	}

	public double getTargetsizePartitionPenalizationFactor() {
		return getParsedDoubleOrDefault(TARGETSIZE_PARTITION_PENALIZATION_FACTOR_KEY,
				TARGETSIZE_PARTITION_PENALIZATION_FACTOR_VALUE);
	}

	private int[] getParsedIntArrayOrDefault(final String key, final String defaultValue) {
		try {
			return parseIntArray(currentConfiguration.getProperty(key));
		} catch (NumberFormatException ex) {
			log.error("Could not parse '" + key + "' value to int array. Using default value '" + defaultValue + "'.");
			try {
				return parseIntArray(defaultValue);
			} catch (NumberFormatException ex2) {
				throw new RuntimeException("Specified default value could not be parsed to int array.");
			}
		}
	}

	private int[] parseIntArray(String value) throws NumberFormatException {
		String valueStripped = value.substring(1, value.length() - 1); // crop brackets
		String[] tokens = valueStripped.split(",");
		int[] intTokens = new int[tokens.length];

		for (int i = 0; i < tokens.length; ++i) {
			intTokens[i] = Integer.parseInt(tokens[i].trim());
		}

		return intTokens;
	}

	private int getParsedIntOrDefault(final String key, final String defaultValue) {
		try {
			return Integer.parseInt(currentConfiguration.getProperty(key));
		} catch (NumberFormatException ex) {
			log.error("Could not parse '" + key + "' value as integer. Using default value '" + defaultValue + "'.");
			return Integer.parseInt(defaultValue);
		}
	}

	private double getParsedDoubleOrDefault(final String key, final String defaultValue) {
		try {
			return Double.parseDouble(currentConfiguration.getProperty(key));
		} catch (NumberFormatException ex) {
			log.error(
					"Could not parse '" + key + "' value as double value. Using default value '" + defaultValue + "'.");
			return Double.parseDouble(defaultValue);
		}
	}

	/**
	 * This method returns a transfer object containing all parameters of the
	 * current configuration.
	 * 
	 * @return Returns a transfer object containing all parameters of the current
	 *         configuration
	 */
	public ConfigTransfer getTransferObject() {
		return new ConfigTransfer(getApiPort(), getApiAccessWhitelist(), getAutostart(), getQueueMaxLength(),
				getDefaultLevels(), getTextureLevelLimits(), getTextureLevelSizes(), getImageCompressionLevels());
	}

}
