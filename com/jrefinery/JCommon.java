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
 * ------------
 * JCommon.java
 * ------------
 * (C) Copyright 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-Feb-2002 : Version 1 (DG);
 * 22-Mar-2002 : Changed version number to 0.6.0 (DG);
 * 25-Mar-2002 : Moved the project info details into a class so that the text can be localised
 *               more easily (DG);
 * 04-Apr-2002 : Added Hari to contributors (DG);
 * 19-Apr-2002 : Added Sam (oldman) to contributors (DG);
 * 07-Jun-2002 : Added contributors (DG);
 * 24-Jun-2002 : Removed unnecessary imports (DG);
 * 27-Aug-2002 : Updated version number to 0.7.0 (DG);
 *
 */

package com.jrefinery;

import java.util.Arrays;
import java.util.ResourceBundle;
import com.jrefinery.ui.about.ProjectInfo;
import com.jrefinery.ui.about.Contributor;
import com.jrefinery.ui.about.Library;
import com.jrefinery.ui.about.Licences;

/**
 * This class contains static information about the JCommon class library.
 */
public class JCommon {

    /** Information about the project. */
    public static final ProjectInfo INFO = new JCommonInfo();

    /**
     * Prints information about JCommon to standard output.
     *
     * @param args  no arguments are honored.
     */
    public static void main(String[] args) {

        System.out.println(JCommon.INFO.toString());

    }

}

/**
 * Information about the JCommon project.  One instance of this class is
 * assigned to JCommon.INFO.
 */
class JCommonInfo extends ProjectInfo {

    public JCommonInfo() {

        // get a locale-specific resource bundle...
        String baseResourceClass = "com.jrefinery.resources.JCommonResources";
        ResourceBundle resources = ResourceBundle.getBundle(baseResourceClass);

        setName(resources.getString("project.name"));
        setVersion(resources.getString("project.version"));
        setInfo(resources.getString("project.info"));
        setCopyright(resources.getString("project.copyright"));

        setLicenceName("LGPL");
        setLicenceText(Licences.LGPL);

        setContributors(Arrays.asList(
            new Contributor[] {
                new Contributor("Anthony Boulestreau", "-"),
                new Contributor("Jeremy Bowman", "-"),
                new Contributor("J. David Eisenberg", "-"),
                new Contributor("Paul English", "-"),
                new Contributor("David Gilbert", "david.gilbert@object-refinery.com"),
                new Contributor("Hans-Jurgen Greiner", "-"),
                new Contributor("Achilleus Mantzios", "-"),
                new Contributor("Thomas Meier", "-"),
                new Contributor("Aaron Metzger", "-"),
                new Contributor("Krzysztof Paz", "-"),
                new Contributor("Nabuo Tamemasa", "-"),
                new Contributor("Mark Watson", "-"),
                new Contributor("Matthew Wright", "-"),
                new Contributor("Hari", "-"),
                new Contributor("Sam (oldman)", "-")
            }
        ));

        setLibraries(Arrays.asList(
            new Library[] {
                new Library("JUnit", "3.7", "IBM Public Licence", "http://www.junit.org/")
            }

        ));

    }

}
