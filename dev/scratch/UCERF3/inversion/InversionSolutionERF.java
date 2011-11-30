package scratch.UCERF3.inversion;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurfaceWithSubsets;
import org.opensha.sha.faultSurface.AbstractEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.CompoundGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;

/**
 *
 */
public class InversionSolutionERF extends AbstractERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;

	public static final String NAME = "Inversion Solution ERF";
	
	private static final String FILE_PARAM_NAME = "Solution Input File";
	private FileParameter fileParam;
	
	// these help keep track of what's changed
	private File prevFile = null;
	private double faultGridSpacing = -1;
	
	private FaultGridSpacingParam faultGridSpacingParam;
	
	private FaultSystemSolution faultSysSolution;
	
	
	public InversionSolutionERF() {
		fileParam = new FileParameter(FILE_PARAM_NAME);
		fileParam.addParameterChangeListener(this);
		adjustableParams.addParameter(fileParam);
		
		faultGridSpacingParam = new FaultGridSpacingParam();
		faultGridSpacingParam.addParameterChangeListener(this);
		adjustableParams.addParameter(faultGridSpacingParam);
		
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(30.);
	}
	
	
	@Override
	public void updateForecast() {
		if (parameterChangeFlag) {
			if (D) System.out.println("Updating forecast");
			
			// set grid spacing
			faultGridSpacing = faultGridSpacingParam.getValue();
			
			// set input file
			File file = fileParam.getValue();
			if (file == null) throw new RuntimeException("No solution file specified");

			if (file != prevFile) {
				if (D) System.out.println("Loading solution from: "+file.getAbsolutePath());
				try {
					faultSysSolution = SimpleFaultSystemSolution.fromFile(file);
					prevFile = file;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
						
			if (D) System.out.println("Done updating forecast.");
		}
	}
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getNumSources() {
		return faultSysSolution.getNumRuptures();
	}

	@Override
	public ProbEqkSource getSource(int iSource) {
		boolean isPoisson = true;
		double prob = 1-Math.exp(-faultSysSolution.getRateForRup(iSource)*timeSpan.getDuration());
		
		FaultRuptureSource src = new FaultRuptureSource(faultSysSolution.getMagForRup(iSource), 
									  faultSysSolution.getCompoundGriddedSurfaceForRupupture(iSource, faultGridSpacing), 
									  faultSysSolution.getAveRakeForRup(iSource), prob, isPoisson);
		src.setName("Inversion Src #"+iSource);
		return src;
	}

}
