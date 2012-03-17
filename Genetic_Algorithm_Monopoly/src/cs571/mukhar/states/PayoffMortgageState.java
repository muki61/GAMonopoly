package cs571.mukhar.states;

import cs571.mukhar.Actions;

public class PayoffMortgageState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    case MORTGAGE_DECISION_EVENT:
      player.processMortgagedLots();
      
      player.nextAction = Actions.MAKE_TRADE_DECISION;
      tradePropertyState.enter();
      return tradePropertyState;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
