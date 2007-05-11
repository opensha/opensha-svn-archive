package org.opensha.cybershake.db;

import java.io.*;
import java.sql.*;
import java.util.Date;
import com.sun.rowset.CachedRowSetImpl;


/**
 *  broker for database connections.
 * Creates and manages database connections.
 * 
 * Author: Nitin Gupta
 * date May 11,2007
 */
public class DBAccess implements DBAccessAPI {


    private String dbDriver, dbServer, dbLogin, dbPassword, logFileString;
 


    private PrintWriter log;

    int debugLevel;

    private Connection createConn()

        throws SQLException {

        Date now = new Date();
        Connection conn = null;
        try {
            Class.forName (dbDriver);

            conn = DriverManager.getConnection
                          (dbServer,dbLogin,dbPassword);

            
        } catch (ClassNotFoundException e2) {
            if(debugLevel > 0) {
                log.println("Error creating connection: " + e2);
            }
        }

        log.println(now.toString() + "  Opening connection " +" " + conn.toString() + ":");
        return conn;
    }
    
    
 
    /**
     * Inserts  the data into the database
     * @param query
     */
    public int insertUpdateOrDeleteData(String sql) throws java.sql.SQLException {
      Connection conn = createConn();
      Statement stat = conn.createStatement();
      int rows = stat.executeUpdate(sql);
      stat.close();
      conn = null;
      return rows;
    }


     /**
      * Runs the select query on the database
      * @param query
      * @return
      */
     public CachedRowSetImpl queryData(String sql) throws java.sql.SQLException {
       Connection conn = createConn();
       Statement stat = conn.createStatement();
       //gets the resultSet after running the query
       ResultSet result = stat.executeQuery(sql);
       // create CachedRowSet and populate
       CachedRowSetImpl crs = new CachedRowSetImpl();
       crs.populate(result);
       result.close();
       stat.close();
       conn = null;
       return crs;
     }

   } // End class
