DEPENDENCIES

All of these should be present in the 'lib' folder:

opencv-343.jar
fontbox-2.0.8.jar
org-apache-commons.io.jar
org-apache-commons-lang.jar (WordUtil)
pdfbox-2.0.8.jar
pdfbox-app-2.0.8.jar
pdfbox-tools-2.0.8.jar
preflight-2.0.8.jar
preflight-app-2.0.8.jar
snakeyaml-1.19.jar
xmpbox-2.0.8.jar
yamlbeans-1.0.jar
yamlbeans-1.06-sources.jar

Because of the size requirements of artefact submission, I couldn't include these dependencies.
But all of them can be found in the MultiQuestions git repo.
In case of any running issues, that git can be pulled and run.
Note: Running opencv can be a bit tricky as it also requires the native library location.
The original set up had the entire opencv folder inside the 'lib' folder (with 'build' and 'sources' folders inside).
The folder could not be included because of the sheer size of the compressed file.
Here's an article that's helpful in setting up OpenCV for IntelliJ:
https://medium.com/@aadimator/how-to-set-up-opencv-in-intellij-idea-6eb103c1d45c


RUNNING THE PROGRAM

1. Open in IDE.

2. Compile and run.

Or do so using command line. No arguments required during execution.

USER GUIDE

The following is a brief guide on using the system.

 - Execute the java file. This should make the program open up.

 - Click on File Menu

 - Select an option that you need to use.

 - Select Generate New Quiz to generate a new pdf quiz

 - Navigate to the directory that contains your yaml questions

 - Select the yaml file

 - Enter a desired name for your resultant pdf file

 - If the file was successfully, it will be indicated in a fresh dialog box

 - This newly generated file can be found in the same directory as the yaml.

 Alternatively select Mark Scanned Quiz. Select your scan, and the    processed, validated image will be displayed in the GUI window with a pink ring around the detected answers.

LIST OF ALL CLASSES

  - Temp_container
  - Page_Setup
  - HoughCirclesRun
  - Main

ALL CODE REFERENCES

https://stackoverflow.com/questions/42811353/adding-of-filled-circles-to-pdf-page-using-apache-pdfbox

https://pdfbox.apache.org/1.8/cookbook/documentcreation.html

https://www.tutorialspoint.com/pdfbox/pdfbox_adding_rectangles.htm

https://docs.opencv.org/2.4/doc/tutorials/imgproc/imgtrans/hough_circle/hough_circle.html

https://docs.opencv.org/3.3.1/d4/d70/tutorial_hough_circle.html

https://stackoverflow.com/questions/6723929/how-can-you-produce-sharp-paint-results-when-rotating-a-bufferedimage

ALL REQUIRED IMPORT STATEMENTS

import net.sourceforge.yamlbeans.YamlReader;
import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

SYSTEM SPECIFICATIONS

Processor       : Intel Core i5-6500 CPU @ 3.20 GHz
Installed RAM   : 16.0 GB
System type     : 64-bit operating system, x64-based Processor
OS              : Windows 10 Education
Build Config    : JDK 1.8
