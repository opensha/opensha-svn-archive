package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultType;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;

import com.google.common.base.Predicate;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class SourcePredicates {
	// @formatter:off

	public static Predicate<FaultSource> name(String name) {
		return new SourceName(name);
	}
	
	public static Predicate<FaultSource> dip(double dip) {
		return new SourceDip(dip);
	}

	public static Predicate<FaultSource> type(FaultType type) {
		return new SourceType(type);
	}

	public static Predicate<FaultSource> mech(FocalMech mech) {
		return new SourceMech(mech);
	}

	public static Predicate<FaultSource> floats(boolean floats) {
		return new SourceFloats(floats);
	}

	
	private static class SourceName implements Predicate<FaultSource> {
		String name;
		SourceName(String name) { this.name = name; }
		@Override public boolean apply(FaultSource input) {
			return input.name.equals(name);
		}
		@Override public String toString() { return "Name: " + name; }
	}

	private static class SourceDip implements Predicate<FaultSource> {
		double dip;
		SourceDip(double dip) { this.dip = dip; }
		@Override public boolean apply(FaultSource input) {
			return input.dip == dip;
		}
		@Override public String toString() { return "Dip: " + dip; }
	}

	private static class SourceType implements Predicate<FaultSource> {
		FaultType type;
		SourceType(FaultType type) { this.type = type; }
		@Override public boolean apply(FaultSource input) {
			return input.type.equals(type);
		}
		@Override public String toString() { return "FaultType: " + type; }
	}

	private static class SourceMech implements Predicate<FaultSource> {
		FocalMech mech;
		SourceMech(FocalMech mech) { this.mech = mech; }
		@Override public boolean apply(FaultSource input) {
			return input.mech.equals(mech);
		}
		@Override public String toString() { return "FocalMech: " + mech; }
	}

	private static class SourceFloats implements Predicate<FaultSource> {
		boolean floats;
		SourceFloats(boolean floats) { this.floats = floats; }
		@Override public boolean apply(FaultSource input) {
			return input.floats == floats;
		}
		@Override public String toString() { return "Floats: " + floats; }
	}

	// @formatter:on
}
