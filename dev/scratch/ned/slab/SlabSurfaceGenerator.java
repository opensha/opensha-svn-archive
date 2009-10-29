package scratch.ned.slab;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.GMT_GrdFile;
import org.opensha.sha.faultSurface.ApproxEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.commons.util.FaultTraceUtils;

import ucar.ma2.InvalidRangeException;

/**
 * This class generates surfaces from the subduction-zone data at http://geohazards.cr.usgs.gov/staffweb/ghayes/Site/Slab1.0.html
 * 
 * Can't get the version that reads URLs directly to work
 * 
 * Some edge-point z-values get extrapolated because they're not within the grd file
 * @author field
 *
 */
public class SlabSurfaceGenerator {
	
//	  public ApproxEvenlyGriddedSurface getGriddedSurface(String topTraceFilename, String bottomTraceFilename, String grdSurfaceFilename, double aveGridSpaceing) {
		  public ApproxEvenlyGriddedSurface getGriddedSurface(String clipFilename, String grdSurfaceFilename, double aveGridSpacing) {

		  /**/
		  ArrayList<FaultTrace> traces = this.getTopAndBottomTrace(clipFilename);
		  FaultTrace origTopTrace = traces.get(0);
		  FaultTrace origBottomTrace = traces.get(1);

//		  FaultTrace origTopTrace = readTraceFiles(topTraceFilename);
//		  FaultTrace origBottomTrace = readTraceFiles(bottomTraceFilename);

		  
		  // Reverse the order of the bottom trace
		  origBottomTrace.reverse();
		  
		  // Now check that Aki-Richards convention is adhered to (fault dips to right)
		  double dipDir = RelativeLocation.getAzimuth(origTopTrace.getLocationAt(0), origBottomTrace.getLocationAt(0));
		  double strikeDir = origTopTrace.getStrikeDirection();
		  if((strikeDir-dipDir) <0 ||  (strikeDir-dipDir) > 180) {
			  origTopTrace.reverse();
			  origBottomTrace.reverse();
			  System.out.println("reversed trace order to adhere to Aki Richards");
		  }
		  
		  // now compute num subsection of trace
		  double aveTraceLength = (origTopTrace.getTraceLength()+origBottomTrace.getTraceLength())/2;
		  int num = (int) Math.round(aveTraceLength/aveGridSpacing);
		  
		  // get resampled traces
		  FaultTrace resampTopTrace = FaultTraceUtils.resampleTrace(origTopTrace, num);
		  FaultTrace resampBottomTrace = FaultTraceUtils.resampleTrace(origBottomTrace, num);

		  /*  The following doesn't work
		  ArrayList<FaultTrace> tracesToPlot = new ArrayList<FaultTrace>();
		  tracesToPlot.add(origTopTrace);
		  tracesToPlot.add(origBottomTrace);
		  tracesToPlot.add(resampTopTrace);
		  tracesToPlot.add(resampBottomTrace);
//		  tracesToPlot.add(getClipTrace(clipFilename));
		  FaultTraceUtils.plotTraces(tracesToPlot);
		*/
		  
		  // compute ave num columns
		  double aveDist=0;
		  for(int i=0; i<resampTopTrace.size(); i++) {
			  Location topLoc = resampTopTrace.getLocationAt(i);
			  Location botLoc = resampBottomTrace.getLocationAt(i);
			  aveDist += RelativeLocation.getHorzDistance(topLoc, botLoc);
		  }
		  aveDist /= resampTopTrace.size();
		  int nRows = (int) Math.round(aveDist/aveGridSpacing)+1;
		  
		  // create the surface object that will be returned
		  ApproxEvenlyGriddedSurface surf = new ApproxEvenlyGriddedSurface(nRows, resampTopTrace.size(), aveGridSpacing);
		  
		  // open the surface grd data file (used for setting depths)
		  GMT_GrdFile grdSurfData=null;
		  try {
//			grdSurfData = new GMT_GrdFile(new URI("http://geohazards.cr.usgs.gov/staffweb/ghayes/Site/Slab1.0_files/sam_slab1.0_clip.grd"));

			grdSurfData = new GMT_GrdFile(this.getClass().getResource("/"+grdSurfaceFilename).toURI());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(grdSurfData.getNumX()+"\t"+grdSurfData.getNumY());
		  

		// now set the surface locations
		int numNaN=0;
		for(int i=0; i<resampTopTrace.size(); i++) {
			Location topLoc = resampTopTrace.getLocationAt(i);
			Location botLoc = resampBottomTrace.getLocationAt(i);
			double length = RelativeLocation.getHorzDistance(topLoc, botLoc);
			double subSectLen = length/(nRows-1);
			Direction dir = RelativeLocation.getDirection(topLoc, botLoc);
			//System.out.println("length1="+length+"\tlength2="+dir.getHorzDistance());
			dir.setVertDistance(0.0);
			for(int s=0; s< nRows; s++) {
				double dist = s*subSectLen;
				dir.setHorzDistance(dist);
				Location loc = RelativeLocation.getLocation(topLoc, dir);
				double depth= 0;
				try {
//					depth = -grdSurfData.getClosestZ(loc);  // notice the minus sign
					depth = -grdSurfData.getWtAveZ(loc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidRangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(Double.isNaN(depth)) {
					numNaN+=1;
//					System.out.println("row="+s+"\tcol="+i+"\t"+loc.getLongitude()+"\t"+loc.getLatitude());
				}
				loc.setDepth(depth);
				surf.setLocation(s, i, loc);
			}

		}
		
		
		// Fix any NaNs on the edges by linear extrapolation
		int nCols=surf.getNumCols();  // already have nRows
		for(int r=0;r<nRows;r++){
			if(Double.isNaN(surf.getLocation(r, 0).getDepth())){
				double depth = 2*surf.getLocation(r, 1).getDepth() - surf.getLocation(r, 2).getDepth();
				surf.getLocation(r, 0).setDepth(depth);
			}
			if(Double.isNaN(surf.getLocation(r, nCols-1).getDepth())){
				double depth = 2*surf.getLocation(r, nCols-2).getDepth() - surf.getLocation(r, nCols-3).getDepth();
				surf.getLocation(r, nCols-1).setDepth(depth);
			}
		}
		for(int c=0;c<nCols;c++){
			if(Double.isNaN(surf.getLocation(0, c).getDepth())){
				double depth = 2*surf.getLocation(1, c).getDepth() - surf.getLocation(2, c).getDepth();
				surf.getLocation(0, c).setDepth(depth);
			}
			if(Double.isNaN(surf.getLocation(nRows-1, c).getDepth())){
				double depth = 2*surf.getLocation(nRows-2, c).getDepth() - surf.getLocation(nRows-3, c).getDepth();
				surf.getLocation(nRows-1, c).setDepth(depth);
			}
		}
		
		//Check for any NaNs
		Iterator<Location> it = surf.getLocationsIterator();
		while (it.hasNext()) {
			Location loc = it.next();
			if(Double.isNaN(loc.getDepth())) {
				System.out.println("NaN depth encountered in SlabSurfaceGenerator; changed value to 0.0");
				loc.setDepth(0);
//				throw new RuntimeException("NaN encountered in SlabSurfaceGenerator");
			}
		}
	

		/*
		  System.out.println("numRows="+surf.getNumRows());
		  System.out.println("numCols="+surf.getNumCols());
		  System.out.println("numPoints="+surf.getNumRows()*surf.getNumCols());
		  System.out.println("numNaN="+numNaN);
		  
		  
		  System.out.println("origTopTrace length = "+origTopTrace.getTraceLength());
		  System.out.println("origBottomTrace length = "+origBottomTrace.getTraceLength());
		  System.out.println("origTopTrace strike = "+origTopTrace.getStrikeDirection());
		  System.out.println("origBottomTrace strike = "+origBottomTrace.getStrikeDirection());
		   */
		  
		  return surf;

	  }
	  
	  
	  private FaultTrace readTraceFiles(String fileName) {
		  String inputFileName = fileName;
		  FaultTrace trace = new FaultTrace(fileName);

		  try {
			  ArrayList<String> fileLines = FileUtils.loadFile(inputFileName);
			  StringTokenizer st;
			  for(int i=0; i<fileLines.size(); ++i){

				  st = new StringTokenizer( (String) fileLines.get(i));
				  double lon = Double.parseDouble(st.nextToken());
				  double lat = Double.parseDouble(st.nextToken());
				  trace.addLocation(new Location(lat,lon,0.0));
			  }
		  } catch (FileNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  return trace;

	  }

	  
	 /**
	  * This extracts the the top and bottom trace from Gavin's
	  * clip file
	  * @param fileName
	  * @return
	  */
	  private ArrayList<FaultTrace> getTopAndBottomTrace(String fileName) {
		  FaultTrace topTrace = new FaultTrace(fileName.toString());
		  FaultTrace bottomTrace = new FaultTrace(fileName.toString());
		  ArrayList<FaultTrace> traces = new ArrayList<FaultTrace>();
		  
		  boolean stillOnTopTrace = true;
		  Location lastLoc=null;

		  try {
			  ArrayList<String> fileLines = FileUtils.loadFile(this.getClass().getResource("/"+fileName));
			  StringTokenizer st;
// System.out.println("first line of file: "+fileLines.get(0));
// System.out.println("2nd to last line of file: "+fileLines.get(fileLines.size()-2));
// System.out.println("last line of file: "+fileLines.get(fileLines.size()-1));

			  // put the first point in
			  st = new StringTokenizer( (String) fileLines.get(fileLines.size()-2));
			  double lon = Double.parseDouble(st.nextToken());
			  double lat = Double.parseDouble(st.nextToken());
			  Location newLoc = new Location(lat,lon,0.0);
			  topTrace.addLocation(newLoc);
			  lastLoc = newLoc;
			  double distThresh = 0;
			  for(int i=fileLines.size()-3; i>=0; --i){ // skip the last point because it closes the polygon (1st point on the top trace)
				  st = new StringTokenizer( (String) fileLines.get(i));
				  lon = Double.parseDouble(st.nextToken());
				  lat = Double.parseDouble(st.nextToken());
				  newLoc = new Location(lat,lon,0.0);
				  if(i==fileLines.size()-3) distThresh = 3*RelativeLocation.getApproxHorzDistance(newLoc, lastLoc);  // assumes equal spacing on top
//System.out.println(fileLines.get(i)+"\t"+newLoc);
				  if(stillOnTopTrace) {
					  // check whether we've jumped to bottom trace
					  if(RelativeLocation.getApproxHorzDistance(newLoc, lastLoc) > distThresh) {
						  stillOnTopTrace = false;  // if distance is greater than 100 km, we've jumped to the bottom trace
						  bottomTrace.addLocation(newLoc);
					  }else {
						  topTrace.addLocation(newLoc);
						  lastLoc = newLoc;
					  }
				  }else{
					  bottomTrace.addLocation(newLoc);
				  }
			  }
		  } catch (FileNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  traces.add(topTrace);
		  traces.add(bottomTrace);
//System.out.println("last loc of bottom trace: "+bottomTrace.getLocationAt(bottomTrace.size()-1));

		  return traces;
	  }


	  
	/*  this was only for plotting/error check  
	  private static FaultTrace getClipTrace(String fileName) {
		  FaultTrace trace = new FaultTrace("clip region");

		  try {
			  ArrayList<String> fileLines = FileUtils.loadFile(fileName);
			  StringTokenizer st;
			  for(int i=0; i<fileLines.size(); ++i){ 
				  st = new StringTokenizer( (String) fileLines.get(i));
				  double lon = Double.parseDouble(st.nextToken());
				  double lat = Double.parseDouble(st.nextToken());
				  trace.addLocation(new Location(lat,lon));
			  }
		  } catch (FileNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }

		  return trace;
	  }
*/

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SlabSurfaceGenerator gen = new SlabSurfaceGenerator();
		/*
		ApproxEvenlyGriddedSurface surf = gen.getGriddedSurface(
				"dev/scratch/ned/slab/sam_slab1_topTrace.txt",
				"dev/scratch/ned/slab/sam_slab1_bottomTrace.txt",
				"dev/scratch/ned/slab/sam_slab1.0_clip.grd",
				100);
		*/
		/**/
		ApproxEvenlyGriddedSurface surf = gen.getGriddedSurface(
				"dev/scratch/ned/slab/sam_slab1.0.clip.txt",
				"dev/scratch/ned/slab/sam_slab1.0_clip.grd",
				50);
		surf.writeXYZ_toFile("dev/scratch/ned/slab/surfXYZ.txt");
		
		/*
		ApproxEvenlyGriddedSurface surf;
		try {
			surf = gen.getGriddedSurface(
					new URL("http://geohazards.cr.usgs.gov/staffweb/ghayes/Site/Slab1.0_files/sum_slab1.0.clip"),
					new URI("http://geohazards.cr.usgs.gov/staffweb/ghayes/Site/Slab1.0_files/sam_slab1.0_clip.grd"),
					50);
			System.out.println("DONE");
			surf.writeXYZ_toFile("dev/scratch/ned/slab/surfXYZ.txt");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

}
