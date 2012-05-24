package edu.uccs.ecgs.ga;

public enum Spaces { 

  SPACE_00("Go"), SPACE_01 ("Mediterranean Avenue"), SPACE_02("Community Chest"), SPACE_03("Baltic Avenue"), SPACE_04("Income Tax"), SPACE_05("Reading Railroad"), 
  SPACE_06("Oriental Avenue"), SPACE_07("Chance"), SPACE_08("Vermont Avenue"), SPACE_09("Connecticut Avenue"), SPACE_10("Jail"), 
  SPACE_11("St Charles Place"), SPACE_12("Electric Company"), SPACE_13("States Avenue"), SPACE_14("Virginia Avenue"), SPACE_15("Pennsylvania Railroad"), 
  SPACE_16("St James Place"), SPACE_17("Community Chest"), SPACE_18("Tennessee Avenue"), SPACE_19("New York Avenue"), SPACE_20("Free Parking"), 
  SPACE_21("Kentucky Avenue"), SPACE_22("Chance"), SPACE_23("Indiana Avenue"), SPACE_24("Illinois Avenue"), SPACE_25("B&O Railroad"), 
  SPACE_26("Atlantic Avenue"), SPACE_27("Ventnor Avenue"), SPACE_28("Water Works"), SPACE_29("Marvin Gardens"), SPACE_30("Go To Jail"), 
  SPACE_31("Pacific Avenue"), SPACE_32("North Carolina Avenue"), SPACE_33("Community Chest"), SPACE_34("Pennsylvania Avenue"), SPACE_35("Short Line"), 
  SPACE_36("Chance"), SPACE_37("Park Place"), SPACE_38("Luxury Tax"), SPACE_39("Boardwalk");

  private String name;

  private Spaces(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }
}
