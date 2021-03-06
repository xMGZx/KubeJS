package dev.latvian.kubejs.integration;

import dev.latvian.kubejs.integration.gamestages.GameStagesIntegration;
import dev.latvian.kubejs.recipe.silentsmek.SilentsMechanismsRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/**
 * @author LatvianModder
 */
public class IntegrationManager
{
	public static void init()
	{
		if (ModList.get().isLoaded("gamestages"))
		{
			GameStagesIntegration.init();
		}

		if (ModList.get().isLoaded("silents_mechanisms") && !ModList.get().isLoaded("kubejs_silents_mechanisms"))
		{
			MinecraftForge.EVENT_BUS.register(SilentsMechanismsRecipes.class);
		}
	}
}