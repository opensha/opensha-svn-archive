package org.opensha.nshmp.sha.calc;

import java.text.DecimalFormat;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.nshmp.util.GlobalConstants;
import org.opensha.nshmp.util.ui.DataDisplayFormatter;

/**
 * <p>Title: SpectrumCalculator</p>
 *
 * <p>Description: </p>
 * @author Ned Field,Nitin Gupta, E.V.Leyendecker
 * @version 1.0
 */
public class SpectrumCalculator {

  private double tPga, tPgaTransition, tVelTransition;

  protected ArbitrarilyDiscretizedFunc saSdfunction;
  protected ArbitrarilyDiscretizedFunc saTfunction;

  private DecimalFormat tFormat = new DecimalFormat("0.0");

  /**
   *
   * @param periodVal double
   * @param sPGA double
   * @param sAccerlation double
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  protected DiscretizedFuncList approxSaSd(double periodVal,
                                           double sAccerlation,
                                           double sVelocity, double sPGA) {
    DiscretizedFuncList funcList = new DiscretizedFuncList();
    saTfunction = new ArbitrarilyDiscretizedFunc();

    double tAcc = periodVal;
    tPga = 0;
    double tMaxVel = 2;
    double tInc = 0.1;
    tVelTransition = sVelocity / sAccerlation;
    tPgaTransition = 0.2 * tVelTransition;

    saTfunction.set(tPga, sPGA);
    saTfunction.set(tPgaTransition, sAccerlation);

    if (tPgaTransition <= tAcc) {
      saTfunction.set(tAcc, sAccerlation);
    }
    saTfunction.set(tVelTransition, sAccerlation);
    double lastT = ( (int) (tVelTransition * 10.0)) / 10.0;
    double nextT = lastT + tInc;

    while (nextT <= tMaxVel) {
      saTfunction.set(nextT, sVelocity / nextT);
      nextT += tInc;
      String nextTString = tFormat.format(nextT);
      nextT = Double.parseDouble(nextTString);
    }
    StdDisplacementCalc calc = new StdDisplacementCalc();
    saSdfunction = calc.getStdDisplacement(saTfunction);

    funcList.add(saSdfunction);
    funcList.add(saTfunction);
    return funcList;
  }

  /**
   *
   * @param saVals ArbitrarilyDiscretizedFunc
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList calculateMapSpectrum(ArbitrarilyDiscretizedFunc
                                                  saVals) {

    float fa = 1.0f;
    float fv = 1.0f;

    double tAcc = saVals.getX(0);
    double sAcc = fa * saVals.getY(0);
    double sVel = fv * saVals.getY(1);
    double sPGA = 0.4 * sAcc;

    DiscretizedFuncList funcList = approxSaSd(tAcc, sAcc, sVel, sPGA);

    saTfunction.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_T_GRAPH);
    saSdfunction.setName(GlobalConstants.MCE_SPECTRUM_SD_Vs_T_GRAPH);
    String title = "MCE Response Spectra for Site Class B";
    String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";

    String info = "";
    info += title + "\n";

    info +=
        DataDisplayFormatter.createSubTitleString(subTitle,
                                                  GlobalConstants.SITE_CLASS_B,
                                                  1, 1);
    info +=
        DataDisplayFormatter.createFunctionInfoString(funcList,
        GlobalConstants.SITE_CLASS_B);
    funcList.setInfo(info);
    return funcList;
  }

  /**
   *
   * @param saVals ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList calculateSMSpectrum(ArbitrarilyDiscretizedFunc
                                                 saVals,
                                                 float fa, float fv,
                                                 String siteClass) {

    double tAcc = saVals.getX(0);
    double sAcc = fa * saVals.getY(0);
    double sVel = fv * saVals.getY(1);
    double sPGA = 0.4 * sAcc;

    DiscretizedFuncList funcList = approxSaSd(tAcc, sAcc, sVel, sPGA);

    saTfunction.setName(GlobalConstants.SITE_MODIFIED_SA_Vs_T_GRAPH);
    saSdfunction.setName(GlobalConstants.SITE_MODIFIED_SD_Vs_T_GRAPH);

    String title = "Site Modified Response Spectra for Site Class " + siteClass;
    String subTitle = "SMs = FaSs and SM1 = FvS1";

    String info = "";
    info += title + "\n";
    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, siteClass,
                                                  fa, fv);

    info +=
        DataDisplayFormatter.createFunctionInfoString(funcList, siteClass);
    funcList.setInfo(info);
    return funcList;
  }

  /**
   *
   * @param saVals ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList calculateSDSpectrum(ArbitrarilyDiscretizedFunc
                                                 saVals,
                                                 float fa, float fv,
                                                 String siteClass) {
    float faVal = (2.0f / 3.0f) * fa;
    float fvVal = (2.0f / 3.0f) * fv;

    double tAcc = saVals.getX(0);
    double sAcc = faVal * saVals.getY(0);
    double sVel = fvVal * saVals.getY(1);
    double sPGA = 0.4 * sAcc;

    DiscretizedFuncList funcList = approxSaSd(tAcc, sAcc, sVel, sPGA);

    saTfunction.setName(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_T_GRAPH);
    saSdfunction.setName(GlobalConstants.DESIGN_SPECTRUM_SD_Vs_T_GRAPH);

    String title = "Design Response Spectra for Site Class " + siteClass;
    String subTitle = "SDs = 2/3 x SMs and SD1 = 2/3 x SM1";

    String info = "";
    info += title + "\n";
    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, siteClass,
                                                  fa, fv);

    info +=
        DataDisplayFormatter.createFunctionInfoString(funcList, siteClass);
    funcList.setInfo(info);

    return funcList;
  }
}
