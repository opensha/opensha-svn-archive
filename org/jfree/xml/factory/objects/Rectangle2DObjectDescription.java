/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ---------------------------------
 * rectangle2DObjectDescription.java
 * ---------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id$
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.awt.geom.Rectangle2D;

/**
 * An object-description for a <code>Rectangle2D</code> object.
 *
 * @author Thomas Morgner
 */
public class Rectangle2DObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public Rectangle2DObjectDescription() {
        super(Rectangle2D.class);
        setParameterDefinition("width", Float.class);
        setParameterDefinition("height", Float.class);
        setParameterDefinition("x", Float.class);
        setParameterDefinition("y", Float.class);
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        Rectangle2D rect = new Rectangle2D.Float();

        float w = getFloatParameter("width");
        float h = getFloatParameter("height");
        float x = getFloatParameter("x");
        float y = getFloatParameter("y");
        rect.setRect(x, y, w, h);
        return rect;
    }

    /**
     * Returns a parameter value as a float.
     *
     * @param param  the parameter name.
     *
     * @return The float value.
     */
    private float getFloatParameter(String param) {
        Float p = (Float) getParameter(param);
        if (p == null) {
            return 0;
        }
        return p.floatValue();
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>Rectangle2D</code>).
     *
     * @throws ObjectFactoryException if the object is not an instance of <code>Rectangle2D</code>.
     */
    public void setParameterFromObject(Object o) throws ObjectFactoryException {
        if (o instanceof Rectangle2D == false) {
            throw new ObjectFactoryException("The given object is no java.awt.geom.Rectangle2D.");
        }

        Rectangle2D rect = (Rectangle2D) o;
        float x = (float) rect.getX();
        float y = (float) rect.getY();
        float w = (float) rect.getWidth();
        float h = (float) rect.getHeight();

        setParameter("x", new Float(x));
        setParameter("y", new Float(y));
        setParameter("width", new Float(w));
        setParameter("height", new Float(h));
    }

}
