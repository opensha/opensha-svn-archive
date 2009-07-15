package org.opensha.nshmp.sha.gui.api;

/**
 * <p>Title: ProbabilisticHazardApplicationAPI</p>
 *
 * <p>Description: This interface is used by all the Gui bean to communicate with
 * main application. This interface is like a Listener class that notifys the
 * application whenever data needs to be updated due to some action that has taken
 * place in the Gui bean.</p>
 * @author : Ned Field,Nitin Gupta and E.V. Lyeyendecker
 * @version 1.0
 */
public interface ProbabilisticHazardApplicationAPI {

  /**
   * Sets the information from the Gui beans in Data window
   * @param dataInfo String
   */
  public void setDataInWindow(String dataInfo);

}
