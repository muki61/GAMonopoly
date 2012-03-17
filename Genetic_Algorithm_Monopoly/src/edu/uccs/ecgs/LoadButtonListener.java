package edu.uccs.ecgs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

public class LoadButtonListener implements ActionListener {

  private PlayerPanel playerPanel;
  private final JFileChooser fc = new JFileChooser();

  public LoadButtonListener(PlayerPanel p) {
    playerPanel = p;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    int returnVal = fc.showOpenDialog(playerPanel);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      int index = Integer.parseInt(file.getName().substring(4,8));
      playerPanel.player = PopulationPropagator.loadPlayer(file.getAbsolutePath(), index);
      playerPanel.idField.setText(""+playerPanel.player.playerIndex);
      playerPanel.idField.setEditable(false);
      playerPanel.cashField.setText(""+playerPanel.player.cash);
      playerPanel.cashField.setEditable(true);
      playerPanel.cashField.addFocusListener(new CashFieldFocusListener(playerPanel));
      playerPanel.cashField.addActionListener(new CashFieldActionListener(playerPanel));
      playerPanel.addPlayer();
      playerPanel.disableLoadButton();
    }
  }
}
