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
 * -----------
 * Parser.java
 * -----------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 09-Jan-2003 : Initial version.
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 * 14-Jul-2003 : More help with the error location given by catching all exceptions.
 * 
 */
 
package org.jfree.xml;

import java.util.HashMap;
import java.util.Stack;

import org.jfree.util.Configuration;
import org.jfree.util.DefaultConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Parser handles the SAXEvents and forwards the event call to the currently
 * active ElementDefinitionHandler. Contains methods to manage and
 * configure the parsing process.
 * <p>
 * An initial report definition handler must be set before the parser can be used.
 *
 * @author Thomas Morgner
 */
public abstract class Parser extends DefaultHandler implements Configuration {

    /** A key for the content base. */
    public static final String CONTENTBASE_KEY = "content-base";

    /** A stack for the active factories. */
    private Stack activeFactories;

    /** The initial factory. */
    private ElementDefinitionHandler initialFactory;

    /** Storage for the parser configuration. */
    private DefaultConfiguration parserConfiguration;

    /** Storage for temporary objects and factories used during the parsing process. */
    private HashMap parserHelperObjects;

    /** The DocumentLocator can be used to resolve the current parse position. */
    private Locator locator;

    /**
     * Creates a new parser.
     */
    public Parser() {
        activeFactories = new Stack();
        parserConfiguration = new DefaultConfiguration();
        parserHelperObjects = new HashMap();
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * The locator allows the application to determine the end position of
     * any document-related event, even if the parser is not reporting an
     * error. Typically, the application will use this information for
     * reporting its own errors (such as character content that does not
     * match an application's business rules). The information returned by
     * the locator is probably not sufficient for use with a search engine.
     *
     * @param locator  the locator.
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * Returns the current locator.
     *
     * @return the locator.
     */
    public Locator getLocator() {
        return locator;
    }

    /**
     * Pushes a handler onto the stack.
     *
     * @param factory  the handler.
     */
    public void pushFactory(ElementDefinitionHandler factory) {
        activeFactories.push(factory);
    }

    /**
     * Reads a handler off the stack without removing it.
     *
     * @return The handler.
     */
    public ElementDefinitionHandler peekFactory() {
        return (ElementDefinitionHandler) activeFactories.peek();
    }

    /**
     * Pops a handler from the stack.
     *
     * @return The handler.
     */
    public ElementDefinitionHandler popFactory() {
        activeFactories.pop();
        return peekFactory();
    }

    /**
     * Receive notification of the end of the document.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end
     * of a document (such as finalising a tree or closing an output
     * file).</p>
     *
     * @exception SAXException Any SAX exception, possibly wrapping another exception.
     *
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument()
        throws SAXException {
        super.endDocument();
    }

    /**
     * Receive notification of the beginning of the document.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the beginning
     * of a document (such as allocating the root node of a tree or
     * creating an output file).</p>
     *
     * @exception SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument()
        throws SAXException {
        activeFactories.clear();
        pushFactory(getInitialFactory());
    }

    /**
     * Receive notification of character data inside an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of character data
     * (such as adding the data to a node or buffer, or printing it to
     * a file).</p>
     *
     * @param ch  the characters.
     * @param start  the start position in the character array.
     * @param length  the number of characters to use from the character array.
     *
     * @exception SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters(char ch[], int start, int length)
        throws SAXException {
        try {
            peekFactory().characters(ch, start, length);
        }
        catch (ParseException pe)
        {
            throw pe;
        }
        catch (Exception e) {
            throw new ParseException(e, getLocator());
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri  the URI.
     * @param localName  the element type name.
     * @param qName  the name.
     *
     * @exception SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        try {
            peekFactory().endElement(qName);
        }
        catch (ParseException pe)
        {
            throw pe;
        }
        catch (Exception e) {
            throw new ParseException(e, getLocator());
        }
    }


    /**
     * Receive notification of the start of an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri  the URI.
     * @param localName  the element type name.
     * @param qName  the name.
     * @param attributes  the specified or defaulted attributes.
     *
     * @exception SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
        throws SAXException {
        try {
            peekFactory().startElement(qName, attributes);
        }
        catch (ParseException pe)
        {
            throw pe;
        }
        catch (Exception e) {
            throw new ParseException(e, getLocator());
        }
    }

    /**
     * Sets the initial handler.
     *
     * @param factory  the initial handler.
     */
    public void setInitialFactory(ElementDefinitionHandler factory) {
        initialFactory = factory;
    }

    /**
     * Returns the initial handler.
     *
     * @return The initial handler.
     */
    public ElementDefinitionHandler getInitialFactory() {
        return initialFactory;
    }

    /**
     * Returns the configuration property with the specified key.
     *
     * @param key  the property key.
     *
     * @return the property value.
     */
    public String getConfigProperty(String key) {
        return getConfigProperty(key, null);
    }

    /**
     * Returns the configuration property with the specified key (or the specified default value
     * if there is no such property).
     * <p>
     * If the property is not defined in this configuration, the code will lookup the property in
     * the parent configuration.
     *
     * @param key  the property key.
     * @param defaultValue  the default value.
     *
     * @return the property value.
     */
    public String getConfigProperty(String key, String defaultValue) {
        return parserConfiguration.getConfigProperty(key, defaultValue);
    }

    /**
     * Sets a parser configuration value.
     *
     * @param key  the key.
     * @param value  the value.
     */
    public void setConfigProperty(String key, String value) {
        if (value == null) {
            parserConfiguration.remove(key);
        }
        else {
            parserConfiguration.setProperty(key, value);
        }
    }

    /**
     * Sets a helper object.
     * 
     * @param key  the key.
     * @param value  the value.
     */
    public void setHelperObject(String key, Object value) {
        if (value == null) {
            parserHelperObjects.remove(key);
        }
        else {
            parserHelperObjects.put(key, value);
        }
    }

    /**
     * Returns a helper object.
     * 
     * @param key  the key.
     * 
     * @return The object.
     */
    public Object getHelperObject(String key) {
        return parserHelperObjects.get(key);
    }

    /**
     * Returns a new instance of the parser.
     *
     * @return a new instance of the parser.
     */
    public abstract Parser getInstance();

    /**
     * Returns the parsed result object after the parsing is complete. Calling
     * this function during the parsing is undefined and may result in an
     * IllegalStateException.
     *
     * @return the parsed result.
     */
    public abstract Object getResult();
}
