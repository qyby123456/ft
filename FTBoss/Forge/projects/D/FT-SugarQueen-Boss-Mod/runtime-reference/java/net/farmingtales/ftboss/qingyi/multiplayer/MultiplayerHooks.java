/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 */
package net.farmingtales.ftboss.qingyi.multiplayer;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyi.multiplayer.MultiplayerRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public final class MultiplayerHooks {
    public static final UUID ARMOR_ID = UUID.fromString("209c36c3-320e-4ed2-8cc6-f9fc176d8b5e");
    private static final String ACTIVE = "ftboss:qingyi_multiplayer_active";
    private static final String PASSIVE = "ftboss:qingyi_multiplayer_passive_remainder";
    private static final String RAIN = "ftboss:qingyi_multiplayer_rain_remainder";

    private MultiplayerHooks() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 5 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_();
    }

    public static int players(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        UUID uUID = ManorFight.fightId((Entity)easterBossEntity);
        Vec3 vec3 = uUID != null && ManorFight.hasActive(uUID) ? ManorFight.anchorOf(uUID) : null;
        int n = 0;
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (serverPlayer instanceof FakePlayer || !serverPlayer.m_6084_() || serverPlayer.m_7500_() || serverPlayer.m_5833_() || serverPlayer.m_9236_() != serverLevel) continue;
            boolean bl = vec3 != null ? serverPlayer.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_) <= 9216.0 : easterBossEntity.m_20191_().m_82400_(48.0).m_82390_(serverPlayer.m_20182_());
            boolean bl2 = bl;
            if (!bl2) continue;
            ++n;
        }
        return n;
    }

    public static void tick(EasterBossEntity easterBossEntity) {
        AttributeInstance attributeInstance;
        Level level = easterBossEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = easterBossEntity.getPersistentData();
        int n = MultiplayerHooks.eligible(easterBossEntity) ? MultiplayerHooks.players(easterBossEntity, serverLevel) : 0;
        boolean bl = MultiplayerHooks.eligible(easterBossEntity) && MultiplayerRules.active(n);
        boolean bl2 = level.m_128471_(ACTIVE);
        if (bl2 != bl) {
            level.m_128379_(ACTIVE, bl);
            level.m_128473_(PASSIVE);
            level.m_128473_(RAIN);
            FTBossMod.LOGGER.info("[FTBoss][QingyiMultiplayer] \u6709\u6548\u6218\u6597\u73a9\u5bb6={}\uff0c\u4e09\u53c9\u621f\u53ec\u5524\u9891\u7387={}\uff0c\u603b\u62a4\u7532={}", new Object[]{n, bl ? "1.5x" : "1.0x", bl ? "1.5x" : "1.0x"});
        }
        if ((attributeInstance = easterBossEntity.m_21051_(Attributes.f_22284_)) == null) {
            return;
        }
        AttributeModifier attributeModifier = attributeInstance.m_22111_(ARMOR_ID);
        if (!bl) {
            if (attributeModifier != null) {
                attributeInstance.m_22120_(ARMOR_ID);
            }
        } else if (attributeModifier == null || attributeModifier.m_22218_() != 0.5 || attributeModifier.m_22217_() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
            if (attributeModifier != null) {
                attributeInstance.m_22120_(ARMOR_ID);
            }
            attributeInstance.m_22118_(new AttributeModifier(ARMOR_ID, "ftboss.qingyi_multiplayer_armor", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static int interval(int n, EasterBossEntity easterBossEntity, String string) {
        if (!(n > 0 && MultiplayerHooks.eligible(easterBossEntity) && easterBossEntity.m_9236_() instanceof ServerLevel && easterBossEntity.getPersistentData().m_128471_(ACTIVE))) {
            return n;
        }
        String string2 = switch (string) {
            case "passive" -> PASSIVE;
            case "rain" -> RAIN;
            default -> throw new IllegalArgumentException("Unknown Qingyi summon channel");
        };
        String string3 = easterBossEntity.getPersistentData();
        int n2 = string3.m_128451_(string2);
        int n3 = MultiplayerRules.interval(n, n2);
        string3.m_128405_(string2, MultiplayerRules.remainder(n, n2));
        return n3;
    }
}

