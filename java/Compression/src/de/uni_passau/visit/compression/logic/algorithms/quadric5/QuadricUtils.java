package de.uni_passau.visit.compression.logic.algorithms.quadric5;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import de.uni_passau.visit.compression.logic.data.Face;
import de.uni_passau.visit.compression.logic.data.TextureCoords;
import de.uni_passau.visit.compression.logic.data.Vertex;

/**
 * This class offers help functions for compressing models via the quadric edge
 * collapse approach. All member of this class can be accessed in a static way.
 * 
 * @author Florian Schlenker
 *
 */
public class QuadricUtils {
	/**
	 * This method returns the element-wise sum of the two given quadrics. The
	 * length of the quadrics doesn't matter, however they are assumed to have equal
	 * length. Otherwise a @see IndexOutOfBounds exception can occur.
	 * 
	 * @param a
	 *            One of the two quadrics that shall be summed
	 * @param b
	 *            One of the two quadrics that shall be summed
	 * @return A new quadric containing the sum of the two input quadrics
	 */
	public static double[] sumQuadrics(double[] a, double[] b) {
		double[] quadric = new double[a.length];

		for (int i = 0; i < quadric.length; ++i) {
			quadric[i] = a[i] + b[i];
		}

		return quadric;
	}

	/**
	 * This method returns a @see RealMatrix representing the derivation of the
	 * given 3-dimensional quadric in the projective space.
	 * 
	 * @param quadric
	 *            The 3-dimensional quadric whose derivation one wants to retrieve
	 * @return Returns the derivation of the given 3-dimensional quadric as @see
	 *         RealMatrix
	 */
	public static RealMatrix derivationFromValues(double[] quadric) {
		double[][] Aa = { { quadric[0], quadric[4], quadric[7], quadric[9] },
				{ quadric[4], quadric[1], quadric[5], quadric[8] }, { quadric[7], quadric[5], quadric[2], quadric[6] },
				{ 0, 0, 0, 1 } };
		return new NoCloneArray2DRowRealMatrix(Aa, false);
	}

	/**
	 * This method returns a @see RealMatrix representing the given 3-dimensional
	 * quadric in the projective space.
	 * 
	 * @param quadric
	 *            The 3-dimensional quadric whose matrix form one wants to retrieve
	 * @return Returns the given 3-dimensional quadric as @see RealMatrix
	 */
	public static RealMatrix quadricFromValues(double[] quadric) {
		double[][] Aa = { { quadric[0], quadric[4], quadric[7], quadric[9] },
				{ quadric[4], quadric[1], quadric[5], quadric[8] }, { quadric[7], quadric[5], quadric[2], quadric[6] },
				{ quadric[9], quadric[8], quadric[6], quadric[3] } };
		return new NoCloneArray2DRowRealMatrix(Aa, false);
	}

	/**
	 * This method returns a @see RealMatrix representing the quadratic part of the
	 * given 5-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric whose quadratic part one wants to
	 *            retrieve
	 * @return Returns the quadratic part of the given 5-dimensional quadric as @see
	 *         RealMatrix
	 */
	public static RealMatrix quadric5MatrixFromValues(double[] quadric) {
		double[][] Aa = { { quadric[0], quadric[5], quadric[9], quadric[12], quadric[14] },
				{ quadric[5], quadric[1], quadric[6], quadric[10], quadric[13] },
				{ quadric[9], quadric[6], quadric[2], quadric[7], quadric[11] },
				{ quadric[12], quadric[10], quadric[7], quadric[3], quadric[8] },
				{ quadric[14], quadric[13], quadric[11], quadric[8], quadric[4] } };
		return new NoCloneArray2DRowRealMatrix(Aa, false);
	}

	/**
	 * This method returns a @see RealVector representing the linear part of the
	 * given 5-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric whose linear part one wants to retrieve
	 * @return Returns the linear part of the given 5-dimensional quadric as @see
	 *         RealVector
	 */
	public static RealVector quadric5VectorFromValues(double[] quadric) {
		double[] a = new double[] { quadric[15], quadric[16], quadric[17], quadric[18], quadric[19] };
		return new ArrayRealVector(a);
	}

	/**
	 * This method returns a scalar representing the constant term of the given
	 * 5-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric whose constant term one wants to
	 *            retrieve
	 * @return Returns the constant term of the given 5-dimensional quadric
	 */
	public static double quadricConstFromValues(double[] quadric) {
		return quadric[20];
	}

	/**
	 * This method computes and returns the minimum of the given 5-dimensional
	 * quadric, if the respective system of equations has full rank. Otherwise one
	 * of the given set of vertices or the average of these vertices will be
	 * returned, whatever results in the lowest cost.
	 * 
	 * Use this function when no distinct texture coordinates are given for the
	 * vertices. Texture coordinates will be used in regular case (as encoded in the
	 * quadric) but not in fallback case, where zeroes will be returned as texture
	 * coordinates.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, whose minimum one wants to retrieve
	 * @param a
	 *            One of the two vertices used as fallback
	 * @param b
	 *            One of the two vertices used as fallback
	 * @return Returns the minimum of the given quadric, if such a minimum exists
	 *         and is unique, or a fallback point otherwise
	 */
	public static RealVector getMinimumForQuadric5(double[] quadric, QuadricVertex a, QuadricVertex b) {
		RealMatrix qA = quadric5MatrixFromValues(quadric);
		RealVector qb = quadric5VectorFromValues(quadric);

		DecompositionSolver solver = new LUDecomposition(qA).getSolver();
		if (solver.isNonSingular()) {
			return solver.solve(qb.mapMultiply(-1.0));
		} else {
			RealVector vv1 = new ArrayRealVector(
					new double[] { a.getCoords()[0], a.getCoords()[1], a.getCoords()[2], 0, 0 });
			RealVector vv2 = new ArrayRealVector(
					new double[] { b.getCoords()[0], b.getCoords()[1], b.getCoords()[2], 0, 0 });
			RealVector vvM = vv1.add(vv2).mapMultiply(0.5);
			double err1 = getCostOfContraction(quadric, vv1);
			double err2 = getCostOfContraction(quadric, vv2);
			double errM = getCostOfContraction(quadric, vvM);

			if (err1 <= err2 && err1 <= errM) {
				return vv1;
			} else if (err2 <= errM) {
				return vv2;
			} else {
				return vvM;
			}
		}
	}

	/**
	 * This method computes and returns the minimum of the given 5-dimensional
	 * quadric, if the respective system of equations has full rank. Otherwise one
	 * of the given set of vertices or the average of these vertices will be
	 * returned, whatever results in the lowest cost.
	 * 
	 * Use this function when distinct texture coords are given for both vertices.
	 * These will be used in regular and in fallback case.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, whose minimum one wants to retrieve
	 * @param geoA
	 *            One of the two vertices used as fallback
	 * @param geoB
	 *            One of the two vertices used as fallback
	 * @param texA
	 *            The texture coordinate corresponding with geoA used as fallback
	 * @param texB
	 *            The texture coordinate corresponding with geoB used as fallback
	 * @return Returns the minimum of the given quadric, if such a minimum exists
	 *         and is unique, or a fallback point otherwise
	 */
	public static RealVector getMinimumForQuadric5(double[] quadric, QuadricVertex geoA, QuadricVertex geoB,
			double[] texA, double[] texB) {
		RealMatrix qA = quadric5MatrixFromValues(quadric);
		RealVector qb = quadric5VectorFromValues(quadric);

		DecompositionSolver solver = new LUDecomposition(qA).getSolver();
		if (solver.isNonSingular()) {
			return solver.solve(qb.mapMultiply(-1.0));
		} else {
			RealVector vv1 = new ArrayRealVector(
					new double[] { geoA.getCoords()[0], geoA.getCoords()[1], geoA.getCoords()[2], texA[0], texA[1] });
			RealVector vv2 = new ArrayRealVector(
					new double[] { geoB.getCoords()[0], geoB.getCoords()[1], geoB.getCoords()[2], texB[0], texB[1] });
			RealVector vvM = vv1.add(vv2).mapMultiply(0.5);
			double err1 = getCostOfContraction(quadric, vv1);
			double err2 = getCostOfContraction(quadric, vv2);
			double errM = getCostOfContraction(quadric, vvM);

			if (err1 <= err2 && err1 <= errM) {
				return vv1;
			} else if (err2 <= errM) {
				return vv2;
			} else {
				return vvM;
			}
		}
	}

	/**
	 * This method computes and returns the minimum of the given 5-dimensional
	 * quadric, if the respective system of equations has full rank, where the first
	 * three geometric dimensions are constrained to a given value. Otherwise the
	 * average of the two given texture coordinates will be used.
	 * 
	 * Use this when you already have a geometric target and now want a optimal
	 * texture target for a specific texture partition.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, whose minimum one wants to retrieve
	 * @param geoConstraint
	 *            The geometric target the result of this function shall be
	 *            constrained to
	 * @param texA
	 *            One of the two texture coordinates used as fallback
	 * @param texB
	 *            One of the two texture coordinates used as fallback
	 * @return Returns the minimum of the given quadric under the given constraint,
	 *         if such a minimum exists and is unique, or a fallback point otherwise
	 */
	public static RealVector getGeoContrainedMinimumForQuadric5(double[] quadric, double[] geoConstraint,
			TextureCoords texA, TextureCoords texB) {
		RealVector b = new ArrayRealVector(new double[] {
				geoConstraint[0] * quadric[12] + geoConstraint[1] * quadric[10] + geoConstraint[2] * quadric[7]
						+ quadric[18],
				geoConstraint[0] * quadric[14] + geoConstraint[1] * quadric[13] + geoConstraint[2] * quadric[11]
						+ quadric[19] });
		RealMatrix A = new NoCloneArray2DRowRealMatrix(
				new double[][] { { quadric[3], quadric[8] }, { quadric[8], quadric[4] } }, false);
		DecompositionSolver solver = new LUDecomposition(A).getSolver();

		RealVector texTarget;
		if (solver.isNonSingular() || false) {
			texTarget = solver.solve(b.mapMultiply(-1.0));
		} else {
			texTarget = new ArrayRealVector(texA.getCoords()).add(new ArrayRealVector(texB.getCoords()))
					.mapMultiply(0.5);
		}

		return new ArrayRealVector(new double[] { geoConstraint[0], geoConstraint[1], geoConstraint[2],
				texTarget.getEntry(0), texTarget.getEntry(1) });
	}

	/**
	 * This method computes and returns the minimum of the given 3-dimensional
	 * quadric, if the respective system of equations has full rank. Otherwise one
	 * of the given set of vertices or the average of these vertices will be
	 * returned, whatever results in the lowest cost.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, whose minimum one wants to retrieve
	 * @param a
	 *            One of the two vertices used as fallback
	 * @param b
	 *            One of the two vertices used as fallback
	 * @return Returns the minimum of the given 3-dimensional quadric, if such a
	 *         minimum exists and is unique, or a fallback point otherwise
	 */
	public static RealVector getMinimumForQuadric3(double[] quadric, QuadricVertex a, QuadricVertex b) {
		RealMatrix Q = QuadricUtils.derivationFromValues(quadric);
		DecompositionSolver solver = new LUDecomposition(Q).getSolver();
		RealVector target;
		if (solver.isNonSingular()) {
			RealVector v = new ArrayRealVector(new double[] { 0, 0, 0, 1 });
			target = solver.solve(v);
		} else {
			RealVector vv1 = new ArrayRealVector(homogenize(a.getCoords()));
			RealVector vv2 = new ArrayRealVector(homogenize(b.getCoords()));
			Q.operate(vv1).dotProduct(vv1);
			Q.operate(vv1).dotProduct(vv2);
			Q.operate(vv2).dotProduct(vv1);

			RealVector vvM = vv1.add(vv2).mapMultiply(0.5);
			double err1 = computeCostForVector(vv1, Q);
			double err2 = computeCostForVector(vv2, Q);
			double errM = computeCostForVector(vvM, Q);

			if (err1 <= err2 && err1 <= errM) {
				target = vv1;
			} else if (err2 <= errM) {
				target = vv2;
			} else {
				target = vvM;
			}
		}

		return target.getSubVector(0, 3);
	}

	/**
	 * This method returns the (quadratic) cost of a given geometric target with
	 * respect to a given 3-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 3-dimensional quadric, with respect to which one wants to
	 *            compute the cost
	 * @param geoTarget
	 *            The geometric target, whose cost one wants to compute
	 * @return The (quadratic) cost of then given geometric target with respect to a
	 *         given 3-dimensional quadric
	 */
	public static double getCostForQuadric3(double[] quadric, double[] geoTarget) {
		RealVector target = new ArrayRealVector(new double[] { geoTarget[0], geoTarget[1], geoTarget[2], 1.0 });
		RealMatrix Q2 = QuadricUtils.quadricFromValues(quadric);
		return Q2.operate(target).dotProduct(target);
	}

	private static double[] homogenize(double[] v) {
		double[] w = new double[v.length + 1];

		for (int i = 0; i < v.length; ++i) {
			w[i] = v[i];
		}

		w[v.length] = 1;
		return w;
	}

	private static double computeCostForVector(RealVector v, RealMatrix Q) {
		return Q.operate(v).dotProduct(v);
	}

	/**
	 * This method returns the (quadratic) cost of a given geometric and texture
	 * target with respect to a given 5-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, with respect to which one wants to
	 *            compute the cost
	 * @param geoTarget
	 *            The geometric target, whose cost one wants to compute
	 * @param texTarget
	 *            The texture target, whose cost one wants to compute
	 * @return The (quadratic) cost of then given geometric and texture target with
	 *         respect to a given 5-dimensional quadric
	 */
	public static double getCostOfContraction(double[] quadric, double[] geoTarget, double[] texTarget) {
		RealVector contraction = new ArrayRealVector(
				new double[] { geoTarget[0], geoTarget[1], geoTarget[2], texTarget[0], texTarget[1] });
		return getCostOfContraction(quadric, contraction);
	}

	/**
	 * This method returns the (quadratic) cost of a given geometric and texture
	 * target encoded in a 5-dimensional @see RealVector with respect to a given
	 * 5-dimensional quadric.
	 * 
	 * @param quadric
	 *            The 5-dimensional quadric, with respect to which one wants to
	 *            compute the cost
	 * @param contradiction
	 *            The 5-dimensional target vector consisting of three geometric
	 *            coordinates and two texture coordinates, whose cost one wants to
	 *            compute
	 * @return The (quadratic) cost of then given target vector with respect to a
	 *         given 5-dimensional quadric
	 */
	public static double getCostOfContraction(double[] quadric, RealVector contraction) {
		RealMatrix qA = quadric5MatrixFromValues(quadric);
		RealVector qb = quadric5VectorFromValues(quadric);
		double qc = quadricConstFromValues(quadric);

		return qA.operate(contraction).dotProduct(contraction) + 2 * qb.dotProduct(contraction) + qc;
	}

	/**
	 * This method computes the 5-dimensional quadric generated by a face's vertices
	 * and it's texture coordinates.
	 * 
	 * @param f
	 *            The face whose quadric one wants to compute
	 * @return The 5-dimensional quadric generated by the face as array of length 21
	 */
	public static double[] computeQuadric5ForFace(Face f) {
		double[] aCoords = f.getVertices()[0].getCoords();
		double[] aTex = f.getTextureCoords()[0].getCoords();
		double[] bCoords = f.getVertices()[1].getCoords();
		double[] bTex = f.getTextureCoords()[1].getCoords();
		double[] cCoords = f.getVertices()[2].getCoords();
		double[] cTex = f.getTextureCoords()[2].getCoords();

		RealVector p = new ArrayRealVector(new double[] { aCoords[0], aCoords[1], aCoords[2], aTex[0], aTex[1] });
		RealVector q = new ArrayRealVector(new double[] { bCoords[0], bCoords[1], bCoords[2], bTex[0], bTex[1] });
		RealVector r = new ArrayRealVector(new double[] { cCoords[0], cCoords[1], cCoords[2], cTex[0], cTex[1] });

		try {
			RealVector e1Vec = q.subtract(p).unitVector();
			double[] e1 = e1Vec.toArray();
			RealVector e2Vec = r.subtract(p).subtract(e1Vec.mapMultiply(e1Vec.dotProduct(r.subtract(p)))).unitVector();
			double[] e2 = e2Vec.toArray();

			double[] b = e1Vec.mapMultiply(p.dotProduct(e1Vec)).add(e2Vec.mapMultiply(p.dotProduct(e2Vec))).subtract(p)
					.toArray();
			double c = p.dotProduct(p) - (p.dotProduct(e1Vec) * p.dotProduct(e1Vec))
					- (p.dotProduct(e2Vec) * p.dotProduct(e2Vec));

			return new double[] { 1 - e1[0] * e1[0] - e2[0] * e2[0], 1 - e1[1] * e1[1] - e2[1] * e2[1],
					1 - e1[2] * e1[2] - e2[2] * e2[2], 1 - e1[3] * e1[3] - e2[3] * e2[3],
					1 - e1[4] * e1[4] - e2[4] * e2[4], -e1[0] * e1[1] - e2[0] * e2[1], -e1[1] * e1[2] - e2[1] * e2[2],
					-e1[2] * e1[3] - e2[2] * e2[3], -e1[3] * e1[4] - e2[3] * e2[4], -e1[0] * e1[2] - e2[0] * e2[2],
					-e1[1] * e1[3] - e2[1] * e2[3], -e1[2] * e1[4] - e2[2] * e2[4], -e1[0] * e1[3] - e2[0] * e2[3],
					-e1[1] * e1[4] - e2[1] * e2[4], -e1[0] * e1[4] - e2[0] * e2[4], b[0], b[1], b[2], b[3], b[4], c };
		} catch (MathArithmeticException ex) {
			return new double[21];
		}
	}

	/**
	 * This method computes the 3-dimensional quadric generated by a face's
	 * vertices.
	 * 
	 * @param f
	 *            The face whose quadric one wants to compute
	 * @return The 3-dimensional quadric generated by the face as array of length 10
	 */
	public static double[] computeQuadric3ForFace(Face f) {
		Vertex[] vertices = f.getVertices();
		Vector3D a = new Vector3D(vertices[0].getCoords());
		Vector3D b = new Vector3D(vertices[1].getCoords());
		Vector3D c = new Vector3D(vertices[2].getCoords());
		Vector3D u = b.subtract(a);
		Vector3D v = c.subtract(a);

		double[] ret;
		Vector3D nn = Vector3D.crossProduct(u, v);

		if (nn.getNorm() > 1E-12) {
			Vector3D n = nn.normalize();
			double d = -Vector3D.dotProduct(n, a);
			ret = new double[] { n.getX() * n.getX(), n.getY() * n.getY(), n.getZ() * n.getZ(), d * d,
					n.getX() * n.getY(), n.getY() * n.getZ(), n.getZ() * d, n.getX() * n.getZ(), n.getY() * d,
					n.getX() * d };
		} else {
			return new double[10];
			// throw new IllegalStateException();
		}

		return ret;
	}
}
