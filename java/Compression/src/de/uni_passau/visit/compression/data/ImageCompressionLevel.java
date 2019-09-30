package de.uni_passau.visit.compression.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a compression level of a picture consisting of the
 * picture's maximum width and height as well as a title for the compression
 * level.
 * 
 * @author Florian Schlenker
 *
 */
public class ImageCompressionLevel {

	private final int maxWidth, maxHeight;
	private final String title;

	/**
	 * This constructor initializes all fields with the given arguments.
	 * 
	 * @param maxWidth
	 *            The maximum width of the compressed picture
	 * @param maxHeight
	 *            The maximum height of the compressed picture
	 * @param title
	 *            The title for the compression level. Has to be a string consisting
	 *            of A-Z, a-z, numbers, underscores and hyphens.
	 */
	@JsonCreator
	public ImageCompressionLevel(@JsonProperty("maxWidth") int maxWidth, @JsonProperty("maxHeight") int maxHeight,
			@JsonProperty("title") String title) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.title = title;
	}

	/**
	 * This method returns the maximum width of pictures compressed at this level.
	 * 
	 * @return Returns the maximum width of this level's pictures in pixels
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * This method returns the maximum height of pictures compressed at this level.
	 * 
	 * @return Returns the maximum height of this level's pictures in pixels
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * This method returns the title of this compression level. The title is a
	 * string consisting of A-Z, a-z, numbers, underscores and hyphens.
	 * 
	 * @return Returns the title of this compression level.
	 */
	public String getTitle() {
		return title;
	}
}
