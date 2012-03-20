package edu.uccs.ecgs.ga;

import java.util.Properties;


public class SpecialLocation extends Location {

  final private PropertyGroups group = PropertyGroups.SPECIAL;

  public SpecialLocation(String key, Properties properties) {
    super(key, properties);

    _string = "Name           : " + name + 
              "\n  index        : " + index + 
              "\n  type         : " + type +
              "\n  group        : " + group;
  }
  
  @Override
  public AbstractPlayer getOwner() {
    return null;
  }

  @Override
  public int getCost() {
    return 0;
  }

  @Override
  public int getRent() {
    return 0;
  }

  @Override
  public PropertyGroups getGroup() {
    return group;
  }

  @Override
  public void setMortgaged() {}
  @Override
  public void setMortgaged(boolean b) {}
}
