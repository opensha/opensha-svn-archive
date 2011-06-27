package scratch.UCERF3;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.param.MagFreqDistConstraint;

import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.SimulatedAnnealing;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;

public class InversionFaultSystemSolution extends FaultSystemSolution {
	
	protected final static boolean D = true;  // for debugging
	
	InversionFaultSystemRupSet faultSystemRupSet;
	double relativeSegRateWt, relativeMagDistWt, relativeRupRateConstraintWt;
	int numIterations;
	ArrayList<SegRateConstraint> segRateConstraints;
	
	double[] aPriorRupConstraint;
	double[] initialRupModel;
		
	ArrayList<MFD_InversionConstraint> mfdConstraints;
	
	FindEquivUCERF2_Ruptures findUCERF2_Rups = null;

	public InversionFaultSystemSolution(InversionFaultSystemRupSet faultSystemRupSet, double relativeSegRateWt, 
			double relativeMagDistWt, double relativeRupRateConstraintWt, int numIterations,
			ArrayList<SegRateConstraint> segRateConstraints, double[] aPriorRupConstraint,
			double[] initialRupModel, ArrayList<MFD_InversionConstraint> mfdConstraints) {
		
		this.faultSystemRupSet=faultSystemRupSet;
		this.relativeSegRateWt=relativeSegRateWt;
		this.relativeMagDistWt=relativeMagDistWt;
		this.relativeRupRateConstraintWt=relativeRupRateConstraintWt;
		this.numIterations=numIterations;
		this.segRateConstraints=segRateConstraints;
		this.aPriorRupConstraint=aPriorRupConstraint;
		this.initialRupModel=initialRupModel;
		this.mfdConstraints=mfdConstraints;
		
		doInversion();
		
	}
	
	
	
	
	private void doInversion() {
		
		int numSections = this.getNumSections();
		int numRuptures = this.getNumRupRuptures();
		double[] sectSlipRateReduced = faultSystemRupSet.getSectSlipRateReduced();
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		
		// Find number of rows in A matrix (equals the total number of constraints)
		int numRows=numSections + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size() + (int)Math.signum(relativeRupRateConstraintWt)*numRuptures;  // number of rows used for slip-rate and paleo-rate constraints
		IncrementalMagFreqDist targetMagFreqDist=null;
		if (relativeMagDistWt > 0.0) {
			// RIGHT NOW THIS ASSUMES THERE IS ONLY ONE MFD CONSTRAINT AND THIS IGNORES THE REGION (APPLIES TO WHOLE INVERSION)
			targetMagFreqDist=mfdConstraints.get(0).getMagFreqDist();
			numRows=numRows+targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
		}
		
		
		// Components of matrix equation to invert (Ax=d)
//		OpenMapRealMatrix A = new OpenMapRealMatrix(numRows,numRuptures); // A matrix
		DoubleMatrix2D A = new SparseCCDoubleMatrix2D(numRows,numRuptures); // A matrix
		double[] d = new double[numRows];	// data vector d
		double[] rupRateSolution = new double[numRuptures]; // final solution (x of Ax=d)
		
		if(D) System.out.println("\nNumber of sections: " + numSections + ". Number of ruptures: " + numRuptures + ".\n");
		if(D) System.out.println("Total number of constraints (rows): " + numRows + ".\n");
		
		
		
		// PUT TOGETHER "A" MATRIX AND DATA VECTOR
		
		// Make sparse matrix of slip in each rupture & data vector of section slip rates
		int numNonZeroElements = 0;  
		if(D) System.out.println("\nAdding slip per rup to A matrix ...");
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			ArrayList<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				int col = rup;
//				A.addToEntry(sects.get(i),rup,slips[i]);
				A.set(row, col, A.get(row, col)+slips[i]);	// IS "A.get(row, col)" NEEDED?
				if(D) numNonZeroElements++;
			}
		}
		for (int sect=0; sect<numSections; sect++) d[sect] = sectSlipRateReduced[sect];	
		if(D) System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);

		
		
		// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
		if (relativeSegRateWt > 0.0) {
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding event rates to A matrix ...");
			for (int i=numSections; i<numSections+segRateConstraints.size(); i++) {
				SegRateConstraint constraint = segRateConstraints.get(i-numSections);
				d[i]=relativeSegRateWt * constraint.getMean() / constraint.getStdDevOfMean();
//				double[] row = A.getRow(constraint.getSegIndex());
				for (int rup=0; rup<numRuptures; rup++) {
//					if (row[rup]>0) {
					if (A.get(constraint.getSegIndex(), rup)>0) {
//						A.setEntry(i,rup,relativeSegRateWt * getProbVisible(rupMeanMag[rup]) / constraint.getStdDevOfMean());
						A.set(i, rup, (relativeSegRateWt * getProbPaleoVisible(rupMeanMag[rup]) / constraint.getStdDevOfMean()));
						if(D) numNonZeroElements++;			
					}
				}
			}
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}

		
		
		// Constrain Solution MFD to equal the Target UCERF2 MFD (minus background eqs)
		// WORKS ONLY FOR NORTHERN CALIFORNIA INVERSION
		int rowIndex = numSections + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size(); // number of rows used for slip-rate and paleo-rate constraints
		if (relativeMagDistWt > 0.0) {	
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding magnitude constraint to A matrix (match Target UCERF2 minus background) ...");
			for(int rup=0; rup<numRuptures; rup++) {
				double mag = rupMeanMag[rup];
//				A.setEntry(rowIndex+targetMagFreqDist.getXIndex(mag),rup,relativeMagDistWt);
				A.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagDistWt);
				numNonZeroElements++;
			}		
			for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
				d[rowIndex]=targetMagFreqDist.getY(m)*relativeMagDistWt;
				rowIndex++; 
			}
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
			
		
		// Constrain Rupture Rate Solution to approximately equal aPriorRupConstraint
		if (relativeRupRateConstraintWt > 0.0) {	
			if(D) System.out.println("\nAdding rupture rate constraint to A matrix ...");
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
//				A.setEntry(rowIndex,rup,relativeRupRateConstraintWt);
				A.set(rowIndex,rup,relativeRupRateConstraintWt);
				d[rowIndex]=aPriorRupConstraint[rup]*relativeRupRateConstraintWt;
				numNonZeroElements++; rowIndex++;
			}		
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		// SOLVE THE INVERSE PROBLEM
		
//		writeInversionIngredientsToFiles(A,d,initial_state); // optional: Write out inversion files to Desktop to load into MatLab (faster than Java)
//		doMatSpeedTest(A, d, initial_state, numiter);
		rupRateSolution = SimulatedAnnealing.getSolution(A,d,initialRupModel, numIterations);    		
//		rupRateSolution = loadMatLabInversionSolution(); // optional: Load in MatLab's SA solution from file instead of using Java SA code
		
		
		// IMPLEMENT THIS ******************************
		// Plots of misfit, magnitude distribution, rupture rates
		plotStuff(faultSystemRupSet.getSectionIndicesForAllRups(), A, d, rupRateSolution, relativeMagDistWt, findUCERF2_Rups);
		// Write out information to files for MatLab plots
//		writeInfoToFiles(rupList);  // Only need to redo this when rupMeanMag or rupList changes

	}

	
	@Override
	public double[] getRateForAllRups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRateForRup(int rupIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAreaForRup(int rupIndex) {
		return faultSystemRupSet.getAreaForRup(rupIndex);
	}

	@Override
	public double getAreaForSection(int sectIndex) {
		return faultSystemRupSet.getAreaForSection(sectIndex);
	}

	@Override
	public double getAveRakeForRup(int rupIndex) {
		return faultSystemRupSet.getAveRakeForRup(rupIndex);
	}

	@Override
	public double[] getAveSlipForAllRups() {
		return faultSystemRupSet.getAveSlipForAllRups();
	}

	@Override
	public double getAveSlipForRup(int rupIndex) {
		return faultSystemRupSet.getAveSlipForRup(rupIndex);
	}

	@Override
	public FaultSectionPrefData getFaultSectionData(int sectIndex) {
		return faultSystemRupSet.getFaultSectionData(sectIndex);
	}

	@Override
	public String getInfoString() {
		return faultSystemRupSet.getInfoString();
	}

	@Override
	public double[] getMagForAllRups() {
		return faultSystemRupSet.getMagForAllRups();
	}

	@Override
	public double getMagForRup(int rupIndex) {
		return faultSystemRupSet.getMagForRup(rupIndex);
	}

	@Override
	public int getNumRupRuptures() {
		return faultSystemRupSet.getNumRupRuptures();
	}

	@Override
	public int getNumSections() {
		return faultSystemRupSet.getNumSections();
	}

	@Override
	public ArrayList<Integer> getSectionsIndicesForRup(int rupIndex) {
		return faultSystemRupSet.getSectionsIndicesForRup(rupIndex);
	}

	@Override
	public ArrayList<double[]> getSlipOnSectionsForAllRups() {
		return faultSystemRupSet.getSlipOnSectionsForAllRups();
	}

	@Override
	public double[] getSlipOnSectionsForRup(int rthRup) {
		return faultSystemRupSet.getSlipOnSectionsForRup(rthRup);
	}
	
	
	private void plotStuff(ArrayList<ArrayList<Integer>> rupList, DoubleMatrix2D A, double[] d, double[] rupRateSolution, double relativeMagDistWt, FindEquivUCERF2_Ruptures findUCERF2_Rups) {
		
		int numSections = faultSystemRupSet.getNumSections();
		int numRuptures = faultSystemRupSet.getNumRupRuptures();
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		
		ArrayList<FaultSectionPrefData> faultSectionData = faultSystemRupSet.getFaultSectionList();
		
		
		// Plot the rupture rates
		if(D) System.out.println("Making plot of rupture rates . . . ");
		ArrayList funcs = new ArrayList();		
		EvenlyDiscretizedFunc ruprates = new EvenlyDiscretizedFunc(0,(double)rupRateSolution.length-1,rupRateSolution.length);
		for(int i=0; i<rupRateSolution.length; i++)
			ruprates.set(i,rupRateSolution[i]);
		funcs.add(ruprates); 	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Inverted Rupture Rates"); 
		graph.setX_AxisLabel("Rupture Index");
		graph.setY_AxisLabel("Rate");
		
		
		// Plot the slip rate data vs. synthetics
		if(D) System.out.println("Making plot of slip rate misfit . . . ");
		ArrayList funcs2 = new ArrayList();		
		EvenlyDiscretizedFunc syn = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc data = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		for (int i = 0; i < numSections; i++) {
			for (int j = 0; j < A.columns(); j++) {	
				syn.add(i,A.get(i,j) * rupRateSolution[j]); // compute predicted data
			}
			data.add(i,d[i]);
		}	
		funcs2.add(syn);
		funcs2.add(data);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Slip Rate Synthetics (blue) & Data (black)"); 
		graph2.setX_AxisLabel("Fault Section Index");
		graph2.setY_AxisLabel("Slip Rate");
		
		
		// Plot the slip rate data vs. synthetics - Averaged over parent sections
//		System.out.println("\n\nratioSR\tsynSR\tdataSR\tparentSectName");
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


		
		
		// Plot the paleo segment rate data vs. synthetics
		if(D) System.out.println("Making plot of event rate misfit . . . ");
		ArrayList funcs3 = new ArrayList();		
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc finalPaleoVisibleEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);	
		for (int r=0; r<numRuptures; r++) {
			ArrayList<Integer> rup=rupList.get(r);
			for (int i=0; i<rup.size(); i++) {			
				finalEventRateFunc.add(rup.get(i),rupRateSolution[r]);  
				finalPaleoVisibleEventRateFunc.add(rup.get(i),this.getProbPaleoVisible(rupMeanMag[r])*rupRateSolution[r]);  			
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
		
		
		
		// plot magnitude histogram for final rupture rates
		if(D) System.out.println("Making plot of final magnitude distribution . . . ");
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRupRuptures();r++)
			magHist.add(rupMeanMag[r], rupRateSolution[r]);
		ArrayList funcs4 = new ArrayList();
		magHist.setName("Magnitude Distribution of SA Solution");
		magHist.setInfo("(number in each mag bin)");
		funcs4.add(magHist);
		// If the magnitude constraint is used, add a plot of the target MFD
		if (relativeMagDistWt > 0.0) {		
			IncrementalMagFreqDist targetMagFreqDist = findUCERF2_Rups.getN_CalTargetMinusBackground_MFD(); 
			targetMagFreqDist.setTolerance(0.1); 
			targetMagFreqDist.setName("Target Magnitude Distribution");
			targetMagFreqDist.setInfo("UCERF2 Solution minus background (with aftershocks added back in)");
			funcs4.add(targetMagFreqDist);
		}
		GraphiWindowAPI_Impl graph4 = new GraphiWindowAPI_Impl(funcs4, "Magnitude Histogram for Final Rates"); 
		graph4.setX_AxisLabel("Magnitude");
		graph4.setY_AxisLabel("Frequency (per bin)");
		
		/*
		// Plot Total NCal target magnitude distribution & UCERF2 background with aftershocks
		if(D) System.out.println("Making more magnitude distribution plots . . . ");
		IncrementalMagFreqDist magHist2 = findUCERF2_Rups.getN_CalTotalTargetGR_MFD();
		IncrementalMagFreqDist magHist3 = findUCERF2_Rups.getN_Cal_UCERF2_BackgrMFD_WithAfterShocks();
		magHist2.setTolerance(0.2);	// this makes it a histogram
		magHist3.setTolerance(0.2);	// this makes it a histogram
		magHist2.setName("Total Target Magnitude Distribution for Northern California");
		magHist3.setName("Northern CA UCERF2 Background with Aftershocks");
		ArrayList funcs6 = new ArrayList();
		funcs5.add(magHist2); funcs6.add(magHist3);
		GraphiWindowAPI_Impl graph6 = new GraphiWindowAPI_Impl(funcs6, "Total Regional Target MFD and UCERF2 Background MFD"); 
		graph4.setX_AxisLabel("Magnitude");
		graph4.setY_AxisLabel("Frequency (per bin)");
		*/
		
	}


}