package org.squiddev.cctweaks.items;

import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Handles actions on computers
 */
public abstract class ItemComputerAction extends ItemBase {
	public ItemComputerAction(String itemName) {
		super(itemName);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking() || world.isRemote) {
			return false;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile == null) return false;

		boolean result;
		if (tile instanceof TileComputerBase) {
			// Allow custom Turtle Actions
			if (tile instanceof TileTurtle) {
				result = upgradeTurtle(stack, player, (TileTurtle) tile, side);
			} else {
				result = upgradeComputer(stack, player, (TileComputerBase) tile, side);
			}
		} else {
			result = itemUse(stack, player, tile, side);
		}

		if (result) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize -= 1;
			}
		}

		return result;
	}

	protected abstract boolean upgradeComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, int side);

	protected boolean upgradeTurtle(ItemStack stack, EntityPlayer player, TileTurtle computerTile, int side) {
		return upgradeComputer(stack, player, computerTile, side);
	}

	/**
	 * Custom action on other tile types
	 */
	protected boolean itemUse(ItemStack stack, EntityPlayer player, TileEntity tile, int side) {
		return false;
	}
}
