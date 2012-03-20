package edu.uccs.ecgs.states;

import edu.uccs.ecgs.AbstractPlayer;
import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.Monopoly;

public class PayoffMortgageState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
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
