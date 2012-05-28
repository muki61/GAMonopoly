package edu.uccs.ecgs.ga;

import java.util.Vector;

/**
 * Compute fitness based on the finish order for the player. For each first
 * place finish in a game, the player is awarded a score that is the number of
 * players in a game minus 1 (e.g. if there are 4 players in a game, the score
 * for a win is 3). Given the number of players n, then 2nd place gets n-2
 * points; 3rd place gets n-3 points; etc. Last place in a game gets 0 points.
 */
public class FinishOrderFitnessEvaluator extends AbstractFitnessEvaluator {
  @Override
  public void evaluate(Vector<AbstractPlayer> players)
  {
    for (AbstractPlayer player : players) {
      // Compute the score for the most recent game
      int gameScore = Main.numPlayers - player.getFinishOrder();

      // Store the new fitness value
      player.addToFitness(gameScore);
    }
  }

  @Override
  public String getDirName()
  {
    return "finish_order";
  }
}
