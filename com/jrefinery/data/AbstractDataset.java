/* ==================================================
 * JCommon : a general purpose class library for Java
 * ==================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
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
 * AbstractDataset.java
 * --------------------
 * (C)opyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 21-Aug-2001)
 * --------------------------
 * 21-Aug-2001 : Added standard header. Fixed DOS encoding problem (DG);
 * 18-Sep-2001 : Updated e-mail address in header (DG);
 * 15-Oct-2001 : Moved to new package (com.jrefinery.data.*) (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 17-Nov-2001 : Changed constructor from public to protected, created new AbstractSeriesDataset
 *               class and transferred series-related methods, updated Javadoc comments (DG);
 * 04-Mar-2002 : Updated import statements (DG);
 * 11-Jun-2002 : Updated for change in the event constructor (DG);
 *
 */

package com.jrefinery.data;

import java.util.List;
import java.util.Iterator;

/**
 * An abstract implementation of the Dataset interface, containing a mechanism for registering
 * change listeners.
 */
public abstract class AbstractDataset implements Dataset {

    /** Storage for registered change listeners. */
    protected List listeners;

    /**
     * Constructs a dataset.
     */
    protected AbstractDataset() {
        this.listeners = new java.util.ArrayList();
    }

    /**
     * Registers an object for notification of changes to the dataset.
     *
     * @param listener The object to register.
     */
    public void addChangeListener(DatasetChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Deregisters an object for notification of changes to the dataset.
     *
     * @param listener The object to deregister.
     */
    public void removeChangeListener(DatasetChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the dataset has changed.
     */
    protected void fireDatasetChanged() {
        notifyListeners(new DatasetChangeEvent(this, // source
                                               this  // dataset
                                               ));
    }

    /**
     * Notifies all registered listeners that the dataset has changed.
     *
     * @param event Contains information about the event that triggered the notification.
     */
    protected void notifyListeners(DatasetChangeEvent event) {
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext()) {
            DatasetChangeListener listener = (DatasetChangeListener)iterator.next();
            listener.datasetChanged(event);
        }
    }

}






