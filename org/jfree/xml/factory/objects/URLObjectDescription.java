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
 * --------------------
 * URLClassFactory.java
 * --------------------
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

import java.net.URL;

import org.jfree.io.IOUtils;
import org.jfree.util.Log;
import org.jfree.xml.Parser;

/**
 * An object-description for a <code>URL</code> object.
 *
 * @author Thomas Morgner
 */
public class URLObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public URLObjectDescription() {
        super(URL.class);
        setParameterDefinition("value", String.class);
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        String o = (String) getParameter("value");
        String baseURL = getConfig().getConfigProperty(Parser.CONTENTBASE_KEY);
        try {
            try {
                URL bURL = new URL(baseURL);
                return new URL(bURL, o);
            }
            catch (Exception e) {
                Log.warn("BaseURL is invalid: ", e);
            }
            return new URL(o);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>URL</code>).
     *
     * @throws ObjectFactoryException if the object is not an instance of <code>URL</code>.
     */
    public void setParameterFromObject(Object o) throws ObjectFactoryException {
        if (o instanceof URL == false) {
            throw new ObjectFactoryException("Is no instance of java.net.URL");
        }

        URL comp = (URL) o;
        String baseURL = getConfig().getConfigProperty(Parser.CONTENTBASE_KEY);
        try {
            URL bURL = new URL(baseURL);
            setParameter("value", IOUtils.getInstance().createRelativeURL(comp, bURL));
        }
        catch (Exception e) {
            Log.warn("BaseURL is invalid: ", e);
        }
        setParameter("value", comp.toExternalForm());
    }

}
