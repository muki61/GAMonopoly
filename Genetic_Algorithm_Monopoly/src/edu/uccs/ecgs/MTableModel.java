package edu.uccs.ecgs;

import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

public class MTableModel extends AbstractTableModel {
  TreeSet<Location> data = new TreeSet<Location>();

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public Object getValueAt(int arg0, int arg1) {
    int count = arg0;
    Iterator<Location> lots = data.iterator();
    while (count-- > 0) {
      lots.next();
    }
    return lots.next();
  }

  @Override
  public void setValueAt(Object arg0, int arg1, int arg2) {
    if (data.add((Location)arg0)) {
      fireTableDataChanged();
    }
  }

  public void removeItem(Location lot) {
    if (data.remove(lot)) {
      fireTableDataChanged();
    }
  }
}
