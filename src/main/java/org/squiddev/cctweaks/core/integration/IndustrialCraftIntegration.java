package org.squiddev.cctweaks.core.integration;

import dan200.computercraft.api.turtle.ITurtleAccess;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.api.turtle.TurtleFuelRegistry;
import org.squiddev.cctweaks.core.Config;

/**
 * Refuel for IC2 energy sources
 */
public class IndustrialCraftIntegration extends APIIntegrationModule {
	public IndustrialCraftIntegration() {
		super("IC2API");
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		TurtleFuelRegistry.addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				return Config.config.turtleEuRefuelEnable && stack.getItem() instanceof IElectricItem;
			}

			@Override
			public int refuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				int euAmount = Config.config.turtleEuRefuelAmount;

				// Avoid over refueling
				int maxRefuel = turtle.getFuelLimit() - turtle.getFuelLevel();
				int euLimit = (limit >= 64 ? maxRefuel : limit) * euAmount;

				return (int) (ElectricItem.manager.discharge(stack, euLimit, Integer.MAX_VALUE, true, true, false) / euAmount);
			}
		});
	}
}
