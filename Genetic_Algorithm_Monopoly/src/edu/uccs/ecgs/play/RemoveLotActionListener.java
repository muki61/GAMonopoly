package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTable;

import edu.uccs.ecgs.Location;
import edu.uccs.ecgs.PropertyFactory;

public class RemoveLotActionListener implements ActionListener {
  JTable table;

  public RemoveLotActionListener(JComboBox comboBox, JTable table) {
    super();
    this.table = table;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (table.getSelectedRow() == -1) {
      return;
    }

    MTableModel tm = (MTableModel) table.getModel();
    Location lot = (Location) tm.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
    lot.owner = null;
    PropertyFactory.getPropertyFactory(PlayerGui.factoryKey).checkForMonopoly();

    tm.removeItem(lot);
    PlayerGui.addLotToList(lot);
  }

}
