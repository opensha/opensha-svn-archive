package org.scec.sha.earthquake;

import org.scec.data.NamedObjectAPI;

/**
 * <b>Title:</b> ProbEqkRuptureInfo <br>
 * <b>Description:</b> Javabean class to encapsulate information of an Potential
 * Earthquake. Right now the only fields are name and version. More may be
 * added in the future<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ProbEqkRuptureInfo implements NamedObjectAPI {

    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ProbEqkRuptureInfo";
    protected final static boolean D = false;

    private String name;
    private String version;


    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** No-arg constructor */
    public ProbEqkRuptureInfo() {
    }

    /** Constructor sets all fields */
    public ProbEqkRuptureInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

    public String getName() { return name; }
    public void setName(String newName) { name = newName; }

    public void setVersion(String newVersion) { version = newVersion; }
    public String getVersion() { return version; }

}
