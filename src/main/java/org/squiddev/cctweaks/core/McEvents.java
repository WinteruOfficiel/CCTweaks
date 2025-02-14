package org.squiddev.cctweaks.core;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.utils.WorldPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * Main event handler
 */
public class McEvents {
	public interface IDropConsumer {
		void consumeDrop(ItemStack stack);
	}

	private static final Map<IWorldPosition, IDropConsumer> blockConsumers = new HashMap<IWorldPosition, IDropConsumer>();
	private static final Map<Entity, IDropConsumer> entityConsumers = new HashMap<Entity, IDropConsumer>();

	/**
	 * Add a consumer for entity drops
	 *
	 * @param entity   The entity whose drops to consume
	 * @param consumer The drop consumer
	 */
	public static void addEntityConsumer(Entity entity, IDropConsumer consumer) {
		if (!entity.captureDrops && (entity.capturedDrops == null || entity.capturedDrops.size() == 0)) {
			entity.captureDrops = true;
			entityConsumers.put(entity, consumer);
		}
	}

	/**
	 * Remove an entity drop consumer
	 *
	 * @param entity The entity who's drops to consume
	 */
	public static void removeEntityConsumer(Entity entity) {
		IDropConsumer consumer = entityConsumers.remove(entity);

		if (consumer != null && entity.captureDrops) {
			entity.captureDrops = false;

			if (entity.capturedDrops != null && entity.capturedDrops.size() > 0) {
				for (EntityItem item : entity.capturedDrops) {
					consumer.consumeDrop(item.getEntityItem());
				}
				entity.capturedDrops.clear();
			}
		}
	}

	public static void addBlockConsumer(IWorldPosition position, IDropConsumer consumer) {
		blockConsumers.put(position, consumer);
	}

	public static void removeBlockConsumer(IWorldPosition position) {
		blockConsumers.remove(position);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		if (blockConsumers.size() > 0) {
			IDropConsumer consumer = blockConsumers.get(new WorldPosition(event.world, event.x, event.y, event.z));
			if (consumer != null) {
				for (ItemStack item : event.drops) {
					if (event.world.rand.nextFloat() < event.dropChance) {
						consumer.consumeDrop(item);
					}
				}
				event.drops.clear();

			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityLivingDrops(LivingDropsEvent event) {
		IDropConsumer consumer = entityConsumers.get(event.entity);
		if (consumer != null) {
			for (EntityItem item : event.drops) {
				consumer.consumeDrop(item.getEntityItem());
			}
			event.drops.clear();
		}
	}
}
