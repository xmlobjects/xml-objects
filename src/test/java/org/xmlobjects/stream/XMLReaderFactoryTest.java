/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xmlobjects.XMLObjects;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers review item C1: readers created from a file or path must close the underlying stream.
 */
class XMLReaderFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void closeReleasesPath() throws Exception {
        Path file = createFile("path.xml");
        XMLReader reader = XMLReaderFactory.newInstance(XMLObjects.newInstance()).createReader(file);
        readAndClose(reader);
        assertReleased(file);
    }

    @Test
    void closeReleasesFile() throws Exception {
        Path file = createFile("file.xml");
        XMLReader reader = XMLReaderFactory.newInstance(XMLObjects.newInstance()).createReader(file.toFile(), "UTF-8");
        readAndClose(reader);
        assertReleased(file);
    }

    @Test
    void failedCreationReleasesFile() throws Exception {
        Path file = tempDir.resolve("broken.xml");
        Files.writeString(file, "<root>", StandardCharsets.UTF_8);
        XMLReaderFactory factory = XMLReaderFactory.newInstance(XMLObjects.newInstance());

        assertThrows(XMLReadException.class, () -> factory.createReader(file, "no-such-encoding"));
        assertReleased(file);
    }

    @Test
    void systemIdWithEncoding() throws Exception {
        String systemId = "http://example.org/data/city.gml";
        byte[] xml = "<root>äöü<broken></root>".getBytes(StandardCharsets.ISO_8859_1);
        XMLReaderFactory factory = XMLReaderFactory.newInstance(XMLObjects.newInstance());

        try (XMLReader reader = factory.createReader(systemId, new java.io.ByteArrayInputStream(xml), "ISO-8859-1")) {
            assertEquals(java.net.URI.create(systemId), reader.getBaseURI());
            reader.nextTag();
            assertEquals("äöü", reader.getTextContent().get());
            XMLReadException e = assertThrows(XMLReadException.class, () -> {
                while (reader.hasNext()) {
                    reader.nextTag();
                }
            });

            javax.xml.stream.XMLStreamException cause = (javax.xml.stream.XMLStreamException) e.getCause();
            assertEquals(systemId, cause.getLocation().getSystemId());
        }
    }

    private Path createFile(String name) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, "<root><child>text</child></root>", StandardCharsets.UTF_8);
        return file;
    }

    private void readAndClose(XMLReader reader) throws XMLReadException {
        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals(EventType.START_ELEMENT, reader.nextTag());
        assertEquals("text", reader.getTextContent().get());
        reader.close();
    }

    private void assertReleased(Path file) {
        // on Windows, deleting or truncating a file fails while a stream is still open on it
        assertDoesNotThrow(() -> {
            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
                channel.truncate(0);
            }
            Files.delete(file);
        });
    }
}
