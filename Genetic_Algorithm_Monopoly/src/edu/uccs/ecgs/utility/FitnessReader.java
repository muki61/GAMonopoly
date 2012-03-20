package edu.uccs.ecgs.utility;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.uccs.ecgs.ga.Utility;

@SuppressWarnings("serial")
public class FitnessReader extends JFrame implements ActionListener {
  JTextField t2;
  JTextField t1;
  JButton button;

  public static void main(String[] args) {
    new FitnessReader();
  }

  public FitnessReader() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(layout);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(2, 2, 2, 2);

    JLabel l1 = new JLabel("Start generation");
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(l1, gbc);
    getContentPane().add(l1, gbc);

    t1 = new JTextField(5);
    t1.setHorizontalAlignment(JTextField.RIGHT);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(t1, gbc);
    getContentPane().add(t1, gbc);

    JLabel l2 = new JLabel("End generation");
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(l2, gbc);
    getContentPane().add(l2, gbc);

    t2 = new JTextField(5);
    t2.setHorizontalAlignment(JTextField.RIGHT);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(t2, gbc);
    getContentPane().add(t2, gbc);

    button = new JButton("Process fitness");
    button.addActionListener(this);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(button, gbc);
    getContentPane().add(button, gbc);

    pack();
    setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (t1.getText() == null || t1.getText().equals("") || t2.getText() == null
        || t2.getText().equals("")) {
      JOptionPane.showMessageDialog(this,
          "Must enter a starting and ending generation");
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select the root directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showOpenDialog(this);
    String dir = "";
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      dir = chooser.getSelectedFile().getAbsolutePath();
    } else {
      return;
    }

    int start = Integer.parseInt(t1.getText());
    int end = Integer.parseInt(t2.getText());

    StringBuilder outfileName = new StringBuilder(dir);
    outfileName.append("/").append("genfitness.csv");

    BufferedWriter bw = null;
    try {
      FileWriter fw = new FileWriter(outfileName.toString());
      bw = new BufferedWriter(fw);
      bw.write("Generation,Min Fitness, Max Fitness");
      bw.newLine();
    } catch (IOException e1) {
      e1.printStackTrace();
      return;
    }

    for (int i = start; i <= end; i++) {
      StringBuilder filename = Utility.getDirForGen(i);
      filename.append("/player_fitness.csv");

      BufferedReader br = null;
      try {
        FileReader fr = new FileReader(filename.toString());
        br = new BufferedReader(fr);

        String line = br.readLine();
        String minScore = "";
        String maxScore = "";
        while (line != null) {
          String[] fitness = line.split(",");
          if (minScore.equals("")) {
            minScore = fitness[0];
          } else {
            maxScore = fitness[0];
          }
          line = br.readLine();
        }

        bw.write(i + "," + minScore + "," + maxScore);
        bw.newLine();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (br != null) {
          try {
            br.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    if (bw != null) {
      try {
        bw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.exit(0);
  }
}
