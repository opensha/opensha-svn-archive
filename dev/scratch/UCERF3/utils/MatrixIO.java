package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

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
		return loadSparse(file, null);
	}
	
	public static DoubleMatrix2D loadSparse(File file, Class<? extends DoubleMatrix2D> clazz) throws IOException {
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
		
		in.close();
		
		DoubleMatrix2D mat;
		if (clazz == null || clazz.equals(SparseRCDoubleMatrix2D.class))
			// default
			mat = new SparseRCDoubleMatrix2D(nRows, nCols, rows, cols, vals, false, false, false);
		else if (clazz.equals(SparseCCDoubleMatrix2D.class))
			mat = new SparseCCDoubleMatrix2D(nRows, nCols, rows, cols, vals, false, false, false);
		else if (clazz.equals(SparseDoubleMatrix2D.class))
			mat = new SparseDoubleMatrix2D(nRows, nCols, rows, cols, vals);
		else
			throw new IllegalArgumentException("Unknown matrix type: "+clazz);
		
		return mat;
	}
	
	/**
	 * Writes the given double array to a file. Output file simply contains a series of big endian double values.
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
		
		long len = file.length();
		
		return doubleArrayFromInputStream(new FileInputStream(file), len);
	}
	
	/**
	 * Reads an imput stream created by {@link MatrixIO.doubleArrayToFile} into a double array.
	 * @param file
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static double[] doubleArrayFromInputStream(InputStream is, long length) throws IOException {
		Preconditions.checkState(length > 0, "file is empty!");
		Preconditions.checkState(length % 8 == 0, "file size isn't evenly divisible by 8, " +
				"thus not a sequence of double values.");
		
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		DataInputStream in = new DataInputStream(is);
		
		int size = (int)(length / 8);
		
		double[] array = new double[size];
		
		for (int i=0; i<size; i++)
			array[i] = in.readDouble();
		
		in.close();
		
		return array;
	}
	
	/**
	 * Writes the given list of double arrays to a file. All values are stored big endian. Output format contains
	 * first an integer, specifying the size of the list, then for each element in the list, an integer, denoting
	 * the size (n) of the array, followed by n double values (the array values). 
	 * @param list
	 * @param file
	 * @throws IOException
	 */
	public static void doubleArraysListToFile(List<double[]> list, File file) throws IOException {
		Preconditions.checkNotNull(list, "list cannot be null!");
		Preconditions.checkArgument(!list.isEmpty(), "list cannot be empty!");
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		
		out.writeInt(list.size());
		
		for (double[] array : list) {
			Preconditions.checkNotNull(array, "array cannot be null!");
			Preconditions.checkState(array.length > 0, "array cannot be empty!");
			out.writeInt(array.length);
			for (double val : array)
				out.writeDouble(val);
		}
		
		out.close();
	}
	
	/**
	 * Reads a file created by {@link MatrixIO.doubleArraysListFromFile} into a double array.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<double[]> doubleArraysListFromFile(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");
		
		long len = file.length();
		Preconditions.checkState(len > 0, "file is empty!");
		Preconditions.checkState(len % 4 == 0, "file size isn't evenly divisible by 4, " +
				"thus not a sequence of double & integer values.");
		
		return doubleArraysListFromInputStream(new FileInputStream(file));
	}
	
	/**
	 * Reads a file created by {@link MatrixIO.doubleArraysListFromFile} into a double array.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static List<double[]> doubleArraysListFromInputStream(InputStream is) throws IOException {
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		
		DataInputStream in = new DataInputStream(is);
		
		int size = in.readInt();
		
		Preconditions.checkState(size > 0, "Size must be > 0!");
		
		ArrayList<double[]> list = new ArrayList<double[]>(size);
		
		for (int i=0; i<size; i++) {
			int arraySize = in.readInt();
			
			double[] array = new double[arraySize];
			for (int j=0; j<arraySize; j++)
				array[j] = in.readDouble();
			
			list.add(array);
		}
		
		in.close();
		
		return list;
	}
	
	/**
	 * Writes the given list of integer lists to a file. All values are stored big endian. Output format contains
	 * first an integer, specifying the size of the list, then for each element in the list, an integer, denoting
	 * the size (n) of the array, followed by n integer values (the list values). 
	 * @param list
	 * @param file
	 * @throws IOException
	 */
	public static void intListListToFile(List<List<Integer>> list, File file) throws IOException {
		Preconditions.checkNotNull(list, "list cannot be null!");
		Preconditions.checkArgument(!list.isEmpty(), "list cannot be empty!");
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		
		out.writeInt(list.size());
		
		for (List<Integer> ints : list) {
			Preconditions.checkNotNull(ints, "list cannot be null!");
//			Preconditions.checkState(!ints.isEmpty(), "list cannot be empty!");
			out.writeInt(ints.size());
			for (int val : ints)
				out.writeInt(val);
		}
		
		out.close();
	}
	
	/**
	 * Reads a file created by {@link MatrixIO.intListListToFile} into an integer list list.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<List<Integer>> intListListFromFile(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");
		
		long len = file.length();
		Preconditions.checkState(len > 0, "file is empty!");
		Preconditions.checkState(len % 4 == 0, "file size isn't evenly divisible by 4, " +
				"thus not a sequence of double & integer values.");
		
		return intListListFromInputStream(new FileInputStream(file));
	}
	
	/**
	 * Reads a file created by {@link MatrixIO.intListListToFile} into an integer list list.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static List<List<Integer>> intListListFromInputStream(
			InputStream is) throws IOException {
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		
		DataInputStream in = new DataInputStream(is);
		
		int size = in.readInt();
		
		Preconditions.checkState(size > 0, "Size must be > 0!");
		
		ArrayList<List<Integer>> list = new ArrayList<List<Integer>>();
		
		for (int i=0; i<size; i++) {
			int listSize = in.readInt();
			
			ArrayList<Integer> ints = new ArrayList<Integer>(listSize);
			for (int j=0; j<listSize; j++)
				ints.add(in.readInt());
			
			list.add(ints);
		}
		
		in.close();
		
		return list;
	}

}
