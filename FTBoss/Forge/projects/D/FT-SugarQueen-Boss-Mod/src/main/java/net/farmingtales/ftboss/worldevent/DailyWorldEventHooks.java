package net.farmingtales.ftboss.worldevent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.PoopCurse;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/** Runtime effects for the active daily event. Every hook self-registers because the deployed bootstrap is preserved. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class DailyWorldEventHooks {
    private static final String DAILY_SPAWN = "ftboss_daily_event_spawn";
    private static final String REPLACEMENT = "ftboss_daily_event_replacement";
    private static final String HARVEST_ADJUSTED = "ftboss_daily_harvest_adjusted";
    private static final ResourceLocation AMETHYST_CRAB = new ResourceLocation("cataclysm", "amethyst_crab");
    private static final ResourceLocation BEER_ELEMENTAL = new ResourceLocation("brewery", "beer_elemental");
    private static final Map<UUID, FoodSnapshot> FOOD_SNAPSHOTS = new HashMap<>();
    private static final Map<HarvestKey, HarvestContext> HARVESTS = new HashMap<>();

    private DailyWorldEventHooks() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
        MinecraftServer server = event.getServer();
        DailyWorldEventManager.tick(server);
        cleanupHarvests(server);
        DailyWorldEvent active = DailyWorldEventManager.active(server);
        if (active == DailyWorldEvent.NO_SUCH_CREATURE) playCreeperWhispers(server);
        if (active == DailyWorldEvent.STARFALL) showStarfall(server);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) DailyWorldEventManager.onLogin(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FOOD_SNAPSHOTS.remove(event.getEntity().getUUID());
    }

    /** FinalizeSpawn retains the spawn reason; EntityJoinLevelEvent is where replacement can safely cancel insertion. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void markDailySpawns(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        if (event.isSpawnCancelled() || mob.getPersistentData().getBoolean(REPLACEMENT)
                || !isDailySpawnType(event.getSpawnType())) return;
        mob.getPersistentData().putBoolean(DAILY_SPAWN, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void alterDailySpawns(EntityJoinLevelEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getPersistentData().getBoolean(DAILY_SPAWN)
                || mob.getPersistentData().getBoolean(REPLACEMENT) || protectedMob(mob)) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        if (active == null) return;
        switch (active) {
            case BIG_FISH_MIGRATION -> replaceAquatic(event, level, mob);
            case KITCHEN_CHAOS -> replaceKitchenMonster(event, level, mob);
            case NO_SUCH_CREATURE -> mountCreeper(level, mob);
            case UNDEAD_MIGRATION -> replaceWithUndead(event, level, mob);
            case PIG_MIGRATION -> {
                if (!(mob instanceof Pig)) replace(event, level, mob, EntityType.PIG);
            }
            case SLIME_RAIN -> replaceWithSlime(event, level, mob);
            case STARFALL -> {
                if (level.random.nextFloat() < 0.45F) mob.setGlowingTag(true);
            }
            default -> {
            }
        }
    }

    private static boolean isDailySpawnType(MobSpawnType type) {
        return type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION
                || type == MobSpawnType.SPAWNER || type == MobSpawnType.PATROL
                || type == MobSpawnType.REINFORCEMENT;
    }

    private static boolean protectedMob(Mob mob) {
        return mob.getType().getCategory() == MobCategory.MISC || BossMercy.typeOf(mob) != null;
    }

    private static void replaceAquatic(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (!(source instanceof AbstractFish)) return;
        int outcome = DailyWorldEventRules.aquaticOutcome(level.random.nextDouble());
        EntityType<?> target = switch (outcome) {
            case DailyWorldEventRules.AQUATIC_AMETHYST_CRAB -> dynamicType(AMETHYST_CRAB);
            case DailyWorldEventRules.AQUATIC_ELDER_GUARDIAN -> EntityType.ELDER_GUARDIAN;
            case DailyWorldEventRules.AQUATIC_GUARDIAN -> EntityType.GUARDIAN;
            case DailyWorldEventRules.AQUATIC_DROWNED -> EntityType.DROWNED;
            case DailyWorldEventRules.AQUATIC_VALUABLE_FISH -> {
                if (source instanceof TropicalFish || source instanceof Pufferfish) yield null;
                yield level.random.nextInt(4) == 0 ? EntityType.PUFFERFISH : EntityType.TROPICAL_FISH;
            }
            default -> null;
        };
        if (target != null) replace(event, level, source, target);
    }

    private static void replaceKitchenMonster(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getType().getCategory() != MobCategory.MONSTER) return;
        int outcome = DailyWorldEventRules.kitchenOutcome(level.random.nextDouble());
        EntityType<?> target = switch (outcome) {
            case DailyWorldEventRules.KITCHEN_GINGERBREAD -> FTEntities.GINGERBREAD_MINION.get();
            case DailyWorldEventRules.KITCHEN_BEER_ELEMENTAL -> dynamicType(BEER_ELEMENTAL);
            case DailyWorldEventRules.KITCHEN_EXPLOSIVE_BARREL -> FTEntities.BARREL_MINION.get();
            case DailyWorldEventRules.KITCHEN_SAYA_FLOWER -> FTEntities.TEA_MINION.get();
            default -> null;
        };
        if (target != null) replace(event, level, source, target);
    }

    private static void replaceWithUndead(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getMobType() == MobType.UNDEAD) return;
        EntityType<?> target;
        if (source instanceof WaterAnimal || source.isInWaterOrBubble()) target = EntityType.DROWNED;
        else if (level.dimension() == Level.NETHER) target = EntityType.ZOMBIFIED_PIGLIN;
        else target = level.random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON;
        replace(event, level, source, target);
    }

    private static void replaceWithSlime(EntityJoinLevelEvent event, ServerLevel level, Mob source) {
        if (source.getType().getCategory() != MobCategory.MONSTER || source instanceof Slime
                || level.random.nextFloat() >= 0.25F) return;
        EntityType<?> target = level.dimension() == Level.NETHER ? EntityType.MAGMA_CUBE : EntityType.SLIME;
        replace(event, level, source, target);
    }

    private static boolean replace(EntityJoinLevelEvent event, ServerLevel level, Mob source, EntityType<?> type) {
        Entity created = type.create(level);
        if (!(created instanceof Mob replacement)) return false;
        replacement.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        replacement.getPersistentData().putBoolean(REPLACEMENT, true);
        replacement.finalizeSpawn(level, level.getCurrentDifficultyAt(replacement.blockPosition()),
                MobSpawnType.EVENT, null, null);
        if (replacement instanceof Slime slime) slime.setSize(1 + level.random.nextInt(3), true);
        if (!level.noCollision(replacement) || !level.addFreshEntity(replacement)) return false;
        event.setCanceled(true);
        return true;
    }

    @Nullable
    private static EntityType<?> dynamicType(ResourceLocation id) {
        return ForgeRegistries.ENTITY_TYPES.containsKey(id) ? ForgeRegistries.ENTITY_TYPES.getValue(id) : null;
    }

    private static void mountCreeper(ServerLevel level, Mob source) {
        if (!(source instanceof Creeper creeper) || creeper.isPassenger()) return;
        Phantom phantom = EntityType.PHANTOM.create(level);
        if (phantom == null) return;
        phantom.moveTo(creeper.getX(), creeper.getY() + 2.0D, creeper.getZ(), creeper.getYRot(), 0.0F);
        phantom.getPersistentData().putBoolean(REPLACEMENT, true);
        phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(phantom.blockPosition()), MobSpawnType.EVENT, null, null);
        phantom.setPhantomSize(level.random.nextInt(2));
        if (level.noCollision(phantom) && level.addFreshEntity(phantom)) creeper.startRiding(phantom, true);
    }

    private static void playCreeperWhispers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level) || level.random.nextInt(45) != 0) continue;
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = 2.5D + level.random.nextDouble() * 3.5D;
            level.playSound(null, player.getX() + Math.cos(angle) * distance, player.getEyeY(),
                    player.getZ() + Math.sin(angle) * distance, SoundEvents.CREEPER_PRIMED,
                    SoundSource.HOSTILE, 0.72F, 0.8F + level.random.nextFloat() * 0.25F);
        }
    }

    private static void showStarfall(MinecraftServer server) {
        if (server.overworld().isDay()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 7.0D, player.getZ(),
                    4, 7.0D, 2.5D, 7.0D, 0.018D);
        }
    }

    @SubscribeEvent
    public static void onCropGrowth(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (DailyWorldEventManager.active(level.getServer()) == DailyWorldEvent.HARVEST_MOON) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void rememberCropHarvest(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.getPlayer().isCreative()
                || !isMatureCrop(event.getState())) return;
        DailyWorldEvent active = DailyWorldEventManager.active(level.getServer());
        int multiplier;
        if (active == DailyWorldEvent.KITCHEN_CHAOS) {
            multiplier = DailyWorldEventRules.kitchenHarvestMultiplier(level.random.nextDouble());
        } else if (active == DailyWorldEvent.HARVEST_MOON) {
            multiplier = 2;
        } else return;
        HARVESTS.put(new HarvestKey(level.dimension(), event.getPos().asLong()),
                new HarvestContext(multiplier, level.getGameTime() + 3L));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void adjustCropDrops(EntityJoinLevelEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ItemEntity item)
                || item.getPersistentData().getBoolean(HARVEST_ADJUSTED)) return;
        HarvestContext context = harvestAt(level, item.blockPosition());
        if (context == null) return;
        item.getPersistentData().putBoolean(HARVEST_ADJUSTED, true);
        if (context.multiplier() == 0) {
            event.setCanceled(true);
            return;
        }
        ItemStack adjusted = item.getItem().copy();
        adjusted.setCount(Math.min(adjusted.getMaxStackSize(), adjusted.getCount() * context.multiplier()));
        item.setItem(adjusted);
    }

    @Nullable
    private static HarvestContext harvestAt(ServerLevel level, BlockPos itemPos) {
        HarvestContext context = HARVESTS.get(new HarvestKey(level.dimension(), itemPos.asLong()));
        return context != null && context.expiresAt() >= level.getGameTime() ? context : null;
    }

    private static boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
        if (state.getBlock() instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= 3;
        if (state.getBlock() instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= 2;
        return state.getBlock() instanceof StemGrownBlock;
    }

    private static void cleanupHarvests(MinecraftServer server) {
        Iterator<Map.Entry<HarvestKey, HarvestContext>> entries = HARVESTS.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<HarvestKey, HarvestContext> entry = entries.next();
            ServerLevel level = server.getLevel(entry.getKey().dimension());
            if (level == null || level.getGameTime() > entry.getValue().expiresAt()) entries.remove();
        }
    }

    @SubscribeEvent
    public static void snapshotFood(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getItem().getFoodProperties(player) == null
                || DailyWorldEventManager.active(player.server) != DailyWorldEvent.HEAVY_SNOW) return;
        FOOD_SNAPSHOTS.put(player.getUUID(), FoodSnapshot.capture(player));
    }

    @SubscribeEvent
    public static void stopFoodSnapshot(LivingEntityUseItemEvent.Stop event) {
        FOOD_SNAPSHOTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void turnFoodIntoPoop(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getItem().getFoodProperties(player) == null) return;
        FoodSnapshot snapshot = FOOD_SNAPSHOTS.remove(player.getUUID());
        if (DailyWorldEventManager.active(player.server) != DailyWorldEvent.HEAVY_SNOW) return;
        if (snapshot != null) snapshot.restore(player);
        PoopCurse.castOnce(player, "大雪");
    }

    @SubscribeEvent
    public static void starfallDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getEntity() instanceof Enemy)
                || !(event.getSource().getEntity() instanceof ServerPlayer)
                || DailyWorldEventManager.active(level.getServer()) != DailyWorldEvent.STARFALL) return;
        ItemStack reward = ItemStack.EMPTY;
        double roll = level.random.nextDouble();
        if (roll < 0.02D) reward = new ItemStack(Items.EMERALD);
        else if (roll < 0.10D) reward = new ItemStack(Items.AMETHYST_SHARD);
        if (!reward.isEmpty()) event.getDrops().add(new ItemEntity(level, event.getEntity().getX(),
                event.getEntity().getY() + 0.4D, event.getEntity().getZ(), reward));
    }

    private record HarvestKey(ResourceKey<Level> dimension, long position) {}
    private record HarvestContext(int multiplier, long expiresAt) {}

    private record FoodSnapshot(int food, float saturation, float health, long startedAt,
                                Map<MobEffect, MobEffectInstance> effects) {
        private static FoodSnapshot capture(ServerPlayer player) {
            Map<MobEffect, MobEffectInstance> effects = new HashMap<>();
            player.getActiveEffects().forEach(instance ->
                    effects.put(instance.getEffect(), new MobEffectInstance(instance)));
            return new FoodSnapshot(player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel(),
                    player.getHealth(), player.level().getGameTime(), effects);
        }

        private void restore(ServerPlayer player) {
            FoodData data = player.getFoodData();
            data.setFoodLevel(this.food);
            data.setSaturation(this.saturation);
            if (player.getHealth() > this.health) player.setHealth(this.health);
            player.getActiveEffects().stream().map(MobEffectInstance::getEffect)
                    .filter(effect -> !this.effects.containsKey(effect)).toList().forEach(player::removeEffect);
            int elapsed = (int) Math.max(0L, Math.min(Integer.MAX_VALUE,
                    player.level().getGameTime() - this.startedAt));
            this.effects.forEach((effect, before) -> {
                int expectedDuration = before.getDuration() - elapsed;
                MobEffectInstance current = player.getEffect(effect);
                if (expectedDuration <= 0) {
                    if (current != null) player.removeEffect(effect);
                    return;
                }
                boolean foodChangedEffect = current == null || current.getAmplifier() != before.getAmplifier()
                        || current.getDuration() > expectedDuration + 2;
                if (!foodChangedEffect) return;
                player.removeEffect(effect);
                player.addEffect(new MobEffectInstance(effect, expectedDuration, before.getAmplifier(),
                        before.isAmbient(), before.isVisible(), before.showIcon()));
            });
        }
    }
}