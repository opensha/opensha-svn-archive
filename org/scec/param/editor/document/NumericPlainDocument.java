package org.scec.param.editor.document;

import java.text.*;
import javax.swing.text.*;

/**
 * <b>Title:</b> NumericPlainDocument<br>
 * <b>Description:</b> Model ( or data) associated with an Numeric Text Field. The insertString() function
 * is called whenever data is being entered into the text field. This is where the text field is checked
 * to make sure only numeric valid charachters are being added.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */


public class NumericPlainDocument extends PlainDocument
{
    protected static final String C = "NumericPlainDocument";
    protected static final boolean D = false;

    protected InsertErrorListener errorListener;

    protected static DecimalFormat defaultFormat = new DecimalFormat();
    protected DecimalFormat format;

    protected char decimalSeparator;
    protected char groupingSeparator;

    protected String positivePrefix;
    protected int positivePrefixLen;
    protected String positiveSuffix;
    protected int positiveSuffixLen;

    protected String negativePrefix;
    protected int negativePrefixLen;
    protected String negativeSuffix;
    protected int negativeSuffixLen;

    protected ParsePosition parsePos;

    public interface InsertErrorListener{

        public void insertFailed(
            NumericPlainDocument numericplaindocument,
            int i,
            String string,
            AttributeSet attributeset
        );

    }

    public NumericPlainDocument() {
    	parsePos = new ParsePosition(0);
	    setFormat(null);
    }

    public NumericPlainDocument(DecimalFormat format) {
	    parsePos = new ParsePosition(0);
	    setFormat(format);
    }

    public NumericPlainDocument(
        AbstractDocument.Content content,
        DecimalFormat format
    ) {
        super(content);
        String S = "NumericPlainDocument: Constructor(content, format): ";

        parsePos = new ParsePosition(0);
        setFormat(format);

        try { format.parseObject(content.getString(0, content.length()), parsePos); }
        catch (Exception e) {
            throw new IllegalArgumentException(S + "Initial context not a valid number" );
        }

        if (parsePos.getIndex() != content.length() - 1)
            throw new IllegalArgumentException(S + "Initial context not a valid number");
    }


    public DecimalFormat getFormat() { return format; }

    public void setFormat(DecimalFormat fmt) {
        format = fmt != null ? fmt : (DecimalFormat) defaultFormat.clone();

        decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();

        groupingSeparator = format.getDecimalFormatSymbols().getGroupingSeparator();

        positivePrefix = format.getPositivePrefix();
        positivePrefixLen = positivePrefix.length();
        positiveSuffix = format.getPositiveSuffix();
        positiveSuffixLen = positiveSuffix.length();

        negativePrefix = format.getNegativePrefix();
        negativePrefixLen = negativePrefix.length();
        negativeSuffix = format.getNegativeSuffix();
        negativeSuffixLen = negativeSuffix.length();
    }


    public Number getNumberValue() throws ParseException {

        String S = "NumericPlainDocument: getNumberValue(): ";
        try {

            String context = this.getText(0, this.getLength());
            parsePos.setIndex(0);
            Number result = format.parse(context, parsePos);

            if (parsePos.getIndex() != this.getLength())
            throw new ParseException(S + "Not a valid number: " + context, 0);

            return result;
        }
        catch (BadLocationException e) {
            throw new ParseException(S + "Not a valid number: ", 0);
        }

    }

    public Long getLongValue() throws ParseException {

        Number result = getNumberValue();
        if (result instanceof Long == false) {
            String S = "NumericPlainDocument: getLongValue(): ";
            throw new ParseException( S + "Not a valid Long: " + result, 0);
        }
        return (Long) result;

    }

    public Double getDoubleValue() throws ParseException {

        Number result = getNumberValue();
        if (result instanceof Double == false) {
            String S = "NumericPlainDocument: getDoubleValue(): ";
            throw new ParseException(S + "Not a valid Double: " + result, 0);
        }
        return (Double) result;
    }



    public void insertString(int offset, String str, AttributeSet a)
	    throws BadLocationException
    {

        String S = C + "insertString: (): ";
        if( D ) System.out.println(S + "Starting");

        if (str != null && str.length() != 0) {

            AbstractDocument.Content content = this.getContent();
            int length = content.length();
            int originalLength = length;
            parsePos.setIndex(0);

            String targetString = content.getString(0, offset) +
                str + content.getString(offset,length - offset - 1);

            boolean gotPositive = targetString.startsWith(positivePrefix);
            boolean gotNegative = targetString.startsWith(negativePrefix);
            length = targetString.length();

            do {
                if (gotPositive == true || gotNegative == true) {

                    if (gotPositive == true && gotNegative == true) {
                        if (positivePrefixLen > negativePrefixLen) gotNegative = false;
                        else gotPositive = false;
                    }

                    String suffix;
                    int suffixLength;
                    int prefixLength;

                    if (gotPositive == true) {
                        suffix = positiveSuffix;
                        suffixLength = positiveSuffixLen;
                        prefixLength = positivePrefixLen;
                    }
                    else {
                        suffix = negativeSuffix;
                        suffixLength = negativeSuffixLen;
                        prefixLength = negativePrefixLen;
                    }

                    if (length == prefixLength) break;

                    if (targetString.endsWith(suffix) == false) {
                        int i;
                        for (i = suffixLength - 1; i > 0; i--) {
                            if (targetString.regionMatches(length - i, suffix, 0, i)) {
                                targetString = targetString + suffix.substring(i);
                                break;
                            }
                        }
                        if (i == 0) targetString += suffix;
                        length = targetString.length();
                    }
                }

                format.parse(targetString, parsePos);
                int endIndex = parsePos.getIndex();

                if (endIndex != length

                    && (positivePrefixLen <= 0 || endIndex >= positivePrefixLen
                    || length > positivePrefixLen
                    || !targetString.regionMatches(0, positivePrefix, 0,length))

                    && (negativePrefixLen <= 0 || endIndex >= negativePrefixLen
                    || length > negativePrefixLen
                    || !targetString.regionMatches(0, negativePrefix, 0, length)))
                {

                    char lastChar = targetString.charAt(originalLength - 1);
                    int decimalIndex = targetString.indexOf(decimalSeparator);

                   /* if ((!format.isGroupingUsed()
                        || lastChar != groupingSeparator
                        || decimalIndex != -1)
                        && (format.isParseIntegerOnly() != false
                        || lastChar != decimalSeparator
                        || decimalIndex != originalLength - 1))
                    {
                        if (errorListener != null) errorListener.insertFailed(this, offset, str, a);
                        return;
                    }*/
                }
            }

            while ( true == false);

            String context = this.getText(0, this.getLength());
            if( D ) System.out.println(S + "Current context = " + context);
            if( D ) System.out.println(S + "Inserting " + str + " at " + offset);

            super.insertString(offset, str, a);
            context = this.getText(0, this.getLength());

            if( D ) System.out.println(S + "Ending: New context = " + context);


        }
    }



    public void addInsertErrorListener(InsertErrorListener l) {
        if (errorListener == null) errorListener = l;
        else throw new IllegalArgumentException
                  ("NumericPlainDocument: addInsertErrorListener(): InsertErrorListener already registered");
    }

    public void removeInsertErrorListener(InsertErrorListener l) {
        if (errorListener == l) errorListener = null;
    }
}
