package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;

import com.google.common.io.Files;

public class TimeDepWebDirOrganizer {

	public static void main(String[] args) throws IOException {
		File dir = new File("/var/www/html/ftp/TimeDependentPreliminary");
		File timeDepAveDir = new File("/var/www/html/ftp/kmilner/ucerf3/TimeDependent_AVE_RI_AVE_NORM_TIME_SINCE");
		File compoundPlotDir = new File("/var/www/html/ftp/kmilner/ucerf3/2013_05_10-ucerf3p3-production-10runs");
		File hazMapDir = new File("/var/www/html/ftp/pmpowers/UC-TimeDep/");
		File figsZipFile = new File("/var/www/html/ftp/kmilner/ucerf3/PaperFigsHighResPDFs.zip");
		File parentSectExcelFile = new File("/var/www/html/ftp/kmilner/ucerf3/ParentSectProbs_04.xlsx");
		
		if (!dir.exists())
			dir.mkdir();
		
		FaultSysSolutionERF_Calc.writeStringToFile(new File(dir, "HEADER.html"),
				"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
				+ "UCERF3 Time Dependent Supplementary Materials</h1>\n"
				+"\n"
				+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
				+ "Each directory contains results for a different magnitude range and time span. For example, 'm6.7_5yr' "
				+ "refers to 5 year results for M >= 6.7 ruptures and 'all_30yr' refers to 30 year results for all "
				+ "supra-seismogenic ruptures.<br>Each calculation includes aftershocks. UCERF2 results, "
				+ "where presented, have been scaled to include aftershocks as: "
				+ "newProb = 1 - exp((1/0.97)*ln(1-oldProb))</p>"
				+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
				+ "<b>Files:</b>"
				+ "<br><b><i>Fault_Probabilities_[duration].pdf</i></b>: Magnitude/Probability distributions for each Fault for the given duration."
				+ "<br><b><i>Figures_From_Report.zip</i></b>: Zip file with all figures from the report."
				+ "<br><b><i>Parent_Section_Probabilities.xlsx</i></b>: Excel file with participation probabilities on each parent fault section."
				+ "<br><b><i>Region_Probabilities_[duration].pdf</i></b>: Magnitude/Probability distributions for a number of Regionsfor the given duration."
				+"</p>");
		
		String aveTypeStr = "default BPT averaging type";
		
		for (File subDir : timeDepAveDir.listFiles()) {
			String name = subDir.getName();
			if (!subDir.isDirectory() || name.startsWith("."))
				continue;
			String label;
			File gridParticDir;
			String fileMagGrep;
			String fileDurGrep;
			if (name.equals("all_5yr")) {
				label = "All Supra-Seismogenic Ruptures, 5 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_5");
				fileMagGrep = "5.0+";
				fileDurGrep = "5yr";
			} else if (name.equals("all_30yr")) {
				label = "All Supra-Seismogenic Ruptures, 30 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_30");
				fileMagGrep = "5.0+";
				fileDurGrep = "30yr";
			} else if (name.equals("m6.7_5yr")) {
				label = "All M>=6.7 Ruptures, 5 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_5");
				fileMagGrep = "6.7+";
				fileDurGrep = "5yr";
			} else if (name.equals("m6.7_30yr")) {
				label = "All M>=6.7 Ruptures, 30 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_30");
				fileMagGrep = "6.7+";
				fileDurGrep = "30yr";
			} else if (name.equals("m7.7_5yr")) {
				label = "All M>=7.7 Ruptures, 5 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_5");
				fileMagGrep = "7.7+";
				fileDurGrep = "5yr";
			} else if (name.equals("m7.7_30yr")) {
				label = "All M>=7.7 Ruptures, 30 Year Forecast";
				gridParticDir = new File(compoundPlotDir, "gridded_time_dep_participation_prob_plots_30");
				fileMagGrep = "7.7+";
				fileDurGrep = "30yr";
			} else
				throw new IllegalStateException("Unexpected directory: "+name);
			
			FaultSysSolutionERF_Calc.writeStringToFile(new File(subDir, "HEADER.html"),
					"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
					+ label+" Figures</h1>\n"
					+"\n"
					+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
					+ "<b><i>BranchSensitivityMaps</i></b>: Sensitivity of Time Dependent Probabilities to each logic tree branch choice"
					+ "<br><b><i>GriddedParticipationMaps</i></b>: Gridded Participation Probability Maps"
					+ "<br><b><i>OtherSensitivityTests</i></b>: Miscelaneous sensitivity tests using the branch averaged solution, Mid COV values, and "+aveTypeStr
					+ "<br><b><i>SubsectionProbabilityMaps</i></b>: Results/Comparisons with UCERF2, aggregated across all logic tree branches"
					+"</p>");
			
			// copy it over
			File copiedDir = new File(dir, subDir.getName());
			org.apache.commons.io.FileUtils.copyDirectory(subDir, copiedDir);
			// rename BranchAveragedResults to SubsectionProbabilityMaps
			org.apache.commons.io.FileUtils.moveDirectory(
					new File(copiedDir, "BranchAveragedResults"), new File(copiedDir, "SubsectionProbabilityMaps"));
			
			// now add gridded
			File griddedDir = new File(copiedDir, "GriddedParticipationMaps");
			griddedDir.mkdir();
			FaultSysSolutionERF_Calc.writeStringToFile(new File(griddedDir, "HEADER.html"),
					"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
					+ "M"+fileMagGrep+", "+fileDurGrep+" Gridded Participation Probability Figures</h1>\n"
					+ "\n"
					+ "<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
					+ "Participation probability maps for both Mean UCERF3 and UCERF2. UCERF3 fault probabilities"
					+ " are spread over their respective fault polygon (and UCERF2 faults are not)."
					+ "<br>All UCERF3 data INCLUDES aftershocks. UCERF2 data DOES NOT INCLUDE AFTERSHOCKS."
					+ "</p>"
					+ "<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
					+ "<b><i>U2_Gain.pdf</i></b>: UCERF2 Time Dependent Probability Gain"
					+ "<br><b><i>U2_TimeDep_Prob.pdf</i></b>: UCERF2 Time Dependent Participation Probability"
					+ "<br><b><i>U2_TimeIndep_Prob.pdf</i></b>: UCERF2 Time Independent Participation Probability"
					+ "<b><i>U3_Gain.pdf</i></b>: UCERF3 Time Dependent Probability Gain"
					+ "<br><b><i>U3_TimeDep_Prob.pdf</i></b>: UCERF3 Time Dependent Participation Probability"
					+ "<br><b><i>U3_TimeIndep_Prob.pdf</i></b>: UCERF3 Time Independent Participation Probability"
					+ "<br><b><i>U3_Gain.pdf</i></b>: UCERF3 Time Dependent Probability Gain"
					+ "<br><b><i>U3_U2_TimeDep_Ratio.pdf</i></b>: UCERF3/UCERF2 Time Dependent Probability Ratio"
					+ "<br><b><i>U3_U2_TimeIndep_Ratio.pdf</i></b>: UCERF3/UCERF2 Time Independent Probability Ratio"
					+ "</p>");
			griddedDir.mkdir();
			for (File file : gridParticDir.listFiles()) {
				String fname = file.getName();
				if (!fname.endsWith(".pdf") || !fname.contains(fileMagGrep) || !fname.contains(fileDurGrep))
					continue;
				String outName;
				if (fname.contains("gridded_partic_u2_ratio"))
					outName = "U2_Gain.pdf";
				else if (fname.contains("gridded_partic_u3_ratio"))
					outName = "U3_Gain.pdf";
				else if (fname.contains("gridded_partic_u3_u2_dep_ratio"))
					outName = "U3_U2_TimeDep_Ratio.pdf";
				else if (fname.contains("gridded_partic_u3_u2_indep_ratio"))
					outName = "U3_U2_TimeIndep_Ratio.pdf";
				else if (fname.contains("u2_timedep_gridded_partic_prob"))
					outName = "U2_TimeDep_Prob.pdf";
				else if (fname.contains("u2_timeindep_gridded_partic_prob"))
					outName = "U2_TimeIndep_Prob.pdf";
				else if (fname.contains("timedep_gridded_partic_prob"))
					outName = "U3_TimeDep_Prob.pdf";
				else if (fname.contains("timeindep_gridded_partic_prob"))
					outName = "U3_TimeIndep_Prob.pdf";
				else
					throw new IllegalStateException("Unexpected plot file: "+fname);
				Files.copy(file, new File(griddedDir, outName));
			}
			
			// clean up BranchSensitivityMaps
			File newSensDir = new File(copiedDir, "BranchSensitivityMaps");
			FaultSysSolutionERF_Calc.writeStringToFile(new File(newSensDir, "HEADER.html"),
					"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
					+ label+" Branch Sensitivity Figures</h1>\n"
					+"\n"
					+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
					+ "<b><i>[Branch]_combined.pdf</i></b>: Ratio of participation probability averaged over each branch choice to the"
					+ " overall Mean UCERF3 average participation probability. This shows the influence of each branch choice on participation"
					+ " probabilities."
					+ "<br><b><i>histograms_combined.pdf</i></b>: Histograms showing the spread of subsection participation ratio"
					+ " values for each of the above maps."
					+"</p>");
			for (File file : newSensDir.listFiles()) {
				if (file.isFile() && !file.getName().contains("combined"))
					file.delete();
			}
			
			// clean up OtherSensitivityTests
			File newOtherSensDir = new File(copiedDir, "OtherSensitivityTests");
			FaultSysSolutionERF_Calc.writeStringToFile(new File(newOtherSensDir, "HEADER.html"),
					"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
					+ label+" Other Sensitivity Test Figures</h1>\n"
					+"\n"
					+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
					+ "<b><i>Branch_Averaged_vs_Mean_Ratio.pdf</i></b>: This shows the ratio of subsection participation probabilities"
					+ " when calculated using a UCERF3 branch averaged ERF (all rupture mags/probs are averaged) to probabilities averaged"
					+ " over each individual logic tree branch."
					+ "<br><b><i>Hist_Open_Interval_Test_[YEAR].pdf</i></b>: These maps show the influence of the historical open interval"
					+ " on subsection participation probabilities. The ratio of the given value to the UCERF3 default value of 1875 is plotted."
					+"</p>");
		}
		
		// copy over hazard maps
		File copiedMapDir = new File(dir, "HazardMaps");
		org.apache.commons.io.FileUtils.copyDirectory(hazMapDir, copiedMapDir);
		FaultSysSolutionERF_Calc.writeStringToFile(new File(copiedMapDir, "HEADER.html"),
				"<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
				+ "Hazard Maps</h1>\n"
				+"\n"
				+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
				+ "UCERF3 results calculated with branch averaged ERF (all rupture mags/probs are averaged), UCERF2 results"
				+ " calculated with MeanUCER2. Hazard calculated with NGA2West IMR and then averaged."
				+"</p>"
				+"<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:540px;\">"
				+ "<b><i>UC2_diff-[IMT]-2in50.pdf</i></b>: Difference between UCERF2 Time Dependent and Time Independent 2% in 50 year"
				+ " hazard for the specified IMT."
				+ "<br><b><i>UC2_ratio-[IMT]-2in50.pdf</i></b>: Ratio between UCERF2 Time Dependent and Time Independent 2% in 50 year"
				+ " hazard for the specified IMT."
				+ "<b><i>UC3_diff-[IMT]-2in50.pdf</i></b>: Difference between UCERF3 Time Dependent and Time Independent 2% in 50 year"
				+ " hazard for the specified IMT."
				+ "<br><b><i>UC3_ratio-[IMT]-2in50.pdf</i></b>: Ratio between UCERF3 Time Dependent and Time Independent 2% in 50 year"
				+ " hazard for the specified IMT."
				+"</p>");
		
		// copy over fault probs
		Files.copy(new File(new File(compoundPlotDir, "small_MPD_faults"), "5yr_combined.pdf"),
				new File(dir, "Fault_Probabilities_5yr.pdf"));
		Files.copy(new File(new File(compoundPlotDir, "small_MPD_faults"), "30yr_combined.pdf"),
				new File(dir, "Fault_Probabilities_30yr.pdf"));
		// copy over region probs
		Files.copy(new File(new File(compoundPlotDir, "small_MPD_plots"), "5yr_combined.pdf"),
				new File(dir, "Region_Probabilities_5yr.pdf"));
		Files.copy(new File(new File(compoundPlotDir, "small_MPD_plots"), "30yr_combined.pdf"),
				new File(dir, "Region_Probabilities_30yr.pdf"));
		
		Files.copy(figsZipFile, new File(dir, "Figures_From_Report.zip"));
		Files.copy(parentSectExcelFile, new File(dir, "Parent_Section_Probabilities.xlsx"));
	}

}
