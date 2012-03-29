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
    return name;
  }

  public AbstractPlayer getOwner() {
    return owner;
  }

  public abstract int getCost();

  public int getHouseCost() {
    return 0;
  }
  
  public int getHotelCost() {
    return 0;
  }
  
  public void setOwner(AbstractPlayer player) {
    owner = player;
  }

  public int getNumHouses() {
    return numHouses;
  }
  
  public int getNumHotels() {
    return numHotels;
  }

  public abstract int getRent(int diceRoll);
  
  public abstract PropertyGroups getGroup();

  public boolean isMortgaged() {
    return isMortgaged;
  }
  
  public void setRentMultiple(int multiple) {
    this.multiple  = multiple;
  }

  protected void resetMultiple() {
    multiple = 1;
  }

  public void sellHouse() {
    assert numHouses > 0 : "Illegal house count: " + numHouses;
    --numHouses;
  }

  public void addHouse() {
    ++numHouses;
    assert numHouses < 5 : "Illegal house count: " + numHouses;
  }
  
  public void sellHotel() {
    --numHotels;
    assert numHotels == 0 : "Illegal hotel count: " + numHotels;
    numHouses = 4;
  }

  public void addHotel() {
    assert numHouses == 4 : "Not enough houses to buy hotel: " + numHouses;
    ++numHotels;
    assert numHotels == 1 : "Illegal hotel count: " + numHotels;
    numHouses = 0;
  }

  public abstract void setMortgaged(boolean b);

  public abstract void setMortgaged();

  public void resetNumHouses() {
    numHouses = 0;
  }

  public void assignHouse() {
    ++numHouses;
  }

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
