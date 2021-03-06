package dev.latvian.kubejs.client;

import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.wrap.Wrap;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class SoundRegistryEventJS extends EventJS
{
	private final Consumer<ResourceLocation> registry;

	public SoundRegistryEventJS(Consumer<ResourceLocation> registry)
	{
		this.registry = registry;
	}

	public void register(@Wrap("id") String id)
	{
		ResourceLocation r = UtilsJS.getMCID(KubeJS.appendModId(id));
		registry.accept(r);
	}
}