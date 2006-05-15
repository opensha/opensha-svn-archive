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
import javax.swing.JLabel;

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
    private String minParamName;
    private final static String MIN_PARAM_NAME_PREFIX="Min ";
    private DoubleParameter maxParam ;
    private String maxParamName;
    private final static String MAX_PARAM_NAME_PREFIX="Max ";
    private IntegerParameter numParam;
    private String numParamName;
    private final static String NUM_PARAM_NAME_PREFIX="Number of Points";

    //private final static String EDITOR_TITLE = "Evenly Discretized ";

    private final static String  ONE_Y_VAL_MSG = "Each line should have just " +
        "one Y value";
    private final static String Y_VALID_MSG = "Y Values entered must be valid numbers" ;
    private final static String INCORRECT_NUM_Y_VALS = "Number of Y vals should be equal to number of X values";
    protected final static Dimension SCROLLPANE_DIM = new Dimension( 70, 230 );
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
    private EvenlyDiscretizedFunc function;
    private String xAxisName = "";
    private String yAxisName = "";
    private  String title ;

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
      focusLostProcessing = true;
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

      xAxisName = "";
      yAxisName = "";
      title = param.getName();
      if(function!=null) {
        if(function.getXAxisName()!=null) xAxisName = function.getXAxisName();
        if(function.getYAxisName()!=null) yAxisName = function.getYAxisName();
      }

      // labels to be displayed on header of text area
      JLabel xLabel = new JLabel(xAxisName);
      JLabel yLabel = new JLabel(yAxisName);


      initParamListAndEditor();
      this.setLayout(GBL);
      add(this.editor, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));

      add(xLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
      add(yLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));

      add(this.xScrollPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
      add(this.yScrollPane, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
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
      minParam = new DoubleParameter(MIN_PARAM_NAME_PREFIX+xAxisName);
      maxParam = new DoubleParameter(MAX_PARAM_NAME_PREFIX+xAxisName);
      numParam = new IntegerParameter(NUM_PARAM_NAME_PREFIX);

      // put all the parameters in the parameter list
      parameterList = new ParameterList();
      parameterList.addParameter(this.minParam);
      parameterList.addParameter(this.maxParam);
      parameterList.addParameter(this.numParam);
      this.editor = new ParameterListEditor(parameterList);
      editor.setTitle(title);

      xTextArea = new JTextArea();
      xTextArea.setEnabled(false);
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
     * When user clicks in the texstarea to fill up the Y values, fill the X values
     * automatically
     * @param e
     */
    public void focusGained(FocusEvent e)  {
      super.focusGained(e);
      focusLostProcessing = false;
      // check that user has entered min Val
      Double minVal = (Double)minParam.getValue();
      String isMissing = " is missing";
      if(minVal==null) {
    	this.editor.getParameterEditor(minParam.getName()).grabFocus();
        JOptionPane.showMessageDialog(this, minParam.getName()+isMissing);
        return;
      }
      double min = minVal.doubleValue();
      // check that user has entered max val
      Double maxVal = (Double)maxParam.getValue();
      if(maxVal==null) {
    	  this.editor.getParameterEditor(maxParam.getName()).grabFocus();
        JOptionPane.showMessageDialog(this, maxParam.getName()+isMissing);
        return;
      }
      double max = maxVal.doubleValue();
      //check that user has entered num values
      Integer numVal = (Integer)numParam.getValue();
      if(numVal==null) {
    	  this.editor.getParameterEditor(numParam.getName()).grabFocus();
        JOptionPane.showMessageDialog(this, numParam.getName()+isMissing);
        
        return;
      }
      int num = numVal.intValue();
      double y[] = new double[function.getNum()];
      for(int i=0; i<function.getNum(); ++i)
        y[i] = function.getY(i);
      function.set(min, max, num);
      String xStr = "";
      String yStr = "";
      for(int i=0; i<num; ++i) {
        if(i<y.length) function.set(i,y[i]);
        else function.set(i,0.0);
        xStr = xStr + function.getX(i) + "\n";
        yStr = yStr +  function.getY(i)+" \n";
      }
      xTextArea.setText(xStr);
      yTextArea.setText(yStr);
      focusLostProcessing = true;
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

      if(!focusLostProcessing ) return;
      System.out.println("Focus lost");

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
      //refreshParamEditor();
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
      if(model==null || model.getValue()==null) return;
      EvenlyDiscretizedFunc func = (EvenlyDiscretizedFunc)model.getValue();
      this.minParam.setValue(func.getMinX());
      this.maxParam.setValue(func.getMaxX());
      this.numParam.setValue(new Integer(func.getNum()));
      if ( func != null ) { // show X, Y values from the function
        this.xTextArea.setText("");
        this.yTextArea.setText("");
        int num = func.getNum();
        String xText = "";
        String yText= "";
        for(int i=0; i<num; ++i) {
          xText += func.getX(i)  + "\n";
          yText += func.getY(i)  + "\n";
        }
        xTextArea.setText(xText);
        yTextArea.setText(yText);
      }
      else {
        xTextArea.setText("");
        yTextArea.setText("");
      }
      this.repaint();
    }


  }
