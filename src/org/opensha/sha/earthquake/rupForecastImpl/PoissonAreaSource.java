package org.opensha.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.MagScalingRelationship;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.griddedForecast.MagFreqDistsForFocalMechs;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * <p>Title: PoissonAreaSource </p>
 * <p>Description: This is basically a more sophisticated version of the class GriddedRegionPoissonEqkSource. 
 * The options to account for the finiteness of rupture surfaces are the same as provided in the <code>PointToLineSource</code>
 * class, plus the option to treat all sources as point sources is added included here. </p>
 * 
 * @author Marco Pagani and Ned Field
 * @version 1.0
 */

public class PoissonAreaSource extends PointToLineSource implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	// for Debug purposes
	private static String C = new String("PoissonAreaSource");
	private static String NAME = "Poisson Area Source";
	private boolean D = false;

	private Region reg;


	/**
	 * This constructor treats all ruptures as point sources.
	 * @param reg
	 * @param gridResolution
	 * @param magFreqDistsForFocalMechs
	 * @param aveRupTopVersusMag
	 * @param defaultHypoDepth
	 * @param magScalingRel
	 * @param lowerSeisDepth
	 * @param duration
	 * @param minMag
	 */
	public PoissonAreaSource(Region reg, double gridResolution, MagFreqDistsForFocalMechs 
			magFreqDistsForFocalMechs, ArbitrarilyDiscretizedFunc aveRupTopVersusMag, 
			double defaultHypoDepth, double duration, double minMag) {
		
		this.duration = duration;
		this.isPoissonian = true;
		this.reg = reg;

		probEqkRuptureList = new ArrayList<ProbEqkRupture>();
		rates = new ArrayList<Double>();
		
		// Region discretization
		GriddedRegion gridReg = new GriddedRegion(reg,gridResolution,null); 
		
		// MFD and focal mechanism arrays
		IncrementalMagFreqDist[] magFreqDists = magFreqDistsForFocalMechs.getMagFreqDistList();
		IncrementalMagFreqDist[] magFreqDistsScld = new IncrementalMagFreqDist[magFreqDists.length];
		
		// Define the focal mechanism array
		FocalMechanism[] focalMechanisms;
		if (magFreqDistsForFocalMechs.getFocalMechanismList() == null){
			focalMechanisms = new FocalMechanism[magFreqDists.length];	
			// TODO fix the default	properties of the focal mechanism	
			for (int i=0; i<magFreqDists.length; i++) {
				focalMechanisms[i] = new FocalMechanism(Double.NaN,45.0,90.0);
			}
			
		} else {
			focalMechanisms = magFreqDistsForFocalMechs.getFocalMechanismList();
		}
		
		// Scale the MagFreqDist 
		for (int i=0; i<magFreqDists.length; i++) {
			IncrementalMagFreqDist tmpDst = magFreqDists[i].deepClone();
			for (int j=0; j < magFreqDists[i].getNum(); j++){
				double tmpX = magFreqDists[i].getX(j);
				double tmpY = magFreqDists[i].getY(j)/gridReg.getNodeCount();
				tmpDst.set(tmpX,tmpY);
			}
			magFreqDistsScld[i] = tmpDst;
			
			if (D){
				for (int j=0; j < magFreqDistsScld[i].getNum(); j++){
					System.out.printf(" %5.2f %6.3f\n",magFreqDistsScld[i].getX(j),
							magFreqDistsScld[i].getY(j));
				}
			}
		}
		
		// Create the ruptures  
		for (int i=0; i<magFreqDists.length; i++) {
			for (int j=0; j<gridReg.getNodeCount(); j++){	
				Location loc = gridReg.getNodeList().get(j);
				for (int k=0; k < magFreqDistsScld.length; k++){
					for (int w=0; w < magFreqDistsScld[k].getNum(); w++){
						double mag = magFreqDistsScld[k].getX(w);
						ProbEqkRupture rup = new ProbEqkRupture();
						rup.setMag(mag);
						
					    // set the probability if it's Poissonian (otherwise this was already set)
					    if(isPoissonian){
					    	double rate = magFreqDistsScld[k].getY(w);
					    	rup.setProbability(1.0 - Math.exp(-duration*(rate)));
					    }
					    
					    // Rake and depth
					    rup.setAveRake(focalMechanisms[i].getRake());
				    	double depth;
				    	if(mag < aveRupTopVersusMag.getMinX())
				    		depth = defaultHypoDepth;
				    	else
				    		depth = aveRupTopVersusMag.getClosestY(mag);
				    	//loc.setDepth(depth);
				    	loc = new Location(
					    		loc.getLatitude(), loc.getLongitude(), depth);
				    	// Location 2 loops out; is above redundant?
				    	
					    // Set 
						rup.setPointSurface(loc,focalMechanisms[i].getDip());
						
						// Adding the rupture 
						this.probEqkRuptureList.add(rup);
					}
				}	
			}
		}
		
		
	}
		
	/**
	 * This constructor takes a Region, grid resolution (grid spacing), MagFreqDistsForFocalMechs, 
	 * depth as a function of mag (aveRupTopVersusMag), and a default depth (defaultHypoDepth).  
	 * The depth of each source is set according to the mag using the aveRupTopVersusMag function; 
	 * if mag is below the minimum x value of this function, then defaultHypoDepth is applied.  
	 * The FocalMechanism of MagFreqDistsForFocalMechs is applied, and a random strike is applied 
	 * if the associated strike is NaN (a different random value for each and every rupture).  
	 * This sets the source as Poissonian.  
	 */
	public PoissonAreaSource(Region reg, double gridResolution, MagFreqDistsForFocalMechs 
			magFreqDistsForFocalMechs, ArbitrarilyDiscretizedFunc aveRupTopVersusMag, 
			double defaultHypoDepth, MagScalingRelationship magScalingRel, double lowerSeisDepth, 
			double duration, double minMag) {

		this.duration = duration;
		this.isPoissonian = true;
		this.reg = reg;

		probEqkRuptureList = new ArrayList<ProbEqkRupture>();
		rates = new ArrayList<Double>();
		
		// Region discretization
		GriddedRegion gridReg = new GriddedRegion(reg,gridResolution,null); 

		// MFD and focal mechanism arrays
		IncrementalMagFreqDist[] magFreqDists = magFreqDistsForFocalMechs.getMagFreqDistList();
		
		// Define the focal mechanism array
		FocalMechanism[] focalMechanisms;
		if (magFreqDistsForFocalMechs.getFocalMechanismList() == null){
			throw new RuntimeException("null FocalMechanismList not yet supported");
		} else {
			focalMechanisms = magFreqDistsForFocalMechs.getFocalMechanismList();
		}	
		
		// compute weights taking into account change is node area with lat
		int numPts = gridReg.getNodeCount();
		double[] weights = new double[numPts];
		double tot=0;
		for(int i=0;i<numPts;i++) {
			double latitude = gridReg.locationForIndex(i).getLatitude();
			weights[i] = Math.cos(latitude*Math.PI/180)/gridReg.getNodeCount();
			tot += weights[i];
		}
		for(int i=0;i<numPts;i++) weights[i] /= tot;
		
		// Create the ruptures  
		for (int i=0; i<magFreqDists.length; i++) {
			for (int j=0; j<gridReg.getNodeCount(); j++){
				Location loc = gridReg.getNodeList().get(j);
				mkAndAddRuptures(loc,magFreqDists[i], focalMechanisms[i], aveRupTopVersusMag, 
					defaultHypoDepth, magScalingRel, lowerSeisDepth, duration, minMag, weights[j]);
			}
		}
	}


	/**
	 * This constructor is the same as the previous one, but rather than using the given or a 
	 * random strike, this applies a spoked source where several strikes are applied with even  
	 * spacing in azimuth. numStrikes defines the number of strikes applied (e.g., numStrikes=2 
	 * would be a cross hair) and firstStrike defines the azimuth of the first one (e.g., 
	 * firstStrike=0 with numStrikes=2 would be a cross-hair source that is perfectly aligned NS 
	 * and EW).
	 */
	public PoissonAreaSource(Region reg, double gridResolution, MagFreqDistsForFocalMechs 
			magFreqDistsForFocalMechs, ArbitrarilyDiscretizedFunc aveRupTopVersusMag, 
			double defaultHypoDepth, MagScalingRelationship magScalingRel, double lowerSeisDepth, 
			double duration, double minMag, int numStrikes, double firstStrike){
		
		this.reg = reg;
		this.duration = duration;
		this.isPoissonian = true;
		
		probEqkRuptureList = new ArrayList<ProbEqkRupture>();
		rates = new ArrayList<Double>();
		
		// Region discretization
		GriddedRegion gridReg = new GriddedRegion(reg,gridResolution,null); 

		// MFD and focal mechanism arrays
		IncrementalMagFreqDist[] magFreqDists = magFreqDistsForFocalMechs.getMagFreqDistList();
		
		// Define the focal mechanism array
		FocalMechanism[] focalMechanisms;
		if (magFreqDistsForFocalMechs.getFocalMechanismList() == null){
			throw new RuntimeException("null FocalMechanismList not yet supported");
		} else {
			focalMechanisms = magFreqDistsForFocalMechs.getFocalMechanismList();
		}

		// set the strikes
		double deltaStrike = 180/numStrikes;
		double[] strike = new double[numStrikes];
		for(int n=0;n<numStrikes;n++)
			strike[n]=firstStrike+n*deltaStrike;
		
		// compute weights taking into account change is node area with lat
		int numPts = gridReg.getNodeCount();
		double[] weights = new double[numPts];
		double tot=0;
		for(int i=0;i<numPts;i++) {
			double latitude = gridReg.locationForIndex(i).getLatitude();
			weights[i] = Math.cos(latitude*Math.PI/180)/(gridReg.getNodeCount()*numStrikes);
			tot += weights[i];
		}
		for(int i=0;i<numPts;i++) weights[i] /= tot;

		for (int i=0; i<magFreqDists.length; i++) {
			FocalMechanism focalMech = focalMechanisms[i].copy(); // COPY THIS
			for (int j=0; j<gridReg.getNodeCount(); j++){
				Location loc = gridReg.getNodeList().get(j);
				for(int s=0;s<numStrikes;s++) {
					focalMech.setStrike(strike[s]);
					mkAndAddRuptures(loc,magFreqDists[i], focalMechanisms[i], aveRupTopVersusMag, 
							defaultHypoDepth, magScalingRel, lowerSeisDepth, duration, minMag, weights[j]);	
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Region getRegion() { return reg;}



	/**
	 * This returns the shortest horizontal dist to the point source (minus half the length of the 
	 * longest rupture).
	 * 
	 * @param site
	 * @return minimum distance
	 */
	public double getMinDistance(Site site) {
		double dist = reg.distanceToLocation(site.getLocation()) - maxLength/2.0;
		if(dist < 0) dist=0;
		return dist;
	}

}

