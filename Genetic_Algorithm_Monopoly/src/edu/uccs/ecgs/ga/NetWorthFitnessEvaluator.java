package edu.uccs.ecgs.ga;

import java.util.Vector;

/**
 * Compute fitness based on the net worth a player has. The raw score is the
 * ratio of the player's net worth to the total net worth of all the players in
 * a game. The raw score is then mapped and rounded to a value between 0 and
 * 300.
 */
public class NetWorthFitnessEvaluator extends AbstractFitnessEvaluator {
  @Override
  public void evaluate(Vector<AbstractPlayer> players)
  {
    for (AbstractPlayer player : players) {
      // Compute the score for the most recent game
      // game score is ratio of net worth to total net worth in game
      // and then mapped and rounded to a scale from 0 to 300
      float gameScore = 300f * ((float) player.getTotalWorth())
          / ((float) player.getGameNetWorth());
      
      assert gameScore >= 0.0f;
      
      int roundedGameScore = Math.round(gameScore);
      assert roundedGameScore >= 0;

      // Store the new fitness value
      player.addToFitness(roundedGameScore);
    }
  }

  @Override
  public String getDirName()
  {
    return "net_worth";
  }
}
