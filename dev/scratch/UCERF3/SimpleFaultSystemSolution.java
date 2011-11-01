package scratch.UCERF3;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.utils.DeformationModelFetcher.DefModName;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;

/**
 * This is a simple (array based) implementation of a {@link FaultSystemSolution}. It is similar to
 * {@link SimpleFaultSystemRupSet} but extends {@link FaultSystemSolution} to provide rupture
 * rates and various calculations. It can also be loaded to/from XML.
 * 
 * @author Kevin
 *
 */
public class SimpleFaultSystemSolution extends FaultSystemSolution implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "SimpleFaultSystemSolution";
	
	private FaultSystemRupSet rupSet;
	protected double[] rupRateSolution;
	
	protected double[][] fractRupsInsideMFD_Regions = null;
	
	/**
	 * Creates a SimpleFaultSystemSolution from any solution.
	 * 
	 * @param solution
	 */
	public SimpleFaultSystemSolution(FaultSystemSolution solution) {
		this(solution, solution.getRateForAllRups());
	}
	
	/**
	 * Creates a SimpleFaultSystemSolution from the given rupture set, and array of rates
	 * 
	 * @param rupSet
	 * @param rupRateSolution
	 */
	public SimpleFaultSystemSolution(FaultSystemRupSet rupSet, double[] rupRateSolution) {
		Preconditions.checkNotNull(rupSet, "FaultSystemRupSet passed in cannot be null!");
		this.rupSet = rupSet;
		Preconditions.checkState(
				rupRateSolution == null || rupRateSolution.length == rupSet.getNumRuptures(),
				"solution must either be null, or the correct size!");
		this.rupRateSolution = rupRateSolution;
	}

	@Override
	public int getNumRuptures() {
		return rupSet.getNumRuptures();
	}

	@Override
	public int getNumSections() {
		return rupSet.getNumSections();
	}

	@Override
	public List<List<Integer>> getSectionIndicesForAllRups() {
		return rupSet.getSectionIndicesForAllRups();
	}

	@Override
	public List<Integer> getSectionsIndicesForRup(int rupIndex) {
		return rupSet.getSectionsIndicesForRup(rupIndex);
	}

	@Override
	public double[] getMagForAllRups() {
		return rupSet.getMagForAllRups();
	}

	@Override
	public double getMagForRup(int rupIndex) {
		return rupSet.getMagForRup(rupIndex);
	}

	@Override
	public double[] getAveSlipForAllRups() {
		return rupSet.getAveSlipForAllRups();
	}

	@Override
	public double getAveSlipForRup(int rupIndex) {
		return rupSet.getAveSlipForRup(rupIndex);
	}

	@Override
	public List<double[]> getSlipOnSectionsForAllRups() {
		return rupSet.getSlipOnSectionsForAllRups();
	}

	@Override
	public double[] getSlipOnSectionsForRup(int rthRup) {
		return rupSet.getSlipOnSectionsForRup(rthRup);
	}

	@Override
	public double[] getAveRakeForAllRups() {
		return rupSet.getAveRakeForAllRups();
	}

	@Override
	public double getAveRakeForRup(int rupIndex) {
		return rupSet.getAveRakeForRup(rupIndex);
	}

	@Override
	public double[] getAreaForAllRups() {
		return rupSet.getAreaForAllRups();
	}

	@Override
	public double getAreaForRup(int rupIndex) {
		return rupSet.getAreaForRup(rupIndex);
	}

	@Override
	public double[] getAreaForAllSections() {
		return rupSet.getAreaForAllSections();
	}

	@Override
	public double getAreaForSection(int sectIndex) {
		return rupSet.getAreaForSection(sectIndex);
	}

	@Override
	public List<FaultSectionPrefData> getFaultSectionDataList() {
		return rupSet.getFaultSectionDataList();
	}

	@Override
	public FaultSectionPrefData getFaultSectionData(int sectIndex) {
		return rupSet.getFaultSectionData(sectIndex);
	}

	@Override
	public double getSlipRateForSection(int sectIndex) {
		return rupSet.getSlipRateForSection(sectIndex);
	}

	@Override
	public double[] getSlipRateForAllSections() {
		return rupSet.getSlipRateForAllSections();
	}

	@Override
	public double getSlipRateStdDevForSection(int sectIndex) {
		return rupSet.getSlipRateStdDevForSection(sectIndex);
	}

	@Override
	public double[] getSlipRateStdDevForAllSections() {
		return rupSet.getSlipRateStdDevForAllSections();
	}

	@Override
	public String getInfoString() {
		return rupSet.getInfoString();
	}

	@Override
	public double getRateForRup(int rupIndex) {
		return rupRateSolution[rupIndex];
	}

	@Override
	public double[] getRateForAllRups() {
		return rupRateSolution;
	}

	@Override
	@Deprecated
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		XMLUtils.doubleArrayToXML(el, rupRateSolution, "rupRateSolution");
		
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.toSimple(rupSet);
		simpleRupSet.toXMLMetadata(el);
		
		return root;
	}
	
	public static SimpleFaultSystemSolution fromXMLMetadata(Element solutionEl) {
		double[] rupRateSolution = XMLUtils.doubleArrayFromXML(solutionEl.element("rupRateSolution"));
		
		Element rupSetEl = solutionEl.element(SimpleFaultSystemRupSet.XML_METADATA_NAME);
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.fromXMLMetadata(rupSetEl);
		
		return new SimpleFaultSystemSolution(simpleRupSet, rupRateSolution);
	}
	
	public static SimpleFaultSystemSolution fromFile(File file) throws IOException, DocumentException {
		if (SimpleFaultSystemRupSet.isXMLFile(file))
			return fromXMLFile(file);
		else
			return fromZipFile(file);
	}
	
	public static FaultSystemRupSet fromFileAsApplicable(File file) throws IOException, DocumentException {
		if (SimpleFaultSystemRupSet.isXMLFile(file)) {
			Document doc = XMLUtils.loadDocument(file);
			Element root = doc.getRootElement();
			// first try to load as a solution
			Element el = root.element(SimpleFaultSystemSolution.XML_METADATA_NAME);
			if (el != null) {
				return fromXMLMetadata(el);
			} else {
				el = root.element(SimpleFaultSystemRupSet.XML_METADATA_NAME);
				return SimpleFaultSystemRupSet.fromXMLMetadata(el);
			}
		} else {
			ZipFile zip = new ZipFile(file);
			ZipEntry ratesEntry = zip.getEntry("rates.bin");
			if (ratesEntry != null)
				return fromZipFile(file);
			else
				return SimpleFaultSystemRupSet.fromZipFile(file);
		}
	}
	
	@Deprecated
	public void toXMLFile(File file) throws IOException {
		XMLUtils.writeObjectToXMLAsRoot(this, file);
	}
	
	public static SimpleFaultSystemSolution fromXMLFile(File file) throws MalformedURLException, DocumentException {
		Document doc = XMLUtils.loadDocument(file);
		Element solutionEl = doc.getRootElement().element(XML_METADATA_NAME);
		return fromXMLMetadata(solutionEl);
	}
	
	public void toZipFile(File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		
		ArrayList<String> zipFileNames = new ArrayList<String>();
		
		File ratesFile = new File(tempDir, "rates.bin");
		MatrixIO.doubleArrayToFile(rupRateSolution, ratesFile);
		zipFileNames.add(ratesFile.getName());
		
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.toSimple(rupSet);
		simpleRupSet.toZipFile(file, tempDir, zipFileNames);
	}
	
	public static SimpleFaultSystemSolution fromZipFile(File zipFile) throws ZipException, IOException, DocumentException {
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.fromZipFile(zipFile);
		
		return fromZipFile(zipFile, simpleRupSet);
	}
	
	private static SimpleFaultSystemSolution fromZipFile(File zipFile, SimpleFaultSystemRupSet simpleRupSet)
	throws ZipException, IOException, DocumentException {
		ZipFile zip = new ZipFile(zipFile);
		
		ZipEntry ratesEntry = zip.getEntry("rates.bin");
		double[] rates = MatrixIO.doubleArrayFromInputStream(
				new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
		
		return new SimpleFaultSystemSolution(simpleRupSet, rates);
	}

	@Override
	public List<Integer> getCloseSectionsList(int sectIndex) {
		return rupSet.getCloseSectionsList(sectIndex);
	}

	@Override
	public List<List<Integer>> getCloseSectionsListList() {
		return rupSet.getCloseSectionsListList();
	}

	@Override
	public int getNumClusters() {
		return rupSet.getNumClusters();
	}

	@Override
	public boolean isClusterBased() {
		return rupSet.isClusterBased();
	}

	@Override
	public int getNumRupturesForCluster(int index) {
		return rupSet.getNumRupturesForCluster(index);
	}

	@Override
	public List<Integer> getSectionsForCluster(int index) {
		return rupSet.getSectionsForCluster(index);
	}

	@Override
	public List<Integer> getRupturesForCluster(int index)
			throws IndexOutOfBoundsException {
		return rupSet.getRupturesForCluster(index);
	}

	@Override
	public DefModName getDeformationModelName() {
		return rupSet.getDeformationModelName();
	}
	
	
	/**
	 * This plots the rupture rates (rate versus rupture index)
	 */
	public void plotRuptureRates() {
		// Plot the rupture rates
		ArrayList funcs = new ArrayList();		
		EvenlyDiscretizedFunc ruprates = new EvenlyDiscretizedFunc(0,(double)rupRateSolution.length-1,rupRateSolution.length);
		for(int i=0; i<rupRateSolution.length; i++)
			ruprates.set(i,rupRateSolution[i]);
		funcs.add(ruprates); 	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Inverted Rupture Rates"); 
		graph.setX_AxisLabel("Rupture Index");
		graph.setY_AxisLabel("Rate");

	}
	
	/**
	 * This plots original and final slip rates versus section index.
	 * This also plot these averaged over parent sections.
	 */
	public void plotSlipRates() {
		int numSections = this.getNumSections();
		int numRuptures = this.getNumRuptures();
		List<FaultSectionPrefData> faultSectionData = this.getFaultSectionDataList();

		ArrayList funcs2 = new ArrayList();		
		EvenlyDiscretizedFunc syn = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc data = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		for (int i=0; i<numSections; i++) {
			data.set(i, this.getSlipRateForSection(i));
			syn.set(i,0);
		}
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			List<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				syn.add(row,slips[i]*rupRateSolution[rup]);
			}
		}
		for (int i=0; i<numSections; i++) data.set(i, this.getSlipRateForSection(i));
		funcs2.add(syn);
		funcs2.add(data);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Slip Rate Synthetics (blue) & Data (black)"); 
		graph2.setX_AxisLabel("Fault Section Index");
		graph2.setY_AxisLabel("Slip Rate");
		
		String info = "index\tratio\tpredSR\tdataSR\tParentSectionName\n";
		String parentSectName = "";
		double aveData=0, aveSyn=0, numSubSect=0;
		ArrayList<Double> aveDataList = new ArrayList<Double>();
		ArrayList<Double> aveSynList = new ArrayList<Double>();
		for (int i = 0; i < numSections; i++) {
			if(!faultSectionData.get(i).getParentSectionName().equals(parentSectName)) {
				if(i != 0) {
					double ratio  = aveSyn/aveData;
					aveSyn /= numSubSect;
					aveData /= numSubSect;
					info += aveSynList.size()+"\t"+(float)ratio+"\t"+(float)aveSyn+"\t"+(float)aveData+"\t"+faultSectionData.get(i-1).getParentSectionName()+"\n";
//					System.out.println(ratio+"\t"+aveSyn+"\t"+aveData+"\t"+faultSectionData.get(i-1).getParentSectionName());
					aveSynList.add(aveSyn);
					aveDataList.add(aveData);
				}
				aveSyn=0;
				aveData=0;
				numSubSect=0;
				parentSectName = faultSectionData.get(i).getParentSectionName();
			}
			aveSyn +=  syn.getY(i);
			aveData +=  data.getY(i);
			numSubSect += 1;
		}
		ArrayList funcs5 = new ArrayList();		
		EvenlyDiscretizedFunc aveSynFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		EvenlyDiscretizedFunc aveDataFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		for(int i=0; i<aveSynList.size(); i++ ) {
			aveSynFunc.set(i, aveSynList.get(i));
			aveDataFunc.set(i, aveDataList.get(i));
		}
		aveSynFunc.setName("Predicted ave slip rates on parent section");
		aveDataFunc.setName("Original (Data) ave slip rates on parent section");
		aveSynFunc.setInfo(info);
		funcs5.add(aveSynFunc);
		funcs5.add(aveDataFunc);
		GraphiWindowAPI_Impl graph5 = new GraphiWindowAPI_Impl(funcs5, "Average Slip Rates on Parent Sections"); 
		graph5.setX_AxisLabel("Parent Section Index");
		graph5.setY_AxisLabel("Slip Rate");

	}
	
	/**
	 * This compares observed section rates (supplied) with those implied by the
	 * Fault System Solution.
	 * 
	 */
	public void plotPaleoObsAndPredPaleoEventRates(ArrayList<SegRateConstraint> segRateConstraints) {
		int numSections = this.getNumSections();
		int numRuptures = this.getNumRuptures();
		ArrayList funcs3 = new ArrayList();		
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc finalPaleoVisibleEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);	
		for (int r=0; r<numRuptures; r++) {
			List<Integer> rup= getSectionsIndicesForRup(r);
			for (int i=0; i<rup.size(); i++) {			
				finalEventRateFunc.add(rup.get(i),rupRateSolution[r]);  
				finalPaleoVisibleEventRateFunc.add(rup.get(i),this.getProbPaleoVisible(this.getMagForRup(r))*rupRateSolution[r]);  			
			}
		}	
		finalEventRateFunc.setName("Total Event Rates oer Section");
		finalPaleoVisibleEventRateFunc.setName("Paleo Visible Event Rates oer Section");
		funcs3.add(finalEventRateFunc);
		funcs3.add(finalPaleoVisibleEventRateFunc);	
		int num = segRateConstraints.size();
		ArbitrarilyDiscretizedFunc func;
		ArrayList obs_er_funcs = new ArrayList();
		SegRateConstraint constraint;
		for (int c = 0; c < num; c++) {
			func = new ArbitrarilyDiscretizedFunc();
			constraint = segRateConstraints.get(c);
			int seg = constraint.getSegIndex();
			func.set((double) seg - 0.0001, constraint.getLower95Conf());
			func.set((double) seg, constraint.getMean());
			func.set((double) seg + 0.0001, constraint.getUpper95Conf());
			func.setName(constraint.getFaultName());
			funcs3.add(func);
		}			
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "Synthetic Event Rates (total - black & paleo visible - blue) and Paleo Data (red)");
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLUE));
		for (int c = 0; c < num; c++)
			plotChars.add(new PlotCurveCharacterstics(
					PlotLineType.SOLID, 1f, PlotSymbol.FILLED_CIRCLE, 4f, Color.RED));
		graph3.setPlottingFeatures(plotChars);
		graph3.setX_AxisLabel("Fault Section Index");
		graph3.setY_AxisLabel("Event Rate (per year)");

	}
	
	
	/**
	 * This compares the MFDs in the given MFD constraints with the MFDs 
	 * implied by the Fault System Solution
	 * @param mfdConstraints
	 */
	public void plotMFDs(ArrayList<MFD_InversionConstraint> mfdConstraints) {
		
		for (int i=0; i<mfdConstraints.size(); i++) {  // Loop over each MFD constraint 	
			IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,40,0.1);
			magHist.setTolerance(0.2);	// this makes it a histogram
			computeFractRupsInsideMFD_Regions(mfdConstraints);
			for(int rup=0; rup<getNumRuptures(); rup++) {
				double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
				magHist.add(getMagForRup(rup), fractRupInside*rupRateSolution[rup]);
			}
			ArrayList funcs4 = new ArrayList();
			magHist.setName("Magnitude Distribution of SA Solution");
			magHist.setInfo("(number in each mag bin)");
			funcs4.add(magHist);
			IncrementalMagFreqDist targetMagFreqDist = mfdConstraints.get(i).getMagFreqDist();; 
			targetMagFreqDist.setTolerance(0.1); 
			targetMagFreqDist.setName("Target Magnitude Distribution");
			targetMagFreqDist.setInfo("UCERF2 Solution minus background (with aftershocks added back in)");
			funcs4.add(targetMagFreqDist);
			GraphiWindowAPI_Impl graph4 = new GraphiWindowAPI_Impl(funcs4, "Magnitude Histogram for Final Rates"); 
			graph4.setX_AxisLabel("Magnitude");
			graph4.setY_AxisLabel("Frequency (per bin)");
		}
	}
	
	
	/**
	 * This computes the fraction of each rupture inside each region in the given mfdConstraints, where results
	 * are stored in the fractRupsInsideMFD_Regions[iRegion][iRup] double array
	 * @param mfdConstraints
	 */
	protected void computeFractRupsInsideMFD_Regions(ArrayList<MFD_InversionConstraint> mfdConstraints) {
		if(fractRupsInsideMFD_Regions == null) {	// do only if not already done
			int numRuptures = getNumRuptures();
			fractRupsInsideMFD_Regions = new double[mfdConstraints.size()][numRuptures];
			double[][] fractSectionInsideMFD_Regions = new double[mfdConstraints.size()][getNumSections()];
			int[] numPtsInSection = new int[getNumSections()];
			double gridSpacing=1; // km; this will be faster if this is increased, or if we used the section trace rather than the whole surface
			boolean aseisReducesArea = true;
			// first fill in fraction of section in each region (do each only once)
			for(int s=0;s<getNumSections(); s++) {
				StirlingGriddedSurface surf = new StirlingGriddedSurface(getFaultSectionData(s).getSimpleFaultData(aseisReducesArea), gridSpacing);
				numPtsInSection[s] = surf.getNumCols()*surf.getNumRows();
				for(int i=0;i<mfdConstraints.size(); i++) {
					Region region = mfdConstraints.get(i).getRegion();
					fractSectionInsideMFD_Regions[i][s] = RegionUtils.getFractionInside(region, surf.getEvenlyDiscritizedListOfLocsOnSurface());
				}
			}
			// now fill in fraction of rupture in each region
			for (int i=0; i < mfdConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				IncrementalMagFreqDist targetMagFreqDist=mfdConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = getMagForRup(rup);
					List<Integer> sectionsIndicesForRup = getSectionsIndicesForRup(rup);
					double fractionRupInRegion=0;
					int totNumPts = 0;
					for(Integer s:sectionsIndicesForRup) {
						fractRupsInsideMFD_Regions[i][rup] += fractSectionInsideMFD_Regions[i][s]*numPtsInSection[s];
						totNumPts += numPtsInSection[s];
					}
					fractRupsInsideMFD_Regions[i][rup] /= totNumPts;
				}
			}
		}
	}
}
