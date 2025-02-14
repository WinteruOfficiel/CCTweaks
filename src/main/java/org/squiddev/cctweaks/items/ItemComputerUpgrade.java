package org.squiddev.cctweaks.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorRecipe;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.BlockNotifyFlags;
import org.squiddev.cctweaks.core.utils.ComputerAccessor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.core.utils.InventoryUtils;

import java.util.List;


public class ItemComputerUpgrade extends ItemComputerAction {
	public ItemComputerUpgrade() {
		super("computerUpgrade");
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return Config.Computer.computerUpgradeEnabled && super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	protected boolean useComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, int side) {
		int x = computerTile.xCoord, y = computerTile.yCoord, z = computerTile.zCoord;
		World world = computerTile.getWorldObj();

		// Check we can copy the tile and it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal || ComputerAccessor.tileCopy == null) {
			return false;
		}

		// Set metadata
		int metadata = world.getBlockMetadata(x, y, z);
		world.setBlock(x, y, z, ComputerCraft.Blocks.computer, metadata + 8, BlockNotifyFlags.ALL);

		TileEntity newTile = world.getTileEntity(x, y, z);

		if (newTile == null || !(newTile instanceof TileComputerBase)) {
			return false;
		}

		// Why is it not public Dan?
		TileComputerBase newComputer = (TileComputerBase) newTile;
		try {
			ComputerAccessor.tileCopy.invoke(newComputer, computerTile);
		} catch (Exception e) {
			DebugLogger.warn("Cannot copy tile in ItemComputerUpgrade", e);
			return false;
		}

		// Setup computer
		newComputer.createServerComputer().setWorld(world);
		newComputer.updateBlock();

		return true;
	}

	@Override
	protected boolean useTurtle(ItemStack stack, EntityPlayer player, TileTurtle computerTile, int side) {
		int x = computerTile.xCoord, y = computerTile.yCoord, z = computerTile.zCoord;
		World world = computerTile.getWorldObj();

		// Ensure it is a normal computer
		if (computerTile.getFamily() != ComputerFamily.Normal) {
			return false;
		}

		if (!player.capabilities.isCreativeMode) {
			int remaining = InventoryUtils.extractItems(player.inventory, Items.gold_ingot, 7);
			if (remaining > 0) {
				player.addChatMessage(new ChatComponentText("7 gold required. Need " + remaining + " more.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)));
				return false;
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		// If we set the turtle as moved, the destroy method won't drop the items
		try {
			ComputerAccessor.turtleTileMoved.setBoolean(computerTile, true);
		} catch (Exception e) {
			DebugLogger.warn("Cannot set TurtleTile m_moved in ItemComputerUpgrade", e);
			return false;
		}

		// Set block as AdvancedTurtle
		world.setBlock(x, y, z, ComputerCraft.Blocks.turtleAdvanced);
		TileEntity newTile = world.getTileEntity(x, y, z);

		// Transfer state
		if (newTile == null || !(newTile instanceof TileTurtle)) {
			return false;
		}

		TileTurtle newTurtle = (TileTurtle) newTile;
		newTurtle.transferStateFrom(computerTile);

		newTurtle.createServerComputer().setWorld(world);
		newTurtle.createServerComputer().setPosition(x, y, z);
		newTurtle.updateBlock();

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(StatCollector.translateToLocal("gui.tooltip.cctweaks.computerUpgrade.normal"));
	}

	@Override
	public void init() {
		super.init();
		if (!Config.Computer.computerUpgradeEnabled) return;

		ItemStack stack = new ItemStack(this);
		if (Config.Computer.computerUpgradeCrafting) {
			GameRegistry.addRecipe(stack, "GGG", "GSG", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
		}


		RecipeSorter.register(CCTweaks.RESOURCE_DOMAIN + ":computer_upgrade_crafting", CraftingComputerUpgrade.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(new CraftingComputerUpgrade());

		// Add some impostor recipes for NEI. We just use CC's default ones
		{
			// Computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				ComputerItemFactory.create(-1, null, ComputerFamily.Advanced),
				new Object[]{
					ComputerItemFactory.create(-1, null, ComputerFamily.Normal),
					stack
				}
			));

			// Turtle (Is is silly to include every possible upgrade so we just do the normal one)
			ItemStack gold = new ItemStack(Items.gold_ingot);
			GameRegistry.addRecipe(new ImpostorRecipe(3, 3,
				new ItemStack[]{
					gold, gold, gold,
					gold, TurtleItemFactory.create(-1, null, null, ComputerFamily.Normal, null, null, 0, null, null), gold,
					gold, stack, gold,
				},
				TurtleItemFactory.create(-1, null, null, ComputerFamily.Advanced, null, null, 0, null, null)
			));

			// Non-wireless pocket computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false),
				new Object[]{
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false),
					stack
				}
			));

			// Wireless pocket computer
			GameRegistry.addRecipe(new ImpostorShapelessRecipe(
				PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, true),
				new Object[]{
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, true),
					stack
				}
			));
		}
	}
}
