package edu.uccs.ecgs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Gui extends JFrame {

  JButton button = null;
  private Main program;

  public static void main(String[] args) {
    String[][] fields = new String[][] { 
        { "Number of generations", "1000" },
        { "Number of matches per generation", "100" },
        { "Max number of turns per game", "50" },
        { "Number of players in population", "1000" },
        { "Number of players per game", "4" },
        { "Load players from disk", "false" }, 
        { "Generation to load", "0" },
        { "Debug", "false" },
        { "Chromosome Type (RGA, SGA, TGA)", "TGA" },
        { "Mutation Rate", "0.01" } };

    new Gui(null, fields);
  }

  public Gui(Main main, String[][] fields) {
    this.program = main;
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("Simulation with chromosome type " + Main.chromoType);

    this.getContentPane().add(getSimPanel(fields));
    this.pack();
    this.setVisible(true);
    assert program != null;
  }

  private JPanel getSimPanel(String[][] fields) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = 0;
    gbc.insets = new Insets(2,2,2,2);

    for (String[] field : fields) {
      JLabel label = new JLabel(field[0]);
      JTextField text = new JTextField(field[1], 3);

      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.ipadx = 0;
      panel.add(label, gbc);

      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx += 1;
      gbc.ipadx = 40;
      panel.add(text, gbc);
      
      gbc.gridy += 1;
    }

    button = new JButton("Start All Games");
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(button, gbc);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (!Main.started) {
          button.setText("Pause All Games");
          startSimulation();
        } else if (Main.paused) {
          // button currently says Run Monopoly
          button.setText("Pause All Games");
          Main.resume();
        } else {
          // button currently says Pause Monopoly
          button.setText("Restart All Games");
          Main.pause();
        }
      }
    });

    return panel;
  }
  
  private void startSimulation() {
    assert program!= null;
    program.startSimulation();
  }
}
