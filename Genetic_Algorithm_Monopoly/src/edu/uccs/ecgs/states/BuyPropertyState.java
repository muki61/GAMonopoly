package edu.uccs.ecgs.states;

import java.util.TreeMap;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.ga.PropertyFactory;

public class BuyPropertyState extends PlayerState {

  @Override
  protected void enter() {
    super.enter();
  }

  @Override
  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    game.logger.info("Player " + player.playerIndex + "; state " + this.getClass().getSimpleName() +
        "; event " + event.name());
    switch (event) {
    
    case BUY_PROPERTY_EVENT:
      Location location = player.getCurrentLocation();
      assert player.canRaiseCash(location.getCost()) : "Player cannot raise cash: " + location.getCost();
      try {
        player.getCash(location.getCost());
        location.setOwner(player);
        player.addProperty(location);
        PropertyFactory.getPropertyFactory(game.gamekey).checkForMonopoly();
        if (location.partOfMonopoly) {
          game.logger.info("Player " + player.playerIndex
              + " acquired monopoly with " + location.name);
        }
      } catch (BankruptcyException e) {
        // player will not buy house unless they have enough cash
        e.printStackTrace();
        //but just in case this happens...
        TreeMap<Integer, Location> lotsToAuction = new TreeMap<Integer,Location>();
        lotsToAuction.put(location.index, location);
        game.auctionLots(lotsToAuction);
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
      rollDice(game, player);
      return determineNextState(player);

    default:
      String msg = "Unexpected event " + event;
      throw new IllegalArgumentException(msg);
    }
  }

}
