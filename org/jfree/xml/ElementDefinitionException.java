/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
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
 * ------------------------------
 * ElementDefinitionException.java
 * ------------------------------
 * (C)opyright 2000-2002, by Object Refinery Limited.
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Apr-2002 : Initial version
 * 31-Aug-2002 : Documentation; changed PrintStackTrace for better tracing
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 */
package org.jfree.xml;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A reportdefinition exception is thrown when the parsing of the report definition
 * failed because invalid or missing attributes are encountered.
 *
 * @author Thomas Morgner
 */
public class ElementDefinitionException extends ParseException {

    /** The parent exception. */
    private Exception parent;

    /**
     * Creates a new ElementDefinitionException without an parent exception and with the given
     * message as explanation.
     *
     * @param message a detail message explaining the reasons for this exception.
     */
    public ElementDefinitionException(String message) {
        super(message);
    }

    /**
     * Creates a new ElementDefinitionException with an parent exception and with the parents
     * message as explaination.
     *
     * @param e the parentException that caused this exception
     */
    public ElementDefinitionException(Exception e) {
        this(e, e.getMessage());
    }

    /**
     * Creates a new ElementDefinitionException with an parent exception and with the given
     * message as explaination.
     *
     * @param e the parentException that caused this exception
     * @param message a detail message explaining the reasons for this exception
     */
    public ElementDefinitionException(Exception e, String message) {
        this(message);
        parent = e;
    }

    /**
     * Returns the parent exception.
     *
     * @return the parent exception.
     */
    public Exception getParentException() {
        return parent;
    }

    /**
     * Prints the stack trace.  If an inner exception exists, use
     * its stack trace.
     *
     * @param s  the stream for writing to.
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (parent != null) {
            s.print("ParentException:");
            parent.printStackTrace(s);
        }
        else {
            s.println("ParentException: <null>");
        }
    }

    /**
     * Prints the stack trace.  If an inner exception exists, use
     * its stack trace.
     *
     * @param s  the stream for writing to.
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (parent != null) {
            s.print("ParentException:");
            parent.printStackTrace(s);
        }
        else {
            s.println("ParentException: <null>");
        }
    }

}
