package scratch.UCERF3.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;


/**
 * This is a general utility class for obtaining a deformation model (defined as an 
 * ArrayList<FaultSectionPrefData>), plus other derivative information.  This class
 * stores files in order to recreate info more quickly, so these files may need to
 * be deleted if things change.
 * 
 * @author Field
 *
 */
public class DeformationModelFetcher {
	
	protected final static boolean D = true;  // for debugging
	
	public enum DefModName {
		UCERF2_NCAL,
		UCERF2_BAYAREA,
		UCERF2_ALL;
	}
	
	String fileNamePrefix;
	File precomputedDataDir;
	
	ArrayList<FaultSectionPrefData> faultSubSectPrefDataList;
	
	/** Set the UCERF2 deformation model ID
	 * D2.1 = 82
	 * D2.2 = 83
	 * D2.3 = 84
	 * D2.4 = 85
	 * D2.5 = 86
	 * D2.6 = 87
	 */
	static int ucerf2_DefModelId = 82;
	static boolean alphabetize = true;
	
	
	/**
	 * Constructor
	 * 
	 * @param name - then name of the desire deformation model (from the DefModName enum here).
	 * @param precomputedDataDir - the dir where pre-computed data can be found (for faster instantiation)
	 */
	public DeformationModelFetcher(DefModName name, File precomputedDataDir) {
		this.precomputedDataDir = precomputedDataDir;
		if(name == DefModName.UCERF2_NCAL) {
			faultSubSectPrefDataList = createNorthCal_UCERF2_SubSections(false, 0.5);
			fileNamePrefix = "nCal_0_82_500";	// now hard coded as no NaN slip rates, defModID=82, & section length = 0.5 times seimso thickness
		}
		else if(name == DefModName.UCERF2_ALL) {
			faultSubSectPrefDataList = createAll_UCERF2_SubSections(false, 0.5);
			fileNamePrefix = "all_0_82_500";			
		}
		else {
			faultSubSectPrefDataList = this.createBayAreaSubSections(0.5);
			fileNamePrefix = "bayArea_0_82_500_";						
		}
	}
	
	public ArrayList<FaultSectionPrefData> getSubSectionList() {
		return faultSubSectPrefDataList;
	}
	
	
	/**
	 * This gets creates UCERF2 subsections for the entire region.
	 * @param includeSectionsWithNaN_slipRates
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 * 
	 */
	private ArrayList<FaultSectionPrefData> createAll_UCERF2_SubSections(boolean includeSectionsWithNaN_slipRates, double maxSubSectionLength) {

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = this.getAll_UCERF2Sections(includeSectionsWithNaN_slipRates);

		// make subsection data
		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		for(int i=0; i<allFaultSectionPrefData.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)allFaultSectionPrefData.get(i);
			double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectionIndex);
			subSectionIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}		
		
		return subSectionPrefDataList;
	}

	
	
	/**
	 * This gets all UCERF2 subsection data in the N. Cal RELM region.
	 * Note that this has to use a modified version of CaliforniaRegions.RELM_NOCAL() in 
	 * order to not include the Parkfield section (one that uses BorderType.GREAT_CIRCLE 
	 * rather than the default BorderType.MERCATOR_LINEAR).
	 * 
	 * @param includeSectionsWithNaN_slipRates
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 */
	private ArrayList<FaultSectionPrefData>  createNorthCal_UCERF2_SubSections(boolean includeSectionsWithNaN_slipRates, double maxSubSectionLength) {
		
		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = getAll_UCERF2Sections(includeSectionsWithNaN_slipRates);

		// remove those that don't have a trace in in the N Cal RELM region
		Region relm_nocal_reg = new CaliforniaRegions.RELM_NOCAL();
		Region mod_relm_nocal_reg = new Region(relm_nocal_reg.getBorder(), BorderType.GREAT_CIRCLE); // needed to exclude Parkfield
		ArrayList<FaultSectionPrefData> nCalFaultSectionPrefData = new ArrayList<FaultSectionPrefData>();
		for(FaultSectionPrefData sectData:allFaultSectionPrefData) {
			FaultTrace trace = sectData.getFaultTrace();
			Location endLoc1 = trace.get(0);
			Location endLoc2 = trace.get(trace.size()-1);
			if(mod_relm_nocal_reg.contains(endLoc1) || mod_relm_nocal_reg.contains(endLoc2))
				nCalFaultSectionPrefData.add(sectData);
		}

		  // write sections IDs and names
		if (D) {
			System.out.println("Fault Sections in the N Cal RELM region");
			  for(int i=0; i< nCalFaultSectionPrefData.size();i++)
					System.out.println("\t"+nCalFaultSectionPrefData.get(i).getSectionId()+"\t"+nCalFaultSectionPrefData.get(i).getName());			
		}

		// make subsection data
		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		for(int i=0; i<nCalFaultSectionPrefData.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)nCalFaultSectionPrefData.get(i);
			double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectionIndex);
			subSectionIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}		
		
		return subSectionPrefDataList;
	}
	
	
	/**
	 * This gets all section data from the UCERF2 deformation model, where the list is alphabetized,
	 * and the creeping section is removed because aseismicity is not set correctly
	 */
	private ArrayList<FaultSectionPrefData> getAll_UCERF2Sections(boolean includeSectionsWithNaN_slipRates) {

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(ucerf2_DefModelId);

		//Alphabetize:
		if(alphabetize)
			Collections.sort(allFaultSectionPrefData, new NamedComparator());
	  
		// remove those with no slip rate if appropriate
		if(!includeSectionsWithNaN_slipRates) {
			if (D)System.out.println("Removing the following due to NaN slip rate:");
			for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
					if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					allFaultSectionPrefData.remove(i);
				}	 
		}
		
		// REMOVE CREEPING SECTION for now (aseismicity not incorporated correctly)
		if (D)System.out.println("Removing SAF Creeping Section.");
		for(int i=0; i< allFaultSectionPrefData.size();i++) {
			if (allFaultSectionPrefData.get(i).getSectionId() == 57)
				allFaultSectionPrefData.remove(i);
		}
		
		return allFaultSectionPrefData;
	}
	
	
	/**
	 * This gets all section data & creates sub-sections for the SF Bay Area
	 * 
	 * @param maxSubSectionLength - in units of seismogenic thickness
	 */
	private ArrayList<FaultSectionPrefData> createBayAreaSubSections(double maxSubSectionLength) {

		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();	
		
		ArrayList<Integer> faultSectionIds = new ArrayList<Integer>();
		// Bay Area Faults
		faultSectionIds.add(26); //  San Andreas (Offshore)
		faultSectionIds.add(27); //  San Andreas (North Coast)
		faultSectionIds.add(67); //  San Andreas (Peninsula)
		faultSectionIds.add(56); //  San Andreas (Santa Cruz Mtn) 
		faultSectionIds.add(25); //  Rodgers Creek
		faultSectionIds.add(68); //  Hayward (No)
		faultSectionIds.add(69); //  Hayward (So)
		faultSectionIds.add(4);  //  Calaveras (No)
		faultSectionIds.add(5);  //  Calaveras (Central)
		faultSectionIds.add(55); //  Calaveras (So)
		faultSectionIds.add(71); //  Green Valley (No)
		faultSectionIds.add(1);  //  Green Valley (So)
		faultSectionIds.add(3);  //  Concord
		faultSectionIds.add(12); //  San Gregorio (No)
		faultSectionIds.add(29); //  San Gregorio (So)
		faultSectionIds.add(6);  //  Greenville (No)
		faultSectionIds.add(7);  //  Greenville (So)
		faultSectionIds.add(2);  //  Mount Diablo Thrust
		
		
		ArrayList<FaultSectionPrefData> subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		for (int i = 0; i < faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = deformationModelPrefDB.getFaultSectionPrefData(ucerf2_DefModelId, faultSectionIds.get(i));
			double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectIndex);
			subSectIndex += subSectData.size();
			subSectionPrefDataList.addAll(subSectData);
		}
		
		return subSectionPrefDataList;
	}
	
	
	/**
	 * This computes the distances between subsection if it hasn't already been done.
	 * (otherwise the values are read from a file).
	 */
	public double[][] getSubSectionDistanceMatrix() {

		int numSubSections = faultSubSectPrefDataList.size();
		double[][] subSectionDistances = new double[numSubSections][numSubSections];

		// construct filename
		String name = fileNamePrefix+"_Distances";
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+name;
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
						subSectionDistances[j][i] = subSectionDistances[i][j];
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
				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(faultSubSectPrefDataList.get(a).getSimpleFaultData(false), 1.0, 1.0);

				for(int b=a+1;b<numSubSections;b++) { // a+1 because array is initialized to zero
//					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
					StirlingGriddedSurface surf2 = new StirlingGriddedSurface(faultSubSectPrefDataList.get(b).getSimpleFaultData(false), 1.0, 1.0);
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
		
		return subSectionDistances;
	}


	/**
	 * This creates (or reads) a matrix giving the azimuth between the midpoint of each subSection.
	 * @return
	 */
	  public double[][] getSubSectionAzimuthMatrix() {

		  int numSubSections = faultSubSectPrefDataList.size();
		  double[][] subSectionAzimuths = new double[numSubSections][numSubSections];

		  // construct filename
		  String name = fileNamePrefix+"_Azimuths";
		  String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+name;
		  File file = new File (fullpathname);
		  
		  // Read data if already computed and saved
		  if(file.exists()) {
			  if(D) System.out.println("Reading existing file: "+ name);
			    try {
			        // Wrap the FileInputStream with a DataInputStream
			        FileInputStream file_input = new FileInputStream (file);
			        DataInputStream data_in    = new DataInputStream (file_input );
					  for(int i=0; i<numSubSections;i++)
						  for(int j=0; j<numSubSections;j++) {
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
				  StirlingGriddedSurface surf1 = new StirlingGriddedSurface(faultSubSectPrefDataList.get(a).getSimpleFaultData(false), 1.0);
				  Location loc1 = surf1.getLocation(surf1.getNumRows()/2, surf1.getNumCols()/2);
				  for(int b=0;b<numSubSections;b++) {
					  StirlingGriddedSurface surf2 = new StirlingGriddedSurface(faultSubSectPrefDataList.get(b).getSimpleFaultData(false), 1.0);
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
					  for(int j=0; j<numSubSections;j++)
						  data_out.writeDouble(subSectionAzimuths[i][j]);
				  // Close file
				  file_output.close ();
			  }
			  catch (IOException e) {
				  System.out.println ("IO exception = " + e );
			  }
		  }
		  
		  return subSectionAzimuths;
	  }
}
