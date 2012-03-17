package cs571.mukhar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IdFieldActionListener implements ActionListener {
  PlayerPanel pp;
  
  public IdFieldActionListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (pp.player == null) {
      pp.player = new RGAPlayer(0);
      pp.cashField.setText("" + pp.player.cash);
      pp.cashField.setEditable(true);
      pp.cashField.addFocusListener(new CashFieldFocusListener(pp));
      pp.cashField.addActionListener(new CashFieldActionListener(pp));
      pp.addPlayer();
      pp.disableLoadButton();
    }
  }
}
