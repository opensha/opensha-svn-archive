package scratch.kevin.ucerf3.erf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

/**
 * This is the MeanUCERF3 ERF. It extends UCERF3_FaultSysSol_ERF, but allows and facilitates creation
 * of solution subsets with different approximations:
 * <br>
 * <br><b>Upper Depth Tolerance:</b>
 * <br>Some sections have varying upper depths on different branches due to variable aseismicity values.
 * This parameter combines upper depths within the given tolerance to reduce the subsection (and thus
 * rupture) count. The "Use Mean Upper Depth" parameter allows you to use the average upper depth
 * (default uses the shallower depth).
 * <br><b>Magnitude Tolerance:</b>
 * <br>Ruptures have varying magnitudes on different branches due to different scaling relationships
 * and even variable aseismicity values. This parameter averages magnitudes within the given tolerance
 * to reducde the ruptures substantially. <b>NOTE: enabling aleatory magnitude variability will first
 * average magnitudes for each rupture!<b>
 * <br><b>Rake Combining:</b>
 * <br>Each Deformation Model supplies its own rakes. You can either use the DM specific rakes for each
 * rupture, or combine otherwise identical ruptures. In this case you can use the weighted averaged
 * rake or the rakes from a specific deformation model.
 * <br><br>
 * @author kevin
 *
 */
public class MeanUCERF3 extends UCERF3_FaultSysSol_ERF {
	
	private static final boolean D = true;
	
	public static final String NAME = "Mean UCERF3";
	
	private static final String DOWNLOAD_URL = "http://opensha.usc.edu/ftp/mean_ucerf3/";
	private static final String RAKE_BASIS_FILE_NAME = "rake_basis.zip";
	private static final String TRUE_MEAN_FILE_NAME = "mean_ucerf3_sol.zip";
	
	private DoubleParameter upperDepthTolParam;
	private double upperDepthTol;
	
	private BooleanParameter upperDepthUseMeanParam;
	private boolean upperDepthUseMean;
	
	private DoubleParameter magTolParam;
	private double magTol;
	
	private StringParameter rakeBasisParam;
	private String rakeBasisStr;
	private static final String RAKE_BASIS_NONE = "Do Not Combine";
	private static final String RAKE_BASIS_MEAN = "Def. Model Mean";
	private Map<HashSet<String>, Double> rakeBasis;
	
	private BooleanParameter ignoreCacheParam;
	private boolean ignoreCache;
	
	private File storeDir;
	private FaultSystemSolution meanTotalSol;
	private DiscretizedFunc[] meanTotalMFDs;
	
	private static File getStoreDir() {
		// first check user prop
		String path = System.getProperty("uc3.store");
		if (path != null) {
			File file = new File(path);
			if (!file.exists())
				Preconditions.checkState(file.mkdir(),
						"Couldn't create uc3.store location: "+file.getAbsolutePath());
			return file;
		}
		
		// now see if we're running in eclispe
		File scratchDir = UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR;
		if (scratchDir.exists()) {
			File meanDir = new File(scratchDir, "MeanUCERF3");
			if (!meanDir.exists())
				Preconditions.checkState(meanDir.mkdir(),
						"Couldn't create MeanUCERF3 eclipse location: "+meanDir.getAbsolutePath());
			return meanDir;
		}
		
		// just use user.home
		path = System.getProperty("user.home");
		File homeDir = new File(path);
		Preconditions.checkState(homeDir.exists(), "user.home dir doesn't exist: "+path);
		File openSHADir = new File(homeDir, ".opensha");
		if (!openSHADir.exists())
			Preconditions.checkState(openSHADir.mkdir(),
					"Couldn't create OpenSHA store location: "+openSHADir.getAbsolutePath());
		File uc3Dir = new File(openSHADir, "mean_ucerf3");
		if (!uc3Dir.exists())
			Preconditions.checkState(uc3Dir.mkdir(),
					"Couldn't create MeanUCERF3 store location: "+uc3Dir.getAbsolutePath());
		return uc3Dir;
	}
	
	public MeanUCERF3() {
		this(getStoreDir());
	}
	
	public MeanUCERF3(File storeDir) {
		this(null, storeDir);
	}

	public MeanUCERF3(FaultSystemSolution meanTotalSol) {
		this(meanTotalSol, getStoreDir());
	}
	
	public MeanUCERF3(FaultSystemSolution meanTotalSol, File storeDir) {
		super();
		ignoreMinMagCheckParam.setValue(true);
		this.meanTotalSol = meanTotalSol;
		this.storeDir = storeDir;
		System.out.println("MeanUCERF3 store dir: "+storeDir);
		Preconditions.checkState(storeDir.exists(), "Store dir doesn't exist: "+storeDir.getAbsolutePath());
		
		upperDepthTolParam = new DoubleParameter("Sect Upper Depth Averaging Tolerance", 0d, 100d);
		upperDepthTolParam.setValue(0d);
		upperDepthTolParam.setUnits("km");
		upperDepthTolParam.setInfo("Some fault sections have different aseismicity values across UCERF3" +
				"\nlogic tree branches. These values change the upper depth of the fault section. If > 0," +
				"\nsections with upper depths within the given tolerance of the mean will be combined in order" +
				"\nto reduce the overall section and rupture count.");
		upperDepthTolParam.addParameterChangeListener(this);
		adjustableParams.addParameter(upperDepthTolParam);
		upperDepthTol = upperDepthTolParam.getValue();
		
		upperDepthUseMeanParam = new BooleanParameter("Use Mean Upper Depth", false);
		upperDepthUseMeanParam.setInfo("If true and upper depth combine tolerance is > 0, mean upper" +
				"\ndepth will be used, else the shallowest upper depth will be used when averaging." +
				"\nNote that averaging does not incorporate participation rates, it is a true mean" +
				"\nand may not be representative.");
		upperDepthUseMeanParam.addParameterChangeListener(this);
		adjustableParams.addParameter(upperDepthUseMeanParam);
		upperDepthUseMean = upperDepthUseMeanParam.getValue();
		
		magTolParam = new DoubleParameter("Rup Mag Averaging Tolerance", 0d, 1d);
		magTolParam.setValue(0d);
		magTolParam.setInfo("Each rupture has a suite of magnitudes from the different scaling relationships." +
				"\nThese magnitudes can be averaged (within a tolerance) in order to reduce the total rupture" +
				"\ncount. Magnitudes are averaged weighted by their rate. Set to '10' to average all mags" +
				"\nfor each rupture.");
		magTolParam.addParameterChangeListener(this);
		adjustableParams.addParameter(magTolParam);
		magTol = magTolParam.getValue();
		
		ArrayList<String> rakeBasisStrs = Lists.newArrayList(RAKE_BASIS_NONE, RAKE_BASIS_MEAN);
		for (DeformationModels dm : DeformationModels.values())
			if (dm.getRelativeWeight(null) > 0)
				rakeBasisStrs.add(dm.name());
		rakeBasisParam = new StringParameter("Rupture Rake To Use", rakeBasisStrs, RAKE_BASIS_NONE);
		rakeBasisParam.setInfo("Each deformation model supplies rake values for each fault section" +
				"\n(and thus each rupture). Invididual rakes can be used, or the rupture count can" +
				"\nbe reduced by either using the rate-averaged rake or rakes from a specific" +
				"\ndeformation model.");
		rakeBasisParam.addParameterChangeListener(this);
		adjustableParams.addParameter(rakeBasisParam);
		rakeBasisStr = rakeBasisParam.getValue();
		loadRakeBasis();
		
		ignoreCacheParam = new BooleanParameter("Ignore Cache", false);
		ignoreCacheParam.setInfo("MeanUCERF3 caches reduced solutions to save time. Setting this to" +
				"\ntrue will disable loading cached versions.");
		ignoreCacheParam.addParameterChangeListener(this);
		adjustableParams.addParameter(ignoreCacheParam);
		upperDepthUseMean = ignoreCacheParam.getValue();
		
		// ensure disabled by default
		aleatoryMagAreaStdDevParam.setValue(0d);
		
		adjustableParams.removeParameter(fileParam);
	}
	
	@Override
	public void updateForecast() {
		if (D) System.out.println("updateForecast called");
		if (getSolution() == null) {
			// this means that we have to load/build the solution (parameter change or never loaded)
			fetchSolution();
		}
		super.updateForecast();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		// we set the solution to null as a flag that something has changed
		// and we need to rebuild the solution on the next update forecast call
		
		Parameter<?> param = event.getParameter();
		if (param == upperDepthTolParam) {
			upperDepthTol = upperDepthTolParam.getValue();
			setSolution(null);
		} else if (param == upperDepthUseMeanParam) {
			upperDepthUseMean = upperDepthUseMeanParam.getValue();
			setSolution(null);
		} else if (param == magTolParam) {
			magTol = magTolParam.getValue();
			setSolution(null);
		} else if (param == rakeBasisParam) {
			rakeBasisStr = rakeBasisParam.getValue();
			loadRakeBasis();
			setSolution(null);
		} else if (param == ignoreCacheParam) {
			ignoreCache = ignoreCacheParam.getValue();
			setSolution(null);
		} else {
			super.parameterChange(event);
		}
	}

	/**
	 * This loads the rake basis for rake-combining
	 */
	private void loadRakeBasis() {
		if (D) System.out.println("loading rake basis for: "+rakeBasisStr);
		if (rakeBasisStr.equals(RAKE_BASIS_NONE) || rakeBasisStr.equals(RAKE_BASIS_MEAN)) {
			rakeBasis = null;
			return;
		}
		
		File rakeBasisFile = checkDownload(RAKE_BASIS_FILE_NAME);
		
		DeformationModels dm = null;
		for (DeformationModels testDM : DeformationModels.values()) {
			if (testDM.name().equals(rakeBasisStr)) {
				dm = testDM;
				break;
			}
		}
		Preconditions.checkState(dm != null, "Couldn't find DM: "+rakeBasisStr);
		
		try {
			ZipFile zip = new ZipFile(rakeBasisFile);
			
			rakeBasis = RakeBasisWriter.loadRakeBasis(zip, dm);
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}
	
	private boolean isTrueMean() {
		return upperDepthTol == 0 && magTol == 0 && rakeBasisStr.equals(RAKE_BASIS_NONE);
	}
	
	public void setTrueMeanSol(FaultSystemSolution meanTotalSol) {
		this.meanTotalSol = meanTotalSol;
		if (meanTotalSol == null)
			this.meanTotalMFDs = null;
		else
			this.meanTotalMFDs = meanTotalSol.getRupMagDists();
	}
	
	/**
	 * This loads the solution with all reductions applied. It will cache
	 * solutions locally and laod from that cache if already computed
	 */
	private void fetchSolution() {
		if (D) System.out.println("fetchSolution called");
		// first see if what we already need is cached
		
		// don't use mag here since we keep that until later
		String fName = "dep"+(float)upperDepthTol;
		if (upperDepthUseMean && upperDepthTol > 0)
			fName += "_depMean";
		else
			fName += "_depShallow";
		if (rakeBasisStr.equals(RAKE_BASIS_MEAN))
			fName += "_rakeMean";
		else if (rakeBasisStr.equals(RAKE_BASIS_NONE))
			fName += "_rakeAll";
		else
			fName += "_rake"+rakeBasisStr;
		
		File solFile = new File(storeDir, "cached_"+fName+".zip");
		if (!ignoreCache && solFile.exists()) {
			// already cached
			if (D) System.out.println("already cached: "+solFile.getName());
			try {
				FaultSystemSolution sol = FaultSystemIO.loadSol(solFile);
				checkCombineMags(sol);
				setSolution(sol);
				return;
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		// if we've gotten this far, we'll need the mean
		if (meanTotalSol == null) {
			// not loaded yet
			if (D) System.out.println("loading mean sol");
			File meanSolFile = checkDownload(TRUE_MEAN_FILE_NAME);
			
			try {
				setTrueMeanSol(FaultSystemIO.loadSol(meanSolFile));
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		if (isTrueMean()) {
			// simple case, just use the mean
			if (D) System.out.println("isTrueMean() = true");
			setSolution(meanTotalSol);
			return;
		}
		
		boolean combineRakes = !rakeBasisStr.equals(RAKE_BASIS_NONE);
		
		FaultSystemSolution reducedSol;
		if (upperDepthTol > 0 || combineRakes) {
			if (D) System.out.println("getting reduced sol");
			reducedSol = RuptureCombiner.getCombinedSolution(meanTotalSol, upperDepthTol,
					upperDepthUseMean, combineRakes, rakeBasis);
			
			// cache it
			try {
				if (D) System.out.println("caching reduced sol to: "+solFile.getName());
				FaultSystemIO.writeSol(reducedSol, solFile);
			} catch (Exception e) {
				// don't fail on caching
				e.printStackTrace();
			}
		} else {
			// must be just mags, create a new sol
			if (D) System.out.println("no reduce, just copying");
			reducedSol = new FaultSystemSolution(meanTotalSol.getRupSet(), meanTotalSol.getRateForAllRups());
			reducedSol.setRupMagDists(meanTotalMFDs);
			reducedSol.setGridSourceProvider(meanTotalSol.getGridSourceProvider());
		}
		
		// deal with mags
		checkCombineMags(reducedSol);
		
		// set the solution
		setSolution(reducedSol);
		
		if (D) System.out.println("fetchSolution done");
	}
	
	private void checkCombineMags(FaultSystemSolution sol) {
		if (magTol > 0) {
			if (D) System.out.println("combining mags");
			if (magTol >= 10)
				sol.setRupMagDists(null);
			else
				sol.setRupMagDists(RuptureCombiner.combineMFDs(magTol, sol.getRupMagDists()));
		}
	}
	
	/**
	 * This downloads the selected file from the OpenSHA server if not already cached locally
	 * 
	 * @param fName
	 */
	private File checkDownload(String fName) {
		File file = new File(storeDir, fName);
		if (file.exists())
			return file;
		CalcProgressBar progress = null;
		// try to show progress bar
		try {
			progress = new CalcProgressBar("Downloading MeanUCERF3 Files", "downloading "+fName);
		} catch (Exception e) {}
		String url = DOWNLOAD_URL+fName;
		System.out.print("Downloading "+url+" to "+file.getAbsolutePath());
		try {
			FileUtils.downloadURL(url, file);
		} catch (Exception e) {
			if (progress != null) {
				// not headless
				progress.setVisible(false);
				progress.dispose();
				String message = "Error downloading "+fName+".\nServer down or file moved, try again later.";
				JOptionPane.showMessageDialog(null, message, "Download Error", JOptionPane.ERROR_MESSAGE);
			}
			ExceptionUtils.throwAsRuntimeException(e);
		}
		System.out.println("DONE.");
		if (progress != null) {
			progress.setVisible(false);
			progress.dispose();
		}
		return file;
	}

}
