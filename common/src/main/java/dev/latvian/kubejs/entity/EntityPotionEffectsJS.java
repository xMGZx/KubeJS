package dev.latvian.kubejs.entity;

import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.wrap.Wrap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class EntityPotionEffectsJS
{
	private final LivingEntity entity;

	public EntityPotionEffectsJS(LivingEntity e)
	{
		entity = e;
	}

	public void clear()
	{
		entity.removeAllEffects();
	}

	public Collection<MobEffectInstance> getActive()
	{
		return entity.getActiveEffects();
	}

	public Map<MobEffect, MobEffectInstance> getMap()
	{
		return entity.getActiveEffectsMap();
	}

	public boolean isActive(@Wrap("id") String potion)
	{
		MobEffect p = UtilsJS.getPotion(potion);
		return p != null && entity.hasEffect(p);
	}

	@Nullable
	public MobEffectInstance getActive(@Wrap("id") String potion)
	{
		MobEffect p = UtilsJS.getPotion(potion);
		return p == null ? null : entity.getEffect(p);
	}

	public void add(@Wrap("id") String potion)
	{
		add(potion, 0, 0);
	}

	public void add(@Wrap("id") String potion, int duration)
	{
		add(potion, duration, 0);
	}

	public void add(@Wrap("id") String potion, int duration, int amplifier)
	{
		add(potion, duration, amplifier, false, true);
	}

	public void add(@Wrap("id") String potion, int duration, int amplifier, boolean ambient, boolean showParticles)
	{
		MobEffect p = UtilsJS.getPotion(potion);

		if (p != null)
		{
			entity.addEffect(new MobEffectInstance(p, duration, amplifier, ambient, showParticles));
		}
	}

	public boolean isApplicable(MobEffectInstance effect)
	{
		return entity.canBeAffected(effect);
	}
}