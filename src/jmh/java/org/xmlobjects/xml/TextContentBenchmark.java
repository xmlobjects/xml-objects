/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Parses a coordinate list as found in gml:posList.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class TextContentBenchmark {
    @Param({"15", "300"})
    public int values;

    private String posList;

    @Setup
    public void setup() {
        Random random = new Random(42);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values; i++) {
            if (i > 0) {
                builder.append(' ');
            }

            builder.append(random.nextDouble() * 1_000_000);
        }

        posList = builder.toString();
    }

    @Benchmark
    public List<Double> parseDoubleList() {
        // a new TextContent per call, as when reading a document
        return TextContent.of(posList).getAsDoubleList();
    }
}
