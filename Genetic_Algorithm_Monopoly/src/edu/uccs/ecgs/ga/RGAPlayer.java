package edu.uccs.ecgs.ga;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//player that uses a genome with continuous values 
public class RGAPlayer extends CGPlayer {

  // Jail Chromosome dimension
  private static final int JAIL_LENGTH = 64;

  public RGAPlayer(int index) {
    super(index);
    jailLength = JAIL_LENGTH;

    chrNoOwners = new double[lotLength];
    chrPlayerOwns = new double[lotLength];
    chrOpponentOwns = new double[lotLength];
    chrTwoOpponentOwns = new double[lotLength];
    chrJail = new double[jailLength][jailLength];

    for (int i = 0; i < chrNoOwners.length; i++) {
      chrNoOwners[i] = r.nextDouble();
      chrPlayerOwns[i] = r.nextDouble();
      chrOpponentOwns[i] = r.nextDouble();
      chrTwoOpponentOwns[i] = r.nextDouble();
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail[i][j] = r.nextDouble();
      }
    }
  }

  public RGAPlayer(int index, DataInputStream dis) throws IOException {
    super(index);
    jailLength = JAIL_LENGTH;

    chrNoOwners = new double[lotLength];
    chrPlayerOwns = new double[lotLength];
    chrOpponentOwns = new double[lotLength];
    chrTwoOpponentOwns = new double[lotLength];
    chrJail = new double[jailLength][jailLength];

    setFitness(dis.readInt());

    for (int i = 0; i < chrNoOwners.length; i++) {
      chrNoOwners[i] = dis.readDouble();
    }
    for (int i = 0; i < chrPlayerOwns.length; i++) {
      chrPlayerOwns[i] = dis.readDouble();
    }
    for (int i = 0; i < chrOpponentOwns.length; i++) {
      chrOpponentOwns[i] = dis.readDouble();
    }
    for (int i = 0; i < chrTwoOpponentOwns.length; i++) {
      chrTwoOpponentOwns[i] = dis.readDouble();
    }

    for (int i = 0; i < jailLength; i++) {
      for (int j = 0; j < jailLength; j++) {
        chrJail[i][j] = dis.readDouble();
      }
    }    
  }

  public RGAPlayer(int index, double[] chrNoOwners, double[] chrPlayerOwns,
      double[] chrOpponentOwns, double[] chrTwoOpponentOwns,
      double[][] chrJail) 
  {
    super(index);
    jailLength = JAIL_LENGTH;

    this.chrNoOwners = chrNoOwners;
    this.chrPlayerOwns = chrPlayerOwns;
    this.chrOpponentOwns = chrOpponentOwns;
    this.chrTwoOpponentOwns = chrTwoOpponentOwns;
    this.chrJail = chrJail;
  }

  /**
   * Predicate asking whether or not player wishes to pay bail 
   * 
   * True --> player wishes to pay bail 
   * 
   * False --> player wishes to attempt to roll doubles
   */
  @Override
  public boolean payBailP() {
    if (!hasAtLeastCash(50) && !canRaiseCash(50) && !hasGetOutOfJailCard()) {
      return false;
    }

    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    int idx1 = pf.getIndexFromProperties(Edges.WEST, this);
    int idx2 = pf.getIndexFromProperties(Edges.NORTH, this);

    return r.nextDouble() < chrJail[idx1][idx2];
  }

  /**
   * Dump the players genome out to a data file (which can be read later in the
   * case of restart or to analyze the player).
   */
  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {
    out.writeChars("RGA");
    super.dumpGenome(out);
  }
}
