package org.opensha.nshmp.sha.data.calc;

import org.opensha.nshmp.util.GlobalConstants;

/**
 * <p>Title: ResidentialSiteCalc</p>
 *
 * <p>Description: Calculates the Residential Site Value and
 * Residential Seismic Design category.</p>
 *
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 *
 * @version 1.0
 */
public class ResidentialSiteCalc {

  private static final String RESIDENTIAL_SDC_A = "A";
  private static final String RESIDENTIAL_SDC_B = "B";
  private static final String RESIDENTIAL_SDC_C = "C";
  private static final String RESIDENTIAL_SDC_D0 = "D0";
  private static final String RESIDENTIAL_SDC_D1 = "D1";
  private static final String RESIDENTIAL_SDC_D2 = "D2";
  private static final String RESIDENTIAL_SDC_E = "E";

  /**
   * Calculates the residential site value
   * @param ss double  Sa at 0.1 sec val
   * @param fa double
   * @return double
   */
  public double calcResidentialSiteValue(double ss, double fa) {
    return (2.0 / 3.0) * ss * fa;
  }

  /**
   * Calculates the Residential Seismic Design Category.
   * @param rsv double Residential Site Value[(2.0/3.0 * Ss(SA at 0.1 sec) * fa]
   * @param selectedEdition String
   * @return String
   */
  public String getResidentialSeismicDesignCategory(double rsv,
      String selectedEdition) {
    if (rsv >= 0 && rsv <= 0.17) {
      return RESIDENTIAL_SDC_A;
    }
    else if (rsv > 0.17 && rsv <= 0.33) {
      return RESIDENTIAL_SDC_B;
    }
    else if (rsv > 0.33 && rsv <= 0.5) {
      return RESIDENTIAL_SDC_C;
    }
    else if (selectedEdition.equals(GlobalConstants.IRC_2000) ||
             selectedEdition.equals(GlobalConstants.IRC_2003)) {
      if (rsv > 0.5 && rsv <= 0.83) {
        return RESIDENTIAL_SDC_D1;
      }
      else if (rsv > 0.83 && rsv <= 1.17) {
        return RESIDENTIAL_SDC_D2;
      }
      else if (rsv > 1.17) {
        return RESIDENTIAL_SDC_E;
      }
    }
    else if (selectedEdition.equals(GlobalConstants.IRC_2004) ||
						 selectedEdition.equals(GlobalConstants.IRC_2006)) {
      if (rsv >= 0.5 && rsv <= 0.67) {
        return RESIDENTIAL_SDC_D0;
      }
      else if (rsv >= 0.67 && rsv <= 0.83) {
        return RESIDENTIAL_SDC_D1;
      }
      else if (rsv >= 0.83 && rsv <= 1.17) {
        return RESIDENTIAL_SDC_D2;
      }
      else if (rsv > 1.17) {
        return RESIDENTIAL_SDC_E;
      }
    }

    return "";
  }

}
