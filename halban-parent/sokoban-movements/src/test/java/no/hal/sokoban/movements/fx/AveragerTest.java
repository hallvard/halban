package no.hal.sokoban.movements.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AveragerTest {

    @Test
    public void testAverage() {
        var averager = new Averager(5);
        assertEquals(5.0, averager.average(5.0));
        assertEquals(6.0, averager.average(7.0));
        assertEquals(6.0, averager.average(6.0));
        assertEquals(5.0, averager.average(2.0));
        assertEquals(6.0, averager.average(10.0));
        assertEquals(7.0, averager.average(10.0));
     }
}