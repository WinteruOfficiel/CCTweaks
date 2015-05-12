package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TileMultipart;
import com.google.common.collect.Iterables;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartModemWithCableIntermediate extends PartCable implements IPeripheralHost {
	public static final String NAME = CCTweaks.NAME + ":networkModemCable";

	private final PartModem modem;

	public PartModemWithCableIntermediate(TileCable modem) {
		this.modem = new PartModem(modem);
	}

	public PartModemWithCableIntermediate() {
		this.modem = new PartModem();
	}

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public void bind(TileMultipart tile) {
		super.bind(tile);
		modem.bind(tile);
	}

	@Override
	public void onAdded() {
		if (!world().isRemote) {
			this.scheduleTick(0);
		}
	}

	@Override
	public void scheduledTick() {
		if (!world().isRemote) {
			BlockCoord pos = new BlockCoord(x(), y(), z());
			World world = world();
			tile().remPart(this);
			TileMultipart.addPart(world, pos, modem);
			TileMultipart.addPart(world, pos, new PartCable());
		}
	}

	@Override
	public int getSlotMask() {
		return modem.getSlotMask() | super.getSlotMask();
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return Iterables.concat(super.getOcclusionBoxes(), modem.getOcclusionBoxes());
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		return Iterables.concat(super.getSubParts(), modem.getSubParts());
	}

	@Override
	public Cuboid6 getBounds() {
		return super.getBounds().enclose(modem.getBounds());
	}

	@Override
	public void onNeighborChanged() {
		super.onNeighborChanged();
		modem.onNeighborChanged();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		super.renderStatic(pos, pass);
		modem.renderStatic(pos, pass);
		return true;
	}

	@Override
	public Iterable<ItemStack> getDrops() {
		List<ItemStack> stacks = new ArrayList<ItemStack>(2);
		stacks.add(PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1));
		stacks.add(PeripheralItemFactory.create(PeripheralType.Cable, null, 1));
		return stacks;
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		super.writeDesc(packet);
		modem.writeDesc(packet);
	}

	@Override
	public void readDesc(MCDataInput packet) {
		super.readDesc(packet);
		modem.readDesc(packet);
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return super.canBeVisited(from) && modem.canBeVisited(from);
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
		return super.canVisitTo(to) && modem.canVisitTo(to);
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		super.receivePacket(packet, distanceTravelled);
		modem.receivePacket(packet, distanceTravelled);
	}

	@Override
	public void networkInvalidated() {
		super.networkInvalidated();
		modem.networkInvalidated();
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		Map<String, IPeripheral> peripherals = super.getConnectedPeripherals();

		Map<String, IPeripheral> modemPerips = modem.getConnectedPeripherals();
		if (modemPerips != null) peripherals.putAll(modemPerips);

		return peripherals;
	}

	@Override
	public Object lock() {
		return modem.lock();
	}

	@Override
	public IPeripheral getPeripheral(int side) {
		return modem.getPeripheral(side);
	}
}
