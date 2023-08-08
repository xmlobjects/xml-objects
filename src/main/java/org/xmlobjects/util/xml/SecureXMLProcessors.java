/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2023 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmlobjects.util.xml;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

public class SecureXMLProcessors {

    private SecureXMLProcessors() {
    }

    public static XMLInputFactory newXMLInputFactory() {
        return secure(XMLInputFactory.newFactory());
    }

    public static XMLInputFactory newXMLInputFactory(String factoryClassName, ClassLoader loader) {
        return secure(XMLInputFactory.newFactory(factoryClassName, loader));
    }

    public static XMLInputFactory newDefaultXMLInputFactory() {
        return secure(XMLInputFactory.newDefaultFactory());
    }

    private static XMLInputFactory secure(XMLInputFactory factory) {
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    public static SAXParserFactory newSAXParserFactory() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        return secure(SAXParserFactory.newInstance());
    }

    public static SAXParserFactory newSAXParserFactory(String factoryClassName, ClassLoader loader) throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        return secure(SAXParserFactory.newInstance(factoryClassName, loader));
    }

    public static SAXParserFactory newDefaultSAXParserFactory() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        return secure(SAXParserFactory.newDefaultInstance());
    }

    private static SAXParserFactory secure(SAXParserFactory factory) throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        factory.setXIncludeAware(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory;
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
        return secure(DocumentBuilderFactory.newInstance());
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory(String factoryClassName, ClassLoader loader) throws ParserConfigurationException {
        return secure(DocumentBuilderFactory.newInstance(factoryClassName, loader));
    }

    public static DocumentBuilderFactory newDefaultDocumentBuilderFactory() throws ParserConfigurationException {
        return secure(DocumentBuilderFactory.newDefaultInstance());
    }

    private static DocumentBuilderFactory secure(DocumentBuilderFactory factory) throws ParserConfigurationException {
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }

    public static TransformerFactory newTransformerFactory() throws TransformerConfigurationException {
        return secure(TransformerFactory.newInstance());
    }

    public static TransformerFactory newTransformerFactory(String factoryClassName, ClassLoader loader) throws TransformerConfigurationException {
        return secure(TransformerFactory.newInstance(factoryClassName, loader));
    }

    public static TransformerFactory newDefaultTransformerFactory(String factoryClassName, ClassLoader loader) throws TransformerConfigurationException {
        return secure(TransformerFactory.newDefaultInstance());
    }

    private static TransformerFactory secure(TransformerFactory factory) throws TransformerConfigurationException {
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return factory;
    }
}
