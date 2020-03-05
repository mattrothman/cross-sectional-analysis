# cross-sectional-analysis

## Creating the .jar
1. Turn all .java to .class by using imageJ's compile and run

2. Inside working directory, use command line to create jar executable

    `jar cvfm Cross_Sectional_Analyzer.jar manifest.txt *.class`

3. Copy and paste jar into plugin folder of choice
    cp /Users/Mattrothman/documents/github/cross-sectional-analysis/Cross_Sectional_Analyzer.jar /Users/Mattrothman/desktop/ImageJ.app/plugins/filters/Cross_Sectional_Analyzer.jar
