package org.scec.gui.plot.jfreechart;

import com.jrefinery.chart.TickUnits;
import com.jrefinery.chart.NumberTickUnit;
import java.text.DecimalFormat;
/**
 * <p>Title: MyTickUnits</p>
 * <p>Description: This class has been made to generate the small tick units for the
 * JFreechart class TickUnit's function setStandardTickUnit size, which allows
 * the programmer to set the  tick unit</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Vipin Gupta & Nitin Gupta  Date:Aug, 23,2002
 * @version 1.0
 */

public class MyTickUnits {
  /**
     * Creates the standard tick units.
     * <P>
     * If you don't like these defaults, create your own instance of TickUnits and then pass it to
     * the setStandardTickUnits(...) method in the NumberAxis class.
     */
  public static TickUnits createStandardTickUnits() {

      TickUnits units = new TickUnits();

      // we can add the units in any order, the TickUnits collection will sort them...
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000001,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000001,    new DecimalFormat("0.0000000")));
      units.add(new NumberTickUnit(0.000001,     new DecimalFormat("0.000000")));
      units.add(new NumberTickUnit(0.00001,      new DecimalFormat("0.00000")));
      units.add(new NumberTickUnit(0.0001,       new DecimalFormat("0.0000")));
      units.add(new NumberTickUnit(0.001,        new DecimalFormat("0.000")));
      units.add(new NumberTickUnit(0.01,         new DecimalFormat("0.00")));
      units.add(new NumberTickUnit(0.1,          new DecimalFormat("0.0")));
      units.add(new NumberTickUnit(1,            new DecimalFormat("0")));
      units.add(new NumberTickUnit(10,           new DecimalFormat("0")));
      units.add(new NumberTickUnit(100,          new DecimalFormat("0")));
      units.add(new NumberTickUnit(1000,         new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(10000,        new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(100000,       new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(1000000,      new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(10000000,     new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(100000000,    new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(1000000000,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(100000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(1000000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(10000000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));

      units.add(new NumberTickUnit(0.000000000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000025,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000025,   new DecimalFormat("0.00000000")));
      units.add(new NumberTickUnit(0.0000025,    new DecimalFormat("0.0000000")));
      units.add(new NumberTickUnit(0.000025,     new DecimalFormat("0.000000")));
      units.add(new NumberTickUnit(0.00025,      new DecimalFormat("0.00000")));
      units.add(new NumberTickUnit(0.0025,       new DecimalFormat("0.0000")));
      units.add(new NumberTickUnit(0.025,        new DecimalFormat("0.000")));
      units.add(new NumberTickUnit(0.25,         new DecimalFormat("0.00")));
      units.add(new NumberTickUnit(2.5,          new DecimalFormat("0.0")));
      units.add(new NumberTickUnit(25,           new DecimalFormat("0")));
      units.add(new NumberTickUnit(250,          new DecimalFormat("0")));
      units.add(new NumberTickUnit(2500,         new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(25000,        new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(250000,       new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(2500000,      new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(25000000,     new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(250000000,    new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(2500000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(2500000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(25000000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(250000000000000000000000000000000000000000000.0,   new DecimalFormat("0.#E0")));

      units.add(new NumberTickUnit(0.0000000000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.000000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.00000005,    new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(0.0000005,    new DecimalFormat("0.0000000")));
      units.add(new NumberTickUnit(0.000005,     new DecimalFormat("0.000000")));
      units.add(new NumberTickUnit(0.00005,      new DecimalFormat("0.00000")));
      units.add(new NumberTickUnit(0.0005,       new DecimalFormat("0.0000")));
      units.add(new NumberTickUnit(0.005,        new DecimalFormat("0.000")));
      units.add(new NumberTickUnit(0.05,         new DecimalFormat("0.00")));
      units.add(new NumberTickUnit(0.5,          new DecimalFormat("0.0")));
      units.add(new NumberTickUnit(5L,           new DecimalFormat("0")));
      units.add(new NumberTickUnit(50L,          new DecimalFormat("0")));
      units.add(new NumberTickUnit(500L,         new DecimalFormat("0")));
      units.add(new NumberTickUnit(5000L,        new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(50000L,       new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(500000L,      new DecimalFormat("#,##0")));
      units.add(new NumberTickUnit(5000000L,     new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(50000000L,    new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(500000000L,   new DecimalFormat("#,###,##0")));
      units.add(new NumberTickUnit(5000000000L,  new DecimalFormat("#,###,###,##0")));
      units.add(new NumberTickUnit(50000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(50000000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(500000000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));
      units.add(new NumberTickUnit(5000000000000000000000000000000000000000000000.0,  new DecimalFormat("0.#E0")));




      return units;

  }

}