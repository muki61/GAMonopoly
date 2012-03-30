package edu.uccs.ecgs.ga;

import java.util.Properties;

/**
 * Class the implements a location (property or special location) in the game.
 * Location can be an actual property like Vermont or Marvin Gardens, a utility,
 * a railroad, or a special location like Go or Chance.
 * 
 */
public abstract class Location implements Comparable<Location> {
  /**
   * A short string that identifies the values for a given property in the
   * locations.properties file.
   */
  final String key;
  
  /**
   * The name of this property.
   */
  public final String name;
  
  /**
   * The type of this location: Property, Railroad, Utility, or Special.
   */
  protected final String type;

  /**
   * The position of this location on the game board. Locations are numbered
   * sequentially starting at Go with an index of 0, and proceeding clockwise
   * around the board to Boardwalk with an index of 39.
   */
  public final int index;

  /**
   * A string that gives information about the property. Access using
   * {@link #getInfo()}.
   */
  protected String _string;

  /**
   * A reference to the player that owns this location. Only has meaning for
   * locations of type Property, Railroad, or Utility.
   */
  public AbstractPlayer owner = null;

  /**
   * Whether this location is part of a monopoly. Only has meaning for locations
   * of type Property.
   */
  public boolean partOfMonopoly = false;
  
  /**
   * Whether the player who is on this space arrived by drawing a Chance card
   * that directed the player to this location. Used only for utilities.
   */
  boolean arrivedFromChance = false;

  /**
   * Number of houses on this location. Only has meaning for locations of type
   * Property.
   */
  private int numHouses = 0;

  /**
   * Number of hotels on this location. Only has meaning for locations of type
   * Property.
   */
  private int numHotels = 0;
  

  /**
   * Whether this location is mortgaged. Only has meaning for locations of type
   * Property, Railroad, or Utility.
   */
  protected boolean isMortgaged = false;

  /**
   * Rent multiplier. See {@link #setRentMultiple(int)}
   */
  protected int multiple = 1;

  public Location(String key2, Properties properties) {
    key = key2;

    index = getInteger(key + ".index", properties);
    name = properties.getProperty(key + ".name");
    type = properties.getProperty(key + ".type");
}

  /**
   * Get an integer from the locations.properties file. This method does not
   * check for key existence. It is the caller's responsibility to ensure that
   * the key is valid and the property can be parsed as an integer.
   * 
   * @param aKey
   *          A string that identifies the integer, such as "Go.index"
   * @param properties
   *          The properties object that contains key-value pairs to be
   *          retrieved.
   * @return An integer
   * @throws java.lang.NumberFormatException
   *           If the properties file does not contain the given key.
   */
  protected int getInteger(String aKey, Properties properties) {
    return Integer.parseInt(properties.getProperty(aKey));
  }

  public String toString() {
    return getGroup().toString() + "/" + name;
  }
  
  /**
   * @return Information about the location.
   */
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
   * Set multiplier for rent. For example, unimproved properties in a monopoly
   * receive double rent so multiplier would be 2 in this case. Railroads
   * receive a multiple of the base rent depending on how many railroads are
   * owned by a player. Utilities also have a rent multiplier based on how many
   * utilities are owned by a player.
   * 
   * @param multiple
   *          The amount to multiply the rent by.
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
