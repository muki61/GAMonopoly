package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Monopoly;

public class InactiveState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case PLAYER_ACTIVATED_EVENT:

      if (player.inJail()) {
        if (player.payBailP()) {
          player.nextAction = Actions.PAY_BAIL;
        } else {
          player.nextAction = Actions.ROLL_DICE;
        }
        inJailState.enter();
        return inJailState;
      } else {
        player.nextAction = Actions.ROLL_DICE;
        activeState.enter();
        return activeState;
      }

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
