package org.squiddev.cctweaks.turtle;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.blocks.network.TileNetworkedWirelessBridge;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.cctweaks.core.registry.Module;
import org.squiddev.cctweaks.core.registry.Registry;

import java.util.Map;

/**
 * Turtle upgrade for the {@link TileNetworkedWirelessBridge} tile
 */
public class TurtleUpgradeWirelessBridge extends Module implements ITurtleUpgrade {
	@Override
	public int getUpgradeID() {
		return Config.Network.WirelessBridge.turtleId;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return "turtle." + CCTweaks.RESOURCE_DOMAIN + ".wirelessBridge.adjective";
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public ItemStack getCraftingItem() {
		return Config.Network.WirelessBridge.turtleEnabled ? new ItemStack(Registry.blockNetworked, 0) : null;
	}

	@Override
	public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
		return Config.Network.WirelessBridge.turtleEnabled ? new TurtleBinding(turtle, side).getModem().modem : null;
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction) {
		return null;
	}

	@Override
	public IIcon getIcon(ITurtleAccess turtle, TurtleSide side) {
		return BlockNetworked.bridgeSmallIcon;
	}

	/**
	 * Called on turtle update. Used to check if messages should be sent
	 *
	 * @param turtle Current turtle
	 * @param side   Peripheral side
	 */
	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
		if (Config.Network.WirelessBridge.turtleEnabled && !turtle.getWorld().isRemote) {
			IPeripheral peripheral = turtle.getPeripheral(side);
			if (peripheral != null && peripheral instanceof TurtleBinding.TurtleModemPeripheral) {
				((TurtleBinding.TurtleModemPeripheral) peripheral).update();
			}
		}
	}

	@Override
	public void init() {
		ComputerCraft.registerTurtleUpgrade(this);
	}

	/**
	 * This is really, really broken.
	 *
	 * The issue is two fold:
	 * - The only way to check if the node is removed is through {@link IPeripheral#detach(IComputerAccess)}. However,
	 * this means we are removing then attaching nodes and so forcing things to be recalculated when the world is
	 * unloaded.
	 * - Wrapping a turtle as a peripheral is hard.
	 */
	public static class TurtleBinding extends NetworkBindingWithModem {
		public final ITurtleAccess turtle;
		public final TurtleSide side;

		public TurtleBinding(ITurtleAccess turtle, TurtleSide side) {
			super(new TurtlePosition(turtle));
			this.turtle = turtle;
			this.side = side;

			load();
			connect();
		}

		@Override
		public BindingModem createModem() {
			return new TurtleModem();
		}

		@Override
		public TurtleModem getModem() {
			return (TurtleModem) modem;
		}

		public void load() {
			load(turtle.getUpgradeNBTData(side));
			getModem().load();
		}

		public void save() {
			save(turtle.getUpgradeNBTData(side));
			getModem().save();
			turtle.updateUpgradeNBTData(side);
		}

		/**
		 * Custom modem that allows modifying bindings
		 */
		public class TurtleModem extends BindingModem {
			protected int id = -1;

			public void load() {
				NBTTagCompound data = turtle.getUpgradeNBTData(side);
				if (data.hasKey("turtle_id")) id = data.getInteger("turtle_id");
			}

			public void save() {
				turtle.getUpgradeNBTData(side).setInteger("turtle_id", id);
			}

			/**
			 * Get the turtle as a peripheral
			 *
			 * @return The turtle peripheral
			 */
			@Override
			public Map<String, IPeripheral> getConnectedPeripherals() {
				/*
				 * Wrapping the turtle as a peripheral is really cool.
				 * However, there are some massive issues with recalculating peripherals
				 * on detach, infinite loops when creating, etc...
				 *
				 * When I enable this, remember to wrap the load, connect methods with FmlEvents.schedule
				 */
				/*ChunkCoordinates pos = turtle.getPosition();
				IPeripheral peripheral = PeripheralUtil.getPeripheral(turtle.getWorld(), pos.posX, pos.posY, pos.posZ, 0);
				if (peripheral == null) {
					id = -1;
					return Collections.emptyMap();
				} else if (id <= -1) {
					id = IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir(turtle.getWorld()), "computer/lastid_" + peripheral.getType() + ".txt"));
				}

				return Collections.singletonMap(peripheral.getType() + "_" + id, peripheral);*/
				return super.getConnectedPeripherals();
			}

			@Override
			protected BasicModemPeripheral createPeripheral() {
				return new TurtleModemPeripheral(this);
			}

			@Override
			public boolean canConnect(ForgeDirection side) {
				return side == ForgeDirection.UNKNOWN;
			}
		}

		/**
		 * Extension of modem with bindToCard and bindFromCard methods
		 */
		public class TurtleModemPeripheral extends BindingModemPeripheral implements IWorldNetworkNodeHost {
			public TurtleModemPeripheral(BindingModem modem) {
				super(modem);
			}

			@Override
			public String[] getMethodNames() {
				String[] methods = super.getMethodNames();
				String[] newMethods = new String[methods.length + 3];
				System.arraycopy(methods, 0, newMethods, 0, methods.length);


				int l = methods.length;
				newMethods[l] = "bindFromCard";
				newMethods[l + 1] = "bindToCard";

				return newMethods;
			}

			@Override
			public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
				String[] methods = super.getMethodNames();
				try {
					switch (method - methods.length) {
						case 0: { // bindFromCard
							ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
							if (stack != null && stack.getItem() instanceof IDataCard) {
								IDataCard card = (IDataCard) stack.getItem();
								if (TurtleBinding.this.load(stack, card)) {
									TurtleBinding.this.save();
									return new Object[]{true};
								}
							}
							return new Object[]{false};
						}
						case 1: { // bindToCard
							ItemStack stack = turtle.getInventory().getStackInSlot(turtle.getSelectedSlot());
							if (stack != null && stack.getItem() instanceof IDataCard) {
								IDataCard card = (IDataCard) stack.getItem();
								TurtleBinding.this.save(stack, card);
								return new Object[]{true};
							}
							return new Object[]{false};
						}
					}

					return super.callMethod(computer, context, method, arguments);
				} catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}
			}

			/**
			 * Handles the update tick.
			 */
			public void update() {
				modem.processQueue();
				if (pollChanged()) TurtleBinding.this.save();
			}

			@Override
			public IWorldNetworkNode getNode() {
				return TurtleBinding.this;
			}
		}
	}
}
