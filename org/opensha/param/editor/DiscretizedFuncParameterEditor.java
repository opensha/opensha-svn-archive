package org.opensha.param.editor;

import java.awt.event.*;
import javax.swing.border.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * <b>Title:</b> IntegerParameterEditor<p>
 *
 * <b>Description:</b> Subclass of ParameterEditor for editing DiscretizedFuncParameters.
 * The widget is a JTextArea which allows X and Y values to be filled in.  <p>
 *
 * @author Vipin Gupta, Nitin Gupta
 * @version 1.0
 */
public class DiscretizedFuncParameterEditor extends ParameterEditor
{

    /** Class name for debugging. */
    protected final static String C = "DiscretizedFuncParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private final static String  ONE_XY_VAL_MSG = "Each line should have just one X and " +
                                          "one Y value, which are space seperated";
     private final static String XY_VALID_MSG = "X and Y Values entered must be valid numbers" ;
     protected final static Dimension WIGET_PANEL_DIM = new Dimension( 140, 230 );
     protected final static GridBagConstraints WIDGET_GBC = new GridBagConstraints(
            0, 0, 1, 1, 1.0, 0.0, 10, GridBagConstraints.BOTH, new Insets( 1, 5, 0, 1 ), 0, 0 );
    protected final static GridBagConstraints WIDGET_PANEL_GBC = new GridBagConstraints(
            0, 1, 1, 1, 1.0, 0.0, 10, GridBagConstraints.BOTH, ZERO_INSETS, 0, 0 );

    /** No-Arg constructor calls parent constructor */
    public DiscretizedFuncParameterEditor() { super(); }

    /**
     * Constructor that sets the parameter that it edits. An
     * Exception is thrown if the model is not an DiscretizedFuncParameter <p>
     */
     public DiscretizedFuncParameterEditor(ParameterAPI model) throws Exception {

        super(model);

        String S = C + ": Constructor(model): ";
        if(D) System.out.println(S + "Starting");

        if ( (model != null ) && !(model instanceof DiscretizedFuncParameter))
            throw new Exception( S + "Input model parameter must be a DiscretizedFuncParameter.");

        this.setParameter(model);
        if(D) System.out.println(S.concat("Ending"));

    }

    /** This is where the JTextArea is defined and configured. */
    protected void addWidget() {

        String S = C + ": addWidget(): ";
        if(D) System.out.println(S + "Starting");

        valueEditor = new JTextArea();

        valueEditor.setBorder(ETCHED);
        valueEditor.setFont(this.DEFAULT_FONT);

        valueEditor.addFocusListener( this );
        JScrollPane scrollPane = new JScrollPane(valueEditor);
        scrollPane.setMinimumSize( WIGET_PANEL_DIM );
        scrollPane.setPreferredSize( WIGET_PANEL_DIM );

        widgetPanel.add(scrollPane, ParameterEditor.WIDGET_GBC);
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

        String str = ((JTextArea) valueEditor).getText();

        // set the value in ArbitrarilyDiscretizedFunc
        ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
        StringTokenizer st = new StringTokenizer(str,"\n");
        while(st.hasMoreTokens()){
          StringTokenizer st1 = new StringTokenizer(st.nextToken());
          int numVals = st1.countTokens();
          if(numVals !=2) {
            JOptionPane.showMessageDialog(this, this.ONE_XY_VAL_MSG);
            return;
          }
          double tempX_Val=0;
          double tempY_Val=0;
          try{
            tempX_Val = Double.parseDouble(st1.nextToken());
            tempY_Val = Double.parseDouble(st1.nextToken());
          }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, XY_VALID_MSG);
            return;
          }
          function.set(tempX_Val,tempY_Val);
        }
        setValue(function);
        refreshParamEditor();
        valueEditor.validate();
        valueEditor.repaint();
        focusLostProcessing = false;
        if(D) System.out.println(S + "Ending");
      }

    /** Sets the parameter to be edited. */
    public void setParameter(ParameterAPI model) {
        String S = C + ": setParameter(): ";
        if(D) System.out.println(S.concat("Starting"));

        super.setParameter(model);
        ((JTextArea) valueEditor).setToolTipText("No Constraints");

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
        DiscretizedFuncAPI func = (DiscretizedFuncAPI)model.getValue();
        if ( func != null ) { // show X, Y values from the function
          JTextArea textArea = (JTextArea) valueEditor;
          textArea.setText("");
          int num = func.getNum();
          for(int i=0; i<num; ++i) textArea.append(func.getX(i)+"\t"+func.getY(i)+"\n");
        }
        else ((JTextArea) valueEditor).setText( "" );

    }
}
