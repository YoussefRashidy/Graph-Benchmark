package io.github.youssefrashidy.graph;

import io.github.youssefrashidy.graph.augumentingDS.DisjointSet;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.primitive.MutableIntStack;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;

import java.util.*;

public class Graph<VD, ED> {
    private int vertexCounter = 0;
    private int edgeCounter = 0;

    private final GraphType type;

    IntObjectHashMap<Vertex<VD>> verticesMap = IntObjectHashMap.newMap();
    IntObjectHashMap<MutableList<Edge<ED>>> adjacencyList = IntObjectHashMap.newMap();
    MutableList<Edge<ED>> edgeList = FastList.newList();

    public Graph(GraphType type) {
        this.type = type;
    }

    Vertex<VD> addVertex(VD data) {
        var vertex = new Vertex<>(vertexCounter++, data);
        verticesMap.put(vertex.id, vertex);
        return vertex;
    }

    Vertex<VD> getVertex(int id) {
        return verticesMap.get(id);
    }

    // adds edge between existing nodes
    public void addEdge(Vertex<VD> u, Vertex<VD> v, ED data, int weight) {
        if (type == GraphType.DIRECTED)
            throw new RuntimeException();
        int edgeId = edgeCounter++;
        // undirected edges are decomposed into two edges one for each vertex with
        Edge<ED> edge = new Edge<>(u.id, v.id, edgeId, weight, data);
        Edge<ED> edge2 = new Edge<>(v.id, u.id, edgeId, weight, data);
        adjacencyList.getIfAbsent(u.id, FastList::newList).add(edge);
        adjacencyList.getIfAbsent(v.id, FastList::newList).add(edge2);
        // edge list stores only unique edges
        edgeList.add(edge);
    }

    public void addDirectedEdge(Vertex<VD> u, Vertex<VD> v, ED data, int weight) {
        if (type == GraphType.UNDIRECTED)
            throw new RuntimeException();

        int edgeId = edgeCounter++;
        Edge<ED> edge = new Edge<>(u.id, v.id, edgeId, weight, data);
        adjacencyList.getIfAbsent(u.id, FastList::newList).add(edge);
        edgeList.add(edge);
    }

    private record QueueEntry<ED>(int vertexId, int key, Edge<ED> parentEdge) {

    }

    List<Edge<ED>> primMST() {
        if (type != GraphType.UNDIRECTED)
            throw new UnsupportedOperationException("Prim's MST is not supported for directed graphs.");
        List<Edge<ED>> mst = FastList.newList();

        IntBooleanHashMap inMST = new IntBooleanHashMap();
        IntIntHashMap key = new IntIntHashMap();

        verticesMap.forEachKey(idx -> {
            inMST.put(idx, false);
            key.put(idx, Integer.MAX_VALUE);
        });

        // pick source I will pick the first node for now
        PriorityQueue<QueueEntry<ED>> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(QueueEntry::key));

        int source = verticesMap.keysView().min();
        inMST.put(source, true);
        key.put(source, 0);

        priorityQueue.add(new QueueEntry<>(source, 0, null));

        while (!priorityQueue.isEmpty()) {
            QueueEntry<ED> minVertex = priorityQueue.poll();
            int u = minVertex.vertexId();
            if (inMST.get(u))
                continue;
            inMST.put(u, true);
            // what a beautiful stream (but what about performance dude)
            // don't worry eclipse DS are optimized
            // technically it is iterable but whatever it is a stream
            adjacencyList.get(minVertex.vertexId).forEach(edge -> {
                int destination = edge.v;
                int weight = edge.weight;
                if (inMST.get(destination))
                    return;
                if (key.get(destination) <= weight)
                    return;
                priorityQueue.offer(new QueueEntry<>(destination, weight, edge));
            });

            if (minVertex.parentEdge() != null)
                mst.add(minVertex.parentEdge);
        }
        return mst;
    }

    List<Edge<ED>> kruskalMST() {
        if (type != GraphType.UNDIRECTED)
            throw new UnsupportedOperationException("Kruskal's MST is not supported for directed graphs.");

        List<Edge<ED>> mst = FastList.newList();
        DisjointSet disjointSet = new DisjointSet();
        adjacencyList.keysView().forEach(disjointSet::makeSet);

        // using to sorted list instead of sort this
        // to avoid abusing ths in place property of fast list
        // each subsequent sort would be O(|E|) instead of O(|E|lg|E|)
        var sortedEdges = edgeList.toSortedList(Comparator.comparingInt(Edge<ED>::getWeight));
        sortedEdges.forEach(edge -> {
            int u = edge.u;
            int v = edge.v;
            if (disjointSet.findSet(u) != disjointSet.findSet(v)) {
                mst.add(edge);
                disjointSet.union(u, v);
            }
        });

        return mst;
    }

    IntIntHashMap dijkstra(Vertex<VD> source) {

        IntBooleanHashMap foundShortestPath = new IntBooleanHashMap();
        IntIntHashMap distanceMap = new IntIntHashMap();
        PriorityQueue<QueueEntry<ED>> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(QueueEntry::key));

        verticesMap.keysView().forEach(key -> {
            foundShortestPath.put(key, false);
            distanceMap.put(key, Integer.MAX_VALUE);
        });

        int id = source.id;
        distanceMap.put(id, 0);
        foundShortestPath.put(id, false);
        priorityQueue.offer(new QueueEntry<>(id, 0, null));

        while (!priorityQueue.isEmpty()) {
            QueueEntry<ED> min = priorityQueue.poll();
            int u = min.vertexId();
            if (foundShortestPath.get(u))
                continue;
            foundShortestPath.put(u, true);
            adjacencyList.get(u).forEach(edge -> {
                int v = edge.v;
                int weight = edge.weight;
                if (distanceMap.get(v) <= weight + distanceMap.get(u) || foundShortestPath.get(v))
                    return;
                // relaxation
                int newWeight = weight + distanceMap.get(u);
                distanceMap.put(v, newWeight);
                priorityQueue.offer(new QueueEntry<>(v, newWeight, edge));
            });
        }
        return distanceMap;
    }

    IntIntHashMap dagShortestPath(Vertex<VD> source) {
        if (type != GraphType.DIRECTED)
            throw new RuntimeException();

        IntIntHashMap distances = new IntIntHashMap();
        MutableIntStack stack = topologicalSort();

        adjacencyList.keysView().forEach(vertex -> distances.put(vertex, Integer.MAX_VALUE));
        distances.put(source.id, 0);

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (distances.get(u) == Integer.MAX_VALUE)
                continue;

            adjacencyList.get(u).forEach(edge -> {
                int v = edge.v;
                int weight = edge.weight;
                // oops, overflow now no overflow
                if (distances.get(v) > weight + distances.get(u))
                    distances.put(v, weight + distances.get(u));
            });
        }
        return distances;
    }

    MutableIntStack topologicalSort() {
        MutableIntStack stack = new IntArrayStack();
        IntHashSet visited = new IntHashSet();
        IntHashSet onStack = new IntHashSet();
        adjacencyList.keysView().forEach(vertex -> {
            if (visited.contains(vertex))
                return;
            dfs(vertex, stack, visited, onStack);
        });
        return stack;
    }

    private void dfs(int vertex, MutableIntStack stack, IntHashSet visited, IntHashSet onStack) {
        visited.add(vertex);
        onStack.add(vertex);
        adjacencyList.get(vertex).forEach(edge -> {
            int v = edge.v;
            if (visited.contains(v))
                return;
            if (onStack.contains(v))
                throw new CycleDetectedException();
            dfs(v, stack, visited, onStack);
        });
        onStack.remove(vertex);
        stack.push(vertex);
    }

    private static class CycleDetectedException extends RuntimeException {
        public CycleDetectedException() {
        }

        public CycleDetectedException(String message) {
            super(message);
        }

        public CycleDetectedException(String message, Throwable cause) {
            super(message, cause);
        }

        public CycleDetectedException(Throwable cause) {
            super(cause);
        }

        public CycleDetectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
