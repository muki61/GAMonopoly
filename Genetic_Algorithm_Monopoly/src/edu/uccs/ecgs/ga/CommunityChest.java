package edu.uccs.ecgs.ga;


public enum CommunityChest {

  ADVANCE_TO_GO, COLLECT_200, PAY_50, GET_OUT_OF_JAIL, GO_TO_JAIL,
  COLLECT_10_FROM_ALL, COLLECT_20, COLLECT_100, PAY_100, PAY_SCHOOL_50,
  COLLECT_25, REPAIR_PROPERTY, COLLECT_10, INHERIT_100, STOCK_50,
  HOLIDAY_FUND;

  public void processCard(AbstractPlayer player, Monopoly game) throws BankruptcyException {
    game.logger.info("Processing Community Chest Card '" + toString()
        + "' for player " + player.playerIndex);

    switch (this) {
    case ADVANCE_TO_GO:
      int locationIndex = 0;
      advancePlayer(player, locationIndex, game);
      break;

    case COLLECT_200:
      player.receiveCash(200);
      break;

    case PAY_50:
      player.getCash(50);
      break;

    case GET_OUT_OF_JAIL:
      player.setGetOutOfJail(this);
      break;

    case GO_TO_JAIL:
      PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
      Location location = pf.getLocationAt(10);
      player.enteredJail();
      player.setLocationIndex(location.index);
      player.setCurrentLocation(location);
      break;
    
    case COLLECT_10_FROM_ALL:
      game.collect10FromAll(player);
      break;

    case COLLECT_20:
      player.receiveCash(20);
      break;

    case COLLECT_100:
      player.receiveCash(100);
      break;

    case PAY_100:
      getCashFromPlayer(player, 100);
      break;

    case PAY_SCHOOL_50:
      getCashFromPlayer(player, 50);
      break;

    case COLLECT_25:
      player.receiveCash(25);
      break;

    case REPAIR_PROPERTY:
      int amountToPay = 0;
      amountToPay += 40 * player.getNumHouses();
      amountToPay += 115 * player.getNumHotels();
      getCashFromPlayer(player, amountToPay);
      break;

    case COLLECT_10:
      player.receiveCash(10);
      break;

    case INHERIT_100:
      player.receiveCash(100);
      break;

    case STOCK_50:
      player.receiveCash(50);
      break;

    case HOLIDAY_FUND:
      player.receiveCash(100);
      break;
    }    
  }

  private void getCashFromPlayer(AbstractPlayer player, int amount) throws BankruptcyException {
    player.getCash(amount);
  }

  public String toString() {
    switch (this) {
    case ADVANCE_TO_GO:
      return "Advance to Go (Collect $200)";
    case COLLECT_200:
      return "Bank error in your favor � collect $200";
    case PAY_50:
      return "Doctor's fees � Pay $50";
    case GET_OUT_OF_JAIL:
      return "Get out of jail free � this card may be kept until needed, or sold";
    case GO_TO_JAIL:
      return "Go to jail � go directly to jail � Do not pass Go, do not collect $200";
    case COLLECT_10_FROM_ALL: 
      return "It is your birthday Collect $10 from each player";
    case COLLECT_20:
      return "Income Tax refund � collect $20";
    case COLLECT_100:
      return "Life Insurance Matures � collect $100";
    case PAY_100:
      return "Pay Hospital Fees of $100";
    case PAY_SCHOOL_50:
      return "Pay School Fees of $50";
    case COLLECT_25:
      return "Receive $25 Consultancy Fee";
    case REPAIR_PROPERTY:
      return "You are assessed for street repairs � $40 per house, $115 per hotel";
    case COLLECT_10:
      return "You have won second prize in a beauty contest � collect $10";
    case INHERIT_100:
      return "You inherit $100";
    case STOCK_50:
      return "From sale of stock you get $50";
    case HOLIDAY_FUND:
      return "Holiday Fund matures - Receive $100 ";
    default:
      return "";
    }    
  }

  private void movePlayer(AbstractPlayer player, int spacesToAdvance, Monopoly game) {
    int newLocation = player.advance(spacesToAdvance);
    PropertyFactory pf = PropertyFactory.getPropertyFactory(game.gamekey);
    Location location = pf.getLocationAt(newLocation);
    player.setCurrentLocation(location);
    if (player.passedGo()) {
      player.receiveCash(200);
    }
  }

  private void advancePlayer(AbstractPlayer player, int locationIndex, Monopoly game) {
    int spacesToAdvance = locationIndex - player.getLocationIndex();
    if (spacesToAdvance < 0) {
      //adjust if locationIndex < player location
      spacesToAdvance += 40;
    }
    movePlayer (player, spacesToAdvance, game);

  }  
}
