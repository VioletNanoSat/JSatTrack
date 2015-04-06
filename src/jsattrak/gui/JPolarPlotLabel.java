/*
 * Shawn E. Gano - Polar Plotting Label
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

package jsattrak.gui;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Arrays;

import javax.swing.JLabel;

import jsattrak.utilities.Arrays2d;
import name.gano.astro.coordinates.CoordinateConversion;

/**
 *
 * @author sgano
 */
public class JPolarPlotLabel extends JLabel implements Printable
{
	private static final long serialVersionUID = -1813208297851214233L;
	// aspect ratio of the image in the frame (should equal value in J2EarthPanel)
//	private double aspectRatio = 2.0;
//    private int imageWidth = 0, imageHeight = 0;
//    private int lastTotalWidth = 0, lastTotalHeight = 0;
    
    private int plotSizePadding = 5; // padding on sides
    
    private Color bgColor = Color.BLACK;
    private Color lineColor = Color.WHITE;
    private Color constraintElvColor = Color.GREEN;
    private Color currentLocColor = Color.ORANGE;
    
    private Color leadColor = Color.BLUE;
    private Color lagColor = Color.BLUE;
    
    
    private double[] elevationRange = new double[] {-90.0,90.0};
    private double elevationMarkInc = 30; // degrees should be evenly divisible by 90
    private double azimuthMarkInc = 22.5; // deg (should be evenly divisible by 180)
    
    private double[] currentAE = new double[] {0,90};
    
    /** rendering hints **/
    private transient RenderingHints renderHints;
    
    private boolean showLeadLagData = true;
    
    /** elevation constraint **/
    private double elevConst = 0; // deg
    
    
    /** lead lag data to plot (if not empty) **/
    private double[][] aerLead = null;
    private double[][] aerLag = null;
    
    /** display options **/
    private boolean displayTime = true;
    private boolean displayNames = false;
    private boolean useDarkColors = true;
    /** if polar plot is limited to horizon **/
    private boolean limit2Horizon = false; 
    /** use Compass directions **/
    private boolean useCompassPoints = true; 
    
    private String timeString = "";
    private String gs2SatNameString = "";
    
    public JPolarPlotLabel() {
        
        // create rendering options -- anti-aliasing
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        //this.setBackground(bgColor);
        
    } // constructor
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // repaints what is already on the buffer
        
        Dimension dim = getSize();
        int w = dim.width;
        int h = dim.height;
        
        // save last width/height
//        lastTotalWidth = w;
//        lastTotalHeight = h;
   
        // draw plot
        drawPolarPlot(g,w,h);
    } // draw
    
    // graphics, width, height
	private void drawPolarPlot(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D)g; // cast to a 2D graphics object
        
        // sent rendering options
        g2.setRenderingHints(renderHints);
        
        //g2.drawLine(0,0,w,h); // draw a line across the map
        int plotSize = min(h, w) - plotSizePadding*2; // size is smallest of width and height
        
        int[] plotCenter = new int[] {w/2,h/2}; // center of plot
        
        // set bg color 
        //g2.setBackground(bgColor);
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, w, h);  
        
        // set line color 
        g2.setPaint(lineColor);
        
        // line style
        g2.setStroke(new BasicStroke());  // standard line, no fancy stuff

        // draw plot region -----------------
        
        // draw outside circle
        g2.drawOval(plotCenter[0]-plotSize/2, plotCenter[1]-plotSize/2, plotSize, plotSize);
        
        // set line pattern to dotted
        // dashed line, 2 pix on 2 pix off
        float[] dashPattern = { 2F, 2F, 2F, 2F };
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        
        // draw elevation rings
        double elevationDegPerPixel = (elevationRange[1]-elevationRange[0])/(plotSize/2.);
        for(double elv=elevationRange[0];elv<=elevationRange[1];elv+=elevationMarkInc) {
            double radiusPix = (plotSize/2.)-(elv-elevationRange[0])/elevationDegPerPixel;
            
            g2.drawOval((int)(plotCenter[0]-radiusPix),(int)( plotCenter[1]-radiusPix), (int)(radiusPix*2), (int)(radiusPix*2));
            
            // add text along +x-axis
            g2.drawString(""+((int)elv), (int)(plotCenter[0]+radiusPix),plotCenter[1]+12);
        }
        
        // line style
        g2.setStroke(new BasicStroke());  // standard line, no fancy stuff
        // draw line at elevation constraint
        double radiusPix = (1.0-(elevConst-elevationRange[0])/(elevationRange[1]-elevationRange[0]))*(plotSize/2.0);
        g2.setPaint(constraintElvColor);
        g2.drawOval((int)(plotCenter[0]-radiusPix),(int)( plotCenter[1]-radiusPix), (int)(radiusPix*2), (int)(radiusPix*2));
        g2.setPaint(lineColor); // reset paint color
        
        // draw azimuth sectors
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        for (double az = 0; az < 360; az += azimuthMarkInc) {
            double endX = plotCenter[0] + (plotSize/2.)*sin(toRadians(az));
            double endY = plotCenter[1] - (plotSize/2.)*cos(toRadians(az));
            
            g2.drawLine(plotCenter[0], plotCenter[1], (int)endX, (int)endY);
            // draw Azimuth lables (numbers or compass points)
			if (!useCompassPoints) {
				g2.drawString("" + ((int) az), (int) endX + 2, (int) endY);
			} else {
				g2.drawString(CoordinateConversion.degrees2CompassPoints(az),
						(int) endX + 2, (int) endY);
			}
        }
        
        // end draw plot region ----------------------
        
        // display Time 
		if (displayTime) {
			g2.setPaint(lineColor);
			g2.drawString(timeString, 2, plotCenter[1] + plotSize / 2);
		}
        if (displayNames) {
			g2.setPaint(lineColor);
			g2.drawString(gs2SatNameString, 2, plotCenter[1] - plotSize / 2 + 12);
		}
        
        double x=0;
        double y=0;
        double r=0;
        
        // plot lead / lag data
		if (showLeadLagData) {
            plotLeadLag(g2, plotSize, plotCenter);
        } // plot lead / lag data

        // plot current point:
        // given currentAE, find x,y pixel location
        g2.setPaint(currentLocColor);
        r = (1. - (currentAE[1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2.0);

        x = plotCenter[0] + (r) * sin(currentAE[0] * PI / 180.);
        y = plotCenter[1] - (r) * cos(currentAE[0] * PI / 180.);

        g2.fillOval((int) (x - 5), (int) (y - 5), 10, 10);
        
    } // drawPolarPlot

	/**
	 * @param g2
	 * @param plotSize
	 * @param plotCenter
	 */
	private void plotLeadLag(Graphics2D g2, int plotSize, int[] plotCenter) {
		double x;
		double y;
		double r;
		int xOld = 0, yOld = 0;

		// plot lead data - dots
		g2.setStroke(new BasicStroke());  // standard line, no fancy stuff
		g2.setPaint(leadColor);

		if (aerLead != null) {
			for (int i = 0; i < aerLead.length; i++) {

		        r = (1.0 - (aerLead[i][1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2.0);

		        x = plotCenter[0] + (r) * sin(toRadians(aerLead[i][0]));
		        y = plotCenter[1] - (r) * cos(toRadians(aerLead[i][0]));

		        if (!Double.isNaN(aerLead[i][0])) {
					g2.fillOval((int) (x - 1), (int) (y - 1), 3, 3);

					// connect
					if (i > 0) {
						if (!(xOld == 0 && yOld == 0)) {
							g2.drawLine((int) x, (int) y, xOld, yOld);
						}
					}
				}
		        xOld = (int) x;
		        yOld = (int) y;
		    }
		}

		// plot lag data - dots
		g2.setPaint(lagColor);
		if (aerLag != null) {
		    for (int i = 0; i < aerLag.length; i++) {

		        r = (1.0 - (aerLag[i][1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2.0);

		        x = plotCenter[0] + (r) * sin(toRadians(aerLag[i][0]));
		        y = plotCenter[1] - (r) * cos(toRadians(aerLag[i][0]));

		        if (!Double.isNaN(aerLag[i][0])) {
		            g2.fillOval((int) (x - 1), (int) (y - 1), 3, 3);

		            // connect
					if (i > 0) {
		                if (!(xOld == 0 && yOld == 0)) {
							g2.drawLine((int) x, (int) y, xOld, yOld);
						}
		            }
		        }
		        xOld = (int) x;
		        yOld = (int) y;
		    }
		}
	}
	

    // SEG 16 jan 2009 -- adds print capibility
    /**
     * General printing method
     */
    public void print()
    {
        //--- Create a printerJob object
        PrinterJob printJob = PrinterJob.getPrinterJob();

        //--- Set the printable class to this one since we
        //--- are implementing the Printable interface
        printJob.setPrintable(this);

        //--- Show a print dialog to the user. If the user
        //--- click the print button, then print otherwise
        //--- cancel the print job
        if(printJob.printDialog())
        {
            try
            {
                printJob.print();
            }
            catch(Exception printException)
            {
                //printException.printStackTrace();
                System.out.println("ERROR printing polar plot: " + printException.toString());

            }
        }
    } // print

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
    {
        if(pageIndex > 0)
        {
            return NO_SUCH_PAGE; // plot is only on one page
        }

        int INCH = 72; // PageFormat uses 72dpi

        double marginInches = 1.0; // margines around the page

        int marginePixels = (int)round(INCH*marginInches);

        boolean darkColorVal = this.useDarkColors; // save this value
        setDarkColors(false); // set to light for printing
        // save other values - and turn them on for printing
        boolean distTimeSave = displayTime;
        boolean dispNameSave = displayNames;
        displayTime = true;
        displayNames = true;


        /* width and height of the imageable area */
        int iw = (int)pageFormat.getImageableWidth();
        int ih = (int)pageFormat.getImageableHeight();

//        // created image for the plot -- this method looks blurry
//        BufferedImage plotBuf = new BufferedImage(iw - 2*marginePixels, ih - 2*marginePixels, BufferedImage.TYPE_INT_RGB );
//        Graphics graphPrint = plotBuf.createGraphics();
//        // draw polar plot
//        drawPolarPlot(graphPrint, iw - 2*marginePixels, ih - 2*marginePixels); // with margines
//        graphics.drawImage(plotBuf, marginePixels,marginePixels, this);

        //drawPolarPlot(graphics, iw, ih); // full page

        // looks a lot sharper, with margines
        graphics.translate(marginePixels, marginePixels);
        drawPolarPlot(graphics, iw-2*marginePixels, ih-2*marginePixels);

        // return values back
        setDarkColors(darkColorVal);
        displayTime = distTimeSave;
        displayNames = dispNameSave;

        return PAGE_EXISTS;
    } // print

    public BufferedImage renderPolarPlotOffScreen(int width, int height)
    {
        BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB );
        drawPolarPlot(buff.getGraphics(), width, height);
        return buff;
    }
    
    public void update(String timeAsString, double[] aer,
            double elevationConst, String gsName, String satName) {
        setCurrentAE(aer);
        setElevConst(elevationConst);
        setTimeString(timeAsString);
        setGs2SatNameString(String.format("%s to %s", gsName.trim(), satName.trim()));
    }

    /**
     * Sets dark color set (or if false light color set - useful for printing)
     * @param useDarColors
     */
    public void setDarkColors(boolean useDarkColorsIn) {
        if (useDarkColors != useDarkColorsIn) {
            useDarkColors = useDarkColorsIn;
        } else {
            return; // nothing to do
        }

        if (useDarkColors) {
            bgColor = Color.BLACK;
            lineColor = Color.WHITE;
            constraintElvColor = Color.GREEN;
        } else {
            bgColor = Color.WHITE;
            lineColor = Color.BLACK;
            constraintElvColor = Color.GREEN;
        }

        // repaint
        this.repaint();
    } // setDarkColors
    
	/** deg (should be evenly divisible by 180) **/
    public double[] getCurrentAE() {
        return Arrays.copyOf(currentAE, currentAE.length);
    }

    public void setCurrentAE(double[] currentAE) {
        this.currentAE = Arrays.copyOf(currentAE, currentAE.length);
    }

    // TEMP -- swap this out with HASH !!!!!
    public double getElevConst() {
        return elevConst;
    }

    public void setElevConst(double elevConst) {
        this.elevConst = elevConst;
    }

    /** deg - lead lag data to plot (if not empty) */
    public double[][] getAerLead() {
        if(aerLead == null) return null;
        return Arrays2d.copyOf(aerLead, aerLead.length);
    }

    public void setAerLead(double[][] aerLead) {
        if(aerLead == null) {this.aerLead = null; return;}
        this.aerLead = Arrays2d.copyOf(aerLead, aerLead.length);
    }

    public double[][] getAerLag() {
        if(aerLag == null) return null;
        return Arrays2d.copyOf(aerLag, aerLag.length);
    }

    public void setAerLag(double[][] aerLag) {
        if (aerLag == null) {this.aerLag = aerLag; return;}
        this.aerLag = Arrays2d.copyOf(aerLag, aerLag.length);
    }

    // clears the data
    public void clearLeadLagData() {
        setAerLead(null);
        setAerLag(null);
    }

    public void resetCurrentPosition() {
        currentAE[0] = 0;
        currentAE[1] = 90;
    }

    public boolean isShowLeadLagData() {
        return showLeadLagData;
    }

    public void setShowLeadLagData(boolean showLeadLagData) {
        this.showLeadLagData = showLeadLagData;
    }

    public String getGs2SatNameString() {
        return gs2SatNameString;
    }

    public void setGs2SatNameString(String gs2SatNameString) {
        this.gs2SatNameString = gs2SatNameString;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public// display options
    boolean isDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(boolean displayTime) {
        this.displayTime = displayTime;
    }

    public boolean isDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(boolean displayNames) {
        this.displayNames = displayNames;
    }

    /**
     * @return the limit2Horizon
     */
    public boolean isLimit2Horizon() {
        return limit2Horizon;
    }

    /**
     * @param limit2Horizon
     *            the limit2Horizon to set
     */
    public void setLimit2Horizon(boolean limit2Horizon2) {
        if (limit2Horizon == limit2Horizon2) { return; }
        this.limit2Horizon = limit2Horizon2;

        if (limit2Horizon) {
            elevationRange[0] = 0; // = new double[] {-90.0,90.0};
            elevationRange[1] = 90;
        } else {
            elevationRange[0] = -90; // = new double[] {-90.0,90.0};
            elevationRange[1] = 90;
        }

        this.repaint();
    }

    /**
     * @return the useCompassPoints
     */
    public boolean isUseCompassPoints() {
        return useCompassPoints;
    }

    /**
     * @param useCompassPointsIn
     *            the useCompassPoints to set
     */
    public void setUseCompassPoints(boolean useCompassPointsIn) {
        if (useCompassPoints == useCompassPointsIn) { return; }

        this.useCompassPoints = useCompassPointsIn;
        this.repaint(); // repaint
    }
}
