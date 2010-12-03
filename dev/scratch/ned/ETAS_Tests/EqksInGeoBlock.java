package scratch.ned.ETAS_Tests;

import java.util.ArrayList;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;

public class EqksInGeoBlock {
	
	double minLat, maxLat, minLon,maxLon, minDepth, maxDepth;
	EqkRupForecast erf;
	ArrayList<Integer> srcIndices;     // this stores the source index for each rupture that nucleates inside the block
	ArrayList<Integer> srcRupIndices;  // this stores the ruptures index (inside the source) for each rupture that nucleates inside the block
	ArrayList<Double> ratesInside;			   // this holds the nucleation rate for each rupture inside the block
	ArrayList<Double> fractInside;			   // this holds the fraction of the rupture that's inside the block
	ArrayList<Double> mag;
	Location blockCenterLoc;
	double totalRateInside = -1;

	
	public EqksInGeoBlock(double minLat, double maxLat, double minLon, double maxLon, 
			double minDepth, double maxDepth) {
		this.minLat=minLat;
		this.maxLat=maxLat;
		this.minLon=minLon;
		this.maxLon=maxLon;
		this.minDepth=minDepth;
		this.maxDepth=maxDepth;
		srcIndices = new ArrayList<Integer>();
		srcRupIndices = new ArrayList<Integer>();
		ratesInside = new ArrayList<Double>();
		fractInside = new ArrayList<Double>();
		mag = new ArrayList<Double>();
		blockCenterLoc = new Location((minLat+maxLat)/2,(minLon+maxLon)/2,(minDepth+maxDepth)/2);
		
	}
	
	
	public EqksInGeoBlock(Location blockCenerLoc, double latLonBlockWidth, double minDepth, double maxDepth) {
		this(blockCenerLoc.getLatitude()-latLonBlockWidth/2, 
				blockCenerLoc.getLatitude()+latLonBlockWidth/2, 
				blockCenerLoc.getLongitude()-latLonBlockWidth/2,
				blockCenerLoc.getLongitude()+latLonBlockWidth/2, 
				minDepth, maxDepth);
	}

	
	
	public EqksInGeoBlock(double minLat, double maxLat, double minLon, double maxLon, 
			double minDepth, double maxDepth, EqkRupForecast erf) {
		
		this(minLat, maxLat, minLon, maxLon, minDepth, maxDepth);
		this.erf=erf;
		processERF();
		
	}
	
	public Location getBlockCenterLoc() {return blockCenterLoc;}
	
	
	public void writeResults() {
		for(int i=0; i<srcIndices.size();i++) {
			System.out.print(i+"\t"+srcIndices.get(i)+"\t"+srcRupIndices.get(i)+"\t"+
					ratesInside.get(i)+"\t"+fractInside.get(i)+"\t"+
					mag.get(i));
			if(erf != null)
				System.out.print("\t"+erf.getSource(srcIndices.get(i)).getName()+"\n");
			else
				System.out.print("\n");
		}
	}
	
	public double getTotalRateInside() {
		if(totalRateInside == -1) {
			totalRateInside=0;
			for(Double rate:this.ratesInside) totalRateInside += rate;
		}
		return totalRateInside;
	}
	
	
	
	public void processERF() {
		int numSrc = erf.getNumSources();
		double forecastDuration = erf.getTimeSpan().getDuration();
		for(int s=0;s<numSrc;s++) {
			int numRups = erf.getNumRuptures(s);
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = erf.getRupture(s, r);
				processRupture(rup, s, r, forecastDuration);
			}
		}
	}
	
	
	/**
	 * This processes a given rupture
	 * @param rup
	 * @param srcIndex
	 * @param rupIndex
	 * @param forecastDuration
	 */
	public void processRupture(ProbEqkRupture rup, int srcIndex, int rupIndex, double forecastDuration) {
		double rate = rup.getMeanAnnualRate(forecastDuration);
		if(rate > 0) {
			LocationList locList = rup.getRuptureSurface().getLocationList();
			int numLoc = locList.size();
			int numLocInside = 0;
			for(Location loc : locList)
				if(isLocInside(loc)) numLocInside+=1;
			if(numLocInside>0) {
				fractInside.add((double)numLocInside/(double)numLoc);
				ratesInside.add((double)rate*(double)numLocInside/(double)numLoc);
				srcIndices.add(srcIndex);
				srcRupIndices.add(rupIndex);
				mag.add(rup.getMag());
			}			
		}
	}
	
	
	public void processRate(double rate, double fracInside, int srcIndex, int rupIndex, double magnitude) {
		if(rate > 0) {
			fractInside.add(fracInside);
			ratesInside.add(rate);
			srcIndices.add(srcIndex);
			srcRupIndices.add(rupIndex);
			mag.add(magnitude);
		}			
	}

	
	/**
	 * This divides the block into equal-sized sub-blocks, where the original block is sliced in all three
	 * dimensions, making the final number of blocks = numAlongLatLon*numAlongLatLon*numAlongDepth 
	 * 
	 * Important: this assumes that any point sources within the block should be equally divided among
	 * sub blocks.
	 * @param numAlongLatLon
	 * @param numAlongDepth
	 * @return
	 */
	public ArrayList<EqksInGeoBlock> getSubBlocks(int numAlongLatLon, int numAlongDepth, EqkRupForecast erf) {
		ArrayList<EqksInGeoBlock> subBlocks = new ArrayList<EqksInGeoBlock>();
		double forecastDuration = erf.getTimeSpan().getDuration();
		int numSubBlocks = numAlongLatLon*numAlongLatLon*numAlongDepth;
		for(int latSlice=0; latSlice<numAlongLatLon; latSlice++)
			for(int lonSlice=0; lonSlice<numAlongLatLon; lonSlice++)
				for(int depSlice=0; depSlice<numAlongDepth; depSlice++) {
					double subMinLat = minLat+latSlice*(maxLat-minLat)/(double)numAlongLatLon;
					double subMaxLat = subMinLat+(maxLat-minLat)/(double)numAlongLatLon;
					double subMinLon = minLon+lonSlice*(maxLon-minLon)/(double)numAlongLatLon;
					double subMaxLon = subMinLon+(maxLon-minLon)/(double)numAlongLatLon;
					double subMinDepth = minDepth+depSlice*(maxDepth-minDepth)/(double)numAlongDepth;
					double subMaxDepth = subMinDepth+(maxDepth-minDepth)/(double)numAlongDepth;
					EqksInGeoBlock subBlock = new EqksInGeoBlock(subMinLat, subMaxLat, subMinLon, subMaxLon, subMinDepth, subMaxDepth);
					for(int r=0; r<getNumRupsInside();r++) {
						int iSrc = srcIndices.get(r);
						int iRup = srcRupIndices.get(r);
						ProbEqkRupture rup = erf.getRupture(iSrc, iRup);
						if(rup.getRuptureSurface().size() > 1)
							subBlock.processRupture(rup, iSrc, iRup, forecastDuration);	
						else { // assume point sources equally divided
							double rate = rup.getMeanAnnualRate(forecastDuration)/numSubBlocks;
							double fracInside = 1/numSubBlocks;
							subBlock.processRate(rate, fracInside, iSrc, iRup, rup.getMag());
						}
							
					}
					subBlocks.add(subBlock);
				}
		
		// check total rates
		double totRate=0;
		for(int b=0; b<subBlocks.size();b++) {
			EqksInGeoBlock block = subBlocks.get(b);
			double rate=block.getTotalRateInside();
			totRate += rate;
//			System.out.println("/nBlock "+b+" rate = "+rate);
		}
//		System.out.println("/nRate Check: "+totRate+" vs "+this.getTotalRateInside());
		
		return subBlocks;
	}
	
	public int getNumRupsInside() {
		return this.srcIndices.size();
	}
	

	private boolean isLocInside(Location loc) {
		if(     loc.getLatitude()>=minLat && loc.getLatitude()< maxLat && 
				loc.getLongitude()>=minLon && loc.getLongitude()< maxLon &&
				loc.getDepth()>=minDepth && loc.getDepth()<maxDepth)
			return true;
		else
			return false;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long startTime=System.currentTimeMillis();

		// Create UCERF2 instance
		int duration = 1;
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		
		double runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("ERF instantiation took "+runtime+" seconds");


		startTime=System.currentTimeMillis();
		EqksInGeoBlock eqksInGeoBlock = new EqksInGeoBlock(33.7-0.05, 33.7+0.05, -116.1-0.05, 
				-116.1+0.05, 0, 16, meanUCERF2);
		
		runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("processing ERF  took "+runtime+" seconds");
		
		eqksInGeoBlock.writeResults();	
		System.out.println("Total Rate Inside = "+eqksInGeoBlock.getTotalRateInside());
		
		
		// Do 2 sub blocks
		startTime=System.currentTimeMillis();
		eqksInGeoBlock.getSubBlocks(2,2,meanUCERF2);
		runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("2 slices sub blocks took "+runtime+" seconds");

		// Do 4 sub blocks
		startTime=System.currentTimeMillis();
		eqksInGeoBlock.getSubBlocks(2,2,meanUCERF2);
		runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("4 slices sub blocks took "+runtime+" seconds");


	}

}
