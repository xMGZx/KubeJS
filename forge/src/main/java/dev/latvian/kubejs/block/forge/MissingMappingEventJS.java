package dev.latvian.kubejs.block.forge;

import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.wrap.Wrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public class MissingMappingEventJS<T extends IForgeRegistryEntry<T>> extends EventJS
{
	private final RegistryEvent.MissingMappings<T> event;
	private final Function<ResourceLocation, T> valueProvider;

	public MissingMappingEventJS(RegistryEvent.MissingMappings<T> e, Function<ResourceLocation, T> v)
	{
		event = e;
		valueProvider = v;
	}

	private void findMapping(@Wrap("id") String key, Consumer<RegistryEvent.MissingMappings.Mapping<T>> callback)
	{
		ResourceLocation k = UtilsJS.getMCID(key);

		for (RegistryEvent.MissingMappings.Mapping<T> mapping : event.getAllMappings())
		{
			if (mapping.key.equals(k))
			{
				callback.accept(mapping);
				return;
			}
		}
	}

	public void remap(@Wrap("id") String key, @Wrap("id") String value)
	{
		findMapping(key, mapping ->
		{
			ResourceLocation idTo = UtilsJS.getMCID(value);
			T to = valueProvider.apply(idTo);

			if (to != null)
			{
				ScriptType.STARTUP.console.info("Remapping " + mapping.key + " to " + idTo + " (" + to.getClass() + ")");
				mapping.remap(UtilsJS.cast(to));
			}
		});
	}

	public void ignore(@Wrap("id") String key)
	{
		findMapping(key, RegistryEvent.MissingMappings.Mapping::ignore);
	}

	public void warn(@Wrap("id") String key)
	{
		findMapping(key, RegistryEvent.MissingMappings.Mapping::warn);
	}

	public void fail(@Wrap("id") String key)
	{
		findMapping(key, RegistryEvent.MissingMappings.Mapping::fail);
	}
}