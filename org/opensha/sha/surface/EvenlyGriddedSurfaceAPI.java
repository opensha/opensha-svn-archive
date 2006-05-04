package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;
import org.opensha.data.Location;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.data.*;

/**
 * <b>Title:</b> EvenlyGriddedSurfaceAPI<p>
 * <b>Description:</b>
 *
 * The EvenlyGriddedSurfaceAPI represents a geographical
 * surface of Location objects slicing through or on the surface of the earth.
 * Recall that a Container2DAPI represents a collection of Objects in
 * a matrix, or grid, accessed by row and column inedexes. All GriddedSurfaces
 * do is to constrain the object at each grid point to be a Location object.
 * There are also additional calculation methods specific to the paradigm
 * model, such as aveDip, aveStrike, etc. that depends on the grid objects
 * being Location objects. <p>
 *
 * There are no constraints on what locations are put where, but the usual presumption
 * is that the the grid of locations map out the surface in some evenly space way.
 * it is also presumed that the zeroeth row represent the top edge (or trace). <p>
 *
 * @author     Steven W. Rock & others
 * @created    February 26, 2002
 * @version    1.0
 */
public interface EvenlyGriddedSurfaceAPI extends GriddedSurfaceAPI {


    /**
     * returns the grid spacing
     *
     * @return
     */
    public  double getGridSpacing() ;


}
