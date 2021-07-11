package uk.ac.ed.inf.aqmaps.mapdetails;

import uk.ac.ed.inf.aqmaps.Constants;

/**
 * Represents the vertex of a NoFlyZone
 */
public class NoFlyZoneVertex implements Node {
    
    private NoFlyZone noFlyZone;
    private Coordinate coordinate;
    
    public NoFlyZoneVertex(NoFlyZone noFlyZone, Coordinate coordinate) {
        this.noFlyZone = noFlyZone;
        this.coordinate = coordinate;
    }
    
    public Coordinate getCoordinate() {
        return coordinate;
    }
    
    public NoFlyZone getNoFlyZone() {
        return noFlyZone;
    }

    
    public boolean withinRange(Coordinate p) {
        return coordinate.euclideanDistance(p) < Constants.MAX_DISTANCE_FROM_VERTEX;
    }

}
