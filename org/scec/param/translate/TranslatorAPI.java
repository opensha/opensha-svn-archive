package org.scec.param.translate;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface TranslatorAPI {
    public double translate(double val) throws ParameterException;
    public double reverse(double val) throws ParameterException;
}