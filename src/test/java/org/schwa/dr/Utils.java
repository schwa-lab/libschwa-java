package org.schwa.dr;

import org.junit.Assert;


final class Utils {
  public static void assertArrayEquals(final byte[] e, final byte[] a) {
    boolean different = false;
    if (e.length != a.length)
      different = true;
    else
      for (int i = 0; i != e.length; i++)
        if (e[i] != a[i]) {
          different = true;
          break;
        }
    if (different)
      compareByteArrays(e, a);
    Assert.assertArrayEquals(e, a);
  }


  public static void compareByteArrays(final byte[] e, final byte[] a) {
    int ei = 0, ai = 0;
    System.out.print("          expected            |             actual            ||");
    System.out.println("           expected            |             actual            ");
    while (ei != e.length || ai != a.length) {
      final int ei_start = ei, ai_start = ai;
      for (int i = 0; i != 10; ++i) {
        if (ei != e.length)
          System.out.printf("%02x ", (int)(e[ei++] & 0xff));
        else
          System.out.print("   ");
      }
      System.out.print("| ");
      for (int i = 0; i != 10; ++i) {
        if (ai != a.length)
          System.out.printf("%02x ", (int)(a[ai++] & 0xff));
        else
          System.out.print("   ");
      }
      System.out.print("|| ");
      ei = ei_start;
      ai = ai_start;
      for (int i = 0; i != 10; ++i) {
        if (ei != e.length) {
          if (Character.isLetterOrDigit(e[ei]))
            System.out.printf("%2c ", (int)(e[ei++] & 0xff));
          else
            System.out.printf("%02x ", (int)(e[ei++] & 0xff));
        }
        else
          System.out.print("   ");
      }
      System.out.print("| ");
      for (int i = 0; i != 10; ++i) {
        if (ai != a.length) {
          if (Character.isLetterOrDigit(a[ai]))
            System.out.printf("%2c ", (int)(a[ai++] & 0xff));
          else
            System.out.printf("%02x ", (int)(a[ai++] & 0xff));
        }
        else
          System.out.print("   ");
      }
      System.out.println();
    }
    System.out.println();
  }
}
