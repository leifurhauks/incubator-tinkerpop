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
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.GremlinProcessRunner;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.MapHelper;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.finalization.MatchAlgorithmStrategy;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.GRATEFUL;
import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.neq;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
import static org.junit.Assert.*;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(GremlinProcessRunner.class)
public abstract class MatchTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_valueMap_matchXa_selectXnameX_bX();

    // very basic query
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_out_bX();

    // query with selection
    public abstract Traversal<Vertex, Object> get_g_V_matchXa_out_bX_selectXb_idX();

    // linked traversals
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__b_created_cX();

    // a basic tree with two leaves
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__a_created_cX();

    // a tree with three leaves
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXd_0knows_a__d_hasXname_vadasX__a_knows_b__b_created_cX();

    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__a_repeatXoutX_timesX2XX_selectXa_bX();

    // illustrates early deduplication in "predicate" traversals
    public abstract Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_lop_b__b_0created_29_c__c_whereXrepeatXoutX_timesX2XXX();

    public abstract Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_lop_b__b_0created_29_cX_whereXc_repeatXoutX_timesX2XX_selectXa_b_cX();

    public abstract Traversal<Vertex, String> get_g_V_out_out_matchXa_0created_b__b_0knows_cX_selectXcX_outXcreatedX_name();

    //TODO: with Traversal.reverse()
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__b_0created_aX();

    // contains an unreachable label
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__c_knows_bX();

    // nested match()
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__b_created_lop__b_matchXb_created_d__d_0created_cX_selectXcX_cX_selectXa_b_cX();

    // contains a pair of traversals which connect the same labels, together with a predicate traversal
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__a_0sungBy_bX();

    // contains an identical pair of sets of traversals, up to variable names and has() conditions
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0sungBy_c__b_writtenBy_d__c_writtenBy_e__d_hasXname_George_HarisonX__e_hasXname_Bob_MarleyXX();

    // forms a non-trivial DAG
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_d__c_sungBy_d__d_hasXname_GarciaXX();

    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_dX_whereXc_sungBy_dX_whereXd_hasXname_GarciaXX();

    // inclusion of where
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__b_0created_cX_whereXa_neq_cX_selectXa_cX();

    //TODO: with Traversal.reverse()
    public abstract Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_b__c_created_bX_selectXa_b_cX_byXnameX();

    public abstract Traversal<Vertex, String> get_g_V_out_asXcX_matchXb_knows_a__c_created_eX_selectXcX();

    // nested or/and with patterns in order that won't execute serially
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_whereXa_neqXcXX__a_created_b__orXa_knows_vadas__a_0knows_and_a_hasXlabel_personXX__b_0created_c__b_0created_count_isXgtX1XXX_selectXa_b_cX_byXidX();

    // uses local barrier count() and no start key
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_matchXa_out_count_c__b_in_count_cX();

    // pulls out has container for index lookup and uses an where() with startKey and predicate
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__b_followedBy_c__c_writtenBy_d__whereXd_neqXaXXX();

    // nested and with oddly dependent end steps
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_knows_b__andXa_created_c__b_created_c__andXb_created_count_d__a_knows_count_dXXX();

    // nested or with infix and and variable dependencies at different depths
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_matchXa_out_count_c__orXa_knows_b__b_in_count_c__and__c_isXgtX2XXXX();

    // uses a not traversal pattern
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXa__a_out_b__notXa_created_bXX();

    // uses 'out of order' conjunction nested where()
    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXwhereXandXa_created_b__b_0created_count_isXeqX3XXXX__a_both_b__whereXb_inXX();

    // distinct values
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa__a_both_b__b_both_cX_dedupXa_bX();

    // distinct values with by()-modulation
    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_both_b__b_both_cX_dedupXa_bX_byXlabelX();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_valueMap_matchXa_selectXnameX_bX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_valueMap_matchXa_selectXnameX_bX();
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            final Map<String, Object> map = traversal.next();
            assertTrue(Map.class.isAssignableFrom(map.get("a").getClass()));
            final String name = ((Map<String,List<String>>) map.get("a")).get("name").get(0);
            assertEquals(name, ((List<String>) map.get("b")).get(0));
        }
        assertEquals(6, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_out_bX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_out_bX();
        printTraversalForm(traversal);
        checkResults(makeMapList(2,
                        "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"),
                        "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"),
                        "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "vadas"),
                        "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "ripple"),
                        "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "lop"),
                        "a", convertToVertex(graph, "peter"), "b", convertToVertex(graph, "lop")),
                traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_out_bX_selectXb_idX() throws Exception {
        final Traversal<Vertex, Object> traversal = get_g_V_matchXa_out_bX_selectXb_idX();
        printTraversalForm(traversal);
        int counter = 0;
        final Object vadasId = convertToVertexId("vadas");
        final Object joshId = convertToVertexId("josh");
        final Object lopId = convertToVertexId("lop");
        final Object rippleId = convertToVertexId("ripple");
        Map<Object, Long> idCounts = new HashMap<>();
        while (traversal.hasNext()) {
            counter++;
            MapHelper.incr(idCounts, traversal.next(), 1l);
        }
        assertFalse(traversal.hasNext());
        assertEquals(idCounts.get(vadasId), Long.valueOf(1l));
        assertEquals(idCounts.get(lopId), Long.valueOf(3l));
        assertEquals(idCounts.get(joshId), Long.valueOf(1l));
        assertEquals(idCounts.get(rippleId), Long.valueOf(1l));
        assertEquals(6, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_knows_b__b_created_cX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_knows_b__b_created_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "lop"),
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "ripple")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_knows_b__a_created_cX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_knows_b__a_created_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "vadas"), "c", convertToVertex(graph, "lop"),
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "lop")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXd_0knows_a__d_hasXname_vadasX__a_knows_b__b_created_cX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXd_0knows_a__d_hasXname_vadasX__a_knows_b__b_created_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(4,
                "d", convertToVertex(graph, "vadas"), "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "lop"),
                "d", convertToVertex(graph, "vadas"), "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "ripple")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_b__a_repeatXoutX_timesX2XX_selectXab_nameX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_created_b__a_repeatXoutX_timesX2XX_selectXa_bX();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        checkResults(makeMapList(2,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_lop_b__b_0created_29_cX_whereXc_repeatXoutX_timesX2XX_selectXa_b_cX() throws Exception {
        final Traversal<Vertex, Map<String, String>> traversal = get_g_V_matchXa_created_lop_b__b_0created_29_cX_whereXc_repeatXoutX_timesX2XX_selectXa_b_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "peter"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_lop_b__b_0created_29_c__c_whereXrepeatXoutX_timesX2XXX() throws Exception {
        final Traversal<Vertex, Map<String, String>> traversal = get_g_V_matchXa_created_lop_b__b_0created_29_c__c_whereXrepeatXoutX_timesX2XXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "peter"), "b", convertToVertex(graph, "lop"), "c", convertToVertex(graph, "marko")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_out_out_matchXa_0created_b__b_0knows_cX_selectXcX_outXcreatedX_name() throws Exception {
        final Traversal<Vertex, String> traversal = get_g_V_out_out_matchXa_0created_b__b_0knows_cX_selectXcX_outXcreatedX_name();
        printTraversalForm(traversal);
        assertEquals("lop", traversal.next());
        assertEquals("lop", traversal.next());
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_b__b_0created_aX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_created_b__b_0created_aX();
        printTraversalForm(traversal);
        checkResults(makeMapList(2,
                        "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"),
                        "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "lop"),
                        "a", convertToVertex(graph, "peter"), "b", convertToVertex(graph, "lop"),
                        "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "ripple")),
                traversal);

    }

    // TODO: this test requires Traversal.reverse()
    @Test(expected = IllegalStateException.class)
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_knows_b__c_knows_bX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_knows_b__c_knows_bX();
        printTraversalForm(traversal);
        traversal.iterate();
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_knows_b__b_created_lop__b_matchXb_created_d__d_0created_cX_selectXcX_cX_selectXa_b_cX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_knows_b__b_created_lop__b_matchXb_created_d__d_0created_cX_selectXcX_cX_selectXa_b_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "josh"),
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "josh"), // expected duplicate: two paths to this solution
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "peter")), traversal);
    }

    // TODO: this test requires Traversal.reverse()
    @Test(expected = IllegalStateException.class)
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_b__c_created_bX_selectXa_b_cX_byXnameX() throws Exception {
        final Traversal<Vertex, Map<String, String>> traversal = get_g_V_matchXa_created_b__c_created_bX_selectXa_b_cX_byXnameX();
        printTraversalForm(traversal);
        traversal.iterate();
    }

    @Test(expected = IllegalStateException.class)
    @LoadGraphWith(MODERN)
    public void g_V_out_asXcX_matchXb_knows_a__c_created_eX_selectXcX() throws Exception {
        final Traversal<Vertex, String> traversal = get_g_V_out_asXcX_matchXb_knows_a__c_created_eX_selectXcX();
        printTraversalForm(traversal);
        traversal.iterate();
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__a_0sungBy_bX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__a_0sungBy_bX();
        printTraversalForm(traversal);
        checkResults(makeMapList(2,
                "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CREAM PUFF WAR"),
                "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT")), traversal);
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_matchXa_0sungBy_b__a_0sungBy_c__b_writtenBy_d__c_writtenBy_e__d_hasXname_George_HarisonX__e_hasXname_Bob_MarleyXX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_0sungBy_b__a_0sungBy_c__b_writtenBy_d__c_writtenBy_e__d_hasXname_George_HarisonX__e_hasXname_Bob_MarleyXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(5,
                "a", convertToVertex(graph, "Garcia"),
                "b", convertToVertex(graph, "I WANT TO TELL YOU"),
                "c", convertToVertex(graph, "STIR IT UP"),
                "d", convertToVertex(graph, "George_Harrison"),
                "e", convertToVertex(graph, "Bob_Marley")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_created_b__b_0created_cX_whereXa_neq_cX_selectXa_cX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_created_b__b_0created_cX_whereXa_neq_cX_selectXa_cX();
        checkResults(makeMapList(2,
                "a", convertToVertex(graph, "marko"), "c", convertToVertex(graph, "josh"),
                "a", convertToVertex(graph, "marko"), "c", convertToVertex(graph, "peter"),
                "a", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "peter"),
                "a", convertToVertex(graph, "peter"), "c", convertToVertex(graph, "marko"),
                "a", convertToVertex(graph, "peter"), "c", convertToVertex(graph, "josh")), traversal);
    }


    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_d__c_sungBy_d__d_hasXname_GarciaXX() throws Exception {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_d__c_sungBy_d__d_hasXname_GarciaXX();
        printTraversalForm(traversal);
        assertCommon(traversal);
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_dX_whereXc_sungBy_dX_whereXd_hasXname_GarciaXX() throws Exception {
        // TODO: the where() is trying to get Garcia's name. Why is ComputerVerificationStrategy allowing this?
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_dX_whereXc_sungBy_dX_whereXd_hasXname_GarciaXX();
        printTraversalForm(traversal);
        assertCommon(traversal);
    }

    private void assertCommon(Traversal<Vertex, Map<String, Vertex>> traversal) {
        checkResults(makeMapList(4,
                    "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CREAM PUFF WAR"), "c", convertToVertex(graph, "CREAM PUFF WAR"), "d", convertToVertex(graph, "Garcia"),
                    "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CREAM PUFF WAR"), "c", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "d", convertToVertex(graph, "Garcia"),
                    "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "c", convertToVertex(graph, "CREAM PUFF WAR"), "d", convertToVertex(graph, "Garcia"),
                    "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "c", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "d", convertToVertex(graph, "Garcia"),
                    "a", convertToVertex(graph, "Grateful_Dead"), "b", convertToVertex(graph, "CANT COME DOWN"), "c", convertToVertex(graph, "DOWN SO LONG"), "d", convertToVertex(graph, "Garcia"),
                    "a", convertToVertex(graph, "Grateful_Dead"), "b", convertToVertex(graph, "THE ONLY TIME IS NOW"), "c", convertToVertex(graph, "DOWN SO LONG"), "d", convertToVertex(graph, "Garcia")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_whereXa_neqXcXX__a_created_b__orXa_knows_vadas__a_0knows_and_a_hasXlabel_personXX__b_0created_c__b_0created_count_isXgtX1XXX_selectXa_b_cX_byXidX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXa_whereXa_neqXcXX__a_created_b__orXa_knows_vadas__a_0knows_and_a_hasXlabel_personXX__b_0created_c__b_0created_count_isXgtX1XXX_selectXa_b_cX_byXidX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertexId("marko"), "b", convertToVertexId("lop"), "c", convertToVertexId("josh"),
                "a", convertToVertexId("marko"), "b", convertToVertexId("lop"), "c", convertToVertexId("peter"),
                "a", convertToVertexId("josh"), "b", convertToVertexId("lop"), "c", convertToVertexId("marko"),
                "a", convertToVertexId("josh"), "b", convertToVertexId("lop"), "c", convertToVertexId("peter")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_out_asXbX_matchXa_out_count_c__b_in_count_cX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_asXaX_out_asXbX_matchXa_out_count_c__b_in_count_cX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3, "a", convertToVertex(graph, "marko"), "c", 3l, "b", convertToVertex(graph, "lop")), traversal);
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__b_followedBy_c__c_writtenBy_d__whereXd_neqXaXXX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__b_followedBy_c__c_writtenBy_d__whereXd_neqXaXXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(4,
                "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "c", convertToVertex(graph, "WHARF RAT"), "d", convertToVertex(graph, "Hunter"),
                "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "c", convertToVertex(graph, "THE OTHER ONE"), "d", convertToVertex(graph, "Weir"),
                "a", convertToVertex(graph, "Garcia"), "b", convertToVertex(graph, "CRYPTICAL ENVELOPMENT"), "c", convertToVertex(graph, "DRUMS"), "d", convertToVertex(graph, "Grateful_Dead")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_knows_b__andXa_created_c__b_created_c__andXb_created_count_d__a_knows_count_dXXX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXa_knows_b__andXa_created_c__b_created_c__andXb_created_count_d__a_knows_count_dXXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(4,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", convertToVertex(graph, "lop"), "d", 2l), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_out_asXbX_matchXa_out_count_c__orXa_knows_b__b_in_count_c__and__c_isXgtX2XXXX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_asXaX_out_asXbX_matchXa_out_count_c__orXa_knows_b__b_in_count_c__and__c_isXgtX2XXXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(3,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"), "c", 3l,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "vadas"), "c", 3l,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"), "c", 3l), traversal);

    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa__a_out_b__notXa_created_bXX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXa__a_out_b__notXa_created_bXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(2,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "josh"),
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "vadas")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXwhereXandXa_created_b__b_0created_count_isXeqX3XXXX__a_both_bX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXwhereXandXa_created_b__b_0created_count_isXeqX3XXXX__a_both_b__whereXb_inXX();
        printTraversalForm(traversal);
        checkResults(makeMapList(2,
                "a", convertToVertex(graph, "marko"), "b", convertToVertex(graph, "lop"),
                "a", convertToVertex(graph, "josh"), "b", convertToVertex(graph, "lop"),
                "a", convertToVertex(graph, "peter"), "b", convertToVertex(graph, "lop")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa__a_both_b__b_both_cX_dedupXa_bX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa__a_both_b__b_both_cX_dedupXa_bX();
        printTraversalForm(traversal);
        int counter = 0;
        final Set<List<Vertex>> results = new HashSet<>();
        while (traversal.hasNext()) {
            final Map<String, Vertex> map = traversal.next();
            assertEquals(3, map.size());
            assertTrue(results.add(Arrays.asList(map.get("a"), map.get("b"))));
            counter++;
        }
        assertEquals(12, counter);
        assertEquals(results.size(), counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_matchXa_both_b__b_both_cX_dedupXa_bX_byXlabelX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_matchXa_both_b__b_both_cX_dedupXa_bX_byXlabelX();
        printTraversalForm(traversal);
        int counter = 0;
        final Set<List<String>> results = new HashSet<>();
        while (traversal.hasNext()) {
            final Map<String, Vertex> map = traversal.next();
            assertEquals(3, map.size());
            assertTrue(results.add(Arrays.asList(map.get("a").label(), map.get("b").label())));
            counter++;
        }
        assertEquals(3, counter);
        assertEquals(results.size(), counter);
    }

    public static class GreedyMatchTraversals extends Traversals {
        @Before
        public void setupTest() {
            super.setupTest();
            g = graphProvider.traversal(graph, MatchAlgorithmStrategy.build().algorithm(MatchStep.GreedyMatchAlgorithm.class).create());
        }
    }

    public static class CountMatchTraversals extends Traversals {
        // make sure default works -- i.e. CountMatchAlgorithm
        /*@Before
        public void setupTest() {
            super.setupTest();
            g = graphProvider.traversal(graph, MatchAlgorithmStrategy.build().algorithm(MatchStep.CountMatchAlgorithm.class).create());
        }*/
    }

    public abstract static class Traversals extends MatchTest {
        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_valueMap_matchXa_selectXnameX_bX() {
            return g.V().valueMap().match(as("a").select("name").as("b"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_out_bX() {
            return g.V().match(as("a").out().as("b"));
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_matchXa_out_bX_selectXb_idX() {
            return g.V().match(as("a").out().as("b")).select("b").by(T.id);
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__b_created_cX() {
            return g.V().match(
                    as("a").out("knows").as("b"),
                    as("b").out("created").as("c"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__a_created_cX() {
            return g.V().match(
                    as("a").out("knows").as("b"),
                    as("a").out("created").as("c"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXd_0knows_a__d_hasXname_vadasX__a_knows_b__b_created_cX() {
            return g.V().match(
                    as("d").in("knows").as("a"),
                    as("d").has("name", "vadas"),
                    as("a").out("knows").as("b"),
                    as("b").out("created").as("c"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__a_repeatXoutX_timesX2XX_selectXa_bX() {
            return g.V().match(
                    as("a").out("created").as("b"),
                    __.<Vertex>as("a").repeat(out()).times(2).as("b")).<Vertex>select("a", "b");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_lop_b__b_0created_29_c__c_whereXrepeatXoutX_timesX2XXX() {
            return g.V().match(
                    as("a").out("created").has("name", "lop").as("b"),
                    as("b").in("created").has("age", 29).as("c"),
                    as("c").where(repeat(out()).times(2)));
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_lop_b__b_0created_29_cX_whereXc_repeatXoutX_timesX2XX_selectXa_b_cX() {
            return g.V().match(
                    as("a").out("created").has("name", "lop").as("b"),
                    as("b").in("created").has("age", 29).as("c"))
                    .where(__.<Vertex>as("c").repeat(out()).times(2)).select("a", "b", "c");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_out_out_matchXa_0created_b__b_0knows_cX_selectXcX_outXcreatedX_name() {
            return g.V().out().out().match(
                    as("a").in("created").as("b"),
                    as("b").in("knows").as("c")).select("c").out("created").values("name");
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__b_0created_aX() {
            return g.V().match(
                    as("a").out("created").as("b"),
                    as("b").in("created").as("a"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__c_knows_bX() {
            return g.V().match(as("a").out("knows").as("b"),
                    as("c").out("knows").as("b"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_knows_b__b_created_lop__b_matchXb_created_d__d_0created_cX_selectXcX_cX_selectXa_b_cX() {
            return g.V().match(
                    as("a").out("knows").as("b"),
                    as("b").out("created").has("name", "lop"),
                    as("b").match(
                            as("b").out("created").as("d"),
                            as("d").in("created").as("c")).select("c").as("c")).<Vertex>select("a", "b", "c");
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__a_0sungBy_bX() {
            return g.V().match(
                    as("a").has("name", "Garcia"),
                    as("a").in("writtenBy").as("b"),
                    as("a").in("sungBy").as("b"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0sungBy_c__b_writtenBy_d__c_writtenBy_e__d_hasXname_George_HarisonX__e_hasXname_Bob_MarleyXX() {
            return g.V().match(
                    as("a").in("sungBy").as("b"),
                    as("a").in("sungBy").as("c"),
                    as("b").out("writtenBy").as("d"),
                    as("c").out("writtenBy").as("e"),
                    as("d").has("name", "George_Harrison"),
                    as("e").has("name", "Bob_Marley"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_d__c_sungBy_d__d_hasXname_GarciaXX() {
            return g.V().match(
                    as("a").in("sungBy").as("b"),
                    as("a").in("writtenBy").as("c"),
                    as("b").out("writtenBy").as("d"),
                    as("c").out("sungBy").as("d"),
                    as("d").has("name", "Garcia"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_0sungBy_b__a_0writtenBy_c__b_writtenBy_dX_whereXc_sungBy_dX_whereXd_hasXname_GarciaXX() {
            return g.V().<Vertex>match(
                    as("a").in("sungBy").as("b"),
                    as("a").in("writtenBy").as("c"),
                    as("b").out("writtenBy").as("d"))
                    .where(as("c").out("sungBy").as("d"))
                    .where(as("d").has("name", "Garcia"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_created_b__b_0created_cX_whereXa_neq_cX_selectXa_cX() {
            return g.V().match(
                    as("a").out("created").as("b"),
                    as("b").in("created").as("c"))
                    .where("a", neq("c"))
                    .<Vertex>select("a", "c");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_matchXa_created_b__c_created_bX_selectXa_b_cX_byXnameX() {
            return g.V().match(
                    as("a").out("created").as("b"),
                    as("c").out("created").as("b")).<String>select("a", "b", "c").by("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_out_asXcX_matchXb_knows_a__c_created_eX_selectXcX() {
            return g.V().out().as("c").match(
                    as("b").out("knows").as("a"),
                    as("c").out("created").as("e")).select("c");
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_whereXa_neqXcXX__a_created_b__orXa_knows_vadas__a_0knows_and_a_hasXlabel_personXX__b_0created_c__b_0created_count_isXgtX1XXX_selectXa_b_cX_byXidX() {
            return g.V().match(
                    where("a", P.neq("c")),
                    as("a").out("created").as("b"),
                    or(
                            as("a").out("knows").has("name", "vadas"),
                            as("a").in("knows").and().as("a").has(T.label, "person")
                    ),
                    as("b").in("created").as("c"),
                    as("b").in("created").count().is(P.gt(1)))
                    .select("a", "b", "c").by(T.id);
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_matchXa_out_count_c__b_in_count_cX() {
            return g.V().as("a").out().as("b").match(as("a").out().count().as("c"), as("b").in().count().as("c"));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_hasXname_GarciaX__a_0writtenBy_b__b_followedBy_c__c_writtenBy_d__whereXd_neqXaXXX() {
            return g.V().match(
                    as("a").has("name", "Garcia"),
                    as("a").in("writtenBy").as("b"),
                    as("b").out("followedBy").as("c"),
                    as("c").out("writtenBy").as("d"),
                    where("d", P.neq("a")));
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_knows_b__andXa_created_c__b_created_c__andXb_created_count_d__a_knows_count_dXXX() {
            return g.V().match(
                    as("a").out("knows").as("b"),
                    and(
                            as("a").out("created").as("c"),
                            as("b").out("created").as("c"),
                            and(
                                    as("b").out("created").count().as("d"),
                                    as("a").out("knows").count().as("d")
                            )
                    ));
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_matchXa_out_count_c__orXa_knows_b__b_in_count_c__and__c_isXgtX2XXXX() {
            return g.V().as("a").out().as("b").
                    match(
                            as("a").out().count().as("c"),
                            or(
                                    as("a").out("knows").as("b"),
                                    as("b").in().count().as("c").and().as("c").is(P.gt(2))
                            )
                    );
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa__a_out_b__notXa_created_bXX() {
            return g.V().match(
                    as("a").out().as("b"),
                    not(as("a").out("created").as("b")));
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXwhereXandXa_created_b__b_0created_count_isXeqX3XXXX__a_both_b__whereXb_inXX() {
            return g.V().match(
                    where(and(
                            as("a").out("created").as("b"),
                            as("b").in("created").count().is(eq(3)))),
                    as("a").both().as("b"),
                    where(as("b").in()));
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa__a_both_b__b_both_cX_dedupXa_bX() {
            return g.V().<Vertex>match(
                    as("a").both().as("b"),
                    as("b").both().as("c")).dedup("a", "b");
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_matchXa_both_b__b_both_cX_dedupXa_bX_byXlabelX() {
            return g.V().<Vertex>match(
                    as("a").both().as("b"),
                    as("b").both().as("c")).dedup("a", "b").by(T.label);
        }
    }
}
