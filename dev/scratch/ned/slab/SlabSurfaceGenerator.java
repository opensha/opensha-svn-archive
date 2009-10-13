package scratch.ned.slab;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;

public class SlabSurfaceGenerator extends EvenlyGriddedSurface {
	
	FaultTrace resampTopTrace, resampBottomTrace;
	double aveGridSpaceing;
	
	
	  public SlabSurfaceGenerator(String topTraceFilename, String bottomTraceFilename, double aveGridSpaceing) {
		  this.aveGridSpaceing=aveGridSpaceing;
		  
		  FaultTrace origTopTrace = readTraceFiles(topTraceFilename);
		  FaultTrace origBottomTrace = readTraceFiles(bottomTraceFilename);
		  
		  // Check that both are in correct order
		  double dist1 = RelativeLocation.getApproxHorzDistance(origTopTrace.getLocationAt(0), origBottomTrace.getLocationAt(0));
		  double dist2 = RelativeLocation.getApproxHorzDistance(origTopTrace.getLocationAt(origTopTrace.size()-1), origBottomTrace.getLocationAt(0));
		  if(dist2 < dist1) {
			  origTopTrace.reverse();
			  System.out.println("reversed top trace to agree with bottom trace");
		  }
		  
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
		  int num = (int) Math.round(aveTraceLength/aveGridSpaceing);
		  resampTopTrace = resampleTraces(origTopTrace, num);
		  resampBottomTrace = resampleTraces(origBottomTrace, num);
		  
		  // compute ave num columns
		  double aveDist=0;
		  for(int i=0; i<resampTopTrace.size(); i++) {
			  Location topLoc = resampTopTrace.getLocationAt(i);
			  Location botLoc = resampBottomTrace.getLocationAt(i);
			  aveDist += RelativeLocation.getHorzDistance(topLoc, botLoc);
		  }
		  aveDist /= resampTopTrace.size();
		  int nRows = (int) Math.round(aveDist/aveGridSpaceing)+1;
		  setNumRowsAndNumCols(nRows, resampTopTrace.size());
		  
		  setSurfaceLocs();
		  
		  System.out.println("numRows="+numRows);
		  System.out.println("numCols="+numCols);
		  System.out.println("numPoints="+numCols*numRows);
		  
		  /*
		  System.out.println("origTopTrace length = "+origTopTrace.getTraceLength());
		  System.out.println("origBottomTrace length = "+origBottomTrace.getTraceLength());
		  System.out.println("origTopTrace strike = "+origTopTrace.getStrikeDirection());
		  System.out.println("origBottomTrace strike = "+origBottomTrace.getStrikeDirection());
		   */
	  }
	  
	  
	  /**
	   * This sets the locations of the surface (but not the depths)
	   */
	  private void setSurfaceLocs() {
		  
		  for(int i=0; i<resampTopTrace.size(); i++) {
			  Location topLoc = resampTopTrace.getLocationAt(i);
			  Location botLoc = resampBottomTrace.getLocationAt(i);
			  double length = RelativeLocation.getHorzDistance(topLoc, botLoc);
			  double subSectLen = length/(numRows-1);
			  Direction dir = RelativeLocation.getDirection(topLoc, botLoc);
//System.out.println("length1="+length+"\tlength2="+dir.getHorzDistance());
			  dir.setVertDistance(0.0);
			  for(int s=0; s< numRows; s++) {
				  double dist = s*subSectLen;
				  dir.setHorzDistance(dist);
				  Location loc = RelativeLocation.getLocation(topLoc, dir);
				  this.setLocation(s, i, loc);
			  }

		  }
	  }

	  
	  private FaultTrace readTraceFiles(String fileName) {
		  String inputFileName = "dev/scratch/ned/slab/"+fileName;
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
	   * This resamples the trace into num subsections of equal length 
	   * (final number of points in trace is num+1).  However, note that
	   * these subsections of are equal length on the original trace, and
	   * that the final subsections will be less than that if there is curvature
	   * in the original between the points (e.g., corners getting cut).
	   * corners.
	   * @param trace
	   * @param num - number of subsections
	   * @return
	   */
	  private FaultTrace resampleTraces(FaultTrace trace, int num) {
		  double resampInt = trace.getTraceLength()/num;
		  FaultTrace resampTrace = new FaultTrace("resampled "+trace.getName());
		  resampTrace.addLocation(trace.getLocationAt(0));  // add the first location
		  double remainingLength = resampInt;
		  Location lastLoc = trace.getLocationAt(0);
		  int NextLocIndex = 1;
		  while (NextLocIndex < trace.size()) {
			  Location nextLoc = trace.getLocationAt(NextLocIndex);
			  double length = RelativeLocation.getTotalDistance(lastLoc, nextLoc);
			  if (length > remainingLength) {
				  	// set the point
				  Direction dir = RelativeLocation.getDirection(lastLoc, nextLoc);
				  dir.setHorzDistance(dir.getHorzDistance()*remainingLength/length);
				  dir.setVertDistance(dir.getVertDistance()*remainingLength/length);
				  Location loc = RelativeLocation.getLocation(lastLoc, dir);
				  resampTrace.addLocation(loc);
				  lastLoc = loc;
				  remainingLength = resampInt;
				  // Next location stays the same
			  } else {
				  lastLoc = nextLoc;
				  NextLocIndex += 1;
				  remainingLength -= length;
			  }
		  }
		  
		  // make sure we got the last one (might be missed because of numerical precision issues?)
		  double dist = RelativeLocation.getTotalDistance(trace.getLocationAt(trace.size()-1), resampTrace.getLocationAt(resampTrace.size()-1));
		  if (dist> resampInt/2) resampTrace.addLocation(trace.getLocationAt(trace.size()-1));
/*		  
		  // write out each to check
		  System.out.println("RESAMPLED");
		  for(int i=0; i<resampTrace.size(); i++) {
			  Location l = resampTrace.getLocationAt(i);
			  System.out.println(l.getLatitude()+"\t"+l.getLongitude()+"\t"+l.getDepth());
		  }

		  System.out.println("ORIGINAL");
		  for(int i=0; i<trace.size(); i++) {
			  Location l = trace.getLocationAt(i);
			  System.out.println(l.getLatitude()+"\t"+l.getLongitude()+"\t"+l.getDepth());
		  }
*/
		  // write out each to check
		  System.out.println("target resampInt="+resampInt+"\tnum="+num);
		  System.out.println("RESAMPLED");
		  double ave=0, min=Double.MAX_VALUE, max=Double.MIN_VALUE;
		  for(int i=1; i<resampTrace.size(); i++) {
			  double d = RelativeLocation.getTotalDistance(resampTrace.getLocationAt(i-1), resampTrace.getLocationAt(i));
			  ave +=d;
			  if(d<min) min=d;
			  if(d>max) max=d;
		  }
		  ave /= resampTrace.size()-1;
		  System.out.println("ave="+ave+"\tmin="+min+"\tmax="+max+"\tnum="+resampTrace.size());


		  System.out.println("ORIGINAL");
		  ave=0; min=Double.MAX_VALUE; max=Double.MIN_VALUE;
		  for(int i=1; i<trace.size(); i++) {
			  double d = RelativeLocation.getTotalDistance(trace.getLocationAt(i-1), trace.getLocationAt(i));
			  ave +=d;
			  if(d<min) min=d;
			  if(d>max) max=d;
		  }
		  ave /= resampTrace.size()-1;
		  System.out.println("ave="+ave+"\tmin="+min+"\tmax="+max+"\tnum="+trace.size());

		  return resampTrace;
	  }
	  
	  public void writeXYZ_toFile(String fileName) {
			try{
				FileWriter fw = new FileWriter("dev/scratch/ned/slab/"+fileName);
				fw.write("lat\tlon\tdepth\n");
				Iterator it = this.getLocationsIterator();
				while(it.hasNext()) {
					Location loc = (Location) it.next();
					fw.write(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth()+"\n");
				}	
				fw.close();
			}catch(Exception e) {
				e.printStackTrace();
			}

	  }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SlabSurfaceGenerator gen = new SlabSurfaceGenerator("sam_slab1_topTrace.txt","sam_slab1_bottomTrace.txt",10);
		gen.writeXYZ_toFile("surfXYZ.txt");
	}

}
