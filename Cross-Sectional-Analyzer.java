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

/**
 * ImageJ plugin to measure the areas of skeletal muscle fibers.
 *
 * @author Matthew Rothman, Nalin Richardson, Thalia Barr-Malec
 */
public class Cross-Sectional-Analyzer extends PlugInFrame implements Measurements{

	public Plugin_Frame() {
		super("Plugin_Frame");
		TextArea ta = new TextArea(15, 50);
		add(ta);
		pack();
		GUI.center(this);
		show();
	}

}
