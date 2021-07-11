package uk.ac.ed.inf.aqmaps.mapdetails;

import java.util.ArrayList;

import java.util.List;
import java.awt.geom.Path2D;

/**
 * Represents a No Fly Zone
 */
public class NoFlyZone {

    // vertices of the NFZ
    private List<NoFlyZoneVertex> vertices;

    // Used for checking if points lie inside the NFZ
    private Path2D polygon;

    public NoFlyZone(List<Coordinate> coordinates) {

        // Create vertex for each coordinate
        vertices = new ArrayList<NoFlyZoneVertex>();
        for (var coord : coordinates) {
            vertices.add(new NoFlyZoneVertex(this, coord));
        }

        // Create a Path2D object
        polygon = new Path2D.Double();
        polygon.moveTo(coordinates.get(0).getLongitude(), coordinates.get(0).getLatitude());
        for (int i = 1; i < vertices.size(); i++) {
            polygon.lineTo(coordinates.get(i).getLongitude(), coordinates.get(i).getLatitude());
        }
        polygon.closePath();
    }

    public List<NoFlyZoneVertex> getVertices() {
        return vertices;
    }

    /**
     * @param p1 a Coordinate
     * @param p2 a Coordinate
     * @return true if the line segment joining p1 and p2 enters the NoFlyZone
     * 
     */
    public boolean intersectedBy(Coordinate p1, Coordinate p2) {

        // Iterate over all edges and check for intersection
        for (var i = 0; i < vertices.size() - 1; i++) {
            if (Coordinate.linesIntersect(p1, p2, vertices.get(i).getCoordinate(),
                    vertices.get(i + 1).getCoordinate())) {
                return true;
            }
        }

        // Check edge joining the last vertex and first
        if (Coordinate.linesIntersect(p1, p2, vertices.get(vertices.size() - 1).getCoordinate(),
                vertices.get(0).getCoordinate())) {
            return true;
        }

        return false;
    }

    /**
     * @param p a Coordinate
     * @return true if p is inside the NoFlyZone
     */
    public boolean contains(Coordinate p) {
        return polygon.contains(p.getLongitude(), p.getLatitude());
    }

    /**
     * @param p1   a Coordinate
     * @param p2   a Coordinate
     * @param NFZs a list of NoFlyZones
     * @return true if the line segment joining p1 and p2 intersects any of the
     *         NoFlyZones
     */
    public static boolean intersectsNoFlyZones(Coordinate p1, Coordinate p2, List<NoFlyZone> NFZs) {

        for (var NFZ : NFZs) {
            if (NFZ.intersectedBy(p1, p2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param p    a Coordinate
     * @param NFZs a list of NoFlyZones
     * @return true if p lies inside any of the NoFlyZones
     */
    public static boolean insideNoFlyZones(Coordinate p, List<NoFlyZone> NFZs) {

        for (var NFZ : NFZs) {
            if (NFZ.contains(p)) {
                return true;
            }
        }
        return false;
    }

}
