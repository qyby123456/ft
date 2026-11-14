/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.BossHitFeedback;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class WeaponFx {
    private static final String LAST_HIT = "ftboss:weapon_fx_hit";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "weapon_fx"), () -> "2", (Predicate)NetworkRegistry.acceptMissingOr((String)"2"), (Predicate)NetworkRegistry.acceptMissingOr((String)"2"));

    private WeaponFx() {
    }

    public static void burst(ServerLevel level, Vec3 center, double radius, WeaponFxRules.Theme theme) {
        WeaponFx.send(level, WeaponFxRules.Kind.BLAST, theme, center, center, radius, 0.0f);
    }

    public static void ring(ServerLevel level, Vec3 center, double radius, WeaponFxRules.Theme theme) {
        WeaponFx.send(level, WeaponFxRules.Kind.RING, theme, center, center, radius, 0.0f);
    }

    public static void beam(ServerLevel level, Vec3 start, Vec3 end, WeaponFxRules.Theme theme) {
        WeaponFx.send(level, WeaponFxRules.Kind.BEAM, theme, start, end, 0.15, 0.0f);
    }

    public static void send(ServerLevel level, WeaponFxRules.Kind kind, WeaponFxRules.Theme theme, Vec3 start, Vec3 end, double radius, float yaw) {
        WeaponFxPacket packet = new WeaponFxPacket(level.m_46472_().m_135782_(), kind, theme, start, end, (float)radius, yaw);
        if (!packet.valid()) {
            return;
        }
        Vec3 segment = end.m_82546_(start);
        double length = segment.m_82556_();
        double range = 48.0 + radius;
        for (ServerPlayer observer : level.m_6907_()) {
            double along;
            double d = along = length < 1.0E-8 ? 0.0 : Math.max(0.0, Math.min(1.0, observer.m_20182_().m_82546_(start).m_82526_(segment) / length));
            if (!(observer.m_20182_().m_82557_(start.m_82549_(segment.m_82490_(along))) <= range * range) || !CHANNEL.isRemotePresent(observer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> observer), (Object)packet);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        ServerPlayer player;
        block8: {
            block7: {
                Entity entity;
                if (event.getAmount() <= 0.0f || !((entity = event.getSource().m_7640_()) instanceof ServerPlayer)) break block7;
                player = (ServerPlayer)entity;
                if (event.getSource().m_276093_(DamageTypes.f_268464_) && !player.m_5833_()) break block8;
            }
            return;
        }
        ItemStack weapon = player.m_21205_();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey((Object)weapon.m_41720_());
        String identifier = item == null ? null : item.toString();
        boolean gourmet = "ftboss:gourmet_blank_blade".equals(identifier);
        if (!gourmet && BossHitFeedback.supports(event.getEntity())) {
            return;
        }
        if (WeaponFxRules.isDoraGunItem(identifier)) {
            return;
        }
        WeaponFxRules.Theme theme = WeaponFxRules.forItem(identifier);
        if (theme == null) {
            return;
        }
        long now = player.m_284548_().m_46467_();
        CompoundTag tag = player.getPersistentData();
        if (tag.m_128441_(LAST_HIT) && now >= tag.m_128454_(LAST_HIT) && now - tag.m_128454_(LAST_HIT) < 6L) {
            return;
        }
        tag.m_128356_(LAST_HIT, now);
        Vec3 center = event.getEntity().m_20191_().m_82399_();
        Vec3 facing = player.m_146892_().m_82546_(center).m_82541_();
        center = center.m_82549_(facing.m_82490_(Math.min(0.6, (double)event.getEntity().m_20205_() * 0.5)));
        double radius = gourmet ? 0.72 + (double)Math.min(8, FoodWeaponEvolution.evolutionLevel(weapon)) * 0.035 + (double)Math.min(30, FoodWeaponEvolution.socketCount(weapon)) * 0.008 : 0.65;
        WeaponFx.send(player.m_284548_(), gourmet ? WeaponFxRules.Kind.GOURMET_HIT : WeaponFxRules.Kind.HIT, theme, center, center, radius, player.m_146908_());
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> CHANNEL.messageBuilder(WeaponFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(WeaponFxPacket::encode).decoder(WeaponFxPacket::decode).consumerMainThread(WeaponFxPacket::handle).add());
        }
    }
}

