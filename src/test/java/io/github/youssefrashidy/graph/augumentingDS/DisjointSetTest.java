package io.github.youssefrashidy.graph.augumentingDS;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetTest {

    @Test
    void newElementIsItsOwnParent() {
        DisjointSet disjointSet = new DisjointSet();
        disjointSet.makeSet(1);
        assertEquals(1, disjointSet.parent.get(1));
        assertEquals(0, disjointSet.rank.get(1));
    }

    @Test
    void unionTwoSetsSameFindSet() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(0);
        ds.makeSet(1);
        ds.union(0, 1);

        assertEquals(ds.findSet(0), ds.findSet(1));
    }

    @Test
    void makeSetCalledTwiceDoesNotOverwrite() {
        DisjointSet disjointSet = new DisjointSet();
        disjointSet.makeSet(1);
        disjointSet.makeSet(2);
        disjointSet.union(1, 2);
        assertEquals(1, disjointSet.rank.get(1));
        disjointSet.makeSet(1);
        assertEquals(1, disjointSet.rank.get(1));
    }

    @Test
    void unionHigherRankBecomesRoot() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(0);
        ds.makeSet(1);
        ds.makeSet(2);

        ds.union(0, 1);
        ds.union(0, 2);

        assertEquals(ds.findSet(0), ds.findSet(2));
        assertEquals(0, ds.findSet(2));
    }

    @Test
    void unionSameSetIsNoOp() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(0);
        ds.makeSet(1);
        ds.union(0, 1);

        int rootBefore = ds.findSet(0);
        int rankBefore = ds.rank.get(rootBefore);

        ds.union(0, 1); // no-op

        assertEquals(rootBefore, ds.findSet(0));
        assertEquals(rankBefore, ds.rank.get(rootBefore));
    }

    @Test
    void unionTransitivityAllInSameSet() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(0);
        ds.makeSet(1);
        ds.makeSet(2);

        ds.union(0, 1);
        ds.union(1, 2);

        assertEquals(ds.findSet(0), ds.findSet(1));
        assertEquals(ds.findSet(1), ds.findSet(2));
    }

    @Test
    void findSetPathCompressionIntermediatePointsToRoot() {
        DisjointSet ds = new DisjointSet();
        ds.makeSet(0);
        ds.makeSet(1);
        ds.makeSet(2);
        ds.makeSet(3);

        // manually build a chain: 3 -> 2 -> 1 -> 0
        ds.parent.put(3, 2);
        ds.parent.put(2, 1);
        ds.parent.put(1, 0);

        // trigger path compression
        int root = ds.findSet(3);

        assertEquals(0, root);
        // after compression all should point directly to root
        assertEquals(0, ds.parent.get(3));
        assertEquals(0, ds.parent.get(2));
        assertEquals(0, ds.parent.get(1));
    }

}