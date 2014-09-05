package org.opensha.sha.cybershake.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.ERF;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.primitives.Doubles;

public class CachedPeakAmplitudesFromDB extends PeakAmplitudesFromDB {
	
	private static final boolean D = true;
	
	private File cacheDir;
	/**
	 * mapping from runID,im to [sourceID][rupID][rvID]
	 */
	private Table<Integer, CybershakeIM, double[][][]> cache;
	private static int maxRuns = 10;
	private ERF erf;

	public CachedPeakAmplitudesFromDB(DBAccess dbaccess, File cacheDir, ERF erf) {
		super(dbaccess);
		
		this.cacheDir = cacheDir;
		cache = HashBasedTable.create();
		this.erf = erf;
	}

	@Override
	public ArrayList<Double> getIM_Values(int runID, int srcId, int rupId,
			CybershakeIM im) throws SQLException {
		double[][][] runVals = cache.get(runID, im);
		if (runVals == null)
			runVals = fillCache(runID, im);
		
		ArrayList<Double> vals = super.getIM_Values(runID, srcId, rupId, im);
		
		return vals;
	}
	
	private synchronized double[][][] fillCache(int runID, CybershakeIM im) throws SQLException {
		// this is synchroinized, lets see if it already got filled
		double[][][] vals = cache.get(runID, im);
		if (vals != null)
			return vals;
		
		// first make room
		while (cache.size() >= maxRuns) {
			Cell<Integer, CybershakeIM, double[][][]> cell = cache.cellSet().iterator().next();
			cache.remove(cell.getRowKey(), cell.getColumnKey());
		}
		
		File cacheFile = getCacheFile(runID, im);
		if (cacheFile != null && cacheFile.exists()) {
			try {
				vals = loadCacheFile(cacheFile);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		} else {
			// need to get it from the db
			vals = loadAmpsFromDB(runID, im);
			if (cacheFile != null) {
				try {
					writeCacheFile(vals, cacheFile);
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			}
		}
		
		cache.put(runID, im, vals);
		
		return vals;
	}
	
	private double[][][] loadAmpsFromDB(int runID, CybershakeIM im) throws SQLException {
		if (D) System.out.println("Loading amps for "+runID);
		String sql = "SELECT Source_ID,Rupture_ID,IM_Value from "+TABLE_NAME+" where Run_ID="+runID
				+" and IM_Type_ID = '"+im.getID()+"' ORDER BY Source_ID,Rupture_ID,Rup_Var_ID";
		//				System.out.println(sql);
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean valid = rs.first();
		
		double[][][] ret = new double[erf.getNumSources()][][];
		
		int prevSourceID = -1;
		int prevRupID = -1;
		List<Double> curIMs = null;

		while (valid) {
			int sourceID = rs.getInt(1);
			int rupID = rs.getInt(2);
			double imVal = rs.getDouble(3);
			
			if (prevSourceID != sourceID) {
				// new source
				Preconditions.checkState(ret[sourceID] == null, "duplicate source?");
				ret[sourceID] = new double[erf.getNumRuptures(sourceID)][];
			}
			
			if (prevSourceID != sourceID || prevRupID != rupID) {
				if (curIMs != null) {
					Preconditions.checkState(ret[prevSourceID].length > prevRupID);
					Preconditions.checkState(ret[prevSourceID][prevRupID] == null, "duplicate rup");
					ret[prevSourceID][prevRupID] = Doubles.toArray(curIMs);
				}
				prevSourceID = sourceID;
				prevRupID = rupID;
				curIMs = Lists.newArrayList();
			}
			curIMs.add(imVal);
			valid = rs.next();
		}
		if (!curIMs.isEmpty()) {
			Preconditions.checkState(ret[prevSourceID].length > prevRupID);
			Preconditions.checkState(ret[prevSourceID][prevRupID] == null, "duplicate rup");
			ret[prevSourceID][prevRupID] = Doubles.toArray(curIMs);
		}
		rs.close();
		
		return ret;
	}
	
	private File getCacheFile(int runID, CybershakeIM im) {
		if (cacheDir == null)
			return null;
		return new File(cacheDir, "run_"+runID+"_im_"+im.getID()+".bin");
	}
	
	private static void writeCacheFile(double[][][] cache, File file) throws IOException {
		if (D) System.out.println("Writing cache to "+file.getName());
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		out.writeInt(cache.length);

		for (double[][] array2 : cache) {
			// source array
			if (array2 == null) {
				out.writeInt(0);
				continue;
			}
			out.writeInt(array2.length);
			for (double[] array : array2) {
				if (array == null) {
					out.writeInt(0);
					continue;
				}
				out.writeInt(array.length);
				for (double val : array)
					out.writeDouble(val);
			}
		}

		out.close();
	}
	
	private static double[][][] loadCacheFile(File file) throws IOException {
		if (D) System.out.println("Loading cache from "+file.getName());
		long len = file.length();
		Preconditions.checkState(len > 0, "file is empty!");
		Preconditions.checkState(len % 4 == 0, "file size isn't evenly divisible by 4, " +
		"thus not a sequence of double & integer values.");

		InputStream is = new FileInputStream(file);
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		is = new BufferedInputStream(is);

		DataInputStream in = new DataInputStream(is);

		int size = in.readInt();

		Preconditions.checkState(size > 0, "Size must be > 0!");
		
		double[][][] ret = new double[size][][];

		for (int i=0; i<size; i++) {
			int arraySize = in.readInt();
			if (arraySize == 0)
				continue;
			
			ret[i] = new double[arraySize][];
			
			for (int j=0; j<arraySize; j++) {
				int array2Size = in.readInt();
				if (array2Size == 0)
					continue;
				
				ret[i][j] = new double[array2Size];
				for (int k=0; k<array2Size; k++)
					ret[i][j][k] = in.readDouble();
			}
		}

		in.close();

		return ret;
	}

}
