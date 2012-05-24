package edu.uccs.ecgs.ga;

import java.util.HashMap;

/**
 * Compute fitness based on the number of wins a player has. For each first
 * place finish in a game, the player is awarded a score that is the number of
 * players in a game minus 1 (e.g. if there are 4 players in a game, the score
 * for a win is 3). The player gets 0 points for any other game result (second
 * through last place).
 */
public class NumWinsFitnessEvaluator implements IFitnessEvaluator {
  private HashMap<AbstractPlayer, Integer> scores = new HashMap<AbstractPlayer, Integer>();
  private static final int FIRST_PLACE = 1;
  private static final int MAX_SCORE = Main.numPlayers - 1;
  private static final int MIN_SCORE = 0;
  private static final int POINTS_PER_GAME = Main.numPlayers - 1;

  @Override
  public void evaluate(AbstractPlayer player) {
    // Get the current fitness
    Integer fitness = scores.get(player);

    // Compute the score for the most recent game
    int gameScore = player.getFinishOrder() == FIRST_PLACE ? MAX_SCORE : MIN_SCORE;

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
