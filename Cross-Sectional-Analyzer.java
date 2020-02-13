import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.gui.Wand;
import ij.WindowManager;

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
		wand.autoOutline(10,10);
		String str = Integer.toString(wand.xpoints[0]);
		IJ.showMessage(str);
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

