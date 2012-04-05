package edu.uccs.ecgs.ga;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;


public class SGAPlayer extends AbstractPlayer {

  private BitSet chrNoOwners;
  private BitSet chrPlayerOwns;
  private BitSet chrOpponentOwns;
  private BitSet chrTwoOpponentOwns;
  private BitSet chrJail;

  private static final int numLots = 40;
  private static final int numJailCombos = 16;
  private static final int numBitsPerGene = 6;
  private static final int chromoLength = numLots * numBitsPerGene;
  private static final int jChromoLength = numJailCombos * numBitsPerGene;
  private static final int maxGeneVal = 64; // 2^numBitsPerGene

  public SGAPlayer(int index) {
    super(index);
    chrNoOwners = new BitSet(chromoLength);
    chrPlayerOwns = new BitSet(chromoLength);
    chrOpponentOwns = new BitSet(chromoLength);
    chrTwoOpponentOwns = new BitSet(chromoLength);
    chrJail = new BitSet(jChromoLength);

    for (int i = 0; i < numLots; i++) {
      for (int j = 0; j < numBitsPerGene; j++) {
        // for non ownable properties (Go, Chance, etc.)
        // skip the bits
        switch (i) {
        case 0:  // Go
        case 2:  // Community Chest
        case 4:  // Tax
        case 7:  // Chance
        case 10: // Jail
        case 17: // Community Chest
        case 20: // Free Parking
        case 22: // Chance
        case 30: // Go To Jail
        case 33: // Community Chest
        case 36: // Chance
        case 38: // Tax
          continue;
        }

        if (r.nextInt(2) == 1) {
          chrNoOwners.set(i * numBitsPerGene + j);
        }
        if (r.nextInt(2) == 1) {
          chrPlayerOwns.set(i * numBitsPerGene + j);
        }
        if (r.nextInt(2) == 1) {
          chrOpponentOwns.set(i * numBitsPerGene + j);
        }
        if (r.nextInt(2) == 1) {
          chrTwoOpponentOwns.set(i * numBitsPerGene + j);
        }
      }
    }

    for (int i = 0; i < jChromoLength; i++) {
      if (r.nextInt(2) == 1) {
        chrJail.set(i);
      }
    }
  }

  public SGAPlayer(int index, DataInputStream dis) {
    super(index);

    try {
      fitnessScore = dis.readInt();
    } catch (IOException e) {
      e.printStackTrace();
    }

    chrNoOwners = new BitSet(chromoLength);
    chrPlayerOwns = new BitSet(chromoLength);
    chrOpponentOwns = new BitSet(chromoLength);
    chrTwoOpponentOwns = new BitSet(chromoLength);
    chrJail = new BitSet(jChromoLength);

    readBitSet(chrNoOwners, dis);
    readBitSet(chrPlayerOwns, dis);
    readBitSet(chrOpponentOwns, dis);
    readBitSet(chrTwoOpponentOwns,dis);
    readBitSet(chrJail, dis);
  }

  public SGAPlayer(int index, BitSet chrNoOwners, BitSet chrPlayerOwns,
      BitSet chrOpponentOwns, BitSet chrTwoOpponentOwns,
      BitSet chrJail) 
  {
    super(index);
    this.chrNoOwners = chrNoOwners;
    this.chrPlayerOwns = chrPlayerOwns;
    this.chrOpponentOwns = chrOpponentOwns;
    this.chrTwoOpponentOwns = chrTwoOpponentOwns;
    this.chrJail = chrJail;
  }

  private void readBitSet(BitSet bs, DataInputStream dis) {
    try {
      int i = dis.readInt();
      while (i != -1) {
        bs.set(i);
        i = dis.readInt();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean payBailP() {
    if (!hasAtLeastCash(50) && !canRaiseCash(50) && !hasGetOutOfJailCard()) {
      return false;
    }

    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    int idx1 = pf.getIndexFromProperties(Edges.WEST, this);
    int idx2 = pf.getIndexFromProperties(Edges.NORTH, this);

    idx1 = BitSetUtility.convertToTwoBits(idx1);
    idx2 = BitSetUtility.convertToTwoBits(idx2);
    
    int idx = idx1 * 4 + idx2;

    return   r.nextInt(maxGeneVal) 
           < BitSetUtility.sixBits2Int(chrJail, idx);
  }

  @Override
  public boolean buyProperty() {
    return buyProperty (location);
  }
  
  @Override
  public boolean buyProperty(Location aLocation) {
    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    GroupOwners flag = pf.getOwnerInformationForGroup(aLocation, this);

    switch (flag) {
    case NONE:
      return r.nextInt(maxGeneVal) < BitSetUtility.sixBits2Int(chrNoOwners,
                                                               aLocation.index);
    case SELF:
      return r.nextInt(maxGeneVal) < BitSetUtility.sixBits2Int(chrPlayerOwns,
                                                               aLocation.index);
    case ONE_OPPONENT:
      return r.nextInt(maxGeneVal) < BitSetUtility.sixBits2Int(chrOpponentOwns,
                                                               aLocation.index);
    case TWO_OPPONENTS:
      return r.nextInt(maxGeneVal) < BitSetUtility.sixBits2Int(chrTwoOpponentOwns, 
                                                               aLocation.index);
    }

    return false;
  }

  @Override
  public void dumpGenome(DataOutputStream out) throws IOException {
    out.writeChars("SGA");
    out.writeInt(fitnessScore);
    dumpBitSet(out, chrNoOwners);
    dumpBitSet(out, chrPlayerOwns);
    dumpBitSet(out, chrOpponentOwns);
    dumpBitSet(out, chrTwoOpponentOwns);
    dumpBitSet(out, chrJail);
  }

  private void dumpBitSet(DataOutputStream out, BitSet bs) throws IOException {
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      out.writeInt(i);
    }
    out.writeInt(-1);
  }

  @Override
  public void printGenome() {
    game.logFinest("Chromosome chrNoOwners (property group has no owners");
    printChromo(chrNoOwners, numLots);
    game.logFinest("Chromosome chrPlayerOwns (player owns property in property group");
    printChromo(chrPlayerOwns, numLots);
    game.logFinest("Chromosome chrOpponentOwns (opponent owns property in property group");
    printChromo(chrOpponentOwns, numLots);
    game.logFinest("Chromosome chrTwoOpponentOwns (two opponents own property in property group");
    printChromo(chrTwoOpponentOwns, numLots);
    game.logFinest("Chromosome chrJail");
    printChromo(chrJail, numJailCombos);
  }

  private void printChromo(BitSet bs, int numGenes) {
    StringBuilder b = new StringBuilder(numGenes * 7);

    for (int i = 0; i < numGenes; i++) {
      int r = BitSetUtility.sixBits2Int(bs, i);
      b.append(BitSetUtility.to6BitBinary(r)).append(" ");
    }

    game.logFinest(b.toString());
  }

  @Override
  public AbstractPlayer[] createChildren(AbstractPlayer parent, int index) {
    AbstractPlayer[] result = new AbstractPlayer[2];

    SGAPlayer parent2 = (SGAPlayer) parent;
    
    int crossPoint = 0;
    
    crossPoint = r.nextInt(chrNoOwners.size());
    
    BitSet c111 = (BitSet) this.chrNoOwners.clone();
    c111.clear(0, crossPoint);
    BitSet c211 = (BitSet) parent2.chrNoOwners.clone();
    c211.clear(crossPoint);
    c111.xor(c211);

    BitSet c112 = (BitSet) this.chrNoOwners.clone();
    c112.clear(crossPoint);
    BitSet c212 = (BitSet) parent2.chrNoOwners.clone();
    c212.clear(0, crossPoint);
    c112.xor(c212);
    
    BitSet c121 = (BitSet) this.chrPlayerOwns.clone();
    c121.clear(0, crossPoint);
    BitSet c221 = (BitSet) parent2.chrPlayerOwns.clone();
    c221.clear(crossPoint);
    c121.xor(c221);

    BitSet c122 = (BitSet) this.chrPlayerOwns.clone();
    c122.clear(crossPoint);
    BitSet c222 = (BitSet) parent2.chrPlayerOwns.clone();
    c222.clear(0, crossPoint);
    c122.xor(c222);

    BitSet c131 = (BitSet) this.chrOpponentOwns.clone();
    c131.clear(0, crossPoint);
    BitSet c231 = (BitSet) parent2.chrOpponentOwns.clone();
    c231.clear(crossPoint);
    c131.xor(c231);

    BitSet c132 = (BitSet) this.chrOpponentOwns.clone();
    c132.clear(crossPoint);
    BitSet c232 = (BitSet) parent2.chrOpponentOwns.clone();
    c232.clear(0, crossPoint);
    c132.xor(c232);

    BitSet c141 = (BitSet) this.chrTwoOpponentOwns.clone();
    c141.clear(0, crossPoint);
    BitSet c241 = (BitSet) parent2.chrTwoOpponentOwns.clone();
    c241.clear(crossPoint);
    c141.xor(c241);

    BitSet c142 = (BitSet) this.chrTwoOpponentOwns.clone();
    c142.clear(crossPoint);
    BitSet c242 = (BitSet) parent2.chrTwoOpponentOwns.clone();
    c242.clear(0, crossPoint);
    c142.xor(c242);

    BitSet c151 = (BitSet) this.chrJail.clone();
    c151.clear(0, crossPoint);
    BitSet c251 = (BitSet) parent2.chrJail.clone();
    c251.clear(crossPoint);
    c151.xor(c251);

    BitSet c152 = (BitSet) this.chrJail.clone();
    c152.clear(crossPoint);
    BitSet c252 = (BitSet) parent2.chrJail.clone();
    c252.clear(0, crossPoint);
    c152.xor(c252);

    result[0] = new SGAPlayer(index, c111, c121, c131, c141, c151);
    ++index;
    result[1] = new SGAPlayer(index, c112, c122, c132, c142, c152);

    return result; 
  }

  @Override
  public AbstractPlayer copyAndMutate() {
    SGAPlayer child = null;
    try {
      child = (SGAPlayer) clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    } 
    
    for (int i = 0; i < numLots; i++) {
      for (int j = 0; j < numBitsPerGene; j++) {
        // for non ownable properties (Go, Chance, etc.)
        // skip the bits
        switch (i) {
        case 0:  // Go
        case 2:  // Community Chest
        case 4:  // Tax
        case 7:  // Chance
        case 10: // Jail
        case 17: // Community Chest
        case 20: // Free Parking
        case 22: // Chance
        case 30: // Go To Jail
        case 33: // Community Chest
        case 36: // Chance
        case 38: // Tax
          continue;
        }

        if (r.nextDouble() < Main.mutationRate) {
          child.chrNoOwners.flip(i * numBitsPerGene + j);
        }
        if (r.nextDouble() < Main.mutationRate) {
          child.chrPlayerOwns.flip(i * numBitsPerGene + j);
        }
        if (r.nextDouble() < Main.mutationRate) {
          child.chrOpponentOwns.flip(i * numBitsPerGene + j);
        }
        if (r.nextDouble() < Main.mutationRate) {
          child.chrTwoOpponentOwns.flip(i * numBitsPerGene + j);
        }
      }
    }

    for (int i = 0; i < jChromoLength; i++) {
      if (r.nextDouble() < Main.mutationRate) {
        child.chrJail.flip(i);
      }
    }
    
    return child;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    BitSet chrNoOwners1 = (BitSet) chrNoOwners.clone();
    BitSet chrPlayerOwns1 = (BitSet) chrPlayerOwns.clone();
    BitSet chrOpponentOwns1 = (BitSet) chrOpponentOwns.clone();
    BitSet chrTwoOpponentOwns1 = (BitSet) chrTwoOpponentOwns.clone();
    BitSet chrJail1 = (BitSet) chrJail.clone();

    SGAPlayer child = new SGAPlayer(0, chrNoOwners1, chrPlayerOwns1, 
        chrOpponentOwns1, chrTwoOpponentOwns1, chrJail1);
    
    return child;
  }
}
