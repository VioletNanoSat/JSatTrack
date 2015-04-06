package name.gano.file;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class FileUtilitiesTest {

    @Test
    public void testGetExtension() {
        File f = new File("test.txt");
        assertEquals("txt", FileUtilities.getExtension(f));
    }

    @Test
    public void testDeleteDirectoryString() {
        File root = new File("a");
        if(!root.exists())assertTrue(root.mkdir());
        File other = new File(root, String.format("b%sc%s",File.separator, File.separator));
        if(!other.exists())assertTrue(other.mkdirs());
        assertTrue(FileUtilities.deleteDirectory(root.getAbsolutePath()));
        assertTrue(!other.exists());
        assertTrue(!root.exists());
    }
}
