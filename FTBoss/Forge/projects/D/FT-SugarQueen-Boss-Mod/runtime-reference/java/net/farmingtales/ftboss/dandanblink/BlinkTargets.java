/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.dandanblink;

import java.util.ArrayList;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.dandanblink.BlinkRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFinaleRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BlinkTargets {
    private static final String FIGHT = "ftboss_dandan_blink_fight";
    private static final String LAST = "ftboss_dandan_blink_last";
    private static final String DUE = "ftboss_dandan_blink_due";
    private static final String SWAP_DISTANCE = "ftboss_dandan_swap_distance";
    private static final String RUSH_DISTANCE = "ftboss_dandan_rush_distance";

    private BlinkTargets() {
    }

    public static boolean managed(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 1 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && ManorMultiplayerBalance.isManagedManorBoss((LivingEntity)easterBossEntity);
    }

    private static Vec3 anchor(EasterBossEntity easterBossEntity) {
        return ManorFight.anchorOf(ManorFightMembers.fightId((Entity)easterBossEntity));
    }

    private static boolean inside(Vec3 vec3, Vec3 vec32) {
        return vec3 != null && vec32 != null && ManorFinaleRules.insideArena(vec3.f_82479_ - vec32.f_82479_, vec3.f_82480_ - vec32.f_82480_, vec3.f_82481_ - vec32.f_82481_);
    }

    public static boolean allowed(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (!BlinkTargets.managed(easterBossEntity)) {
            return easterBossEntity.canTeleportWith(livingEntity);
        }
        return livingEntity instanceof ServerPlayer && ManorLivingTargets.valid((Entity)easterBossEntity, livingEntity) && BlinkTargets.inside(livingEntity.m_20182_(), BlinkTargets.anchor(easterBossEntity));
    }

    private static LivingEntity random(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        Level level;
        block6: {
            block5: {
                level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)level;
                if (easterBossEntity.m_6084_() && !easterBossEntity.m_21224_()) break block6;
            }
            return null;
        }
        level = BlinkTargets.anchor(easterBossEntity);
        if (level == null) {
            return null;
        }
        ArrayList<ServerPlayer> arrayList = new ArrayList<ServerPlayer>();
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!ManorLivingTargets.valid((Entity)easterBossEntity, (LivingEntity)serverPlayer) || !BlinkTargets.inside(serverPlayer.m_20182_(), (Vec3)level)) continue;
            arrayList.add(serverPlayer);
        }
        return (LivingEntity)BlinkRules.choose(arrayList, arg_0 -> ((RandomSource)easterBossEntity.m_217043_()).m_188503_(arg_0));
    }

    private static void remember(EasterBossEntity easterBossEntity, LivingEntity livingEntity, String string) {
        easterBossEntity.getPersistentData().m_128347_(string, Math.sqrt(easterBossEntity.m_20280_((Entity)livingEntity)));
        easterBossEntity.m_6710_(livingEntity);
    }

    public static LivingEntity swapTarget(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (!BlinkTargets.managed(easterBossEntity)) {
            return livingEntity;
        }
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        if (uUID == null) {
            return null;
        }
        if (!compoundTag.m_128403_(FIGHT) || !uUID.equals(compoundTag.m_128342_(FIGHT))) {
            compoundTag.m_128362_(FIGHT, uUID);
            compoundTag.m_128473_(LAST);
            compoundTag.m_128473_(DUE);
        }
        if (!BlinkRules.ready(easterBossEntity.m_9236_().m_46467_(), compoundTag.m_128454_(LAST), compoundTag.m_128454_(DUE))) {
            return null;
        }
        LivingEntity livingEntity2 = BlinkTargets.random(easterBossEntity);
        if (livingEntity2 != null) {
            BlinkTargets.remember(easterBossEntity, livingEntity2, SWAP_DISTANCE);
        }
        return livingEntity2;
    }

    public static LivingEntity rushTarget(EasterBossEntity easterBossEntity) {
        if (!BlinkTargets.managed(easterBossEntity)) {
            return easterBossEntity.m_5448_();
        }
        LivingEntity livingEntity = BlinkTargets.random(easterBossEntity);
        if (livingEntity != null) {
            BlinkTargets.remember(easterBossEntity, livingEntity, RUSH_DISTANCE);
        }
        return livingEntity;
    }

    public static void swapped(EasterBossEntity easterBossEntity) {
        if (!BlinkTargets.managed(easterBossEntity)) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_();
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        compoundTag.m_128356_(LAST, l);
        compoundTag.m_128356_(DUE, l + (long)BlinkRules.swapCooldown(compoundTag.m_128459_(SWAP_DISTANCE)));
    }

    public static int rushed(EasterBossEntity easterBossEntity) {
        if (!BlinkTargets.managed(easterBossEntity)) {
            return 5;
        }
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        long l = easterBossEntity.m_9236_().m_46467_();
        int n = BlinkRules.rushCooldown(compoundTag.m_128459_(RUSH_DISTANCE));
        compoundTag.m_128356_(LAST, l);
        compoundTag.m_128356_(DUE, Math.max(compoundTag.m_128454_(DUE), l + (long)n));
        return n;
    }

    public static Vec3 find(ServerLevel serverLevel, Entity entity, Vec3 vec3, int n, EasterBossEntity easterBossEntity) {
        Vec3 vec32 = ManorSafeTeleport.find(serverLevel, entity, vec3, n);
        return BlinkTargets.managed(easterBossEntity) && !BlinkTargets.inside(vec32, BlinkTargets.anchor(easterBossEntity)) ? null : vec32;
    }
}

