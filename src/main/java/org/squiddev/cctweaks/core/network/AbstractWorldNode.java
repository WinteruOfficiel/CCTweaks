package org.squiddev.cctweaks.core.network;

import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;

import java.util.Set;

/**
 * Basic world network node class with additional methods
 */
public abstract class AbstractWorldNode extends AbstractNode implements IWorldNetworkNode {
	@Override
	public boolean canConnect(ForgeDirection direction) {
		return true;
	}

	/**
	 * Get the adjacent nodes.
	 *
	 * This is primarily used when choosing a network to connect to.
	 * Use {@link org.squiddev.cctweaks.core.network.cable.BasicCable} if you need
	 * more advanced handling.
	 *
	 * This set can be modified in place.
	 */
	public Set<INetworkNode> getConnectedNodes() {
		return NetworkAPI.helpers().getAdjacentNodes(this);
	}

	/**
	 * Attempt to connect to {@link #getConnectedNodes()} using {@link NetworkHelpers#joinOrCreateNetwork(INetworkNode, Set)}
	 */
	public void connect() {
		NetworkAPI.helpers().joinOrCreateNetwork(this, getConnectedNodes());
	}

	/**
	 * Remove this node from the network
	 */
	public void destroy() {
		if (networkController != null) networkController.removeNode(this);
	}

	@Override
	public String toString() {
		IWorldPosition position = getPosition();
		return super.toString() + String.format(" (%s, %s, %s)", position.getX(), position.getY(), position.getZ());
	}
}
