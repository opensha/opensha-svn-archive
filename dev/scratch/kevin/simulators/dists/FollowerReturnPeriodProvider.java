package scratch.kevin.simulators.dists;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.GraphPanel;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.utils.IDPairing;
import scratch.kevin.simulators.ElementMagRangeDescription;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.RuptureIdentifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FollowerReturnPeriodProvider implements
		ProbabilisticReturnPeriodProvider {
	
	private RuptureIdentifier driver;
	private RuptureIdentifier follower;
//	private HistogramFunction condProbFunc;
	// x is auto-inter event time, y is cross-inter event time
	private EvenlyDiscrXYZ_DataSet jointProbDataset;
//	private EvenlyDiscrXYZ_DataSet condProbDataset;
	private EvenlyDiscrXYZ_DataSet cumCondProbDataset;
	private EvenlyDiscrXYZ_DataSet hitsData;
	private EvenlyDiscrXYZ_DataSet totData;
	private double maxVal;
	private HistogramFunction refHist;
	
	private HistogramFunction followerIndepCumDist;
	
	private ActualDistReturnPeriodProvider driverActualDist;
	private ActualDistReturnPeriodProvider followerActualDist;
	
	// these will be used at the start of simulations where no prior has been found
	private double fakeStartPrevDriverTime;
	private double fakeStartPrevFollowerTime;
	
	private static final double FLAG_REVERT_REG_DIST = -1234.56;
	
	private long dep_count = 0;
	private long fallback_count = 0;
	private long tot_count = 0;
	private EvenlyDiscrXYZ_DataSet hitDataset;
	
	public FollowerReturnPeriodProvider(List<EQSIM_Event> events, RuptureIdentifier driver,
			RuptureIdentifier follower, double distDeltaYears, double maxTimeDiff) {
		this(events, driver, driver.getMatches(events), follower, follower.getMatches(events), distDeltaYears, maxTimeDiff);
	}
	
	public FollowerReturnPeriodProvider(List<EQSIM_Event> events, RuptureIdentifier driver, List<EQSIM_Event> driverMatches,
			RuptureIdentifier follower, List<EQSIM_Event> followerMatches, double distDeltaYears, double maxTimeDiff) {
		this.driver = driver;
		this.follower = follower;
		driverActualDist = new ActualDistReturnPeriodProvider(PeriodicityPlotter.getRPs(driverMatches));
		followerActualDist = new ActualDistReturnPeriodProvider(PeriodicityPlotter.getRPs(followerMatches));
		
		// create PDF
		int num = (int)(maxTimeDiff/distDeltaYears - 1);
		double[] discr_vals = null;
		// organized by auto, then each hist contains cross
		HistogramFunction[] hists = new HistogramFunction[num];
		for (int i=0; i<num; i++) {
			HistogramFunction hist = new HistogramFunction(0.5*distDeltaYears, num, distDeltaYears);
			hists[i] = hist;
			if (discr_vals == null) {
				maxVal = hist.getMaxX()+0.5*distDeltaYears;
				discr_vals = new double[num];
				for (int j=0; j<num; j++)
					discr_vals[j] = hist.getX(j);
			}
		}
		refHist = hists[0];
		
		HistogramFunction followerIndepDist = new HistogramFunction(0.5*distDeltaYears, num, distDeltaYears);
		
//		double iVal = PRECISION_SCALE * (x - minX) / delta;
//		int i = (delta == 0) ? 0 : (int) Math.round(iVal);
//		return (i<0) ? 0 : (i>=num) ? num-1 : i;
		
//		List<EQSIM_Event> combEvents = Lists.newArrayList();
//		combEvents.addAll(driverMatches);
//		combEvents.addAll(followerMatches);
//		Collections.sort(combEvents);
		List<EQSIM_Event> combEvents = events;
		
		double prevDriver = Double.NaN;
		double prevFollower = Double.NaN;
		for (EQSIM_Event e : combEvents) {
			double time = e.getTimeInYears();
			if (driver.isMatch(e)) {
				prevDriver = time;
			}
			if (follower.isMatch(e)) {
				if (!Double.isNaN(prevFollower)&& !Double.isNaN(prevDriver)) {
					double autoDelta = time - prevFollower;
					double crossDelta = time - prevDriver;
					if (autoDelta < maxVal && crossDelta < maxVal) {
						int autoIndex = refHist.getClosestXIndex(autoDelta);
						HistogramFunction hist = hists[autoIndex];
						hist.add(crossDelta, 1d);
					}
				}
				if (!Double.isNaN(prevFollower)) {
					double autoDelta = time - prevFollower;
					if (autoDelta < maxVal)
						followerIndepDist.add(autoDelta, 1d);
				}
				prevFollower = time;
			}
		}
		
		// build fallback that is independent of driver
		double indepHistTot = followerIndepDist.calcSumOfY_Vals();
		followerIndepCumDist = new HistogramFunction(followerIndepDist.getMinX(),
				followerIndepDist.getNum(), followerIndepDist.getDelta());
		
		double indepBefore = 0d;
		for (int i=0; i<followerIndepCumDist.getNum(); i++) {
			double binNum = followerIndepDist.getY(i);
			double binFract = binNum / indepHistTot;
			
			double probGivenNoBefore = binFract / (1d - indepBefore);
			followerIndepCumDist.set(i, probGivenNoBefore);
			
			indepBefore += binFract;
		}
		
//		int fIndex = 0;
//		for (EQSIM_Event d : driverMatches) {
//			double dTime = d.getTimeInYears();
//			for (int i=fIndex; i<followerMatches.size(); i++) {
//				EQSIM_Event f = followerMatches.get(i);
//				double fTime = f.getTimeInYears();
//				if (fTime > dTime) {
//					fIndex = i;
//					double delta = fTime - dTime;
//					if (delta < maxVal)
//						hist.add(delta, 1d);
//					break;
//				}
//			}
//		}
		
		// this is the probability that of Taa and Tab
		jointProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
		
		double overallNum = 0;
		for (HistogramFunction hist : hists)
			overallNum += hist.calcSumOfY_Vals();
		
		for (int i=0; i<hists.length; i++) {
			HistogramFunction hist = hists[i];
			
//			double sumY = hist.calcSumOfY_Vals();
//			double prevCum = 0;
			for (int j=0; j<hist.getNum(); j++) {
//				double binVal = hist.getY(j)/sumY;
				double jointProb = hist.getY(j)/overallNum;
//				double cumProb = jointProb / 
				
				jointProbDataset.set(i, j, jointProb);
				
//				if (prevCum == sumY) {
//					cumCondProbDataset.set(i, j, sumY);
//				} else {
//					double cumVal = jointProb/(sumY-prevCum);
//					cumCondProbDataset.set(i, j, cumVal);
//					
//					prevCum += jointProb;
//				}
			}
		}
		
		// prob of auto-time conditioned on cross-time
//		condProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//			double sumYVals = 0;
//			for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++)
//				sumYVals += jointProbDataset.get(xInd, yInd);
//			for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//				double condProb = jointProbDataset.get(xInd, yInd)/sumYVals;
//				condProbDataset.set(xInd, yInd, condProb);
//			}
//		}
		
//		// prob of Taa given Tab = P(Taa & Tab)/P(Tab)
//		condProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//			// this is total P(Tab)
//			double sumYVals = 0;
//			for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++)
//				// across all autos, sum up the cross at the given cross bin
//				sumYVals += jointProbDataset.get(xInd, yInd);
//			for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//				double condProb = jointProbDataset.get(xInd, yInd)/sumYVals;
//				condProbDataset.set(xInd, yInd, condProb);
//			}
//		}
//		
//		// now condition on it not happening before that bin
//		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		double runningProbBefore = 0;
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			if (xInd>0)
//				for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++)
//					runningProbBefore += jointProbDataset.get(xInd-1, yInd);
//			double probNotBefore = 1d-runningProbBefore;
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double cumProb = condProbDataset.get(xInd, yInd)/probNotBefore;
//				cumCondProbDataset.set(xInd, yInd, cumProb);
//			}
//		}
		
//		// TODO new test
//		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double probAtAndAfterX = 0d;
//				for (int myX=xInd; myX<jointProbDataset.getNumX(); myX++)
//					probAtAndAfterX += jointProbDataset.get(myX, yInd);
//				double val = jointProbDataset.get(xInd, yInd)/probAtAndAfterX;
//				cumCondProbDataset.set(xInd, yInd, val);
//			}
//		}
//		
//		// TODO newer test
//		EvenlyDiscrXYZ_DataSet probA_C_NPA = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		runningProbBefore = 0d;
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; xInd>0 && yInd<jointProbDataset.getNumY(); yInd++)
//				runningProbBefore += jointProbDataset.get(xInd-1, yInd);
//			double afterScale = 1d/(1d-runningProbBefore);
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++)
//				probA_C_NPA.set(xInd, yInd, jointProbDataset.get(xInd, yInd)*afterScale);
//		}
//		
//		double[] probC = new double[jointProbDataset.getNumY()];
//		for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++)
//			for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++)
//				probC[yInd] += jointProbDataset.get(xInd, yInd);
//		
//		EvenlyDiscrXYZ_DataSet probNPA_given_C = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double myProbBefore = 0d;
//				for (int myX=0; myX<xInd; myX++)
//					myProbBefore += jointProbDataset.get(myX, yInd);
//				probNPA_given_C.set(xInd, yInd, (1d-myProbBefore)/probC[yInd]);
//			}
//		}
//		
//		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double val = probA_C_NPA.get(xInd, yInd)/(probC[yInd]*probNPA_given_C.get(xInd, yInd));
//				cumCondProbDataset.set(xInd, yInd, val);
//			}
//		}
//		
//		// TODO lets try another
//		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		double runningNPA = 0d;
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; xInd>0 && yInd<jointProbDataset.getNumY(); yInd++)
//				runningNPA += jointProbDataset.get(xInd-1, yInd);
//			double probNotBefore = 1d-runningNPA;
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double totAboveRightInclusive = 0d;
//				for (int myX=xInd; myX<jointProbDataset.getNumX(); myX++)
//					for (int myY=yInd; myY<jointProbDataset.getNumY(); myY++)
//						totAboveRightInclusive += jointProbDataset.get(myX, myY);
//				Preconditions.checkState(Double.isNaN(totAboveRightInclusive)
//						|| (totAboveRightInclusive >= 0 && totAboveRightInclusive <= 1), totAboveRightInclusive);
////				double val = (jointProbDataset.get(xInd, yInd)/totAboveRightInclusive)/probNotBefore;
//				double val = jointProbDataset.get(xInd, yInd)/probNotBefore;
//				Preconditions.checkState(Double.isNaN(val) || (val >= 0 && val <= 1), "Val: "+val);
//				cumCondProbDataset.set(xInd, yInd, val);
//			}
//		}
		
//		// TODO even newer test
//		EvenlyDiscrXYZ_DataSet probA_C_NPA_NPC = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		double[] probNPA = new double[jointProbDataset.getNumX()];
//		double runningNPA = 0d;
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; xInd>0 && yInd<jointProbDataset.getNumY(); yInd++)
//				runningNPA += jointProbDataset.get(xInd-1, yInd);
//			probNPA[xInd] = 1d-runningNPA;
//			double runningNPC = 0d;
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				// now find everything below my y level and right of what's already accounted for in NPA
//				for (int myX=xInd; yInd>0 && myX<jointProbDataset.getNumX(); myX++)
//					runningNPC += jointProbDataset.get(myX, yInd-1);
//				double totProbBefore = runningNPA + runningNPC;
//				double totProbAfterInclusive = 1d-totProbBefore;
//				Preconditions.checkState(totProbAfterInclusive >= 0 && totProbAfterInclusive <= 1d,
//						"totProbAfterInclusive="+totProbAfterInclusive+", totProbBefore="+totProbBefore);
//				double val = jointProbDataset.get(xInd, yInd)*totProbAfterInclusive;
//				Preconditions.checkState(Double.isNaN(val) || (val >= 0 && val <= 1));
//				probA_C_NPA_NPC.set(xInd, yInd, val);
//			}
//		}
//		
//		EvenlyDiscrXYZ_DataSet probNPC_given_NPA = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
//		runningNPA = 0d;
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; xInd>0 && yInd<jointProbDataset.getNumY(); yInd++)
//				runningNPA += jointProbDataset.get(xInd-1, yInd);
//			probNPA[xInd] = 1d-runningNPA;
//			double runningNPC = 0d;
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				// now find everything below my y level and right of what's already accounted for in NPA
//				for (int myX=xInd; yInd>0 && myX<jointProbDataset.getNumX(); myX++)
//					runningNPC += jointProbDataset.get(myX, yInd-1);
//				double val = 1d-runningNPA-runningNPC;
//				Preconditions.checkState(Double.isNaN(val) || (val >= 0 && val <= 1));
//				probNPC_given_NPA.set(xInd, yInd, val);
//			}
//		}
//		
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			for (int yInd=0; yInd<jointProbDataset.getNumY(); yInd++) {
//				double val = probA_C_NPA_NPC.get(xInd, yInd)/(probNPA[xInd]*probNPC_given_NPA.get(xInd, yInd));
//				Preconditions.checkState(Double.isNaN(val) || (val >= 0 && val <= 1));
//				cumCondProbDataset.set(xInd, yInd, val);
//			}
//		}
		
		
		// TODO lets try this damn one
		double maxTime = events.get(events.size()-1).getTimeInYears();
		double startTime = events.get(0).getTimeInYears();
		int numSteps = (int)((maxTime - startTime)/distDeltaYears);
		
		List<EQSIM_Event> prevEvents = Lists.newArrayList();
		int eventIndex = 0;
		
		hitsData = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
		totData = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
		
		for (int step=0; step<numSteps; step++) {
			double windowStart = startTime + distDeltaYears*step;
			double windowEnd = windowStart + distDeltaYears;
			
			for (int i=eventIndex; i<events.size(); i++) {
				double time = events.get(i).getTimeInYears();
				Preconditions.checkState(time >= windowStart);
				if (time > windowEnd)
					break;
				prevEvents.add(events.get(i));
				eventIndex = i+1;
			}
			
			double prevDriverTime = Double.NaN;
			double prevFollowerTime = Double.NaN;
			double prevPrevFollowerTime = Double.NaN;
			for (int i=prevEvents.size(); --i>=0;) {
				EQSIM_Event e = prevEvents.get(i);
				double eTime = e.getTimeInYears();
				Preconditions.checkState(!Double.isNaN(eTime));
				if (Double.isNaN(prevDriverTime) && driver.isMatch(e)) {
					prevDriverTime = eTime;
				}
				if (follower.isMatch(e)) {
					if (Double.isNaN(prevFollowerTime)) {
						prevFollowerTime = eTime;
					} else {
						prevPrevFollowerTime = eTime;
					}
				}
				if (!(Double.isNaN(prevPrevFollowerTime) || Double.isNaN(prevFollowerTime) || Double.isNaN(prevDriverTime)))
					break;
			}
//			System.out.println(prevEvents.size()+", "+eventIndex+"/"+events.size()+", "+prevFollowerTime+", "+prevDriverTime);
			if (Double.isNaN(prevFollowerTime) || Double.isNaN(prevDriverTime))
				continue;
			boolean hit = false;
			if (prevFollowerTime >= windowStart && prevFollowerTime <= windowEnd) {
				hit = true;
				prevFollowerTime = prevPrevFollowerTime;
			}
			double autoDelta = windowEnd - prevFollowerTime;
			double crossDelta = windowEnd - prevDriverTime;
			
//			System.out.println(autoDelta+","+crossDelta);
			
			if (autoDelta < maxVal && crossDelta < maxVal) {
				int autoIndex = refHist.getClosestXIndex(autoDelta);
				int crossIndex = refHist.getClosestXIndex(crossDelta);
				
//				System.out.println(autoIndex+", "+crossIndex+" ("+autoDelta+","+crossDelta+")");
				
				totData.set(autoIndex, crossIndex, totData.get(autoIndex, crossIndex)+1d);
				if (hit)
					hitsData.set(autoIndex, crossIndex, hitsData.get(autoIndex, crossIndex)+1d);
			}
		} 
		
//		showDist(hitsData, hitsData.getMaxZ());
//		showDist(totData, totData.getMaxZ());
//		System.exit(0);
		
		cumCondProbDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
		for (int xInd=0; xInd<cumCondProbDataset.getNumX(); xInd++) {
			for (int yInd=0; yInd<cumCondProbDataset.getNumY(); yInd++) {
				double hits = hitsData.get(xInd, yInd);
				double tot = totData.get(xInd, yInd);
				
				double prob = hits / tot;
				cumCondProbDataset.set(xInd, yInd, prob);
			}
		}
		
		
		// fill in top right corner of dist
		// first find x val with val at greatest y
		int highestYInd = 0;
		int startBuffXInd = 0;
		int[] maxYInds = new int[jointProbDataset.getNumX()];
		for (int i=0; i<maxYInds.length; i++)
			maxYInds[i] = -1;
		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
			for (int yInd=jointProbDataset.getNumY(); --yInd>0;) {
				if (cumCondProbDataset.get(xInd, yInd) > 0) {
					maxYInds[xInd] = yInd;
					if (yInd > highestYInd) {
						highestYInd = yInd;
						startBuffXInd = xInd;
					}
					break;
				}
			}
		}
		highestYInd++;
		startBuffXInd++;
		for (int i=0; i<maxYInds.length; i++)
			if (maxYInds[i] >= 0)
				maxYInds[i]++;
//		// move up in a diagonal
//		for (int startXInd=startBuffXInd; startXInd<jointProbDataset.getNumX(); startXInd++) {
//			int startYInd = maxYInds[startXInd];
//			for (int xInd=startXInd,yInd=startYInd; ++xInd<jointProbDataset.getNumX() && ++yInd<jointProbDataset.getNumY();)
//				cumCondProbDataset.set(xInd, yInd, 1d);
//		}
		
		// fill vertically
//		for (int x=startBuffXInd; x<jointProbDataset.getNumX(); x++)
//			for (int y=maxYInds[x]; y<jointProbDataset.getNumY(); y++)
//				cumCondProbDataset.set(x, y, 1d);
		
		// move down diagonals
		// first along top
//		int startXProjectedTop = startBuffXInd + (jointProbDataset.getNumX()-1-highestYInd);
//		for (int startXInd=startXProjectedTop; startXInd<jointProbDataset.getNumX(); startXInd++) {
//			int startYInd = jointProbDataset.getNumY()-1;
//			for (int xInd=startXInd,yInd=startYInd; xInd>=startBuffXInd && yInd>=0 && yInd>maxYInds[xInd]; xInd--,yInd--) {
//				// check if we've hit a real value
//				double curVal = cumCondProbDataset.get(xInd, yInd);
//				if (curVal != 0 && !Double.isNaN(curVal))
//					break;
////				// check if we've gone too far
////				int xDiff = xInd - startBuffXInd;
////				int yDiff = highestYInd - yInd;
////				if (xDiff < 0 || yDiff > xDiff)
////					break;
//				cumCondProbDataset.set(xInd, yInd, 1d);
//			}
//		}
		int startXProjectedTop = 0;
		for (int startXInd=startXProjectedTop; startXInd<jointProbDataset.getNumX(); startXInd++) {
			int startYInd = jointProbDataset.getNumY()-1;
			for (int xInd=startXInd,yInd=startYInd; xInd>=0 && yInd>=0 && yInd>maxYInds[xInd]; xInd--,yInd--) {
				// check if we've hit a real value
				double curVal = cumCondProbDataset.get(xInd, yInd);
				if (curVal != 0 && !Double.isNaN(curVal))
					break;
//				// check if we've gone too far
//				int xDiff = xInd - startBuffXInd;
//				int yDiff = highestYInd - yInd;
//				if (xDiff < 0 || yDiff > xDiff)
//					break;
				if (xInd < startBuffXInd) {
					// filling in top left, see if we should stop
					if (yInd < highestYInd + (startBuffXInd-xInd))
						break;
				}
				cumCondProbDataset.set(xInd, yInd, FLAG_REVERT_REG_DIST);
			}
		}
		// now along right
		int startYProjectedY = jointProbDataset.getNumY()-1;
		while (startXProjectedTop >= jointProbDataset.getNumX()) {
			startXProjectedTop--;
			startYProjectedY--;
		}
		for (int startYInd=startYProjectedY; startYInd>=0; startYInd--) {
			int startXInd = jointProbDataset.getNumX()-1;
			for (int xInd=startXInd,yInd=startYInd; xInd>=startBuffXInd && yInd>=0 && yInd>maxYInds[xInd]; xInd--,yInd--) {
				double curVal = cumCondProbDataset.get(xInd, yInd);
				if (curVal != 0 && !Double.isNaN(curVal))
					break;
				cumCondProbDataset.set(xInd, yInd, FLAG_REVERT_REG_DIST);
			}
		}
//		// finally to the top left
//		for (int startXInd=startBuffXInd; --startXInd>=0;) {
//			int startYInd = highestYInd + (startBuffXInd-startXInd);
//			for (int xInd=startXInd,yInd=startYInd; ++xInd<jointProbDataset.getNumX() && ++yInd<jointProbDataset.getNumY();)
//				cumCondProbDataset.set(xInd, yInd, 1d);
//			for (int yInd=startYInd; yInd<jointProbDataset.getNumY(); yInd++)
//				cumCondProbDataset.set(startXInd, yInd, 1d);
//		}
		
		
//		double sumY = hist.calcSumOfY_Vals();
//		
//		condProbFunc = new HistogramFunction(hist.getMinX(), hist.getNum(), hist.getDelta());
//		runningTots = new double[hist.getNum()];
//		double running = 0;
//		for (int i=0; i<hist.getNum(); i++) {
//			double binVal = hist.getY(i)/sumY;
//			condProbFunc.set(i, binVal);
//			running += binVal;
//			runningTots[i] = running;
//		}
//		showDist(jointProbDataset, jointProbDataset.getMaxZ());
//		showDist(condProbDataset, 1d);
//		showDist(cumCondProbDataset, 1d);
		
//		System.out.println("**********************");
//		System.out.println("EVENTS(0,0): "+hists[0].getY(0));
//		MinMaxAveTracker crossZeroTrack = new MinMaxAveTracker();
//		EvenlyDiscretizedFunc crossZeroHist = new EvenlyDiscretizedFunc(refHist.getMinX(), refHist.getNum(), refHist.getDelta());
//		for (int i=0; i<hists.length; i++) {
//			HistogramFunction hist = hists[i];
//			crossZeroTrack.addValue(hist.getY(0));
//			crossZeroHist.set(i, hist.getY(0));
//		}
//		List<EvenlyDiscretizedFunc> elems = Lists.newArrayList();
//		elems.add(crossZeroHist);
//		new GraphWindow(elems, "Cross Zero Actual Hist");
//		EvenlyDiscretizedFunc crossZeroJointHist = new EvenlyDiscretizedFunc(refHist.getMinX(), refHist.getNum(), refHist.getDelta());
//		EvenlyDiscretizedFunc crossZeroCondHist = new EvenlyDiscretizedFunc(refHist.getMinX(), refHist.getNum(), refHist.getDelta());
//		for (int xInd=0; xInd<jointProbDataset.getNumX(); xInd++) {
//			double jointVal = jointProbDataset.get(xInd, 0);
//			if (jointVal == 0 || Double.isNaN(jointVal))
//				continue;
//			crossZeroJointHist.set(xInd, jointProbDataset.get(xInd, 0));
//			crossZeroCondHist.set(xInd, cumCondProbDataset.get(xInd, 0));
//		}
//		elems = Lists.newArrayList();
//		elems.add(crossZeroJointHist);
//		elems.add(crossZeroCondHist);
//		new GraphWindow(elems, "Cross Zero Actual Hist");
//		System.out.println("Cross 0 stats: "+crossZeroTrack);
//		System.out.println("Joint Prob(0,0): "+jointProbDataset.get(0, 0));
//		System.out.println("Cond Prob(0,0): "+condProbDataset.get(0, 0));
//		System.out.println("Cum Cond Prob(0,0): "+cumCondProbDataset.get(0, 0));
//		System.out.println("**********************");
		
		fakeStartPrevDriverTime = -Math.random()*driverActualDist.getReturnPeriod();
		fakeStartPrevFollowerTime = -Math.random()*followerActualDist.getReturnPeriod();
		hitDataset = new EvenlyDiscrXYZ_DataSet(num, num, discr_vals[0], discr_vals[0], distDeltaYears);
	}
	
	private void showDist(EvenlyDiscrXYZ_DataSet dataset, double cptMax) {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.rescale(0d, cptMax);
		cpt.setBelowMinColor(Color.WHITE);
		XYZPlotSpec spec = new XYZPlotSpec(dataset, cpt, "Dataset", "Auto-years", "Cross-years", "z value");
		new XYZPlotWindow(spec);
		if (!"sadf".isEmpty())
			return;
//		List<HistogramFunction> funcs = Lists.newArrayList();
//		List<PlotCurveCh>
		
		double totPixels = 800;
		double funcNum = refHist.getNum();
		
		int pixelsPerCell = (int)(totPixels / funcNum);
		if (pixelsPerCell < 1)
			pixelsPerCell = 1;
		int sizeX = pixelsPerCell * jointProbDataset.getNumX();
		int sizeY = pixelsPerCell * jointProbDataset.getNumY();
		BufferedImage img = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_4BYTE_ABGR);
		
		for (int x=0; x<img.getWidth(); x++) {
			for (int y=0; y<img.getHeight(); y++) {
				// need to flip y
				int myY = img.getHeight()-1-y;
				
				int r = x/pixelsPerCell;
				int c = myY/pixelsPerCell;
				
//				System.out.println("("+x+","+y+") => ("+r+","+c+")");
				
				float val = (float)dataset.get(r, c);
				
//				Color color = cpt.getColor((float)matrix.getEntry(r, c));
//				Color color = cpt.getColor((float)condProbDataset.get(r, c));
				Color color = cpt.getColor(val);
				if (val == 0)
					color = Color.GRAY;
				
				img.setRGB(x, y, color.getRGB());
			}
		}
		
		JFrame frame = new JFrame();
		frame.setTitle("Dist");
		JLabel lblimage = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(lblimage, BorderLayout.CENTER);
		frame.setSize(img.getWidth()+50, img.getHeight()+50);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	@Override
	public double getReturnPeriod() {
		return followerActualDist.getReturnPeriod();
	}
	
	private enum EventTimeType {
		RELATIVE_CENTERED,
		WINDOW_CENTERED,
		BASIS_RANDOM;
	}

	@Override
	public PossibleRupture getPossibleRupture(List<EQSIM_Event> prevEvents,
			double windowStart, double windowEnd) {
		EventTimeType timeType = EventTimeType.WINDOW_CENTERED;
		tot_count++;
		// find previous driver event
		Preconditions.checkState((float)(windowEnd-windowStart)==(float)getPreferredWindowLength());
		double prevDriverTime = Double.NaN;
		double prevFollowerTime = Double.NaN;
		for (int i=prevEvents.size(); --i>=0;) {
			Preconditions.checkState(Double.isNaN(prevFollowerTime) || Double.isNaN(prevDriverTime));
			EQSIM_Event e = prevEvents.get(i);
			double eTime = e.getTimeInYears();
			Preconditions.checkState(!Double.isNaN(eTime));
			if (Double.isNaN(prevDriverTime) && driver.isMatch(e)) {
				Preconditions.checkState(!follower.isMatch(e), "Coruptures still exist in randomized catalog!");
				prevDriverTime = eTime;
				if (!Double.isNaN(prevFollowerTime))
					// we already have a follower time, done
					break;
			}
			if (Double.isNaN(prevFollowerTime) && follower.isMatch(e)) {
				Preconditions.checkState(!driver.isMatch(e), "Coruptures still exist in randomized catalog!");
				prevFollowerTime = eTime;
				if (!Double.isNaN(prevDriverTime))
					// we already have a driver time, done
					break;
			}
		}
		// this is for triggering the first event
//		if (Double.isNaN(prevDriverTime))
//			prevDriverTime = fakeStartPrevDriverTime;
		if (Double.isNaN(prevFollowerTime))
			prevFollowerTime = fakeStartPrevFollowerTime;
//		curTime -= 0.5*getPreferredWindowLength();
		double autoDelta = windowEnd - prevFollowerTime;
		if (Double.isNaN(prevDriverTime) && autoDelta < maxVal) {
			fallback_count++;
			return buildRup(followerIndepCumDist.getY(autoDelta), timeType, windowStart,
					prevFollowerTime+followerIndepCumDist.getX(followerIndepCumDist.getClosestXIndex(autoDelta)), windowStart);
		}
		double crossDelta = windowEnd - prevDriverTime;
//		Preconditions.checkState(autoDelta < maxVal, "Stuck at time="+curTime+", prevFollower="+prevFollowerTime
//				+", autoDelta="+autoDelta+", prevDriver="+prevDriverTime+", crossDelta="+crossDelta);
		double rupWindowBasis;
		if (prevDriverTime < windowStart)
			rupWindowBasis = windowStart;
		else
			rupWindowBasis = prevDriverTime;
		if (autoDelta < maxVal && crossDelta < maxVal) {
			int autoIndex = refHist.getClosestXIndex(autoDelta);
			int crossIndex = refHist.getClosestXIndex(crossDelta);
			double val = cumCondProbDataset.get(autoIndex, crossIndex);
			if (val == FLAG_REVERT_REG_DIST) {
				fallback_count++;
				return buildRup(followerIndepCumDist.getY(autoDelta),timeType, windowStart,
						prevFollowerTime+followerIndepCumDist.getX(followerIndepCumDist.getClosestXIndex(autoDelta)), rupWindowBasis);
			}
			dep_count++;
			hitDataset.set(autoIndex, crossIndex, hitDataset.get(autoIndex, crossIndex)+1d);
			return buildRup(val, timeType, windowStart, prevDriverTime+refHist.getX(crossIndex), rupWindowBasis);
		}
		// it's been a long time, wait for a driver event then force corupture
		if (autoDelta < maxVal) {
			fallback_count++;
			return buildRup(followerIndepCumDist.getY(autoDelta),timeType, windowStart,
					prevFollowerTime+followerIndepCumDist.getX(followerIndepCumDist.getClosestXIndex(autoDelta)), rupWindowBasis);
		}
		if (crossDelta < 4*getPreferredWindowLength())
			return buildRup(1d, timeType, windowStart, 0.5d*(windowStart+windowEnd), rupWindowBasis);
		return buildRup(1d, timeType, windowStart, 0.5d*(windowStart+windowEnd), rupWindowBasis);
//		double timeDelta = curTime-prevDriverEvent.getTimeInYears();
//		if (timeDelta<maxVal)
//			return condProbFunc.getY(timeDelta);
//		return 0d;
	}
	
	private PossibleRupture buildRup(double prob, EventTimeType timeType, double windowStart, double relativeCenter, double refBasis) {
		double time;
		switch (timeType) {
		case BASIS_RANDOM:
			time = refBasis + Math.random()*getPreferredWindowLength();
			break;
		case WINDOW_CENTERED:
			time = windowStart + 0.5d*getPreferredWindowLength();
			break;
		case RELATIVE_CENTERED:
			time = relativeCenter;
			break;

		default:
			throw new IllegalStateException();
		}
		return new PossibleRupture(prob, time);
	}

	@Override
	public double getPreferredWindowLength() {
		return jointProbDataset.getGridSpacing();
//		return condProbFunc.getDelta();
	}
	
	void printStats() {
		System.out.println("Dep: "+dep_count+"/"+tot_count+" ("+(float)(100d*(double)dep_count/(double)tot_count)+" %)");
		System.out.println("Fallback: "+fallback_count+"/"+tot_count+" ("+(float)(100d*(double)fallback_count/(double)tot_count)+" %)");
		long other = tot_count - dep_count - fallback_count;
		System.out.println("Other: "+other+"/"+tot_count+" ("+(float)(100d*(double)other/(double)tot_count)+" %)");
		hitDataset.scale(1d/hitDataset.getMaxZ());
//		showDist(hitDataset, 1d);
	}
	
	private void writeDistPDF(File file, String followerName, String driverName) throws IOException {
		List<XYZPlotSpec> specs = Lists.newArrayList();
		List<Range> xRanges = Lists.newArrayList();
		List<Range> yRanges = Lists.newArrayList();
		
		xRanges.add(new Range(0d, maxVal));
		
		CPT maxSpect = GMT_CPT_Files.MAX_SPECTRUM.instance();
		maxSpect.setBelowMinColor(Color.WHITE);
		
		CPT probCPT = maxSpect.rescale(0d, 1d);
		CPT hitsCPT = maxSpect.rescale(0d, hitsData.getMaxZ());
		CPT totCPT = maxSpect.rescale(0d, totData.getMaxZ());
		
		String xAxisLabel = "Years since prev "+followerName;
		String yAxisLabel = "Years since prev "+driverName;
		String title = followerName+" dists ("+driverName+" driver)";
		
		specs.add(new XYZPlotSpec(hitsData, hitsCPT, title, xAxisLabel, yAxisLabel, ""));
		yRanges.add(new Range(0d, 750));
		specs.add(new XYZPlotSpec(totData, totCPT, title, xAxisLabel, yAxisLabel, ""));
		yRanges.add(new Range(0d, 750));
		specs.add(new XYZPlotSpec(cumCondProbDataset, probCPT, title, xAxisLabel, yAxisLabel, ""));
		yRanges.add(new Range(0d, 750));
		
		HistogramFunction driverHist = new HistogramFunction(refHist.getMinX(), refHist.getNum(), refHist.getDelta());
		for (double rp : driverActualDist.getRPs())
			if (rp <= maxVal)
				driverHist.add(rp, 1d);
		HistogramFunction followerHist = new HistogramFunction(refHist.getMinX(), refHist.getNum(), refHist.getDelta());
		for (double rp : followerActualDist.getRPs())
			if (rp <= maxVal)
				followerHist.add(rp, 1d);
		GraphPanel intereventGP = new GraphPanel(PlotPreferences.getDefault());
		List<PlotElement> intereventElems = Lists.newArrayList();
		intereventElems.add(driverHist);
		intereventElems.add(followerHist);
		List<PlotCurveCharacterstics> intereventChars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK),
				new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		PlotSpec intereventSpec = new PlotSpec(intereventElems, intereventChars, title, xAxisLabel, yAxisLabel);
		intereventGP.drawGraphPanel(intereventSpec, false, false);
		List<XYPlot> extraPlots = Lists.newArrayList(intereventGP.getPlot());
		
		XYZGraphPanel xyzGP = new XYZGraphPanel();
		
		xyzGP.drawPlot(specs, false, false, xRanges, yRanges, extraPlots);
		xyzGP.getChartPanel().setSize(1000, 2000);
		xyzGP.saveAsPDF(file.getAbsolutePath());
	}
	
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		tools.read_EQSIMv04_EventsFile(eventFile);
		List<EQSIM_Event> events = tools.getEventsList();
		
		boolean coachella_only = false;
		boolean recover_debug = true;
		
		File writeDir = new File(dir, "period_plots");
		if (!writeDir.exists())
			writeDir.mkdir();
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<String> rupIdenNames = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		
		if (!coachella_only) {
			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, 7d, 10d));
			rupIdenNames.add("SAF Cholame 7+");
			colors.add(Color.RED);

			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, 7d, 10d));
			rupIdenNames.add("SAF Carrizo 7+");
			colors.add(Color.BLUE);

//		}
			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
			rupIdenNames.add("Garlock 7+");
			colors.add(Color.GREEN);
		}
		
		RuptureIdentifier driver = new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d);
		String driverName = "SAF Mojave 7+";
		rupIdens.add(driver);
		rupIdenNames.add(driverName);
		colors.add(Color.BLACK);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Coachella 7+");
		colors.add(Color.RED);
		
		if (!coachella_only) {
			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
			rupIdenNames.add("San Jacinto 7+");
			colors.add(Color.CYAN);
		}
		
		writeDir = new File("/tmp");
		RandomDistType randDistType = RandomDistType.MOJAVE_DRIVER;
		
		if (recover_debug) {
			List<EQSIM_Event> rand_events = RandomCatalogBuilder.getRandomResampledCatalog(
					events, rupIdens, randDistType, true);
			
			RandomCatalogBuilder.getRandomResampledCatalog(
					rand_events, rupIdens, randDistType, true);
		} else {
			writeDir = new File(writeDir, randDistType.getFNameAdd()+"_corr_plots");
			if (!writeDir.exists())
				writeDir.mkdir();
			
			Map<IDPairing, HistogramFunction> origFuncs =
					PeriodicityPlotter.plotTimeBetweenAllIdens(writeDir, events, rupIdens, rupIdenNames, colors,
							null, null, 2000d, 10d);
			PeriodicityPlotter.	plotTimeBetweenAllIdens(writeDir, events, rupIdens, rupIdenNames, colors,
					RandomDistType.MOJAVE_DRIVER, origFuncs, 2000d, 10d);
			
//			File subDir = new File(writeDir, "round2");
//			if (!subDir.exists())
//				subDir.mkdir();
//			PeriodicityPlotter.	plotTimeBetweenAllIdens(subDir, rand_events, rupIdens, rupIdenNames, colors,
//					RandomDistType.MOJAVE_DRIVER, origFuncs, 2000d, 10d);
			
			System.out.println("DONE");
		}
		
		File pdfDir = new File("/tmp/follower_pdfs");
		if (!pdfDir.exists())
			pdfDir.mkdir();
		
		List<File> pdfs = Lists.newArrayList();
		
		for (int i=0; i<rupIdens.size(); i++) {
			RuptureIdentifier iden = rupIdens.get(i);
			String name = rupIdenNames.get(i);
			
			if (iden == driver)
				continue;
			
			FollowerReturnPeriodProvider prov = new FollowerReturnPeriodProvider(events, driver, iden, 10d, 1500d);
			
			String fName = PeriodicityPlotter.getFileSafeString(name);
			File file = new File(pdfDir, fName+".pdf");
			
			prov.writeDistPDF(file, name, driverName);
			
			pdfs.add(file);
		}
		
		PeriodicityPlotter.combinePDFs(pdfs, new File(pdfDir, "follower_dists.pdf"));
		
//		System.exit(0);
	}

}
