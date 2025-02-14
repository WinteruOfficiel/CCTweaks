package org.squiddev.cctweaks.core.network.cable;

import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.core.network.modem.DirectionalPeripheralModem;

import java.util.Set;

public abstract class SingleModemCable extends CableWithInternalSidedParts {
	public abstract DirectionalPeripheralModem getModem();

	@Override
	public boolean canConnectInternally(ForgeDirection dir) {
		return getModem().getDirection() == dir.ordinal();
	}

	@Override
	public void connect() {
		super.connect();
		getAttachedNetwork().formConnection(this, getModem());
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		Set<INetworkNode> nodes = super.getConnectedNodes();
		nodes.add(getModem());
		return nodes;
	}
}
