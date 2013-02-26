package scratch.kevin.magDepth;

import java.awt.geom.Point2D;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.exceptions.Point2DException;

public class NoCollissionFunc extends ArbitrarilyDiscretizedFunc {

	private long collisions = 0;
	@Override
	public void set(Point2D point) throws Point2DException {
		double origX = point.getX();
		double incr = origX / 1000000;
		if (origX == 0)
			incr = 1e-14;
//		long cnt = 0;
		while (hasPoint(point)) {
			point = new Point2D.Double(point.getX()+Math.random()*incr, point.getY());
//			point = new Point2D.Double(point.getX()+1e-14, point.getY());
			collisions++;
//			cnt++;
//			System.out.println("Collision: "+collisions+" ("+cnt+")\t"+origX+"\t"+point.getX());
		}
		super.set(point);
	}
	
	public long getNumCollisions() {
		return collisions;
	}

}
