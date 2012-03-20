package edu.uccs.ecgs.ga;

import java.util.Properties;

public class RailroadLocation extends Location {

  final private PropertyGroups group = PropertyGroups.RAILROADS;
  final private int rentOneLine;
  final private int rentTwoLines;
  final private int rentThreeLines;
  final private int rentFourLines;
  private int cost;

  public RailroadLocation(String key2, Properties properties) {
    super(key2, properties);

    cost = getInteger(key + ".cost", properties);
    rentOneLine = getInteger(key + ".rent.one_railroad", properties);
    rentTwoLines = getInteger(key + ".rent.two_railroads", properties);
    rentThreeLines = getInteger(key + ".rent.three_railroads", properties);
    rentFourLines = getInteger(key + ".rent.four_railroads", properties);

    _string = "Name              : " + name + "\n  index           : " + index
        + "\n  group           : " + group
        + "\n  type            : " + type + "\n  cost            : "
        + cost + "\n  rent 1 railroads: " + rentOneLine + "\n  rent 2 railroads: " 
        + rentTwoLines + "\n  rent 3 railroads: "
        + rentThreeLines + "\n  rent 4 railroads: " + rentFourLines;
  }

  @Override
  public int getCost() {
    return cost;
  }

  @Override
  public int getRent() {
    int rent = 0;

    if (isMortgaged) {
      multiple = 0;
    }

    switch (owner.getNumRailroads()) {
    case 1: 
      rent = rentOneLine * multiple;
      break;
    case 2: 
      rent = rentTwoLines * multiple;
      break;
    case 3: 
      rent = rentThreeLines * multiple;
      break;
    case 4: 
      rent = rentFourLines * multiple;
      break;
    }

    if (isMortgaged) {
//      game.logger.info("Lot is mortgaged, rent: 0");
    } else {
//      game.logger.info("Rent for " + name + ": " + rent);
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
