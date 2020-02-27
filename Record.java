import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

class Record {

  // To-do: decide if this is the right data structure for the job
  public ArrayList<Cell> cells;

  public Record() {
    this.cells = new ArrayList();
  }

  // We could make this return either the cell itself or the current record if we wanted to
  public void addCell(int[] xpoints, int[] ypoints, int startx, int starty) {
    Cell cellToAdd = new Cell(xpoints, ypoints, startx, starty);
    cells.add(cellToAdd);
  }

  public void printData(String fileLocation) throws FileNotFoundException {
    File data = new File(fileLocation);
    PrintWriter pw = new PrintWriter(data);

    // This is probably not the way we will want the text file to look. Some sort of table would be best.
    for (int i = 0; i < cells.size(); i++) {
      Cell currCell = cells.get(i);
      pw.println(i + ": " +currCell.getArea());
    }

    pw.close();
  }

  /**
  * Checks to see if there is already an existing cell in record that contains the given point.
  * @param x
  * @param y
  * @return Whether there exists a cell in record that contains the given point
  */
  public boolean cellExists(int x, int y) {
    for (int i = 0; i < cells.size(); i++) {
      Cell currCell = cells.get(i);
      if (currCell.contains(x,y)) {
        return true;
      }
    }
    return false;
  }

  /**
  * Returns the size() of cells.
  * @return size  The size() of cells.
  */
  public int size() {
    return cells.size();
  }
}
