/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellPreCastEvent
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.zheergenrevision.SealRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ZheergenAreaSeal {
    private static final Map<ServerLevel, Map<UUID, Long>> SEALED = new WeakHashMap<ServerLevel, Map<UUID, Long>>();

    private ZheergenAreaSeal() {
    }

    public static List<LivingEntity> targets(ServerLevel serverLevel, LivingEntity livingEntity, Vec3 vec3, double d) {
        EasterBossEntity easterBossEntity;
        if (!(livingEntity instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() != 7) {
            return ManorSpellEffects.targets(serverLevel, livingEntity, vec3, d);
        }
        double d2 = BossSpellTuning.radius(livingEntity, d);
        return serverLevel.m_6443_(LivingEntity.class, new AABB(vec3, vec3).m_82400_(d2), livingEntity2 -> {
            ServerPlayer serverPlayer;
            return livingEntity2 instanceof ServerPlayer && !(serverPlayer = (ServerPlayer)livingEntity2).m_7500_() && !serverPlayer.m_5833_() && livingEntity2.m_6084_() && BossSpellEffects.canTarget(livingEntity, livingEntity2) && SealRules.inside(livingEntity2.m_20238_(vec3), d2) && serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.25, 0.0), livingEntity2.m_20191_().m_82399_(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)livingEntity)).m_6662_() == HitResult.Type.MISS;
        });
    }

    public static boolean hurt(LivingEntity livingEntity, LivingEntity livingEntity2, ManorSignatureSpell manorSignatureSpell, float f) {
        ServerPlayer serverPlayer;
        EasterBossEntity easterBossEntity;
        boolean bl = ManorSpellEffects.hurt(livingEntity, livingEntity2, manorSignatureSpell, f);
        if (!(livingEntity instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() != 7 || manorSignatureSpell.kind != ManorSpellRules.Kind.SHADOW_BRAND || !(livingEntity2 instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)livingEntity2).m_6084_() || serverPlayer.m_7500_() || serverPlayer.m_5833_() || !BossSpellEffects.canTarget(livingEntity, livingEntity2)) {
            return bl;
        }
        ServerLevel serverLevel2 = (ServerLevel)serverPlayer.m_9236_();
        int n = SealRules.duration(BossSpellTuning.buffTicks(livingEntity, MobEffects.f_19597_, 60));
        Map map = SEALED.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
        long l = serverLevel2.m_46467_();
        map.entrySet().removeIf(entry -> (Long)entry.getValue() <= l);
        map.merge(serverPlayer.m_20148_(), l + (long)n, Math::max);
        serverPlayer.m_147207_(new MobEffectInstance(MobEffects.f_19597_, n, 6, false, true, true), (Entity)livingEntity);
        ZheergenAreaSeal.interrupt(serverPlayer);
        return bl;
    }

    private static boolean sealed(ServerPlayer serverPlayer) {
        Object object = serverPlayer.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = SEALED.get(serverLevel);
        if (object == null) {
            return false;
        }
        Long l = (Long)object.get(serverPlayer.m_20148_());
        if (l == null) {
            return false;
        }
        boolean bl = SealRules.active(serverLevel.m_46467_(), l, serverPlayer.m_6084_() && !serverPlayer.m_7500_() && !serverPlayer.m_5833_(), true);
        if (!bl) {
            object.remove(serverPlayer.m_20148_());
            if (object.isEmpty()) {
                SEALED.remove(serverLevel);
            }
        }
        return bl;
    }

    private static void interrupt(ServerPlayer serverPlayer) {
        MagicData magicData = MagicData.getPlayerMagicData((LivingEntity)serverPlayer);
        if (magicData.isCasting()) {
            magicData.getCastingSpell().getSpell().onServerCastComplete(serverPlayer.m_9236_(), magicData.getCastingSpellLevel(), (LivingEntity)serverPlayer, magicData, true);
        }
    }

    @SubscribeEvent
    public static void preCast(SpellPreCastEvent spellPreCastEvent) {
        ServerPlayer serverPlayer;
        Player player = spellPreCastEvent.getEntity();
        if (player instanceof ServerPlayer && ZheergenAreaSeal.sealed(serverPlayer = (ServerPlayer)player)) {
            spellPreCastEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent livingTickEvent) {
        ServerPlayer serverPlayer;
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (!(livingEntity instanceof ServerPlayer) || !ZheergenAreaSeal.sealed(serverPlayer = (ServerPlayer)livingEntity)) {
            return;
        }
        livingEntity = serverPlayer.m_20184_();
        serverPlayer.m_20334_(0.0, Math.min(0.0, livingEntity.f_82480_), 0.0);
        serverPlayer.f_19864_ = true;
        ZheergenAreaSeal.interrupt(serverPlayer);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        LevelAccessor levelAccessor = unload.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)levelAccessor;
            SEALED.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        SEALED.clear();
    }
}

