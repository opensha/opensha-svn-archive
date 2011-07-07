package scratch.UCERF3;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;

public class SimpleFaultSystemSolution extends FaultSystemSolution implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "SimpleFaultSystemSolution";
	
	private FaultSystemRupSet rupSet;
	protected double[] rupRateSolution;
	
	public SimpleFaultSystemSolution(FaultSystemSolution solution) {
		this(solution, solution.getRateForAllRups());
	}
	
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
	
	public void toFile(File file) throws IOException {
		XMLUtils.writeObjectToXMLAsRoot(this, file);
	}
	
	public static SimpleFaultSystemSolution fromFile(File file) throws MalformedURLException, DocumentException {
		Document doc = XMLUtils.loadDocument(file);
		Element solutionEl = doc.getRootElement().element(XML_METADATA_NAME);
		return fromXMLMetadata(solutionEl);
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

}
