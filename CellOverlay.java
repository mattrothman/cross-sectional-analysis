import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.awt.*;

/**
 * Cell Overlay contains methods for drawing a cell overlay that could
 * later be used merge onto a stain that hs unclear cell boarders
 *
 * @author Nalin Richardson, Matthew Rothman, Thalia Barr-Malec
 */
public class CellOverlay {
    public ImagePlus imp;
    public Record rec;
    public Color overLayColor;

    /**
     * Contructor for Cell Overlay
     * @param cells The Record that we will draw from
     * @param height Height of the new image
     * @param width Width of the new image
     * @param color Color to draw the cell outlines
     */
    public CellOverlay(Record cells, int height, int width, Color color) {
        imp = NewImage.createRGBImage("Overlay", width, height, 1, 6);
        rec = cells;
        overLayColor = color;
    }

    /**
     * Creates a new overlay and saves it as a tiff file
     */
    public void createAndSave() {
        ImageProcessor ip = imp.getProcessor();
        ip.setLineWidth(2);
        ip.setFontSize(20);
        if (overLayColor != null) {
            ip.setColor(overLayColor);
        } else {
            ip.setColor(new Color(0x03fcf4));
        }

        drawCellsAndNums(ip, rec);
        imp.updateAndDraw();
        imp.show();
        save(imp);
    }

    /**
     * Saves an image as a tiff
     * @param imp The image to save
     */
    public void save(ImagePlus imp) {
        FileSaver fs = new FileSaver(imp);
        fs.saveAsTiff();
    }

    /**
     * Using the ImageProcessor, draws cells from Record and draws their numbers within each cell
     * @param ip ImageProcessor from image on which we want to draw
     * @param cellRec Record containg cells we want to draw
     */
    public void drawCellsAndNums(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            ip.drawPolygon(curr.getShape());
            String num = Integer.toString(curr.getcellNum());
            ip.drawString(num, curr.getstartx(), curr.getstarty());
        }
    }

    /**
     * Only draws Cells
     * @param ip ImageProcessor from image on which we want to draw
     * @param cellRec Record containg cells we want to draw
     */
    public void drawCells(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            ip.drawPolygon(curr.getShape());
        }
    }

    /**
     * Draws each cell number inside of each cell
     * @param ip ImageProcessor from image on which we want to draw
     * @param cellRec Record containg numbers we want to draw
     */
    public void drawNums(ImageProcessor ip, Record cellRec) {
        for (Cell curr : cellRec.cells) {
            String num = Integer.toString(curr.getcellNum());
            ip.drawString(num, curr.getstartx(), curr.getstarty());
        }
    }


    public ImagePlus getImp() {
        return imp;
    }
}
