package org.opensha.param.editor;

import java.awt.event.*;
import javax.swing.border.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * <b>Title:</b> EvenlyDiscretizedFuncParameterEditor<p>
 *
 * <b>Description:</b> Subclass of ParameterEditor for editing EvenlyDiscretizedFuncParameters.
 * The widget consists of fields to specfiy min/max/delta and a JTextArea
 * which allows Y values to be filled in.  <p>
 *
 * @author Vipin Gupta, Nitin Gupta
 * @version 1.0
 */
public class EvenlyDiscretizedFuncParameterEditor extends ParameterEditor
{

    /** Class name for debugging. */
    protected final static String C = "EvenlyDiscretizedFuncParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    private DoubleParameter minParam ;
    private final static String MIN_PARAM_NAME="Min";
    private DoubleParameter maxParam ;
    private final static String MAX_PARAM_NAME="Max";
    private IntegerParameter numParam;
    private final static String NUM_PARAM_NAME="Num";

    private final static String EDITOR_TITLE = "Evenly Discretized XY Vals";

    private final static String  ONE_Y_VAL_MSG = "Each line should have just " +
        "one Y value";
    private final static String Y_VALID_MSG = "Y Values entered must be valid numbers" ;
    private final static String INCORRECT_NUM_Y_VALS = "Number of Y vals should be equal to number of X values";
    protected final static Dimension SCROLLPANE_DIM = new Dimension( 140, 230 );
    /**
     * Paramter List for holding all parameters
     */
    private ParameterList parameterList;

    /**
     * ParameterListEditor for holding parameters
     */
    private ParameterListEditor editor;

    /**
     * X values text area
     */
    private JTextArea xTextArea;
    // x scroll pane
    private JScrollPane xScrollPane;

    /**
     * Y values text area
     */
    private JTextArea yTextArea;
    // y scroll pane
    private JScrollPane yScrollPane;
    EvenlyDiscretizedFunc function;

    /** No-Arg constructor calls parent constructor */
    public EvenlyDiscretizedFuncParameterEditor() {
      super();
    }

    /**
     * Constructor that sets the parameter that it edits. An
     * Exception is thrown if the model is not an DiscretizedFuncParameter <p>
     */
    public EvenlyDiscretizedFuncParameterEditor(ParameterAPI model) throws
        Exception {

      super(model);

      String S = C + ": Constructor(model): ";
      if (D)
        System.out.println(S + "Starting");

      this.setParameter(model);
      if (D)
        System.out.println(S.concat("Ending"));

    }


    /**
     * Main GUI Initialization point. This block of code is updated by JBuilder
     * when using it's GUI Editor.
     */
    protected void jbInit() throws Exception {
      this.setLayout(GBL);
    }



    public void setParameter(ParameterAPI param) {

      String S = C + ": Constructor(): ";
      if (D) System.out.println(S + "Starting:");
      if ( (model != null) && ! (model instanceof EvenlyDiscretizedFuncParameter))
        throw new RuntimeException(S +
                                   "Input model parameter must be a EvenlyDiscretizedFuncParameter.");

      // make the params editor
      function = (EvenlyDiscretizedFunc)param.getValue();
      initParamListAndEditor();
      this.setLayout(GBL);
      add(this.editor, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
      add(this.xScrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
      add(this.yScrollPane, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));


      this.refreshParamEditor();
      // All done
      if (D) System.out.println(S + "Ending:");
    }


    /*
     *
     */
    protected void initParamListAndEditor() {

      // Starting
      String S = C + ": initControlsParamListAndEditor(): ";
      if (D)
        System.out.println(S + "Starting:");
      minParam = new DoubleParameter(MIN_PARAM_NAME, new  Double(function.getMinX()));
      maxParam = new DoubleParameter(MAX_PARAM_NAME, new Double(function.getMaxX()));
      numParam = new IntegerParameter(NUM_PARAM_NAME, new Integer(function.getNum()));

      // put all the parameters in the parameter list
      parameterList = new ParameterList();
      parameterList.addParameter(this.minParam);
      parameterList.addParameter(this.maxParam);
      parameterList.addParameter(this.numParam);
      this.editor = new ParameterListEditor(parameterList);
      editor.setTitle(EDITOR_TITLE);

      xTextArea = new JTextArea();
      xTextArea.setEditable(false);
      xScrollPane = new JScrollPane(xTextArea);
      xScrollPane.setMinimumSize( SCROLLPANE_DIM );
      xScrollPane.setPreferredSize( SCROLLPANE_DIM );

      yTextArea = new JTextArea();
      yTextArea.addFocusListener(this);
      yScrollPane = new JScrollPane(yTextArea);
      yScrollPane.setMinimumSize( SCROLLPANE_DIM );
      yScrollPane.setPreferredSize( SCROLLPANE_DIM );

    }

    /**
     * It enables/disables the editor according to whether user is allowed to
     * fill in the values.
     */
    public void setEnabled(boolean isEnabled) {
      this.editor.setEnabled(isEnabled);
      this.xTextArea.setEnabled(isEnabled);
      this.yTextArea.setEnabled(isEnabled);

    }


    /**
     * Set the X values in the text area based on user specified min/max/num
     */
    private void setXValues() {
      double min = ((Double)minParam.getValue()).doubleValue();
      double max = ((Double)maxParam.getValue()).doubleValue();
      int num = ((Integer)numParam.getValue()).intValue();
      function.set(min, max, num);
      String str = "";
      for(int i=0; i<num; ++i) str=str+function.getX(i)+"\n";
      this.xTextArea.setEditable(true);
      xTextArea.setText(str);
      this.xTextArea.setEditable(false);
    }


    /**
     * When user clicks in the texstarea to fill up the Y values, fill the X values
     * automatically
     * @param e
     */
    public void focusGained(FocusEvent e)  {
      setXValues();
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

      String str = yTextArea.getText();
      StringTokenizer st = new StringTokenizer(str,"\n");
      int yIndex = 0;
      while(st.hasMoreTokens()){
        StringTokenizer st1 = new StringTokenizer(st.nextToken());
        int numVals = st1.countTokens();
        // check that each line in text area just contains 1 value
        if(numVals !=1) {
          JOptionPane.showMessageDialog(this, this.ONE_Y_VAL_MSG);
          return;
        }
        double tempY_Val=0;
        // check that y value is a valid number
        try{
          tempY_Val = Double.parseDouble(st1.nextToken());
          // set the Y value in the function
          function.set(yIndex, tempY_Val);
          ++yIndex;
        }catch(NumberFormatException ex){
          JOptionPane.showMessageDialog(this, Y_VALID_MSG);
          return;
        }catch(DataPoint2DException ex) {
           JOptionPane.showMessageDialog(this, INCORRECT_NUM_Y_VALS);
           return;
        }
      }
      // check that user has entered correct number of Y values
      if(yIndex!=function.getNum())
        JOptionPane.showMessageDialog(this, INCORRECT_NUM_Y_VALS);
      refreshParamEditor();
      focusLostProcessing = false;
      if(D) System.out.println(S + "Ending");
    }


    /**
     * Called when the parameter has changed independently from
     * the editor, such as with the ParameterWarningListener.
     * This function needs to be called to to update
     * the GUI component ( text field, picklist, etc. ) with
     * the new parameter value.
     */
    public void refreshParamEditor() {
      editor.refreshParamEditor();
      this.repaint();
    }

  }
