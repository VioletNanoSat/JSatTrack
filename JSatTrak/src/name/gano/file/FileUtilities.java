/**
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 *
 * This file is part of JSatTrak.
 *
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */

package name.gano.file;

import java.io.File;

/**
 *
 * @author sgano
 */
public class FileUtilities
{

    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    } // getExtension

    /**
     * Recursively deletes a directory
     * 
     * @return <code>true</code> if successful
     */
    static public boolean deleteDirectory(String path2Dir) {
        return deleteDirectory(new File(path2Dir));
    } // deleteDirectory

    /**
     * Recursively deletes a directory
     * 
     * @return <code>true</code> if successful
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.exists() && dir.isDirectory())
            for (File child : dir.listFiles())
                if (!deleteDirectory(child.getAbsolutePath())) return false;
        return dir.delete();
    }

}
