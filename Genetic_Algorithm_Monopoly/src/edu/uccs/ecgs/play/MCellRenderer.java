package edu.uccs.ecgs.play;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import edu.uccs.ecgs.ga.Location;

@SuppressWarnings("serial")
public class MCellRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable jtable,
                                                 Object obj,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int col) {
    Location lot = (Location)obj;
    if (lot.isMortgaged()) {
      setBackground(Color.YELLOW);
    } else {
      setBackground(Color.GREEN);
    }
    return super.getTableCellRendererComponent(jtable, obj, isSelected, hasFocus, row, col);
  }
  
}
