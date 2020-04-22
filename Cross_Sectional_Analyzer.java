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
import java.util.ArrayList;

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
	private int minDiameter = 50;
	private int traverseDistance = 10;
	private int        width;           // Width of the original image
	private int        height;          // Height of the original image
	private int        size;            // Total number of pixels
    private int        maginification;

	private Record record;
	//Variables for Dialog
	private String userTitle = "User Edit Mode";
	private String userText = "Use the Brush Tool to clarify cell borders that the program is not recognizing. \nIgnore non-cell regions that have been outlined and numbered; you will be able to remove these items from the image and readout at another point in this program. \nClick OK when you are done editing, and the program will re-run.";
	private String deletePrompt = "";
	private GenericDialog gd = new GenericDialog("");

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

	public boolean showDeletionDialog(){
		GenericDialog gd = new GenericDialog("Delete Cells");
		int size = record.size();
		String[] labels = new String[size];
		boolean[] states = new boolean[size];
		int c = 1;
		for(int i = 0; i < size; i++){
			labels[i] = "Cell " + c;
			c++;
		}
		gd.addCheckboxGroup(size/3, 3, labels, states);
		gd.showDialog();
		ArrayList<Integer> cellsToBeDeleted = new ArrayList<Integer>();
		// for(int i = 1; i <= size + 1; i++){
		// 	if (gd.getNextBoolean()){
		// 		cellsToBeDeleted.add(i);
		// 	}
		// }
		for(int i = 1; i <= size; i++){
			if (states[i-1]){
				cellsToBeDeleted.add(i);
			}
		}
		int index = 0;
		for(int i = cellsToBeDeleted.size()-1; i >= 0; i--){
			index = cellsToBeDeleted.get(i).intValue();
			record.removeCell(index);
		}
		return true;
  }

    public void initialOptions() {
        GenericDialog gd = new GenericDialog("Cross Analyzer Setup");
        //(("label", default number)
        //magnification, traverse distance, minimum cell area
        gd.addNumericField("magnification", 400.0, 0);
        gd.addNumericField("traverse distance", 50.0, 0);
        gd.addNumericField("minimum cell perimeter", 250.0, 0);
        gd.showDialog();
        maginification = (int)gd.getNextNumber();
        traverseDistance = (int)gd.getNextNumber();
        minDiameter = (int)gd.getNextNumber();
//        IJ.log("Initial Options: ");
//        IJ.log(Integer.toString(mag));
//        IJ.log(Integer.toString(traverse));
//        IJ.log(Integer.toString(perimeter));


    }

	public void run(ImageProcessor ip) {
		ip.setAutoThreshold(AutoThresholder.Method.Mean, true);
		imp.updateAndDraw();

		//if (!showDialog()) return;
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		this.size = width*height;
		this.wand = new Wand(ip);
        initialOptions();
		this.record = new Record();
		Traverser traverser = new Traverser(imp, ip, minDiameter, traverseDistance, record);
		//Consider using NonBlockingGenericDialog instead
		WaitForUserDialog wait = new WaitForUserDialog(userTitle, userText);

		//Enter Traverse and Edit Loop
		while(true){
			traverser.traverse();
			this.deletePrompt = " " + record.size() + " cells identified\n To clarify cell borders that the program is not recognizing, press \"OK\".\n To delete non-cell regions that have been outlined and numbered, and to view readout, press \"CANCEL\".";
			gd.addMessage(deletePrompt);
			//IJ.log("Record length: " + Integer.toString(record.cells.size()));
			//Polygon reduced = record.cells.get(0).reducePoints(8);
			//IJ.log("UPDATED");
			//IJ.log("Initial length: " + Integer.toString(record.cells.get(0).getShape().npoints));

			//Create button that breaks loop
			gd.showDialog();
			if (gd.wasCanceled()) break;
			//If "OK" is selected, continue the loop.
			wait.show();
			this.record = new Record();
			traverser = new Traverser(imp, ip, minDiameter, traverseDistance, record);
			wait = new WaitForUserDialog(userTitle, userText);
			this.gd = new GenericDialog("");
			//wait.waitForUser(userTitle, userText);
		}
		showDeletionDialog();
		traverser.drawAllCells();
		//Display Readout and Allow Cell Deletion
		record.createTable();
		IJ.showMessage("Finished!");
	}
}
