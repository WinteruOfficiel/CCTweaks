package org.squiddev.cctweaks.core.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import org.squiddev.cctweaks.CCTweaks;

public abstract class ItemBase extends Item {

	private final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setUnlocalizedName(CCTweaks.RESOURCE_DOMAIN + "." + name);
		setCreativeTab(CCTweaks.getCreativeTab());
		setMaxStackSize(stackSize);
	}

	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister registry) {
		itemIcon = registry.registerIcon(CCTweaks.RESOURCE_DOMAIN + ":" + name);
	}

	public void registerItem() {
		GameRegistry.registerItem(this, getUnlocalizedName());
	}
}
