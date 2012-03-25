package edu.uccs.ecgs.ga;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.JOptionPane;

public class Main {

  /**
   * The number of individual players in a population. Can be overridden by
   * passing maxPlayers=nnn (where n is the max number of players desired) in
   * the command line args.
   */
  public static int maxPlayers = 1000;

  /**
   * The number of generations to propagate the genetic algorithm. Can be
   * overridden by passing numGenerations=nnn (where n is the number of desired
   * generations) in the command line args.
   */
  public static int numGenerations = 1000;

  /**
   * The number of matches per generation. The individuals in the population are
   * divided into subgroups and each subgroup plays a game. The set of all games
   * played by the subgroups is a single match. The number of games in a match
   * is maxPlayers/numPlayers. Since each individual plays one game per match,
   * numMatches is also the number of games played by each individual in the
   * population before the fitness of the players is evaluated.
   */
  public static int numMatches = 100;

  /**
   * The number of turns in a single game. Each individual gets this many rolls
   * of the dice before the game is terminated. This prevents the situation
   * where the game goes forever when no player is ever able to dominate the
   * game.
   */
  public static int maxTurns = 50;

  /**
   * The number of players that participate in a game.
   */
  public static int numPlayers = 4;

  /**
   * Should existing players be loaded from data files (loadFromDisk=true) or
   * generated from scratch (loadFromDisk=false).
   */
  public static boolean loadFromDisk = false;

  /**
   * When loading existing players from disk, this value indicates which
   * generation to load the players from. Normally this will be the last
   * generation to complete a round of games.
   */
  public static int lastGeneration = 417;

  /**
   * Whether or not to output debug information.
   */
  public static boolean debug = false;

  /**
   * Which chromosome types to use for a player. See
   * {@link edu.uccs.ecgs.ga.ChromoTypes} for valid values. Each type is
   * implemented by a concrete class which is used in
   * {@link edu.uccs.ecgs.ga.Monopoly#createPlayers() createPlayers}.
   */
  public static ChromoTypes chromoType = ChromoTypes.TGA;

  /**
   * Rate at which to mutate the genome.
   */
  public static double mutationRate = 0.01;

  /**
   * Whether or not to use a random seed. During testing, it helps to use the
   * same seed for each run (i.e., to not use a random seed), so that it is
   * easier to compare program execution from run to run.
   */
  public static boolean useRandomSeed = true;

  private static GAEngine gaEngine;

  private Gui gui = null;

  public static boolean useGui = false;

  public static boolean paused = true;

  public static boolean started = false;

  public static void main(String[] args)
  {
    Main main = new Main();
    main.start();
  }

  public void start()
  {
    InputStream inStream = this.getClass().getResourceAsStream("Main.properties");
    Properties props = new Properties();
    try {
      props.load(inStream);
      for(String key : props.stringPropertyNames()) {
        String value = props.getProperty(key);
        if (key.equals("maxPlayers")) {
          maxPlayers = Integer.parseInt(value);
        } else if (key.equals("numGenerations")) {
          numGenerations = Integer.parseInt(value);
        } else if (key.equals("numMatches")) {
          numMatches = Integer.parseInt(value);
        } else if (key.equals("maxTurns")) {
          maxTurns = Integer.parseInt(value);
        } else if (key.equals("numPlayers")) {
          numPlayers = Integer.parseInt(value);
        } else if (key.equals("loadFromDisk")) {
          loadFromDisk = Boolean.parseBoolean(value);
        } else if (key.equals("lastGeneration")) {
          lastGeneration = Integer.parseInt(value);
        } else if (key.equals("useGui")) {
          useGui = Boolean.parseBoolean(value);
        } else if (key.equals("debug")) {
          debug = Boolean.parseBoolean(value);
        } else if (key.equals("chromoType")) {
          chromoType = ChromoTypes.valueOf(value);
        } else if (key.equals("mutationRate")) {
          mutationRate = Double.parseDouble(value);
        } else if (key.equals("useRandomSeed")) {
          useRandomSeed = Boolean.parseBoolean(value);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    if (useGui) {
      String[][] fields = new String[][] { 
          { "Number of generations", "" + numGenerations },
          { "Number of matches per generation", "" + numMatches },
          { "Max number of turns per game", "" + maxTurns },
          { "Number of players in population", "" + maxPlayers },
          { "Number of players per game", "" + numPlayers },
          { "Load players from disk", "" + loadFromDisk }, 
          { "Generation to load", "" + lastGeneration },
          { "Debug", "" + debug }, 
          { "Chromosome Type (RGA, SGA, TGA)", "" + ChromoTypes.TGA.toString() },
          { "Mutation Rate", "" + mutationRate },
          { "Use random seed for games", "" + useRandomSeed} };

      gui = new Gui(this);
      gui.init(fields);
      
    } else {
      startSimulation();
    }
  }

  public void startSimulation()
  {
    started = true;
    paused = false;
    gaEngine = new GAEngine();

    Thread t = new Thread(gaEngine);
    t.start();

    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // gui.dispose();

    JOptionPane.showMessageDialog(null, "Monopoly simulation is complete",
        "Simulation Complete", JOptionPane.INFORMATION_MESSAGE);
    System.exit(0);
  }

  public static void pause()
  {
    paused = true;
  }

  public static void resume()
  {
    paused = false;
    gaEngine.resume();
  }

  public static void setExecutionValue(int index, String text)
  {
    switch (index) {
    case 0:
      //Number of generations
      numGenerations = Integer.parseInt(text);
      break;
    case 1:
      //Number of matches per generation
      numMatches = Integer.parseInt(text);
      break;
    case 2:
      // Max number of turns per game
      maxTurns = Integer.parseInt(text);
      break;
    case 3:
      // Number of players in population
      maxPlayers = Integer.parseInt(text);
      break;
    case 4:
      // Number of players per game
      numPlayers = Integer.parseInt(text);
      break;
    case 5:
      // Load players from disk 
      loadFromDisk = Boolean.parseBoolean(text);
      break;
    case 6:
      // Generation to load
      lastGeneration = Integer.parseInt(text);
      break;
    case 7:
      // Debug
      debug = Boolean.parseBoolean(text);
      break;
    case 8:
      // Chromosome Type
      chromoType = ChromoTypes.valueOf(text);
      break;
    case 9:
      // Mutation Rate
      mutationRate = Double.parseDouble(text);
      break;
    default:

    }
  }
}
