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
 * ----------------
 * Contributor.java
 * ----------------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Dec-2001 : Version 1 (DG);
 * 28-Feb-2002 : Moved into package com.jrefinery.ui.about (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui.about;

/**
 * A simple class representing a contributor to a software project.
 * <P>
 * Used in the AboutFrame class.
 *
 * @author David Gilbert
 */
public class Contributor {

    /** The name. */
    private String name;

    /** The e-mail address. */
    private String email;

    /**
     * Creates a new contributor.
     *
     * @param name  the name.
     * @param email  the e-mail address.
     */
    public Contributor(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Returns the contributor's name.
     *
     * @return the contributor's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the contributor's e-mail address.
     *
     * @return the contributor's e-mail address.
     */
    public String getEmail() {
        return this.email;
    }

}
