package cs571.mukhar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Gui extends JFrame {

  JButton button = null;

  public static void main(String[] args) {
    new Gui();
  }

  public Gui () {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle(""+Main.chromoType);
    button = new JButton("Pause Monopoly");
    
    button.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (Main.paused) {
          //button currently says Run Monopoly
          button.setText("Pause Monopoly");
          Main.paused = false;
          Monopoly.getMonopolyGame().unpause();
        } else {
          //button currently says Pause Monopoly
          button.setText("Run Monopoly");
          Main.paused = true;
        }
      }});
    
    this.getContentPane().add(button);
    this.pack();
    this.setVisible(true);
  }
}
