/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.faultSurface;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.ContainerSubset2D;
import org.opensha.commons.data.Window2D;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;

/**
 * <b>Title:</b> GriddedSubsetSurface<p>
 *
 * <b>Description:</b> This represents a subset of an EvenlyGriddedSurface
 * (as a pointer, not duplicated in memory)
 *
 * <b>Note:</b> This class is purely a convinience class that translates indexes so the
 * user can deal with a smaller window than the full GriddedSurface. Think of
 * this as a "ZOOM" function into a GriddedSurface.<p>
 *
 *
 * @see Window2D
 * @author     Steven W. Rock & revised by Ned Field
 * @created    February 26, 2002
 * @version    1.0
 */
public class GriddedSubsetSurface extends ContainerSubset2D<Location>  implements EvenlyGriddedSurface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// for distance measures
	Location siteLocForDistCalcs= new Location(Double.NaN,Double.NaN);
	Location siteLocForDistXCalc= new Location(Double.NaN,Double.NaN);
	double distanceJB, distanceSeis, distanceRup, distanceX;
	AbstractEvenlyGriddedSurface parentSurface;


    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Specifies the length of the window.
     * @param  numCols                             Specifies the height of the window
     * @param  startRow                            Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @param  data                                The main GriddedSurface this is a window into
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol, AbstractEvenlyGriddedSurface data )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol, data );
        parentSurface = data;
    }
    


    /** Add a Location to the grid. This method throws UnsupportedOperationException as it is disabled. */
    public void setLocation( int row, int col,
            org.opensha.commons.geo.Location location ) {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /** Proxy method that returns the number of rows in the main GriddedSurface. */
    public int getMainNumRows() {
        return data.getNumRows();
    }


    /** Proxy method that returns the number of colums in the main GriddedSurface. */
    public int getMainNumCols() {
        return data.getNumCols();
    }


     @Override
    public double getAveStrike() {
        return getEvenlyDiscritizedUpperEdge().getAveStrike();
    }
     
    
     @Override
    public LocationList getEvenlyDiscritizedListOfLocsOnSurface() {
      LocationList locList = new LocationList();
      Iterator<Location> it = listIterator();
      while(it.hasNext()) locList.add((Location)it.next());
      return locList;

    }


    /**
     *  Proxy method that returns the aveDip of the main GriddedSurface. <P>
     *
     *  This should actually be recomputed if the main surface is a
     *  SimpleListricGriddedSurface.
     */
     @Override
    public double getAveDip() {
        return ( ( AbstractEvenlyGriddedSurfaceWithSubsets ) data ).getAveDip();
    }
     

    /** Debug string to represent a tab. Used by toString().  */
    final static char TAB = '\t';


    @Override
    public double getAveLength() {
        return getGridSpacingAlongStrike() * (getNumCols()-1);
    }


    @Override
    public double getAveWidth() {
      return getGridSpacingDownDip() * (getNumRows()-1);
    }
    

    @Override
    public LocationList getEvenlyDiscritizedPerimeter() {
    	return GriddedSurfaceUtils.getEvenlyDiscritizedPerimeter(this);
    }



    /**
     * returns the grid spacing along strike
     *
     * @return
     */
    public double getGridSpacingAlongStrike() {
      return ((AbstractEvenlyGriddedSurface)data).getGridSpacingAlongStrike();
    }


    /**
     * returns the grid spacing down dip
     *
     * @return
     */
    public double getGridSpacingDownDip() {
      return ((AbstractEvenlyGriddedSurface)data).getGridSpacingDownDip();
    }
    
	/**
	 * this tells whether along-strike and down-dip grid spacings are the same
	 * @return
	 */
	public Boolean isGridSpacingSame() {
		return ((AbstractEvenlyGriddedSurface)data).isGridSpacingSame();
	}

 
	@Override
   public double getArea() {
      return getAveWidth()*getAveLength();
    }

	
	public Location getLocation(int row, int column) {
		return get(row, column);
	}
	

	@Override
	public ListIterator<Location> getLocationsIterator() {
		return listIterator();
	}
	
	
	public FaultTrace getRowAsTrace(int row) {
		FaultTrace trace = new FaultTrace(null);
		for(int col=0; col<getNumCols(); col++)
			trace.add(get(row, col));
		return trace;
	}


	@Override
	public double getAveDipDirection() {
		return ( ( AbstractEvenlyGriddedSurface) data ).getAveDipDirection();
	}


	@Override
	public double getAveGridSpacing() {
		return ( ( AbstractEvenlyGriddedSurface) data ).getAveGridSpacing();
	}


	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		return getRowAsTrace(0);
	}


	@Override
	public Location getFirstLocOnUpperEdge() {
		return get(0,0);
	}


	@Override
	public Location getLastLocOnUpperEdge() {
		// TODO Auto-generated method stub
		return get(0,getNumCols()-1);
	}


	@Override
	/**
	 * This assumes the lateral edges are straight lines
	 */
	public LocationList getPerimeter() {
		FaultTrace topTr = getRowAsTrace(0);
		FaultTrace botTr = getRowAsTrace(getNumRows()-1);
		botTr.reverse();
		LocationList list = new LocationList();
		list.addAll(topTr);
		list.addAll(botTr);
		list.add(topTr.get(0));
		return list;
	}


	/**
	 * Returns same as getEvenlyDiscritizedUpperEdge()
	 */
	@Override
	public FaultTrace getUpperEdge() {
		return getEvenlyDiscritizedUpperEdge();
	}
	
	private void setPropagationDistances() {
		double[] dists = GriddedSurfaceUtils.getPropagationDistances(this, siteLocForDistCalcs);
		distanceRup = dists[0];
		distanceJB = dists[1];
		distanceSeis = dists[2];
	}
	
	
	/**
	 * This returns rupture distance (kms to closest point on the 
	 * rupture surface), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return 
	 */
	public double getDistanceRup(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceRup;
	}

	/**
	 * This returns distance JB (shortest horz distance in km to surface projection 
	 * of rupture), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceJB(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceJB;
	}

	/**
	 * This returns "distance seis" (shortest distance in km to point on rupture 
	 * deeper than 3 km), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceSeis(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceSeis;
	}

	/**
	 * This returns distance X (the shortest distance in km to the rupture 
	 * trace extended to infinity), where values >= 0 are on the hanging wall
	 * and values < 0 are on the foot wall.  The location is assumed to be at zero
	 * depth (for numerical expediency).
	 * @return
	 */
	public double getDistanceX(Location siteLoc){
		if(!siteLocForDistXCalc.equals(siteLoc)) {
			siteLocForDistXCalc = siteLoc;
			distanceX = GriddedSurfaceUtils.getDistanceX(getEvenlyDiscritizedUpperEdge(), siteLocForDistCalcs);
		}
		return distanceX;
	}
	

	@Override
	public double getAveRupTopDepth() {
		if (this.data instanceof EvenlyGriddedSurfFromSimpleFaultData) // all depths are the same on the top row
			return getLocation(0,0).getDepth();
		else {
			double depth = 0;
			FaultTrace topTrace = getRowAsTrace(0);
			for(Location loc:topTrace)
				depth += loc.getDepth();
			return depth/topTrace.size();
		}
	}

	@Override
	public double getFractionOfSurfaceInRegion(Region region) {
		double numInside=0;
		for(Location loc: this) {
			if(region.contains(loc))
				numInside += 1;
		}
		return numInside/size();
	}
	
	@Override
	public String getInfo() {
	      return GriddedSurfaceUtils.getSurfaceInfo(this);
	}
	
	@Override
	public boolean isPointSurface() {
		return (size() == 1);
	}


	/**
	 * This returns the parent surface
	 * @return
	 */
	public AbstractEvenlyGriddedSurface getParentSurface() {
		return parentSurface;
	}


}
