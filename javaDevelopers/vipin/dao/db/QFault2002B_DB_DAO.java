package javaDevelopers.vipin.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import javaDevelopers.vipin.dao.QFault2002B_DAO_API;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.QFault2002B;
import javaDevelopers.vipin.dao.exception.QueryException;

/**
 * <p>Title: QFault2002B_DB_DAO.java </p>
 * <p>Description: get the fault sections info from Qfault2002B table in pasadena
 * oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class QFault2002B_DB_DAO implements QFault2002B_DAO_API {

  private final static String TABLE_NAME="QFault_2002B";
  private final static String SECTION_ID="Section_Id";
  private final static String SECTION_NAME="Section_Name";
  private final static String EFFECTIVE_DATE="Effective_Date";
  private final static String COMMENTS="comments";
  private final static String AVG_SLIP_RATE="Avg_Slip_Rate";
  private final static String SLIP_COMMENTS="Slip_Comments";
  private final static String SLIP_RATE_STDDEV="Slip_Rate_StdDev";
  private final static String SLIP_DEV_COMMENTS="Slip_Dev_Comment";
  private final static String AVG_DIP="Avg_Dip";
  private final static String DIP_COMMENTS="Dip_Comments";
  private final static String AVG_UPPER_DEPTH="Avg_Upper_Depth";
  private final static String UPPER_D_COMMENT="Upper_D_Comment";
  private final static String AVG_LOWER_DEPTH="Avg_Lower_Depth";
  private final static String LOWER_D_COMMENT="Lower_D_Comment";
  private final static String AVG_RAKE="Avg_Rake";
  private final static String RAKE_COMMENTS="Rake_Comments";

  private DB_Connection dbConnection;

 /**
  * Constructor.
  * @param dbConnection
  */
 public QFault2002B_DB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
 }


 public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
 }

  public QFault2002B getFaultSection(String sectionId) throws QueryException {
    QFault2002B faultSection=null;
    String condition = " where "+SECTION_ID+"='"+sectionId+"'";
    ArrayList faultSectionList = query(condition);
    if(faultSectionList.size()>0) faultSection = (QFault2002B)faultSectionList.get(0);
    return faultSection;
  }

  public ArrayList getAllFaultSections() throws QueryException {
    return query(" ");
  }

  private ArrayList query(String condition) throws QueryException {
    ArrayList faultSectionList = new ArrayList();
    String sql = "select " + SECTION_ID + "," + SECTION_NAME + "," +
        EFFECTIVE_DATE + "," +
        COMMENTS + "," + AVG_SLIP_RATE + "," + SLIP_COMMENTS + "," +
        SLIP_RATE_STDDEV + "," +
        SLIP_DEV_COMMENTS + "," + AVG_DIP + "," + DIP_COMMENTS + "," +
        AVG_UPPER_DEPTH + "," +
        UPPER_D_COMMENT + "," + AVG_LOWER_DEPTH + "," + LOWER_D_COMMENT + "," +
        AVG_RAKE + "," + RAKE_COMMENTS +
        " from " + TABLE_NAME + condition;

    try {
      ResultSet rs = dbConnection.queryData(sql);
      while (rs.next()) {
        QFault2002B faultSection = new QFault2002B();
        faultSection.setSectionId(rs.getString(SECTION_ID));
        faultSection.setSectionName(rs.getString(SECTION_NAME));
        faultSection.setEffectiveDate(rs.getDate(EFFECTIVE_DATE));
        faultSection.setComments(rs.getString(COMMENTS));
        faultSection.setAvgSlipRate(rs.getFloat(AVG_SLIP_RATE));
        faultSection.setSlipComments(rs.getString(SLIP_COMMENTS));
        faultSection.setSlipRateStdDev(rs.getFloat(SLIP_RATE_STDDEV));
        faultSection.setSlipDevComment(rs.getString(SLIP_DEV_COMMENTS));
        faultSection.setAveDip(rs.getFloat(AVG_DIP));
        faultSection.setDipComments(rs.getString(DIP_COMMENTS));
        faultSection.setAvgUpperDepth(rs.getFloat(AVG_UPPER_DEPTH));
        faultSection.setUpperDepthComment(rs.getString(UPPER_D_COMMENT));
        faultSection.setAvgLowerDepth(rs.getFloat(AVG_LOWER_DEPTH));
        faultSection.setLowerDepthComment(rs.getString(LOWER_D_COMMENT));
        faultSection.setAveRake(rs.getFloat(AVG_RAKE));
        faultSection.setRakeComments(rs.getString(RAKE_COMMENTS));
        faultSectionList.add(faultSection);
      }
      rs.close();
    }
    catch (SQLException e) {
      throw new QueryException(e.getMessage());
    }
    return faultSectionList;
  }

}