package org.scec.gui.plot.jfreechart;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.function.DiscretizedFuncList;

/**
 * <p>Title: MagFreqDistDiscretizedFunction</p>
 * <p>Description:  Wrapper for a DiscretizedFuncList. This class extends the
 * DiscretizedFunctionXYDataSet to Zero data values in the function list to
 * Double.MIN_VALUE when plotting the Y-log in case of the MagFreqDist classes</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta          Date: Aug,20,2002
 * @version 1.0
 */

public class MagFreqDistDiscretizedFunctionXYDataSet extends DiscretizedFunctionXYDataSet {

  public MagFreqDistDiscretizedFunctionXYDataSet() {
    super();
  }


    /**
     * Returns the y-value for an item within a series.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The y-value for an item within a series.
     */
    public Number getYValue( int series, int item ) {

        if ( series < functions.size() ) {
            Object obj = functions.get( series );
            if( obj != null && obj instanceof DiscretizedFuncAPI){

                if( DiscretizedFunctionXYDataSet.isAdjustedIndexIfFirstXZero(( DiscretizedFuncAPI ) obj, xLog, yLog) )
                  ++item;

                // get the value
                double y = ( ( DiscretizedFuncAPI ) obj ).getY(item);

                // return if not NaN
                if(y == 0 && yLog) return (Number)(new Double(Double.MIN_VALUE));
                if( y != Double.NaN ) return (Number)(new Double(y));

            }
        }
        return null;
    }

}