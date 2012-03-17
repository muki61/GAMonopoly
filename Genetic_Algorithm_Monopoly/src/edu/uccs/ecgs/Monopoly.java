package cs571.mukhar;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import cs571.mukhar.states.Events;

public class Monopoly implements Runnable {

  static Logger logger = Logger.getLogger("cs571.mukhar");
  static Formatter formatter;
  FileHandler fh;

  private int delayRate;
  private boolean done = false;
  // All the players in a generation
  private Vector<AbstractPlayer> playerPool = new Vector<AbstractPlayer>(Main.maxPlayers);
  private Vector<AbstractPlayer> playersDone = new Vector<AbstractPlayer>(Main.maxPlayers);
  int playerIndex = 0;
  Dice dice = Dice.getDice();
  int turnCounter = 0;
  Random r;
  
  int generation = 0;
  int matches = 0;
  int gameNumber = 0;
  private int minEliteScore;
  private static int bankruptCount;

  private static int numHouses;
  private static int numHotels;

  private static Monopoly _this = new Monopoly();

  private Monopoly() {
    r = new Random();
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    System.out.println("Monopoly seed      : " + seed);
    r.setSeed(seed);
    createPlayers();
  }

  public static Monopoly getMonopolyGame() {
    return _this;
  }

  public void runGame() {
    if (Main.loadFromDisk) {
      generation = Main.lastGeneration + 1;
    }

    while (generation < Main.numGenerations) {
      matches = 0;
      while (matches < Main.numMatches) {
        gameNumber = 0;
        while (!playerPool.isEmpty()) {
          turnCounter = 0;
          PropertyFactory.getPropertyFactory().reset();
          numHouses = 32;
          numHotels = 12;
          logFileSetup();

          // logger.info("Lots at start of game");
          // for (Location location :
          // PropertyFactory.getPropertyFactory().locations) {
          // logger.info(location.toString());
          // logger.info("  owner        : " + location.owner);
          // assert location.owner == null : "Owner not null for " +
          // location.name;
          // logger.info("");
          // }

          runGame2();
          Cards.getCards().resetCards();
          resetPlayers();
          ++gameNumber;
        }
        ++matches;

        playerPool.addAll(playersDone);
        playersDone.removeAllElements();
        initializeAllPlayers();
      }

      dumpGenome();
      dumpPlayerFitness();
      
      Vector<AbstractPlayer> newPopulation = PopulationPropagator.evolve(playerPool, minEliteScore);
      playerPool.clear();
      playerPool.addAll(newPopulation);
      
      generation++;
//      if (generation == 1) {
//        logger.setLevel(Level.OFF);
//      }
    }
  }

  private void resetPlayers() {
    for (AbstractPlayer p : GamePlayers.players) {
      p.clearAllProperties();
    }
  }

  private void initializeAllPlayers() {
    for (AbstractPlayer p : playerPool) {
      p.initCash(1500);
      p.resetAll();
    }
  }

  private void dumpPlayerFitness() {
    //create a map sorted by score, where the value is the number of 
    //players with that score
    TreeMap<Integer, Integer> scores = new TreeMap<Integer, Integer>();
    //the set is used to dump a list of each player with the player's individual score
    ArrayList<AbstractPlayer> fitness = new ArrayList<AbstractPlayer>(Main.maxPlayers);
    
    for (AbstractPlayer player : playerPool) {
      fitness.add(player);

      Integer val = scores.get(player.fitnessScore);
      if (val == null) {
        scores.put(player.fitnessScore, 1);
      } else {
       Integer newVal = val.intValue() + 1; 
       scores.put(player.fitnessScore, newVal);
      }
    }

    //determine the minimum elite score
    minEliteScore = 0;
    int playerCount = 0;
    int maxPlayerCount = (int)(0.1 * Main.maxPlayers);
    for (Integer key : scores.descendingKeySet()) {
      playerCount += scores.get(key).intValue();
      if (playerCount >= maxPlayerCount) {
        break;
      }
      minEliteScore = key.intValue();
    }

    StringBuilder dir = Utility.getDirForGen(generation);

    File file = new File(dir.toString());
    if (!file.exists()) {
      file.mkdir();
    }
    
    //dump the score counts
    BufferedWriter bw = null; 
    try {
      FileWriter fw = new FileWriter(dir.toString() + "/fitness_scores.csv");
      bw = new BufferedWriter(fw); 

      //all scores
      int maxScore = Main.numMatches * 3;
      
      for (int i = 0; i <= maxScore; i++) {
        if (scores.containsKey(i)) {
          bw.write(i + "," + scores.get(i).intValue());
          bw.newLine();
        } else {
          bw.write(i + ",0");
          bw.newLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    Collections.sort(fitness);

    //dump individual fitness scores
    try {
      FileWriter fw = new FileWriter(dir.toString() + "/player_fitness.csv");
      bw = new BufferedWriter(fw);

      for (AbstractPlayer player : fitness) {
        bw.write(player.fitnessScore + "," + player.playerIndex);
        bw.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void dumpGenome() {
    for (AbstractPlayer player : playerPool) {
      StringBuilder fn1 = new StringBuilder(32);
      fn1.append("0000").append(player.playerIndex);
      String fn2 = fn1.reverse().substring(0, 4);
      StringBuilder fn3 = new StringBuilder(fn2).append("rylp").reverse();
      fn3.append(".dat");

      StringBuilder dir = Utility.getDirForGen(generation);
      
      File file = new File(dir.toString());
      if (!file.exists()) {
        file.mkdir();
      }

      DataOutputStream dos = null;
      try {
        FileOutputStream fos = new FileOutputStream(dir.toString() + "/" + fn3.toString());
        dos = new DataOutputStream(fos);
        player.dumpGenome(dos);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (dos != null) {
          try {
            dos.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public void runGame2() {
    done = false;
    getFourPlayers();

    logger.info("Started game " + gameNumber + " with players: ");
    for (AbstractPlayer p : GamePlayers.players) {
      logger.info("Player " + p.playerIndex);
    }

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
      player.reset();

      logger.info("");
      logger.info("Turn: " + turnCounter);
      logger.info("Player " + player.playerIndex);

      Events event = Events.PLAYER_ACTIVATED_EVENT;
      Actions action = Actions.NULL;

      while (action != Actions.DONE) {
        action = player.getNextActionEnum(event);
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
          throw new IllegalArgumentException ("Unhandled action "+
              action);
        }
      }
      
      if (bankruptCount == 3) {
        done = true;
      }
    }
    
    TreeMap<Integer, AbstractPlayer> sortedPlayers = new TreeMap<Integer, AbstractPlayer>();
    for (AbstractPlayer p : GamePlayers.players) {
      if (p.getTotalWorth() == 0) {
        sortedPlayers.put(p.getBankruptIndex(), p);
      } else {
        sortedPlayers.put(p.getTotalWorth(), p);
      }
    }
    
    logger.info('\f'+"GAME OVER");
    
    //In order from loser to winner, add score points to player
    int score = 0;
    for (AbstractPlayer p : sortedPlayers.values()) {
      p.addToScore(score);
      score++;
    }

    for (AbstractPlayer p : sortedPlayers.values()) {
      logger.info("");
      p.printTotalWorth();
    }
  }

  private void getFourPlayers() {
    for (int i = 0; i < GamePlayers.players.length; i++) {
      GamePlayers.players[i] = null;
      AbstractPlayer player = playerPool.remove(r.nextInt(playerPool.size()));
      GamePlayers.players[i] = player;
      playersDone.add(player);
      assert !playerPool.contains(player);
    }
  }

  private AbstractPlayer getNextPlayer() {
    while (GamePlayers.players[playerIndex].bankrupt()) {
      playerIndex = ++playerIndex % 4;
    }

    AbstractPlayer p = GamePlayers.players[playerIndex];
    playerIndex = ++playerIndex % 4;

    return p;
  }

  private void createPlayers() {
    if (Main.loadFromDisk) {
      playerPool.addAll(PopulationPropagator.loadPlayers(Main.lastGeneration));
      Vector<AbstractPlayer> newPopulation = PopulationPropagator.evolve(playerPool, minEliteScore);
      playerPool.clear();
      playerPool.addAll(newPopulation);      
    } else {
      for (int i = 0; i < Main.maxPlayers; i++) {
        AbstractPlayer player = null;
        
        switch (Main.chromoType) {
        case RGA:
          player = new RGAPlayer(i);
          break;
        case SGA:
          player = new SGAPlayer(i);
          break;
        case TGA:
          player = new TGAPlayer(i);
          break;
        }

        player.initCash(1500);
        playerPool.add(player);
      }
    }
  }

  public static void payRent(AbstractPlayer from, AbstractPlayer to, int amount)
      throws BankruptcyException 
  {
    from.getCash(amount);
    to.receiveCash(amount);
  }

  // public void setDelay(int rate) {
  // delayRate = rate * 1000;
  // if (delayRate > 0) {
  // delay = true;
  // } else {
  // delay = false;
  // }
  // }

  public void run() {
    try {
      initLogger();
      runGame();
    } catch (Throwable t) {
      t.printStackTrace();
      logger.log(Level.SEVERE, "", t);
    }
  }

  public void initLogger() {
    // This block configures the logger with handler and formatter
    formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage() + "\n";
      }
    };

    if (Main.debug) {
      logger.setLevel(Level.INFO);
    } else {
      logger.setLevel(Level.OFF);
    }
  }

  public void logFileSetup() {
    if (!Main.debug) {
      return;
    }

    if (fh != null) {
      fh.flush();
      fh.close();
      logger.removeHandler(fh);
    }
    
    StringBuilder dir = Utility.getDirForGen(generation);
    
    File file = new File(dir.toString());
    if (!file.exists()) {
      file.mkdir();
    }
    
    dir.append("/").append(getMatchString());
    file = new File(dir.toString());
    if (!file.exists()) {
      file.mkdir();
    }
    
    StringBuilder fileName = new StringBuilder(getGameString().append(".rtf"));

    try {
      // Handler[] handlers = Logger.getLogger("").getHandlers();
      // for (Handler h : handlers) {
      // if (h.getClass() == java.util.logging.ConsoleHandler.class) {
      // h.setFormatter(formatter);
      // }
      // }

      fh = new FileHandler(dir + "/" + fileName, false);
      logger.addHandler(fh);
      fh.setFormatter(formatter);

    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private StringBuilder getMatchString() {
    StringBuilder result = new StringBuilder("" + matches);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Match_");

    return result;
  }

  private StringBuilder getGameString() {
    StringBuilder result = new StringBuilder("" + gameNumber);

    while (result.length() < 3) {
      result.insert(0, 0);
    }

    result.insert(0, "Game_");

    return result;
  }

  public static void sellHouse(AbstractPlayer player, Location location) {
    location.sellHouse();
    player.receiveCash(location.getHouseCost()/2);
    ++numHouses;
    assert numHouses < 33 : "Invalid number of houses: " + numHouses;
  }

  public static void sellHotel(AbstractPlayer player, Location location, Collection<Location> owned) {
    int numHotelsInGroup = PropertyFactory.getPropertyFactory().getNumHotelsInGroup(location);
    int numHousesInGroup = PropertyFactory.getPropertyFactory().getNumHousesInGroup(location);

    int numHousesToSell1 = 0; //number of houses to sell on 1st property in group
    int numHousesToSell2 = 0; //number of houses to sell on 2nd property in group
    int numHousesToSell3 = 0; //number of houses to sell on 3rd property in group

    switch (numHouses) {
    case 4:
      // 4 houses
    default:
      // more than 4 houses
      location.sellHotel();
      player.receiveCash(location.getHotelCost() / 2);
      ++numHotels;
      assert numHotels <= 12 : "Invalid number of hotels: " + numHotels;
      numHouses = numHouses - 4;
      //return rather than break because we don't want to call the sell method at the
      //end of the case block, since that method sells all hotels.
      return;

    case 3:
      if (numHotelsInGroup == 3) {
        // must sell all hotels and 9 houses and then arrange houses as 1/1/1
        numHousesToSell1 = 3;
        numHousesToSell2 = 3;
        numHousesToSell3 = 3;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 4) {
        // must sell both hotels and then arrange houses as 2/2/3
        numHousesToSell1 = 2;
        numHousesToSell2 = 2;
        numHousesToSell3 = 1;

      } else if (numHotelsInGroup == 2 && numHousesInGroup == 0) {
        // must sell both hotels and then arrange houses as 1/2
        numHousesToSell1 = 3;
        numHousesToSell2 = 2;
        numHousesToSell3 = 4;
        
      } else {
        logger
            .severe("Invalid number of hotels/houses in property group for location "
                + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup);        
        assert false : "Invalid number of hotels/houses in property group for location "
            + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup; 
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
        logger
        .severe("Invalid number of hotels/houses in property group for location "
            + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup);        
        assert false : "Invalid number of hotels/houses in property group for location "
            + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup; 
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
        logger
        .severe("Invalid number of hotels/houses in property group for location "
            + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup);        
        assert false : "Invalid number of hotels/houses in property group for location "
            + location + "; hotels/houses: " + numHotelsInGroup + "/" + numHousesInGroup; 
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

  //change the location of the number of houses to sell based on the property group
  private static void swapHousesToSell(Location location, int[] numHousesToSell) {
    switch (location.getGroup()) {
    case PURPLE:
    case RED:
    case YELLOW:
    case GREEN:
      //for these properties, ensure first property has any 2nd extra house
      if (numHousesToSell[0] < numHousesToSell[1]) {
        int temp = numHousesToSell[1];
        numHousesToSell[1] = numHousesToSell[0];
        numHousesToSell[0] = temp;
      }
      break;
    default:
      //for everything else, no change
    }
  }

  private static void sell(AbstractPlayer player,
                           Location location,
                           Collection<Location> owned,
                           int numHousesToSell1,
                           int numHousesToSell2,
                           int numHousesToSell3) 
  {
    
    //change the order of the houses to sell based on property group
    swapHousesToSell(location, new int[] {numHousesToSell1, numHousesToSell2, numHousesToSell3});

    int count = 0;

    for (Location loc : owned) {
      if (loc.getGroup() == location.getGroup()) {
        ++count;        

        loc.sellHotel();
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
          loc.sellHouse();
          //in this method, don't increment the number of houses
          //because the houses at the location are virtual houses 
          player.receiveCash(loc.getHouseCost() / 2);
        }
      }
    }
  }

  public static void buyHouse(AbstractPlayer player, Location location) {
    if (numHouses == 0) {
      logger.info("Player " + player.playerIndex + " wanted to buy house, but none are available");
      return;
    }
    
    try {
      assert player.canRaiseCash(location.getHouseCost()) : "Player tried to buy house with insufficient cash";
      player.getCash(location.getHouseCost());
      location.addHouse();
      --numHouses;
      assert numHouses >= 0 : "Invalid number of houses: " + numHouses;
      logger.info("Bank now has " + numHouses + " houses");
    } catch (BankruptcyException ignored) {
      // expect that any player that buys a house first verifies they
      // have enough cash
      ignored.printStackTrace();
    }
  }

  public static void buyHotel(AbstractPlayer player, Location location) {
    try {
      assert player.canRaiseCash(location.getHotelCost()) : "Player tried to buy house with insufficient cash";
      location.addHotel();
      --numHotels;
      player.getCash(location.getHotelCost());
      assert numHotels >= 0 : "Invalid number of hotels: " + numHotels;

      //add the houses back to the bank
      numHouses += 4;
      assert numHouses < 33 : "Invalid number of houses: " + numHouses;
    } catch (BankruptcyException ignored) {
      // expect that any player that buys a house first verifies they
      // have enough cash
      ignored.printStackTrace();
    }
  }

  public static void processBankruptcy(AbstractPlayer player, AbstractPlayer gainingPlayer) {
    
    logger.info("Player " + player.playerIndex + " is bankrupt");
    player.setBankruptIndex(bankruptCount);
    ++bankruptCount;

    boolean gameOver = false;
    if (bankruptCount == 3) {
      gameOver = true;
    }

    if (gainingPlayer != null) {
      logger.info("Gaining player is " + gainingPlayer.playerIndex);
    } else {
      logger.info("Gaining player is bank");
    }

    //return any get out of jail cards to the stack
    while (player.hasGetOutOfJailCard()) {
      player.useGetOutOfJailCard();
    }

    logger.info("Bankrupt count: " + bankruptCount);

    if (gainingPlayer == null) {
      player.getAllCash();

      if (!gameOver) {
        // remove houses and hotels from lots and remove mortgage
        for (Location lot : player.getAllProperties().values()) {
          numHotels += lot.getNumHotels();
          lot.resetNumHotels();

          numHouses += lot.getNumHouses();
          lot.resetNumHouses();

          lot.setMortgaged(false);
        }

        auctionLots(player.getAllProperties());
      }

      player.clearAllProperties();

    } else {
      player.sellAllHousesHotels();

      //give all cash to gaining player
      gainingPlayer.receiveCash(player.getAllCash());

      //give all property
      //mortgaged properties are handled in the addProperties method
      gainingPlayer.addProperties(player.getAllProperties(), gameOver);

      player.clearAllProperties();
    }

    player.setBankrupt();
  }

  public static void auctionLots(TreeMap<Integer, Location> lotsToAuction) {
    // logger.setLevel(Level.INFO);

    //set owner to null for all lots
    for (Location location : lotsToAuction.values()) {
      location.owner = null;
    }

    String msg = "";

    for (Location location : lotsToAuction.values()) {
      logger.info("Bank is auctioning " + location.name);

      int highBid = 0;
      AbstractPlayer highBidPlayer = null;
      int secondHighestBid = 0;

      for (AbstractPlayer p : GamePlayers.players) {
        int bid = p.getBidForLocation(location);
        logger.info("Player " + p.playerIndex + " has " + p.cash + " dollars and bids " + bid);

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
      assert highBidPlayer.canRaiseCash(finalBid) : "Player " + highBidPlayer.playerIndex +
                                                    " cannot raise cash " + finalBid;

      try {
        processAuction(highBidPlayer, location, finalBid);
      } catch (BankruptcyException e) {
        // assume player cannot win auction unless they have enough cash
        e.printStackTrace();
      }      
    }
    
    logger.info("Auction has ended " + msg);
    boolean printHead = true;
    for (Location location : lotsToAuction.values()) {
      if (location.owner == null) {
        if (printHead) {
          logger.info("The following lots were returned to the bank:");
          printHead = false;
        }
        logger.info(location.name);
      }
    }
    // logger.setLevel(Level.OFF);
  }

  private static void processAuction(AbstractPlayer aPlayer, Location aLocation,
      int amount) throws BankruptcyException {
    aPlayer.getCash(amount);
    logger.fine("Player " + aPlayer.playerIndex + " won auction for " + aLocation.name);
    aLocation.setOwner(aPlayer);
    aPlayer.addProperty(aLocation);
    PropertyFactory.getPropertyFactory().checkForMonopoly();
    if (aLocation.partOfMonopoly) {
      logger.info("Player " + aPlayer.playerIndex + " acquired monopoly with "
          + aLocation.name);
    }
  }

  public static int getNumHouses() {
    return numHouses;
  }

  public synchronized void unpause() {
    notify();
  }
}
