package scratch.kevin.magDepth;

import java.awt.geom.Point2D;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.exceptions.Point2DException;

public class NoCollissionFunc extends ArbitrarilyDiscretizedFunc {

	@Override
	public void set(Point2D point) throws Point2DException {
		while (hasPoint(point)) {
			point = new Point2D.Double(point.getX()+1e-14, point.getY());
		}
		super.set(point);
	}

}
