package de.uni_passau.visit.compression.models;

/**
 * This interface represents a filter that can be used to check if a given
 * compression level shall be created.
 * 
 * @author Florian Schlenker
 *
 */
public interface AbstractCompressionLevelFilter {

	/**
	 * An implementation of this method defines a filter determining if a version of
	 * a model for the compression level with the given identifier shall be created.
	 * 
	 * @param levelTitle
	 *            The identifier of the respective compression level
	 * @return Returns true, if the compressed version for the given level shall be
	 *         created, otherwise false
	 */
	public boolean filterCompressionLevel(String levelTitle);

}
