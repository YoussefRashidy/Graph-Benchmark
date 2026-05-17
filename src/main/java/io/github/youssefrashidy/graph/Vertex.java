package io.github.youssefrashidy.graph;

public class Vertex <D>{
    protected final int id ;
    protected D data ;

    public Vertex(int id ,D data) {
        this.id = id ;
        this.data = data;
    }

    public Vertex(int id) {
        this.id = id;
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
}
