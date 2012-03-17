package edu.uccs.ecgs;

import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Main {

  /**
   * The number of individual players in a population. Can be overridden by
   * passing maxPlayers=nnn (where n is the max number of players desired) in
   * the command line args
   */
  public static int maxPlayers = 1000;

  /**
   * The number of generations to propagate the genetic algorithm. Can be
   * overridden by passing numGenerations=nnn (where n is the number of desired
   * generations) in the command line args
   */
  public static int numGenerations = 1000;
  
  /**
   * The number of matches (games) per generation. Each individual in the
   * population plays this many games before the fitness of the player
   * is evaluated.
   */
  public static int numMatches = 100;
  
  /**
   * The number of turns in a single game. Each individual gets this many rolls
   * of the dice before the game is terminated. This prevents the situation
   * where the game goes forever when no player is ever able to dominate the game.
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
   * 
   */
  public static int delay = 0;

  /**
   * Whether or not to output debug information.
   */
  public static boolean debug = false;
  
  /**
   * Which chromosome types to use for a player. See
   * {@link edu.uccs.ecgs.ChromoTypes} for valid values. Each type is
   * implemented by a concrete class which is used in
   * {@link edu.uccs.ecgs.Monopoly#createPlayers() createPlayers}.
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
   * Whether the program is paused or not. for internal use.
   */
  public static boolean paused; 

  public static void main(String[] args) {
    boolean useGui = true;
    Gui gui = null;

    Properties args2 = new Properties();

    for (int i = 0; i < args.length; i++) {
      String[] kv = args[i].split("=");
      args2.setProperty(kv[0].trim(), kv[1].trim());
    }

    for (String key : args2.keySet().toArray(new String[args2.size()])) {
      
      String value = args2.getProperty(key);
      
      if (key.equals("maxPlayers")) {
        maxPlayers = Integer.parseInt(value);
      } else if (key.equals("numGenerations")) {
        numGenerations = Integer.parseInt(value);
      } else if (key.equals("numMatches")) {  
        numMatches = Integer.parseInt(value);
      } else if (key.equals("maxTurns")) {
        maxTurns = Integer.parseInt(value);
      } else if (key.equals("loadFromDisk")) {
        loadFromDisk = Boolean.parseBoolean(value);
      } else if (key.equals("lastGeneration")) {
        lastGeneration = Integer.parseInt(value);
      } else if (key.equals("delay")) {
        delay = Integer.parseInt(value);
      } else if (key.equals("useGui")) {
        useGui = Boolean.parseBoolean(value);
      } else if (key.equals("debug")) {
        debug = Boolean.parseBoolean(value);
      }
    }

    if (useGui) {
      gui = new Gui();
      
      Monopoly game = Monopoly.getMonopolyGame();

      Thread t = new Thread(game);
      t.start();

      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      gui.dispose();
//      JOptionPane.showMessageDialog(null, "Monopoly simulation is complete");
      JOptionPane pane = new JOptionPane("Monopoly simulation is complete",
          JOptionPane.INFORMATION_MESSAGE);
      JDialog dialog = new JDialog();
      dialog.getContentPane().add(pane);
      dialog.pack();
      dialog.setVisible(true);
      try {
        Thread.sleep(1800000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.exit(0);
    } else {
      //use gui
    }
  }
}
