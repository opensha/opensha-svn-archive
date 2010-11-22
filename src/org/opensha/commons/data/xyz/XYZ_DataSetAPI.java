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

package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;
import java.util.List;
/**
 * <p>Title: XYZ_DataSetAPI</p>
 * <p>Description: This interface defines the DataSet for the X,Y and Z.
 * This is the parent interface for <code>GeographicDataSetAPI</code>, which
 * should be used for any Geographic (Location based) XYZ datasets.</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: </p>
 * @author : Kevin Milner
 * @version 1.0
 */

public interface XYZ_DataSetAPI extends java.io.Serializable, Cloneable {

	/**
	 * Returns the minimum X value in this dataset.
	 * 
	 * @return
	 */
	public double getMinX();

	/**
	 * Returns the maximum X value in this dataset.
	 * 
	 * @return
	 */
	public double getMaxX();

	/**
	 * Returns the minimum Y value in this dataset.
	 * 
	 * @return
	 */
	public double getMinY();

	/**
	 * Returns the maximum Y value in this dataset.
	 * 
	 * @return
	 */
	public double getMaxY();

	/**
	 * Returns the minimum Z value in this dataset.
	 * 
	 * @return
	 */
	public double getMinZ();

	/**
	 * Returns the maximum Z value in this dataset.
	 * 
	 * @return
	 */
	public double getMaxZ();
	
	/**
	 * Sets the value at the given point. If the point doesn't exist, it will be added
	 * to the dataset.
	 * 
	 * @param point
	 * @param z
	 */
	public void set(Point2D point, double z);
	
	/**
	 * Sets the value at the given point. If the point doesn't exist, it will be added
	 * to the dataset.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set(double x, double y, double z);
	
	/**
	 * Sets the value at the given index. If index < 0 or index >= size(),
	 * then an exception is thrown.
	 * 
	 * @param index
	 * @param z
	 */
	public void set(int index, double z);
	
	/**
	 * Gets the value at the given point. If the point doesn't exist, null will be returned.
	 * 
	 * @param point
	 * @return
	 */
	public double get(Point2D point);
	
	/**
	 * Gets the value at the given point. If the point doesn't exist, null will be returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double get(double x, double y);

	/**
	 * Gets the value at the given index. If index < 0 or index >= size(),
	 * then an exception is thrown.
	 * @param index
	 * @return
	 */
	public double get(int index);
	
	/**
	 * Gets the point at the given index. If index < 0 or index >= size(),
	 * then an exception is thrown.
	 * 
	 * @param index
	 * @return
	 */
	public Point2D getPoint(int index);
	
	/**
	 * Returns the index of the given point, or -1 if it isn't in the dataset.
	 * 
	 * @param point
	 * @return
	 */
	public int indexOf(Point2D point);
	
	/**
	 * Returns the index of the given point, or -1 if it isn't in the dataset.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int indexOf(double x, double y);
	
	/**
	 * Returns true if the dataset contains the given point, false otherwise.
	 * 
	 * @param point
	 * @return
	 */
	public boolean contains(Point2D point);
	
	/**
	 * Returns true if the dataset contains the given point, false otherwise.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(double x, double y);
	
	/**
	 * Returns the size of the given dataset.
	 * 
	 * @return
	 */
	public int size();
	
	/**
	 * Sets every point in this dataset from the given dataset.
	 * 
	 * @param dataset
	 */
	public void setAll(XYZ_DataSetAPI dataset);
	
	/**
	 * Returns a list of all points in the correct order (as defined by indexOf).
	 * 
	 * @return
	 */
	public List<Point2D> getPointList();
	
	/**
	 * Returns a list of all values in the correct order (as defined by indexOf).
	 * 
	 * @return
	 */
	public List<Double> getValueList();
	
	public Object clone();
}
