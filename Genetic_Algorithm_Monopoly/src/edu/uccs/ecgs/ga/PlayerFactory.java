package edu.uccs.ecgs.ga;


public class PlayerFactory {

  public static AbstractPlayer getPlayer(int index, ChromoTypes chromoType) {
    AbstractPlayer player = null;
    switch (chromoType) {
    case RGA:
      player = new RGAPlayer(index);
      break;
    case SGA:
      player = new SGAPlayer(index);
      break;
    case TGA:
      player = new TGAPlayer(index);
      break;
    default:
      throw new java.lang.IllegalArgumentException(
          "Unsupported chromosome type: " + chromoType.toString());
    }
    return player;
  }
}
