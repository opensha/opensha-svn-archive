package javaDevelopers.vipin.servlets;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;

import javaDevelopers.vipin.dao.db.DB_AccessAPI;
import javaDevelopers.vipin.dao.db.DB_ConnectionPool;
import java.sql.SQLException;
import com.sun.rowset.CachedRowSetImpl;
import java.io.ObjectOutputStream;

/**
 * <p>Title: DB_AccessServlet</p>
 *
 * <p>Description: Creates a two-tier database connection pool that can be shared.</p>
 *
 * @author Edward (Ned) Field, Vipin Gupta, Nitin Gupta
 * @version 1.0
 */
public class DB_AccessServlet extends HttpServlet{


  protected static DB_AccessAPI myBroker;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    if (myBroker == null) { // Only created by first servlet to call
      Properties p = new Properties();
      try {
        p.load(new
               FileInputStream("DbConnection_Prop.dat"));

        String dbDriver = (String) p.get("dbDriver");
        String dbServer = (String) p.get("dbServer");
        String dbLogin = (String) p.get("dbLogin");
        String dbPassword = (String) p.get("dbPassword");
        int minConns = Integer.parseInt( (String) p.get("minConns"));
        int maxConns = Integer.parseInt( (String) p.get("maxConns"));
        String logFileString = (String) p.get("logFileString");
        double maxConnTime =
            (new Double( (String) p.get("maxConnTime"))).doubleValue();

        myBroker = new
            DB_ConnectionPool(dbDriver, dbServer, dbLogin, dbPassword,
                               minConns, maxConns, logFileString, maxConnTime);
      }
      catch (FileNotFoundException f) {}
      catch (IOException e) {}
    }
  }

  /**
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {

      // get an input stream from the applet
      ObjectInputStream inputFromApp = new ObjectInputStream(request.
          getInputStream());
      ObjectOutputStream outputToApp = new ObjectOutputStream(response.
        getOutputStream());
      try {
        //receiving the name of the Function to be performed
        String functionToPerform = (String) inputFromApp.readObject();
        //receiving the query
        String query = (String)inputFromApp.readObject();

        /**
         * Checking the type of function that needs to be performed in the database
         */
        //getting the sequence number from Data table
        if(functionToPerform.equals(DB_AccessAPI.SEQUENCE_NUMBER)){
          int seqNo = myBroker.getNextSequenceNumber(query);
          outputToApp.writeObject(new Integer(seqNo));
        }
        //inserting new data in the table
        else if(functionToPerform.equals(DB_AccessAPI.INSERT_UPDATE_QUERY)){
          int key = myBroker.insertUpdateOrDeleteData(query);
          outputToApp.writeObject(new Integer(key));
        }
        //reading the data form the database
        else if(functionToPerform.equals(DB_AccessAPI.SELECT_QUERY)){
          CachedRowSetImpl resultSet= myBroker.queryData(query);
          outputToApp.writeObject(resultSet);
        }
        outputToApp.close();
      }
      catch(SQLException e){
        outputToApp.writeObject(e);
        outputToApp.close();
      }
      catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
  }



  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    // call the doPost method
    doGet(request, response);
  }

}
