/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
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
 * -----------------------------
 * BooleanObjectDescription.java
 * -----------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
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

/**
 * An object-description for a <code>Boolean</code> object.
 *
 * @author Thomas Morgner
 */
public class BooleanObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public BooleanObjectDescription() {
        super(Boolean.class);
        setParameterDefinition("value", String.class);
    }

    /**
     * Creates a new <code>Boolean</code> based on the settings of this description object.
     *
     * @return A <code>Boolean</code>.
     */
    public Object createObject() {
        String o = (String) getParameter("value");
        return Boolean.valueOf(o);
    }

    /**
     * Sets the description object parameters to match the supplied object
     * (which should be an instance of <code>Boolean</code>.
     *
     * @param o  the object.
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(Object o) throws ObjectFactoryException {
        if (o instanceof Boolean == false) {
            throw new ObjectFactoryException("The given object is no java.lang.Boolean. ");
        }
        setParameter("value", String.valueOf(o));
    }
}
