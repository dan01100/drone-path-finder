package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;
import uk.ac.ed.inf.aqmaps.mapdetails.HttpRequests;
import uk.ac.ed.inf.aqmaps.mapdetails.StartingPosition;
import uk.ac.ed.inf.aqmaps.movegenerator.MoveGenerator;
import uk.ac.ed.inf.aqmaps.tourfinder.TourFinder;

public class App {

    public static void main(String[] args) {

        // Parse input
        var day = String.format("%02d", Integer.parseInt(args[0]));
        var month = String.format("%02d", Integer.parseInt(args[1]));
        var year = args[2];
        var startingLatitude = Double.parseDouble(args[3]);
        var startingLongitude = Double.parseDouble(args[4]);
        var seed = Integer.parseInt(args[5]);
        var port = Integer.parseInt(args[6]);

        // Get Sensors, NFZs and StartingPosition
        var sensors = HttpRequests.sensorRequest(year, month, day, port);
        var NFZs = HttpRequests.noFlyZonesRequest(port);
        var startingPosition = new StartingPosition(new Coordinate(startingLatitude, startingLongitude));

        // Find the shortest tour
        var tourFinder = new TourFinder(startingPosition, sensors, NFZs, seed);
        var shortestTour = tourFinder.findTour();

        // Generate Moves
        var moveGen = new MoveGenerator(shortestTour, NFZs);
        var moves = moveGen.getMoves();

        // If number of moves is too large, remove a sensor and compute a new tour
        while (moves.size() > Constants.MOVE_LIMIT) {
            tourFinder.removeMostIsolated();
            // reset visited boolean on sensors
            for (var sensor : sensors) {
                sensor.setVisited(false);
            }
            shortestTour = tourFinder.findTour();
            moveGen = new MoveGenerator(shortestTour, NFZs);
            moves = moveGen.getMoves();
        }

        // write Move log
        var filename = "flightpath-" + day + "-" + month + "-" + year + ".txt";
        FileWrite.writeMoves(moves, filename);

        // write GeoJson
        filename = "readings-" + day + "-" + month + "-" + year + ".geojson";
        FileWrite.createMap(moves, sensors, filename);

    }
    

}
