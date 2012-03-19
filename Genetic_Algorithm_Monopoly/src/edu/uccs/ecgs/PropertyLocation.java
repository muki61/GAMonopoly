package edu.uccs.ecgs;

import java.util.Properties;

public class PropertyLocation extends Location {

  final private PropertyGroups group;
  final private int houseCost;
  final private int rentUnimproved;
  final private int rentOneHouse;
  final private int hotelCost;
  final private int rentTwoHouses;
  final private int rentThreeHouses;
  final private int rentFourHouses;
  final private int rentHotel;
  private int cost;

  public PropertyLocation(String key, Properties properties) {
    super(key, properties);

    cost = getInteger(key + ".cost", properties);
    houseCost = getInteger(key + ".cost.house", properties);
    hotelCost = getInteger(key + ".cost.hotel", properties);
    rentUnimproved = getInteger(key + ".rent.unimproved", properties);
    rentOneHouse = getInteger(key + ".rent.one_house", properties);
    rentTwoHouses = getInteger(key + ".rent.two_houses", properties);
    rentThreeHouses = getInteger(key + ".rent.three_houses", properties);
    rentFourHouses = getInteger(key + ".rent.four_houses", properties);
    rentHotel = getInteger(key + ".rent.hotel", properties);

    String grp = properties.getProperty(key + ".group");
    if (grp.equals("brown")) {
      group = PropertyGroups.BROWN;
    } else if (grp.equals("light_blue")) {
      group = PropertyGroups.LIGHT_BLUE;
    } else if (grp.equals("purple")) {
      group = PropertyGroups.PURPLE;
    } else if (grp.equals("red")) {
      group = PropertyGroups.RED;
    } else if (grp.equals("orange")) {
      group = PropertyGroups.ORANGE;
    } else if (grp.equals("yellow")) {
      group = PropertyGroups.YELLOW;
    } else if (grp.equals("green")) {
      group = PropertyGroups.GREEN;
    } else if (grp.equals("dark_blue")) {
      group = PropertyGroups.DARK_BLUE;
    } else {
      group = PropertyGroups.SPECIAL;
      throw new IllegalArgumentException("Property value is "+grp);
    }

    _string = "Name           : " + name + "\n  index        : " + index
        + "\n  group        : " + group + "\n  type         : " + type
        + "\n  cost         : " + cost + "\n" + "  house cost   : " + houseCost
        + "\n" + "  hotel cost   : " + hotelCost + "\n" + "  rent         : "
        + rentUnimproved + "\n" + "  rent 1 houses: " + rentOneHouse + "\n"
        + "  rent 2 houses: " + rentTwoHouses + "\n" + "  rent 3 houses: "
        + rentThreeHouses + "\n" + "  rent 4 houses: " + rentFourHouses + "\n"
        + "  rent hotel   : " + rentHotel;
  }

  @Override
  public int getCost() {
    return cost;
  }

  @Override
  public int getHotelCost() {
    return hotelCost;
  }

  @Override
  public int getHouseCost() {
    return houseCost;
  }

  @Override
  public int getRent() {
    int rent = 0;

    if (isMortgaged) {
      multiple = 0;
    }

    if (partOfMonopoly) {
      int buildCount = getNumHouses() + (getNumHotels() * 5);
      switch (buildCount) {
      case 0:
        multiple = multiple * 2;
        rent = rentUnimproved * multiple;
        break;
      case 1:
        rent = rentOneHouse * multiple;
        break;
      case 2:
        rent = rentTwoHouses * multiple;
        break;
      case 3:
        rent = rentThreeHouses * multiple;
        break;
      case 4:
        rent = rentFourHouses * multiple;
        break;
      case 5:
        rent = rentHotel * multiple;
        break;
      }
    } else {
    // else not part of monopoly (and this means 0 houses or hotels)
      rent = rentUnimproved * multiple;
    }
    
    if (isMortgaged) {
      logger.info("Lot is mortgaged, rent: 0");
    } else {
      logger.info("Rent for " + name + " with " + getNumHouses() + 
          " houses: " + rent);
    }

    resetMultiple();
    return rent;
  }

  @Override
  public PropertyGroups getGroup() {
    return group;
  }

  @Override
  public void setMortgaged() {
    setMortgaged(true);
  }

  @Override
  public void setMortgaged(boolean b) {
    isMortgaged = b;
  }
}
