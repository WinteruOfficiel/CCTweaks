package org.squiddev.cctweaks.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.squiddev.cctweaks.blocks.IMultiBlock;

/**
 * An item to place instances of {@link IMultiBlock}
 */
public class ItemMultiBlock extends ItemBlock {
	public ItemMultiBlock(Block block) {
		super(block);
		if (!(block instanceof IMultiBlock)) throw new RuntimeException(block + " must be instance of IMultiBlock");

		setMaxStackSize(64);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return field_150939_a.getIcon(0, meta);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return ((IMultiBlock) field_150939_a).getUnlocalizedName(stack.getItemDamage());
	}
}
