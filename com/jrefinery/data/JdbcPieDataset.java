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
 *  JdbcPieDataset.java
 *  ------------------
 *  (C) Copyright 2002, by Bryan Scott and Contributors.
 *
 *  Original Author:  Bryan Scott; Andy
 *  Contributor(s):   -;
 *
 *
 *  Changes
 *  -------
 *  26-Apr-2002 : Creation based on JdbcXYDataSet, but extending DefaultPieDataset
 *
 */

import java.sql.*;
import java.util.*;

public class JdbcPieDataset extends DefaultPieDataset {

  Connection connection;
  Statement statement;
  ResultSet resultSet;
  ResultSetMetaData metaData;

  public JdbcPieDataset() {
    super();
  }

 /**
   * Constructor
   * Create a new JdbcCategoryDataset and establish a new database connection.
   *
   * @param  url         URL of the database connection
   * @param  driverName  The database driver class name
   * @param  user        The database user
   * @param  passwd      The database users password.
   */
  public JdbcPieDataset(String url,
      String driverName,
      String user,
      String passwd) {
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
  public JdbcPieDataset(Connection con) {
    try {
      connection = con;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public JdbcPieDataset(Connection con, String query) {
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
        Object xObject = null;
        int column = 0;
        int currentColumn = 0;
        int numberOfColumns = 0;
        int numberOfValidColumns = 0;
        int columnTypes[] = null;

        if (connection == null) {
            System.err.println("There is no database to execute the query.");
            return;
        }

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            metaData = resultSet.getMetaData();

            numberOfColumns = metaData.getColumnCount();
            if (numberOfColumns != 2) {
              System.err.println("Invalid sql generated.  PieDataSet requires 2 columns only");
            } else {

              columnTypes = new int[numberOfColumns];
              columnTypes[0] = Types.VARCHAR ;

              /// Yes this could be simple but I left it for now, as it follows standard
              /// jdbcXXXXdataset format
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
                            columnTypes[column] = Types.NULL ;
                            break;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    columnTypes[column] = Types.NULL ;
                }
            }

          //System.out.println("Finished column names");

          /// Might need to add, to free memory from any previous result sets
          this.keys.clear();
          this.vals.clear();

          while (resultSet.next()) {
            Object category = resultSet.getString(1);
            Number value = null ;

            xObject = resultSet.getObject(2);
            switch (columnTypes[1]) {
                case Types.NUMERIC:
                case Types.REAL:
                case Types.INTEGER:
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.BIT:
                  value = (Number) xObject;
                  break ;

                case Types.DATE:
                case Types.TIMESTAMP:
                  value = new Long(((java.util.Date) xObject).getTime());
                  break;
                case Types.NULL:
                  break ;
                default:
                  System.err.println("UnKnown Data");
                  columnTypes[1] = Types.NULL ;
                  break;
              }
              keys.add(category);
              vals.add(value);
            }
        }

        fireDatasetChanged();// Tell the listeners a new table has arrived.
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