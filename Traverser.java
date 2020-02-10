//Create Traverser class that traverses image, from left to right, searching for cells

/**

Variables:
    clickDistance //holds the distance between clicks (pixels)
    location //holds the current location (x,y coordinate) of where it is in the image
    imageBoundary //holds the image boundary coordinates
    minDiameter //holds the minimum diameter of a cell. In otherwards, how big of a diameter must be able to fit somewhere in the cell outline
    image //holds the cell image

Methods:

    traverse //traverses to the next point in the image (calls isNextEdge, if the next right is past the edge, traverses down),
                checks if the coordinate is already inside an existing cell, checks diameter, if the cell does
                not already exist and the maximum diameter is acceptable then calls click which uses ImageJ's
                wand, then calls addCell

                wand source code: https://github.com/imagej/imagej1/blob/master/ij/gui/Wand.java

    isRecorded //uses the Record class to check if a cell already exists in that coordinate. Calls the inside method of each recorded cell

    checkDiameter //checks to see if the maximum diameter is acceptable using the minDiameter variable

    addCell //creates a new Cell object and adds it to Record class

    isNextEdge //determines whether the next right click will be off the edge of the image



*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;
//To learn about analyzer, read:
//https://imagej.nih.gov/ij/docs/menus/analyze.html
//https://github.com/imagej/imagej1/blob/850870a50df7fe81f3cd47494a6ea46164bfcec8/ij/plugin/filter/Analyzer.java
import ij.plugin.filter.Analyzer;
import ij.gui.Wand;
import ij.plugin.frame.PlugInFrame;
import ij.measure.*;

/**
* The traverser class traverses an image, from left to right,
* identifying unique cells, and adding them to the Record.
*
* @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
*/

//look into do-wand wand auto-line tolerence 19


//Should it: extends PlugInFrame implements Measurements ??
public class Traverser {
  Wand wand;
  int traverseDistance; //The distance between in pixels between calls of traverseOnce()
  int x, y; //The location (x,y coordinate) of the current pixel
  int minDiameter; //holds the minimum diameter of a cell. In other words, how big of a diameter must be able to fit somewhere in the cell outline
  ImagePlus image;
  int width; //The width of image
  int height; //The heght of image
  Record record;

  //Do we need these things? Let's find out!
  // int width;
  // int height;
  // Roi roi;
  // ImageStack stack;
  // ImageStatistics stats;

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
  public Traverser(ImagePlus image, Wand wand, int minDiameter, int traverseDistance, Record record) {
    if ( DEBUG ) IJ.log("Traverser being constructed...");
    this.image = image;
    this.minDiameter = minDiameter;
    this.traverseDistance = traverseDistance;
    this.record = record;
    this.wand = wand;
    this.x = traverseDistance;
    this.y = traverseDistance;

    //I don't know if we need this code, but I think we should leave it here for now...
    // this.stack = image.getStack();
    // //Is this true for us?
    // if (stack == null) {
    //   IJ.error("This plugin only works on image stacks, not single frames.");
    //   return;
    // }
    // this.roi = image.getRoi();
    // if (roi == null) { // ensure there is always some ROI.
    //     roi = new Roi(0, 0, image.getWidth(), image.getHeight(), image);
    //     image.setRoi(roi);
    // }

    int measurements = Analyzer.getMeasurements();
    Analyzer.setMeasurements(measurements);

    // this.stats = image.getStatistics(measurements);
    // this.width = image.getWidth();
    // this.height = image.getHeight();

  }

  /**
  * Traverses the image, adding new cells to the record class when appropriate.
  */
  public void traverse(){
    while (this.y < this.height){
      traverseOnce();
    }
  }

  /**
  * Traverses to the next point in the image.
  * If the point is inside a new, valid cell, a new cell is created and added to the record class.
  */
  public void traverseOnce(){
    if (!isRecorded()) {
      //doWand(x, y, tolerance, mode)
      wand.autoOutline(this.x, this.y, 19.0, LEGACY_MODE);
      int[] xpoints = wand.xpoints;
      int[] ypoints = wand.ypoints;
      if(checkDiameter()){
        addCell(xpoints, ypoints);
      }
    }
    nextPoint();
  }

  /**
  * Checks whether the next point is already inside of a recorded cell.
  * @return  Whether or not the next point is already inside of a recorded cell
  * OR
  * @return  Either the number of cell that the next point is inside,
  * or -1 if the next point is not already inside of a recorded cell.
  */
  public boolean isRecorded(){ //OR public int isRecorded(){
    if (record.cellExists(this.x, this.y)) {
      return true;
    }
    return false;
  }

  //ABOUT checkDiameter:
  //should any cell with an insufficient diameteer stil be added to record so that this process is not repeated?
  //We could mark these cells so that at the end of the process, they are deleted
  /**
  * Checks whether a new cell is large enough to be considered a cell.
  * @return  whether a new cell is large enough to be considered a cell
  */
  public boolean checkDiameter(){
    //TODO: Calculate the maximum diameter of the cell
    int maxDiameter = 0;
    if (maxDiameter >= minDiameter){
      return true;
    }
    return false;
  }

  /**
  * Creates a new Cell object and adds it to Record class
  * @param   xpoints  The x coordinates of the points that outline the cell
  * @param   ypoints  The y coordinates of the points that outline the cell
  */
  public void addCell(int[] xpoints, int[] ypoints){
    record.addCell(xpoints, ypoints);
  }

  /**
  * Calculates the nextPoint to be traversed to, and updates location or x and y
  */
  public void nextPoint(){
    int nextX = this.x + this.traverseDistance;
    if (nextX > this.width){
      nextX = this.traverseDistance;
      this.y = this.y + this.traverseDistance;
    }
    this.x = nextX;
  }
}
