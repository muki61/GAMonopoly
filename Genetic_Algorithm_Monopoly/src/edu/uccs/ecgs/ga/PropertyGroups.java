package edu.uccs.ecgs.ga;

public enum PropertyGroups {
  BROWN, LIGHT_BLUE, PURPLE, RED, ORANGE, YELLOW, GREEN, DARK_BLUE, RAILROADS, UTILITIES, SPECIAL;
  
  public String toString() {
    return name().substring(0,1) + name().substring(1).toLowerCase();
  }
}
