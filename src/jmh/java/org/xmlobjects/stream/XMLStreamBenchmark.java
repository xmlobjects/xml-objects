/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.stream.XMLTestSupport.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.xmlobjects.stream.CityGMLShapedRoundTripTest.*;

/**
 * Writes and reads a generated CityGML-shaped document (see {@link CityGMLShapedRoundTripTest}).
 * The document has ~200 buildings, ~1,200 polygons and ~6 MB of UTF-8 output.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class XMLStreamBenchmark {
    @Param({"UTF-8", "ISO-8859-1"})
    public String encoding;

    @Param({"", "  "})
    public String indent;

    private XMLObjects xmlObjects;
    private Node document;
    private byte[] xml;

    @Setup
    public void setup() throws Exception {
        xmlObjects = XMLObjects.newInstance();
        document = createCityModel(new Random(42), 200);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out);
        xml = out.toByteArray();
    }

    @Benchmark
    public void write(Blackhole blackhole) throws Exception {
        CountingOutputStream out = new CountingOutputStream();
        write(out);
        blackhole.consume(out.count);
    }

    @Benchmark
    public void read(Blackhole blackhole) throws Exception {
        XMLReaderFactory factory = XMLReaderFactory.newInstance(xmlObjects);
        try (XMLReader reader = factory.createReader(new ByteArrayInputStream(xml), encoding)) {
            while (reader.hasNext()) {
                if (reader.nextTag() == EventType.START_ELEMENT) {
                    blackhole.consume(reader.getAttributes());
                    if (reader.getName().getLocalPart().equals("posList")) {
                        blackhole.consume(reader.getTextContent());
                    }
                }
            }
        }
    }

    private void write(OutputStream out) throws Exception {
        XMLWriterFactory factory = XMLWriterFactory.newInstance(xmlObjects);
        try (XMLWriter writer = factory.createWriter(out, encoding)) {
            writer.withPrefix("core", CORE)
                    .withPrefix("bldg", BLDG)
                    .withPrefix("gml", GML)
                    .withPrefix("xlink", XLINK)
                    .withIndent(indent.isEmpty() ? null : indent);
            writer.writeStartDocument();
            XMLTestSupport.write(writer, document);
            writer.writeEndDocument();
        }
    }

    private static class CountingOutputStream extends OutputStream {
        long count;

        @Override
        public void write(int b) {
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            count += len;
        }
    }
}
