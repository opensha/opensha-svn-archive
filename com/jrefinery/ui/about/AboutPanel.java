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
 * ---------------
 * AboutPanel.java
 * ---------------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Nov-2001 : Version 1 (DG);
 *
 */

package com.jrefinery.ui.about;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.jrefinery.ui.RefineryUtilities;

/**
 * A standard panel for displaying information about an application.
 */
public class AboutPanel extends JPanel {

    /**
     * Constructs a panel.
     * @param application The application name.
     * @param version The version.
     * @param info Other info.
     */
    public AboutPanel(String application, String version, String copyright, String info) {

        this(application, version, copyright, info, null);

    }

    /**
     * Constructs a panel.
     * @param application The application name.
     * @param version The version.
     * @param info Other info.
     * @param image To be implemented...
     */
    public AboutPanel(String application, String version, String copyright, String info,
                      Image image) {

        this.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 4));

        JPanel appPanel = new JPanel();
        Font f1 = new Font("Dialog", Font.BOLD, 14);
        JLabel appLabel = RefineryUtilities.createJLabel(application, f1, Color.black);
        appLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        appPanel.add(appLabel);

        JPanel verPanel = new JPanel();
        Font f2 = new Font("Dialog", Font.PLAIN, 12);
        JLabel verLabel = RefineryUtilities.createJLabel(version, f2, Color.black);
        verLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        verPanel.add(verLabel);

        JPanel copyrightPanel = new JPanel();
        JLabel copyrightLabel = RefineryUtilities.createJLabel(copyright, f2, Color.black);
        copyrightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        copyrightPanel.add(copyrightLabel);

        JPanel infoPanel = new JPanel();
        JLabel infoLabel = RefineryUtilities.createJLabel(info, f2, Color.black);
        infoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        infoPanel.add(infoLabel);


        panel.add(appPanel);
        panel.add(verPanel);
        panel.add(copyrightPanel);
        panel.add(infoPanel);

        this.add(panel, BorderLayout.NORTH);

    }

}