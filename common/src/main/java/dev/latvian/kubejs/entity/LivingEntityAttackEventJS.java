package dev.latvian.kubejs.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author LatvianModder
 */
public class LivingEntityAttackEventJS extends LivingEntityEventJS
{
	private final LivingEntity entity;
	private final DamageSource source;
	private final float amount;

	public LivingEntityAttackEventJS(LivingEntity entity, DamageSource source, float amount)
	{
		this.entity = entity;
		this.source = source;
		this.amount = amount;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}

	@Override
	public EntityJS getEntity()
	{
		return entityOf(entity);
	}

	public DamageSourceJS getSource()
	{
		return new DamageSourceJS(getWorld(), source);
	}

	public float getDamage()
	{
		return amount;
	}
}