package edu.uccs.ecgs.utility;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import javax.swing.JFileChooser;

import edu.uccs.ecgs.BitSetUtility;
import edu.uccs.ecgs.PropertyFactory;

public class GenomeReader {
	public static void main(String[] args) {
		GenomeReader gr = new GenomeReader();
		gr.readGenome();
	}

	private int jailLength = 0;
	private int lotLength = 40;
	private double[] chrNoOwners;
	private double[] chrPlayerOwns;
	private double[] chrOpponentOwns;
	private double[] chrTwoOpponentOwns;
	private double[][] chrJail;
	private int fitnessScore;
	private BitSet chrNoOwnersBS;

	private static final int numLots = 40;
	private static final int numJailCombos = 16;
	private static final int numBitsPerGene = 6;
	private static final int chromoLength = numLots * numBitsPerGene;
	private static final int jChromoLength = numJailCombos * numBitsPerGene;
	private BitSet chrPlayerOwnsBS;
	private BitSet chrOpponentOwnsBS;
	private BitSet chrTwoOpponentOwnsBS;
	private BitSet chrJailBS;

	public void readGenome() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select the player file to process");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showOpenDialog(null);
		String dir = "";
		String filename = "";
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dir = chooser.getSelectedFile().getParentFile().getPath();
			filename = chooser.getSelectedFile().getName();
		} else {
			return;
		}

		String playerIndex = filename.substring(4, 8);

		DataInputStream dis = null;
		
		try {
			FileInputStream fis = new FileInputStream(dir + "/" + filename);
			dis = new DataInputStream(fis);

			char[] header = new char[3];
			header[0] = dis.readChar();
			header[1] = dis.readChar();
			header[2] = dis.readChar();

			String headerStr = new String(header);
			if (headerStr.equals("RGA")) {
				jailLength = 64;
				readRealGenome(dis);
				dumpRealGenome(dir, playerIndex);
			} else if (headerStr.equals("TGA")) {
				jailLength = 4;
				readRealGenome(dis);
				dumpRealGenome(dir, playerIndex);
			} else if (headerStr.equals("SGA")) {
				readSimpleGenome(dis);
				dumpSimpleGenome(dir, playerIndex);
			} else {
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void dumpSimpleGenome(String dir, String playerIndex) {
		// TODO Auto-generated method stub
		StringBuilder outfileName = new StringBuilder(dir);
		outfileName.append("/").append("genome" + playerIndex + ".csv");

		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(outfileName.toString());
			bw = new BufferedWriter(fw);
			
			bw.write("Fitness," + fitnessScore);
			bw.newLine();

			// bw.write(",");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + PropertyFactory.getPropertyFactory().getLocationAt(i).name);
				}
			}
			bw.newLine();

			bw.write("No owners");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					String s = BitSetUtility.to6BitBinary(BitSetUtility.sixBits2Int(chrNoOwnersBS, i));
					bw.write(",\u0027" + s);
				}
			}
			bw.newLine();
			
			bw.write("Player owns");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					String s = BitSetUtility.to6BitBinary(BitSetUtility.sixBits2Int(chrPlayerOwnsBS, i));
					bw.write(",\u0027" + s);
				}
			}
			bw.newLine();
			
			bw.write("1 Opponent owns");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					String s = BitSetUtility.to6BitBinary(BitSetUtility.sixBits2Int(chrOpponentOwnsBS, i));
					bw.write(",\u0027" + s);
				}
			}
			bw.newLine();

            bw.write("2 Opponents own");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					String s = BitSetUtility.to6BitBinary(BitSetUtility.sixBits2Int(chrTwoOpponentOwnsBS, i));
					bw.write(",\u0027" + s);
				}
			}
			bw.newLine();

			for (int i = 0; i < 4; i++) {
				bw.write(","+i);
			}
			bw.newLine();

			for (int i = 0; i < 4; i++) {
				bw.write("Pay Bail " + i);
				for (int j = 0; j < 4; j++) {
					String s = BitSetUtility.to6BitBinary(BitSetUtility
							.sixBits2Int(chrJailBS, i*4+j));
					bw.write(",\u0027" + s);
				}
				bw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}

	private void dumpRealGenome(String dir, String playerIndex) {
		StringBuilder outfileName = new StringBuilder(dir);
		outfileName.append("/").append("genome" + playerIndex + ".csv");
		
		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(outfileName.toString());
			bw = new BufferedWriter(fw);
			
			bw.write("Fitness," + fitnessScore);
			bw.newLine();

			// bw.write(",");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + PropertyFactory.getPropertyFactory().getLocationAt(i).name);
				}
			}
			bw.newLine();

			bw.write("No owners");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + chrNoOwners[i]);
				}
			}
			bw.newLine();
			
			bw.write("Player owns");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + chrPlayerOwns[i]);
				}
			}
			bw.newLine();
			
			bw.write("1 Opponent owns");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + chrOpponentOwns[i]);
				}
			}
			bw.newLine();
			
			bw.write("2 Opponents own");
			for (int i = 0; i < 40; i++) {
				if (!skipIndex(i)) {
					bw.write("," + chrTwoOpponentOwns[i]);
				}
			}
			bw.newLine();

			int count = 0;
			for (int i = 0; i < jailLength; i++) {
				bw.write(","+i);
			}
			bw.newLine();

			for (double[] d1 : this.chrJail) {
				bw.write("Pay Bail " + count++);
				for (double d2 : d1) {
					bw.write("," + d2);
				}
				bw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean skipIndex(int i) {
		switch(i) {
        case 0:  // Go
        case 2:  // Community Chest
        case 4:  // Tax
        case 7:  // Chance
        case 10: // Jail
        case 17: // Community Chest
        case 20: // Free Parking
        case 22: // Chance
        case 30: // Go To Jail
        case 33: // Community Chest
        case 36: // Chance
        case 38: // Tax
          return true;
		}

		return false;
	}

	private void readSimpleGenome(DataInputStream dis) throws IOException {
		fitnessScore = dis.readInt();

		chrNoOwnersBS = new BitSet(chromoLength);
		chrPlayerOwnsBS = new BitSet(chromoLength);
		chrOpponentOwnsBS = new BitSet(chromoLength);
		chrTwoOpponentOwnsBS = new BitSet(chromoLength);
		chrJailBS = new BitSet(jChromoLength);

		readBitSet(chrNoOwnersBS, dis);
		readBitSet(chrPlayerOwnsBS, dis);
		readBitSet(chrOpponentOwnsBS, dis);
		readBitSet(chrTwoOpponentOwnsBS, dis);
		readBitSet(chrJailBS, dis);
	}

	private void readBitSet(BitSet bs, DataInputStream dis) {
		try {
			int i = dis.readInt();
			while (i != -1) {
				bs.set(i);
				i = dis.readInt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readRealGenome(DataInputStream dis) throws IOException {
		chrNoOwners = new double[lotLength];
		chrPlayerOwns = new double[lotLength];
		chrOpponentOwns = new double[lotLength];
		chrTwoOpponentOwns = new double[lotLength];
		chrJail = new double[jailLength][jailLength];

		fitnessScore = dis.readInt();

		for (int i = 0; i < chrNoOwners.length; i++) {
			chrNoOwners[i] = dis.readDouble();
		}
		for (int i = 0; i < chrPlayerOwns.length; i++) {
			chrPlayerOwns[i] = dis.readDouble();
		}
		for (int i = 0; i < chrOpponentOwns.length; i++) {
			chrOpponentOwns[i] = dis.readDouble();
		}
		for (int i = 0; i < chrTwoOpponentOwns.length; i++) {
			chrTwoOpponentOwns[i] = dis.readDouble();
		}

		for (int i = 0; i < jailLength; i++) {
			for (int j = 0; j < jailLength; j++) {
				chrJail[i][j] = dis.readDouble();
			}
		}
	}
}
