package org.scec.sha.fault.demo;

import org.scec.sha.fault.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class GriddedFaultDemo {

    protected final static String C = "GriddedFaultDemo";
    protected final static boolean D = true;

    StirlingGriddedFaultFactory factory = null;

    public GriddedFaultDemo() {

        String S = C + " ; (): ";
        if( D ) System.out.println(S + "Starting");

        if( D ) System.out.println(S + "Getting Sierra Madre fault trace");
        FaultTrace sierraMadre = FaultTraceFactory.getSierraMadre();

        if( D ) System.out.println(S + "Creating StirlingGriddedFaultFactory with Sierra Madre fault");
        factory = new StirlingGriddedFaultFactory(
            sierraMadre,
            new Double( 45 ),
            new Double( 0 ),
            new Double( 13),
            new Double( 1 )
        );


        if( D ) System.out.println(S + "Factory creating gridded surface for Sierra Madre Fault");
        GriddedSurfaceAPI surface = factory.getGriddedSurface();

        if( D ) System.out.println(S + "Surface = " + surface.toString());
        if( D ) System.out.println(S + "Ending");


    }
    public static void main(String[] args) {
        GriddedFaultDemo griddedFaultDemo1 = new GriddedFaultDemo();
    }
}
