package de.uni_passau.visit.compression.logic.algorithms.quadric5;

/**
 * This class represents default values for the parameters needed for the
 * quadric edge collapse compression algorithm.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricEdgeCollapseDefaultConfig implements QuadricEdgeCollapseConfig {

	@Override
	public double getTargetsizeBoundaryPenalty() {
		return 100.0;
	}

	@Override
	public double getTargetsizeNormalDifferenceThreshold() {
		return 0.5;
	}

	@Override
	public double getTargetsizeQualityThreshold() {
		return 0.3;
	}

	@Override
	public double getTargetsizeNormalPenalization() {
		return 1000.0;
	}

	@Override
	public double getTargetsizePartitionPenalizationFactor() {
		return 10.0;
	}

}
