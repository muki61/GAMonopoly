package edu.uccs.ecgs.states;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;

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
        if (location.isMortgaged()) {
            game.logger.info("Lot is mortgaged, rent: 0");
        } else {
          int amount = location.getRent();
          assert amount >= 0 : "Invalid rent: " + location.name + "; rent: "
              + amount;

          game.logger.info("Rent for " + location.toString() + " with "
              + location.getNumHouses() + " houses: " + amount);
          
          if (amount > 0) {
            try {
              game.payRent(player, location.owner, amount);
            } catch (BankruptcyException e) {
              // e.printStackTrace();
              game.processBankruptcy(player, location.owner);
              player.nextAction = Actions.DONE;
              return inactiveState;
            }
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
