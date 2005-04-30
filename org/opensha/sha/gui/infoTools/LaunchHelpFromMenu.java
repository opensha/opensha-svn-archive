package org.opensha.sha.gui.infoTools;

import java.net.URL;
import javax.help.HelpBroker;
import javax.help.HelpSet;

/**
 * <p>Title: LaunchHelpFromMenu</p>
 *
 * <p>Description: This class allows the user to view the application help from the
 *  application's help menu.</p>
 * @author Nitin Gupta
 * @version 1.0
 */
public class LaunchHelpFromMenu {


    /**
     * Adding the help set file to launch the Scenario Shakemap help when user presses the Help from "Menu"
     * @param helpSetFileName String Name of the helpset file with full path
     */
    public HelpBroker createHelpMenu(String helpSetFileName){

       ClassLoader cl = this.getClass().getClassLoader();
       HelpSet hs = null;
       try {
         URL hsURL = HelpSet.findHelpSet(cl, helpSetFileName);
         hs = new HelpSet(cl, hsURL);
       }
       catch (Exception ee) {
         ee.printStackTrace();
         return null;
       }
     // Create a HelpBroker object:
      return hs.createHelpBroker();
  }



}
