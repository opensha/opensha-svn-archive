package javaDevelopers.matt.calc;

import java.util.ArrayList;

/**
 * <p>Title: MagSortingTools</p>
 * <p>Description: A suite of tools that will sort or operate on a catalog
* based on event magnitudes </p>
 * @author Matt Gerstenberger 2004
 * @version 1.0
 */

public class MagSortingTools {
  private static double minMag;
  private static double maxMag;
  private static double meanMag;
  private static int indMatch;
  private static double[] magBinList;
  private static double[] cutCat;
  private static double[] cumSumMagList;

  /**
   * default constructor
   */
  public MagSortingTools() {
  }

  /**
   * Calculate the minimum catalog magnitude
   * @return double minMag
   */
  public static double getMinMag(double[] magList) {
    calcMinMag(magList);
    return minMag;
  }

  /**
   * Calculate the maximum catalog magnitude
   * @return double maxMag
   */
  public static double getMaxMag(double[] magList) {
    calcMaxMag(magList);
    return maxMag;
  }

  /**
     * Calculate the maximum catalog magnitude
     * @return double maxMag
     */
    public static double getMaxMag(int[] magList) {
      calcMaxMag(magList);
      return maxMag;
    }


  /**
   * Calculate the mean catalog magnitude
   * @return double meanMag
   */
  public static double getMeanMag(double[] magList) {
    calcMeanMag(magList);
    return meanMag;
  }

  /**
   * find the index of the given value in the vector
   * @return double indMatch
   */
  public static int getIndMatch(double magList, double[] indList) {
    findInd(magList,indList);
    return indMatch;
  }
  /**
   * find the index of the given value in the vector
   * @return double indMatch
   */
  public static int getIndMatch(double magList, int[] indList) {
    findInd(magList,indList);
    return indMatch;
  }


  public static double[] getMagRange(double maxMag, double minMag,
                                   double deltaMag){
    magRange(minMag, maxMag, deltaMag);
    return magBinList;
  }

  public static double[] getCutCat(double[] magList, double magCut){
    magCutCat(magList,magCut);
    return cutCat;
  }

  public static double[] getCumSum(double[] magList, boolean flip){
      calcCumSum(magList, flip);
      return cumSumMagList;
    }

  public static double[] getCumSum(int[] magList, boolean flip){
      calcCumSum(magList, flip);
      return cumSumMagList;
        }


  /**
   * set the list of magnitudes to be used in the calculations
   * @param magList double
   */
  // public static void setMags(double[] magList){
  // }
  /**
   * set the list of magnitudes to be used in the calculations
   * @param magList ArrayList
   */
  // public static void setMags(ArrayList magList){
  // }

  // public static void setMags(int[] magList){
  // }

  private static void calcMinMag(double[] magList) {
    // find the minimum magnitude of the given catalogue
    double minMag = magList[0];

    int size = magList.length;
    for (int magLoop = 1; magLoop < size; ++magLoop)
      if (magList[magLoop] < minMag) {
        minMag = magList[magLoop];
      }
  }

  private static void calcMaxMag(double[] magList) {
    // find the maximum magnitude of the given catalogue
    double maxMag = magList[0];

    int size = magList.length;
    for (int magLoop = 1; magLoop < size; ++magLoop)
      if (magList[magLoop] > minMag) {
        maxMag = magList[magLoop];
      }
  }

  private static void calcMaxMag(int[] magList) {
      // find the maximum magnitude of the given catalogue
      int maxMag = magList[0];

      int size = magList.length;
      for (int magLoop = 1; magLoop < size; ++magLoop)
        if (magList[magLoop] > minMag) {
          maxMag = magList[magLoop];
        }
    }




  private static void calcMeanMag(double[] magList) {
    // find the mean magnitude for use in the GR calculation
    double sum = 0;
    int size = magList.length;
    for (int magLoop = 0; magLoop < size; ++magLoop)
      sum += magList[magLoop];
    meanMag = sum / size;
  }

  private static void findInd(double magList, double[] indList) {
    // find the index that corresponds to a certain value
    int size = indList.length;
      for (int indLoop = 0; indLoop < size; ++indLoop) {
        if (magList == indList[indLoop]){
          indMatch = indLoop;
        }
      }
  }

  private static void findInd(double magList, int[] indList) {
    // find the index that corresponds to a certain value
    int size = indList.length;
      for (int indLoop = 0; indLoop < size; ++indLoop) {
        if (magList == indList[indLoop]){
          indMatch = indLoop;
        }
      }
  }

  private static void magRange(double minMag, double maxMag, double deltaBin){

    int ct = 0;
    for (double mLoop = minMag; mLoop < maxMag; mLoop += deltaBin) {
      magBinList[ct] = mLoop;
      ++ct;
    }
  }
  private static void magCutCat(double[] magList, double magCut){
    int size = magList.length;
    int ct = 0;
    for(int mLoop = 0; mLoop < size; ++mLoop){
      if(magList[mLoop] >= magCut){
        cutCat[ct] = magList[mLoop];
        ++ct;
      }
    }
  }

  private static void calcCumSum(double[] magList, boolean flip){
    int mlLength = magList.length;
    double cumSumMagList[] = new double[mlLength];
    if(flip){
      cumSumMagList[9] = magList[mlLength-1];
      for (int csLoop = mlLength-2; csLoop >= 0; csLoop--){
        cumSumMagList[csLoop] = cumSumMagList[csLoop+1] + magList[csLoop];
      }
    }
    else{
      cumSumMagList[0] = magList[0];
        for (int csLoop = 1; csLoop < mlLength; csLoop++) {
            cumSumMagList[csLoop] = cumSumMagList[csLoop - 1] + magList[csLoop];
          }
    }
  }

  private static void calcCumSum(int[] magList, boolean flip){
      int mlLength = magList.length;
      double cumSumMagList[] = new double[mlLength];
      if(flip){
        cumSumMagList[9] = magList[mlLength-1];
        for (int csLoop = mlLength-2; csLoop >= 0; csLoop--){
          cumSumMagList[csLoop] = cumSumMagList[csLoop+1] + magList[csLoop];
        }
      }
      else{
        cumSumMagList[0] = magList[0];
          for (int csLoop = 1; csLoop < mlLength; csLoop++) {
              cumSumMagList[csLoop] = cumSumMagList[csLoop - 1] + magList[csLoop];
            }
      }
    }


  public static void main(String[] args) {
      double[] magList = new double[10];
      boolean flip = true;
      double startMag = 12;
      for(int synMag = 0;synMag<10;++synMag){
        magList[synMag] = startMag;
        System.out.print(" "+magList[synMag]);
        --startMag;


      }

      System.out.println("cumsum: "+MagSortingTools.getCumSum(magList,flip));


    }


}
