package javaDevelopers.vipin;

import java.io.FileWriter;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import org.opensha.data.Location;

/**
 * <p>Title: RuptureFileReaderWriter.java </p>
 * <p>Description: This class writes ruptures to a  text file as well as
 * read from that file</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RuptureFileReaderWriter {



  /**
   * write the ruptures to the file. For each rupture, it writes:
   * 1. Rupture Id
   * 2. Rupture Length
   * 3. All Locations on this rupture
   * 4. Fault Sections with which each location is associated
   */

  public static void  writeRupsToFile(FileWriter fw, ArrayList rupList) {
    try {
      // get total number of ruptures
      int numRups = 0;
      if (rupList != null) numRups = rupList.size();
        // loop over all ruptures and print them
      for (int i = 0; i < numRups; ++i) {
        MultiSectionRupture multiSectionRup = (MultiSectionRupture)rupList.get(i);
        ArrayList nodesList = multiSectionRup.getNodesList();
        fw.write("#Rupture " + i + " "+multiSectionRup.getLength()+"\n");
        // loop over all locations on this rupture and write them to file
        for (int k = 0; k < nodesList.size(); ++k) {
          Node node = (Node) nodesList.get(k);
          fw.write("\t" + node.getLoc() + ","+node.getFaultSectionName()+","+node.getId()+"\n");
        }
     }
   }catch(Exception e) {
     e.printStackTrace();
   }
 }

 /**
  * It reads a text file and loads all the ruptures into an ArrayList
  *
  * @param fileName Text file containing information about all the ruptures
  * @return
  */
 public static ArrayList loadRupturesFromFile(String fileName) {
   try {
     FileReader frRups = new FileReader(fileName);
     BufferedReader brRups = new BufferedReader(frRups); // buffered reader
     brRups.readLine(); // skip first line as it just contains number of ruptures
     String line = brRups.readLine().trim();
     double lat, lon;
     ArrayList nodesList=null; // it will hold the list of alocation/sectionanme/id for each location on a rupture
     ArrayList rupturesList = new ArrayList(); // list of ruptures
     float rupLen=0.0f;
     while(line!=null) {
       line=line.trim();
       if(!line.equalsIgnoreCase("")) { // if line is not a blank line
         if(line.startsWith("#"))  { // this is new rupture name

           if(nodesList!=null) { // add the rupture to the list of all ruptures
             MultiSectionRupture multiSectionRup = new MultiSectionRupture(
                 nodesList);
             multiSectionRup.setLength(rupLen);
             rupturesList.add(multiSectionRup);
           }
           // initalize for start of next rupture
           StringTokenizer tokenizer = new StringTokenizer(line);
           tokenizer.nextToken(); // rupture string
           tokenizer.nextToken(); // rupture counter
           rupLen = Float.parseFloat(tokenizer.nextToken()); // rupture length
           nodesList = new ArrayList();
         } else {
           // get the lat/lon, sectionName and locationId for each location on this rupture
           StringTokenizer tokenizer = new StringTokenizer(line,",");
           lat = Double.parseDouble(tokenizer.nextToken());// lat
           lon = Double.parseDouble(tokenizer.nextToken()); //lon
           tokenizer.nextToken(); // depth
           String sectionName = tokenizer.nextToken(); //section name
           int id = Integer.parseInt(tokenizer.nextToken());//id
           Node node = new Node(id, sectionName, new Location(lat,lon,0.0));
           nodesList.add(node);
         }
       }
       line=brRups.readLine();
     }
     // add the last rupture to the list
     MultiSectionRupture multiSectionRup = new MultiSectionRupture(
          nodesList);
     multiSectionRup.setLength(rupLen);
     rupturesList.add(multiSectionRup);
     // close the files
     brRups.close();
     frRups.close();
     return rupturesList;
   }catch(Exception e) {
     e.printStackTrace();
   }
   return null;
 }

}