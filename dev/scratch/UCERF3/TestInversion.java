package scratch.UCERF3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.opensha.commons.data.NamedObjectComparator;
import org.opensha.commons.exceptions.FaultException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;

public class TestInversion {

	protected final static boolean D = true;  // for debugging

	ArrayList<FaultSectionPrefData> subSectionPrefDataList;
	int numSubSections;
	double subSectionDistances[][],subSectionAzimuths[][];
	double maxSubSectionLength;
	
	RupsInFaultSystemInversion rupsInFaultSysInv;

	int deformationModelId;
	
	public TestInversion() {
		double subSectionLength = 0.5;  // in units of seimogenic thickness
		double maxJumpDist = 5.0;
		double maxAzimuthChange = 45;
		double maxTotAzimuthChange = 90;
		int minNumSectInRup = 2;
		/** Set the deformation model
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		deformationModelId = 82;
		createSubSections(false, subSectionLength);
		calcSubSectionDistances();
		calcSubSectionAzimuths();
		rupsInFaultSysInv = new RupsInFaultSystemInversion(subSectionPrefDataList,
				subSectionDistances, subSectionAzimuths, maxJumpDist, 
				maxAzimuthChange, maxTotAzimuthChange, minNumSectInRup);
		
//		rupsInFaultSysInv.writeCloseSubSections();
	}
	
	public RupsInFaultSystemInversion getRupsInFaultSystemInversion() {
		return rupsInFaultSysInv;
	}


	/**
	 * This gets the section data, creates subsections, and fills in arrays giving the 
	 * name of section endpoints, angles between section endpoints, and distances between
	 * section endpoints (these latter arrays are for sections, not subsections)
	 * @param includeSectionsWithNaN_slipRates
	 */
	private void createSubSections(boolean includeSectionsWithNaN_slipRates, double maxSubSectionLength) {

		this.maxSubSectionLength=maxSubSectionLength;
		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);

		//Alphabetize:
		Collections.sort(allFaultSectionPrefData, new NamedObjectComparator());

		/*		  
		  // write sections IDs and names
		  for(int i=0; i< this.allFaultSectionPrefData.size();i++)
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
		 */

		// remove those with no slip rate if appropriate
		if(!includeSectionsWithNaN_slipRates) {
			if (D)System.out.println("Removing the following due to NaN slip rate:");
			for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
					if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					allFaultSectionPrefData.remove(i);
				}	 
		}

		/*	
		  // find and print max Down-dip width
		  double maxDDW=0;
		  int index=-1;
		  for(int i=0; i<allFaultSectionPrefData.size(); i++) {
			  double ddw = allFaultSectionPrefData.get(i).getDownDipWidth();
			  if(ddw>maxDDW) {
				  maxDDW = ddw;
				  index=i;
			  }
		  }
		  System.out.println("Max Down-Dip Width = "+maxDDW+" for "+allFaultSectionPrefData.get(index).getSectionName());
		 	*/

		// make subsection data
		subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		int numSections = allFaultSectionPrefData.size();
		for(int i=0; i<numSections; ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)allFaultSectionPrefData.get(i);
			double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectionIndex);
			subSectionIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}		
		
		numSubSections = subSectionPrefDataList.size();
		if(D) System.out.println("numSubsections = "+numSubSections);
	}
	
	

	/**
	 * This computes the distances between subsection if it hasn't already been done.
	 * Otherwise the values are read from a file.
	 */
	private void calcSubSectionDistances() {

		subSectionDistances = new double[numSubSections][numSubSections];

		// construct filename
		String name = deformationModelId+"_"+(int)(maxSubSectionLength*1000)+"_Distances";
//		String fullpathname = "/Users/pagem/eclipse/workspace/OpenSHA/dev/scratch/pagem/rupsInFaultSystem/PreComputedSubSectionDistances/"+name;
		  String fullpathname = "/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/"+name;
		File file = new File (fullpathname);

		// Read data if already computed and saved
		if(file.exists()) {
			if(D) System.out.println("Reading existing file: "+ name);
			try {
				// Wrap the FileInputStream with a DataInputStream
				FileInputStream file_input = new FileInputStream (file);
				DataInputStream data_in    = new DataInputStream (file_input );
				for(int i=0; i<numSubSections;i++)
					for(int j=i; j<numSubSections;j++) {
						subSectionDistances[i][j] = data_in.readDouble();
					}
				data_in.close ();
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
			}

		}
		else {// Calculate new distance matrix & save to a file
			System.out.println("Calculating data and will save to file: "+name);

			int progress = 0, progressInterval=10;  // for progress report
			System.out.print("Dist Calc % Done:");
			for(int a=0;a<numSubSections;a++) {
				if (100*a/numSubSections > progress) {
					System.out.print("\t"+progress);
					progress += progressInterval;
				}
//				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 2.0);
				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 1.0, 1.0);

				for(int b=a+1;b<numSubSections;b++) {
//					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 1.0, 1.0);
					double minDist = surf1.getMinDistance(surf2);
					subSectionDistances[a][b] = minDist;
					subSectionDistances[b][a] = minDist;
				}
			}
			System.out.print("\n");
			// Now save to a binary file
			try {
				// Create an output stream to the file.
				FileOutputStream file_output = new FileOutputStream (file);
				// Wrap the FileOutputStream with a DataOutputStream
				DataOutputStream data_out = new DataOutputStream (file_output);
				for(int i=0; i<numSubSections;i++)
					for(int j=i; j<numSubSections;j++)
						data_out.writeDouble(subSectionDistances[i][j]);
				// Close file
				file_output.close ();
			}
			catch (IOException e) {
				System.out.println ("IO exception = " + e );
			}
		}
	}



	  private void calcSubSectionAzimuths() {

		  subSectionAzimuths = new double[numSubSections][numSubSections];

		  // construct filename
		  String name = deformationModelId+"_"+(int)(maxSubSectionLength*1000)+"_Azimuths";
		  String fullpathname = "/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/"+name;
		  File file = new File (fullpathname);
		  
		  // Read data if already computed and saved
		  if(file.exists()) {
			  if(D) System.out.println("Reading existing file: "+ name);
			    try {
			        // Wrap the FileInputStream with a DataInputStream
			        FileInputStream file_input = new FileInputStream (file);
			        DataInputStream data_in    = new DataInputStream (file_input );
					  for(int i=0; i<numSubSections;i++)
						  for(int j=i; j<numSubSections;j++) {
							  subSectionAzimuths[i][j] = data_in.readDouble();
						  }
			        data_in.close ();
			      } catch  (IOException e) {
			         System.out.println ( "IO Exception =: " + e );
			      }

		  }
		  else {// Calculate new distance matrix & save to a file
			  System.out.println("Calculating azimuth data and will save to file: "+name);

			  int progress = 0, progressInterval=10;  // for progress report
			  System.out.print("Azimuth Calc % Done:");
			  
			  for(int a=0;a<numSubSections;a++) {
				  if (100*a/numSubSections > progress) {
					  System.out.print("\t"+progress);
					  progress += progressInterval;
				  }
				  StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 1.0);
				  Location loc1 = surf1.getLocation(surf1.getNumRows()/2, surf1.getNumCols()/2);
				  for(int b=0;b<numSubSections;b++) {
					  StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 1.0);
					  Location loc2 = surf2.getLocation((int)(surf2.getNumRows()/2), (int)(surf2.getNumCols()/2));
					  subSectionAzimuths[a][b] = LocationUtils.azimuth(loc1, loc2);
				  }
			  }
			  System.out.print("\n");
			  // Now save to a binary file
			  try {
				  // Create an output stream to the file.
				  FileOutputStream file_output = new FileOutputStream (file);
				  // Wrap the FileOutputStream with a DataOutputStream
				  DataOutputStream data_out = new DataOutputStream (file_output);
				  for(int i=0; i<numSubSections;i++)
					  for(int j=i; j<numSubSections;j++)
						  data_out.writeDouble(subSectionAzimuths[i][j]);
				  // Close file
				  file_output.close ();
			  }
			  catch (IOException e) {
				  System.out.println ("IO exception = " + e );
			  }
		  }
	  }
	  


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestInversion test = new TestInversion();
	}

}
