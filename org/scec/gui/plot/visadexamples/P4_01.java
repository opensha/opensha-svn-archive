package org.scec.gui.plot.visadexamples;

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

  /**
  VisAD Tutorial example 4_01
  Like example 3_09 but using a 3D display
  We have the functions altitude = h(latitude, longitude)
  			temperature = f(latitude, longitude)

  represented by the MathType
  ( (latitude, longitude) -> (altitude, temperature ) )
  Map the altitude to ZAxis and temperature to RGB
  Run program with "java P4_01"
 */



public class P4_01{

// Declare variables
  // The domain quantities longitude and latitude
  // and the dependent quantities altitude, temperature

  private RealType longitude, latitude;
  private RealType altitude, temperature,  precipitation;

  // Two Tuples: one to pack longitude and latitude together, as the domain
  // and the other for the range (altitude, temperature)

  private RealTupleType domain_tuple, range_tuple;


  // The function (domain_tuple -> range_tuple )

  private FunctionType func_domain_range;


   // Our Data values for the domain are represented by the Set

  private Set domain_set;


  // The Data class FlatField

  private FlatField vals_ff;

  // The DataReference from data to display

  private DataReferenceImpl data_ref;

  // The 2D display, and its the maps

  private DisplayImpl display;
  private ScalarMap latMap, lonMap;
  private ScalarMap altZMap, tempRGBMap;
  private ScalarMap tempZMap, altRGBMap;


  public P4_01(String []args)
    throws RemoteException, VisADException {

    // Create the quantities
    // Use RealType(String name);

    latitude = new RealType("latitude",SI.meter,null);
    longitude = new RealType("longitude",SI.meter,null);

    domain_tuple = new RealTupleType(latitude, longitude);

    temperature = new RealType("temperature",SI.kelvin,null);
    altitude = new RealType("altitude",SI.meter,null);

    // Create the range tuple ( altitude, temperature )
    // Use RealTupleType( RealType[] )

    range_tuple = new RealTupleType( altitude, temperature  );


    // Create a FunctionType (domain_tuple -> range_tuple )
    // Use FunctionType(MathType domain, MathType range)

    func_domain_range = new FunctionType( domain_tuple, range_tuple);

    // Create the domain Set
    // Use LinearDSet(MathType type, double first1, double last1, int lengthX,
    //				     double first2, double last2, int lengthY)

    int NCOLS = 50;
    int NROWS = NCOLS;

    domain_set = new Linear2DSet(domain_tuple, -Math.PI, Math.PI, NROWS,
    					       -Math.PI, Math.PI, NCOLS);




    // Get the Set samples to facilitate the calculations

    float[][] set_samples = domain_set.getSamples( true );


    // We create another array, with the same number of elements of
    // altitude and temperature, but organized as
    // float[2][ number_of_samples ]

    float[][] flat_samples = new float[2][NCOLS * NROWS];

    // ...and then we fill our 'flat' array with the generated values
    // by looping over NCOLS and NROWS

    for(int c = 0; c < NCOLS; c++)

      for(int r = 0; r < NROWS; r++){

	// ...altitude
	flat_samples[0][ c * NROWS + r ] = 01.0f / (float)( (set_samples[0][ c * NROWS + r ] *
						     set_samples[0][ c * NROWS + r ]) +
						     (set_samples[1][ c * NROWS + r ] *
						     set_samples[1][ c * NROWS + r ]) + 1.0f );

	// ...temperature
	flat_samples[1][ c * NROWS + r ] = (float)( (Math.sin( 0.50*(double) set_samples[0][ c * NROWS + r ])  ) * Math.cos( (double) set_samples[1][ c * NROWS + r ] ) ) ;


    }


    // Create a FlatField
    // Use FlatField(FunctionType type, Set domain_set)

    vals_ff = new FlatField( func_domain_range, domain_set);

    // ...and put the values above into it

    // Note the argument false, meaning that the array won't be copied

    vals_ff.setSamples( flat_samples , false );

    // Create Display and its maps

    // A 2D display

    display = new DisplayImplJ3D("display1");

    // Get display's graphics mode control and draw scales

    GraphicsModeControl dispGMC = (GraphicsModeControl)  display.getGraphicsModeControl();
    dispGMC.setScaleEnable(true);


    // Create the ScalarMaps: latitude to YAxis, longitude to XAxis and
    // altitude to ZAxis and temperature to RGB
    // Use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)

    latMap = new ScalarMap( latitude,    Display.YAxis );
    lonMap = new ScalarMap( longitude, Display.XAxis );

    // Add maps to display

    display.addMap( latMap );
    display.addMap( lonMap );

     // altitude to z-axis and temperature to color

    altZMap = new ScalarMap( altitude,  Display.ZAxis );
    //tempRGBMap = new ScalarMap( temperature,  Display.RGB );
    // Add maps to display
    display.addMap( altZMap );
    //display.addMap( tempRGBMap );


    // Uncomment following lines to have different data depiction
    // temperature to z-axis and altitude to color

    altRGBMap = new ScalarMap( altitude,  Display.RGB );
    //tempZMap = new ScalarMap( temperature,  Display.ZAxis );
    display.addMap( altRGBMap );
    //display.addMap( tempZMap );


    // Create a data reference and set the FlatField as our data

    data_ref = new DataReferenceImpl("data_ref");

    data_ref.setData( vals_ff );

    // Add reference to display

    display.addReference( data_ref );


    // Create application window and add display to window

    JFrame jframe = new JFrame("VisAD Tutorial example 4_01");
    jframe.getContentPane().add(display.getComponent());


    // Set window size and make it visible

    jframe.setSize(300, 300);
    jframe.setVisible(true);


  }


  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new P4_01(args);
  }

} //end of Visad Tutorial Program 4_01
