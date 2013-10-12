package scratch.UCERF3.erf;

import static org.opensha.sha.earthquake.param.IncludeBackgroundOption.EXCLUDE;
import static org.opensha.sha.earthquake.param.IncludeBackgroundOption.ONLY;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.magdist.GaussianMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;

import com.google.common.collect.Lists;

/**
 * This class represents an ERF for a given FaultSystemSolution (FSS).  Each "rupture" in the FaultSystemSolution
 * is treated as a separate source (each of which will have more than one rupture only if the 
 * AleatoryMagAreaStdDevParam has a non-zero value, or if multiple branches are represented as in subclass MeanUCERF3.
 * 
 * The fault system solution can be provided in the constructor (as an object or file name) or the file 
 * can be set in the file parameter.
 * 
 * This class make use of multiple mags for a given FSS rupture if they exist (e.g., from more than one logic tree
 * branch), but only the mean is currently used if aleatoryMagAreaStdDev !=0.
 * 
 * This filters out fault system ruptures that have zero rates, or have a magnitude below the section minimum
 * (as determined by InversionFaultSystemRupSet.isRuptureBelowSectMinMag(r)).
 * 
 * To make accessing ruptures less confusing, this class keeps track of "nth" ruptures within the ERF 
 * (see the last 7 methods here); these methods could be added to AbstractERF if more generally useful.
 * 
 * All sources are created regardless of the value of IncludeBackgroundParam
 * 
 * Subclasses can add other (non fault system) sources by simply overriding and implementing:
 * 
 *  	initOtherSources()
 *  	getOtherSource(int)
 *  
 * the first must set the numOtherSources variable (which can't change with adjustable parameters???) and must return 
 * whether the total number of ruptures has changed.  The getOtherSource(int) method must take into account any changes in 
 * the timespan duration (e.g., by making sources on the fly).
 * 
 * TODO: 
 * 
 * 1) make the list of adjustable parameters dynamic (e.g., hide those that aren't relevant 
 * based on other param settings); there was some memory leak with the way it was being handled
 * previously, but MeanUCERF2 approach seems to be working.
 * 
 * 2) evaluate whether pre-computing fault-based sources (rather than creating dynamically 
 * in the getSource() method) is really an advantage given memory consumption.
 * 
 * 
 */
public class FaultSystemSolutionERF extends AbstractERF {
	
	/** This sets the type of probability gain calculations
	 * 1 for averaging section recurrence intervals and time since last
	 * 2 for averaging section rates and normalized time since last
	 * 3 for WG02 calculations
	 */
	private static int PROB_GAIN_CALC_TYPE = 1;	
	
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = true;

	public static final String NAME = "Fault System Solution ERF";
	
	// Adjustable parameters
	public static final String FILE_PARAM_NAME = "Solution Input File";
	protected FileParameter fileParam;
	protected FaultGridSpacingParam faultGridSpacingParam;
	protected AleatoryMagAreaStdDevParam aleatoryMagAreaStdDevParam;
	protected ApplyGardnerKnopoffAftershockFilterParam applyAftershockFilterParam;
	protected IncludeBackgroundParam bgIncludeParam;
	protected BackgroundRupParam bgRupTypeParam;
	private static final String QUAD_SURFACES_PARAM_NAME = "Use Quad Surfaces (otherwise gridded)";
	private static final boolean QUAD_SURFACES_PARAM_DEFAULT = false;
	private BooleanParameter quadSurfacesParam;
	private ProbabilityModelParam probModelParam;
	private BPT_AperiodicityParam bpt_AperiodicityParam;

	
	// The primitive versions of parameters; and values here are the param defaults: (none for fileParam)
	protected double faultGridSpacing = 1.0;
	double aleatoryMagAreaStdDev = 0.0;
	protected boolean applyAftershockFilter = false;
	protected IncludeBackgroundOption bgInclude = IncludeBackgroundOption.EXCLUDE;
	protected BackgroundRupType bgRupType = BackgroundRupType.POINT;
	private boolean quadSurfaces = false;
	private ProbabilityModelOptions probModel = ProbabilityModelOptions.POISSON;
	private double bpt_Aperiodicity=0.3;

	// Parameter change flags: (none for bgIncludeParam) 
	protected boolean fileParamChanged=false;	// set as false since most subclasses ignore this parameter
	protected boolean faultGridSpacingChanged=true;
	protected boolean aleatoryMagAreaStdDevChanged=true;
	protected boolean applyAftershockFilterChanged=true;
	protected boolean bgRupTypeChanged=true;
	protected boolean quadSurfacesChanged=true;
	protected boolean probModelChanged=true;
	protected boolean bpt_AperiodicityChanged=true;
	

	// moment-rate reduction to remove aftershocks from supra-seis ruptures
	final public static double MO_RATE_REDUCTION_FOR_SUPRA_SEIS_RUPS = 0.97;	// 3%

	// this keeps track of time span changes
	boolean timeSpanChangeFlag=true;
	
	// these help keep track of what's changed
	protected File prevFile = null;
	private boolean faultSysSolutionChanged = true;
	
	// leave as a FaultSystemSolution for use with Simulator/other FSS
	private FaultSystemSolution faultSysSolution;		// the FFS for the ERF
	protected int numNonZeroFaultSystemSources;			// this is the number of faultSystemRups with non-zero rates (each is a source here)
	int totNumRupsFromFaultSystem;						// the sum of all nth ruptures that come from fault system sources (and not equal to faultSysSolution.getNumRuptures())
	
	protected int numOtherSources=0; 					// the non fault system sources
	protected int[] fltSysRupIndexForSource;  			// used to keep only inv rups with non-zero rates
	protected int[] srcIndexForFltSysRup;				// this stores the src index for the fault system source (-1 if there is no mapping)
	protected int[] fltSysRupIndexForNthRup;			// the fault system rupture index for the nth rup
	protected ArrayList<int[]> nthRupIndicesForSource;	// this gives the nth indices for a given source
	protected double[] longTermRateOfFltSysRupInERF;	// this holds the long-term rate of FSS rups as used by this ERF (e.g., small mags set to rate of zero); these rates include aftershocks
	
	// THESE COULD BE ADDED TO ABSRACT ERF:
	protected int totNumRups;
	protected int[] srcIndexForNthRup;
	protected int[] rupIndexForNthRup;
	
	
	protected List<FaultRuptureSource> faultSourceList;
	
	ProbabilityModelsCalc probModelsCalc;
	
	/**
	 * This creates the ERF from the given FaultSystemSolution.  FileParameter is removed 
	 * from the adjustable parameter list (to prevent changes after instantiation).
	 * @param faultSysSolution
	 */
	public FaultSystemSolutionERF(FaultSystemSolution faultSysSolution) {
		this();
		setSolution(faultSysSolution);
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
	}

	
	/**
	 * This creates the ERF from the given file.  FileParameter is removed from the adjustable
	 * parameter list (to prevent changes after instantiation).
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionERF(String fullPathInputFile) {
		this();
		fileParam.setValue(new File(fullPathInputFile));
		// remove the fileParam from the adjustable parameter list
		adjustableParams.removeParameter(fileParam);
	}

	
	/**
	 * This creates the ERF with a parameter for setting the input file
	 * (e.g., from a GUI).
	 */
	public FaultSystemSolutionERF() {
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

		bgIncludeParam = new IncludeBackgroundParam();
		adjustableParams.addParameter(bgIncludeParam);

		bgRupTypeParam = new BackgroundRupParam();
		adjustableParams.addParameter(bgRupTypeParam);
		
		quadSurfacesParam = new BooleanParameter(QUAD_SURFACES_PARAM_NAME, QUAD_SURFACES_PARAM_DEFAULT);
		adjustableParams.addParameter(quadSurfacesParam);
		
		probModelParam = new ProbabilityModelParam();
		adjustableParams.addParameter(probModelParam);
		
		bpt_AperiodicityParam = new BPT_AperiodicityParam();
		adjustableParams.addParameter(bpt_AperiodicityParam);


		// set listeners
		fileParam.addParameterChangeListener(this);
		faultGridSpacingParam.addParameterChangeListener(this);
		aleatoryMagAreaStdDevParam.addParameterChangeListener(this);
		applyAftershockFilterParam.addParameterChangeListener(this);
		bgIncludeParam.addParameterChangeListener(this);
		bgRupTypeParam.addParameterChangeListener(this);
		quadSurfacesParam.addParameterChangeListener(this);
		probModelParam.addParameterChangeListener(this);
		bpt_AperiodicityParam.addParameterChangeListener(this);

		
		// set parameters to the primitive values
		// don't do anything here fileParam 
		faultGridSpacingParam.setValue(faultGridSpacing);
		aleatoryMagAreaStdDevParam.setValue(aleatoryMagAreaStdDev);
		applyAftershockFilterParam.setValue(applyAftershockFilter);
		bgIncludeParam.setValue(bgInclude);
		bgRupTypeParam.setValue(bgRupType);
		quadSurfacesParam.setValue(quadSurfaces);
		probModelParam.setValue(probModel);
		bpt_AperiodicityParam.setValue(bpt_Aperiodicity);


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
		
		// read FSS solution from file if specified;
		// this sets faultSysSolutionChanged and bgRupTypeChanged (since this is obtained from the FSS) as true
		if(fileParamChanged) {
			readFaultSysSolutionFromFile();	// this will not re-read the file if the name has not changed
		}
		
		// update other sources if needed
		boolean numOtherRupsChanged=false;
		if(bgRupTypeChanged) {
			numOtherRupsChanged = initOtherSources();	// these are created even if not used; this sets numOtherSources
		}
		
		// update following FSS-related arrays if needed: longTermRateOfFltSysRupInERF[], srcIndexForFltSysRup[], fltSysRupIndexForSource[], numNonZeroFaultSystemSources
		boolean numFaultRupsChanged = false;
		if (faultSysSolutionChanged) {	
			makeMiscFSS_Arrays(); 
			numFaultRupsChanged = true;	// not necessarily true, but a safe assumption
		}
		
		// update prob model calculator if needed
		if (faultSysSolutionChanged || bpt_AperiodicityChanged || timeSpanChangeFlag || probModelChanged) {
			if(probModel != ProbabilityModelOptions.POISSON)
				probModelsCalc = new ProbabilityModelsCalc(faultSysSolution, longTermRateOfFltSysRupInERF, bpt_Aperiodicity, timeSpan);
		}
		
		// update the following ERF rup-related fields: totNumRups, totNumRupsFromFaultSystem, nthRupIndicesForSource, srcIndexForNthRup[], rupIndexForNthRup[], fltSysRupIndexForNthRup[]
		if(numOtherRupsChanged || numFaultRupsChanged) {
			setAllNthRupRelatedArrays();
		}

		// now make the list of fault-system sources
		if (faultSysSolutionChanged || faultGridSpacingChanged || aleatoryMagAreaStdDevChanged || applyAftershockFilterChanged || 
				quadSurfacesChanged || probModelChanged || bpt_AperiodicityChanged || timeSpanChangeFlag) {
			makeAllFaultSystemSources();	// overrides all fault-based source objects; created even if not fault sources aren't wanted
		}
		
		// reset change flags (that haven't already been done so)
		fileParamChanged = false;
		faultSysSolutionChanged = false;
		faultGridSpacingChanged = false;
		aleatoryMagAreaStdDevChanged = false;
		applyAftershockFilterChanged = false;
		bgRupTypeChanged = false;			
		quadSurfacesChanged= false;
		probModelChanged = false;
		bpt_AperiodicityChanged = false;
		timeSpanChangeFlag = false;
		
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
			applyAftershockFilterChanged = true;
		} else if (paramName.equalsIgnoreCase(bgIncludeParam.getName())) {
			bgInclude = bgIncludeParam.getValue();
		} else if (paramName.equalsIgnoreCase(bgRupTypeParam.getName())) {
			bgRupType = bgRupTypeParam.getValue();
			bgRupTypeChanged = true;
		} else if (paramName.equals(QUAD_SURFACES_PARAM_NAME)) {
			quadSurfaces = quadSurfacesParam.getValue();
			quadSurfacesChanged = true;
		} else if (paramName.equals(probModelParam.getName())) {
			probModel = probModelParam.getValue();
			probModelChanged = true;
		} else if (paramName.equals(bpt_AperiodicityParam.getName())) {
			bpt_Aperiodicity = bpt_AperiodicityParam.getValue();
			bpt_AperiodicityChanged = true;
		} else {
			throw new RuntimeException("parameter name not recognized");
		}
	}

	
	
	/**
	 * This method initializes the following arrays:
	 * 
	 *		longTermRateOfFltSysRupInERF[]
	 * 		srcIndexForFltSysRup[]
	 * 		fltSysRupIndexForSource[]
	 * 		numNonZeroFaultSystemSources
	 */
	private void makeMiscFSS_Arrays() {
		FaultSystemRupSet rupSet = faultSysSolution.getRupSet();
		longTermRateOfFltSysRupInERF = new double[rupSet.getNumRuptures()];
				
		if(D) {
			System.out.println("Running makeFaultSystemSources() ...");
			System.out.println("   aleatoryMagAreaStdDev = "+aleatoryMagAreaStdDev);
			System.out.println("   faultGridSpacing = "+faultGridSpacing);
			System.out.println("   faultSysSolution.getNumRuptures() = "
					+rupSet.getNumRuptures());
		}
		
		numNonZeroFaultSystemSources =0;
		ArrayList<Integer> fltSysRupIndexForSourceList = new ArrayList<Integer>();
		srcIndexForFltSysRup = new int[rupSet.getNumRuptures()];
		for(int i=0; i<srcIndexForFltSysRup.length;i++)
			srcIndexForFltSysRup[i] = -1;				// initialize values to -1 (no mapping due to zero rate or mag too small)
		int srcIndex = 0;
		// loop over FSS ruptures
		for(int r=0; r< rupSet.getNumRuptures();r++){
			boolean rupTooSmall = false;	// filter out the too-small ruptures
			if(rupSet instanceof InversionFaultSystemRupSet)
				rupTooSmall = ((InversionFaultSystemRupSet)rupSet).isRuptureBelowSectMinMag(r);
//			System.out.println("rate="+faultSysSolution.getRateForRup(r));
			if(faultSysSolution.getRateForRup(r) > 0.0 && !rupTooSmall) {
				numNonZeroFaultSystemSources +=1;
				fltSysRupIndexForSourceList.add(r);
				srcIndexForFltSysRup[r] = srcIndex;
				longTermRateOfFltSysRupInERF[r] = faultSysSolution.getRateForRup(r);
				srcIndex += 1;
			}
		}
		
		// convert the list to array
		if(fltSysRupIndexForSourceList.size() != numNonZeroFaultSystemSources)
			throw new RuntimeException("Problem");
		fltSysRupIndexForSource = new int[numNonZeroFaultSystemSources];
		for(int i=0;i<numNonZeroFaultSystemSources;i++)
			fltSysRupIndexForSource[i] = fltSysRupIndexForSourceList.get(i);
		
		if(D) {
			System.out.println("   " + numNonZeroFaultSystemSources+" of "+
					rupSet.getNumRuptures()+ 
					" fault system sources had non-zero rates");
		}
	}
		
	/**
	 * This makes all the fault-system sources and put them into faultSourceList
	 */
	private void makeAllFaultSystemSources() {
		faultSourceList = Lists.newArrayList();
		for (int i=0; i<numNonZeroFaultSystemSources; i++) {
			faultSourceList.add(makeFaultSystemSource(i));
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
				setSolution(FaultSystemIO.loadSol(file));
				prevFile = file;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if(D) {
				runTime = (System.currentTimeMillis()-runTime)/1000;
				if(D) System.out.println("Loading solution took "+runTime+" seconds.");
			}
		}
	}
	
	/**
	 * Set the current solution. Can overridden to ensure it is a particular subclass.
	 * This sets both faultSysSolutionChanged and bgRupTypeChanged as true.
	 * @param sol
	 */
	protected void setSolution(FaultSystemSolution sol) {
		this.faultSysSolution = sol;
		faultSysSolutionChanged = true;
		bgRupTypeChanged = true;  // because the background ruptures come from the FSS
	}
	
	public FaultSystemSolution getSolution() {
		return faultSysSolution;
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
			return faultSourceList.get(iSource);
		} else if (iSource < numNonZeroFaultSystemSources) {
			return faultSourceList.get(iSource);
		} else {
			return getOtherSource(iSource - numNonZeroFaultSystemSources);
		}
	}


	/**
	 * Creates a fault source.
	 * @param iSource - source index in ERF
	 * @return
	 */
	protected FaultRuptureSource makeFaultSystemSource(int iSource) {
		FaultSystemRupSet rupSet = faultSysSolution.getRupSet();
		int fltSystRupIndex = fltSysRupIndexForSource[iSource];
		FaultRuptureSource src;
		
		double meanMag = rupSet.getMagForRup(fltSystRupIndex);	// this is the average if there are more than one mags
		
		// set aftershock rate correction
		double aftRateCorr = 1.0;
		if(applyAftershockFilter) aftRateCorr = MO_RATE_REDUCTION_FOR_SUPRA_SEIS_RUPS; // GardnerKnopoffAftershockFilter.scaleForMagnitude(mag);
		
		// get time-dependent probability gain
		double probGain=1d;	// default
		if(probModel == ProbabilityModelOptions.BPT && iSource < numNonZeroFaultSystemSources) {
			if(PROB_GAIN_CALC_TYPE == 1)
				probGain = probModelsCalc.getU3_ProbGain1_ForRup(fltSystRupIndex, false);
			else if (PROB_GAIN_CALC_TYPE == 2)
				probGain = probModelsCalc.getU3_ProbGain2_ForRup(fltSystRupIndex, false);
			else if (PROB_GAIN_CALC_TYPE == 3)
				probGain = probModelsCalc.getWG02_ProbGainForRup(fltSystRupIndex, false);
		}
		
		if(aleatoryMagAreaStdDev == 0) {
			// TODO allow rup MFD with aleatory?
			DiscretizedFunc rupMFD = faultSysSolution.getRupMagDist(fltSystRupIndex);	// this exists for multi-branch mean solutions
			if (rupMFD == null || rupMFD.getNum() < 2) {
				// normal source
				boolean isPoisson = true;		// TODO Does this matter for BPT?
				double prob = 1-Math.exp(-aftRateCorr*probGain*faultSysSolution.getRateForRup(fltSystRupIndex)*timeSpan.getDuration());
				src = new FaultRuptureSource(meanMag, 
						rupSet.getSurfaceForRupupture(fltSystRupIndex, faultGridSpacing, quadSurfaces), 
						rupSet.getAveRakeForRup(fltSystRupIndex), prob, isPoisson);
			} else {
				// we have a MFD for this rupture
				if (aftRateCorr != 1d || probGain != 1d) {
					// apply aftershock and/or gain corrections
					rupMFD = rupMFD.deepClone();
					rupMFD.scale(aftRateCorr*probGain);
				}
				src = new FaultRuptureSource(rupMFD, 
						rupSet.getSurfaceForRupupture(fltSystRupIndex, faultGridSpacing, quadSurfaces),
						rupSet.getAveRakeForRup(fltSystRupIndex), timeSpan.getDuration());
			}
		} else {
			// this currently only uses the mean magnitude
			double totMoRate = aftRateCorr*probGain*faultSysSolution.getRateForRup(fltSystRupIndex)*MagUtils.magToMoment(meanMag);
			GaussianMagFreqDist srcMFD = new GaussianMagFreqDist(5.05,8.65,37,meanMag,aleatoryMagAreaStdDev,totMoRate,2.0,2);
			src = new FaultRuptureSource(srcMFD, 
					rupSet.getSurfaceForRupupture(fltSystRupIndex, faultGridSpacing, quadSurfaces),
					rupSet.getAveRakeForRup(fltSystRupIndex), timeSpan.getDuration());			
		}
		// make and set the name
		List<FaultSectionPrefData> data = rupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		String name = data.size()+" SECTIONS BETWEEN "+data.get(0).getName()+" AND "+data.get(data.size()-1).getName();
		src.setName("Inversion Src #"+fltSystRupIndex+"; "+name);
		return src;
	}
	
	
	/**
	 * TODO move this elsewhere?
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
	 * Any subclasses that wants to include other (gridded) sources can override
	 * this method (and the getOtherSource() method), and make sure you return true if the
	 * number of ruptures changes.
	 */
	protected boolean initOtherSources() {
		numOtherSources=0;
		return false;
	}

	@Override
	public void timeSpanChange(EventObject event) {
		timeSpanChangeFlag = true;
	}
	
	
	
	/**
	 * This sets the following: totNumRups, totNumRupsFromFaultSystem, nthRupIndicesForSource,
	 * srcIndexForNthRup[], rupIndexForNthRup[], fltSysRupIndexForNthRup[]
	 * 
	 */
	protected void setAllNthRupRelatedArrays() {
		
		if(D) System.out.println("Running setAllNthRupRelatedArrays()");
		
		totNumRups=0;
		totNumRupsFromFaultSystem=0;
		nthRupIndicesForSource = new ArrayList<int[]>();

		// make temp array lists to avoid making each source twice
		ArrayList<Integer> tempSrcIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempRupIndexForNthRup = new ArrayList<Integer>();
		ArrayList<Integer> tempFltSysRupIndexForNthRup = new ArrayList<Integer>();
		int n=0;
		
		for(int s=0; s<getNumSources(); s++) {	// this includes gridded sources
			int numRups = getSource(s).getNumRuptures();
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
	 * This checks whether what's returned from get_nthRupIndicesForSource(s) gives
	 *  successive integer values when looped over all sources.
	 *  TODO move this to a test class
	 *  
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
		return get_nthRupIndicesForSource(s)[r];
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
