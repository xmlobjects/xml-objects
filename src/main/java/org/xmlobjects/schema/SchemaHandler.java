/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2024 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects.schema;

import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XSOMParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xmlobjects.util.xml.SecureXMLProcessors;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class SchemaHandler {
    protected final Map<String, XSSchemaSet> schemas = new HashMap<>();
    protected final Map<String, String> visitedSchemaLocations = new LinkedHashMap<>();
    private final Map<String, String> userSchemaLocations = new HashMap<>();

    private SAXParserFactory saxParserFactory;
    private ErrorHandler errorHandler;
    private AnnotationParserFactory annotationParserFactory;

    public SchemaHandler() {
    }

    public SchemaHandler(SAXParserFactory saxParserFactory) {
        this.saxParserFactory = Objects.requireNonNull(saxParserFactory, "SAX parser factory must not be null.");
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public AnnotationParserFactory getAnnotationParserFactory() {
        return annotationParserFactory;
    }

    public void setAnnotationParserFactory(AnnotationParserFactory annotationParserFactory) {
        this.annotationParserFactory = annotationParserFactory;
    }

    public void registerSchemaLocation(String namespaceURI, Path file) {
        userSchemaLocations.putIfAbsent(namespaceURI, file.toUri().normalize().toString());
    }

    public void registerSchemaLocation(String namespaceURI, File file) {
        userSchemaLocations.putIfAbsent(namespaceURI, file.toURI().normalize().toString());
    }

    public void parseSchema(InputStream stream) throws SchemaHandlerException {
        parse(new InputSource(stream));
    }

    public void parseSchema(Reader reader) throws SchemaHandlerException {
        parse(new InputSource(reader));
    }

    public void parseSchema(Path file) throws SchemaHandlerException {
        parseSchema(file.toUri().normalize().toString());
    }

    public void parseSchema(File file) throws SchemaHandlerException {
        parseSchema(file.toURI().normalize().toString());
    }

    public void parseSchema(URL url) throws SchemaHandlerException {
        parseSchema(url.toExternalForm());
    }

    public void parseSchema(String systemId) throws SchemaHandlerException {
        parse(new InputSource(systemId));
    }

    public void parseSchema(String namespaceURI, String schemaLocation) throws SchemaHandlerException {
        if (schemas.containsKey(namespaceURI)) {
            return;
        }

        parseSchema(userSchemaLocations.getOrDefault(namespaceURI, schemaLocation));
    }

    public void resolveAndParseSchema(String namespaceURI) throws SchemaHandlerException {
        if (schemas.containsKey(namespaceURI)) {
            return;
        }

        String schemaLocation = userSchemaLocations.get(namespaceURI);
        if (schemaLocation != null) {
            InputSource source = new InputSource(schemaLocation);
            source.setPublicId(namespaceURI);
            parse(source);
        } else {
            throw new SchemaHandlerException("Failed to resolve the schema location for the target namespace '" +
                    namespaceURI + "'.");
        }
    }

    private void parse(InputSource source) throws SchemaHandlerException {
        for (String schemaLocation : visitedSchemaLocations.values()) {
            if (schemaLocation.equals(source.getSystemId())) {
                return;
            }
        }

        XSOMParser parser = createXSOMParser();
        XSSchemaSet schemaSet;
        try {
            parser.parse(source);
            schemaSet = parser.getResult();
        } catch (SAXException e) {
            throw new SchemaHandlerException("Failed to parse schema document.", e);
        }

        if (schemaSet != null) {
            for (XSSchema schema : schemaSet.getSchemas()) {
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(schema.getTargetNamespace())
                        || XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(schema.getTargetNamespace())
                        || XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(schema.getTargetNamespace())
                        || XMLConstants.XML_NS_URI.equals(schema.getTargetNamespace())) {
                    continue;
                }

                schemas.put(schema.getTargetNamespace(), schemaSet);

                Locator locator = schema.getLocator();
                if (locator != null) {
                    String schemaLocation = visitedSchemaLocations.get(schema.getTargetNamespace());
                    if (schemaLocation == null) {
                        visitedSchemaLocations.put(schema.getTargetNamespace(), locator.getSystemId());
                    } else {
                        try {
                            URL url = new URL(locator.getSystemId());
                            if (url.getProtocol().equals("file") || url.getProtocol().equals("jar")) {
                                visitedSchemaLocations.put(schema.getTargetNamespace(), locator.getSystemId());
                            }
                        } catch (MalformedURLException e) {
                            //
                        }
                    }
                }
            }
        }
    }

    public XSSchemaSet getSchemaSet(String namespaceURI) {
        return schemas.get(namespaceURI);
    }

    public Source[] getSchemas() {
        return visitedSchemaLocations.values().stream()
                .map(StreamSource::new)
                .toArray(Source[]::new);
    }

    public Set<String> getTargetNamespaces() {
        return schemas.keySet();
    }

    public SchemaHandler copy(SchemaHandler other) {
        schemas.putAll(other.schemas);
        visitedSchemaLocations.putAll(other.visitedSchemaLocations);
        userSchemaLocations.putAll(other.userSchemaLocations);
        saxParserFactory = other.saxParserFactory;
        errorHandler = other.errorHandler;
        annotationParserFactory = other.annotationParserFactory;
        return this;
    }

    private XSOMParser createXSOMParser() throws SchemaHandlerException {
        XSOMParser parser = new XSOMParser(getSAXParserFactory());
        parser.setErrorHandler(errorHandler);
        parser.setAnnotationParser(annotationParserFactory);
        parser.setEntityResolver((publicId, systemId) -> {
            InputSource resolved = null;
            if (publicId != null) {
                String schemaLocation = visitedSchemaLocations.get(publicId);
                if (schemaLocation == null) {
                    schemaLocation = userSchemaLocations.get(publicId);
                }

                if (schemaLocation != null) {
                    resolved = new InputSource(schemaLocation);
                    resolved.setPublicId(publicId);
                }
            }

            return resolved;
        });

        return parser;
    }

    private SAXParserFactory getSAXParserFactory() throws SchemaHandlerException {
        if (saxParserFactory == null) {
            try {
                saxParserFactory = SecureXMLProcessors.newSAXParserFactory();
            } catch (Exception e) {
                throw new SchemaHandlerException("Failed to initialize secure XML schema reader.", e);
            }
        }

        return saxParserFactory;
    }
}
