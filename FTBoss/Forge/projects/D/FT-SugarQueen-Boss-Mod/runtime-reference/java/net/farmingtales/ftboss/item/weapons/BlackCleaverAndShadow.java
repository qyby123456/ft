/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrownEnderpearl
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.DandanPearlBan;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class BlackCleaverAndShadow {
    private BlackCleaverAndShadow() {
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class ShadowPearlWatcher {
        private static final List<Tracked> LIVE = new ArrayList<Tracked>();
        private static final List<Tracked> DONE = new ArrayList<Tracked>();

        private ShadowPearlWatcher() {
        }

        public static void watch(ThrownEnderpearl pearl, Player owner) {
            LIVE.add(new Tracked(pearl, owner, owner.f_19797_, pearl.m_20182_()));
        }

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || LIVE.isEmpty()) {
                return;
            }
            DONE.clear();
            for (Tracked t : LIVE) {
                if (t.pearl().m_213877_() || t.owner().f_19797_ - t.born() > 200) {
                    Level level = t.owner().m_9236_();
                    if (level instanceof ServerLevel) {
                        ServerLevel sl = (ServerLevel)level;
                        ShadowPearlWatcher.impact(sl, t.owner(), t.pearl().m_20182_());
                    }
                    DONE.add(t);
                    continue;
                }
                LIVE.set(LIVE.indexOf(t), new Tracked(t.pearl(), t.owner(), t.born(), t.pearl().m_20182_()));
            }
            LIVE.removeAll(DONE);
        }

        private static void impact(ServerLevel sl, Player owner, Vec3 at) {
            ServerPlayer sp;
            if (owner instanceof ServerPlayer && DandanPearlBan.blockLanding(sp = (ServerPlayer)owner, "\u6697\u5f71\u5251 \u00b7 \u672b\u5f71\u73cd\u73e0\u843d\u70b9")) {
                return;
            }
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123765_, at.f_82479_, at.f_82480_ + 0.4, at.f_82481_, 40, 1.2, 0.6, 1.2, 0.05);
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123760_, at.f_82479_, at.f_82480_ + 0.6, at.f_82481_, 30, 1.5, 0.8, 1.5, 0.2);
            sl.m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, SoundEvents.f_11852_, SoundSource.PLAYERS, 1.4f, 0.6f);
            int n = 0;
            for (LivingEntity le : sl.m_45976_(LivingEntity.class, new AABB(at, at).m_82400_(3.0))) {
                if (le == owner || !le.m_6084_()) continue;
                le.m_7292_(new MobEffectInstance(MobEffects.f_19610_, 120, 0, false, true, true));
                le.f_19802_ = 0;
                le.m_6469_(owner.m_269291_().m_269104_((Entity)owner, (Entity)owner), 10.0f);
                ++n;
            }
            FTBossMod.LOGGER.info("[FTBoss][\u6697\u5f71\u5251] \u672b\u5f71\u73cd\u73e0\u843d\u5730: 3 \u683c\u5185 {} \u4e2a\u751f\u7269\u5931\u660e + {} \u70b9\u9b54\u6cd5\u4f24\u5bb3", (Object)n, (Object)10);
        }

        private record Tracked(ThrownEnderpearl pearl, Player owner, int born, Vec3 last) {
        }
    }

    public static class ShadowSword
    extends SwordItem {
        public static final int COOLDOWN = 40;
        public static final double AOE_RADIUS = 3.0;
        public static final float AOE_DAMAGE = 10.0f;
        public static final int BLIND_TICKS = 120;

        public ShadowSword() {
            super((Tier)Tiers.NETHERITE, 10, -2.4f, new Item.Properties());
        }

        public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.m_21120_(hand);
            if (!level.f_46443_) {
                ServerPlayer sp;
                if (player instanceof ServerPlayer && DandanPearlBan.blockThrow(sp = (ServerPlayer)player, "\u6697\u5f71\u5251 \u00b7 \u672b\u5f71\u73cd\u73e0\u6295\u63b7")) {
                    return InteractionResultHolder.m_19100_((Object)stack);
                }
                ThrownEnderpearl pearl = new ThrownEnderpearl(level, (LivingEntity)player);
                pearl.m_37446_(new ItemStack((ItemLike)Items.f_42584_));
                pearl.m_37251_((Entity)player, player.m_146909_(), player.m_146908_(), 0.0f, 1.5f, 1.0f);
                Vec3 eye = player.m_146892_();
                pearl.m_6034_(eye.f_82479_, eye.f_82480_ - 0.1, eye.f_82481_);
                level.m_7967_((Entity)pearl);
                ShadowPearlWatcher.watch(pearl, player);
                level.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11857_, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            player.m_36335_().m_41524_((Item)this, 40);
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.m_5776_());
        }
    }

    public static class BlackCleaver
    extends AxeItem {
        public BlackCleaver() {
            super((Tier)Tiers.NETHERITE, 7.0f, -2.6f, new Item.Properties());
        }

        public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.m_7579_(stack, target, attacker);
            return hit;
        }
    }
}

