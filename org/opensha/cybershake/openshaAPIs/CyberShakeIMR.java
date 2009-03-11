package org.opensha.cybershake.openshaAPIs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.cybershake.db.CybershakeIM;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurveComputation;
import org.opensha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.cybershake.db.Runs2DB;
import org.opensha.cybershake.db.SiteInfo2DB;
import org.opensha.data.DataPoint2D;
import org.opensha.data.Site;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.exceptions.IMRException;
import org.opensha.exceptions.ParameterException;
import org.opensha.exceptions.WarningException;
import org.opensha.param.DoubleConstraint;
import org.opensha.param.DoubleDiscreteConstraint;
import org.opensha.param.DoubleDiscreteParameter;
import org.opensha.param.DoubleParameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.StringConstraint;
import org.opensha.param.StringParameter;
import org.opensha.param.WarningDoubleParameter;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.param.PropagationEffect;

public class CyberShakeIMR extends AttenuationRelationship implements ParameterChangeListener {

	EqkRupture curRupture = null;

	/** ParameterList of all Site parameters */
	protected ParameterList siteParams = new ParameterList();

	/** ParameterList of all eqkRupture parameters */
	protected ParameterList eqkRuptureParams = new ParameterList();

	public static final String SA_PERIOD_SELECTOR_PARAM_NAME = "SA Period";
	//	protected StringParameter saPeriodParam = null;
	//	protected String saPeriod;

	boolean dbConnInitialized = false;
	DBAccess db = null;
	SiteInfo2DB site2db = null;
	PeakAmplitudesFromDB ampsDB = null;
	Runs2DB runs2db = null;

	ArrayList<CybershakeSite> sites = null;
	CybershakeSite csSite = null;

	public static final String SGT_VAR_PARAM = "SGT Variation ID";
	public static final String RUP_VAR_SCENARIO_PARAM = "Rupture Variation Scenario ID";

	//source index parameter
	private StringParameter sgtVarParam;

	//rupture index parameter
	private StringParameter rupVarScenarioParam;

	int selectedSGTVariation = 5;
	int selectedRupVarScenario = 3;

	double curPeriod = 0;
	CybershakeIM curIM = null;

	ArrayList<CybershakeIM> csIMs = null;

	private boolean isInitialized = false;
	private boolean loading = false;

	public CyberShakeIMR(ParameterChangeWarningListener listener) {
		super();
		loading = true;
		initSupportedIntensityMeasureParams();

		sgtVarParam = new StringParameter(SGT_VAR_PARAM, "");
		rupVarScenarioParam = new StringParameter(RUP_VAR_SCENARIO_PARAM, "");
		//		saPeriod = saPeriods.get(0);
		//		saPeriodParam = new StringParameter(SA_PERIOD_SELECTOR_PARAM_NAME,
		//		saPeriods,saPeriod);
		//		saPeriodParam.addParameterChangeListener(this);

		//		this.supportedIMParams.addParameter(saPeriodParam);

		initOtherParams();
		loading = false;
	}

	private void checkInit() {
		// we don't want to initilize the DB connection until we know the user actually wants to use this
		if (!isInitialized && !loading) {
			System.out.println("Initializing CyberShake IMR!");
			this.initDB();

			csIMs = ampsDB.getSupportedIMs();
			curIM = getIMForPeriod(3);

			// SGT Variation ID
			ArrayList<Integer> ids = ampsDB.getSGTVarIDs();
			selectedSGTVariation = ids.get(0);
			ArrayList<String> vals = new ArrayList<String>();
			for (int val : ids) {
				vals.add(val + "");
			}
			sgtVarParam.setValue(vals.get(0));
			sgtVarParam.setConstraint(new StringConstraint(vals));
			sgtVarParam.addParameterChangeListener(this);

			// Rupture Variation IDs
			ids = ampsDB.getRupVarScenarioIDs();
			selectedRupVarScenario = ids.get(0);
			vals = new ArrayList<String>();
			for (int val : ids) {
				vals.add(val + "");
			}
			rupVarScenarioParam.setValue(vals.get(0));
			rupVarScenarioParam.setConstraint(new StringConstraint(vals));
			rupVarScenarioParam.addParameterChangeListener(this);

			otherParams.addParameter(rupVarScenarioParam);
			otherParams.addParameter(sgtVarParam);

			periodParam.addParameterChangeListener(this);

			isInitialized = true;
		}
	}

	private void initDB() {
		db = Cybershake_OpenSHA_DBApplication.db;
		site2db = new SiteInfo2DB(db);
		ampsDB = new PeakAmplitudesFromDB(db);
		runs2db = new Runs2DB(db);
		sites = site2db.getAllSitesFromDB();
		dbConnInitialized = true;
	}

	@Override
	public void setSite(Site site) {
		checkInit();
		System.out.println("Setting the site!");
		if (!dbConnInitialized)
			initDB();
		double site_tol = 0.01;
		for (CybershakeSite csSite : sites) {
			if (Math.abs(csSite.lat - site.getLocation().getLatitude()) < site_tol) {
				if (Math.abs(csSite.lon - site.getLocation().getLongitude()) < site_tol) {
					// it's a match!
					System.out.println("Idedntified CyberShake site: " + csSite);
					this.csSite = csSite;
					break;
				}
			}
		}
		this.site = site;
	}

	@Override
	public double getExceedProbability() throws ParameterException,
			IMRException {
		double iml = (Double)this.getIntensityMeasureLevel();
		return getExceedProbability(iml);
	}
	
	private CyberShakeEqkRupture getRuptureAsCSRup() {
		if (this.eqkRupture instanceof CyberShakeEqkRupture) {
			return (CyberShakeEqkRupture)this.eqkRupture;
		} else
			throw new RuntimeException("The CyberShakeIMR isn't being used with a CyberShake ERF!");
	}
	
	/**
	 * Returns a normalized cumulative distribution for the CyberShake rupture variation values
	 * @param vals
	 * @return
	 */
	
	private ArbitrarilyDiscretizedFunc getCumDistFunction(ArrayList<Double> vals) {
		ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
		
		for (double val : vals) {
			function.set(val,1);
		}
		
		ArbitrarilyDiscretizedFunc normCumDist = function.getNormalizedCumDist();
		
		return normCumDist;
	}
	
	/**
	 * Returns a new ArbitrarilyDiscretizedFunc where each x value is the natural log
	 * of the original function
	 * @param func
	 * @return
	 */
	private ArbitrarilyDiscretizedFunc getLogXFunction(ArbitrarilyDiscretizedFunc func) {
		ArbitrarilyDiscretizedFunc logFunc = new ArbitrarilyDiscretizedFunc();
		
		for (int i=0; i<func.getNum(); i++) {
			logFunc.set(Math.log(func.getX(i)), func.getY(i));
		}
		
		return logFunc;
	}
	
	private void oneMinusYFunction(ArbitrarilyDiscretizedFunc func) {
		for (int i=0; i<func.getNum(); i++) {
			func.set(func.getX(i), 1 - func.getY(i));
		}
	}
	
	/**
	 * First gets the norm cum dist using getCumDistFunction(). Then it creates a new function where
	 * x = log(x) and y = 1 - y;
	 * @param vals
	 * @return
	 */
	
	private ArbitrarilyDiscretizedFunc getLogX_OneMinusYCumDistFunction(ArrayList<Double> vals) {
		ArbitrarilyDiscretizedFunc normCumDist = getCumDistFunction(vals);
		
		ArbitrarilyDiscretizedFunc logFunc = getLogXFunction(normCumDist);
		oneMinusYFunction(logFunc);
		
		return logFunc;
	}
	
	private double getProbabilityFromLogCumDistFunc(ArbitrarilyDiscretizedFunc logFunc, double iml) {
		double prob;
		if(iml < logFunc.getMinX())
			prob = 1;
		else if(iml > logFunc.getMaxX())
			prob = 0;
		else
			prob = logFunc.getInterpolatedY(iml);

		return prob;
	}

	@Override
	public double getExceedProbability(double iml) {
		checkInit();
		CyberShakeEqkRupture rup = getRuptureAsCSRup();

		int srcID = rup.getSrcID();
		int rupID = rup.getRupID();
		int erfID = rup.getErfID();
		int sgtVarID = this.selectedSGTVariation;
		int rupVarID = this.selectedRupVarScenario;

		ArrayList<Double> imVals = null; 

		try {
			imVals = getIMVals(this.csSite.id, erfID, sgtVarID, rupVarID, srcID, rupID, curIM);
		} catch (SQLException e) {
			//			e.printStackTrace();
			return 0;
		}
		
		ArbitrarilyDiscretizedFunc logFunc = getLogX_OneMinusYCumDistFunction(imVals);
		
		return getProbabilityFromLogCumDistFunc(logFunc, iml);
	}

	/**
	 *  This fills in the exceedance probability for multiple intensityMeasure
	 *  levels (often called a "hazard curve"); the levels are obtained from
	 *  the X values of the input function, and Y values are filled in with the
	 *  asociated exceedance probabilities. NOTE: THE PRESENT IMPLEMENTATION IS
	 *  STRANGE IN THAT WE DON'T NEED TO RETURN ANYTHING SINCE THE FUNCTION PASSED
	 *  IN IS WHAT CHANGES (SHOULD RETURN NULL?).
	 *
	 * @param  intensityMeasureLevels  The function to be filled in
	 * @return                         The function filled in
	 * @exception  ParameterException  Description of the Exception
	 */
	public DiscretizedFuncAPI getExceedProbabilities(
			DiscretizedFuncAPI intensityMeasureLevels
	) throws ParameterException {
		checkInit();
		
		CyberShakeEqkRupture rup = null;
		if (this.eqkRupture instanceof CyberShakeEqkRupture) {
			rup = (CyberShakeEqkRupture)this.eqkRupture;
		} else throw new RuntimeException("The CyberShakeIMR isn't being used with a CyberShake ERF!");

		int srcID = rup.getSrcID();
		int rupID = rup.getRupID();
		int erfID = rup.getErfID();
		int sgtVarID = this.selectedSGTVariation;
		int rupVarID = this.selectedRupVarScenario;

		ArrayList<Double> imVals = null; 

		try {
			imVals = getIMVals(this.csSite.id, erfID, sgtVarID, rupVarID, srcID, rupID, curIM);
		} catch (SQLException e) {
			//			e.printStackTrace();
			for (int i=0; i<intensityMeasureLevels.getNum(); i++) {
				intensityMeasureLevels.set(i, 0);
			}
			return intensityMeasureLevels;
		}
		
		ArbitrarilyDiscretizedFunc logFunc = getLogX_OneMinusYCumDistFunction(imVals);
		
		for (int i=0; i<intensityMeasureLevels.getNum(); i++) {
			double iml = intensityMeasureLevels.getX(i);
			double prob = getProbabilityFromLogCumDistFunc(logFunc, iml);
			intensityMeasureLevels.set(i, prob);
		}
		
		return intensityMeasureLevels;
	}
	
	@Override
	protected void initOtherParams() {

		// Sigma truncation type parameter:
		StringConstraint sigmaTruncTypeConstraint = new StringConstraint();
		sigmaTruncTypeConstraint.addString(SIGMA_TRUNC_TYPE_NONE);
		// only allow NONE
//		sigmaTruncTypeConstraint.addString(SIGMA_TRUNC_TYPE_1SIDED);
//		sigmaTruncTypeConstraint.addString(SIGMA_TRUNC_TYPE_2SIDED);
		sigmaTruncTypeConstraint.setNonEditable();
		sigmaTruncTypeParam = new StringParameter(SIGMA_TRUNC_TYPE_NAME,
				sigmaTruncTypeConstraint,
				SIGMA_TRUNC_TYPE_DEFAULT);
		sigmaTruncTypeParam.setInfo(SIGMA_TRUNC_TYPE_INFO);
		sigmaTruncTypeParam.setNonEditable();

		// Sigma truncation level parameter:
		DoubleConstraint sigmaTruncLevelConstraint = new DoubleConstraint(
				SIGMA_TRUNC_LEVEL_MIN, SIGMA_TRUNC_LEVEL_MAX);
		sigmaTruncLevelConstraint.setNonEditable();
		sigmaTruncLevelParam = new DoubleParameter(SIGMA_TRUNC_LEVEL_NAME,
				sigmaTruncLevelConstraint,
				SIGMA_TRUNC_LEVEL_UNITS,
				SIGMA_TRUNC_LEVEL_DEFAULT);
		sigmaTruncLevelParam.setInfo(SIGMA_TRUNC_LEVEL_INFO);
		sigmaTruncLevelParam.setNonEditable();

		// Put parameters in the otherParams list:
		otherParams.clear();
		otherParams.addParameter(sigmaTruncTypeParam);
		otherParams.addParameter(sigmaTruncLevelParam);

	}

	@Override
	protected void initSupportedIntensityMeasureParams() {

		// Create SA Parameter:
		DoubleConstraint saConstraint = new DoubleConstraint(SA_MIN, SA_MAX);
		saConstraint.setNonEditable();
		saParam = new WarningDoubleParameter(SA_NAME, saConstraint, SA_UNITS);
		saParam.setInfo(SA_INFO);
		DoubleConstraint warn1 = new DoubleConstraint(SA_WARN_MIN, SA_WARN_MAX);
		warn1.setNonEditable();
		saParam.setWarningConstraint(warn1);

		//		// Damping-level parameter for SA
		//		// (overide this in subclass of other damping levels are available)
		//		dampingConstraint = new DoubleDiscreteConstraint();
		//		dampingConstraint.addDouble(DAMPING_DEFAULT);
		//		// leave constrain editable in case subclasses want to add other options
		//		dampingParam = new DoubleDiscreteParameter(DAMPING_NAME, dampingConstraint,
		//		DAMPING_UNITS, DAMPING_DEFAULT);
		//		dampingParam.setInfo(DAMPING_INFO);
		//		dampingParam.setNonEditable();

		DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
//		periodConstraint.addDouble(1);
		periodConstraint.addDouble(2);
		periodConstraint.addDouble(3);
		periodConstraint.addDouble(5);
		periodConstraint.addDouble(10);
		periodConstraint.setNonEditable();
		periodParam = new DoubleDiscreteParameter(PERIOD_NAME, periodConstraint,
				PERIOD_UNITS, null);
		periodParam.setInfo(PERIOD_INFO);
		periodParam.setNonEditable();
		periodParam.setValue(new Double(3));
		periodParam.addParameterChangeListener(this);
		saParam.addIndependentParameter(periodParam);

		supportedIMParams.addParameter(saParam);
	}

	public String getShortName() {
		return "CyberShakeIMR";
	}

	public String getName() {
		return "CyberShake Fake Attenuation Relationship";
	}

	public void setParamDefaults() {
		// TODO Auto-generated method stub

	}

	private CybershakeIM getIMForPeriod(double period) {
		for (CybershakeIM im : csIMs) {
			if (Math.abs(im.getVal() - period) < 0.01) {
				curPeriod = im.getVal();
				System.out.println("Matched period of " + period +  " with: " + im);
				return im;
			}
		}
		return null;
	}

	public void parameterChange(ParameterChangeEvent event) {
		checkInit();
		String paramName = event.getParameterName();

		if (paramName.equals(PERIOD_NAME)) {
			this.curIM = getIMForPeriod((Double)event.getParameter().getValue());
			System.out.println("We got a period of " + (Double)event.getParameter().getValue() + "! " + curIM);
		} else if (paramName.equals(SGT_VAR_PARAM)) {
			selectedSGTVariation = Integer.parseInt((String)sgtVarParam.getValue());
			//			this.reloadParams();
		} else if (paramName.equals(RUP_VAR_SCENARIO_PARAM)) {
			selectedRupVarScenario = Integer.parseInt((String)rupVarScenarioParam.getValue());
			//			this.reloadParams();
		}

	}

	@Override
	protected void initPropagationEffectParams() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setPropagationEffectParams() {
		// TODO Auto-generated method stub

	}

	private String getBuffKey(int siteID, int erfID, int sgtVarID, int rupVarID, int srcID, int rupID, CybershakeIM im) {
		return siteID + "_" + erfID + "_" + sgtVarID + "_" + rupVarID + "_" + srcID + "_" + rupID + "_" + im.getID();
	}

	private ArrayList<Double> getIMVals(int siteID, int erfID, int sgtVarID, int rupVarID, int srcID, int rupID, CybershakeIM im) throws SQLException {
		if (imValsBuff == null) {
			imValsBuff = new LinkedList<ArrayList<Double>>();
			imValsBuffKeys = new LinkedList<String>();
			//			for (int i=0; i<IM_VALS_BUFF_SIZE; i++) {
			//				imValsBuff.add(null);
			//				imValsBuffKeys.add(null);
			//			}
		}

		String key = getBuffKey(siteID, erfID, sgtVarID, rupVarID, srcID, rupID, im);
		for (int i=0; i<imValsBuffKeys.size(); i++) {
			String bufKey = imValsBuffKeys.get(i);

			if (bufKey.equals(key)) {
				return imValsBuff.get(i);
			}
		}

		// if we made it this far, then it's not in the buffer...we'll need to get it manually
//		System.out.println("Loading amps for " + erfID + " " + srcID + " " + rupID);
		int runID = runs2db.getLatestRunID(siteID, erfID, sgtVarID, rupVarID, null, null, null, null);
		ArrayList<Double> imVals = ampsDB.getIM_Values(runID, srcID, rupID, im);

//		String valStr = "";
		for (int i=0; i<imVals.size(); i++) {
			double val = imVals.get(i);
//			valStr += val + " ";
			imVals.set(i, imVals.get(i)/HazardCurveComputation.CONVERSION_TO_G);
		}
//		System.out.println("VALS: " + valStr);

		// first if the buffer is full, make room for it
		if (imValsBuff.size() >= IM_VALS_BUFF_SIZE) {
			imValsBuff.removeFirst();
			imValsBuffKeys.removeFirst();
		}

		// now add it to the buffer
		imValsBuff.add(imVals);
		imValsBuffKeys.add(key);

		return imVals;
	}

	private static final int IM_VALS_BUFF_SIZE = 5;
	private LinkedList<String> imValsBuffKeys = null;
	private LinkedList<ArrayList<Double>> imValsBuff = null;

	private double calcMean(ArrayList<Double> vals) {
		double tot = 0;
		for (double val : vals) {
			tot += Math.log(val);
		}
		double mean = tot / (double)vals.size();
		return mean;
	}

	private double calcStdDev(ArrayList<Double> vals) {
		double mean = calcMean(vals);

		// subtract the mean from each one, square them, and sum them
		double sum = 0;
		for (double val : vals) {
			val = Math.log(val);
			val = val - mean;
			val = Math.pow(val, 2);
			sum += val;
		}
		//		System.out.println("Sum: " + sum);
		// std deviation is the sqrt(sum / (numVals - 1))
		double std = Math.sqrt(sum / (vals.size() - 1));
		//		if (std != 0)
		//			System.out.println("********************************** STD DEV: " + std);
		return std;
	}

	public double getMean() {
		if (csSite == null)
			return Double.NEGATIVE_INFINITY;
		CyberShakeEqkRupture rup = null;
		if (this.eqkRupture instanceof CyberShakeEqkRupture) {
			rup = (CyberShakeEqkRupture)this.eqkRupture;
		} else throw new RuntimeException("The CyberShakeIMR isn't being used with a CyberShake ERF!");

		int srcID = rup.getSrcID();
		int rupID = rup.getRupID();
		int erfID = rup.getErfID();
		int sgtVarID = this.selectedSGTVariation;
		int rupVarID = this.selectedRupVarScenario;

		try {
			ArrayList<Double> imVals = getIMVals(this.csSite.id, erfID, sgtVarID, rupVarID, srcID, rupID, curIM);

			return calcMean(imVals);
		} catch (SQLException e) {
			//			e.printStackTrace();
			//			System.out.println("Skipping rupture: " + srcID + " " + rupID);
			return Double.NEGATIVE_INFINITY;
		}
	}

	public double getStdDev() {
		if (csSite == null)
			return 0;
		CyberShakeEqkRupture rup = null;
		if (this.eqkRupture instanceof CyberShakeEqkRupture) {
			rup = (CyberShakeEqkRupture)this.eqkRupture;
		} else throw new RuntimeException("The CyberShakeIMR isn't being used with a CyberShake ERF!");

		int srcID = rup.getSrcID();
		int rupID = rup.getRupID();
		int erfID = rup.getErfID();
		int sgtVarID = this.selectedSGTVariation;
		int rupVarID = this.selectedRupVarScenario;

		try {
			ArrayList<Double> vals = getIMVals(this.csSite.id, erfID, sgtVarID, rupVarID, srcID, rupID, curIM);

			return calcStdDev(vals);
		} catch (SQLException e) {
			//			e.printStackTrace();
//			System.out.println("Skipping rupture: " + srcID + " " + rupID);
			return 0;
		}
	}

	@Override
	public ListIterator getOtherParamsIterator() {
		// this is called when the IMR gets activated in the GUI bean
		checkInit();
		return super.getOtherParamsIterator();
	}

	public static void main(String args[]) {
		CyberShakeIMR imr = new CyberShakeIMR(null);
		imr.checkInit();
		try {
			imr.getIMVals(28, 34, 5, 3, 1, 0, new CybershakeIM(21, "safddsa", 3, ""));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(1);
	}
}