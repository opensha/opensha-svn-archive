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
  public RuptureFileReaderWriter() {
  }



  // write the ruptures to the file
 public static void  writeRupsToFile(FileWriter fw, ArrayList rupList) {
   try {
     int numRups = 0;
     if (rupList != null) numRups = rupList.size();
     //fw.write("#Num Ruptures=" + numRups + "\n");
     int rupCount=0;
     //FileWriter fwPrintOrder = new FileWriter("SectionPrintOrder.txt");
     //for(int j=0; j<faultSectionPrintOrder.size(); ++j) {
      //String sectionName = (String)faultSectionPrintOrder.get(j);
      //fwPrintOrder.write(sectionName+"\n");
       for (int i = 0; i < numRups; ++i) {
         MultiSectionRupture multiSectionRup = (MultiSectionRupture)rupList.get(i);
         ArrayList nodesList = multiSectionRup.getNodesList();
       //  String firstLocationSectionName = ((Node) nodesList.get(0)).getFaultSectionName();
        // if(firstLocationSectionName.equalsIgnoreCase(sectionName)) {
           fw.write("#Rupture " + rupCount + " "+multiSectionRup.getLength()+"\n");
           ++rupCount;
           for (int k = 0; k < nodesList.size(); ++k) {
             Node node = (Node) nodesList.get(k);
             fw.write("\t" + node.getLoc() + ","+node.getFaultSectionName()+","+node.getId()+"\n");
           }
         //}
       }
     //}
     //fwPrintOrder.close();

     /*if (rupList != null) numRups = rupList.size();
     fw.write("#Num Ruptures=" + numRups + "\n");
     for (int i = 0; i < numRups; ++i) {
       fw.write("#Rupture " + i + "\n");
       MultiSectionRupture multiSectionRup = (MultiSectionRupture)rupList.get(i);
       ArrayList nodesList = multiSectionRup.getNodesList();
       for (int j = 0; j < nodesList.size(); ++j)
         fw.write("\t" + ((Node) nodesList.get(j)).getLoc()+"\n");
     }*/
   }catch(Exception e) {
     e.printStackTrace();
   }
 }

 public static ArrayList loadRupturesFromFile(String fileName) {
   try {
     FileReader frRups = new FileReader(fileName);
     BufferedReader brRups = new BufferedReader(frRups); // buffered reader
     brRups.readLine(); // skip first line as it just contains number of ruptures
     String line = brRups.readLine().trim();
     double lat, lon;
     ArrayList nodesList=null;
     ArrayList rupturesList = new ArrayList();
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
           StringTokenizer tokenizer = new StringTokenizer(line);
           tokenizer.nextToken(); // rupture string
           tokenizer.nextToken(); // rupture counter
           rupLen = Float.parseFloat(tokenizer.nextToken());
           nodesList = new ArrayList();
         } else { // location on a rupture
           StringTokenizer tokenizer = new StringTokenizer(line,",");
           lat = Double.parseDouble(tokenizer.nextToken());
           lon = Double.parseDouble(tokenizer.nextToken());
           String sectionName = tokenizer.nextToken();
           int id = Integer.parseInt(tokenizer.nextToken());
           Node node = new Node(id, sectionName, new Location(lat,lon,0.0));
           nodesList.add(node);
         }
       }
       line=brRups.readLine();
     }
     brRups.close();
     frRups.close();
     return rupturesList;
   }catch(Exception e) {
     e.printStackTrace();
   }
   return null;
 }

}