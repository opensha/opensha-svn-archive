package org.opensha.edison.meansigma;

import java.io.IOException;
import java.io.FileNotFoundException;
import org.opensha.util.FileUtils;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.calc.GaussianDistCalc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MeanSigmaCalcTestComparedToSRL {

  private String dirName ;
  public MeanSigmaCalcTestComparedToSRL(String dirName) {
    this.dirName = dirName;
  }


  private float[] rupRates;
  private float[] meanVals;
  private float[] sigVals;

  double[] imlVals = {0.3220, 0.5972};


  private void readFile() throws FileNotFoundException, IOException {
    ArrayList fileLines = FileUtils.loadFile(dirName+"/"+"USGS_2004_PGA.txt");
    int numEntries = fileLines.size();
    rupRates = new float[numEntries];
    meanVals = new float[numEntries];
    sigVals = new float[numEntries];
    //getting the Mean and Sigma for each rup from the output file
    for(int i=0;i<numEntries;++i){
      StringTokenizer st = new StringTokenizer((String)fileLines.get(i));
      st.nextToken();
      st.nextToken();
      meanVals[i] = Float.parseFloat(st.nextToken().trim());
      sigVals[i] = Float.parseFloat(st.nextToken().trim());
    }
    fileLines = null;

    fileLines = FileUtils.loadFile(dirName+"/"+"src_rup_metadata.txt");
    //getting the event rates for each rup from Src-Rup file
    for(int i=0;i<numEntries;++i){
      StringTokenizer st = new StringTokenizer((String)fileLines.get(i));
      st.nextToken();
      st.nextToken();
      rupRates[i] = Float.parseFloat(st.nextToken().trim());
     }
    fileLines = null;
  }


  private void createArbFuncForEachIML(){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    int numIMLs = imlVals.length;
    int numMeanVals = meanVals.length;
    for(int i=0;i<numIMLs;++i){
      double imlExceedProb = 0 ;
      for(int j=0;j<numMeanVals;++j){
        double stRndVar = (Math.log(imlVals[i]) - meanVals[j]) / sigVals[j];
        imlExceedProb += (GaussianDistCalc.getExceedProb(stRndVar,1,3.0)*rupRates[j]);
      }
       function.set(imlVals[i],imlExceedProb);
    }

    System.out.println(function.toString());
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage :\n\t" +
          "java -jar [jarfileName] [inputFileName] [output directory name]\n\n");
      System.out.println("jarfileName : Name of the executable jar file, by default it is MeanSigmaCalc.jar");
      System.out.println("inputFileName :Name of the input file, this input file should contain only 3 columns" +
                         " \"Lon Lat Vs30\", For eg: see \"MeanSigmaCalcTest_InputFile.txt\". ");
      System.out.println("output directory name : Name of the output directory where all the output files will be generated");
      System.exit(0);
    }

    //MeanSigmaCalc.main(args);

    MeanSigmaCalcTestComparedToSRL meansigmacalctestcomparedtosrl = new
        MeanSigmaCalcTestComparedToSRL(args[1]);
    try {
      meansigmacalctestcomparedtosrl.readFile();
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    meansigmacalctestcomparedtosrl.createArbFuncForEachIML();
  }
}
