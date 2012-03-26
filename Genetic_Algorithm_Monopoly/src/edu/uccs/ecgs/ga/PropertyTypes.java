package edu.uccs.ecgs.ga;

import java.util.Properties;

public enum PropertyTypes {
  STREET {
    @Override
    public Location getProperty(String key, Properties properties) {
      return new PropertyLocation(key, properties);
    }
  },

  UTILITY {
    @Override
    public Location getProperty(String key, Properties properties) {
      return new UtilityLocation(key, properties);
    }
  },

  RAILROAD {
    @Override
    public Location getProperty(String key, Properties properties) {
      return new RailroadLocation(key, properties);
    }
  },

  SPECIAL {
    @Override
    public Location getProperty(String key, Properties properties) {
      return new SpecialLocation(key, properties);
    }
  };

  public abstract Location getProperty(String key, Properties properties);
}
