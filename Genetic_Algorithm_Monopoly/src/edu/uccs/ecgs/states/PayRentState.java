package cs571.mukhar.states;

import cs571.mukhar.Actions;
import cs571.mukhar.BankruptcyException;
import cs571.mukhar.Location;
import cs571.mukhar.Monopoly;

public class PayRentState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
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
            Monopoly.payRent(player, location.owner, amount);
          } catch (BankruptcyException e) {
            //e.printStackTrace();
            Monopoly.processBankruptcy(player, location.owner);
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
      rollDice();
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
