/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSparrowCasting;
import net.farmingtales.ftboss.integ.spells.ManorSpellCombat;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.MaqueFlightRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class MaqueFeatherCombat {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Cast>>();
    private static final String READY = "ftboss:maque_feather_ready";

    private MaqueFeatherCombat() {
    }

    private static boolean ready(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 9 && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && !easterBossEntity.m_20147_() && ManorSpellCombat.eligible(easterBossEntity);
    }

    private static boolean valid(ServerLevel serverLevel, EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2, LivingEntity livingEntity) {
        return MaqueFeatherCombat.ready(easterBossEntity) && easterBossEntity2 != null && easterBossEntity2.m_6084_() && !easterBossEntity2.m_213877_() && !easterBossEntity2.isMini() && !easterBossEntity2.m_20147_() && ManorSpellCombat.eligible(easterBossEntity2) && easterBossEntity2.getVariant() != 9 && easterBossEntity2.m_9236_() == serverLevel && Objects.equals(ManorFightMembers.fightId((Entity)easterBossEntity), ManorFightMembers.fightId((Entity)easterBossEntity2)) && Objects.equals(easterBossEntity.getManorSummonerId(), easterBossEntity2.getManorSummonerId()) && easterBossEntity.m_20280_((Entity)easterBossEntity2) <= 1024.0 && livingEntity != null && livingEntity.m_9236_() == serverLevel && easterBossEntity2.m_5448_() == livingEntity && BossSpellEffects.canTarget((LivingEntity)easterBossEntity, livingEntity) && BossSpellEffects.canTarget((LivingEntity)easterBossEntity2, livingEntity) && easterBossEntity.m_20280_((Entity)livingEntity) <= 576.0 && easterBossEntity.m_142582_((Entity)livingEntity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent livingTickEvent) {
        EasterBossEntity easterBossEntity;
        Object object = livingTickEvent.getEntity();
        if (!(object instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)object).getVariant() != 9 || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel2 = (ServerLevel)object;
        object = ACTIVE.get(serverLevel2);
        long l = serverLevel2.m_46467_();
        if (object != null && l % 20L == 0L) {
            object.entrySet().removeIf(entry -> serverLevel2.m_8791_((UUID)entry.getKey()) == null);
        }
        Cast cast = object == null ? null : (Cast)object.get(easterBossEntity.m_20148_());
        ManorSignatureSpell manorSignatureSpell = ManorIronSpells.spell(ManorSpellRules.Kind.FEATHER_FAN);
        if (cast != null) {
            EasterBossEntity easterBossEntity3;
            Entity entity = serverLevel2.m_8791_(cast.owner());
            EasterBossEntity easterBossEntity4 = entity instanceof EasterBossEntity ? (easterBossEntity3 = (EasterBossEntity)entity) : null;
            Entity entity2 = serverLevel2.m_8791_(cast.target());
            easterBossEntity3 = entity2 instanceof LivingEntity ? (entity = (LivingEntity)entity2) : null;
            int n = MaqueFlightRules.castState(l - cast.started(), cast.duration(), MaqueFeatherCombat.valid(serverLevel2, easterBossEntity, easterBossEntity4, (LivingEntity)easterBossEntity3), manorSignatureSpell.isEnabled());
            if (n < 0) {
                MaqueFeatherCombat.finish(easterBossEntity, (Map<UUID, Cast>)object, cast, true);
                easterBossEntity.getPersistentData().m_128356_(READY, l + 40L);
                return;
            }
            easterBossEntity.m_21563_().m_24960_((Entity)easterBossEntity3, 60.0f, 60.0f);
            cast.magic().setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(easterBossEntity3.m_20182_().m_82520_(0.0, 0.5, 0.0)));
            if (n == 0) {
                cast.magic().handleCastDuration();
                manorSignatureSpell.onServerCastTick((Level)serverLevel2, cast.rank(), (LivingEntity)easterBossEntity, cast.magic());
                return;
            }
            try {
                manorSignatureSpell.onCast((Level)serverLevel2, cast.rank(), (LivingEntity)easterBossEntity, CastSource.MOB, cast.magic());
            }
            finally {
                MaqueFeatherCombat.finish(easterBossEntity, (Map<UUID, Cast>)object, cast, false);
            }
            return;
        }
        if (!MaqueFeatherCombat.ready(easterBossEntity) || !manorSignatureSpell.isEnabled()) {
            return;
        }
        if (!easterBossEntity.getPersistentData().m_128425_(READY, 99)) {
            easterBossEntity.getPersistentData().m_128356_(READY, l + 40L + (long)Math.floorMod(easterBossEntity.m_19879_(), 20));
            return;
        }
        if (!MaqueFlightRules.canStart(l, easterBossEntity.getPersistentData().m_128454_(READY), object == null ? 0 : object.size(), easterBossEntity.isCastingLike() || easterBossEntity.isPerformingSkill() || ManorSparrowCasting.isCastingFor(serverLevel2, easterBossEntity))) {
            return;
        }
        EasterBossEntity easterBossEntity5 = serverLevel2.m_6443_(EasterBossEntity.class, easterBossEntity.m_20191_().m_82400_(32.0), easterBossEntity2 -> MaqueFeatherCombat.valid(serverLevel2, easterBossEntity, easterBossEntity2, easterBossEntity2.m_5448_())).stream().min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)easterBossEntity).m_20280_(arg_0))).orElse(null);
        if (easterBossEntity5 == null) {
            easterBossEntity.getPersistentData().m_128356_(READY, l + 10L);
            return;
        }
        LivingEntity livingEntity = easterBossEntity5.m_5448_();
        int n = Math.max(manorSignatureSpell.getMinLevel(), Math.min(2, manorSignatureSpell.getMaxLevel()));
        int n2 = Math.max(1, Math.min(200, manorSignatureSpell.getEffectiveCastTime(n, (LivingEntity)easterBossEntity)));
        MagicData magicData = new MagicData(true);
        magicData.setSyncedData(new SyncedSpellData((LivingEntity)easterBossEntity));
        if (!manorSignatureSpell.checkPreCastConditions((Level)serverLevel2, n, (LivingEntity)easterBossEntity, magicData)) {
            easterBossEntity.getPersistentData().m_128356_(READY, l + 40L);
            return;
        }
        magicData.initiateCast((AbstractSpell)manorSignatureSpell, n, n2, CastSource.MOB, "mainhand");
        manorSignatureSpell.onServerPreCast((Level)serverLevel2, n, (LivingEntity)easterBossEntity, magicData);
        Vec3 vec3 = livingEntity.m_20182_().m_82520_(0.0, 0.5, 0.0);
        magicData.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(vec3));
        ACTIVE.computeIfAbsent(serverLevel2, serverLevel -> new HashMap()).put(easterBossEntity.m_20148_(), new Cast(easterBossEntity5.m_20148_(), livingEntity.m_20148_(), l, n2, n, magicData));
        easterBossEntity.getPersistentData().m_128356_(READY, l + 160L);
        easterBossEntity.getPersistentData().m_128356_("ftboss:manor_cd_feather_fan", l + 160L);
        easterBossEntity.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        easterBossEntity.playParallelSpell(n2);
        ManorSpellEffects.warning(serverLevel2, (LivingEntity)easterBossEntity, ManorSpellRules.Kind.FEATHER_FAN, vec3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void finish(EasterBossEntity easterBossEntity, Map<UUID, Cast> map, Cast cast, boolean bl) {
        map.remove(easterBossEntity.m_20148_());
        try {
            ManorIronSpells.spell(ManorSpellRules.Kind.FEATHER_FAN).onServerCastComplete(easterBossEntity.m_9236_(), cast.rank(), (LivingEntity)easterBossEntity, cast.magic(), bl);
        }
        finally {
            easterBossEntity.finishParallelSpell();
        }
    }

    public static boolean afterHit(boolean bl, LivingEntity livingEntity, LivingEntity livingEntity2, ManorSignatureSpell manorSignatureSpell) {
        if (!(bl && manorSignatureSpell.kind == ManorSpellRules.Kind.FEATHER_FAN && livingEntity2.m_9236_() instanceof ServerLevel && livingEntity2.m_6084_())) {
            return bl;
        }
        MobEffect mobEffect = (MobEffect)FTEffects.HEAL_BAN.get();
        MobEffectInstance mobEffectInstance = livingEntity2.m_21124_(mobEffect);
        if (mobEffectInstance == null || MaqueFlightRules.refreshBan(mobEffectInstance.m_19557_(), mobEffectInstance.m_267577_())) {
            int n = mobEffectInstance == null ? 0 : Math.max(0, mobEffectInstance.m_19564_());
            livingEntity2.m_147207_(new MobEffectInstance(mobEffect, 100, n, false, true, true), (Entity)livingEntity);
        }
        return bl;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        Object object = unload.getLevel();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ACTIVE.remove(serverLevel);
        if (object == null) {
            return;
        }
        for (UUID uUID : List.copyOf(object.keySet())) {
            Entity entity = serverLevel.m_8791_(uUID);
            if (!(entity instanceof EasterBossEntity)) continue;
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            MaqueFeatherCombat.finish(easterBossEntity, (Map<UUID, Cast>)object, (Cast)object.get(uUID), true);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        ACTIVE.clear();
    }

    private record Cast(UUID owner, UUID target, long started, int duration, int rank, MagicData magic) {
    }
}

