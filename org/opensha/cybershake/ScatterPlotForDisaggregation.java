package org.opensha.cybershake;

import java.io.*;
import org.opensha.util.FileUtils;
import java.util.*;

/**
 * <p>Title: ScatterPlotForDisaggregation </p>
 *
 * <p>Description: This class creates a scatter plot for the Ground motion Level .284g.
 * Plots created using the AS-1997 attenuation relationship from  OpenSHA
 * and Cybershake data.
 * It reads both the OpenSHA and Cybershake data files and includes sources and
 * ruptures that are in both data files.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class ScatterPlotForDisaggregation {
  public ScatterPlotForDisaggregation() {
    super();
    //first modify the Cybershake file for all the sources and ruptures in file to
    //be only that are contain in OpenSHA file
    readAndModifyCyberShakeDataFile();
    //modifying the OpenSHA file to have only those sources and ruptures that are
    //in the Cybershake file
    readAndModifyOpenSHA_DataFile();
  }


  public void readAndModifyCyberShakeDataFile(){
    try {
      ArrayList cybershakeFile = FileUtils.loadFile("CyberShake.txt");
      ArrayList openshaFile = FileUtils.loadFile("OpenSHA.txt");

      int openshaFileSize = openshaFile.size();
      //going over all the sources in the Cybershake file.
      for(int i=1;i<cybershakeFile.size();){
        String cybershakeFileLine = (String)cybershakeFile.get(i);
        StringTokenizer st = new StringTokenizer(cybershakeFileLine,",");
        int cSourceIndex = Integer.parseInt(st.nextToken().trim());
        boolean sourceFound = false;
        //going over all the file lines of the opensha and looking if source there
        //remove the source and its rupture.
        for(int j=1;j<openshaFileSize;++j){
          String openshafileLine = (String)openshaFile.get(j);
          StringTokenizer st1 = new StringTokenizer(openshafileLine,",");
          int oSourceIndex = Integer.parseInt(st1.nextToken().trim());
          //looking for the source if in OpenSHA
          if(oSourceIndex == cSourceIndex){
            sourceFound = true;
            break;
          }
        }
        if(sourceFound){ // if source found then go to the next source
          int cSourceIndex_later = cSourceIndex;
          while(cSourceIndex_later == cSourceIndex){
            ++i;
            if(i >= cybershakeFile.size())
              break;
              cybershakeFileLine = (String) cybershakeFile.get(i);
              st = new StringTokenizer(cybershakeFileLine, ",");
              cSourceIndex_later = Integer.parseInt(st.nextToken().trim());

          }
        }
        else{ // if not found then remove that source totally.
          int cSourceIndex_later = cSourceIndex;
          while(cSourceIndex_later == cSourceIndex){
            cybershakeFile.remove(i);
            cybershakeFileLine = (String)cybershakeFile.get(i);
            st = new StringTokenizer(cybershakeFileLine,",");
            cSourceIndex_later = Integer.parseInt(st.nextToken().trim());
          }
        }
      }

      FileWriter fw = new FileWriter("Cybershake_modified.txt");
      int size = cybershakeFile.size();
      for(int i=0;i<size;++i)
        fw.write((String)cybershakeFile.get(i)+"\n");
      fw.close();
    }
    catch (FileNotFoundException ex) {
    }
    catch (IOException ex) {
    }

  }


  public void readAndModifyOpenSHA_DataFile(){
    try {
      ArrayList cybershakeFile = FileUtils.loadFile("CyberShake_modified.txt");
      ArrayList openshaFile = FileUtils.loadFile("OpenSHA.txt");

      int cybershakeFileSize = cybershakeFile.size();
      //going over all the sources in the Cybershake file.
      for(int i=1;i<openshaFile.size();){
        String openshaFileLine = (String)openshaFile.get(i);
        StringTokenizer st = new StringTokenizer(openshaFileLine,",");
        int oSourceIndex = Integer.parseInt(st.nextToken().trim());
        boolean sourceFound = false;
        //going over all the file lines of the opensha and looking if source there
        //remove the source and its rupture.
        int sourceStartIndex =0;
        for(int j=1;j<cybershakeFileSize;++j){
          String cyberfileLine = (String)cybershakeFile.get(j);
          StringTokenizer st1 = new StringTokenizer(cyberfileLine,",");
          int cSourceIndex = Integer.parseInt(st1.nextToken().trim());
          //looking for the source if in OpenSHA
          if(cSourceIndex == oSourceIndex){
            sourceFound = true;
            sourceStartIndex = j;
            break;
          }
        }

        if(sourceFound){ // if source found
          int oSourceIndex_later = oSourceIndex;
          //look for ruptures in the sources, match both Cybershake and OpenSHA
          //should have same ruptures.
          while(oSourceIndex_later == oSourceIndex){
              st.nextToken();
              st.nextToken();
              //getting the rupture index of the Source from openSHa file
              int oRupIndex = Integer.parseInt(st.nextToken().trim());
              int cSourceIndex = oSourceIndex;
              boolean ruptureFound= false;
              //matching that rupture from the OpenSHA if it is in all the ruptures
              //of the Cybershake for that source
              for (int k = sourceStartIndex; cSourceIndex == oSourceIndex && k < cybershakeFile.size();
                   ++k) {
                String cyberFileLine = (String) cybershakeFile.get(k);
                st = new StringTokenizer(cyberFileLine, ",");
                cSourceIndex = Integer.parseInt(st.nextToken().trim());
                st.nextToken();
                st.nextToken();
                int cRupIndex = Integer.parseInt(st.nextToken().trim());
                if(cRupIndex == oRupIndex){
                  ruptureFound = true;
                  break;
                }
              }
              //if rupture not found then remove it
              if(!ruptureFound)
                openshaFile.remove(i);
              else //else go to next opensha line
                ++i;

              if (i >= openshaFile.size())
                break;
              //reading the next opensha line
              openshaFileLine = (String) openshaFile.get(i);
              st = new StringTokenizer(openshaFileLine, ",");
              //getting the source index from the next liine of the OpenSHA.
              oSourceIndex_later = Integer.parseInt(st.nextToken().trim());
              if (oSourceIndex_later != oSourceIndex)
                break;
          }
        }
        else{ // if not found then remove that source totally.
          int oSourceIndex_later = oSourceIndex;
          while(oSourceIndex_later == oSourceIndex){
            openshaFile.remove(i);
            openshaFileLine = (String)openshaFile.get(i);
            st = new StringTokenizer(openshaFileLine,",");
            oSourceIndex_later = Integer.parseInt(st.nextToken().trim());
          }
        }
      }

      FileWriter fw = new FileWriter("OpenSHA_modified.txt");
      int size = openshaFile.size();
      for(int i=0;i<size;++i)
        fw.write((String)openshaFile.get(i)+"\n");
      fw.close();
    }
    catch (FileNotFoundException ex) {
    }
    catch (IOException ex) {
    }

  }






  public static void main(String[] args) {
    ScatterPlotForDisaggregation scatterplotfordisaggregation = new
        ScatterPlotForDisaggregation();
  }
}
