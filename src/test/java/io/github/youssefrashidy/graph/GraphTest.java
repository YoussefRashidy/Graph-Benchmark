package io.github.youssefrashidy.graph;

import io.github.youssefrashidy.graph.exceptions.CycleDetectionException;
import io.github.youssefrashidy.graph.exceptions.EdgeMismatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Nested
    class EdgeAdditionTests {
        @Test
        void addUndirectedEdge() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            g.addEdge(u, v, null, 10);

            assertEquals(1, g.adjacencyList.get(u.getId()).size());
            assertEquals(1, g.adjacencyList.get(v.getId()).size());

            assertEquals(10, g.adjacencyList.get(u.getId()).getFirst().getWeight());
            assertEquals(10, g.adjacencyList.get(v.getId()).getFirst().getWeight());
        }

        @Test
        void addEdge_undirected_edgeListStoresOnlyOneEdge() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            g.addEdge(u, v, null, 5);

            assertEquals(1, g.edgeList.size());
        }

        @Test
        void addDirectedEdge() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            g.addDirectedEdge(u, v, null, 10);

            assertEquals(1, g.adjacencyList.get(u.getId()).size());
            assertNull(g.adjacencyList.get(v.getId()));

            assertEquals(10, g.adjacencyList.get(u.getId()).getFirst().getWeight());
        }
    }

    @Nested
    class EdgeMismatchTests {
        @Test
        void addDirectedEdgeToUndirectedGraph() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            assertThrows(EdgeMismatchException.class, () -> g.addDirectedEdge(u, v, null, 10));
        }

        @Test
        void addUndirectedEdgeToDirectedGraph() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            assertThrows(EdgeMismatchException.class, () -> g.addEdge(u, v, null, 10));
        }
    }

    @Nested
    class PrimMSTTests {
        @Test
        void primMSTCorrectWeightAndSize() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);
            g.addEdge(v0, v2, null, 5);

            var mst = g.primMST();

            assertEquals(3, mst.size());
            int totalWeight = mst.stream().mapToInt(Edge::getWeight).sum();
            assertEquals(6, totalWeight);
        }

        @Test
        void primMSTSpanAllVertices() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);
            g.addEdge(v0, v2, null, 5);

            var mst = g.primMST();

            HashSet<Integer> spannedVertices = new HashSet<>();
            mst.forEach(edge -> {
                spannedVertices.add(edge.u);
                spannedVertices.add(edge.v);
            });

            assertEquals(Set.of(0, 1, 2, 3), spannedVertices);
        }

        @Test
        void primMSTSingleVertexReturnEmptyList() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            g.addVertex(null);

            var mst = g.primMST();

            assertTrue(mst.isEmpty());
        }

        @Test
        void primMSTOnDirectedGraphThrowsUnsupportedOperationException() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            g.addDirectedEdge(u, v, null, 5);

            assertThrows(UnsupportedOperationException.class, g::primMST);
        }

        @Test
        void primMST_largeRandomGraph_correctSizeAndBoundedWeight() {
            int V = 1000;
            Random rng = new Random(42);
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);

            List<Vertex<Void>> vertices = new ArrayList<>();
            for (int i = 0; i < V; i++)
                vertices.add(g.addVertex(null));

            // build a random spanning tree first — guarantees connectivity
            List<Integer> shuffled = new ArrayList<>();
            for (int i = 0; i < V; i++) shuffled.add(i);
            Collections.shuffle(shuffled, rng);

            int treeWeight = 0;
            for (int i = 1; i < V; i++) {
                int w = rng.nextInt(101);
                g.addEdge(vertices.get(shuffled.get(i - 1)), vertices.get(shuffled.get(i)), null, w);
                treeWeight += w;
            }

            for (int i = 0; i < 2000; i++) {
                int a = rng.nextInt(V);
                int b = rng.nextInt(V);
                if (a != b)
                    g.addEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(900) + 101);
            }

            var mst = g.primMST();

            assertEquals(V - 1, mst.size());

            Set<Integer> spanned = new HashSet<>();
            mst.forEach(e -> {
                spanned.add(e.getU());
                spanned.add(e.getV());
            });
            assertEquals(V, spanned.size());

            int mstWeight = mst.stream().mapToInt(Edge::getWeight).sum();
            assertTrue(mstWeight <= treeWeight,
                    "MST weight " + mstWeight + " exceeded known spanning tree weight " + treeWeight);
        }
    }

    @Nested
    class KruskalTest {
        @Test
        void kruskalMSTCorrectWeightAndSize() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);
            g.addEdge(v0, v2, null, 5);

            var mst = g.kruskalMST();

            assertEquals(3, mst.size());
            int totalWeight = mst.stream().mapToInt(Edge::getWeight).sum();
            assertEquals(6, totalWeight);
        }

        @Test
        void kruskalMSTSpanAllVertices() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);
            g.addEdge(v0, v2, null, 5);

            var mst = g.kruskalMST();

            HashSet<Integer> spannedVertices = new HashSet<>();
            mst.forEach(edge -> {
                spannedVertices.add(edge.u);
                spannedVertices.add(edge.v);
            });

            assertEquals(Set.of(0, 1, 2, 3), spannedVertices);
        }

        @Test
        void kruskalMSTSingleVertexReturnEmptyList() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            g.addVertex(null);

            var mst = g.kruskalMST();

            assertTrue(mst.isEmpty());
        }

        @Test
        void kruskalMSTOnDirectedGraphThrowsUnsupportedOperationException() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var u = g.addVertex(null);
            var v = g.addVertex(null);
            g.addDirectedEdge(u, v, null, 5);

            assertThrows(UnsupportedOperationException.class, g::kruskalMST);
        }

        @Test
        void kruskalMSTLargeRandomGraphCorrectSizeAndBoundedWeight() {
            int V = 1000;
            Random rng = new Random(42);
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);

            List<Vertex<Void>> vertices = new ArrayList<>();
            for (int i = 0; i < V; i++)
                vertices.add(g.addVertex(null));

            // build a random minimum spanning tree first — guarantees connectivity
            List<Integer> shuffled = new ArrayList<>();
            for (int i = 0; i < V; i++) shuffled.add(i);
            Collections.shuffle(shuffled, rng);

            int treeWeight = 0;
            for (int i = 1; i < V; i++) {
                int w = rng.nextInt(101);
                g.addEdge(vertices.get(shuffled.get(i - 1)), vertices.get(shuffled.get(i)), null, w);
                treeWeight += w;
            }

            for (int i = 0; i < 2000; i++) {
                int a = rng.nextInt(V);
                int b = rng.nextInt(V);
                if (a != b)
                    g.addEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(900) + 101);
            }

            var mst = g.kruskalMST();

            assertEquals(V - 1, mst.size());

            Set<Integer> spanned = new HashSet<>();
            mst.forEach(e -> {
                spanned.add(e.getU());
                spanned.add(e.getV());
            });
            assertEquals(V, spanned.size());

            int mstWeight = mst.stream().mapToInt(Edge::getWeight).sum();
            assertTrue(mstWeight <= treeWeight,
                    "MST weight " + mstWeight + " exceeded known spanning tree weight " + treeWeight);
        }
    }

    @Nested
    class KruskalPrimAgreement {
        @Test
        void kruskalMST_largeRandomGraph_sameWeightAsPrim() {
            int V = 1000;
            Random rng = new Random(42);
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);

            List<Vertex<Void>> vertices = new ArrayList<>();
            for (int i = 0; i < V; i++)
                vertices.add(g.addVertex(null));

            List<Integer> shuffled = new ArrayList<>();
            for (int i = 0; i < V; i++) shuffled.add(i);
            Collections.shuffle(shuffled, rng);

            for (int i = 1; i < V; i++) {
                int w = rng.nextInt(100) + 1;
                g.addEdge(vertices.get(shuffled.get(i - 1)), vertices.get(shuffled.get(i)), null, w);
            }

            for (int i = 0; i < 2000; i++) {
                int a = rng.nextInt(V);
                int b = rng.nextInt(V);
                if (a != b)
                    g.addEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(900) + 101);
            }

            int primWeight = g.primMST().stream().mapToInt(Edge::getWeight).sum();
            int kruskalWeight = g.kruskalMST().stream().mapToInt(Edge::getWeight).sum();

            assertEquals(primWeight, kruskalWeight);
        }
    }

    @Nested
    class DijkstraTest {
        @Test
        void dijkstraCorrectDistances() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);

            var distances = g.dijkstra(v0);

            assertEquals(0, distances.get(0));
            assertEquals(1, distances.get(1));
            assertEquals(3, distances.get(2));
            assertEquals(4, distances.get(3));

        }

        @Test
        void dijkstraUnreachableVertexReturnsMaxValue() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null); // isolated

            g.addEdge(v0, v1, null, 5);

            var distances = g.dijkstra(v0);

            assertEquals(Integer.MAX_VALUE, distances.get(v2.getId()));
        }

        @Test
        void dijkstraTriangleInequalityHoldsForAllEdges() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addEdge(v0, v1, null, 1);
            g.addEdge(v1, v2, null, 2);
            g.addEdge(v2, v3, null, 3);
            g.addEdge(v0, v3, null, 4);

            var distances = g.dijkstra(v0);

            g.edgeList.forEach(edge -> {
                int u = edge.getU();
                int v = edge.getV();
                int w = edge.getWeight();
                assertTrue(distances.get(v) <= distances.get(u) + w,
                        "Triangle inequality violated: dist[" + v + "] > dist[" + u + "] + " + w);
                assertTrue(distances.get(u) <= distances.get(v) + w,
                        "Triangle inequality violated: dist[" + u + "] > dist[" + v + "] + " + w);
            });
        }

        @Test
        void dijkstraSingleVertexReturnsZeroForSource() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);

            var distances = g.dijkstra(v0);

            assertEquals(0, distances.get(v0.getId()));
            assertEquals(1, distances.size());
        }

        @Test
        void dijkstraLargeRandomGraphValidDistances() {
            int V = 1000;
            Random rng = new Random(42);
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);

            List<Vertex<Void>> vertices = new ArrayList<>();
            for (int i = 0; i < V; i++)
                vertices.add(g.addVertex(null));

            // build random spanning tree
            List<Integer> shuffled = new ArrayList<>();
            for (int i = 0; i < V; i++) shuffled.add(i);
            Collections.shuffle(shuffled, rng);

            for (int i = 1; i < V; i++)
                g.addEdge(vertices.get(shuffled.get(i - 1)), vertices.get(shuffled.get(i)), null, rng.nextInt(1001));

            for (int i = 0; i < 2000; i++) {
                int a = rng.nextInt(V);
                int b = rng.nextInt(V);
                if (a != b)
                    g.addEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(1001));
            }

            var distances = g.dijkstra(vertices.getFirst());

            assertEquals(0, distances.get(0));

            g.edgeList.forEach(edge -> {
                int u = edge.getU();
                int v = edge.getV();
                int w = edge.getWeight();
                assertTrue(distances.get(v) <= distances.get(u) + w,
                        "Triangle inequality violated on edge (" + u + "," + v + ")");
                assertTrue(distances.get(u) <= distances.get(v) + w,
                        "Triangle inequality violated on edge (" + v + "," + u + ")");
            });
        }
    }

    @Nested
    class DAGTest {
        @Test
        void dagShortestPathKnownDistances() {

            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);

            g.addDirectedEdge(v0, v1, null, 1);
            g.addDirectedEdge(v1, v2, null, 2);
            g.addDirectedEdge(v0, v2, null, 5);

            var distances = g.dagShortestPath(v0);

            assertEquals(0, distances.get(0));
            assertEquals(1, distances.get(1));
            assertEquals(3, distances.get(2));
        }

        @Test
        void dagShortestPathAgreesWithDijkstra() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);
            var v3 = g.addVertex(null);

            g.addDirectedEdge(v0, v1, null, 1);
            g.addDirectedEdge(v0, v2, null, 5);
            g.addDirectedEdge(v1, v2, null, 2);
            g.addDirectedEdge(v1, v3, null, 8);
            g.addDirectedEdge(v2, v3, null, 3);

            var dagDistances = g.dagShortestPath(v0);
            var dijkstraDistances = g.dijkstra(v0);

            g.verticesMap.keysView().forEach(id ->
                    assertEquals(dijkstraDistances.get(id), dagDistances.get(id),
                            "Mismatch at vertex " + id)
            );
        }

        @Test
        void dagShortestPathUnreachableVertexReturnsMaxValue() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null); // unreachable from v0

            g.addDirectedEdge(v0, v1, null, 5);

            var distances = g.dagShortestPath(v0);

            assertEquals(Integer.MAX_VALUE, distances.get(v2.getId()));
        }

        @Test
        void dagShortestPath_cycleDetected_throwsCycleDetectionException() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            var v2 = g.addVertex(null);

            g.addDirectedEdge(v0, v1, null, 1);
            g.addDirectedEdge(v1, v2, null, 2);
            g.addDirectedEdge(v2, v0, null, 3); // cycle

            assertThrows(CycleDetectionException.class, () -> g.dagShortestPath(v0));
        }

        @Test
        void dagShortestPath_undirectedGraph_throwsRuntimeException() {
            Graph<Void, Void> g = new Graph<>(GraphType.UNDIRECTED);
            var v0 = g.addVertex(null);
            var v1 = g.addVertex(null);
            g.addEdge(v0, v1, null, 5);

            assertThrows(RuntimeException.class, () -> g.dagShortestPath(v0));
        }

        @Test
        void dagShortestPath_singleVertex_returnsZeroForSource() {
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);
            var v0 = g.addVertex(null);

            var distances = g.dagShortestPath(v0);

            assertEquals(0, distances.get(v0.getId()));
            assertEquals(1, distances.size());
        }

        @Test
        void dagShortestPath_largeRandomDAG_agreesWithDijkstra() {
            int V = 1000;
            Random rng = new Random(42);
            Graph<Void, Void> g = new Graph<>(GraphType.DIRECTED);

            List<Vertex<Void>> vertices = new ArrayList<>();
            for (int i = 0; i < V; i++)
                vertices.add(g.addVertex(null));

            // edges only go from lower index to higher index — guaranteed DAG, no cycles
            for (int i = 0; i < V - 1; i++)
                g.addDirectedEdge(vertices.get(i), vertices.get(i + 1), null, rng.nextInt(1001));

            for (int i = 0; i < 3000; i++) {
                int a = rng.nextInt(V);
                int b = rng.nextInt(V);
                if (a < b)
                    g.addDirectedEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(1001));
                else {
                    int temp = a;
                    a = b;
                    b = temp;
                    g.addDirectedEdge(vertices.get(a), vertices.get(b), null, rng.nextInt(1001));
                }
            }

            var dagDistances = g.dagShortestPath(vertices.getFirst());
            var dijkstraDistances = g.dijkstra(vertices.getFirst());

            g.verticesMap.keysView().forEach(id ->
                    assertEquals(dijkstraDistances.get(id), dagDistances.get(id),
                            "Mismatch at vertex " + id)
            );
        }
    }
}