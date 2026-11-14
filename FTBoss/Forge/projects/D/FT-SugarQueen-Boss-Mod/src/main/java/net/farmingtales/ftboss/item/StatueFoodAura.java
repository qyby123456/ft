package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Persistent food-powered statue fields: a lightweight, category-themed beacon system. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class StatueFoodAura extends SavedData {
    public static final int FOOD_COST = 4;
    public static final int RADIUS = 32;
    public static final int DURATION_TICKS = 12_000;
    private static final int REFRESH_INTERVAL = 40;
    private static final int EFFECT_DURATION = 100;
    private static final String DATA_NAME = "ftboss_statue_food_auras";

    private final List<Aura> auras = new ArrayList<>();

    private record Aura(ResourceKey<Level> dimension, BlockPos pos, String profile,
                        ResourceLocation effect, long expiresAt) {}

    private StatueFoodAura() {}

    private static StatueFoodAura get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StatueFoodAura::load,
                StatueFoodAura::new, DATA_NAME);
    }

    private static StatueFoodAura load(CompoundTag tag) {
        StatueFoodAura data = new StatueFoodAura();
        for (Tag raw : tag.getList("Auras", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dimension"));
            ResourceLocation effect = ResourceLocation.tryParse(entry.getString("Effect"));
            if (dimension == null || effect == null) continue;
            data.auras.add(new Aura(ResourceKey.create(Registries.DIMENSION, dimension),
                    BlockPos.of(entry.getLong("Position")), entry.getString("Profile"), effect,
                    entry.getLong("ExpiresAt")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Aura aura : this.auras) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Dimension", aura.dimension.location().toString());
            entry.putLong("Position", aura.pos.asLong());
            entry.putString("Profile", aura.profile);
            entry.putString("Effect", aura.effect.toString());
            entry.putLong("ExpiresAt", aura.expiresAt);
            list.add(entry);
        }
        tag.put("Auras", list);
        return tag;
    }

    public static void offer(ServerPlayer player, BlockPos statuePos, String profileId, Component profileName,
                             FoodType expected, ItemStack food, FoodType actual) {
        if (actual != expected) {
            player.displayClientMessage(Component.translatable("message.ftboss.aura.wrong_food",
                    expected.displayName()), true);
            return;
        }
        if (!player.getAbilities().instabuild && food.getCount() < FOOD_COST) {
            player.displayClientMessage(Component.translatable("message.ftboss.aura.need_food",
                    FOOD_COST, expected.displayName()), true);
            return;
        }
        ServerLevel level = player.serverLevel();
        MobEffect[] pool = effectPool(profileId);
        StatueFoodAura data = get(level.getServer());
        Aura previous = data.find(level.dimension(), statuePos);
        int picked = player.getRandom().nextInt(pool.length);
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(pool[picked]);
        if (previous != null && pool.length > 1 && effectId != null && effectId.equals(previous.effect)) {
            effectId = ForgeRegistries.MOB_EFFECTS.getKey(pool[(picked + 1) % pool.length]);
        }
        if (effectId == null) return;
        if (!player.getAbilities().instabuild) food.shrink(FOOD_COST);
        data.auras.removeIf(aura -> aura.dimension.equals(level.dimension()) && aura.pos.equals(statuePos));
        data.auras.add(new Aura(level.dimension(), statuePos.immutable(), profileId, effectId,
                level.getGameTime() + DURATION_TICKS));
        data.setDirty();

        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
        player.sendSystemMessage(Component.translatable("message.ftboss.aura.activated", profileName,
                RADIUS, effectName(effect), DURATION_TICKS / 1200).withStyle(ChatFormatting.AQUA));
        level.playSound(null, statuePos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.15F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, statuePos.getX() + 0.5, statuePos.getY() + 1.2,
                statuePos.getZ() + 0.5, 28, 0.65, 1.0, 0.65, 0.05);
    }

    public static void showStatus(ServerPlayer player, BlockPos statuePos) {
        StatueFoodAura data = get(player.serverLevel().getServer());
        Aura aura = data.find(player.level().dimension(), statuePos);
        if (aura == null || aura.expiresAt <= player.serverLevel().getGameTime()) {
            player.sendSystemMessage(Component.translatable("message.ftboss.aura.inactive", FOOD_COST)
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(aura.effect);
        long seconds = Math.max(1, (aura.expiresAt - player.serverLevel().getGameTime()) / 20);
        player.sendSystemMessage(Component.translatable("message.ftboss.aura.status", effectName(effect), RADIUS,
                seconds / 60, seconds % 60).withStyle(ChatFormatting.AQUA));
    }

    private Aura find(ResourceKey<Level> dimension, BlockPos pos) {
        for (Aura aura : this.auras) {
            if (aura.dimension.equals(dimension) && aura.pos.equals(pos)) return aura;
        }
        return null;
    }

    private static Component effectName(MobEffect effect) {
        return effect == null ? Component.literal("?") : Component.translatable(effect.getDescriptionId());
    }

    private static MobEffect[] effectPool(String profile) {
        return switch (profile) {
            case "sugar" -> new MobEffect[]{MobEffects.MOVEMENT_SPEED, MobEffects.JUMP, MobEffects.ABSORPTION};
            case "butcher" -> new MobEffect[]{MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE, MobEffects.REGENERATION};
            case "saya" -> new MobEffect[]{MobEffects.DIG_SPEED, MobEffects.MOVEMENT_SPEED, MobEffects.REGENERATION};
            case "wine" -> new MobEffect[]{MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE, MobEffects.LUCK};
            case "rat" -> new MobEffect[]{MobEffects.DIG_SPEED, MobEffects.DAMAGE_RESISTANCE, MobEffects.LUCK};
            case "defect" -> new MobEffect[]{MobEffects.DIG_SPEED, MobEffects.NIGHT_VISION, MobEffects.MOVEMENT_SPEED};
            case "compost" -> new MobEffect[]{MobEffects.REGENERATION, MobEffects.DAMAGE_RESISTANCE, MobEffects.ABSORPTION};
            case "apple" -> new MobEffect[]{MobEffects.REGENERATION, MobEffects.ABSORPTION, MobEffects.HEALTH_BOOST};
            case "tide" -> new MobEffect[]{MobEffects.WATER_BREATHING, MobEffects.DOLPHINS_GRACE, MobEffects.CONDUIT_POWER};
            case "benger" -> new MobEffect[]{MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.JUMP};
            default -> new MobEffect[]{MobEffects.REGENERATION, MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED};
        };
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % REFRESH_INTERVAL != 0) return;
        StatueFoodAura data = get(event.getServer());
        boolean changed = false;
        Iterator<Aura> iterator = data.auras.iterator();
        while (iterator.hasNext()) {
            Aura aura = iterator.next();
            ServerLevel level = event.getServer().getLevel(aura.dimension);
            if (level == null) continue;
            if (level.getGameTime() >= aura.expiresAt) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (level.hasChunkAt(aura.pos)
                    && !(level.getBlockState(aura.pos).getBlock() instanceof CandyStatueBlock)) {
                iterator.remove();
                changed = true;
                continue;
            }
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(aura.effect);
            if (effect == null) {
                iterator.remove();
                changed = true;
                continue;
            }
            double cx = aura.pos.getX() + 0.5;
            double cy = aura.pos.getY() + 1.0;
            double cz = aura.pos.getZ() + 0.5;
            double rangeSquared = RADIUS * RADIUS;
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(cx, cy, cz) <= rangeSquared) {
                    player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, 0, true, true, true));
                }
            }
            if (level.hasChunkAt(aura.pos)) {
                level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.35, cz, 8, 0.55, 0.8, 0.55, 0.02);
            }
        }
        if (changed) data.setDirty();
    }

    @SubscribeEvent
    public static void broken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getState().getBlock() instanceof CandyStatueBlock)) return;
        BlockPos pos = event.getPos();
        if (event.getState().getValue(CandyStatueBlock.HALF) == DoubleBlockHalf.UPPER) pos = pos.below();
        StatueFoodAura data = get(level.getServer());
        BlockPos target = pos;
        if (data.auras.removeIf(aura -> aura.dimension.equals(level.dimension()) && aura.pos.equals(target))) {
            data.setDirty();
        }
    }
}
