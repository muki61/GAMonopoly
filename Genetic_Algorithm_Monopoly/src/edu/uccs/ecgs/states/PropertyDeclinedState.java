package edu.uccs.ecgs.states;

import edu.uccs.ecgs.AbstractPlayer;
import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.Monopoly;

public class PropertyDeclinedState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case DECLINE_PROPERTY_EVENT:
      player.nextAction = Actions.AUCTION_BID;
      auctionState.enter();
      return auctionState;

    case LOST_AUCTION_EVENT:
      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return this;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
