package uk.ac.ed.inf.aqmaps.mapdetails;


/**
 * Represents a node on the map that the drone can travel towards.
 */
public interface Node {
    
    
    /**
     * @return the coordinate of the Node
     */
    public Coordinate getCoordinate();

    /**
     * @param p a Coordinate
     * @return true if p is within range of the Node
     */
    public boolean withinRange(Coordinate p);

}
