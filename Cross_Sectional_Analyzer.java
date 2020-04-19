import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.gui.Wand;
import ij.WindowManager;
import java.util.Arrays;
//Import statements for Dialog
import java.awt.Dialog;
import ij.gui.WaitForUserDialog;
import ij.gui.GenericDialog;

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
	//Variables for Dialog
	private String userTitle = "User Edit Mode";
	private String userText = "Use the Brush Tool to clarify cell borders that the program is not recognizing. \nIgnore non-cell regions that have been outlined and numbered; you will be able to remove these items from the image and readout at another point in this program. \nClick OK when you are done editing, and the program will re-run.";
	private String deletePrompt = "To clarify cell borders that the program is not recognizing, press \"OK\".\n To delete non-cell regions that have been outlined and numbered, and to view readout, press \"CANCEL\".";
	final GenericDialog gd = new GenericDialog("");

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
		ip.setAutoThreshold(AutoThresholder.Method.Mean, true);
		imp.updateAndDraw();

		//if (!showDialog()) return;
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		this.size = width*height;
		this.wand = new Wand(ip);

		this.record = new Record();
		Traverser traverser = new Traverser(imp, ip, minDiameter, traverseDistance, record);
		WaitForUserDialog wait = new WaitForUserDialog(userTitle, userText);
		gd.addMessage(deletePrompt);

		//Enter Traverse and Edit Loop
		while(true){
			traverser.traverse();
			IJ.log("Record length: " + Integer.toString(record.cells.size()));
			//Polygon reduced = record.cells.get(0).reducePoints(8);
			IJ.log("UPDATED");
			IJ.log("Initial length: " + Integer.toString(record.cells.get(0).getShape().npoints));
			//Create button that breaks loop
			gd.showDialog();
			if (gd.wasCanceled()) break;
			//If "OK" is selected, continue the loop.
			wait.show();
			this.record = new Record();
			traverser = new Traverser(imp, ip, minDiameter, traverseDistance, record);
			//wait.waitForUser(userTitle, userText);
		}
        record.createTable();
		//Display Readout and Allow Cell Deletion
	}
}
