package org.scec.sha.fault;

import java.util.*;
import org.scec.exceptions.*;

/**
 * <p>Title: FaultTraceList</p>
 * <p>Description: List container for a collection of Fault Traces</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Steven W. Rock
 * @version 1.0
 */

public class FaultTraceList {

    protected final static String C = "FaultTraceList";
    protected final static boolean D = false;

    /**
     *  Contains the list of Locations
     */
    protected ArrayList faultTraces = new ArrayList();
    protected HashMap map = new HashMap();


    /**
     *  Returns parameter at the specified index if exist, else throws
     *  exception. Recall that these faultTraces are stored in a Vector, which is
     *  like an array. Therefore you can access items by index.
     *
     * @param  index  Description of the Parameter
     * @return        The faultTraceAt value
     */
    public FaultTrace getFaultTraceAt( int index ) throws InvalidRangeException {
        checkIndex(index);
        return (FaultTrace)faultTraces.get( index );
    }


    /**
     *  Returns the named fault trace
     *
     * @param  index  Description of the Parameter
     * @return        The faultTraceAt value
     */
    public FaultTrace getFaultTrace( String name)  {
        return (FaultTrace)map.get( name );
    }


    private void checkIndex(int index) throws InvalidRangeException {

        if( size() < index + 1 ) throw new InvalidRangeException(
            C + ": getFaultTraceAt(): " +
            "Specified index larger than array size."
        );

    }

    /**
     *  adds the parameter if it doesn't exist, else throws exception
     *
     * @param  faultTrace  Description of the Parameter
     * @param  index     Description of the Parameter
     */
    public void replaceFaultTraceAt( FaultTrace faultTrace, int index ) throws InvalidRangeException  {
        checkIndex(index);
        FaultTrace f1 = this.getFaultTraceAt( index );
        map.remove( f1.getName() );
        faultTraces.add(index, faultTrace);
        map.put( faultTrace.getName(), faultTrace );
    }


    /**
     *  adds the parameter to the end of the list
     *
     * @param  faultTrace  The feature to be added to the FaultTrace attribute
     */
    public void addFaultTrace( FaultTrace faultTrace ) {
        map.put( faultTrace.getName(), faultTrace );
        faultTraces.add(faultTrace);
    }


    /**
     *  Returns a list iterator of all Fault Traces in this list, in the order they
     *  were added to the list
     *
     * @return    list iterator over fault traces
     */
    public ListIterator listIterator() {
        return faultTraces.listIterator();
    }


    /**
     *  Removes all Fault Traces from this list
     */
    public void clear() {
        faultTraces.clear();
        map.clear();
    }


    /**
     *  Returns the number of Fault Traces in this list
     *
     * @return    number of fault traces
     */
    public int size() { return faultTraces.size(); }


    private final static String TAB = "  ";
    public String toString(){

        StringBuffer b = new StringBuffer(C);
        b.append('\n');
        b.append(TAB + "Size = " + size());

        ListIterator it = listIterator();
        while( it.hasNext() ){

            FaultTrace trace = (FaultTrace)it.next();
            b.append('\n' + TAB + trace.toString());
        }

        return b.toString();

    }

}
