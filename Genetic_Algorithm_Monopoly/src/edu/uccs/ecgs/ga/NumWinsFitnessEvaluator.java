package edu.uccs.ecgs.ga;

import java.util.HashMap;

public class NumWinsFitnessEvaluator extends AbstractFitnessEvaluator {

  private HashMap<AbstractPlayer, Integer> scores = new HashMap<AbstractPlayer, Integer>();

  @Override
  public void evaluate(AbstractPlayer player) {
    Integer score = scores.get(player);
    Integer newScore = new Integer(player.getFinishOrder());

    if (score != null) {
      newScore = newScore.intValue() + score.intValue();
    }

    scores.put(player, newScore);
    player.setFitness(newScore.intValue());
  }

}
