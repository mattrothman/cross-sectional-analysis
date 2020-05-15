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

public class Traverser {
  private Wand wand;
  private int traverseDistance; //The distance between in pixels between calls of traverseOnce()
  private int x, y; //The location (x,y coordinate) of the current pixel
  private double minArea; //holds the minimum diameter of a cell. In other words, how big of a diameter must be able to fit somewhere in the cell outline
  private ImagePlus imp;
  private int width; //The width of image
  private int height; //The heght of image
  private double TOLERANCE = 19.0;
  private Record record;
  private ImageProcessor ip;
  private ImageCanvas ic;
  private Graphics g;

  //Stolen code and comment:
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
  public Traverser(ImagePlus image, ImageProcessor ip, double minArea, int traverseDistance, Record record) {
    if (DEBUG) IJ.log("Traverser being constructed...");
    this.imp = image;
    this.minArea = minArea;
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
    g.setColor(Color.CYAN);
    ic.update(g);
    while (this.y < this.height) {
      traverseOnce();
    }
  }

  /**
  * Traverses to the next point in the image, and calls doWand().
  * If the point is inside a new, valid cell, a new cell is created and added to the record class.
  */
  public void traverseOnce () {
    doWand(x, y, TOLERANCE);
    nextPoint();
  }

  /**
  * Creates a new Cell object and adds it to Record class
  * @param   xpoints  The x coordinates of the points that outline the cell
  * @param   ypoints  The y coordinates of the points that outline the cell
  */
  public void addCell (Cell c){
    record.addCell(c);
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

  /**
   * Draws the given cell
   * @param cell The cell to be drawn
   */
  public void drawCell(Cell cell){
    Cell c = new Cell(cell.getShape().xpoints, cell.getShape().ypoints, cell.getcellNum());
    Polygon p = c.getShape();
    double mag = ic.getMagnification();

    String size = Integer.toString(c.getcellNum());
    int mx = (int) (mag * c.getcenterX());
    int my = (int) (mag * c.getcenterY());
    g.drawString(size, mx, my);

    //Adjust polygon outline to image magnification
    if (mag!=1.0){
      for (int i=0; i < p.npoints; i++) {
        p.xpoints[i] = (int) (mag * p.xpoints[i]);
        p.ypoints[i] = (int) (mag * p.ypoints[i]);
      }
    }

    g.drawPolygon(p.xpoints, p.ypoints, p.npoints);
  }

  /**
	 * Draws every cell in the record in cyan
	 */
  public void drawAllCells(){
    this.g = ic.getGraphics();
    g.setColor(Color.CYAN);
    ic.update(g);
    Cell c;
    for(int i = 0; i < record.size(); i++) {
      c = record.cells.get(i);
      drawCell(c);
    }
  }

  /**
	 * Draws every cell in the given arraylist of cells in yellow
   * @param deletedCells the given arraylist of cells
	 */
  public void drawDeletedCells(ArrayList<Cell> deletedCells){
    g.setColor(Color.YELLOW);
    Cell c = record.cells.get(0);
    for(int i = 0; i < deletedCells.size(); i++){
      c = deletedCells.get(i);
      drawCell(c);
    }
    g.setColor(Color.CYAN);
  }

  /**
	 * Determines whether the bounding rectangle around a cell is small enough.
   * This method is meant to help doWand avoid large, thin, cells that can occur when doWand clicks on the cell border.
   * Returns true if the height of the bounding rectangle of the cell is less than 1/3 the image Height
   * And if the width of the bounding rectangle of the cell is less than 1/3 the image width
   * @param c the cell whose bounding box is being checked
   * @return whether the bounding rectangle around a cell is small enough
	 */
  private Boolean cellBoundsSmallEnough(Cell c){
    Rectangle bounds = c.getShape().getBounds();
    int h = (int) bounds.getHeight();
    int w = (int) bounds.getWidth();
    if((h > this.height/3) && (w > this.width/3)){
      return false;
    }
    return true;
  }

  /**
	 * Determines whether the given cell is an edge cell
   * @param c the cell being checked
   * @return whether the given cell is an edge cell
	 */
  public boolean isEdgeCell(Cell c){
      int[] xpoints = c.getShape().xpoints;
      int[] ypoints = c.getShape().ypoints;
      for(int i = 0; i < xpoints.length; i++){
          if (xpoints[i] == 0 || ypoints[i] == 0 || xpoints[i] == width || ypoints[i] == height) {
              if (DEBUG) IJ.log("EDGE CELL");
              if (DEBUG) IJ.log("FINSISHED");
              return true;
          }
      }
      if (DEBUG) IJ.log("FINSISHED");
      return false;
  }

  /**
   * Adapted from doWand method in ImageJ
	 * This method calls the built-in wand tracing tool at the given (x,y) coordinates, at the given tolerance,
   * and makes that polygon into a cell. If that cell is not too large or small to be a cell, is not an edge cell,
   * and is a unique cell, it is added to the record and drawn.
   * @param x the x coordinate at which the wand is called
   * @param y the y coordinate at which the wand is called
   * @param tolerance the tolerance for the wand
	 */
  public void doWand(int x, int y, double tolerance) {

    if (DEBUG) IJ.log("\nPossible new cell..");
    int imode = Wand.LEGACY_MODE;
    Wand w = new Wand(ip);
    double t1 = ip.getMinThreshold();
    if (t1==ImageProcessor.NO_THRESHOLD || (ip.getLutUpdateMode()==ImageProcessor.NO_LUT_UPDATE&& tolerance>0.0)) {
      w.autoOutline(x, y, tolerance, imode);
    }
    else w.autoOutline(x, y, t1, ip.getMaxThreshold(), imode);

    int[] xpoints = w.xpoints;
    int[] ypoints = w.ypoints;
    Cell c = new Cell(xpoints, ypoints, 0);

    if (c.getArea() > minArea && !isEdgeCell(c) && cellBoundsSmallEnough(c) && !record.cellAlreadyExists(c) && !record.sameCenterPoints(c)) {
      addCell(c);
      drawCell(c);

      if (DEBUG) {
        IJ.showMessage("Added cell #" + c.getcellNum() + " based on point= " + c.getcenterX() + "," + c.getcenterY());
        IJ.log("Added cell #" + c.getcellNum() + " based on point= " + c.getcenterX() + "," + c.getcenterY());
        IJ.log("" + c.toString());
        IJ.log("Area: " + c.getArea());
        IJ.log("\n");
      }
     }

    else{
      if (DEBUG) IJ.log("Outline based on " + this.x + "," + this.y + " had too few points defining its outline\n");
    }
    return;
  }
}
