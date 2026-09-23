package api.hbm.energymk2;

import api.hbm.tile.ILoadedTile;

/** DO NOT USE DIRECTLY! This is simply the common ancestor to providers and receivers, because all this behavior has to be excluded from conductors! */
public interface IEnergyHandlerMK2 extends IEnergyConnectorMK2, ILoadedTile {

	public long getPower();
	public void setPower(long power);
	public long getMaxPower();
}
