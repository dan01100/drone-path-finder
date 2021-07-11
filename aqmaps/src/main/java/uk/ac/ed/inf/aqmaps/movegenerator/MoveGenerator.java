package uk.ac.ed.inf.aqmaps.movegenerator;

import java.util.ArrayList;

import java.util.List;

import uk.ac.ed.inf.aqmaps.Constants;
import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;
import uk.ac.ed.inf.aqmaps.mapdetails.NoFlyZone;
import uk.ac.ed.inf.aqmaps.mapdetails.Node;
import uk.ac.ed.inf.aqmaps.mapdetails.Sensor;

/**
 * Generates a list of Moves for the drone from an ordered list of nodes.
 */

public class MoveGenerator {

    // Keeps track if we have moved since our last sensor reading
    private boolean movedSinceLastReading;
    private List<Node> nodes;
    private List<NoFlyZone> NFZs;
    // Current list of moves
    private List<Move> moves;

    public MoveGenerator(List<Node> nodes, List<NoFlyZone> NFZs) {
        this.nodes = nodes;
        this.NFZs = NFZs;
        this.movedSinceLastReading = false;
        this.moves = new ArrayList<Move>();
        generateMoves();

    }

    public List<Move> getMoves() {
        return moves;
    }

    /**
     * Converts the list of nodes to a list of Moves
     */
    private void generateMoves() {

        for (var i = 1; i < nodes.size(); i++) {
            addMovesToNextNode(nodes.get(i));
        }

    }

    /**
     * Adds the moves from our current position to the next node
     * 
     * @param next Node
     */
    private void addMovesToNextNode(Node next) {

        // Get the the drone's current position
        Coordinate currentPosition;
        if (moves.isEmpty()) {
            currentPosition = nodes.get(0).getCoordinate();
        } else {
            // start is the ending position of the drones last move
            currentPosition = moves.get(moves.size() - 1).getEndingPosition();
        }
        // If we aren't within range of next, perform beamSearch to get a path from
        // start to next node
        if (!next.withinRange(currentPosition)) {
            var newMoves = beamSearch(currentPosition, next);
            this.moves.addAll(newMoves);
            // Update movedSinceLastScan as we have moved again
            movedSinceLastReading = true;
        }

        // If Sensor then check if we have moved since last reading.
        // If we haven't then make an extra move towards the sensor

        if (next instanceof Sensor) {
            var sensor = (Sensor) next;
            if (!movedSinceLastReading) {
                if (moves.isEmpty()) {
                    currentPosition = nodes.get(0).getCoordinate();
                } else {
                    currentPosition = moves.get(moves.size() - 1).getEndingPosition();
                }
                var move = calculateMoves(currentPosition, next.getCoordinate(), 1).get(0);
                moves.add(move);

                // If this move takes us out of range, move back
                if (!next.withinRange(move.getEndingPosition())) {
                    var move2 = new Move((move.getAngle() + 180) % 360, move.getEndingPosition());
                    moves.add(move2);
                }
            }
            // Update our last move to contain the sensors location
            // and set the sensor's visited value to true and set movedSinceLastReading back
            // to false
            moves.get(moves.size() - 1).setW3Wlocation(sensor.getLocation());
            sensor.setVisited(true);
            movedSinceLastReading = false;
        }

    }

    /**
     * Searches for a sequence of moves from the start to end Coordinate
     * 
     * @param start a Coordinate
     * @param end   a Coordinate
     * @return a list of moves from the start to the end Coordinate
     */
    private List<Move> beamSearch(Coordinate start, Node end) {

        var beamWidth = Constants.INITIAL_BEAM_WIDTH;

        // Already in range, so return empty list of Moves
        if (end.withinRange(start)) {
            return new ArrayList<Move>();
        }

        // Create root node with null parents and null Move
        var root = new TreeNode(null, null);

        // Create an array of leaves and add the root
        var leaves = new ArrayList<TreeNode>();
        leaves.add(root);

        // nextLeaves will hold the leaves in the layer after leaves
        var nextLeaves = new ArrayList<TreeNode>();

        while (true) {

            for (TreeNode leaf : leaves) {
                // For each leaf calculate beamWidth moves
                List<Move> moves;
                if (leaf == root) {
                    // if leaf is root calculate beamWidth moves from start
                    moves = calculateMoves(start, end.getCoordinate(), beamWidth);
                } else {
                    // otherwise calculate beamWidth moves from the move at that leaf
                    moves = calculateMoves(leaf.getMove().getEndingPosition(), end.getCoordinate(), beamWidth);
                }
                // Add them as children
                leaf.addChildren(moves);
                // Add the children to nextLeaves for the next iteration of the loop
                nextLeaves.addAll(leaf.getChildren());
                // If one of the children's moves ends up within range return it and its
                // predecessors Moves
                for (var child : leaf.getChildren()) {
                    if (end.withinRange(child.getMove().getEndingPosition())) {
                        return child.getMoves();
                    }
                }

            }

            // Update leaves to be nextLeaves and reset nextLeaves
            leaves = nextLeaves;
            nextLeaves = new ArrayList<TreeNode>();

            // reduce beamWidth
            if (beamWidth > 1)
                beamWidth--;

        }

    }

    /**
     * Calculates numMoves possible Moves in the direction of the end Coordinate
     * starting at the start Coordinate
     * 
     * @param start    a Coordinate
     * @param end      a Coordinate
     * @param numMoves the number of moves to generate
     * @return numMoves Moves
     */
    private List<Move> calculateMoves(Coordinate start, Coordinate end, int numMoves) {

        ArrayList<Move> moves = new ArrayList<Move>();

        // Get direction
        var angle = start.angle(end);

        // round angle to nearest 10 degrees
        var rounded = ((int) Math.round(angle / 10.0) * 10) % 360;

        // Create a move in this direction
        var move = new Move(rounded, start);

        // If valid then add it to moves
        if (tryMove(move)) {
            moves.add(move);
        }

        // Initialise angles for the next possible moves
        var up = (rounded + 10) % 360;
        var down = rounded - 10;
        if (down < 0)
            down += 360;

        // Stop when we have enough Moves
        while (moves.size() < numMoves) {

            var upMove = new Move(up, start);
            var downMove = new Move(down, start);

            // if up is closer to actual angle try upMove first
            if (Math.abs(up - angle) < Math.abs(down - angle)) {
                if (tryMove(upMove)) {
                    moves.add(upMove);
                }
                if (moves.size() < numMoves && tryMove(downMove)) {
                    moves.add(downMove);
                }
            } else { // Otherwise try downMove first
                if (tryMove(downMove)) {
                    moves.add(downMove);
                }
                if (moves.size() < numMoves && tryMove(upMove)) {
                    moves.add(upMove);
                }
            }

            if (up == down) // ran out of moves
                break;

            up = (up + 10) % 360;
            down = down - 10;
            if (down < 0)
                down += 360;

        }

        return moves;

    }

    /**
     * @param move
     * @return true if the move is valid i.e. it doesn't cross any NFZs or leave the
     *         confinement area
     */
    private boolean tryMove(Move move) {

        // Check the move doesn't leave the confinement area
        for (var i = 0; i < Constants.CONFINEMENT_AREA.length; i++) {
            if (Coordinate.linesIntersect(move.getStartingPosition(), move.getEndingPosition(),
                    Constants.CONFINEMENT_AREA[i],
                    Constants.CONFINEMENT_AREA[(i + 1) % Constants.CONFINEMENT_AREA.length])) {
                return false;
            }
        }

        // Check the move doesn't enter any NFZs
        return !NoFlyZone.intersectsNoFlyZones(move.getStartingPosition(), move.getEndingPosition(), NFZs);

    }

}
