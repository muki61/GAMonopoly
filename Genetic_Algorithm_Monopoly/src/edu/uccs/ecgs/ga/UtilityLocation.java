package edu.uccs.ecgs.ga;

import java.util.Properties;

/**
 * Represents one of the utilities in the game, either Electric Company or Water
 * Works.
 */
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

    if (arrivedFromChance ) {
      setRentMultiple(10);
    } else {
      setRentMultiple(owner.getNumUtilities() == 1 ? 4 : 10);
    }
    
    int rent = 0;

    rent = diceRoll * multiple;

    resetMultiple();
    arrivedFromChance = false; // this value no longer matters, so reset to default  

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
