package org.scec.data;

import java.util.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.util.*;


/**
 *  <b>Title:</b> Container2DAPI<br>
 *  <b>Description:</b> Main interface that all 2d data containers must
 *  implement. These provide functions for iteration through the elements,
 *  replacing elements, etc. Each element is any object that extends Object.
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 25, 2002
 * @version    1.0
 */

public interface Container2DAPI extends NamedObjectAPI{

    /**
     *  Returns the number of rows int this two dimensional container.
     *
     * @return    Number of rows.
     */
    public int getNumRows();

    public void setName(String name);


    /**
     *  Returns the number of columns in this two dimensional container.
     *
     * @return    Get number of columns.
     */
    public int getNumCols();


    /**
     *  deletes all data
     */
    public void clear();


    /**
     *  check if this grid cell has a java object stored in it. Returns false if
     *  this data point is null.
     *
     * @param  row     The x coordinate of the cell.
     * @param  column  The y coordinate of the cell.
     * @return         True if an object has been set in this cell.
     */
    public boolean exist( int row, int column );


    /**
     *  returns the number of cells in this two dimensional matrix.
     *
     * @return    The number of cells.
     */
    public long size();


    /**
     *  Places a Java object into one cell in this two dimensional matrix
     *  specified by the row and column indices.
     *
     * @param  row                                 The x coordinate of the cell.
     * @param  column                              The y coordinate of the cell.
     * @param  obj                                 The Java object to place in
     *      the cell.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row and column
     *      are beyond the two dimensional matrix range.
     * @exception  ClassCastException              Thrown by subclasses that
     *      expect a particular type of Java object.
     */
    public void set( int row, int column, Object obj ) throws
            ArrayIndexOutOfBoundsException,
            ClassCastException;

    /**
     *  Returns the object stored in this two dimensional cell.
     *
     * @param  row     The x coordinate of the cell.
     * @param  column  The y coordinate of the cell.
     * @return
     */
    public Object get( int row, int column );


    /**
     *  set's an object in the 2D grid
     *
     * @param  row     The x coordinate of the cell.
     * @param  column  The y coordinate of the cell.
     */
    public void delete( int row, int column );


    /**
     *  Returns an ordered list iterator over all columns associated with one
     *  row. This returns all the objects in that row.
     *
     * @param  row                                 The x coordinate of the cell.
     * @return                                     The columnIterator value
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row is beyond
     *      the two dimensional matrix range.
     */
    public ListIterator getColumnIterator( int row ) throws ArrayIndexOutOfBoundsException;


    /**
     *  Returns an ordered list iterator over all rows associated with one
     *  column. This returns all the objects in that column.
     *
     * @param  column                              The y coordinate of the cell.
     * @return                                     The rowIterator value
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the column is
     *      beyond the two dimensional matrix range.
     */
    public ListIterator getRowIterator( int column ) throws ArrayIndexOutOfBoundsException;


    /**
     *  This returns an iterator of all the Java objects stored in this two
     *  dimensional matrix iterating over all rows within a column and then
     *  moving to the next column until iteration has been done over all rows
     *  and all columns.
     *
     * @return    The allByColumnsIterator value
     */
    public ListIterator getAllByColumnsIterator();


    /**
     *  This returns an iterator of all the Java objects stored in this two
     *  dimensional matrix iterating over all columns within a rows and then
     *  moving to the next column until iteration has been done over all columns
     *  and all rows.
     *
     * @return    The allByRowsIterator value
     */
    public ListIterator getAllByRowsIterator();


    /**
     *  The most generic iterator that returns all Java objects stored in this two
     *  dimensional matrix with no guarantee of ordering either by rows or by
     *  columns. Internally this function will probably just call
     *  get allByRowsIterator
     *
     * @return    Description of the Return Value
     */
    public ListIterator listIterator();

    public String toString();

}
