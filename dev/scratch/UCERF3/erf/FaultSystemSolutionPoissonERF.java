package scratch.UCERF3.erf;

import static org.opensha.sha.earthquake.param.IncludeBackgroundOption.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.magdist.GaussianMagFreqDist;

import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.GardnerKnopoffAftershockFilter;

/**
 * This class creates a Poisson ERF from a given FaultSystemSolution.  Each "rupture" in the FaultSystemSolution
 * is treated as a separate source (each of which will have more than one rupture only if the 
 * AleatoryMagAreaStdDevParam has a non-zero value.
 * 
 * The fault system solution can be provided in the constructor (as an object or file name) or the file 
 * can be set in the file parameter.
 * 
 * This filters out fault system ruptures that have zero rates.
 * 
 * To make accessing ruptures less confusing, this class keeps track of "nth" ruptures within the ERF 
 * (see the last 7 methods here); these methods could be added to AbstractERF.
 * 
 * Note that all sources are created regardless of the value of IncludeBackgroundParam
 * 
 * Subclasses can add other (non-fault system) sources by simply overriding and implementing the private 
 * getOtherSource(iSource) method and setting numOtherSources accordingly in the subclass constructor.
 * 
 * 
 */
public class FaultSystemSolutionPoissonERF extends AbstractERF {
	
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = true;

	public static final String NAME = "Fault System Solution Poisson ERF";
	
	// Adjustable parameters
	public static final String FILE_PARAM_NAME = "Solution Input File";
	protected FileParameter fileParam;
	protected boolean fileParamChanged;
	protected FaultGridSpacingParam faultGridSpacingParam;
	protected boolean faultGridSpacingChanged;
	protected double faultGridSpacing = -1;
	protected AleatoryMagAreaStdDevParam aleatoryMagAreaStdDevParam;
	protected boolean aleatoryMagAreaStdDevChanged;
	double aleatoryMagAreaStdDev = Double.NaN;
	protected ApplyGardnerKnopoffAftershockFilterParam applyAftershockFilterParam;
	protected boolean applyAftershockFilter;
	protected boolean applyAftershockFilterChanged;
	protected IncludeBackgroundParam bgIncludeParam;
	protected IncludeBackgroundOption bgInclude; // this is the primitive field
	protected BackgroundRupParam bgRupTypeParam;
	protected BackgroundRupType bgRupType; // this is the primitive field
	
	final public static double MO_RATE_REDUCTION_FOR_SUPRA_SEIS_RUPS = 0.97;	// 3%

	
	// these help keep track of what's changed
	protected File prevFile = null;
//	int lastSrcRequested = -1;
//	ProbEqkSource currentSrc=null;

	
	protected FaultSystemSolution faultSysSolution;
	protected int numNonZeroFaultSystemSources;		// this is the number of faultSystemRups with non-zero rates (each is a source here)
	int totNumRupsFromFaultSystem;					// the sum of all nth ruptures that come from fault system sources (and not equal to faultSysSolution.getNumRuptures())
	
	protected int numOtherSources=0; 					// the non fault system sources
	protected int[] fltSysRupIndexForSource;  			// used to keep only inv rups with non-zero rates
	protected int[] srcIndexForFltSysRup;				// this stores the src index for the fault system source (-1 if there is no mapping)
	protected int[] fltSysRupIndexForNthRup;			// the fault system rupture index for the nth rup
	protected ArrayList<int[]> nthRupIndicesForSource;	// this gives the nth indices for a given source
	
	// THESE COULD BE ADDED TO ABSRACT ERF:
	protected int totNumRups;
	protected int[] srcIndexForNthRup;
	protected int[] rupIndexForNthRup;
	protected HashMap<String,Integer> nthRupForSrcAndRupIndices;
	
	
	private List<ProbEqkSource> faultSources;
	
	/**
	 * This creates the ERF from the given FaultSystemSolution.  FileParameter is removed 
	 * from the adjustable parameter list (to prevent changes after instantiation).
	 * @param faultSysSolution
	 */
	public FaultSystemSolutionPoissonERF(FaultSystemSolution faultSysSolution) {
		this();
		this.faultSysSolution=faultSysSolution;
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
		aleatoryMagAreaStdDevChanged = true;	// set so everything is updated in updateForecast()
	}

	
	/**
	 * This creates the ERF from the given file.  FileParameter is removed from the adjustable
	 * parameter list (to prevent changes after instantiation).
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionPoissonERF(String fullPathInputFile) {
		this();
		fileParam.setValue(new File(fullPathInputFile));
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
	}

	
	/**
	 * This creates the ERF with a parameter for setting the input file
	 * (e.g., from a GUI).
	 */
	public FaultSystemSolutionPoissonERF() {
		initParams();
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(30.);
	}
	
	
	protected void initParams() {
		fileParam = new FileParameter(FILE_PARAM_NAME);
		adjustableParams.addParameter(fileParam);
		
		faultGridSpacingParam = new FaultGridSpacingParam();
		adjustableParams.addParameter(faultGridSpacingParam);
		
		aleatoryMagAreaStdDevParam = new AleatoryMagAreaStdDevParam();
		adjustableParams.addParameter(aleatoryMagAreaStdDevParam);
		
		applyAftershockFilterParam= new ApplyGardnerKnopoffAftershockFilterParam();  // default is false
		adjustableParams.addParameter(applyAftershockFilterParam);

		// TODO I have commented out these references because they create a memory leak;
		// need to figure out how to make these invisible when there is no background seis
		bgIncludeParam = new IncludeBackgroundParam();
//		bgIncludeParam.getEditor().setEnabled(false);
		adjustableParams.addParameter(bgIncludeParam);

		bgRupTypeParam = new BackgroundRupParam();
//		bgRupTypeParam.getEditor().setEnabled(false);
		adjustableParams.addParameter(bgRupTypeParam);

		// set listeners
		fileParam.addParameterChangeListener(this);
		faultGridSpacingParam.addParameterChangeListener(this);
		aleatoryMagAreaStdDevParam.addParameterChangeListener(this);
		applyAftershockFilterParam.addParameterChangeListener(this);
		bgIncludeParam.addParameterChangeListener(this);
		bgRupTypeParam.addParameterChangeListener(this);
		
		// set primitives
		faultGridSpacing = faultGridSpacingParam.getValue();
		aleatoryMagAreaStdDev = aleatoryMagAreaStdDevParam.getValue();
		applyAftershockFilter = applyAftershockFilterParam.getValue();
		bgInclude = bgIncludeParam.getValue();
		bgRupType = bgRupTypeParam.getValue();

	}
	
	/**
	 * This returns the number of fault system sources
	 * (that have non-zero rates)
	 * @return
	 */
	public int getNumFaultSystemSources(){
		return numNonZeroFaultSystemSources;
	}
	
	@Override
	public void updateForecast() {
		
		if (D) System.out.println("Updating forecast");
		long runTime = System.currentTimeMillis();
			
		if(fileParamChanged) {
			readFaultSysSolutionFromFile();	// this will not re-read the file if the name has not changed
			setupArraysAndLists();
		}
		else if (aleatoryMagAreaStdDevChanged || applyAftershockFilterChanged || faultGridSpacingChanged) {	// faultGridSpacingChanged not influential here
			setupArraysAndLists();
			aleatoryMagAreaStdDevChanged = false;
			applyAftershockFilterChanged = false;
			faultGridSpacingChanged = false;
		} else {
			// time span changes not being monitored
			setupArraysAndLists();
		}
		// TODO arrays like srcIndexForNthRup set in setupArraysAndLists() wont include gridded seismicity until that is called again;

		initOtherSources();	// these are created even if not used
				
		runTime = (System.currentTimeMillis()-runTime)/1000;
		if(D) {
			System.out.println("Done updating forecast (took "+runTime+" seconds)");
			System.out.println("numFaultSystemSources="+numNonZeroFaultSystemSources);
			System.out.println("totNumRupsFromFaultSystem="+totNumRupsFromFaultSystem);
			System.out.println("totNumRups="+totNumRups);
			System.out.println("numOtherSources="+this.numOtherSources);
			System.out.println("getNumSources()="+this.getNumSources());
		}
		
	}
	
	@Override
	public void parameterChange(ParameterChangeEvent event) {
		super.parameterChange(event);	// sets parameterChangeFlag = true;
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(fileParam.getName())) {
			fileParamChanged=true;
		} else if(paramName.equalsIgnoreCase(faultGridSpacingParam.getName())) {
			faultGridSpacing = faultGridSpacingParam.getValue();
			faultGridSpacingChanged=true;
		} else if (paramName.equalsIgnoreCase(aleatoryMagAreaStdDevParam.getName())) {
			aleatoryMagAreaStdDev = aleatoryMagAreaStdDevParam.getValue();
			aleatoryMagAreaStdDevChanged = true;
		} else if (paramName.equalsIgnoreCase(applyAftershockFilterParam.getName())) {
			applyAftershockFilter = applyAftershockFilterParam.getValue();
		} else if (paramName.equalsIgnoreCase(bgIncludeParam.getName())) {
			bgInclude = bgIncludeParam.getValue();
			boolean enable = !bgInclude.equals(IncludeBackgroundOption.EXCLUDE);
			// TODO disabled due to memory leak
//			bgRupTypeParam.getEditor().setEnabled(enable);
		} else if (paramName.equalsIgnoreCase(bgRupTypeParam.getName())) {
			bgRupType = bgRupTypeParam.getValue();

		} else {
			throw new RuntimeException("parameter name not recognized");
		}
	}

	
	
	/**
	 * This method sets a bunch of fields, arrays, and ArrayLists.
	 */
	private void setupArraysAndLists() {
				
		if(D) {
			System.out.println("Running setupArraysAndLists() ...");
			System.out.println("   aleatoryMagAreaStdDev = "+aleatoryMagAreaStdDev);
			System.out.println("   faultGridSpacing = "+faultGridSpacing);
			System.out.println("   faultSysSolution.getNumRuptures() = "+faultSysSolution.getNumRuptures());
		}
		
		// count number of non-zero rate inversion ruptures (each will be a source)
		numNonZeroFaultSystemSources =0;
		for(int r=0; r< faultSysSolution.getNumRuptures();r++){
			boolean rupTooSmall = false;	// filter out the too-small ruptures
			if(faultSysSolution instanceof InversionFaultSystemSolution)
				rupTooSmall = ((InversionFaultSystemSolution)faultSysSolution).isRuptureBelowSectMinMag(r);
//			System.out.println("rate="+faultSysSolution.getRateForRup(r));
			if(faultSysSolution.getRateForRup(r) > 0.0 && !rupTooSmall)
				numNonZeroFaultSystemSources +=1;			
		}
		
		if(D) {
			System.out.println("   " + numNonZeroFaultSystemSources+" of "+
					faultSysSolution.getNumRuptures()+ 
					" fault system sources had non-zero rates");
		}
		
		// make fltSysRupIndexForSource & srcIndexForFltSysRup
		srcIndexForFltSysRup = new int[faultSysSolution.getNumRuptures()];
		for(int i=0; i<srcIndexForFltSysRup.length;i++)
			srcIndexForFltSysRup[i] = -1;				// initialize values to -1 (no mapping due to zero rate)
		fltSysRupIndexForSource = new int[numNonZeroFaultSystemSources];
		int srcIndex = 0;
		for(int r=0; r< faultSysSolution.getNumRuptures();r++) {
			boolean rupTooSmall = false;	// filter out the too-small ruptures
			if(faultSysSolution instanceof InversionFaultSystemSolution)
				rupTooSmall = ((InversionFaultSystemSolution)faultSysSolution).isRuptureBelowSectMinMag(r);
			if(faultSysSolution.getRateForRup(r) > 0.0 && !rupTooSmall) {
				fltSysRupIndexForSource[srcIndex] = r;
				srcIndexForFltSysRup[r] = srcIndex;
				srcIndex += 1;
			}
		}
		// now populate the following (requires making each source):
//							int totNumRups;
//							int[] srcIndexForNthRup;
//							int[] rupIndexForNthRup;
//							HashMap<String,Integer> nForSrcAndRupIndices;
		
//		System.out.println("starting to make nForSrcAndRupIndices HashMap");
		
		// TODO code below needs to be cleaned now that list of src objects is created and kept (plus srcIndexForNthRup etc don't includ background seis on first run)

		totNumRups=0;
		totNumRupsFromFaultSystem=0;
		nthRupForSrcAndRupIndices = new HashMap<String,Integer>();
		nthRupIndicesForSource = new ArrayList<int[]>();

		// make temp array lists to avoid making each source twice
		ArrayList<Integer> tempSrcIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempRupIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempFltSysRupIndexForNthRup = new ArrayList<Integer>();
		int n=0;
		
	
		// create reference array of all non-gridded sources and ruptures
		if (!bgInclude.equals(ONLY)) {
			faultSources = Lists.newArrayList();
			for (int i=0; i<numNonZeroFaultSystemSources; i++) {
				faultSources.add(makeFaultSystemSource(i));
			}
		}

		for(int s=0; s<getNumSources(); s++) {
// ProbEqkSource src = getSource(s);
// System.out.println("src.getName()="+src.getName()+"\tsrc.getNumRuptures()="+src.getNumRuptures());

			int numRups = getSource(s).getNumRuptures();	// prob at 7773
			totNumRups += numRups;
			if(s<numNonZeroFaultSystemSources) {
				totNumRupsFromFaultSystem += numRups;
			}
			int[] nthRupsForSrc = new int[numRups];
			for(int r=0; r<numRups; r++) {
				tempSrcIndexForNthRup.add(s);
				tempRupIndexForNthRup.add(r);
				if(s<numNonZeroFaultSystemSources)
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
		
		if (D) {
			System.out.println("   getNumSources() = "+getNumSources());
			System.out.println("   totNumRupsFromFaultSystem = "+totNumRupsFromFaultSystem);
			System.out.println("   totNumRups = "+totNumRups);
		}
	}
	
	
	/**
	 * This returns the fault system rupture index for the ith source
	 * @param iSrc
	 * @return
	 */
	public int getFltSysRupIndexForSource(int iSrc) {
		return fltSysRupIndexForSource[iSrc];
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
			fileParamChanged = false;
		}
	}
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getNumSources() {
		if (bgInclude.equals(ONLY)) return numOtherSources;
		if (bgInclude.equals(EXCLUDE)) return numNonZeroFaultSystemSources;
		return numNonZeroFaultSystemSources + numOtherSources;
	}
	
	@Override
	public ProbEqkSource getSource(int iSource) {
		if (bgInclude.equals(ONLY)) {
			return getOtherSource(iSource);
		} else if(bgInclude.equals(EXCLUDE)) {
			return faultSources.get(iSource);
		} else if (iSource < numNonZeroFaultSystemSources) {
			return faultSources.get(iSource);
		} else {
			return getOtherSource(iSource - numNonZeroFaultSystemSources);
		}
	}


	/**
	 * Creates a fault source.
	 * @param iSource
	 * @return
	 */
	protected ProbEqkSource makeFaultSystemSource(int iSource) {
		
		int invRupIndex= fltSysRupIndexForSource[iSource];
		FaultRuptureSource src;
		
		double mag = faultSysSolution.getMagForRup(invRupIndex);
		double aftRateCorr = 1d;
		if(applyAftershockFilter) aftRateCorr = MO_RATE_REDUCTION_FOR_SUPRA_SEIS_RUPS; // GardnerKnopoffAftershockFilter.scaleForMagnitude(mag);
		
		if(aleatoryMagAreaStdDev == 0) {
			boolean isPoisson = true;
			double prob = 1-Math.exp(-aftRateCorr*faultSysSolution.getRateForRup(invRupIndex)*timeSpan.getDuration());
			src = new FaultRuptureSource(mag, 
										  faultSysSolution.getSurfaceForRupupture(invRupIndex, faultGridSpacing), 
										  faultSysSolution.getAveRakeForRup(invRupIndex), prob, isPoisson);
		}
		else {

			double totMoRate = aftRateCorr*faultSysSolution.getRateForRup(invRupIndex)*MagUtils.magToMoment(mag);
			GaussianMagFreqDist srcMFD = new GaussianMagFreqDist(5.05,8.65,37,mag,aleatoryMagAreaStdDev,totMoRate,2.0,2);
			src = new FaultRuptureSource(srcMFD, 
					faultSysSolution.getSurfaceForRupupture(invRupIndex, faultGridSpacing),
					faultSysSolution.getAveRakeForRup(invRupIndex), timeSpan.getDuration());			
		}

		List<FaultSectionPrefData> data = faultSysSolution.getFaultSectionDataForRupture(invRupIndex);
		String name = data.size()+" SECTIONS BETWEEN "+data.get(0).getName()+" AND "+data.get(data.size()-1).getName();
		src.setName("Inversion Src #"+invRupIndex+"; "+name);
		return src;
	}
	
	
	/**
	 * @param fileNameAndPath
	 */
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
	 * This provides a mechanism for adding other sources in subclasses
	 * @param iSource - note that this index is relative to the other sources list (numFaultSystemSources has already been subtracted out)
	 * @return
	 */
	protected ProbEqkSource getOtherSource(int iSource) {
		return null;
	}
	
	/**
	 * Any subclasses that need to update background sources should override
	 * this method. This method will only be called when the internal
	 * faultSystemSolution changes
	 */
	protected void initOtherSources() {
		// default implementation does nothing
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
	
	/**
	 * This returns the nth rup indices for the given source
	 */
	public int[] get_nthRupIndicesForSource(int iSource) {
		return nthRupIndicesForSource.get(iSource);
	}
	
	/**
	 * This returns the total number of ruptures (the sum of all ruptures in all sources)
	 */
	public int getTotNumRups() {
		return totNumRups;
	}
	
	/**
	 * This returns the nth rupture index for the given source and rupture index
	 * (where the latter is the rupture index within the source)
	 */	
	public int getIndexN_ForSrcAndRupIndices(int s, int r) {
		String str = s+","+r;
		return nthRupForSrcAndRupIndices.get(str);
	}
	
	/**
	 * This returns the source index for the nth rupture
	 * @param nthRup
	 * @return
	 */
	public int getSrcIndexForNthRup(int nthRup) {
		return srcIndexForNthRup[nthRup];
	}

	/**
	 * This returns the rupture index (with its source) for the
	 * given nth rupture.
	 * @param nthRup
	 * @return
	 */
	public int getRupIndexInSourceForNthRup(int nthRup) {
		return rupIndexForNthRup[nthRup];
	}
	
	/**
	 * This returns the nth rupture in the ERF
	 * @param n
	 * @return
	 */
	public ProbEqkRupture getNthRupture(int n) {
		return getRupture(getSrcIndexForNthRup(n), getRupIndexInSourceForNthRup(n));
	}

}
