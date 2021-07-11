package uk.ac.ed.inf.aqmaps.tourfinder;

import uk.ac.ed.inf.aqmaps.mapdetails.Node;

/**
 * Represents an undirected edge between Nodes
 */
public class Edge {

    private Node node1;
    private Node node2;
    private double weight;

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = node1.getCoordinate().euclideanDistance(node2.getCoordinate());
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Checks if both of the endpoints are the same, in either order
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Edge) {
            var other = (Edge) obj;
            if (this.node1 == other.node1 && this.node2 == other.node2) {
                return true;
            }
            if (this.node1 == other.node2 && this.node2 == other.node1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return node1.hashCode() + node2.hashCode();
    }

}
