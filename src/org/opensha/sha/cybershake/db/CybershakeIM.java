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

package org.opensha.sha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class CybershakeIM implements Comparable<CybershakeIM> {
	
	public enum IMType implements DBField {
		SA("spectral acceleration", "SA");
		
		private String dbName, shortName;
		private IMType(String dbName, String shortName) {
			this.dbName = dbName;
			this.shortName = shortName;
		}
		@Override
		public String getDBName() {
			return dbName;
		}
		@Override
		public String getShortName() {
			return shortName;
		}
		@Override
		public String toString() {
			return getShortName();
		}
	}
	
	public enum Component implements DBField {
		GEOM_MEAN("geometric mean", "GEOM"),
		X("X", "X"),
		Y("Y", "Y"),
		RotD100("RotD100", "RotD100"),
		RotD50("RotD50", "RotD50");

		private String dbName, shortName;
		private Component(String dbName, String shortName) {
			this.dbName = dbName;
			this.shortName = shortName;
		}
		@Override
		public String getDBName() {
			return dbName;
		}
		@Override
		public String getShortName() {
			return shortName;
		}
		@Override
		public String toString() {
			return getShortName();
		}
	}
	
	private interface DBField {
		public String getDBName();
		public String getShortName();
	}
	
	public static <E extends Enum<E>> E fromDBField(String dbName, Class<E> clazz) {
		for (E e : clazz.getEnumConstants()) {
			Preconditions.checkState(e instanceof DBField);
			DBField db = (DBField)e;
			if (db.getDBName().equals(dbName.trim()))
				return e;
		}
		throw new IllegalArgumentException("DB field for type "
				+ClassUtils.getClassNameWithoutPackage(clazz)+" not found: "+dbName);
	}
	
	public static <E extends Enum<E>> E fromShortName(String shortName, Class<E> clazz) {
		for (E e : clazz.getEnumConstants()) {
			Preconditions.checkState(e instanceof DBField);
			DBField db = (DBField)e;
			if (db.getShortName().toLowerCase().equals(shortName.trim().toLowerCase()))
				return e;
		}
		throw new IllegalArgumentException("Short name for type "
				+ClassUtils.getClassNameWithoutPackage(clazz)+" not found: "+shortName);
	}
	
	public static <E extends Enum<E>> List<String> getShortNames(Class<E> clazz) {
		List<String> names = Lists.newArrayList();
		for (E e : clazz.getEnumConstants()) {
			Preconditions.checkState(e instanceof DBField);
			DBField db = (DBField)e;
			names.add(db.getShortName());
		}
		return names;
	}
	
	private int id;
	private IMType measure;
	private double val;
	private String units;
	private Component component;
	
	public CybershakeIM(int id, IMType measure, double val, String units, Component component) {
		this.id = id;
		this.measure = measure;
		this.val = val;
		this.units = units;
		this.component = component;
	}

	public int getID() {
		return id;
	}

	public IMType getMeasure() {
		return measure;
	}
	
	public Component getComponent() {
		return component;
	}

	public double getVal() {
		return val;
	}

	public String getUnits() {
		return units;
	}
	
	public String toString() {
		return this.measure + ": " + this.val + " (" + this.units + ")";
	}
	
	public boolean equals(Object im) {
		if (im instanceof CybershakeIM)
			return id == ((CybershakeIM)im).id;
		return false;
	}

	public int compareTo(CybershakeIM im) {
		if (val > im.val)
			return 1;
		if (val < im.val)
			return -1;
		return 0;
	}
	
	public static CybershakeIM fromResultSet(ResultSet rs) throws SQLException {
		int id = rs.getInt("IM_Type_ID");
		String measureStr = rs.getString("IM_Type_Measure");
		IMType measure = fromDBField(measureStr, IMType.class);
		Double value = rs.getDouble("IM_Type_Value");
		String units = rs.getString("Units");
		String componentStr = rs.getString("IM_Type_Component");
		Component component = fromDBField(componentStr, Component.class);
		return new CybershakeIM(id, measure, value, units, component);
	}
}
