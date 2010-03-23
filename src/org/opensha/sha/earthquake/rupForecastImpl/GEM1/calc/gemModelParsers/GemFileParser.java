package org.opensha.sha.earthquake.rupForecastImpl.GEM1.calc.gemModelParsers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMFaultSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.scratch.GEMAreaSourceData;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

public class GemFileParser {
	
	protected ArrayList<GEMSourceData> srcDataList;
	private static boolean INFO = false; 
	
	/**
	 * This gives the list of GEMSourceData parsed from the input ascii file
	 * @return srcDataList 
	 */
	public ArrayList<GEMSourceData> getList (){
		return this.srcDataList;
	}
	
	/**
	 * This is just for testing purposed and will be removed
	 */
	public void setList (ArrayList<GEMSourceData> lst){
		this.srcDataList = lst;
	}
	
	/**
	 * 
	 * @return number of source data objects 
	 */
	public int getNumSources(){
		return this.srcDataList.size();
	}
	
	/**
	 * This writes to a file the coordinates of the polygon. The format of the outfile is compatible 
	 * with the GMT psxy multiple segment file. The separator adopted here is the default separator 
	 * suggested in GMT (i.e. '>') 
	 *   
	 * @param file
	 * @throws IOException 
	 */
	public void writeAreaGMTfile (FileWriter file) throws IOException{
		BufferedWriter out = new BufferedWriter(file);
		
		// Search for area sources
		int idx = 0;
		for (GEMSourceData dat: srcDataList){
			if (dat instanceof GEMAreaSourceData){

				// Get the polygon vertexes
				GEMAreaSourceData src = (GEMAreaSourceData) dat;
				
				// Get polygon area 
				double area = src.getArea();
				
				// Total scalar seismic moment above m 
				double totMom = 0.0;
				double momRate = 0.0;
				double magThreshold = 5.0;

				for (IncrementalMagFreqDist mfdffm: src.getMagfreqDistFocMech().getMagFreqDistList()){
					EvenlyDiscretizedFunc momRateDist = mfdffm.getMomentRateDist();
				
					if (INFO) System.out.println("MinX "+momRateDist.getMinX()+" MaxX"+momRateDist.getMaxX());
					if (INFO) System.out.println("Mo(idx5):"+src.getMagfreqDistFocMech().getMagFreqDist(0).getMomentRate(5));
					
					for (int i=0; i < momRateDist.getNum(); i++ ){
						if (momRateDist.get(i).getX() >= magThreshold){
							totMom += momRateDist.get(i).getY();
							if (INFO) System.out.println(i+" "+momRateDist.get(i).getY());
						}
					}
					
				}
				momRate = totMom / area;
				
				// Info
				if (INFO) System.out.println(src.getID()+" "+totMom);
				
				// Write separator +
				// Scalar seismic moment rate per units of time and area above 'magThreshold'
				out.write(String.format("> -Z %6.2e idx %d \n",Math.log10(momRate),idx));
				
				// Write trace coordinates
				for (Location loc: src.getRegion().getBorder()){
					out.write(String.format("%+7.3f %+6.3f %+6.2f\n",
							loc.getLongitude(),
							loc.getLatitude(),
							loc.getDepth()));
					System.out.printf("%+7.3f %+6.3f %+6.2f\n",
							loc.getLongitude(),
							loc.getLatitude(),
							loc.getDepth());
				}
			}	
		}
		// Write separator
		out.write('>');
		out.close();
	}
	
	/**
	 * This writes the coordinates of the fault traces to a file. The format of the outfile is 
	 * compatible with the GMT psxy multiple segment file format. The separator adopted here is the 
	 * default separator suggested in GMT (i.e. '>') 
	 *   
	 * @param file
	 * @throws IOException 
	 */
	public void writeFaultGMTfile (FileWriter file) throws IOException{
		BufferedWriter out = new BufferedWriter(file);
		
		// Search for fault sources
		for (GEMSourceData dat: srcDataList){
			if (dat instanceof GEMFaultSourceData){
				// Write the trace coordinate to a file
				GEMFaultSourceData src = (GEMFaultSourceData) dat;
				// Trace length
				Double len = src.getTrace().getTraceLength();
				// Total scalar seismic moment above m 
				EvenlyDiscretizedFunc momRateDist = src.getMfd().getMomentRateDist();
				double totMom = 0.0;
				double momRate = 0.0;
				double magThreshold = 5.0;
				for (int i=0; i < momRateDist.getNum(); i++ ){
					if (momRateDist.get(i).getX() >= magThreshold){
						totMom += totMom;
					}
				}
				momRate = totMom / len;
				// Write separator
				out.write(String.format("> -Z %6.2e",Math.log10(momRate)));
				// Write trace coordinates
				for (Location loc: src.getTrace()){
					out.write(String.format("%+7.3f %+6.3f %+6.2f",
							loc.getLongitude(),
							loc.getLatitude(),
							loc.getDepth()));
				}

			}	

		}
		out.write('>');
	}
	
}
