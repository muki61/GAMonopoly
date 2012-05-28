package edu.uccs.ecgs.ga;

/**
 * Compute fitness based on the net worth a player has. 
 */
public class NetWorthFitnessEvaluator extends AbstractFitnessEvaluator {
  @Override
  public void evaluate(AbstractPlayer player) {
    // Compute the score for the most recent game
    int gameScore = player.getTotalWorth();

    // Store the new fitness value
    player.addToFitness(gameScore);
  }
}
