import java.util.ArrayList;
import java.util.Arrays;
import ij.*;
import ij.measure.ResultsTable;

/**
 * Record class keeps a list of that are created by traverser. A ResultsTable of cell stats can be printed as well.
 */

class Record {

  // TO DO: rename arraySharesPoints
  public ArrayList<Cell> cells;
  private static final boolean DEBUG = false;

  public Record() {
    this.cells = new ArrayList<Cell>();
  }



  /**
   * Adds a cell to record
   */

  public void addCell(Cell cell) {
    int cellNum = cells.size() + 1;
    cell.updateCellNum(cellNum);
    cells.add(cell);
  }

  /**
   * Checks to see if a cell is the same as a prexisting cell in traverser by testing if the cell shares all
   * coordinates with another cell in record.
   **/
  public boolean arraySharesPoints(Cell c) {
    for (Cell cell : cells) {
      if (Arrays.equals(c.getShape().xpoints, cell.getShape().xpoints)) { // && Arrays.equals(c.getShape().ypoints, cell.getShape().ypoints)) {

        return true;
      }
    }
    return false;

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
    if(DEBUG) IJ.log("Cell " + (index+1) + " is gone. Now record has " + size() + " cells.");
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

  /**
   * Creates a Results table to print stats of all the cell's areas. Table has values of cell number and area.
   * @param pixelSize
   * @param unit
   */
  public void createTable(double pixelSize, String unit) {

    ResultsTable table = new ResultsTable();
    for (Cell cell : cells) {
      table.incrementCounter();
      table.addValue("Cell #", cell.cellNum);
      table.addValue("Area (square " + unit + ")", cell.getArea() * pixelSize * pixelSize);
    }
    String title = "Results";
    table.show(title);
  }

  /**
   * Renumbers cells so that the numbers reflect the order in which they were created
   */
  public void renumberCells() {
    int num = 1;
    for (Cell cell : cells) {
      cell.updateCellNum(num);
      num++;
    }
  }
}
