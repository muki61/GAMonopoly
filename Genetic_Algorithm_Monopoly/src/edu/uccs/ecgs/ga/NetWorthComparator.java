package edu.uccs.ecgs.ga;

import java.util.Comparator;

public class NetWorthComparator implements Comparator<AbstractPlayer> {
  private static NetWorthComparator _ref = new NetWorthComparator();
  private NetWorthComparator() {}

  public static NetWorthComparator get() {
    return _ref;
  }

  /**
   * Compare two players based on their various factors. Start by comparing net
   * worth. If the net worths are equal, compare the number of monopolies. If
   * both players have the same number of monopolies, then compare the number of
   * properties. If they have the same number of properties, then compare the
   * initial generation. If there is still a tie, then compare the player ids.
   * 
   * @param o1
   *          First player to compare
   * @param o2
   *          Second player to compare
   */
  @Override
  public int compare(AbstractPlayer o1, AbstractPlayer o2)
  {
    if (o1.bankrupt() && o2.bankrupt()) {
      return o1.getBankruptIndex() < o2.getBankruptIndex() ? -1 : 1;
    }

    if (o1.getTotalWorth() != o2.getTotalWorth()) {
      return o1.getTotalWorth() < o2.getTotalWorth() ? -1 : 1;
    }

    if (o1.getNumMonopolies() != o2.getNumMonopolies()) {
      return o1.getNumMonopolies() < o2.getNumMonopolies() ? -1 : 1;
    }

    if (o1.getNumProperties() != o2.getNumProperties()) {
      return o1.getNumProperties() < o2.getNumProperties() ? -1 : 1;
    }

    if (o1.initialGeneration != o2.initialGeneration) {
      return o1.initialGeneration < o2.initialGeneration ? -1 : 1;
    }

    return o1.playerIndex < o2.playerIndex ? -1 : 1;
  }
}
