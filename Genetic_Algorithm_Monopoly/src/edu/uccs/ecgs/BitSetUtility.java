package edu.uccs.ecgs;

import java.util.BitSet;

public class BitSetUtility {
  public static void main(String[] args) {
    BitSet bs = new BitSet();
    bs.set(0);
    bs.set(2);
    bs.set(4);
    int[] intArray = bits2Ints(bs);

    for (int i = 0; i < intArray.length; i++) {
      System.out.println(intArray[i]);
      System.out.println(toBinary(intArray[i]));
    }

    int r = sixBits2Int(bs, 0);
    System.out.println(""+r);
    System.out.println(to6BitBinary(r));
  }

  public static int[] bits2Ints(BitSet bs) {
    int[] temp = new int[bs.size() / 32];

    for (int i = 0; i < temp.length; i++)
      for (int j = 0; j < 32; j++)
        if (bs.get(i * 32 + j))
          temp[i] |= 1 << j;

    return temp;
  }

  public static int sixBits2Int(BitSet bs, int index) {
    int temp = 0;

    for (int j = 0; j < 6; j++)
      if (bs.get((index * 6) + j))
        temp |= 1 << j;

    return temp;
  }

  public static String toBinary(int num) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 32; i++) {
      sb.append(((num & 1) == 1) ? '1' : '0');
      num >>= 1;
    }

    return sb.reverse().toString();
  }

  public static String to6BitBinary(int num) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 6; i++) {
      sb.append(((num & 1) == 1) ? '1' : '0');
      num >>= 1;
    }

    return sb.reverse().toString();
  }

  public static int convertToTwoBits(int idx) {
    // idx is a 6 bit number nnn_nnn
    // convert each set of three bits to a 1 or 0
    // 111  -> 1
    // else -> 0
    int highBits = idx & 0x38; //0x111000
    int lowBits  = idx & 0x07; //0x000111
    
    int result = 0;

    if (highBits == 0x38) {
      result = 2; //0x1n - sets the second bit
    }
    if (lowBits == 0x07) {
      result = result + 1; //0xn1 - sets the first bit
    }
    
    return result;
  }
}
