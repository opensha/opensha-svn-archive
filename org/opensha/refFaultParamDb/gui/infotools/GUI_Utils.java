package org.opensha.refFaultParamDb.gui.infotools;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import org.opensha.gui.TitledBorderPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * <p>Title: GUI_Utils.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GUI_Utils {
  public final static GridBagLayout gridBagLayout = new GridBagLayout();
  private static String userName;
  private static String password;

  /**
   * Get Bordered Panel
   *
   * @param infoLabel
   * @param borderTitle
   * @return
   */
   public static JPanel getPanel(InfoLabel infoLabel, String borderTitle) {
     JPanel panel = new TitledBorderPanel(borderTitle+":");
     panel.setLayout(gridBagLayout);
     panel.add(infoLabel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
     return panel;
   }

   /**
    * Set the username typed by the user in the login window. This username is used
    * for db connection
    * @param userName
    */
   public static void setUserName(String userName) {
     GUI_Utils.userName = userName;
   }
   /**
    * Get the username to be used for DB connection
    * @return
    */
   public static String getUserName() { return userName; }
   /**
    * Set the password for making the database connection
    * @param password
    */
   public static void setPassword(String password) {
     GUI_Utils.password = password;
   }
   /**
    * Get the password
    * @return
    */
   public static String getPassword() { return password; }


}