/**
Preprocesses image to sharpen the contrast and make it easier to distinguish cells.
Things to consider: if the image is already preprocessed by the user danger in overprocessing? --> processing thickens cell 
    border lines. Create inaccurate data?
*/

/**
Variables:
    image //cell image
    
Methods:

    makeBlackAndWhite //uses functionality built into ImageJ to convert the image to black and white
    
    adjustContrast //uses functionality built into ImageJ to convert the image to adjust the contrast. If you decrease
                     minimum and maximum and increase contrast you get a much crisper image
        link to ImageJ code in github: https://github.com/imagej/imagej1/blob/c1045944023a30da378d0e8996c6efb44bd9d9b7/ij/plugin/frame/ContrastAdjuster.java
    
    
    
*/
