/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

public final class BossDamageGate {
    public static final boolean LIMIT_ENABLED = false;
    private static final Map<String, Double> DAMAGE_CAP = Map.of("ftboss:sugar_queen", 58.0, "ftboss:meat_mountain", 58.0, "ftboss:tea_tree", 56.0, "ftboss:wine_barrel", 58.0, "ftboss:rat_boss", 46.0);
    private static final double DEFAULT_CAP = 48.0;
    private static final Map<String, Double> ANTI_CHEESE = Map.of("ftboss:sugar_queen", 32.0, "ftboss:meat_mountain", 32.0, "ftboss:tea_tree", 40.0, "ftboss:wine_barrel", 32.0, "ftboss:rat_boss", 32.0, "ftboss:defect_core", 36.0);
    private static final double DEFAULT_ANTI_CHEESE = 32.0;
    public static final int ADAPT_TICKS = 24;
    public static final int FACTOR_STEP = 6;
    public static final int FACTOR_MIN = 20;
    public static final int FACTOR_MAX = 100;
    public static final float COMBO_FLOOR = 0.7f;
    public static final int HURT_CD = 30;
    private static final String K_ADAPT = "ftboss_adapt_ticks";
    private static final String K_FACTOR = "ftboss_time_factor";
    private static final String K_CD = "ftboss_hurt_cd";
    private static final String K_COUNT = "ftboss_hurt_count";
    private static final String K_NOTIFY = "ftboss_anticheese_notify";
    private static final Set<String> IMMUNE_NAMESPACES = Set.of();
    private static final Set<String> IMMUNE_IDS = Set.of("hemorrhage", "blood_loss", "bloodloss", "bleed", "bleeding", "doom", "impending_doom", "executioner", "assassination", "desecration", "true_damage");
    private static final String K_IMMUNE_NOTIFY = "ftboss_immune_notify";

    private BossDamageGate() {
    }

    public static boolean isImmune(DamageSource source) {
        String msg;
        Optional key = source.m_269150_().m_203543_();
        if (key.isPresent()) {
            ResourceLocation loc = ((ResourceKey)key.get()).m_135782_();
            if (IMMUNE_NAMESPACES.contains(loc.m_135827_())) {
                return true;
            }
            if (IMMUNE_IDS.contains(loc.m_135815_())) {
                return true;
            }
        }
        if ((msg = source.m_19385_()) != null) {
            String low = msg.toLowerCase(Locale.ROOT);
            for (String s : IMMUNE_IDS) {
                if (!low.contains(s)) continue;
                return true;
            }
        }
        return false;
    }

    public static float gate(LivingEntity boss, DamageSource source, float amount) {
        EasterBossEntity manor;
        boolean impossible;
        if (amount <= 0.0f) {
            return amount;
        }
        if (BossDamageGate.isImmune(source)) {
            BossDamageGate.notifyImmune(boss, source);
            return 0.0f;
        }
        boolean bl = impossible = boss instanceof EasterBossEntity && (manor = (EasterBossEntity)boss).isImpossibleChallenge();
        if (!impossible) {
            return amount;
        }
        if (source.m_269533_(DamageTypeTags.f_268738_)) {
            return amount;
        }
        float result = (float)Math.min(BossDamageGate.capOf(boss), (double)amount);
        float mult = Mth.m_14036_((float)((float)BossDamageGate.factorOf(boss) / 100.0f), (float)0.7f, (float)1.0f);
        result *= mult;
        double limit = BossDamageGate.antiCheeseOf(boss);
        if (limit > 0.0) {
            ServerPlayer attacker;
            if (impossible) {
                v1 = EasterBossEntity.realAttacker(source);
            } else if (boss instanceof Mob) {
                Mob mob = (Mob)boss;
                v1 = mob.m_5448_();
            } else {
                v1 = attacker = null;
            }
            if (attacker == null) {
                attacker = source.m_7639_();
            }
            if (attacker != null && attacker != boss && (double)boss.m_20270_((Entity)attacker) > limit) {
                BossDamageGate.notifyAntiCheese(boss, source, limit);
                return 0.0f;
            }
        }
        return result;
    }

    @Deprecated
    public static boolean nullified(LivingEntity boss, DamageSource source, float amount) {
        return false;
    }

    public static void onHit(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        tag.m_128405_(K_ADAPT, 24);
        tag.m_128405_(K_FACTOR, Math.max(20, BossDamageGate.factorOf(boss) - 6));
        tag.m_128405_(K_CD, 30);
        int count = tag.m_128451_(K_COUNT) + 1;
        tag.m_128405_(K_COUNT, count >= 2 ? 0 : count);
    }

    public static void tick(LivingEntity boss) {
        int adapt;
        if (boss.m_9236_().m_5776_()) {
            return;
        }
        CompoundTag tag = boss.getPersistentData();
        int cd = tag.m_128451_(K_CD);
        if (cd > 0) {
            tag.m_128405_(K_CD, cd - 1);
        }
        if ((adapt = tag.m_128451_(K_ADAPT)) > 0) {
            tag.m_128405_(K_ADAPT, --adapt);
            if (adapt == 0) {
                tag.m_128405_(K_FACTOR, 100);
            }
        } else if (tag.m_128451_(K_FACTOR) != 100 && tag.m_128441_(K_FACTOR)) {
            tag.m_128405_(K_FACTOR, 100);
        }
    }

    public static boolean isInvulnerableByCd(LivingEntity boss) {
        return boss.getPersistentData().m_128451_(K_CD) > 0;
    }

    public static int timeFactor(LivingEntity boss) {
        return BossDamageGate.factorOf(boss);
    }

    public static double capOf(LivingEntity boss) {
        return DAMAGE_CAP.getOrDefault(BossDamageGate.entityId(boss), 48.0);
    }

    public static double antiCheeseOf(LivingEntity boss) {
        return ANTI_CHEESE.getOrDefault(BossDamageGate.entityId(boss), 32.0);
    }

    private static int factorOf(LivingEntity boss) {
        int f = boss.getPersistentData().m_128451_(K_FACTOR);
        return f <= 0 ? 100 : f;
    }

    private static String entityId(LivingEntity boss) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey((Object)boss.m_6095_());
        return key == null ? "" : key.toString();
    }

    private static void notifyImmune(LivingEntity boss, DamageSource source) {
        Entity entity = source.m_7639_();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        CompoundTag tag = boss.getPersistentData();
        long now = boss.m_9236_().m_46467_();
        if (now - tag.m_128454_(K_IMMUNE_NOTIFY) < 40L) {
            return;
        }
        tag.m_128356_(K_IMMUNE_NOTIFY, now);
        player.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.immune", (Object[])new Object[]{boss.m_5446_()}), true);
        FTBossMod.LOGGER.info("[FTBoss][\u514d\u75ab] {} \u7684 {} \u4f24\u5bb3\u88ab\u5b8c\u5168\u514d\u75ab(\u4f24\u5bb3\u7c7b\u578b {})", new Object[]{boss.m_5446_().getString(), player.m_7755_().getString(), source.m_19385_()});
    }

    private static void notifyAntiCheese(LivingEntity boss, DamageSource source, double limit) {
        Entity entity = source.m_7639_();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        CompoundTag tag = boss.getPersistentData();
        long now = boss.m_9236_().m_46467_();
        if (now - tag.m_128454_(K_NOTIFY) < 40L) {
            return;
        }
        tag.m_128356_(K_NOTIFY, now);
        player.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.anticheese", (Object[])new Object[]{boss.m_5446_(), String.format("%.0f", limit)}), true);
    }
}

