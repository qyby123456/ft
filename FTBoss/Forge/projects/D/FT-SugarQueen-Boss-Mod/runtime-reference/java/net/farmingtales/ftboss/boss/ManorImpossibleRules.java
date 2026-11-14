/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorBan;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorImpossibleRules {
    public static final Set<String> BANNED_BUFF_SPELLS = Set.of("heartstop", "oakskin");
    public static final Set<String> BANNED_FOOD_BUFFS = Set.of("kaleidoscope_cookery:satiated_shield", "farmersdelight:nourishment", "farm_and_charm:satiation");
    public static final Set<String> SHARED_BANNED_FOOD_BUFFS = Set.of("kaleidoscope_cookery:satiated_shield");

    private ManorImpossibleRules() {
    }

    public static boolean isAttack(DamageSource source) {
        return ManorImpossibleRules.attackDifficulty(source) == ManorDifficulty.IMPOSSIBLE;
    }

    private static ManorDifficulty attackDifficulty(DamageSource source) {
        Entity owner = source.m_7639_();
        if (owner == null) {
            owner = source.m_7640_();
        }
        if (owner == null) {
            owner = ManorMercy.damageOwner();
        }
        ManorDifficulty difficulty = null;
        for (int depth = 0; owner != null && depth < 8; ++depth) {
            Level level;
            UUID fight;
            if (owner instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)owner;
                if (boss.isImpossibleChallenge()) {
                    return ManorDifficulty.IMPOSSIBLE;
                }
                difficulty = ManorDifficulty.NORMAL;
            }
            if ((fight = ManorFightMembers.fightId(owner)) != null && (difficulty = ManorFight.difficulty(fight)) == ManorDifficulty.IMPOSSIBLE) {
                return difficulty;
            }
            Entity parent = ManorFightMembers.owner(owner);
            if (parent == null && (level = owner.m_9236_()) instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                UUID identifier = MinionBlessing.ownerId(owner);
                if (identifier != null) {
                    parent = level2.m_8791_(identifier);
                }
            }
            if (parent == owner) break;
            owner = parent;
        }
        return difficulty;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        block5: {
            block4: {
                LivingEntity livingEntity;
                if (event.isCanceled() || !((livingEntity = event.getEntity()) instanceof Player)) break block4;
                Player player = (Player)livingEntity;
                if (!player.m_9236_().f_46443_ && player.m_6084_() && !player.m_7500_() && !player.m_5833_() && Float.isFinite(event.getAmount()) && !(event.getAmount() <= 0.0f)) break block5;
            }
            return;
        }
        ManorDifficulty difficulty = ManorImpossibleRules.attackDifficulty(event.getSource());
        if (difficulty == null) {
            return;
        }
        float multiplier = difficulty == ManorDifficulty.NORMAL ? 0.8333333f : 4.0f;
        event.setAmount(event.getAmount() * multiplier);
    }

    public static int purgeFoodBuffs(ServerPlayer player) {
        boolean impossible = ManorFight.isImpossibleFor((Player)player);
        if (!impossible && !ManorFight.isPlayerFighting((Player)player)) {
            return 0;
        }
        Set<String> banned = impossible ? BANNED_FOOD_BUFFS : SHARED_BANNED_FOOD_BUFFS;
        int removed = 0;
        for (MobEffectInstance effect : new ArrayList(player.m_21220_())) {
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect.m_19544_());
            if (key == null || !banned.contains(key.toString()) || !player.m_21195_(effect.m_19544_())) continue;
            ++removed;
        }
        if (removed > 0) {
            ManorDifficulty difficulty = impossible ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL;
            ManorBan.scold(player, difficulty.label() + "\u7981\u7528\u7684\u98df\u7269\u589e\u76ca\uff08" + removed + " \u9879\uff09");
        }
        return removed;
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        EasterBossEntity boss;
        if (event.getEntity().m_9236_().f_46443_) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof EasterBossEntity && (boss = (EasterBossEntity)livingEntity).isImpossibleChallenge()) {
            BossDamageGate.tick((LivingEntity)boss);
        }
        if ((livingEntity = event.getEntity()) instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            if (player.f_19797_ % 10 == 0) {
                ManorImpossibleRules.purgeFoodBuffs(player);
            }
        }
    }
}

