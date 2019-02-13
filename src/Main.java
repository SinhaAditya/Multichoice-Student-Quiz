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

/**
 * Created by Aditya Sinha on 6/12/2017.
 * Supervisor: Dr. Eric McCreath
 * The following code is the artefact for the Multiple Choice Student Quiz Systems project
 * This project was created as a part of COMP4560, aimed at submission in Oct 2018
 */



/**
* Class Temp_Container objects act as temporary containers for
 * extracting questions and answers from the Yaml file
**/

class Temp_Container {

    String question_paper;
    List answers = new ArrayList<Character>();

    Temp_Container(String question_paper, List answers) {
        this.question_paper = question_paper;
        this.answers = answers;
    }

    String getQuestion_paper() {
        return question_paper;
    }

    List getAnswers() {
        return answers;
    }

}

/**
* Class Page_Setup is one of the three major components of this system.
 *
 * It deals with the structure and setup of the PDF file using the info obtained from the yaml file
 *
 * The drawCircle() method that prints circles using bezier curves was taken from Victor Semenovich's answer at Stack Overflow
 *
 * Here's the link: https://stackoverflow.com/questions/42811353/adding-of-filled-circles-to-pdf-page-using-apache-pdfbox
 *
 * Some small snippets in border-making and text printing were inspired by some code in apache's pdf box docs and Tutorialspoint
 *
 * Here are the links: https://pdfbox.apache.org/1.8/cookbook/documentcreation.html
 *                     https://www.tutorialspoint.com/pdfbox/pdfbox_adding_rectangles.htm
* */

class Page_Setup {

    float page_height;
    float page_width;
    int question_xpos;                                                                     // x-coordinate of cursor
    int ypos;                                                                             // y-coordinate of cursor
    int question_spacing;
    PDFont font;
    int fontSize;
    float line_spacing;
    int paragraph_width;
    int question_width;
    int choices_width;
    int mid_padding;
    int answer_xpos;
    float border_x;
    float border_y;
    float border_height;
    float border_width;
    ArrayList<Integer> question_heights;
    ArrayList<Integer> answer_heights;
    PDPageContentStream content;

    Page_Setup(PDPage page, PDPageContentStream content_stream) {

        float page_height = page.getMediaBox().getHeight();
        float page_width = page.getMediaBox().getWidth();
        question_xpos = (int) page_width/12;
        ypos = (int) (0.75*page_height);
        question_spacing = (int) (0.15*page_height);
        font = PDType1Font.HELVETICA;
        fontSize = 11;
        line_spacing = 14.5f;
        paragraph_width = 60;
        question_width = 50;
        choices_width = 50;
        mid_padding = 30;
        answer_xpos = 400;
        border_x = 25.0f;
        border_y = 25.0f;
        border_height = page_height - 50.0f;
        border_width = page_width - 50.0f;
        question_heights = new ArrayList<>();
        answer_heights = new ArrayList<>();
        content = content_stream;

    }

    // Make the page ready, adding border, setting font, stroke width, cursor position and so on.

    void make_setup_ready () throws IOException {

        content.addRect(border_x, border_y, border_width, border_height);
        content.setStrokingColor(Color.black);
        content.setLineWidth(3.0f);
        content.stroke();
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(question_xpos, ypos);
        content.setLeading(line_spacing);

    }

    // print questions on the page

    void print_questions (ArrayList<String> questions) throws IOException {

        for (String question: questions) {
            String[] q_lines = WordUtils.wrap(question, question_width).split("\\r?\\n");
            question_heights.add(q_lines.length);
            for (String line: q_lines) {
                content.showText(line);
                content.newLine();
            }
            content.newLineAtOffset(0, -question_spacing);
        }
        content.endText();

    }

    // print choices next to each question

    void print_answers (ArrayList<String> choices) throws IOException {

        content.beginText();
        content.newLineAtOffset(answer_xpos,ypos);
        /* Printing answers on the PDF... */
        int i = 0;
        for (String c: choices) {
            String[] temp = WordUtils.wrap(c, choices_width).split("\\r?\\n");
            answer_heights.add(temp.length);
            for (String t: temp) {
                content.showText(t);
                content.newLine();
            }
            content.newLineAtOffset(0, -(question_spacing + Math.abs(question_heights.get(i) - answer_heights.get(i)) * fontSize));
            i += 1;
        }
        content.endText();

    }

    // draw blank circles next to every choice

    void draw_circles (int circle_radius, int circle_padding) throws IOException {

        int correction_factor = (int) Math.ceil(0.2*fontSize);
        int circle_centreX = answer_xpos - circle_radius - circle_padding;
        int circle_centreY = ypos + fontSize - circle_radius - correction_factor;
        content.setLineWidth(1.0f);

        for (int i = 0; i < answer_heights.size(); i++) {
            for (int j = 0; j < 4; j++) {
                drawCircle(content, circle_centreX, circle_centreY, circle_radius);
                circle_centreY = circle_centreY - (int) line_spacing;
            }
            circle_centreY = circle_centreY - (question_spacing + Math.abs(question_heights.get(i) - answer_heights.get(i)) * fontSize) - correction_factor;
        }

    }

    // This method is called upon by draw_circles() to draw each individual circle using bezier curves

    private static void drawCircle(PDPageContentStream contentStream, int cx, int cy, int r) throws IOException {

        final float k = 0.552284749831f;
        contentStream.moveTo(cx - r, cy);
        contentStream.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
        contentStream.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
        contentStream.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
        contentStream.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
        contentStream.stroke();

    }

    double get_answer_position() {
        return answer_xpos;
    }

}

/**
 * The following class is the second major component of the system
 *
 * The code is a modified version of the Hough Circles code in the OpenCV official documentation
 *
 * Relevant links: https://docs.opencv.org/2.4/doc/tutorials/imgproc/imgtrans/hough_circle/hough_circle.html
 *                 https://docs.opencv.org/3.3.1/d4/d70/tutorial_hough_circle.html
 */

class HoughCirclesRun {

    public BufferedImage run(File file) {

        // Loading file
        Mat src = Imgcodecs.imread(file.getName(), Imgcodecs.IMREAD_UNCHANGED);
        Size sz = new Size(0,0);
        Imgproc.resize(src,src,sz, 0.4, 0.4);

        // Convert to Grey
        Mat gray = new Mat();
        Mat thr = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Make image binary
        Imgproc.threshold(gray, thr, 220,255,THRESH_BINARY);

        // Reduce noise
        Imgproc.medianBlur(gray, gray, 5);

        // Hough circles
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)gray.rows()/16,                           // change this value to detect circles with different distances to each other
                10.0, 30.0, 1, 50);     // change the last two parameters (min_radius & max_radius) to detect larger/smaller circles



        // Optional: Display detected circles' centres and radii
//        for (int i = 0; i < circles.cols(); i++) {
//            double[] c = circles.get(0, i);
//            System.out.println("x: "+ c[0]);
//            System.out.println("y: "+ c[1]);
//            System.out.println("r: "+ c[2]);
//        }


        // draw pink rings around detected circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            org.opencv.core.Point center = new org.opencv.core.Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(src, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(src, center, radius, new Scalar(255,0,255), 3, 8, 0 );
        }

        BufferedImage image = (BufferedImage) HighGui.toBufferedImage(src);
        return image;

    }
}

/**
 * This is the third and final component of the system
 * The Main class houses the main() method and all GUI components
 * Rendering hints code when deskewing image is taken from Stack overflow:
 * https://stackoverflow.com/questions/6723929/how-can-you-produce-sharp-paint-results-when-rotating-a-bufferedimage
 * */

public class Main implements Runnable {

    JFrame frame;
    JMenuBar menuBar;
    JMenu fileMenu;
    JMenuItem generate_new_quiz, mark_scanned_quiz, exit;
    JFileChooser fc;
    JPanel panel;
    JLabel scan_img;

    File yaml_file, scan_file;

    String question_paper, pdf_save_name;
    List answers;
    BufferedImage read_scan;

    public Main() {
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {

        frame = new JFrame("Student Multiple Choice Quiz System");
        panel = new JPanel(new BorderLayout());
        frame.setSize(1100,1100);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(panel);

        fc = new JFileChooser();
        fc.setCurrentDirectory(new File("C://"));
        fc.setDialogTitle("Open File");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        fileMenu = new JMenu("File");
        generate_new_quiz = new JMenuItem("Generate new quiz");
        mark_scanned_quiz = new JMenuItem("Mark scanned quiz");
        exit = new JMenuItem("Exit");
        fileMenu.add(generate_new_quiz);
        fileMenu.add(mark_scanned_quiz);
        fileMenu.add(exit);
        menuBar.add(fileMenu);

        // Action listener for when Generate new quiz is clicked
        generate_new_quiz.addActionListener(e -> {
            fc.showOpenDialog(null);
            yaml_file = fc.getSelectedFile();

            if (yaml_file.getName().endsWith(".yml")) {

                try {
                    YamlReader reader = new YamlReader(new FileReader(yaml_file.getName()));
                    Object object = reader.read();
                    question_paper = format_Yaml(object);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                Temp_Container temp_container = remove_and_store_answers(question_paper);
                question_paper = temp_container.getQuestion_paper();
                answers = temp_container.getAnswers();
                pdf_save_name = JOptionPane.showInputDialog("Enter quiz pdf name: ");
                try {
                    make_new_pdf(pdf_save_name, question_paper);
                }
                catch (IOException ioexception) {
                    ioexception.printStackTrace();
                    JOptionPane.showMessageDialog(null, "IO Error: Encountered exception while saving quiz as pdf.");
                }
                JOptionPane.showMessageDialog(null, "Quiz successfully saved as pdf file.");
            }
            else
                JOptionPane.showMessageDialog(null, "Invalid file type: The file you selected was not a yaml file.");
        });

        // Action Listener for when mark scanned quiz is clicked
        mark_scanned_quiz.addActionListener(e -> {

            fc.showOpenDialog(null);
            scan_file = fc.getSelectedFile();

            if(scan_file.getName().endsWith(".pdf")) {
                try {
                    create_png_from_pdf(scan_file);
                }
                catch (IOException ioexception) {
                    ioexception.printStackTrace();
                }
                scan_file = new File(scan_file.getName().substring(0,scan_file.getName().length() - 4)+".png");
            }
            try {
                read_scan = ImageIO.read(scan_file);
            }
            catch (IOException ioexception) {
                ioexception.printStackTrace();
            }

            int ap_1 = read_scan.getWidth()/3;
            int ap_2 = 2*ap_1;
            BufferedImage deskewed_image = deskew_image(read_scan, read_scan.getWidth(), read_scan.getHeight(), ap_1, ap_2);
            BufferedImage cropped_image = crop_image(deskewed_image, ap_1, ap_2);

            try {
                    ImageIO.write(cropped_image, "png", new File(scan_file.getName().substring(0,scan_file.getName().length() - 4)+"_cropped.png"));
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }

            File cropped_scan_file = new File(scan_file.getName().substring(0,scan_file.getName().length() - 4)+"_cropped.png");

            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            BufferedImage image = new HoughCirclesRun().run(cropped_scan_file);

            scan_img = new JLabel(new ImageIcon(image));
            panel.add(scan_img);
            frame.setVisible(true);
        });

        exit.addActionListener(new ExitHandler());

    }

    class ExitHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }

    }

    // Make new pdf quiz. This is where the Page_Setup class and its methods are called
    public void make_new_pdf(String pdf_save_name, String question_paper) throws IOException {

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        Page_Setup page_setup = new Page_Setup(page, content);


        String[] lines = question_paper.split("\\r?\\n");
        ArrayList<String> questions = new ArrayList<>();
        ArrayList<String> choices = new ArrayList<>();

        for (String line : lines) {
            if (line.contains("question: ")) {
                questions.add(line);
            }
            if (line.contains("answers: ")) {
                String temp = line.replace("answers: ", "");
                temp = temp.replace(", ", "\n");
                choices.add(temp);
            }
        }

        int circle_radius = 5;
        int circle_padding = 5;

        page_setup.make_setup_ready();
        page_setup.print_questions(questions);
        page_setup.print_answers(choices);
        page_setup.draw_circles(circle_radius, circle_padding);
        content.close();
        doc.save(pdf_save_name+".pdf");
        doc.close();

    }

    // This method is called in case the scan is a pdf and needs conversion into png
    public void create_png_from_pdf(File scan_file) throws IOException {

        PDDocument doc = PDDocument.load(scan_file);
        PDFRenderer renderer = new PDFRenderer(doc);
        for (int page_no = 0; page_no < doc.getNumberOfPages(); ++page_no) {
            BufferedImage fresh_img = renderer.renderImageWithDPI(page_no, 300, ImageType.RGB);
            // suffix in filename will be used as the file format
            ImageIOUtil.writeImage(fresh_img, scan_file.getName().substring(0,scan_file.getName().length() - 4) + ".png", 300);
        }
    }

    public static void main(String[] args) throws IOException, net.sourceforge.yamlbeans.YamlException {

        new Main();

    }

    // Deskew scan
    private static BufferedImage deskew_image(BufferedImage bimg, int bimg_width, int bimg_height, int ap_1, int ap_2) {

        int i, j = 0, k = 0;

        // Reaching the top (white) edge of the page (by skipping the non-white portion) on the left side
        while(j < ap_1) {
            if (bimg.getRGB(ap_1, j) != -1)
                j++;
            else
                break;
        }

        // Reaching the top (white) edge of the page (by skipping the non-white portion) on the right side
        while(k < ap_1) {
            if (bimg.getRGB(ap_2, k) != -1)
                k++;
            else
                break;
        }

        // Reaching the top edge of the black border from the top edge of the white border on the left side
        for(i = j; i < ap_1; i++)
            if (bimg.getRGB(ap_1, i) != -1)
                break;

        double y1 = i;
        double x1 = ap_1;

        // Reaching the top edge of the black border from the top edge of the white border on the right side
        for(i = k; i < ap_1; i++)
            if (bimg.getRGB(ap_2, i) != -1)
                break;

        double y2 = i;
        double x2 = ap_2;

        // Finding angle of skew
        double skew_angle = Math.atan2(y2-y1,x2-x1);
        AffineTransform at = new AffineTransform();

        // Rotating in the opposite direction
        at.rotate(-skew_angle, bimg_width/2, bimg_height/2);
        BufferedImage deskewed_image = new BufferedImage(bimg_width, bimg_height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = deskewed_image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bimg,at,null);

//        Optional : save deskewed image as a separate file
//        try {
//            ImageIO.write(deskewed_image, "png", new File("QuizDeskewed.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return deskewed_image;
    }

    private static BufferedImage crop_image(BufferedImage deskewed_image, int ap_1, int ap_2) {

        int i, j = 0;

        // Finding the top edge of the (outer) white border
        while(j < ap_1) {
            if (deskewed_image.getRGB(ap_1, j) != -1)
                j++;
            else
                break;
        }

        // Finding the top edge of the (inner) black border
        for(i = j; i < ap_1; i++)
            if (deskewed_image.getRGB(ap_1, i) != -1)
                break;

        int i2 = i;

        // To find the top-left corner from the height of the top edge of the black border (reached above)
        // We now traverse left to right on the image (instead of top to bottom)

        // Finding the white border edge
        j = 0;
        while(j < ap_1) {
            if (deskewed_image.getRGB(j, i2) != -1)
                j++;
            else
                break;
        }

        // Reaching the black border top-left corner through the white border
        for(i = j; i < ap_1; i++)
            if (deskewed_image.getRGB(i, i2) != -1)
                break;

        // corner co-ordinates = (i, i2)
        int crop_x = i;
        int crop_y = i2;

        // Now we try to find the width of the border by traversing the image from right to left
        j = deskewed_image.getWidth() - 1;

        // Reaching the white border
        while(j > ap_2) {
            if (deskewed_image.getRGB(j, i2) != -1)
                j--;
            else
                break;
        }

        // Reaching the black border
        int p;

        for(p = j; p > ap_2; p--)
            if (deskewed_image.getRGB(p, i2) != -1)
                break;

        int crop_width = p - crop_x;
        int crop_height = deskewed_image.getHeight() - 2*crop_y;

        BufferedImage img = deskewed_image.getSubimage(crop_x+100, crop_y, crop_width-120, crop_height);
        BufferedImage cropped_img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g1 = cropped_img.createGraphics();
        g1.drawImage(img, 0, 0, null);

//        Optional: save cropped image as a separate file
//        try {
//            ImageIO.write(cropped_img, "png", new File("QuizCropped.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return cropped_img;

    }

    // Remove answers from the yaml data to be saved for marking in the future
    private static Temp_Container remove_and_store_answers(String question_paper) {

        int additive;
        List answers = new ArrayList<Character>();
        ArrayList<String> to_be_removed = new ArrayList<>();


        /* Extracting answers for later use... */

        for(int i = 0; i < question_paper.length(); ) {
            additive = question_paper.indexOf("answer=", i);
            answers.add(question_paper.charAt(additive+7));
            i = i + additive + 6; // Was previously 1 not 6. 6 should help faster traversal
            to_be_removed.add(question_paper.substring(additive-2, additive+10));
        }


        /* Removing answers from question paper... */

        for(String r: to_be_removed) {
            question_paper = question_paper.replace(r, "\n");
        }

        /* returning formatted question paper and list of extracted answers... */

        return new Temp_Container(question_paper, answers);

    }

    // Remove unnecessary bits from the yaml data
    private static String format_Yaml (Object object) {

        String question_paper = object.toString();
        question_paper = question_paper.replace("{questions=[{", "questions:\n");
        question_paper = question_paper.replace(", {","\n");
        question_paper = question_paper.replace("question=","question: ");
        question_paper = question_paper.replace("]}", "");
        question_paper = question_paper.replace("answers=[","answers: ");
        return question_paper;

    }

}
