package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import uk.ac.ed.inf.aqmaps.mapdetails.Sensor;
import uk.ac.ed.inf.aqmaps.movegenerator.Move;

/**
 * For writing text files and GeoJson files
 */

public class FileWrite {

    /**
     * Writes a list of moves to a text file.
     * 
     * @param moves
     * @param filename
     */
    public static void writeMoves(List<Move> moves, String filename) {

        try {
            var fw = new FileWriter(filename);
            for (var i = 0; i < moves.size(); i++) {
                fw.write(i + 1 + "," + moves.get(i).toString() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("Failed to write to file.");
            e.printStackTrace();
        }

    }

    /**
     * Writes the Sensors and Moves to a GeoJson file
     * 
     * @param moves
     * @param sensors
     * @param filename
     */
    public static void createMap(List<Move> moves, List<Sensor> sensors, String filename) {

        var features = new ArrayList<Feature>();
        for (var sensor : sensors) {
            features.add(sensor.toGeoJson());
        }

        features.add(Move.toGeoJson(moves));
        var fc = FeatureCollection.fromFeatures(features);
        FileWrite.writeGeoJson(fc, filename);

    }


    /**
     * Writes a featureCollection to a GeoJson file.
     * 
     * @param featureCollection
     * @param filename
     */
    private static void writeGeoJson(FeatureCollection featureCollection, String filename) {

        try {
            var fw = new FileWriter(filename);
            fw.write(featureCollection.toJson());
            fw.close();
        } catch (IOException e) {
            System.out.println("Failed to write to file.");
            e.printStackTrace();
        }

    }

}
