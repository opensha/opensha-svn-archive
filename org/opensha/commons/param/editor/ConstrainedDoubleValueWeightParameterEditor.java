
package org.opensha.commons.param.editor;

import java.awt.Color;

import javax.swing.border.Border;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WarningParameterAPI;
import org.opensha.util.ParamUtils;

/**
 *<b>Title:</b> ConstrainedDoubleValueWeightParameterEditor<p>
 *
 * <b>Description:</b>Special ParameterEditor for editing Constrained
 * DoubleValueWeightParameters. The widget are two NumericTextField (one to enter value, other to enter weight)
 * so that only numbers can be typed in. When hitting <enter> or moving the
 * mouse away from the NumericField, the value will change back to the
 * original if the new number is outside the constraints range. The constraints
 * also appear as a tool tip when you hold the mouse cursor over
 * the NumericTextField. <p>
 *
 * @author vipingupta
 *
 */
public class ConstrainedDoubleValueWeightParameterEditor {
	

}
