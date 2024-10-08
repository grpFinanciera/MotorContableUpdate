/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
/* This file has been modified by Open Source Strategies, Inc. */
package org.ofbiz.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javolution.util.FastList;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.XStream;

/**
 * Utilities methods to simplify dealing with JAXP & DOM XML parsing
 *
 */
public class UtilXml {

    public static final String module = UtilXml.class.getName();
    protected static final XStream xstream = new XStream();

    // ----- DOM Level 3 Load and Save Methods -- //

    /** Returns a <code>DOMImplementationLS</code> instance.
     * @return A <code>DOMImplementationLS</code> instance
     * @see <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407/">DOM Level 3 Load and Save Specification</a>
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static DOMImplementationLS getDomLsImplementation() throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        return (DOMImplementationLS)registry.getDOMImplementation("LS");
    }

    /** Returns a <code>LSOutput</code> instance.
     * @param impl A <code>DOMImplementationLS</code> instance
     * @param os Optional <code>OutputStream</code> instance
     * @param encoding Optional character encoding, default is UTF-8
     * @return A <code>LSOutput</code> instance
     * @see <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407/">DOM Level 3 Load and Save Specification</a>
     */
    public static LSOutput createLSOutput(DOMImplementationLS impl, OutputStream os, String encoding) {
        LSOutput out = impl.createLSOutput();
        if (os != null) {
            out.setByteStream(os);
        }
        if (encoding != null) {
            out.setEncoding(encoding);
        }
        return out;
    }

    /** Returns a <code>LSSerializer</code> instance.
     * @param impl A <code>DOMImplementationLS</code> instance
     * @param includeXmlDeclaration If set to <code>true</code>,
     * the xml declaration will be included in the output
     * @param enablePrettyPrint If set to <code>true</code>, the
     * output will be formatted in human-readable form. If set to
     * <code>false</code>, the entire document will consist of a single line.
     * @return A <code>LSSerializer</code> instance
     * @see <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407/">DOM Level 3 Load and Save Specification</a>
     */
    public static LSSerializer createLSSerializer(DOMImplementationLS impl, boolean includeXmlDeclaration, boolean enablePrettyPrint) {
        LSSerializer writer = impl.createLSSerializer();
        DOMConfiguration domConfig = writer.getDomConfig();
        domConfig.setParameter("xml-declaration", includeXmlDeclaration);
        domConfig.setParameter("format-pretty-print", enablePrettyPrint);
        return writer;
    }

    /** Serializes a DOM Node to an <code>OutputStream</code> using DOM 3.
     * @param os The <code>OutputStream</code> instance to write to
     * @param node The DOM <code>Node</code> object to be serialized
     * @param encoding Optional character encoding
     * @param includeXmlDeclaration If set to <code>true</code>,
     * the xml declaration will be included in the output
     * @param enablePrettyPrint If set to <code>true</code>, the
     * output will be formatted in human-readable form. If set to
     * <code>false</code>, the entire document will consist of a single line.
     * @see <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-LS-20040407/">DOM Level 3 Load and Save Specification</a>
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static void writeXmlDocument(OutputStream os, Node node, String encoding, boolean includeXmlDeclaration, boolean enablePrettyPrint) throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        DOMImplementationLS impl = getDomLsImplementation();
        LSOutput out = createLSOutput(impl, os, encoding);
        LSSerializer writer = createLSSerializer(impl, includeXmlDeclaration, enablePrettyPrint);
        writer.write(node, out);
    }

    // ----- TrAX Methods ----------------- //

    /** Creates a JAXP TrAX Transformer suitable for pretty-printing an
     * XML document. This method is provided as an alternative to the
     * deprecated <code>org.apache.xml.serialize.OutputFormat</code> class.
     * @param encoding Optional encoding, defaults to UTF-8
     * @param omitXmlDeclaration If <code>true</code> the xml declaration
     * will be omitted from the output
     * @param indent If <code>true</code>, the output will be indented
     * @param indentAmount If <code>indent</code> is <code>true</code>,
     * the number of spaces to indent. Default is 4.
     * @return A <code>Transformer</code> instance
     * @see <a href="http://java.sun.com/javase/6/docs/api/javax/xml/transform/package-summary.html">JAXP TrAX</a>
     * @throws TransformerConfigurationException
     */
    public static Transformer createOutputTransformer(String encoding, boolean omitXmlDeclaration, boolean indent, int indentAmount) throws TransformerConfigurationException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xalan=\"http://xml.apache.org/xslt\" version=\"1.0\">\n");
        sb.append("<xsl:output method=\"xml\" encoding=\"");
        sb.append(encoding == null ? "UTF-8" : encoding);
        sb.append("\"");
        if (omitXmlDeclaration) {
            sb.append(" omit-xml-declaration=\"yes\"");
        }
        sb.append(" indent=\"");
        sb.append(indent ? "yes" : "no");
        sb.append("\"");
        if (indent) {
            sb.append(" xalan:indent-amount=\"");
            sb.append(indentAmount <= 0 ? 4 : indentAmount);
            sb.append("\"");
        }
        sb.append("/>\n<xsl:strip-space elements=\"*\"/>\n");
        sb.append("<xsl:template match=\"@*|node()\">\n");
        sb.append("<xsl:copy><xsl:apply-templates select=\"@*|node()\"/></xsl:copy>\n");
        sb.append("</xsl:template>\n</xsl:stylesheet>\n");
        ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        return transformerFactory.newTransformer(new StreamSource(bis));
    }

    /** Serializes a DOM <code>Node</code> to an <code>OutputStream</code>
     * using JAXP TrAX.
     * @param transformer A <code>Transformer</code> instance
     * @param node The <code>Node</code> to serialize
     * @param os The <code>OutputStream</code> to serialize to
     * @see <a href="http://java.sun.com/javase/6/docs/api/javax/xml/transform/package-summary.html">JAXP TrAX</a>
     * @throws TransformerException
     */
    public static void transformDomDocument(Transformer transformer, Node node, OutputStream os) throws TransformerException {
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(os);
        transformer.transform(source, result);
    }

    /** Serializes a DOM <code>Node</code> to an <code>OutputStream</code>
     * using JAXP TrAX.
     * @param node The <code>Node</code> to serialize
     * @param os The <code>OutputStream</code> to serialize to
     * @param encoding Optional encoding, defaults to UTF-8
     * @param omitXmlDeclaration If <code>true</code> the xml declaration
     * will be omitted from the output
     * @param indent If <code>true</code>, the output will be indented
     * @param indentAmount If <code>indent</code> is <code>true</code>,
     * the number of spaces to indent. Default is 4.
     * @see <a href="http://java.sun.com/javase/6/docs/api/javax/xml/transform/package-summary.html">JAXP TrAX</a>
     * @throws TransformerException
     */
    public static void writeXmlDocument(Node node, OutputStream os, String encoding, boolean omitXmlDeclaration, boolean indent, int indentAmount) throws TransformerException {
        Transformer transformer = createOutputTransformer(encoding, omitXmlDeclaration, indent, indentAmount);
        transformDomDocument(transformer, node, os);
    }

    // ----- Java Object Marshalling/Unmarshalling ----- //

    /** Deserialize an object from an <code>InputStream</code>.
     *
     * @param input The <code>InputStream</code>
     * @return The deserialized <code>Object</code>
     */
    public static Object fromXml(InputStream input) {
        return xstream.fromXML(input);
    }

    /** Deserialize an object from a <code>Reader</code>.
     *
     * @param reader The <code>Reader</code>
     * @return The deserialized <code>Object</code>
     */
    public static Object fromXml(Reader reader) {
        return xstream.fromXML(reader);
    }

    /** Deserialize an object from a <code>String</code>.
     *
     * @param str The <code>String</code>
     * @return The deserialized <code>Object</code>
     */
    public static Object fromXml(String str) {
        return xstream.fromXML(str);
    }

    /** Serialize an object to an XML <code>String</code>.
     *
     * @param obj The object to serialize
     * @return An XML <code>String</code>
     */
    public static String toXml(Object obj) {
        return xstream.toXML(obj);
    }

    /** Serialize an object to an <code>OutputStream</code>.
     *
     * @param obj The object to serialize
     * @param output The <code>OutputStream</code>
     */
    public static void toXml(Object obj, OutputStream output) {
        xstream.toXML(obj, output);
    }

    /** Serialize an object to a <code>Writer</code>.
     *
     * @param obj The object to serialize
     * @param writer The <code>Writer</code>
     */
    public static void toXml(Object obj, Writer writer) {
        xstream.toXML(obj, writer);
    }

    // ------------------------------------------------- //

    public static String writeXmlDocument(Node node) throws java.io.IOException {
        if (node == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] Node was null, doing nothing", module);
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeXmlDocument(bos, node);
        return bos.toString("UTF-8");
    }

    public static void writeXmlDocument(String filename, Node node) throws FileNotFoundException, IOException {
        if (node == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] Node was null, doing nothing", module);
            return;
        }
        if (filename == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] Filename was null, doing nothing", module);
            return;
        }
        File outFile = new File(filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            writeXmlDocument(fos, node);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void writeXmlDocument(OutputStream os, Node node) throws java.io.IOException {
        if (node == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] Node was null, doing nothing", module);
            return;
        }
        // OutputFormat defaults are: indent on, indent = 4, include XML declaration,
        // charset = UTF-8, line width = 72
        try {
            writeXmlDocument(node, os, "UTF-8", false, true, 4);
        } catch (TransformerException e) {
            // Wrapping this exception for backwards compatibility
            throw new IOException(e.getMessage());
        }
    }

    /**
     * @deprecated Use  <a href="#writeXmlDocument(org.w3c.dom.Node,%20java.io.OutputStream,%20java.lang.String,%20boolean,%20boolean,%20int)">writeXmlDocument(Node node, OutputStream os, String encoding, boolean omitXmlDeclaration, boolean indent, int indentAmount)</a>
     */
    @Deprecated
    public static void writeXmlDocument(OutputStream os, Element element, OutputFormat format) throws java.io.IOException {
        if (element == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] Element was null, doing nothing", module);
            return;
        }
        if (os == null) {
            Debug.logWarning("[UtilXml.writeXmlDocument] OutputStream was null, doing nothing", module);
            return;
        }
        try {
            writeXmlDocument(element, os, format.getEncoding(), format.getOmitXMLDeclaration(), format.getIndenting(), format.getIndent());
        } catch (Exception e) {
            // Wrapping this exception for backwards compatibility
            throw new IOException(e.getMessage());
        }
    }

    public static Document readXmlDocument(String content)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        return readXmlDocument(content, true);
    }

    public static Document readXmlDocument(String content, boolean validate)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (content == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] content was null, doing nothing", module);
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes("UTF-8"));
        return readXmlDocument(bis, validate, "Internal Content");
    }

    public static Document readXmlDocument(String content, boolean validate, boolean withPosition)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (content == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] content was null, doing nothing", module);
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes("UTF-8"));
        return readXmlDocument(bis, validate, "Internal Content", withPosition);
    }

    public static Document readXmlDocument(URL url)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        return readXmlDocument(url, true);
    }

    public static Document readXmlDocument(URL url, boolean validate)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (url == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] URL was null, doing nothing", module);
            return null;
        }
        InputStream is = url.openStream();
        Document document = readXmlDocument(is, validate, url.toString());
        is.close();
        return document;
    }

    public static Document readXmlDocument(URL url, boolean validate, boolean withPosition)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (url == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] URL was null, doing nothing", module);
            return null;
        }
        InputStream is = url.openStream();
        Document document = readXmlDocument(is, validate, url.toString(), withPosition);
        is.close();
        return document;
    }

    public static Document readXmlDocument(InputStream is, String docDescription)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        return readXmlDocument(is, true, docDescription);
    }

    public static Document readXmlDocument(InputStream is, String docDescription, boolean withPosition)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        return readXmlDocument(is, true, docDescription, withPosition);
    }

    public static Document readXmlDocument(InputStream is, boolean validate, String docDescription)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (is == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] InputStream was null, doing nothing", module);
            return null;
        }

        long startTime = System.currentTimeMillis();

        // DON'T do this: seems to be causing problems with Catalina/Tomcat, maybe it is expecting a different parser?
        //System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

        Document document = null;

        /* Xerces DOMParser direct interaction; the other seems to be working better than this, so we'll stay with the standard JAXP stuff
        DOMParser parser = new DOMParser();
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        } catch (SAXException e) {
            Debug.logWarning("Could not set parser feature: " + e.toString(), module);
        }
        parser.parse(new InputSource(is));
        document = parser.getDocument();
        */

        /* Standard JAXP (mostly), but doesn't seem to be doing XML Schema validation, so making sure that is on... */
        DocumentBuilderFactory factory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
        factory.setValidating(validate);
        factory.setNamespaceAware(true);

        factory.setAttribute("http://xml.org/sax/features/validation", validate);
        factory.setAttribute("http://apache.org/xml/features/validation/schema", validate);

        // with a SchemaUrl, a URL object
        //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", SchemaUrl);
        DocumentBuilder builder = factory.newDocumentBuilder();
        if (validate) {
            LocalResolver lr = new LocalResolver(new DefaultHandler());
            ErrorHandler eh = new LocalErrorHandler(docDescription, lr);

            builder.setEntityResolver(lr);
            builder.setErrorHandler(eh);
        }
        document = builder.parse(is);

        double totalSeconds = (System.currentTimeMillis() - startTime)/1000.0;
        if (Debug.verboseOn()) Debug.logVerbose("XML Read " + totalSeconds + "s: " + docDescription, module);
        return document;
    }

    public static Document readXmlDocument(InputStream is, boolean validate, String docDescription, boolean withPosition)
            throws SAXException, ParserConfigurationException, java.io.IOException {
        if (!withPosition) {
            return readXmlDocument(is, validate, docDescription);
        }

        if (is == null) {
            Debug.logWarning("[UtilXml.readXmlDocument] InputStream was null, doing nothing", module);
            return null;
        }

        long startTime = System.currentTimeMillis();

        Document document = null;

        DOMParser parser = new DOMParser() {
            private XMLLocator locator;

            private void setLineColumn(Node node) {
                if (node.getUserData("startLine") != null) {
                    return;
                }
                node.setUserData("systemId",locator.getLiteralSystemId(), null);
                node.setUserData("startLine",locator.getLineNumber(), null);
                node.setUserData("startColumn",locator.getColumnNumber(), null);
            }

            private void setLineColumn() {
                try {
                    Node node = (Node) getProperty("http://apache.org/xml/properties/dom/current-element-node");
                    if (node != null) {
                        setLineColumn(node);
                    }
                } catch (SAXException ex) {
                    Debug.logWarning(ex, module);
                }
            }

            private void setLastChildLineColumn() {
                try {
                    Node node = (Node) getProperty("http://apache.org/xml/properties/dom/current-element-node");
                    if (node != null) {
                       setLineColumn(node.getLastChild());
                    }
                } catch (SAXException ex) {
                    Debug.logWarning(ex, module);
                }
            }

            @Override
            public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs) throws XNIException {
                super.startGeneralEntity(name, identifier, encoding, augs);
                setLineColumn();
            }

            @Override
            public void comment(XMLString text, Augmentations augs) throws XNIException {
                super.comment(text, augs);
                setLastChildLineColumn();
            }

            @Override
            public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
                super.processingInstruction(target, data, augs);
                setLastChildLineColumn();
            }

            @Override
            public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs) throws XNIException {
                super.startDocument(locator, encoding, namespaceContext, augs);
                this.locator = locator;
                setLineColumn();
            }

            @Override
            public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs) throws XNIException {
                super.doctypeDecl(rootElement, publicId, systemId, augs);
            }

            @Override
            public void startElement(QName elementQName, XMLAttributes attrList, Augmentations augs) throws XNIException {
                super.startElement(elementQName, attrList, augs);
                setLineColumn();
            }

            @Override
            public void characters(XMLString text, Augmentations augs) throws XNIException {
                super.characters(text, augs);
                setLastChildLineColumn();
            }

            @Override
            public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
                super.ignorableWhitespace(text, augs);
                setLastChildLineColumn();
            }
        };
        parser.setFeature("http://xml.org/sax/features/namespaces", true);
        parser.setFeature("http://xml.org/sax/features/validation", validate);
        parser.setFeature("http://apache.org/xml/features/validation/schema", validate);
        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        // with a SchemaUrl, a URL object
        //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", SchemaUrl);
        if (validate) {
            LocalResolver lr = new LocalResolver(new DefaultHandler());
            ErrorHandler eh = new LocalErrorHandler(docDescription, lr);

            parser.setEntityResolver(lr);
            parser.setErrorHandler(eh);
        }
        parser.parse(new InputSource(is));
        document = parser.getDocument();

        double totalSeconds = (System.currentTimeMillis() - startTime)/1000.0;
        if (Debug.verboseOn()) Debug.logVerbose("XML Read " + totalSeconds + "s: " + docDescription, module);
        return document;
    }

    public static Document makeEmptyXmlDocument() {
        return makeEmptyXmlDocument(null);
    }

    public static Document makeEmptyXmlDocument(String rootElementName) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(true);
        // factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            document = builder.newDocument();
        } catch (Exception e) {
            Debug.logError(e, module);
        }

        if (document == null) return null;

        if (rootElementName != null) {
            Element rootElement = document.createElement(rootElementName);
            document.appendChild(rootElement);
        }

        return document;
    }

    /** Creates a child element with the given name and appends it to the element child node list. */
    public static Element addChildElement(Element element, String childElementName, Document document) {
        Element newElement = document.createElement(childElementName);

        element.appendChild(newElement);
        return newElement;
    }

    /** Creates a child element with the given name and appends it to the element child node list.
     *  Also creates a Text node with the given value and appends it to the new elements child node list.
     */
    public static Element addChildElementValue(Element element, String childElementName,
            String childElementValue, Document document) {
        Element newElement = addChildElement(element, childElementName, document);

        newElement.appendChild(document.createTextNode(childElementValue));
        return newElement;
    }

    /** Creates a child element with the given namespace supportive name and appends it to the element child node list. */
    public static Element addChildElementNSElement(Element element, String childElementName,
            Document document, String nameSpaceUrl) {
        Element newElement = document.createElementNS(nameSpaceUrl, childElementName);
        element.appendChild(newElement);
        return element;
    }

    /** Creates a child element with the given namespace supportive name and appends it to the element child node list.
     *  Also creates a Text node with the given value and appends it to the new elements child node list.
     */
    public static Element addChildElementNSValue(Element element, String childElementName,
            String childElementValue, Document document, String nameSpaceUrl) {
        Element newElement = document.createElementNS(nameSpaceUrl, childElementName);
        newElement.appendChild(document.createTextNode(childElementValue));
        element.appendChild(newElement);
        return element;
    }

    /** Creates a child element with the given name and appends it to the element child node list.
     *  Also creates a CDATASection node with the given value and appends it to the new elements child node list.
     */
    public static Element addChildElementCDATAValue(Element element, String childElementName,
            String childElementValue, Document document) {
        Element newElement = addChildElement(element, childElementName, document);

        newElement.appendChild(document.createCDATASection(childElementValue));
        return newElement;
    }

    /** Return a List of Element objects that are children of the given element */
    public static List<? extends Element> childElementList(Element element) {
        if (element == null) return null;

        List<Element> elements = FastList.newInstance();
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;
                    elements.add(childElement);
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return elements;
    }

    /** Return a List of Element objects that have the given name and are
     * immediate children of the given element; if name is null, all child
     * elements will be included. */
    public static List<? extends Element> childElementList(Element element, String childElementName) {
        if (element == null) return null;

        List<Element> elements = FastList.newInstance();
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE && (childElementName == null ||
                        childElementName.equals(node.getNodeName()))) {
                    Element childElement = (Element) node;

                    elements.add(childElement);
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return elements;
    }

    /** Return a List of Element objects that have the given name and are
     * immediate children of the given element; if name is null, all child
     * elements will be included. */
    public static List<? extends Element> childElementList(Element element, Set<String> childElementNames) {
        if (element == null) return null;

        List<Element> elements = FastList.newInstance();
        if (childElementNames == null) return elements;
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE && childElementNames.contains(node.getNodeName())) {
                    Element childElement = (Element) node;
                    elements.add(childElement);
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return elements;
    }

    /** Return a List of Element objects that have the given name and are
     * immediate children of the given element; if name is null, all child
     * elements will be included. */
    public static List<? extends Element> childElementList(Element element, String... childElementNames) {
        return childElementList(element, UtilMisc.toSetArray(childElementNames));
    }

    /** Return a List of Element objects that are children of the given DocumentFragment */
    public static List<? extends Element> childElementList(DocumentFragment fragment) {
        if (fragment == null) return null;
        List<Element> elements = FastList.newInstance();
        Node node = fragment.getFirstChild();
        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;
                    elements.add(childElement);
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return elements;
    }

    /** Return a List of Node objects that have the given name and are immediate children of the given element;
      * if name is null, all child elements will be included. */
    public static List<? extends Node> childNodeList(Node node) {
        if (node == null) return null;

        List<Node> nodes = FastList.newInstance();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.COMMENT_NODE) {
                    nodes.add(node);
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return nodes;
    }

    /** Return the first child Element
     * returns the first element. */
    public static Element firstChildElement(Element element, Set<String> childElementNames) {
        if (element == null) return null;
        // get the first element with the given name
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE && childElementNames.contains(node.getLocalName())) {
                    Element childElement = (Element) node;

                    return childElement;
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return null;
    }

    /** Return the first child Element
     * returns the first element. */
    public static Element firstChildElement(Element element, String... childElementNames) {
        return firstChildElement(element, UtilMisc.toSetArray(childElementNames));
    }

    /** Return the first child Element
     * returns the first element. */
    public static Element firstChildElement(Element element) {
        if (element == null) return null;
        // get the first element with the given name
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;

                    return childElement;
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return null;
    }

    /** Return the first child Element with the given name; if name is null
     * returns the first element. */
    public static Element firstChildElement(Element element, String childElementName) {
        if (element == null) return null;
        if (UtilValidate.isEmpty(childElementName)) return null;
        // get the first element with the given name
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE && (childElementName == null ||
                        childElementName.equals(node.getNodeName()))) {
                    Element childElement = (Element) node;
                    return childElement;
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return null;
    }

    /** Return the first child Element with the given name; if name is null
     * returns the first element. */
    public static Element firstChildElement(Element element, String childElementName, String attrName, String attrValue) {
        if (element == null) return null;
        // get the first element with the given name
        Node node = element.getFirstChild();

        if (node != null) {
            do {
                if (node.getNodeType() == Node.ELEMENT_NODE && (childElementName == null ||
                        childElementName.equals(node.getNodeName()))) {
                    Element childElement = (Element) node;

                    String value = childElement.getAttribute(attrName);

                    if (value != null && value.equals(attrValue)) {
                        return childElement;
                    }
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return null;
    }

    /** Return the text (node value) contained by the named child node. */
    public static String childElementValue(Element element, String childElementName) {
        if (element == null) return null;
        // get the value of the first element with the given name
        Element childElement = firstChildElement(element, childElementName);

        return elementValue(childElement);
    }

    /** Return the text (node value) contained by the named child node or a default value if null. */
    public static String childElementValue(Element element, String childElementName, String defaultValue) {
        if (element == null) return defaultValue;
        // get the value of the first element with the given name
        Element childElement = firstChildElement(element, childElementName);
        String elementValue = elementValue(childElement);

        if (UtilValidate.isEmpty(elementValue))
            return defaultValue;
        else
            return elementValue;
    }

    /** Return the text (node value) of the first node under this, works best if normalized. */
    public static String elementValue(Element element) {
        if (element == null) return null;
        // make sure we get all the text there...
        element.normalize();
        Node textNode = element.getFirstChild();

        if (textNode == null) return null;

        StringBuilder valueBuffer = new StringBuilder();
        do {
            if (textNode.getNodeType() == Node.CDATA_SECTION_NODE || textNode.getNodeType() == Node.TEXT_NODE) {
                valueBuffer.append(textNode.getNodeValue());
            }
        } while ((textNode = textNode.getNextSibling()) != null);
        return valueBuffer.toString();
    }

    /** Return the text (node value) of the first node under this */
    public static String nodeValue(Node node) {
        if (node == null) return null;

        StringBuilder valueBuffer = new StringBuilder();
        do {
            if (node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.COMMENT_NODE) {
                valueBuffer.append(node.getNodeValue());
            }
        } while ((node = node.getNextSibling()) != null);
        return valueBuffer.toString();
    }

    public static String elementAttribute(Element element, String attrName, String defaultValue) {
        if (element == null) return defaultValue;
        String attrValue = element.getAttribute(attrName);
        return UtilValidate.isNotEmpty(attrValue) ? attrValue : defaultValue;
    }

    public static String checkEmpty(String string) {
        if (UtilValidate.isNotEmpty(string))
            return string;
        else
            return "";
    }

    public static String checkEmpty(String string1, String string2) {
        if (UtilValidate.isNotEmpty(string1))
            return string1;
        else if (UtilValidate.isNotEmpty(string2))
            return string2;
        else
            return "";
    }

    public static String checkEmpty(String string1, String string2, String string3) {
        if (UtilValidate.isNotEmpty(string1))
            return string1;
        else if (UtilValidate.isNotEmpty(string2))
            return string2;
        else if (UtilValidate.isNotEmpty(string3))
            return string3;
        else
            return "";
    }

    public static boolean checkBoolean(String str) {
        return checkBoolean(str, false);
    }

    public static boolean checkBoolean(String str, boolean defaultValue) {
        if (defaultValue) {
            //default to true, ie anything but false is true
            return !"false".equals(str);
        } else {
            //default to false, ie anything but true is false
            return "true".equals(str);
        }
    }

    /**
     * Local entity resolver to handle J2EE DTDs. With this a http connection
     * to sun is not needed during deployment.
     * Function boolean hadDTD() is here to avoid validation errors in
     * descriptors that do not have a DOCTYPE declaration.
     */
    public static class LocalResolver implements EntityResolver {

        private boolean hasDTD = false;
        private EntityResolver defaultResolver;

        public LocalResolver(EntityResolver defaultResolver) {
            this.defaultResolver = defaultResolver;
        }

        /**
         * Returns DTD inputSource. If DTD was found in the dtds Map and inputSource was created
         * flag hasDTD is set to true.
         * @param publicId - Public ID of DTD
         * @param systemId - System ID of DTD
         * @return InputSource of DTD
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            //Debug.logInfo("resolving XML entity with publicId [" + publicId + "], systemId [" + systemId + "]", module);
            hasDTD = false;
            String dtd = UtilProperties.getSplitPropertyValue(UtilURL.fromResource("localdtds.properties"), publicId);
            if (UtilValidate.isNotEmpty(dtd)) {
                if (Debug.verboseOn()) Debug.logVerbose("[UtilXml.LocalResolver.resolveEntity] resolving DTD with publicId [" + publicId +
                        "], systemId [" + systemId + "] and the dtd file is [" + dtd + "]", module);
                try {
                    URL dtdURL = UtilURL.fromResource(dtd);
                    if (dtdURL == null) {
                        throw new GeneralException("Local DTD not found - " + dtd);
                    }
                    InputStream dtdStream = dtdURL.openStream();
                    InputSource inputSource = new InputSource(dtdStream);

                    inputSource.setPublicId(publicId);
                    hasDTD = true;
                    if (Debug.verboseOn()) Debug.logVerbose("[UtilXml.LocalResolver.resolveEntity] got LOCAL DTD input source with publicId [" +
                            publicId + "] and the dtd file is [" + dtd + "]", module);
                    return inputSource;
                } catch (Exception e) {
                    Debug.logWarning(e, module);
                }
            } else {
                // nothing found by the public ID, try looking at the systemId, or at least the filename part of it and look for that on the classpath
                int lastSlash = systemId.lastIndexOf("/");
                String filename = null;
                if (lastSlash == -1) {
                    filename = systemId;
                } else {
                    filename = systemId.substring(lastSlash + 1);
                }

                URL resourceUrl = UtilURL.fromResource(filename);

                if (resourceUrl != null) {
                    InputStream resStream = resourceUrl.openStream();
                    InputSource inputSource = new InputSource(resStream);

                    if (UtilValidate.isNotEmpty(publicId)) {
                        inputSource.setPublicId(publicId);
                    }
                    hasDTD = true;
                    if (Debug.verboseOn()) Debug.logVerbose("[UtilXml.LocalResolver.resolveEntity] got LOCAL DTD/Schema input source with publicId [" +
                            publicId + "] and the file/resource is [" + filename + "]", module);
                    return inputSource;
                } else {
                    Debug.logWarning("[UtilXml.LocalResolver.resolveEntity] could not find LOCAL DTD/Schema with publicId [" +
                            publicId + "] and the file/resource is [" + filename + "]", module);
                    return null;
                }
            }
            //Debug.logInfo("[UtilXml.LocalResolver.resolveEntity] local resolve failed for DTD with publicId [" +
            //        publicId + "] and the dtd file is [" + dtd + "], trying defaultResolver", module);
            return defaultResolver.resolveEntity(publicId, systemId);
        }

        /**
         * Returns the boolean value to inform id DTD was found in the XML file or not
         * @return boolean - true if DTD was found in XML
         */
        public boolean hasDTD() {
            return hasDTD;
        }
    }


    /** Local error handler for entity resolver to DocumentBuilder parser.
     * Error is printed to output just if DTD was detected in the XML file.
     */
    public static class LocalErrorHandler implements ErrorHandler {

        private String docDescription;
        private LocalResolver localResolver;

        public LocalErrorHandler(String docDescription, LocalResolver localResolver) {
            this.docDescription = docDescription;
            this.localResolver = localResolver;
        }

        public void error(SAXParseException exception) {
            String exceptionMessage = exception.getMessage();
            Pattern valueFlexExpr = Pattern.compile("value '\\$\\{.*\\}'");
            Matcher matcher = valueFlexExpr.matcher(exceptionMessage.toLowerCase());
            if (localResolver.hasDTD() && !matcher.find()) {
                Debug.logError("XmlFileLoader: File "
                    + docDescription
                    + " process error. Line: "
                    + String.valueOf(exception.getLineNumber())
                    + ". Error message: "
                    + exceptionMessage, module
               );
            }
        }

        public void fatalError(SAXParseException exception) {
            if (localResolver.hasDTD()) {
                Debug.logError("XmlFileLoader: File "
                    + docDescription
                    + " process fatal error. Line: "
                    + String.valueOf(exception.getLineNumber())
                    + ". Error message: "
                    + exception.getMessage(), module
               );
            }
        }

        public void warning(SAXParseException exception) {
            if (localResolver.hasDTD()) {
                Debug.logError("XmlFileLoader: File "
                    + docDescription
                    + " process warning. Line: "
                    + String.valueOf(exception.getLineNumber())
                    + ". Error message: "
                    + exception.getMessage(), module
               );
            }
        }
    }
}
