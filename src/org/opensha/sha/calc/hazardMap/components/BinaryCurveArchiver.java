package org.opensha.sha.calc.hazardMap.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Map;

import org.dom4j.Element;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.util.ExceptionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class BinaryCurveArchiver implements CurveResultsArchiver {
	
	public static final String XML_METADATA_NAME = "BinaryFileCurveArchiver";
	
	private File outputDir;
	private Map<String, DiscretizedFunc> xValsMap;
	
	private Map<Integer, byte[]> doubleRecordBuffers;
	private Map<Integer, DoubleBuffer> doubleBuffs;
	
	private byte[] singleDoubleRecordBuffer;
	private DoubleBuffer singleDoubleBuff;
	
	private Map<String, RandomAccessFile> filesMap;
	
	private static final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
	
	private int numSites;
	
	public BinaryCurveArchiver(File outputDir, int numSites, Map<String, DiscretizedFunc> xValsMap) {
		this.outputDir = outputDir;
		this.numSites = numSites;
		if (!outputDir.exists())
			outputDir.mkdir();
		this.xValsMap = xValsMap;
		this.filesMap = Maps.newHashMap();
		
		doubleRecordBuffers = Maps.newHashMap();
		doubleBuffs = Maps.newHashMap();
		
		for (DiscretizedFunc xVals : xValsMap.values()) {
			Integer num = xVals.getNum();
			if (!doubleRecordBuffers.containsKey(num)) {
				byte[] doubleRecordBuffer = new byte[calcRecordLen(num)];
				
				ByteBuffer record = ByteBuffer.wrap(doubleRecordBuffer);
				record.order(byteOrder);
				
				doubleRecordBuffers.put(num, doubleRecordBuffer);
				doubleBuffs.put(num, record.asDoubleBuffer());
			}
		}
		
		singleDoubleRecordBuffer = new byte[8];
		ByteBuffer record = ByteBuffer.wrap(singleDoubleRecordBuffer);
		record.order(byteOrder);
		
		singleDoubleBuff = record.asDoubleBuffer();
	}
	
	public void initialize() {
		// we're rank zero, create file if necessary
		byte[] intRecordBuffer = new byte[4];

		ByteBuffer record = ByteBuffer.wrap(intRecordBuffer);
		record.order(byteOrder);

		IntBuffer intBuff = record.asIntBuffer();

		for (String imrName : xValsMap.keySet()) {
			
			File outputFile = new File(outputDir, imrName+".bin");
			if (!outputFile.exists()) {
				DiscretizedFunc xVals = xValsMap.get(imrName);
				int numXVals = xVals.getNum();
				long fileSize = calcFilePos(numXVals, numSites);

				try {
					// write the header
					RandomAccessFile file = new RandomAccessFile(outputFile, "rws");
					long pos = 0;

					file.seek(pos);
					intBuff.put(0, numXVals);

					file.write(intRecordBuffer);

					pos += 4;

					for (int i=0; i<numXVals; i++) {
						file.seek(pos);
						singleDoubleBuff.put(0, xVals.getX(i));
						file.write(singleDoubleRecordBuffer);
						pos += 8;
					}

					// now fill the rest with nans
					int recLen = calcRecordLen(numXVals);
					Preconditions.checkState(recLen % 8 == 0);
					int numNans = recLen / 8;
					double[] nanVals = new double[numNans];
					for (int i=0; i<nanVals.length; i++)
						nanVals[i] = Double.NaN;
					byte[] recordNans = new byte[recLen];

					record = ByteBuffer.wrap(recordNans);
					record.order(byteOrder);

					DoubleBuffer recordNanBuff = record.asDoubleBuffer();
					recordNanBuff.put(nanVals, 0, numNans);
					for (int i=0; i<numSites; i++) {
						file.seek(pos);
						file.write(recordNans);
						pos += recLen;
					}

					Preconditions.checkState(pos == fileSize);

					filesMap.put(imrName, file);
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
		}
	}
	
	private static int calcRecordLen(int numXVals) {
		// record: lat, lon, and one for each curve point. all 8 bit doubles
		return (numXVals+2)*8;
	}
	
	private static int calcHeaderLen(int numXVals) {
		// header: [num x vals] [x1] [x2] ... [xN]. num is 4 bit int, vals are 8 bit doubles
		return 4+(numXVals)*8;
	}
	
	private static long calcFilePos(int numXVals, int index) {
		return calcHeaderLen(numXVals) + ((long)index)*(long)calcRecordLen(numXVals);
	}

	@Override
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		el.addAttribute("outputDir", outputDir.getAbsolutePath());
		
		return root;
	}
	
	public static BinaryCurveArchiver fromXMLMetadata(Element archiverEl, int numSites,
			Map<String, DiscretizedFunc> xValsMap) throws IOException {
		String outputDir = archiverEl.attributeValue("outputDir");
		return new BinaryCurveArchiver(new File(outputDir), numSites, xValsMap);
	}

	@Override
	public synchronized void archiveCurve(DiscretizedFunc curve,
			CurveMetadata meta) throws IOException {
		String imrName = meta.getShortLabel();
		RandomAccessFile file = getFile(imrName);
		int numX = xValsMap.get(imrName).getNum();
		Preconditions.checkState(curve.getNum() == numX);
		long pos = calcFilePos(numX, meta.getIndex());
		DoubleBuffer doubleBuff = doubleBuffs.get(numX);
		byte[] doubleRecordBuffer = doubleRecordBuffers.get(numX);
		doubleBuff.position(0);
		file.seek(pos);
		doubleBuff.put(meta.getSite().getLocation().getLatitude());
		doubleBuff.put(meta.getSite().getLocation().getLongitude());
		for (int i=0; i<numX; i++)
			doubleBuff.put(curve.getY(i));
		file.write(doubleRecordBuffer);
	}
	
	/*
	 * External synchronization required!
	 */
	private RandomAccessFile getFile(String imrName) throws FileNotFoundException {
		RandomAccessFile file = filesMap.get(imrName);
		if (file == null) {
			File outputFile = new File(outputDir, imrName+".bin");
			synchronized (BinaryCurveArchiver.class) {
				if (!outputFile.exists()) {
					// initialize everything, synchonized to this class
					System.out.println("Warning, not initialized. Initializing with first archive");
					initialize();
				}
			}
			Preconditions.checkState(outputFile.exists(), "Output file doesn't exist! Maybe not initialzed?");
			file = new RandomAccessFile(outputFile, "rws");
			filesMap.put(imrName, file);
		}
		return file;
	}

	@Override
	public synchronized boolean isCurveCalculated(CurveMetadata meta,
			DiscretizedFunc xVals) {
		try {
			String imrName = meta.getShortLabel();
			RandomAccessFile file = getFile(imrName);
			int numX = xValsMap.get(imrName).getNum();
			long pos = calcFilePos(numX, meta.getIndex());
			file.seek(pos);
			file.read(singleDoubleRecordBuffer);
			double val = singleDoubleBuff.get(0);
			return !Double.isNaN(val);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	@Override
	public File getStoreDir() {
		return outputDir;
	}

	@Override
	public void close() {
		for (RandomAccessFile file : filesMap.values())
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
