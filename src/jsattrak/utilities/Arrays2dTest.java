package jsattrak.utilities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Arrays2dTest {

    @Test
    public void testCopyOf() {
        final double[][] original = new double[][] { 
                {  0,  1,  2,  3,  4 },
                {  5,  6,  7,  8,  9 }, 
                { 10, 11, 12, 13, 14 } };
        
        // Only the first row
        final double[][] copy1 = Arrays2d.copyOf(original, 1);
        assertNotSame(original, copy1);
        assertNotSame(original[0], copy1[0]);
        assertArrayEquals(original[0], copy1[0], 0F);
        
        // Complete copy
        final double[][] copy2 = Arrays2d.copyOf(original, original.length);
        assertNotSame(original, copy2);
        for (int i = 0; i < copy2.length; i++) {
            assertNotSame(original[i], copy2[i]);
            assertArrayEquals(original[i], copy2[i], 0F);
        }
        
        // Copy with extra rows
        final double[][] copy3 = Arrays2d.copyOf(original, original.length + 2);
        assertNotSame(original, copy3);
        for (int i = 0; i < copy3.length; i++) {
            if (i < original.length) {
                assertNotSame(original[i], copy3[i]);
                assertArrayEquals(original[i], copy3[i], 0F);
            } else {
                assertNull(copy3[i]);
            }
        }
    }

}
