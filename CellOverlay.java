import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.awt.*;

import static java.lang.Float.NaN;


public class CellOverlay {
    public ImagePlus imp;
    public Record rec;

    public CellOverlay(Record cells, int height, int width) {
        imp = NewImage.createRGBImage("Overlay", width, height, 1, 6);
        rec = cells;
    }

    public void create() {
        ImageProcessor ip = imp.getProcessor();
        ip.setLineWidth(2);
        ip.setColor(new Color(0x03fcf4));
        for (Cell curr : rec.cells) {
            ip.drawPolygon(curr.getShape());
        }
        imp.updateAndDraw();

        FileSaver fs = new FileSaver(imp);
        fs.saveAsTiff();
    }

    public ImagePlus getImp() {
        return imp;
    }
}
