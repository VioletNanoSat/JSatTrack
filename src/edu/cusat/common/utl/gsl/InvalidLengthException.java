package edu.cusat.common.utl.gsl;

/**
 * Checked version of IndexOutOfBounds/IllegalArgumentException, as this can
 * happen in some corner cases, if the incoming GSL data is not parsed
 * correctly.
 * 
 * @author Nate Parsons nsp25
 * 
 */
public class InvalidLengthException extends Exception {

    /** Allows the class to be read from an ObjectInputStream */
    private static final long serialVersionUID = 3981263535209067114L;

    /**
     * @param mExpected - the number of elements expected in the array
     * @param mFound - the number of elements actually in the array
     */
    public InvalidLengthException(int mExpected, int mFound) {
        super("Array is " + mFound + " bytes long," + "should be " + mExpected);
    }

    /**
     * @param mExpected - the number of elements expected in the array
     * @param mFound - the number of elements actually in the array
     * @throws InvalidLengthException - if mExpected != mFound
     */
    public static void checkLength(int mExpected, int mFound)
            throws InvalidLengthException {
        if (mExpected != mFound)
            throw new InvalidLengthException(mExpected, mFound);
    }
}
