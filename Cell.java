
import java.awt.Polygon;
import java.lang.Math;
import java.util.Arrays;
import ij.*;


/**
Cell class holds a representation of a cell by storing a list of x and y coordinates of its perimeter`
*/


class Cell {

  private double area;  // Area of the cell in pixels
  private Polygon shape; // Polygon shape- contains an array of x and y points that make the coordinates of the cell
  private int centerX; // The x coordinate of the cell number label
  private int centerY; // The y coordinate of the cell number label
  public int cellNum; // The cell number



  public Cell(int[] xpoints, int[] ypoints, int cellNum) {
    //Wand returns polygon with unwanted trailing 0's at the end
    int end = xpoints.length;
    for (int i = xpoints.length -1; i > 0; i--) { //loops through and finds the index where the 0's start
      if ((xpoints[i] != 0) || (ypoints[i] != 0)){
        end = i + 1;
        break;
      }
    }
    //copies over meaningful part of the array so we only get actual coordinates in our cells
    int[] xp = Arrays.copyOfRange(xpoints, 0, end);
    int[] yp = Arrays.copyOfRange(ypoints, 0, end);
    Polygon cell = new Polygon(xp, yp, xp.length);
    this.shape = cell;
    this.area = calculateArea(shape);
    centerPoints();
    this.cellNum = cellNum;
  }


  /**
   * Centers the point in the middle of the cell so the cell number is in a good spot. This value is stored in the
   * centerX and centerY
   */
  public void centerPoints() {
    int countx = 0;
    int county = 0;
    for (int i = 0; i < shape.npoints; i++) { //adds up all the x and y points in the cell
      countx += shape.xpoints[i];
      county += shape.ypoints[i];
    }
    centerX = countx/shape.npoints; // divides total x by number of points to get the average x
    centerY = county/shape.npoints; // divides total y by number of points to get the average y
  }


  public void updateCellNum(int cellNum) {
      this.cellNum = cellNum;
  }


  /**
   * Copied over from a private method in imageJ: Finds the number of pixels in the cell. The real area can be calculated
   * by multiplying the number of pixels by the calibrated area of each pixel.
   */
  public static final double calculateArea(Polygon p) {
    if (p==null) return Double.NaN;
    int carea = 0;
    int iminus1;
    for (int i=0; i<p.npoints; i++) {
      iminus1 = i-1;
      if (iminus1<0) iminus1=p.npoints-1;
      carea += (p.xpoints[i]+p.xpoints[iminus1])*(p.ypoints[i]-p.ypoints[iminus1]);
    }
    return (Math.abs(carea/2.0));
  }

  public int getcenterX(){
    return centerX;
  }

  public int getcenterY(){
    return centerY;
  }

  public int getcellNum(){
    return cellNum;
  }

  public double getArea() { return area; }

  public Polygon getShape() { return shape; }

  //*****************************DEBGUGGING METHODS BELOW

  /**
   * Creates a log of total points so that they can easily be plotted in excel for debugging purposes
   */
  public void logPoints(){

    IJ.log("\tX Points: ");
    for (int xpoint : shape.xpoints)
    {
      IJ.log(Integer.toString(xpoint));
    }
    IJ.log("\tY Points: ");
    for (int ypoint : shape.ypoints)
    {
      IJ.log(Integer.toString(ypoint));
    }

  }

  /**
   * ToString method that prints the x and y point arrays
   * @return
   */
  public String toString(){
    String toString = "X-Points: " + Arrays.toString(shape.xpoints) + "\n Y-Points: " + Arrays.toString(shape.ypoints);
    return toString;

  }


}
