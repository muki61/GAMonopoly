package edu.uccs.ecgs.ga;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

//import org.junit.Assert;

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

  public static Random r = new Random();
  static {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
//    System.out.println("AbstractPlayer seed: " + seed);
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
      
      game.logger.info("Player " + playerIndex + " jailSentence: " + jailSentence);
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
        game.logger.info("Player " + playerIndex + " landed on Go");
      } else {
        game.logger.info("Player " + playerIndex + " passed Go");
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
      game.logger.info("Player " + playerIndex + " landed on " + location.name);
      if (inJail) {
        game.logger.info("Player " + playerIndex + " is in Jail");
        game.logger.info("Player sentence: " + jailSentence);
        game.logger.info("Player inJail flag: " + inJail);
        assert inJail : "Flag inJail is not valid";
        assert jailSentence == 3 : "JailSentence value is not correct";
      } else {
        game.logger.info("Player " + playerIndex + " is Just Visiting");
      }
    } else {
      game.logger.info("Player " + playerIndex + " landed on " + location.name);
    }
  }

  public boolean passedGo() {
    return passedGo;
  }

  public void receiveCash(int amount) {
    cash += amount;
    game.logger.info("Player " + playerIndex + " received " + amount
        + " dollars.");
    game.logger.info("Player " + playerIndex + " has " + cash + " dollars.");
  }

  public void getCash(int amount) throws BankruptcyException {
    raiseCash(amount);
    cash = cash - amount;
    game.logger.info("Player " + playerIndex + " paid " + amount + " dollars.");
    game.logger.info("Player " + playerIndex + " has " + cash + " dollars.");
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
   * Return the number of houses that have been bought for all properties that are owned by this player
   * @return
   */
  public int getNumHouses() {
    int result = 0;
    
    for (Location loc : owned.values()) {
      result += loc.getNumHouses();
    }

    return result;
  }

  /**
   * Return the number of hotels that have been bought for all properties that are owned by this player
   * @return
   */
  public int getNumHotels() {
    int result = 0;
    
    for (Location loc : owned.values()) {
      result += loc.getNumHotels();
    }

    return result;
  }

  public boolean canRaiseCash (int amount) {
    int totalWorth = cash;
    
    if (totalWorth >= amount) {
      return true;
    }

    for (Location location : owned.values()) {
      totalWorth += location.getNumHouses() * location.getHouseCost() / 2;
      totalWorth += location.getNumHotels() * location.getHotelCost() / 2;
      if (!location.isMortgaged()) {
        totalWorth += location.getCost() / 2;
      }
    }
    
    if (totalWorth >= amount) {
      return true;
    }
    
    return false;
  }

  // Predicate asking whether or not player wishes to pay bail
  // True --> player wishes to pay bail
  // False --> player wishes to attempt to roll doubles
  public abstract boolean payBailP();

  // If player wants to buy current location -->
  // return true
  // Otherwise -->
  // return false
  // Player will buy property if random double is less than chromosome
  // value; in other words, a higher chromosome value means a higher
  // likelihood of buying a property
  public abstract boolean buyProperty();
  public abstract boolean buyProperty(Location location);

  public abstract void dumpGenome(DataOutputStream out) throws IOException;

  public void printTotalWorth() {
    game.logger.info("Player " + playerIndex);
    game.logger.info("Total cash: " + cash);
    game.logger.info("Net worth : " + getTotalWorth());
    game.logger.info("Fitness   : " + fitnessScore);

    if (!isBankrupt) {
      game.logger.info("Has monopoly: " + hasMonopoly());
      game.logger.info("Properties owned: ");

      for (Location location : owned.values()) {
        if (location.getNumHouses() > 0) {
          game.logger.info(location.name + " (" + location.getNumHouses() + " houses)");
        } else {
          game.logger.info(location.name);
        }
      }
    }
  }

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

  public void enteredJail() {
    inJail = true;
    jailSentence = 3;
  }

  public boolean jailSentenceCompleted() {
    return jailSentence == 0;
  }

  public void paidBail() {
    inJail = false;
    jailSentence = 0;
  }

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

  public void raiseCash(int amount) throws BankruptcyException {
    game.logger.info("Player " + playerIndex + " has " + cash + " dollars");
    if (cash >= amount) {
      return;
    }
    
    if (canRaiseCash(amount)) {
      game.logger.info("Player " + playerIndex + " attempting to raise " + amount
          + " dollars");
      for (Location l : owned.values()) {
        //mortgage single properties first
        if (!l.partOfMonopoly
            && !l.isMortgaged()
            && l.getGroup() != PropertyGroups.UTILITIES
            && l.getGroup() != PropertyGroups.RAILROADS) 
        {
          // mortgage property if not part of monopoly
          game.logger.info("Player will mortgage " + l.name);
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
            game.logger.info("Player will mortgage " + l.name);
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
            game.logger.info("Player will mortgage " + l.name);
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
          game.logger.info("Player will mortgage " + l.name);
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
          game.logger.info(locMaxHouses.name + " has " + locMaxHouses.getNumHouses() + " houses");
          game.logger.info("Will sell house at " + locMaxHouses.name);
          game.sellHouse(this, locMaxHouses);
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
          game.logger.info("Player will mortgage " + l.name);
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

  public void addProperties(TreeMap<Integer, Location> allProperties, 
                            boolean gameOver) 
  {
    //this is called when another player goes bankrupt against this player

    //add all properties first
    for (Location l : allProperties.values()) {
      owned.put(l.index, l);
      l.owner = this;
    }
    
    PropertyFactory.getPropertyFactory(game.gamekey).checkForMonopoly();
  
    if (!gameOver) {
      processMortgagedNewProperties(allProperties);
    }
  }

  private void processMortgagedNewProperties(TreeMap<Integer, Location> allProperties) {
    Vector<Location> mortgaged = new Vector<Location>();

    // want to handle mortgages of added properties in this order:
    // properties that are part of monopolies
    for (Location lot : allProperties.values()) {
      if (lot.partOfMonopoly && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }
        
    // utility monopolies
    // count number of utilties
    int countUtilities = 0;
    for (Location lot : allProperties.values()) {
      if (lot.getGroup() == PropertyGroups.UTILITIES) {
        ++countUtilities;
      }
    }
    countUtilities += getNumUtilities();
    
    //add utilities if player has 2 utilities
    if (countUtilities == 2) {
      for (Location lot : allProperties.values()) {
        if (lot.getGroup() == PropertyGroups.UTILITIES && lot.isMortgaged()) {
          mortgaged.add(lot);
        }
      }
    }
    
    // add railroads
    for (Location lot : allProperties.values()) {
      if (lot.getGroup() == PropertyGroups.RAILROADS && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }
    
    // add single utilities
    if (countUtilities == 1) {
      for (Location lot : allProperties.values()) {
        if (lot.getGroup() == PropertyGroups.UTILITIES && lot.isMortgaged()) {
          mortgaged.add(lot);
        }
      }
    }

    //lots that are not part of monopolies
    for (Location lot : allProperties.values()) {
      if (!lot.partOfMonopoly && lot.getGroup() != PropertyGroups.RAILROADS
          && lot.getGroup() != PropertyGroups.UTILITIES && lot.isMortgaged()) {
        mortgaged.add(lot);
      }
    }    
    
    processMortgagedLots(mortgaged);

    for (Location lot : mortgaged) {
      if (lot.isMortgaged()) {
        // leave property mortgaged and only pay the fee
        int amountToPay = (int) (0.1 * lot.getCost() / 2);
        game.logger.info("Player " + playerIndex + 
                    " will only pay mortgage fee for " + lot.name + 
                    "; fee is " + amountToPay);

        try {
          getCash(amountToPay);
        } catch (BankruptcyException e) {
          //ignored exception
          //player is in this method because some other player
          //went bankrupt, so assume this player has enough cash
          //or can raise enough cash
          e.printStackTrace();
        }
      }
    }
  }

  public void processMortgagedLots() {
    Vector<Location> mortgaged = new Vector<Location>();
    for (Location lot : owned.values()) {
      if (lot.isMortgaged()) {
        game.logger.info(lot.name + " is mortgaged");
        mortgaged.add(lot);
      }
    }
    if (mortgaged.size() > 0) {
      processMortgagedLots(mortgaged);
    }
  }

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
        game.logger.info("Player will pay off mortgage for " + lot.name + "; cost is "
            + amountToPay);
        try {
          getCash(amountToPay);
          lot.setMortgaged(false);
          game.logger.info(lot.name + " is no longer mortgaged");
          ++count;
        } catch (BankruptcyException e) {
          // the player will not decide to pay mortgage unless
          // enough free cash is available
          e.printStackTrace();
        }
      }
    }
    game.logger.info(count + " mortgaged lots were paid off; " + 
        (mortgaged.size()-count) + " lots are still mortgaged");
  }

  //make the decision of whether or not to pay the mortgage on the property
  private boolean payMortgageP(Location lot) {
    int cost = (int) (1.1 * lot.getCost() / 2); 

    if (cash < getMinimumCash() + cost) {
      return false;
    }
    
    return true;
  }

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

  public void sellAllHousesHotels() {
    //sell all houses first
    for (Location l : owned.values()) {
      while (l.getNumHouses() > 0) {
        game.sellHouse(this, l);
      }
    }
    
    //now sell any hotels, and then houses again because selling a hotel may result
    //in houses at the location
    for (Location l : owned.values()) {
      if (l.getNumHotels() > 0) {
        game.sellHotel(this, l, owned.values());
      }
      while (l.getNumHouses() > 0) {
        game.sellHouse(this, l);
      }
    }
  }

  public void processDevelopHouseEvent() {
    if (game.getNumHouses() == 0) {
      game.logger.info("Bank has no more houses");
      return;
    }
      
    if (!hasMonopoly()) {
      game.logger.info("Player does not have monopoly");
      return;
    }
    
    game.logger.info("Player has " + cash + " dollars");
    int minCash = getMinimumCash();
    game.logger.info("Player minimum cash is " + minCash);

    if (cash < minCash) {
      game.logger.info("Player does not have minimum cash");
      return;
    }

    //build in this order
    // 1 Orange
    // 2 Light Blue
    // 3 Red
    // 4 Light Purple
    // 5 Dark Blue
    // 6 Yellow
    // 8 Green
    // 9 Brown
    
    PropertyGroups[] groupOrder = new PropertyGroups[] {PropertyGroups.ORANGE, PropertyGroups.LIGHT_BLUE,
        PropertyGroups.RED, PropertyGroups.PURPLE, PropertyGroups.DARK_BLUE, PropertyGroups.YELLOW,
        PropertyGroups.GREEN, PropertyGroups.BROWN};
    
    Vector<Location> monopolies = new Vector<Location>();
    for (Location location : owned.values()) {
      if (location.partOfMonopoly) {
        monopolies.add(location);
      }
    }
    
    for (int i = 0; i < groupOrder.length; i++) {
      for (Location location : monopolies) {
        if (location.getGroup() != groupOrder[i]) {
          continue;
        }

        while (location.getNumHouses() < 3) {
          if (location.getNumHouses() >= 3 || location.getNumHotels() > 0) {
            game.logger.info("Location " + location.name + " has enough houses");
            break;
          }

          // at this point location is part of monopoly
          // correct group
          // not enough houses/hotels
          // now check that there is enough cash to buy a house
          if (cash < (getMinimumCash() + location.getHouseCost())) {
            game.logger.info("Player does not have " + location.getHouseCost()
                + " dollars extra");
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
        // extra houses on third and first property
        lots.elementAt(2).assignHouse();
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
      game.logger.info(location.name + " has " + location.getNumHouses() + " houses");
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
}
