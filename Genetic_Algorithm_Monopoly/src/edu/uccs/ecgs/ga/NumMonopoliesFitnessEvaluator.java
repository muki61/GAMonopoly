package edu.uccs.ecgs.ga;

import java.util.HashMap;

/**
 * Compute fitness based on the number of monopolies a player owns. This
 * evaluator is based solely on the number of monopolies owned, and does not
 * include any information about monopolies which may be more highly valued. 
 * <br> TODO Maybe have an evaluator that also include property value or strength.
 */
public class NumMonopoliesFitnessEvaluator implements IFitnessEvaluator {
  private HashMap<AbstractPlayer, Integer> scores = new HashMap<AbstractPlayer, Integer>();
  private static final int POINTS_PER_GAME = 8; // this is a theoretical max

  @Override
  public void evaluate(AbstractPlayer player) {
    // Get the current fitness
    Integer fitness = scores.get(player);

    // Compute the score for the most recent game
    int gameScore = player.getNumMonopolies();

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
