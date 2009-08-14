package org.opensha.commons.data.region;

/**
 * This class represents a subset of a <code>GriddedRegion</code>. A
 * <code>GriddedSubRegion</code> contains a reference to a
 * parent <code>GriddedRegion</code> from which it retrieves node
 * <code>Location</code> data. <img style="padding: 30px 40px; float: right;" 
 * src="{@docRoot}/img/gridded_regions_border.jpg"/> If any sub-region 
 * extends beyond the border of its parent region, it is clipped on 
 * initialization. In the adjacent figure, the dashed line represents 
 * the border of the parent <code>GriddedRegion</code>. The light gray 
 * dots mark the <code>Location</code>s of nodes outside the parent region,
 * the black dots those inside the parent region, and pink dots those within
 * the sub-regions.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class GriddedSubRegion extends EvenlyGriddedGeographicRegion {

	private EvenlyGriddedGeographicRegion parent;
	
	/**
	 * @param region 
	 * @param parent
	 * @throws NullPointerException if <code>parent</code> or 
	 * 		<code>region</code> are <code>null</code>
	 */
	public GriddedSubRegion(
			GeographicRegion region,
			EvenlyGriddedGeographicRegion parent) {
		
//		super(regionCheck(region), )
		if (parent == null || region == null) {
			throw new NullPointerException();
		}
		
//		private static
		//else if (checkRegion())
	}
}
