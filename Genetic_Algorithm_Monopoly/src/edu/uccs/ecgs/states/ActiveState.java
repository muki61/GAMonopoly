package edu.uccs.ecgs.states;

import edu.uccs.ecgs.AbstractPlayer;
import edu.uccs.ecgs.Monopoly;

public class ActiveState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());

    switch (event) {
    case ROLL_DICE_EVENT:
      // nextAction is set by rollDice method
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
