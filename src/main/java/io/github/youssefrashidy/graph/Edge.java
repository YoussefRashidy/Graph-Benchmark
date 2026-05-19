package io.github.youssefrashidy.graph;

import java.util.Objects;

public class Edge<D> {
    protected final int u, v, id;
    protected int weight;
    protected D data;

    public Edge(int u, int v, int id, int weight, D data) {
        this.u = u;
        this.v = v;
        this.id = id;
        this.weight = weight;
        this.data = data;
    }

    public Edge(int u, int v, int id, int weight) {
        this.u = u;
        this.v = v;
        this.id = id;
        this.weight = weight;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge<?> edge)) return false;
        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
