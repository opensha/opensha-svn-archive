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
 * ProjectInfo.java
 * ----------------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (since 27-Jun-2002)
 * ---------------------------
 * 27-Jun-2002 : Added logo, updated source header and Javadocs (DG);
 * 08-Oct-2002 : Added set methods for most attributes. Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui.about;

import java.awt.Image;
import java.util.Iterator;
import java.util.List;

/**
 * A class for recording the basic information about a free or open source software project.
 *
 * @author David Gilbert
 */
public class ProjectInfo {

    /** The project name. */
    private String name;

    /** The project version. */
    private String version;

    /** The project copyright statement. */
    private String copyright;

    /** Further info about the project (typically a URL for the project's web page). */
    private String info;

    /** An optional project logo. */
    private Image logo;

    /** The name of the licence. */
    private String licenceName;

    /** The licence text. */
    private String licenceText;

    /** A list of contributors. */
    private List contributors;

    /** A list of libraries used by the project. */
    private List libraries;

    /**
     * Constructs an empty project info object.
     */
    public ProjectInfo() {
    }

    /**
     * Constructs a project info object.
     *
     * @param name  the name of the project.
     * @param version  the version.
     * @param info  other info (usually a URL).
     * @param logo  the project logo.
     * @param copyright  a copyright statement.
     * @param licenceName  the name of the licence that applies to the project.
     * @param licenceText  the text of the licence that applies to the project.
     */
    public ProjectInfo(String name,
                       String version,
                       String info,
                       Image logo,
                       String copyright,
                       String licenceName,
                       String licenceText) {

        this.name = name;
        this.version = version;
        this.info = info;
        this.logo = logo;
        this.copyright = copyright;
        this.licenceName = licenceName;
        this.licenceText = licenceText;

    }

    /**
     * Returns the project name.
     *
     * @return the project name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the project name.
     *
     * @param name  the project name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the project version.
     *
     * @return the project version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the project version.
     *
     * @param version  the project version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns other information about the project (typically a URL for the project home page).
     *
     * @return the project info.
     */
    public String getInfo() {
        return this.info;
    }

    /**
     * Sets the project info (typically a URL).
     *
     * @param info  the project info.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Returns the logo.
     *
     * @return the project logo.
     */
    public Image getLogo() {
        return this.logo;
    }

    /**
     * Sets the project logo.
     *
     * @param logo  the project logo.
     */
    public void setLogo(Image logo) {
        this.logo = logo;
    }

    /**
     * Returns the copyright statement.
     *
     * @return the copyright statement.
     */
    public String getCopyright() {
        return this.copyright;
    }

    /**
     * Sets the project copyright statement.
     *
     * @param copyright  the project copyright statement.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * Returns the licence name.
     *
     * @return the licence name.
     */
    public String getLicenceName() {
        return this.licenceName;
    }

    /**
     * Sets the project licence name.
     *
     * @param licenceName  the licence name.
     */
    public void setLicenceName(String licenceName) {
        this.licenceName = licenceName;
    }

    /**
     * Returns the licence text.
     *
     * @return the licence text.
     */
    public String getLicenceText() {
        return this.licenceText;
    }

    /**
     * Sets the project licence text.
     *
     * @param licenceText  the licence text.
     */
    public void setLicenceText(String licenceText) {
        this.licenceText = licenceText;
    }

    /**
     * Returns the list of contributors for the project.
     *
     * @return the list of contributors.
     */
    public List getContributors() {
        return this.contributors;
    }

    /**
     * Sets the list of contributors.
     *
     * @param contributors  the list of contributors.
     */
    public void setContributors(List contributors) {
        this.contributors = contributors;
    }

    /**
     * Returns a list of libraries used by the project.
     *
     * @return the list of libraries.
     */
    public List getLibraries() {
        return this.libraries;
    }

    /**
     * Sets the list of libraries.
     *
     * @param libraries  the list of libraries.
     */
    public void setLibraries(List libraries) {
        this.libraries = libraries;
    }

    /**
     * Returns a string describing the project.
     *
     * @return a string describing the project.
     */
    public String toString() {

        String result = getName() + " version " + getVersion() + ".\n";
        result = result + getCopyright() + ".\n";
        result = result + "\n";
        result = result + "For terms of use, see the licence below.\n";
        result = result + "\n";
        result = result + "FURTHER INFORMATION:";
        result = result + getInfo();
        result = result + "\n";
        result = result + "CONTRIBUTORS:";
        List contributors = getContributors();
        if (contributors != null) {
            Iterator iterator = getContributors().iterator();
            while (iterator.hasNext()) {
                Contributor contributor = (Contributor) iterator.next();
                result = result + contributor.getName() + " (" + contributor.getEmail() + ").";
            }
        }
        else {
            result = result + "None";
        }

        result = result + "\n";
        result = result + "OTHER LIBRARIES USED BY " + getName() + ":";
        List libraries = getLibraries();
        if (libraries != null) {
            Iterator iterator = getLibraries().iterator();
            while (iterator.hasNext()) {
                Library lib = (Library) iterator.next();
                result = result + lib.getName() + " "
                                + lib.getVersion() + " ("
                                + lib.getInfo() + ").";
            }
        }
        else {
            result = result + "None";
        }
        result = result + "\n";
        result = result + getName() + " LICENCE TERMS:";
        result = result + "\n";
        result = result + getLicenceText();

        return result;

    }

}
