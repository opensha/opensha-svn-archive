package org.opensha.data.estimate;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFunc;
/**
 * <p>Title: PDF_Estimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values from an EvenlyDiscretizedFunction. (it is asssumed that the first and last values are the
 * first and last non-zero values, respectively)
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PDF_Estimate extends DiscretizedFuncEstimate {
  public final static String NAME  =  "PDF";
  public final static String MSG_EVENLY_DISCRETIZED_ALLOWED="Only evenly discretized function is allowed for PDF estimate";


  /**
   * Constructor - Accepts a EvenlyDiscretizedFunction and an indication of whether it is
   * normalized. Note that the function passed in is cloned..
   * MaxX and MinX are set according to those of the function
   * passed in. (it is asssumed that the first and last values are the
   * first and last non-zero values, respectively).
   * It normalizes so that PDF has unit area
   * @param func
   */
  public PDF_Estimate(EvenlyDiscretizedFunc func, boolean isNormalized) {
    super(func, isNormalized);
  }


  /**
  * As implemented, the function passed in is cloned.
  *  MaxX and MinX are set by those in the function passed in.
  *
  * @param func
  */
 public void setValues(DiscretizedFunc newFunc, boolean isNormalized) {
   if(!(newFunc instanceof EvenlyDiscretizedFunc))
     throw new InvalidParamValException(MSG_EVENLY_DISCRETIZED_ALLOWED);
   int num = newFunc.getNum();
   /*double lastY = newFunc.getY(num-1);
   double firstY = newFunc.getY(0);
   if(firstY!=0 || lastY!=0) throw new InvalidParamValException(this.EST_MSG_FIRST_LAST_Y_ZERO);
  */
   super.setValues(newFunc, isNormalized);
   //normalizeForUnitArea(); // normalize so that PDF has unit area
 }

 /**
  * normalize the function so that we have unit area for PDF
  */
/* private void normalizeForUnitArea() {
   int num = func.getNum();
   double delta = ((EvenlyDiscretizedFunc)func).getDelta();
   for(int i=0; i<num; ++i) {
     func.set(i, func.getY(i)/delta);
     //cumDistFunc.set(i, cumDistFunc.getY(i)/delta);
   }
 }*/


  public String getName() {
   return NAME;
 }


  /**
  * Get the cumulative distribution function
  * @return
  */
 public DiscretizedFunc getCDF_Test() {
   EvenlyDiscretizedFunc cdfFunc = (EvenlyDiscretizedFunc)func.deepClone();
   double deltaX = cdfFunc.getDelta();
   int numPoints = cdfFunc.getNum();
   double x;
   for(int i=0; i<numPoints; ++i) {
     x = cdfFunc.getX(i);
     cdfFunc.set(i, getProbLessThanEqual(x));
   }
   cdfFunc.setInfo("CDF from PDF Distribution");
   return cdfFunc;
 }

 /**
  * Get the probability for that the true value is less than or equal to provided
  * x value
  *
  * @param x
  * @return
  */
 public double getProbLessThanEqual(double x) {
   if(x<cumDistFunc.getX(0)) return 0;
   else if (x>cumDistFunc.getX(cumDistFunc.getNum()-1)) return 1;
   return cumDistFunc.getInterpolatedY(x);
 }

 /**
 * Return the discrete fractile for this probability value.
 *
 * @param prob Probability for which fractile is desired
 * @return
 */
 public double getFractile(double prob) {
   if(prob<cumDistFunc.getY(0)) return 0;
   return this.cumDistFunc.getFirstInterpolatedX(prob);
 }

 /**
  * Get the PDF
  * @return
  */
 public  DiscretizedFunc getPDF_Test() {

   EvenlyDiscretizedFunc pdfFunc = (EvenlyDiscretizedFunc)func.deepClone();
   double deltaX = pdfFunc.getDelta();
   int numPoints = pdfFunc.getNum();
   double x;
   for(int i=0; i<numPoints; ++i) {
     x = pdfFunc.getX(i);
     pdfFunc.set(i, getProbLessThanEqual(x + deltaX / 2) - getProbLessThanEqual(x - deltaX / 2));
   }
   pdfFunc.setInfo("PDF from PDF Distribution");
   return pdfFunc;

 }

}
