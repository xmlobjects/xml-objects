/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlobjects.XMLObjects;
import org.xmlobjects.stream.XMLTestSupport.Node;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round trip of a generated document that mimics the structure of CityGML: all elements prefixed,
 * namespaces declared on the root, deep nesting, namespaced attributes and long coordinate lists.
 */
class CityGMLShapedRoundTripTest {
    static final String CORE = "http://www.opengis.net/citygml/2.0";
    static final String BLDG = "http://www.opengis.net/citygml/building/2.0";
    static final String GML = "http://www.opengis.net/gml";
    static final String XLINK = "http://www.w3.org/1999/xlink";

    private static final Pattern START_TAG = Pattern.compile("<([A-Za-z_][\\w.-]*)(:)?([\\w.-]*)");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"  "})
    void roundTrip(String indent) throws Exception {
        Node expected = createCityModel(new Random(42), 20);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance(XMLObjects.newInstance());
        try (XMLWriter writer = factory.createWriter(out)) {
            writer.withPrefix("core", CORE)
                    .withPrefix("bldg", BLDG)
                    .withPrefix("gml", GML)
                    .withPrefix("xlink", XLINK)
                    .withIndent(indent);
            writer.writeStartDocument();
            XMLTestSupport.write(writer, expected);
            writer.writeEndDocument();
        }

        byte[] xml = out.toByteArray();
        assertEquals(expected, XMLTestSupport.parse(xml));

        // every element uses one of the declared prefixes, and namespaces are only declared on the root
        String doc = new String(xml, StandardCharsets.UTF_8);
        Matcher matcher = START_TAG.matcher(doc);
        while (matcher.find()) {
            if (matcher.group(1).equals("?xml")) {
                continue;
            }

            assertTrue(matcher.group(2) != null && matcher.group(1).matches("core|bldg|gml"),
                    "unexpected element name: " + matcher.group());
        }

        int rootEnd = doc.indexOf('>', doc.indexOf("<core:CityModel"));
        assertEquals(-1, doc.indexOf("xmlns", rootEnd), "namespaces should only be declared on the root element");
    }

    static Node createCityModel(Random random, int buildings) {
        Node cityModel = new Node(new QName(CORE, "CityModel"));
        for (int b = 0; b < buildings; b++) {
            Node building = new Node(new QName(BLDG, "Building"))
                    .attribute(new QName(GML, "id"), "BLDG_" + b)
                    .child(new Node(new QName(GML, "name")).text("Building " + b))
                    .child(new Node(new QName(BLDG, "measuredHeight"))
                            .attribute(new QName("uom"), "m")
                            .text(String.valueOf(5 + random.nextInt(50))));

            for (int s = 0; s < 6; s++) {
                Node posList = new Node(new QName(GML, "posList"))
                        .attribute(new QName("srsDimension"), "3")
                        .text(coordinates(random, 5 + random.nextInt(200)));

                Node polygon = new Node(new QName(GML, "Polygon"))
                        .attribute(new QName(GML, "id"), "POLY_" + b + "_" + s)
                        .child(new Node(new QName(GML, "exterior"))
                                .child(new Node(new QName(GML, "LinearRing")).child(posList)));

                building.child(new Node(new QName(BLDG, "boundedBy"))
                        .child(new Node(new QName(BLDG, s == 0 ? "GroundSurface" : s == 5 ? "RoofSurface" : "WallSurface"))
                                .child(new Node(new QName(BLDG, "lod2MultiSurface"))
                                        .child(new Node(new QName(GML, "MultiSurface"))
                                                .child(new Node(new QName(GML, "surfaceMember")).child(polygon))))));
            }

            building.child(new Node(new QName(BLDG, "lod2Solid"))
                    .attribute(new QName(XLINK, "href"), "#SOLID_" + b));

            cityModel.child(new Node(new QName(CORE, "cityObjectMember")).child(building));
        }

        return cityModel;
    }

    private static String coordinates(Random random, int points) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < points * 3; i++) {
            if (i > 0) {
                builder.append(' ');
            }

            builder.append(random.nextDouble() * 1_000_000);
        }

        return builder.toString();
    }
}
