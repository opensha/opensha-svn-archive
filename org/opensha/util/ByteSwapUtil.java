package org.opensha.util;

/**
 * <p>Title: ByteSwapUtil</p>
 *
 * <p>Description: This program swaps the byte ordering and read the data value.
 * This progarm can be used when a binary data file created with "Little Endian" byte
 * ordering technique is being read by a java program . </p>
 * <p>Note: Java always reads binary files in "Big Endian" format.</p>
 * <p>This program will swap the byte ordering of the bytes read by java program,
 * so as to get the correct value data.</p>
 * @author not attributable
 * @version 1.0
 */
public final class ByteSwapUtil {

  /**
   * Swaps the byte ordering for the integer value
   * @param value int
   * @return int
   */
  public static int swap(int value) {
    int b1 = (value >> 0) & 0xff;
    int b2 = (value >> 8) & 0xff;
    int b3 = (value >> 16) & 0xff;
    int b4 = (value >> 24) & 0xff;

    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
  }

  /**
   * Swaps the byte prdering for the float value
   * @param value float
   * @return float
   */
  public static float swap(float value) {
    int intValue = Float.floatToIntBits(value);
    intValue = swap(intValue);
    return Float.intBitsToFloat(intValue);
  }

  /**
   * Byte swap a single short value.
   *
   * @param value  Value to byte swap.
   * @return       Byte swapped representation.
   */
  public static short swap(short value) {

    int b1 = value & 0xff;
    int b2 = (value >> 8) & 0xff;

    return (short) (b1 << 8 | b2 << 0);
  }

  /**
   * Byte swap a single long value.
   *
   * @param value  Value to byte swap.
   * @return       Byte swapped representation.
   */
  public static long swap(long value) {
    long b1 = (value >> 0) & 0xff;
    long b2 = (value >> 8) & 0xff;
    long b3 = (value >> 16) & 0xff;
    long b4 = (value >> 24) & 0xff;
    long b5 = (value >> 32) & 0xff;
    long b6 = (value >> 40) & 0xff;
    long b7 = (value >> 48) & 0xff;
    long b8 = (value >> 56) & 0xff;

    return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
        b5 << 24 | b6 << 16 | b7 << 8 | b8 << 0;
  }

}
