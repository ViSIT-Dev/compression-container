package de.uni_passau.visit.compression.logic.algorithms.quadric5;

/**
 * This interface represents the parameters needed for the quadric edge collapse
 * compression algorithm.
 * 
 * @author Florian Schlenker
 *
 */
public interface QuadricEdgeCollapseConfig {
	/**
	 * The value returned by this method describes the penalty for deviations in the
	 * model's boundary.
	 * 
	 * @return the penalty for deviations in the model's boundary
	 */
	public double getTargetsizeBoundaryPenalty();

	/**
	 * The value returned by this method describes the maximum face normal
	 * deviation. Higher deviations will be penalized.
	 * 
	 * @return the threshold for face normal deviations
	 */
	public double getTargetsizeNormalDifferenceThreshold();

	/**
	 * The value returned by this method describes the triangle shape quality
	 * threshold. Triangles with a quality less than this value will be penalized.
	 * 
	 * @return the triangle shape quality threshold
	 */
	public double getTargetsizeQualityThreshold();

	/**
	 * The value returned by this method describes the penalization factor for
	 * collapses producing faces with a normal deviation higher than the specified
	 * threshold.
	 * 
	 * @return Returns the penalization factor for high normal deviations
	 */
	public double getTargetsizeNormalPenalization();

	/**
	 * The value returned by this method describes the penalization factor for
	 * collapses along texture partition borders. These factor influences the
	 * collapse's cost quadratically.
	 * 
	 * @return Returns the penalization factor for collapses along texture partition
	 *         borders
	 */
	public double getTargetsizePartitionPenalizationFactor();

}
