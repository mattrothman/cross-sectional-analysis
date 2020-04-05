import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.gui.Wand;
import ij.WindowManager;
import java.util.Arrays;


/**
 * ImageJ plugin to measure the areas of skeletal muscle fibers.
 *
 * @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
 */
public class Cross_Sectional_Analyzer implements PlugInFilter {
	private ImagePlus  imp;             // Original image
	//private ImageStack sstack;          // Stack result
	private ImageProcessor ip;
	private Wand wand;
	private static final int minDiameter = 50;
	private static final int traverseDistance = 100;
	private int        width;           // Width of the original image
	private int        height;          // Height of the original image
	private int        size;            // Total number of pixels
	private Record record;

	public static void main(String[] args) {
		Cross_Sectional_Analyzer csa = new Cross_Sectional_Analyzer();
		csa.setup("", null);
	}

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

		//Beginning of Matt's test code
		this.record = new Record();
		Traverser traverser = new Traverser(imp, ip, minDiameter, traverseDistance, record);
		traverser.traverse();
		IJ.log("Record length: " + Integer.toString(record.cells.size()));
		Polygon reduced = record.cells.get(0).reducePoints(5);
		Cell newCell = new Cell(reduced.xpoints, reduced.ypoints, reduced.xpoints[0], reduced.ypoints[0], 100);
		newCell.logPoints();
		//IJ.log(Arrays.toString(record.cells.get(2).getShape().xpoints));
        //IJ.log(Arrays.toString(record.cells.get(2).getShape().ypoints));
        //IJ.log("Is cell 2 an edge cell?: " + Boolean.toString(traverser.isEdgeCell(record.cells.get(1))));


		// Wand w1 = traverser.doWand(200, 200, 19.0);
		// Cell cell11 = new Cell(w1.xpoints, w1.ypoints);
		// double area1 = cell11.getArea();
		// IJ.showMessage("cell11 area: " + area1);
		//
		// Wand w2 = traverser.doWand(300, 300, 19.0);
		// Cell cell22 = new Cell(w2.xpoints, w2.ypoints);
		// area1 = cell22.getArea();
		// IJ.showMessage("cell22 area: " + area1);
		//
		// Wand w3 = traverser.doWand(800, 800, 19.0);
		// Cell cell33 = new Cell(w3.xpoints, w3.ypoints);
		// area1 = cell33.getArea();
		// IJ.showMessage("cell33 area: " + area1);
		//End of Matt's test code

		// wand.autoOutline(106,38,19, Wand.LEGACY_MODE);
		// int point = IJ.doWand(100, 100);
		// Cell cell = new Cell(wand.xpoints, wand.ypoints);
		// int[] xs = {108, 106, 106, 108};
		// int[] ys = {40, 40, 38, 38};
		// Cell cell2 = new Cell(xs, ys);
		// double area = cell2.getArea();
		// String str = Integer.toString(wand.xpoints[0]);
		// //IJ.showMessage(Integer.toString(point));
		// int[] xcell1 = {108, 106, 106, 108};
		// int[] ycell1 = {40, 40, 38, 38};
		// int[] xcell2 = {108, 106, 106, 108, 45, 83};
		// int[] ycell2 = {40, 40, 38, 38, 89, 90};
		// int[] xcell3 = {1, 2, 6, 7, 8, 3};
		// int[] ycell3 = {3, 2, 6, 40, 38, 38};
		// Record record = new Record();
		// record.addCell(xcell3, ycell3);
		// record.addCell(xcell2, ycell2);
		// record.addCell(xcell1, ycell1);
		// Traverser traverse = new Traverser(imp, ip, 300, 4, record);
		// traverse.traverse();

		//boolean doesIt = record.cellExists(107, 39);
		//IJ.showMessage(Boolean.toString(record.cellExists(107, 39)));
		//IJ.showMessage(Integer.toString(wand.npoints));
		//IJ.showMessage(cell.toString());
	} }
