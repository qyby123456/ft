/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideBoltEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TideTridentItem
extends SwordItem {
    public TideTridentItem(Item.Properties properties) {
        super((Tier)Tiers.NETHERITE, 5, -2.6f, properties);
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.m_19090_((Object)stack);
        }
        ServerLevel server = (ServerLevel)level;
        boolean whirlpool = player.m_6144_();
        if (whirlpool) {
            for (LivingEntity target : server.m_45976_(LivingEntity.class, player.m_20191_().m_82400_(5.0))) {
                Player other;
                if (target == player || !target.m_6084_() || player.m_7307_((Entity)target) || !player.m_142582_((Entity)target) || player.m_20280_((Entity)target) > 25.0 || target instanceof Player && (!BossTargets.valid(other = (Player)target) || !player.m_7099_(other)) || !target.m_6469_(player.m_269291_().m_269075_(player), 16.0f)) continue;
                target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 80, 1));
                Vec3 pull = player.m_20182_().m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_().m_82490_(0.35);
                target.m_5997_(pull.f_82479_, 0.1, pull.f_82481_);
                target.f_19864_ = true;
            }
            player.m_7292_(new MobEffectInstance(MobEffects.f_19608_, 200, 0));
            for (int index = 0; index < 72; ++index) {
                double angle = (double)index * Math.PI * 2.0 / 72.0;
                server.m_8767_((ParticleOptions)ParticleTypes.f_123769_, player.m_20185_() + Math.cos(angle) * 5.0, player.m_20186_() + 0.4, player.m_20189_() + Math.sin(angle) * 5.0, 2, 0.0, 0.15, 0.0, 0.05);
            }
        } else {
            for (int index = -1; index <= 1; ++index) {
                TideBoltEntity bolt = new TideBoltEntity((EntityType<? extends TideBoltEntity>)((EntityType)FTEntities.TIDE_BOLT.get()), (Level)server);
                bolt.m_5602_((Entity)player);
                bolt.setDamage(12.0f);
                bolt.m_6034_(player.m_20185_(), player.m_20188_() - 0.15, player.m_20189_());
                Vec3 aim = player.m_20154_().m_82524_((float)index * 0.1f);
                bolt.m_6686_(aim.f_82479_, aim.f_82480_, aim.f_82481_, 1.65f, 0.0f);
                server.m_7967_((Entity)bolt);
            }
        }
        player.m_36335_().m_41524_((Item)this, whirlpool ? 200 : 80);
        stack.m_41622_(whirlpool ? 3 : 1, (LivingEntity)player, owner -> owner.m_21190_(hand));
        server.m_5594_(null, player.m_20183_(), SoundEvents.f_12517_, SoundSource.PLAYERS, 0.9f, 1.1f);
        player.m_21011_(hand, true);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.tide_trident"));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.tide_trident_shift"));
    }
}

