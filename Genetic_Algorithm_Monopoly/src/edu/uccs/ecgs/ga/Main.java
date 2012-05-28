package edu.uccs.ecgs.ga;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

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
   * The fitness evaluator type to use for the algorithm.
   */
  public static FitEvalTypes fitnessEvaluator = FitEvalTypes.NUM_WINS;

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
  public static Level debug = Level.OFF;

  /**
   * Which chromosome types to use for a player. See
   * {@link edu.uccs.ecgs.ga.ChromoTypes} for valid values. Each type is
   * implemented by a concrete class which is used in
   * {@link edu.uccs.ecgs.ga.PlayerFactory#getPlayer(int index, ChromoTypes chromoType)}
   * .
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

  /**
   * The number of threads to use when running games. Each thread is used to run
   * 1 game, and when the game is complete, the thread can then run another
   * game.
   */
  public static int numThreads = 8;
  
  private static GAEngine gaEngine;

  private Gui gui = null;

  public static boolean useGui = false;

  public static boolean paused = true;

  public static boolean started = false;

  /**
   * How often to dump the player genome to data files. Player data will be
   * written to disk when generation mod dumpPeriod == 0. To dump every
   * generation, set the dumpPeriod = 1; To dump generation 0, 100, 200, etc.,
   * set the dumpPeriod = 100. NOTE: Player data is also dumped in the last
   * generation.
   */
  public static int dumpPeriod = 100;

  // TODO create other labels
  public static final String loadFromDiskLabel = "Load players from disk";
  public static final String randomSeedLabel = "Use random seed for games";
  public static final String dumpPeriodLabel = "Dump Player Data every n generations";

  public static void main(String[] args)
  {
    Main main = new Main();
    main.start();
  }

  public void start()
  {
    File f = new File("Main.properties");
    BufferedInputStream inStream = null;
    try {
      inStream = new BufferedInputStream(new FileInputStream(f));
      Properties props = new Properties();
      try {
        props.load(inStream);
        for(String key : props.stringPropertyNames()) {
          String value = props.getProperty(key);
          if (key.equalsIgnoreCase("maxPlayers")) {
            maxPlayers = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("numGenerations")) {
            numGenerations = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("numMatches")) {
            numMatches = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("maxTurns")) {
            maxTurns = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("numPlayers")) {
            numPlayers = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("fitnessEvaluator")) {
            fitnessEvaluator = FitEvalTypes.valueOf(value);
          } else if (key.equalsIgnoreCase("loadFromDisk")) {
            loadFromDisk = Boolean.parseBoolean(value);
          } else if (key.equalsIgnoreCase("lastGeneration")) {
            lastGeneration = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("useGui")) {
            useGui = Boolean.parseBoolean(value);
          } else if (key.equalsIgnoreCase("debug")) {
            debug = Level.parse(value);
          } else if (key.equalsIgnoreCase("chromoType")) {
            chromoType = ChromoTypes.valueOf(value);
          } else if (key.equalsIgnoreCase("mutationRate")) {
            mutationRate = Double.parseDouble(value);
          } else if (key.equalsIgnoreCase("useRandomSeed")) {
            useRandomSeed = Boolean.parseBoolean(value);
          } else if (key.equalsIgnoreCase("numThreads")) {
            numThreads = Integer.parseInt(value);
          } else if (key.equalsIgnoreCase("dumpPeriod")) {
            dumpPeriod = Integer.parseInt(value);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          inStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (FileNotFoundException ignored) {
      System.out.println("Could not find properties file " + f.getAbsolutePath());
      System.out.println("Using default program parameters.");
    }
    
    if (useGui) {
      Object[][] fields = new Object[][] { 
          { "Number of generations", "" + numGenerations },
          { "Number of matches per generation", "" + numMatches },
          { "Max number of turns per game", "" + maxTurns },
          { "Number of players in population", "" + maxPlayers },
          { "Number of players per game", "" + numPlayers },
          { "Fitness Evaluator", FitEvalTypes.values() },
          { loadFromDiskLabel,
              new Boolean[] { Boolean.TRUE, Boolean.FALSE } },
          { "Generation to load", "" + lastGeneration },
          { "Debug",
              new String[] { Level.OFF.toString(), Level.FINEST.toString(),
                  Level.INFO.toString(), Level.SEVERE.toString() } },
          { "Chromosome Type", ChromoTypes.values() },
          { "Mutation Rate", "" + mutationRate },
          { randomSeedLabel, new Boolean[] { Boolean.TRUE, Boolean.FALSE } },
          { "Number of threads (1 thread per concurrent game)", "" + numThreads },
          { dumpPeriodLabel, ""+dumpPeriod} };

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
    gaEngine = new GAEngine(this);

    Thread t = new Thread(gaEngine);
    t.start();

    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (useGui) {
      JOptionPane.showMessageDialog(null, "Monopoly simulation is complete",
          "Simulation Complete", JOptionPane.INFORMATION_MESSAGE);
    } else {
      System.out.println("Monopoly simulation is complete");
    }
    System.exit(0);
  }

  /**
   * Pause the simulation
   */
  public static void pause()
  {
    paused = true;
  }

  /**
   * Resume the simulation
   */
  public static void resume()
  {
    paused = false;
    gaEngine.resume();
  }

  /**
   * Utility method for GUI to set simulation parameters. Note that there are
   * two versions of this method, this one for Integers and Doubles, and another 
   * method for objects, which is why the switch statement does not cover all
   * possible index values.
   * 
   * @param index
   *          The index of the parameter in the class to set. The index is
   *          zero-based and based on the position of the parameter in the GUI,
   *          with the first parameter given index 0, the second parameter in
   *          the GUI is index 1, etc.
   * @param text
   *          The value of the parameter
   */
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
    case 7:
      // Generation to load
      lastGeneration = Integer.parseInt(text);
      break;
    case 10:
      // Mutation Rate
      mutationRate = Double.parseDouble(text);
      break;
    case 12:
      // number of threads
      numThreads = Integer.parseInt(text);
      break;
    case 13:
      // dump period
      dumpPeriod = Integer.parseInt(text);
      break;
    default:
      break;
    }
  }

  /**
   * Utility method for GUI to set simulation parameters from a Combo Box. Note
   * that there are two versions of this method, this one for objects and
   * another method for Integers and Doubles, which is why the switch statement
   * does not cover all possible index values.
   * 
   * @param index
   *          The index of the parameter in the class to set. The index is
   *          zero-based and based on the position of the parameter in the GUI,
   *          with the first parameter given index 0, the second parameter in
   *          the GUI is index 1, etc.
   * @param text
   *          The object used to set the parameter. For Fitness Evaluator and
   *          Chromosome Type, the object is an instance of the appropriate
   *          enum. For Log Level, the object is a String.
   */
  public static void setExecutionValue(int index, Object selectedItem) {
    switch (index) {
    case 5:
      // Fitness Evaluator
      fitnessEvaluator = (FitEvalTypes) selectedItem;
      break;
    case 6:
      // Load players from disk 
      loadFromDisk = (Boolean) selectedItem;
      break;
    case 8:
      // Debug
      debug = Level.parse(selectedItem.toString());
      break;
    case 9:
      // Chromosome Type
      chromoType = (ChromoTypes) selectedItem;
      break;
    case 11:
      // use random seed
      useRandomSeed = (Boolean) selectedItem;
      break;
    default:
    }
  }

  /**
   * Utility method for this object to set the Match field in the GUI
   * 
   * @param matches
   *          The current match number
   */
  public void setMatchNum(int matches)
  {
    if (useGui) {
      gui.matchNum.setText("" + matches);
    }
  }

  /**
   * Utility method for this object to set the Generation field in the GUI
   * 
   * @param matches
   *          The current generation number
   */
  public void setGenNum(int generation)
  {
    if (useGui) {
      gui.genNum.setText("" + generation);
    }
  }
}
