package org.opensha.sha.faultSurface;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;

/**
 * This class represents compound EvenlyGriddedSurfaces to represent multi-fault ruptures. 
 * The most challenging thing here is maintaining the Aki Richards convention for the total
 * surface.  The main method here was used to make various tests to ensure that these things are
 * handled properly (these data were analyzed externally using Igor).
 * 
 * @author field
 *
 */
public class CompoundRuptureSurface implements RuptureSurface {
	
	ArrayList<RuptureSurface> surfaces;
	
	final static boolean D = true;
	
	// this tells whether any traces need to be reversed
	boolean[] reverseSurfTrace; //  indicates which surface traces need to be reversed in building the entire upper surface
	boolean reverseOrderOfSurfaces = false; // indicates whether the order of surfaces needs to be reversed to honor Aki and Richards
	double aveDip, totArea,aveLength=-1,aveRupTopDepth=-1,aveWidth=-1, aveGridSpacing=-1;
	FaultTrace upperEdge = null;
	
	// for distance measures
	Location siteLocForDistCalcs= new Location(Double.NaN,Double.NaN);
	Location siteLocForDistXCalc= new Location(Double.NaN,Double.NaN);
	double distanceJB, distanceSeis, distanceRup, distanceX;

	
	
	public CompoundRuptureSurface(ArrayList<RuptureSurface> surfaces) {
		this.surfaces = surfaces;
		computeInitialStuff();
	}
	
	
	/** this returns the list of surfaces provided in the constructor
	 * 
	 * @return ArrayList<AbstractEvenlyGriddedSurface>
	 */
	public ArrayList<RuptureSurface> getSurfaceList() {
		return surfaces;
	}
	
	
	private void computeInitialStuff() {
		
		reverseSurfTrace = new boolean[surfaces.size()];

		// determine if either of the first two sections need to be reversed
		RuptureSurface surf1 = surfaces.get(0);
		RuptureSurface surf2 = surfaces.get(1);
		double[] dist = new double[4];
		dist[0] = LocationUtils.horzDistanceFast(surf1.getFirstLocOnUpperEdge(), surf2.getFirstLocOnUpperEdge());
		dist[1] = LocationUtils.horzDistanceFast(surf1.getFirstLocOnUpperEdge(), surf2.getLastLocOnUpperEdge());
		dist[2]= LocationUtils.horzDistanceFast(surf1.getLastLocOnUpperEdge(), surf2.getFirstLocOnUpperEdge());
		dist[3] = LocationUtils.horzDistanceFast(surf1.getLastLocOnUpperEdge(), surf2.getLastLocOnUpperEdge());
		
		double min = dist[0];
		int minIndex = 0;
		for(int i=1; i<4;i++) {
			if(dist[i]<min) {
				minIndex = i;
				min = dist[i];
			}
		}

		if(D) {
			for(int i=0;i<4;i++)
				System.out.println("\t"+i+"\t"+dist[i]);
			if(D) System.out.println("minIndex="+minIndex);
		}
		if(minIndex==0) { // first_first
			reverseSurfTrace[0] = true;
			reverseSurfTrace[1] = false;
		}
		else if (minIndex==1) { // first_last
			reverseSurfTrace[0] = true;
			reverseSurfTrace[1] = true;
		}
		else if (minIndex==2) { // last_first
			reverseSurfTrace[0] = false;
			reverseSurfTrace[1] = false;
		}
		else { // minIndex==3 // last_last
			reverseSurfTrace[0] = false;
			reverseSurfTrace[1] = true;
		}

		// determine which subsequent sections need to be reversed
		for(int i=1; i< surfaces.size()-1; i++) {
			surf1 = surfaces.get(i);
			surf2 = surfaces.get(i+1);
			double d1 = LocationUtils.horzDistanceFast(surf1.getLastLocOnUpperEdge(), surf2.getFirstLocOnUpperEdge());
			double d2 = LocationUtils.horzDistanceFast(surf1.getLastLocOnUpperEdge(), surf2.getLastLocOnUpperEdge());
			if(d1<d2)
				reverseSurfTrace[i+1] = false;
			else
				reverseSurfTrace[i+1] = true;
		}
		
		// compute average dip (wt averaged by area) & total area
		aveDip = 0;
		totArea=0;
		for(int s=0; s<surfaces.size();s++) {
			RuptureSurface surf = surfaces.get(s);
			double area = surf.getArea();
			totArea += area;
			if(reverseSurfTrace[s])
				aveDip += (180-surf.getAveDip())*area;
			else
				aveDip += surf.getAveDip()*area;
		}
		aveDip /= totArea;  // wt averaged by area
		if(aveDip > 90.0) {
			aveDip = 180-aveDip;
			reverseOrderOfSurfaces = true;
		}
		
		if(D) {
			System.out.println("aveDip="+(float)aveDip);
			System.out.println("reverseOrderOfSurfaces="+reverseOrderOfSurfaces);
			for(int i=0;i<reverseSurfTrace.length;i++)
				System.out.println("reverseSurfTrace "+i+" = "+reverseSurfTrace[i]);
		}
	}
	

	@Override
	public double getArea() {
		return totArea;
	}

	@Override
	public double getAveDip() {
		return aveDip;
	}

	@Override
	/**
	 * This returns getUpperEdge().getDipDirection() 
	 */
	public double getAveDipDirection() {
		return this.getUpperEdge().getDipDirection();
	}

	@Override
	/**
	 * This computes the grid spacing wt-averaged by area
	 */
	public double getAveGridSpacing() {
		if(aveGridSpacing == -1) {
			aveGridSpacing = 0;
			for(RuptureSurface surf: surfaces) {
				aveGridSpacing += surf.getAveGridSpacing()*surf.getArea();
			}
			aveGridSpacing /= getArea();
		}
		return aveGridSpacing;
	}

	@Override
	/**
	 * This sums the lengths of the given surfaces
	 */
	public double getAveLength() {
		if(aveLength == -1) {
			aveLength = 0;
			for(RuptureSurface surf: surfaces) {
				aveLength += surf.getAveLength();
			}
		}
		return aveLength;
	}

	@Override
	/**
	 * This returns the area-wt-averaged rup-top depths of the given surfaces
	 */
	public double getAveRupTopDepth() {
		if(aveRupTopDepth == -1) {
			aveRupTopDepth = 0;
			for(RuptureSurface surf: surfaces) {
				aveRupTopDepth += surf.getAveRupTopDepth()*surf.getArea();
			}
			aveRupTopDepth /= getArea();
		}
		return aveRupTopDepth;
	}

	@Override
	/**
	 * This returns getUpperEdge().getAveStrike()
	 */
	public double getAveStrike() {
		return getUpperEdge().getAveStrike();
	}

	@Override
	/**
	 * This returns the area-wt-averaged width of the given surfaces
	 */
	public double getAveWidth() {
		if(aveWidth == -1) {
			aveWidth = 0;
			for(RuptureSurface surf: surfaces) {
				aveWidth += surf.getAveWidth()*surf.getArea();
			}
			aveWidth /= getArea();
		}
		return aveWidth;
	}
	
	/**
	 * This computes distanceJB, distanceRup, and distanceSeis as the least values
	 * among the given surfaces, respectively.
	 * @param siteLoc
	 */
	private void computeDistances() {
		distanceJB = Double.MAX_VALUE;
		distanceSeis = Double.MAX_VALUE;
		distanceRup = Double.MAX_VALUE;
		double dist;
		for(RuptureSurface surf: surfaces) {
			dist = surf.getDistanceJB(siteLocForDistCalcs);
			if(dist<distanceJB) distanceJB=dist;
			dist = surf.getDistanceRup(siteLocForDistCalcs);
			if(dist<distanceRup) distanceRup=dist;
			dist = surf.getDistanceSeis(siteLocForDistCalcs);
			if(dist<distanceSeis) distanceSeis=dist;
		}
	}

	@Override
	public double getDistanceJB(Location siteLoc) {
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			computeDistances();
		}
		return distanceJB;
	}

	@Override
	public double getDistanceRup(Location siteLoc) {
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			computeDistances();
		}
		return distanceRup;
	}

	@Override
	public double getDistanceSeis(Location siteLoc) {
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			computeDistances();
		}
		return distanceSeis;
	}

	@Override
	public double getDistanceX(Location siteLoc) {
		if(!siteLocForDistXCalc.equals(siteLoc)) {
			siteLocForDistXCalc = siteLoc;
			distanceX = GriddedSurfaceUtils.getDistanceX(getEvenlyDiscritizedUpperEdge(), siteLocForDistCalcs);
		}
		return distanceX;
	}
	
	@Override
	/**
	 * This simply adds what's returned from the getEvenlyDiscritizedListOfLocsOnSurface() 
	 * method of each surface to a big master list.
	 */
	public LocationList getEvenlyDiscritizedListOfLocsOnSurface() {
		LocationList locList = new LocationList();
		for(RuptureSurface surf:surfaces) {
			locList.addAll(surf.getEvenlyDiscritizedListOfLocsOnSurface());
		}
		return locList;
	}

	@Override
	public LocationList getEvenlyDiscritizedPerimeter() {
		LocationList perimeter = new LocationList();
		// add the upper edge
		perimeter.addAll(getEvenlyDiscritizedUpperEdge());
		// make the lower edge
		if(reverseOrderOfSurfaces) {
			for(int s=0; s<surfaces.size(); s++) {
				RuptureSurface surf = surfaces.get(s);
				FaultTrace trace = surf.getEvenlyDiscritizedUpperEdge(); 
				if(reverseSurfTrace[s]) { // start at the beginning
					for(int c=trace.size()-1; c>=0 ; c--)
						perimeter.add(trace.get(c));					
				}
				else { // start at the end
					for(int c=0; c< trace.size(); c++)
						perimeter.add(trace.get(c));
				}
			}
		}
		else { // no reverse order of surfaces; start at last surface
			for(int s=surfaces.size()-1; s>=0; s--) {
				RuptureSurface surf = surfaces.get(s);
				FaultTrace trace = surf.getEvenlyDiscritizedUpperEdge(); 
				if(reverseSurfTrace[s]) { // start at the beginning
					for(int c=0; c< trace.size(); c++)
						perimeter.add(trace.get(c));
				}
				else { // start at the end
					for(int c=trace.size()-1; c>=0 ; c--)
						perimeter.add(trace.get(c));					
				}
			}
		}
		return perimeter;
	}

	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		FaultTrace evenUpperEdge = new FaultTrace(null);
			if(reverseOrderOfSurfaces) {
				for(int s=surfaces.size()-1; s>=0;s--) {
					FaultTrace trace = surfaces.get(s).getEvenlyDiscritizedUpperEdge();
					if(reverseSurfTrace[s]) {
						for(int i=0; i<trace.size();i++)
							evenUpperEdge.add(trace.get(i));
					}
					else {
						for(int i=trace.size()-1; i>=0;i--)
							evenUpperEdge.add(trace.get(i));
					}
				}				
			}
			else { // don't reverse order of surfaces
				for(int s=0; s<surfaces.size();s++) {
					FaultTrace trace = surfaces.get(s).getEvenlyDiscritizedUpperEdge();
					if(reverseSurfTrace[s]) {
						for(int i=trace.size()-1; i>=0;i--)
							evenUpperEdge.add(trace.get(i));
					}
					else {
						for(int i=0; i<trace.size();i++)
							evenUpperEdge.add(trace.get(i));
					}
				}
			}
		return evenUpperEdge;
	}

	@Override
	public Location getFirstLocOnUpperEdge() {
		return getUpperEdge().get(0);
	}
	

	@Override
	public Location getLastLocOnUpperEdge() {
		return getUpperEdge().get(getUpperEdge().size()-1);
	}

	@Override
	public double getFractionOfSurfaceInRegion(Region region) {
		LocationList locList = getEvenlyDiscritizedListOfLocsOnSurface();
		double numInside = 0;
		for(Location loc: locList)
			if(region.contains(loc))
				numInside += 1;
		return numInside/(double)locList.size();
	}

	@Override
	public String getInfo() {
		throw new RuntimeException("Need to implement this method");
	}

	@Override
	public ListIterator<Location> getLocationsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocationList getPerimeter() {
		return getEvenlyDiscritizedPerimeter();
	}

	@Override
	/**
	 * Should we remove adjacent points that are very close to each other
	 */
	public FaultTrace getUpperEdge() {
		if(upperEdge == null) {
			upperEdge = new FaultTrace(null);
			if(reverseOrderOfSurfaces) {
				for(int s=surfaces.size()-1; s>=0;s--) {
					FaultTrace trace = surfaces.get(s).getUpperEdge();
					if(reverseSurfTrace[s]) {
						for(int i=0; i<trace.size();i++)
							upperEdge.add(trace.get(i));
					}
					else {
						for(int i=trace.size()-1; i>=0;i--)
							upperEdge.add(trace.get(i));
					}
				}				
			}
			else { // don't reverse order of surfaces
				for(int s=0; s<surfaces.size();s++) {
					FaultTrace trace = surfaces.get(s).getUpperEdge();
					if(reverseSurfTrace[s]) {
						for(int i=trace.size()-1; i>=0;i--)
							upperEdge.add(trace.get(i));
					}
					else {
						for(int i=0; i<trace.size();i++)
							upperEdge.add(trace.get(i));
					}
				}
			}
		}
		return upperEdge;
	}

	@Override
	public boolean isPointSurface() {
		return false;
	}
	
	
	/**
	 * This returns the minimum distance as the minimum among all location
	 * pairs between the two surfaces
	 * @param surface RuptureSurface 
	 * @return distance in km
	 */
	@Override
	public double getMinDistance(RuptureSurface surface) {
		return GriddedSurfaceUtils.getMinDistanceBetweenSurfaces(surface, this);
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// this get's the DB accessor (version 3)
	    System.out.println("Accessing database...");
	    DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
	    PrefFaultSectionDataDB_DAO faultSectionDB_DAO = new PrefFaultSectionDataDB_DAO(db);
	    List<FaultSectionPrefData> sections = faultSectionDB_DAO.getAllFaultSectionPrefData();
//	    for(int s=0; s<sections.size();s++) {
//	    	FaultSectionPrefData data = sections.get(s);
//	    	System.out.println(s+"\t"+data.getName());
//	    }
	    System.out.println("Done accessing database.");

	    
	    SimpleFaultData sierraMadre = sections.get(367).getSimpleFaultData(true);
//	    System.out.println(sierraMadre.getFaultTrace());
//	    sierraMadre.getFaultTrace().reverse();
//	    System.out.println(sierraMadre.getFaultTrace());
	    SimpleFaultData cucamonga = sections.get(74).getSimpleFaultData(true);
//	    System.out.println(cucamonga.getFaultTrace());
	    cucamonga.getFaultTrace().reverse();
//	    System.out.println(cucamonga.getFaultTrace());
	    SimpleFaultData sanJacintoSanBer = sections.get(332).getSimpleFaultData(true);
	    sanJacintoSanBer.getFaultTrace().reverse();
	    System.out.println(sierraMadre.getFaultTrace().getName());
	    System.out.println(cucamonga.getFaultTrace().getName());
	    System.out.println(sanJacintoSanBer.getFaultTrace().getName());
	    ArrayList<RuptureSurface> surfList = new ArrayList<RuptureSurface>();
	    surfList.add(new StirlingGriddedSurface(sierraMadre,1.0));
	    surfList.add(new StirlingGriddedSurface(cucamonga,1.0));
	    surfList.add(new StirlingGriddedSurface(sanJacintoSanBer,1.0));
	    
	    CompoundRuptureSurface compoundSurf = new CompoundRuptureSurface(surfList);
	    
	    System.out.println("aveDipDir="+compoundSurf.getAveDipDirection());
	    System.out.println("aveStrike="+compoundSurf.getAveStrike());
	    System.out.println("aveGridSpacing="+compoundSurf.getAveGridSpacing());
	    System.out.println("aveArea="+compoundSurf.getArea()+"\t(should be "+(surfList.get(0).getArea()+
	    		surfList.get(1).getArea()+surfList.get(2).getArea())+")");
	    System.out.println("aveLength="+compoundSurf.getAveLength()+"\t("+surfList.get(0).getAveLength()+"+"+
	    		surfList.get(1).getAveLength()+"+"+surfList.get(2).getAveLength()+")");
	    System.out.println("aveWidth="+compoundSurf.getAveWidth()+"\t("+surfList.get(0).getAveWidth()+"\t"+
	    		surfList.get(1).getAveWidth()+"\t"+surfList.get(2).getAveWidth()+")");
	    System.out.println("aveRupTopDepth="+compoundSurf.getAveRupTopDepth()+"\t("+surfList.get(0).getAveRupTopDepth()+"\t"+
	    		surfList.get(1).getAveRupTopDepth()+"\t"+surfList.get(2).getAveRupTopDepth()+")");
	    
	    System.out.println("first loc: "+compoundSurf.getFirstLocOnUpperEdge());
	    System.out.println("last loc: "+compoundSurf.getLastLocOnUpperEdge());
	    
//	    System.out.println("sm_lat\tsm_lon\tsm_dep");
//	    for(Location loc:surfList.get(0).getEvenlyDiscritizedListOfLocsOnSurface())
//		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
//	    System.out.println("c_lat\tc_lon\tc_dep");
//	    for(Location loc:surfList.get(1).getEvenlyDiscritizedListOfLocsOnSurface())
//		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
//	    System.out.println("sj_lat\tsj_lon\tsj_dep");
//	    for(Location loc:surfList.get(2).getEvenlyDiscritizedListOfLocsOnSurface())
//		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());

	    System.out.println("tr_lat\ttr_lon\ttr_dep");
	    for(Location loc:compoundSurf.getUpperEdge())
		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());

	    System.out.println("tr2_lat\ttr2_lon\ttr2_dep");
	    for(Location loc:compoundSurf.getEvenlyDiscritizedUpperEdge())
		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());

	    System.out.println("per_lat\tper_lon\tper_dep");
	    for(Location loc:compoundSurf.getEvenlyDiscritizedPerimeter())
		    System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
    
	    System.out.println("done");

	}


}
