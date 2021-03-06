/*
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
 */
package org.apache.tinkerpop.gremlin.spark.process.computer;

import org.apache.tinkerpop.gremlin.GraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.groovy.util.SugarTestHelper;
import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.hadoop.HadoopGraphProvider;
import org.apache.tinkerpop.gremlin.hadoop.groovy.plugin.HadoopGremlinPluginCheck;
import org.apache.tinkerpop.gremlin.hadoop.structure.io.FileSystemStorageCheck;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.engine.ComputerTraversalEngine;
import org.apache.tinkerpop.gremlin.spark.structure.Spark;
import org.apache.tinkerpop.gremlin.spark.structure.io.PersistedOutputRDD;
import org.apache.tinkerpop.gremlin.spark.structure.io.SparkContextStorageCheck;
import org.apache.tinkerpop.gremlin.spark.structure.io.ToyGraphInputRDD;
import org.apache.tinkerpop.gremlin.spark.structure.io.gryo.GryoSerializer;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Map;
import java.util.Random;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@GraphProvider.Descriptor(computer = SparkGraphComputer.class)
public final class SparkHadoopGraphProvider extends HadoopGraphProvider {

    private static final Random RANDOM = new Random();

    @Override
    public Map<String, Object> getBaseConfiguration(final String graphName, final Class<?> test, final String testMethodName, final LoadGraphWith.GraphData loadGraphWith) {
        final Map<String, Object> config = super.getBaseConfiguration(graphName, test, testMethodName, loadGraphWith);
        config.put(Constants.GREMLIN_SPARK_PERSIST_CONTEXT, true);  // this makes the test suite go really fast
        if (!test.equals(FileSystemStorageCheck.class) && null != loadGraphWith && RANDOM.nextBoolean()) {
            config.put(Constants.GREMLIN_SPARK_GRAPH_INPUT_RDD, ToyGraphInputRDD.class.getCanonicalName());
        }

        // tests persisted RDDs
        if (test.equals(SparkContextStorageCheck.class)) {
            config.put(Constants.GREMLIN_SPARK_GRAPH_INPUT_RDD, ToyGraphInputRDD.class.getCanonicalName());
            config.put(Constants.GREMLIN_SPARK_GRAPH_OUTPUT_RDD, PersistedOutputRDD.class.getCanonicalName());
        }

        // sugar plugin causes meta-method issues with a persisted context
        if (test.equals(HadoopGremlinPluginCheck.class)) {
            Spark.close();
            SugarTestHelper.clearRegistry(this);
        }

        /// spark configuration
        config.put("spark.master", "local[4]");
        config.put("spark.serializer", GryoSerializer.class.getCanonicalName());
        config.put("spark.kryo.registrationRequired", true);
        return config;
    }

    @Override
    public GraphTraversalSource traversal(final Graph graph) {
        return RANDOM.nextBoolean() ?
                GraphTraversalSource.build().engine(ComputerTraversalEngine.build().computer(SparkGraphComputer.class).workers(RANDOM.nextInt(3) + 1)).create(graph) :
                GraphTraversalSource.build().engine(ComputerTraversalEngine.build().computer(SparkGraphComputer.class)).create(graph);
    }
}