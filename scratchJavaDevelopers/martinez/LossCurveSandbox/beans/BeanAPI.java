package scratchJavaDevelopers.martinez.LossCurveSandbox.beans;

import java.io.Serializable;

/**
 * <p>
 * Declaring a class to be a &ldquo;Bean&rdquo; requires that it implement this
 * interface. Classes that do not implement this interface should not be
 * considered a bean and bean editors should not accept them as such. This
 * interface has no methods or fields and serves only to identify the semantics
 * of a bean.
 * </p>
 * <p>
 * To start, all beans should follow the official JavaBeans API specification
 * from Sun Mircosystems:<br />
 * <a href="http://java.sun.com/products/javabeans/docs/spec.html"
 *   title="JavaBeans">http://java.sun.com/products/javabeans/docs/spec.html</a>
 * <br />
 * Of principle interest is supporting introspection of the bean class and
 * exposing all member variables via getter and setter methods.
 * </p>
 * 
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 */
public interface BeanAPI extends Serializable {

}
