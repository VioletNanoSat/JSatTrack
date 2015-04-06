package jsattrak.utilities;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.regex.Pattern;

/**
 * TODO Comment
 * <p> Status
 * <table>
 * <tr> <td>Code</td>     <td>94%</td> </tr>
 * <tr> <td>Tests</td>    <td>24%</td>   </tr>
 * <tr> <td>Comments</td> <td>41%</td>  </tr>
 * <tr> <td>Logging</td>  <td>NA</td>   </tr>
 * <tr> <td>Style</td>    <td>Good</td> </tr>
 * </table>
 * @author Nate Parssons nsp25
 * @author Mike Goetz mag222
 */
public class Bytes {
    
	// Static usage only //
    private Bytes(){ }

///////////////// ASCII /////////////////
   
    public static final Charset ASCII = Charset.forName("US-ASCII");
    
	/** 
	 * Create the bytes underlying the given string using ASCII representation
	 * The inverse of {@link Bytes#toAscii(byte[])} 
	 **/
    public static byte[] fromAscii(String s){
    	return ASCII.encode(s).array();
    }
    
    /**
     * Create a string from a byte array using their ASCII representation
     * The inverse of {@link Bytes#fromAscii(byte[])} 
     */
    public static String toAscii(byte[] bs){
    	return ASCII.decode(ByteBuffer.wrap(bs)).toString();
    }
    
    public static void prettyPrint(byte[] array) {
        for (int i = 0; i < array.length; i++)
            System.out.print(String.format("%3d", i) + " ");
        System.out.println();
        for (byte b : array) {
            System.out.print(String.format("%3x", b) + " ");
        }
        System.out.println();
    }
    
///////////////// SHORTS /////////////////

    private static byte short1(short x) { return (byte) (x >> 8); }
    private static byte short0(short x) { return (byte) (x >> 0); }
    
    static private short makeShort(byte b1, byte b0) {
//    	System.arraycopy(Bytes.toArray((short)crc.getValue(), false), 0, packet, crc_offset, 2);
        return (short) ((b1 << 8) | (b0 & 0xff));
    }

    public static byte[] toArray(short s, boolean lsbFirst) {
        return lsbFirst ? new byte[] { short0(s), short1(s) } 
        : new byte[] { short1(s), short0(s) };
    }

    public static short toShort(byte[] b, int i, boolean lsbFirst) {
        return (lsbFirst ? makeShort(b[i + 1], b[i])
                : makeShort(b[i], b[i + 1]));
    }

    public static int toUShort(byte[] b, int i, boolean lsbFirst) {
        return (lsbFirst ? makeInt((byte) 0, (byte) 0, b[i + 1], b[i])
                : makeInt((byte) 0, (byte) 0, b[i], b[i + 1]));
    }

///////////////// INTS /////////////////
    
    private static byte int3(int x) { return (byte)(x >> 24); }
    private static byte int2(int x) { return (byte)(x >> 16); }
    private static byte int1(int x) { return (byte)(x >>  8); }
    private static byte int0(int x) { return (byte)(x >>  0); }
    
    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return ((((b3 & 0xff) << 24) | ((b2 & 0xff) << 16)
                | ((b1 & 0xff) << 8) | ((b0 & 0xff) << 0)));
    }

    public static byte[] toArray(int i, boolean lsbFirst) {
        return lsbFirst ? new byte[] { int0(i), int1(i), int2(i), int3(i) }
        : new byte[] { int3(i), int2(i), int1(i), int0(i) };
    }
    
    public static int toInt(byte[] mArray, int mIndex, boolean lsbFirst) {
        return lsbFirst ?
        		  ((mArray[mIndex + 3]& 0xff) << 24)
                | ((mArray[mIndex + 2] & 0xff) << 16)
                | ((mArray[mIndex + 1] & 0xff) << 8)
                | ((mArray[mIndex]     & 0xff) << 0)
                : ((mArray[mIndex]     & 0xff) << 24)
                | ((mArray[mIndex + 1] & 0xff) << 16)
                | ((mArray[mIndex + 2] & 0xff) << 8)
                | ((mArray[mIndex + 3] & 0xff) << 0);
    }
    
///////////////// LONGS /////////////////
    
    private static byte long7(long x) { return (byte)(x >> 56); }
    private static byte long6(long x) { return (byte)(x >> 48); }
    private static byte long5(long x) { return (byte)(x >> 40); }
    private static byte long4(long x) { return (byte)(x >> 32); }
    private static byte long3(long x) { return (byte)(x >> 24); }
    private static byte long2(long x) { return (byte)(x >> 16); }
    private static byte long1(long x) { return (byte)(x >>  8); }
    private static byte long0(long x) { return (byte)(x >>  0); }

    public static byte[] toArray(long l, boolean lsbFirst){
        return lsbFirst ? 
        new byte[] { long0(l), long1(l), long2(l), long3(l),
                long4(l), long5(l), long6(l), long7(l) } 
        : new byte[] {
                long7(l), long6(l), long5(l), long4(l), long3(l), long2(l),
                long1(l), long0(l) };
    }

    public static byte[] toArrayLen4(long l, boolean lsbFirst) {
        return lsbFirst ? new byte[] { long0(l), long1(l), long2(l), long3(l) }
        : new byte[] { long7(l), long6(l), long5(l), long4(l) };
    }
    
    /**
     * Extract a long from a byte array, copied from java.nio.Bits#makeLong
     * 
     * @param b
     *            byte array to copy from
     * @param i
     *            index to start at
     * @param lsbFirst
     *            <code>true</code> if the least significant byte of the array
     *            is at index i
     */
    public static long toLong(byte[] b, int i, boolean lsbFirst) {
        return lsbFirst ? (
                (((long)b[i+7] & 0xff) << 56) |
                (((long)b[i+6] & 0xff) << 48) |
                (((long)b[i+5] & 0xff) << 40) |
                (((long)b[i+4] & 0xff) << 32) |
                (((long)b[i+3] & 0xff) << 24) |
                (((long)b[i+2] & 0xff) << 16) |
                (((long)b[i+1] & 0xff) <<  8) |
                (((long)b[i+0] & 0xff) <<  0)) 
                : (
                (((long)b[i+0] & 0xff) << 56) |
                (((long)b[i+1] & 0xff) << 48) |
                (((long)b[i+2] & 0xff) << 40) |
                (((long)b[i+3] & 0xff) << 32) |
                (((long)b[i+4] & 0xff) << 24) |
                (((long)b[i+5] & 0xff) << 16) |
                (((long)b[i+6] & 0xff) <<  8) |
                (((long)b[i+7] & 0xff) <<  0));
    }

////////////// BYTE ARRAYS //////////////

    /**
     * Copy a section of an array.
     * 
     * @param src
     *            the source array
     * @param srcPos
     *            where to start
     * @param length
     *            how many bytes to copy
     * @param rev
     *            true if the copied array is to be the reverse of the src
     * @return a new array
     */
    public static byte[] copy(byte[] src, int srcPos, int length, boolean rev) {
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = (rev) ? src[srcPos + length - i - 1] : src[srcPos + i];
        }
        return ret;
    }

    /**
     * Creates a copy of the source array, starting at the specified index and
     * copying the specified number of bytes.
     * 
     * @param src
     *            the array to copy
     * @param srcPos
     *            where in src to start copying
     * @param length
     *            how many bytes to copy
     * @return a new array that is exactly the right size to hold all of the
     *         copied bytes
     */
    public static byte[] arraySlice(byte[] src, int srcPos, int length) {
        byte[] ret = new byte[length];
        System.arraycopy(src, srcPos, ret, 0, length);
        return ret;
    }


    /** 
     * Returns a {@link BitSet} containing the values in bytes.
     * <p>The inverse of
     * {@link Bytes#bitSetToByteArray(BitSet, boolean, boolean)
     * <p>Examples
     * <table border="1">
     * <th>RevBits</th><th>RevBytes</th><th>Result</th>
     * <tr><td align="center">true</td> <td align="center">true</td> <td>0000 1111 1010 1010 -> 0101 0101 1111 0000</td></tr>
     * <tr><td align="center">true</td> <td align="center">false</td><td>0000 1111 1010 1010 -> 1111 0000 0101 0101</td></tr>
     * <tr><td align="center">false</td><td align="center">true</td> <td>0000 1111 1010 1010 -> 1010 1010 0000 1111</td></tr>
     * <tr><td align="center">false</td><td align="center">false</td><td>0000 1111 1010 1010 -> 0000 1111 1010 1010</td></tr>
     * </table>
     */
    public static BitSet bitSetFromByteArray(byte[] bytes, boolean revBits,
            boolean revBytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[(revBytes ? bytes.length - i / 8 - 1 : i / 8)] & (1 << ((revBits ? i % 8
                    : 7 - (i % 8))))) > 0) {
                bits.set(i);
            }
        }

        return bits;
    }

    /**
     * Returns a byte array of at least length 1.
     * <p>The inverse of
     * {@link Bytes#bitSetFromByteArray(byte[], boolean, boolean)}
     * <p>Examples
     * <table border="1">
     * <th>RevBits</th><th>RevBytes</th><th>Result</th>
     * <tr><td align="center">true</td> <td align="center">true</td> <td>0000 1111 1010 1010 -> 0101 0101 1111 0000</td></tr>
     * <tr><td align="center">true</td> <td align="center">false</td><td>0000 1111 1010 1010 -> 1111 0000 0101 0101</td></tr>
     * <tr><td align="center">false</td><td align="center">true</td> <td>0000 1111 1010 1010 -> 1010 1010 0000 1111</td></tr>
     * <tr><td align="center">false</td><td align="center">false</td><td>0000 1111 1010 1010 -> 0000 1111 1010 1010</td></tr>
     * </table>
     **/
    public static byte[] bitSetToByteArray(BitSet bits, boolean revBits, boolean revBytes) {
        throw new UnsupportedOperationException(); // TODO FIXME
//        byte[] bytes = new byte[bits.length() / 8 + 1];
//        for (int i = 0; i < bits.length(); i++) {
//            if (bits.get(i)) {
//                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
//            }
//        }
//        return bytes;
    }
     
///////////////// HEX /////////////////

    public static String toHexString(int i, boolean reverse) {
        return toHexString(toArray(i, reverse));
    }

    public static String toHexString(short sh, boolean reverse) {
        return toHexString(toArray(sh, reverse));
    }

    /**
     * Generates the hex representation of an array. This:
     * <p>
     * <code>toHexString(b)</code>
     * <p>
     * is the same as this:
     * <p>
     * <code>toHexString(b, 0, b.length)</code>
     * <p>Does the inverse of {@link Bytes#fromHexString(byte[])}
     */
    public static String toHexString(byte[] b) {
        return toHexString(b, 0, b.length);
    }

    /**
     * TODO explanation
     * 
     * @throws IndexOutOfBoundsException
     *             if <code>start</code> or <code>start+length</code> is less
     *             than <code>b.length</code>
     * @throws NullPointerException if b is <code>null</code>
     */
    public static String toHexString(byte[] b, int start, int length) throws IndexOutOfBoundsException {
        return toHexString(b, start, length, 0, "");
    }
    
    /**
     * <p>This:
     * <p><code>AsciiFormatter.toHexString(b, true)</code>
     * <p>is the same as this:
     * <p><code>Bytes.toHexString(b, 2, " ")</code>
     */
	public static String toHexString(byte[] b, int groupSize, String delim) {
    	return toHexString(b, 0, b.length, groupSize, delim);
    }
    
	public static String toHexString(byte[] b, int start, int length,
			int groupSize, String delim) {
		if(b==null) return "";
    	int tmp = -1;
        StringBuffer buf = new StringBuffer(length);
        for(int i=start; i<start+length; i++){
            tmp = b[i] & 0xff;
            if(tmp < 0x10) buf.append("0");
            buf.append(Integer.toHexString(tmp));
			// Insert a delimiter only if we have a nonzero group size, non-null
			// delimiter, we're not at the end of the array, and it's time to
			// add one.
			// Note that comparisons are done with i+1 because we are doing
			// one-based indexing to make % happy)
			if (groupSize > 0 && delim != null && (i + 1) != start + length
					&& (i + 1) % groupSize == 0)
				buf.append(delim);
        }
        return buf.toString();
    }

	/**
	 * Creates a byte array from a given hex string. Assumes that each byte is
	 * represented by 2 hex digits. Removes any non-hex character before parsing
	 * <p>Does the inverse of {@link Bytes#toHexString(byte[])}
	 * 
	 * @param s
	 *            the string representation of a byte array, no delimiters, two
	 *            characters per byte
	 */
    public static byte[] fromHexString(String s) {
    	// Remove any delimiters, if present
    	if(!s.matches("^([0-9a-fA-F][0-9a-fA-F])+$")){
	    	StringBuffer buf = new StringBuffer(s.length());
	    	for (int i = 0; i < s.length(); i++) {
				if(Pattern.matches("^[0-9a-fA-F]$", s.subSequence(i, i+1)))
					buf.append(s.charAt(i));
			}
	    	s = buf.toString();
    	}
    	
    	byte[] ary = new byte[s.length()/2];
        for (int i = 0; i < ary.length; i++) {
            String tmp = s.substring(2*i, 2*i+2);
            ary[i] = (byte)Integer.parseInt(tmp,16);
        }
        return ary;
    }
}
