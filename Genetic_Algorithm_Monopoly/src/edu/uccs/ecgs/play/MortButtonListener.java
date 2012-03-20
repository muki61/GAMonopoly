package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTable;

import edu.uccs.ecgs.ga.Location;

public class MortButtonListener implements ActionListener {
  JTable table;
  
  public MortButtonListener(JTable table) {
    super();
    this.table = table;
  }

  @Override
  public void actionPerformed(ActionEvent actionevent) {
    JButton button = (JButton) actionevent.getSource();
    Location lot = (Location) table.getModel().getValueAt(table.getSelectedRow(), 0);
    if (lot.isMortgaged()) {
      lot.setMortgaged(false);
      button.setText("Mortgage");
    } else {
      lot.setMortgaged();
      button.setText("Remove Mortgage");
    }
  }

}
