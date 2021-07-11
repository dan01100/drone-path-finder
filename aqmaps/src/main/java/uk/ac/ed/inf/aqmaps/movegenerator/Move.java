package uk.ac.ed.inf.aqmaps.movegenerator;

import java.util.ArrayList;

import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import uk.ac.ed.inf.aqmaps.Constants;
import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;

/**
 * Represent a move by the drone
 */

public class Move {

    // Direction of the move
    private int angle;
    // W3W location of the sensor read after this move ("null" if no reading is
    // taken)
    private String W3Wlocation;
    private Coordinate start;
    private Coordinate end;

    public Move(int angle, Coordinate start) {
        this.angle = angle;
        this.start = start;
        this.W3Wlocation = "null";
        this.end = start.move(angle, Constants.MOVE_SIZE);
    }

    public int getAngle() {
        return angle;
    }

    public String getW3Wlocation() {
        return W3Wlocation;
    }

    public Coordinate getStartingPosition() {
        return start;
    }

    public Coordinate getEndingPosition() {
        return end;
    }

    public void setW3Wlocation(String w3Wlocation) {
        W3Wlocation = w3Wlocation;
    }

    @Override
    public String toString() {
        var s = start.getLongitude() + "," + start.getLatitude() + "," + angle + "," + end.getLongitude() + ","
                + end.getLatitude() + "," + W3Wlocation;
        return s;
    }


    /**
     * @return a GeoJson feature from a list of Moves
     */
    public static Feature toGeoJson(List<Move> moves) {

        var points = new ArrayList<Point>();
        var start = moves.get(0).start;
        points.add(start.toGeoJson());

        for (var move : moves) {
            points.add(move.end.toGeoJson());
        }

        var lineString = LineString.fromLngLats(points);
        var feature = Feature.fromGeometry((Geometry) lineString);
        return feature;
    }

}
