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
import java.awt.*;
import ij.plugin.frame.*;

//Matt added these, 2/5/20, not sure how many are necessary.
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;
import ij.CompositeImage;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import ij.gui.Wand;


/**
 * ImageJ plugin to measure the areas of skeletal muscle fibers.
 *
 * @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
 */
public class CrossSectionalAnalyzer extends PlugInFrame implements Measurements{

	ImagePlus img;
	ImageProcessor ip;
	Wand wand;
	private static final int minDiameter = 3;
	private static final int traverseDistance = 5;
	//Record record;

	public CrossSectionalAnalyzer() {
		// super("CrossSectionalAnalyzer");
		// TextArea ta = new TextArea(15, 50);
		// add(ta);
		// pack();
		// GUI.center(this);
		// show();

		initializeImage();
		//Create a new wand
			this.wand = new Wand(ip);
		//Determine the traverseDistance based on the magnification and image setDimensions
		//Determine minDiameter in same method as traverseDistance
		//Create a new record
		this.record = new Record();
	}


	void run(ImageProcessor ip){
		wand.autoOutline(10,10);
	}

	private void initializeImage(){
		this.img = WindowManager.getCurrentImage();
    if (img==null){
      IJ.noImage();
    }
		else if (img.getStackSize() == 1) {
      this.ip = img.getProcessor();
      ip.resetRoi();
			//WTF is this?
      // counterImg = new ImagePlus("Counter Window - "+img.getTitle(), ip);
      // Vector displayList = v139t?img.getCanvas().getDisplayList():null;
      // ic = new CellCntrImageCanvas(counterImg,typeVector,this,displayList);
      // new ImageWindow(counterImg, ic);
    }
		else if (img.getStackSize() > 1){
      ImageStack stack = img.getStack();
      int size = stack.getSize();
      ImageStack counterStack = img.createEmptyStack();
      for (int i = 1; i <= size; i++){
        ImageProcessor ip = stack.getProcessor(i);
        // counterStack.addSlice(stack.getSliceLabel(i), ip);
			}
    }
      // counterImg = new ImagePlus("Counter Window - "+img.getTitle(), counterStack);
      // counterImg.setDimensions(img.getNChannels(), img.getNSlices(), img.getNFrames());
      // if (img.isComposite()) {
      //     counterImg = new CompositeImage(counterImg, ((CompositeImage)img).getMode());
      //     ((CompositeImage) counterImg).copyLuts(img);
      // }
      // counterImg.setOpenAsHyperStack(img.isHyperStack());
      // Vector displayList = v139t?img.getCanvas().getDisplayList():null;
      // ic = new CellCntrImageCanvas(counterImg,typeVector,this,displayList);
      // new StackWindow(counterImg, ic);
  }
}


}
