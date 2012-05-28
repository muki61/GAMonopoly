package edu.uccs.ecgs.ga;

import java.util.Vector;

/**
 * Compute fitness based on the net worth a player has. The raw score is the
 * ratio of the player's net worth to the total net worth of all the players
 * in a game.
 */
public class NetWorthFitnessEvaluator extends AbstractFitnessEvaluator {
  @Override
  public void evaluate(Vector<AbstractPlayer> players)
  {
    for (AbstractPlayer player : players) {
      // Compute the score for the most recent game
      // game score is ration of net worth to total net worth in game
      // and then mapped to a scale from 0 to 100
      float gameScore = 100f * ((float) player.getTotalWorth())
          / ((float) player.getGameNetWorth());
      
      // Store the new fitness value
      player.addToFitness(Math.round(gameScore));
    }
  }

  @Override
  public String getDirName()
  {
    return "net_worth";
  }
}
