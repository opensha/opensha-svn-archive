package com.jrefinery.threads;

import java.util.Vector;
import java.util.Enumeration;

/**
 * A reader-writer lock from "Java Threads" by Scott Oak and Henry Wong.
 */
public class ReaderWriterLock {

    private Vector waiters;

    public ReaderWriterLock() {
        this.waiters = new Vector();
    }

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


class ReaderWriterNode {

    static final int READER = 0;
    static final int WRITER = 1;
    Thread t;
    int state;
    int nAcquires;

    ReaderWriterNode(Thread t, int state) {
        this.t = t;
        this.state = state;
        this.nAcquires = 0;
    }

}
