package dev.latvian.kubejs.script;

import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.KubeJSEvents;
import dev.latvian.kubejs.bindings.DefaultBindings;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.event.EventsJS;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.ClassShutter;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.RhinoException;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ScriptManager
{
	private static final Object2BooleanOpenHashMap<String> CLASS_WHITELIST_CACHE = new Object2BooleanOpenHashMap<>();

	private static final String[] BLACKLISTED_PACKAGES = {
			"java.io.", // IO and network
			"java.nio.",
			"java.net.",
			"sun.",
			"com.sun.",
			"io.netty.",
			"java.lang.reflect.",

			"dev.latvian.mods.rhino.", // Rhino itself
			"dev.latvian.kubejs.script.", // KubeJS itself

			"cpw.mods.modlauncher.", // Forge / FML internal stuff
			"cpw.mods.gross.",
			"net.minecraftforge.fml.",
			"net.minecraftforge.accesstransformer.",
			"net.minecraftforge.coremod.",
			"org.openjdk.nashorn.",
			"jdk.nashorn.",

			"net.fabricmc.accesswidener.", // Fabric internal stuff
			"net.fabricmc.devlaunchinjector.",
			"net.fabricmc.loader.",
			"net.fabricmc.tinyremapper.",

			"org.objectweb.asm.", // ASM
			"org.spongepowered.asm.", // Sponge ASM
			"me.shedaniel.architectury.", // Architectury

			"com.chocohead.mm.", // Manningham Mills
	};

	private static final Predicate<String> CLASS_WHITELIST_FUNCTION = s -> {
		for (String s1 : BLACKLISTED_PACKAGES)
		{
			if (s.startsWith(s1))
			{
				return false;
			}
		}

		return true;
	};

	public final ScriptType type;
	public final Path directory;
	public final String exampleScript;
	public final EventsJS events;
	public final Map<String, ScriptPack> packs;

	public ScriptManager(ScriptType t, Path p, String e)
	{
		type = t;
		directory = p;
		exampleScript = e;
		events = new EventsJS(this);
		packs = new LinkedHashMap<>();
	}

	public void unload()
	{
		events.clear();
		packs.clear();
		type.errors.clear();
		type.warnings.clear();
		type.console.resetFile();
	}

	public void loadFromDirectory()
	{
		if (Files.notExists(directory))
		{
			UtilsJS.tryIO(() -> Files.createDirectories(directory));

			try (InputStream in = KubeJS.class.getResourceAsStream(exampleScript);
				 OutputStream out = Files.newOutputStream(directory.resolve("script.js")))
			{
				out.write(IOUtils.toByteArray(in));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		ScriptPack pack = new ScriptPack(this, new ScriptPackInfo(directory.getFileName().toString(), ""));
		KubeJS.loadScripts(pack, directory, "");

		for (ScriptFileInfo fileInfo : pack.info.scripts)
		{
			ScriptSource.FromPath scriptSource = info -> directory.resolve(info.file);

			Throwable error = fileInfo.preload(scriptSource);

			if (error == null)
			{
				pack.scripts.add(new ScriptFile(pack, fileInfo, scriptSource));
			}
			else
			{
				KubeJS.LOGGER.error("Failed to pre-load script file " + fileInfo.location + ": " + error);
			}
		}

		pack.scripts.sort(null);
		packs.put(pack.info.namespace, pack);
	}

	public void load()
	{
		Context context = Context.enter();
		context.setLanguageVersion(Context.VERSION_ES6);
		context.setClassShutter((fullClassName, type) -> type != ClassShutter.TYPE_CLASS_IN_PACKAGE || CLASS_WHITELIST_CACHE.computeBooleanIfAbsent(fullClassName, CLASS_WHITELIST_FUNCTION));
		context.getTypeWrappers().register("id", ResourceLocation.class, String.class, ResourceLocation::toString);
		context.getTypeWrappers().register("id", String.class, ResourceLocation.class, ResourceLocation::new);

		long startAll = System.currentTimeMillis();

		int i = 0;
		int t = 0;

		for (ScriptPack pack : packs.values())
		{
			try
			{
				pack.context = context;
				pack.scope = context.initStandardObjects();

				BindingsEvent event = new BindingsEvent(type, pack.scope);
				BindingsEvent.EVENT.invoker().accept(event);
				DefaultBindings.init(this, event);

				for (ScriptFile file : pack.scripts)
				{
					t++;
					long start = System.currentTimeMillis();

					if (file.load())
					{
						i++;
						type.console.info("Loaded script " + file.info.location + " in " + (System.currentTimeMillis() - start) / 1000D + " s");
					}
					else if (file.getError() != null)
					{
						if (file.getError() instanceof RhinoException)
						{
							type.console.error("Error loading KubeJS script: " + file.getError().getMessage());
						}
						else
						{
							type.console.error("Error loading KubeJS script: " + file.info.location + ": " + file.getError());
							file.getError().printStackTrace();
						}
					}
				}
			}
			catch (Throwable ex)
			{
				type.console.error("Failed to read script pack " + pack.info.namespace + ": ", ex);
				ex.printStackTrace();
			}
		}

		type.console.info("Loaded " + i + "/" + t + " KubeJS " + type.name + " scripts in " + (System.currentTimeMillis() - startAll) / 1000D + " s");
		Context.exit();

		events.postToHandlers(KubeJSEvents.LOADED, events.handlers(KubeJSEvents.LOADED), new EventJS());

		if (i != t && type == ScriptType.STARTUP)
		{
			throw new RuntimeException("There were startup script syntax errors! See logs/kubejs/startup.txt for more info");
		}
	}
}