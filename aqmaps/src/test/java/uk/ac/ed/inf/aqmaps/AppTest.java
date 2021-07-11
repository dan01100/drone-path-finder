package uk.ac.ed.inf.aqmaps;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ed.inf.aqmaps.mapdetails.Coordinate;
import uk.ac.ed.inf.aqmaps.mapdetails.NoFlyZone;
import uk.ac.ed.inf.aqmaps.mapdetails.Sensor;
import uk.ac.ed.inf.aqmaps.tourfinder.VisibilityGraph;

public class AppTest {

    Coordinate origin = new Coordinate(0, 0);
    Coordinate testCoord = new Coordinate(1, 0);

    Double delta = 0.0000000001;
    
    Coordinate p1 = new Coordinate(0.5, 0.5);
    Coordinate p2 = new Coordinate(0, 0.5);
    Coordinate p3 = new Coordinate(-1, 0.5);
    Coordinate p4 = new Coordinate(-2, 0.5);
    
    NoFlyZone NFZ;
    NoFlyZone NFZ2;

    @Before
    public void initNFZs() {
        
        var NFZcoords = new ArrayList<Coordinate>();
        var c1 = new Coordinate(1, 1);
        var c2 = new Coordinate(1, 0);
        var c3 = new Coordinate(0, 0);
        var c4 = new Coordinate(0, 1);
        NFZcoords.add(c1);
        NFZcoords.add(c2);
        NFZcoords.add(c3);
        NFZcoords.add(c4);
        NFZ = new NoFlyZone(NFZcoords);

        NFZcoords = new ArrayList<Coordinate>();
        c1 = new Coordinate(1, 2);
        c2 = new Coordinate(2, 2);
        c3 = new Coordinate(2, 3);
        c4 = new Coordinate(1, 3);
        NFZcoords.add(c1);
        NFZcoords.add(c2);
        NFZcoords.add(c3);
        NFZcoords.add(c4);
        NFZ2 = new NoFlyZone(NFZcoords);
    }

    @Test
    public void coordinateAngleTest() {

        assertEquals(origin.angle(testCoord), 90.0, delta);
        assertEquals(testCoord.angle(origin), 270.0, delta);

        assertEquals(origin.angle(new Coordinate(0, 1)), 0.0, delta);
        assertEquals(testCoord.angle(new Coordinate(2, 1)), 45.0, delta);

    }

    @Test
    public void coordinateMoveTest() {

        assertEquals(origin.move(90.0, 1.0).getLatitude(), 1.0, delta);
        assertEquals(origin.move(90.0, 1.0).getLongitude(), 0.0, delta);

        assertEquals(testCoord.move(45.0, Math.sqrt(2)).getLatitude(), 2.0, delta);
        assertEquals(testCoord.move(45.0, Math.sqrt(2)).getLongitude(), 1.0, delta);

    }

    @Test
    public void lineIntersectTest() {
        
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(0, 1);

        // Line segments intersect
        assertTrue(Coordinate.linesIntersect(p1, p4, c1, c2));
        // Line segments touch
        assertTrue(Coordinate.linesIntersect(p2, p4, c1, c2));
        // No touch or intersect
        assertFalse(Coordinate.linesIntersect(p3, p4, c1, c2));

    }

    @Test
    public void NFZtest() {
        
        // Crosses
        assertTrue(NFZ.intersectedBy(p4, p1));

        // Touches
        assertTrue(NFZ.intersectedBy(p4, p2));

        // Does not touch or cross
        assertFalse(NFZ.intersectedBy(p4, p3));

        // in NFZ
        assertTrue(NFZ.contains(p1));

        // On edge of NFZ
        assertTrue(NFZ.contains(p2));

        // Outside
        assertFalse(NFZ.contains(p3));

    }

    @Test
    public void visibilityGraphTest() {

        //One NFZ
        var NFZs = new ArrayList<NoFlyZone>();
        NFZs.add(NFZ);

        var start = new Sensor("null", 0, "null");
        start.setCoordinate(new Coordinate(0.5, -0.5));
        
        var end = new Sensor("null", 0, "null");
        end.setCoordinate(new Coordinate(0.5, 1.5));

        var vg = new VisibilityGraph(start, end, NFZs);

        assertEquals(vg.bestPath().getCost(), 1 + 2 * Math.sqrt(0.5), delta);

        //Two NFZs
        NFZs.add(NFZ2);
        
        end = new Sensor("null", 0, "null");
        end.setCoordinate(new Coordinate(1.5, 3.5));

        vg = new VisibilityGraph(start, end, NFZs);

        assertEquals(vg.bestPath().getCost(), 3 + 2 * Math.sqrt(0.5), delta);

    }

}
