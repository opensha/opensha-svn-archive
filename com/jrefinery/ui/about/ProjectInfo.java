package com.jrefinery.ui.about;

import java.util.List;
import java.util.Iterator;

/**
 * A class for recording the basic information about an open source software project.
 */
public class ProjectInfo {

    /** The project name. */
    protected String name;

    /** The project version. */
    protected String version;

    /** The project copyright statement. */
    protected String copyright;

    /** Further info about the project (typically a URL for the project's web page). */
    protected String info;

    /** The name of the licence. */
    protected String licenceName;

    /** The licence text. */
    protected String licenceText;

    protected List contributors;

    protected List libraries;

    /**
     * Constructs an empty project info object.
     */
    public ProjectInfo() {
    }

    /**
     * Constructs a project info object.
     */
    public ProjectInfo(String name,
                       String version,
                       String info,
                       String copyright,
                       String licence) {
    }

    /**
     * Returns the project name.
     */
    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getInfo() {
        return this.info;
    }

    public String getCopyright() {
        return this.copyright;
    }

    public String getLicenceName() {
        return this.licenceName;
    }

    public String getLicenceText() {
        return this.licenceText;
    }

    public List getContributors() {
        return this.contributors;
    }

    public List getLibraries() {
        return this.libraries;
    }

    /**
     * Returns a string describing the project.
     */
    public String toString() {

        String result = this.getName()+" version "+this.getVersion()+".\n";
        result = result+this.getCopyright()+".\n";
        result = result+"\n";
        result = result+"For terms of use, see the licence below.\n";
        result = result+"\n";
        result = result+"FURTHER INFORMATION:";
        result = result+this.getInfo();
        result = result+"\n";
        result = result+"CONTRIBUTORS:";
        List contributors = this.getContributors();
        if (contributors!=null) {
            Iterator iterator = this.getContributors().iterator();
            while (iterator.hasNext()) {
                Contributor contributor = (Contributor)(iterator.next());
                result = result+contributor.getName()+" ("+contributor.getEmail()+").";
            }
        }
        else {
            result = result+"None";
        }

        result = result+"\n";
        result = result+"OTHER LIBRARIES USED BY JCOMMON:";
        List libraries = this.getLibraries();
        if (libraries!=null) {
            Iterator iterator = this.getLibraries().iterator();
            while (iterator.hasNext()) {
                Library lib = (Library)(iterator.next());
                result = result+lib.getName()+" "+lib.getVersion()+" ("+lib.getInfo()+").";
            }
        }
        else {
            result = result+"None";
        }
        result = result+"\n";
        result = result+"JCOMMON LICENCE TERMS:";
        result = result+"\n";
        result = result+this.getLicenceText();

        return result;

    }

}