package cs571.mukhar;

public interface Player {
	void getRent(int amount);

	void payRent(int amount);

	boolean hasAtLeastCash(int amount);

	int[] rollDice();

	public int setNewLocation(int sum);
}
