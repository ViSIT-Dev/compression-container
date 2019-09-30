package de.uni_passau.visit.compression.logic.data;

import java.util.Comparator;

/**
 * This comparator class allows to compare two vertices according to their
 * index.
 * 
 * @author Florian Schlenker
 *
 */
public class VertexIndexComparator implements Comparator<Vertex> {

	@Override
	public int compare(Vertex v, Vertex w) {
		return Integer.compare(v.getIndex(), w.getIndex());
	}

}
