package edu.uccs.ecgs.play;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.uccs.ecgs.ga.AbstractPlayer;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.PropertyFactory;

@SuppressWarnings("serial")
public class PlayerPanel extends JPanel implements ListSelectionListener {
  public AbstractPlayer player;
  int index = 0;
  
  JButton loadButton = new JButton("Load Player");
  JButton mortgageButton = new JButton("Mortgage");
  JButton buyButton = new JButton("Buy Property?");
  JButton bailButton = new JButton("Pay Bail?");

  JLabel idLabel = new JLabel("Player ID");
  JTextField idField = new JTextField();
  
  JLabel cashLabel = new JLabel("Cash");
  JTextField cashField = new JTextField();
  
  JLabel lotLabel = new JLabel("Location");
  JComboBox<Location> lotComboBox;

  JLabel dummy = new JLabel();
  JButton addLotButton = new JButton("Add");
  JButton removeLotButton = new JButton("Remove");

  JLabel ownedLabel = new JLabel("Lots owned");
  JTable table = new JTable();
  MTableModel m = new MTableModel();
  JScrollPane scrollPane = new JScrollPane(table);

  @SuppressWarnings("unchecked")
  public PlayerPanel(int index) {
    this.index = index;
    
    GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    int i = 5;
    gbc.insets = new Insets(i,i,i,i);
    gbc.weightx = 1.0;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    loadButton.addActionListener(new LoadButtonListener(this));
    layout.setConstraints(loadButton, gbc);
    add(loadButton, gbc);

    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(idLabel, gbc);
    add(idLabel, gbc);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(idField, gbc);
    idField.setHorizontalAlignment(JTextField.RIGHT);
    idField.setEditable(true);
    idField.addActionListener(new IdFieldActionListener(this));
    add(idField, gbc);

    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(cashLabel, gbc);
    add(cashLabel, gbc);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(cashField, gbc);
    cashField.setHorizontalAlignment(JTextField.RIGHT);
    cashField.setEditable(false);
    add(cashField, gbc);

    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(lotLabel, gbc);
    add(lotLabel, gbc);

    lotComboBox = new JComboBox<Location>();
    MComboBoxModel model = new MComboBoxModel(this); 
    lotComboBox.setModel(model);
    for (Location lot : PropertyFactory.getPropertyFactory(PlayerGui.factoryKey).getLocations()) {
      model.addElement(lot);
    }
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(lotComboBox, gbc);
    add(lotComboBox, gbc);
    
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(dummy, gbc);
    add(dummy, gbc);

    AddLotActionListener al = new AddLotActionListener(player, lotComboBox, table);
    addLotButton.addActionListener(al);

    RemoveLotActionListener rl = new RemoveLotActionListener(lotComboBox, table);
    removeLotButton.addActionListener(rl);
    
    JPanel p = new JPanel();
    p.add(addLotButton);
    p.add(removeLotButton);
    p.setVisible(true);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(p, gbc);
    add(p,gbc);

    gbc.gridwidth = GridBagConstraints.RELATIVE;
    layout.setConstraints(ownedLabel, gbc);
    add(ownedLabel, gbc);

    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(10,100));
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    
    table.setModel(m);
    table.getSelectionModel().addListSelectionListener(this);
    table.setDefaultRenderer(Object.class, new edu.uccs.ecgs.play.MCellRenderer());
    layout.setConstraints(table, gbc);
    add(scrollPane, gbc);

    gbc.gridwidth = GridBagConstraints.RELATIVE;
    JLabel d2 = new JLabel();
    layout.setConstraints(d2, gbc);
    add(d2, gbc);
    
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mortgageButton.setEnabled(false);
    mortgageButton.addActionListener(new MortButtonListener(table));
    layout.setConstraints(mortgageButton, gbc);
    add(mortgageButton, gbc);
    
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    JLabel d3 = new JLabel();
    layout.setConstraints(d3, gbc);
    add(d3, gbc);
    
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    buyButton.setEnabled(false);
    buyButton.addActionListener(new BuyButtonListener(this, lotComboBox));
    layout.setConstraints(buyButton, gbc);
    add(buyButton, gbc);
    
    gbc.gridwidth = GridBagConstraints.RELATIVE;
    JLabel d4 = new JLabel();
    layout.setConstraints(d4, gbc);
    add(d4, gbc);
    
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    bailButton.addActionListener(new BailButtonListener(this));
    layout.setConstraints(bailButton, gbc);
    add(bailButton, gbc);

    this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setVisible(true);
  }

  //TODO Is this method needed??
  // public void addPlayer() {
  // GamePlayers.players[index] = player;
  // }

  public void disableLoadButton() {
    loadButton.setEnabled(false);
  }
  public void enableMortgageButton() {
    mortgageButton.setEnabled(true);
  }
  public void disableMortgageButton() {
    mortgageButton.setEnabled(false);
  }

  @Override
  public void valueChanged(ListSelectionEvent listselectionevent) {
    int row = table.getSelectedRow();
    if (row != -1) {
      enableMortgageButton();
      Location lot = (Location) m.getValueAt(row, 0);
      if (lot.isMortgaged()) {
        mortgageButton.setText("Remove mortgage");
      } else {
        mortgageButton.setText("Mortgage");
      }
    } else {
      disableMortgageButton();
    }
  }

  public void enableBuyButton() {
    if (player != null) {
      buyButton.setEnabled(true);
    }
  }

  public void disableBuyButton() {
    buyButton.setEnabled(false);
  }
}
