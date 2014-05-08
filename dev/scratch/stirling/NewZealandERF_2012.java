package scratch.stirling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;

/**
 * An ERF for New Zealand using updated 2012 sources.
 *
 * @author Mark Stirling
 */
public class NewZealandERF_2012 extends AbstractERF {

	public static final String NAME = "New Zealand ERF 2012";
	
	public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
	public final static String BACK_SEIS_INCLUDE = new String ("Include");
	public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
	private StringParameter backSeisParam;
	
	private static final double FAULT_SPACING = 0.1;
	private static final double DURATION = 50.0;
	
	private List<ProbEqkSource> faultSources;
	private List<ProbEqkSource> gridSources;

	public NewZealandERF_2012() {
		
		//create the timespan object with start time and duration in years
		timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
		timeSpan.setDuration(50);
		timeSpan.addParameterChangeListener(this);

		initSources();
	}
	
	private void initSources() {
		try {
			faultSources = NewZealandParser.loadFaultSources(FAULT_SPACING, DURATION);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			
		}
	}
	
	private void initAdjParams() {
		// background seismicity include/exclude  
		ArrayList<String> backSeisOptionsStrings = new ArrayList<String>();
		backSeisOptionsStrings.add(UCERF2.BACK_SEIS_EXCLUDE);
		backSeisOptionsStrings.add(UCERF2.BACK_SEIS_INCLUDE);
		backSeisOptionsStrings.add(UCERF2.BACK_SEIS_ONLY);
		backSeisParam = new StringParameter(UCERF2.BACK_SEIS_NAME, backSeisOptionsStrings, UCERF2.BACK_SEIS_DEFAULT);
		backSeisParam.setInfo("Background source enabler");

	}

	@Override
	public int getNumSources() {
		return faultSources.size();
		// TODO do nothing
		
	}

	@Override
	public ProbEqkSource getSource(int idx) {
		return faultSources.get(idx);
	}

	@Override
	public void updateForecast() {
		if(parameterChangeFlag) {
			System.out.println("parameter changed");
//			allSources = new ArrayList<ProbEqkSource>();
//			mkFaultSources();
//			String bgVal = (String)backSeisParam.getValue();
//			if(bgVal.equals(BACK_SEIS_INCLUDE)){
//				mkBackRegion();
//			}
		}
		parameterChangeFlag = false;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
}
