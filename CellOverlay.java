import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.awt.*;

public class CellOverlay {
    public ImagePlus imp;
    public Record rec;
    public String color;

    public CellOverlay(Record cells, int height, int width, String color) {
        imp = NewImage.createRGBImage("Overlay", width, height, 1, 6);
        rec = cells;
        this.color = color;
    }

    public void createAndSave() {
        ImageProcessor ip = imp.getProcessor();
        ip.setLineWidth(2);
        ip.setFontSize(20);

        if (color.equals("blue")) {
            ip.setColor(new Color(0x03fcf4));
        } else if (color.equals("red")) {
            ip.setColor(new Color(0xff0000));
        } else {
            ip.setColor(new Color(0x00fc3d));
        }

        drawCellsAndNums(ip, rec);
        imp.show();
        save(imp);
    }

    public void save(ImagePlus imp) {
        FileSaver fs = new FileSaver(imp);
        fs.saveAsTiff();
    }

    public void drawCellsAndNums(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            ip.drawPolygon(curr.getShape());
            String num = Integer.toString(curr.getcellNum());
            ip.drawString(num, curr.getstartx(), curr.getstarty());
        }
        imp.updateAndDraw();
    }

    public void drawCells(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            ip.drawPolygon(curr.getShape());
        }
        imp.updateAndDraw();
    }

    public void drawNums(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            String num = Integer.toString(curr.getcellNum());
            ip.drawString(num, curr.getstartx(), curr.getstarty());
        }
        imp.updateAndDraw();
    }


    public ImagePlus getImp() {
        return imp;
    }
}
