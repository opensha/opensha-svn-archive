package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.base.Preconditions;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;

public class MatrixIO {
	
	/**
	 * Saves a binary file containing the given sparse matrix. Only non zero values are stored.
	 * <br><br>
	 * Output format:<br>
	 * 3 integers, for rows, cols, # values.<br>
	 * Then for each value, 2 integers for row, col, then 1 double for value. 
	 * 
	 * @param mat
	 * @param file
	 * @throws IOException
	 */
	public static void saveSparse(DoubleMatrix2D mat, File file) throws IOException {
		Preconditions.checkNotNull(mat, "array cannot be null!");
		Preconditions.checkArgument(mat.size() > 0, "matrix can't be empty!");
		
		IntArrayList rowList = new IntArrayList();
		IntArrayList colList = new IntArrayList();
		DoubleArrayList valList = new DoubleArrayList();
		mat.getNonZeros(rowList, colList, valList);
		
		Preconditions.checkState(rowList.size()>0, "rowList is empty!");
		Preconditions.checkState(rowList.size() == colList.size() && colList.size() == valList.size(),
				"array sizes incorrect!");
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		
		// write header: rows, cols, values
		out.writeInt(mat.rows());
		out.writeInt(mat.columns());
		out.writeInt(valList.size());
		
		for (int i=0; i<valList.size(); i++) {
			int row = rowList.get(i);
			int col = colList.get(i);
			double val = valList.get(i);
			
			out.writeInt(row);
			out.writeInt(col);
			out.writeDouble(val);
		}
		
		out.close();
	}
	
	/**
	 * Loads a matrix saved in the format of {@link MatrixIO.saveSparse}.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @see MatrixIO.saveSparse
	 */
	public static DoubleMatrix2D loadSparse(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		
		int nRows = in.readInt();
		int nCols = in.readInt();
		int nVals = in.readInt();
		
		System.out.println("Mat size: "+nRows+"x"+nCols);
		System.out.println("Num non zero: "+nVals);
		
		Preconditions.checkState(nRows > 0, "file contains no rows!");
		Preconditions.checkState(nCols > 0, "file contains no columns!");
		
		int[] cols = new int[nVals];
		int[] rows = new int[nVals];
		double[] vals = new double[nVals];
		
		for (int i=0; i<nVals; i++) {
			rows[i] = in.readInt();
			cols[i] = in.readInt();
			vals[i] = in.readDouble();
		}
		
		DoubleMatrix2D mat = new SparseCCDoubleMatrix2D(nRows, nCols, rows, cols, vals, false, false, false);
		
		in.close();
		
		return mat;
	}
	
	/**
	 * Writes the given double array to a file. Output file simple contains a series of big endian double values.
	 * @param array
	 * @param file
	 * @throws IOException
	 */
	public static void doubleArrayToFile(double[] array, File file) throws IOException {
		Preconditions.checkNotNull(array, "array cannot be null!");
		Preconditions.checkArgument(array.length > 0, "array cannot be empty!");
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		
		for (double val : array) {
			out.writeDouble(val);
		}
		
		out.close();
	}
	
	/**
	 * Reads a file created by {@link MatrixIO.doubleArrayToFile} into a double array.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static double[] doubleArrayFromFile(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		long len = file.length();
		Preconditions.checkState(len > 0, "file is empty!");
		Preconditions.checkState(len % 8 == 0, "file size isn't evenly divisible by 8, " +
				"thus not a sequence of double values.");
		
		int size = (int)(len / 8);
		
		double[] array = new double[size];
		
		for (int i=0; i<size; i++)
			array[i] = in.readDouble();
		
		in.close();
		
		return array;
	}

}
