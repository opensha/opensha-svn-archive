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
 * ----------------
 * Log.java
 * ----------------
 * (C)opyright 2002, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 */
package org.jfree.util;

import java.util.List;
import java.util.Arrays;

/**
 * A simple logging facility. Create a class implementing the {@link org.jfree.util.LogTarget}
 * interface to use this feature.
 *
 * @author Thomas Morgner
 */
public class Log {

    /**
     * A simple message class.
     */
    public static class SimpleMessage {

        /** The message. */
        private String message;

        /** The parameters. */
        private Object[] param;

        /**
         * Creates a new message.
         *
         * @param message  the message text.
         * @param param1  parameter 1.
         */
        public SimpleMessage(String message, Object param1) {
            this.message = message;
            this.param = new Object[]{param1};
        }

        /**
         * Creates a new message.
         *
         * @param message  the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         */
        public SimpleMessage(String message, Object param1, Object param2) {
            this.message = message;
            this.param = new Object[]{param1, param2};
        }

        /**
         * Creates a new message.
         *
         * @param message  the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         * @param param3  parameter 3.
         */
        public SimpleMessage(String message, Object param1, Object param2, Object param3) {
            this.message = message;
            this.param = new Object[]{param1, param2, param3};
        }

        /**
         * Creates a new message.
         *
         * @param message  the message text.
         * @param param1  parameter 1.
         * @param param2  parameter 2.
         * @param param3  parameter 3.
         * @param param4  parameter 4.
         */
        public SimpleMessage(String message, Object param1, Object param2, Object param3, Object param4) {
            this.message = message;
            this.param = new Object[]{param1, param2, param3, param4};
        }

        /**
         * Creates a new message.
         *
         * @param message  the message text.
         * @param param  the parameters.
         */
        public SimpleMessage(String message, Object[] param) {
            this.message = message;
            this.param = param;
        }

        /**
         * Returns a string representation of the message (useful for debugging).
         *
         * @return the string.
         */
        public String toString() {
            StringBuffer b = new StringBuffer();
            b.append(message);
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    b.append(param[i]);
                }
            }
            return b.toString();
        }
    }


    /** The logging threshold. */
    private int debuglevel;

    /** Storage for the log targets. */
    private LogTarget[] logTargets;

    /** the singleton instance of the Log system. */
    private static Log singleton;

    /**
     * Creates a new Log instance. The Log is used to manage the log targets.
     */
    protected Log() {
        logTargets = new LogTarget[0];
        debuglevel = 100;
    }

    /**
     * Returns the singleton Log instance. A new instance is created if necessary.
     *
     * @return the singleton instance.
     */
    protected static Log getInstance() {
        if (singleton == null) {
            singleton = new Log();
        }
        return singleton;
    }

    /**
     * Redefines or clears the currently used log instance.
     *
     * @param log the new log instance or null, to return to the default implementation.
     */
    protected static void defineLog(Log log) {
        singleton = log;
    }

    /**
     * Returns the currently defined debug level. The higher the level, the more details
     * are printed.
     *
     * @return the debug level.
     */
    public int getDebuglevel() {
        return debuglevel;
    }

    /**
     * Defines the debug level for the log system.
     *
     * @see #getDebuglevel
     * @param debuglevel the new debug level
     */
    protected void setDebuglevel(int debuglevel) {
        this.debuglevel = debuglevel;
    }

    /**
     * Adds a log target to this facility. Log targets get informed, via the LogTarget interface,
     * whenever a message is logged with this class.
     *
     * @param target  the target.
     */
    public synchronized void addTarget(LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        LogTarget[] data = new LogTarget[logTargets.length + 1];
        System.arraycopy(logTargets, 0, data, 0, logTargets.length);
        data[logTargets.length] = target;
        logTargets = data;
    }

    /**
     * Removes a log target from this facility.
     *
     * @param target  the target to remove.
     */
    public synchronized void removeTarget(LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        List l = Arrays.asList(logTargets);
        l.remove(target);

        LogTarget[] targets = new LogTarget[l.size()];
        logTargets = (LogTarget[]) l.toArray(targets);
    }

    /**
     * Returns the registered logtargets.
     *
     * @return the logtargets.
     */
    public LogTarget[] getTargets() {
        LogTarget[] targets = new LogTarget[logTargets.length];
        System.arraycopy(targets, 0, logTargets, 0, logTargets.length);
        return targets;
    }

    /**
     * Replaces all log targets by the given target.
     *
     * @param target the new and only logtarget.
     */
    public synchronized void replaceTargets(LogTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        logTargets = new LogTarget[]{target};
    }

    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message  the message.
     */
    public static void debug(Object message) {
        log(LogTarget.DEBUG, message);
    }

    /**
     * A convenience method for logging a 'debug' message.
     *
     * @param message  the message.
     * @param e  the exception.
     */
    public static void debug(Object message, Exception e) {
        log(LogTarget.DEBUG, message, e);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message  the message.
     */
    public static void info(Object message) {
        log(LogTarget.INFO, message);
    }

    /**
     * A convenience method for logging an 'info' message.
     *
     * @param message  the message.
     * @param e  the exception.
     */
    public static void info(Object message, Exception e) {
        log(LogTarget.INFO, message, e);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message  the message.
     */
    public static void warn(Object message) {
        log(LogTarget.WARN, message);
    }

    /**
     * A convenience method for logging a 'warning' message.
     *
     * @param message  the message.
     * @param e  the exception.
     */
    public static void warn(Object message, Exception e) {
        log(LogTarget.WARN, message, e);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message  the message.
     */
    public static void error(Object message) {
        log(LogTarget.ERROR, message);
    }

    /**
     * A convenience method for logging an 'error' message.
     *
     * @param message  the message.
     * @param e  the exception.
     */
    public static void error(Object message, Exception e) {
        log(LogTarget.ERROR, message, e);
    }

    /**
     * Logs a message to the main log stream.  All attached log targets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * @param level  log level of the message.
     * @param message  text to be logged.
     */
    protected void doLog(int level, Object message) {
        if (level > 3) {
            level = 3;
        }
        if (level <= debuglevel) {
            for (int i = 0; i < logTargets.length; i++) {
                LogTarget t = logTargets[i];
                t.log(level, message);
            }
        }
    }

    /**
     * Logs a message to the main log stream.  All attached log targets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * @param level  log level of the message.
     * @param message  text to be logged.
     */
    public static void log(int level, Object message) {
        getInstance().doLog(level, message);
    }

    /**
     * Logs a message to the main log stream. All attached logTargets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * The exception's stacktrace will be appended to the log-stream
     *
     * @param level  log level of the message.
     * @param message  text to be logged.
     * @param e  the exception, which should be logged.
     */
    public static void log(int level, Object message, Exception e) {
        getInstance().doLog(level, message, e);
    }

    /**
     * Logs a message to the main log stream. All attached logTargets will also
     * receive this message. If the given log-level is higher than the given debug-level
     * in the main config file, no logging will be done.
     *
     * The exception's stacktrace will be appended to the log-stream
     *
     * @param level  log level of the message.
     * @param message  text to be logged.
     * @param e  the exception, which should be logged.
     */
    protected void doLog(int level, Object message, Exception e) {
        if (level > 3) {
            level = 3;
        }

        if (level <= debuglevel) {
            for (int i = 0; i < logTargets.length; i++) {
                LogTarget t = logTargets[i];
                t.log(level, message, e);
            }
        }
    }
}
