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
  Wand wand;
  int traverseDistance; //The distance between in pixels between calls of traverseOnce()
  int x, y; //The location (x,y coordinate) of the current pixel
  int minDiameter; //holds the minimum diameter of a cell. In other words, how big of a diameter must be able to fit somewhere in the cell outline
  ImagePlus imp;
  int width; //The width of image
  int height; //The heght of image
  double TOLERANCE = 19.0;
  Record record;
  ImageProcessor ip;

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
  }

  /**
   * Traverses the image, adding new cells to the record class when appropriate.
   */
  public void traverse () {
    while (this.y < this.height) {
      traverseOnce();
    }
  }

  /**
   * Traverses to the next point in the image.
   * If the point is inside a new, valid cell, a new cell is created and added to the record class.
   */
  public void traverseOnce () {
    if (!isRecorded()) {
      //doWand(x, y, tolerance, mode)
      //wand.autoOutline(this.x, this.y, 19.0, LEGACY_MODE);
      Wand wand = doWand(x, y, TOLERANCE);
      int[] xpoints = wand.xpoints;
      int[] ypoints = wand.ypoints;
      //if (checkDiameter()) {
      addCell(xpoints, ypoints); // until we find a way to check the diameter we should keep this commented out
      //}
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
  public boolean isRecorded () { //OR public int isRecorded(){
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
  public void addCell ( int[] xpoints, int[] ypoints){
    record.addCell(xpoints, ypoints);
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


    /** Adapted from doWand method in ImageJ. Changed to return a wand rather than an int */
  public Wand doWand(int x, int y, double tolerance) {
    int imode = Wand.LEGACY_MODE;
		boolean smooth = false;
    Wand w = new Wand(ip);

    double t1 = ip.getMinThreshold();
    if (t1==ImageProcessor.NO_THRESHOLD || (ip.getLutUpdateMode()==ImageProcessor.NO_LUT_UPDATE&& tolerance>0.0)) {
      w.autoOutline(x, y, tolerance, imode);
    } else
      w.autoOutline(x, y, t1, ip.getMaxThreshold(), imode);
		if (w.npoints>0) {
			Roi previousRoi = imp.getRoi();
			Roi roi = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, Roi.TRACED_ROI);
			imp.deleteRoi();
			imp.setRoi(roi);
			if (previousRoi!=null)
				roi.update(false, false);  // add/subtract ROI to previous one if shift/alt key down
			Roi roi2 = imp.getRoi();
			if (smooth && roi2!=null && roi2.getType()==Roi.TRACED_ROI) {
				Rectangle bounds = roi2.getBounds();
				if (bounds.width>1 && bounds.height>1) {
					String smoothMacro = null;
					if (smoothMacro==null)
						smoothMacro = BatchProcessor.openMacroFromJar("SmoothWandTool.txt");
					// if (EventQueue.isDispatchThread())
					// 	new MacroRunner(smoothMacro); // run on separate thread
					// else 
					// 	Macro.eval(smoothMacro);
				}
			}
		}
    return w;
  }
}
