package io.github.youssefrashidy.gshelll

import io.github.youssefrashidy.graph.Edge
import io.github.youssefrashidy.graph.Graph
import io.github.youssefrashidy.graph.GraphType

// Kotlin taste
class DotMapper {
    val edgeColor = "gray"
    val mstEdgeColor = "red";
    val mstPenWidth = 3;
    val penWidth = 1
    fun <VD, ED> toDot(graph: Graph<VD, ED>, identifier: String): String {
        val graphType = if (graph.graphType == GraphType.UNDIRECTED) "graph" else "digraph";
        val edgeSymbol = if (graph.graphType == GraphType.UNDIRECTED) "--" else "->";
        val dotBody = graph.edges.joinToString("\n") { edge -> renderEdge(graph, edge, edgeSymbol) }
        return """
            $graphType $identifier {
                $dotBody
            }
        """.trimIndent()
    }

    fun <VD, ED> toDotMst(graph: Graph<VD, ED>, identifier: String, mst: List<Edge<ED>>): String {
        val graphType = if (graph.graphType == GraphType.UNDIRECTED) "graph" else "digraph";
        val edgeSymbol = if (graph.graphType == GraphType.UNDIRECTED) "--" else "->";

        val mstSet = mst.toSet()

        val dotBody = graph.edges.joinToString("\n") { edge -> renderMstEdge(graph, edge, edgeSymbol, edge in mstSet) }
        return """
            $graphType $identifier {
                $dotBody
            }
        """.trimIndent()
    }


    // TODO Distance with edge instead of only hashMap
//    fun <VD, ED> toDotShortestPath(graph: Graph<VD, ED>, identifier: String, distances: IntIntHashMap): String {
//        val graphType = if (graph.graphType == GraphType.UNDIRECTED) "graph" else "digraph";
//        val edgeSymbol = if (graph.graphType == GraphType.UNDIRECTED) "--" else "->";
//    }


    private fun <VD, ED> renderEdge(graph: Graph<VD, ED>, edge: Edge<ED>, symbol: String): String {
        val u = graph.getVertex(edge.u).data.toString()
        val v = graph.getVertex(edge.v).data.toString()

        return "\"$u\" $symbol \"$v\" [label=\"${edge.weight}\"];"
    }

    private fun <VD, ED> renderMstEdge(graph: Graph<VD, ED>, edge: Edge<ED>, symbol: String, inMst: Boolean): String {
        val u = graph.getVertex(edge.u).data.toString()
        val v = graph.getVertex(edge.v).data.toString()
        return if (inMst)
            "\"$u\" $symbol \"$v\" [label=\"${edge.weight}\", color=\"$mstEdgeColor\", penwidth=$mstPenWidth];"
        else
            "\"$u\" $symbol \"$v\" [label=\"${edge.weight}\", color=\"$edgeColor\", penwidth=$penWidth];"
    }


}