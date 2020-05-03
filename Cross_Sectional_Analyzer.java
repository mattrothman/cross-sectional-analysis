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
import ij.measure.*;


/**
 * ImageJ plugin to measure the areas of skeletal muscle fibers.
 *
 * @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
 */
public class Cross_Sectional_Analyzer implements PlugInFilter {
	private ImagePlus  imp;             // Original image
	private ImageProcessor ip;
	private Wand wand;
	private double minArea;
	private int traverseDistance = 10;
	private int        width;           // Width of the original image
	private int        height;          // Height of the original image
	private int        size;            // Total number of pixels
	private double pixelSize;
	private Record record;
	//Variables for Dialog
	private String userTitle = "User Edit Mode";
	private String userText = "Use the Brush Tool to clarify cell borders that the program is not recognizing. \nIgnore non-cell regions that have been outlined and numbered; you will be able to remove these items from the image and readout at another point in this program. \nClick OK when you are done editing, and the program will re-run.";
	private String deletePrompt = "";
	private GenericDialog gd = new GenericDialog("");
	private ArrayList<Cell> cellsToBeDeleted = new ArrayList<Cell>();
	private ArrayList<Cell> deletedCells = new ArrayList<Cell>();
	private ArrayList<Cell> cellsToBeAdded = new ArrayList<Cell>();
	private static final boolean DEBUG = false;

	public static void main(String[] args) {
		Cross_Sectional_Analyzer csa = new Cross_Sectional_Analyzer();
		csa.setup("", null);
	}

	public int setup(String arg, ImagePlus imp){
		this.imp = imp;
		return DOES_ALL;
	}

	public boolean showDeletionDialog1(){
		GenericDialog gd = new GenericDialog("Delete Cells");
		gd.setResizable(true);
		gd.addMessage("Check the box next to a cell to delete it:");
		int size = record.size();
		String[] labels = new String[size];
		boolean[] states = new boolean[size];
		int c = 1;
		for(int i = 0; i < size; i++){
			labels[i] = "Cell " + c;
			states[i] = false;
			c++;
		}

		gd.addCheckboxGroup((size/6 + 1), 6, labels, states);
		gd.addMessage("Press DELETE CELLS to delete the selected cells. \nPress CONTINUE to view results for the current " + record.size() + " cells." );
        gd.enableYesNoCancel("Delete Cells", "Continue");
        gd.hideCancelButton();
        gd.showDialog();

        if (!gd.wasOKed()) return false;

        for(int i = 0; i < size; i++){
			if (gd.getNextBoolean()){
				cellsToBeDeleted.add(record.cells.get(i));
				deletedCells.add(record.cells.get(i));
				if (DEBUG) IJ.log("cellsToBeDeleted.add(" + i + "), Cell " + (i+1));
			}
		}
		if (DEBUG) IJ.log("deletedCells.size() = " + deletedCells.size());
		return true;
  }

	public void deleteCells() {
		int index = 0;
		for(int i = cellsToBeDeleted.size()-1; i >= 0; i--){
			index = cellsToBeDeleted.get(i).cellNum - 1;
			this.record.removeCell(index);
		}
		cellsToBeDeleted.clear();
  }

	public void renumberDeletedCells() {
		int num = record.size() + 1;
    for (Cell cell : this.deletedCells) {
      cell.updateCellNum(num);
      num++;
    }
  }

	public void addCells() {
		for(Cell cell : this.cellsToBeAdded){
			this.record.addCell(cell);
		}
  }

	public Boolean showDeletionDialog2(){
		GenericDialog gd = new GenericDialog("Delete Cells");
		gd.setResizable(true);
		gd.pack();

		gd.addMessage("Check the box next to a cell to delete it:");
		int size1 = record.size();
		String[] labels1 = new String[size1];
		boolean[] states1 = new boolean[size1];
		int c = 1;
		for(int i = 0; i < size1; i++){
			labels1[i] = "Cell " + c;
			states1[i] = false;
			c++;
		}
		gd.addCheckboxGroup((size1/6 + 1), 6, labels1, states1);

		gd.addMessage("Check the box next to a currently deleted cell to re-add it:");
		int size2 = cellsToBeDeleted.size();
		String[] labels2 = new String[size2];
		boolean[] states2 = new boolean[size2];
		c = size1 + 1;
		for(int i = 0; i < size2; i++){
			labels1[i] = "Deleted Cell " + c;
			states1[i] = false;
			c++;
		}
		gd.addCheckboxGroup((size2/6 + 1), 6, labels2, states2);
		gd.addMessage("Press DELETE CELLS to delete and/or re-add the selected cells. \nPress CONTINUE to view results for the current " + record.size() + " cells." );
        gd.enableYesNoCancel("Delete Cells", "Continue");
        gd.hideCancelButton();
        gd.showDialog();

        if (!gd.wasOKed()) return false;

		for(int i = 0; i < size1; i++){
			if (gd.getNextBoolean()){
				cellsToBeDeleted.add(record.cells.get(i));
				deletedCells.add(record.cells.get(i));
				if (DEBUG) IJ.log("cellsToBeDeleted.add(" + i + "), Cell " + (i+1));
			}
		}
		if (DEBUG) IJ.log("deletedCells.size() = " + deletedCells.size());

		cellsToBeAdded.clear();
		for(int j = 0; j < (size2 - size1); j++){
			if (gd.getNextBoolean()){
				cellsToBeAdded.add(deletedCells.remove(j));
				if (DEBUG) IJ.log("cellsToBeAdded.add(" + j + "), Cell " + (j+1));
			}
		}
		if (DEBUG) IJ.log("cellsToBeAdded.size() = " + cellsToBeAdded.size());

		return true;
  }

	public Boolean initialOptions(String unit) {
    GenericDialog gd = new GenericDialog("Cross Analyzer Setup");
		traverseDistance = 10;
		minArea = 825.0 * pixelSize * pixelSize;
		while(true){
			gd = new GenericDialog("Cross Analyzer Setup");
			gd.addNumericField("Traverse Distance (pixels)", traverseDistance, 0);
			gd.addNumericField("Minimum Cell Area (square " + unit + ")", minArea, 0);
	    gd.showDialog();
			if (gd.wasCanceled()) return false;
	    traverseDistance = (int)gd.getNextNumber();
	    minArea = (double)gd.getNextNumber()/(pixelSize * pixelSize);
			if((traverseDistance < 1) && (minArea < 0)){
				traverseDistance = 10;
				minArea = 825.0 * pixelSize * pixelSize;
				IJ.showMessage("Traverse Distance cannot be less than 1 pixel and Minimum Cell Area cannot be negative. Please enter valid traverse inputs.");
				continue;
			}
	    else if (traverseDistance < 1) {
				traverseDistance = 10;
	      IJ.showMessage("Traverse Distance cannot be less than 1 pixel. Please enter a valid traverse distance.");
				continue;
	    }
			else if (minArea < 0) {
				minArea = 825.0 * pixelSize * pixelSize;
	      IJ.showMessage("Minimum Cell Area cannot be negative. Please enter a valid minimum cell area.");
				continue;
			}
			break;
		}
		return true;
  }

	/**
	 * Asks the user if they would like to save the final cell selection as an overlay for later use
	 */
	private void overLayPrompt() {
		String[] choices = new String[]{"red", "blue", "green"};
		GenericDialog gd = new GenericDialog("Save Overlay");
		gd.addMessage("Would you like to save an overlay of the cells?");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		Color color = new ColorChooser("Choose a color for overlay", new Color(0x03fcf4), false).getColor();
		CellOverlay co = new CellOverlay(this.record, this.height, this.width, color);
		co.createAndSave();
	}

	//doesn't work
	// public void finalDrawing() {
	// 	//ImageProcessor ip = imp.getProcessor();
	// 	imp.updateAndDraw();
	// 	ip.setLineWidth(2);
	// 	ip.setFontSize(20);
	// 	ip.setColor(new Color(0x03fcf4));
	// 	for (Cell curr : this.record.cells) {
	// 			ip.drawPolygon(curr.getShape());
	// 			String num = Integer.toString(curr.getcellNum());
	// 			ip.drawString(num, curr.getstartx(), curr.getstarty());
	// 	}
	// 	imp.updateAndDraw();
	// 	imp.show();
	// }

	public void run(ImageProcessor ip) {
		//if (!showDialog()) return;
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		this.size = width*height;
		this.wand = new Wand(ip);
		Calibration cal = imp.getCalibration();
		pixelSize = cal.pixelWidth;
		String unit = cal.getUnit();
        Boolean runPlugin = initialOptions(unit);
		if(!runPlugin) return;
		ip.setAutoThreshold(AutoThresholder.Method.Mean, true);
		imp.updateAndDraw();

		this.record = new Record();
		Traverser traverser = new Traverser(imp, ip, minArea, traverseDistance, record);
		//Consider using NonBlockingGenericDialog instead
		WaitForUserDialog wait = new WaitForUserDialog(userTitle, userText);

		//Enter Traverse and Edit Loop
		while(true){
			traverser.traverse();
			this.deletePrompt = " " + record.size() + " cells identified\n To clarify cell borders that the program is not recognizing, press \"EDIT CELLS\".\n To delete non-cell regions that have been outlined and numbered, and to view readout, press \"Continue\".";
			gd.addMessage(deletePrompt);
			gd.enableYesNoCancel("Edit Cells", "Continue");
			gd.hideCancelButton();
			//IJ.log("Record length: " + Integer.toString(record.cells.size()));
			//Polygon reduced = record.cells.get(0).reducePoints(8);
			//IJ.log("UPDATED");
			//IJ.log("Initial length: " + Integer.toString(record.cells.get(0).getShape().npoints));

			gd.showDialog();

            if (!gd.wasOKed()) break;

			//If "OK" is selected, continue the loop.
			wait.show();
			this.record = new Record();
			traverser = new Traverser(imp, ip, minArea, traverseDistance, record);
			wait = new WaitForUserDialog(userTitle, userText);
			this.gd = new GenericDialog("");
		}

		//Deletion Mode
        Boolean deletionLoop = showDeletionDialog1();
		if (deletionLoop) {
            deleteCells();
            this.record.renumberCells();
            renumberDeletedCells();
            traverser.drawAllCells();
            traverser.drawDeletedCells(deletedCells);

            //Enter Deletion Loop
            while (deletionLoop) {
                deletionLoop = showDeletionDialog2();
                if (deletionLoop) {
                    deleteCells();
                    addCells();
                    this.record.renumberCells();
                    renumberDeletedCells();
                    traverser.drawAllCells();
                    traverser.drawDeletedCells(deletedCells);
                }
            }
        }

		//Display Results Table
		this.record.createTable(pixelSize, unit);
		if (DEBUG) IJ.showMessage("Finished!");
		traverser.drawAllCells();
		// traverser.finalize();
		// finalDrawing();

//        Calibration cal = imp.getCalibration();
//        double pixel = cal.pixelWidth;
//        IJ.log(Double.toString(pixel));
//        Polygon cell1 = record.cells.get(0).getShape();
//        int[] x = new int[]{10,10,30,30};
//        int[] y = new int[]{40,40,50,50};
//        Polygon square = new Polygon(x,y,4);
//        double pixelArea = Cell.pixelArea(square);
//
//        double cellArea = pixelArea * pixel * pixel;
//        IJ.log("Square Area: " + Double.toString(cellArea));
//
//        Cell cell = new Cell(cell1.xpoints, cell1.ypoints, 20,45, 3);
//        IJ.log("Cell1 Area: " + Double.toString(cell.calculateArea(cell1) * pixel * pixel));

        overLayPrompt();

	}
}
