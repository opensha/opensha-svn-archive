/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.earthquake.calc;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;


/**
 * <p>Title: ERF_Calculator</p>
 * <p>Description: This for various calculations related to ERFs.  
 * This is to replace ERF2GriddedSeisRatesCalc (which is overly complex)
 * </p>
 * @author Ned Field
 * @version 1.0
 */
public class ERF_Calculator {


  /**
   * default class Constructor.
   */
  public ERF_Calculator() {}

  /**
   * This computes the annualized total magnitude frequency distribution for the ERF.
   * Magnitudes that are out of the range specified are ignored.
   * @param eqkRupForecast - assumed to be updated before being passed in
   * @param min - for MagFreqDist x axis
   * @param max - for MagFreqDist x axis
   * @param num - for MagFreqDist x axis
   * @param preserveRates - if true rates are assigned to nearest discrete magnitude 
   * without modification,if false rates are adjusted to preserve moment rate.
   * @return
   */
  public static SummedMagFreqDist getTotalMFD_ForERF(ERF eqkRupForecast, double min,double max,int num, boolean preserveRates) {
	  SummedMagFreqDist mfd = new SummedMagFreqDist(min,max,num);
	  double duration = eqkRupForecast.getTimeSpan().getDuration();
	  for(int s=0;s<eqkRupForecast.getNumSources();s++) {
		  ProbEqkSource src = eqkRupForecast.getSource(s);
		  for(int r=0;r<src.getNumRuptures();r++) {
			  ProbEqkRupture rup = src.getRupture(r);
			  mfd.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), preserveRates);
		  }
	  }
		 return mfd;
  }
  
  
  /**
   * This computes the annualized total magnitude frequency distribution for the ERF.
   * Magnitudes that are out of the range specified are ignored.
   * @param src - an earthquake source
   * @param duration - the forecast duration (e.g., from a time span)
   * @param min - for MagFreqDist x axis
   * @param max - for MagFreqDist x axis
   * @param num - for MagFreqDist x axis
   * @param preserveRates - if true rates are assigned to nearest discrete magnitude 
   * without modification,if false rates are adjusted to preserve moment rate.
   * @return
   */
  public static SummedMagFreqDist getTotalMFD_ForSource(ProbEqkSource src, double duration, double min,double max,int num, boolean preserveRates) {
	  SummedMagFreqDist mfd = new SummedMagFreqDist(min,max,num);
	  for(int r=0;r<src.getNumRuptures();r++) {
		  ProbEqkRupture rup = src.getRupture(r);
		  mfd.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), preserveRates);
	  }
	  return mfd;
  }

  
  
  /**
   * This computes the total magnitude frequency distribution (equivalent poisson rates) for the
   * ERF inside the region (only the fraction of each rupture inside the region is included)
   * @param erf
   * @param region
   * @param minMag
   * @param numMag
   * @param deltaMag
   * @param preserveRates - this tells whether to preserve rates or preserve moment rates
   * @return
   */
  public static SummedMagFreqDist getMagFreqDistInRegion(ERF erf, Region region,
		  double minMag,int numMag,double deltaMag, boolean preserveRates) {
	  
	  SummedMagFreqDist magFreqDist = new SummedMagFreqDist(minMag, numMag, deltaMag);
	  double duration = erf.getTimeSpan().getDuration();
	  for (int s = 0; s < erf.getNumSources(); ++s) {
		  ProbEqkSource source = erf.getSource(s);
		  for (int r = 0; r < source.getNumRuptures(); ++r) {
			  ProbEqkRupture rupture = source.getRupture(r);
			  double mag = rupture.getMag();
			  double equivRate = rupture.getMeanAnnualRate(duration);
			  EvenlyGriddedSurface rupSurface = rupture.getRuptureSurface();
			  double ptRate = equivRate/rupSurface.size();
			  ListIterator<Location> it = rupSurface.getAllByRowsIterator();
			  while (it.hasNext()) {
				  //discard the pt if outside the region 
				  if (!region.contains(it.next()))
					  continue;
				  magFreqDist.addResampledMagRate(mag, ptRate, preserveRates);
			  }
		  }
	  }
	  return magFreqDist;
  }
  
  
  
  /**
   * This computes the rate of event (equivalent poisson rates) for the
   * ERF inside the region (only the fraction of each rupture inside the region is included)
   * @param erf
   * @param region
   * @param minMag - ruptures with magnitudes less than this are not included in the total
   * @return
   */
  public static double getTotalRateInRegion(ERF erf, Region region, double minMag) {
	  double duration = erf.getTimeSpan().getDuration();
	  double totRate=0;
	  for (int s = 0; s < erf.getNumSources(); ++s) {
		  ProbEqkSource source = erf.getSource(s);
		  for (int r = 0; r < source.getNumRuptures(); ++r) {
			  ProbEqkRupture rupture = source.getRupture(r);
			  if(rupture.getMag()>= minMag) {
				  double fractionInside = RegionUtils.getFractionInside(region, rupture.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface());
				  totRate += fractionInside*rupture.getMeanAnnualRate(duration);
			  }
		  }
	  }
	  return totRate;
  }

  
  
  
  /**
   * This computes the  magnitude frequency distribution (equivalent poisson rates) for each
   * location in the supplied GriddedRegion and ERF.  This assumes a uniform distribution of 
   * nucleations on each rupture.  Ruptures that fall outside the region are ignored.  The
   * indices for the returned array list are the same as for the GriddedRegion.
   * @param erf
   * @param griddedRegion
   * @param minMag
   * @param numMag
   * @param deltaMag
   * @param preserveRates - this tells whether to preserve rates or preserve moment rates
   * @return
   */
  public static ArrayList<SummedMagFreqDist> getMagFreqDistsAtLocsInRegion(ERF erf, GriddedRegion griddedRegion,
		  double minMag,int numMag,double deltaMag, boolean preserveRates) {
	  
	  ArrayList<SummedMagFreqDist> magFreqDists = new ArrayList<SummedMagFreqDist>();
	  for(Location loc:griddedRegion)
		  magFreqDists.add(new SummedMagFreqDist(minMag, numMag, deltaMag));
	  
	  SummedMagFreqDist unassignedMFD = new SummedMagFreqDist(minMag, numMag, deltaMag);
	  
	  double duration = erf.getTimeSpan().getDuration();
	  for (int s = 0; s < erf.getNumSources(); ++s) {
		  ProbEqkSource source = erf.getSource(s);
		  for (int r = 0; r < source.getNumRuptures(); ++r) {
			  ProbEqkRupture rupture = source.getRupture(r);
			  double mag = rupture.getMag();
			  double equivRate = rupture.getMeanAnnualRate(duration);
			  EvenlyGriddedSurface rupSurface = rupture.getRuptureSurface();
			  double ptRate = equivRate/rupSurface.size();
			  ListIterator<Location> it = rupSurface.getAllByRowsIterator();
			  while (it.hasNext()) {
				  Location loc = it.next();
				  int index = griddedRegion.indexForLocation(loc);
				  if(index >=0)
					  magFreqDists.get(index).addResampledMagRate(mag, ptRate, preserveRates);
				  else
					  unassignedMFD.addResampledMagRate(mag, ptRate, preserveRates);
			  }
		  }
	  }
	  System.out.println(unassignedMFD.getCumRateDistWithOffset());
	  return magFreqDists;
  }
  
  
  /**
   * This writes a STEP format file, which gives the lat, lon, and rates for the associated mag-freq
   * dist on each line.
   * @param mfds
   * @param griddedRegion
   * @param filePathAndName
   */
  public static void writeSTEP_FormatFile(ArrayList<SummedMagFreqDist> mfds, GriddedRegion griddedRegion,
		  String filePathAndName) {
		// The following matches the total MFD in the UCERF2 report within 1% below M 8.3
		SummedMagFreqDist totalMFD = new SummedMagFreqDist(5.05,36,0.1);
		for(IncrementalMagFreqDist mfd:mfds)
			totalMFD.addResampledMagFreqDist(mfd, true);
		System.out.println(totalMFD.getCumRateDistWithOffset());
		
		FileWriter fw;
		try {
			fw = new FileWriter(filePathAndName);
			String header = "lat\tlon";
			for(int m=0; m<totalMFD.getNum();m++)
				header += "\t"+(float)totalMFD.getX(m);
			fw.write(header+"\n");
			for(int i=0;i<griddedRegion.getNodeCount();i++) {
				Location loc = griddedRegion.locationForIndex(i);
				String line = (float)loc.getLatitude()+"\t"+(float)loc.getLongitude();
				IncrementalMagFreqDist mfd = mfds.get(i);
				for(int m=0; m<mfd.getNum();m++)
					line += "\t"+(float)mfd.getY(m);
				fw.write(line+"\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

  
  
	public static void main(String[] args) {
		
		
		// OLD STUFF BELOW
		/*
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_EMPIRICAL);
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.updateForecast();
		IncrementalMagFreqDist mfd = meanUCERF2.getTotalMFD();
//		System.out.println(mfd.toString());
		
//		SummedMagFreqDist calcMFD = ERF_Calculator.getTotalMFD_ForERF(meanUCERF2, mfd.getMinX(), mfd.getMaxX(), mfd.getNum(), true); 
		
		// get the observed rates for comparison
		ArrayList<EvenlyDiscretizedFunc> obsCumDists = UCERF2.getObsCumMFD(false);
		
		// PLOT Cum MFDs
		ArrayList mfd_funcs = new ArrayList();
		mfd_funcs.add(mfd.getCumRateDistWithOffset());
//		mfd_funcs.add(calcMFD.getCumRateDistWithOffset());
		mfd_funcs.addAll(obsCumDists);

		GraphiWindowAPI_Impl mfd_graph = new GraphiWindowAPI_Impl(mfd_funcs, "Magnitude Frequency Distributions");   
		mfd_graph.setYLog(true);
		mfd_graph.setY_AxisRange(1e-5, 10);
		mfd_graph.setX_AxisRange(5.0, 9.0);
		mfd_graph.setX_AxisLabel("Magnitude");
		mfd_graph.setY_AxisLabel("Rate (per yr)");

		ArrayList<PlotCurveCharacterstics> plotMFD_Chars = new ArrayList<PlotCurveCharacterstics>();
		plotMFD_Chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLUE, 2));
//		plotMFD_Chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLACK, 2));
		plotMFD_Chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.RED, 2));
		plotMFD_Chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 4));
		plotMFD_Chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 4));
		mfd_graph.setPlottingFeatures(plotMFD_Chars);
		mfd_graph.setTickLabelFontSize(12);
		mfd_graph.setAxisAndTickLabelFontSize(14);
		
		*/
	}

}
