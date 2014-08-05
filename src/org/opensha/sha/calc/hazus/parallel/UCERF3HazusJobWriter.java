package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.NSHMP_2008_CA;

import com.google.common.collect.Lists;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;

public class UCERF3HazusJobWriter {
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
	
	private static AbstractERF getERF(File fsdFile, int years, boolean backSeis, BackgroundRupType backSeisType) {
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF();
		erf.setParameter(FaultSystemSolutionERF.FILE_PARAM_NAME, fsdFile);
		erf.getTimeSpan().setDuration((double)years);
		erf.setParameter(BackgroundRupParam.NAME, backSeisType);
		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, true);
		return erf;
	}

	public static void main(String[] args) throws IOException, InvocationTargetException {
		boolean backSeis = true;
		
		ScalarIMR imr = AttenRelRef.NSHMP_2008.instance(null);
		imr.setParamDefaults();
		imr.getParameter(NSHMP_2008_CA.IMR_UNCERT_PARAM_NAME).setValue(true);
		
		double spacing = 0.1; // TODO
		
		int years = 50;
		
		boolean nullBasin = true;
		SiteDataValue<?> hardcodedVal = null;
		
		boolean noBasin = false;
		boolean useWald = false;
		
		int mins = 500;
		int nodes = 20;
		int ppn = 8;
		String queue = "nbns";
		
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(new File("/home/scec-02/kmilner/"
				+ "ucerf3/inversion_compound_plots/2013_05_10-ucerf3p3-production-10runs/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip"));
		File hazMapsDir = new File("/home/scec-02/kmilner/hazMaps");
		
//		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(new File(
//				"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
//				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip"));
//		File hazMapsDir = new File("/tmp/hazMaps");
		
		List<LogicTreeBranch> branches = Lists.newArrayList();
		List<String> dirNames = Lists.newArrayList();
		
		
		
		dirNames.add("ref_wills");
		branches.add((LogicTreeBranch) LogicTreeBranch.DEFAULT.clone());
		
		dirNames.add("ref_geol_wills");
		LogicTreeBranch branch = (LogicTreeBranch) LogicTreeBranch.DEFAULT.clone();
		branch.setValue(DeformationModels.GEOLOGIC);
		branches.add(branch);
		
		dirNames.add("ref_neok_wills");
		branch = (LogicTreeBranch) LogicTreeBranch.DEFAULT.clone();
		branch.setValue(DeformationModels.NEOKINEMA);
		branches.add(branch);
		
		dirNames.add("ref_abm_wills");
		branch = (LogicTreeBranch) LogicTreeBranch.DEFAULT.clone();
		branch.setValue(DeformationModels.ABM);
		branches.add(branch);
		
		dirNames.add("ref_ucerf2spatial_wills");
		branch = (LogicTreeBranch) LogicTreeBranch.DEFAULT.clone();
		branch.setValue(SpatialSeisPDF.UCERF2);
		branches.add(branch);
		
		Date today = new Date();
		
		for (int i=0; i<dirNames.size(); i++) {
			String dirName = df.format(today)+"_"+dirNames.get(i);
			branch = branches.get(i);
			
			File jobDir = new File(hazMapsDir, dirName);
			if (!jobDir.exists())
				jobDir.mkdir();
			
			FaultSystemSolution sol = cfss.getSolution(branch);
			File solFile = new File(jobDir, branch.buildFileName()+"_sol.zip");
			FaultSystemIO.writeSol(sol, solFile);
			
			AbstractERF erf = getERF(solFile, years, backSeis, BackgroundRupType.POINT);
			
			HazusJobWriter.prepareJob(erf, imr, spacing, years, nullBasin, hardcodedVal,
					noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		}
		
		// now ref no soil
		branch = (LogicTreeBranch) LogicTreeBranch.DEFAULT.clone();
		hardcodedVal = new SiteDataValue<Double>(SiteData.TYPE_VS30, SiteData.TYPE_FLAG_INFERRED, 760d);
		noBasin = true;
		String dirName = df.format(today)+"_ref_rock";
		
		File jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		FaultSystemSolution sol = cfss.getSolution(branch);
		File solFile = new File(jobDir, branch.buildFileName()+"_sol.zip");
		FaultSystemIO.writeSol(sol, solFile);
		
		AbstractERF erf = getERF(solFile, years, backSeis, BackgroundRupType.POINT);
		
		HazusJobWriter.prepareJob(erf, imr, spacing, years, nullBasin, hardcodedVal,
				noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		
		// now ref no soil
		dirName = df.format(today)+"_ref_rock_crosshair";
		
		jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		sol = cfss.getSolution(branch);
		solFile = new File(jobDir, branch.buildFileName()+"_sol.zip");
		FaultSystemIO.writeSol(sol, solFile);
		
		erf = getERF(solFile, years, backSeis, BackgroundRupType.CROSSHAIR);
		
		HazusJobWriter.prepareJob(erf, imr, spacing, years, nullBasin, hardcodedVal,
				noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		
		// now ref no soil with 3NGA
		dirName = df.format(today)+"_ref_rock_+3nga";
		
		jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		solFile = new File(jobDir, branch.buildFileName()+"_sol.zip");
		FaultSystemIO.writeSol(sol, solFile);
		
		erf = getERF(solFile, years, backSeis, BackgroundRupType.POINT);
		
		HazusJobWriter.prepareJob(erf, HazusJobWriter.getIMR(HazusJobWriter.MultiIMR_NO_AS_NAME, 3d, true),
				spacing, years, nullBasin, hardcodedVal, noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		
		
		// now UCERF2, rocj
//		if (!"a".isEmpty())
//			throw new RuntimeException("remember to make sure back seis consistent between UCERF2/UCERF3!");
		erf = HazusJobWriter.getUCERF2(years, 0, backSeis);
		dirName = df.format(today)+"_ucerf2_ref_rock";
		jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		HazusJobWriter.prepareJob(erf, imr, spacing, years, nullBasin, hardcodedVal,
				noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		
		// now UCERF2 point sources, rock
		erf = HazusJobWriter.getUCERF2(years, 0, backSeis, UCERF2.BACK_SEIS_RUP_POINT);
		dirName = df.format(today)+"_ucerf2_ref_rock_point";
		jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
		
		HazusJobWriter.prepareJob(erf, imr, spacing, years, nullBasin, hardcodedVal,
				noBasin, useWald, dirName, mins, nodes, ppn, queue, hazMapsDir);
		
		
		hardcodedVal = null;
		noBasin = false;
//		if (!"a".isEmpty())
//			throw new RuntimeException("remember to make sure back seis consistent between UCERF2/UCERF3!");
		erf = HazusJobWriter.getUCERF2(years, 0, backSeis);
		dirName = df.format(today)+"_ucerf2_wills";
		jobDir = new File(hazMapsDir, dirName);
		if (!jobDir.exists())
			jobDir.mkdir();
	}

}
