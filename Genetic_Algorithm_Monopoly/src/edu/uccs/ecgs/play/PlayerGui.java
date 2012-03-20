package edu.uccs.ecgs.play;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;

import edu.uccs.ecgs.Location;

@SuppressWarnings("serial")
public class PlayerGui extends JFrame {
  public static void main(String[] args) {
    new PlayerGui();
  }

  static PlayerPanel pp1 = new PlayerPanel(0); 
  static PlayerPanel pp2 = new PlayerPanel(1); 
  static PlayerPanel pp3 = new PlayerPanel(2); 
  static PlayerPanel pp4 = new PlayerPanel(3); 
  static String factoryKey = "edu.uccs.ecgs.play";

  public PlayerGui() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(2,2,2,2);

    getContentPane().add(pp1,gbc);
    getContentPane().add(pp2,gbc);
    getContentPane().add(pp3,gbc);
    getContentPane().add(pp4,gbc);

    pack();
    setVisible(true);
  }
  
  public static void removeLotFromList(Location lot) {
    pp1.lotComboBox.removeItem(lot);
    pp2.lotComboBox.removeItem(lot);
    pp3.lotComboBox.removeItem(lot);
    pp4.lotComboBox.removeItem(lot);
  }

	public static void addLotToList(Location lot) {
		pp1.lotComboBox.addItem(lot);
		pp2.lotComboBox.addItem(lot);
		pp3.lotComboBox.addItem(lot);
		pp4.lotComboBox.addItem(lot);
	}
}
