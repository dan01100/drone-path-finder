package uk.ac.ed.inf.aqmaps.mapdetails;

import uk.ac.ed.inf.aqmaps.Constants;

/**
 * Represents the starting position of the drone
 */

public class StartingPosition implements Node {

    private Coordinate coordinate;

    public StartingPosition(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public boolean withinRange(Coordinate p) {
        return coordinate.euclideanDistance(p) < Constants.MAX_DISTANCE_FROM_START;
    }

}
