package javaDevelopers.matt.calc;

import java.util.ArrayList;
import javaDevelopers.matt.calc.MagSortingTools;
import java.lang.Object;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;

/**
 * <p>Title: CompletenessMagCalc</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CompletenessMagCalc {
  public static double mcBest;
  public static double mcSynth;
  public static double mcMaxCurv;
  public static int numInBin;
  private static double deltaBin = 0.1;
  private static double GR_MMax = 10.0; // max mag to use in GR calc

  public CompletenessMagCalc() {

  }
  /**
   * Calculate the best Mc estimate based on the synthetic and max curvature
   * methods
   * @return mcBest double
   */
  public static double getMcBest(){
      return mcBest;
    }

    /**
     * Calculate Mc based on the max curvature method
     * @return mcMaxCurv double
     */
    public static double getMcMaxCurv(){
      return mcMaxCurv;
    }

    /**
     * Calculate Mc based on synthetic GR distributions.  Return the value
     * estimated at 95% probability, if not return the 90%.  If this is not
     * possible to estimate, return a Nan
     * @return double
     */
    public static double getMcSynth(){
      return mcSynth;
    }

    private static void calcMcMaxCurv(double[] magList){

      double minMag = MagSortingTools.getMinMag(magList);
      double maxMag = MagSortingTools.getMaxMag(magList);
      if(minMag>0){
        minMag=0;
      }

      //number of mag bins
      int numMags = (int)(maxMag*10)+1;

      //create the histogram of the mag bins
      MagHist.setMags(magList,minMag,maxMag,deltaBin);
      int[] numInBin = MagHist.getNumInBins();
      // find the value of max curvature and the bin it corresponds to
      double maxCurv = MagSortingTools.getMaxMag(numInBin);
      double[] magRange = MagSortingTools.getMagRange(minMag,maxMag,deltaBin);
      int maxCurvInd = MagSortingTools.getIndMatch((int)maxCurv,numInBin);
      //set mc to the M value at the maximum curvature
      mcMaxCurv = magRange[maxCurvInd];
    }

    private void calcMcSynth(double[] magList){
      // make a first guess at Mc using max curvature
      mcMaxCurv = getMcMaxCurv();
      int numEvents = magList.length;
      // loop over a range of completeness guesses
      for(double mcLoop = mcMaxCurv-0.9; mcLoop < mcMaxCurv + 1.5;
          mcLoop += deltaBin){
        double[] magBins = new double[numEvents];
      // get all events above the completeness guess (mcLoop)
        double[] cutCat = MagSortingTools.getCutCat(magList,mcLoop);
        int sizeCutCat = cutCat.length;
        // if > 25 events calculate the b value and estimate Mc
        if(sizeCutCat >= 25){
          MaxLikeGR_Calc.setMags(cutCat);
          double bvalMaxLike = MaxLikeGR_Calc.get_bValueMaxLike();
          int numBins = (int)Math.round((GR_MMax-mcLoop)/deltaBin)+1;

          // create the GR distribution of events
          GutenbergRichterMagFreqDist GR_FMD =
              new GutenbergRichterMagFreqDist(mcLoop,GR_MMax,numBins);
          GR_FMD.setAllButTotMoRate(mcLoop,GR_MMax,sizeCutCat,bvalMaxLike);
          // loop over all bins and get the # of events in each bin
          int mIndex = 0;
          double[] mbinRates = new double[numBins];
          for(double mbinLoop = mcLoop; mbinLoop <= GR_MMax; mbinLoop += deltaBin){
            mbinRates[mIndex] = GR_FMD.getIncrRate(mIndex++);
          }
          //create the histogram of the mag bins
          boolean flip = true;
          MagHist.setMags(magList,mcLoop,GR_MMax,deltaBin);
          int[] numObsInBin = MagHist.getNumInBins();
          double[] obsCumSum = MagSortingTools.getCumSum(numObsInBin,flip);
          //double[] resid = Math.sum(Math.abs(obsCumSum-)

        }
      }
    }

}
