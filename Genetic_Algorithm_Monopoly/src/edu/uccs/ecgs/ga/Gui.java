package edu.uccs.ecgs.ga;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Gui extends JFrame {

  JButton button = null;
  private Main program;
  private ArrayList<JComponent> textFields = new ArrayList<JComponent>();

  public JTextField matchNum = new JTextField(3);
  public JTextField genNum = new JTextField(4);

  public Gui(Main main) {
    this.program = main;
  }
  
  public void init(Object[][] fields) {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("Simulation with chromosome type " + Main.chromoType);

    this.getContentPane().add(getSimPanel(fields));
    this.pack();
    this.setVisible(true);
    assert program != null;
  }

  private JPanel getSimPanel(Object[][] fields) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = 0;
    gbc.insets = new Insets(2,2,2,2);

    for (Object[] field : fields) {
      JLabel label = new JLabel(field[0].toString());

      JComponent choice = null;
      if (field[1] instanceof String) {
        choice = new JTextField(field[1].toString(), 6);
      } else {
        choice = new JComboBox((Object[])field[1]);
      }

      textFields.add(choice);
      
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.ipadx = 0;
      panel.add(label, gbc);

      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx += 1;
      gbc.ipadx = 40;
      panel.add(choice, gbc);
      
      gbc.gridy += 1;
    }
    
    JPanel genPanel = new JPanel();
    genPanel.add(new JLabel("Generation: "));
    genPanel.add(genNum);
    genNum.setText("0");
    genNum.setEditable(false);
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.ipadx = 0;
    panel.add(genPanel, gbc);
    
    JPanel matchPanel = new JPanel();
    matchPanel.add(new JLabel("Match: "));
    matchPanel.add(matchNum);
    matchNum.setText("0");
    matchNum.setEditable(false);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx += 1;
    gbc.ipadx = 40;
    panel.add(matchPanel, gbc);
    gbc.gridy += 1;

    button = new JButton("Start All Games");
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(button, gbc);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (!Main.started) {
          button.setText("Pause All Games");
          Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
              setExecutionValues();
              startSimulation();
            }});
          t.start();
        } else if (Main.paused) {
          // button currently says Run Monopoly
          button.setText("Pause All Games");
          Main.resume();
        } else {
          // button currently says Pause Monopoly
          button.setText("Restart All Games");
          Main.pause();
        }
      }
    });

    return panel;
  }

  private void setExecutionValues()
  {
    int i = 0;
    for (JComponent choice : textFields) {
      if (choice instanceof JTextField) {
        JTextField field = (JTextField) choice;
        field.setEditable(false);
        String value = field.getText();
        Main.setExecutionValue(i++, value);
      } else {
        JComboBox<?> field = (JComboBox<?>) choice;
        field.setEditable(false);
        Main.setExecutionValue(i++, field.getSelectedItem());
      }
    }
    startSimulation();
  }

  private void startSimulation() {
    assert program!= null;
    program.startSimulation();
  }
}
