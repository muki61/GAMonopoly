package edu.uccs.ecgs.ga;

import java.util.Properties;

public class UtilityLocation extends Location {

  private int cost;
  private PropertyGroups group = PropertyGroups.UTILITIES;

  public UtilityLocation(String key2, Properties properties) {
    super(key2, properties);

    cost = getInteger(key + ".cost", properties);

    _string = "Name         : " + name + "\n  index        : " + index
        + "\n  group        : " + group
        + "\n  type         : " + type + "\n  cost         : " + cost;
  }

  @Override
  public int getCost() {
    return cost;
  }

  @Override
  public int getRent(int diceRoll) {
    assert !isMortgaged() : "Location is mortgaged in getRent";

    setRentMultiple(owner.getNumUtilities() == 1 ? 4 : 10);
    
    int rent = 0;

    rent = diceRoll * multiple;

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

  @Override
  public String toString() {
    return super.toString() + (isMortgaged() ? " (mortgaged)" : "");

  }
}
