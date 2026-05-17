package io.github.youssefrashidy.graph.augumentingDS;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

public class DisjointSet {
    IntIntHashMap parent = new IntIntHashMap();
    IntIntHashMap rank = new IntIntHashMap();

    public void makeSet(int vertex) {
        // Should overwrite ?
        // Should ignore ?
        // throw exception ?
        if (parent.containsKey(vertex))
            return;
        parent.put(vertex, vertex);
        rank.put(vertex, 0);
    }

    public int findSet(int vertex) {
        int parent = this.parent.get(vertex);

        if (parent != vertex) {
            this.parent.put(vertex, findSet(parent));
        }

        return this.parent.get(vertex);
    }

    public void union(int u, int v) {
        int uParent = findSet(u);
        int vParent = findSet(v);
        if (uParent == vParent)
            return;
        int uRank = rank.get(uParent);
        int vRank = rank.get(vParent);

        if (uRank > vRank)
            parent.put(vParent, uParent);
        else if (vRank > uRank)
            parent.put(uParent, vParent);
        else {
            parent.put(vParent, uParent);
            rank.put(uParent, uRank + 1);
        }
    }

}
