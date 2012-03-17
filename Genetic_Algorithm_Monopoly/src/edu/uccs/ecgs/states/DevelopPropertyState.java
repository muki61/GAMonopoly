package cs571.mukhar.states;

import cs571.mukhar.Actions;

public class DevelopPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case DEVELOP_DECISION_EVENT:
      player.processDevelopHouseEvent();

      player.nextAction = Actions.MAKE_MORTGAGE_DECISION;
      payoffMortgageState.enter();
      return payoffMortgageState;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
