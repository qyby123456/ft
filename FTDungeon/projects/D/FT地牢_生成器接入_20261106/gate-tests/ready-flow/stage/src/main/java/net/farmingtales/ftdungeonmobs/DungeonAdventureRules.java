package net.farmingtales.ftdungeonmobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTDungeonMobs.ID)
public final class DungeonAdventureRules {
    static final String STATE_KEY = "FTDungeonAdventure";

    private DungeonAdventureRules() {}

    private static boolean protectedLevel(Level level) {
        return level.dimension().equals(DungeonGateTravel.DUNGEON);
    }

    static boolean canOffer(Player player) {
        return !player.isSpectator() && (player.mayBuild() || protectedLevel(player.level()));
    }

    private static void remember(ServerPlayer player, GameType mode) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persistent.contains(STATE_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag state = new CompoundTag();
            state.putInt("OriginalMode", mode.getId());
            persistent.put(STATE_KEY, state);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
        }
    }

    static void synchronize(ServerPlayer player) {
        if (protectedLevel(player.level())) {
            remember(player, player.gameMode.getGameModeForPlayer());
            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                player.setGameMode(GameType.ADVENTURE);
                player.displayClientMessage(Component.translatable("message.ftdungeonmobs.adventure.enter"), true);
            }
            return;
        }
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persistent.contains(STATE_KEY, Tag.TAG_COMPOUND)) {
            int identity = persistent.getCompound(STATE_KEY).getInt("OriginalMode");
            GameType mode = identity >= 0 && identity <= 3 ? GameType.byId(identity) : GameType.SURVIVAL;
            if (player.gameMode.getGameModeForPlayer() != mode) {
                player.setGameMode(mode);
            }
            if (player.gameMode.getGameModeForPlayer() == mode) {
                persistent.remove(STATE_KEY);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && protectedLevel(player.level())) {
            remember(player, event.getCurrentGameMode());
            event.setNewGameMode(GameType.ADVENTURE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            synchronize(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            synchronize(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            synchronize(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag previous = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (previous.contains(STATE_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag persistent = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            persistent.put(STATE_KEY, previous.getCompound(STATE_KEY).copy());
            event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.tickCount % 10 == 0) {
            synchronize(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protectedLevel(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (protectedLevel(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (protectedLevel(event.getLevel())) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Item item = event.getItemStack().getItem();
        if (protectedLevel(event.getLevel()) && (item instanceof BlockItem || item instanceof BucketItem
                || item instanceof FlintAndSteelItem || item instanceof FireChargeItem || item instanceof BoneMealItem
                || item instanceof SpawnEggItem || item instanceof EndCrystalItem || item instanceof BoatItem || item instanceof MinecartItem)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protectedLevel(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBucket(FillBucketEvent event) {
        if (protectedLevel(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protectedLevel(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protectedLevel(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (protectedLevel(event.getEntity().level()) && !(event.getTarget() instanceof Mob monster
                && (monster instanceof Enemy || monster.getType().getCategory() == MobCategory.MONSTER
                    || DungeonEncounterPool.rank(net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(monster.getType())) != null))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (protectedLevel(event.getLevel())) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        if (protectedLevel(event.getEntity().level())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDestroy(LivingDestroyBlockEvent event) {
        if (protectedLevel(event.getEntity().level())) {
            event.setCanceled(true);
        }
    }
}
