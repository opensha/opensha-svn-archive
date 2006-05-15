package org.opensha.param.editor;

import java.awt.event.*;
import javax.swing.border.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JLabel;
import java.awt.*;

/**
 * <b>Title:</b> ArbitrarilyDiscretizedFuncParameterEditor<p>
 *
 * <b>Description:</b> Subclass of ParameterEditor for editing ArbitrarilyDiscretizedFunc.
 * The widget is a JTextArea which allows X and Y values to be filled in.  <p>
 *
 * @author Vipin Gupta, Nitin Gupta
 * @version 1.0
 */
public class ArbitrarilyDiscretizedFuncParameterEditor extends ParameterEditor
{

    /** Class name for debugging. */
    protected final static String C = "DiscretizedFuncParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private final static String  ONE_XY_VAL_MSG = "Each line should have exactly one X and " +
        "one Y value";
    private final static String XY_VALID_MSG = "X and Y Values entered must be valid numbers" ;
    protected final static Dimension WIGET_PANEL_DIM = new Dimension( 140, 230 );
    protected final static Dimension SCROLLPANE_DIM = new Dimension( 70, 215 );
    protected final static GridBagConstraints WIDGET_GBC = new GridBagConstraints(
      0, 0, 1, 1, 1.0, 0.0, 10, GridBagConstraints.BOTH, new Insets( 1, 5, 0, 1 ), 0, 0 );
    protected final static GridBagConstraints WIDGET_PANEL_GBC = new GridBagConstraints(
      0, 1, 1, 1, 1.0, 0.0, 10, GridBagConstraints.BOTH, ZERO_INSETS, 0, 0 );


    private JTextArea xValsTextArea;
    private JTextArea yValsTextArea;
    private boolean isFocusListenerForX = false;

    /** No-Arg constructor calls parent constructor */
    public ArbitrarilyDiscretizedFuncParameterEditor() { super(); }

    /**
     * Constructor that sets the parameter that it edits. An
     * Exception is thrown if the model is not an DiscretizedFuncParameter <p>
     */
     public ArbitrarilyDiscretizedFuncParameterEditor(ParameterAPI model) throws Exception {

        super(model);
        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        //this.setParameter(model);
        if(D) System.out.println(S.concat("Ending"));

    }

    /**
     * Whether you want the user to be able to type in the X values
     * @param isEnabled
     */
    public void setXEnabled(boolean isEnabled) {
      this.xValsTextArea.setEnabled(isEnabled);
    }

    /** This is where the JTextArea is defined and configured. */
    protected void addWidget() {

        String S = C + ": addWidget(): ";
        if(D) System.out.println(S + "Starting");

        String xLabelText = "";
        String yLabelText = "";

        // set the value in ArbitrarilyDiscretizedFunc
        ArbitrarilyDiscretizedFunc function = (ArbitrarilyDiscretizedFunc)model.getValue();
        if(function!=null) {
          if(function.getXAxisName()!=null) xLabelText = function.getXAxisName();
          if(function.getYAxisName()!=null) yLabelText = function.getYAxisName();
        }

        // labels to be displayed on header of text area
        JLabel xLabel = new JLabel(xLabelText);
        JLabel yLabel = new JLabel(yLabelText);

        // text area to enter x values
        xValsTextArea = new JTextArea();
        JScrollPane xScrollPane = new JScrollPane(xValsTextArea);
        xScrollPane.setMinimumSize( SCROLLPANE_DIM );
        xScrollPane.setPreferredSize( SCROLLPANE_DIM );
        widgetPanel.add(xLabel, new GridBagConstraints(
        0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
        widgetPanel.add(xScrollPane, new GridBagConstraints(
        0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));


        // text area to enter y values
        yValsTextArea = new JTextArea();
        yValsTextArea.addFocusListener( this );
        JScrollPane yScrollPane = new JScrollPane(yValsTextArea);
        yScrollPane.setMinimumSize( SCROLLPANE_DIM );
        yScrollPane.setPreferredSize( SCROLLPANE_DIM );
        widgetPanel.add(yLabel, new GridBagConstraints(
        1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0), 0, 0 ));
        widgetPanel.add(yScrollPane, new GridBagConstraints(
        1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));

        widgetPanel.setBackground(null);
        widgetPanel.validate();
        widgetPanel.repaint();
        if(D) System.out.println(S + "Ending");
    }


    /**
        * Main GUI Initialization point. This block of code is updated by JBuilder
        * when using it's GUI Editor.
        */
       protected void jbInit() throws Exception {

           // Main component
           titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(FORE_COLOR,1),"");
           titledBorder1.setTitleColor(FORE_COLOR);
           titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
           titledBorder1.setTitle("Parameter Name");
           border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
           this.setLayout( GBL );


           // Outermost panel
           outerPanel.setLayout( GBL );
           outerPanel.setBorder(border1);

           // widgetPanel panel init
           //widgetPanel.setBackground( BACK_COLOR );
           widgetPanel.setLayout( GBL );
           widgetPanel.setMinimumSize( WIGET_PANEL_DIM );
           widgetPanel.setPreferredSize( WIGET_PANEL_DIM );


           // nameLabel panel init
           //nameLabel.setBackground( BACK_COLOR );
           nameLabel.setMaximumSize( LABEL_DIM );
           nameLabel.setMinimumSize( LABEL_DIM );
           nameLabel.setPreferredSize( LABEL_DIM );
           nameLabel.setHorizontalAlignment( SwingConstants.LEFT );
           nameLabel.setHorizontalTextPosition( SwingConstants.LEFT );
           nameLabel.setText( LABEL_TEXT );
           nameLabel.setFont( DEFAULT_LABEL_FONT );
           outerPanel.add( widgetPanel, WIDGET_PANEL_GBC );

           this.add( outerPanel, OUTER_PANEL_GBC );

       }





    /**
     * Called when the user clicks on another area of the GUI outside
     * this editor panel. This synchornizes the editor text field
     * value to the internal parameter reference.
     */
    public void focusLost(FocusEvent e) throws ConstraintException {

        String S = C + ": focusLost(): ";
        if(D) System.out.println(S + "Starting");

        super.focusLost(e);

        focusLostProcessing = false;
        if( keyTypeProcessing == true ) return;
        focusLostProcessing = true;
        setValueInParameter();
        if(!isFocusListenerForX)  {
        	xValsTextArea.addFocusListener(this);
        	isFocusListenerForX = true;
        }
        focusLostProcessing = false;
        if(D) System.out.println(S + "Ending");
      }

    private void setValueInParameter() throws DataPoint2DException,
        HeadlessException, NumberFormatException {
      String xValsStr = this.xValsTextArea.getText();
      String yValsStr = this.yValsTextArea.getText();

      // set the value in ArbitrarilyDiscretizedFunc
      ArbitrarilyDiscretizedFunc function = (ArbitrarilyDiscretizedFunc)model.getValue();
      function.clear();
      StringTokenizer xStringTokenizer = new StringTokenizer(xValsStr,"\n");
      StringTokenizer yStringTokenizer = new StringTokenizer(yValsStr,"\n");


      while(xStringTokenizer.hasMoreTokens()){
        double tempX_Val=0;
        double tempY_Val=0;
        try{
          tempX_Val = Double.parseDouble(xStringTokenizer.nextToken());
          tempY_Val = Double.parseDouble(yStringTokenizer.nextToken());
        }catch(Exception ex){
          JOptionPane.showMessageDialog(this, XY_VALID_MSG);
          return;
        }
        function.set(tempX_Val,tempY_Val);
      }

      if(yStringTokenizer.hasMoreTokens()) {
        JOptionPane.showMessageDialog(this, ONE_XY_VAL_MSG);
      }
      refreshParamEditor();
    }

    /** Sets the parameter to be edited. */
    public void setParameter(ParameterAPI model) {
        String S = C + ": setParameter(): ";
        if(D) System.out.println(S.concat("Starting"));
        if ( (model != null ) && !(model instanceof ArbitrarilyDiscretizedFuncParameter))
            throw new RuntimeException( S + "Input model parameter must be a DiscretizedFuncParameter.");
        super.setParameter(model);
        xValsTextArea.setToolTipText("No Constraints");
        yValsTextArea.setToolTipText("No Constraints");

        String info = model.getInfo();
        if( (info != null ) && !( info.equals("") ) ){
            this.nameLabel.setToolTipText( info );
        }
        else this.nameLabel.setToolTipText( null);
        if(D) System.out.println(S.concat("Ending"));
    }

    /**
     * Updates the JTextArea string with the parameter value. Used when
     * the parameter is set for the first time, or changed by a background
     * process independently of the GUI. This could occur with a ParameterChangeFail
     * event.
     */
    public void refreshParamEditor(){
        ArbitrarilyDiscretizedFunc func = (ArbitrarilyDiscretizedFunc)model.getValue();
        if ( func != null ) { // show X, Y values from the function
          this.xValsTextArea.setText("");
          this.yValsTextArea.setText("");
          int num = func.getNum();
          String xText = "";
          String yText= "";
          for(int i=0; i<num; ++i) {
            xText += func.getX(i)  + "\n";
            yText += func.getY(i)  + "\n";
          }
          xValsTextArea.setText(xText);
          yValsTextArea.setText(yText);
        }
        else {
          xValsTextArea.setText("");
          yValsTextArea.setText("");
        }
        this.repaint();
    }

    /**
    * It enables/disables the editor according to whether user is allowed to
    * fill in the values.
    */
   public void setEnabled(boolean isEnabled) {
     this.xValsTextArea.setEnabled(isEnabled);
     this.yValsTextArea.setEnabled(isEnabled);
   }

}
