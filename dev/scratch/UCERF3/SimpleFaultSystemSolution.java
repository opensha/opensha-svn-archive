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
}
