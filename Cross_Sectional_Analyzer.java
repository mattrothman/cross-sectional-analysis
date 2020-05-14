import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.gui.Wand;
import ij.WindowManager;
import java.util.Arrays;
import java.awt.event.*;
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
public class Cross_Sectional_Analyzer implements PlugInFilter, MouseListener {
	private ImagePlus  imp;             // Original image
	private ImageProcessor ip;
	private Wand wand;
	private double minArea;
	private int traverseDistance = 10;
	private boolean preprocess;
	private int        width;           // Width of the original image
	private int        height;          // Height of the original image
	private double pixelSize;
	private Record record;
	private Traverser traverser;
	private ImageCanvas canvas;
	private Boolean mouseEnabled = true;
	//Variables for Dialog
	private String userTitle = "Edit Cells";
	private String userText = "Cells may not be recognized due to thin or broken borders.\nEdge cells that are cut off by the image will not be included.\nCells that share a broken border with an edge cell will not be included until the border is fixed.\nCells that share a broken or thin border may be considered one cell until the border is fixed.\n \nUse the Brush Tool to edit cell borders.\n \nIf a fully enclosed cell is not included after editing, you may need to re-run the plugin and\ndecrease the Traverse Distance and/or Minimum Cell Area.\n \nWhen you are done editing, press \"OK\" and the cell outlines will be redrawn to reflect your edits.";
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

	/**
	 * Shows a dialog that allows the user to set the Traverse Distance, Minimum Cell Area, and opt out of pre-processing
	 * @return returns false if the dialog box is exited or if "Cancel" is selected
	 */
	private Boolean initialOptions(String unit) {
    GenericDialog gd = new GenericDialog("Cross-Sectional-Analyzer Setup");
		traverseDistance = 10;
		minArea = 825.0 * pixelSize * pixelSize;
		while(true){
			gd = new GenericDialog("Cross Analyzer Setup");
			gd.addNumericField("Traverse Distance (pixels)", traverseDistance, 0);
			gd.addNumericField("Minimum Cell Area (square " + unit + ")", minArea, 0);
			gd.addHelp(getHelpInitialOptions());
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
	 * Returns a string for the Help Dialog
	 * @return a string for the Help Dialog
	 */
	private String getHelpInitialOptions() {
		Boolean isPencil = true;
		String ctrlString = IJ.isMacintosh()? "<i>cmd</i>":"<i>ctrl</i>";
		return
			 "<html>"
			+"<font size=+1>"
			+"<b>Help\n</b>"
			+" <br>"
			+"Please calibrate your image before using Cross-Sectional-Analyzer.\n"
			+" <br>"
			+"If the unit of \"Minimum Cell Area\" does not reflect the unit of your calibration, please close this plugin and re-calibrate globally.\n"
			+" <br>"
			+"The pre-set \"Traverse Distance\" and \"Minimum Cell Area\" values are calculated based on the dimensions of the image, and are suitable for most images.\n"
			+" <br>"
			+"If fully enclosed cells were not included after editing at some \"Traverse Distance\" and \"Minimum Cell Area\", we recommend decreasing the \"Traverse Distance\" to 5 pixels and decreasing \"Minimum Cell Area\".\n"
			+" <br>"
			+"If an excessive number of non-cell regions were outlined and numbered as cells, increase the \"Minimum Cell Area\".\n"
			+" <br>"
			+"Flashing during cell-editing is normal. If the cell outlines disapear, dragging the mouse on the image will make the outlines reappear.\n"
			+" <br>"
			+"</font>";
	}

	/**
	 * Shows a dialog that allows the user to delete unwanted cells.
	 * @return returns false if the dialog box is exited or if "Continue" is selected
	 */
	private boolean showDeletionDialog1(){
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
		gd.addMessage("Press \"Delete Cells\" to delete the selected cells. \nPress \"Continue\" to view results for the current " + record.size() + " cells." );
		gd.enableYesNoCancel("Delete Cells", "Continue");
		gd.hideCancelButton();
		WindowTools.addScrollBars(gd);
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


	/**
	 * Shows a dialog that allows the user to delete unwanted cells and/or re-add deleted cells.
	 * @return boolean returns false if the dialog box is exited or if "Continue" is selected
	 */
	private Boolean showDeletionDialog2(){
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
		int size2 = deletedCells.size();
		if (DEBUG) IJ.log("deletedCells.size() = " + size2);
		String[] labels2 = new String[size2];
		boolean[] states2 = new boolean[size2];
		c = size1 + 1;
		for(int j = 0; j < size2; j++){
			if (DEBUG) IJ.log("labels1[j] = Deleted Cell "  + c);
			labels2[j] = "Deleted Cell " + c;
			states2[j] = false;
			c++;
		}
		gd.addCheckboxGroup((size2/6 + 1), 6, labels2, states2);
		gd.addMessage("Press \"Delete Cells\" to delete and/or re-add the selected cells. \nPress \"Continue\" to view results for the current " + record.size() + " cells." );
		gd.enableYesNoCancel("Delete Cells", "Continue");
		gd.hideCancelButton();
		WindowTools.addScrollBars(gd);
		gd.showDialog();

		if (!gd.wasOKed()) return false;

		for(int i = 0; i < size1; i++){
			if (gd.getNextBoolean()){
				cellsToBeDeleted.add(record.cells.get(i));
				deletedCells.add(record.cells.get(i));
				if (DEBUG) IJ.log("cellsToBeDeleted.add(" + i + "), Cell " + (i+1));
			}
		}

		cellsToBeAdded.clear();
		for(int j = 0; j < (size2); j++){
			if (gd.getNextBoolean()){
				cellsToBeAdded.add(deletedCells.get(j));
				if (DEBUG) IJ.log("cellsToBeAdded.add(" + j + "), Cell " + (j+1));
			}
		}
		for(int k = 0; k < cellsToBeAdded.size(); k++){
			deletedCells.remove(cellsToBeAdded.get(k));
		}
		if (DEBUG) IJ.log("cellsToBeAdded.size() = " + cellsToBeAdded.size());
		if (DEBUG) IJ.log("deletedCells.size() = " + cellsToBeAdded.size());

		return true;
	}

	/**
	 * Helper method that deletes cells that have been "checked" in a deletion dialog
	 */
	private void deleteCells() {
		int index = 0;
		for(int i = cellsToBeDeleted.size()-1; i >= 0; i--){
			index = cellsToBeDeleted.get(i).cellNum - 1;
			this.record.removeCell(index);
		}
		cellsToBeDeleted.clear();
	}

	/**
	 * Helper method that re-adds cells that have been "checked" in a deletion dialog
	 */
	private void addCells() {
		for(Cell cell : this.cellsToBeAdded){
			this.record.addCell(cell);
		}
	}

	/**
	 * Helper method that renumbers the cells in record after a deletion dialog,
	 * so that the cell numbering reflects any cell deletions/additions
	 */
	private void renumberDeletedCells() {
		int num = record.size() + 1;
		for (Cell cell : this.deletedCells) {
			cell.updateCellNum(num);
			num++;
		}
	}

	/**
	 * Asks the user if they would like to save the final cell selection as an overlay for later use
	 */
	private void overLayPrompt() {
		GenericDialog gd = new GenericDialog("Save Outline");
		gd.addMessage("Would you like to save an image of the outlined and numbered cells?");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		Color color = new ColorChooser("Choose a color for the outline of the numbered cells:", new Color(0x03fcf4), false).getColor();
		CellOverlay co = new CellOverlay(this.record, this.height, this.width, color);
		co.createAndSave();
	}

	/**
	 * Returns a string of the directions for "How To Merge Images"
	 * @return a string of the directions for "How To Merge Images"
	 */
	private String mergeImagesText() {
		return
			"To create a composite image of the outputed image of the outlined cells,\n"
			+"\"Overlay.tif,\" and an image of cells, \"Cells.tif,\" open both images in ImageJ.\n"
			+"Click on \"Cells.tif\" and use Image>Type>RGB Color.\n"
			+"Next, use Image>Color>Merge Channels.\n"
			+"Select \"Overlay.tif\" for C1 (red), C2 (green), and C3 (blue).  Select \"Cells.tif\" for C4 (gray).\n"
			+"When the composite image opens, use Image>Type>RGB Color.";
	}

	/**
	 * MouseListener methods that prompt the cell outlines to be redrawn during cell editing
	 */
	public void mouseClicked(MouseEvent e) {}
	public void	mouseEntered(MouseEvent e){}
	public void	mouseExited(MouseEvent e){}
	public void	mouseReleased(MouseEvent e){
		if (!this.mouseEnabled) return;
		traverser.drawAllCells();
	}
	public void mousePressed(MouseEvent e){
		if (!this.mouseEnabled) return;
		traverser.drawAllCells();
	}

	/**
	 * Runs the Plugin
	 */
	public void run(ImageProcessor ip) {
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		this.wand = new Wand(ip);

		Calibration cal = imp.getCalibration();
		pixelSize = cal.pixelWidth;
		String unit = cal.getUnit();

		Boolean runPlugin = initialOptions(unit);
		if(!runPlugin) return;
		//Preprocess image
		if (preprocess == true) {
			ip.setAutoThreshold(AutoThresholder.Method.Mean, true);
			imp.updateAndDraw();
		}
		//Setup MouseListener for user-edit mode
		canvas = imp.getCanvas();
		canvas.addMouseListener(this);
		this.record = new Record();
		this.traverser = new Traverser(imp, ip, minArea, traverseDistance, record);
		WaitForUserDialog wait = new WaitForUserDialog(userTitle, userText);
		wait.setResizable(true);

		//Enter Traverse and Edit Loop
		while(true){
			traverser.traverse();
			this.deletePrompt = "" + record.size() + " cells identified.\nTo edit cell borders that are incorrect or that have not been recognized, press \"Edit Cells\".\nTo delete unwanted cells, press \"Continue\".";
			gd.addMessage(deletePrompt);
			gd.enableYesNoCancel("Edit Cells", "Continue");
			gd.hideCancelButton();
			WindowTools.addScrollBars(gd);
			gd.showDialog();
			if (!gd.wasOKed()) break;
			//If "Edit Cells" is selected, continue the loop.
			wait.show();
			this.record = new Record();
			traverser = new Traverser(imp, ip, minArea, traverseDistance, record);
			wait = new WaitForUserDialog(userTitle, userText);
			this.gd = new GenericDialog("");
		}
		//Disable mouseListener so the flashing stops after "Edit Cells"
		this.mouseEnabled = false;

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
		traverser.drawAllCells();
		overLayPrompt();
		this.gd = new GenericDialog("How To Merge Images");
		gd.addMessage(mergeImagesText());
		WindowTools.addScrollBars(gd);
		gd.showDialog();
	}
}
