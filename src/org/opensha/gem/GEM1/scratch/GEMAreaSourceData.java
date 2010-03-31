package org.opensha.gem.GEM1.scratch;

import java.io.BufferedWriter;
import java.io.IOException;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.griddedForecast.MagFreqDistsForFocalMechs;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;

import org.opensha.sha.util.TectonicRegionType;

public class GEMAreaSourceData extends GEMSourceData {

	// this defines the geometry (border) of the region
	private Region reg;
	// this holds the MagFreqDists, FocalMechs, and location.
	private MagFreqDistsForFocalMechs magfreqDistFocMech;
	// the following specifies the average depth to top of rupture as a function of magnitude.
	private ArbitrarilyDiscretizedFunc aveRupTopVsMag;
	// the following is used to locate small sources (i.e., for all mags lower than the minimum mag in aveRupTopVsMag)
	private double aveHypoDepth;
	
	/**
	 * This is the constructor for the GEMAreaSourceData class. It takes as input parameters the ID 
	 * of the source, its name and <code>TectonicRegion</code> definition, the <code>Region</code> 
	 * bordering the area, a <code>MagFreqDistsForFocalMechs</code> object containing the MFD and 
	 * the focal mechanism of the fault families within the source,   
	 * 
	 * @param id
	 * @param name
	 * @param tectReg
	 * @param reg
	 * @param magfreqDistFocMech
	 * @param aveRupTopVsMag
	 * @param aveHypoDepth
	 */
	public GEMAreaSourceData(String id, String name, TectonicRegionType tectReg, 
			Region reg, MagFreqDistsForFocalMechs magfreqDistFocMech, 
			ArbitrarilyDiscretizedFunc aveRupTopVsMag, double aveHypoDepth){
		this.id = id;
		this.name = name;
		this.tectReg = tectReg;
		this.reg = reg;
		this.magfreqDistFocMech = magfreqDistFocMech;
		this.aveRupTopVsMag = aveRupTopVsMag;
		this.aveHypoDepth = aveHypoDepth;
	}
	
	public Region getRegion() {
		return this.reg;
	}

	public double getMagfreqDistFocMech(int i) {
		return this.getMagfreqDistFocMech(i);
	}
	
	public MagFreqDistsForFocalMechs getMagfreqDistFocMech() {
		return this.magfreqDistFocMech;
	}

	public ArbitrarilyDiscretizedFunc getAveRupTopVsMag() {
		return this.aveRupTopVsMag;
	}

	public double getAveHypoDepth() {
		return this.aveHypoDepth;
	}
	
	/**
	 * This computes an approximate extension of the area. 
	 * @return area
	 */
	public double getArea(){
		
		double area = 0.0;
		double grdSpacing = 0.005;
		double earthRadiusEquator = 6378.1370;
		double earthRadiusPole = 6356.7523;
		double oldLat = 0.0;
		double tmpArea = 0.0;
		double tmpRadius = 0.0;
		
		// Gridding the region
		GriddedRegion grd = new GriddedRegion(this.reg,grdSpacing,null);
		
		// Mean radius
		double meanRadius = (2*earthRadiusEquator + earthRadiusPole) / 3.0;
		
		// Computing the area
		for (Location loc: grd.getNodeList()){
			double tmpLat = loc.getLatitude();
			if ( Math.abs(tmpLat-oldLat) > grdSpacing/10) {
				double tmpLatRad = tmpLat / 180 * Math.PI;
				double tmpNum = Math.pow(earthRadiusEquator*earthRadiusEquator*Math.cos(tmpLatRad),2) +
					Math.pow(earthRadiusPole*earthRadiusPole*Math.sin(tmpLatRad),2);
				double tmpDen = Math.pow(earthRadiusEquator*Math.cos(tmpLatRad),2) +
					Math.pow(earthRadiusPole*Math.sin(tmpLatRad),2);
				tmpRadius = Math.sqrt(tmpNum/tmpDen);
				
				tmpArea = Math.pow(grdSpacing/360*2.0*Math.PI,2) * tmpRadius * 
					Math.sin(Math.PI-tmpLatRad) * meanRadius; 
				area += tmpArea;
				oldLat = tmpLat;
			} else {
				area += tmpArea;
			}
		}
		System.out.println("Area:"+area);
		return area;
		
	}
	
	/**
	 * 
	 * @param buf
	 * @throws IOException
	 */
	public void writeXML(BufferedWriter buf) throws IOException{
		String prefix = "ns4:";
		String prefix1 = "ns3:";
		
		// Write the geometry
		buf.write(String.format("<%sSource>\n",prefix));
		buf.write(String.format("<%sArea>\n",prefix));
		buf.write(String.format("\t<%sPolygon>\n",prefix));
		buf.write(String.format("\t\t<%sexterior>\n",prefix1));
		buf.write(String.format("\t\t\t<%sLinearRing>\n",prefix1));
		buf.write(String.format("\t\t\t\t<%sposList srsDimension=\"2\" count=\"%d\">",
				prefix1,
				this.reg.getBorder().size()));
		LocationList border = this.reg.getBorder();
		for (int i = 0; i < border.size(); i++){
			buf.write(String.format("%.4f %.4f ",
					border.getLocationAt(i).getLongitude(),
					border.getLocationAt(i).getLatitude()
					));
		} 
		buf.write(String.format("</%sposList>\n",prefix1));
		buf.write(String.format("\t\t\t</%sLinearRing>\n",prefix1));
		buf.write(String.format("\t\t</%sexterior>\n",prefix1));
		buf.write(String.format("\t</%sPolygon>\n",prefix));
		
		// Write the MFD
		buf.write(String.format("\t<%sHypoRateModelList>\n",prefix));
		buf.write(String.format("\t\t<%sModel mMax=\"%.2f\">\n",
				prefix,
				this.magfreqDistFocMech.getMagFreqDist(0).getMaxX()+
				this.magfreqDistFocMech.getMagFreqDist(0).getDelta()/2 ));
		buf.write(String.format("\t\t\t<%sParameters>\n",prefix));
		buf.write(String.format("\t\t\t\t<%sMagFreqDist>\n",prefix));
		buf.write(String.format("\t\t\t\t\t<%sEvenlyDiscretized binSize=\"%.2f\" minVal=\"5.0\" binCount=\"%d\"> \n",
				prefix,
				this.magfreqDistFocMech.getMagFreqDist(0).getDelta(),
				this.magfreqDistFocMech.getMagFreqDist(0).getNum()));
		buf.write(String.format("\t\t\t\t\t\t<DistributionValues xmlns=\"\">"));
		for (int j=0; j < this.magfreqDistFocMech.getMagFreqDist(0).getNum(); j++){
			buf.write(String.format("%.3f %.5e ",
					this.magfreqDistFocMech.getMagFreqDist(0).get(j).getX(),
					this.magfreqDistFocMech.getMagFreqDist(0).get(j).getY()));
		}
		buf.write(String.format("\n\t\t\t\t\t\t</DistributionValues>\n"));
		buf.write(String.format("\t\t\t\t\t</%sEvenlyDiscretized>\n",prefix));
		buf.write(String.format("\t\t\t\t</%sMagFreqDist>\n",prefix));
		if (this.getMagfreqDistFocMech().getFocalMechanismList().length > 0){
			buf.write(String.format("\t\t\t\t<%sFocalMech dip=\"90.0\" rake=\"90.0\" strike=\"0.0\"/>\n",
					prefix));
			buf.write(String.format("\t\t\t\t\t<%sRupTopDist>\n",prefix));
			buf.write(String.format("\t\t\t\t\t\t<DistributionValues xmlns=\"\">"));
			for (int j=0; j < this.getAveRupTopVsMag().getNum(); j++){
				buf.write(String.format("%.3f %.5e ",
						this.getAveRupTopVsMag().getX(j),
						this.getAveRupTopVsMag().getY(j) ));
			}
			buf.write(String.format("\n\t\t\t\t\t\t</DistributionValues>"));
			buf.write(String.format("\n\t\t\t\t\t</%sRupTopDist>\n",prefix));
		}
		buf.write(String.format("\t\t\t</%sParameters>\n",prefix));
		buf.write(String.format("\t\t</%sModel>\n",prefix));
		buf.write(String.format("\t</%sHypoRateModelList>\n",prefix));	

		buf.write(String.format("</%sArea>\n",prefix));
		buf.write(String.format("</%sSource>\n",prefix));
	}
}
