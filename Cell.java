//To do
//Create wrapper class "CellCollection" or something like that, holds a hashmap of cells, where key is the number label, value is the cell object

//import java.util.ArrayList;
import ij.plugin.filter.Analyzer;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.lang.Math;
import java.util.Arrays;
import ij.*;
import java.util.ArrayList;
import java.util.*;


/**
Cell class holds a representation of a cell by storing a list of x and y coordinates of its parimeter
*/

class Cell {
  //private int[] xpoints;
  //private int[] ypoints;
  private double area;
  private Polygon shape;
  private int startx;
  private int starty;
  public int cellNum;
  //https://books.google.com/books?id=YEm5BQAAQBAJ&pg=PA735&lpg=PA735&dq=roundness+range+of+cells&source=bl&ots=EnoKFIoelk&sig=ACfU3U0FxVJIPjL3KEJtNMSTg0wchFnIWw&hl=en&ppis=_c&sa=X&ved=2ahUKEwiV1fDO8r3nAhUUoZ4KHY9DA_IQ6AEwDXoECAoQAQ#v=onepage&q=roundness%20range%20of%20cells&f=false
  //Acorrding to link, roundness or a normal cell is 0.43-0.97
  //and roundness of an abnormal cell is 0.06-0.87
  //Starting value of
  private static final double minRoundness  = 0.2; //This will change
  //declare a center coordinate

  public Cell(int[] xpoints, int[] ypoints, int startx, int starty, int cellNum) { //(int[] newXpoints, int[] newYpoints, int newCellId) {
    int end = xpoints.length;
    for (int i = xpoints.length -1; i > 0; i--) {
      if ((xpoints[i] != 0) || (ypoints[i] != 0)){
        end = i + 1;
        break;
      }
    }
    int[] xp = Arrays.copyOfRange(xpoints, 0, end);
    int[] yp = Arrays.copyOfRange(ypoints, 0, end);
    Polygon cell = new Polygon(xp, yp, xp.length);
    this.shape = cell;
    this.area = calculateArea(shape);
    centerPoints();
    this.cellNum = cellNum;
  }

  public boolean contains(int xpoint, int ypoint) {
    return shape.contains(xpoint, ypoint);

  }


  /**
   * Centers the point made with startx and starty so the cell number is in a good spot.
   */
  public void centerPoints() {
    int countx = 0;
    int county = 0;
    for (int i = 0; i < shape.npoints; i++) {
      countx += shape.xpoints[i];
      county += shape.ypoints[i];
    }
    startx = countx/shape.npoints;
    starty = county/shape.npoints;
  }
//
//  public static double pixelArea(Polygon shape) {
//
//      int areaTotal = 0;
//      for (int i = 0; i < shape.npoints - 2; i++) {
//        areaTotal += -1 * shape.ypoints[i] * shape.xpoints[i+1] + shape.xpoints[i] * shape.ypoints[i+1];
//      }
//      areaTotal += -1 * shape.ypoints[shape.npoints-1] * shape.xpoints[0] + shape.xpoints[shape.npoints-1] * shape.ypoints[0];
//      areaTotal += Math.abs(areaTotal);
//      return areaTotal;
////    area = 0
////    for i in [0, N - 2]:
////    area += -p[i].y * p[i+1].x + p[i].x * p[i+1].y
////    area += -p[N-1].y * p[0].x + p[N-1].x * p[0].y
////    area = 0.5 * abs(area)
//
//  }

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

  public Polygon reducePoints(int reduction) {
    int reducedLength = shape.npoints/reduction + 1;
    int[] reducedX = new int[reducedLength];
    int[] reducedY = new int[reducedLength];

    for (int i = 0; i < shape.npoints; i += reduction) {
      reducedX[i/reduction] = shape.xpoints[i];
      reducedY[i/reduction] = shape.ypoints[i];

    }
    Polygon reduced = new Polygon(reducedX, reducedY, reducedLength);
    return reduced;

  }



  public void updateCellNum(int cellNum) {
      this.cellNum = cellNum;
  }
  /**
  * Returns true if either:
  *   (1) p's outline has points that make up >= 20% of shape's outline
  *   (2) shape's outline has points that make up >= 20% of the outline of p
  * @param p
  * @return Whether p is the same cell as this cell
  */
  public boolean sameCell(Polygon p) {
    int n = shape.npoints;
    if(p.npoints < n) n = p.npoints;

    int[] px = p.xpoints;
    int[] py = p.ypoints;
    int[] sx = shape.xpoints;
    int[] sy = shape.ypoints;


    double samePoints = 0;
    for (int i = 0; i < n; i++){
      if((px[i] == sx[i]) && (py[i] == sy[i])){
        samePoints++;
      }
    }

    double shape_npoints = (double) (shape.npoints);
    double p_npoints = (double) (p.npoints);
    // If the shared points make up >= 20% of shape's outline, return true
    if (samePoints/shape_npoints >= 0.2) return true;
    // If the shared points make up >= 20% of p's outline, return true
    if (samePoints/p_npoints >= 0.2) return true;
    return false;
  }

  // Unfortunately the imageJ method using getArea was private so I copied the code over.
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

  //DEBGUGGING METHOD
  public int getstartx(){
    return startx;
  }

  //DEBGUGGING METHOD
  public int getstarty(){
    return starty;
  }

  //DEBGUGGING METHOD
  public int getcellNum(){
    return cellNum;
  }

  //https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3544517/
  //Roundness is based on the above article
  public boolean roundness(){
    Rectangle bounds = shape.getBounds();
    double height = bounds.getHeight();
    double width = bounds.getWidth();
    double length = width;
    if(height > width){
      length = height;
    }
    double roundness = this.area/(Math.pow(length, 2.0));
    if (roundness < minRoundness){
      return false;
    }
    else{
      return true;
    }
  }

  //This method will not be used in final implementation!
  //This method exists so we can get a range for the roundness or our cells
  //So that we can adjust our minRoundness value
  public double calcRoundness(){
    Rectangle bounds = shape.getBounds();
    double height = bounds.getHeight();
    double width = bounds.getWidth();
    double length = width;
    if(height > width){
      length = height;
    }
    double roundness = this.area/(Math.pow(length, 2.0));
    return roundness;
  }

  public double getArea() { return area; }

  public Polygon getShape() { return shape; }

  // @Override
	// @SuppressWarnings("unchecked")
  public Boolean equals(Cell c){
    Polygon p = c.getShape();
    int[] xp = p.xpoints;
    int[] yp = p.ypoints;
    int overlap = 0;
    for (int i = 0; i < p.npoints; i++){
      if (contains(xp[i], yp[i])) overlap++;
      if (overlap > 5) return true;
    }
    for (int i = 0; i < shape.npoints; i++){
      if (c.contains(shape.xpoints[i], shape.ypoints[i])) overlap++;
      if (overlap > 5) return true;
    }
    if(area == c.getArea()) return true;
    if((area - c.getArea() <= 2) || (-area + c.getArea() <= 2)) return true;
    else return false;
  }


  public String toString(){
    String toString = "X-Points: " + Arrays.toString(shape.xpoints) + "\n Y-Points: " + Arrays.toString(shape.ypoints);
    return toString;

  }

}
