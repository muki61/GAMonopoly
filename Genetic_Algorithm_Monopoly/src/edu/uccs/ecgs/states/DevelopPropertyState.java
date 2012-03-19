package edu.uccs.ecgs.states;

import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.Monopoly;

public class DevelopPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event, Monopoly game) {
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
