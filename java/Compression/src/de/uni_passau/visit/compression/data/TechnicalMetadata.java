package de.uni_passau.visit.compression.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.visit.compression.exceptions.InvalidTechnicalMetaDataException;

/**
 * This class represents technical meta data regarding one specific media
 * object. Since for the compression system only the different compression
 * levels are relevant, this class only provides methods to access these
 * compression levels. Further information can't be accessed.
 * 
 * The methods contained in this class provide functionality to retrieve the
 * data stored for one specific compression level, to add a new compression
 * level and to check for the existence of a specific compression level.
 * Furthermore the information stored in this object can be converted to a
 * JSON-string.
 * 
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadata {

	private static final String COMPRESSION_LEVELS_CONTAINER_IDENTIFIER = "files";

	private final HashMap<String, Object> data;
	private final ObjectMapper mapper;
	private final HashMap<String, TechnicalMetadataCompressionLevel> compressionLevels;

	/**
	 * This constructor initializes the technical meta data object with the
	 * information stored in the given JSON-argument.
	 * 
	 * @param json
	 *            A JSON-string compliant with the ViSIT-specification for technical
	 *            meta data JSON-strings
	 * @throws IOException
	 *             Will be thrown if an error occurs while reading the given
	 *             JSON-string.
	 * @throws InvalidTechnicalMetaDataException
	 *             Will be thrown if the given JSON-string is incorrect or is not
	 *             compliant with the ViSIT-specification for technical meta data
	 *             JSON-strings
	 */
	public TechnicalMetadata(String json) throws IOException, InvalidTechnicalMetaDataException {
		JsonFactory factory = new JsonFactory();
		mapper = new ObjectMapper(factory);

		try {
			data = getMapFromJson(json);
			compressionLevels = getCompressionLevelsFromMap(data);
		} catch (JsonParseException | JsonMappingException ex) {
			throw new InvalidTechnicalMetaDataException(
					"The given technical meta data have an invalid format and could not be parsed: " + ex.getMessage()
							+ " (" + json + ").",
					ex);
		}
	}

	/**
	 * This method returns a ViSIT-specification compliant JSON-string containing
	 * all the information stored in this object.
	 * 
	 * @return Returns a JSON-representation of the data stored in this object,
	 *         which is compliant with the ViSIT-specification for technical meta
	 *         data.
	 * @throws InvalidTechnicalMetaDataException
	 *             Will be thrown if an error occurs during JSON conversion or if
	 *             the data stored within this object can't be converted to a
	 *             ViSIT-compliant JSON-string.
	 */
	public String getJson() throws InvalidTechnicalMetaDataException {
		try {
			updateMapFromCompressionLevels(compressionLevels);
			return mapper.writeValueAsString(data);
		} catch (JsonProcessingException ex) {
			throw new InvalidTechnicalMetaDataException(
					"The technical meta data could not be converted to json format.", ex);
		}
	}

	/**
	 * This method checks, if this technical meta data object contains information
	 * regarding one specific compression level.
	 * 
	 * @param identifier
	 *            This identifier of the compression level that shall be checked
	 * @return Returns {@code true}, if the object contains information regarding
	 *         the given compression level, otherwise {@code false}.
	 */
	public boolean hasCompressionLevel(String identifier) {
		return compressionLevels.containsKey(identifier);
	}

	/**
	 * This method returns this media object's technical meta data regarding the
	 * given compression level.
	 * 
	 * @param identifier
	 *            The identifier for the compression level, whose data shall be
	 *            retrieved
	 * @return Returns the technical meta data regarding the given compression level
	 *         if existing, otherwise null.
	 */
	public TechnicalMetadataCompressionLevel getCompressionLevel(String identifier) {
		return compressionLevels.get(identifier);
	}

	/**
	 * This method adds a new compression level with the given identifier and the
	 * given level-specific data to the technical meta data object. If data for this
	 * level are already existent, they will be overwritten by this call.
	 * 
	 * @param identifier
	 *            The identifier for the new compression level
	 * @param compressionLevel
	 *            The level-specific data stored for the given compression level
	 */
	public void addCompressionLevel(String identifier, TechnicalMetadataCompressionLevel compressionLevel) {
		compressionLevels.put(identifier, compressionLevel);
	}

	private HashMap<String, Object> getMapFromJson(String json)
			throws IOException, JsonParseException, JsonMappingException {
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};

		return mapper.readValue(json, typeRef);
	}

	private HashMap<String, TechnicalMetadataCompressionLevel> getCompressionLevelsFromMap(HashMap<String, Object> data)
			throws JsonProcessingException, IOException, InvalidTechnicalMetaDataException {
		HashMap<String, TechnicalMetadataCompressionLevel> compressionLevels = new HashMap<>();

		if (data.containsKey(COMPRESSION_LEVELS_CONTAINER_IDENTIFIER)) {
			Object container = data.get(COMPRESSION_LEVELS_CONTAINER_IDENTIFIER);
			if (container instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<Object, Object> containerMap = (Map<Object, Object>) container;

				for (Object identifier : containerMap.keySet()) {
					String compressionLevelJson = mapper.writeValueAsString(containerMap.get(identifier));
					compressionLevels.put(identifier.toString(),
							mapper.readValue(compressionLevelJson, TechnicalMetadataCompressionLevel.class));
				}
			} else {
				throw new InvalidTechnicalMetaDataException(
						"The technical meta data contain invalid information about distinct files. Corresponding entry has to be of type object.");
			}
		} else {
			throw new InvalidTechnicalMetaDataException(
					"The technical meta data contain no information about distinct files.");
		}

		return compressionLevels;
	}

	private void updateMapFromCompressionLevels(HashMap<String, TechnicalMetadataCompressionLevel> compressionLevels)
			throws JsonProcessingException {
		HashMap<String, TechnicalMetadataCompressionLevel> jsonCompressionLevels = new HashMap<>();

		for (String identifier : compressionLevels.keySet()) {
			jsonCompressionLevels.put(identifier, compressionLevels.get(identifier));
		}

		data.put(COMPRESSION_LEVELS_CONTAINER_IDENTIFIER, jsonCompressionLevels);
	}

	@Override
	public String toString() {
		return "TechnicalMetadata [data=" + data.toString() + ", mapper=" + mapper.toString() + ", compressionLevels="
				+ compressionLevels.toString() + "]";
	}
}
