package org.scec.sha.hazus;

import java.io.*;
import java.util.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HazusIML_FileGenerator {

  private final String Hazus ="hazus/";
  public HazusIML_FileGenerator() {
    /**
     *using the equation provided for the Ned to get the Prob. for the Hazus.
     */
    double prob =0;
    double time = 50;
    Vector imlVector = new Vector();
    File dirs =new File(Hazus);
    String[] dirList=dirs.list();
    // for each data set, read the meta data and sites info

    try{
      FileWriter fw = new FileWriter(Hazus+"final.dat");
      fw.write("#Column Info: Lat Lon IML values for the return period: 100, 250,"+
               "500,750,1000,1500,2000,2500"+"\n\n");
      for(int i=0;i<dirList.length;++i){
        imlVector.removeAllElements();

        if(dirList[i].endsWith(".txt")){
          double returnPd = 100;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 100: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 250;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 250: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 500;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 500: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 750;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 750: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 1000;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 1000: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 1500;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 1500: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 2000;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 2000: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));

          returnPd = 2500;
          prob = -1*(1- Math.exp((1/returnPd)*50));
          System.out.println("Prob for 2500: "+prob);
          imlVector.add(new Double(getIML(dirList[i],prob)));
          String lat = dirList[i].substring(0,dirList[i].indexOf("_"));
          String lon = dirList[i].substring(dirList[i].indexOf("_")+1,dirList[i].indexOf(".txt"));
          fw.write(lat+"  "+lon+"  ");
          for(int j=0;j<imlVector.size()-1;++j)
            fw.write(""+((Double)imlVector.get(j)).doubleValue()+",");
          fw.write(""+((Double)imlVector.get(imlVector.size()-1)).doubleValue()+"\n");
        }
      }
      fw.close();
    }catch(Exception e){
    }
  }


  public static void main(String[] args) {
    HazusIML_FileGenerator hazusIML_FileGenerator1 = new HazusIML_FileGenerator();
  }

  private double getIML(String filename , double prob){
    try{
      FileReader fr = new FileReader(Hazus+filename);
      BufferedReader br = new BufferedReader(fr);
      String prevLine = br.readLine();
      String currLine= br.readLine();
      StringTokenizer st =null;
      while(currLine!=null){
        st = new StringTokenizer(prevLine);
        double prevIML = new Double(st.nextToken()).doubleValue();
        double prevProb = new Double(st.nextToken()).doubleValue();
        st = new StringTokenizer(currLine);
        double currIML = new Double(st.nextToken()).doubleValue();
        double currProb = new Double(st.nextToken()).doubleValue();
        //System.out.println("CurrProb: "+currProb+" PrevProb: "+prevProb+" prob: "+prob);
        if(prob >=currProb && prob <=prevProb){
          double logCurrProb = Math.log(currProb);
          double logPrevProb = Math.log(prevProb);
          double logCurrIML = Math.log(currIML);
          double logPrevIML = Math.log(prevIML);
          double iml = (((prob-logCurrProb)/(logPrevProb- logCurrProb)) *
                        (logPrevIML - logCurrIML)) + logCurrIML;
          return Math.exp(iml);
        }
        prevLine = currLine;
        currLine = br.readLine();
      }
    }catch(Exception e){
      System.out.println(filename+" file not found");
      e.printStackTrace();
    }
    return 0;
  }
}

