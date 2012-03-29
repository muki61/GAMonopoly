package edu.uccs.ecgs.ga;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import edu.uccs.ecgs.states.Events;
import edu.uccs.ecgs.states.PlayerState;

public abstract class AbstractPlayer 
  implements Comparable<AbstractPlayer>, Cloneable
{
  public int cash;
  private boolean rolledDoubles = false;
  
  // Set to 3 when entering jail, must leave when count reaches 0
  protected int jailSentence = 0;

  private boolean passedGo = false;
  private int locationIndex = 0;
  protected Location location;
  public int playerIndex;
  public int initialGeneration;

  public GameState currentState = GameState.INACTIVE;

  public Actions nextAction = Actions.NULL;

  PlayerState playerState = PlayerState.inactiveState;

  //build in this order
  // 1 Orange
  // 2 Light Blue
  // 3 Red
  // 4 Purple
  // 5 Dark Blue
  // 6 Yellow
  // 8 Green
  // 9 Brown
  private static final PropertyGroups[] groupOrder = new PropertyGroups[] 
      {PropertyGroups.ORANGE, PropertyGroups.LIGHT_BLUE,
      PropertyGroups.RED, PropertyGroups.PURPLE, 
      PropertyGroups.DARK_BLUE, PropertyGroups.YELLOW,
      PropertyGroups.GREEN, PropertyGroups.BROWN};
  
  public static Random r = new Random();
  static {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);
  }

  private TreeMap<Integer, Location> owned;
  public boolean inJail = false;
  private Chance chanceGOOJ;
  private CommunityChest ccGOOJ;
  int fitnessScore = 0;
  
  private boolean isBankrupt = false;
  private int bankruptIndex = 0;
  protected Monopoly game;

  public AbstractPlayer(int index) {
    playerIndex = index;
    owned = new TreeMap<Integer, Location>();
    clearAllProperties();
    cash = 1500;
  }

  public void clearAllProperties() {
    if (owned != null) {
      owned.clear();
    }
  }

  public boolean hasAtLeastCash(int amount) {
    if (cash >= amount) {
      return true;
    }

    return false;
  }

  public void initCash(int amount) {
    cash = amount;
  }

  public void reset() {
    rolledDoubles = false;
    assert game != null : "Found null game reference";
  }
  
  public void resetAll() {
    cash = 1500;
    rolledDoubles = false;
    jailSentence = 0;

    passedGo = false;
    locationIndex = 0;

    currentState = GameState.INACTIVE;

    nextAction = Actions.NULL;

    playerState = PlayerState.inactiveState;

    inJail = false;
    chanceGOOJ = null;
    ccGOOJ = null;

    isBankrupt = false;
    bankruptIndex = 0;
  }

  public void setInactive() {
    setNewState(GameState.INACTIVE);
  }

  public Actions getNextActionEnum(Events event) {
    playerState = playerState.processEvent(game, this, event);
    return nextAction;
  }

  public void setNewState(GameState gameState) {
    currentState = gameState;
  }

  public void setDoubles(boolean rolledDoubles) {
    this.rolledDoubles = rolledDoubles;
    if (inJail && !rolledDoubles) {
      --jailSentence;
      
      logInfo("Player " + playerIndex + " jailSentence: " + jailSentence);
      assert jailSentence>=0 : "Illegal jailSentence value: " + jailSentence;
    }
  }

  public int advance(int numSpaces) {
    passedGo = false;
    locationIndex += numSpaces;
    if (locationIndex >= 40) {
      locationIndex -= 40;
      passedGo = true;

      if (locationIndex == 0) {
        logInfo("Player " + playerIndex + " landed on Go");
      } else {
        logInfo("Player " + playerIndex + " passed Go");
      }
    }
    return locationIndex;
  }

  public int getLocationIndex() {
    return locationIndex;
  }

  public void setCurrentLocation(Location location) {
    this.location = location;

    if (location.name.equals("Jail")) {
      logInfo("Player " + playerIndex + " landed on " + location.name);
      if (inJail) {
        logInfo("Player " + playerIndex + " is in Jail");
        logInfo("Player sentence: " + jailSentence);
        logInfo("Player inJail flag: " + inJail);
        assert inJail : "Flag inJail is not valid";
        assert jailSentence == 3 : "JailSentence value is not correct";
      } else {
        logInfo("Player " + playerIndex + " is Just Visiting");
      }
    } else {
      logInfo("Player " + playerIndex + " landed on " + location.name);
    }
  }

  public boolean passedGo() {
    return passedGo;
  }

  public void receiveCash(int amount) {
    try {
      cash += amount;
      logInfo("Player " + playerIndex + " received " + amount
          + " dollars.");
      logInfo("Player " + playerIndex + " has " + cash + " dollars.");
    } catch (RuntimeException e) {
      Throwable t = new Throwable(game.toString(), e);
      t.printStackTrace();
    }
  }

  public void getCash(int amount) throws BankruptcyException {
    raiseCash(amount);
    cash = cash - amount;
    logInfo("Player " + playerIndex + " paid " + amount + " dollars.");
    logInfo("Player " + playerIndex + " has " + cash + " dollars.");
  }

  public int getNumRailroads() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.type.equals("railroad")) {
        ++count;
      }
    }
    return count;
  }

  public int getNumUtilities() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.type.equals("utility")) {
        ++count;
      }
    }
    return count;
  }

  public void addProperty(Location location2) {
    owned.put(location2.index, location2);
  }

  public void setLocationIndex(int index) {
    locationIndex = index;
  }

  public boolean inJail() {
    return inJail;
  }

  public Location getCurrentLocation() {
    return location;
  }

  public boolean rolledDoubles() {
    return rolledDoubles;
  }

  public boolean hasGetOutOfJailCard() {
    return chanceGOOJ != null || ccGOOJ != null;
  }

  public void useGetOutOfJailCard() {
    if (chanceGOOJ != null) {
      game.getCards().returnChanceGetOutOfJail();
      chanceGOOJ = null;
    } else if (ccGOOJ != null) {
      game.getCards().returnCCGetOutOfJail();
      ccGOOJ = null;
    } else {
      throw new IllegalArgumentException("Illegal attempt to use Get Out Of Jail Card");
    }
  }
  
  public int getTotalWorth() {
    int totalWorth = cash;

    for (Location location : owned.values()) {
      totalWorth += location.getNumHouses() * location.getHouseCost();
      
      if (location.getNumHotels() != 0) {
        // for hotels, add the cost of the 4 houses that had to be built
        // before the hotel was built
        totalWorth += (location.getHotelCost() * 5);
      }
      
      if (location.isMortgaged()) {
        totalWorth += location.getCost()/2;
      } else {
        totalWorth += location.getCost();        
      }
    }

    return totalWorth;
  }
  
  public void addToScore(int amount) {
    fitnessScore += amount;
  }
  
  public boolean bankrupt() {
    return isBankrupt ;
  }

  public void setGetOutOfJail(Chance chanceJailCard) {
    chanceGOOJ = chanceJailCard;
  }
  
  public void setGetOutOfJail(CommunityChest ccJailCard) {
    ccGOOJ = ccJailCard;
  }

  /**
   * Return the number of houses that have been bought for all properties that
   * are owned by this player
   * 
   * @return The number of houses owned by this player
   */
  public int getNumHouses() {
    int result = 0;
    
    for (Location loc : owned.values()) {
      result += loc.getNumHouses();
    }

    return result;
  }

  /**
   * Return the number of hotels that have been bought for all properties that
   * are owned by this player
   * 
   * @return The number of hotels owned by this player.
   */
  public int getNumHotels() {
    int result = 0;
    
    for (Location loc : owned.values()) {
      result += loc.getNumHotels();
    }

    return result;
  }

  /**
   * Calculate whether the player has or can sell enough things to have at least
   * the cash given by amount. If the player's cash is already greater than
   * amount then the method simply returns true. if the player's current cash is
   * less than amount, then the method determines if the player can sell enough
   * houses, hotels, and mortgage properties to have cash greater than or equal
   * to amount. This method does not actually sell any houses, hotels, or
   * properties; it just computes how much cash could be raised if the player
   * sold everything.
   * 
   * @param amount
   *          The amount the player needs to have in cash
   * @return True if the player has or can sell stuff to raise cash greater than
   *         amount.
   */
  public boolean canRaiseCash (int amount) {
    int totalWorth = cash;
    
    if (totalWorth >= amount) {
      return true;
    }

    for (Location location : owned.values()) {
      // add selling price for all houses
      totalWorth += location.getNumHouses() * location.getHouseCost() / 2;
      // add selling price for all hotels (hotels == 5 houses)
      totalWorth += location.getNumHotels() * 5 * location.getHotelCost() / 2;
      // add cash for mortgaging any unmortgaged properties
      if (!location.isMortgaged()) {
        totalWorth += location.getCost() / 2;
      }
    }
    
    if (totalWorth >= amount) {
      return true;
    }
    
    return false;
  }

  /**
   * Ask if the player wishes to pay bail to get out of jail. This method must
   * be implemented by subclasses.
   * 
   * @return True --> player wishes to pay bail<br>
   *         False --> player wishes to attempt to roll doubles
   */
  public abstract boolean payBailP();

  /**
   * Ask if the player wants to buy the location where they current are. This
   * method must be implemented by subclasses
   * 
   * @return True --> If the player wants to buy their current location
   *         False --> If the player does not want to buy the property at
   *                   their current location.
   */
  public abstract boolean buyProperty();

  /**
   * Ask if the player wants to buy the location. This method must be
   * implemented by subclasses
   * 
   * @return True --> If the player wants to buy the given location 
   *         False --> If the player does not want to buy the given location.
   */
  public abstract boolean buyProperty(Location location);

  /**
   * Output the player genome to a data file.
   * 
   * @param out The output stream to which data should be written.
   * @throws IOException If there is a problem writing out the data.
   */
  public abstract void dumpGenome(DataOutputStream out) throws IOException;

  /**
   * Output the player's total worth to the debug log.
   */
  public void printTotalWorth() {
    logInfo("Player " + playerIndex);
    logInfo("Total cash: " + cash);
    logInfo("Net worth : " + getTotalWorth());
    logInfo("Fitness   : " + fitnessScore);

    if (!isBankrupt) {
      logInfo("Has monopoly: " + hasMonopoly());
      logInfo("Properties owned: ");

      for (Location location : owned.values()) {
        if (location.getNumHouses() > 0) {
          logInfo(location.name + " (" + location.getNumHouses() + " houses)");
        } else {
          logInfo(location.name);
        }
      }
    }
  }

  /**
   * Determine the amount that this player wants to bid in an auction for the
   * given location. A player can bid on a property in an auction even if the
   * player just decided not to buy it directly after landing on the property.
   * 
   * @param currentLocation
   *          The property being auctioned.
   * @return The amount of this player's bid.
   */
  public int getBidForLocation(Location currentLocation) {
    int bid = 0;

    if (cash < 50) {
      bid = 0;
    } else if (buyProperty(currentLocation)) {
      //player wants to buy, so start with current cost
      bid = currentLocation.getCost();
      
      double adjustFactor = Math.abs(r.nextGaussian());
      adjustFactor = adjustFactor * (double)(bid / 10);
      bid += (int) adjustFactor;
    } else {
      // otherwise, player does not want location
      if (currentLocation == location) {
        //if player is the one at the location, then bid some small
        //amount (cost/2 or cost/3 or cost/4)
        int factor = r.nextInt(3) + 2; //factor is 2,3,4
        bid = currentLocation.getCost()/factor;
      } else {
        //otherwise, other players bid half cost 
        //plus some random fluctuation
        bid = (currentLocation.getCost() / 2)
            + (int) (Math.abs(r.nextGaussian()) * (double)(currentLocation.getCost() / 6));
      }
    }
    
    //ensure bid does not exceed cash
    if (bid > cash) {
      bid = cash;
    }

    assert bid >= 0 : "Invalid bid amount: " + bid;
    //ensure some minimum bid (cost/4, cost/5 or cost/6)
    // if (bid <= 0) {
    // int factor = r.nextInt(3) + 4; //factor is 4,5,6
    // bid = currentLocation.getCost()/factor;
    //      
    // if (bid > cash) {
    // bid = cash;
    // }
    // }

    return bid;
  }

  /**
   * Called by game if the player lands in jail either through rolling doubles
   * three times, getting a Go To Jail card, or landing on the Go To Jail
   * location.  
   */
  public void enteredJail() {
    inJail = true;
    jailSentence = 3;
  }

  /**
   * Ask whether player must leave jail or not. Player must leave jail when they
   * have declined to pay bail three times and have had 3 chances to roll
   * doubles but have not rolled doubles.
   * 
   * @return True if the player must pay bail and leave jail, false otherwise.
   */
  public boolean jailSentenceCompleted() {
    return jailSentence == 0;
  }

  public void paidBail() {
    inJail = false;
    jailSentence = 0;
  }

  /**
   * TODO Even if the player has all the properties of a color group, they 
   * cannot build houses if one or more properties are mortgaged.
   * @return True if the player has at least one monopoly, false otherwise.
   */
  public boolean hasMonopoly() {
    boolean result = false;
    for (Location l : owned.values()) {
      if (l.partOfMonopoly) {
        result = true;
        break;
      }
    }
    
    return result;
  }

  /**
   * Attempt to sell houses and hotels, and mortgage properties until the
   * player's cash is greater than or equal to amount.
   * 
   * @param amount
   *          The amount of cash that the player is trying to have on hand.
   * @throws BankruptcyException
   *           If the player cannot raise enough cash to equal or exceed amount.
   */
  public void raiseCash(int amount) throws BankruptcyException {
    logInfo("Player " + playerIndex + " has " + cash + " dollars");
    if (cash >= amount) {
      return;
    }
    
    if (canRaiseCash(amount)) {
      logInfo("Player " + playerIndex + " attempting to raise " + amount
          + " dollars");
      for (Location l : owned.values()) {
        //mortgage single properties first
        if (!l.partOfMonopoly
            && !l.isMortgaged()
            && l.getGroup() != PropertyGroups.UTILITIES
            && l.getGroup() != PropertyGroups.RAILROADS) 
        {
          // mortgage property if not part of monopoly
          logInfo("Player will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }

      // then mortgage single utilities
      if (getNumUtilities() == 1) {
        for (Location l : owned.values()) {
          if (l.getGroup() == PropertyGroups.UTILITIES && !l.isMortgaged()) 
          {
            logInfo("Player will mortgage " + l.name);
            l.setMortgaged();
            receiveCash(l.getCost() / 2);
          }
        }
        if (cash >= amount) {
          return;
        }
      }

      // then mortgage railroads
      // sell in order 1,4,2,3 or location index 5,35,15,25
      int[] index = new int[] { 5, 35, 15, 25 };
      for (int i = 0; i < index.length; i++) {
        if (owned.containsKey(index[i])) {
          Location l = owned.get(index[i]);
          if (!l.isMortgaged) {
            logInfo("Player will mortgage " + l.name);
            l.setMortgaged();
            receiveCash(l.getCost() / 2);
          }
        }
        if (cash >= amount) {
          return;
        }
      }

      // then mortgage utility monopolies
      for (Location l : owned.values()) {
        if (l.getGroup() == PropertyGroups.UTILITIES && !l.isMortgaged()) 
        {
          logInfo("Player will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }

      // sell hotels
      for (Location l : owned.values()) {
        if (l.getNumHotels() > 0) {
          game.sellHotel(this, l, owned.values());
        }
        if (cash >= amount) {
          return;
        }
      }

      // then sell houses
      while (cash < amount) {
        int maxHouses = 0;
        Location locMaxHouses = null;

        for (Location l : owned.values()) {
          // find property with greatest number of houses
          if (l.getNumHouses() > maxHouses) {
            maxHouses = l.getNumHouses();
            locMaxHouses = l;
          }
        }
        
        if (maxHouses > 0) {
          logInfo(locMaxHouses.name + " has " + locMaxHouses.getNumHouses() + " houses");
          logInfo("Will sell house at " + locMaxHouses.name);
          game.sellHouse(locMaxHouses);
        } else {
          break;
        }
        
        if (cash >= amount) {
          return;
        }
      }

      //then mortgage any remaining unmortgaged lots
      for (Location l : owned.values()) {
        if (!l.isMortgaged()) 
        {
          logInfo("Player will mortgage " + l.name);
          l.setMortgaged();
          receiveCash(l.getCost() / 2);
        }
        if (cash >= amount) {
          return;
        }
      }
    }

    //don't have cash and can't raise cash
    throw new BankruptcyException();
  }

  public TreeMap<Integer, Location> getAllProperties() {
    return owned;
  }

  /**
   * Add properties from a bankrupt player to this player
   * 
   * @param allProperties
   *          All the properties owned by a player who has gone bankrupt
   * @param gameOver
   *          Whether the game is over because the bankrupt player is the last
   *          other player in the game
   */
  public void addProperties(TreeMap<Integer, Location> allProperties, 
                            boolean gameOver) 
  {
    //add all properties first
    for (Location l : allProperties.values()) {
      owned.put(l.index, l);
      l.owner = this;
    }

    // mark all the properties that are part of monopolies 
    PropertyFactory.getPropertyFactory(game.gamekey).checkForMonopoly();
  
    // if the game isn't over, then the gaining player needs to pay the
    // 10% fee on any mortgaged properties, and possible unmortgage the 
    // properties
    if (!gameOver) {
      processMortgagedNewProperties(allProperties);
    }
  }

  /**
   * Go through all the new properties gained by this player, and for the ones that are
   * mortgaged, pay the fee and then determine whether or not to unmortgage the property
   * @param newProperties
   */
  private void processMortgagedNewProperties(TreeMap<Integer, Location> newProperties) {
    Vector<Location> mortgaged = new Vector<Location>();

    // want to handle mortgages of added properties in this order:
    // properties that are part of monopolies
    for (Location lot : newProperties.values()) {
      if (lot.partOfMonopoly && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }
        
    // utility monopolies
    int countUtilities = getNumUtilities();
    
    //add utilities if player has 2 utilities
    if (countUtilities == 2) {
      for (Location lot : newProperties.values()) {
        if (lot.getGroup() == PropertyGroups.UTILITIES && lot.isMortgaged()) {
          mortgaged.add(lot);
        }
      }
    }
    
    // add railroads
    for (Location lot : newProperties.values()) {
      if (lot.getGroup() == PropertyGroups.RAILROADS && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }
    
    // add single utilities
    if (countUtilities == 1) {
      for (Location lot : newProperties.values()) {
        if (lot.getGroup() == PropertyGroups.UTILITIES && lot.isMortgaged()) {
          mortgaged.add(lot);
        }
      }
    }

    //lots that are not part of monopolies
    for (Location lot : newProperties.values()) {
      if (!lot.partOfMonopoly && lot.getGroup() != PropertyGroups.RAILROADS
          && lot.getGroup() != PropertyGroups.UTILITIES && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }    
    
    processMortgagedLots(mortgaged);

    for (Location lot : mortgaged) {
      if (lot.isMortgaged()) {
        // leave property mortgaged and only pay the fee
        // TODO, what if player wants to unmortgage? Need to handle
        int amountToPay = (int) (0.1 * lot.getCost() / 2);
        logInfo("Player " + playerIndex + 
                    " will only pay mortgage fee for " + lot.name + 
                    "; fee is " + amountToPay);

        try {
          getCash(amountToPay);
        } catch (BankruptcyException e) {
          //ignored exception
          //player is in this method because some other player
          //went bankrupt, so assume this player has enough cash
          //or can raise enough cash
          // TODO assumption is flawed, need to fix
          Throwable t = new Throwable(game.toString(), e);
          t.printStackTrace();
        }
      }
    }
  }

  /**
   * Create a list of all mortgaged properties and decide whether or not to pay
   * them off.
   */
  public void payOffMortgages() {
    Vector<Location> mortgaged = new Vector<Location>();
    for (Location lot : owned.values()) {
      if (lot.isMortgaged()) {
        logInfo(lot.name + " is mortgaged");
        mortgaged.add(lot);
      }
    }
    if (mortgaged.size() > 0) {
      processMortgagedLots(mortgaged);
    }
  }

  /**
   * Actually does the work of paying off the mortgages in the list created by
   * payOffMortgages().
   * 
   * @param mortgaged
   *          A list of mortgaged properties owned by the player.
   */
  private void processMortgagedLots(Vector<Location> mortgaged) {
    // only mortgage if other monopolies have been developed
    for (Location location : owned.values()) {
      boolean locationFullyBuilt = (location.getNumHouses() == 3 || 
                                    (location.getNumHouses() == 0 && 
                                     location.getNumHotels() == 1));

      if ( location.partOfMonopoly && 
          !location.isMortgaged && 
          !locationFullyBuilt) 
      {
        return;
      }
    }

    int count = 0;
    
    for (Location lot : mortgaged) {
      int amountToPay = 0;

      if (payMortgageP(lot)) {
        // pay off mortgage
        amountToPay = (int) (1.1 * lot.getCost() / 2);
        logInfo("Player will pay off mortgage for " + lot.name + "; cost is "
            + amountToPay);
        try {
          getCash(amountToPay);
          lot.setMortgaged(false);
          logInfo(lot.name + " is no longer mortgaged");
          ++count;
        } catch (BankruptcyException e) {
          // the player will not decide to pay mortgage unless
          // enough free cash is available
          // TODO
          Throwable t = new Throwable(game.toString(), e);
          t.printStackTrace();
        }
      }
    }
    logInfo(count + " mortgaged lots were paid off; " + 
        (mortgaged.size()-count) + " lots are still mortgaged");
  }

  /**
   * make the decision of whether or not to pay the mortgage on the property
   * @param lot
   * @return True if the player can pay the mortgage, False otherwise
   */
  private boolean payMortgageP(Location lot) {
    int cost = (int) (1.1 * lot.getCost() / 2); 

    //After paying cost, player should still have minimum cash, so
    //if player's current cash is less than minimum cash + cost,
    //then player should not pay mortgage.
    if (cash < getMinimumCash() + cost) {
      return false;
    }
    
    return true;
  }

  /**
   * Compute the minimum amount of cash the player should have on hand based on
   * current game conditions.
   * 
   * @return
   */
  private int getMinimumCash() {
    //Frayn: Keep a minimum of 200 pounds (dollars) in cash,
    int result = 200;
    
    //plus 1% of the total and average opponent net worth,
    int totalnet = game.getTotalNetWorth();
    totalnet -= getTotalWorth();
    
    int count = game.getNumActivePlayers() - 1;
    
    int avgnet = totalnet / count;
    result += (int) (totalnet * 0.01);
    result += (int) (avgnet * 0.01);

    //plus 5% of the number of houses or hotels. (assume frayn meant 5% of cost)
    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    for (int i = 0; i < 40; i++) {
      Location l = pf.getLocationAt(i);
      if (l.getNumHouses() > 0 && l.owner != this) {
        result += (int) (l.getNumHouses() * l.getHouseCost() * 0.05);
      }
      if (l.getNumHotels() > 0 && l.owner != this) {
        //multiply by 5 because hotels cost is hotelCost + 4 houses
        result += (int) (l.getNumHotels() * l.getHotelCost() * 0.05) * 5;
      }
    }
    
    return result;
  }

  public int getAllCash() {
    int amount = cash;
    cash = 0;
    return amount;
  }

  public void setBankrupt() {
    isBankrupt = true;
  }

  /**
   * Sell all the hotels and houses owned by the player
   */
  public void sellAllHousesAndHotels() {
    // Sell all hotels first
    for (Location l : owned.values()) {
      if (l.getNumHotels() > 0) {
        game.sellHotel(this, l, owned.values());
      }
      while (l.getNumHouses() > 0) {
        game.sellHouse(l);
      }
    }

    // And sell all houses
    for (Location l : owned.values()) {
      while (l.getNumHouses() > 0) {
        game.sellHouse(l);
      }
    }
  }

  /**
   * Attempt to buy a house for a property
   */
  public void processDevelopHouseEvent() {
    // Bank has to have houses available
    if (game.getNumHouses() == 0) {
      logInfo("Bank has no more houses");
      return;
    }
      
    //Player has to have a monopoly
    if (!hasMonopoly()) {
      logInfo("Player does not have monopoly");
      return;
    }
    
    logInfo("Player has " + cash + " dollars");
    int minCash = getMinimumCash();
    logInfo("Player minimum cash is " + minCash);

    //TODO can player raise cash?
    if (cash < minCash) {
      logInfo("Player does not have minimum cash");
      return;
    }

    //Create a list of all the properties that the player owns that are also
    //part of monopolies
    Vector<Location> monopolies = new Vector<Location>();
    for (Location location : owned.values()) {
      if (location.partOfMonopoly) {
        monopolies.add(location);
      }
    }

    //TODO Need to process monopolies data structure to remove any groups that have
    //mortgaged properties.
    
    for (int i = 0; i < groupOrder.length; i++) {
      for (Location location : monopolies) {
        if (location.getGroup() != groupOrder[i]) {
          continue;
        }
        
        logInfo("Checking " + location.name + " for build decision");
        
        if (location.getNumHotels() > 0) {
          logInfo("Location " + location.name + " has a hotel; nothing to build");
        }

        logInfo("Location " + location.name + " has " + location.getNumHouses() + "houses");
        // At this point, location is part of a monopoly and the player might
        // want to build on the property.
        //
        // But good strategy says to build all properties up to 3 houses first,
        // and then if all monopolies of player have 3 houses, then build hotels.
        //
        // So while location has less than three houses, build on it
        while (location.getNumHouses() < 3) {
          // at this point location is part of monopoly
          // correct group
          // not enough houses/hotels
          // now check that there is enough cash to buy a house
          if (cash < (getMinimumCash() + location.getHouseCost())) {
            logInfo("Player does not have " + location.getHouseCost()
                + " dollars extra to buy house for " + location.name);
            break;
          }

          game.buyHouse(this, location);
          balanceHouses(monopolies, location.getGroup());

          if (game.getNumHouses() == 0) {
            return;
          }
        }
      }
    }
  }

  private void balanceHouses(Vector<Location> monopolies, PropertyGroups group) {
    Vector<Location> lots = new Vector<Location>();
    int houseCount = 0;

    for (Location location : monopolies) {
      if (location.getGroup() == group) {
        lots.add(location);
        houseCount += location.getNumHouses();
        location.resetNumHouses();
      }
    }

    for (int i = 0; i < lots.size() - 1; i++) {
      assert lots.elementAt(i).index < lots.elementAt(i+1).index : "Lot order is invalid";
    }

    while (houseCount >= lots.size()) {
      for (Location location : lots) {
        location.assignHouse();
        --houseCount;
      }
    }

    if (houseCount > 0) {
      switch (group) {
      case PURPLE:
      case RED:
      case YELLOW:
      case GREEN:
        assert lots.size() == 3 : "Bad lot size: " + lots.elementAt(0).toString() + "/" + lots.elementAt(1).toString();
        try {
        // extra houses on third and first property
        lots.elementAt(2).assignHouse();
        } catch (ArrayIndexOutOfBoundsException e) {
          for (Location location : lots) {
            System.out.println(location.toString());
          }
        }
        --houseCount;

        if (houseCount == 1) {
          lots.elementAt(0).assignHouse();
          --houseCount;
        }
        break;

      case LIGHT_BLUE:
      case ORANGE:
        assert lots.size() == 3 : "Bad lot size: " + lots.elementAt(0).toString() + "/" + lots.elementAt(1).toString();
        // extra houses on third and second property
        lots.elementAt(2).assignHouse();
        --houseCount;

        if (houseCount == 1) {
          lots.elementAt(1).assignHouse();
          --houseCount;
        }
        break;

      case BROWN:
      case DARK_BLUE:
        assert lots.size() == 2 : "Bad lot size: " + lots.elementAt(0).toString();
        lots.elementAt(1).assignHouse();
        --houseCount;
        break;
      }
    }
    
    for (Location location : lots) {
      logInfo(location.name + " has " + location.getNumHouses() + " houses");
    }
  }

  public Integer getBankruptIndex() {
    return Integer.valueOf(bankruptIndex);
  }
  
  public void setBankruptIndex(int index) {
    bankruptIndex = index;
  }

  public abstract void printGenome();

  public int compareTo(AbstractPlayer arg0) {
    return Integer.valueOf(fitnessScore).compareTo(Integer.valueOf(arg0.fitnessScore));
  }

  public void setIndex(int index) {
    playerIndex = index;    
  }

  public abstract AbstractPlayer[] createChildren(AbstractPlayer parent2, int index);
  
  public String toString() {
    String result = "Player " + playerIndex +
                    "\n  cash: " + cash +
                    "\n  Lots owned: ";

    for (Location lot : owned.values()) {
      result += "\n    " + lot.name;
      if (lot.isMortgaged()) {
        result += " (mortgaged)";
      }
      if (lot.getNumHouses() > 0) {
        result += " (" + lot.getNumHouses() + ")";
      }
    }

    return result;
  }

  public abstract AbstractPlayer copyAndMutate();

  @Override
  protected abstract Object clone() throws CloneNotSupportedException;

  public void joinGame(Monopoly game) {
    this.game = game;
    location = PropertyFactory.getPropertyFactory(game.gamekey).getLocationAt(locationIndex);
  }
  
  private void logInfo(String s) {
    Logger.getLogger(game.gamekey).info(s);
  }
}
