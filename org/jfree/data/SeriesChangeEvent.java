/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * ----------------------
 * SeriesChangeEvent.java
 * ----------------------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 15-Nov-2001 : Version 1 (DG);
 * 18-Aug-2003 : Implemented Serializable (DG);
 *
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.EventObject;

/**
 * An event with details of a change to a series.
 *
 * @author David Gilbert
 */
public class SeriesChangeEvent extends EventObject implements Serializable {

    /**
     * Constructs a new event.
     *
     * @param source  the source of the change event.
     */
    public SeriesChangeEvent(Object source) {
        super(source);
    }

}
