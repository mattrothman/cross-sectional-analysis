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
		//Polygon reduced = record.cells.get(0).reducePoints(8);
		IJ.log("UPDATED");
		IJ.log("Initial length: " + Integer.toString(record.cells.get(0).getShape().npoints));
	} }
