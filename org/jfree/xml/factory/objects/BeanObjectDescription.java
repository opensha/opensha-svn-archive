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
 * --------------------------
 * BeanObjectDescription.java
 * --------------------------
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.jfree.util.Log;

/**
 * An object-description for a bean object.
 *
 * @author Thomas Morgner
 */
public class BeanObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     */
    public BeanObjectDescription(Class className) {
        this(className, true);
    }

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     * @param init  set to true, to autmaoticly initialise the object description.
     * If set to false, the initialisation is elsewhere. 
     */
    public BeanObjectDescription(Class className, boolean init) {
        super(className);
        // now create some method descriptions ..

        if (init == false) {
            return;
        }

        Method[] methods = getObjectClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (Modifier.isPublic(m.getModifiers()) == false) {
                //Log.debug ("Is not Public: " + m);
                continue;
            }
            if (Modifier.isStatic(m.getModifiers()) == true) {
                //Log.debug ("Is Static: " + m);
                continue;
            }
            if (m.getParameterTypes().length != 1) {
                //Log.debug ("Wrong Parameters: " + m);
                continue;
            }

            if (m.getName().startsWith("set")) {
                try {
                    String propertyName = getPropertyName(m.getName());
                    findGetMethod(propertyName, m.getParameterTypes()[0]);
                    setParameterDefinition(propertyName,
                        m.getParameterTypes()[0]);
                }
                catch (NoSuchMethodException nsme) {
                    // ignore property if this is a read only property ...
                }
            }
            else {
                //Log.debug ("Wrong Name: " + m);
            }
        }
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        try {
            Object o = getObjectClass().newInstance();
            // now add the various parameters ...

            Iterator it = getParameterNames();
            while (it.hasNext()) {
                String name = (String) it.next();
                Method method = findSetMethod(name);
                Object parameterValue = getParameter(name);
                if (parameterValue == null) {
                    // Log.debug ("Parameter: " + name + " is null");
                }
                else {
                    method.invoke(o, new Object[]{parameterValue});
                }
            }
            return o;
        }
        catch (Exception e) {
            Log.error("Unable to invoke bean method", e);
        }
        return null;
    }

    /**
     * Finds a set method in the bean.
     *
     * @param parameterName  the parameter name.
     *
     * @return The method.
     *
     * @throws NoSuchMethodException if there is no set method.
     */
    private Method findSetMethod(String parameterName)
        throws NoSuchMethodException {
        return getObjectClass().getMethod(getSetterName(parameterName),
            new Class[]{getParameterDefinition(parameterName)});
    }

    /**
     * Finds a get method in the bean.
     *
     * @param parameterName  the paramater name.
     * @param retval  the return type.
     *
     * @return The method.
     *
     * @throws NoSuchMethodException if there is no get method.
     */
    private Method findGetMethod(String parameterName, Class retval)
        throws NoSuchMethodException {
        return getObjectClass().getMethod(getGetterName(parameterName, retval),
            new Class[0]);
    }

    /**
     * Returns the setter name.
     *
     * @param parameterName  the parameter name.
     *
     * @return The setter name.
     */
    private String getSetterName(String parameterName) {
        if (parameterName.length() == 0) {
            return "set";
        }

        StringBuffer b = new StringBuffer();
        b.append("set");
        b.append(Character.toUpperCase(parameterName.charAt(0)));
        if (parameterName.length() > 1) {
            b.append(parameterName.substring(1));
        }
        return b.toString();
    }

    /**
     * Returns the getter name.
     *
     * @param parameterName  the parameter name.
     * @param retval  the return type.
     *
     * @return The getter name.
     */
    private String getGetterName(String parameterName, Class retval) {
        String prefix = "get";
        if (Boolean.TYPE.equals(retval)) {
            prefix = "is";
        }

        if (parameterName.length() == 0) {
            return prefix;
        }

        StringBuffer b = new StringBuffer();
        b.append(prefix);
        b.append(Character.toUpperCase(parameterName.charAt(0)));
        if (parameterName.length() > 1) {
            b.append(parameterName.substring(1));
        }
        return b.toString();
    }

    /**
     * Gets a property name.
     *
     * @param methodName  the method name.
     *
     * @return The property name.
     */
    private String getPropertyName(String methodName) {
        if (methodName.length() < 3) {
            throw new IllegalArgumentException();
        }
        if (methodName.length() == 3) {
            return "";
        }
        StringBuffer b = new StringBuffer();
        b.append(Character.toLowerCase(methodName.charAt(3)));
        if (methodName.length() > 4) {
            b.append(methodName.substring(4));
        }
        return b.toString();
    }

    /**
     * Sets the parameters in the description to match the supplied object.
     *
     * @param o  the object (<code>null</code> not allowed).
     *
     * @throws ObjectFactoryException if there is a problem.
     */
    public void setParameterFromObject(Object o)
        throws ObjectFactoryException {
        if (o == null) {
            throw new NullPointerException("Given object is null");
        }
        Class c = getObjectClass();
        if (c.isInstance(o) == false) {
            throw new ObjectFactoryException("Object is no instance of " + c + "(is "
                + o.getClass() + ")");
        }

        Iterator it = getParameterNames();
        while (it.hasNext()) {
            String propertyName = (String) it.next();

            try {
                Class parameter = getParameterDefinition(propertyName);
                Method method = findGetMethod(propertyName, parameter);
                Object retval = method.invoke(o, new Object[]{});
                if (retval != null) {
                    setParameter(propertyName, retval);
                }
            }
            catch (Exception e) {
                Log.info("Exception on method invokation.", e);
            }

        }
    }
}
