package edu.uccs.ecgs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTable;

public class AddLotActionListener implements ActionListener {
  JComboBox comboBox;
  JTable table;
  AbstractPlayer player;
  
  public AddLotActionListener(AbstractPlayer player, JComboBox comboBox, JTable table) {
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
      PropertyFactory.getPropertyFactory().checkForMonopoly();

      MTableModel tm = (MTableModel) table.getModel();
      tm.setValueAt(lot, 0, 0);
    }
  }
}
