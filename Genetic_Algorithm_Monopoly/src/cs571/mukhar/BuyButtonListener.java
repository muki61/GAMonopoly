package cs571.mukhar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class BuyButtonListener implements ActionListener {
  JComboBox list;
  PlayerPanel pp;
  
  public BuyButtonListener(PlayerPanel pp, JComboBox list) {
    super();
    this.pp = pp;
    this.list = list;
  }

  @Override
  public void actionPerformed(ActionEvent actionevent) {
    Location lot = (Location) list.getSelectedItem();
    if (lot.getGroup() != PropertyGroups.SPECIAL) {
      if (pp.player.buyProperty(lot)) {
        JOptionPane.showMessageDialog(null, "Player decides to buy property",
            "Buy Property Decision", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(null, "Player decides to NOT buy property",
            "Buy Property Decision", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

}
