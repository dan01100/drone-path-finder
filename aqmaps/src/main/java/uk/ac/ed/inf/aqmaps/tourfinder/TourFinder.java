package uk.ac.ed.inf.aqmaps.tourfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.ed.inf.aqmaps.Constants;
import uk.ac.ed.inf.aqmaps.mapdetails.NoFlyZone;
import uk.ac.ed.inf.aqmaps.mapdetails.Node;
import uk.ac.ed.inf.aqmaps.mapdetails.Sensor;
import uk.ac.ed.inf.aqmaps.mapdetails.StartingPosition;

/**
 * Generates a tour (list of Nodes) using the 2-opt heuristic going through all
 * of the Sensors (starting and ending at StartingPosition), avoiding the
 * NoFlyZones.
 */
public class TourFinder {

    // nodes contains the StartingPosition and the Sensors
    private List<Node> nodes;
    // Shortest Paths between all pairs of nodes
    private Path[][] paths;
    private List<NoFlyZone> NFZs;
    private int seed;

    public TourFinder(StartingPosition startingPosition, List<Sensor> sensors, List<NoFlyZone> NFZs, int seed) {
        this.seed = seed;
        this.NFZs = NFZs;

        this.nodes = new ArrayList<Node>();
        this.nodes.add(startingPosition);
        this.nodes.addAll(sensors);

        this.paths = shortestPaths();
    }

    /**
     * @return the shortest Paths between all pairs of nodes
     */
    private Path[][] shortestPaths() {

        var n = nodes.size();

        var result = new Path[nodes.size()][nodes.size()];

        // Loop over all pairs of nodes
        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {

                var n1 = nodes.get(i);
                var n2 = nodes.get(j);

                // Path from node to itself has cost 0 and contains only itself
                if (i == j) {
                    result[i][j] = new Path(new ArrayList<Node>(Arrays.asList(n1)), 0.0);
                }

                // If the line joining the two nodes doesn't pass through a NFZ then the path is
                // just those two node and the cost is just the Euclidean distance between them

                if (!NoFlyZone.intersectsNoFlyZones(n1.getCoordinate(), n2.getCoordinate(), NFZs)) {

                    var nodesInPath = new ArrayList<Node>();
                    nodesInPath.add(n1);
                    nodesInPath.add(n2);

                    var p = new Path(nodesInPath, n1.getCoordinate().euclideanDistance(n2.getCoordinate()));
                    result[i][j] = p;

                    // Otherwise we construct a visibility graph and get the path from that
                } else {

                    var VG = new VisibilityGraph(n1, n2, NFZs);
                    var bestPath = VG.bestPath();
                    result[i][j] = bestPath;

                }

            }
        }

        return result;

    }

    /**
     * @return a full tour including Starting Position, Sensors and NFZ vertices.
     */
    public List<Node> findTour() {

        // Use twoOpt to construct a tour (containing Starting Position and Sensors)
        // and rotate the tour so the Starting Position is the first item in the tour
        var tour = twoOpt();

        // Last element is just the same as first
        tour.remove(tour.size() - 1);

        var spLocation = 0;
        for (var i = 0; i < tour.size(); i++) {
            if (tour.get(i) instanceof StartingPosition) {
                spLocation = i;
            }
        }

        var p1 = new ArrayList<>(tour.subList(spLocation, tour.size()));
        var p2 = new ArrayList<>(tour.subList(0, spLocation));

        p1.addAll(p2);
        tour = p1;

        // If the first sensor to visit is very close to the StartingPosition, visit it
        // last to (potentially) save moves
        // (Because we have to move before making our first sensor reading, and if this
        // move overshoots then we will have to move back)
        if (tour.get(0).getCoordinate().euclideanDistance((tour.get(1).getCoordinate())) < Constants.MOVE_SIZE
                - Constants.MAX_DISTANCE_FROM_SENSOR) {
            tour.add(tour.get(1));
            tour.remove(1);
        }

        // Our current tour contains just the starting position and the sensors
        // Now we will construct the full tour (with NFZ vertices) from the paths we
        // computed previously

        var fullTour = new ArrayList<Node>();

        int p;
        int q;
        for (var i = 0; i < tour.size() - 1; i++) {
            p = nodes.indexOf((tour.get(i)));
            q = nodes.indexOf((tour.get(i + 1)));
            fullTour.addAll(paths[p][q].getNodes());
            // When we add nodes from p to q we remove the last node
            // As it will get added again in the next iteration of the loop
            fullTour.remove(fullTour.size() - 1);
        }

        // Add nodes from last sensor back to Starting position
        p = nodes.indexOf((tour.get(tour.size() - 1)));
        q = nodes.indexOf((tour.get(0)));
        fullTour.addAll(paths[p][q].getNodes());

        return fullTour;

    }

    /**
     * Uses the TwoOptHeurstic from JgraphT to compute a tour for the nodes (Sensors
     * and StartingPosition)
     * 
     * @return a tour optimized by two-opt
     */
    private List<Node> twoOpt() {

        // Create a weighted graph
        DefaultUndirectedWeightedGraph<Node, DefaultWeightedEdge> graph = 
                new DefaultUndirectedWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // Add nodes to graph
        for (var node : nodes) {
            graph.addVertex(node);
        }

        // Add all weighted edges to graph
        var n = paths.length;
        for (var i = 0; i < n; i++) {
            for (var j = i + 1; j < n; j++) {
                var edge = graph.addEdge(nodes.get(i), nodes.get(j));
                graph.setEdgeWeight(edge, paths[i][j].getCost());
            }
        }

        // Perform TwoOptHeuristic
        var iterations = Constants.TWO_OPT_ITERATIONS;
        var tsp = new TwoOptHeuristicTSP<Node, DefaultWeightedEdge>(iterations, seed);
        var tour = tsp.getTour(graph).getVertexList();

        // Return the tour
        return tour;

    }

    /**
     * Removes a node. The node removed will be the one that is the furthest away
     * from every other node.
     */
    public void removeMostIsolated() {

        if (nodes.size() <= 1) {
            return;
        }

        var furthest = 0;
        var cost = 0.0;

        // Iterate over nodes, start at index 1 (don't want to remove StartingPosition
        // which is at index 0)
        for (var i = 1; i < nodes.size(); i++) {
            var currentCost = 0.0;
            for (var j = 0; j < nodes.size(); j++) {
                currentCost += paths[i][j].getCost();
            }
            if (currentCost > cost) {
                cost = currentCost;
                furthest = i;
            }
        }

        // Remove the sensor that has highest cost
        nodes.remove(furthest);

        // update paths
        paths = shortestPaths();

    }

}
