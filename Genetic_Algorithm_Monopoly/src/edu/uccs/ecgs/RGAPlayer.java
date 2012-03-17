package cs571.mukhar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//player that uses a genome with continuous values 
public class RGAPlayer extends CGPlayer {

  public RGAPlayer(int index) {
    super(index);
    jailLength = 64;

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
    jailLength = 64;

    chrNoOwners = new double[lotLength];
    chrPlayerOwns = new double[lotLength];
    chrOpponentOwns = new double[lotLength];
    chrTwoOpponentOwns = new double[lotLength];
    chrJail = new double[jailLength][jailLength];

    fitnessScore = dis.readInt();

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
    jailLength = 64;

    this.chrNoOwners = chrNoOwners;
    this.chrPlayerOwns = chrPlayerOwns;
    this.chrOpponentOwns = chrOpponentOwns;
    this.chrTwoOpponentOwns = chrTwoOpponentOwns;
    this.chrJail = chrJail;
  }

  // Predicate asking whether or not player wishes to pay bail
  // True --> player wishes to pay bail
  // False --> player wishes to attempt to roll doubles
  @Override
  public boolean payBailP() {
    if (!hasAtLeastCash(50) && !canRaiseCash(50) && !hasGetOutOfJailCard()) {
      return false;
    }

    PropertyFactory pf = PropertyFactory.getPropertyFactory();
    int idx1 = pf.getIndexFromProperties(Edges.WEST, this);
    int idx2 = pf.getIndexFromProperties(Edges.NORTH, this);

    return r.nextDouble() < chrJail[idx1][idx2];
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {
    out.writeChars("RGA");
    super.dumpGenome(out);
  }
}
