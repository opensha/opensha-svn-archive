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
 * ---------------
 * AboutPanel.java
 * ---------------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Nov-2001 : Version 1 (DG);
 * 27-Jun-2002 : Added logo (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.ui.RefineryUtilities;

/**
 * A standard panel for displaying information about an application.
 *
 * @author David Gilbert
 */
public class AboutPanel extends JPanel {

    /**
     * Constructs a panel.
     *
     * @param application  the application name.
     * @param version  the version.
     * @param copyright  the copyright statement.
     * @param info  other info.
     */
    public AboutPanel(String application, String version, String copyright, String info) {

        this(application, version, copyright, info, null);

    }

    /**
     * Constructs a panel.
     *
     * @param application  the application name.
     * @param version  the version.
     * @param copyright  the copyright statement.
     * @param info  other info.
     * @param logo  an optional logo.
     */
    public AboutPanel(String application, String version, String copyright, String info,
                      Image logo) {

        setLayout(new BorderLayout());

        JPanel textPanel = new JPanel(new GridLayout(4, 1, 0, 4));

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

        textPanel.add(appPanel);
        textPanel.add(verPanel);
        textPanel.add(copyrightPanel);
        textPanel.add(infoPanel);

        add(textPanel);

        if (logo != null) {
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(logo)));
            imagePanel.setBorder(BorderFactory.createLineBorder(Color.black));
            JPanel imageContainer = new JPanel(new BorderLayout());
            imageContainer.add(imagePanel, BorderLayout.NORTH);
            add(imageContainer, BorderLayout.WEST);
        }

    }

}
