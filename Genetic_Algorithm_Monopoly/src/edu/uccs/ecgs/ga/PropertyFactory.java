package edu.uccs.ecgs.ga;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to manage the properties in a game of Monopoly
 */
public class PropertyFactory {

  private Location[] locations;
  private Properties properties = null;

  /**
   * A map of all the active factories. One instance of this class is created
   * for every game instance. The various instances of PropertyFactory are
   * stored in this map and accessed by a key which is unique to a game
   * instance.
   */
  static ConcurrentHashMap<String, PropertyFactory> factories = 
      new ConcurrentHashMap<String, PropertyFactory>();

  /**
   * Get the PropertyFactory for the given game key
   * @param gamekey The key that identifies the factory for a given game
   * @return A PropertyFactory instance
   */
  public static PropertyFactory getPropertyFactory(String gamekey) {
    PropertyFactory pf = factories.get(gamekey);

    if (pf == null) {
      pf = new PropertyFactory();
      factories.put(gamekey, pf);
    }
    return pf;
  }

  /**
   * Constructor
   */
  private PropertyFactory() {
    locations = new Location[40];

    properties = new Properties();

    Class<edu.uccs.ecgs.ga.PropertyFactory> c = PropertyFactory.class;
    InputStream fis = c.getResourceAsStream("locations.properties");

    try {
      properties.load(fis);
    } catch (IOException e) {
      //TODO Probably need to abort if the properties file cannot be loaded
      e.printStackTrace();
    } finally {
      close(fis);
    }

    createLocations(properties);
  }

  /**
   * Close the FileInputStream used in the constructor.
   * 
   * @param fis FileInputStream
   */
  private void close(InputStream fis) {
    if (fis != null) {
      try {
        fis.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param index The index of the desired location.
   * @return The Location object for the given index. 
   */
  public Location getLocationAt(int index) {
    return locations[index];
  }

  /**
   * Create the Location objects from the properties file.
   * @param properties Properties file with data to create Location objects.
   */
  private void createLocations(Properties properties) {
    String props = properties.getProperty("names");
    String[] keys = props.split(",");
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      String type = properties.getProperty(key + ".type");

      PropertyTypes propertyType = PropertyTypes.valueOf(type.toUpperCase());
      Location location = propertyType.getProperty(key, properties);
      locations[location.index] = location;
    }
  }

  /**
   * @param edge The edge for which Streets should be retrieved.
   * @return An array of Street Location objects on the given edge.
   */
  private Location[] getStreetsOnEdge(Edges edge) {
    Location[] edgeProperties = null;

    switch (edge) {
    case SOUTH:
      edgeProperties = new Location[] {
          locations[Spaces.SPACE_01.ordinal()],
          locations[Spaces.SPACE_03.ordinal()],
          locations[Spaces.SPACE_06.ordinal()],
          locations[Spaces.SPACE_08.ordinal()],
          locations[Spaces.SPACE_09.ordinal()] };
      break;
    case WEST:
      edgeProperties = new Location[] { 
          locations[Spaces.SPACE_11.ordinal()], 
          locations[Spaces.SPACE_13.ordinal()],
          locations[Spaces.SPACE_14.ordinal()], 
          locations[Spaces.SPACE_16.ordinal()], 
          locations[Spaces.SPACE_18.ordinal()], 
          locations[Spaces.SPACE_19.ordinal()] };
      break;
    case NORTH:
      edgeProperties = new Location[] { 
          locations[Spaces.SPACE_21.ordinal()], 
          locations[Spaces.SPACE_23.ordinal()],
          locations[Spaces.SPACE_24.ordinal()], 
          locations[Spaces.SPACE_26.ordinal()], 
          locations[Spaces.SPACE_27.ordinal()], 
          locations[Spaces.SPACE_29.ordinal()] };
      break;
    case EAST:
      edgeProperties = new Location[] { 
          locations[Spaces.SPACE_31.ordinal()], 
          locations[Spaces.SPACE_32.ordinal()],
          locations[Spaces.SPACE_34.ordinal()], 
          locations[Spaces.SPACE_37.ordinal()], 
          locations[Spaces.SPACE_39.ordinal()] };
    }

    return edgeProperties;
  }

  /**
   * Iterate through all properties and determine if any properties are part of
   * a monopoly. Properties are part of a monopoly when all properties in a
   * group have the same owner.
   */
  public void checkForMonopoly() {
    for (Location lot : locations) {
      lot.partOfMonopoly = false;
    }

    // First check all the groups with 3 streets
    // setup an array with all the indices to check
    int[][] monos = new int[][] {
        { Spaces.SPACE_06.ordinal(),
            Spaces.SPACE_08.ordinal(),
            Spaces.SPACE_09.ordinal() },

        { Spaces.SPACE_11.ordinal(),
            Spaces.SPACE_13.ordinal(),
            Spaces.SPACE_14.ordinal() },

        { Spaces.SPACE_16.ordinal(),
            Spaces.SPACE_18.ordinal(),
            Spaces.SPACE_19.ordinal() },

        { Spaces.SPACE_21.ordinal(),
            Spaces.SPACE_23.ordinal(),
            Spaces.SPACE_24.ordinal() },

        { Spaces.SPACE_26.ordinal(),
            Spaces.SPACE_27.ordinal(),
            Spaces.SPACE_29.ordinal() },

        { Spaces.SPACE_31.ordinal(),
            Spaces.SPACE_32.ordinal(),
            Spaces.SPACE_34.ordinal() } };

    // Check the streets. If the owners are not null and are all the same, then
    // set the partOfMonopoly property to true.
    for (int i = 0; i < monos.length; i++) {
      int x = monos[i][0];
      int y = monos[i][1];
      int z = monos[i][2];
      if (locations[x].owner != null) {
        if (locations[x].owner == locations[y].owner
            && locations[x].owner == locations[z].owner) {
          locations[x].partOfMonopoly = true;
          locations[y].partOfMonopoly = true;
          locations[z].partOfMonopoly = true;
        }
      }
    }

    // Now check the two groups with 2 streets
    // setup an array with all the indices to check
    monos = new int[][] {
        { Spaces.SPACE_01.ordinal(), Spaces.SPACE_03.ordinal() },
        { Spaces.SPACE_37.ordinal(), Spaces.SPACE_39.ordinal() } };

    // Check the streets. If the owners are not null and are all the same, then
    // set the partOfMonopoly property to true.
    for (int i = 0; i < monos.length; i++) {
      int x = monos[i][0];
      int y = monos[i][1];
      if (locations[x].owner != null) {
        if (locations[x].owner == locations[y].owner) {
          locations[x].partOfMonopoly = true;
          locations[y].partOfMonopoly = true;
        }
      }
    }
  }

  /**
   * Create a 5 or 6 bit index based on the streets on a given edge of the
   * board. When called for the SOUTH or EAST edges of the board, this will
   * return a 5 bit index. For WEST or NORTH a 6 bit index is returned. The
   * method iterates through the properties on the given edge in location order;
   * for each street that is owed by some player other than the player p, a bit
   * in the index is set, starting at bit 0 for the first street, then bit 1 for
   * the second street, etc.<br/>
   * <br/>
   * 
   * For example, assume that in location order, the 1st, 5th, and 6th streets
   * on the WEST or NORTH edge are owned by some other player; then this method
   * would return the index<br/>
   * <br/>
   * <code>
   * 110001
   * </code> <br/>
   * <br/>
   * which is the decimal value 49.
   * 
   * @param edge
   *          The edge to create the index for
   * @param p
   *          The player to exclude from the index
   * @return An index based on the streets on the given edge that are not owned
   *         by the player p
   */
  public int getIndexFromProperties(Edges edge, AbstractPlayer p) {
    int index = 0;
    Location[] properties = getStreetsOnEdge(edge);
    for (int i = 0; i < properties.length; i++) {
      if (properties[i].owner != null && properties[i].owner != p && !properties[i].isMortgaged()) {
        index = index + ((int) Math.pow(2, i));
      }
    }

    return index;
  }

  /**
   * Checks the streets in a group and returns a value identifying whether two
   * opponents own any streets in that group, only one opponent owns a street in
   * the group, only the player owns a street in the group, or no one owns a
   * street in the group.<br/>
   * <br/>
   * 
   * NOTE: The return values TWO_OPPONENTS and ONE_OPPONENT are exclusive of the
   * value SELF. That is, if the player owns a street in the group, and one or
   * two opponents own streets in the group, then this method returns
   * ONE_OPPONENT or TWO_OPPONENTS but not the value SELF. SELF is only returned
   * if the player owns a street and no other opponent owns a street.
   * 
   * @param location
   *          The location that identifies the group
   * @param player
   *          The player that is checking the group
   * @return Return a GroupOwners value
   */
  public GroupOwners getOwnerInformationForGroup(Location location,
      AbstractPlayer player) 
  {
    PropertyGroups targetGroup = location.getGroup();
    GroupOwners result = GroupOwners.NONE;
    Hashtable<AbstractPlayer, AbstractPlayer> owners = new Hashtable<AbstractPlayer, AbstractPlayer>();

    int numSelfOwners = 0;

    for (Location loc : locations) {
      if (loc == location)
        continue;

      if (targetGroup == loc.getGroup()) {
        AbstractPlayer owner = loc.getOwner(); // could be null
        
        if (player == owner) {
          ++numSelfOwners;
        } else if (owner != null) {
          owners.put(owner, owner);
        }
      }
    }

    if (owners.size() > 1) {
      result = GroupOwners.TWO_OPPONENTS;
    } else if (owners.size() == 1) {
      result = GroupOwners.ONE_OPPONENT;
    } else if (numSelfOwners == 1) {
      result = GroupOwners.SELF;
    }

    return result;
  }

  /**
   * Get the number of hotels in the property group that includes location.
   * 
   * @param location
   *          The property that will be used to determine the group
   * @return The number of hotels that are on all properties in the group that
   *         includes location
   */
  public int getNumHotelsInGroup(Location location) {
    int result = 0;

    for (Location loc : locations) {
      if (loc.getGroup() == location.getGroup()) {
        result =+ loc.getNumHotels();
      }
    }
    return result;
  }

  /**
   * Get the number of houses in the property group that includes location.
   * 
   * @param location
   *          The property that will be used to determine the group
   * @return The number of houses that are on all properties in the group that
   *         includes location
   */
  public int getNumHousesInGroup(Location location) {
    int result = 0;

    for (Location loc : locations) {
      if (loc.getGroup() == location.getGroup()) {
        result =+ loc.getNumHouses();
      }
    }
    return result;
  }

  /**
   * Get the number of monopolies that the player controls.
   * @param player The player to check
   * @return The number of monopolies that the player controls.
   */
  public int getNumMonopolies(AbstractPlayer player) {
    Hashtable<PropertyGroups, Boolean> groups = new Hashtable<PropertyGroups, Boolean>();

    for (Location loc : locations) {
      if (player == loc.owner) {
        // partOfMonopoly should have same true or false value for all streets in group
        // so putting same group into Hashtable multiple times should not change result
        groups.put(loc.getGroup(), loc.partOfMonopoly);
      }
    }

    // count the number of true values in hashtable
    int result = 0;
    for (Boolean b : groups.values()) {
      if (b) ++result;
    }

    return result;
  }

  /**
   * Compute an index based on the monopolies not controlled by the player. <br/><br/>
   * If 0 monopolies on given edge --> return 0 <br/>
   * If monopoly in first group only (location order) --> return 1 <br/>
   * If monopoly in second group only (location order) --> return 2 <br/>
   * If monopoly in both groups --> return 3 
   * 
   * @param p
   *          The player
   * @param edge
   *          The edge to check for monopolies
   * @return An index based on the monopolies on the given edge that are not controlled
   *         by the given player. 
   */
  public int getIndexFromMonopolies(AbstractPlayer p, Edges edge) {
    int result = 0;
    int index1 = 0;
    int index2 = 0;

    switch (edge) {
    case SOUTH:
      index1 = Spaces.SPACE_01.ordinal();
      index2 = Spaces.SPACE_06.ordinal();
      break;
    case WEST:
      index1 = Spaces.SPACE_11.ordinal();
      index2 = Spaces.SPACE_16.ordinal();
      break;
    case NORTH:
      index1 = Spaces.SPACE_21.ordinal();
      index2 = Spaces.SPACE_26.ordinal();
      break;
    case EAST:
      index1 = Spaces.SPACE_31.ordinal();
      index2 = Spaces.SPACE_37.ordinal();
      break;
    }

    if (locations[index1].owner != null && locations[index1].owner != p && locations[index1].partOfMonopoly) {
      result += 1;
    }
    if (locations[index2].owner != null && locations[index2].owner != p && locations[index2].partOfMonopoly) {
      result += 2;
    }

    return result;
  }

  /**
   * @return The array containing all the Location objects.
   */
  public Location[] getLocations() {
    return locations;
  }

  /**
   * Release the reference to a PropertyFactory, allowing its resources to be
   * released from the heap.
   * 
   * @param gamekey
   *          The key that identifies the PropertyFactory to release.
   */
  public static void releasePropertyFactory(String gamekey) {
    assert factories != null;
    assert gamekey != null;
    assert factories.containsKey(gamekey);

    factories.remove(gamekey);
  }

  /**
   * Ask if any property in the group is mortgaged
   * @param group The group which contains the locations to check
   * @return True --> If any property in the group is mortgaged<br/>
   *         False --> otherwise
   */
  public boolean groupIsMortgaged(PropertyGroups group) {
    for (Location location : locations) {
      if (location.getGroup() == group) {
        if (location.isMortgaged()) {
          return true;
        }
      }
    }
    return false;
  }
}
