package javaDevelopers.matt.calc;

import java.util.ArrayList;

/**
 * <p>Title: MaxLikeGR_Calc </p>
 * <p>Description: Calculate maximum likelihood a and b values from the
* Gutenberg Richter distribution</p>
 * @author Matt Gerstenberger
 * @version 1.0
 */

public class MaxLikeGR_Calc {
  private static double aVal;
  private static double bVal;
  private static double binning = 0.1;

  /**
   * default constuctor
   */
  public MaxLikeGR_Calc() {

  }
  /**
   * get the a value from the Gutenberg Richter Distribution
   * @return double
   */
  public static double get_aValueMaxLike(){
   return aVal;
  }
  /**
   * get the b value from the Gutenberg Richter distribution
   * @return double
   */

  public static double get_bValueMaxLike(){
    return bVal;
  }
  /**
   * Get the magnitudes of the earthquakes to be used in the GR calculation
   * @param magList double[]
   */
  public static void setMags(double[] magList){
    calcGR_MaxLike(magList);
  }
  /**
   * Get the magnitudes of the earthquakes to be used in the GR calculation
   * @param magList ArrayList array of Doubles
   */

  public static void setMags(ArrayList magList){

  }
  private static double getMinMag(double[] magList){
    // find the minimum magnitude of the given catalogue
   double minMag = magList[0];

   int size = magList.length;
   for(int magLoop = 1;magLoop < size; ++magLoop)
     if(magList[magLoop] < minMag){
       minMag = magList[magLoop];
     }
   return minMag;
    }
  private static double getMeanMag(double[] magList){
    // find the mean magnitude for use in the GR calculation
    double sum = 0;
    int size = magList.length;
   for(int magLoop = 0;magLoop < size; ++magLoop)
     sum += magList[magLoop];
    return sum/size;
  }

  private static void calcGR_MaxLike(double[] magList){
  /*  fMinMag = min(mCatalog(:,6));
   fMeanMag = mean(mCatalog(:,6));
   % Calculate the b-value (maximum likelihood)
   fBValue = (1/(fMeanMag-(fMinMag-(fBinning/2))))*log10(exp(1));
   fAValue = log10(nLen) + fBValue * fMinMag;
*/
   double minMag = getMinMag(magList);
   double meanMag = getMeanMag(magList);
   int size = magList.length;
   bVal = (1/(meanMag-(minMag-(binning/2.0))))*0.43429;
   aVal = Math.log(size)*.43429 + bVal * minMag;
  }


  public static void main(String[] args) {
    double[] magList = new double[10];
    double startMag = 3;
    for(int synMag = 0;synMag<10;++synMag){
      magList[synMag] = startMag;
      ++startMag;
    }
    MaxLikeGR_Calc.setMags(magList);
    System.out.println("aVal is: "+MaxLikeGR_Calc.get_aValueMaxLike());
    System.out.println("bVal is: "+MaxLikeGR_Calc.get_bValueMaxLike());

  }

}
