package org.scec.sha.calc.demo;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.scec.data.*;
import org.scec.gui.*;
import org.scec.sha.calc.*;
import org.scec.calc.RelativeLocation;

/**
 * <b>Title:</b> RelativeLocationApplet <p>
 *
 * <b>Description:</b> Tester Applet that contains 2 directions and 1 relative location, all are editable.
 * When you click on calculate the tester applet takes two of the values to calculate the third, depending on
 * which button you click on. For example you can calculate the Direction from two location objects, or calculate
 * the second location from a location and diection. This is a visual verification of the RelativeLocation calculator.
 * <p>
 *
 * There are two input Location editors and 1 input Direction editor. There are two buttons; calculate Location, and
 * calculate Direction. If calcualate Location is pressed the first input location and direction fields are used.
 * if the calculate Direction is used, only the two input locations are used. In other words there are 3 input sections
 * but only two are used for any one calculation. Once a calculation is performed then the output fields are shown, either
 * for the output Direction, or output Location. Initially the output fields are not seen.<p>
 *
 * A helpful feature of this tester is that once you calculate the output value from two inputs, you can now
 * specify this output as a new input. It copies all the output values into the input editor automatically.
 * It acts like a special copy and pate function.<p>
 *
 * @author  Steven W. Rock
 * @version 1.0
 */

public class RelativeLocationApplet extends JApplet {

    protected final static String C = "DirectionEditor";
    protected final static boolean D = true;

    boolean isStandalone = false;

    protected final static GridBagLayout GBL = new GridBagLayout();

    private final static Color darkBlue = new Color(80,80,133);
    private final static Color lightBlue = new Color(200,200,230);
    private final static Color background = Color.white;

    private final static Font TITLE_FONT = new java.awt.Font("Dialog", 1, 16);
    private final static Font BUTTON_FONT = new java.awt.Font("Dialog", 1, 11);
    private final static Font TEXT_FONT = new java.awt.Font("Dialog", 0, 10);

    private final static SidesBorder topBorder = new SidesBorder(darkBlue, background, background, background);
    private final static SidesBorder bottomBorder = new SidesBorder(background, darkBlue, background, background);
    private final static SidesBorder fullTopBorder = new SidesBorder(darkBlue, darkBlue, darkBlue, darkBlue);
    private final static SidesBorder fullBottomBorder = new SidesBorder(background, darkBlue, darkBlue, darkBlue);
    private final static OvalBorder oval = new OvalBorder(7,4,darkBlue, darkBlue);
    private final static Border BUTTON_BORDER = BorderFactory.createRaisedBevelBorder();

    protected final static Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

    protected final static Insets TWO_INSETS = new Insets(2, 2, 2, 2);
    protected final Dimension BUTTON_DIM = new Dimension(130, 20);



    LocationEditor loc1 = new LocationEditor();
    LocationEditor loc2 = new LocationEditor();
    LocationEditor locOut = new LocationEditor();

    DirectionEditor dir1 = new DirectionEditor();
    DirectionEditor dirOut = new DirectionEditor();


    JPanel mainPanel = new JPanel();
    JPanel titlePanel = new JPanel();
    JLabel titleLabel = new JLabel();
    JPanel inputsPanel = new JPanel();
    JPanel outputPanel = new JPanel();
    JPanel controlPanel = new JPanel();


    JButton setLoc2Button = new JButton();
    JButton setLoc1Button = new JButton();
    JButton setDirButton = new JButton();
    JButton locationButton = new JButton();
    JButton dirButton = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();
    JScrollPane outScrollPane = new JScrollPane();
    JTextArea outTextArea = new JTextArea();



    /**Get a parameter value*/
    public String getParameter(String key, String def) {
        return isStandalone ? System.getProperty(key, def) :
            (getParameter(key) != null ? getParameter(key) : def);
    }

    /**Construct the applet*/
    public RelativeLocationApplet() {
        init();
    }
    /**Initialize the applet*/
    public void init() {

        oval.setBottomColor(darkBlue);
        oval.setTopColor(darkBlue);
        oval.setHeight(10);
        oval.setWidth(10);

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private JButton makeButton(JButton button, String text){

        button.setBackground(lightBlue);
        button.setForeground(darkBlue);
        button.setFont(BUTTON_FONT);
        button.setBorder(BUTTON_BORDER);
        button.setFocusPainted(false);
        button.setText(text);
        button.setPreferredSize(BUTTON_DIM);
        button.setMinimumSize(BUTTON_DIM);
        return button;

    }


    /**Component initialization*/
    private void jbInit() throws Exception {

        Location location1 = new Location(10, 20, 2);
        Location location2 = new Location(11, 21, 3);

        loc1.setTheLocation(location1);
        loc1.setName("Location 1:");

        loc2.setTheLocation(location2);
        loc2.setName("Location 2:");

        locOut.setName("Resulting Location");


        Direction direction = new Direction(1, 20, 89, 88);
        dir1.setTheDirection( direction );
        dir1.setName( " Direction 1:" );
        dirOut.setName("Resulting Directoin");


        this.getContentPane().setBackground(background);
        this.setSize(new Dimension(517, 451));
        this.getContentPane().setLayout(GBL);

        mainPanel.setBackground(Color.white);
        mainPanel.setBorder(oval);
        mainPanel.setLayout(GBL);

        titlePanel.setBackground(background);
        titlePanel.setBorder(bottomBorder);

        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("Location and Direction Calculator");
        titleLabel.setFont(TITLE_FONT);

        inputsPanel.setBackground(background);
        inputsPanel.setBorder(oval);
        inputsPanel.setLayout(GBL);

        outputPanel.setBackground(background);
        outputPanel.setBorder(oval);
        outputPanel.setLayout(GBL);


        controlPanel.setBackground(Color.white);
        controlPanel.setBorder(oval);
        //controlPanel.setLayout(flowLayout1);
        controlPanel.setLayout(GBL);



        outScrollPane.getViewport().setBackground(Color.white);
        outTextArea.setText("Output goes here");
        outTextArea.setFont(TEXT_FONT);

        titlePanel.add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, ZERO_INSETS, 0, 0));

        mainPanel.add(titlePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, TWO_INSETS, 0, 0));


        inputsPanel.add(loc1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, ZERO_INSETS, 0, 0));

        inputsPanel.add(loc2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, ZERO_INSETS, 0, 0));

        inputsPanel.add(dir1, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, ZERO_INSETS, 0, 0));

        mainPanel.add(inputsPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, TWO_INSETS, 0, 0));

        makeButtonsPanel();
        mainPanel.add(controlPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, TWO_INSETS, 0, 0));

        outputPanel.add(dirOut, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        outputPanel.add(locOut, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        outputPanel.add(setDirButton, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        outputPanel.add(setLoc1Button, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, TWO_INSETS, 0, 0));
        outputPanel.add(setLoc2Button, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, TWO_INSETS, 0, 0));


        outputPanel.add(outScrollPane, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));



        locOut.setVisible(false);
        dirOut.setVisible(false);

        outScrollPane.getViewport().add(outTextArea, null);

        mainPanel.add(outputPanel, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, TWO_INSETS, 0, 0));

        this.getContentPane().add(mainPanel, new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, ZERO_INSETS, 1, 1));


    }

    private void makeButtonsPanel(){

        dirButton = makeButton(dirButton, "Get Direction");
        dirButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                direction(e);
            }
        });


        locationButton = makeButton(locationButton, "Get Location");
        locationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                location(e);
            }
        });


        setDirButton = makeButton(setDirButton, "Set Result: Dir");
        setDirButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setDirection(e);
            }
        });

        setLoc1Button = makeButton(setLoc1Button, "Set Result: Loc1");
        setLoc1Button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setLocation1(e);
            }
        });


        setLoc2Button = makeButton(setLoc2Button, "Set Result: Loc2");
        setLoc2Button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setLocation2(e);
            }
        });



        controlPanel.add(dirButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        controlPanel.add(locationButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, TWO_INSETS, 0, 0));


        setDirButton.setVisible(false);
        setLoc1Button.setVisible(false);
        setLoc2Button.setVisible(false);
    }


    void direction(MouseEvent e) {

        // Starting
        String S = C + ": direction(): ";
        if(D) System.out.println("\n\n" + S + "Starting");

        StringBuffer b = new StringBuffer();
        b.append("Calculating Direction from two Locations\n\n");

        Location location1 = this.loc1.getTheLocation();
        this.loc1.setTheLocation(location1);
        location1 = this.loc1.getTheLocation();
        if(D) System.out.println(S + "Location 1: " + location1);
        b.append("Location 1: " + location1 + '\n');

        Location location2 = this.loc2.getTheLocation();
        this.loc2.setTheLocation(location2);
        location2 = this.loc2.getTheLocation();
        if(D) System.out.println(S + "Location 2: " + location2);
        b.append("Location 2: " + location2 + "\n\n");

        Direction direction = RelativeLocation.getDirection(location1, location2);
        dirOut.setTheDirection(direction);
        if(D) System.out.println(S + "Direction: " + direction);
        b.append("Direction: \n\t" + direction + '\n');

        locOut.setVisible(false);
        dirOut.setVisible(true);

        setDirButton.setVisible(true);
        setLoc1Button.setVisible(false);
        setLoc2Button.setVisible(false);

        this.outTextArea.setText(b.toString());

        this.validate();
        this.repaint();

        if(D) System.out.println(S + "Ending");

    }

    void location(MouseEvent e) {

        // Starting
        String S = C + ": location(): ";
        if(D) System.out.println(S + "Starting");


        StringBuffer b = new StringBuffer();
        b.append("Calculating New Location from a start Location and Direction\n\n");

        Location location = this.loc1.getTheLocation();
        this.loc1.setTheLocation(location);
        location = this.loc1.getTheLocation();
        if(D) System.out.println(S + "Start Location: " + location);
        b.append("Start Location: " + location + '\n');

        Direction direction = this.dir1.getTheDirection();
        this.dir1.setTheDirection(direction);
        direction = this.dir1.getTheDirection();
        if(D) System.out.println(S + "Direction: " + direction);
        b.append("Direction: " + direction + "\n\n");


        Location endLoc = RelativeLocation.getLocation(location, direction);
        locOut.setTheLocation(endLoc);
        if(D) System.out.println(S + "New Location: " + endLoc);
        b.append("New Location: \n\t" + endLoc + '\n');

        locOut.setVisible(true);
        dirOut.setVisible(false);

        setDirButton.setVisible(false);
        setLoc1Button.setVisible(true);
        setLoc2Button.setVisible(true);

        this.outTextArea.setText(b.toString());

        this.validate();
        this.repaint();

        if(D) System.out.println(S + "Ending");

    }

    /** Copies the output direction into the input diection fields */
    void setDirection(MouseEvent e) {

        // Starting
        String S = C + ": setDirection(): ";
        if(D) System.out.println(S + "Starting");

        StringBuffer b = new StringBuffer();
        b.append("Setting Output Direction as the new input Direction\n\n");


        dir1.setTheDirection(dirOut.getTheDirection());
        if(D) System.out.println(S + "Direction: " + dir1);
        b.append("Direction: \n\t" + dir1 + '\n');

        locOut.setVisible(false);
        dirOut.setVisible(false);

        setDirButton.setVisible(false);
        setLoc1Button.setVisible(false);
        setLoc2Button.setVisible(false);

        this.outTextArea.setText(b.toString());

        this.validate();
        this.repaint();


        if(D) System.out.println(S + "Ending");

    }

    /** Copies the calculated output location into the input location 1 */
    void setLocation2(MouseEvent e) {

        // Starting
        String S = C + ": setLocation2(): ";
        if(D) System.out.println(S + "Starting");

        StringBuffer b = new StringBuffer();
        b.append("Setting Output Location as the new input Location 2\n\n");


        loc2.setTheLocation(locOut.getTheLocation());
        if(D) System.out.println(S + "Location: " + loc2);
        b.append("Location: \n\t" + loc2 + '\n');

        locOut.setVisible(false);
        dirOut.setVisible(false);

        setDirButton.setVisible(false);
        setLoc1Button.setVisible(false);
        setLoc2Button.setVisible(false);

        this.outTextArea.setText(b.toString());

        this.validate();
        this.repaint();


        if(D) System.out.println(S + "Ending");

    }

    /** Copies the calculated output location into the input location 1 */
    void setLocation1(MouseEvent e) {

        // Starting
        String S = C + ": setLocation1(): ";
        if(D) System.out.println(S + "Starting");

        StringBuffer b = new StringBuffer();
        b.append("Setting Output Location as the new input Location 1\n\n");


        loc1.setTheLocation(locOut.getTheLocation());
        if(D) System.out.println(S + "Location: " + loc1);
        b.append("Location: \n\t" + loc1 + '\n');

        locOut.setVisible(false);
        dirOut.setVisible(false);

        setDirButton.setVisible(false);
        setLoc1Button.setVisible(false);
        setLoc2Button.setVisible(false);

        this.outTextArea.setText(b.toString());

        this.validate();
        this.repaint();

        if(D) System.out.println(S + "Ending");

    }


    /**Start the applet*/
    public void start() {
    }
    /**Stop the applet*/
    public void stop() {
    }
    /**Destroy the applet*/
    public void destroy() {
    }
    /**Get Applet information*/
    public String getAppletInfo() {
        return "Applet Information";
    }
    /**Get parameter info*/
    public String[][] getParameterInfo() {
        return null;
    }
    /**Main method*/
    public static void main(String[] args) {
        RelativeLocationApplet applet = new RelativeLocationApplet();
        applet.isStandalone = true;
        JFrame frame = new JFrame();
        //EXIT_ON_CLOSE == 3
        frame.setDefaultCloseOperation(3);
        frame.setTitle("Location and Direction Calculator ...");
        frame.getContentPane().add(applet, BorderLayout.CENTER);
        applet.init();
        applet.start();
        frame.setSize(900,600);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }


}
