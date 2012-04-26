package edu.uccs.ecgs.ga;

import java.util.HashMap;

/**
 * Compute fitness based on the finish order for the player. For each first
 * place finish in a game, the player is awarded a score that is the number of
 * players in a game minus 1 (e.g. if there are 4 players in a game, the score
 * for a win is 3). Given the number of players n, then 2nd place gets n-2 points;
 * 3rd place gets n-3 points; etc. Last place in a game gets 0 points.
 */
public class FinishOrderFitnessEvaluator implements IFitnessEvaluator {
  private HashMap<AbstractPlayer, Integer> scores = new HashMap<AbstractPlayer, Integer>();
  private static final int POINTS_PER_GAME = 6;

  @Override
  public void evaluate(AbstractPlayer player) {
    // Get the current fitness
    Integer fitness = scores.get(player);

    // Compute the score for the most recent game
    int gameScore = Main.numPlayers - player.getFinishOrder();

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
