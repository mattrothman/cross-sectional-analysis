//To do
//Create wrapper class "CellCollection" or something like that, holds a hashmap of cells, where key is the number label, value is the cell object

//import java.util.ArrayList;
import ij.plugin.filter.Analyzer;
import java.awt.Polygon;

/**
Cell class holds a representation of a cell by storing a list of x and y coordinates of its parimeter
*/

class Cell {
   //private int[] xpoints;
   //private int[] ypoints;
   private int cellId;
   private double area;
   private Polygon shape;
   //declare a center coordinate

   public Cell(int[] xpoints, int[] ypoints, int newCellId) { //(int[] newXpoints, int[] newYpoints, int newCellId) {
       //xpoints = newXpoints;
       //ypoints = newYpoints;
       Polygon cell = new Polygon(xpoints, ypoints, xpoints.length);
       shape = cell;
       cellId = newCellId;
       area = calculateArea(shape);
   }

   public boolean inside(int ypoint, int xpoint) {
       return shape.inside(xpoint, ypoint);

   }

   public double calculateArea(Polygon shape) {
       return Analyzer.getArea(shape);
   }

   public boolean roundness(){
        // roundness = area/(length^2)
       //Polygon.getBounds()??
   }

   public int getCellId() { return cellId; }
   public double getArea() { return area; }

}

/**
Variables:
 xCoordinates //array of x-coordinates that correspond with the outside parimeter of the cell
 yCoordinates //array of x-coordinates that correspond with the outside parimeter of the cell
 cellNumber //??? holds the number idenifier assocated with the cell? do you guys think this is a good idea??

Methods:
   inside --> parameters: x and y coordinates, returns: boolean //determines whether a given coordinate is inside the cell, modification of the 'inside' function in wand

   roundness --> parameters: ?, returns: boolean?  //determines if the cell is suffincently round (maybe there should be some class variables that hold standards for roundess?)

   calculateArea --> parameters: none?, returns: double //calculates area using the area function built into imageJ
*/
