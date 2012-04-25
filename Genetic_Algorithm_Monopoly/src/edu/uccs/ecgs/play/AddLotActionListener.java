package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTable;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.PropertyFactory;
import edu.uccs.ecgs.ga.PropertyGroups;

public class AddLotActionListener implements ActionListener {
  JComboBox<Location> comboBox;
  JTable table;
  AbstractPlayer player;
  
  public AddLotActionListener(AbstractPlayer player, JComboBox<Location> comboBox, JTable table) {
    super();
    this.comboBox = comboBox;
    this.table = table;
    this.player = player;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    MComboBoxModel model = (MComboBoxModel)comboBox.getModel();
    model.buttonClicked = true;

    Location lot = (Location) comboBox.getSelectedItem();
    if (lot.getGroup() != PropertyGroups.SPECIAL) {
      PlayerGui.removeLotFromList(lot);
      lot.owner = player;
      PropertyFactory.getPropertyFactory(PlayerGui.factoryKey).checkForMonopoly();

      MTableModel tm = (MTableModel) table.getModel();
      tm.setValueAt(lot, 0, 0);
    }
  }
}
