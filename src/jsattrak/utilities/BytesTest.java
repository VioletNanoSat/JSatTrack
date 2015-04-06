package jsattrak.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static jsattrak.utilities.Bytes.toShort;
import static jsattrak.utilities.Bytes.toUShort;
import static jsattrak.utilities.Bytes.toHexString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BytesTest {

    byte[] barray;
    byte[] barray2;
    byte[] ttiArray;
    
    @Before
    protected void setUp() throws Exception {
        barray = new byte[]{0,1,2,3,4,5,6,7, 8};
        barray2 = new byte[]{0,1,2,3,4,5,6,7};
        ttiArray = new byte[]{(byte)0xEB,0,0,0,0,0,0,0};
    }
    
    @After
    public void tearDown(){
        barray = null;
    }
	
    public void testAscii(){
    	String t1 = "Test\r\n";
    	assertEquals(Bytes.toAscii(Bytes.fromAscii(t1)), t1);
    }
	
    @Test
    public void testToShort() {
       	assertEquals(toShort(barray, 0, false), 1);
        assertEquals(toShort(barray, 0, true), 256);
    }

    @Test
    public void testToUShort() {
        assertEquals(toUShort(barray, 4, false), 256 * 4 + 5);
        assertEquals(toUShort(barray, 7, true), 256 * 8 + 7);
    }

    @Test
    public void testToInt() {
    	System.out.println(Bytes.toInt(ttiArray, 0, true));
    	assertEquals(Bytes.toInt(ttiArray, 0, true), 0xEB);
    	assertEquals(Bytes.toInt(ttiArray, 0, false), 0xEB000000);
        fail("Not completely implemented"); // TODO
    }

    @Test
    public void testToLong() {
    	assertEquals(barray2, Bytes.toArray(Bytes.toLong(barray2, 0, false), false));//goes out of range
    	assertEquals(barray2, Bytes.toArray(Bytes.toLong(barray2, 0, true), true));
    }

    @Test
    public void testToArrayIntBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testToArrayShortBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testLongTo4ByteArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testCopy() {
        byte[] bcopy = new byte[barray.length];
        for (int i = 0; i < bcopy.length; i++)
            bcopy[i] = barray[i];
        // byte[] test = Bytes.copy(barray, 0, 3, false);
        fail("Not yet implemented");
    }

    @Test
    public void testNewByteArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testMain() {
        fail("Not yet implemented");
    }

    @Test
    public void testFromByteArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testToHexStringIntBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testToHexStringShortBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testToHexStringByteArrayIntInt() {
        assertEquals("000102030405060708",
                toHexString(barray, 0, barray.length));
    }
}