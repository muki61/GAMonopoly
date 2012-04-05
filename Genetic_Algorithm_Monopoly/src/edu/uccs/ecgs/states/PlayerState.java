package edu.uccs.ecgs.states;

import java.util.Random;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.Dice;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Main;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.ga.PropertyFactory;
import edu.uccs.ecgs.ga.PropertyGroups;

public class PlayerState {

  public static PlayerState activeState = new ActiveState();
  public static PlayerState atNewLocationState = new AtNewLocationState();
  public static PlayerState auctionState = new AuctionState();
  public static PlayerState bailPaidState = new BailPaidState();
  public static PlayerState developPropertyState = new DevelopPropertyState();
  public static PlayerState inactiveState = new InactiveState();
  public static PlayerState inJailState = new InJailState();
  public static PlayerState buyPropertyState = new BuyPropertyState();
  public static PlayerState propertyDeclinedState = new PropertyDeclinedState();
  public static PlayerState payRentState = new PayRentState();
  public static PlayerState tradePropertyState = new TradePropertyState();
  public static PlayerState playerState = new PlayerState();
  public static PlayerState payoffMortgageState = new PayoffMortgageState();

  Random r = new Random();

  int numDoubles = 0;

  PlayerState() {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
//    System.out.println("PlayerState seed   : " + seed);

    r.setSeed(seed);
  }

  public PlayerState processEvent(Monopoly game, AbstractPlayer player, Events event) {
    throw new IllegalAccessError();
  }

  protected void enter() {
  }

  double nextDouble() {
    return r.nextDouble();
  }

  protected void rollDice(Monopoly game, AbstractPlayer player) {
    Dice dice = game.getDice();
    int[] roll = dice.roll();
    game.logDiceRoll(roll);

    player.setDoubles(dice.rolledDoubles());
    if (dice.rolledDoubles()) {
      numDoubles += 1;
      game.logger.finest("numDoubles : " + numDoubles);
    }

    Location location;
    int currentRoll = roll[0] + roll[1];

    if (numDoubles == 3) {
      // send to jail
      PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
      location = pf.getLocationAt(10);
      player.enteredJail();
      player.setLocationIndex(location.index);
      player.setCurrentLocation(location);
      game.logger.finest("Player " + player.playerIndex
          + " rolled doubles 3 times in a row. Player sent to jail.");
      player.nextAction = Actions.MAKE_BUILD_DECISION;

    } else {
      movePlayer (currentRoll, game, player);

      if (player.passedGo()) {
        payPlayer(player, 200);
      }
    }
  }

  protected void movePlayer(int currentRoll, Monopoly game, AbstractPlayer player) {
    int newLocation = player.advance(currentRoll);

    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    Location location = pf.getLocationAt(newLocation);
    
    player.setCurrentLocation(location);
    
    if (location.getGroup() == PropertyGroups.SPECIAL) {
      int locationIndex = player.getLocationIndex();
      if (locationIndex == 0 || locationIndex == 20 || 
          locationIndex == 10 && !player.inJail) {
        // location is either Go, Just Visiting, or Free Parking
        if (player.rolledDoubles()) {
          player.nextAction = Actions.ROLL_DICE;
        } else {
          player.nextAction = Actions.MAKE_BUILD_DECISION;
        }
      } else {
        player.nextAction = Actions.PROCESS_SPECIAL_ACTION;
      }
    } else if (location.owner != null) {
      player.nextAction = Actions.PAY_RENT;
    } else {
      player.nextAction = Actions.EVAL_PROPERTY;
    }
  }

  protected PlayerState determineNextState(AbstractPlayer player) {
    switch (player.nextAction) {
    case MAKE_BUILD_DECISION:
      //player has landed on go, jail, or free parking
      developPropertyState.enter();
      return developPropertyState;

    case ROLL_DICE:
      return this;

    case PROCESS_SPECIAL_ACTION:
      atNewLocationState.enter();
      return atNewLocationState;

    case PAY_RENT:
      payRentState.enter();
      return payRentState;

    case EVAL_PROPERTY:
      atNewLocationState.enter();
      return atNewLocationState;

    default:
      String msg = "Unexpected action " + player.nextAction;
      throw new IllegalArgumentException(msg);
    }
  }

  void payPlayer(AbstractPlayer player, int amount) {
    player.receiveCash(amount);
  }
}
