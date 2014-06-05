package com.tinkerpop.gremlin.structure.strategy;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.TraversalStrategy;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.filter.FilterStep;
import com.tinkerpop.gremlin.process.graph.map.EdgeVertexStep;
import com.tinkerpop.gremlin.process.graph.map.GraphStep;
import com.tinkerpop.gremlin.process.graph.map.VertexStep;
import com.tinkerpop.gremlin.process.util.Reversible;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class SubgraphStrategy implements GraphStrategy {

    private final Predicate<Vertex> vertexCriterion;
    private final Predicate<Edge> edgeCriterion;

    public SubgraphStrategy(final Predicate<Vertex> vertexCriterion, final Predicate<Edge> edgeCriterion) {
        this.vertexCriterion = vertexCriterion;
        this.edgeCriterion = edgeCriterion;
    }

    @Override
    public GraphTraversal applyStrategyToTraversal(final GraphTraversal traversal) {
        traversal.strategies().register(new SubgraphGraphTraversalStrategy());
        return traversal;
    }

    @Override
    public UnaryOperator<Function<Object, Vertex>> getGraphvStrategy(final Strategy.Context<StrategyWrappedGraph> ctx) {
        return (f) -> (id) -> {
            final Vertex v = f.apply(id);
            if (!testVertex(v)) {
                throw Graph.Exceptions.elementNotFound();
            }

            return new StrategyWrappedVertex(v, ctx.getCurrent());
        };
    }

    @Override
    public UnaryOperator<Function<Object, Edge>> getGrapheStrategy(final Strategy.Context<StrategyWrappedGraph> ctx) {
        return (f) -> (id) -> {
            final Edge e = f.apply(id);

            if (!testEdge(e)) {
                throw Graph.Exceptions.elementNotFound();
            }

            return new StrategyWrappedEdge(e, ctx.getCurrent());
        };
    }

    private boolean testVertex(final Vertex vertex) {
        return vertexCriterion.test(vertex);
    }

    private boolean testEdge(final Edge edge) {
        // the edge must pass the edge criterion, and both of its incident vertices must also pass the vertex criterion
        return edgeCriterion.test(edge) && edge.inV().hasNext() && edge.outV().hasNext();
    }

    private boolean testElement(final Element element) {
        return element instanceof Vertex
                ? testVertex((Vertex) element)
                : testEdge((Edge) element);
    }

    /*
    @Override
    public UnaryOperator<Function<Object[], Vertex>> getAddVertexStrategy(final Strategy.Context<StrategyWrappedGraph> ctx) {
        return (f) -> (keyValues) -> {
            final List<Object> o = new ArrayList<>(Arrays.asList(keyValues));
            o.addAll(Arrays.asList(this.partitionKey, writePartition));
            return f.apply(o.toArray());
        };
    }

    @Override
    public UnaryOperator<TriFunction<String, Vertex, Object[], Edge>> getAddEdgeStrategy(final Strategy.Context<StrategyWrappedVertex> ctx) {
        return (f) -> (label, v, keyValues) -> {
            final List<Object> o = new ArrayList<>(Arrays.asList(keyValues));
            o.addAll(Arrays.asList(this.partitionKey, writePartition));
            return f.apply(label, v, o.toArray());
        };
    }*/

	@Override
	public String toString() {
		return SubgraphStrategy.class.getSimpleName();
	}

    public class SubgraphGraphTraversalStrategy implements TraversalStrategy.FinalTraversalStrategy {

        public void apply(final Traversal traversal) {
            // inject a SubgraphFilterStep after each GraphStep, VertexStep or EdgeVertexStep
            final List<Class> stepsToLookFor = Arrays.<Class>asList(GraphStep.class, VertexStep.class, EdgeVertexStep.class);
            final List<Integer> positions = new ArrayList<>();
            final List<?> traversalSteps = traversal.getSteps();
            for (int ix = 0; ix < traversalSteps.size(); ix++) {
                final int pos = ix;
                if (stepsToLookFor.stream().anyMatch(c -> c.isAssignableFrom(traversalSteps.get(pos).getClass()))) positions.add(ix);
            }

            Collections.reverse(positions);
            for (int pos : positions) {
                TraversalHelper.insertStep(new SubgraphFilterStep(traversal), pos + 1, traversal);
            }
        }
    }

    private class SubgraphFilterStep extends FilterStep<Element> implements Reversible {

        public SubgraphFilterStep(final Traversal traversal) {
            super(traversal);
            this.setPredicate(traverser -> testElement(traverser.get()));
        }

        public String toString() {
            return TraversalHelper.makeStepString(this, vertexCriterion, edgeCriterion);
        }
    }
}