package org.scec.gui.ned;

/**
 * <b>Title:<b><br>
 * <b>Description:<b>  Auxiliary class to hold the data read in from the file.
 * Should be declared as a private subclass in future, so no extra file "DataPont.class"
 * is generated. Private subclasses will become available in Version 1.1 of Java<br>
 * <b>Copyright:<b>    Copyright (c) 2001<br>
 * <b>Company:<b>      <br>
 * @author
 * @version 1.0
 */

public /**  */

class DataPoint
{
    public float x,dx,y,dy;
    public DataPoint (Float X,Float DX,Float Y,Float DY)
    {
      if (X  != null) x  = X.floatValue();
      if (DX != null) dx = DX.floatValue();
      if (Y  != null) y  = Y.floatValue();
      if (DY != null) dy = DY.floatValue();
    }
}
