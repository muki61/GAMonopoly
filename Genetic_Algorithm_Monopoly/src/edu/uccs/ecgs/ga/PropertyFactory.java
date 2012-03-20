package edu.uccs.ecgs.ga;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TreeMap;


public class PropertyFactory {

  private Location[] locations;
  private Properties properties = null;

  static TreeMap<String, PropertyFactory> factories = new TreeMap<String, PropertyFactory>();
  
  public static PropertyFactory getPropertyFactory(String gamekey) {
    PropertyFactory pf = factories.get(gamekey);

    if (pf == null) {
      pf = new PropertyFactory(gamekey);
      factories.put(gamekey, pf);
    }
    return pf;
  }

  public void reset () {
    for (Location l : locations) {
      l.initialize();
    }
  }

  private PropertyFactory(String gamekey) {
    locations = new Location[40];

    if (properties == null) {
      properties = new Properties();

      Class<edu.uccs.ecgs.ga.PropertyFactory> c = PropertyFactory.class;
      InputStream fis = c.getResourceAsStream("/locations.properties");

      try {
        properties.load(fis);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    createLocations(properties, gamekey);
  }

  public Location getLocationAt(int index) {
    return locations[index];
  }

  private void createLocations(Properties properties, String gamekey) {
    String props = properties.getProperty("names");
    String[] keys = props.split(",");
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      String type = properties.getProperty(key + ".type");

      if (type.equals("property")) {
        Location location = new PropertyLocation(key, properties);
        locations[location.index] = location;
      } else if (type.equals("utility")) {
        Location location = new UtilityLocation(key, properties);
        locations[location.index] = location;
      } else if (type.equals("railroad")) {
        Location location = new RailroadLocation(key, properties);
        locations[location.index] = location;
      } else if (type.equals("special")) {
        Location location = new SpecialLocation(key, properties);
        locations[location.index] = location;
      }
    }
  }

  public Location[] getPropertiesOnEdge(Edges edge) {
    Location[] edgeProperties = null;

    switch (edge) {
    case SOUTH:
      edgeProperties = new Location[5];
      edgeProperties[0] = locations[1];
      edgeProperties[1] = locations[3];
      edgeProperties[2] = locations[6];
      edgeProperties[3] = locations[8];
      edgeProperties[4] = locations[9];
      break;
    case WEST:
      edgeProperties = new Location[6];
      edgeProperties[0] = locations[11];
      edgeProperties[1] = locations[13];
      edgeProperties[2] = locations[14];
      edgeProperties[3] = locations[16];
      edgeProperties[4] = locations[18];
      edgeProperties[5] = locations[19];
      break;
    case NORTH:
      edgeProperties = new Location[6];
      edgeProperties[0] = locations[21];
      edgeProperties[1] = locations[23];
      edgeProperties[2] = locations[24];
      edgeProperties[3] = locations[26];
      edgeProperties[4] = locations[27];
      edgeProperties[5] = locations[29];
      break;
    case EAST:
      edgeProperties = new Location[5];
      edgeProperties[0] = locations[31];
      edgeProperties[1] = locations[32];
      edgeProperties[2] = locations[34];
      edgeProperties[3] = locations[37];
      edgeProperties[4] = locations[39];
    }

    return edgeProperties;
  }

  public void checkForMonopoly() {
    for (Location lot : locations) {
      lot.partOfMonopoly = false;
    }

    // ugly brute force for Baltic and Med
    if (locations[1].owner != null) {
      if (locations[1].owner == locations[3].owner) {
        locations[1].partOfMonopoly = true;
        locations[3].partOfMonopoly = true;
      }
    }

    int[][] monos = new int[][] { { 6, 8, 9 }, { 11, 13, 14 }, { 16, 18, 19 },
        { 21, 23, 24 }, { 26, 27, 29 }, { 31, 32, 34 } };

    for (int i = 0; i < monos.length; i++) {
      int x = monos[i][0];
      int y = monos[i][1];
      int z = monos[i][2];
      if (locations[x].owner != null) {
        if (locations[x].owner == locations[y].owner
            && locations[y].owner == locations[z].owner) {
          locations[x].partOfMonopoly = true;
          locations[y].partOfMonopoly = true;
          locations[z].partOfMonopoly = true;
        }
      }
    }

    // Ugly brute force for Park Place and Boardwalk
    if (locations[37].owner != null) {
      if (locations[37].owner == locations[39].owner) {
        locations[37].partOfMonopoly = true;
        locations[39].partOfMonopoly = true;
      }
    }
  }

  public int getIndexFromProperties(Edges edge, AbstractPlayer p) {
    int index = 0;
    Location[] properties = this.getPropertiesOnEdge(edge);
    for (int i = 0; i < properties.length; i++) {
      if (properties[i].owner != null && properties[i].owner != p && !properties[i].isMortgaged()) {
        index = index + ((int) Math.pow(2, i));
      }
    }

    return index;
  }

  public GroupOwners getOwnerInformationForGroup(Location location,
      AbstractPlayer player) {
    PropertyGroups targetGroup = location.getGroup();
    GroupOwners result = GroupOwners.NONE;

    int numOtherOwners = 0;
    int numSelfOwners = 0;

    for (Location loc : locations) {
      if (loc == location)
        continue;

      if (targetGroup == loc.getGroup()) {
        if (loc.owner != null && loc.getOwner() != player) {
          ++numOtherOwners;
        } else {
          ++numSelfOwners;
        }
      }
    }

    if (numOtherOwners > 1) {
      result = GroupOwners.TWO_OPPONENTS;
    } else if (numOtherOwners == 1) {
      result = GroupOwners.ONE_OPPONENT;
    } else if (numSelfOwners > 0) {
      result = GroupOwners.SELF;
    }

    return result;
  }

  public int getNumHotelsInGroup(Location location) {
    int result = 0;

    for (Location loc : locations) {
      if (loc.getGroup() == location.getGroup()) {
        result =+ loc.getNumHotels();
      }
    }
    return result;
  }

  public int getNumHousesInGroup(Location location) {
    int result = 0;

    for (Location loc : locations) {
      if (loc.getGroup() == location.getGroup()) {
        result =+ loc.getNumHouses();
      }
    }
    return result;
  }

//  public int getTotalNumMonopolies() {
//    int result = 0;
//    
//    for (AbstractPlayer player : GamePlayers.players) {
//      result += getNumMonopolies(player);
//    }
//
//    return result;
//  }

  public int getNumMonopolies(AbstractPlayer player) {
    Hashtable<PropertyGroups, Boolean> h = new Hashtable<PropertyGroups, Boolean>();
    
    for (Location loc : locations) {
      if (loc.partOfMonopoly && loc.owner == player) {
        h.put(loc.getGroup(), true);
      }
    }    

    int result = 0;
    for (Boolean b : h.values()) {
      if (b) ++result;
    }

    return result;
  }

  public int getNumOtherMonopolies(AbstractPlayer p, Edges edge) {
    int result = 0;
    int index1 = 0;
    int index2 = 0;

    switch (edge) {
    case SOUTH:
      index1 = 1;
      index2 = 6;
      break;
    case WEST:
      index1 = 11;
      index2 = 16;
      break;
    case NORTH:
      index1 = 21;
      index2 = 26;
      break;
    case EAST:
      index1 = 31;
      index2 = 37;
      break;
    }
    
    if (locations[index1].owner != null && locations[index1].owner != p && locations[index1].partOfMonopoly) {
      ++result;
    }
    if (locations[index2].owner != null && locations[index2].owner != p && locations[index2].partOfMonopoly) {
      ++result;
    }

    return result;
  }

  public Location[] getLocations() {
    return locations;
  }
}
