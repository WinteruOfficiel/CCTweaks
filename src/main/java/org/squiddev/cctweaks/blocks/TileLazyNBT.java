package org.squiddev.cctweaks.blocks;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.core.FmlEvents;

/**
 * A tile entity that lazy loads NBT
 */
public abstract class TileLazyNBT extends TileBase {
	private NBTTagCompound lazyTag;

	/**
	 * Lazy load the NBT tag
	 *
	 * @param tag The NBT tag to load
	 */
	public abstract void readLazyNBT(NBTTagCompound tag);

	/**
	 * The fields that the tag stores.
	 *
	 * Used in the rare case that we are saving without having had an update tick
	 *
	 * @return The list of fields to keep
	 */
	public abstract Iterable<String> getFields();

	@Override
	public void create() {
		super.create();
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				if (lazyTag != null) {
					readLazyNBT(lazyTag);
					lazyTag = null;
				}
			}
		});
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (worldObj == null) {
			lazyTag = tag;
		} else {
			readLazyNBT(tag);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (lazyTag != null) {
			for (String field : getFields()) {
				NBTBase fieldTag = lazyTag.getTag(field);
				if (fieldTag != null) tag.setTag(field, fieldTag);
			}
		}
	}
}
