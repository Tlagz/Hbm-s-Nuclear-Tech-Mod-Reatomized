package api.hbm.tile;

/** Heaters (firebox, oven...) that machines placed on top of them draw heat (TU) from */
public interface IHeatSource {

	public int getHeatStored();

	/**
	 * Removes heat from the system. Implementation has to include the checks preventing the heat from going into the negative.
	 * @param heat
	 */
	public void useUpHeat(int heat);
}
