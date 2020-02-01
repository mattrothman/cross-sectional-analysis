//Create Traverser class that traverses image, from left to right, searching for cells

/**

Variables:
    clickDistance //holds the distance between clicks (pixels)
    location //holds the current location (x,y coordinate) of where it is in the image
    imageBoundary //holds the image boundary coordinates
    minDiameter //holds the minimum diameter of a cell. In otherwards, how big of a diameter must be able to fit somewhere in the cell outline
    image //holds the cell image
 
Methods:
    
    traverse //traverses to the next point in the image (calls isNextEdge, if the next right is past the edge, traverses down), 
                checks if the coordinate is already inside an existing cell, checks diameter, if the cell does
                not already exist and the maximum diameter is acceptable then calls click which uses ImageJ's 
                wand, then calls addCell
                
                wand source code: https://github.com/imagej/imagej1/blob/master/ij/gui/Wand.java
    
    isRecorded //uses the Record class to check if a cell already exists in that coordinate. Calls the inside method of each recorded cell
    
    checkDiameter //checks to see if the maximum diameter is acceptable using the minDiameter variable
    
    addCell //creates a new Cell object and adds it to Record class
    
    isNextEdge //determines whether the next right click will be off the edge of the image
    
    
    
*/


