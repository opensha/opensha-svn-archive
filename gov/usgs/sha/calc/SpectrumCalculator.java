package gov.usgs.sha.calc;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;
import gov.usgs.sha.data.DataDisplayFormatter;
import gov.usgs.util.GlobalConstants;

/**
 * <p>Title: SpectrumCalculator</p>
 *
 * <p>Description: </p>
 * @author Ned Field,Nitin Gupta, E.V.Leyendecker
 * @version 1.0
 */
public class SpectrumCalculator {



  private DiscretizedFuncList approxSaSd(ArbitrarilyDiscretizedFunc saVals,
                                         float fa, float fv) {
    DiscretizedFuncList funcList = new DiscretizedFuncList();
    ArbitrarilyDiscretizedFunc saSdfunction = new ArbitrarilyDiscretizedFunc();
    ArbitrarilyDiscretizedFunc saTfunction = new ArbitrarilyDiscretizedFunc();
    funcList.add(saSdfunction);
    funcList.add(saTfunction);

    double tAcc = saVals.getX(0);
    double sAcc = fa*saVals.getY(0);
    double tVel = saVals.getX(0);
    double sVel = fv*saVals.getY(1);
    double spga = 0.4*fa*saVals.getX(0);
    double tPga = 0;
    double tMaxVel = 2;
    double tInc = 0.1;
    double tVelTransition = sVel/sAcc;
    double tPgaTransition = 0.2*tVelTransition;

    saTfunction.set(spga,tPga);
    saTfunction.set(sAcc,tPgaTransition);

    if(tPgaTransition <= tAcc) {
      saTfunction.set(sAcc, tAcc);
    }
    saTfunction.set(sAcc, tVelTransition);
    double lastT = ((int)(tVelTransition * 10.0)) / 10.0;
    double nextT = lastT + tInc;
    while(nextT<tMaxVel) {
        saTfunction.set(sVel/nextT,nextT);
        nextT+=tInc;
    }

    for(int i=0; i < saTfunction.getNum(); ++i) {
      saSdfunction.set(saTfunction.getX(0), 9.77*saTfunction.getX(0)*Math.pow(saTfunction.getY(0),2));
    }


    return funcList;
  }



  /**
   *
   * @param saVals ArbitrarilyDiscretizedFunc
   * @return DiscretizedFuncList
   */
  public DiscretizedFuncList calculateMapSpectrum(ArbitrarilyDiscretizedFunc saVals){
    DiscretizedFuncList funcList = approxSaSd(saVals,1,1);
    String title = "MCE Response Spectra for Site Class B";
    String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";

    String info="";
    info +=title;

    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, GlobalConstants.SITE_CLASS_B,
                                                  1, 1);
    for (int i = 0; i < funcList.size(); ++i)
      info +=
          DataDisplayFormatter.createFunctionInfoString( (ArbitrarilyDiscretizedFunc)
          funcList.get(i), "", "", "",
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
  public DiscretizedFuncList calculateSMSpectrum(ArbitrarilyDiscretizedFunc saVals,
                                                 float fa, float fv,String siteClass){
    DiscretizedFuncList funcList = approxSaSd(saVals,fa,fv);
    String title = "MCE Response Spectra for Site Class B";
    String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";

    String info="";
    info +=title;
    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, siteClass,
                                                  fa, fv);
    for (int i = 0; i < funcList.size(); ++i)
          info +=
              DataDisplayFormatter.createFunctionInfoString( (ArbitrarilyDiscretizedFunc)
              funcList.get(i), "", "", "",
              siteClass);
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
    float faVal = (2 / 3) * fa;
    float fvVal = (2 / 3) * fv;

    DiscretizedFuncList funcList = approxSaSd(saVals, faVal, fvVal);
    String title = "MCE Response Spectra for Site Class B";
    String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";

    String info = "";
    info += title;
    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, siteClass,
                                                  faVal, fvVal);
    for (int i = 0; i < funcList.size(); ++i)
      info +=
          DataDisplayFormatter.createFunctionInfoString( (
          ArbitrarilyDiscretizedFunc)
          funcList.get(i), "", "", "",
          siteClass);
    funcList.setInfo(info);

    return funcList;
  }
}
