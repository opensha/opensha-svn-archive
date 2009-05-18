package org.opensha.nshmp.sha.calc.servlet;

import java.util.ArrayList;
import org.opensha.nshmp.sha.calc.HazardDataCalc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncList;
import org.opensha.data.function.*;
import java.lang.reflect.Method;

/**
 * <p>Title: HazardDataCalcServletHelper.java </p>
 * <p>Description: This class gets the function name and parameters and then
 * calls the corresponding function in HazardDataCalc using reflection. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class HazardDataCalcServletHelper {
	public Object getResult(String methodName, ArrayList objectsList) {
    boolean isCurrent = false;
	 try {
      HazardDataCalc hazardDataCalc = new HazardDataCalc();
	// Cycle these comments as you release new versions
		//if (methodName.endsWith("_V4") ) {
		//if (methodName.endsWith("_V5") ) {
		//if (methodName.endsWith("_V6") ) {
		//if (methodName.endsWith("_V7") ) {
		//if (methodName.endsWith("_V8") ) {
		if (methodName.endsWith("_V9") ) {
			methodName = methodName.substring(0, methodName.length() - 3);
			isCurrent = true;
		}

      Method method = hazardDataCalc.getClass().getMethod(methodName,
          getClasses(objectsList));

		if (isCurrent) {
      	return method.invoke(hazardDataCalc, getObjects(objectsList));
		} else {
      	Object o =  method.invoke(hazardDataCalc, getObjects(objectsList));
			if (o instanceof ArbitrarilyDiscretizedFunc) {
				ArbitrarilyDiscretizedFunc rtn = (ArbitrarilyDiscretizedFunc) o;
				rtn.setInfo("\n\n************************************************************\n" +
					"************************************************************\n" +
					"YOU ARE USING AN OLD VERSION OF THIS APPLICATION.\nPLEASE VISIT:\n\n" +
					"     http://earthquake.usgs.gov/research/hazmaps/design/\n\n" +
					"TO DOWNLOAD THE MOST RECENT VERSION OF THIS APPLICATION\n" +
					"************************************************************\n" +
					"************************************************************\n");
				return rtn;
			} else if (o instanceof DiscretizedFuncList) {
				DiscretizedFuncList rtn = (DiscretizedFuncList) o;
				rtn.setInfo("\n\n************************************************************\n" +
					"************************************************************\n" +
					"YOU ARE USING AN OLD VERSION OF THIS APPLICATION.\nPLEASE VISIT:\n\n" +
					"     http://earthquake.usgs.gov/research/hazmaps/design/\n\n" +
					"TO DOWNLOAD THE MOST RECENT VERSION OF THIS APPLICATION\n" +
					"************************************************************\n" +
					"************************************************************\n");
				return rtn;
			} else {
				return null;
			}
		}

    } catch (Exception e) {
      e.printStackTrace(System.out);
    	return null;
    } 
  }

  /**
   * Make object array from the arraylist
   * @param objectList
   * @return
   */
  private Object[] getObjects(ArrayList objectList) {
    Object[] objects  = new Object[objectList.size()];
    for(int i=0; i<objectList.size(); ++i)
      objects[i] = objectList.get(i);
    return objects;
  }
  /**
   * Return clas[] for passed object list
   * @param objectList
   * @return
   */
  private Class[] getClasses(ArrayList objectList) {
    Class []classTypes = new Class[objectList.size()];
    for(int i=0; i<objectList.size(); ++i) {
      Object obj = objectList.get(i);
      if(obj instanceof Double) classTypes[i] = Double.TYPE;
      else if(obj instanceof Float) classTypes[i] = Float.TYPE;
      else classTypes[i] = obj.getClass();
    }
    return classTypes;
  }
}
