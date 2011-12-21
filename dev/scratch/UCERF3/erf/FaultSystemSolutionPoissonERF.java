package scratch.UCERF3.erf;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.eq.MagUtils;
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
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
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
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;

/**
 * TODO:  Add grid sources, and update getNumSources();  grid sources should come after the fault system sources
 */
public class FaultSystemSolutionPoissonERF extends AbstractERF {
	
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;

	public static final String NAME = "Fault System Solution Poisson ERF";
	
	// Adjustable parameters
	protected static final String FILE_PARAM_NAME = "Solution Input File";
	protected FileParameter fileParam;
	protected FaultGridSpacingParam faultGridSpacingParam;
	protected AleatoryMagAreaStdDevParam aleatoryMagAreaStdDevParam;
	protected double faultGridSpacing = -1;
	double aleatoryMagAreaStdDev = Double.NaN;
	
	// these help keep track of what's changed
	protected File prevFile = null;
	int lastSrcRequested = -1;
	ProbEqkSource currentSrc=null;

	
	protected FaultSystemSolution faultSysSolution;
	int numFaultSystemSources;		// this is the number of faultSystemRups with non-zero rates (each is a souce here)
	int totNumRupsFromFaultSystem;	// the sum of all ruptures that come from fault system sources (and not equal to faultSysSolution.getNumRuptures())
	
	protected int[] fltSysRupIndexForSource;  		// used to keep only inv rups with non-zero rates
	protected int[] srcIndexForFltSysRup;			// this stores the src index for the fault system source (-1 if there is no mapping?)
	protected int[] fltSysRupIndexForNthRup;		// the fault system rupture index for the nth rup
	protected ArrayList<int[]> nthRupIndicesForSource;	// this gives the nth indices for a given source
	
	// THESE COULD BE ADDED TO ABSRACT ERF
	protected int totNumRups;
	protected int[] srcIndexForNthRup;
	protected int[] rupIndexForNthRup;
	protected HashMap<String,Integer> nthRupForSrcAndRupIndices;
	
	// this is stored here for time-dependent subclasses (all values are Double.NaN here)
	double[] probGainForFaultSystemSource;	// initialized to NaN???


	
	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionPoissonERF(FaultSystemSolution faultSysSolution) {
		this();
		this.faultSysSolution=faultSysSolution;
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
		setupArraysAndLists();
	}

	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionPoissonERF(String fullPathInputFile) {
		this();
		fileParam.setValue(new File(fullPathInputFile));
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
	}

	
	/**
	 * This creates the ERF with a parameter for choosing the input file
	 */
	public FaultSystemSolutionPoissonERF() {
		fileParam = new FileParameter(FILE_PARAM_NAME);
		fileParam.addParameterChangeListener(this);
		adjustableParams.addParameter(fileParam);
		
		faultGridSpacingParam = new FaultGridSpacingParam();
		faultGridSpacingParam.addParameterChangeListener(this);
		adjustableParams.addParameter(faultGridSpacingParam);
		
		aleatoryMagAreaStdDevParam = new AleatoryMagAreaStdDevParam();
		aleatoryMagAreaStdDevParam.addParameterChangeListener(this);
		adjustableParams.addParameter(aleatoryMagAreaStdDevParam);		
		
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(30.);	// anything with listeners here?
	}
	
	
	
	@Override
	public void updateForecast() {
		
		if (D) System.out.println("Updating forecast");
		long runTime = System.currentTimeMillis();
			
		// set grid spacing
		faultGridSpacing = faultGridSpacingParam.getValue();
		aleatoryMagAreaStdDev = aleatoryMagAreaStdDevParam.getValue();

		if(fileParam.getValue() != null) // will be null if constructor was given FaultSysSolutionFrom
			readFaultSysSolutionFromFile();	// this will not re-read the file if the name has not changed
				
		runTime = (System.currentTimeMillis()-runTime)/1000;
		if(D) {
			System.out.println("Done updating forecast (took "+runTime+" seconds)");
			System.out.println("numFaultSystemSources="+numFaultSystemSources);
			System.out.println("totNumRupsFromFaultSystem="+totNumRupsFromFaultSystem);
			System.out.println("totNumRups="+totNumRups);
		}
		
	}
	
	private void setupArraysAndLists() {
		
		// count number of non-zero rate inversion ruptures (each will be a source)
		numFaultSystemSources =0;
		for(int r=0; r< faultSysSolution.getNumRuptures();r++)
			if(faultSysSolution.getRateForRup(r) > 0.0)
				numFaultSystemSources +=1;
		
		probGainForFaultSystemSource = new double[numFaultSystemSources] ;	// initialized to NaN???
		for(int s=0;s<probGainForFaultSystemSource.length;s++)
			probGainForFaultSystemSource[s] = Double.NaN;
		
		if(D) System.out.println(numFaultSystemSources+" of "+faultSysSolution.getNumRuptures()+ " fault system sources had non-zero rates");
		
		// make fltSysRupIndexForSource & srcIndexForFltSysRup
		srcIndexForFltSysRup = new int[faultSysSolution.getNumRuptures()];
		for(int i=0; i<srcIndexForFltSysRup.length;i++)
			srcIndexForFltSysRup[i] = -1;				// initialize values to -1 (no mapping due to zero rate)
		fltSysRupIndexForSource = new int[numFaultSystemSources];
		int srcIndex = 0;
		for(int r=0; r< faultSysSolution.getNumRuptures();r++)
			if(faultSysSolution.getRateForRup(r) > 0.0) {
				fltSysRupIndexForSource[srcIndex] = r;
				srcIndexForFltSysRup[r] = srcIndex;
				srcIndex += 1;
			}
		
		// now populate the following (requires making each source):
//							int totNumRups;
//							int[] srcIndexForNthRup;
//							int[] rupIndexForNthRup;
//							HashMap<String,Integer> nForSrcAndRupIndices;
		
//		System.out.println("starting to make nForSrcAndRupIndices HashMap");
		totNumRups=0;
		totNumRupsFromFaultSystem=0;
		nthRupForSrcAndRupIndices = new HashMap<String,Integer>();
		nthRupIndicesForSource = new ArrayList<int[]>();

		// make temp array lists to avoid making each source twice
		ArrayList<Integer> tempSrcIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempRupIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempFltSysRupIndexForNthRup = new ArrayList<Integer>();
		int n=0;
		for(int s=0; s<getNumSources(); s++) {
			int numRups = getNumRuptures(s);
			totNumRups += numRups;
			if(s<numFaultSystemSources) {
				totNumRupsFromFaultSystem += numRups;
			}
			int[] nthRupsForSrc = new int[numRups];
			for(int r=0; r<numRups; r++) {
				tempSrcIndexForNthRup.add(s);
				tempRupIndexForNthRup.add(r);
				if(s<numFaultSystemSources)
					tempFltSysRupIndexForNthRup.add(fltSysRupIndexForSource[s]);

				String srcAndRup = s+","+r;
				nthRupForSrcAndRupIndices.put(srcAndRup, new Integer(n));
				nthRupsForSrc[r]=n;
				n++;
			}
			nthRupIndicesForSource.add(nthRupsForSrc);
		}
		// now make final int[] arrays
		srcIndexForNthRup = new int[tempSrcIndexForNthRup.size()];
		rupIndexForNthRup = new int[tempRupIndexForNthRup.size()];
		fltSysRupIndexForNthRup = new int[tempFltSysRupIndexForNthRup.size()];
		for(n=0; n<totNumRups;n++)
		{
			srcIndexForNthRup[n]=tempSrcIndexForNthRup.get(n);
			rupIndexForNthRup[n]=tempRupIndexForNthRup.get(n);
			if(n<tempFltSysRupIndexForNthRup.size())
				fltSysRupIndexForNthRup[n] = tempFltSysRupIndexForNthRup.get(n);
		}
	}
	
	
	private void readFaultSysSolutionFromFile() {
		// set input file
		File file = fileParam.getValue();
		if (file == null) throw new RuntimeException("No solution file specified");

		if (file != prevFile) {
			if (D) System.out.println("Loading solution from: "+file.getAbsolutePath());
			long runTime = System.currentTimeMillis();
			try {
				faultSysSolution = SimpleFaultSystemSolution.fromFile(file);
				prevFile = file;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if(D) {
				runTime = (System.currentTimeMillis()-runTime)/1000;
				if(D) System.out.println("Loading solution took "+runTime+" seconds.");
			}
			setupArraysAndLists();
		}
	}
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getNumSources() {
		return fltSysRupIndexForSource.length;
	}
	
	
	public ProbEqkSource getSource(int iSource) {
		if(iSource == lastSrcRequested)
			return currentSrc;
		else if (iSource <numFaultSystemSources) {
			ProbEqkSource src;
			double probGain = probGainForFaultSystemSource[iSource];
			if(Double.isNaN(probGain))
				probGain = 1.0;
			src = makeFaultSystemSource(iSource, probGain);
			currentSrc = src;
			lastSrcRequested = iSource;		
			return src;
		}
		else	// this is where grid based sources can go
			return null;
	}


	protected ProbEqkSource makeFaultSystemSource(int iSource, double probGain) {
		
		int invRupIndex= fltSysRupIndexForSource[iSource];
		FaultRuptureSource src;
		
		if(aleatoryMagAreaStdDev == 0) {
			boolean isPoisson = true;
			double prob = 1-Math.exp(-faultSysSolution.getRateForRup(invRupIndex)*probGain*timeSpan.getDuration());
			src = new FaultRuptureSource(faultSysSolution.getMagForRup(invRupIndex), 
										  faultSysSolution.getCompoundGriddedSurfaceForRupupture(invRupIndex, faultGridSpacing), 
										  faultSysSolution.getAveRakeForRup(invRupIndex), prob, isPoisson);
		}
		else {
			double mag = faultSysSolution.getMagForRup(invRupIndex);
			double totMoRate = faultSysSolution.getRateForRup(invRupIndex)*probGain*MagUtils.magToMoment(mag);
			GaussianMagFreqDist srcMFD = new GaussianMagFreqDist(5.05,8.65,37,mag,aleatoryMagAreaStdDev,totMoRate,2.0,2);
			src = new FaultRuptureSource(srcMFD, 
					faultSysSolution.getCompoundGriddedSurfaceForRupupture(invRupIndex, faultGridSpacing),
					faultSysSolution.getAveRakeForRup(invRupIndex), timeSpan.getDuration());			
		}
		
//		if(D && (iSource==0 || iSource==1000)) 
//			System.out.println(iSource+"; aleatoryMagAreaStdDev = "+aleatoryMagAreaStdDev+"; numRups="+src.getNumRuptures());

		List<FaultSectionPrefData> data = faultSysSolution.getFaultSectionDataForRupture(invRupIndex);
		String name = data.size()+" SECTIONS BETWEEN "+data.get(0).getName()+" AND "+data.get(data.size()-1).getName();
		src.setName("Inversion Src #"+invRupIndex+"; "+name);
		return src;
	}
	
	
	
	public void writeSourceNamesToFile(String fileNameAndPath) {
		try{
			FileWriter fw1 = new FileWriter(fileNameAndPath);
			fw1.write("s\tname\n");
			for(int i=0;i<this.getNumSources();i++) {
				fw1.write(i+"\t"+getSource(i).getName()+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * This checks whether what's returned from get_nthRupIndicesForSource(s) gives
	 *  successive integer values when looped over all sources.
	 */
	public void testNthRupIndicesForSource() {
		int index = 0;
		for(int s=0; s<this.getNumSources(); s++) {
			int[] test = get_nthRupIndicesForSource(s);
			for(int r=0; r< test.length;r++) {
				int nthRup = test[r];
				if(nthRup !=index)
					throw new RuntimeException("Error found");
				index += 1;
			}
		}
		System.out.println("testNthRupIndicesForSource() was successful");
	}
	
	public int[] get_nthRupIndicesForSource(int iSource) {
		return nthRupIndicesForSource.get(iSource);
	}
	
	public int getTotNumRups() {
		return totNumRups;
	}
	
	public int getIndexN_ForSrcAndRupIndices(int s, int r) {
		String str = s+","+r;
		return nthRupForSrcAndRupIndices.get(str);
	}
	
	public int getSrcIndexForNthRup(int nthRup) {
		return srcIndexForNthRup[nthRup];
	}

	public int getRupIndexInSourceForNthRup(int nthRup) {
		return rupIndexForNthRup[nthRup];
	}
	
	public ProbEqkRupture getNthRupture(int n) {
		return getRupture(getSrcIndexForNthRup(n), getRupIndexInSourceForNthRup(n));
	}

}
