package org.opensha.sha.gui.infoTools;

import java.net.URL;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;

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

       //ClassLoader cl = frame.getClass().getClassLoader();
       HelpSet hs = null;
       try {
    	   	//URL url = HelpSet.findHelpSet(cl, helpSetFileName);
    	   URL url = this.getClass().getResource("/"+helpSetFileName);
    	   //System.out.println("URL ="+url);
    	   	//URL url = new URL(helpSetFileName);
         hs = new HelpSet(null, url);
       }
       catch (Exception ee) {
         ee.printStackTrace();
         return null;
       }
     // Create a HelpBroker object:
      return hs.createHelpBroker();
  }

/*public static void main(String args[]){
  LaunchHelpFromMenu helpMenu = new LaunchHelpFromMenu();
  HelpBroker hb = helpMenu.createHelpMenu("file:///Users/field/jbproject/sha/OpenSHA_docs/ScenarioShakeMap_UserManual/shaHelp.xml");
  //helpLaunchMenu.addActionListener(new CSH.DisplayHelpFromSource(hb));
}*/

}
