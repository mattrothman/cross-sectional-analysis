import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.gui.Wand;
import ij.WindowManager;

// cell import statements
import ij.plugin.filter.Analyzer;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.lang.Math;
import java.util.Arrays;

//record import statements
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
/**
 * ImageJ plugin to measure the areas of skeletal muscle fibers.
 *
 * @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
 */
public class Cross_Sectional_Analyzer implements PlugInFilter{
	private ImagePlus  imp;             // Original image
	//private ImageStack sstack;          // Stack result
	private ImageProcessor ip;
	private Wand wand;
	private static final int minDiameter = 3;
	private static final int traverseDistance = 5;
	private int        width;           // Width of the original image
	private int        height;          // Height of the original image
	private int        size;            // Total number of pixels
	//private Record record;

	public int setup(String arg, ImagePlus imp){
		this.imp = imp;
		//this.imp = WindowManager.getCurrentImage();
		// if (arg.equals("about"))
		// {showAbout(); return DONE;}
		return DOES_ALL;
		//return 0;
	}

	public void run(ImageProcessor ip) {
		//if (!showDialog()) return;
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		this.size = width*height;
		this.wand = new Wand(ip);
		wand.autoOutline(106,38,19, Wand.LEGACY_MODE);
		int point = IJ.doWand(100, 100);
		Cell cell = new Cell(wand.xpoints, wand.ypoints);
		int[] xs = {108, 106, 106, 108};
		int[] ys = {40, 40, 38, 38};
		Cell cell2 = new Cell(xs, ys);
		double area = cell2.getArea();
		String str = Integer.toString(wand.xpoints[0]);
		//IJ.showMessage(Integer.toString(point));
		int[] xcell1 = {108, 106, 106, 108};
		int[] ycell1 = {40, 40, 38, 38};
		int[] xcell2 = {108, 106, 106, 108, 45, 83};
		int[] ycell2 = {40, 40, 38, 38, 89, 90};
		int[] xcell3 = {1, 2, 6, 7, 8, 3};
		int[] ycell3 = {3, 2, 6, 40, 38, 38};
		Record record = new Record();
		record.addCell(xcell3, ycell3);
		record.addCell(xcell2, ycell2);
		record.addCell(xcell1, ycell1);
		//record.printData("/Users/thaliabarr-malec/Documents");
		//boolean doesIt = record.cellExists(107, 39);
		//IJ.showMessage(Boolean.toString(record.cellExists(107, 39)));
		//IJ.showMessage(Integer.toString(wand.npoints));
		//IJ.showMessage(cell.toString());
	}

	//CELL
	public class Cell {
		//private int[] xpoints;
		//private int[] ypoints;
		private double area;
		private Polygon shape;
		//https://books.google.com/books?id=YEm5BQAAQBAJ&pg=PA735&lpg=PA735&dq=roundness+range+of+cells&source=bl&ots=EnoKFIoelk&sig=ACfU3U0FxVJIPjL3KEJtNMSTg0wchFnIWw&hl=en&ppis=_c&sa=X&ved=2ahUKEwiV1fDO8r3nAhUUoZ4KHY9DA_IQ6AEwDXoECAoQAQ#v=onepage&q=roundness%20range%20of%20cells&f=false
		//Acorrding to link, roundness or a normal cell is 0.43-0.97
		//and roundness of an abnormal cell is 0.06-0.87
		//Starting value of
		private static final double minRoundness  = 0.2; //This will change
		//declare a center coordinate

		public Cell(int[] xpoints, int[] ypoints) { //(int[] newXpoints, int[] newYpoints, int newCellId) {
			//xpoints = newXpoints;
			//ypoints = newYpoints;
			Polygon cell = new Polygon(xpoints, ypoints, xpoints.length);
			shape = cell;
			area = calculateArea(shape);
		}

		public boolean contains(int xpoint, int ypoint) {
			//Deprecated. As of JDK version 1.1, replaced by contains(int, int).
			//return shape.inside(xpoint, ypoint);
			return shape.contains(xpoint, ypoint);

		}

		// Unfortunately the imageJ method using getArea was private so I copied the code over.
		final double calculateArea(Polygon p) {
			if (p==null) return Double.NaN;
			int carea = 0;
			int iminus1;
			for (int i=0; i<p.npoints; i++) {
				iminus1 = i-1;
				if (iminus1<0) iminus1=p.npoints-1;
				carea += (p.xpoints[i]+p.xpoints[iminus1])*(p.ypoints[i]-p.ypoints[iminus1]);
			}
			return (Math.abs(carea/2.0));
		}

		//https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3544517/
		//Roundness is based on the above article
		public boolean roundness(){
			Rectangle bounds = shape.getBounds();
			double height = bounds.getHeight();
			double width = bounds.getWidth();
			double length = width;
			if(height > width){
				length = height;
			}
			double roundness = this.area/(Math.pow(length, 2.0));
			if (roundness < minRoundness){
				return false;
			}
			else{
				return true;
			}
		}

		//This method will not be used in final implementation!
		//This method exists so we can get a range for the roundness or our cells
		//So that we can adjust our minRoundness value
		public double calcRoundness(){
			Rectangle bounds = shape.getBounds();
			double height = bounds.getHeight();
			double width = bounds.getWidth();
			double length = width;
			if(height > width){
				length = height;
			}
			double roundness = this.area/(Math.pow(length, 2.0));
			return roundness;
		}

		public double getArea() { return area; }

		public Polygon getShape() { return shape; }

		public String toString(){
			int[] x = Arrays.copyOfRange(shape.xpoints, 0, 20);
			int[] y = Arrays.copyOfRange(shape.ypoints, 0, 20);
			String toString = "X-Points: " + Arrays.toString(x) + "\n Y-Points: " + Arrays.toString(y);
			return toString;

		}
	}
	class Record {

		// To-do: decide if this is the right data structure for the job
		public ArrayList<Cell> cells;

		public Record() {
			//this.cells = new ArrayList<Cell>();
			this.cells = new ArrayList();
		}

		// We could make this return either the cell itself or the current record if we wanted to
		public void addCell(int[] xpoints, int[] ypoints) {
			Cell cellToAdd = new Cell(xpoints, ypoints);
			cells.add(cellToAdd);
		}

		public void printData(String fileLocation) throws FileNotFoundException {
			File data = new File(fileLocation);
			PrintWriter pw = new PrintWriter(data);

			// This is probably not the way we will want the text file to look. Some sort of table would be best.
			for (int i = 0; i < cells.size(); i++) {
				Cell currCell = cells.get(i);
				pw.println(i + ": " +currCell.getArea());
			}

			pw.close();
		}

		/**
		 * Checks to see if there is already an existing cell in record that contains the given point.
		 * @param x
		 * @param y
		 * @return Whether there exists a cell in record that contains the given point
		 */
		public boolean cellExists(int x, int y) {
			for (int i = 0; i < cells.size(); i++) {
				Cell currCell = cells.get(i);
				if (currCell.contains(x,y)) {
					return true;
				}
			}
			return false;
		}
	}

}

// import java.awt.*;
// import java.awt.event.*;
// import javax.swing.*;
// import javax.swing.event.*;
// import java.util.*;
//
// import ij.*;
// import ij.process.*;
// import ij.gui.*;
// import ij.plugin.*;
// import ij.plugin.filter.Analyzer;
// import java.awt.*;
// import ij.plugin.frame.*;
//
// //Matt added these, 2/5/20, not sure how many are necessary.
// import ij.IJ;
// import ij.ImagePlus;
// import ij.ImageStack;
// import ij.WindowManager;
// import ij.gui.ImageWindow;
// import ij.gui.StackWindow;
// import ij.process.ImageProcessor;
// import ij.CompositeImage;
// import java.awt.Dimension;
// import java.awt.EventQueue;
// import java.awt.FileDialog;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.event.ItemEvent;
// import java.awt.event.ItemListener;

// initializeImage();
// //Create a new wand
// 	this.wand = new Wand(ip);
// //Determine the traverseDistance based on the magnification and image setDimensions
// //Determine minDiameter in same method as traverseDistance
// //Create a new record
// this.record = new Record();

/**
 private void initializeImage(){
 this.imp = WindowManager.getCurrentImage();
 if (imp==null){
 IJ.noImage();
 }
 else if (imp.getStackSize() == 1) {
 this.ip = imp.getProcessor();
 ip.resetRoi();
 //WTF is this?
 // counterimp = new ImagePlus("Counter Window - "+imp.getTitle(), ip);
 // Vector displayList = v139t?imp.getCanvas().getDisplayList():null;
 // ic = new CellCntrImageCanvas(counterimp,typeVector,this,displayList);
 // new ImageWindow(counterimp, ic);
 }
 else if (imp.getStackSize() > 1){
 ImageStack stack = imp.getStack();
 int size = stack.getSize();
 ImageStack counterStack = imp.createEmptyStack();
 for (int i = 1; i <= size; i++){
 ImageProcessor ip = stack.getProcessor(i);
 // counterStack.addSlice(stack.getSliceLabel(i), ip);
 }
 } */
// counterimp = new ImagePlus("Counter Window - "+imp.getTitle(), counterStack);
// counterimp.setDimensions(imp.getNChannels(), imp.getNSlices(), imp.getNFrames());
// if (imp.isComposite()) {
//     counterimp = new CompositeImage(counterimp, ((CompositeImage)imp).getMode());
//     ((CompositeImage) counterimp).copyLuts(imp);
// }
// counterimp.setOpenAsHyperStack(imp.isHyperStack());
// Vector displayList = v139t?imp.getCanvas().getDisplayList():null;
// ic = new CellCntrImageCanvas(counterimp,typeVector,this,displayList);
// new StackWindow(counterimp, ic);
//}
//}
