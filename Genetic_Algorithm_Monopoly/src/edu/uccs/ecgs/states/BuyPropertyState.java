package edu.uccs.ecgs.states;

import java.util.TreeMap;

import edu.uccs.ecgs.Actions;
import edu.uccs.ecgs.BankruptcyException;
import edu.uccs.ecgs.Location;
import edu.uccs.ecgs.Monopoly;
import edu.uccs.ecgs.PropertyFactory;

public class BuyPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Events event) {
    logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case BUY_PROPERTY_EVENT:
      Location location = player.getCurrentLocation();
      assert player.canRaiseCash(location.getCost()) : "Player cannot raise cash: " + location.getCost();
      try {
        player.getCash(location.getCost());
        location.setOwner(player);
        player.addProperty(location);
        PropertyFactory.getPropertyFactory().checkForMonopoly();
        if (location.partOfMonopoly) {
          logger.info("Player " + player.playerIndex
              + " acquired monopoly with " + location.name);
        }
      } catch (BankruptcyException e) {
        // player will not buy house unless they have enough cash
        e.printStackTrace();
        //but just in case this happens...
        TreeMap<Integer, Location> lotsToAuction = new TreeMap<Integer,Location>();
        lotsToAuction.put(location.index, location);
        Monopoly.auctionLots(lotsToAuction);
      }

      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
        return activeState;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }
      
    case WON_AUCTION_EVENT:
      if (player.rolledDoubles()) {
        player.nextAction = Actions.ROLL_DICE;
      } else {
        player.nextAction = Actions.MAKE_BUILD_DECISION;
        developPropertyState.enter();
        return developPropertyState;
      }
      return this;

    case ROLL_DICE_EVENT:
      rollDice();
      return determineNextState();

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
