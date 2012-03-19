package edu.uccs.ecgs;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The Genetic Algorithm Engine. Create and manage the population of players.
 * Evaluate the fitness of the genome, and call the code to reproduce a
 * population.
 */
public class GAEngine implements Runnable {
  static Logger logger = Logger.getLogger("edu.uccs.ecgs");
  static Formatter formatter;

  private int generation = 0;
  private int matches = 0;
  private int gameNumber = 0;

  /**
   * The minimum score required for a player chromosome to advance to the next
   * generation.
   */
  private int minEliteScore;

  /**
   * The pool of available players in a generation. When a player is picked for
   * a game, the player is removed from playerPool and placed into playersDone.
   * After all games have been played, the players are placed back into the
   * playerPool for the next set of games.
   */
  private Vector<AbstractPlayer> playerPool = new Vector<AbstractPlayer>(
      Main.maxPlayers);
  private Vector<AbstractPlayer> playersDone = new Vector<AbstractPlayer>(
      Main.maxPlayers);

  Random r;

  public GAEngine() {
    r = new Random();
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    System.out.println("Monopoly seed      : " + seed);
    r.setSeed(seed);
    createPlayers();
  }

  private void createPlayers() {
    if (Main.loadFromDisk) {
      playerPool.addAll(PopulationPropagator.loadPlayers(Main.lastGeneration));
      Vector<AbstractPlayer> newPopulation = PopulationPropagator.evolve(
          playerPool, minEliteScore);
      playerPool.clear();
      playerPool.addAll(newPopulation);
    } else {
      // Create new population of players
      for (int i = 0; i < Main.maxPlayers; i++) {
        AbstractPlayer player = PlayerFactory.getPlayer(i, Main.chromoType);
        player.initCash(1500);
        playerPool.add(player);
      }
    }
  }

  private AbstractPlayer[] getFourPlayers() {
    AbstractPlayer[] players = new AbstractPlayer[] { null, null, null, null };
    for (int i = 0; i < players.length; i++) {
      players[i] = null;
      AbstractPlayer player = playerPool.remove(r.nextInt(playerPool.size()));
      players[i] = player;
      playersDone.add(player);
      assert !playerPool.contains(player);
    }
    return players;
  }

  @Override
  public void run() {
    try {
      initLogger();
      runGame();
    } catch (Throwable t) {
      t.printStackTrace();
      logger.log(Level.SEVERE, "", t);
    }
  }

  public void runGame() {
    if (Main.loadFromDisk) {
      generation = Main.lastGeneration + 1;
    }

    while (generation < Main.numGenerations) {
      matches = 0;
      while (matches < Main.numMatches) {
        gameNumber = 0;

        ArrayList<Thread> gameThreads = new ArrayList<Thread>();

        while (!playerPool.isEmpty()) {
          Monopoly game = new Monopoly(generation, matches, gameNumber,
              getFourPlayers());

          Thread t = new Thread(game);
          gameThreads.add(t);

          ++gameNumber;
        }

        for (Thread t : gameThreads) {
          t.start();
        }
        for (Thread t : gameThreads) {
          try {
            t.join();
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

        ++matches;

        playerPool.addAll(playersDone);
        playersDone.removeAllElements();
        resetPlayers();
      }

      dumpGenome();
      dumpPlayerFitness();

      Vector<AbstractPlayer> newPopulation = PopulationPropagator.evolve(
          playerPool, minEliteScore);
      playerPool.clear();
      playerPool.addAll(newPopulation);

      generation++;
    }
  }

  public void initLogger() {
    // This block configures the logger with handler and formatter
    formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage() + "\n";
      }
    };

    if (Main.debug) {
      logger.setLevel(Level.INFO);
    } else {
      logger.setLevel(Level.OFF);
    }
  }

  private void resetPlayers() {
    for (AbstractPlayer p : playerPool) {
      p.clearAllProperties();
      p.initCash(1500);
      p.resetAll();
    }
  }

  private void dumpPlayerFitness() {
    // create a map sorted by score, where the value is the number of
    // players with that score
    TreeMap<Integer, Integer> scores = new TreeMap<Integer, Integer>();
    // the set is used to dump a list of each player with the player's
    // individual score
    ArrayList<AbstractPlayer> fitness = new ArrayList<AbstractPlayer>(
        Main.maxPlayers);

    for (AbstractPlayer player : playerPool) {
      fitness.add(player);

      Integer val = scores.get(player.fitnessScore);
      if (val == null) {
        scores.put(player.fitnessScore, 1);
      } else {
        Integer newVal = val.intValue() + 1;
        scores.put(player.fitnessScore, newVal);
      }
    }

    // determine the minimum elite score
    minEliteScore = 0;
    int playerCount = 0;
    int maxPlayerCount = (int) (0.1 * Main.maxPlayers);
    for (Integer key : scores.descendingKeySet()) {
      playerCount += scores.get(key).intValue();
      if (playerCount >= maxPlayerCount) {
        break;
      }
      minEliteScore = key.intValue();
    }

    StringBuilder dir = Utility.getDirForGen(generation);

    File file = new File(dir.toString());
    if (!file.exists()) {
      file.mkdir();
    }

    // dump the score counts
    BufferedWriter bw = null;
    try {
      FileWriter fw = new FileWriter(dir.toString() + "/fitness_scores.csv");
      bw = new BufferedWriter(fw);

      // all scores
      int maxScore = Main.numMatches * 3;

      for (int i = 0; i <= maxScore; i++) {
        if (scores.containsKey(i)) {
          bw.write(i + "," + scores.get(i).intValue());
          bw.newLine();
        } else {
          bw.write(i + ",0");
          bw.newLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    Collections.sort(fitness);

    // dump individual fitness scores
    try {
      FileWriter fw = new FileWriter(dir.toString() + "/player_fitness.csv");
      bw = new BufferedWriter(fw);

      for (AbstractPlayer player : fitness) {
        bw.write(player.fitnessScore + "," + player.playerIndex);
        bw.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void dumpGenome() {
    for (AbstractPlayer player : playerPool) {
      StringBuilder fn1 = new StringBuilder(32);
      fn1.append("0000").append(player.playerIndex);
      String fn2 = fn1.reverse().substring(0, 4);
      StringBuilder fn3 = new StringBuilder(fn2).append("rylp").reverse();
      fn3.append(".dat");

      StringBuilder dir = Utility.getDirForGen(generation);

      File file = new File(dir.toString());
      if (!file.exists()) {
        file.mkdir();
      }

      DataOutputStream dos = null;
      try {
        FileOutputStream fos = new FileOutputStream(dir.toString() + "/"
            + fn3.toString());
        dos = new DataOutputStream(fos);
        player.dumpGenome(dos);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (dos != null) {
          try {
            dos.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public void pause() {
    // TODO Auto-generated method stub
  }

  public void resume() {
    // TODO Auto-generated method stub    
  }
}
