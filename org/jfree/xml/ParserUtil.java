/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
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
 * ParserUtil.java
 * ---------------
 * (C)opyright 2002, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 21-May-2002 : Contains utility functions to make parsing easier.
 * 10-Dec-2002 : Fixed issues reported by Checkstyle (DG);
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 *
 */
package org.jfree.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;

/**
 * Basic helper functions to ease up the process of parsing.
 *
 * @author Thomas Morgner
 */
public class ParserUtil {

    /**
     * Parses the string <code>text</code> into an int. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param message  the error message if parsing fails.
     *
     * @return the int value.
     *
     * @throws SAXException if there is a problem with the parsing.
     */
    public static int parseInt(String text, String message) throws SAXException {
        if (text == null) {
            throw new SAXException(message);
        }

        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException nfe) {
            throw new SAXException("NumberFormatError: " + message);
        }
    }

    /**
     * Parses an integer.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return the integer.
     */
    public static int parseInt(String text, int defaultVal) {
        if (text == null) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * Parses the string <code>text</code> into an float. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param message  the error message if parsing fails.
     *
     * @return the float value.
     *
     * @throws SAXException if there is a problem with the parsing.
     */
    public static float parseFloat(String text, String message) throws SAXException {
        if (text == null) {
            throw new SAXException(message);
        }
        try {
            return Float.parseFloat(text);
        }
        catch (NumberFormatException nfe) {
            throw new SAXException("NumberFormatError: " + message);
        }
    }

    /**
     * Parses the string <code>text</code> into an float. If text is null or does not
     * contain a parsable value, the message given in <code>message</code> is used to
     * throw a SAXException.
     *
     * @param text  the text to parse.
     * @param defaultVal the defaultValue returned if parsing fails.
     *
     * @return the float value.
     */
    public static float parseFloat(String text, float defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        try {
            return Float.parseFloat(text);
        }
        catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * Parses a boolean. If the string <code>text</code> contains the value of "true", the
     * true value is returned, else false is returned.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return a boolean.
     */
    public static boolean parseBoolean(String text, boolean defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        return text.equalsIgnoreCase("true");
    }

    /**
     * Parses a string. If the <code>text</code> is null, defaultval is returned.
     *
     * @param text  the text to parse.
     * @param defaultVal  the default value.
     *
     * @return a string.
     */
    public static String parseString(String text, String defaultVal) {
        if (text == null) {
            return defaultVal;
        }
        return text;
    }

    /**
     * Creates a basic stroke given the width contained as float in the given string.
     * If the string could not be parsed into a float, a basic stroke with the width of
     * 1 is returned.
     *
     * @param weight  a string containing a number (the stroke weight).
     *
     * @return the stroke.
     */
    public static Stroke parseStroke(String weight) {
        try {
            if (weight != null) {
                Float w = new Float(weight);
                return new BasicStroke(w.floatValue());
            }
        }
        catch (NumberFormatException nfe) {
            //Log.warn("Invalid weight for stroke", nfe);
        }
        return new BasicStroke(1);
    }

    /**
     * Parses a color entry. If the entry is in hexadecimal or ocal notation, the color is
     * created using Color.decode(). If the string denotes a constant name of on of the color
     * constants defined in java.awt.Color, this constant is used.
     * <p>
     * As fallback the color black is returned if no color can be parsed.
     *
     * @param color  the color (as a string).
     *
     * @return the paint.
     */
    public static Color parseColor(String color) {
        if (color == null) {
            return Color.black;
        }
        try {
            // get color by hex or octal value
            return Color.decode(color);
        }
        catch (NumberFormatException nfe) {
            // if we can't decode lets try to get it by name
            try {
                // try to get a color by name using reflection
                // black is used for an instance and not for the color itselfs
                Field f = Color.class.getField(color);

                return (Color) f.get(null);
            }
            catch (Exception ce) {
                //Log.warn("No such Color : " + color);
                // if we can't get any color return black
                return Color.black;
            }
        }
    }


    /**
     * Parses a position of an element. If a relative postion is given, the returnvalue
     * is a negative number between 0 and -100.
     *
     * @param value  the value.
     * @param exceptionMessage  the exception message.
     *
     * @return the float value.
     *
     * @throws SAXException if there is a problem parsing the string.
     */
    public static float parseRelativeFloat(String value, String exceptionMessage)
        throws SAXException {
        if (value == null) {
            throw new SAXException(exceptionMessage);
        }
        String tvalue = value.trim();
        if (tvalue.endsWith("%")) {
            String number = tvalue.substring(0, tvalue.indexOf("%"));
            float f = parseFloat(number, exceptionMessage) * -1.0f;
            return f;
        }
        else {
            return parseFloat(tvalue, exceptionMessage);
        }
    }

    /**
     * Parses an element position. The position is stored in the attributes "x", "y", "width" and
     * "height". The attributes are allowed to have relative notion.
     *
     * @param atts  the attributes.
     *
     * @return the element position.
     *
     * @throws SAXException if there is a problem getting the element position.
     */
    public static Rectangle2D getElementPosition(Attributes atts) throws SAXException {
        float x = ParserUtil.parseRelativeFloat(atts.getValue("x"),
            "Element x not specified");
        float y = ParserUtil.parseRelativeFloat(atts.getValue("y"),
            "Element y not specified");
        float w = ParserUtil.parseRelativeFloat(atts.getValue("width"),
            "Element width not specified");
        float h = ParserUtil.parseRelativeFloat(atts.getValue("height"),
            "Element height not specified");
        Rectangle2D.Float retval = new Rectangle2D.Float(x, y, w, h);
        return retval;
    }

}
