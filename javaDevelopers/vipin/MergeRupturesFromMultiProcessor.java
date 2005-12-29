package javaDevelopers.vipin;

import java.util.ArrayList;
import java.io.FileWriter;

/**
 * <p>Title: MergeRupturesFromMultiProcessor.java </p>
 * <p>Description: When each section is processed on a different processor,
 * we get multiple rupture files. These rupture files may have duplicate ruptures.
 * This program reads each file and then creates one common rupture file
 * which does not have any duplicates. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MergeRupturesFromMultiProcessor {
  private String ruptureFilePrefix, outputFilename;
  private int startIndex, endIndex;
  private final static String STATUS_FILE_NAME = "MergeStatus.txt";

  public MergeRupturesFromMultiProcessor() {
  }


  public void doMerge() {
    ArrayList masterRuptureList = new ArrayList();
    try {
      //loop over all files
      for(int i=startIndex; i<=endIndex; ++i) {
        String rupFileName = ruptureFilePrefix+"_"+i+".txt";
        FileWriter statusFile = new FileWriter(STATUS_FILE_NAME,true);
        statusFile.write(rupFileName+"\n");
        statusFile.close();
        System.out.println(rupFileName);
        ArrayList rupList = RuptureFileReaderWriter.loadRupturesFromFile(rupFileName);
        // loop over each rupture in that file
        for(int k=0; k<rupList.size(); ++k) {
          MultiSectionRupture rup = (MultiSectionRupture)rupList.get(k);
          // if it is not a duplicate rupture, add it to list
          if(masterRuptureList.contains(rup)) masterRuptureList.add(rup);
        }
      }

      FileWriter fw = new FileWriter(outputFilename);
      RuptureFileReaderWriter.writeRupsToFile(fw, masterRuptureList);
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * File prefix for the files to be read
   *
   * @param rupFilePrefix
   */
  public void setRupFilePrefix(String rupFilePrefix) {
    this.ruptureFilePrefix = rupFilePrefix;
  }

  /**
   * File numbers range for which merge needs to be done
   *
   * @param startIndex
   * @param endIndex
   */
  public void setSectionRange(int startIndex, int endIndex) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  /**
   * Name of the output file to be generated
   *
   * @param outFilename
   */
  public void setOutFilename(String outFilename) {
    this.outputFilename=outFilename;
  }

  /**
   * It accepts following command line arguments
   * 1. Rupture files prefix
   * 2. starting index of file
   * 3. End index of file
   * 4. Name of resultant output file
   *
   * @param args
   */
  public static void main(String[] args) {
    MergeRupturesFromMultiProcessor mergeRupturesFromMultiProcessor =
        new MergeRupturesFromMultiProcessor();
    mergeRupturesFromMultiProcessor.setRupFilePrefix(args[0]);
    mergeRupturesFromMultiProcessor.setSectionRange(Integer.parseInt(args[1]),
        Integer.parseInt(args[2]));
    mergeRupturesFromMultiProcessor.setOutFilename(args[3]);
    mergeRupturesFromMultiProcessor.doMerge();
  }

}