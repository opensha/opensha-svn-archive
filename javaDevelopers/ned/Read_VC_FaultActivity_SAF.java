package javaDevelopers.ned;

import java.io.*;
import java.util.*;


/**
 * <p>Title: Read_VC_FaultActivity_SAF</p>
 *
 * <p>Description: This class reads the VC_Fault_Activity_SAF.txt . Then
 * extract the slip info at time period on each segment.</p>
 * @author Edward (Ned) Field
 * @version 1.0
 */
public class Read_VC_FaultActivity_SAF {


  private String[] segmentAreas;

  private String inputFile = "javaDevelopers/ned/RundleVC_data/VC_Fault_Activity_SAF.txt";

  public void getSegmentSlipTimeInfo() throws FileNotFoundException,
      IOException {

    //reading the file to extract Slip time infor for each segment
    FileReader fr = new FileReader(inputFile);
    BufferedReader br = new BufferedReader(fr);
    //skipping the first line
    br.readLine();
    //reading the next line in the file that tells how many segments are there in the file
    String numSegmentsLine = br.readLine();
    StringTokenizer st = new StringTokenizer(numSegmentsLine);
    int numSegments = Integer.parseInt(st.nextToken().trim());
    segmentAreas = new String[numSegments];
    //skipping the next line as it just provide with String " SEGMENT Area" String
    br.readLine();
    for(int i=0;i<numSegments;++i)
      segmentAreas[i] = br.readLine();

    //Skipping the lines for Segment Velocity
    for(int i=0;i<=numSegments;++i)
      br.readLine();



  }


  public static void main(String[] args) {
    Read_VC_FaultActivity_SAF read_vc_faultactivity_saf = new
        Read_VC_FaultActivity_SAF();
  }
}
