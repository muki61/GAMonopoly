package edu.uccs.ecgs;

import java.io.IOException;

import javax.swing.JFileChooser;

public class Utility {
  private static String rootDir;
  
  public static StringBuilder getDirForGen(int generation) {
    if (rootDir == null || rootDir.equals("")) {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setDialogTitle("Select directory to save data files");
      int returnVal = JFileChooser.CANCEL_OPTION;
      while (returnVal != JFileChooser.APPROVE_OPTION) {
        returnVal = fc.showDialog(null, "Select");
        System.out.println(returnVal);
      }

      try {
        rootDir = fc.getSelectedFile().getCanonicalPath();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
   
    StringBuilder dir = new StringBuilder(rootDir);
    dir.append("/");
    
    if (generation < 10) {
      dir.append("Generation_0000" + generation);
    } else if (generation < 100) {
      dir.append("Generation_000" + generation);
    } else if (generation < 1000) {
      dir.append("Generation_00" + generation);
    } else if (generation < 10000) {
      dir.append("Generation_0" + generation);
    } else if (generation < 100000) {
      dir.append("Generation_" + generation);
    }
    return dir;
  }
}
