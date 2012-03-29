package edu.uccs.ecgs.ga;

public enum ChromoTypes {
  RGA {
    @Override
    public AbstractPlayer getPlayer(int index) {
      return new RGAPlayer(index);
    }

    @Override
    public AbstractPlayer getPlayer(int index, double[] chrNoOwners,
        double[] chrPlayerOwns, double[] chrOpponentOwns,
        double[] chrTwoOpponentOwns, double[][] chrJail) {
      return new RGAPlayer(index, chrNoOwners, chrPlayerOwns, chrOpponentOwns,
          chrTwoOpponentOwns, chrJail);
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

    @Override
    public AbstractPlayer getPlayer(int index, double[] chrNoOwners,
        double[] chrPlayerOwns, double[] chrOpponentOwns,
        double[] chrTwoOpponentOwns, double[][] chrJail) {
      return new TGAPlayer(index, chrNoOwners, chrPlayerOwns, chrOpponentOwns,
          chrTwoOpponentOwns, chrJail);
    }
  };  
  
  public abstract AbstractPlayer getPlayer(int index);

  public AbstractPlayer getPlayer(int index, double[] chrNoOwners,
      double[] chrPlayerOwns, double[] chrOpponentOwns,
      double[] chrTwoOpponentOwns, double[][] chrJail)
  {
    return null;
  }
}
