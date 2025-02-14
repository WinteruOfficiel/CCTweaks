package org.squiddev.cctweaks.core;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * The main config class
 */
@org.squiddev.configgen.Config(languagePrefix = "gui.config.cctweaks.")
public final class Config {
	public static Configuration configuration;
	public static Set<String> turtleDisabledActions;

	public static void init(File file) {
		org.squiddev.cctweaks.lua.ConfigForgeLoader.init(file);
		org.squiddev.cctweaks.core.ConfigForgeLoader.init(org.squiddev.cctweaks.lua.ConfigForgeLoader.getConfiguration());
	}

	public static void sync() {
		org.squiddev.cctweaks.core.ConfigForgeLoader.doSync();
		org.squiddev.cctweaks.lua.ConfigForgeLoader.sync();
	}

	@OnSync
	public static void onSync() {
		configuration = org.squiddev.cctweaks.lua.ConfigForgeLoader.getConfiguration();

		// Handle generation of HashSets, etc...
		Set<String> disabledActions = turtleDisabledActions = new HashSet<String>();
		for (String action : Turtle.disabledActions) {
			disabledActions.add(action.toLowerCase());
		}

		Computer.computerUpgradeCrafting &= Computer.computerUpgradeEnabled;

		Network.WirelessBridge.crafting &= Network.WirelessBridge.enabled;
		Network.WirelessBridge.turtleEnabled &= Network.WirelessBridge.enabled;
	}

	/**
	 * Computer tweaks and items.
	 */
	public static final class Computer {
		/**
		 * Enable upgrading computers.
		 */
		@DefaultBoolean(true)
		public static boolean computerUpgradeEnabled;

		/**
		 * Enable crafting the computer upgrade.
		 * Requires computerUpgradeEnabled.
		 */
		@DefaultBoolean(true)
		@RequiresRestart
		public static boolean computerUpgradeCrafting;

		/**
		 * Enable using the debug wand.
		 */
		@DefaultBoolean(true)
		public static boolean debugWandEnabled;
	}

	/**
	 * Turtle tweaks and items.
	 */
	public static final class Turtle {
		/**
		 * Amount of RF required for one refuel point
		 * Set to 0 to disable.
		 */
		@DefaultInt(100)
		@Range(min = 0)
		public static int fluxRefuelAmount;

		/**
		 * Amount of Eu required for one refuel point.
		 * Set to 0 to disable.
		 */
		@DefaultInt(25)
		@Range(min = 0)
		public static int euRefuelAmount;

		/**
		 * Fun actions for turtle names
		 */
		@DefaultBoolean(true)
		public static boolean funNames;

		/**
		 * Disabled turtle actions:
		 * (compare, compareTo, craft, detect, dig,
		 * drop, equip, inspect, move, place,
		 * refuel, select, suck, tool, turn).
		 */
		public static String[] disabledActions;

		/**
		 * Various tool host options
		 */
		public static class ToolHost {
			/**
			 * Enable the Tool Host
			 */
			@DefaultBoolean(true)
			public static boolean enabled;

			/**
			 * Enable the Tool Manipulator
			 */
			@DefaultBoolean(true)
			public static boolean advanced;

			/**
			 * Enable crafting the Tool Host
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean crafting;

			/**
			 * Upgrade Id
			 */
			@DefaultInt(332)
			@RequiresRestart
			@Range(min = 0)
			public static int upgradeId;

			/**
			 * Upgrade Id for Tool Manipulator
			 */
			@DefaultInt(333)
			@RequiresRestart
			@Range(min = 0)
			public static int advancedUpgradeId;

			/**
			 * The dig speed factor for tool hosts.
			 * 20 is about normal player speed.
			 */
			@DefaultInt(10)
			@Range(min = 1)
			public static int digFactor;
		}
	}

	/**
	 * Additional network functionality.
	 */
	public static final class Network {
		/**
		 * The wireless bridge allows you to connect
		 * wired networks across dimensions.
		 */
		public static class WirelessBridge {
			/**
			 * Enable the wireless bridge
			 */
			@DefaultBoolean(true)
			@RequiresRestart(mc = false, world = true)
			public static boolean enabled;

			/**
			 * Enable the crafting of Wireless Bridges.
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean crafting;

			/**
			 * Enable the Wireless Bridge upgrade for turtles.
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean turtleEnabled;

			/**
			 * The turtle upgrade Id
			 */
			@DefaultInt(331)
			@Range(min = 1)
			@RequiresRestart
			public static int turtleId;

			/**
			 * Enable the Wireless Bridge upgrade for pocket computers.
			 * Requires Peripherals++
			 */
			@DefaultBoolean(true)
			@RequiresRestart
			public static boolean pocketEnabled;

			/**
			 * The pocket upgrade Id
			 * Requires Peripherals++
			 */
			@DefaultInt(331)
			@Range(min = 1)
			@RequiresRestart
			public static int pocketId;
		}

		/**
		 * Enable the crafting of full block modems.
		 *
		 * If you disable, existing ones will still function,
		 * and you can obtain them from creative.
		 */
		@DefaultBoolean(true)
		@RequiresRestart
		public static boolean fullBlockModemCrafting;
	}

	/**
	 * Integration with other mods.
	 */
	@RequiresRestart
	public static final class Integration {
		/**
		 * Allows pushing items from one inventory
		 * to another inventory on the network.
		 */
		@DefaultBoolean(true)
		public static boolean openPeripheralInventories;

		/**
		 * Enable ChickenBones Multipart
		 * (aka ForgeMultipart) integration.
		 */
		@DefaultBoolean(true)
		public static boolean cbMultipart;
	}

	/**
	 * Various tweaks that don't belong to anything
	 */
	public static final class Misc {
		/**
		 * The light level given off by normal monitors.
		 * Redstone torches are 7, normal torches are 14.
		 */
		@DefaultInt(7)
		@Range(min = 0, max = 15)
		public static int monitorLight;

		/**
		 * The light level given off by advanced monitors.
		 * Redstone torches are 7, normal torches are 14.
		 */
		@DefaultInt(10)
		@Range(min = 0, max = 15)
		public static int advancedMonitorLight;
	}

	/**
	 * Only used when testing and developing the mod.
	 * Nothing to see here, move along...
	 */
	public static final class Testing {
		/**
		 * Enable debug blocks/items.
		 * Only use for testing.
		 */
		@DefaultBoolean(false)
		public static boolean debugItems;

		/**
		 * Controller validation occurs by default as a
		 * way of ensuring that your network has been
		 * correctly created.
		 *
		 * By enabling this it is easier to trace
		 * faults, though it may slow things down
		 * slightly
		 */
		@DefaultBoolean(false)
		public static boolean extendedControllerValidation;
	}
}
