package edu.uccs.ecgs.ga;

import java.util.Vector;

public interface IFitnessEvaluator {
  public void evaluate(Vector<AbstractPlayer> players);
  public void normalize(Vector<AbstractPlayer> playerPool);
  public String getDirName();
}
