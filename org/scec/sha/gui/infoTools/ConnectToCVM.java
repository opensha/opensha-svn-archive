package org.scec.sha.gui.infoTools;


import java.util.*;
import java.io.Serializable;
import java.net.*;
import java.io.Serializable;
import java.io.*;

/**
 * <p>Title: ConnectToCVM</p>
 * <p>Description: This class connects to the CVM servlets to get the values for the
 * WillsSiteClass and SCEC Basin Depth</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public final class ConnectToCVM {

  /**
   * Gets the Wills et al. Site Type (2000) Map Web Service from the CVM servlet
   */
  public static Vector getWillsSiteTypeFromCVM (double lonMin,double lonMax,double latMin,double latMax,
                              double gridSpacing) throws Exception{
    Vector vs30 = null;
    // if we want to the paramter from the servlet


    // make connection with servlet
    URL cvmServlet = new URL("http://gravity.usc.edu/OpenSHA/servlet/WillsSiteClassServlet");
    URLConnection servletConnection = cvmServlet.openConnection();

    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);

    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

    // send the student object to the servlet using serialization
    ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());


    outputToServlet.writeObject(new Double(lonMin));
    outputToServlet.writeObject(new Double(lonMax));
    outputToServlet.writeObject(new Double(latMin));
    outputToServlet.writeObject(new Double(latMax));
    outputToServlet.writeObject(new Double(gridSpacing));

    outputToServlet.flush();
    outputToServlet.close();

    // now read the connection again to get the vs30 as sent by the servlet
    ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
    //Vector of Wills Site Class Values translated from the Vs30 Values.
    vs30=(Vector)ois.readObject();
    ois.close();

    return vs30;
  }



  /**
   * Gets the Basin Depth from the CVM servlet
   */
  public static Vector getBasinDepthFromCVM(double lonMin,double lonMax,double latMin,double latMax,
                                    double gridSpacing) throws Exception{
    Vector basinDepth = null;
    // if we want to the paramter from the servlet

    // make connection with servlet
    URL cvmServlet = new URL("http://gravity.usc.edu/OpenSHA/servlet/SCEC_BasinDepthServlet");
    URLConnection servletConnection = cvmServlet.openConnection();

    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);

    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

    // send the student object to the servlet using serialization
    ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

    outputToServlet.writeObject(new Double(lonMin));
    outputToServlet.writeObject(new Double(lonMax));
    outputToServlet.writeObject(new Double(latMin));
    outputToServlet.writeObject(new Double(latMax));
    outputToServlet.writeObject(new Double(gridSpacing));

    outputToServlet.flush();
    outputToServlet.close();

    // now read the connection again to get the vs30 as sent by the servlet
    ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());

    //vectors of Basin Depth for each gridded site
    basinDepth=(Vector)ois.readObject();
    ois.close();


    return basinDepth;
 }

}