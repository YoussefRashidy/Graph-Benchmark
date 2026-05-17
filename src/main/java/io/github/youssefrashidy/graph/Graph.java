package io.github.youssefrashidy.graph;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Graph<VD, ED> {
    private int vertexCounter = 0;
    private int edgeCounter = 0;

    private boolean isDirected = false;

    IntObjectHashMap<Vertex<VD>> verticesMap = IntObjectHashMap.newMap();
    IntObjectHashMap<MutableList<Edge<ED>>> adjacencyList = IntObjectHashMap.newMap();
    MutableList<Edge<ED>> edgeList = FastList.newList();

    Vertex<VD> addVertex(VD data) {
        var vertex = new Vertex<VD>(vertexCounter++, data);
        verticesMap.put(vertex.id, vertex);
        return vertex;
    }

    // adds edge between existing nodes
    public void addEdge(Vertex<VD> u, Vertex<VD> v, ED data, int weight) {
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
        int edgeId = edgeCounter++;
        Edge<ED> edge = new Edge<>(u.id, v.id, edgeId, weight, data);
        adjacencyList.getIfAbsent(u.id, FastList::newList).add(edge);
        edgeList.add(edge);
        isDirected = true;
    }

    private static record QueueEntry<ED>(int vertexId, int key, Edge<ED> parentEdge) {

    }

    List<Edge<ED>> primMST() {
        if (isDirected)
            throw new UnsupportedOperationException();
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

        priorityQueue.add(new QueueEntry<ED>(source, 0, null));

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
                priorityQueue.offer(new QueueEntry<ED>(destination, weight, edge));
            });

            if (minVertex.parentEdge() != null)
                mst.add(minVertex.parentEdge);
        }
        return mst;
    }

    List<Edge<ED>> kruskalMST() {
        List<Edge<ED>> mst = FastList.newList();



        return mst ;
    }
}
