package uk.ac.ed.inf.aqmaps.tourfinder;

import java.util.List;

import uk.ac.ed.inf.aqmaps.mapdetails.Node;

/**
 * Represents a path of nodes along with the cost of traversing them
 */
public class Path {

    private List<Node> nodes;
    private double cost;

    public Path(List<Node> nodes, double cost) {
        this.nodes = nodes;
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public List<Node> getNodes() {
        return nodes;
    }

}