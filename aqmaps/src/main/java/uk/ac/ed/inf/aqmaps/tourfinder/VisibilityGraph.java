package uk.ac.ed.inf.aqmaps.tourfinder;

import java.util.ArrayList;

import java.util.List;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;
import uk.ac.ed.inf.aqmaps.mapdetails.NoFlyZone;
import uk.ac.ed.inf.aqmaps.mapdetails.NoFlyZoneVertex;
import uk.ac.ed.inf.aqmaps.mapdetails.Node;

/**
 * Creates visibility graphs for producing optimal routes around NoFlyZones.
 */

public class VisibilityGraph {

    private Node start;
    private Node end;
    private List<NoFlyZone> NFZs;

    // nodes and edges in the graph
    private List<Node> nodes;
    private List<Edge> edges;

    // nodes and edges of the NFZs
    private List<Node> NfzNodes;
    private List<Edge> NfzEdges;


    public VisibilityGraph(Node start, Node end, List<NoFlyZone> NFZs) {

        this.start = start;
        this.end = end;
        this.NFZs = NFZs;
        this.NfzEdges = new ArrayList<Edge>();
        this.NfzNodes = new ArrayList<Node>();
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
        generateGraph();

    }

    /**
     * Creates the Visibility Graph by filling the nodes and edges arrays
     */
    private void generateGraph() {

        // Generate NfzEdges and NfzNodes
        generateNfzNodesAndEdges();

        // Add start and end nodes
        nodes.add(start);
        nodes.add(end);

        // add all NFZ edges and nodes
        edges.addAll(NfzEdges);
        nodes.addAll(NfzNodes);

        var n = nodes.size();

        // Loop over all pairs of nodes and create new edges
        for (var i = 0; i < n; i++) {
            for (var j = i + 1; j < n; j++) {

                var n1 = nodes.get(i);
                var n2 = nodes.get(j);

                var edge = new Edge(n1, n2);
                if (isVisible(n1, n2) && !edges.contains(edge)) {
                    edges.add(new Edge(n1, n2));
                }

            }
        }

    }

    /**
     * Adds the edges of the all the NFZs to NfzEdges and all their vertices to
     * NfzNodes
     */
    private void generateNfzNodesAndEdges() {
        for (var NFZ : NFZs) {
            generateNfzNodesAndEdges(NFZ);
        }
    }

    /**
     * Adds the vertices of NFZ to NfzNodes and adds the edges to NfzEdges.
     * 
     * @param NFZ a NoFlyZone
     */
    private void generateNfzNodesAndEdges(NoFlyZone NFZ) {

        var vertices = NFZ.getVertices();

        // Add vertices to NfzNodes
        NfzNodes.addAll(vertices);

        // Add edges between adjacent vertices to NfzEdges
        for (var i = 0; i < vertices.size() - 1; i++) {
            var n1 = vertices.get(i);
            var n2 = vertices.get(i + 1);
            NfzEdges.add(new Edge(n1, n2));
        }

        var n1 = vertices.get(vertices.size() - 1);
        var n2 = vertices.get(0);
        NfzEdges.add(new Edge(n1, n2));

    }

    /**
     * @param node1
     * @param node2
     * @return true if node1 is visible from node2
     */
    private boolean isVisible(Node node1, Node node2) {

        // Iterate over NfzEdges and check for intersection, ignoring edges that the
        // nodes belong to
        for (var edge : NfzEdges) {
            if (node1 == edge.getNode1() || node1 == edge.getNode2()) {
                continue;
            }
            if (node2 == edge.getNode1() || node2 == edge.getNode2()) {
                continue;
            }
            if (Coordinate.linesIntersect(node1.getCoordinate(), node2.getCoordinate(), edge.getNode1().getCoordinate(),
                    edge.getNode2().getCoordinate())) {
                return false;
            }

        }

        // If both nodes belong to the same NFZ, we check if the centre of the line
        // segment between them is in the NFZ to avoid adding interior edges
        if (node1 instanceof NoFlyZoneVertex && node2 instanceof NoFlyZoneVertex
                && ((NoFlyZoneVertex) node1).getNoFlyZone() == ((NoFlyZoneVertex) node2).getNoFlyZone()) {

            var lng = node1.getCoordinate().getLongitude() + node2.getCoordinate().getLongitude();
            var lat = node1.getCoordinate().getLatitude() + node2.getCoordinate().getLatitude();
            var p = new Coordinate(lat / 2, lng / 2);
            if (((NoFlyZoneVertex) node1).getNoFlyZone().contains(p)) {
                return false;
            }
        }

        return true;

    }

    /**
     * Perform Dijkstra's algorithm on the graph
     * 
     * @return a Path
     */
    public Path bestPath() {

        // Create a weighted graph
        DefaultUndirectedWeightedGraph<Node, DefaultWeightedEdge> graph = 
                new DefaultUndirectedWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // Add the nodes to the graph
        for (var node : nodes) {
            graph.addVertex(node);
        }

        // Add edges to the graph
        for (var e : edges) {
            var edge = graph.addEdge(e.getNode1(), e.getNode2());
            graph.setEdgeWeight(edge, e.getWeight());
        }

        // Execute Dijkstra
        var dijkstra = new DijkstraShortestPath<Node, DefaultWeightedEdge>(graph);

        // Get nodes and cost
        var nodes = dijkstra.getPath(start, end).getVertexList();
        var cost = dijkstra.getPathWeight(start, end);

        return new Path(nodes, cost);

    }

}
