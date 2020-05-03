import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.awt.Polygon;
import java.util.Arrays;
import ij.*;
import ij.measure.ResultsTable;


class Record {

  // To-do: decide if this is the right data structure for the job
  public ArrayList<Cell> cells;

  public Record() {
    this.cells = new ArrayList<Cell>();
  }

  // We could make this return either the cell itself or the current record if we wanted to

  /**
   * public void addCell(int[] xpoints, int[] ypoints, int startx, int starty) {
   * int cellNum = cells.size() + 1;
   * Cell cellToAdd = new Cell(xpoints, ypoints, startx, starty, cellNum);
   * cells.add(cellToAdd);
   * }
   */

  public void addCell(Cell cell) {
    int cellNum = cells.size() + 1;
    cell.updateCellNum(cellNum);
    cells.add(cell);
  }

  /**
   * public boolean cellsSharePoints(Cell c1, Cell c2) {
   * if (Arrays.equals(c1.getShape().xpoints, c2.getShape().xpoints) &&  Arrays.equals(c1.getShape().ypoints, c2.getShape().ypoints)) {
   * return true;
   * }
   * return false;
   * }
   **/
  public boolean arraySharesPoints(Cell c) {
    for (Cell cell : cells) {
      if (Arrays.equals(c.getShape().xpoints, cell.getShape().xpoints)) { // && Arrays.equals(c.getShape().ypoints, cell.getShape().ypoints)) {
//          IJ.log("\tArrays are equal");
        return true;
      }
//      IJ.log(Arrays.toString(c.getShape().xpoints));
//      IJ.log(Arrays.toString(cell.getShape().xpoints));
//      IJ.log("\t Arrays are not equal");
    }
    return false;

  }


  /**
   * Checks to see if the last cell .equals() any other cell in the record..
   *
   * @return Returns the index of the cell that .equals() the last cell, or else returns -1.
   */
  public int equals() {
    Cell c = getLastCell();
    for (int i = 0; i < cells.size() - 1; i++) {
      if (cells.get(i).equals(c)) return (i + 1);
    }
    return -1;
  }

  /**
   * Returns which cell in record contains the given point.
   *
   * @param p
   * @return Returns the cell in record that contains the given point, or else returns -1.
   */
  public int whichCell(int x, int y) {
    for (int i = 0; i < cells.size(); i++) {
      Cell currCell = cells.get(i);
      if (currCell.contains(x, y)) {
        return (currCell.getcellNum());
      }
    }
    return -1;
  }

  /**
   * Returns the last cell.
   *
   * @return cell  The last cell added.
   */
  public Cell getLastCell() {
    return cells.get(cells.size() - 1);
  }

  /**
   * Returns the size() of cells.
   *
   * @return size  The size() of cells.
   */
  public int size() {
    return cells.size();
  }

  /**
   * Removes the cell at index in cells.
   */
  public void removeCell(int index) {
    cells.remove(index);
    IJ.log("Cell " + (index+1) + " is gone. Now record has " + size() + " cells.");
  }

  /**
   * Removes the last cell from cells.
   */
  public void removeLastCell() {
    cells.remove(cells.size() - 1);
  }

  /**
   * Checks to see if we're about to draw overlapping cell nums (hopefully will catch repeat cells missed by arraySharesPoints)
   */
  public Boolean sameCenterPoints(Cell cell) {
    int startx = cell.getstartx();
    int starty = cell.getstarty();
    for (int i = 0; i < cells.size(); i++) {
      Cell currCell = cells.get(i);
      if (currCell.getstartx()==startx && currCell.getstarty()==starty) {
        return (true);
      }
    }
    return false;
  }

  public void createTable() {
    //new ResultsTable
    ///** Adds a value to the end of the given column. If the column
    //		does not exist, it is created.
    //		There is an example at:<br>
    //		http://imagej.nih.gov/ij/plugins/sine-cosine.html
    //	*/
    //	public void addValue(String column, double value)

    ///** Constructs a ResultsTable with 'nRows' rows. */
    //	public ResultsTable(Integer nRows) {
    //		init();
    //		for (int i=0; i<nRows; i++)
    //			incrementCounter();
    //	}

    ResultsTable table = new ResultsTable();
    for (Cell cell : cells) {
      table.incrementCounter();
      table.addValue("Cell #", cell.cellNum);
      table.addValue("Area", cell.getArea());
      table.addValue("Number of Points", cell.getShape().npoints);
    }
    String title = "Results";
    table.show(title);
  }

  public void renumberCells() {
    int num = 1;
    for (Cell cell : cells) {
      cell.updateCellNum(num);
      num++;
    }
  }
}
