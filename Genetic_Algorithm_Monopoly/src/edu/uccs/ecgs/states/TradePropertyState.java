package cs571.mukhar.states;

import cs571.mukhar.Actions;

public class TradePropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent (Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
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
