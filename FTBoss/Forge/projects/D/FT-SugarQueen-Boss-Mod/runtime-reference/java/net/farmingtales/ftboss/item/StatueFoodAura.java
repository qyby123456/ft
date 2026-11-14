/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class StatueFoodAura
extends SavedData {
    public static final int FOOD_COST = 4;
    public static final int RADIUS = 32;
    public static final int DURATION_TICKS = 12000;
    private static final int REFRESH_INTERVAL = 40;
    private static final int EFFECT_DURATION = 100;
    private static final String DATA_NAME = "ftboss_statue_food_auras";
    private final List<Aura> auras = new ArrayList<Aura>();

    private StatueFoodAura() {
    }

    private static StatueFoodAura get(MinecraftServer server) {
        return (StatueFoodAura)server.m_129783_().m_8895_().m_164861_(StatueFoodAura::load, StatueFoodAura::new, DATA_NAME);
    }

    private static StatueFoodAura load(CompoundTag tag) {
        StatueFoodAura data = new StatueFoodAura();
        for (Tag raw : tag.m_128437_("Auras", 10)) {
            CompoundTag entry = (CompoundTag)raw;
            ResourceLocation dimension = ResourceLocation.m_135820_((String)entry.m_128461_("Dimension"));
            ResourceLocation effect = ResourceLocation.m_135820_((String)entry.m_128461_("Effect"));
            if (dimension == null || effect == null) continue;
            data.auras.add(new Aura((ResourceKey<Level>)ResourceKey.m_135785_((ResourceKey)Registries.f_256858_, (ResourceLocation)dimension), BlockPos.m_122022_((long)entry.m_128454_("Position")), entry.m_128461_("Profile"), effect, entry.m_128454_("ExpiresAt")));
        }
        return data;
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Aura aura : this.auras) {
            CompoundTag entry = new CompoundTag();
            entry.m_128359_("Dimension", aura.dimension.m_135782_().toString());
            entry.m_128356_("Position", aura.pos.m_121878_());
            entry.m_128359_("Profile", aura.profile);
            entry.m_128359_("Effect", aura.effect.toString());
            entry.m_128356_("ExpiresAt", aura.expiresAt);
            list.add((Object)entry);
        }
        tag.m_128365_("Auras", (Tag)list);
        return tag;
    }

    public static void offer(ServerPlayer player, BlockPos statuePos, String profileId, Component profileName, FoodWeaponEvolution.FoodType expected, ItemStack food, FoodWeaponEvolution.FoodType actual) {
        if (actual != expected) {
            player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.aura.wrong_food", (Object[])new Object[]{expected.displayName()}), true);
            return;
        }
        if (!player.m_150110_().f_35937_ && food.m_41613_() < 4) {
            player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.aura.need_food", (Object[])new Object[]{4, expected.displayName()}), true);
            return;
        }
        ServerLevel level = player.m_284548_();
        MobEffect[] pool = StatueFoodAura.effectPool(profileId);
        StatueFoodAura data = StatueFoodAura.get(level.m_7654_());
        Aura previous = data.find((ResourceKey<Level>)level.m_46472_(), statuePos);
        int picked = player.m_217043_().m_188503_(pool.length);
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey((Object)pool[picked]);
        if (previous != null && pool.length > 1 && effectId != null && effectId.equals((Object)previous.effect)) {
            effectId = ForgeRegistries.MOB_EFFECTS.getKey((Object)pool[(picked + 1) % pool.length]);
        }
        if (effectId == null) {
            return;
        }
        if (!player.m_150110_().f_35937_) {
            food.m_41774_(4);
        }
        data.auras.removeIf(aura -> aura.dimension.equals((Object)level.m_46472_()) && aura.pos.equals((Object)statuePos));
        data.auras.add(new Aura((ResourceKey<Level>)level.m_46472_(), statuePos.m_7949_(), profileId, effectId, level.m_46467_() + 12000L));
        data.m_77762_();
        MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(effectId);
        player.m_213846_((Component)Component.m_237110_((String)"message.ftboss.aura.activated", (Object[])new Object[]{profileName, 32, StatueFoodAura.effectName(effect), 10}).m_130940_(ChatFormatting.AQUA));
        level.m_5594_(null, statuePos, SoundEvents.f_11736_, SoundSource.BLOCKS, 1.0f, 1.15f);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123748_, (double)statuePos.m_123341_() + 0.5, (double)statuePos.m_123342_() + 1.2, (double)statuePos.m_123343_() + 0.5, 28, 0.65, 1.0, 0.65, 0.05);
    }

    public static void showStatus(ServerPlayer player, BlockPos statuePos) {
        StatueFoodAura data = StatueFoodAura.get(player.m_284548_().m_7654_());
        Aura aura = data.find((ResourceKey<Level>)player.m_9236_().m_46472_(), statuePos);
        if (aura == null || aura.expiresAt <= player.m_284548_().m_46467_()) {
            player.m_213846_((Component)Component.m_237110_((String)"message.ftboss.aura.inactive", (Object[])new Object[]{4}).m_130940_(ChatFormatting.DARK_GRAY));
            return;
        }
        MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(aura.effect);
        long seconds = Math.max(1L, (aura.expiresAt - player.m_284548_().m_46467_()) / 20L);
        player.m_213846_((Component)Component.m_237110_((String)"message.ftboss.aura.status", (Object[])new Object[]{StatueFoodAura.effectName(effect), 32, seconds / 60L, seconds % 60L}).m_130940_(ChatFormatting.AQUA));
    }

    private Aura find(ResourceKey<Level> dimension, BlockPos pos) {
        for (Aura aura : this.auras) {
            if (!aura.dimension.equals(dimension) || !aura.pos.equals((Object)pos)) continue;
            return aura;
        }
        return null;
    }

    private static Component effectName(MobEffect effect) {
        return effect == null ? Component.m_237113_((String)"?") : Component.m_237115_((String)effect.m_19481_());
    }

    private static MobEffect[] effectPool(String profile) {
        MobEffect[] mobEffectArray;
        switch (profile) {
            case "sugar": {
                MobEffect[] mobEffectArray2 = new MobEffect[3];
                mobEffectArray2[0] = MobEffects.f_19596_;
                mobEffectArray2[1] = MobEffects.f_19603_;
                mobEffectArray = mobEffectArray2;
                mobEffectArray2[2] = MobEffects.f_19617_;
                break;
            }
            case "butcher": {
                MobEffect[] mobEffectArray3 = new MobEffect[3];
                mobEffectArray3[0] = MobEffects.f_19600_;
                mobEffectArray3[1] = MobEffects.f_19606_;
                mobEffectArray = mobEffectArray3;
                mobEffectArray3[2] = MobEffects.f_19605_;
                break;
            }
            case "saya": {
                MobEffect[] mobEffectArray4 = new MobEffect[3];
                mobEffectArray4[0] = MobEffects.f_19598_;
                mobEffectArray4[1] = MobEffects.f_19596_;
                mobEffectArray = mobEffectArray4;
                mobEffectArray4[2] = MobEffects.f_19605_;
                break;
            }
            case "wine": {
                MobEffect[] mobEffectArray5 = new MobEffect[3];
                mobEffectArray5[0] = MobEffects.f_19600_;
                mobEffectArray5[1] = MobEffects.f_19606_;
                mobEffectArray = mobEffectArray5;
                mobEffectArray5[2] = MobEffects.f_19621_;
                break;
            }
            case "rat": {
                MobEffect[] mobEffectArray6 = new MobEffect[3];
                mobEffectArray6[0] = MobEffects.f_19598_;
                mobEffectArray6[1] = MobEffects.f_19606_;
                mobEffectArray = mobEffectArray6;
                mobEffectArray6[2] = MobEffects.f_19621_;
                break;
            }
            case "defect": {
                MobEffect[] mobEffectArray7 = new MobEffect[3];
                mobEffectArray7[0] = MobEffects.f_19598_;
                mobEffectArray7[1] = MobEffects.f_19611_;
                mobEffectArray = mobEffectArray7;
                mobEffectArray7[2] = MobEffects.f_19596_;
                break;
            }
            case "compost": {
                MobEffect[] mobEffectArray8 = new MobEffect[3];
                mobEffectArray8[0] = MobEffects.f_19605_;
                mobEffectArray8[1] = MobEffects.f_19606_;
                mobEffectArray = mobEffectArray8;
                mobEffectArray8[2] = MobEffects.f_19617_;
                break;
            }
            case "apple": {
                MobEffect[] mobEffectArray9 = new MobEffect[3];
                mobEffectArray9[0] = MobEffects.f_19605_;
                mobEffectArray9[1] = MobEffects.f_19617_;
                mobEffectArray = mobEffectArray9;
                mobEffectArray9[2] = MobEffects.f_19616_;
                break;
            }
            case "tide": {
                MobEffect[] mobEffectArray10 = new MobEffect[3];
                mobEffectArray10[0] = MobEffects.f_19608_;
                mobEffectArray10[1] = MobEffects.f_19593_;
                mobEffectArray = mobEffectArray10;
                mobEffectArray10[2] = MobEffects.f_19592_;
                break;
            }
            case "benger": {
                MobEffect[] mobEffectArray11 = new MobEffect[3];
                mobEffectArray11[0] = MobEffects.f_19596_;
                mobEffectArray11[1] = MobEffects.f_19600_;
                mobEffectArray = mobEffectArray11;
                mobEffectArray11[2] = MobEffects.f_19603_;
                break;
            }
            default: {
                MobEffect[] mobEffectArray12 = new MobEffect[3];
                mobEffectArray12[0] = MobEffects.f_19605_;
                mobEffectArray12[1] = MobEffects.f_19596_;
                mobEffectArray = mobEffectArray12;
                mobEffectArray12[2] = MobEffects.f_19598_;
            }
        }
        return mobEffectArray;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().m_129921_() % 40 != 0) {
            return;
        }
        StatueFoodAura data = StatueFoodAura.get(event.getServer());
        boolean changed = false;
        Iterator<Aura> iterator = data.auras.iterator();
        while (iterator.hasNext()) {
            Aura aura = iterator.next();
            ServerLevel level = event.getServer().m_129880_(aura.dimension);
            if (level == null) continue;
            if (level.m_46467_() >= aura.expiresAt) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (level.m_46805_(aura.pos) && !(level.m_8055_(aura.pos).m_60734_() instanceof CandyStatueBlock)) {
                iterator.remove();
                changed = true;
                continue;
            }
            MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(aura.effect);
            if (effect == null) {
                iterator.remove();
                changed = true;
                continue;
            }
            double cx = (double)aura.pos.m_123341_() + 0.5;
            double cy = (double)aura.pos.m_123342_() + 1.0;
            double cz = (double)aura.pos.m_123343_() + 0.5;
            double rangeSquared = 1024.0;
            for (ServerPlayer player : level.m_6907_()) {
                if (!(player.m_20275_(cx, cy, cz) <= rangeSquared)) continue;
                player.m_7292_(new MobEffectInstance(effect, 100, 0, true, true, true));
            }
            if (!level.m_46805_(aura.pos)) continue;
            level.m_8767_((ParticleOptions)ParticleTypes.f_123809_, cx, cy + 0.35, cz, 8, 0.55, 0.8, 0.55, 0.02);
        }
        if (changed) {
            data.m_77762_();
        }
    }

    @SubscribeEvent
    public static void broken(BlockEvent.BreakEvent event) {
        ServerLevel level;
        block6: {
            block5: {
                LevelAccessor levelAccessor = event.getLevel();
                if (!(levelAccessor instanceof ServerLevel)) break block5;
                level = (ServerLevel)levelAccessor;
                if (event.getState().m_60734_() instanceof CandyStatueBlock) break block6;
            }
            return;
        }
        BlockPos pos = event.getPos();
        if (event.getState().m_61143_((Property)CandyStatueBlock.f_52858_) == DoubleBlockHalf.UPPER) {
            pos = pos.m_7495_();
        }
        StatueFoodAura data = StatueFoodAura.get(level.m_7654_());
        BlockPos target = pos;
        if (data.auras.removeIf(aura -> aura.dimension.equals((Object)level.m_46472_()) && aura.pos.equals((Object)target))) {
            data.m_77762_();
        }
    }

    private record Aura(ResourceKey<Level> dimension, BlockPos pos, String profile, ResourceLocation effect, long expiresAt) {
    }
}

