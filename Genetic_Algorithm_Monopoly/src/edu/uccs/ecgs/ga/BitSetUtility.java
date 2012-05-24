package edu.uccs.ecgs.ga;

import java.util.BitSet;

/**
 * Help manage the bits in the Simple Genetic Algorithm string chromosome.
 */
public class BitSetUtility {
  public static void main(String[] args) {
    BitSet bs = new BitSet();
    bs.set(0);
    bs.set(2);
    bs.set(4);
    
    int r = sixBits2Int(bs, 0);
    System.out.println(""+r);
    System.out.println(to6BitBinary(r));
  }

  /**
   * Returns the int value of a six bit substring from the BitSet.
   * 
   * @param bs
   *          The BitSet
   * @param index
   *          Which set opf six bits to convert (0 based)
   * @return The int value of the six bits of the BitSet from index+0 to index+5
   *         inclusive.
   */
  public static int sixBits2Int(BitSet bs, int index) {
    int temp = 0;

    for (int j = 0; j < 6; j++)
      if (bs.get((index * 6) + j))
        temp |= 1 << j;

    return temp;
  }

  /**
   * Convert an integer into a six bit binary string.
   * 
   * @param num The int to convert.
   * 
   * @return A six character string that represents the int.
   */
  public static String to6BitBinary(int num) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 6; i++) {
      sb.append(((num & 1) == 1) ? '1' : '0');
      num >>= 1;
    }

    return sb.reverse().toString();
  }
}
