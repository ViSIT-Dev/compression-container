package de.uni_passau.visit.compression.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.data.TechnicalMetadata;
import de.uni_passau.visit.compression.exceptions.InvalidTechnicalMetaDataException;
import de.uni_passau.visit.compression.exceptions.TechnicalMetadataNotFoundException;
import de.uni_passau.visit.compression.models.ConfigModel;

/**
 * This class provides methods to retrieve or to store technical meta data in
 * the ViSIT meta database.
 * 
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadataCommunicator {

	private static final Logger log = LogManager.getLogger(TechnicalMetadataCommunicator.class);

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_PAYLOAD_CONTENT_TYPE = "text/json";
	private static final String API_ENDPOINT_MEDIAID_PARAM = "id";

	// object uid: http://visit.de/data/5c8929297f307
	// media uid: http://visit.de/metadb/7764408c-1f7e-4269-8153-05d4a6129b4c

	private final ConfigModel config;

	/**
	 * This constructor creates a new TechnicalMetadataCommunicator using the
	 * settings specified in the given configuration model.
	 * 
	 * @param config
	 *            The configuration model specifying the settings used for the new
	 *            instance
	 */
	public TechnicalMetadataCommunicator(ConfigModel config) {
		this.config = config;
	}

	/**
	 * This method retrieves, parses and returns the technical meta data attached to
	 * the media file with the given media UID.
	 * 
	 * @param mediaUid
	 *            The media UID of the media file, whose technical meta data shall
	 *            be retrieved
	 * @return Returns the technical meta data that have been retrieved from the
	 *         ViSIT meta database
	 * @throws InvalidTechnicalMetaDataException
	 *             if the technical meta data that have been retrieved are invalid
	 * @throws IOException
	 *             if there occurs an error in the network communication
	 * @throws URISyntaxException
	 *             if the URI combined by the API end point and the given media UID
	 *             is invalid
	 */
	public TechnicalMetadata getTechnicalMetadata(String mediaUid)
			throws InvalidTechnicalMetaDataException, TechnicalMetadataNotFoundException, IOException, URISyntaxException {
		String json = fetchJsonStringFromMetaDb(mediaUid);
		return new TechnicalMetadata(json);
	}

	private String fetchJsonStringFromMetaDb(String mediaUid) throws IOException, URISyntaxException, TechnicalMetadataNotFoundException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String fullMediaUid = config.getMetadbApiMediaUidPrefix() + mediaUid;
		URI uri = new URIBuilder(config.getMetadbApiEndpointFetchUrl())
				.setParameter(API_ENDPOINT_MEDIAID_PARAM, fullMediaUid).build();
		HttpGet httpget = new HttpGet(uri);
		httpget.setHeader(HttpHeaders.AUTHORIZATION, config.getMetadbApiAuthString());
		String responseString;
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);

			InputStream in = response.getEntity().getContent();
			String encoding = response.getEntity().getContentEncoding() == null ? DEFAULT_ENCODING
					: response.getEntity().getContentEncoding().getValue();

			if (response.getStatusLine().getStatusCode() == 200) {
				responseString = IOUtils.toString(in, encoding);
			} else if (response.getStatusLine().getStatusCode() == 404) {
				throw new TechnicalMetadataNotFoundException("Could not retrieve technical meta data json. Specified entity doesn't exist.");
			} else {
				throw new TechnicalMetadataNotFoundException("Could not retrieve technical meta data json. HTTP error code: "
						+ response.getStatusLine().getStatusCode());
			}

			response.close();
			httpclient.close();
		} catch (IOException ex) {
			httpclient.close();
			throw new IOException("Could not retrieve technical meta data json. " + ex.getMessage() + " Get request was: " + uri.toString(), ex);
		}

		return responseString;
	}

	/**
	 * This method stores the specified technical meta data in the ViSIT meta
	 * database along with the given media UID.
	 * 
	 * @param mediaUid
	 *            The media UID of the media file the given technical meta data
	 *            refer to
	 * @param technicalMetadata
	 *            The technical meta data that shall be stored in the ViSIT meta
	 *            database
	 * @throws URISyntaxException
	 *             if the URI combined by the API end point and the given media UID
	 *             is invalid
	 * @throws InvalidTechnicalMetaDataException
	 *             if the specified technical meta data are invalid or an error
	 *             occurred during the parsing
	 * @throws IOException
	 *             if there occurs an error in the network communication
	 * 
	 */
	public void putTechnicalMetadata(String mediaUid, TechnicalMetadata technicalMetadata)
			throws URISyntaxException, InvalidTechnicalMetaDataException, IOException {
		String fullMediaUid = config.getMetadbApiMediaUidPrefix() + mediaUid;
		URI uri = new URIBuilder(config.getMetadbApiEndpointSendUrl())
				.setParameter(API_ENDPOINT_MEDIAID_PARAM, fullMediaUid).build();
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader(HttpHeaders.AUTHORIZATION, config.getMetadbApiAuthString());
		String json = technicalMetadata.getJson();
		log.debug("Technical meta data update json: " + json);
		httpput.setEntity(new StringEntity(json, ContentType.create(DEFAULT_PAYLOAD_CONTENT_TYPE, DEFAULT_ENCODING)));

		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = httpclient.execute(httpput);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			String encoding = response.getEntity().getContentEncoding() == null ? DEFAULT_ENCODING
					: response.getEntity().getContentEncoding().getValue();
			String payload = response.getEntity().getContentLength() > 0
					? IOUtils.toString(response.getEntity().getContent(), encoding)
					: "none";
			response.close();
			httpclient.close();
			throw new IOException("Could not update technical meta data json. Response status: "
					+ response.getStatusLine().getStatusCode() + "; Payload: " + payload);
		}

		response.close();
		httpclient.close();
	}

}
