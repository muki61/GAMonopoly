package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Monopoly;

public class RentPaidState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
