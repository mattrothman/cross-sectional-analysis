//To do
//Create wrapper class "CellCollection" or something like that, holds a hashmap of cells, where key is the number label, value is the cell object

//import java.util.ArrayList;
import ij.plugin.filter.Analyzer;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.lang.Math;
import java.util.Arrays;

/**
Cell class holds a representation of a cell by storing a list of x and y coordinates of its parimeter
*/

class Cell {
   //private int[] xpoints;
   //private int[] ypoints;
   private double area;
   private Polygon shape;
   //https://books.google.com/books?id=YEm5BQAAQBAJ&pg=PA735&lpg=PA735&dq=roundness+range+of+cells&source=bl&ots=EnoKFIoelk&sig=ACfU3U0FxVJIPjL3KEJtNMSTg0wchFnIWw&hl=en&ppis=_c&sa=X&ved=2ahUKEwiV1fDO8r3nAhUUoZ4KHY9DA_IQ6AEwDXoECAoQAQ#v=onepage&q=roundness%20range%20of%20cells&f=false
   //Acorrding to link, roundness or a normal cell is 0.43-0.97
   //and roundness of an abnormal cell is 0.06-0.87
   //Starting value of
   private static final double minRoundness  = 0.2; //This will change
   //declare a center coordinate

   public Cell(int[] xpoints, int[] ypoints) { //(int[] newXpoints, int[] newYpoints, int newCellId) {
       //xpoints = newXpoints;
       //ypoints = newYpoints;
       Polygon cell = new Polygon(xpoints, ypoints, xpoints.length);
       shape = cell;
       area = calculateArea(shape);
   }

   public boolean contains(int xpoint, int ypoint) {
       //Deprecated. As of JDK version 1.1, replaced by contains(int, int).
       //return shape.inside(xpoint, ypoint);
       return shape.contains(xpoint, ypoint);

   }
// Unfortunately the imageJ method using getArea was private so I copied the code over.
    public final double calculateArea(Polygon p) {
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

   public String toString(){
       String toString = "X-Points: " + Arrays.toString(shape.xpoints) + "\n Y-Points: " + Arrays.toString(shape.xpoints);
       return toString;

   }

}
