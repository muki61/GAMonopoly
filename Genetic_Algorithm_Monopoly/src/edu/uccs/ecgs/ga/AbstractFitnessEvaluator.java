package edu.uccs.ecgs.ga;

import java.util.Vector;

public abstract class AbstractFitnessEvaluator implements IFitnessEvaluator {
  @Override
  public void normalize(Vector<AbstractPlayer> playerPool)
  {
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
  
    // find the max score
    for (AbstractPlayer player : playerPool) {
      float score = (float) player.getFitness();
      max = (score > max ? score : max);
      min = (score < min ? score : min);
    }
  
    // normalize to range 1 to 100
    for (AbstractPlayer player : playerPool) {
      float score = (float) player.getFitness();
      float normalizedScore = (score - min) * 100.0f / (max - min);
      // TODO This means some player will get a score of 0 and will be dropped
      // from the population. Do we really want that?
      player.setFitness(Math.round(normalizedScore));
    }    
  }
}