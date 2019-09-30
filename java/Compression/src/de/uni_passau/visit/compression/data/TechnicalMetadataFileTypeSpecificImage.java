package de.uni_passau.visit.compression.data;

import java.io.Serializable;

/**
 * This class represents additional technical meta data for images regarding one
 * specific compression level. These contain the maximum width and height of the
 * image. All properties are read-only.
 * 
 * The class implements the Serializable-interface enabling easy object
 * serialization.
 *
 * @author Florian Schlenker
 *
 */
public class TechnicalMetadataFileTypeSpecificImage implements Serializable {
	private static final long serialVersionUID = 6141378686647920073L;

	private long maxWidth, maxHeight;

	/**
	 * This constructor initializes the object with the given arguments.
	 * 
	 * @param maxWidth The maximum width of the compressed version of the image in pixels
	 * @param maxHeight The maximum height of the compressed version of the image in pixels
	 */
	public TechnicalMetadataFileTypeSpecificImage(int maxWidth, int maxHeight) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}

	/**
	 * This method returns the maximum width of the compressed version of an image.
	 * 
	 * @return Returns the maximum width of the compressed version of an image in pixels.
	 */
	public long getMaxWidth() {
		return maxWidth;
	}

	/**
	 * This method returns the maximum height of the compressed version of an image.
	 * 
	 * @return Returns the maximum height of the compressed version of an image in pixels.
	 */
	public long getMaxHeight() {
		return maxHeight;
	}

}
