package org.opensha.refFaultParamDb.dao.db;

import java.util.ArrayList;

import com.sun.rowset.CachedRowSetImpl;

/**
 * <p>Title: SpatialQueryResult.java </p>
 * <p>Description: This class can be used to return the results of spatial queries.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SpatialQueryResult implements java.io.Serializable{
  private CachedRowSetImpl cachedRowSetImpl;
  private ArrayList geomteryObjectsList = new ArrayList();

  public SpatialQueryResult() {
  }

  public void setCachedRowSet(CachedRowSetImpl cachedRowSetImpl) {
    this.cachedRowSetImpl = cachedRowSetImpl;
  }

  public ArrayList getGeometryObjectsList(int index) {
    return (ArrayList)geomteryObjectsList.get(index);
  }

  public void add(ArrayList geomteryObjects) {
    geomteryObjectsList.add(geomteryObjects);
  }

  public CachedRowSetImpl getCachedRowSet() {
    return this.cachedRowSetImpl;
  }



}