package gov.usgs.sha.calc;

import gov.usgs.util.GlobalConstants;
import gov.usgs.util.ui.DataDisplayFormatter;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;

/**
 * <p>Title: UHS_SpectrumCalculator</p>
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
public class UHS_SpectrumCalculator
    extends SpectrumCalculator {



    /**
     *
     * @param saVals ArbitrarilyDiscretizedFunc
     * @return DiscretizedFuncList
     */
    public DiscretizedFuncList calculateApproxUHSSpectrum(ArbitrarilyDiscretizedFunc pgaVals){

      double tAcc = pgaVals.getX(1);
      double sAcc = pgaVals.getY(1);
      double sVel = pgaVals.getY(2);

      DiscretizedFuncList funcList = approxSaSd(tAcc,sAcc,sVel,1,1);

      saTfunction.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_T_GRAPH);
      saSdfunction.setName(GlobalConstants.MCE_SPECTRUM_SD_Vs_T_GRAPH);
      String title = "MCE Response Spectra for Site Class B";
      String subTitle = "Ss and S1 = Mapped Spectral Acceleration Values";

      String info="";
      info +=title+"\n";

      info +=
          DataDisplayFormatter.createSubTitleString(subTitle, GlobalConstants.SITE_CLASS_B,
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
    public DiscretizedFuncList calculateSMS_UHSpectrum(ArbitrarilyDiscretizedFunc
        pgaVals,
        float fa, float fv, String siteClass) {

      double tAcc = pgaVals.getX(1);
      double sAcc = pgaVals.getY(1);
      double sVel = pgaVals.getY(2);

      DiscretizedFuncList funcList = approxSaSd(tAcc, sAcc, sVel, fa, fv);

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
    public DiscretizedFuncList calculateSD_UHSpectrum(ArbitrarilyDiscretizedFunc
                                                   pgaVals,
                                                   float fa, float fv,
                                                   String siteClass) {
      float faVal = (2.0f / 3.0f) * fa;
      float fvVal = (2.0f / 3.0f) * fv;

      double tAcc = pgaVals.getX(1);
      double sAcc = pgaVals.getY(1);
      double sVel = pgaVals.getY(2);

      DiscretizedFuncList funcList = approxSaSd(tAcc,sAcc,sVel, faVal, fvVal);

      saTfunction.setName(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_T_GRAPH);
      saSdfunction.setName(GlobalConstants.DESIGN_SPECTRUM_SD_Vs_T_GRAPH);

      String title = "Design Response Spectra for Site Class "+siteClass;
      String subTitle = "SDs = 2/3 x SMs and SD1 = 2/3 x SM1";

      String info = "";
      info += title+"\n";
      info +=
          DataDisplayFormatter.createSubTitleString(subTitle, siteClass,
                                                    faVal, fvVal);

        info +=
            DataDisplayFormatter.createFunctionInfoString(funcList,siteClass);
      funcList.setInfo(info);

      return funcList;
  }





}
