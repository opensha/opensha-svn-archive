package scratch.kevin.ucerf3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.SectionCluster;
import scratch.UCERF3.inversion.SectionClusterList;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;

public class StandaloneSubSectRupGen {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		// this is the input fault section data file
		File fsdFile = new File("dev/scratch/UCERF3/data/FaultModels/FM3_1.xml");
		// directory to write output files
		File outputDir = new File("/tmp");
		// maximum sub section length (in units of DDW)
		double maxSubSectionLength = 0.5;
		// max distance for linking multi fault ruptures, km
		double maxDistance = 5d;
		
		// load in the fault section data ("parent sections")
		List<FaultSectionPrefData> fsd = FaultModels.loadStoredFaultSections(fsdFile);
		
		// this list will store our subsections
		List<FaultSectionPrefData> subSections = Lists.newArrayList();
		
		// build the subsections
		int sectIndex = 0;
		for (FaultSectionPrefData parentSect : fsd) {
			double ddw = parentSect.getOrigDownDipWidth();
			double maxSectLength = ddw*maxSubSectionLength;
			// the "2" here sets a minimum number of sub sections
			List<FaultSectionPrefData> newSubSects = parentSect.getSubSectionsList(maxSectLength, sectIndex, 2);
			subSections.addAll(newSubSects);
			sectIndex += newSubSects.size();
		}
		
		// write subsection data to file
		File subSectDataFile = new File(outputDir, "sub_sections.xml");
		Document doc = XMLUtils.createDocumentWithRoot();
		SimpleFaultSystemRupSet.fsDataToXML(doc.getRootElement(), subSections, FaultModels.XML_ELEMENT_NAME, null, null);
		XMLUtils.writeDocumentToFile(subSectDataFile, doc);
		
		// instantiate our laugh test filter
		LaughTestFilter laughTest = LaughTestFilter.getDefault();
		// you will have to disable our coulomb filter as it uses a data file specific to our subsections
		laughTest.setCoulombFilter(null);
		
		// calculate distances between each subsection
		Map<IDPairing, Double> subSectionDistances = DeformationModelFetcher.calculateDistances(maxDistance, subSections);
		Map<IDPairing, Double> reversed = Maps.newHashMap();
		// now add the reverse distance
		for (IDPairing pair : subSectionDistances.keySet()) {
			IDPairing reverse = pair.getReversed();
			reversed.put(reverse, subSectionDistances.get(pair));
		}
		subSectionDistances.putAll(reversed);
		Map<IDPairing, Double> subSectionAzimuths = DeformationModelFetcher.getSubSectionAzimuthMap(
				subSectionDistances.keySet(), subSections);
		
		// this separates the sub sections into clusters which are all within maxDist of each other and builds ruptures
		// fault model and deformation model here are needed by InversionFaultSystemRuptSet later, just to create a rup set
		// zip file
		SectionClusterList clusters = new SectionClusterList(
				FaultModels.FM3_1, DeformationModels.GEOLOGIC, laughTest, subSections, subSectionDistances, subSectionAzimuths);
		
		List<List<Integer>> ruptures = Lists.newArrayList();
		for (SectionCluster cluster : clusters) {
			ruptures.addAll(cluster.getSectionIndicesForRuptures());
		}
		
		System.out.println("Created "+ruptures.size()+" ruptures");
		
		// write rupture/subsection associations to file
		// format: rupID	sectID1,sectID2,sectID3,...,sectIDN
		File rupFile = new File(outputDir, "ruptures.txt");
		FileWriter fw = new FileWriter(rupFile);
		Joiner j = Joiner.on(",");
		for (int i=0; i<ruptures.size(); i++) {
			fw.write(i+"\t"+j.join(ruptures.get(i))+"\n");
		}
		fw.close();
		
		// build actual rupture set for magnitudes and such
		LogicTreeBranch branch = LogicTreeBranch.fromValues(FaultModels.FM3_1, DeformationModels.GEOLOGIC,
				ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED);
		InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(branch, clusters, subSections);
		
		File zipFile = new File(outputDir, "rupSet.zip");
		new SimpleFaultSystemRupSet(rupSet).toZipFile(zipFile);
	}

}
