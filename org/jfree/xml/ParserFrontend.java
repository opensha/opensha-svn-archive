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
 * --------------------
 * ReportGenerator.java
 * --------------------
 * (C)opyright 2002, 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-May-2002 : Initial version
 * 12-Dec-2002 : Fixed issues reported by Checkstyle (DG);
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.jfree.util.Log;

/**
 * The reportgenerator initializes the parser and provides an interface
 * the the default parser.
 *
 * To create a report from an URL, use
 * <code>
 * ReportGenerator.getInstance().parseReport (URL myURl, URL contentBase);
 * </code>
 *
 * @author Thomas Morgner
 */
public class ParserFrontend {

    /** The report handler. */
    private Parser defaulthandler;

    /** The parser factory. */
    private SAXParserFactory factory;

    /** The DTD. */
    private EntityResolver entityResolver;

    /** Validate the DTD? */
    private boolean validateDTD;

    /**
     * Creates a new report generator. The generator uses the singleton pattern by default,
     * so use generator.getInstance() to get the generator.
     *
     * @param parser the parser that is used to coordinate the parsing process.
     */
    protected ParserFrontend(Parser parser) {
        if (parser == null) {
            throw new NullPointerException();
        }
        defaulthandler = parser;
    }

    /**
     * Returns <code>true</code> if the report definition should be validated against the
     * DTD, and <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isValidateDTD() {
        return validateDTD;
    }

    /**
     * Sets a flag that controls whether or not the report definition is validated
     * against the DTD.
     *
     * @param validateDTD  the flag.
     */
    public void setValidateDTD(boolean validateDTD) {
        this.validateDTD = validateDTD;
    }

    /**
     * Returns the entity resolver.
     *
     * @return The entity resolver.
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver  the entity resolver.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Returns a SAX parser.
     *
     * @return a SAXParser.
     *
     * @throws ParserConfigurationException if there is a problem configuring the parser.
     * @throws SAXException if there is a problem with the parser initialisation
     */
    protected SAXParser getParser() throws ParserConfigurationException, SAXException {
        if (factory == null) {
            factory = SAXParserFactory.newInstance();
            if (isValidateDTD()) {
                try {
                    // dont touch the validating feature, if not needed ..
                    factory.setValidating(true);
                }
                catch (Exception ex) {
                    // the parser does not like the idea of validating ...
                    Log.debug("The parser will not validate the xml document.", ex);
                }
            }
        }
        return factory.newSAXParser();
    }

    /**
     * Sets the default handler used for parsing reports. This handler is used to
     * initiate parsing.
     *
     * @param handler  the handler.
     */
    public void setDefaultHandler(Parser handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.defaulthandler = handler;
    }

    /**
     * Returns the ElementDefinitionHandler used for parsing reports.
     *
     * @return the report handler.
     */
    public Parser getDefaultHandler() {
        return defaulthandler;
    }

    /**
     * Creates a new instance of the currently set default handler and sets the contentbase
     * for the handler to <code>contentBase</code>.
     *
     * @param contentBase  the content base.
     *
     * @return the report handler.
     */
    protected Parser createDefaultHandler(URL contentBase) {
        Parser handler = getDefaultHandler().getInstance();
        if (contentBase != null) {
            handler.setConfigProperty(Parser.CONTENTBASE_KEY, contentBase.toExternalForm());
        }
        return handler;
    }

    /**
     * Parses an XML report template file.
     *
     * @param input  the input source.
     * @param contentBase  the content base.
     *
     * @return the report.
     *
     * @throws ElementDefinitionException if an error occurred.
     */
    protected Object parse(InputSource input, URL contentBase)
        throws ElementDefinitionException {
        try {
            SAXParser parser = getParser();
            XMLReader reader = parser.getXMLReader();

            try {
                reader.setFeature("http://xml.org/sax/features/validation", isValidateDTD());
            }
            catch (SAXException se) {
                Log.debug("The XMLReader will not validate the xml document.", se);
            }
            Parser handler = createDefaultHandler(contentBase);
            configureReader(reader, handler);
            try {
                reader.setContentHandler(handler);
                reader.setDTDHandler(handler);
                reader.setEntityResolver(getEntityResolver());
                reader.setErrorHandler(handler);
                reader.parse(input);
                return handler.getResult();
            }
            catch (IOException e) {
                throw new ElementDefinitionException(e);
            }
        }
        catch (ParserConfigurationException e) {
            throw new ElementDefinitionException(e);
        }
        catch (SAXException e) {
            throw new ElementDefinitionException(e);
        }
    }

    /**
     * Configures the xml reader. Use this to set features or properties
     * before the documents get parsed.
     *
     * @param handler the parser implementation that will handle the SAX-Callbacks.
     * @param reader the xml reader that should be configured.
     */
    protected void configureReader(XMLReader reader, Parser handler) {
        // do nothing by default ...
    }

    /**
     * Parses an XML file which is loaded using the given URL. All
     * needed relative file- and resourcespecification are loaded
     * using the URL <code>contentBase</code> as base.
     * <p>
     * After the report is generated, the ReportDefinition-source and the contentbase are
     * stored as string in the reportproperties.
     *
     * @param file  the URL for the report template file.
     * @param contentBase  the URL for the report template content base.
     *
     * @return the parsed report.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ElementDefinitionException if there is a problem parsing the report template.
     */
    public Object parse(URL file, URL contentBase)
        throws ElementDefinitionException, IOException {
        if (file == null) {
            throw new NullPointerException("File may not be null");
        }

        BufferedInputStream bin = new BufferedInputStream(file.openStream());
        InputSource in = new InputSource(bin);
        in.setSystemId(file.toString());
        Object result = parse(in, contentBase);
        bin.close();
        return result;
    }

}
