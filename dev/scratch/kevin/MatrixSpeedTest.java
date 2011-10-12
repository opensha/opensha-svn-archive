package scratch.kevin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.util.ClassUtils;

import scratch.UCERF3.utils.MatrixIO;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

public class MatrixSpeedTest {
	
	private static void doMults(DoubleMatrix2D A, DoubleMatrix1D x, int num) {
		DenseDoubleMatrix1D syn = new DenseDoubleMatrix1D(A.rows());
		
		Stopwatch watch = new Stopwatch();
		watch.start();
		
		for (int i=0; i<num; i++)
			A.zMult(x, syn);
		
		watch.stop();
		
		double milis = watch.elapsedMillis();
		System.out.println(ClassUtils.getClassNameWithoutPackage(A.getClass())
				+" x "+ClassUtils.getClassNameWithoutPackage(x.getClass())+" ("+num+"):\t"+(milis/1000d));
	}
	
	private static void setSparse(int[] rows, int[] cols, double[] vals, DoubleMatrix2D A) {
		
		
		Stopwatch watch = new Stopwatch();
		watch.start();
		
		for (int i=0; i<rows.length; i++)
			A.set(rows[i], cols[i], vals[i]);
		
		watch.stop();
		double milis = watch.elapsedMillis();
		System.out.println(ClassUtils.getClassNameWithoutPackage(A.getClass())
				+" set time: "+(milis/1000d));
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("D:\\Documents\\temp");
//		DataInputStream in = new DataInputStream(new FileInputStream(new File(dir, "A.bin")));
//		
//		int nRows = in.readInt();
//		int nCols = in.readInt();
//		int nVals = in.readInt();
//		
//		System.out.println("Mat size: "+nRows+"x"+nCols);
//		System.out.println("Num non zero: "+nVals);
//		
//		Preconditions.checkState(nRows > 0, "file contains no rows!");
//		Preconditions.checkState(nCols > 0, "file contains no columns!");
//		
//		int[] cols = new int[nVals];
//		int[] rows = new int[nVals];
//		double[] vals = new double[nVals];
//		
//		for (int i=0; i<nVals; i++) {
//			rows[i] = in.readInt();
//			cols[i] = in.readInt();
//			vals[i] = in.readDouble();
//		}
		double[] x = MatrixIO.doubleArrayFromFile(new File(dir, "run1.mat"));
		
		ArrayList<DoubleMatrix2D> As = new ArrayList<DoubleMatrix2D>();
//		As.add(new SparseDoubleMatrix2D(nRows, nCols));
//		As.add(new SparseRCDoubleMatrix2D(nRows, nCols));
//		
//		for (DoubleMatrix2D mat : As)
//			setSparse(rows, cols, vals, mat);
		
		// now add CCD...it takes wayyyyy too long to set here and this loads as CCD anyway
		As.add(MatrixIO.loadSparse(new File(dir, "A.bin"), SparseRCDoubleMatrix2D.class));
		As.add(MatrixIO.loadSparse(new File(dir, "A.bin"), SparseCCDoubleMatrix2D.class));
		As.add(MatrixIO.loadSparse(new File(dir, "A.bin"), SparseDoubleMatrix2D.class));
		
		DenseDoubleMatrix1D xMat = new DenseDoubleMatrix1D(x);
		
		for (DoubleMatrix2D mat : As)
			doMults(mat, xMat, 2000);
	}

}
