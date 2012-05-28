package edu.uccs.ecgs.ga;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;


public class PopulationPropagator {
  private static Random r = new Random();

  // Fitness is normalized to a 0 to 100 scale, so average fitness should
  // be around 50. Add a buffer fact of 1.
  private static final int avgPointsPerGame = 51;

  private static final int rouletteSize = avgPointsPerGame * Main.maxPlayers;
  
  private static PopulationPropagator _this = new PopulationPropagator();

  private PopulationPropagator() {
    long seed = 1241797664697L;
    if (Main.useRandomSeed) {
      seed = System.currentTimeMillis();
    }
    r.setSeed(seed);    
  }
  
  public PopulationPropagator getEvolver() {
    return _this;
  }
  
  public static Vector<AbstractPlayer> evolve(Vector<AbstractPlayer> population,
                                              int minEliteScore)
  {
    Vector<AbstractPlayer> newPopulation = new Vector<AbstractPlayer>(
        Main.maxPlayers);

    // Each player gets one entry in the roulette for each point of fitness, so
    // make the roulette have has many slots as the total fitness in a
    // generation
    Vector<AbstractPlayer> roulette = new Vector<AbstractPlayer>(rouletteSize);

    for (AbstractPlayer player : population) {
      // elitism - pick the top 10% best players (might be more than 10% due to
      // duplicate fitness)
      if (player.getFitness() >= minEliteScore) {
        newPopulation.add(player);
      }

      //fill up the roulette wheel
      for (int i = 0; i < player.getFitness(); i++) {
        roulette.add(player);
      }
    }

    AbstractPlayer parent1 = null;
    AbstractPlayer parent2 = null;

    //now take the next 30% based on tournament selection with replacement
    int count = (int)(0.3 * Main.maxPlayers);
    while (--count >= 0) {
      parent1 = population.remove(r.nextInt(population.size()));
      parent2 = population.remove(r.nextInt(population.size()));

      //add the parents back to the general population
      population.add(parent1);
      population.add(parent2);

      try {
        if (parent1.getFitness() >= parent2.getFitness()) {
          newPopulation.add((AbstractPlayer) parent1.clone());
        } else {
          newPopulation.add((AbstractPlayer) parent2.clone());
        }
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }

    //take another 30% by tournament selection with mutation
    count = (int)(0.3 * Main.maxPlayers);
    while (--count >= 0) {
      parent1 = population.remove(r.nextInt(population.size()));
      parent2 = population.remove(r.nextInt(population.size()));

      //add the parents back to the general population
      population.add(parent1);
      population.add(parent2);

      AbstractPlayer child = null;
      if (parent1.getFitness() >= parent2.getFitness()) {
        child = parent1.copyAndMutate();
        newPopulation.add(child);
      } else {
        child = parent2.copyAndMutate();
        newPopulation.add(child);
      }
    }
    
    AbstractPlayer[] children = null;

    //now create the rest of the population by reproduction
    while (newPopulation.size() < Main.maxPlayers) {
      //pick two parents 
      parent1 = population.remove(r.nextInt(population.size()));
      parent2 = population.remove(r.nextInt(population.size()));

      //add the parents back to the general population
      population.add(parent1);
      population.add(parent2);
      
      children = parent1.createChildren(parent2, 0);

      // add children
      for (int i = 0; i < children.length; i++) {
        newPopulation.add(children[i]);
      }
    }

    while (newPopulation.size() > Main.maxPlayers) {
      newPopulation.remove(newPopulation.size() - 1);
    }

    int index = 0;
    for (AbstractPlayer player : newPopulation) {
      player.setIndex(index);
      player.resetFitness();
      ++index;
    }

    //release references
    roulette.clear();
    
    assert newPopulation.size() == Main.maxPlayers;
    return newPopulation;
  }
  
  public static Vector<AbstractPlayer> loadPlayers(int gen) {
    int playerCount = 0;

    Vector<AbstractPlayer> newPopulation = new Vector<AbstractPlayer>(Main.maxPlayers);

    StringBuilder dir = Utility.getDirForGen(gen);

    File theDirectory = new File(dir.toString());
    String[] files = theDirectory.list();
    
    for (String filename : files) {
      if (playerCount == Main.maxPlayers) {
        break;
      }

      // filenames run from plyr0000.dat to plyr0999.dat
      if (filename.matches("player\\d\\d\\d\\d.dat")) {
        //System.out.println("Found matching filename: " + filename);
        int index = Integer.parseInt(filename.substring(4, 8));
        AbstractPlayer player = loadPlayer(dir + "/" + filename, index);
        newPopulation.add(player);
        
        playerCount++;
      }
    }

    return newPopulation;
  }

  public static AbstractPlayer loadPlayer(String filename, int index) {
    DataInputStream dis = null;
    AbstractPlayer player = null;

    try {
      FileInputStream fis = new FileInputStream(filename);
      dis  = new DataInputStream(fis);

      char[] header = new char[3];
      header[0] = dis.readChar();
      header[1] = dis.readChar();
      header[2] = dis.readChar();
      
      String headerStr = new String(header);

      ChromoTypes chromoType = ChromoTypes.valueOf(headerStr);
      player = chromoType.getPlayer(index, dis);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (dis != null) {
        try {
          dis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return player;
  }
}
