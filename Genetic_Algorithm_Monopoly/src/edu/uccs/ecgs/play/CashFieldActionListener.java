package edu.uccs.ecgs.play;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

public class CashFieldActionListener implements ActionListener {
  PlayerPanel pp;
  
  public CashFieldActionListener(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    JTextField source = (JTextField) arg0.getSource();
    pp.player.cash = Integer.parseInt(source.getText());
    source.setText(""+pp.player.cash);
//    System.out.println(""+pp.player.cash);
  }
}
