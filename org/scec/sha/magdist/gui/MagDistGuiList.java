package org.scec.sha.magdist.gui;


import java.util.HashMap;
/**
 * <p>Title: MagDistGuiList </p>
 * <p>Description:  MagDistGuiList is just a container list of all the Mag Dist guis
 *  that have been initialized for the MagFreqDist tester applet. For each Mag Dist that is
 *  picked an MagDist gui is created with the name of the MagDist and then stored in the
 *  MagDist gui list so that when that same MagDist is requested again instead of
 *  recreating it it can just be accessed from this list. This list simply uses
 *  a hash map mapping the MagDist gui names to the MagDist gui beans.<br></p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * Date : Aug 11,2002
 * @version 1.0
 */

public class MagDistGuiList {


    protected final static String C = "MagDistGuiList";
    protected final static boolean D = true;


    /** This is the hash map containing all the instanciated MagDistGuiBeans. */
    private HashMap magDistGuis = new HashMap();


    /** This is the current selected Mag Dist contained within an MagDistGuiBean.  */
    private MagDistGuiBean currentGui = null;


    /**
     *  Constructor for the MagDistGuiList object
     */
    public MagDistGuiList() { }


    /**
     *  Sets the MagDist attribute of the MagDistGuiList object.In this function if the
     *  MagDist exists and is already the selected one it returns what has been
     *  selected. If it is not the current it looks inside the hash map to see
     *  if it has already been instanciated and returns that then if not it
     *  creates a new MagDistGuiBean from the MagDist name adds it to the list sets it
     *  as the current and returns it.
     *
     * @param  magDistName  This is the fully qualified package and class name of
     *      the MagDist to implement.
     * @param  applet   The main application that uses this list needed to
     *      create the MagDistGuiBean.
     * @return          Description of the Return Value
     */
    public MagDistGuiBean setMagDist( String magDistName, MagFreqDistTesterApplet applet ) {

        if ( ( currentGui != null )  && ( currentGui.getName().equals( magDistName ) ) )  return currentGui;
        else if ( magDistGuis.containsKey( magDistName ) ) {
            currentGui = ( MagDistGuiBean ) magDistGuis.get( magDistName );
            return currentGui;
        }
        else {

            String className = MagFreqDistTesterApplet.magDistNames.get( magDistName ).toString();
            MagDistGuiBean bean = new MagDistGuiBean( className, magDistName, applet );
            magDistGuis.put( magDistName, bean );
            currentGui = bean;
            return currentGui;

        }

    }
}