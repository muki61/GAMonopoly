package edu.uccs.ecgs.ga;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import edu.uccs.ecgs.states.Events;

public class Monopoly implements Runnable {

  private Logger logger;
  Formatter formatter;
  FileHandler fh;

  boolean done = false;

  private int generation;
  private int match;
  private int game;

  int playerIndex = 0;
  Dice dice = Dice.getDice();
  int turnCounter = 0;
  Random r;
  private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SS");
  
  /**
   * The players for this game.
   */
  private AbstractPlayer[] players;

  /**
   * The Chance and Community Chest cards.
   */
  private Cards cards;
  private int bankruptCount;

  private int numHouses;
  private int numHotels;

  public String gamekey;
  private long seed;

  /**
   * Constructor
   * @param generation Generation number for this game.
   * @param match Match number for this game.
   * @param gameNumber Game number for this game.
   * @param players Array of players for this game.
   */
  public Monopoly(int generation, int match, int gameNumber,
      AbstractPlayer[] players) {
    this.generation = generation;
    this.match = match;
    this.game = gameNumber;
    this.players = players;

    gamekey = "edu.uccs.ecgs.ga." + this.generation + "." + this.match + "." + game;

    r = new Random();
    seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);

    turnCounter = 0;
    numHouses = 32;
    numHotels = 12;

    cards = Cards.getCards();
  }

  @Override
  public void run() {
    try {
      playGame();
    } catch (Throwable t) {
      t.printStackTrace();
      StackTraceElement[] ste = t.getStackTrace();
      logSevere(t.toString());
      for (StackTraceElement s : ste) {
        logSevere(s.toString());
      }
    } finally {
      endGame();
    }
  }

  /**
   * Play the game
   */
  private void playGame() {
    initLogger();

    done = false;

    logFinest("Started game " + this.generation + "." + this.match + "." + this.game + " with players: ");
    for (AbstractPlayer p : players) {
      logFinest("Player " + p.playerIndex);
    }

    for (AbstractPlayer player : players) {
      player.joinGame(this);
    }

    logFinest("Game seed: " + seed);

    bankruptCount = 0;

    while (!done) {

      synchronized (this) {
        if (Main.paused) {
          try {
            wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

      ++turnCounter;
      if (turnCounter == Main.maxTurns * Main.numPlayers) {
        done = true;
      }

      AbstractPlayer player = getNextPlayer();
      player.resetDoubles();

      logFinest("");
      logFinest("Turn: " + turnCounter);
      logFinest("Player " + player.playerIndex);

      Events event = Events.PLAYER_ACTIVATED_EVENT;
      Actions action = Actions.NULL;

      while (action != Actions.DONE) {
        action = player.getNextActionEnum(event);
        
        if (bankruptCount == Main.numPlayers - 1) {
          break;
        }
        
        switch (action) {
        case ROLL_DICE:
          event = Events.ROLL_DICE_EVENT;
          break;

        case PAY_BAIL:
          event = Events.PAY_BAIL_EVENT;
          break;

        case PAY_RENT:
          event = Events.PAY_RENT_EVENT;
          break;

        case EVAL_PROPERTY:
          event = Events.EVAL_PROPERTY_EVENT;
          break;

        case BUY_PROPERTY:
          event = Events.BUY_PROPERTY_EVENT;
          break;

        case DECLINE_PROPERTY:
          event = Events.DECLINE_PROPERTY_EVENT;
          break;

        case AUCTION_BID:
          event = Events.AUCTION_STARTED_EVENT;
          break;

        case AUCTION_WON:
          event = Events.WON_AUCTION_EVENT;
          break;

        case AUCTION_LOST:
          event = Events.LOST_AUCTION_EVENT;
          break;

        case PROCESS_SPECIAL_ACTION:
          event = Events.PROCESS_SPECIAL_ACTION_EVENT;
          break;

        case MAKE_BUILD_DECISION:
          event = Events.DEVELOP_DECISION_EVENT;
          break;

        case MAKE_MORTGAGE_DECISION:
          event = Events.MORTGAGE_DECISION_EVENT;
          break;

        case MAKE_TRADE_DECISION:
          event = Events.TRADE_DECISION_EVENT;
          break;

        case DONE:
          break;
        case NULL:
          break;

        default:
          throw new IllegalArgumentException("Unhandled action " + action);
        }
      }

      if (bankruptCount == Main.numPlayers - 1) {
        done = true;
      }
    }

    ArrayList<AbstractPlayer> sortedPlayers = new ArrayList<AbstractPlayer>();

    int totalNetWorth = 0;
    
    for (AbstractPlayer p : players) {
      int playerNetWorth = p.getTotalWorth();
      totalNetWorth += playerNetWorth;
      sortedPlayers.add(p);
    }
    
    Collections.sort(sortedPlayers, NetWorthComparator.get());

    logFinest('\f' + "GAME OVER");
    assert totalNetWorth > 0;

    // In order from loser to winner, add score points to player
    int score = Main.numPlayers;
    for (AbstractPlayer p : sortedPlayers) {
      p.setFinishOrder(score);
      p.setGameNetWorth(totalNetWorth);
      score--;
    }

    for (AbstractPlayer p : sortedPlayers) {
      logFinest("");
      logFinest(p.toString());
    }    
  }

  /**
   * Get a reference to the player whose turn it is to play.
   * @return The player who is next to play.
   */
  private AbstractPlayer getNextPlayer() {
    while (players[playerIndex].bankrupt()) {
      playerIndex = ++playerIndex % 4;
    }

    AbstractPlayer p = players[playerIndex];
    playerIndex = ++playerIndex % 4;

    return p;
  }

  /**
   * Transfer rent amount from one player to another
   * @param from Player who owes rent
   * @param to Player to whom rent is owed.
   * @param amount The amount of rent.
   * @throws BankruptcyException If the paying player does not have amount.
   */
  public void payRent(AbstractPlayer from, AbstractPlayer to, int amount)
      throws BankruptcyException {
    from.getCash(amount);
    to.receiveCash(amount);
  }

  /**
   * Create a formatter and set logging on or off depending on state of
   * {@link edu.uccs.ecgs.ga.Main#debug}. If Main.debug is true, then logging is
   * turned on; if debug is false, logging is turned off.
   */
  public void initLogger() {
    if (Main.debug != Level.OFF) {
      logger = Logger.getLogger(gamekey);
      logger.setLevel(Main.debug);

      formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
          return Monopoly.this.generation + ":" + Monopoly.this.match
              + ":" + Monopoly.this.game + ":"
              + Thread.currentThread().getName() + ":"
              + dateFormat.format(Calendar.getInstance().getTime()) + ": "
              + record.getMessage() + "\n";
        }
      };

      logFileSetup();
    }
  }

  /**
   * Create the log file based on generation, match, and game number, and add
   * the formatter to the logger.
   */
  public void logFileSetup() {
    StringBuilder dir = Utility.getDirForGen(Main.chromoType,
        Main.fitnessEvaluator, generation);

    dir.append("/").append(getMatchString());
    File file = new File(dir.toString());
    if (!file.exists()) {
      file.mkdir();
    }

    StringBuilder fileName = new StringBuilder(getGameString().append(".rtf"));

    try {
      fh = new FileHandler(dir + "/" + fileName, false);
      logger.addHandler(fh);
      fh.setFormatter(formatter);

    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create a string of the form "Match_nnn" where nnn is the match number.
   * 
   * @return A string of the form "Match_nnn" where nnn is the match number.
   */
  private StringBuilder getMatchString() {
    StringBuilder result = new StringBuilder("" + match);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Match_");

    return result;
  }

  /**
   * Create a string of the form "Game_nnn" where nnn is the game number.
   * 
   * @return A string of the form "Game_nnn" where nnn is the game number.
   */
  private StringBuilder getGameString() {
    StringBuilder result = new StringBuilder("" + game);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Game_");

    return result;
  }

  /**
   * Sell a house from the given location. The proceeds from the sale are given
   * to the player that owns the property (identified by location.owner).
   * 
   * @param location
   *          The property to be sold.
   */
  public void sellHouse(Location location) {
    location.removeHouse();
    location.owner.receiveCash(location.getHouseCost() / 2);
    ++numHouses;
    
    logFinest("Sold house at " + location.toString() + "; property now has " + location.getNumHouses() + " houses");
    
    assert numHouses < 33 : "Invalid number of houses: " + numHouses;
  }

  /**
   * Sell a hotel from the given location. According to the rules of Monopoly,
   * the player must be able to put four houses on the location when the hotel
   * is sold. If 4 houses are not available, the player is forced to sell as
   * many hotels and (virtual) houses until the player can put any real houses
   * on the property group in accordance with the rules.
   * 
   * For example, if 4 real houses are available, the player simply sells the
   * hotel and puts 4 houses on the location.
   * 
   * However, if only 3 real houses are available and the player has more than
   * one hotel (H) on the property group, the player is not allowed to put only
   * 3 houses (h) on a location while one or two other locations in the group
   * still have hotels (building must always be balanced). Thus, when selling a
   * hotel, the only allowed configurations after selling are H/H/4h, H/4h/4h,
   * 4h/4h/4h, or 3h/4h/4h. If the player has 3 or 2 hotels (H/H/H or H/H/4h)
   * and only 3 houses are available, selling a hotel would result in H/H/3h or
   * H/4h/3h, both of which are illegal since they are unbalanced. Thus the
   * player is required to sell as many hotels and houses, until the number of
   * houses and hotels on the location are balanced. So for example, if a player
   * has H/H/H and only 3 houses remain unsold in the game, the player would be
   * required to sell all the hotels and 9 virtual houses, and they place 1
   * house on each location.
   * 
   * @param player
   * @param location
   * @param owned
   */
  public void sellHotel(AbstractPlayer player, Location location,
      Collection<Location> owned) {
    int numHotelsInGroup = PropertyFactory.getPropertyFactory(gamekey)
        .getNumHotelsInGroup(location);
    int numHousesInGroup = PropertyFactory.getPropertyFactory(gamekey)
        .getNumHousesInGroup(location);

    int numHousesToSell1 = 0; // number of houses to sell on 1st property in
                              // group
    int numHousesToSell2 = 0; // number of houses to sell on 2nd property in
                              // group
    int numHousesToSell3 = 0; // number of houses to sell on 3rd property in
                              // group

    switch (numHouses) {
    case 4:
      // 4 houses
    default:
      // more than 4 houses
      location.removeHotel();
      logFinest("Sold hotel at " + location.toString() + "; property now has 4 houses");
      player.receiveCash(location.getHotelCost() / 2);
      ++numHotels;
      assert numHotels <= 12 : "Invalid number of hotels: " + numHotels;
      numHouses = numHouses - 4;
      // return rather than break because we don't want to call the sell method
      // at the end of the case block, since that method sells all hotels.
      return;

    case 3:
      if (numHotelsInGroup == 3) {
        // must sell all hotels and 9 houses and then arrange houses as 1/1/1
        numHousesToSell1 = 3;
        numHousesToSell2 = 3;
        numHousesToSell3 = 3;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 4) {
        // location must be 3 property group
        // must sell both hotels and then arrange houses as 2/2/3
        numHousesToSell1 = 2;
        numHousesToSell2 = 2;
        numHousesToSell3 = 1;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 0) {
        // location must be 2 property group (Baltic/Med or Park Place/Boardwalk)
        // must sell both hotels and then arrange houses as 1/2
        numHousesToSell1 = 3;
        numHousesToSell2 = 2;
        numHousesToSell3 = 4;

      } else {
        logSevere("Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup);
        assert false : "Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup;
      }
      break;

    case 2:
      if (numHotelsInGroup == 3) {
        // must sell all hotels and 10 houses and then arrange houses as 0/1/1
        numHousesToSell1 = 4;
        numHousesToSell2 = 3;
        numHousesToSell3 = 3;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 4) {
        // must sell both hotels and then arrange houses as 2/2/2
        numHousesToSell1 = 2;
        numHousesToSell2 = 2;
        numHousesToSell3 = 2;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 0) {
        // must sell both hotels then arrange houses as 1/1
        numHousesToSell1 = 3;
        numHousesToSell2 = 3;
        numHousesToSell3 = 4;

      } else {
        logSevere("Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup);
        assert false : "Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup;
      }
      break;

    case 1:
      if (numHotelsInGroup == 3) {
        // must sell all hotels and 11 houses and then arrange houses as 0/0/1
        numHousesToSell1 = 4;
        numHousesToSell2 = 4;
        numHousesToSell3 = 3;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 4) {
        // must sell both hotels and then arrange houses as 1/2/2
        numHousesToSell1 = 2;
        numHousesToSell2 = 2;
        numHousesToSell3 = 3;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 0) {
        // must sell both hotels and then arrange houses as 0/1
        numHousesToSell1 = 4;
        numHousesToSell2 = 3;
        numHousesToSell3 = 4;

      } else {
        logSevere("Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup);
        assert false : "Invalid number of hotels/houses in property group for location "
            + location
            + "; hotels/houses: "
            + numHotelsInGroup
            + "/"
            + numHousesInGroup;
      }
      break;

    case 0:
      numHousesToSell1 = 4;
      numHousesToSell2 = 4;
      numHousesToSell3 = 4;
      break;
    }

    sell(player, location, owned, numHousesToSell1, numHousesToSell2,
        numHousesToSell3);
    numHouses = 0;
  }

  /**
   * Change the location of the number of houses to sell based on the property
   * group. In certain property groups, any extra house should go on the first
   * property in the group. In other groups, any extra house goes on the second
   * property. This method adjusts the numHousesToSell array so the extra house
   * is on the correct property.
   * 
   * @param location
   *          The property the identifies the group.
   * @param numHousesToSell
   *          A 3-element array containing the number of houses to sell. This array
   *          is modified so that if there is an uneven number of houses to sell, the
   *          correct property gets the extra house.
   */
  private void swapHousesToSell(Location location, int[] numHousesToSell) {
    switch (location.getGroup()) {
    case PURPLE:
    case RED:
    case YELLOW:
    case GREEN:
      // for these properties, ensure first property has any 2nd extra house
      if (numHousesToSell[0] < numHousesToSell[1]) {
        int temp = numHousesToSell[1];
        numHousesToSell[1] = numHousesToSell[0];
        numHousesToSell[0] = temp;
      }
      break;
    default:
      // for everything else, no change
    }
  }

  /**
   * Sell all hotels in the property group to which the location belongs, and
   * then sell the houses given by each of the numHouses params.
   * 
   * @param player
   *          The player who owns the properties and who will receive the money
   *          from the sale.
   * @param location
   *          The property that identifies the group from which hotels and
   *          houses will be sold.
   * @param owned
   *          The collection of properties owned by the player.
   * @param numHousesToSell1
   *          The number of houses to sell from the first property in the group.
   * @param numHousesToSell2
   *          The number of houses to sell from the second property in the
   *          group.
   * @param numHousesToSell3
   *          The number of houses to sell from the third property in the group.
   */
  private void sell(AbstractPlayer player, Location location,
      Collection<Location> owned, int numHousesToSell1, int numHousesToSell2,
      int numHousesToSell3) {

    // change the order of the houses to sell based on property group
    swapHousesToSell(location, new int[] { numHousesToSell1, numHousesToSell2,
        numHousesToSell3 });

    int count = 0;

    for (Location loc : owned) {
      if (loc.getGroup() == location.getGroup()) {
        ++count;

        loc.removeHotel();
        logFinest("Sold hotel at " + loc.toString());
        ++numHotels;
        assert numHotels < 13 : "Invalid number hotels: " + numHotels;

        player.receiveCash(loc.getHotelCost() / 2);

        int sellCount = 0;
        switch (count) {
        case 1:
          sellCount = numHousesToSell1;
          break;
        case 2:
          sellCount = numHousesToSell2;
          break;
        case 3:
          sellCount = numHousesToSell3;
          break;
        }

        for (; sellCount > 0; sellCount--) {
          loc.removeHouse();
          // in this method, don't increment the number of houses
          // because the houses at the location are virtual houses
          player.receiveCash(loc.getHouseCost() / 2);
        }
      }
    }
  }

  /**
   * Buy a house for player at location.
   * 
   * @param player
   *          The player that is buying the house.
   * @param location
   *          The location which will initially receive the house. The player
   *          must build evenly, so after buying a house for this location, the
   *          player may rebalance the houses among the properties in this
   *          location's group.
   */
  public void buyHouse(AbstractPlayer player, Location location) {
    if (numHouses == 0) {
      logFinest("Player " + player.playerIndex
          + " wanted to buy house, but none are available");
      return;
    }

    try {
      assert player.canRaiseCash(location.getHouseCost()) : "Player tried to buy house with insufficient cash";
      assert !location.isMortgaged : "Player tried to buy house; Location " + location.name + " is mortgaged.";
      assert location.partOfMonopoly : "Player tried to buy house; Location " + location.name + " is not part of monopoly";
      assert !PropertyFactory.getPropertyFactory(gamekey).groupIsMortgaged(location.getGroup()) : 
        "Player tried to buy house; Some property in " + location.getGroup() + " is mortgaged.";

      player.getCash(location.getHouseCost());
      location.addHouse();
      --numHouses;
      logFinest("Bought house for property group " + location.getGroup());
      assert numHouses >= 0 : "Invalid number of houses: " + numHouses;
      logFinest("Bank now has " + numHouses + " houses");
    } catch (BankruptcyException ignored) {
      // expect that any player that buys a house first verifies they
      // have enough cash
      ignored.printStackTrace();
    } catch (AssertionError ae) {
      logFinest(player.toString());
      throw ae;
    }
  }

  /**
   * Buy a hotel for player at location.
   * 
   * @param player
   *          The player that is buying the hotel.
   * @param location
   *          The location which will receive the hotel.
   */
  public void buyHotel(AbstractPlayer player, Location location) {
    try {
      assert player.canRaiseCash(location.getHotelCost()) : "Player tried to buy house with insufficient cash";
      location.addHotel();
      --numHotels;
      player.getCash(location.getHotelCost());
      logFinest("Bought hotel at " + location.toString());
      assert numHotels >= 0 : "Invalid number of hotels: " + numHotels;

      // add the houses back to the bank
      numHouses += 4;
      assert numHouses < 33 : "Invalid number of houses: " + numHouses;
    } catch (BankruptcyException ignored) {
      // expect that any player that buys a house first verifies they
      // have enough cash
      // TODO Verify that this exception will not occur
      Throwable t = new Throwable(toString(), ignored);
      t.printStackTrace();
    }
  }

  /**
   * Take all the necessary actions when a player goes bankrupt. This includes
   * selling all houses and hotels, and giving all cash and properties to the
   * gaining player.
   * 
   * @param player
   *          The player who went bankrupt.
   * @param gainingPlayer
   *          The player who receives all the bankrupt player's assets. This can
   *          be another player in the game, or it can be the bank. If the
   *          parameter is null, then the player went bankrupt against the bank
   *          (for example, by owing income tax).
   */
  public void processBankruptcy(AbstractPlayer player,
      AbstractPlayer gainingPlayer) {

    logFinest("Player " + player.playerIndex + " is bankrupt");
    player.setBankruptIndex(bankruptCount);
    ++bankruptCount;

    boolean gameOver = false;
    if (bankruptCount == Main.numPlayers - 1) {
      gameOver = true;
    }

    if (gainingPlayer != null) {
      logFinest("Gaining player is " + gainingPlayer.playerIndex);
    } else {
      logFinest("Gaining player is bank");
    }

    // return any get out of jail cards to the stack
    while (player.hasGetOutOfJailCard()) {
      player.useGetOutOfJailCard();
    }

    logFinest("Bankrupt count: " + bankruptCount);

    player.sellAllHousesAndHotels();

    if (gainingPlayer == null) {
      // player went bankrupt because they owed bank money
      player.getAllCash();

      if (!gameOver) {
        TreeMap<Integer, Location> lotsToAuction = new TreeMap<Integer, Location>();
        lotsToAuction.putAll(player.getAllProperties());
        player.clearAllProperties();
        auctionLots(lotsToAuction);
      }
    } else {
      // give all cash to gaining player
      gainingPlayer.receiveCash(player.getAllCash());

      // give all property to gaining player
      // mortgaged properties are handled in the addProperties method
      try {
        gainingPlayer.addProperties(player.getAllProperties(), gameOver);
      } catch (BankruptcyException e) {
        //rarely, the player gaining the properties will not be able to raise
        //the case to pay the interest. The gainingPlayer goes bankrupt also.
        processBankruptcy(gainingPlayer, null);
      }
    }

    player.clearAllProperties();
    player.setBankrupt();
    assert player.cash == 0;
  }

  public void auctionLots(TreeMap<Integer, Location> lotsToAuction) {    
    // set owner to null and mortgaged to false for all lots
    for (Location location : lotsToAuction.values()) {
      location.owner = null;
      location.setMortgaged(false);
    }

    String msg = "";

    for (Location location : lotsToAuction.values()) {
      logFinest("Bank is auctioning " + location.name);

      int highBid = 0;
      AbstractPlayer highBidPlayer = null;
      int secondHighestBid = 0;

      for (AbstractPlayer p : players) {
        int bid = p.getBidForLocation(location);
        logFinest("Player " + p.playerIndex + " has " + p.cash
            + " dollars and bids " + bid);

        if (bid > highBid) {
          secondHighestBid = highBid;
          highBid = bid;
          highBidPlayer = p;
        } else if (bid > secondHighestBid) {
          secondHighestBid = bid;
        }
      }

      int finalBid = 0;

      if (highBid == 0) {
        msg = "early";
        // end auction
        break;
      } else {
        finalBid = secondHighestBid + 5;
        if (finalBid > highBid) {
          finalBid = highBid;
        }
      }

      assert finalBid > 0 : "Final bid is invalid: " + finalBid;
      assert highBidPlayer != null : "High bid player is null";
      assert highBidPlayer.canRaiseCash(finalBid) : "Player "
          + highBidPlayer.playerIndex + " cannot raise cash " + finalBid;

      try {
        processAuctionResult(highBidPlayer, location, finalBid);
      } catch (BankruptcyException e) {
        // assume player cannot win auction unless they have enough cash
        // TODO Verify that this exception will not occur
        Throwable t = new Throwable(toString(), e);
        t.printStackTrace();
      }
    }

    logFinest("Auction has ended " + msg);
    boolean printHead = true;
    for (Location location : lotsToAuction.values()) {
      if (location.owner == null) {
        if (printHead) {
          logFinest("The following lots were not bought at auction:");
          printHead = false;
        }
        logFinest(location.name);
      }
    }
  }

  /**
   * Sell the Location to the Player for the given Amount.
   * @param aPlayer The player who won the auction.
   * @param aLocation The location that was being auctioned.
   * @param amount The amount that player owes for the location.
   * @throws BankruptcyException If the player does not have amount.
   */
  private void processAuctionResult(AbstractPlayer aPlayer, Location aLocation,
      int amount) throws BankruptcyException 
  {
    aPlayer.getCash(amount);
    logFinest("Player " + aPlayer.playerIndex + " won auction for "
        + aLocation.name);
    aLocation.setOwner(aPlayer);
    aPlayer.addProperty(aLocation);
    PropertyFactory.getPropertyFactory(gamekey).checkForMonopoly();
    if (aLocation.partOfMonopoly) {
      logFinest("Player " + aPlayer.playerIndex + " acquired monopoly with "
          + aLocation.name);
    }
  }

  /**
   * Get the number of houses in the bank.
   * @return The number of houses in the bank.
   */
  public int getNumHouses() {
    return numHouses;
  }

  /**
   * Unpause the game.
   */
  public synchronized void resume() {
    notify();
  }

  /**
   * Release the logger resources and PropertyFactory resource for this game.
   */
  public void endGame() {
    if (fh != null) {
      fh.flush();
      fh.close();
      logger.removeHandler(fh);
      fh = null;
    }
    PropertyFactory.releasePropertyFactory(gamekey);
    logger = null;
  }

  /**
   * Called after a special card instructs the player to pay all other players
   * $50.
   * 
   * @param player
   *          The player that owes the amount to other players.
   * @throws BankruptcyException
   *           If the player does not have enough money to pay other players.
   */
  public void payEachPlayer50(AbstractPlayer player) throws BankruptcyException {
    int numPlayersToPay = 0;
    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        ++numPlayersToPay;
      }
    }

    int amount = numPlayersToPay * 50;
    player.getCash(amount);

    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        p.receiveCash(50);
      }
    }
  }

  /**
   * Called after a special card awards the player $10 from all other players
   * 
   * @param player
   *          The player that receives the amount from other players.
   */
  public void collect10FromAll(AbstractPlayer player) {
    for (AbstractPlayer p : players) {
      if (p != player && !p.bankrupt()) {
        try {
          p.getCash(10);
          player.receiveCash(10);
        } catch (BankruptcyException e) {
          processBankruptcy(p, player);
        }
      }
    }
  }

  public Cards getCards() {
    return cards;
  }

  /**
   * Return the total net worth of all players in the game.
   * 
   * @return The total net worth of all players in the game.
   */
  public int getTotalNetWorth() {
    int result = 0;
    for (AbstractPlayer player : players) {
      result += player.getTotalWorth();
    }
    return result;
  }

  /**
   * Return the number of players in the game that are not bankrupt.
   * 
   * @return The number of players in the game that are not bankrupt.
   */
  public int getNumActivePlayers() {
    return players.length - bankruptCount;
  }

  /**
   * Log information about the most recent dice roll.
   * 
   * @param roll
   *          The values of each dice.
   */
  public void logDiceRoll(int[] roll) {
    logFinest("Dice 1: " + roll[0]);
    logFinest("Dice 2: " + roll[1]);

    if (roll[0] == roll[1]) {
      logFinest("Doubles!!");
    }
  }

  /**
   * Get a reference to the Dice instance for this game.
   * 
   * @return The Dice instance for this game.
   */
  public Dice getDice() {
    return dice;
  }

  @Override
  public String toString() {
    return "Gen: " + generation + "; Match: " + match + "; Game: " + game;
  }
  
  public void logInfo(String s) {
    if (logger != null) {
      logger.info(s);
    }
  }
  
  public void logFinest(String s) {
    if (logger != null) {
      logger.finest(s);
    }
  }
  
  public void logSevere(String s) {
    if (logger != null) {
      logger.severe(s);
    }
  }
}
