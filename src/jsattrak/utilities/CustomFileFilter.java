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
package jsattrak.utilities;

import java.io.File;
import javax.swing.filechooser.*;
import static name.gano.file.FileUtilities.getExtension;

/**
 * ImageFilter.java is used to filter out only image files in Save As dialog
 */
public class CustomFileFilter extends FileFilter implements
        java.io.Serializable {
	
    private static final long serialVersionUID = 3924520389614797738L;
    private String fileExtension;
    private String description;
    
    public CustomFileFilter(String fileExtension, String description) {
    	this.fileExtension = fileExtension;
    	this.description = description;
    }

    /**
     * Accept all directories and all gif, jpg, tiff, or png files.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }

        String extension = getExtension(f);
        if (extension != null) {
            return extension.equalsIgnoreCase(fileExtension); 
            } 

        return false;
    }

    /**
     * The description of this filter
     */
    public String getDescription() {
        return description;
    }
    
}
