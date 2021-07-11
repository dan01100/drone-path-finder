package uk.ac.ed.inf.aqmaps.mapdetails;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.aqmaps.Constants;

public class HttpRequests {

    /**
     * Makes a request to the server
     * 
     * @param path
     * @param port
     * @return Response
     */
    private static HttpResponse<String> httpRequest(String path, int port) {

        var url = Constants.SERVER + ":" + port + path;

        // Create a new HttpClient with default settings.
        var client = HttpClient.newHttpClient();
        // HttpClient assumes that it is a GET request by default.
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        // The response object is of class HttpResponse<String>
        HttpResponse<String> response = null;

        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Fatal error: Unable to connect to " + Constants.SERVER + " at port " + port + ".");
            System.exit(1); // Exit the application
        }

        if (response.statusCode() == 404) {
            System.out.println("404 Not Found: " + url);
            System.exit(1);
        }

        return response;

    }

    /**
     * Returns the Sensors that need to be read on the given date by making a
     * request to the webserver
     * 
     * @param year
     * @param month
     * @param day
     * @param port
     * @return list of Sensors
     */
    public static List<Sensor> sensorRequest(String year, String month, String day, int port) {

        var path = "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";

        var response = httpRequest(path, port);

        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(response.body(), listType);

        // Update the coordinate field for the sensors
        updateCoordinates(sensorList, port);
        return sensorList;

    }

    /**
     * Updates the Coordinate fields for all the Sensors
     * 
     * @param sensors
     * @param port
     */
    private static void updateCoordinates(List<Sensor> sensors, int port) {

        for (var sensor : sensors) {
            updateCoordinate(sensor, port);
        }

    }

    /**
     * Updates the Coordinate field for the Sensor by making a request to the
     * webserver and finding the coordinate corresponding to the Sensor’s W3W
     * location.
     * 
     * @param sensor
     * @param port
     */
    private static void updateCoordinate(Sensor sensor, int port) {

        var words = sensor.getLocation().split("\\.");

        var path = "/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";

        var response = httpRequest(path, port);

        // Use W3wDetails to get the lat and lng
        var details = new Gson().fromJson(response.body(), W3wDetails.class);

        var lat = details.coordinates.lat;
        var lng = details.coordinates.lng;

        sensor.setCoordinate(new Coordinate(lat, lng));

    }

    /**
     * Returns the NoFlyZones by making a request to the webserver
     * 
     * @param port
     * @return list of NoFlyZones
     */
    public static List<NoFlyZone> noFlyZonesRequest(int port) {

        var path = "/buildings/no-fly-zones.geojson";
        var response = httpRequest(path, port);

        var featureCollection = FeatureCollection.fromJson(response.body());
        var buildings = featureCollection.features();

        var noFlyZones = new ArrayList<NoFlyZone>();

        for (var building : buildings) {
            var polygon = (Polygon) building.geometry();
            var points = polygon.coordinates();

            // Create a list of coordinates for the NFZ
            var coordinates = new ArrayList<Coordinate>();
            for (var p : points.get(0)) {
                coordinates.add(new Coordinate(p.latitude(), p.longitude()));
            }

            // Last coordinate is same as first
            coordinates.remove(coordinates.size() - 1);
            // Create NoFlyZone for building
            noFlyZones.add(new NoFlyZone(coordinates));
        }

        return noFlyZones;

    }

}
