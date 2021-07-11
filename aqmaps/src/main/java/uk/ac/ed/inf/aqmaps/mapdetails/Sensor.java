package uk.ac.ed.inf.aqmaps.mapdetails;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;

import uk.ac.ed.inf.aqmaps.Constants;

/**
 * Represent a sensor
 */

public class Sensor implements Node {

    // W3W location of the sensor
    private String location;
    private double battery;
    private String reading;
    // The actual coordinate of the sensor
    private Coordinate coordinate;
    private boolean visited;

    public Sensor(String location, double battery, String reading) {
        this.location = location;
        this.battery = battery;
        this.reading = reading;
        this.visited = false;
    }

    public String getLocation() {
        return location;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return an RGB String for the GeoJson Feature based on the reading
     */
    private String readingToRGBString() {

        if (!visited) {
            return "#aaaaaa";
        }

        if (battery < 10) {
            return "#000000";
        }

        double fill = Double.parseDouble(reading);

        if (fill < 32) {
            return "#00ff00";
        } else if (fill < 64) {
            return "#40ff00";
        } else if (fill < 96) {
            return "#80ff00";
        } else if (fill < 128) {
            return "#c0ff00";
        } else if (fill < 160) {
            return "#ffc000";
        } else if (fill < 192) {
            return "#ff8000";
        } else if (fill < 224) {
            return "#ff4000";
        } else {
            return "#ff0000";
        }

    }

    /**
     * @return a String representing the type of marker to use in the GeoJson
     *         Feature based on the reading.
     */
    private String readingToMarker() {

        if (!visited) {
            return "";
        }

        if (battery < 10 || reading == "null" || reading == "NaN") {
            return "cross";
        }

        var r = Double.parseDouble(reading);

        if (r < 128) {
            return "lighthouse";
        } else {
            return "danger";
        }

    }
    
    /**
     * @return a GeoJson representation of the Sensor
     */
    public Feature toGeoJson() {

        var point = this.coordinate.toGeoJson();

        var feature = Feature.fromGeometry((Geometry) point);

        feature.addStringProperty("location", location);
        feature.addStringProperty("rgb-string", readingToRGBString());
        feature.addStringProperty("marker-color", readingToRGBString());
        feature.addStringProperty("marker-symbol", readingToMarker());

        return feature;

    }

    public boolean withinRange(Coordinate p) {
        return coordinate.euclideanDistance(p) < Constants.MAX_DISTANCE_FROM_SENSOR;
    }

}
