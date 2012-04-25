package edu.uccs.ecgs.ga;

public interface IFitnessEvaluator {
  public int getMaxPointsPerGame();
  public void evaluate(AbstractPlayer player);
}
