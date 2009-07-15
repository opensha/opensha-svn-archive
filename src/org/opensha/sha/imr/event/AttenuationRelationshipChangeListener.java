package org.opensha.sha.imr.event;

import java.util.EventListener;

/**
 *  <b>Title:</b> AttenuationRelationshipChangeListener<p>
 *
 *  <b>Description:</b> The change listener receives change events whenever a new
 *  Attenuation Relationship is selected, such as from an IMR Gui Bean.<p>
 *
 * @author     Kevin Milner
 * @created    February 27 2009
 * @version    1.0
 */

public interface AttenuationRelationshipChangeListener extends EventListener {
    /**
     *  Function that must be implemented by all Listeners for
     *  AttenuationRelationshipChangeEvents.
     *
     * @param  event  The Event which triggered this function call
     */
    public void attenuationRelationshipChange( AttenuationRelationshipChangeEvent event );
}