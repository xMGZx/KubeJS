package dev.latvian.kubejs.world;

import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.player.AttachPlayerDataEvent;
import dev.latvian.kubejs.player.ClientPlayerDataJS;
import dev.latvian.kubejs.player.PlayerDataJS;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@DocClass("Client side dimension")
public class ClientWorldJS extends WorldJS
{
	private static ClientWorldJS inst;

	public static ClientWorldJS get()
	{
		if (inst == null || inst.world != Minecraft.getMinecraft().world || inst.clientPlayerData.player.player != Minecraft.getMinecraft().player)
		{
			inst = new ClientWorldJS();
			MinecraftForge.EVENT_BUS.post(new AttachWorldDataEvent(inst, inst.data));
			MinecraftForge.EVENT_BUS.post(new AttachPlayerDataEvent(inst.clientPlayerData, inst.clientPlayerData.data));
		}

		return inst;
	}

	public final Minecraft minecraft;
	public final ClientPlayerDataJS clientPlayerData;

	public ClientWorldJS()
	{
		super(Minecraft.getMinecraft().world);
		minecraft = Minecraft.getMinecraft();
		clientPlayerData = new ClientPlayerDataJS(this, minecraft.player.getUniqueID(), minecraft.player.getName());
	}

	@Override
	@Nullable
	public PlayerDataJS playerData(UUID id)
	{
		return id.equals(clientPlayerData.uuid) ? clientPlayerData : null;
	}
}