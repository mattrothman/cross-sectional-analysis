import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

class Record {

    // To-do: decide if this is the right data structure for the job
    public ArrayList<Cell> cells;

    public Record() {
        cells = new ArrayList<>();
    }

    // We could make this return either the cell itself or the current record if we wanted to
    public void addCell(Cell cellToAdd) {
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
     * @return
     */
    public boolean cellExists(int x, int y) {

        for (int i = 0; i < cells.size(); i ++) {
            Cell currCell = cells.get(i);
            if (currCell.contains(x,y)) {
                return true;
            }
        }
        return false;
    }
}


//     /**
//      * Checks to see if there is already an existing cell in record that contains the given point.
//      * @param x
//      * @param y
//      * @return
//      */
//     //FINISH
//     public int cellCellId(int x, int y) {

//         for (int current : cells.keySet()) {
//             Cell currCell = cells.get(current);
//             if (currCell.contains(x,y)) {
//                 return current;
//             }
//         }
//         return -1;
//     }



/**
Holds all the Cell objects and prints a report with the cells and areas.
*/

/**
Variables:
    cells //holds an array of Cell objects? Matt I think you were talking about using a hashmap. Feel free to change this
          //After thinking about the issue of changing the cell numberings, i think an arrayList might be our best bet, to begin with
Methods:

    addCell //adds a Cell object to the cells variable
    
    printData //creates file and writes each cell number with it's corresponding area
*/
