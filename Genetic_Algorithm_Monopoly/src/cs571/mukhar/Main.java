package cs571.mukhar;

import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Main {

  public static int maxPlayers = 1000;
  public static int numGenerations = 1000;
  public static int numMatches = 100;  
  public static int maxTurns = 50;
  public static int numPlayers = 4;
  public static boolean loadFromDisk = false;
  public static int lastGeneration = 417;
  public static int delay = 0;
  public static boolean debug = false;
  public static ChromoTypes chromoType = ChromoTypes.TGA;
  public static double mutationRate = 0.01;
  public static boolean useRandomSeed = true;

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
