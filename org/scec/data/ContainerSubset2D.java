package org.scec.data;
import java.io.Serializable;

import java.util.*;


/**
 *  <b>Title:</b> ContainerSubset2D<br>
 *  <b>Description:</b> Small read only window into larger 2 dimensinal data set
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 25, 2002
 * @version    1.0
 */

public class ContainerSubset2D implements Container2DAPI, Serializable {

    /**
     *  Description of the Field
     */
    protected final static String C = "ContainerSubset2D";
    /**
     *  Description of the Field
     */
    protected final static boolean D = false;

    protected String name = "";

    /**
     *  Data containing indexing information into larger dataset
     */
    protected Window2D window = new Window2D();

    /**
     *  Description of the Field
     */
    protected Window2D oldWindow = null;

    /**
     *  pointer to gridded data
     */
    protected Container2DAPI data = null;


    /**
     *  Constructor for the ContainerSubset2D object
     *
     * @param  numRows                             Description of the Parameter
     * @param  numCols                             Description of the Parameter
     * @param  startRow                            Description of the Parameter
     * @param  startCol                            Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ContainerSubset2D( int numRows, int numCols, int startRow, int startCol )
             throws ArrayIndexOutOfBoundsException {

        String S = C + ": Constructor():";

        window.startRow = startRow;
        window.startCol = startCol;
        window.numRows = numRows;
        window.numCols = numCols;

        window.checkLowerBounds( S );
        window.calcUpperBounds();

    }


    /**
     *  Constructor for the ContainerSubset2D object
     *
     * @param  numRows                             Description of the Parameter
     * @param  numCols                             Description of the Parameter
     * @param  startRow                            Description of the Parameter
     * @param  startCol                            Description of the Parameter
     * @param  data                                Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ContainerSubset2D( int numRows, int numCols, int startRow, int startCol, Container2DAPI data )
             throws ArrayIndexOutOfBoundsException {

        String S = C + ": Constructor2():";

        window.startRow = startRow;
        window.startCol = startCol;
        window.numRows = numRows;
        window.numCols = numCols;

        window.checkLowerBounds( S );
        window.calcUpperBounds();
        setContainer2D( data );

    }


    /**
     *  this is the data set that this class provides a window into. All indexes
     *  must have been set already. If invalid data, the window is rolled back
     *  to the old one.
     *
     * @param  data                                The new container2D value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void setContainer2D( Container2DAPI data ) throws ArrayIndexOutOfBoundsException {
        String S = C + ": setContainer2D():";

        initTransaction();

        window.maxNumRows = data.getNumRows();
        window.maxNumCols = data.getNumCols();

        try {
            window.checkUpperBounds( S );
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
        this.data = data;
    }


    /**
     *  used to initialize the start row pointer to the main dataset. If invalid
     *  data, the window is rolled back to the old one.
     *
     * @param  startRow                            The new startRow value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void setStartRow( int startRow ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": setStartRow():";
        initTransaction();
        window.startRow = startRow;

        try {
            window.checkLowerBounds( S );
            window.calcUpperBounds();
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
    }

    protected void validate() throws ArrayIndexOutOfBoundsException {

        String S = C + ": validate():";
        window.checkLowerBounds( S );
        window.calcUpperBounds();
        window.checkUpperBounds( S );

        if(this.data == null){
            throw new ArrayIndexOutOfBoundsException(S + "Data list cannot be null");
        }


    }

    /**
     *  used to initialize the start row pointer to the main dataset. If invalid
     *  data, the window is rolled back to the old one.
     *
     * @param  startCol                            The new startCol value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void setStartCol( int startCol ) throws ArrayIndexOutOfBoundsException {
        String S = C + ": setStartCol():";
        initTransaction();
        window.startCol = startCol;

        try {
            window.checkLowerBounds( S );
            window.calcUpperBounds();
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
    }


    /**
     *  Sublcass not allowed to modify data, i.e. read only
     *
     * @param  row                                 Description of the Parameter
     * @param  column                              Description of the Parameter
     * @param  obj                                 Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void set( int row, int column, Object obj ) throws ArrayIndexOutOfBoundsException {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     *  Gets the container2D attribute of the ContainerSubset2D object
     *
     * @return    The container2D value
     */
    public Container2DAPI getContainer2D() {
        return data;
    }


    /**
     *  Gets the startRow attribute of the ContainerSubset2D object
     *
     * @return    The startRow value
     */
    public int getStartRow() {
        return window.startRow;
    }


    /**
     *  Gets the startCol attribute of the ContainerSubset2D object
     *
     * @return    The startCol value
     */
    public int getStartCol() {
        return window.startCol;
    }


    /**
     *  used to initialize the number of rows
     *
     * @return    The numRows value
     */
    public int getNumRows() {
        return window.numRows;
    }


    /**
     *  used to initialize the number of columns
     *
     * @return    The numCols value
     */
    public int getNumCols() {
        return window.numCols;
    }


    /**
     *  used to initialize the number of rows
     *
     * @return    The numRows value
     */
    public int getEndRow() {
        return window.endRow;
    }


    /**
     *  used to initialize the number of columns
     *
     * @return    The numCols value
     */
    public int getEndCol() {
        return window.endCol;
    }


    /**
     *  Description of the Method
     *
     * @param  row                                 Description of the Parameter
     * @param  column                              Description of the Parameter
     * @return                                     Description of the Return
     *      Value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public Object get( int row, int column ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": getColumnIterator(): ";

        if ( !window.isValidCol( column ) ) {
            throw new ArrayIndexOutOfBoundsException( S + "The specified column is invalid, either negative or beyond upper index of window. " + column );
        }

        if ( !window.isValidRow( row ) ) {
            throw new ArrayIndexOutOfBoundsException( S + "The specified row is invalid, either negative or beyond upper index of window. " + column );
        }

        int transRow = window.getTranslatedRow( row );
        int transCol = window.getTranslatedCol( column );

        return data.get( transRow, transCol );

    }



    /**
     *  iterate over all columns in one row of the surface
     *
     * @param  row                                 Description of the Parameter
     * @return                                     The columnIterator value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ListIterator getColumnIterator( int row ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": getColumnIterator(): ";

        if ( !window.isValidRow( row ) ) {
            throw new ArrayIndexOutOfBoundsException( S +
                    "The specified row is invalid, either negative or beyond upper index of window. " + row );
        }

        validate();
        ColumnIterator it = new ColumnIterator( row );
        return ( ListIterator ) it;
    }


    /**
     *  iterate over all rows in one column in the surface
     *
     * @param  column                              Description of the Parameter
     * @return                                     The rowIterator value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ListIterator getRowIterator( int column ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": getRowIterator(): ";
        if ( !window.isValidCol( column ) ) {
            throw new ArrayIndexOutOfBoundsException( S + "The specified column is invalid, either negative or beyond upper index of window. " + column );
        }

        validate();
        RowIterator it = new RowIterator( column );
        return ( ListIterator ) it;
    }


    /**
     *  iterate over all points, all rows per column, iterating over all columns
     *
     * @return    The allByColumnsIterator value
     */
    public ListIterator getAllByColumnsIterator() {
        validate();
        AllByColumnsIterator it = new AllByColumnsIterator();
        return ( ListIterator ) it;
    }


    /**
     *  iterate over all points, all columns per row, iterating over all rows
     *
     * @return    The allByRowsIterator value
     */
    public ListIterator getAllByRowsIterator() {
        validate();
        AllByRowsIterator it = new AllByRowsIterator();
        return ( ListIterator ) it;
    }


    /**
     *  Description of the Method
     *
     * @param  delta                               Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void shiftRows( int delta ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": shiftRows():";
        initTransaction();

        try {
            window.shiftRows( delta );
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
    }


    /**
     *  Description of the Method
     */
    protected void initTransaction() {
        oldWindow = ( Window2D ) window.clone();
    }


    /**
     *  Description of the Method
     */
    protected void rollback() {
        window = oldWindow;
        oldWindow = null;
    }

    /**
     *  Description of the Method
     */
    protected void commit() {
        oldWindow = null;
    }


    /**
     *  Description of the Method
     *
     * @param  delta                               Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void shiftCols( int delta ) throws ArrayIndexOutOfBoundsException {

        String S = C + ": shiftCols():";
        initTransaction();

        try {
            window.shiftCols( delta );
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }

    }



    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public long size() {
        return window.windowSize();
    }


    /**
     *  Description of the Method
     *
     * @param  row     Description of the Parameter
     * @param  column  Description of the Parameter
     * @return         Description of the Return Value
     */
    public boolean exist( int row, int column ) {

        String S = C + ": exist():";

        if ( !window.isValidCol( column ) ) {
            throw new ArrayIndexOutOfBoundsException( S + "The specified column is invalid, either negative or beyond upper index of window. " + column );
        }

        if ( !window.isValidRow( row ) ) {
            throw new ArrayIndexOutOfBoundsException( S + "The specified row is invalid, either negative or beyond upper index of window. " + column );
        }

        int transRow = window.getTranslatedRow( row );
        int transCol = window.getTranslatedCol( column );

        return data.exist(transRow, transCol);
    }


    /**
     *  Sublcass not allowed to modify data, i.e. read only
     */
    public void clear() {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     *  Sublcass not allowed to modify data, i.e. read only
     *
     * @param  row                                          Description of the
     *      Parameter
     * @param  column                                       Description of the
     *      Parameter
     * @exception  ArrayIndexOutOfBoundsException           Description of the
     *      Exception
     * @exception  java.lang.UnsupportedOperationException  Description of the
     *      Exception
     */
    public void delete( int row, int column )
             throws
            ArrayIndexOutOfBoundsException,
            java.lang.UnsupportedOperationException {

        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     *  iterate over all data points, no guarentee of order returned
     *
     * @return    Description of the Return Value
     */
    public ListIterator listIterator() {
        validate();
        AllByRowsIterator it = new AllByRowsIterator();
        return ( ListIterator ) it;
    }


    /**
     *  Use Window2D to generate this
     *
     * @return    Description of the Return Value
     */
    public Object[][] toJava2D() {

        int transRow, transCol;
        Object[][] d = new Object[window.numRows][window.numCols];
        for ( int j = 0; j < window.numRows; j++ ) {
            for ( int i = 0; i < window.numCols; i++ ) {
                transRow = window.getTranslatedRow( j );
                transCol = window.getTranslatedCol( i );
                d[i][j] =  data.get(transRow, transCol);
            }
        }
        return d;
    }



    /**
     *  <b>Title:</b> Container2DListIterator<br>
     *  <b>Description:</b> Base abstract class for all iterators. Stores the
     *  indexes, etc, and implements nextIndex() and hasNext(). All unsupported
     *  methods throws Exceptions. <br>
     *  This is how iterators should be handled, i.e. the class should be an
     *  inner class so that the outside world only ever sees a ListIterator.
     *  <br>
     *  The iterator shouldn't be in a seperate class file because it needs
     *  intimate knowledge to the data structure (in this case a java array)
     *  which is usually hidden to the outside world. By making it an inner
     *  class, the iterator has full access to the private variables of the data
     *  class.<br>
     *  <b>Copyright:</b> Copyright (c) 2001<br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W. Rock
     * @created    February 25, 2002
     * @version    1.0
     */

    abstract class Container2DListIterator implements ListIterator {

        /**
         *  Description of the Field
         */
        int cursor = 0;
        /**
         *  Description of the Field
         */
        int lastRet = -1;
        /**
         *  Description of the Field
         */
        int lastIndex = 0;


        /**
         *  returns full column to iterate over, pinned to one row
         */
        public Container2DListIterator() { }


        /**
         *  Description of the Method
         *
         * @param  obj                                Description of the
         *      Parameter
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public void set( Object obj ) throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "set(Object obj) Not implemented." );
        }


        /**
         *  Description of the Method
         *
         * @return    Description of the Return Value
         */
        public boolean hasNext() {
            return cursor != lastIndex;
        }


        /**
         *  Description of the Method
         *
         * @return    Description of the Return Value
         */
        public int nextIndex() {
            return cursor;
        }


        /**
         *  Description of the Method
         *
         * @return                             Description of the Return Value
         * @exception  NoSuchElementException  Description of the Exception
         */
        public abstract Object next() throws NoSuchElementException;


        /**
         *  Description of the Method
         *
         * @return                                    Description of the Return
         *      Value
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public Object previous() throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "hasPrevious() Not implemented." );
        }


        /**
         *  Description of the Method
         *
         * @return                                    Description of the Return
         *      Value
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public int previousIndex() throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "hasPrevious() Not implemented." );
        }


        /**
         *  Description of the Method
         *
         * @return                                    Description of the Return
         *      Value
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public boolean hasPrevious() throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "hasPrevious() Not implemented." );
        }


        /**
         *  Description of the Method
         *
         * @param  obj                                Description of the
         *      Parameter
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public void add( Object obj ) throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "add(Object obj) Not implemented." );
        }


        /**
         *  Description of the Method
         *
         * @exception  UnsupportedOperationException  Description of the
         *      Exception
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "remove() Not implemented." );
        }

    }


    /**
     *  <b>Title:</b> ColumnIterator<br>
     *  <b>Description:</b> Returns all column points for one row<br>
     *  <b>Copyright:</b> Copyright (c) 2001<br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W. Rock
     * @created    February 25, 2002
     * @version    1.0
     */
    class ColumnIterator extends Container2DListIterator implements ListIterator {

        final static String C = "ColumnIterator";

        /**
         *  Description of the Field
         */
        int translatedPinnedRow;


        /**
         *  returns full column to iterate over, pinned to one row
         *
         * @param  row  Description of the Parameter
         */
        public ColumnIterator( int row ) throws ArrayIndexOutOfBoundsException{
            super();
            String S = C + ": Constructor():";

            if ( !window.isValidRow( row ) ) {
                throw new ArrayIndexOutOfBoundsException( S + "The specified row is invalid, either negative or beyond upper index of window. " + row );
            }

            translatedPinnedRow = window.getTranslatedRow( row );
            lastIndex = window.numCols;

        }


        /**
         *  Description of the Method
         *
         * @return                             Description of the Return Value
         * @exception  NoSuchElementException  Description of the Exception
         */
        public Object next() throws NoSuchElementException {
            try {

                int transColumn = window.getTranslatedCol( cursor );
                Object object = data.get(translatedPinnedRow, transColumn);
                lastRet = cursor++;
                return object;
            } catch ( IndexOutOfBoundsException e ) {
                throw new NoSuchElementException( "You have iterated past the last element." + e.toString() );
            }
        }

    }


    /**
     *  <b>Title:</b> RowIterator<br>
     *  <b>Description:</b> Returns all column points for one row<br>
     *  <b>Copyright:</b> Copyright (c) 2001<br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W. Rock
     * @created    February 25, 2002
     * @version    1.0
     */
    class RowIterator extends Container2DListIterator implements ListIterator {

        final static String C = "RowIterator";

        /**
         *  Description of the Field
         */
        int translatedPinnedCol;


        /**
         *  returns full row to iterate over, pinned to one column
         *
         * @param  col  Description of the Parameter
         */
        public RowIterator( int col ) throws ArrayIndexOutOfBoundsException{
            super();
            String S = C + ": Constructor():";

            if ( !window.isValidRow( col ) ) {
                throw new ArrayIndexOutOfBoundsException( S + "The specified col is invalid, either negative or beyond upper index of window. " + col );
            }

            translatedPinnedCol = window.getTranslatedRow( col );
            lastIndex = window.numRows;

        }


        /**
         *  Description of the Method
         *
         * @return                             Description of the Return Value
         * @exception  NoSuchElementException  Description of the Exception
         */
        public Object next() throws NoSuchElementException {
            try {

                int transRow = window.getTranslatedRow( cursor );
                Object object = data.get(transRow, translatedPinnedCol);
                lastRet = cursor++;
                return object;
            } catch ( IndexOutOfBoundsException e ) {
                throw new NoSuchElementException( "You have iterated past the last element." + e.toString() );
            }
        }

    }


    /**
     *  <b>Title:</b> AllByColumnsIterator<br>
     *  <b>Description:</b> Returns all rows for a column, then moves to the
     *  next column<br>
     *  <b>Copyright:</b> Copyright (c) 2001<br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W. Rock
     * @created    February 25, 2002
     * @version    1.0
     */
    class AllByColumnsIterator extends Container2DListIterator implements ListIterator {

        /**
         *  Description of the Field
         */
        int currentColumn = 0;
        /**
         *  Description of the Field
         */
        int currentRow = 0;

        int transRow = 0;
        int transCol = 0;


        /**
         *  Constructor for the AllByColumnsIterator object
         */
        public AllByColumnsIterator() {
            super();
            lastIndex = window.windowSize();
            transRow = window.startRow;
            transCol = window.startCol;
        }


        /**
         *  Description of the Method
         *
         * @return                             Description of the Return Value
         * @exception  NoSuchElementException  Description of the Exception
         */
        public Object next() throws NoSuchElementException {

            try {

                Object object = data.get(transRow, transCol);

                currentRow++;
                transRow = window.getTranslatedRow( currentRow );
                if ( currentRow == window.numRows ) {
                    currentRow = 0;
                    transRow = window.getTranslatedRow( currentRow );
                    currentColumn++;
                    transCol = window.getTranslatedCol( currentColumn );
                }

                lastRet = cursor++;
                return object;
            } catch ( IndexOutOfBoundsException e ) {
                throw new NoSuchElementException( "You have iterated past the last element. " + e.toString() );
            }

        }
    }



    /**
     *  <b>Title:</b> AllByRowsIterator<br>
     *  <b>Description:</b> Returns all columns for a row, then moves to the
     *  next row<br>
     *  <b>Copyright:</b> Copyright (c) 2001<br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W. Rock
     * @created    February 25, 2002
     * @version    1.0
     */
    class AllByRowsIterator extends Container2DListIterator implements ListIterator {


        /**
         *  Description of the Field
         */
        int currentCol = 0;
        /**
         *  Description of the Field
         */
        int currentRow = 0;

        int transRow = 0;
        int transCol = 0;

        /**
         *  Constructor for the AllByRowsIterator object
         */
        public AllByRowsIterator() {
            super();
            lastIndex = window.windowSize();
            transRow = window.startRow;
            transCol = window.startCol;
        }


        /**
         *  Description of the Method
         *
         * @return                             Description of the Return Value
         * @exception  NoSuchElementException  Description of the Exception
         */
        public Object next() throws NoSuchElementException {

            try {

                Object object = data.get(transRow, transCol);

                currentCol++;
                transCol = window.getTranslatedCol( currentCol );
                if ( currentCol == window.numCols ) {
                    currentCol = 0;
                    transCol = window.getTranslatedCol( currentCol );
                    currentRow++;
                    transRow = window.getTranslatedRow( currentRow );
                }

                lastRet = cursor++;
                return object;
            } catch ( IndexOutOfBoundsException e ) {
                throw new NoSuchElementException( "You have iterated past the last element." + e.toString() );
            }

        }

    }


    final protected static char TAB = '\t';
    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append('\n');

        if( window == null ){
            b.append( "No window specified, unable to print out locations" );
        }
        else{

            int i = 0, j, counter = 0;
            while( i < window.numRows){

                j = 0;
                while( j < window.numCols){

                    b.append( "" + i + TAB + j + TAB);
                    Object obj = this.get(i, j);
                    if( obj != null ) {
                        b.append( obj.toString() );
                        counter++;
                    }
                    else b.append( "NULL" );
                    b.append('\n');

                    j++;
                }
                i++;

            }
            b.append( "\nNumber of Rows = " + window.numRows + '\n' );
            b.append( "Number of Columns = " + window.numCols + '\n' );
            b.append( "Size = " + window.numCols * window.numRows + '\n' );
            b.append( "Number of non-null objects = " + counter + '\n' );
            b.append( "Start Row of main Surface = " + window.startRow + '\n' );
            b.append( "Start COl of main Surface = " + window.startCol + '\n' );
        }
        return b.toString();
    }


    /**
     *  The main program for the Container2D class
     *
     * @param  args  The command line arguments
     */
    public static void main( String[] args ) {

        String S = C + ": Main(): ";
        System.out.println( S + "Starting" );

        int xsize = 5;
        int ysize = 10;

        Container2D data = new Container2D( xsize, ysize );
        for ( int x = 0; x < xsize; x++ ) {
            for ( int y = 0; y < ysize; y++ ) {
                data.set( x, y, "[" + x + ", " + y + ']');
                System.out.println(S + data.get(x,y).toString() );
            }
        }

        int numRows = 2;
        int numCols = 3;
        int startRow = 1;
        int startCol = 2;
        ContainerSubset2D sub = new ContainerSubset2D(numRows, numCols, startRow, startCol, data);
        sub.validate();
        System.out.println( S + sub.window.toString() );



        System.out.println( S );
        System.out.println( S );
        System.out.println( S + "getColumnIterator");

        ListIterator it = sub.getColumnIterator(0);
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S );
        System.out.println( S );
        System.out.println( S + "getRowIterator");

        it = sub.getRowIterator(0);
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S );
        System.out.println( S );
        System.out.println( S + "getAllByRowssIterator");

        it = sub.getAllByRowsIterator();
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S );
        System.out.println( S );
        System.out.println( S + "getAllByColumnsIterator");

        it = sub.getAllByColumnsIterator();
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S );
        System.out.println( S );
        System.out.println( S + "List Iterator");

        it = sub.listIterator();
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S );
        System.out.println( S );
        System.out.println( S  + "Shifting rows by 2");
        System.out.println( S + "Shifting cols by 1");
        System.out.println( S + "List Iterator");

        sub.shiftCols(1);
        sub.shiftRows(2);
        it = sub.listIterator();
        while ( it.hasNext() ) {

            Object obj = it.next();
            if ( obj != null ) {
                System.out.println( S + obj.toString() );
            } else {
                System.out.println( S + obj.toString() );
            }

        }


        System.out.println( S + "Ending" );

    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
