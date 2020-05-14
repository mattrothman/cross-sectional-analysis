
import java.awt.Polygon;
import java.awt.Rectangle;
import java.lang.Math;
import java.util.Arrays;
import ij.*;



/**
Cell class holds a representation of a cell by storing a list of x and y coordinates of its perimeter`
*/

//TO DO: change the name "startx" and "starty" to centerX and centerY
  //TO DO: startx and starty don't need to be in the constructor
  //TO DO: remove rectangle import

class Cell {

  private double area;  // Area of the cell in pixels
  private Polygon shape; // Polygon shape- contains an array of x and y points that make the coordinates of the cell
  private int startx; // The x coordinate of the cell number label
  private int starty; // The y coordinate of the cell number label
  public int cellNum; // The cell number



  public Cell(int[] xpoints, int[] ypoints, int startx, int starty, int cellNum) {
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
   * startx and starty
   */
  public void centerPoints() {
    int countx = 0;
    int county = 0;
    for (int i = 0; i < shape.npoints; i++) { //adds up all the x and y points in the cell
      countx += shape.xpoints[i];
      county += shape.ypoints[i];
    }
    startx = countx/shape.npoints; // divides total x by number of points to get the average x
    starty = county/shape.npoints; // divides total y by number of points to get the average y
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

  //*****************************DEBGUGGING METHODS BELOW

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

  public double getArea() { return area; }

  public Polygon getShape() { return shape; }


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
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  //This method will not be used in final implementation!
  //This method exists so we can get a range for the roundness or our cells
  //So that we can adjust our minRoundness value
  //FIND IN TRAVERSER
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

  // FIND IN TRAVERSER
  public boolean contains(int xpoint, int ypoint) {
    return shape.contains(xpoint, ypoint);

  }

  // @Override
  // @SuppressWarnings("unchecked")
  //DELETE IN TRAVERSER FIRST
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

}
