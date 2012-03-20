package edu.uccs.ecgs;

public enum Chance {

  ADVANCE_TO_GO, ADVANCE_TO_ILLINOIS, ADVANCE_TO_UTILITY, 
  ADVANCE_TO_RAILROAD1, ADVANCE_TO_RAILROAD2, ADVANCE_TO_ST_CHARLES, 
  GO_BACK_3, GO_TO_JAIL, ADVANCE_TO_READING, ADVANCE_TO_BOARDWALK, 
  BANK_PAYS_50, BANK_PAYS_150, REPAIR_PROPERTY, PAY_15, 
  PAY_EACH_PLAYER_50, GET_OUT_OF_JAIL;

  private Monopoly game;

  public void processChance(AbstractPlayer player, Monopoly game) throws BankruptcyException {
    int locationIndex = 0;
    int spacesToAdvance = 0;
    Location location = null;
    this.game = game;

    game.logger.info("Processing Chance Card '" + toString() + "' for player "
        + player.playerIndex);

    switch (this) {
    case ADVANCE_TO_GO:
      locationIndex = 0;
      advancePlayer(player, locationIndex);
      break;

    case ADVANCE_TO_ILLINOIS:
      locationIndex = 24;
      advancePlayer(player, locationIndex);
      break;

    case ADVANCE_TO_UTILITY:
      locationIndex = 12;
      if (player.getLocationIndex() > 12 && player.getLocationIndex() < 28) {
        locationIndex = 28;
      }
      location = PropertyFactory.getPropertyFactory(game.gamekey).getLocationAt(locationIndex);
      ((UtilityLocation) location).arrivedFromChance = true;
      game.logger.info("Rolling dice...");
      game.logDiceRoll(Dice.getDice().roll());
      advancePlayer(player, locationIndex);

      break;

    case ADVANCE_TO_RAILROAD1:
    case ADVANCE_TO_RAILROAD2:
      locationIndex = player.getLocationIndex();
      while (locationIndex % 5 != 0) {
        ++locationIndex;
        if (locationIndex % 10 == 0) {
          ++locationIndex;
        }
      }
      advancePlayer(player, locationIndex);
      if (player.location.owner != null && player.location.owner != player) {
        player.location.setRentMultiple(2);
      }
      break;

    case ADVANCE_TO_ST_CHARLES:
      locationIndex = 11;
      advancePlayer(player, locationIndex);
      break;

    case ADVANCE_TO_READING:
      locationIndex = 5;
      advancePlayer(player, locationIndex);
      break;

    case ADVANCE_TO_BOARDWALK:
      locationIndex = 39;
      advancePlayer(player, locationIndex);
      break;

    case GO_BACK_3:
      spacesToAdvance = -3;
      movePlayer(player, spacesToAdvance);
      break;

    case GO_TO_JAIL:
      PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
      location = pf.getLocationAt(10);
      player.enteredJail();
      player.setLocationIndex(location.index);
      player.setCurrentLocation(location);
      break;

    case BANK_PAYS_50:
      player.receiveCash(50);
      break;

    case BANK_PAYS_150:
      player.receiveCash(150);
      break;

    case REPAIR_PROPERTY:
      int amountToPay = 0;
      amountToPay += 25 * player.getNumHouses();
      amountToPay += 100 * player.getNumHotels();
      getCashFromPlayer(player, amountToPay);
      break;

    case PAY_15:
      getCashFromPlayer(player, 15);
      break;

    case PAY_EACH_PLAYER_50:
      game.payEachPlayer50(player);
      break;

    case GET_OUT_OF_JAIL:
      player.setGetOutOfJail(this);
      break;
    }
  }

  private void getCashFromPlayer(AbstractPlayer player, int amount)
      throws BankruptcyException {
    player.getCash(amount);
  }

  private void movePlayer(AbstractPlayer player, int spacesToAdvance) {
    int newLocation = player.advance(spacesToAdvance);
    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    Location location = pf.getLocationAt(newLocation);
    player.setCurrentLocation(location);
    if (player.passedGo()) {
      player.receiveCash(200);
    }
  }

  private void advancePlayer(AbstractPlayer player, int locationIndex) {
    int spacesToAdvance = locationIndex - player.getLocationIndex();
    if (spacesToAdvance < 0) {
      // adjust if locationIndex < player location
      spacesToAdvance += 40;
    }
    movePlayer(player, spacesToAdvance);
  }

  public String toString() {
    switch (this) {
    case ADVANCE_TO_GO:
      return "Advance to Go (Collect $200).";

    case ADVANCE_TO_ILLINOIS:
      return "Advance to Illinois Ave.";

    case ADVANCE_TO_UTILITY:
      return "Advance token to nearest Utility. If unowned, "
          + "you may buy it from the Bank. If owned, throw dice and "
          + "pay owner a total ten times the amount thrown.";

    case ADVANCE_TO_RAILROAD1:
    case ADVANCE_TO_RAILROAD2:
      return "Advance token to the nearest Railroad and pay owner "
          + "twice the rental to which he/she is otherwise entitled. If "
          + "Railroad is unowned, you may buy it from the Bank.";

    case ADVANCE_TO_ST_CHARLES:
      return "Advance to St. Charles Place � if you pass Go, "
          + "collect $200.";

    case ADVANCE_TO_READING:
      return "Take a trip to Reading Railroad � if you pass Go "
          + "collect $200.";

    case ADVANCE_TO_BOARDWALK:
      return "Advance to Boardwalk.";

    case GO_BACK_3:
      return "Go back 3 spaces.";

    case GO_TO_JAIL:
      return "Go directly to Jail � do not pass Go, " + "do not collect $200.";

    case BANK_PAYS_50:
      return "Bank pays you dividend of $50.";

    case BANK_PAYS_150:
      return "Your building loan matures � collect $150.";

    case REPAIR_PROPERTY:
      return "Make general repairs on all your property � "
          + "for each house pay $25 � for each hotel $100.";

    case PAY_15:
      return "Speeding Fine $15.";

    case PAY_EACH_PLAYER_50:
      return "You have been elected chairman of the board � "
          + "pay each player $50.";

    case GET_OUT_OF_JAIL:
      return "Get out of Jail free � "
          + "this card may be kept until needed, or traded.";

    default:
      return "";
    }
  }
}
