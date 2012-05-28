package edu.uccs.ecgs.ga;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Represents a player with a real-valued genome, i.e. each gene in the 
 * chromosome is a real number not a binary string. Since all children
 * will have a 40 element array for the properties chromosomes, the primary
 * difference in subclasses will be the length of the jail array.
 */
public abstract class CGPlayer extends AbstractPlayer {
  protected int jailLength = 0;       //num of entries in chrJailArray
  protected final int lotLength = 40; //num of entries in any lot array
  
  /**
   * Chromosome used when no players own the property in the group where the
   * player is currently located
   */
  double[] chrNoOwners;

  /**
   * Chromosome used when the player owns one or two properties in the group
   * where the player is currently located, and no other player owns properties
   * in that group
   */
  double[] chrPlayerOwns;

  /**
   * Chromosome used when at least one opposing player owns property in the
   * group where the player is currently located
   */
  double[] chrOpponentOwns;

  /**
   * Chromosome used when at two or more opposing players own property in the
   * group where the player is currently located
   */
  double[] chrTwoOpponentOwns;

  /**
   * Chromosome to determine when to pay to get out of jail
   * 
   * Use the properties on East to create a number
   * 
   * Use the properties on North to create a number
   * 
   * Use those two numbers as an index into the array
   * 
   * Assume that each time the player must choose, this choice is determined
   * independently of the previous choice. Thus, based on chance, the player
   * could make different choices for the same index on different turns
   */
  public double[][] chrJail;

  // TODO Chromosome to determine when and how to trade properties?
  // Or use some other technique? For now, we ignore trading.

  public CGPlayer(int index) {
    super(index);
  }

  /**
   * If player wants to buy current location --> return true Otherwise -->
   * return false.
   * 
   * Player will buy property if random double is less than chromosome value; in
   * other words, a higher chromosome value means a higher likelihood of buying
   * a property
   */
  @Override
  public boolean buyProperty() {
    return buyProperty (location);
  }  

  /**
   * If player wants to buy the given location --> return true<br/>
   * Otherwise --> return false.<br/>
   * <br/>
   * 
   * Player will buy property if random double is less than chromosome value; in
   * other words, a higher chromosome value means a higher likelihood of buying
   * a property.
   */
  @Override
  public boolean buyProperty(Location aLocation) {
    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    GroupOwners flag = pf.getOwnerInformationForGroup(aLocation, this);

    switch (flag) {
    case NONE:
      return r.nextDouble() < chrNoOwners[aLocation.index];
    case SELF:
      return r.nextDouble() < chrPlayerOwns[aLocation.index];
    case ONE_OPPONENT:
      return r.nextDouble() < chrOpponentOwns[aLocation.index];
    case TWO_OPPONENTS:
      return r.nextDouble() < chrTwoOpponentOwns[aLocation.index];
    }

    return false;
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {
    out.writeInt(getFitness());

    for (double d : chrNoOwners) {
      out.writeDouble(d);
    }

    for (double d : chrPlayerOwns) {
      out.writeDouble(d);
    }

    for (double d : chrOpponentOwns) {
      out.writeDouble(d);
    }

    for (double d : chrTwoOpponentOwns) {
      out.writeDouble(d);
    }

    for (double[] a : chrJail) {
      for (double d: a) {
        out.writeDouble(d);
      }
    }
  }

  @Override
  public void printGenome() {
    game.logFinest("Chromosome chrNoOwners (property group has no owners");
    printChromo(chrNoOwners);
    game.logFinest("Chromosome chrPlayerOwns (player owns property in property group");
    printChromo(chrPlayerOwns);
    game.logFinest("Chromosome chrOpponentOwns (opponent owns property in property group");
    printChromo(chrOpponentOwns);
    game.logFinest("Chromosome chrTwoOpponentOwns (two opponents own property in property group");
    printChromo(chrTwoOpponentOwns);
    game.logFinest("Chromosome chrJail");
    for (double[] a : chrJail) {
      printChromo(a);
    }
  }
  
  private void printChromo(double[] c) {
    boolean prependComma = false;
    StringBuilder b = new StringBuilder();

    for (double d : c) {
      if (prependComma) {
        b.append(",").append(d);
      } else {
        b.append(d);
        prependComma = true;
      }
    }
  }

  @Override
  public AbstractPlayer[] createChildren(AbstractPlayer parent, int index) {
    CGPlayer[] result = new CGPlayer[2];
    CGPlayer parent1 = this;
    CGPlayer parent2 = (CGPlayer) parent;

    // get a beta value, uniformly distributed, 0 <= beta < 1
    double beta = r.nextDouble();

    double[] chrNoOwners1 = new double[lotLength];
    double[] chrPlayerOwns1 = new double[lotLength];
    double[] chrOpponentOwns1 = new double[lotLength];
    double[] chrTwoOpponentOwns1 = new double[lotLength];
    double[][] chrJail1 = new double[jailLength][jailLength];

    double[] chrNoOwners2 = new double[lotLength];
    double[] chrPlayerOwns2 = new double[lotLength];
    double[] chrOpponentOwns2 = new double[lotLength];
    double[] chrTwoOpponentOwns2 = new double[lotLength];
    double[][] chrJail2 = new double[jailLength][jailLength];

    for (int i = 0; i < lotLength; i++) {
      // p1 * beta + p2 * (1-beta)
      chrNoOwners1[i] = parent1.chrNoOwners[i] * beta  
                        + parent2.chrNoOwners[i] * (1.0 - beta);
      chrPlayerOwns1[i] = parent1.chrPlayerOwns[i] * beta
                          + parent2.chrPlayerOwns[i] * (1.0 - beta);
      chrOpponentOwns1[i] = parent1.chrOpponentOwns[i] * beta
                            + parent2.chrOpponentOwns[i] * (1.0 - beta);
      chrTwoOpponentOwns1[i] = parent1.chrTwoOpponentOwns[i] * beta
                               + parent2.chrTwoOpponentOwns[i] * (1.0 - beta);

      // p1 * (1-beta) + p2 * beta
      chrNoOwners2[i] = parent1.chrNoOwners[i] * (1.0 - beta)  
                        + parent2.chrNoOwners[i] * beta;
      chrPlayerOwns2[i] = parent1.chrPlayerOwns[i] * (1.0 - beta)
                          + parent2.chrPlayerOwns[i] * beta;
      chrOpponentOwns2[i] = parent1.chrOpponentOwns[i] * (1.0 - beta)
                            + parent2.chrOpponentOwns[i] * beta;
      chrTwoOpponentOwns2[i] = parent1.chrTwoOpponentOwns[i] * (1.0 - beta)
                               + parent2.chrTwoOpponentOwns[i] * beta;
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail1[i][j] = parent1.chrJail[i][j] * beta 
                         + parent2.chrJail[i][j] * (1.0 - beta);
        chrJail2[i][j] = parent1.chrJail[i][j] * (1.0 - beta)
                         + parent2.chrJail[i][j] * beta;
      }
    }

    result[0] = createChild(index, chrNoOwners1, chrPlayerOwns1,
        chrOpponentOwns1, chrTwoOpponentOwns1, chrJail1);

    ++index;

    result[1] = createChild(index, chrNoOwners2, chrPlayerOwns2,
        chrOpponentOwns2, chrTwoOpponentOwns2, chrJail2);

    return result; 
  }

  @Override
  public AbstractPlayer copyAndMutate() {
    CGPlayer child = null;
    try {
      child = (CGPlayer) clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < child.chrNoOwners.length; i++) {
      if (r.nextDouble() < Main.mutationRate) {
        child.chrNoOwners[i] += r.nextGaussian()/10.0;
        if (child.chrNoOwners[i] < 0.0) child.chrNoOwners[i] = 0.0;
        if (child.chrNoOwners[i] > 1.0) child.chrNoOwners[i] = 1.0;
      }
      if (r.nextDouble() < Main.mutationRate) {
        child.chrPlayerOwns[i] += r.nextGaussian()/10.0;
        if (child.chrPlayerOwns[i] < 0.0) child.chrPlayerOwns[i] = 0.0;
        if (child.chrPlayerOwns[i] > 1.0) child.chrPlayerOwns[i] = 1.0;
      }
      if (r.nextDouble() < Main.mutationRate) {
        child.chrOpponentOwns[i] += r.nextGaussian()/10.0;
        if (child.chrOpponentOwns[i] < 0.0) child.chrOpponentOwns[i] = 0.0;
        if (child.chrOpponentOwns[i] > 1.0) child.chrOpponentOwns[i] = 1.0;
      }
      if (r.nextDouble() < Main.mutationRate) {
        child.chrTwoOpponentOwns[i] += r.nextGaussian()/10.0;
        if (child.chrTwoOpponentOwns[i] < 0.0) child.chrTwoOpponentOwns[i] = 0.0;
        if (child.chrTwoOpponentOwns[i] > 1.0) child.chrTwoOpponentOwns[i] = 1.0;
      }
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        if (r.nextDouble() < Main.mutationRate) {
          child.chrJail[i][j] += r.nextGaussian()/10.0;
          if (child.chrJail[i][j] < 0.0) child.chrJail[i][j] = 0.0;
          if (child.chrJail[i][j] > 1.0) child.chrJail[i][j] = 1.0;
        }
      }
    }
    
    return child;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    double[] chrNoOwners1 = new double[lotLength];
    double[] chrPlayerOwns1 = new double[lotLength];
    double[] chrOpponentOwns1 = new double[lotLength];
    double[] chrTwoOpponentOwns1 = new double[lotLength];
    double[][] chrJail1 = new double[jailLength][jailLength];
    
    for (int i = 0; i < lotLength; i++) {
      chrNoOwners1[i] = this.chrNoOwners[i];
      chrPlayerOwns1[i] = this.chrPlayerOwns[i];
      chrOpponentOwns1[i] = this.chrOpponentOwns[i];
      chrTwoOpponentOwns1[i] = this.chrTwoOpponentOwns[i];
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail1[i][j] = this.chrJail[i][j];
      }
    }

    return createChild(0, chrNoOwners1, chrPlayerOwns1, chrOpponentOwns1,
        chrTwoOpponentOwns1, chrJail1);
  }

  /**
   * Create a new player instance from the passed in chromosomes.
   * 
   * @param index
   *          The index of the player
   * @param chrNoOwners
   *          The chromosome to use when no player owns a property in a group.
   * @param chrPlayerOwns
   *          The chromosome to use when this player owns a property in a group.
   * @param chrOpponentOwns
   *          The chromosome to use when another player owns a property in a
   *          group.
   * @param chrTwoOpponentOwns
   *          The chromosome to use when two other players own property in a
   *          group.
   * @param chrJail
   *          The chromosome to use when determining whether to pay bail to
   *          leave jail.
   * @return The new player instance.
   */
  private CGPlayer createChild(int index, 
      double[] chrNoOwners, double[] chrPlayerOwns,
      double[] chrOpponentOwns, double[] chrTwoOpponentOwns,
      double[][] chrJail) 
  {
    CGPlayer player = (CGPlayer) Main.chromoType.getPlayer(index);
    player.jailLength = this.jailLength;
    player.chrNoOwners = chrNoOwners;
    player.chrPlayerOwns = chrPlayerOwns;
    player.chrOpponentOwns = chrOpponentOwns;
    player.chrTwoOpponentOwns = chrTwoOpponentOwns;
    player.chrJail = chrJail;
    
    return player;
  }
}
