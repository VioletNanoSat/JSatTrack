package jsattrak.utilities;

import java.util.Arrays;

public class Arrays2d {

    private Arrays2d() { }

    /**
     * Copies the specified array, truncating or padding with <code>null</code>
     * (if necessary) so the copy has the specified length.
     * 
     * @param original
     * @return
     */
    public static double[][] copyOf(double[][] original, int newLength) {
        double[][] copy = new double[newLength][];
        for (int i = 0; i < copy.length; i++)
            if (i < original.length)
                copy[i] = Arrays.copyOf(original[i], original[i].length);
        return copy;
    }

}
