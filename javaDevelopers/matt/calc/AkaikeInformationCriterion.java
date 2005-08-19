package javaDevelopers.matt.calc;

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
public class AkaikeInformationCriterion {
  private int sampleSize,numFreeParams;
  private double singleAICc;
  private double logLikelihood;

  public AkaikeInformationCriterion() {
  }

  /**
   * setNumberFreeParams
   */
  public void setNumberFreeParams(double numFreeParams) {
  }

  /**
   * setSampleSize
   */
  public void setSampleSize(double sampleSize) {
  }

  /**
   * calc_AIC
   */
  public void calc_AIC() {

    if (sampleSize-numFreeParams-1 == 0)
      singleAICc = -2*logLikelihood+2*numFreeParams;
    else
      singleAICc = -2*logLikelihood+2*numFreeParams+
          (2*numFreeParams*(numFreeParams+1))/(sampleSize-numFreeParams-1);
  }




}
