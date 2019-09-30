package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import de.uni_passau.visit.compression.logic.io.AbstractModel;

/**
 * This interface describes a class that can be used to treat a compressed
 * version of a model. A typical job for such a class would be to store this
 * newly created model.
 * 
 * @author Florian Schlenker
 *
 */
public interface QuadricAbstractCompressedModelHandler {

	/**
	 * This method treats the given compressed model with the given count of
	 * vertices.
	 * 
	 * @param model
	 *            The compressed model that shall be treated
	 * @param vertexCount
	 *            The count of vertices of the given model
	 * @return Returns false, if an error occurred during the treatment of the given
	 *         model, otherwise true.
	 */
	public boolean handleCompressedModel(AbstractModel model, int vertexCount);

}
