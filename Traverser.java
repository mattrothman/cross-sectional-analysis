import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.filter.Analyzer;
import ij.gui.Wand;
import ij.plugin.frame.PlugInFrame;
import ij.measure.*;

import ij.text.*;
import ij.io.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.util.Tools;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.ThresholdAdjuster;
import ij.macro.Interpreter;
import ij.macro.MacroRunner;
import ij.Macro;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.measure.Measurements;
import java.text.*;
import java.applet.Applet;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;
import java.security.KeyStore;
import java.nio.ByteBuffer;

//for doWand
import ij.macro.MacroRunner;
import ij.plugin.BatchProcessor;

/**
* The traverser class traverses an image, from left to right,
* identifying unique cells, and adding them to the Record.
*
* @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
*/

//look into do-wand wand auto-line tolerence 19


//Should it: extends PlugInFrame implements Measurements ??
public class Traverser {
  private Wand wand;
  private int traverseDistance; //The distance between in pixels between calls of traverseOnce()
  private int x, y; //The location (x,y coordinate) of the current pixel
  private int minDiameter; //holds the minimum diameter of a cell. In other words, how big of a diameter must be able to fit somewhere in the cell outline
  private ImagePlus imp;
  private int width; //The width of image
  private int height; //The heght of image
  private double TOLERANCE = 19.0;
  private Record record;
  private ImageProcessor ip;
  private ImageCanvas ic;
  private Graphics g;

  //Stolen code and comment, but a debug mode is probably a very good idea ???
  // by declaring this static final, we allow javac to perform the test
  // at compile time rather than runtime, and remove debug code when
  // debug is false. thus there is *no* performance hit in non-debug mode
  // with the disadvantage that we cannot switch between debug and
  // release without a recompile.
  private static final boolean DEBUG = false;
  public final static int LEGACY_MODE = 1;

  /**
  * Creates a new Traverser
  */
  public Traverser(ImagePlus image, ImageProcessor ip, int minDiameter, int traverseDistance, Record record) {
    if (DEBUG) IJ.log("Traverser being constructed...");
    this.imp = image;
    this.minDiameter = minDiameter;
    this.traverseDistance = traverseDistance;
    this.record = record;
    this.x = traverseDistance;
    this.y = traverseDistance;
    this.height = image.getHeight();
    this.width = image.getWidth();
    this.ip = ip;
    this.ic = image.getCanvas();
    this.g = ic.getGraphics();
    g.setColor(Color.CYAN);
  }

  /**
  * Traverses the image, adding new cells to the record class when appropriate.
  */
  public void traverse () {
    for(int i = 0; i <20; i++){
    //while (this.y < this.height) {
      IJ.log("Checking (" + Integer.toString(this.x) + "," + Integer.toString(this.y) + ")");
      traverseOnce();
    }
    // IJ.showMessage("Cells in record: " + record.size());
    // drawLabel("(100,100)"  , 100, 100);
		// String cellNum = Integer.toString(record.whichCell(100, 100));
		// IJ.showMessage("Is there a cell at (100, 100): #" + cellNum);
    // drawLabel("(200,100)"  , 200, 100);
		// cellNum = Integer.toString(record.whichCell(200, 100));
		// IJ.showMessage("Is there a cell at (200, 100): #" + cellNum);
    // drawLabel("(300,100)"  , 300, 100);
		// cellNum = Integer.toString(record.whichCell(300, 100));
		// IJ.showMessage("Is there a cell at (300, 100): #" + cellNum);
    // drawLabel("(400,100)"  , 400, 100);
		// cellNum = Integer.toString(record.whichCell(400, 100));
		// IJ.showMessage("Is there a cell at (400,100): #" + cellNum);
    // drawLabel("(500,100)"  , 500, 100);
		// cellNum = Integer.toString(record.whichCell(500, 100));
		// IJ.showMessage("Is there a cell at (500,100): #" + cellNum);
    // drawLabel("(600,100)"  , 600, 100);
		// cellNum = Integer.toString(record.whichCell(600, 100));
		// IJ.showMessage("Is there a cell at (600,100): #" + cellNum);
    // drawLabel("(700,100)"  , 700, 100);
		// cellNum = Integer.toString(record.whichCell(700, 100));
		// IJ.showMessage("Is there a cell at (700,100): #" + cellNum);
    // drawLabel("(800,100)"  , 800, 100);
		// cellNum = Integer.toString(record.whichCell(800, 100));
		// IJ.showMessage("Is there a cell at (800,100): #" + cellNum);
  }

  /**
  * Traverses to the next point in the image.
  * If the point is inside a new, valid cell, a new cell is created and added to the record class.
  */
  public void traverseOnce () {
    int recorded = isRecorded();
    // IJ.log("(" + this.x + "," + this.y + ") is contained within cell " + recorded);
    //Wand wand = doWand(x, y, TOLERANCE);
    if (recorded == -1) {
      Wand wand = doWand(x, y, TOLERANCE);
      // int[] xpoints = wand.xpoints;
      // int[] ypoints = wand.ypoints;
      // //if (checkDiameter()) {
      // addCell(xpoints, ypoints, x, y); // until we find a way to check the diameter we should keep this commented out
      // IJ.log("Cell #" + record.size() + " based on point= " + x + "," + y);
      //}
    }
    else{
      IJ.log("(" + this.x + "," + this.y + ") is contained within cell " + recorded);
    }
    nextPoint();
  }

  /**
  * Checks whether the next point is already inside of a recorded cell.
  * @return Whether or not the next point is already inside of a recorded cell
  * OR
  * @return Either the number of cell that the next point is inside,
  * or -1 if the next point is not already inside of a recorded cell.
  */
  public int isRecorded () { //OR public int isRecorded(){
    return record.whichCell(this.x, this.y);
  }

  /**
  * Checks whether the polygon shares >= 20% of its points with any cell in record.
  * @return whether the polygon shares >= 20% of its points with any cell in record.
  */
  public int isRedundant (int[] xpoints, int[] ypoints, int npoints) {
    Polygon p = new Polygon(xpoints, ypoints, npoints);
    return record.cellOutlineOverlaps(p);
  }

  //ABOUT checkDiameter:
  //should any cell with an insufficient diameteer stil be added to record so that this process is not repeated?
  //We could mark these cells so that at the end of the process, they are deleted
  /**
  * Checks whether a new cell is large enough to be considered a cell.
  * @return whether a new cell is large enough to be considered a cell
  */
  public boolean checkDiameter () {
    //TODO: Calculate the maximum diameter of the cell
    int maxDiameter = 0;
    if (maxDiameter >= minDiameter) {
      return true;
    }
    return false;
  }

  /**
  * Creates a new Cell object and adds it to Record class
  * @param   xpoints  The x coordinates of the points that outline the cell
  * @param   ypoints  The y coordinates of the points that outline the cell
  */
  public void addCell ( int[] xpoints, int[] ypoints, int startx, int starty){
    record.addCell(xpoints, ypoints, startx, starty);
  }

  /**
  * Calculates the nextPoint to be traversed to, and updates location or x and y
  */
  public void nextPoint () {
    int nextX = this.x + this.traverseDistance;
    if (nextX > this.width) {
      nextX = this.traverseDistance;
      this.y = this.y + this.traverseDistance;
    }
    this.x = nextX;
  }

  //Code source: https://introcs.cs.princeton.edu/java/35purple/Polygon.java.html
  //Currently just returns a point vaguely near the centroid of the polygon, hopefully inside it
  public Point getCentroid(Polygon p) {
    Rectangle r = p.getBounds();
    double cx = r.x + 0.5*this.width;
    double cy = r.y + 0.5*this.height;
    // int[] xpoints = p.xpoints;
    // int[] ypoints = p.ypoints;
    // double cx = 0, cy = 0;
    // for (int i = 0; i < p.npoints; i++) {
    // 	cx = cx + (xpoints[i] + xpoints[i+1]) * (ypoints[i] * xpoints[i+1] - xpoints[i] * ypoints[i+1]);
    // 	cy = cy + (ypoints[i] + ypoints[i+1]) * (ypoints[i] * xpoints[i+1] - xpoints[i] * ypoints[i+1]);
    // }
    // cx /= (6 * calculateArea(p));
    // cy /= (6 * calculateArea(p));
    int x = (int) cx;
    int y = (int) cy;
    return new Point(x, y);
  }

  public void drawCell(Cell c){
    Polygon p = c.getShape();
    double mag = ic.getMagnification();

    String size = Integer.toString(c.getcellNum());
    int mx = (int) (mag * c.getstartx());
    int my = (int) (mag * c.getstarty());
    g.drawString(size, mx, my);

    //Adjust polygon outline to image magnification
    // int end = p.npoints;
    if (mag!=1.0){
      for (int i=0; i < p.npoints; i++) {
        p.xpoints[i] = (int) (mag * p.xpoints[i]);
        p.ypoints[i] = (int) (mag * p.ypoints[i]);
        // if ((p.xpoints[i] == 0) && (p.ypoints[i] == 0)){
        //   end = i;
        //   break;
        // }
      }
    }

    // int[] xp = Arrays.copyOfRange(p.xpoints, 0, end);
    // int[] yp = Arrays.copyOfRange(p.ypoints, 0, end);
    // int np = xp.length;
    // int n = np - 1;
    g.drawPolygon(p.xpoints, p.ypoints, p.npoints);

    // //Let's figure out what's going on with that line
    // g.setColor(Color.MAGENTA);
    // // g.drawLine(p.xpoints[0], p.ypoints[0], 0, 0);

    //IJ.log("x[0], y[0] = " + xp[0] + "," + yp[0] + "  x[1], y[1] = " + xp[1] + "," + yp[1] + "\nx[n], y[n] = " + xp[n] + "," + Integer.toString(yp[n]) );
    //IJ.log("xpoints: " + Arrays.toString(xp));
    //IJ.log("\n\nypoints: " + Arrays.toString(yp));
    // g.setColor(Color.CYAN);

  }

  public void drawLabel(String st, int x, int y){
    double mag = ic.getMagnification();
    int mx = (int) (mag * x);
    int my = (int) (mag * y);
    g.drawString(st, mx, my);
  }

  //Returns true if the height of the bounding rectangle of the cell is less than 1/3 the image Height
  //And if the width of the bounding rectangle of the cell is less than 1/3 the image width
  public Boolean cellBoundsSmallEnough(Cell c){
    Rectangle bounds = c.getShape().getBounds();
    int h = (int) bounds.getHeight();
    int w = (int) bounds.getWidth();
    if((h > this.height/3) && (w > this.width/3)){
      return false;
    }
    return true;
  }

  /** Adapted from doWand method in ImageJ. Changed to return a wand rather than an int */
  public Wand doWand(int x, int y, double tolerance) {
    IJ.log("\nPossible new cell..");
    int imode = Wand.LEGACY_MODE;
    boolean smooth = false;
    Wand w = new Wand(ip);

    double t1 = ip.getMinThreshold();
    if (t1==ImageProcessor.NO_THRESHOLD || (ip.getLutUpdateMode()==ImageProcessor.NO_LUT_UPDATE&& tolerance>0.0)) {
      w.autoOutline(x, y, tolerance, imode);
    }
    else w.autoOutline(x, y, t1, ip.getMaxThreshold(), imode);
    //If there is no cell here, but the outline we find is too small, ignore it
    if (w.npoints>200) { //I raised the standard from 0 to 400
      int[] xpoints = w.xpoints;
      int[] ypoints = w.ypoints;
      int redundant = isRedundant(xpoints, ypoints, xpoints.length);

      // Everything is
      // if(redundant != -1){
      //   IJ.log("Outline based on " + this.x + "," + this.y + " overlapped with cell " + redundant + "\n");
      //   return w;
      // }

      addCell(xpoints, ypoints, x, y);
      Cell c = record.getLastCell();

      // if(!c.roundness()){
      //   IJ.log("Outline based on " + this.x + "," + this.y + " had insufficient roundness\n");
      //   record.removeLastCell();
      //   return w;
      // }
      if(!cellBoundsSmallEnough(c)){
        record.removeLastCell();
        IJ.log("Cell had bounds that were too large.");
      }
      int eq = record.equals();
      // if(eq != -1){
      //   IJ.log("Cell based on " + this.x + "," + this.y + " was equal to cell #" + eq +"\n");
      //   record.removeLastCell();
      //   return w;
      // }
      IJ.log("Added cell #" + c.getcellNum() + " based on point= " + c.getstartx() + "," + c.getstarty());
      IJ.log("Roundness: " + c.calcRoundness());
      IJ.log("Area: " + c.getArea());
      if(redundant != -1) IJ.log("Outline overlapped with cell" + redundant);
      if(eq != -1) IJ.log("Cell was equal to cell #" + eq);
      IJ.log("" + c.toString());
      IJ.log("\n");


      drawCell(c);

      IJ.showMessage("Added cell #" + c.getcellNum() + " based on point= " + c.getstartx() + "," + c.getstarty());
    }
    else{
      IJ.log("Outline based on " + this.x + "," + this.y + " had too few points defining its outline\n");
    }
    return w;
  }
}
