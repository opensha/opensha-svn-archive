package scratchJavaDevelopers.ned.SSAF_Inversion;

import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.calc.RelativeLocation;

public class CreateRupturesFromSections {
	
	ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	double sectionDistances[][];
	double sectionAngleDiffs[][];
	String endPointNames[];
	int num_sections, numSectEndPts;

	
	CreateRupturesFromSections() {
		getAllSections();
	}
	
	
	
	private void getAllSections() {
		/** Set the deformation model
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		int deformationModelId = 82;
		
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);
//		System.out.println("size = "+allFaultSectionPrefData.size());
		
		num_sections = allFaultSectionPrefData.size();
		
		for(int s=0;s<num_sections;s++) System.out.println(allFaultSectionPrefData.get(s).getFaultTrace().getStrikeDirection());

		
		numSectEndPts = 2*num_sections;
		sectionDistances = new double[numSectEndPts][numSectEndPts];
		sectionAngleDiffs = new double[num_sections][num_sections];
		endPointNames = new String[numSectEndPts];
		
		// loop over first fault section (A)
		for(int a=0;a<allFaultSectionPrefData.size();a++) {
			FaultSectionPrefData dataA = allFaultSectionPrefData.get(a);
			int indexA_firstPoint = 2*a;
			int indexA_lastPoint = indexA_firstPoint+1;
			endPointNames[indexA_firstPoint] = dataA.getSectionName() +" -- first";
			endPointNames[indexA_lastPoint] = dataA.getSectionName() +" -- last";
//			System.out.println(endPointNames[indexA_firstPoint]+"\t"+endPointNames[indexA_lastPoint]);
			// loop over second fault section (B)
			for(int b=0;b<allFaultSectionPrefData.size();b++) {
				FaultSectionPrefData dataB = allFaultSectionPrefData.get(b);
				int indexB_firstPoint = 2*b;
				int indexB_lastPoint = indexB_firstPoint+1;
				Location locA_1st = dataA.getFaultTrace().getLocationAt(0);
				Location locA_2nd = dataA.getFaultTrace().getLocationAt(dataA.getFaultTrace().size()-1);
				Location locB_1st = dataB.getFaultTrace().getLocationAt(0);
				Location locB_2nd = dataB.getFaultTrace().getLocationAt(dataB.getFaultTrace().size()-1);
				sectionDistances[indexA_firstPoint][indexB_firstPoint] = RelativeLocation.getApproxHorzDistance(locA_1st, locB_1st);
				sectionDistances[indexA_firstPoint][indexB_lastPoint] = RelativeLocation.getApproxHorzDistance(locA_1st, locB_2nd);
				sectionDistances[indexA_lastPoint][indexB_firstPoint] = RelativeLocation.getApproxHorzDistance(locA_2nd, locB_1st);
				sectionDistances[indexA_lastPoint][indexB_lastPoint] = RelativeLocation.getApproxHorzDistance(locA_2nd, locB_2nd);
				
				sectionAngleDiffs[a][b] = dataA.getFaultTrace().getStrikeDirectionDifference(dataB.getFaultTrace());
			
			}
		}
	}
	
	public void writeSectionDistances() {
		
		try{
			FileWriter fw = new FileWriter("/Users/field/workspace/OpenSHA/scratchJavaDevelopers/ned/SSAF_Inversion/sectionDistances.txt");
			
			int num = numSectEndPts;
			String outputString = new String();
			outputString += "\t";
			for(int i=0;i<num;i++) outputString += endPointNames[i]+"\t";
			outputString += "\n";
			fw.write(outputString);
			/* */
			for(int i=0;i<num;i++) {
//				System.out.print(i);
				outputString = new String();
				outputString += endPointNames[i]+"\t";
				for(int j=0;j<num;j++) {
					String distString = new Double(Math.round(sectionDistances[i][j])).toString()+"\t";
//					if(i==100) System.out.println(distString);
					outputString += distString;
//					if(sectionDistances[i][j] <= 5 && i != j) System.out.println(endPointNames[i]+"\t"+endPointNames[j]+"\t"+
//							Math.round(sectionDistances[i][j])+"\t"+Math.round(sectionAngleDiffs[i/2][j/2]));
				}
				outputString += "\n";
				fw.write(outputString);
			}

			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
//		System.out.print(output);

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreateRupturesFromSections createRups = new CreateRupturesFromSections();
		createRups.writeSectionDistances();
		
		

	}

}
