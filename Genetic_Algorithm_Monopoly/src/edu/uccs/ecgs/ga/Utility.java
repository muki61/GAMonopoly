package edu.uccs.ecgs.ga;

import java.io.File;
import javax.swing.JFileChooser;

public class Utility {
  private static String rootDir;

  public static synchronized StringBuilder getDirForGen(int generation)
  {
    return getDirForGen(null, null, generation);
  }

  public static synchronized StringBuilder getDirForGen(ChromoTypes chromoType,
                                                        FitEvalTypes fitEval,
                                                        int generation)
  {
    File f = null;
    if (rootDir == null || rootDir.equals("")) {
      if (Main.useGui) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select directory to save data files");
        int returnVal = JFileChooser.CANCEL_OPTION;
        while (returnVal != JFileChooser.APPROVE_OPTION) {
          returnVal = fc.showDialog(null, "Select");
        }

        f = fc.getSelectedFile();
      } else {
        // not using gui
        f = new File("data");
      }

      rootDir = f.getAbsolutePath();
      System.out.println("Log dir: " + rootDir);
    }

    StringBuilder dir = new StringBuilder(rootDir);
    dir.append("/").append(chromoType.toString());
    dir.append("/").append(fitEval.get().getDirName()).append("/");

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
    
    f = new File(dir.toString());
    if (!f.exists()) {
      f.mkdirs();
    }

    return dir;
  }
}
