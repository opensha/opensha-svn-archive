package org.opensha.commons.data.function;

import java.awt.geom.Point2D;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Utility class for splitting a set of Point2D data into function bins by some scalar. Useful
 * for coloring many points by a CPT or scaling point size by magnitude without having a separate
 * dataset/plot charactersitc for each point.
 * @author kevin
 *
 */
public class XY_DatasetBinner {
	
	public static XY_DataSet[] bin(List<Point2D> points, List<Double> scalars,
			double min, int num, double delta) {
		return bin(points, scalars, new EvenlyDiscretizedFunc(min, num, delta));
	}
	
	public static XY_DataSet[] bin(List<Point2D> points, List<Double> scalars, EvenlyDiscretizedFunc binFunc) {
		Preconditions.checkArgument(points.size() == scalars.size());
		
		XY_DataSet[] ret = new XY_DataSet[binFunc.size()];
		
		for (int i=0; i<ret.length; i++)
			ret[i] = new DefaultXY_DataSet();
		
		for (int i=0; i<points.size(); i++) {
			double scalar = scalars.get(i);
			int index = binFunc.getClosestXIndex(scalar);
			ret[index].set(points.get(i));
		}
		
		return ret;
	}
	
}
