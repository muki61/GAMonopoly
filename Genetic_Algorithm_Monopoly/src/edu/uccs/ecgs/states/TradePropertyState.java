package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Monopoly;

public class TradePropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent (Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case TRADE_DECISION_EVENT:
      player.nextAction = Actions.DONE;
      inactiveState.enter();
      return inactiveState;
      
    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
