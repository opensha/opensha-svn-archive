package org.scec.sha.imr.attenRelImpl.gui;

import java.util.*;

/**
 *  <b>Title:</b> IMRGuiList<p>
 *
 *  <b>Description:</b> IMRGuiList is just a container list of all the IMR guis
 *  that have been initialized for the IMR tester applet. For each IMR that is
 *  picked an IMR gui is created with the name of the IMR and then stored in the
 *  IMR gui list so that when that same IMR is requested again instead of
 *  recreating it it can just be accessed from this list. This list simply uses
 *  a hash map mapping the IMR gui names to the IMR gui beans.<p>
 *
 * @author     Steven W. Rock
 * @created    February 28, 2002
 * @see        BJF_1997_AttenRel
 * @see        AS_1997_AttenRel
 * @version    1.0
 */
public class AttenuationRelationshipGuiList {


    protected final static String C = "IMRGuiList";
    protected final static boolean D = false;

    /** This is the hash map containing all the instanciated IMRGuiBeans. */
    private HashMap imrGuis = new HashMap();

    /** This is the current selected IMR contained within an IMRGuiBean.  */
    private AttenuationRelationshipGuiBean currentGui = null;


    /**
     *  Constructor for the IMRGuiList object
     */
    public AttenuationRelationshipGuiList() { }


    /**
     *  Sets the IMR attribute of the IMRGuiList object.In this function if the
     *  IMR exists and is already the selected one it returns what has been
     *  selected. If it is not the current it looks inside the hash map to see
     *  if it has already been instanciated and returns that then if not it
     *  creates a new IMRGuiBean from the IMR name adds it to the list sets it
     *  as the current and returns it.
     *
     * @param  imrName  This is the fully qualified package and class name of
     *      the IMR to implement.
     * @param  applet   The main application that uses this list needed to
     *      create the IMRGuiBean.
     * @return          Description of the Return Value
     */
    public AttenuationRelationshipGuiBean setImr( String imrName, AttenuationRelationshipApplet applet ) {

        if ( ( currentGui != null )  && ( currentGui.getName().equals( imrName ) ) )  return currentGui;
        else if ( imrGuis.containsKey( imrName ) ) {
            currentGui = ( AttenuationRelationshipGuiBean ) imrGuis.get( imrName );
            return currentGui;
        }
        else {

            String className = AttenuationRelationshipApplet.imrNames.get( imrName ).toString();
            AttenuationRelationshipGuiBean bean = new AttenuationRelationshipGuiBean( className, imrName, applet );
            imrGuis.put( imrName, bean );
            currentGui = bean;
            return currentGui;

        }

    }

}
