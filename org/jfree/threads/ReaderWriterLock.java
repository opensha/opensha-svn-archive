/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ---------------------
 * ReaderWriterLock.java
 * ---------------------
 *
 * $Id$
 *
 * Changes
 * -------
 * 29-Jan-2003 : Added standard header (DG);
 *
 */

package org.jfree.threads;

import java.util.Vector;
import java.util.Enumeration;

/**
 * A reader-writer lock from "Java Threads" by Scott Oak and Henry Wong.
 * 
 * @author Scott Oak and Henry Wong
 */
public class ReaderWriterLock {

    /** The waiting threads. */
    private Vector waiters;

    /**
     * Default constructor.
     */
    public ReaderWriterLock() {
        this.waiters = new Vector();
    }

    /**
     * Grab the read lock.
     */
    public synchronized void lockRead() {
        ReaderWriterNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new ReaderWriterNode(me, ReaderWriterNode.READER);
            waiters.addElement(node);
        }
        else {
            node = (ReaderWriterNode) waiters.elementAt(index);
        }
        while (getIndex(me) > firstWriter()) {
            try {
                wait();
            }
            catch (Exception e) {
                System.err.println("ReaderWriterLock.lockRead(): exception.");
                System.err.print(e.getMessage());
            }
        }
        node.nAcquires++;
    }

    /**
     * Grab the write lock.
     */
    public synchronized void lockWrite() {
        ReaderWriterNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new ReaderWriterNode(me, ReaderWriterNode.WRITER);
            waiters.addElement(node);
        }
        else {
            node = (ReaderWriterNode) waiters.elementAt(index);
            if (node.state == ReaderWriterNode.READER) {
                throw new IllegalArgumentException("Upgrade lock");
            }
            node.state = ReaderWriterNode.WRITER;
        }
        while (getIndex(me) != 0) {
            try {
                wait();
            }
            catch (Exception e) {
                System.err.println("ReaderWriterLock.lockWrite(): exception.");
                System.err.print(e.getMessage());
            }
        }
        node.nAcquires++;
    }

    /**
     * Unlock.
     */
    public synchronized void unlock() {

        ReaderWriterNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index > firstWriter()) {
            throw new IllegalArgumentException("Lock not held");
        }
        node = (ReaderWriterNode) waiters.elementAt(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            waiters.removeElementAt(index);
        }
        notifyAll();
    }

    /**
     * Returns the index of the first waiting writer.
     * 
     * @return The index.
     */
    private int firstWriter() {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            ReaderWriterNode node = (ReaderWriterNode) e.nextElement();
            if (node.state == ReaderWriterNode.WRITER) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Returns the index of a thread.
     * 
     * @param t  the thread.
     * 
     * @return The index.
     */
    private int getIndex(Thread t) {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            ReaderWriterNode node = (ReaderWriterNode) e.nextElement();
            if (node.t == t) {
                return index;
            }
        }
        return -1;
    }

}

/**
 * A node for the waiting list.
 * 
 * @author Scott Oak and Henry Wong
 */
class ReaderWriterNode {

    /** A reader. */
    static final int READER = 0;
    
    /** A writer. */
    static final int WRITER = 1;
    
    /** The thread. */
    Thread t;
    
    /** The state. */
    int state;

    /** The number of acquires.*/
    int nAcquires;

    /**
     * Creates a new node.
     * 
     * @param t  the thread.
     * @param state  the state.
     */
    ReaderWriterNode(Thread t, int state) {
        this.t = t;
        this.state = state;
        this.nAcquires = 0;
    }

}
