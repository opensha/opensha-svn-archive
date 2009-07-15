package org.opensha.sha.gui.controls;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

import java.util.ArrayList;

/**
 * <p>Title: XY_ValuesControlPanelAPI</p>
 *
 * <p>Description: This interface provides interface to the XY_ValuesControlPanel.
 * Any application using XY_ValuesControlPanel needs to implement this interface</p>
 * @author : Nitin Gupta
 * @version 1.0
 */
public interface XY_ValuesControlPanelAPI {


    /**
     * Sets ArbitraryDiscretizedFunc inside list containing all the functions.
     * @param function ArbitrarilyDiscretizedFunc
     */
    public void setArbitraryDiscretizedFuncInList(ArbitrarilyDiscretizedFunc function);

}
