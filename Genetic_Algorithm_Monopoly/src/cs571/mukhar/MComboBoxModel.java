package cs571.mukhar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class MComboBoxModel extends DefaultComboBoxModel implements ActionListener {
  TreeSet<Location> data = new TreeSet<Location>();
  Location selected = PropertyFactory.getPropertyFactory().getLocationAt(0);
  boolean buttonClicked = false;
  PlayerPanel pp;
  
  public MComboBoxModel(PlayerPanel pp) {
    super();
    this.pp = pp;
  }

  @Override
  public void addElement(Object obj) {
    data.add((Location)obj);
    fireContentsChanged(this,0,data.size());
  }

  @Override
  public Object getElementAt(int i) {
    int count = i;
    Iterator<Location> lots = data.iterator();
    while (count-- > 0) {
      lots.next();
    }
    return lots.next();
  }

  @Override
  public int getIndexOf(Object obj) {
    int count = 0;

    Iterator<Location> lots = data.iterator();
    while (lots.hasNext()) {
      Location lot = lots.next();
      if (lot == obj) {
        return count;
      }
      ++count;
    }
    return -1;
  }

  @Override
  public Object getSelectedItem() {
    return selected;
  }

  @Override
  public int getSize() {
    return data.size();
  }

  @Override
  public void insertElementAt(Object obj, int i) {
    addElement(obj);
  }

  @Override
  public void removeAllElements() {
    data.clear();
  }

  @Override
  public void removeElement(Object obj) {
//    System.out.println("removeElement(Object obj) "+getClass().toString());
    int i = getIndexOf(obj) - 1;
    if (i < 0) i = 0;
    data.remove((Location)obj);
    if (buttonClicked) {
      setSelectedItem(getElementAt(i));
      fireContentsChanged(this, i, i);
      buttonClicked = false;
    } else {
      fireContentsChanged(this,0,data.size());
    }
  }

  @Override
  public void removeElementAt(int i) {    
    Object obj = getElementAt(i);
    
    i -= 1;
    if (i < 0) i = 0;
    
    data.remove(obj);

    setSelectedItem(getElementAt(i));
    this.fireContentsChanged(this, i, i);
  }

  @Override
  public void setSelectedItem(Object obj) {
    selected = (Location)obj;
    if (selected.getGroup() != PropertyGroups.SPECIAL) {
      pp.enableBuyButton();
    } else {
      pp.disableBuyButton();
    }
  }

  @Override
  public void actionPerformed(ActionEvent actionevent) {
    JComboBox cb = (JComboBox)actionevent.getSource();
    Location lot = (Location)cb.getSelectedItem();
    setSelectedItem(lot);
  }

}
