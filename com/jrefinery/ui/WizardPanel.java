/* ================================================================
 * JCommon : a general purpose, open source, class library for Java
 * ================================================================
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
 * ----------------
 * WizardPanel.java
 * ----------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 *
 */

package com.jrefinery.ui;

import java.awt.*;
import javax.swing.*;

/**
 * A panel that provides the user interface for a single step in a WizardDialog.
 */
public abstract class WizardPanel extends JPanel {

    /** The owner. */
    protected WizardDialog owner;

    /**
     * Standard constructor.
     */
    protected WizardPanel(LayoutManager layout) {
        super(layout);
        this.owner = owner;
    }

    /**
     * Returns a reference to the dialog that owns the panel.
     */
    public WizardDialog getOwner() {
        return this.owner;
    }

    /**
     * Sets the reference to the dialog that owns the panel (this is called automatically by the
     * dialog when the panel is added to the dialog).
     */
    public void setOwner(WizardDialog owner) {
        this.owner = owner;
    }

    /** */
    public Object getResult() {
        return null;
    }

    /**
     * This method is called when the dialog redisplays this panel as a result of the user clicking
     * the "Previous" button.  Inside this method, subclasses should make a note of their current
     * state, so that they can decide what to do when the user hits "Next".
     */
    public abstract void returnFromLaterStep();

    /**
     * Returns true if it is OK to redisplay the last version of the next panel, or false if a new
     * version is required.
     */
    public abstract boolean canRedisplayNextPanel();

    /** */
    public abstract boolean hasNextPanel();

    /** */
    public abstract boolean canFinish();

    /**
     * Returns the next panel in the sequence, given the current user input.  Returns null if this
     * panel is the last one in the sequence.
     */
    public abstract WizardPanel getNextPanel();

}