package org.scec.param.editor;

import java.awt.Toolkit;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;

import org.scec.param.editor.document.IntegerPlainDocument;

// Fix - Needs more comments

/**
 * <b>Title:</b> IntegerTextField<p>
 *
 * <b>Description:</b> Special JTextField that only allows integers to be typed in. This
 * text field allows for a negative sign as the first character, only numbers thereafter<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class IntegerTextField extends JTextField
    implements IntegerPlainDocument.InsertErrorListener
{

    /** Class name for debugging. */
    protected final static String C = "IntegerTextField";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    public IntegerTextField() { this(null, 0); }

    public IntegerTextField(String text) { this(text, 0); }

    public IntegerTextField(String text, int columns) {
        super(null, text, columns);
        IntegerPlainDocument doc = (IntegerPlainDocument) this.getDocument();
        doc.addInsertErrorListener(this);
    }

    public Integer getIntegerValue() throws ParseException {
	    return ((IntegerPlainDocument) this.getDocument()).getIntegerValue();
    }

    public void setValue(Integer integer) { this.setText(integer.toString()); }
    public void setValue(int i) { this.setText("" + i); }

    public void insertFailed(
        IntegerPlainDocument doc,
        int offset, String str,
        AttributeSet a, String reason
    ) { Toolkit.getDefaultToolkit().beep();  }

    protected Document createDefaultModel() { return new IntegerPlainDocument(); }
}
