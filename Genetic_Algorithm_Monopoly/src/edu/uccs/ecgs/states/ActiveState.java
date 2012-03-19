package edu.uccs.ecgs.states;

import edu.uccs.ecgs.Monopoly;

public class ActiveState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event, Monopoly game) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());

    switch (event) {
    case ROLL_DICE_EVENT:
      // nextAction is set by rollDice method
      rollDice();
      return determineNextState();

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
