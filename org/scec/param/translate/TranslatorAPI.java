package org.scec.param.translate;

import org.scec.exceptions.TranslateException;

/**
 * <b>Title:</b> TranslatorAPI<p>
 *
 * <b>Description:</b> Interface framework API for translators. These
 * translators translate values from normal space to the translated
 * space using translate(). Reverse translates back to normal space. <p>
 *
 * Since the translate() is typically a mathmatical function such as log()
 * or sin(), there are many mathmatical errors that can occur. The Math
 * class is used heavily by this package. Any errors Math may throw are
 * caught anc recast into a TranslateException. <p>
 *
 * Note that for reverse to work properly it must be a one-to-one mapping.
 * However there are no constraints programmed into this assumption. It
 * is up to the developer to program reverse any way they desire.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public interface TranslatorAPI {
    public double translate(double val) throws TranslateException;
    public double reverse(double val) throws TranslateException;
}
