package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class CyberShakeDeterministicCalc {
	
	private CachedPeakAmplitudesFromDB amps2db;
	private ERF erf;
	private double percentile;
	private double magRange;
	
	public CyberShakeDeterministicCalc(CachedPeakAmplitudesFromDB amps2db, ERF erf,
			double percentile, double magRange) {
		this.amps2db = amps2db;
		this.erf = erf;
		this.percentile = percentile;
		this.magRange = magRange;
	}
	
	public DeterministicResult calculate(int runID, CybershakeIM im) throws SQLException {
		double[][][] vals = amps2db.getAllIM_Values(runID, im);
		
		Preconditions.checkState(vals.length == erf.getNumSources(), "num sources inconsistant!");
		
		int maxRupsIncluded = -1;
		int maxNumAmps = -1;
		double[] boundingAmps = null;
		int numBelow = -1;
		int numAbove = -1;
		
		DeterministicResult maxVal = null;
		for (int sourceID=0; sourceID<vals.length; sourceID++) {
			// peak amp values for each rupture in this source
			double[][] rupVals = vals[sourceID];
			if (rupVals == null)
				// rup not applicable for site
				continue;
			
			ProbEqkSource source = erf.getSource(sourceID);
			Preconditions.checkState(rupVals.length == source.getNumRuptures(),
					"Rupture count inconsistant for source "+sourceID+": "+rupVals.length+" != "+source.getNumRuptures());
			
			// find the maximum magnitude of this source
			double maxMag = 0d;
			int maxRupID = -10;
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				if (rupVals[rupID] == null)
					// this rup is more than cutoff dist away
					continue;
				ProbEqkRupture rup = source.getRupture(rupID);
				if (rup.getMag() > maxMag) {
					maxMag = rup.getMag();
					maxRupID = rupID;
				}
			}
			Preconditions.checkState(maxMag > 0);
			// we will consider all ruptures with M>=(maxMag - magRange)
			double minMag = maxMag - magRange;
			
			// find all peak amplitudes that match this criteria
			// units are cm/sec^2
			List<Double> ampsToInclude = Lists.newArrayList();
			int rupsIncluded = 0;
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				if (rupVals[rupID] == null)
					// this rup is more than cutoff dist away
					continue;
				ProbEqkRupture rup = source.getRupture(rupID);
				if (rup.getMag() >= minMag) {
					ampsToInclude.addAll(Doubles.asList(rupVals[rupID]));
					rupsIncluded++;
				}
			}
			Preconditions.checkState(!ampsToInclude.isEmpty());
			
			double val = percentile(ampsToInclude, percentile);
			if (maxVal == null || val > maxVal.getVal()) {
				maxVal = new DeterministicResult(sourceID, maxRupID, maxMag, source.getName(), val);
				maxRupsIncluded = rupsIncluded;
				maxNumAmps = ampsToInclude.size();
				Collections.sort(ampsToInclude);
				boundingAmps = new double[] { Double.NaN, Double.NaN };
				for (int i=0; i<ampsToInclude.size(); i++) {
					double amp = ampsToInclude.get(i);
					if (amp <= val)
						boundingAmps[0] = amp;
					if (amp >= val) {
						numAbove = ampsToInclude.size()-i;
						numBelow = ampsToInclude.size()-numAbove;
						boundingAmps[1] = amp;
						break;
					}
				}
			}
		}
		
		// convert to G
		double maxValCM = maxVal.getVal();
		maxVal.setVal(maxVal.getVal() / HazardCurveComputation.CONVERSION_TO_G);
		
		System.out.println("RunID="+runID+", IM: "+im+", percentile="+percentile+", magRange="+magRange);
		System.out.println("CS Det Max: "+maxVal.getVal()+" g = "+maxValCM+" cm/s^2");
		System.out.println("Source("+maxVal.getSourceID()+","+maxVal.getRupID()+"): "+maxVal.getSourceName()
				+" (Mmax="+(float)+maxVal.getMag()+")");
		System.out.println(maxRupsIncluded+" rups and "+maxNumAmps+" amps used. "
				+ "Bounding amps: ["+boundingAmps[0]+","+boundingAmps[1]+"]. "+numBelow+" below, "+numAbove+" above.");
		
		return maxVal;
	}
	
	private static double percentile(List<Double> amps, double percentile) {
		// apache commons is apparently very efficient but gets weird for small sample sizes
//		return StatUtils.percentile(Doubles.toArray(amps), percentile);
		
		// linearly assign percentiles to each data value, then interpolate to find the value at the given percentile
		Collections.sort(amps);
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<amps.size(); i++) {
			double amp = amps.get(i);
			double x = 100d*(double)i/((double)amps.size()-1);
			func.set(x, amp);
		}
		
		return func.getInterpolatedY(percentile);
	}

	public static void main(String[] args) {
		File baseDir = new File("/home/kevin/CyberShake/MCER");
		File cacheDir = new File(baseDir, ".amps_cache");
		if (!cacheDir.exists())
			cacheDir.mkdir();
		
		double percentile = 84;
		double magRange = 0.11;
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		
		CachedPeakAmplitudesFromDB amps2db = new CachedPeakAmplitudesFromDB(db, cacheDir, erf);
		
		CyberShakeDeterministicCalc calc = new CyberShakeDeterministicCalc(amps2db, erf, percentile, magRange);
		
		int runID = 2657;
		CybershakeIM im = new CybershakeIM(146, IMType.SA, 3d, null, CyberShakeComponent.RotD100);
//		CybershakeIM im = new CybershakeIM(142, IMType.SA, 5d, null, CyberShakeComponent.RotD100);
//		CybershakeIM im = new CybershakeIM(138, IMType.SA, 7.5d, null, CyberShakeComponent.RotD100);
		
		try {
			calc.calculate(runID, im);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.destroy();
		}
		
		System.exit(0);
	}

}
