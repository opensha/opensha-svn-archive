package org.scec.util;

import org.scec.param.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ParamUtils {

    public static boolean isDoubleOrDoubleDiscreteConstraint(ParameterAPI param) {
        if( isDoubleConstraint(param) || isDoubleDiscreteConstraint(param) ) return true;
        else return false;
    }

    public static boolean isDoubleConstraint(ParameterAPI param) {
        ParameterConstraintAPI constraint = param.getConstraint();
        if ( constraint instanceof DoubleConstraint ) return true;
        else return false;
    }

    public static boolean isDoubleDiscreteConstraint(ParameterAPI param) {
        ParameterConstraintAPI constraint = param.getConstraint();
        if ( constraint instanceof DoubleDiscreteConstraint ) return true;
        else return false;
    }

    public static boolean isWarningParameterAPI(ParameterAPI param) {
        if ( param instanceof WarningParameterAPI ) return true;
        else return false;
    }

}
