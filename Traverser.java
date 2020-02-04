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
* Traverser class traverses an image, from left to right, outlining cells.
*
* @author
*/

//Should it: extends PlugInFrame implements Measurements ??
public class Traverser {
  Wand wand;
  int traverseDistance; //holds the distance between clicks (pixels)
  //Is location supposed to be an array?
  int location, x, y; //holds the current location (x,y coordinate) of where it is in the image
  int[] imageBoundary; //holds the image boundary coordinates
  int minDiameter; //holds the minimum diameter of a cell. In other words, how big of a diameter must be able to fit somewhere in the cell outline
  ImagePlus image; //holds the cell image, not sure about the type
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
    //while next point is valid
    //traverseOnce()
  }
  
  /**
  * Traverses to the next point in the image.
  * If the point is inside a new, valid cell, a new cell is created and added to the record class.
  */
  public void traverseOnce(){
    //next point has already been checked, and is in the image
    //if (isRecorded()) return;
    //wand.autoOutline(x, y)
    //int[] xpoints = wand.xpoints;
    //int[] ypoints = wand.ypoints;
    //if (!checkDiameter()) return;
    //addCell();
  }
  
  /**
  * Checks whether the next point is already inside of a recorded cell.
  * @return  Whether or not the next point is already inside of a recorded cell
  * OR
  * @return  Either the number of cell that the next point is inside,
  * or -1 if the next point is not already inside of a recorded cell.
  */
  public boolean isRecorded(){ //OR public int isRecorded(){
    //for all cells in recorded
    //if cell.inside(nextpoint), return false
    //return true
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
    //Cell cell = new Cell(xpoints, ypoints);
    //record.addCell(cell);
  }
  
  /**
  * Calculates the nextPoint to be traversed to, and updates location or x and y?
  */
  public void nextPoint(){
    int nextX = this.x + this.traverseDistance;
    if (nextX > this.width){
      nextX = 0;
      this.y = this.y + this.traverseDistance;
    }
    this.x = nextX;
  }
}


