package edu.uccs.ecgs.ga;

public enum ChromoTypes {
  RGA {
    @Override
    public AbstractPlayer getPlayer(int index) {
      return new RGAPlayer(index);
    }
  },
  
  SGA  {
    @Override
    public AbstractPlayer getPlayer(int index) {
      return new SGAPlayer(index);
    }
  },
  
  TGA {
    @Override
    public AbstractPlayer getPlayer(int index) {
      return new TGAPlayer(index);
    }
  };  
  
  public abstract AbstractPlayer getPlayer(int index);
}
