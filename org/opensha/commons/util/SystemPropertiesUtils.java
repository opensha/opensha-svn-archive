package org.opensha.commons.util;

/**
 * <p>Title: SystemPropertiesUtils</p>
 * <p>Description: Gets the System properties of the current user</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public final class SystemPropertiesUtils {

  private final static String SYSTEM_LINE_SEPERATOR = System.getProperty("line.separator");
  private final static String SYSTEM_FILE_SEPERATOR = System.getProperty("file.separator");
  private final static String SYSTEM_OS = System.getProperty("os.name");

  /**
   *
   * @returns the System specific line separtor
   */
  public static String getSystemLineSeparator(){
    return SYSTEM_LINE_SEPERATOR;
  }

  /**
   *
   * @returns the System specific file separator
   */
  public static String getSystemFileSeparator(){
    return SYSTEM_FILE_SEPERATOR;
  }

  /**
   *
   * @returns the System specific system name
   */
  public static String getSystemName(){
    return SYSTEM_OS;
  }

}
