package edu.uccs.ecgs;

public class Utility {
  public static StringBuilder getDirForGen(int generation) {
    StringBuilder dir = new StringBuilder("D:/Documents and Data/Kevin/CS571/Mono_SGA");
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
