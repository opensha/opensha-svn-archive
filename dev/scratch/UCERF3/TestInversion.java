package scratch.UCERF3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import scratch.UCERF3.utils.FaultSectionDataWriter;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.data.NamedObjectComparator;
import org.opensha.commons.exceptions.FaultException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import org.apache.commons.math.linear.OpenMapRealMatrix; // for sparse matrices


public class TestInversion {

	protected final static boolean D = true;  // for debugging

	ArrayList<FaultSectionPrefData> subSectionPrefDataList;
	int numSubSections; int numRuptures;
	double subSectionDistances[][],subSectionAzimuths[][];
	double maxSubSectionLength;
	boolean includeSectionsWithNaN_slipRates;
	int minNumSectInRup;
	double[] rupMeanMag;
	double[] segSlipRate;
	double moRateReduction;
	MagAreaRelationship magAreaRel;
	
	String subsectsNameForFile;
	
	RupsInFaultSystemInversion rupsInFaultSysInv;

	int deformationModelId;
	
	private File precomputedDataDir;
	
	public TestInversion() {
		this(new File("dev/scratch/UCERF3/preComputedData/"));
	}
	
	public TestInversion(File precomputedDataDir) {
		this.precomputedDataDir = precomputedDataDir;
		
		maxSubSectionLength = 0.5;  // in units of seismogenic thickness
		double maxJumpDist = 5.0;
		double maxAzimuthChange = 45;
		double maxTotAzimuthChange = 90;
		double maxRakeDiff = 90;
		minNumSectInRup = 2;
		moRateReduction = 0.1;
		includeSectionsWithNaN_slipRates = false;
		magAreaRel = new Ellsworth_B_WG02_MagAreaRel();
		
		/** Set the deformation model
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		deformationModelId = 82;
		
		if(D) System.out.println("Making subsections...");
//		createAllSubSections();
		createBayAreaSubSections(); 
		
		calcSubSectionDistances();
		
		calcSubSectionAzimuths();
		
		rupsInFaultSysInv = new RupsInFaultSystemInversion(subSectionPrefDataList,
				subSectionDistances, subSectionAzimuths, maxJumpDist, 
				maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, minNumSectInRup, magAreaRel);
		
//		rupsInFaultSysInv.writeCloseSubSections(precomputedDataDir.getAbsolutePath()+File.separator+"closeSubSections.txt");
		
//		doInversion(rupsInFaultSysInv);  
		
		
		
		
	}
	
	public RupsInFaultSystemInversion getRupsInFaultSystemInversion() {
		return rupsInFaultSysInv;
	}


	/**
	 * This gets all section data & creates subsections
	 * @param includeSectionsWithNaN_slipRates
	 */
	private void createAllSubSections() {

		if(includeSectionsWithNaN_slipRates)
			subsectsNameForFile = "all_1_";
		else
			subsectsNameForFile = "all_0_";

		// fetch the sections
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		ArrayList<FaultSectionPrefData> allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);

		//Alphabetize:
		Collections.sort(allFaultSectionPrefData, new NamedObjectComparator());

		/*	*/	  
		  // write sections IDs and names
		  for(int i=0; i< allFaultSectionPrefData.size();i++)
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
		 

		// remove those with no slip rate if appropriate
		if(!includeSectionsWithNaN_slipRates) {
			if (D)System.out.println("Removing the following due to NaN slip rate:");
			for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
					if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					allFaultSectionPrefData.remove(i);
				}	 
		}

		// make subsection data
		subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectionIndex=0;
		for(int i=0; i<numSubSections; ++i) {
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
	 * This gets all section data & creates subsections
	 * @param includeSectionsWithNaN_slipRates
	 */
	private void createBayAreaSubSections() {

		if(includeSectionsWithNaN_slipRates)
			subsectsNameForFile = "bayArea_1_";
		else
			subsectsNameForFile = "bayArea_0_";

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
		
/*		// Other important faults to add for scalability testing
		faultSectionIds.add(287);	//San Andreas (Big Bend)
		faultSectionIds.add(300);	//San Andreas (Carrizo) rev
		faultSectionIds.add(285);	//San Andreas (Cholame) rev
		faultSectionIds.add(295);	//San Andreas (Coachella) rev
		faultSectionIds.add(57);	//San Andreas (Creeping Segment)
		faultSectionIds.add(286);	//San Andreas (Mojave N)
		faultSectionIds.add(301);	//San Andreas (Mojave S)
		faultSectionIds.add(32);	//San Andreas (Parkfield)
		faultSectionIds.add(282);	//San Andreas (San Bernardino N)
		faultSectionIds.add(283);	//San Andreas (San Bernardino S)
		faultSectionIds.add(284);	//San Andreas (San Gorgonio Pass-Garnet HIll)
		faultSectionIds.add(56);	//San Andreas (Santa Cruz Mtn)
		faultSectionIds.add(341);	//Garlock (Central)
		faultSectionIds.add(48);	//Garlock (East)
		faultSectionIds.add(49);	//Garlock (West)
		faultSectionIds.add(293);	//San Jacinto (Anza) rev
		faultSectionIds.add(291);	//San Jacinto (Anza, stepover)
		faultSectionIds.add(99);	//San Jacinto (Borrego)
		faultSectionIds.add(292);	//San Jacinto (Clark) rev
		faultSectionIds.add(101);	//San Jacinto (Coyote Creek)
		faultSectionIds.add(119);	//San Jacinto (San Bernardino)
		faultSectionIds.add(289);	//San Jacinto (San Jacinto Valley) rev
		faultSectionIds.add(290);	//San Jacinto (San Jacinto Valley, stepover)
		faultSectionIds.add(28);	//San Jacinto (Superstition Mtn)
*/		
		
		subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		int subSectIndex = 0;
		for (int i = 0; i < faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = deformationModelPrefDB
					.getFaultSectionPrefData(deformationModelId, faultSectionIds.get(i));
			double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength, subSectIndex);
			subSectIndex += subSectData.size();
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
		String name = subsectsNameForFile+deformationModelId+"_"+(int)(maxSubSectionLength*1000)+"_Distances";
//		String fullpathname = "/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/"+name;
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+name;
//		String fullpathname = "dev/scratch/UCERF3/preComputedData/"+name;
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
				StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 1.0, 1.0);

				for(int b=a+1;b<numSubSections;b++) { // a+1 because array is initialized to zero
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
		  String name = subsectsNameForFile+deformationModelId+"_"+(int)(maxSubSectionLength*1000)+"_Azimuths";
//		  String fullpathname = "/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/"+name;
//		  String fullpathname = "dev/scratch/UCERF3/preComputedData/"+name;
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
					  for(int j=0; j<numSubSections;j++)
						  data_out.writeDouble(subSectionAzimuths[i][j]);
				  // Close file
				  file_output.close ();
			  }
			  catch (IOException e) {
				  System.out.println ("IO exception = " + e );
			  }
		  }
	  }
	  
	  
	  public void writeSectionsToFile(String filePathAndName) {
		  ArrayList<String> metaData = new ArrayList<String>();
		  metaData.add("deformationModelId = "+deformationModelId);
		  metaData.add("includeSectionsWithNaN_slipRates = "+includeSectionsWithNaN_slipRates);
		  metaData.add("maxSubSectionLength = "+maxSubSectionLength+"  (in units of section down-dip width)");
		  FaultSectionDataWriter.writeSectionsToFile(subSectionPrefDataList, 
					metaData, filePathAndName);

	  }
	  


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestInversion test = new TestInversion();
			
//		test.writeSectionsToFile("dev/scratch/UCERF3/exampleSubSectDataFile");

//		RupsInFaultSystemInversion inversion = test.getRupsInFaultSystemInversion();
//		for(int i=0; i<inversion.getNumClusters(); i++)
//			System.out.println("Cluster "+i+" has "+inversion.getCluster(i).getNumRuptures()+" ruptures");
	}


public void doInversion(RupsInFaultSystemInversion rupsInFaultSysInv) {
	
	this.rupsInFaultSysInv = rupsInFaultSysInv;

	numRuptures = rupsInFaultSysInv.getNumRupRuptures();
	System.out.println("\nNumber of sections: " + numSubSections + ". Number of ruptures: " + numRuptures + ".\n");
	ArrayList<SegRateConstraint> segRateConstraints = rupsInFaultSysInv.getPaleoSegRateConstraints();
	
	if(D) System.out.println("Getting list of ruptures ...");
	ArrayList<ArrayList<Integer>> rupList = rupsInFaultSysInv.getRupList();
	
	// Compute segment slip rates, ruptures areas & mean magnitudes of ruptures
	if(D) System.out.println("\nComputing rupture areas and magnitudes ...");
	computeInitialStuff(rupList);
	
	// create A matrix and data vector
	OpenMapRealMatrix A = new OpenMapRealMatrix(numSubSections+segRateConstraints.size(),numRuptures);
	double[] d = new double[numSubSections+segRateConstraints.size()];		
	
	// Make sparse matrix of slip in each rupture & data vector of section slip rates
	int numElements = 0;
	if(D) System.out.println("\nAdding slip per rup to A matrix ...");
	for (int rup=0; rup<numRuptures; rup++) {
		ArrayList<Integer> sectIndicesForRup = rupList.get(rup);
		double[] slips = rupsInFaultSysInv.getSlipOnSectionsForRup(sectIndicesForRup);
		for (int sect=0; sect < slips.length; sect++) {
			A.addToEntry(sect,rup,slips[sect]);
			if(D) numElements++;
		}
	}
	for (int sect=0; sect<numSubSections; sect++) {
		d[sect] = segSlipRate[sect] * (1 - moRateReduction);	
	}
	if(D) System.out.println("Number of nonzero elements in A matrix = "+numElements);
	
	// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
	// TO DO: Add event-rate constraint weight (relative to slip rate constraint)
	numElements = 0;
	if(D) System.out.println("\nAdding event rates to A matrix ...");
	OpenMapRealMatrix A2 = new OpenMapRealMatrix(segRateConstraints.size(),numRuptures);
	for (int i=numSubSections; i<numSubSections+segRateConstraints.size(); i++) {
		SegRateConstraint constraint = segRateConstraints.get(i-numSubSections);
		d[i]=constraint.getMean()/ constraint.getStdDevOfMean();
		double[] row = A.getRow(constraint.getSegIndex());
		for (int rup=0; rup<numRuptures; rup++) {
			if (row[rup]>0) {
				A.setEntry(i,rup,getProbVisible(rupMeanMag[rup])/constraint.getStdDevOfMean());  
				if(D) numElements++;
				
			}
		}
	}
	if(D) System.out.println("Number of new nonzero elements in A matrix = "+numElements);
	

	

	// SOLVE THE INVERSE PROBLEM
	double[] rupRateSolution = new double[numRuptures];
	if(D) System.out.println("\nSolving inverse problem with simulated annealing ... ");
	rupRateSolution = getSimulatedAnnealing_solution(A,d);          

	}




private static double[] getSimulatedAnnealing_solution(OpenMapRealMatrix A,
		double[] d) {

	int nRow = A.getRowDimension();
	int nCol = A.getColumnDimension();

	if(D) System.out.println("nRow = " + nRow);
	if(D) System.out.println("nCol = " + nCol);
	
	double[] x = new double[nCol]; // current model
	double[] xbest = new double[nCol]; // best model seen so far
	double[] xnew = new double[nCol]; // new perturbed model
	double[] initial_state = new double[nCol]; // starting model
	double[] perturb = new double[nCol]; // perturbation to current model
	double[] syn = new double[nRow]; // data synthetics
	double[] misfit = new double[nRow]; // mifit between data and synthetics

	double E, Enew, Ebest, T, P;
	int i, j, iter, index;
	int numiter=5000;
	
	if(D) System.out.println("Total number of iterations = " + numiter);
	
//	Random r = new Random(System.currentTimeMillis());
	
	// Set initial state (random or a priori starting model)
	for (i = 0; i < nCol; i++) {
		initial_state[i] = 0; // Need to Change !!!
	//	initial_state[i] = Math.random() / 100000;
	}
	for (j = 0; j < nCol; j++) {
		x[j] = initial_state[j];
	}
	// x=initial_state.clone();  // not sure why this doesn't work in lieu of above code
	
	// Initial "best" solution & its Energy
	for (j = 0; j < nCol; j++) {
		xbest[j] = x[j];  
	}
	
	E = 0;
	for (i = 0; i < nRow; i++) {
		syn[i] = 0;
		for (j = 0; j < nCol; j++) {
			syn[i] += A.getEntry(i,j) * x[j]; // compute predicted data
		}
		misfit[i] = syn[i] - d[i];  // misfit between synthetics and data
		E += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
	}
	//E = Math.sqrt(E);
	Ebest = E;
	if(D) System.out.println("Starting energy = " + Ebest);
	
	for (iter = 1; iter <= numiter; iter++) {
		
		
		// Simulated annealing "temperature"
		// T = 1 / (double) iter; 
		// T = 1/Math.log( (double) iter);
		// T = Math.pow(0.999,iter-1);
		T = Math.exp(-( (double) iter - 1));
		
		if ((double) iter / 1000 == Math.floor(iter / 1000)) {
			if(D) System.out.println("Iteration # " + iter);
		//	if(D) System.out.println("T = " + T);
			if(D) System.out.println("Lowest energy found = " + Ebest);
		}
				
		
		// Pick neighbor of current model
		for (j = 0; j < nCol; j++) {
			xnew[j]=x[j];
		}
		
		// Index of model to randomly perturb
		index = (int) Math.floor(Math.random() * nCol); 
		
		// How much to perturb index (can be a function of T)	
		perturb[index] = (Math.random()-0.5) * 0.001;
		// perturb[index] =  (1/Math.sqrt(T)) * r.nextGaussian() * 0.0001 * Math.exp(1/(2*T)); 
		// perturb[index] = T * 0.001 * Math.tan(Math.PI*Math.random() - Math.PI/2);		
		// r = Math.random();
		// perturb[index] = Math.signum(r-0.5) * T * 0.001 * (Math.pow(1+1/T,Math.abs(2*r-1))-1);
		
		// Nonnegativity constraint
		// while (x[index] + perturb[index] < 0) {
		// perturb[index] = (Math.random()-0.5)*0.001;
		// }		
		if (xnew[index] + perturb[index] < 0) {
			perturb[index] = -xnew[index];
		}
		xnew[index] += perturb[index];
		
		// Calculate "energy" of new model (high misfit -> high energy)
		Enew = 0;
		for (i = 0; i < nRow; i++) {
			syn[i] = 0;
			for (j = 0; j < nCol; j++) {
				syn[i] += A.getEntry(i,j) * xnew[j]; // compute predicted data
			}
			misfit[i] = syn[i] - d[i];  // misfit between synthetics and data
			Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		//Enew = Math.sqrt(Enew);
		
		// Is this a new best?
		if (Enew < Ebest) {
			for (j = 0; j < nCol; j++) {
				xbest[j] = xnew[j]; }
			Ebest = Enew;
		}

		// Change state? Calculate transition probability P
		if (Enew < E) {
			P = 1; // Always keep new model if better
		} else {
		
			// Sometimes keep new model if worse (depends on T)
			P = Math.exp((E - Enew) / (double) T); 
		}
		
		if (P > Math.random()) {
			for (j = 0; j < nCol; j++) {
				x[j]=xnew[j];
			}
			E = Enew;
			//if(D) System.out.println("New soluton kept! E = " + E + ". P = " + P);
			
		}
		
	}

	// Preferred model is best model seen during annealing process
	if(D) System.out.println("Annealing schedule completed.");
	return xbest;
}



private void computeInitialStuff(ArrayList<ArrayList<Integer>> rupList) {
	double[] rupArea = new double[numRuptures];
	double[] segArea = new double[numSubSections];
	segSlipRate = new double[numSubSections];
	double[] segSlipRateStdDev = new double[numSubSections];
	double[] segMoRate = new double[numSubSections];
	double minLength = Double.MAX_VALUE;
	double maxLength = 0;
	double minArea = Double.MAX_VALUE;
	double maxArea = 0;
	FaultSectionPrefData segData;
	double totMoRate = 0;
	double aveSegDDW = 0, aveSegLength = 0;
	rupMeanMag = new double[numRuptures];
//	double[] rupMeanMo = new double[numRuptures];
	
	for (int seg = 0; seg < numSubSections; seg++) {
		segData = subSectionPrefDataList.get(seg);
		segArea[seg] = segData.getDownDipWidth() * segData.getLength()
				* 1e6 * (1.0 - segData.getAseismicSlipFactor()); // km --> m
		segSlipRate[seg] = segData.getAveLongTermSlipRate() * 1e-3; // mm/yr --> m/yr
		segSlipRateStdDev[seg] = segData.getSlipRateStdDev() * 1e-3; // mm/yr --> m/yr
		segMoRate[seg] = FaultMomentCalc.getMoment(segArea[seg], segSlipRate[seg]);  
		totMoRate += segMoRate[seg];
		aveSegDDW += segData.getDownDipWidth();
		aveSegLength += segData.getLength();

		// keep min and max length and area
		if (segData.getLength() < minLength)
			minLength = segData.getLength();
		if (segData.getLength() > maxLength)
			maxLength = segData.getLength();
		if (segArea[seg] / 1e6 < minArea)
			minArea = segArea[seg] / 1e6;
		if (segArea[seg] / 1e6 > maxArea)
			maxArea = segArea[seg] / 1e6;
	}
	aveSegDDW /= numSubSections;
	aveSegLength /= numSubSections;

	if(D) System.out.println("minSegArea = "+(float)minArea+";  maxSegArea = "+(float)maxArea);
	if(D) System.out.println("minSegLength = "+(float)minLength+";  maxSegLength = "+(float)maxLength+"");

	//System.out
	//.print("\nAverage DDW & Length of sub-sections, and implied mag of "
	//		+ minNumSectInRup + " of these rupturing: ");
	// double mag = this.magAreaRel.getMedianMag(minNumSectInRup*aveSegDDW*aveSegLength);
	// round and save this for the GR constraint
	// smallestGR_constriantMag = ((double) Math.round(mag * 10.0)) / 10.0;
	// System.out.println((float) aveSegDDW + "\t" + (float) aveSegLength
	//		+ "\t" + (float) mag + "\t" + smallestGR_constriantMag + "\n");

	// compute rupture areas
	for (int rup=0; rup<numRuptures; rup++) {
		rupArea[rup] = 0;
		ArrayList<Integer> sectIndicesForRup = rupList.get(rup);
		for (int i=0; i < sectIndicesForRup.size(); i++) {
			rupArea[rup] += segArea[sectIndicesForRup.get(i)];
		}
	}
	
	
	// compute rupture mean mags from mag-area relationship
	for (int rup = 0; rup < numRuptures; rup++) {
		double mag = this.magAreaRel.getMedianMag(rupArea[rup] / 1e6);  //null pointer exception :(
		// round this to nearest 10th unit
		rupMeanMag[rup] = ((double) Math.round(10 * mag)) / 10.0;
		// rupMeanMo[rup] = MomentMagCalc.getMoment(rupMeanMag[rup])
		//		* gaussMFD_slipCorr; // increased if magSigma >0
	}
	
}

/**
 * This returns the probability that the given magnitude event will be
 * observed at the ground surface. This is based on equation 4 of Youngs et
 * al. [2003, A Methodology for Probabilistic Fault Displacement Hazard
 * Analysis (PFDHA), Earthquake Spectra 19, 191-219] using the coefficients
 * they list in their appendix for "Data from Wells and Coppersmith (1993)
 * 276 worldwide earthquakes". Their function has the following
 * probabilities:
 * 
 * mag prob 5 0.10 6 0.45 7 0.87 8 0.98 9 1.00
 * 
 * @return
 */
private double getProbVisible(double mag) {
	return Math.exp(-12.51 + mag * 2.053)
			/ (1.0 + Math.exp(-12.51 + mag * 2.053));
	/*
	 * Ray & Glenn's equation if(mag <= 5) return 0.0; else if (mag <= 7.6)
	 * return -0.0608*mag*mag + 1.1366*mag + -4.1314; else return 1.0;
	 */
}


}

