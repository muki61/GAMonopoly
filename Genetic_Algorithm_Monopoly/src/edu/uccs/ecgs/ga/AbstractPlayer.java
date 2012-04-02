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
  
  public Random r = new Random();

  private TreeMap<Integer, Location> owned;
  public boolean inJail = false;
  private Chance chanceGOOJ;
  private CommunityChest ccGOOJ;
  int fitnessScore = 0;
  
  private boolean isBankrupt = false;
  private int bankruptIndex = 0;
  protected Monopoly game;

  public AbstractPlayer(int index) {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);

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
  
  /**
   * Reset the player to the default state, ready to play a game. This resets cash to 1500,
   * removes all properties, resets location, resets bankruptcy state, etc.
   */
  public void resetAll() {
    logInfo("Player " + playerIndex + " entering resetAll()");
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

    clearAllProperties();
  }

  /**
   * Set player's state to inactive.
   */
  public void setInactive() {
    setNewState(GameState.INACTIVE);
  }

  public Actions getNextActionEnum(Events event) {
    playerState = playerState.processEvent(game, this, event);
    return nextAction;
  }

  /**
   * Set player's state to gameState.
   * @param gameState State in which player is.
   */
  public void setNewState(GameState gameState) {
    currentState = gameState;
  }

  /**
   * Set the player's rolledDoubles flag to the input parameter. If the player
   * is in jail and the parameter is false (player did not roll doubles), this
   * method reduces the jail term counter.
   * 
   * @param rolledDoubles True if the player rolled doubles, false otherwise.
   */
  public void setDoubles(boolean rolledDoubles) {
    this.rolledDoubles = rolledDoubles;
    if (inJail && !rolledDoubles) {
      --jailSentence;
      
      logInfo("Player " + playerIndex + " jailSentence: " + jailSentence);
      assert jailSentence>=0 : "Illegal jailSentence value: " + jailSentence;
    }
  }

  /**
   * Advance the player's location by numSpaces.
   * @param numSpaces The number of spaces to advance.
   * @return The player's location index after advancing.
   */
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

  /**
   * @return The player's current location index.
   */
  public int getLocationIndex() {
    return locationIndex;
  }

  /**
   * Set the player's current location to the location parameter.
   * @param location The location where the player is currently located.
   */
  public void setCurrentLocation(Location location) {
    this.location = location;

    logInfo("Player " + playerIndex + " landed on " + location.name);
    if (location.owner != null) {
      logInfo(location.name + " is owned by " + location.owner.playerIndex);
    }

    if (location.name.equals("Jail")) {
      if (inJail) {
        logInfo("Player " + playerIndex + " is in Jail");
        logInfo("Player sentence: " + jailSentence);
        logInfo("Player inJail flag: " + inJail);
        assert inJail : "Flag inJail is not valid";
        assert jailSentence == 3 : "JailSentence value is not correct";
      } else {
        logInfo("Player " + playerIndex + " is Just Visiting");
      }
    }
  }

  /**
   * @return True if the player passed Go or landed on Go during the most recent
   *         movement, false otherwise.
   */
  public boolean passedGo() {
    return passedGo;
  }

  /**
   * Add cash to the player's current amount of cash. 
   * @param amount The amount of cash to add the player's current amount of cash.
   */
  public void receiveCash(int amount) {
    cash += amount;
    logInfo("Player " + playerIndex + " received " + amount + " dollars.");
    logInfo("Player " + playerIndex + " has " + cash + " dollars.");
  }

  /**
   * Take some cash from the player.
   * 
   * @param amount
   *          The amount of cash to take from the player
   * @throws BankruptcyException
   *           If player does not have the amount and cannot sell houses or
   *           hotels and cannot mortgage any properties to raise the amount.
   */
  public void getCash(int amount) throws BankruptcyException {
    raiseCash(amount);
    cash = cash - amount;
    logInfo("Player " + playerIndex + " paid " + amount + " dollars.");
    logInfo("Player " + playerIndex + " has " + cash + " dollars.");
  }

  /**
   * @return The number of railroads that the player owns.
   */
  public int getNumRailroads() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.type.equals("railroad")) {
        ++count;
      }
    }
    return count;
  }

  /**
   * @return The number of Utilities that the player owns.
   */
  public int getNumUtilities() {
    int count = 0;
    for (Location property : owned.values()) {
      if (property.type.equals("utility")) {
        ++count;
      }
    }
    return count;
  }

  /**
   * Add a property to the player's inventory, normally by buying a property or
   * receiving a property through another player's bankruptcy.
   * 
   * @param location2
   *          The property to be added.
   */
  public void addProperty(Location location2) {
    owned.put(location2.index, location2);
    // mark all the properties that are part of monopolies 
    PropertyFactory.getPropertyFactory(game.gamekey).checkForMonopoly();
  }

  public void setLocationIndex(int index) {
    locationIndex = index;
  }

  /** 
   * @return True if the player is in jail, false otherwise.
   */
  public boolean inJail() {
    return inJail;
  }

  /**
   * @return A reference to the Location where the player current is.
   */
  public Location getCurrentLocation() {
    return location;
  }

  public boolean rolledDoubles() {
    return rolledDoubles;
  }

  /**
   * @return True if the player has either Get Out Of Jail Free card.
   */
  public boolean hasGetOutOfJailCard() {
    return chanceGOOJ != null || ccGOOJ != null;
  }

  /**
   * Use the player's Get Out Of Jail Free card by returning it to the Card
   * collection; modifying other state related to being in jail is not performed
   * by this method.
   */
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

  /**
   * @return The total worth of the player including cash, value of all houses
   *         and hotels, and value of all property owned by the player.
   */
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

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * @param chanceJailCard The card to add.
   */
  public void setGetOutOfJail(Chance chanceJailCard) {
    chanceGOOJ = chanceJailCard;
  }

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * @param ccJailCard The card to add.
   */
  public void setGetOutOfJail(CommunityChest ccJailCard) {
    ccGOOJ = ccJailCard;
  }

  /**
   * @return The number of houses that have been bought for all properties that
   *         are owned by this player
   */
  public int getNumHouses() {
    int result = 0;
    
    for (Location loc : owned.values()) {
      result += loc.getNumHouses();
    }

    return result;
  }

  /**
   * @return The number of hotels that have been bought for all properties that
   *         are owned by this player.
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
   *         amount, false otherwise.
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
   * Ask if the player wishes to pay bail.
   * 
   * @return True --> player wishes to pay bail to leave jail.<br>
   *         False --> player wishes to attempt to roll doubles to leave jail.
   */
  public abstract boolean payBailP();

  /**
   * Ask if the player wants to buy their current location.
   * 
   * @return True --> If the player wants to buy the property at their current
   *         location<br>
   *         False --> If the player does not want to buy the property at their
   *         current location.
   */
  public abstract boolean buyProperty();

  /**
   * Ask if the player wants to buy the given location.
   * 
   * @return True --> If the player wants to buy the given location <br>
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

  /**
   * Change player state based on having paid bail to leave jail.
   */
  public void paidBail() {
    inJail = false;
    jailSentence = 0;
  }

  /**
   * Ask if the player has a monopoly in any property group, ignoring whether
   * any of those properties are mortgaged; callers of this method should also
   * check for mortgaged properties before taking any action for a monopoly.
   * Mortgaged properties can impact what a player can do with a monopoly. For
   * example, even if the player has all the properties of a color group, the
   * player cannot build any houses for a color group if one or more properties
   * are mortgaged.
   * 
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
        //mortgage single street properties first
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

      // then mortgage railroads, and then utilities
      // sell railroads in order 1,4,2,3 or location index 5,35,15,25
      // sell utilities in order electric, water or location index 12, 28
      int[] index = new int[] { 5, 35, 15, 25, 12, 28 };
      for (int i : index) {
        Location l = owned.get(i);
        if (l != null && !l.isMortgaged) {
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
      int maxHouses = 4;
      while (maxHouses > 0) {
        for (Location l : owned.values()) {
          if (l.getNumHouses() == maxHouses) {
            logInfo(l.name + " has " + l.getNumHouses() + " houses");
            logInfo("Will sell house at " + l.name);
            game.sellHouse(l);
          }
        }

        if (cash >= amount) {
          return;
        }

        --maxHouses;
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

    // Decide whether to pay off any of the mortgaged properties, and if so,
    // then pay them off
    processMortgagedLots(mortgaged);

    // If any lots are still mortgaged, need to pay the interest
    for (Location lot : mortgaged) {
      if (lot.isMortgaged()) {
        // pay the interest of 10%
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
          // TODO assumption may be flawed, need to fix
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
        logInfo(lot.name + " is mortgaged; added to list of properties to unmortgage");
        mortgaged.add(lot);
      }
    }

    processMortgagedLots(mortgaged);
  }

  /**
   * Actually does the work of paying off the mortgages in the list created by
   * {@link #payOffMortgages()} or {@link #processMortgagedNewProperties(TreeMap)}.
   * 
   * @param mortgaged
   *          A list of mortgaged properties owned by the player.
   */
  private void processMortgagedLots(Vector<Location> mortgaged) {
    // only unmortgage when other monopolies have been fully developed
    // TODO: Need to validate this FOR block. It appears that the original
    // intent here was that if a location was part of a monopoly, then it should
    // be fully developed with 3 houses or a hotel before the player pays off the
    // mortgage for other properties.
    for (Location location : owned.values()) {
      if (location.isMortgaged() || location.groupIsMortgaged(game.gamekey)) {
        continue;
      }

      if (location.partOfMonopoly && !location.isFullyBuilt()) 
      {
        return;
      }
    }

    int count = 0;
    
    for (Location lot : mortgaged) {
      int amountToPay = 0;

      if (canPayMortgage(lot)) {
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
          // payMortgageP() should only return true if the player can raise the
          // cash to pay off the mortgage, thus getCash should not throw a
          // bankruptcy exception.
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
   * @param lot The property for which to pay off mortgage 
   * @return True if the player can pay the mortgage, False otherwise
   */
  private boolean canPayMortgage(Location lot) {
    int cost = (int) (1.1 * lot.getCost() / 2); 

    // After paying cost, player should still have minimum cash, so player can
    // pay off mortgage if player's current cash is more than minimum
    // cash + cost,
    return cash >= getMinimumCash() + cost;
  }

  /**
   * Compute the minimum amount of cash the player should have on hand based on
   * current game conditions.
   * 
   * @return The minimum amount of cash the player should have to avoid problems
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

    // Assume player will not try to raise cash for this. In most cases it
    // will not make sense for the player to sell houses or mortgage properties
    // to buy houses or hotels.
    //
    // TODO This might be implemented in the future: A player might want to sell
    // houses from one property to buy them for another property if an opponent
    // is far from the first property and close to the second property.
    if (cash < minCash) {
      logInfo("Player does not have minimum cash");
      return;
    }

    // Create a list of all the properties that the player owns that are also
    // part of monopolies; however, if any property in the group is mortgaged,
    // do not add that property to the vector.
    Vector<Location> monopolies = new Vector<Location>();
    for (Location location : owned.values()) {
      if (PropertyFactory.getPropertyFactory(game.gamekey).groupIsMortgaged(
          location.getGroup())) {
        //skip this property since the group is mortgaged
        continue;
      }

      if (location.partOfMonopoly) {
        monopolies.add(location);
        logInfo(location.toString()
            + " added to list of monopolies in processDevelopHouseEvent");
      }
    }
    
    for (int i = 0; i < groupOrder.length; i++) {
      for (Location location : monopolies) {
        // Process properties in group order, so if location is not part of current
        // group, then skip it for now
        if (location.getGroup() != groupOrder[i]) {
          continue;
        }
        
        logInfo("Checking " + location.name + " for build decision");
        
        if (location.getNumHotels() > 0) {
          logInfo("Location " + location.name + " has a hotel; nothing to build");
          continue;
        }

        logInfo("Location " + location.name + " has " + location.getNumHouses()
            + " houses");

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

  /**
   * Properly distribute the houses among all the properties in the given group.
   * 
   * @param monopolies
   *          The list of all properties for which the owner has a monopoly
   * @param group
   *          The group for which to balance the house distribution.
   */
  private void balanceHouses(Vector<Location> monopolies, PropertyGroups group) {
    // a list of the street locations in group
    Vector<Location> lots = new Vector<Location>();
    // the number of houses in the group
    int houseCount = 0;
    PropertyGroups g = group;
    int lotSize = 0;
    
    // go through the list of monopolies and add all street locations for group
    // to the list
    for (Location location : monopolies) {
      if (location.getGroup() == group) {
        lots.add(location);
        lotSize = lots.size();
        houseCount += location.getNumHouses();
        location.resetNumHouses();
      }
    }

   if (g != PropertyGroups.BROWN && g != PropertyGroups.DARK_BLUE) {
     assert lotSize == 3 : "Invalid lot size " + lotSize + " for Property Group " + g;
   }
   if (g == PropertyGroups.BROWN || g == PropertyGroups.DARK_BLUE) {
     assert lotSize == 2 : "Invalid lot size " + lotSize + " for Property Group " + g;
   }
        
    // verify that the lots are in order by index
    for (int i = 0; i < lots.size() - 1; i++) {
      assert lots.elementAt(i).index < lots.elementAt(i+1).index : "Lot order is invalid";
    }

    // start by evenly distributing the houses among the streets, 
    if (houseCount % lots.size() == 0) {
      // all houses can be distributed evenly
      while (houseCount > 0) {
        for (Location location : lots) {
          location.addHouse();
          --houseCount;
        }
      }
    } else {
      // distribute as many houses as possible, leftovers will be handled later
      while (houseCount > lots.size()) {
        for (Location location : lots) {
          location.addHouse();
          --houseCount;
        }
      }
    }

    if (houseCount > 0) {
      switch (group) {
      case PURPLE:
      case RED:
      case YELLOW:
      case GREEN:
        assert lots.size() == 3 : "Bad lot size: " + lots.elementAt(0).toString() + "; " + lots.elementAt(1).toString();
        try {
        // extra houses on third and first property
        lots.elementAt(2).addHouse();
        } catch (ArrayIndexOutOfBoundsException e) {
          for (Location location : lots) {
            System.out.println(location.toString());
          }
        }
        --houseCount;

        if (houseCount == 1) {
          lots.elementAt(0).addHouse();
          --houseCount;
        }
        break;

      case LIGHT_BLUE:
      case ORANGE:
        assert lots.size() == 3 : "Bad lot size: " + lots.elementAt(0).toString() + "/" + lots.elementAt(1).toString();
        // extra houses on third and second property
        lots.elementAt(2).addHouse();
        --houseCount;

        if (houseCount == 1) {
          lots.elementAt(1).addHouse();
          --houseCount;
        }
        break;

      case BROWN:
      case DARK_BLUE:
        assert lots.size() == 2 : "Bad lot size: " + lots.elementAt(0).toString();
        lots.elementAt(1).addHouse();
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
    String separator = System.getProperty("line.separator");
    StringBuilder result = new StringBuilder(1024);
    result.append(separator)
        .append("Player ").append(playerIndex).append(separator)
        .append("  Total cash  : ").append(cash).append(separator)
        .append("  Net worth   : ").append(getTotalWorth()).append(separator)
        .append("  Fitness     : ").append(fitnessScore).append(separator)
        .append("  Has Monopoly: ").append(hasMonopoly()).append(separator)
        .append("  Is Bankrupt : ").append(isBankrupt).append(separator);

    if (!owned.isEmpty()) {
      result.append("  Properties owned: ").append(separator);
      for (Location location : owned.values()) {
        result.append("    ").append(location.toString()).append(separator);
      }
    }

    return result.toString();
  }

  public abstract AbstractPlayer copyAndMutate();

  @Override
  protected abstract Object clone() throws CloneNotSupportedException;

  public void joinGame(Monopoly game) {
    this.game = game;
    location = PropertyFactory.getPropertyFactory(game.gamekey).getLocationAt(locationIndex);
  }
  
  private void logInfo(String s) {
    if (game != null) {
      Logger.getLogger(game.gamekey).info(s);
    }
  }
}
