package de.uni_passau.visit.compression.data;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * This class represents the information on one specific compression level in an
 * media object's technical meta data. This class extends the @see
 * TechnicalMetadataCompressionLevel class and allows changes to some of the
 * data: File type specific meta data can be set after constructing the object.
 * Furthermore files and their sizes of the corresponding compression level can
 * be added iteratively. Before accessing the arrays of all referenced files and
 * their sizes, the method @see close() has to be called.
 * 
 * @author Florian Schlenker
 *
 */
public class EditableTechnicalMetadataCompressionLevel extends TechnicalMetadataCompressionLevel {

	private static final long serialVersionUID = -5554273920153394000L;

	private final transient LinkedList<String> filePathsList;
	private transient long fileSize;
	private transient boolean isClosed = false;

	/**
	 * This constructor initializes all of the object's properties with the given
	 * arguments.
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
	 * @param fileSizes
	 *            A value determined by the summed size of files corresponding with
	 *            this compression level in bytes
	 * @param filePaths
	 *            An array containing the file names of all files corresponding with
	 *            this compression level in the same order as {@code fileSizes}
	 */
	public EditableTechnicalMetadataCompressionLevel(String uploadDate, String accessLevel, String license,
			Object fileTypeSpecificMeta, long fileSize, String[] filePaths) {
		super(uploadDate, accessLevel, license, fileTypeSpecificMeta, fileSize, filePaths);
		this.filePathsList = new LinkedList<>(Arrays.asList(filePaths));
		this.fileSize = fileSize;
	}

	/**
	 * This constructor initializes some of the object's properties with the given
	 * arguments.
	 * 
	 * @param uploadDate
	 *            The upload date of this compressed version of the media object
	 * @param accessLevel
	 *            The access level of this compressed version of the media object
	 * @param license
	 *            The license under which this version of the media object is
	 *            published
	 */
	public EditableTechnicalMetadataCompressionLevel(String uploadDate, String accessLevel, String license) {
		super(uploadDate, accessLevel, license, new Object(), 0L, null);
		this.filePathsList = new LinkedList<>();
		this.fileSize = 0L;
	}

	/**
	 * This method adds another file and its size corresponding with the compression
	 * level to the list of files. The object may not be closed at that moment,
	 * otherwise an @see IllegalStateException will be thrown.
	 * 
	 * @param filePath
	 * @param fileSize
	 */
	public void addFile(String filePath, long fileSize) {
		if (!isClosed) {
			filePathsList.add(filePath);
			this.fileSize += fileSize;
		} else {
			throw new IllegalStateException(
					"Compression level technical metadata object is already closed. No further files can be added.");
		}
	}

	/**
	 * This method sets the summed size of all files corresponding with this
	 * compression level to the given value.
	 * 
	 * @param fileSize
	 *            The summed size of all files corresponding with this compression
	 *            level
	 */
	public void setFileSize(long fileSize) {
		if (!isClosed) {
			this.fileSize = fileSize;
		} else {
			throw new IllegalStateException(
					"Compression level technical metadata object is already closed. Thus file size cannot be modified.");
		}
	}

	/**
	 * This method returns the sum of the sizes of all files corresponding with this
	 * compression level in bytes. It may be called only after closing the object,
	 * otherwise an @see IllegalStateException will be thrown.
	 * 
	 * @return Returns the size of all files corresponding with this compression
	 *         level in bytes
	 */
	@Override
	public long getFileSize() {
		if (isClosed) {
			return fileSize;
		} else {
			throw new IllegalStateException(
					"Compression level technical metadata object hasn't been closed yet. Hence file sizes can't be retrieved.");
		}
	}

	/**
	 * This method returns the file name of all files corresponding with this
	 * compression level. It may be called only after closing the object, otherwise
	 * an @see IllegalStateException will be thrown.
	 * 
	 * @return Returns the file name of all files corresponding with this
	 *         compression level
	 */
	@Override
	public String[] getPaths() {
		if (isClosed) {
			return super.getPaths();
		} else {
			throw new IllegalStateException(
					"Compression level technical metadata object hasn't been closed yet. Hence file paths can't be retrieved.");
		}
	}

	/**
	 * This method closes the object, so that @see getFileSizes() and @see
	 * getFilePaths() may be called. After closing the object no further files can
	 * be added.
	 */
	public void close() {
		isClosed = true;
		paths = filePathsList.toArray(new String[filePathsList.size()]);
	}

	/**
	 * This method changes the compression level's file type specific meta data.
	 * 
	 * @param metaInfo
	 *            The new file type specific meta data that shall be stored with the
	 *            compression level
	 */
	public void setFileTypeSpecificMeta(Object metaInfo) {
		this.fileTypeSpecificMeta = metaInfo;
	}

}
