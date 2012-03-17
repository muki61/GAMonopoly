package cs571.mukhar.states;

import java.util.Random;
import java.util.logging.Logger;

import cs571.mukhar.AbstractPlayer;
import cs571.mukhar.Actions;
import cs571.mukhar.Dice;
import cs571.mukhar.Location;
import cs571.mukhar.Main;
import cs571.mukhar.PropertyFactory;
import cs571.mukhar.PropertyGroups;

public class PlayerState {

  protected static Logger logger = Logger.getLogger("cs571.mukhar");

  protected static AbstractPlayer player;
  protected static AbstractPlayer[] otherPlayers;

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

  static Random r = new Random();
  static {
    long seed = 1241797664978L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    System.out.println("PlayerState seed   : " + seed);
    r.setSeed(seed);
  }

  Dice dice = Dice.getDice();
  static int numDoubles = 0;

  PlayerState() {
  }

  public PlayerState processEvent(Events event) {
    throw new IllegalAccessError();
  }

  protected void enter() {
  }

  double nextDouble() {
    return r.nextDouble();
  }

  protected void rollDice() {
    int[] roll = dice.roll();

    player.setDoubles(dice.rolledDoubles());
    if (dice.rolledDoubles()) {
      numDoubles += 1;
      logger.info("numDoubles : " + numDoubles);
    }

    Location location;
    int currentRoll = roll[0] + roll[1];

    if (numDoubles == 3) {
      // send to jail
      PropertyFactory pf = PropertyFactory.getPropertyFactory();
      location = pf.getLocationAt(10);
      player.enteredJail();
      player.setLocationIndex(location.index);
      player.setCurrentLocation(location);
      logger.info("Player " + player.playerIndex
          + " rolled doubles 3 times in a row. Player sent to jail.");
      player.nextAction = Actions.MAKE_BUILD_DECISION;

    } else {
      movePlayer (currentRoll);

      if (player.passedGo()) {
        payPlayer(player, 200);
      }
    }
  }

  protected void movePlayer(int currentRoll) {
    int newLocation = player.advance(currentRoll);

    PropertyFactory pf = PropertyFactory.getPropertyFactory();
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

  protected PlayerState determineNextState() {
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

  public void setActivePlayer(AbstractPlayer abstractPlayer) {
    player = abstractPlayer;
    numDoubles = 0;
    // logger.info("SET NUM DOUBLES = 0");
    // logger.info("numDoubles : " + numDoubles);
  }

  public void setOtherPlayers(AbstractPlayer[] others) {
    otherPlayers = new AbstractPlayer[others.length];
    int index = 0;
    for (AbstractPlayer player : others) {
      otherPlayers[index++] = player;
    }
  }
}
