package edu.uccs.ecgs.ga;

public enum FitEvalTypes {
  FINISH_ORDER("Finish Order"),
  NET_WORTH("Net Worth"),
  NUM_MONOPOLIES("Number of Monopolies"), 
  NUM_PROPERTIES("Number of Properties"),
  NUM_WINS("Number of Wins"),
  TOURNAMENT("Tournament");

  private String name;

  private FitEvalTypes(String name) {
    this.name = name;
  }

  public IFitnessEvaluator get() {
    switch (this) {
    case FINISH_ORDER: return new FinishOrderFitnessEvaluator();
    case NET_WORTH: return new NetWorthFitnessEvaluator();
    case NUM_MONOPOLIES: return new NumMonopoliesFitnessEvaluator();
    case NUM_PROPERTIES: return new NumPropertiesFitnessEvaluator();
    case NUM_WINS: return new NumWinsFitnessEvaluator();
    case TOURNAMENT: return new TournamentFitnessEvaluator();
    default: return null;
    }
  }

  public String toString() {
    return name;
  }
}
