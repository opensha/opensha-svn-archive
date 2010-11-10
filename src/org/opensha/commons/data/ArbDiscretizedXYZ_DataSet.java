/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.data;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.opensha.commons.data.xyz.XYZ_DataSetAPI;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.FileUtils;

/**
 * <p>Title: ArbDiscretizedXYZ_DataSet</p>
 * <p>Description: This class creates a vector for the XYZ dataset.
 * FIX : FIX - The implementation is the quick and dirty solution for the time being and will needed to
 * modified later on based on our requirements.
 * Requires Fixation to be consistent with our implementation of the 2-D data representation</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ArbDiscretizedXYZ_DataSet implements XYZ_DataSetAPI,java.io.Serializable{

	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1l;
	
	// we need a separate list of points (vs just using map for everything) to ensure order
	private ArrayList<Point2D> points;
	// mapping of poitns to values
	private HashMap<Point2D, Double> map;
	
	public ArbDiscretizedXYZ_DataSet() {
		points = new ArrayList<Point2D>();
		map = new HashMap<Point2D, Double>();
	}
	
	private MinMaxAveTracker getXTracker() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		for (Point2D pt : map.keySet()) {
			tracker.addValue(pt.getX());
		}
		return tracker;
	}
	
	private MinMaxAveTracker getYTracker() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		for (Point2D pt : map.keySet()) {
			tracker.addValue(pt.getY());
		}
		return tracker;
	}
	
	private MinMaxAveTracker getZTracker() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		for (double val : map.values()) {
			tracker.addValue(val);
		}
		return tracker;
	}

	@Override
	public double getMinX() {
		return getXTracker().getMin();
	}

	@Override
	public double getMaxX() {
		return getXTracker().getMax();
	}

	@Override
	public double getMinY() {
		return getYTracker().getMin();
	}

	@Override
	public double getMaxY() {
		return getYTracker().getMax();
	}

	@Override
	public double getMinZ() {
		return getZTracker().getMin();
	}

	@Override
	public double getMaxZ() {
		return getZTracker().getMax();
	}

	@Override
	public void set(Point2D point, double z) {
		if (!contains(point))
			points.add(point);
		map.put(point, z);
	}

	@Override
	public void set(double x, double y, double z) {
		set(new Point2D.Double(x, y), z);
	}

	@Override
	public void set(int index, double z) {
		set(getPoint(index), z);
	}

	@Override
	public double get(Point2D point) {
		return map.get(point);
	}

	@Override
	public double get(double x, double y) {
		return get(new Point2D.Double(x, y));
	}

	@Override
	public double get(int index) {
		return get(getPoint(index));
	}

	@Override
	public Point2D getPoint(int index) {
		return points.get(index);
	}

	@Override
	public int indexOf(Point2D point) {
		return points.indexOf(point);
	}

	@Override
	public boolean contains(Point2D point) {
		return points.contains(point);
	}

	@Override
	public boolean contains(double x, double y) {
		return contains(new Point2D.Double(x, y));
	}

	@Override
	public int size() {
		return points.size();
	}

	@Override
	public void setAll(XYZ_DataSetAPI dataset) {
		for (int i=0; i<dataset.size(); i++) {
			set(dataset.getPoint(i), dataset.get(i));
		}
	}
	
	public static void writeXYZFile(XYZ_DataSetAPI xyz, String fileName) throws IOException {
		
		FileWriter fw = new FileWriter(fileName);
		for (int i=0; i<xyz.size(); i++) {
			Point2D point = xyz.getPoint(i);
			double z = xyz.get(i);
			
			fw.write(point.getX() + "\t" + point.getY() + "\t" + z + "\n");
		}
		fw.close();
	}
	
	public static ArbDiscretizedXYZ_DataSet loadXYZFile(String fileName) throws FileNotFoundException, IOException {
		ArrayList<String> lines = FileUtils.loadFile(fileName);
		
		ArbDiscretizedXYZ_DataSet xyz = new ArbDiscretizedXYZ_DataSet();
		
		for (String line : lines) {
			if (line.startsWith("#"))
				continue;
			if (line.length() < 2)
				continue;
			StringTokenizer tok = new StringTokenizer(line);
			if (tok.countTokens() < 3)
				continue;
			
			double x = Double.parseDouble(tok.nextToken());
			double y = Double.parseDouble(tok.nextToken());
			double z = Double.parseDouble(tok.nextToken());
			
			xyz.set(x, y, z);
		}
		
		return xyz;
	}

	@Override
	public Object clone() {
		ArbDiscretizedXYZ_DataSet xyz = new ArbDiscretizedXYZ_DataSet();
		for (int i=0; i<size(); i++) {
			xyz.set(getPoint(i), get(i));
		}
		return xyz;
	}
	
}
