package scratch.UCERF3.erf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class UCERF3_FaultSysSol_ERF extends FaultSystemSolutionPoissonERF {

	private GridSourceProvider gridSources;
	public static final String NAME = "UCERF3 Poisson ERF";
	private String name = NAME;
	
	/**
	 * No-arg constructor. This sets ERF to include background sources and
	 * the aftershock filter is off (aftershocks included).
	 */
	public UCERF3_FaultSysSol_ERF() {
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
	}
	
	/**
	 * Constructs a new ERF using the supplied {@code file}. {@code File} must
	 * be a zipped up fault system solution.
	 * @param file
	 */
	public UCERF3_FaultSysSol_ERF(File file) {
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
		fileParam.setValue(file);
	}
	
	/**
	 * Constructs a new ERF using an {@code FaultSystemSolution}.
	 * @param faultSysSolution
	 */
	public UCERF3_FaultSysSol_ERF(FaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
	}
		
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		return gridSources.getSource(iSource, timeSpan.getDuration(),
			applyAftershockFilter, bgRupType == BackgroundRupType.CROSSHAIR);
	}

	@Override
	protected void initOtherSources() {
			System.out.println("Initing other sources...");
			
			FaultSystemSolution sol = getSolution();

			// fetch grid sources from solution. By default this will be a UC3_GridSourceGenerator
			// unless the grid sources have been cached or averaged (for a mean solution).
			gridSources = sol.getGridSourceProvider();
			
			if (bgRupType.equals(BackgroundRupType.POINT)) {
				// default is false; gridGen will create point sources for those
				// with M<6 anyway; this forces those M>6 to be points as well
				gridSources.setAsPointSources(true);
			}
	
			// update parent source count
			numOtherSources = gridSources.size();
	}	
	
	/**
	 * Sets the erf name. For UCERF3 erf this will commonly be the branch
	 * identifier string or similar.
	 * @param name
	 */
	public void setName(String name) {
		checkArgument(!StringUtils.isBlank(name), "Name cannot be empty");
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String f = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/refGR/FM3_1_NEOK_EllB_DsrUni_GRConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_run5_sol.zip";
//		String f = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/refCH/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_run5_sol.zip";
		String f ="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";

		File file = new File(f);
				
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF();
		erf.getParameter(FILE_PARAM_NAME).setValue(file);
		erf.updateForecast();
//		UCERF3_FaultSysSol_ERF erf = FaultSysSolutionERF_Calc.getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL_OFF,SmallMagScaling.MO_REDUCTION);
//		int otherRups = 0;
//		for (int i=0; i<erf.gridSources.size(); i++) {
//			ProbEqkSource src = erf.gridSources.getSource(i, 1d, false, false);
//			otherRups += src.getNumRuptures();
//		}
//		System.out.println("NumOtherRups: " + otherRups);
//		System.out.println("src100rups: " + erf.getSource(100).getNumRuptures());
//		System.out.println();
	}

}
