package io.github.youssefrashidy.graph;

import io.github.youssefrashidy.graph.augumentingDS.DisjointSet;
import io.github.youssefrashidy.graph.exceptions.CycleDetectionException;
import io.github.youssefrashidy.graph.exceptions.EdgeMismatchException;
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

    public Vertex<VD> addVertex(VD data) {
        var vertex = new Vertex<>(vertexCounter++, data);
        verticesMap.put(vertex.id, vertex);
        return vertex;
    }

    public Vertex<VD> getVertex(int id) {
        return verticesMap.get(id);
    }

    // adds edge between existing nodes
    public void addEdge(Vertex<VD> u, Vertex<VD> v, ED data, int weight) {
        if (type == GraphType.DIRECTED)
            throw new EdgeMismatchException("Cannot add undirected edge to a directed graph.");
        int edgeId = edgeCounter++;
        // undirected edges are decomposed into two edges one for each vertex with the same id
        Edge<ED> edge = new Edge<>(u.id, v.id, edgeId, weight, data);
        Edge<ED> edge2 = new Edge<>(v.id, u.id, edgeId, weight, data);
        adjacencyList.getIfAbsentPut(u.id, FastList::newList).add(edge);
        adjacencyList.getIfAbsentPut(v.id, FastList::newList).add(edge2);
        // edge list stores only unique edges
        edgeList.add(edge);
    }

    public void addDirectedEdge(Vertex<VD> u, Vertex<VD> v, ED data, int weight) {
        if (type == GraphType.UNDIRECTED)
            throw new EdgeMismatchException("Cannot add directed edge to an undirected graph.");

        int edgeId = edgeCounter++;
        Edge<ED> edge = new Edge<>(u.id, v.id, edgeId, weight, data);
        adjacencyList.getIfAbsentPut(u.id, FastList::newList).add(edge);
        edgeList.add(edge);
    }

    public long getEdgeCount() {
        return edgeList.size();
    }

    public List<Edge<ED>> getEdges() {
        return edgeList.clone();
    }

    public GraphType getGraphType() {
        return this.type;
    }


    private record QueueEntry<ED>(int vertexId, int key, Edge<ED> parentEdge) {

    }

    public List<Edge<ED>> primMST() {
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
        key.put(source, 0);

        priorityQueue.add(new QueueEntry<>(source, 0, null));

        while (!priorityQueue.isEmpty()) {
            QueueEntry<ED> minVertex = priorityQueue.poll();
            int u = minVertex.vertexId();
            if (inMST.get(u))
                continue;
            inMST.put(u, true);
            // what a beautiful stream (but what about performance dude)
            // don't worry eclipse DS are optimized it operates on internal primitive Array
            // technically it is iterable but whatever it is a stream
            adjacencyList.getIfAbsentPut(minVertex.vertexId, FastList.newList()).forEach(edge -> {
                int destination = edge.v;
                int weight = edge.weight;
                if (inMST.get(destination))
                    return;
                if (key.get(destination) <= weight)
                    return;

                key.put(destination, weight);
                priorityQueue.offer(new QueueEntry<>(destination, weight, edge));
            });

            if (minVertex.parentEdge() != null)
                mst.add(minVertex.parentEdge);
        }
        return mst;
    }

    public List<Edge<ED>> kruskalMST() {
        if (type != GraphType.UNDIRECTED)
            throw new UnsupportedOperationException("Kruskal's MST is not supported for directed graphs.");

        List<Edge<ED>> mst = FastList.newList();
        DisjointSet disjointSet = new DisjointSet();
        verticesMap.keysView().forEach(disjointSet::makeSet);

        // using to sorted list instead of sort this
        // to avoid abusing the in place property of fast list
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

    public IntIntHashMap dijkstra(Vertex<VD> source) {

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
            adjacencyList.getIfAbsentPut(u, FastList.newList()).forEach(edge -> {
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

    public IntIntHashMap dagShortestPath(Vertex<VD> source) {
        if (type != GraphType.DIRECTED)
            throw new EdgeMismatchException("DAG shortest path requires a directed graph.");

        IntIntHashMap distances = new IntIntHashMap();
        MutableIntStack stack = topologicalSort();

        verticesMap.keysView().forEach(vertex -> distances.put(vertex, Integer.MAX_VALUE));
        distances.put(source.id, 0);

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (distances.get(u) == Integer.MAX_VALUE)
                continue;

            adjacencyList.getIfAbsentPut(u, FastList::newList).forEach(edge -> {
                int v = edge.v;
                int weight = edge.weight;
                // oops, overflow now no overflow
                if (distances.get(v) > weight + distances.get(u))
                    distances.put(v, weight + distances.get(u));
            });
        }
        return distances;
    }

    public MutableIntStack topologicalSort() {
        MutableIntStack stack = new IntArrayStack();
        IntHashSet visited = new IntHashSet();
        IntHashSet onStack = new IntHashSet(); // plays the role of gray color
        verticesMap.keysView().forEach(vertex -> {
            if (visited.contains(vertex))
                return;
            dfsIterative(vertex, stack, visited, onStack);
        });
        return stack;
    }

    private void dfs(int vertex, MutableIntStack stack, IntHashSet visited, IntHashSet onStack) {
        visited.add(vertex);
        onStack.add(vertex);

        adjacencyList.getIfAbsentPut(vertex, FastList::newList).forEach(edge -> {
            int v = edge.v;
            if (onStack.contains(v))
                throw new CycleDetectionException("Cycle detected in the graph during topological sort.");
            if (visited.contains(v))
                return;
            dfs(v, stack, visited, onStack);
        });

        onStack.remove(vertex);
        stack.push(vertex);
    }

    private record DFSFrame(int vertex, boolean processed) {
    }

    private void dfsIterative(int start, MutableIntStack topoStack, IntHashSet visited, IntHashSet onStack) {

        Deque<DFSFrame> dfsStack = new ArrayDeque<>();

        dfsStack.push(new DFSFrame(start, false));

        while (!dfsStack.isEmpty()) {

            DFSFrame frame = dfsStack.pop();
            int u = frame.vertex;

            // second time we see the node
            if (frame.processed) {
                onStack.remove(u);
                topoStack.push(u);
                continue;
            }

            if (visited.contains(u))
                continue;

            visited.add(u);
            onStack.add(u);

            dfsStack.push(new DFSFrame(u, true));

            MutableList<Edge<ED>> edges = adjacencyList.getIfAbsentPut(u, FastList::newList);

            for (int i = edges.size() - 1; i >= 0; i--) {

                Edge<ED> edge = edges.get(i);
                int v = edge.v;

                if (onStack.contains(v))
                    throw new CycleDetectionException(
                            "Cycle detected in the graph during topological sort."
                    );

                if (!visited.contains(v)) {
                    dfsStack.push(new DFSFrame(v, false));
                }
            }
        }
    }

}
