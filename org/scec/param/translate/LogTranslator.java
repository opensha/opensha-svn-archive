package org.scec.param.translate;

import org.scec.exceptions.ParameterException;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class LogTranslator implements TranslatorAPI {
    public double translate(double val)  throws ParameterException{
        return Math.log(val);
    }
    public double reverse(double val)  throws ParameterException{
        return Math.log(val);
    }
}