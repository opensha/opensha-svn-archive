package org.scec.param.editor.document;

import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

// Fix - Needs more comments

/**
 * <b>Title:</b> IntegerPlainDocument<p>
 *
 * <b>Description:</b> Model ( or data) associated with an Integer Text Field. The insertString() function
 * is called whenever data is being entered into the text field. This is where the text field is checked
 * to make sure only integer valid charachters are being added.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */


public class IntegerPlainDocument extends PlainDocument{

    protected static final String C = "IntegerPlainDocument";
    protected static final boolean D = false;

    protected int min = Integer.MIN_VALUE;
    protected int max = Integer.MAX_VALUE;

    protected ParsePosition parsePos = new ParsePosition(0);
    protected InsertErrorListener errorListener;

    public static interface InsertErrorListener {
	    public void insertFailed(
            IntegerPlainDocument integerplaindocument,
            int i, String string,
            AttributeSet attributeset, String string_0_
        );
    }

    public void insertString(int offset, String str, AttributeSet a)
	    throws BadLocationException
    {

        String originalModel = this.getText(0, this.getLength() );
        if(D) System.out.println("IntegerPlainDocument: insertString(): Old Model = " + (originalModel) );
        if(D) System.out.println("IntegerPlainDocument: insertString(): Old Model Length = " + this.getLength() );
        if(D) System.out.println("IntegerPlainDocument: insertString(): Adding String at offset = " + offset);

        if (str != null) {
            if (this.getLength() == 0 && str.charAt(0) == '-')
                super.insertString(offset, str, a);
            else {
                try {
                    int i = new Integer(str).intValue();

                    if (i >= min && i <= max) super.insertString(offset, str, a);
                    else if (errorListener != null) {
                        String s = "IntegerPlainDocument: insertString(): Integer value " +
                                    i + " > max(" + max + ") or min(" + min + ')';
                        errorListener.insertFailed(this, offset, str, a, s);
                    }
                }
                catch (NumberFormatException e) {
                    if (errorListener != null)
                        errorListener.insertFailed(this, offset, str, a,e.toString());
                }
            }
        }
    }

    public void addInsertErrorListener(InsertErrorListener l) {
	    if (errorListener == null) errorListener = l;
	    else throw new IllegalArgumentException (C + "addInsertErrorListenerInsert(): ErrorListener already registered");
    }

    public void removeInsertErrorListener(InsertErrorListener l) {
	    if (errorListener == l) errorListener = null;
    }

    public Integer getIntegerValue() throws ParseException {

        String S = "IntegerPlainDocument: getIntegerValue(): ";
	    try {

            String context = this.getText(0, this.getLength());
            parsePos.setIndex(0);

            Integer result = new Integer(context);
            if (parsePos.getIndex() != this.getLength())
                throw new ParseException (S + "Not a valid number: " + context, 0);

            return result;
        }
        catch (BadLocationException e) {
            throw new ParseException(S + "Not a valid number: ", 0);
        }
        catch (NumberFormatException e) {
            throw new ParseException (S + "Model String cannot be converted to an Integer" , 0);
        }
    }
}
