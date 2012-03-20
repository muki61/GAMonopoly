package edu.uccs.ecgs.states;

import edu.uccs.ecgs.AbstractPlayer;
import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.BankruptcyException;
import edu.uccs.ecgs.Chance;
import edu.uccs.ecgs.CommunityChest;
import edu.uccs.ecgs.Location;
import edu.uccs.ecgs.Monopoly;
import edu.uccs.ecgs.PropertyFactory;
import edu.uccs.ecgs.PropertyGroups;

public class AtNewLocationState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    Location location = player.getCurrentLocation();

    switch (event) {

    case EVAL_PROPERTY_EVENT:
      if (player.buyProperty() && player.canRaiseCash(location.getCost())) {
        player.nextAction = Actions.BUY_PROPERTY;
        buyPropertyState.enter();
        return buyPropertyState;
      } else {
        player.nextAction = Actions.DECLINE_PROPERTY;
        propertyDeclinedState.enter();
        return propertyDeclinedState;
      }

    case PROCESS_SPECIAL_ACTION_EVENT:
      //If moved location, then decide between pay rent or eval
      //If paid fee, then decide between roll again or eval
      //If get out of jail card, then decide between roll or eval
      if (location.name.equals("Community Chest")) {
        CommunityChest ccCard = game.getCards().getNextCommunityChestCard();

        try {
          ccCard.processCard(player, game);
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }
        
        location = player.getCurrentLocation();
        if (!location.name.equals("Community Chest")) {
          //location name has changed, so the player must have gotten an advance to someplace card
          if (location.getGroup() == PropertyGroups.SPECIAL) {
            // we advanced or moved to another special location
            if (player.inJail()) {
              player.nextAction = Actions.MAKE_BUILD_DECISION;
              developPropertyState.enter();
              return developPropertyState;
            } else if (player.getLocationIndex() == 0) {
              if (player.rolledDoubles()) {
                player.nextAction = Actions.ROLL_DICE;
                return activeState;
              } else {
                player.nextAction = Actions.MAKE_BUILD_DECISION;
                developPropertyState.enter();
                return developPropertyState;
              }
            } else {
              player.nextAction = Actions.PROCESS_SPECIAL_ACTION;
            }
          } else if (location.owner != null) {
            player.nextAction = Actions.PAY_RENT;
          } else {
            player.nextAction = Actions.EVAL_PROPERTY;
          }
        } else {
          //did not change location
          if (player.rolledDoubles()) {
            player.nextAction = Actions.ROLL_DICE;
            return activeState;
          } else {
            player.nextAction = Actions.MAKE_BUILD_DECISION;
            developPropertyState.enter();
            return developPropertyState;
          }
        }
      } else if (location.name.equals("Chance")) {
        Chance chanceCard = game.getCards().getNextChanceCard();

        try {
          chanceCard.processChance(player, game);
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }

        location = player.getCurrentLocation();
        if (!location.name.equals("Chance")) {
          //location name has changed, so the player must have gotten an advance to someplace card
          if (location.getGroup() == PropertyGroups.SPECIAL) {
            //we advanced or moved to another special location
            if (player.inJail()) {
              player.nextAction = Actions.MAKE_BUILD_DECISION;
              developPropertyState.enter();
              return developPropertyState;
            } else if (player.getLocationIndex() == 0) {
              if (player.rolledDoubles()) {
                player.nextAction = Actions.ROLL_DICE;
                return activeState;
              } else {
                player.nextAction = Actions.MAKE_BUILD_DECISION;
                developPropertyState.enter();
                return developPropertyState;
              }
            } else {
              player.nextAction = Actions.PROCESS_SPECIAL_ACTION;
            }
          } else if (location.owner != null) {
            player.nextAction = Actions.PAY_RENT;
            payRentState.enter();
            return payRentState;
          } else {
            player.nextAction = Actions.EVAL_PROPERTY;
          }
        } else {
          //did not change location
          if (player.rolledDoubles()) {
            player.nextAction = Actions.ROLL_DICE;
            return activeState;
          } else {
            player.nextAction = Actions.MAKE_BUILD_DECISION;
            developPropertyState.enter();
            return developPropertyState;
          }
        }
      } else if (location.name.equals("Income Tax")) {
        try {
          player.getCash(200);
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }

        if (player.rolledDoubles()) {
          player.nextAction = Actions.ROLL_DICE;
          return activeState;
        } else {
          player.nextAction = Actions.MAKE_BUILD_DECISION;
          developPropertyState.enter();
          return developPropertyState;
        }
      } else if (location.name.equals("Luxury Tax")) {

        try {
          player.getCash(100);
        } catch (BankruptcyException e) {
          //e.printStackTrace();
          game.processBankruptcy(player, null);
          player.nextAction = Actions.DONE;
          return inactiveState;
        }

        if (player.rolledDoubles()) {
          player.nextAction = Actions.ROLL_DICE;
          return activeState;
        } else {
          player.nextAction = Actions.MAKE_BUILD_DECISION;
          developPropertyState.enter();
          return developPropertyState;
        }
      } else if (location.name.equals("Go To Jail")) {
        game.logger.info("Player " + player.playerIndex + " landed on Go To Jail.");

        PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
        player.enteredJail();
        player.setLocationIndex(10);
        player.setCurrentLocation(pf.getLocationAt(10));

        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      } else {
        game.logger.info("****************************************************");
        game.logger.info("Location : " + location.name);
        game.logger.info("****************************************************");
        System.exit(1);
      }
      return this;

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }
}
