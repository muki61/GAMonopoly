package edu.uccs.ecgs.ga;

import java.util.Random;

public class Dice {
  Random dice = new Random();
  private int[] result = new int[2];
  private boolean doubles;
  private int currentRoll = 0;
  
  private Dice() {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    System.out.println("Dice seed          : " + seed);
    dice.setSeed(seed);
  }

  public static Dice getDice() {
    return new Dice();
  }

  public synchronized int[] roll() {
    doubles = false;

    result[0] = dice.nextInt(6) + 1;
    result[1] = dice.nextInt(6) + 1;

    currentRoll = result[0] + result[1];

    if (result[0] == result[1]) {
      doubles = true;
    }
    return result;
  }

  public int getLastRoll() {
    return currentRoll;
  }

  public boolean rolledDoubles() {
    return doubles;
  }
}
