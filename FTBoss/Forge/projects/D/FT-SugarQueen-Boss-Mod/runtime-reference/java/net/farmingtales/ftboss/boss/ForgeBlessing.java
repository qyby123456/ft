/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ForgeBlessing {
    public static final int PERIOD_TICKS = 1200;
    public static final int BUFF_TICKS = 500;
    public static final int BUFF_AMPLIFIER = 3;
    public static final String[][] EFFECT_IDS = new String[][]{{"armamentarium", "adamantine_effect"}, {"armamentarium", "damon_effect"}, {"armamentarium", "lone_graveseeker_effect"}};
    public static final boolean ONLY_WHEN_BOSS_NEARBY = true;
    private static final List<MobEffect> CACHE = new ArrayList<MobEffect>();
    private static boolean cacheTried = false;

    private ForgeBlessing() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        ServerPlayer p;
        block7: {
            block6: {
                if (e.phase != TickEvent.Phase.END) {
                    return;
                }
                Player player = e.player;
                if (!(player instanceof ServerPlayer)) break block6;
                p = (ServerPlayer)player;
                if (!p.m_9236_().f_46443_) break block7;
            }
            return;
        }
        if (p.f_19797_ % 1200 != 0 || p.f_19797_ == 0) {
            return;
        }
        if (!ManorFight.isPlayerFighting((Player)p)) {
            return;
        }
        ForgeBlessing.grant(p);
    }

    public static void grant(ServerPlayer p) {
        int cleared = 0;
        for (Object inst : new ArrayList(p.m_21220_())) {
            if (inst.m_19544_().m_19483_() != MobEffectCategory.HARMFUL || !p.m_21195_(inst.m_19544_())) continue;
            ++cleared;
        }
        StringBuilder sb = new StringBuilder();
        for (MobEffect eff : ForgeBlessing.effects()) {
            if (eff == null) continue;
            p.m_7292_(new MobEffectInstance(eff, 500, 3, false, true, true));
            sb.append(eff.m_19482_().getString()).append(' ');
        }
        Level level = p.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123767_, p.m_20185_(), p.m_20186_() + 1.0, p.m_20189_(), 30, 0.6, 0.9, 0.6, 0.25);
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123810_, p.m_20185_(), p.m_20186_() + 1.0, p.m_20189_(), 14, 0.5, 0.8, 0.5, 0.12);
            sl.m_6263_(null, p.m_20185_(), p.m_20186_(), p.m_20189_(), SoundEvents.f_11739_, SoundSource.PLAYERS, 1.2f, 1.3f);
        }
        p.m_5661_((Component)Component.m_237113_((String)("\u00a76\u00a7l\u3010Forge \u7684\u795d\u798f\u3011\u00a7r\u00a77\u6e05\u9664\u4e86 \u00a7c" + cleared + " \u00a77\u4e2a\u51cf\u76ca \u00b7 \u83b7\u5f97 \u00a7d" + sb.toString().trim() + " \u00a77IV \u7ea7(25 \u79d2)")), false);
        FTBossMod.LOGGER.info("[FTBoss][\u795d\u798f] {} \u83b7\u5f97 Forge \u7684\u795d\u798f: \u6e05\u9664 {} \u4e2a\u51cf\u76ca, \u5957\u88c5\u6548\u679c {} \u7ea7 {} \u79d2 [{}]", new Object[]{p.m_7755_().getString(), cleared, 4, 25, sb.toString().trim()});
    }

    private static List<MobEffect> effects() {
        if (!cacheTried) {
            cacheTried = true;
            for (String[] id : EFFECT_IDS) {
                MobEffect eff = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id[0], id[1]));
                if (eff == null) {
                    FTBossMod.LOGGER.warn("[FTBoss][\u795d\u798f] \u627e\u4e0d\u5230\u6548\u679c {}:{} \u2014\u2014 \u5df2\u8df3\u8fc7", (Object)id[0], (Object)id[1]);
                    continue;
                }
                CACHE.add(eff);
            }
            FTBossMod.LOGGER.info("[FTBoss][\u795d\u798f] \u795d\u798f\u5957\u88c5\u6548\u679c\u5c31\u7eea {} \u4ef6: {}", (Object)CACHE.size(), CACHE.stream().map(m -> m.m_19482_().getString()).toList());
        }
        return CACHE;
    }
}

