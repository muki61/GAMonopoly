package edu.uccs.ecgs.ga;

import java.util.HashMap;

/**
 * Compute fitness based on the net worth a player has. 
 */
public class NetWorthFitnessEvaluator implements IFitnessEvaluator {
  private HashMap<AbstractPlayer, Integer> scores = new HashMap<AbstractPlayer, Integer>();
  private static int POINTS_PER_GAME = 0;

  @Override
  public void evaluate(AbstractPlayer player) {
    // Get the current fitness
    Integer fitness = scores.get(player);

    // Compute the score for the most recent game
    int gameScore = player.getTotalWorth() / 100;

    // Add gameScore to fitness
    if (fitness != null) {
      fitness = gameScore + fitness;
    } else {
      fitness = gameScore;
    }

    // Store the new fitness value
    scores.put(player, fitness);
    player.setFitness(fitness);
  }

  @Override
  public int getMaxPointsPerGame() {
    return POINTS_PER_GAME;
  }
}
