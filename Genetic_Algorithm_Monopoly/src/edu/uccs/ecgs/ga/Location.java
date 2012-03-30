package edu.uccs.ecgs.ga;

import java.util.Properties;

/**
 * Class the implements a location (property or special location) in the game.
 * Location can be an actual property like Vermont or Marvin Gardens, a utility,
 * a railroad, or a special location like Go or Chance.
 * 
 */
public abstract class Location implements Comparable<Location> {
  final String key;
  public final String name;
  final String type;
  public final int index;
  protected String _string;
  public AbstractPlayer owner = null;
  public boolean partOfMonopoly = false;

  private int numHouses = 0;
  private int numHotels = 0;
  
  protected boolean isMortgaged = false;
  protected int multiple = 1;

  public Location(String key2, Properties properties) {
    key = key2;

    index = getInteger(key + ".index", properties);
    name = properties.getProperty(key + ".name");
    type = properties.getProperty(key + ".type");
}

  protected int getInteger(String aKey, Properties properties) {
    return Integer.parseInt(properties.getProperty(aKey));
  }

  public String toString() {
    return getGroup().toString() + "/" + name;
  }
  
  public String getInfo() {
    return _string;
  }

  /**
   * @return A reference to the player that owns the property
   */
  public AbstractPlayer getOwner() {
    return owner;
  }

  /**
   * @return The cost of purchasing the property
   */
  public abstract int getCost();

  /**
   * @return The cost to buy a house for this property. For Utilities,
   *         Railroads, and special locations, this method returns 0.
   */
  public int getHouseCost() {
    return 0;
  }
  
  /**
   * @return The cost to buy a hotel for this property. For Utilities,
   *         Railroads, and special locations, this method returns 0.
   */
  public int getHotelCost() {
    return 0;
  }
  
  /**
   * Set the owner for the property to the given player.
   * @param player The player that owns the property.
   */
  public void setOwner(AbstractPlayer player) {
    owner = player;
  }

  /**
   * @return The number of houses on the property. For Utilities, Railroads, and
   *         special locations, this method returns 0.
   */
  public int getNumHouses() {
    return numHouses;
  }
  
  /**
   * @return The number of hotels on the property. For Utilities, Railroads, and
   *         special locations, this method returns 0.
   */
  public int getNumHotels() {
    return numHotels;
  }

  /**
   * @param diceRoll
   *          The total value of the dice roll of the player who landed on this
   *          property.
   * @return The rent owed.
   */
  public abstract int getRent(int diceRoll);

  /**
   * @return The PropertyGroup value that the property belongs to.
   */
  public abstract PropertyGroups getGroup();

  /**
   * @return True if the property is mortgaged, false otherwise.
   */
  public boolean isMortgaged() {
    return isMortgaged;
  }
  
  /**
   * Set multiplier for rent. For example, unimproved properties in a monopoly receive
   * double rent so multiplier would be 2 in this case. 
   * @param multiple The amount to multiply the rent by.
   */
  protected void setRentMultiple(int multiple) {
    this.multiple  = multiple;
  }

  /**
   * Reset rent multiplier to 1.
   */
  protected void resetMultiple() {
    multiple = 1;
  }

  /**
   * Remove a house from the property.
   */
  public void removeHouse() {
    assert numHouses > 0 : "Illegal house count: " + numHouses;
    --numHouses;
  }

  /**
   * Add a house to this property.
   */
  public void addHouse() {
    ++numHouses;
    assert numHouses < 5 : "Illegal house count: " + numHouses;
  }
  
  /**
   * Remove a hotel from this property (also sets number of houses on this property to 4).
   */
  public void removeHotel() {
    --numHotels;
    assert numHotels == 0 : "Illegal hotel count: " + numHotels;
    numHouses = 4;
  }

  /**
   * Add a hotel to this property by removing 4 houses from property and adding
   * hotel. Property must have 4 houses prior to calling this method; caller is
   * responsible for returning the 4 houses to the game inventory.
   */
  public void addHotel() {
    assert numHouses == 4 : "Not enough houses to buy hotel: " + numHouses;
    ++numHotels;
    assert numHotels == 1 : "Illegal hotel count: " + numHotels;
    numHouses = 0;
  }

  /**
   * Set the property to be mortgaged or not, based on the input parameter.
   * @param b True if property is mortgaged, false otherwise.
   */
  public abstract void setMortgaged(boolean b);

  /**
   * Set the property to be mortgaged.
   */
  public abstract void setMortgaged();

  /**
   * Set number of houses on property to 0.
   */
  public void resetNumHouses() {
    numHouses = 0;
  }

  /**
   * Set number of hotels on property to 0.
   */
  public void resetNumHotels() {
    numHotels = 0;
  }

  @Override
  public int compareTo(Location arg0) {
		return Integer.valueOf(index).compareTo(Integer.valueOf(arg0.index));
	}

  /**
   * Is the location fully developed.
   * @return True if the location has 3 or more houses or a hotel, false otherwise 
   */
  public boolean isFullyBuilt() {
    return getNumHouses() >= 3 || getNumHotels() > 0;
  }

  /**
   * Is any property in the same group mortgaged
   * 
   * @return True if any property in the same group as this property is
   *         mortgaged, false otherwise.
   */
  public boolean groupIsMortgaged(String gamekey) {
    return PropertyFactory.getPropertyFactory(gamekey).groupIsMortgaged(this.getGroup());
  }
}
