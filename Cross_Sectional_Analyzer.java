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
import ij.measure.ResultsTable;

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

	public ArrayList<Integer> showDeletionDialog(){
		GenericDialog gd = new GenericDialog("Delete Cells");
		int size = record.size();
		String[] labels = new String[size];
		boolean[] states = new boolean[size];
		int c = 1;
		for(int i = 0; i < size; i++){
			labels[i] = "Cell " + c;
			states[i] = false;
			c++;
		}
		gd.addCheckboxGroup((size/3 + 1), 3, labels, states);
		gd.showDialog();
		ArrayList<Integer> cellsToBeDeleted = new ArrayList<Integer>();
		// for(int i = 1; i <= size + 1; i++){
		// 	if (gd.getNextBoolean()){
		// 		cellsToBeDeleted.add(i);
		// 	}
		// }
		for(int i = 0; i < size; i++){
			if (gd.getNextBoolean()){
				cellsToBeDeleted.add(i);
				IJ.log("cellsToBeDeleted.add(" + i + ")");
			}
		}
		IJ.log("cellsToBeDeleted.size() = " + cellsToBeDeleted.size());
		return cellsToBeDeleted;
  }

  //no negitive values
    //eventually change the n-points to area
    public void initialOptions() {
        GenericDialog gd = new GenericDialog("Cross Analyzer Setup");
        //(("label", default number)
        //magnification, traverse distance, minimum cell area
        gd.addNumericField("magnification", 400.0, 0);
        gd.addNumericField("traverse distance", 10.0, 0);
        gd.addNumericField("minimum cell perimeter", 250.0, 0);
        gd.showDialog();
        maginification = (int)gd.getNextNumber();
        traverseDistance = (int)gd.getNextNumber();
        minDiameter = (int)gd.getNextNumber();
        if (maginification < 0) {
            IJ.showMessage("Magnification cannot be negative. Magnification will be reset to the default value of 400X");
            maginification = 400;
        }
        if (traverseDistance < 1) {
            IJ.showMessage("Traverse distance cannot be less than 1. Traverse distance will be reset to the default value of 10");
            traverseDistance = 10;
        }
        if (minDiameter < 0) {
            IJ.showMessage("Minimum Cell Diameter cannot be negative. Magnification will be reset to the default value of 250");
            maginification = 400;
        }
              
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
		ArrayList<Integer> cellsToBeDeleted = showDeletionDialog();
		int index = 0;
		for(int i = cellsToBeDeleted.size()-1; i >= 0; i--){
			index = cellsToBeDeleted.get(i).intValue();
			this.record.removeCell(index);
		}
		traverser.drawAllCells();
		//Display Readout and Allow Cell Deletion
		this.record.renumberCells();
		this.record.createTable();
		IJ.showMessage("Finished!");
		traverser.drawAllCells();
	}
}
