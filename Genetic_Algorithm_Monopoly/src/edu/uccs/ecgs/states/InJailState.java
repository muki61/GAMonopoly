package edu.uccs.ecgs.states;

//import org.junit.Assert;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Dice;
import edu.uccs.ecgs.ga.Monopoly;

public class InJailState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logFinest("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    Dice dice = game.getDice();
    switch (event) {
    
    case ROLL_DICE_EVENT:
      int[] roll = dice.roll();
      game.logDiceRoll(roll);
      int currentRoll = roll[0] + roll[1];

      //even though player rolled doubles, they do not get to roll again
      //so do not call player.setDoubles()

      if (dice.rolledDoubles()) {
        player.paidBail();
        movePlayer(currentRoll, game, player);
        assert player.nextAction != Actions.ROLL_DICE : "Invalid action: ";
        atNewLocationState.enter();
        return determineNextState(player);
      } else {
        //did not roll doubles
        player.setDoubles(false);
        if (player.jailSentenceCompleted()) {
          if (player.hasGetOutOfJailCard()) {
            player.useGetOutOfJailCard();
          } else {
            //actually pay bail
            try {
              player.getCash(50);
            } catch (BankruptcyException e) {
              //e.printStackTrace();
              game.processBankruptcy(player, null);
              player.nextAction = Actions.DONE;
              return inactiveState;
            }
          }
          player.paidBail();
          movePlayer(currentRoll, game, player);
          assert player.nextAction != Actions.ROLL_DICE : "Invalid action: ";
          return determineNextState(player);
        }
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }

    case PAY_BAIL_EVENT:
      //assume that if the player wants to pay bail, they would
      //use a Get Out Of Jail Card first, if they have one
      if (player.hasGetOutOfJailCard()) {
        player.useGetOutOfJailCard();
        game.logFinest("Player used Get Out of Jail Free card");
      } else {
        //actually pay bail
        try {
          game.logFinest("Player will pay $50 to get out of jail");
          player.getCash(50);
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }
      }

      player.paidBail();
      player.nextAction = Actions.ROLL_DICE;
      bailPaidState.enter();
      return bailPaidState;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
