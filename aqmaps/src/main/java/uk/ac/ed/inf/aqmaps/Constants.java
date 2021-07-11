package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;


/**
 * List of constants for the application
 */
public class Constants {
    
    //Maximum distance the drone can be from the sensor to take a reading
    public static final double MAX_DISTANCE_FROM_SENSOR = 0.0002;
    //Maximum distance the drone can be from the starting position after completing a tour
    public static final double MAX_DISTANCE_FROM_START = 0.0003;
    //How far the drone should be from a NFZ vertex before moving onto the next node in the tour
    public static final double MAX_DISTANCE_FROM_VERTEX = 0.0003;

    //Size of the drones moves
    public static final double MOVE_SIZE = 0.0003;
    //Maximum number of moves the drone is allowed to take
    public static final int MOVE_LIMIT = 150;
    //Number of iterations to run two-opt heuristic when finding a tour
    public static final int TWO_OPT_ITERATIONS = 100;
    //Initial Beam width when searching for a sequence of moves from one node to the next
    public static final int INITIAL_BEAM_WIDTH = 7;
    //Server address
    public static final String SERVER = "http://localhost";
    
    //Coordinates of the confinement area
    public static final Coordinate[] CONFINEMENT_AREA = {
            new Coordinate(55.946233, -3.192473),
            new Coordinate(55.946233, -3.184319),
            new Coordinate(55.942617, -3.184319),
            new Coordinate(55.942617, -3.192473)
    };
    



}
