package name.gano.astro;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static java.lang.Math.*;
import static name.gano.astro.MathUtils.*;

public class MathUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMultDoubleArrayArrayDoubleArrayArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testMultDoubleArrayArrayDoubleArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testDot() {
        fail("Not yet implemented");
    }

    @Test
    public void testTranspose() {
        fail("Not yet implemented");
    }

    @Test
    public void testSub() {
        fail("Not yet implemented");
    }

    @Test
    public void testAdd() {
        fail("Not yet implemented");
    }

    @Test
    public void testNorm() {
        fail("Not yet implemented");
    }

    @Test
    public void testScale() {
        fail("Not yet implemented");
    }

    @Test
    public void testCross() {
        fail("Not yet implemented");
    }

    @Test
    public void testFrac() {
        assertEquals(0.3, frac(5.3), 0.00001);
        assertEquals(-0.3, frac(-5.3), 0.00001);
    }

    @Test
    public void testModulo() {
        assertEquals(PI/2F, modulo(3*PI/2, PI), 0.00001);
        assertEquals(-PI/2F, modulo(-3*PI/2, PI), 0.00001);
    }

    @Test
    public void testUnitVector() {
        fail("Not yet implemented");
    }

    @Test
    public void testR_x() {
        fail("Not yet implemented");
    }

    @Test
    public void testR_y() {
        fail("Not yet implemented");
    }

    @Test
    public void testR_z() {
        fail("Not yet implemented");
    }

    @Test
    public void testRoundToZero() {
        assertEquals(5, roundToZero(5.3), 0.000001);
        assertEquals(-5, roundToZero(-5.3), 0.000001);
    }

}
