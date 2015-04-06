/*
 * SaveImageFile.java
 * Utility class to help save images to a file -- lets you specify compression if the format supports it
 * 
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

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import static name.gano.file.FileUtilities.getExtension;

/**
 * Utility class to help save images to a file -- lets you specify compression
 * if the format supports it
 * 
 * @author Shawn Gano, 13 November 2008
 * @author Nate Parsons nsp25
 */
public class SaveImageFile {
    public static final float DEFAULT_COMPRESSION = 0.75f;

    /**
     * saves image
     * 
     * @param format
     *            image type, e.g.: jpg, jpeg, gif, png
     * @param file
     *            what to save the image as
     * @param buff
     *            image
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void saveImage(String format, File file, BufferedImage buff)
            throws FileNotFoundException, IOException {
        saveImage(format, file, buff, SaveImageFile.DEFAULT_COMPRESSION);
    }

    /**
     * saves image
     * 
     * @param format
     *            image type, e.g.: jpg, jpeg, gif, png
     * @param file
     *            what to save the image as
     * @param buff
     *            image
     * @param compressionQuality
     *            0.0f-1.0f , 1 = best quality
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void saveImage(String format, File file, BufferedImage buff,
            float compressionQuality) throws FileNotFoundException, IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        // instantiate an ImageWriteParam object w/ default compression options
        ImageWriteParam iwp = writer.getDefaultWriteParam();

        if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("JPEG")) {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(compressionQuality);
        }

        // write file
        FileImageOutputStream output = new FileImageOutputStream(file);
        writer.setOutput(output);
        IIOImage image = new IIOImage(buff, null, null);
        writer.write(null, image, iwp);
        output.close(); // Fixed SEG - 22 Dec 2008
    }

    /**
     * Open a save dialog for the specified image and types
     */
    public static void saveImageAsType(Component parent, BufferedImage image,
            String... mTypes) throws FileNotFoundException, IOException {
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();
        List<FileFilter> filters = new ArrayList<FileFilter>(mTypes.length);
        for (String type : mTypes) {
            FileFilter filter = new FileNameExtensionFilter(type, type);
            filters.add(filter);
            fc.addChoosableFileFilter(filter);
        }
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            String extension = getExtension(file);
            if (extension == null) { // We add an extension based on type
                extension = fc.getFileFilter().getDescription();
                file = new File(file.getAbsolutePath() + "." + extension);
            }

            SaveImageFile.saveImage(extension, file, image, 0.9f);
        }
    }
}
