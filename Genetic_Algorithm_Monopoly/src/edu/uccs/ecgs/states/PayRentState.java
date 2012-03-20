package edu.uccs.ecgs.states;

import edu.uccs.ecgs.AbstractPlayer;
import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.BankruptcyException;
import edu.uccs.ecgs.Location;
import edu.uccs.ecgs.Monopoly;

public class PayRentState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {

    case PAY_RENT_EVENT:
      Location location = player.getCurrentLocation();

      if (location.owner != player) {
        int amount = location.getRent();
        assert amount >= 0 : "Invalid rent: " + location.name + "; rent: "
            + amount;
        if (amount > 0) {
          try {
            game.payRent(player, location.owner, amount);
          } catch (BankruptcyException e) {
            //e.printStackTrace();
            game.processBankruptcy(player, location.owner);
            player.nextAction = Actions.DONE;
            return inactiveState;
          }
        }
      }

      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return activeState;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        return developPropertyState;
      }

    case ROLL_DICE_EVENT:
      rollDice(game, player);
      if (player.nextAction == Actions.MAKE_BUILD_DECISION) {
        developPropertyState.enter();
        return developPropertyState;
      } else if (player.nextAction == Actions.ROLL_DICE) {
        return activeState;
      } else {
        atNewLocationState.enter();
        return atNewLocationState;
      }

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
