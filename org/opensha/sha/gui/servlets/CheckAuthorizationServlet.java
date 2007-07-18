
package org.opensha.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import org.opensha.sha.gui.servlets.user_auth_db.OpenSHA_UsersDBDAO;
import org.opensha.sha.gui.servlets.user_auth_db.OpenSHA_UsersVO;
import java.io.*;


/**
 *
 * <p>Title: CheckAuthorizationServlet.java </p>
 * <p>Description: It will check the database to see whether user is authorized to
 * use the system</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta, Nitin Gupta
 * @date Nov 18, 2004
 * @version 1.0
 */

public class CheckAuthorizationServlet extends HttpServlet {
  private OpenSHA_UsersDBDAO dao = new OpenSHA_UsersDBDAO();
  public final static String USERNAME = "username";
  public final static String PASSWORD = "password";

  /**Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    String uname = request.getParameter(USERNAME);
    String password = request.getParameter(PASSWORD);
    OpenSHA_UsersVO userInfo  = dao.getUserInfo(uname, password);
    // get an ouput stream from the applet
    ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
    if(userInfo==null)
    //name of the image file as the URL
    outputToApplet.writeObject(Boolean.FALSE);
    else outputToApplet.writeObject(new Boolean(dao.isUserAuthorized(userInfo)));
    outputToApplet.close();
  }

  /** Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    doGet(request, response);
  }

}