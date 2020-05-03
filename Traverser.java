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
  * Traverses the image three times, and sets record equal to the record with the most cells.
  */
  public void getBestTraversal () {
    traverse ();
    Record record1 = this.record;
    this.x = traverseDistance;
    this.y = traverseDistance;
    int size1 = record1.size();
    IJ.showMessage("Finished Traversal 1: " + size1 + " cells");
    Record emptyRecord1 = new Record();
    traverse ();
    Record record2 = this.record;
    this.x = traverseDistance;
    this.y = traverseDistance;
    int size2 = record2.size();
    IJ.showMessage("Finished Traversal 2: " + size2 + " cells");
    Record emptyRecord2 = new Record();
    traverse ();
    Record record3 = this.record;
    this.x = traverseDistance;
    this.y = traverseDistance;
    int size3 = record3.size();
    IJ.showMessage("Finished Traversal 3: " + size3 + " cells");
    Record bigRecord;
    if (size3 >= size2 && size3 >= size1){
      bigRecord = record3;
    }
    else if (size2 >= size3 && size2 >= size1){
      bigRecord = record2;
    }
    else{
      bigRecord = record1;
    }
    this.record = bigRecord;
    drawAllCells();
  }

  /**
  * Traverses the image, adding new cells to the record class when appropriate.
  */
  public void traverse () {
    while (this.y < this.height) {
      // IJ.log("Checking (" + Integer.toString(this.x) + "," + Integer.toString(this.y) + ")");
      // IJ.log("CHANGES");
      traverseOnce();
    }
  }

  /**
  * Traverses to the next point in the image.
  * If the point is inside a new, valid cell, a new cell is created and added to the record class.
  */
  public void traverseOnce () {
    int recorded = isRecorded();
    if (DEBUG) IJ.log("(" + this.x + "," + this.y + ") is contained within cell " + recorded);
    if (recorded == -1) {
      Wand wand = doWand(x, y, TOLERANCE);
    }
    else{
      if (DEBUG) IJ.log("(" + this.x + "," + this.y + ") is contained within cell " + recorded);
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

  public void drawCell(Cell cell){
    Cell c = new Cell(cell.getShape().xpoints, cell.getShape().ypoints, cell.getstartx(), cell.getstarty(), cell.getcellNum());
    Polygon p = c.getShape();
    double mag = ic.getMagnification();

    String size = Integer.toString(c.getcellNum());
    int mx = (int) (mag * c.getstartx());
    int my = (int) (mag * c.getstarty());
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

  //This method draws every cell in the record
  public void drawAllCells(){
    this.g = ic.getGraphics();
    g.setColor(Color.CYAN);
    ic.update(g);
    Cell c = record.cells.get(0);
    for(int i = 0; i < record.size(); i++){
      c = record.cells.get(i);
      drawCell(c);
    }
  }

  //didn't work :(
  // public void finalize(){
  //   ic.paint(g);
  // }

  //This method draws every cell in the given cellsToBeDeleted
  public void drawDeletedCells(ArrayList<Cell> deletedCells){
    g.setColor(Color.YELLOW);
    Cell c = record.cells.get(0);
    for(int i = 0; i < deletedCells.size(); i++){
      c = deletedCells.get(i);
      drawCell(c);
    }
    g.setColor(Color.CYAN);
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
//delete
  public void print4(Cell c) {
    for (int i = 0; i < 4; i++) {
      if (DEBUG) IJ.log(Integer.toString(c.getShape().xpoints[i]));
    }
  }

  /** Adapted from doWand method in ImageJ. Changed to return a wand rather than an int */
  public Wand doWand(int x, int y, double tolerance) {

    if (DEBUG) IJ.log("\nPossible new cell..");
    int imode = Wand.LEGACY_MODE;
    boolean smooth = false;
    Wand w = new Wand(ip);
    double t1 = ip.getMinThreshold();
    if (t1==ImageProcessor.NO_THRESHOLD || (ip.getLutUpdateMode()==ImageProcessor.NO_LUT_UPDATE&& tolerance>0.0)) {
      w.autoOutline(x, y, tolerance, imode);
    }

    else w.autoOutline(x, y, t1, ip.getMaxThreshold(), imode);
    //If there is no cell here, but the outline we find is too small, ignore it


    int[] xpoints = w.xpoints;
    int[] ypoints = w.ypoints;
    Cell c = new Cell(xpoints, ypoints, x, y, 0);


    if (w.npoints>minDiameter && !isEdgeCell(c) && cellBoundsSmallEnough(c) && !record.arraySharesPoints(c) && !record.sameCenterPoints(c)) {

      addCell(c);

      int eq = record.equals();

      drawCell(c);

      if (DEBUG) {
        IJ.showMessage("Added cell #" + c.getcellNum() + " based on point= " + c.getstartx() + "," + c.getstarty());
        IJ.log("Added cell #" + c.getcellNum() + " based on point= " + c.getstartx() + "," + c.getstarty());
        IJ.log("" + c.toString());
        IJ.log("Roundness: " + c.calcRoundness());
        IJ.log("Area: " + c.getArea());
        IJ.log("\n");
      }
     }
    else{
      if (DEBUG) IJ.log("Outline based on " + this.x + "," + this.y + " had too few points defining its outline\n");
    }

    return w;
  }
}
