package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipException;

import org.dom4j.DocumentException;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;

public class RupSetDiffMaker {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException, DocumentException {
		File rupSet1File = new File("/tmp/GarlockPintoMtnFix_RupSet.zip");
//		File rupSet2File = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/" +
//				"scratch/InversionSolutions/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7" +
//				"_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
		boolean oldRups = true;
		
		File diffFile;
		if (oldRups)
			diffFile = new File("/tmp/garlockOldRups.zip");
		else
			diffFile = new File("/tmp/garlockNewRups.zip");
		
		FaultSystemRupSet rupSet1 = SimpleFaultSystemRupSet.fromZipFile(rupSet1File);
//		FaultSystemRupSet rupSet2 = SimpleFaultSystemRupSet.fromZipFile(rupSet2File);
		FaultSystemRupSet rupSet2 = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1);
		if (oldRups) {
			FaultSystemRupSet tmp = rupSet1;
			rupSet1 = rupSet2;
			rupSet2 = tmp;
		}
		
		HashSet<Rup> rups2 = new HashSet<Rup>();
		for (int r=0; r<rupSet2.getNumRuptures(); r++) {
			rups2.add(new Rup(rupSet2.getSectionsIndicesForRup(r)));
		}
		
		List<Integer> newRups = Lists.newArrayList();
		
		for (int r=0; r<rupSet1.getNumRuptures(); r++) {
			Rup rup = new Rup(rupSet1.getSectionsIndicesForRup(r));
			if (!rups2.contains(rup))
				newRups.add(r);
		}
		
		System.out.println("Found "+newRups.size()+" new rups ("
				+rupSet1.getNumRuptures()+" => "+rupSet2.getNumRuptures()+")");
		
		// verify
		for (Integer r : newRups) {
			HashSet<Integer> rup = new HashSet<Integer>(rupSet1.getSectionsIndicesForRup(r));
			rup2loop:
			for (int r2=0; r2<rupSet2.getNumRuptures(); r2++) {
				List<Integer> rup2 = rupSet2.getSectionsIndicesForRup(r2);
				if (rup2.size() == rup.size()) {
					for (Integer s : rup2)
						if (!rup.contains(s))
							continue rup2loop;
					System.out.println("Equals ? "+new Rup(rupSet1.getSectionsIndicesForRup(r))
								.equals(new Rup(rupSet2.getSectionsIndicesForRup(r2))));
					throw new IllegalStateException("Found a match, wtf??");
				}
			}
		}
		
		double[] mags = new double[newRups.size()];
		double[] rupAveSlips = new double[newRups.size()];
		double[] rakes = new double[newRups.size()];
		double[] rupAreas = new double[newRups.size()];
		List<List<Integer>> sectionForRups = Lists.newArrayList();
		
		for (int i=0; i<newRups.size(); i++) {
			int r = newRups.get(i);
			mags[i] = rupSet1.getMagForRup(r);
			rupAveSlips[i] = rupSet1.getAveSlipForRup(r);
			rakes[i] = rupSet1.getAveRakeForRup(r);
			rupAreas[i] = rupSet1.getAreaForRup(r);
			sectionForRups.add(rupSet1.getSectionsIndicesForRup(r));
		}
		
		SimpleFaultSystemRupSet diffSet = new SimpleFaultSystemRupSet(
				rupSet1.getFaultSectionDataList(), mags, rupAveSlips, null,
				rupSet1.getSlipAlongRuptureModel(), rupSet1.getSlipRateForAllSections(),
				rupSet1.getSlipRateStdDevForAllSections(), rakes, rupAreas,
				rupSet1.getAreaForAllSections(), sectionForRups, rupSet1.getInfoString(),
				rupSet1.getCloseSectionsListList(), rupSet1.getFaultModel(),
				rupSet1.getDeformationModel());
		
		diffSet.toZipFile(diffFile);
	}
	
	private static class Rup {
		private List<Integer> sects;
		public Rup(List<Integer> sects) {
			this.sects = Lists.newArrayList(sects);
			Collections.sort(this.sects);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sects == null) ? 0 : sects.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Rup other = (Rup) obj;
			if (sects == null) {
				if (other.sects != null)
					return false;
			} else if (!sects.equals(other.sects))
				return false;
			return true;
		}
	}

}
