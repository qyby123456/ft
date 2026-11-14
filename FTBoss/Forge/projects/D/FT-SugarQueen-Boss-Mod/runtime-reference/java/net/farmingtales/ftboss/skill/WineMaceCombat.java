/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.weapons.WineBottleMaceItem;
import net.farmingtales.ftboss.skill.NonManorWeaponSkills;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WineMaceRules;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class WineMaceCombat {
    private static final Map<UUID, Cast> CASTS = new HashMap<UUID, Cast>();

    private WineMaceCombat() {
    }

    private static long now(ServerPlayer player) {
        return player.m_284548_().m_7654_().m_129783_().m_46467_();
    }

    private static int pressure(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().m_128469_("FTWineMacePressure");
        return WineMaceRules.pressure(tag.m_128451_("count"), tag.m_128454_("expires"), WineMaceCombat.now(player));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void melee(LivingDamageEvent event) {
        ServerPlayer player;
        Entity entity;
        if (event.isCanceled() || event.getAmount() <= 0.0f || !((entity = event.getSource().m_7639_()) instanceof ServerPlayer) || !(player = (ServerPlayer)entity).m_6084_() || player.m_5833_() || event.getSource().m_7640_() != player || !event.getSource().m_276093_(DamageTypes.f_268464_) || !(player.m_21205_().m_41720_() instanceof WineBottleMaceItem) || CASTS.containsKey(player.m_20148_()) || !NonManorWeaponSkills.canTarget(player, event.getEntity())) {
            return;
        }
        CompoundTag tag = player.getPersistentData().m_128469_("FTWineMacePressure");
        long now = WineMaceCombat.now(player);
        if (!WineMaceRules.hitReady(tag.m_128454_("ready"), now)) {
            return;
        }
        int pressure = Math.min(3, WineMaceCombat.pressure(player) + 1);
        tag.m_128405_("count", pressure);
        tag.m_128356_("expires", now + 300L);
        tag.m_128356_("ready", now + 20L);
        player.getPersistentData().m_128365_("FTWineMacePressure", (Tag)tag);
        player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.wine_mace.pressure", (Object[])new Object[]{pressure, 3}), true);
        player.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_175828_, event.getEntity().m_20185_(), event.getEntity().m_20186_() + 0.8, event.getEntity().m_20189_(), 7, 0.25, 0.3, 0.25, 0.03);
        player.m_284548_().m_5594_(null, player.m_20183_(), SoundEvents.f_11770_, SoundSource.PLAYERS, 0.45f, 0.8f + (float)pressure * 0.18f);
    }

    public static boolean begin(ServerPlayer player) {
        if (!(player.m_21205_().m_41720_() instanceof WineBottleMaceItem) || CASTS.containsKey(player.m_20148_())) {
            return false;
        }
        Vec3 impact = NonManorWeaponSkills.aim(player, 7.0);
        Cast cast = new Cast(player, impact, WineMaceCombat.pressure(player));
        CASTS.put(player.m_20148_(), cast);
        CompoundTag pressureTag = player.getPersistentData().m_128469_("FTWineMacePressure");
        pressureTag.m_128405_("count", 0);
        pressureTag.m_128356_("expires", 0L);
        player.getPersistentData().m_128365_("FTWineMacePressure", (Tag)pressureTag);
        WineMaceCombat.ring(player.m_284548_(), impact, WineMaceRules.radius(cast.pressure), false);
        player.m_284548_().m_5594_(null, player.m_20183_(), SoundEvents.f_11772_, SoundSource.PLAYERS, 0.8f, 0.75f);
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        Player player;
        if (event.phase != TickEvent.Phase.END || !((player = event.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Cast cast = CASTS.get(player2.m_20148_());
        if (cast == null) {
            return;
        }
        if (!player2.m_6084_() || player2.m_213877_() || player2.m_5833_() || player2.m_5803_() || !player2.m_9236_().m_46472_().equals(cast.dimension) || player2.m_21205_() != cast.weapon || cast.weapon.m_41619_() || player2.m_6117_() || player2.m_20238_(cast.start) > 16.0 || !NonManorWeaponSkills.clearLine(player2.m_284548_(), player2, player2.m_146892_(), cast.impact)) {
            CASTS.remove(player2.m_20148_());
            player2.m_5661_((Component)Component.m_237115_((String)"message.ftboss.wine_mace.cancelled"), true);
            return;
        }
        ++cast.age;
        if (cast.age < 12) {
            if (cast.age % 3 == 0) {
                WineMaceCombat.ring(player2.m_284548_(), cast.impact, WineMaceRules.radius(cast.pressure), false);
                player2.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_175828_, cast.impact.f_82479_, cast.impact.f_82480_, cast.impact.f_82481_, 6, 0.3, 0.5, 0.3, 0.04);
            }
            return;
        }
        try {
            WineMaceCombat.impact(player2, cast);
        }
        finally {
            CASTS.remove(player2.m_20148_());
        }
    }

    private static void impact(ServerPlayer player, Cast cast) {
        ServerLevel level = player.m_284548_();
        double radius = WineMaceRules.radius(cast.pressure);
        ArrayList<LivingEntity> candidates = new ArrayList<LivingEntity>(level.m_6443_(LivingEntity.class, new AABB(cast.impact, cast.impact).m_82377_(radius, 3.0, radius), target -> NonManorWeaponSkills.canTarget(player, target)));
        candidates.sort(Comparator.comparingDouble(target -> target.m_20238_(cast.impact)));
        int hits = 0;
        for (LivingEntity target2 : candidates) {
            Vec3 center = target2.m_20182_().m_82520_(0.0, Math.min(0.5, (double)target2.m_20206_() * 0.5), 0.0);
            Vec3 offset = center.m_82546_(cast.impact);
            if (!WineMaceRules.inBlast(offset.m_165925_(), offset.f_82480_, cast.pressure) || !NonManorWeaponSkills.clearLine(level, player, cast.impact, target2.m_20191_().m_82399_()) || !target2.m_6469_(player.m_269291_().m_269075_((Player)player), cast.damage)) continue;
            ++hits;
            if (target2.m_6084_()) {
                target2.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 60 + cast.pressure * 20, 1), (Entity)player);
                target2.m_147207_(new MobEffectInstance(MobEffects.f_19604_, 100, 0), (Entity)player);
                NonManorWeaponSkills.knockAway(target2, cast.impact, 0.55 + (double)cast.pressure * 0.2);
            }
            if (hits < 24) continue;
            break;
        }
        if (hits > 0 && cast.pressure == 3) {
            player.m_147207_(new MobEffectInstance(MobEffects.f_19606_, 80, 0), (Entity)player);
        }
        WineMaceCombat.ring(level, cast.impact, radius, true);
        level.m_8767_((ParticleOptions)new BlockParticleOption(ParticleTypes.f_123794_, Blocks.f_50213_.m_49966_()), cast.impact.f_82479_, cast.impact.f_82480_, cast.impact.f_82481_, 16, 0.65, 0.6, 0.65, 0.16);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123796_, cast.impact.f_82479_, cast.impact.f_82480_ + 0.2, cast.impact.f_82481_, 8, radius * 0.3, 0.15, radius * 0.3, 0.04);
        level.m_6263_(null, cast.impact.f_82479_, cast.impact.f_82480_, cast.impact.f_82481_, SoundEvents.f_11983_, SoundSource.PLAYERS, 1.0f, 0.7f);
        level.m_6263_(null, cast.impact.f_82479_, cast.impact.f_82480_, cast.impact.f_82481_, SoundEvents.f_11913_, SoundSource.PLAYERS, 0.55f, 1.25f);
        player.m_21011_(InteractionHand.MAIN_HAND, true);
    }

    private static void ring(ServerLevel level, Vec3 center, double radius, boolean released) {
        if (released) {
            WeaponFx.burst(level, center.m_82520_(0.0, 0.05, 0.0), radius, WeaponFxRules.Theme.WINE);
        } else {
            WeaponFx.ring(level, center.m_82520_(0.0, 0.05, 0.0), radius, WeaponFxRules.Theme.GOLD);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CASTS.remove(event.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        CASTS.remove(event.getEntity().m_20148_());
        event.getEntity().getPersistentData().m_128473_("FTWineMacePressure");
        if (!event.isWasDeath() && event.getOriginal().getPersistentData().m_128441_("FTWineMacePressure")) {
            event.getEntity().getPersistentData().m_128365_("FTWineMacePressure", (Tag)event.getOriginal().getPersistentData().m_128469_("FTWineMacePressure").m_6426_());
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        CASTS.clear();
    }

    private static final class Cast {
        private final ResourceKey<Level> dimension;
        private final Vec3 start;
        private final Vec3 impact;
        private final ItemStack weapon;
        private final int pressure;
        private final float damage;
        private int age;

        private Cast(ServerPlayer player, Vec3 impact, int pressure) {
            this.dimension = player.m_9236_().m_46472_();
            this.start = player.m_20182_();
            this.impact = impact;
            this.weapon = player.m_21205_();
            this.pressure = pressure;
            this.damage = WineMaceRules.damage(player.m_21133_(Attributes.f_22281_), pressure);
        }
    }
}

