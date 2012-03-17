package edu.uccs.ecgs;

import java.util.Random;
import java.util.logging.Logger;

public class Dice {
  Random dice = new Random();
  private int[] result = new int[2];
  private boolean doubles;
  private int currentRoll = 0;
  
  private static Dice _dice = new Dice();

  static Logger logger = Logger.getLogger("edu.uccs.ecgs");

  private Dice() {
    long seed = 1241722060907L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    System.out.println("Dice seed          : " + seed);
    dice.setSeed(seed);
  }

  public static Dice getDice() {
    return _dice;
  }

  public int[] roll() {
    doubles = false;

    result[0] = dice.nextInt(6) + 1;
    result[1] = dice.nextInt(6) + 1;

    currentRoll = result[0] + result[1];

    logger.info("Dice 1: " + result[0]);
    logger.info("Dice 2: " + result[1]);

    if (result[0] == result[1]) {
      logger.info("Doubles!!");
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
