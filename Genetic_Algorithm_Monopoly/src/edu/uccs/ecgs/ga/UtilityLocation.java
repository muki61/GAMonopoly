package edu.uccs.ecgs.ga;

import java.util.Properties;

public class UtilityLocation extends Location {

  private int cost;
  private PropertyGroups group = PropertyGroups.UTILITIES;
  public boolean arrivedFromChance;

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
    int rent = 0;

    if (isMortgaged) {
      multiple = 0;
    } else if (arrivedFromChance || owner.getNumUtilities() == 1) {
      multiple = 4;
      arrivedFromChance = false;
    } else if (owner.getNumUtilities() == 2) {
      multiple = 10;
    } else {
      assert false : "Invalid conditions at " + name + "; owner.numUtilities: "
          + owner.getNumUtilities() + "; arrived from chance: "
          + arrivedFromChance;
    }

    rent = diceRoll * multiple;
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
