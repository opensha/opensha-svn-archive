package org.opensha.sha.imr.event;

import java.util.EventObject;

import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

/**
 *  <b>Title:</b> AttenuationRelationshipChangeEvent<p>
 *
 *  <b>Description:</b> Any time the selected Attenuation Relationship changed via the IMR
 *  GUI bean, this event is triggered and received by all listeners<p>
 *
 * @author     Kevin Milner
 * @created    February 27 2009
 * @version    1.0
 */

public class AttenuationRelationshipChangeEvent extends EventObject {

    /** New value for the Parameter. */
    private ScalarIntensityMeasureRelationshipAPI newAttenRel;

    /** Old value for the Parameter. */
    private ScalarIntensityMeasureRelationshipAPI oldAttenRel;


    /**
     * Constructor for the AttenuationRelationshipChangeEvent object.
     *
     * @param  reference      Object which created this event
     * @param  oldAttenRel       Old AttenuationRelationship
     * @param  newAttenRel       New AttenuationRelationship
     */
    public AttenuationRelationshipChangeEvent(
            Object reference,
            ScalarIntensityMeasureRelationshipAPI oldAttenRel,
            ScalarIntensityMeasureRelationshipAPI newAttenRel
             ) {
        super( reference );
        this.oldAttenRel = oldAttenRel;
        this.newAttenRel = newAttenRel;

    }

    /**
     *  Gets the new AttenuationRelationship.
     *
     * @return    New AttentuationRelationship
     */
    public ScalarIntensityMeasureRelationshipAPI getNewAttenRel() {
        return newAttenRel;
    }


    /**
     *  Gets the old AttentuationRelationship.
     *
     * @return    Old AttentuationRelationship
     */
    public ScalarIntensityMeasureRelationshipAPI getOldValue() {
        return oldAttenRel;
    }
}
