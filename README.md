# Cross-Sectional-Analyzer

## Installing and running the plugin
1. Download this github repository to your computer.
2. Unzip the folder, locate Cross_Sectional_Analyzer.jar.
3. Open the package contents of ImageJ, and navigate to ImageJ/plugins/Filters.
4. Put Cross_Sectional_Analyzer.jar inside the ImageJ/plugins/Filters folder, and restart imageJ.
5. Open ImageJ, and open an image to analyze. To run the plugin, use Plugins>Filters>Cross Sectional Analyzer.

## Creating the .jar and updating your copy in ImageJ (only applicable to those editing Cross-Sectional-Analyzer)
1. Turn all .java to .class by using imageJ's compile and run

2. Inside working directory, use command line to create jar executable

    `jar cvfm Cross_Sectional_Analyzer.jar manifest.txt *.class`

3. Copy and paste jar into plugin folder of choice

    `cp /Users/UserName/documents/github/cross-sectional-analysis/Cross_Sectional_Analyzer.jar /Users/UserName/desktop/ImageJ.app/plugins/filters/Cross_Sectional_Analyzer.jar`
