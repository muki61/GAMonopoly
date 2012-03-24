package edu.uccs.ecgs.ga;

import java.util.Random;
import java.util.Vector;

public class Cards {

  Vector<Chance> chanceCards = new Vector<Chance>(16);
  Vector<CommunityChest> ccCards = new Vector<CommunityChest>(16);

  Random random = new Random();

  private Cards() {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
//    System.out.println("Cards seed         : " + seed);
    random.setSeed(seed);

    shuffleChance();
    shuffleCC();
  }

  public static Cards getCards() {
    return new Cards();
  }
  
  public Chance getNextChanceCard() {
    Chance c = chanceCards.remove(0);
    if (c != Chance.GET_OUT_OF_JAIL) {
      chanceCards.add(c);
    } else {
      assert !chanceCards.contains(Chance.GET_OUT_OF_JAIL) ;
    }
    return c;
  }

  public CommunityChest getNextCommunityChestCard() {
    CommunityChest c = ccCards.remove(0);
    if (c != CommunityChest.GET_OUT_OF_JAIL) {
      ccCards.add(c);
    } else {
      assert !ccCards.contains(CommunityChest.GET_OUT_OF_JAIL) ;
    }
    return c;
  }

  public void returnChanceGetOutOfJail() {
    assert !chanceCards.contains(Chance.GET_OUT_OF_JAIL) : "Duplicate Chance Get Out Of Jail Card";
    chanceCards.add(Chance.GET_OUT_OF_JAIL);
  }

  public void returnCCGetOutOfJail() {
    assert !ccCards.contains(CommunityChest.GET_OUT_OF_JAIL) : "Duplicate Community Chest Get Out Of Jail Card";
    ccCards.add(CommunityChest.GET_OUT_OF_JAIL);
  }

  private void shuffleChance() {
    Vector<Chance> temp = new Vector<Chance>(16);
    for (Chance cTemp : Chance.values()) {
      temp.add(cTemp);
    }

    while (!temp.isEmpty()) {
      chanceCards.add(temp.remove(random.nextInt(temp.size())));
    }
  }

  private void shuffleCC() {
    Vector<CommunityChest> temp = new Vector<CommunityChest>(16);
    for (CommunityChest cTemp : CommunityChest.values()) {
      temp.add(cTemp);
    }

    while (!temp.isEmpty()) {
      ccCards.add(temp.remove(random.nextInt(temp.size())));
    }
  }
}