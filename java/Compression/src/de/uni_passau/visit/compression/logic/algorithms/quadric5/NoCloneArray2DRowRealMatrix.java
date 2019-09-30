package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 * This class represents a dense real matrix and overwrites the @see getData()
 * method of the original implementation for performance purposes: While the
 * original implementation returns a copy of the matrix's data, this
 * implementation just returns a reference to the original data.
 * 
 * @author Florian Schlenker
 *
 */
public class NoCloneArray2DRowRealMatrix extends Array2DRowRealMatrix {

	private static final long serialVersionUID = 8940345464533956296L;

	/**
	 * Create a new RealMatrix using the input array as the underlying data array.
	 * 
	 * The input array is copied, not referenced. This constructor has the same
	 * effect as calling @see Array2DRowRealMatrix(double[][], boolean) with the
	 * second argument set to {@code true}.
	 * 
	 * @param d
	 *            Data for the new matrix.
	 * @throws DimensionMismatchException
	 *             if d is not rectangular.
	 * @throws NoDataException
	 *             if d row or column dimension is zero.
	 * @throws NullArgumentException
	 *             if d is null.
	 */
	public NoCloneArray2DRowRealMatrix(final double[][] d)
			throws DimensionMismatchException, NoDataException, NullArgumentException {
		super(d);
	}

	/**
	 * Create a new RealMatrix using the input array as the underlying data array.
	 * If an array is built specially in order to be embedded in a RealMatrix and
	 * not used directly, the {@code copyArray} may be set to {code false}. This
	 * will prevent the copying and improve performance as no new array will be
	 * built and no data will be copied.
	 * 
	 * @param d
	 *            Data for new matrix.
	 * @param copyArray
	 *            if true, the input array will be copied, otherwise it will be
	 *            referenced.
	 * @throws DimensionMismatchException
	 *             if d is not rectangular.
	 * @throws NoDataException
	 *             if d row or column dimension is zero.
	 * @throws NullArgumentException
	 *             if d is null.
	 */
	public NoCloneArray2DRowRealMatrix(final double[][] d, final boolean copyArray)
			throws DimensionMismatchException, NoDataException, NullArgumentException {
		super(d, copyArray);
	}

	/**
	 * While the original method in the underlying @see Array2DRowRealMatrix class
	 * returns a copy of the data array as return value, this override method just
	 * returns the reference to the original data array.
	 * 
	 * @return returns a reference to the original data array in the underlying object.
	 */
	@Override
	public double[][] getData() {
		return getDataRef();
	}

}
