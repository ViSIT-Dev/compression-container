package de.uni_passau.visit.compression.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_passau.visit.compression.data.EditableTechnicalMetadataCompressionLevel;
import de.uni_passau.visit.compression.data.ImageCompressionLevel;
import de.uni_passau.visit.compression.data.JobState;
import de.uni_passau.visit.compression.data.QueueEntry;
import de.uni_passau.visit.compression.data.TechnicalMetadata;
import de.uni_passau.visit.compression.data.TechnicalMetadataCompressionLevel;
import de.uni_passau.visit.compression.data.TechnicalMetadataFileTypeSpecific3D;
import de.uni_passau.visit.compression.data.TechnicalMetadataFileTypeSpecificImage;
import de.uni_passau.visit.compression.exceptions.ImageCompressionException;
import de.uni_passau.visit.compression.exceptions.InvalidTechnicalMetaDataException;
import de.uni_passau.visit.compression.exceptions.ModelReadException;
import de.uni_passau.visit.compression.exceptions.NonManifoldModelException;
import de.uni_passau.visit.compression.exceptions.TechnicalMetadataNotFoundException;
import de.uni_passau.visit.compression.exceptions.UnsupportedModelException;
import de.uni_passau.visit.compression.logic.algorithms.image.ImageCompressor;
import de.uni_passau.visit.compression.logic.algorithms.quadric5.QuadricCompressedModelHandler;
import de.uni_passau.visit.compression.logic.algorithms.quadric5.QuadricEdgeCollapse;
import de.uni_passau.visit.compression.logic.io.ObjModel;
import de.uni_passau.visit.compression.logic.io.ObjReader;
import de.uni_passau.visit.compression.logic.io.ObjWriter;
import de.uni_passau.visit.compression.network.TechnicalMetadataCommunicator;

/**
 * This class represents the model responsible for the procession of the
 * compression jobs stored in the job queue by initiating and controlling the
 * compression of the respective jobs.
 * 
 * @author Florian Schlenker
 *
 */
public class CompressionModel {

	public static final String AUTOMATIC_COMPRESSION_CODE = "Automatisch";

	public static final String ORIGINAL_FILE_INDICATOR = "origin";
	private static final String MIME_TYPE_JPEG = "image/jpeg";
	private static final String MIME_TYPE_PNG = "image/png";
	private static final String MIME_TYPE_OBJ = "text/plain";
	private static final String PNG_EXTENSION = "png";

	private static final int IDLE_SLEEP_TIME = 2000;

	private static final Logger log = LogManager.getLogger(CompressionModel.class);

	private final QueueModel queue;
	private final ConfigModel configModel;
	private final TechnicalMetadataCommunicator techMetaCommunicator;
	private boolean isShutDown = false;
	private boolean isPaused = true;
	private boolean shutdownProcessRemaning = false;

	/**
	 * This constructor creates a new compression model fetch its jobs from the
	 * given @see QueueModel and its configuration from the given @see ConfigModel
	 * 
	 * @param queue
	 *            The queue model from which this compression model shall fetch its
	 *            jobs
	 * @param configModel
	 *            The configuration model from which this compression model shall
	 *            fetch its configuration
	 */
	public CompressionModel(QueueModel queue, ConfigModel configModel) {
		this.queue = queue;
		this.configModel = configModel;
		this.techMetaCommunicator = new TechnicalMetadataCommunicator(configModel);
	}

	/**
	 * This method starts the asynchronous procession of the queue.
	 */
	public void start() {
		new Thread((new Runnable() {
			@Override
			public void run() {

				log.info("Compression handler started.");

				boolean isRunning = true;

				while (isRunning) {
					if (!isPaused && queue.getEnqueuedJobCount() > 0) {
						processJob(queue.getNextEnqueuedJob());
					} else {
						if (isShutDown) {
							isRunning = false;
						} else {
							// log.info("Queue empty. Waiting...");
							try {
								Thread.sleep(IDLE_SLEEP_TIME);
							} catch (InterruptedException e) {
								isRunning = false;
							}
						}
					}

					if (isShutDown && !shutdownProcessRemaning) {
						isRunning = false;
					}
				}

				log.info("Compression handler exiting.");
			}
		})).start();
	}

	/**
	 * This method shuts down the processing of the compression job queue and closes
	 * the compression model. The current compression job will be finished, however.
	 * Depending on the given parameter the whole remaining queue will be processed
	 * or the procession will be shut down after the current job.
	 * 
	 * @param processRemainingQueue
	 *            If true, the whole queue will be processed before shutting down,
	 *            otherwise the compression will be stopped after the current job.
	 */
	public void shutdown(boolean processRemainingQueue) {
		this.shutdownProcessRemaning = processRemainingQueue;
		this.isShutDown = true;
	}

	/**
	 * This method pauses or unpauses the procession of the compression job queue
	 * depending on the given parameter. If the procession shall be paused, this
	 * will happen only after finishing the current job.
	 * 
	 * @param isPaused
	 *            If true, the procession will be paused after to current job,
	 *            otherwise the queue procession will be unpaused.
	 */
	public void setPause(boolean isPaused) {
		this.isPaused = isPaused;
	}

	/**
	 * This method processes the given compression job depending on its file type.
	 * 
	 * @param job
	 *            The queue entry of the compression job that shall be compressed.
	 */
	private void processJob(QueueEntry job) {
		queue.setJobState(job.getId(), JobState.PROCESSING);
		log.info("Started processing job " + job.getId() + " (" + job.getJob().getTitle() + ")");

		log.info("Fetching technical meta data...");
		TechnicalMetadata techMeta = getTechnicalMetadata(job.getJob().getMediaUid());
		boolean success = techMeta != null;

		if (success) {
			switch (job.getJob().getMimeType().toLowerCase()) {
			case MIME_TYPE_JPEG:
			case MIME_TYPE_PNG:
				success = processJobPicture(job, techMeta);
				break;
			case MIME_TYPE_OBJ:
				success = processJobModel(job, techMeta);
				break;
			default:
				log.error("Error while compressing job " + job.getId() + ": Mime type not supported");
				success = false;
			}
		}

		if (success) {
			success = updateTechnicalMetadata(job.getJob().getMediaUid(), techMeta);
		}

		queue.setJobState(job.getId(), success ? JobState.COMPLETED : JobState.ERROR);
		log.info("Finished processing job " + job.getId() + " (" + job.getJob().getTitle() + ")");
	}

	/**
	 * This method retrieves the technical meta data for the given media file.
	 * 
	 * @param mediaUid
	 *            The UID of the media file, whose technical meta data shall be
	 *            retrieved
	 * @return The technical meta data, if they could be retrieved, otherwise null
	 */
	private TechnicalMetadata getTechnicalMetadata(String mediaUid) {
		try {
			return techMetaCommunicator.getTechnicalMetadata(mediaUid);
		} catch (URISyntaxException ex) {
			log.error("Error during technical meta data retrieval: The URI built with given media uid '" + mediaUid
					+ "' is invalid: " + ex.getMessage());
		} catch (IOException ex) {
			log.error("Error during technical meta data retrieval: Could not retrieve meta data from server: "
					+ ex.getMessage());
		} catch (InvalidTechnicalMetaDataException ex) {
			log.error(
					"Error during technical meta data retrieval: The received technical meta data serialization is invalid: "
							+ ex.getMessage());
		} catch (TechnicalMetadataNotFoundException ex) {
			log.error("Error during technical meta data retrieval: " + ex.getMessage());
		}

		return null;
	}

	/**
	 * This method uploads the given technical meta data for the given media file.
	 * 
	 * @param mediaUid
	 *            The UID of the media file, whose technical meta data shall be
	 *            updated
	 * @param techMeta
	 *            The new technical meta data for the media file with the given UID
	 * @return Returns true, if the meta data upload was successful, otherwise false
	 */
	private boolean updateTechnicalMetadata(String mediaUid, TechnicalMetadata techMeta) {
		boolean success = true;

		try {
			techMetaCommunicator.putTechnicalMetadata(mediaUid, techMeta);
		} catch (URISyntaxException ex) {
			log.error("Error during technical meta data update: The URI built with given media uid '" + mediaUid
					+ "' is invalid: " + ex.getMessage());
			success = false;
		} catch (IOException ex) {
			log.error("Error during technical meta data update: Could not update meta data at server: "
					+ ex.getMessage());
			success = false;
		} catch (InvalidTechnicalMetaDataException ex) {
			log.error(
					"Error during technical meta data update: The built technical meta data serialization is invalid: "
							+ ex.getMessage());
			success = false;
		}

		return success;
	}

	/**
	 * This method processes the compression job enclosed in the given queue entry,
	 * which has to be an image compression job.
	 * 
	 * @param job
	 *            The queue entry enclosing compression job that shall be processed
	 * @param techMeta
	 *            The technical meta data for the media file that shall be processed
	 * @return Returns true if the compression was successful, otherwise false
	 */
	private boolean processJobPicture(QueueEntry job, TechnicalMetadata techMeta) {
		String extension = getPictureFilenameExtension(job, techMeta);
		FilenameGeneratorImage filenameGen = new FilenameGeneratorImage(configModel, job.getJob(), extension, ORIGINAL_FILE_INDICATOR);

		if (extension == null) {
			log.error("Could not compress image: Technical meta data are required to reference exactly one file");
			return false;
		}

		ImageCompressor compressor = new ImageCompressor();
		ImageCompressionLevel[] levels = configModel.getImageCompressionLevels();

		File[] outputPaths = new File[levels.length];
		String[] outputFilenames = new String[levels.length];
		for (int i = 0; i < levels.length; ++i) {
			outputPaths[i] = filenameGen.getImageFilePath(levels[i].getTitle());
			outputFilenames[i] = filenameGen.getImageFilename(levels[i].getTitle());
		}

		// filter compression levels to avoid duplicates
		AbstractCompressionLevelFilter filter = new AbstractCompressionLevelFilter() {
			@Override
			public boolean filterCompressionLevel(String levelTitle) {
				return !techMeta.hasCompressionLevel(levelTitle);
			}
		};

		try {
			compressor.compressImageFile(filenameGen.getImageFilePath(), outputPaths, levels, filter);
		} catch (ImageCompressionException e) {
			log.error("Could not compress image '" + filenameGen.getImageFilename() + "': " + e.getMessage());
			return false;
		}

		// register levels in technical meta data
		TechnicalMetadataCompressionLevel originalLevelInfo = techMeta.getCompressionLevel(ORIGINAL_FILE_INDICATOR);
		String uploadTimestamp = String.valueOf(System.currentTimeMillis() / 1000L);
		for (int i = 0; i < levels.length; ++i) {
			if (filter.filterCompressionLevel(levels[i].getTitle()) && outputPaths[i].exists()) {
				long fileSize = FileUtils.sizeOf(outputPaths[i]);
				TechnicalMetadataCompressionLevel compressionLevelInfo = new TechnicalMetadataCompressionLevel(
						uploadTimestamp, originalLevelInfo.getAccessLevel(), originalLevelInfo.getLicense(),
						new TechnicalMetadataFileTypeSpecificImage(levels[i].getMaxWidth(), levels[i].getMaxHeight()),
						fileSize, new String[] { outputFilenames[i] });
				techMeta.addCompressionLevel(levels[i].getTitle(), compressionLevelInfo);
			}
		}

		return true;

	}

	/**
	 * This method returns the extension of the image file that shall be compressed
	 * by the compression job enclosed in the given queue entry, which has to be an
	 * image compression job. Since the exact JPEG-extension can't be derived from
	 * job's MIME-type, get this information from technical meta data.
	 * 
	 * @param job
	 *            The queue entry enclosing the current compression job
	 * @param techMeta
	 *            The technical meta data of the object of the current compression
	 *            job. Can be null, if the file being compressed is a PNG-file
	 * @return Returns the file's extension including the leading dot
	 */
	private String getPictureFilenameExtension(QueueEntry job, TechnicalMetadata techMeta) {
		if (job.getJob().getMimeType().equals(MIME_TYPE_PNG)) {
			return PNG_EXTENSION;
		} else {
			String[] filePaths = techMeta.getCompressionLevel(ORIGINAL_FILE_INDICATOR).getPaths();
			if (filePaths.length != 1) {
				return null;
			} else {
				return FilenameUtils.getExtension(filePaths[0]);
			}
		}
	}

	/**
	 * This method processes the compression job enclosed in the given queue entry,
	 * which has to be an 3D-model compression job.
	 * 
	 * @param job
	 *            The queue entry enclosing compression job that shall be processed
	 * @param techMeta
	 *            The technical meta data for the media file that shall be processed
	 * @return Returns true if the compression was successful, otherwise false
	 */
	private boolean processJobModel(QueueEntry job, TechnicalMetadata techMeta) {
		boolean success = true;
		FilenameGenerator3D filenameGen = new FilenameGenerator3D(configModel.getMediaFileRoot(), job.getJob().getBasePath(), job.getJob().getObjectUid(), job.getJob().getMediaUid(), ORIGINAL_FILE_INDICATOR);
		String filename = filenameGen.getObjFilename();
		HashSet<Integer> levels = getModelCompressionLevels(job);

		try {
			ObjModel in = ObjReader.read(filenameGen.getObjFilePath().getAbsolutePath(), ".");
			Pair<ObjModel, TechnicalMetadataCompressionLevel> updatedModelAndTechData = updateInitialReferences(job, in,
					techMeta.getCompressionLevel(ORIGINAL_FILE_INDICATOR), filenameGen);

			if (updatedModelAndTechData != null) {
				techMeta.addCompressionLevel(ORIGINAL_FILE_INDICATOR, updatedModelAndTechData.getRight());
				if (levels.size() > 0) {
					// filter compression levels to avoid duplicates
					AbstractCompressionLevelFilter filter = new AbstractCompressionLevelFilter() {
						@Override
						public boolean filterCompressionLevel(String levelTitle) {
							return !techMeta.hasCompressionLevel(levelTitle);
						}
					};

					QuadricCompressedModelHandler compressedModelHandler = new QuadricCompressedModelHandler(filter,
							updatedModelAndTechData.getLeft().getHeader(), configModel.getTextureLevelLimits(),
							configModel.getTextureLevelSizes(), techMeta, filenameGen);

					QuadricEdgeCollapse decimator = new QuadricEdgeCollapse(configModel);

					try {
						success &= decimator.compute(in, levels.toArray(new Integer[] {}), compressedModelHandler);
					} catch (NonManifoldModelException ex) {
						log.error(
								"Error while processing non-manifold OBJ-file (" + filename + "): " + ex.getMessage());
						success = false;
					}

					compressedModelHandler.compressTextures();
					compressedModelHandler.updateTechnicalMetadata();
				}
			}

		} catch (FileNotFoundException ex) {
			log.error("Could not find specified file: " + filename + "; " + ex.getMessage());
			success = false;
		} catch (ModelReadException ex) {
			log.error("Error while reading model: " + filename + "; " + ex.getMessage());
			success = false;
		} catch (InvalidAlgorithmParameterException ex) {
			log.error("Error while compressing model: " + ex.getMessage());
			success = false;
		} catch (InvalidTechnicalMetaDataException ex) {
			log.error("Invalid technical meta data: " + ex.getMessage());
			success = false;
		}

		return success;
	}

	/**
	 * Since all filenames have been changed during upload, the references in the
	 * OBJ-file to MTL-files and in MTL-files to texture files are broken and can be
	 * fixed with this method. Both OBJ- and MTL-files are updated on the hard drive
	 * and an updated model and updated technical meta data for the original
	 * compression level containing additional information are returned. This method
	 * internally calls the @see updateMtlReferencesInObjFile method and treats
	 * potentially occurring errors.
	 * 
	 * @param job
	 *            The queue entry enclosing the compression job of the file, whose
	 *            references shall be updated
	 * @param in
	 *            The original OBJ-model with the broken references
	 * @param originalTechMeta
	 *            The original technical meta data of the model
	 * @param filenameGen
	 *            The filename generator used to retrieve the path and filename of
	 *            the model's MTL-file.
	 * @return Returns a pair consisting of the updated OBJ-model and the updated
	 *         technical meta data
	 * @throws InvalidTechnicalMetaDataException
	 *             If the given original technical meta data are null
	 */
	private Pair<ObjModel, TechnicalMetadataCompressionLevel> updateInitialReferences(QueueEntry job, ObjModel in,
			TechnicalMetadataCompressionLevel originalTechMeta, FilenameGenerator3D filenameGen)
			throws InvalidTechnicalMetaDataException {
		Pair<ObjModel, TechnicalMetadataCompressionLevel> updatedModelAndTechData = null;
		if (originalTechMeta != null) {
			try {
				updatedModelAndTechData = updateMtlReferencesInObjFile(job, in, originalTechMeta, filenameGen);
			} catch (FileNotFoundException ex) {
				log.error("Could not find referenced MTL-file: " + ex.getMessage());
			} catch (IOException ex) {
				log.error("Error while reading or writing file: " + ex.getMessage());
			} catch (UnsupportedModelException ex) {
				log.error("Error while updating references in original model (" + filenameGen.getObjFilename() + "): "
						+ ex.getMessage());
			}
		} else {
			throw new InvalidTechnicalMetaDataException(
					"Given technical meta data contain no information about original model.");
		}

		return updatedModelAndTechData;
	}

	/**
	 * Since all filenames have been changed during upload, the references in the
	 * OBJ-file to MTL-files and in MTL-files to texture files are broken and can be
	 * fixed with this method. Both OBJ- and MTL-files are updated on the hard drive
	 * and an updated model and updated technical meta data for the original
	 * compression level containing additional information are returned.
	 * 
	 * @param job
	 *            The queue entry enclosing the compression job of the file, whose
	 *            references shall be updated
	 * @param original
	 *            The original OBJ-model with the broken references
	 * @param originalTechData
	 *            The original technical meta data of the model
	 * @param filenameGen
	 *            The filename generator used to create the expected MTL-filename
	 * @return Returns a pair consisting of the updated OBJ-model and the updated
	 *         technical meta data
	 * @throws UnsupportedModelException
	 *             If the OBJ-file references to more than one MTL-file or the
	 *             MTL-file references to more than one texture file
	 * @throws IOException
	 *             If the OBJ-file or the MTL-file could not be written on the hard
	 *             drive
	 */
	private Pair<ObjModel, TechnicalMetadataCompressionLevel> updateMtlReferencesInObjFile(QueueEntry job,
			ObjModel original, TechnicalMetadataCompressionLevel originalTechData, FilenameGenerator3D filenameGen)
			throws UnsupportedModelException, IOException {

		// Build header of the updated OBJ-file
		Scanner oldHeader = new Scanner(original.getHeader());
		StringBuilder newHeader = new StringBuilder();
		String mtlFilename = null;
		final String expectedMtlFilename = filenameGen.getMtlFilename();

		while (oldHeader.hasNextLine()) {
			String line = oldHeader.nextLine();
			String[] tokens = line.split(" ");
			if (tokens.length >= 2 && tokens[0].equals(ObjModel.MTL_DECLARATION_PREFIX)) {
				if (mtlFilename == null) {
					mtlFilename = tokens[1];
					newHeader.append(
							ObjModel.MTL_DECLARATION_PREFIX + " " + expectedMtlFilename + System.lineSeparator());
				} else {
					oldHeader.close();
					throw new UnsupportedModelException(
							"File exchange system only supports object files with one referenced material file.");
				}
			} else {
				newHeader.append(line + System.lineSeparator());
			}
		}

		oldHeader.close();

		if (mtlFilename != null && !mtlFilename.equals(expectedMtlFilename)) {
			// create and write updated OBJ-file
			ObjModel updated = new ObjModel(original.getVertices(), original.getNormals(), original.getTextureCoords(),
					original.getFaces(), newHeader.toString());

			try {
				ObjWriter.write(filenameGen.getObjFilePath().getAbsolutePath(), updated);
			} catch (IOException ex) {
				throw new IOException("Could not write update OBJ-file: " + ex.getMessage(), ex);
			}

			Pair<String, Long> mtlFileData;

			// update MTL-file
			try {
				mtlFileData = updateTextureReferenceInMtlFile(filenameGen);
			} catch (IOException ex) {
				throw new IOException("Error while accessing referenced MTL-file: " + ex.getMessage(), ex);
			}

			// create updated technical meta data
			EditableTechnicalMetadataCompressionLevel updatedTechData = new EditableTechnicalMetadataCompressionLevel(
					originalTechData.getUploadDate(), originalTechData.getAccessLevel(), originalTechData.getLicense());
			updatedTechData.setFileTypeSpecificMeta(
					new TechnicalMetadataFileTypeSpecific3D(original.getVertices().size(), original.getFaces().size()));
			updatedTechData.addFile(filenameGen.getObjFilename(), FileUtils.sizeOf(filenameGen.getObjFilePath()));
			updatedTechData.addFile(mtlFileData.getLeft(), mtlFileData.getRight());

			for (int i = 0; i < originalTechData.getPaths().length; ++i) {
				if (!originalTechData.getPaths()[i].endsWith(FilenameGenerator3D.OBJ_EXTENSION)
						&& !originalTechData.getPaths()[i].endsWith(FilenameGenerator3D.MTL_EXTENSION)) {
					updatedTechData.addFile(originalTechData.getPaths()[i], FileUtils
							.sizeOf(new File(configModel.getMediaFileRoot(), originalTechData.getPaths()[i])));
				}
			}

			updatedTechData.close();

			return Pair.of(updated, updatedTechData);
		} else {
			// if no mtl file is referenced or reference has already been updated, no
			// updates are necessary. In this case assume texture reference in mtl file has
			// already been upated
			return Pair.of(original, originalTechData);
		}

	}

	/**
	 * This method updates the texture file reference in model's MTL-file containing
	 * a broken reference to a texture file according to the current compression
	 * job.
	 * 
	 * @param filenameGen
	 *            The filename generator used to retrieve the paths to material and
	 *            texture files
	 * @return Returns a pair consisting of the new MTL-file's filename and its size
	 * @throws IOException
	 *             if the updated MTL-file can't be written
	 * @throws UnsupportedModelException
	 *             If the MTL-file references to more than one texture file, which
	 *             is not supported by the ViSIT file management system
	 */
	private Pair<String, Long> updateTextureReferenceInMtlFile(FilenameGenerator3D filenameGen)
			throws IOException, UnsupportedModelException {
		Scanner s = new Scanner(filenameGen.getMtlFilePath());
		StringBuilder mtlBuilder = new StringBuilder();

		boolean found = false;
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] tokens = line.split(" ");

			if (tokens.length >= 2 && tokens[0].equals(ObjModel.MTL_TEXTURE_IMPORT_PREFIX)) {
				if (found) {
					s.close();
					throw new UnsupportedModelException(
							"File system only supports object files with one referenced texture file.");
				} else {
					String suffix = FilenameUtils.getExtension(tokens[1]);
					tokens[1] = filenameGen.getTextureFilename(suffix);
					mtlBuilder.append(String.join(" ", tokens));
					found = true;
				}
			} else {
				mtlBuilder.append(line);
			}

			mtlBuilder.append(System.lineSeparator());
		}

		s.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(filenameGen.getMtlFilePath()));
		writer.write(mtlBuilder.toString());
		writer.close();

		return Pair.of(filenameGen.getMtlFilename(), FileUtils.sizeOf(filenameGen.getMtlFilePath()));
	}

	/**
	 * This method returns a set containing the vertex counts of all desired
	 * compression levels of the given compression job.
	 * 
	 * @param job
	 *            The queue entry encapsulating the compression job describing the
	 *            compression levels
	 * @return Returns a set containing the vertex counts of all desired compression
	 *         levels
	 */
	private HashSet<Integer> getModelCompressionLevels(QueueEntry job) {
		List<String> levelList = new ArrayList<>(Arrays.asList(job.getJob().getLevels()));
		if (levelList.contains(AUTOMATIC_COMPRESSION_CODE)) {
			levelList.addAll(Arrays.asList(configModel.getDefaultLevels()));
		}

		levelList.addAll(Arrays.asList(job.getJob().getLevels()));

		HashSet<Integer> levels = new HashSet<>();
		for (String level : levelList) {
			try {
				levels.add(Integer.parseInt(level));
			} catch (NumberFormatException ex) {
				// ignore string values like "Automatisch"
			}
		}

		return levels;
	}
}
