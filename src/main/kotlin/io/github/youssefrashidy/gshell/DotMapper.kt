package io.github.youssefrashidy.gshell

import io.github.youssefrashidy.graph.Edge
import io.github.youssefrashidy.graph.Graph
import io.github.youssefrashidy.graph.GraphType

// Kotlin taste
class DotMapper {
    val edgeColor = "gray"
    val mstEdgeColor = "green"
    val mstPenWidth = 4;
    val penWidth = 1
    private val mstNodeFillColor = "#00C853"   // vivid green fill
    private val mstNodeBorderColor = "#007230" // dark green border
    private val mstNodeFontColor = "white"
    private val mstNodePenWidth = 2.5

    fun <VD, ED> toDot(graph: Graph<VD, ED>, identifier: String): String {
        val graphType = if (graph.graphType == GraphType.UNDIRECTED) "graph" else "digraph";
        val edgeSymbol = if (graph.graphType == GraphType.UNDIRECTED) "--" else "->";
        val dotBody = graph.edges.joinToString("\n") { edge -> renderEdge(graph, edge, edgeSymbol) }
        return """
            $graphType $identifier  {
                graph [layout=sfdp K=2.0 nodesep=2.0 overlap=prism splines=true size="20,20!" dpi=150]
                node  [shape=circle width=0.9 fixedsize=true style=filled fillcolor="#1E88E5" color="#0D47A1" fontcolor="white" fontname="Helvetica" fontsize=13]
                $dotBody
            }
        """
    }

    fun <VD, ED> toDotMst(graph: Graph<VD, ED>, identifier: String, mst: List<Edge<ED>>): String {
        val graphType = if (graph.graphType == GraphType.UNDIRECTED) "graph" else "digraph";
        val edgeSymbol = if (graph.graphType == GraphType.UNDIRECTED) "--" else "->";

        val mstSet = mst.toSet()
        val verticesSet = mst.flatMap { edge -> listOf(edge.u,edge.v) }.toSet()
        val verticesStyles = verticesSet.joinToString("\n") { vertex -> renderVertex(graph.getVertex(vertex).data.toString() , true) }

        val dotBody = graph.edges.joinToString("\n") { edge -> renderMstEdge(graph, edge, edgeSymbol, edge in mstSet) }
        return """
            $graphType $identifier {
            graph [layout=sfdp K=2.0 nodesep=2.0 overlap=prism splines=true size="20,20!" dpi=150]
            node  [shape=circle width=0.9 fixedsize=true style=filled fillcolor="#1E88E5" color="#0D47A1" fontcolor="white" fontname="Helvetica" fontsize=13]
            $verticesStyles
            $dotBody
            }
        """
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

    private fun renderVertex(label: String, inMst: Boolean): String {
        return if (inMst)
            "\"$label\" [fillcolor=\"$mstNodeFillColor\" color=\"$mstNodeBorderColor\" fontcolor=\"$mstNodeFontColor\" penwidth=$mstNodePenWidth];"
        else
            "\"$label\";"
    }


}