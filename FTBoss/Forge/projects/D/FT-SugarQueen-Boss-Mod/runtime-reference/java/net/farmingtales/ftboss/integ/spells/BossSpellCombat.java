/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossSpellCombat {
    static final String READY = "ftboss:signature_ready";
    static final String CAST = "ftboss:signature_cast";
    private static final String NEXT = "ftboss:signature_next";

    private BossSpellCombat() {
    }

    public static List<BossSpellRules.Kind> spellPool(Mob boss) {
        if (boss instanceof EasterBossEntity) {
            EasterBossEntity lemon = (EasterBossEntity)boss;
            return lemon.lemonSacrifice().spells();
        }
        return BossSpellRules.forBoss(EntityType.m_20613_((EntityType)boss.m_6095_()).toString());
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        Mob boss;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Mob) || !((livingEntity = (boss = (Mob)livingEntity).m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)livingEntity;
        if (boss instanceof EasterBossEntity) {
            boss.getPersistentData().m_128473_(CAST);
            return;
        }
        List<BossSpellRules.Kind> pair = BossSpellCombat.spellPool(boss);
        if (pair.isEmpty()) {
            return;
        }
        CompoundTag data = boss.getPersistentData();
        LivingEntity target = boss.m_5448_();
        if (!boss.m_6084_() || boss.m_213877_() || boss.m_21525_() || boss.m_20147_() || target == null || target.m_9236_() != level || !BossSpellEffects.canTarget((LivingEntity)boss, target) || boss.m_20280_((Entity)target) > 1024.0 || !boss.m_142582_((Entity)target)) {
            data.m_128473_(CAST);
            return;
        }
        long now = level.m_46467_();
        if (data.m_128425_(CAST, 10)) {
            CompoundTag cast = data.m_128469_(CAST);
            BossSpellRules.Kind kind = pair.stream().filter(candidate -> candidate.spell.equals(cast.m_128461_("spell"))).findFirst().orElse(null);
            if (kind == null) {
                data.m_128473_(CAST);
                return;
            }
            BossSignatureSpell spell = BossIronSpells.spell(kind);
            long elapsed = now - cast.m_128454_("started");
            if (!(spell.isEnabled() && elapsed >= 0L && elapsed <= 60L && cast.m_128403_("target") && target.m_20148_().equals(cast.m_128342_("target")))) {
                data.m_128473_(CAST);
                return;
            }
            Vec3 center = new Vec3(cast.m_128459_("x"), cast.m_128459_("y"), cast.m_128459_("z"));
            if (BossSpellRules.selfCentered(kind)) {
                center = boss.m_20182_();
            }
            if (elapsed < (long)spell.getEffectiveCastTime(1, (LivingEntity)boss)) {
                if (elapsed % 4L == 0L) {
                    BossSpellEffects.warning(level, (LivingEntity)boss, kind, center);
                }
                return;
            }
            data.m_128473_(CAST);
            MagicData magic = new MagicData();
            magic.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(center));
            if (boss instanceof EasterBossEntity) {
                EasterBossEntity lemon = (EasterBossEntity)boss;
                lemon.lemonSacrifice().runInherited(() -> spell.onCast((Level)level, Math.min(2, spell.getMaxLevel()), (LivingEntity)boss, CastSource.MOB, magic));
            } else {
                spell.onCast((Level)level, Math.min(2, spell.getMaxLevel()), (LivingEntity)boss, CastSource.MOB, magic);
            }
            FTBossMod.LOGGER.info("[FTBoss][Signature] {} released {}", (Object)boss.m_7755_().getString(), (Object)spell.getSpellId());
            return;
        }
        if (!data.m_128425_(READY, 99)) {
            data.m_128356_(READY, now + (long)NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)boss, 60 + Math.floorMod(boss.m_19879_(), 20)));
            return;
        }
        if (now < data.m_128454_(READY) || boss.m_20280_((Entity)target) > 576.0) {
            return;
        }
        int preferred = Math.floorMod(data.m_128451_(NEXT), pair.size());
        BossSpellRules.Kind kind = null;
        for (int offset = 0; offset < pair.size(); ++offset) {
            BossSpellRules.Kind candidate2 = pair.get((preferred + offset) % pair.size());
            if (!BossIronSpells.spell(candidate2).isEnabled() || now < data.m_128454_("ftboss:signature_ready:" + candidate2.spell)) continue;
            kind = candidate2;
            data.m_128405_(NEXT, (preferred + offset + 1) % pair.size());
            break;
        }
        if (kind == null) {
            return;
        }
        BossSignatureSpell spell = BossIronSpells.spell(kind);
        data.m_128356_(READY, now + (long)NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)boss, 60));
        data.m_128356_("ftboss:signature_ready:" + kind.spell, now + (long)NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)boss, BossSpellPacing.signatureCooldown(spell.getSpellCooldown())));
        Vec3 center = BossSpellRules.selfCentered(kind) ? boss.m_20182_() : BossSpellEffects.aim((LivingEntity)boss);
        CompoundTag cast = new CompoundTag();
        cast.m_128356_("started", now);
        cast.m_128359_("spell", kind.spell);
        cast.m_128362_("target", target.m_20148_());
        cast.m_128347_("x", center.f_82479_);
        cast.m_128347_("y", center.f_82480_);
        cast.m_128347_("z", center.f_82481_);
        data.m_128365_(CAST, (Tag)cast);
        spell.playSound(spell.getCastStartSound(), (Entity)boss);
        BossSpellEffects.warning(level, (LivingEntity)boss, kind, center);
        if (boss instanceof RegularBossCastHost) {
            RegularBossCastHost animated = (RegularBossCastHost)boss;
            animated.startSignatureCast(kind, spell.getEffectiveCastTime(1, (LivingEntity)boss));
        }
        FTBossMod.LOGGER.info("[FTBoss][Signature] {} charging {}", (Object)boss.m_7755_().getString(), (Object)spell.getSpellId());
    }
}

