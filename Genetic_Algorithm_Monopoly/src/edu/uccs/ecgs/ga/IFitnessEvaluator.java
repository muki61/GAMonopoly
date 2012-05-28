package edu.uccs.ecgs.ga;

import java.util.Vector;

public interface IFitnessEvaluator {
  public void evaluate(AbstractPlayer player);
  public void normalize(Vector<AbstractPlayer> playerPool);
}
