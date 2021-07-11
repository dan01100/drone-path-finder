package uk.ac.ed.inf.aqmaps.mapdetails;

import java.awt.geom.Line2D;

import com.mapbox.geojson.Point;

/**
 * This class represents a coordinate in the form (latitude, longitude).
 */

public class Coordinate {

    private double latitude;
    private double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * @param p a Coordinate
     * @return Euclidean distance between this and p
     */
    public double euclideanDistance(Coordinate p) {
        return Math.sqrt(Math.pow(this.longitude - p.longitude, 2) + Math.pow(this.latitude - p.latitude, 2));
    }

    /**
     * Computes the angle of this to p measured counter-clockwise from the
     * horizontal axis
     * 
     * @param p a Coordinate
     * @return an angle in degrees between 0 and 360, exclusive of 360
     */
    public double angle(Coordinate p) {

        var rad = Math.atan2(p.latitude - this.latitude, p.longitude - this.longitude);
        var deg = rad * 180 / Math.PI;
        if (deg < 0) {
            return deg + 360;
        }
        return deg;

    }

    /**
     * Computes a Coordinate that is the specified distance away from this at the
     * specified angle
     * 
     * @param angle    in degrees
     * @param distance
     * @return new Coordinate
     */
    public Coordinate move(double angle, double distance) {

        var lng = this.longitude + (distance * Math.cos(angle * Math.PI / 180));
        var lat = this.latitude + (distance * Math.sin(angle * Math.PI / 180));

        return new Coordinate(lat, lng);
    }

    /**
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @return true if the line segment between p1 and p2 intersects the line
     *         segment between p3 and p4
     */
    public static boolean linesIntersect(Coordinate p1, Coordinate p2, Coordinate p3, Coordinate p4) {

        return Line2D.linesIntersect(p1.longitude, p1.latitude, p2.longitude, p2.latitude, p3.longitude, p3.latitude,
                p4.longitude, p4.latitude);

    }

    /**
     * @return a GeoJson representation of the Coordinate
     */
    public Point toGeoJson() {
        return Point.fromLngLat(this.longitude, this.latitude);
    }

    @Override
    public String toString() {
        return "(" + this.latitude + ", " + this.longitude + ")";
    }

}
