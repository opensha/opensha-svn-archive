package com.jrefinery.data;

/*
 *  =======================================
 *  JFreeChart : a Java Chart Class Library
 *  =======================================
 *
 *  Project Info:  http://www.object-refinery.com/jfreechart/index.html
 *  Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 *  (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License as published by the Free Software Foundation;
 *  either version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307, USA.
 *
 *  ------------------
 *  JDBCChartAdapter.java
 *  ------------------
 *  (C) Copyright 2002, by Bryan Scott and Contributors.
 *
 *  Original Author:  Bryan Scott; Andy
 *  Contributor(s):   -;
 *
 *
 *  Changes
 *  -------
 *  26-Apr-2003 : Creation based on JdbcXYDataSet, using code contributed from Andy
 *  I think need to maintain a Hashmap of catagories.
 *
 */

import java.sql.*;
import java.util.HashMap;
import java.util.Vector;

/**
 *  Description of the Class
 *  This class provides an chart CategoryDataset implementation over a database JDBC result set.
 *  The dataset is populated via a call to executeQuery with the string sql query.
 *  The sql query must return at least two columns.  The first column will be
 *  the catagory name and remaining columns values.
 *  executeQuery can be called a number of times.
 *
 *  The database connection is read-only and no write back facility exists.
 *
 * @author           bryan_sco
 * @created          26 April 2002
 * @version          1.0
 * @copyright        Copyright (c) Antarctic Division 2001
 * @company          Australian Antarctic Division
 * @modifications    Modifications to the orginal code
 */

public class JdbcCategoryDataset extends DefaultCategoryDataset implements CategoryDataset {

  Connection connection;
  Statement statement;
  ResultSet resultSet;
  ResultSetMetaData metaData;

  /**
   * Constructor
   * Create a new JdbcCategoryDataset and establish a new database connection.
   *
   * @param  url         URL of the database connection
   * @param  driverName  The database driver class name
   * @param  user        The database user
   * @param  passwd      The database users password.
   */
  public JdbcCategoryDataset(String url,
      String driverName,
      String user,
      String passwd) {
    super(new Number[][] { {new Double(0.0)} });
    try {
      Class.forName(driverName);
      System.out.println("Opening db connection");
      connection = DriverManager.getConnection(url, user, passwd);
      statement = connection.createStatement();
    } catch (ClassNotFoundException ex) {
      System.err.println("Cannot find the database driver classes.");
      System.err.println(ex);
    } catch (SQLException ex) {
      System.err.println("Cannot connect to this database.");
      System.err.println(ex);
    }
  }

  /**
   * Constructor
   * Create a new JdbcCategoryDataset using the specificied database connection.
   *
   * @param  con  The database connection to use
   */
  public JdbcCategoryDataset(Connection con) {
    super(new double[][] { {0.0} });
    try {
      connection = con;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public JdbcCategoryDataset(Connection con, String query) {
    this(con);
    executeQuery(query);
  }

  /**
   *  ExecuteQuery will attempt execute the query passed to it against the existing database
   *  connection.  If no connection exists then no action is taken.
   *  The results from the query are extracted and cached locally, thus applying an upper limit
   *  on how many rows can be retrieved successfully.
   *
   * @param  query  The query to be executed
   */
  public void executeQuery(String query) {
    //System.out.println("JdbcCategoryDataset starting.");

    Object xObject = null;
    int column = 0;
    int currentColumn = 0;
    int numberOfColumns = 0;
    int numberOfValidColumns = 0;
    int columnTypes[] = null;
    HashMap categoryNames = new HashMap();
    Vector rows = new Vector();
    Number[] newRow ;

    if (connection == null) {
      System.err.println("There is no database to execute the query.");
      return;
    }

    //System.out.println("JdbcCategoryDataset executing query.");
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      metaData = resultSet.getMetaData();

      numberOfColumns = metaData.getColumnCount();

      if (numberOfColumns < 2) {
        System.err.println("Insuffiecient columns returned from the database.");
        return;
      } else {
        columnTypes = new int[numberOfColumns];

        for (column = 1; column < numberOfColumns; column++) {
          try {
            int type = metaData.getColumnType(column + 1);
            switch (type) {
              case Types.NUMERIC:
              case Types.REAL:
              case Types.INTEGER:
              case Types.DOUBLE:
              case Types.FLOAT:
              case Types.BIT:
              case Types.DATE:
              case Types.TIMESTAMP:
                ++numberOfValidColumns;
                columnTypes[column] = type;
                break;
              default:
                System.err.println("Unable to load column " + column + "(" + type + ")");
                columnTypes[column] = Types.NULL;
                break;
            }
          } catch (SQLException e) {
            e.printStackTrace();
            columnTypes[column] = Types.NULL;
          }
        }

        //System.out.println("Retrieving column names");
        /// Get the column names and cache them for use as series names.
        seriesNames = new String[numberOfValidColumns];
        currentColumn = 0;
        for (column = 1; column < numberOfColumns; column++) {
          if (columnTypes[column] != Types.NULL) {
            seriesNames[currentColumn] = metaData.getColumnLabel(column + 1);
            ++currentColumn;
          }
        }

        // Get all rows.
        //System.out.println("Retrieving row data");
        rows = new Vector();
        while (resultSet.next()) {
          /// First column is a category name
          categoryNames.put(resultSet.getString(1), "");
          newRow = new Number[numberOfValidColumns];

          currentColumn = 0;
          for (column = 0; column < numberOfColumns; column++) {
            xObject = resultSet.getObject(column + 1);
            switch (columnTypes[column]) {
              case Types.NUMERIC:
              case Types.REAL:
              case Types.INTEGER:
              case Types.DOUBLE:
              case Types.FLOAT:
              case Types.BIT:
                newRow[currentColumn] = (Number) xObject;
                ++currentColumn ;
                break;

              case Types.DATE:
              case Types.TIMESTAMP:
                newRow[currentColumn] = new Long(((java.util.Date) xObject).getTime());
                ++currentColumn ;
                break;

              case Types.NULL:
                break;
              default:
                System.err.println("UnKnown Data");
                columnTypes[column] = Types.NULL;
                break;
            }
          }
          rows.addElement(newRow);
        }

        /// A Kludge to make everything work when no rows returned
        if (rows.size() == 0) {
          newRow = new Number[numberOfValidColumns];
          for (column = 0; column < numberOfValidColumns; column++) {
            newRow[column] = new Integer(0);
          }
          rows.addElement(newRow);
        }


        /// Need to extract data
        numberOfColumns = rows.size() ;
        //System.out.println("Extracting data : " + numberOfColumns + " x " + numberOfValidColumns);
        data = new Number[numberOfValidColumns][numberOfColumns];
        for (column = 0; column < numberOfValidColumns ; ++column) {
          for (currentColumn = 0 ; currentColumn < numberOfColumns; ++currentColumn) {
            newRow = (Number[]) rows.get(currentColumn) ;
            data[column][currentColumn] = newRow[column] ;
          }
        }


        categories = categoryNames.keySet().toArray();

        categoryNames.clear();
        rows.removeAllElements();

        /*********************
        /// Ouput details
        System.out.println("Completed data extraction");
        System.out.println("Categories = " + this.getCategoryCount() );
        System.out.print("Categories = " );
        for (column = categories.length -1; column >= 0 ; --column) {
          System.out.print("" + categories[column] + ", ");
        }
        System.out.println("");
        System.out.println("Series     = " + this.getSeriesCount() );

        System.out.print("Series = " );
        for (column = seriesNames.length -1; column >= 0 ; --column) {
          System.out.print("" + seriesNames[column] + ", ");
        }
        System.out.println("");

        for (column = 0; column < data.length ; ++column) {
          System.out.print("row " + column + ": ");
          for (currentColumn = 0 ; currentColumn < numberOfColumns; ++currentColumn) {
            System.out.print("" + data[column][currentColumn] + ", ");
          }
          System.out.println("");
        }
        /******************/
        fireDatasetChanged();// Tell the listeners a new table has arrived.
      }
    } catch (SQLException ex) {
      System.err.println(ex);
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (Exception e) {
        }
      }
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception e) {
        }
      }
    }
  }


}
