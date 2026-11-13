package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftdungeonmobs.DungeonEncounterPool.Rank;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Area;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Point;
import net.farmingtales.ftdungeonmobs.DungeonSpawnData.Room;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DungeonRoomRulesGameTests {
    private DungeonRoomRulesGameTests() {}

    @GameTest(template = "empty", batch = "zzz_room_rules", timeoutTicks = 400)
    public static void classifiedNativeEncounters(GameTestHelper helper) {
        ServerLevel level = dungeon(helper);
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        BlockPos origin = new BlockPos(2048, 64, 2048);
        Area area = new Area("M99", origin, new Vec3i(24, 14, 24), origin.offset(12, 1, 0), "test");
        for (BlockPos position : BlockPos.betweenClosed(origin, origin.offset(23, 13, 23))) {
            level.setBlock(position, position.getY() == origin.getY() ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (String identity : List.of("S1", "S2", "S3", "LEGACY_30_1")) {
            helper.assertTrue(DungeonEncounterPool.roomRank(identity) == Rank.NORMAL, "Small room rank: " + identity);
        }
        for (String identity : List.of("M1", "M2", "L1", "L2", "L3", "L4", "LEGACY_31", "CHALLENGE_TEST")) {
            helper.assertTrue(DungeonEncounterPool.roomRank(identity) == Rank.ELITE, "Medium/challenge room rank: " + identity);
        }
        for (String identity : List.of("minecraft:wither", "minecraft:ender_dragon", "minecraft:warden",
                "cataclysm:ender_guardian", "cataclysm:ignis", "cataclysm:netherite_monstrosity", "cataclysm:the_harbinger",
                "cataclysm:the_leviathan", "cataclysm:ancient_remnant", "cataclysm:maledictus", "cataclysm:scylla",
                "cataclysm:the_baby_leviathan", "cataclysm:modern_remnant", "cataclysm:netherite_ministrosity")) {
            helper.assertTrue(DungeonEncounterPool.rank(ResourceLocation.tryParse(identity)) == null, "No full boss or pet: " + identity);
        }
        DungeonSpawnData data = new DungeonSpawnData();
        level.getDataStorage().set(DungeonSpawnData.FILE_NAME, data);
        DungeonSpawnPoints.beginRound(level, data, UUID.randomUUID(), 5);
        Room generated = DungeonSpawnPoints.generate(level, data, area);
        helper.assertTrue(!generated.points().isEmpty() && generated.points().stream().flatMap(point -> point.group.stream())
                .allMatch(identity -> DungeonEncounterPool.rank(identity) == Rank.ELITE), "Medium rooms contain elites only");
        generated.points().forEach(point -> level.removeBlock(point.position, false));
        data.rooms.clear();
        Point legacy = new Point(origin.offset(12, 1, 12), List.of(FTDungeonMobs.resource("skeleton_archer"),
                FTDungeonMobs.resource("skeleton_warrior"), FTDungeonMobs.resource("skeleton_mage")));
        legacy.spawned = 1;
        legacy.activated = true;
        data.rooms.put(area.id(), new Room(area, List.of(legacy)));
        CompoundTag oldSave = data.save(new CompoundTag());
        oldSave.remove("RosterVersion");
        oldSave.getList("Rooms", 10).getCompound(0).getList("Points", 10).getCompound(0).put("Group",
                strings("skeleton_archer", "skeleton_warrior", "skeleton_mage"));
        DungeonSpawnData migrated = DungeonSpawnData.load(oldSave);
        DungeonSpawnPoints.classifyLegacyGroups(level, migrated);
        Point replacement = migrated.rooms.get(area.id()).points().get(0);
        helper.assertTrue(replacement.spawned == 1 && replacement.activated && replacement.group.get(0).equals(legacy.group.get(0))
                        && replacement.group.subList(1, 3).stream().allMatch(identity -> DungeonEncounterPool.rank(identity) == Rank.ELITE),
                "Migration preserves consumed progress and only changes unspawned ordinary monsters in elite rooms");
        helper.assertTrue(DungeonSpawnData.load(migrated.save(new CompoundTag())).classified, "Migration is persisted, never re-rolled on login");
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "FTNativeSpawn"));
        player.setGameMode(GameType.ADVENTURE);
        DungeonGateData gate = new DungeonGateData();
        level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", gate);
        gate.bind(Level.OVERWORLD, new BlockPos(100, 64, 100));
        gate.setTier(5);
        gate.startRound(player.getUUID(), level.getServer().overworld().getGameTime());
        DungeonSpawnPoints.beginRound(level, data, gate.roundId(), 5);
        List<ResourceLocation> identities = new ArrayList<>(List.of(ResourceLocation.tryParse("minecraft:zombie"),
                ResourceLocation.tryParse("minecraft:vindicator"), ResourceLocation.tryParse("minecraft:witch"),
                ResourceLocation.tryParse("minecraft:wither_skeleton"), ResourceLocation.tryParse("minecraft:piglin_brute")));
        if (ModList.get().isLoaded("cataclysm")) {
            for (Rank rank : Rank.values()) {
                for (String value : DungeonEncounterPool.defaults(rank)) {
                    ResourceLocation identity = ResourceLocation.tryParse(value);
                    if (identity.getNamespace().equals("cataclysm")) {
                        helper.assertTrue(DungeonEncounterPool.type(identity) != null, "Installed Cataclysm 3.31 registry ID: " + identity);
                        identities.add(identity);
                    }
                }
            }
        } else {
            helper.assertTrue(DungeonEncounterPool.available(Rank.ELITE).stream().noneMatch(identity -> identity.getNamespace().equals("cataclysm")),
                    "Optional missing mod is safely skipped without spawning ordinary monsters in elite rooms");
        }
        for (ResourceLocation identity : identities) {
            Point point = new Point(origin.offset(12, 1, 12), List.of(identity));
            data.rooms.put(area.id(), new Room(area, List.of(point)));
            level.setBlock(point.position, FTDungeonMobs.DUNGEON_SPAWN_POINT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            player.moveTo(Vec3.atBottomCenterOf(point.position.offset(0, 0, 5)));
            DungeonSpawnPoints.tick(level, List.of(player));
            List<Mob> spawned = owned(level, gate.roundId());
            helper.assertTrue(point.spawned == 1 && spawned.size() == 1 && ForgeRegistries.ENTITY_TYPES.getKey(spawned.get(0).getType()).equals(identity),
                    "Native encounter must spawn the exact registered monster once: " + identity);
            helper.assertTrue(spawned.get(0).isPersistenceRequired() && spawned.get(0).getTarget() == player
                    && spawned.get(0).getPersistentData().getInt("FTDungeonSpawnTier") == 5 && level.noCollision(spawned.get(0)),
                    "Native encounter retains target, ownership, locked tier and safe position: " + identity);
            spawned.forEach(Entity::discard);
            DungeonSpawnPoints.tick(level, List.of(player));
            helper.assertTrue(owned(level, gate.roundId()).isEmpty(), "No refill after death: " + identity);
            System.out.println("NATIVE_ENCOUNTER_PASS: " + identity);
        }
        Point canceled = new Point(origin.offset(12, 1, 12), List.of(ResourceLocation.tryParse("minecraft:vindicator")));
        data.rooms.put(area.id(), new Room(area, List.of(canceled)));
        level.setBlock(canceled.position, FTDungeonMobs.DUNGEON_SPAWN_POINT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        Consumer<MobSpawnEvent.FinalizeSpawn> cancel = event -> {
            if (event.getEntity().getType() == EntityType.VINDICATOR) {
                event.setSpawnCancelled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            DungeonSpawnPoints.tick(level, List.of(player));
            helper.assertTrue(canceled.spawned == 0 && owned(level, gate.roundId()).isEmpty(), "FinalizeSpawn cancellation consumes nothing");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        DungeonSpawnPoints.tick(level, List.of(player));
        helper.assertTrue(canceled.spent() && owned(level, gate.roundId()).size() == 1, "Canceled native monster retries without reroll");
        owned(level, gate.roundId()).forEach(Entity::discard);
        System.out.println("DUNGEON_ROOM_RANKS_PASS: ordinary small rooms; elite medium/challenge rooms; no bosses/pets; legacy migration; optional mod; native spawning; finalize cancellation");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zzzz_adventure", timeoutTicks = 400)
    public static void adventureLifecycleAndProtection(GameTestHelper helper) {
        ServerLevel level = dungeon(helper);
        ServerLevel outside = level.getServer().overworld();
        for (GameType original : List.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE, GameType.SPECTATOR)) {
            FakePlayer player = FakePlayerFactory.get(outside, new GameProfile(UUID.randomUUID(), "FTModeTest"));
            player.setGameMode(original);
            player.setServerLevel(level);
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, DungeonGateTravel.DUNGEON));
            helper.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE, "All dungeon entrants must be Adventure: " + original);
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE, "Gamemode command cannot bypass dungeon rules");
            DungeonAdventureRules.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            helper.assertTrue(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DungeonAdventureRules.STATE_KEY)
                    .getInt("OriginalMode") == original.getId(), "Reconnect never overwrites original mode");
            FakePlayer clone = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "FTModeClone"));
            DungeonAdventureRules.onClone(new PlayerEvent.Clone(clone, player, true));
            DungeonAdventureRules.onRespawn(new PlayerEvent.PlayerRespawnEvent(clone, false));
            helper.assertTrue(clone.gameMode.getGameModeForPlayer() == GameType.ADVENTURE, "Dungeon death keeps Adventure");
            clone.setServerLevel(outside);
            DungeonAdventureRules.onRespawn(new PlayerEvent.PlayerRespawnEvent(clone, false));
            helper.assertTrue(clone.gameMode.getGameModeForPlayer() == original, "Death outside restores pre-entry mode: " + original);
            player.setServerLevel(outside);
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, DungeonGateTravel.DUNGEON, Level.OVERWORLD));
            helper.assertTrue(player.gameMode.getGameModeForPlayer() == original && !player.getPersistentData()
                    .getCompound(Player.PERSISTED_NBT_TAG).contains(DungeonAdventureRules.STATE_KEY), "Any exit restores and clears mode state: " + original);
        }
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "FTProtection"));
        DungeonAdventureRules.synchronize(player);
        BlockPos position = new BlockPos(2300, 64, 2300);
        level.setBlock(position, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        BlockEvent.BreakEvent breaking = new BlockEvent.BreakEvent(level, position, level.getBlockState(position), player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        net.minecraft.nbt.ListTag canDestroy = strings("minecraft:stone");
        player.getMainHandItem().getOrCreateTag().put("CanDestroy", canDestroy);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(breaking), "CanDestroy tools cannot bypass block protection");
        BlockEvent.EntityPlaceEvent placing = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, position),
                Blocks.STONE.defaultBlockState(), player);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(placing), "Placement is denied");
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), net.minecraft.core.Direction.UP, position, false);
        PlayerInteractEvent.RightClickBlock container = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, position, hit);
        MinecraftForge.EVENT_BUS.post(container);
        helper.assertTrue(!container.isCanceled() && container.getUseBlock() != Event.Result.DENY && container.getUseItem() == Event.Result.DENY,
                "Container, gate and altar block interactions remain enabled while item block edits are denied");
        BlockPos altarPos = new BlockPos(2300, 70, 2340);
        level.removeBlock(altarPos, false);
        BloodPoolCommands.blueprint(altarPos, net.minecraft.core.Direction.NORTH).forEach((cell, state) ->
                level.setBlock(cell, state, Block.UPDATE_CLIENTS));
        helper.assertTrue(BloodPoolStructure.validate(level, altarPos, net.minecraft.core.Direction.NORTH).valid(), "Use a complete native blood pool fixture");
        player.moveTo(Vec3.atBottomCenterOf(altarPos.north()));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SKELETON_SKULL, 2));
        BlockHitResult altarHit = new BlockHitResult(Vec3.atCenterOf(altarPos), net.minecraft.core.Direction.UP, altarPos, false);
        for (int offering = 0; offering < 2; offering++) {
            level.getBlockState(altarPos).use(level, player, InteractionHand.MAIN_HAND, altarHit);
        }
        BloodOfferingAltarEntity altar = (BloodOfferingAltarEntity) level.getBlockEntity(altarPos);
        helper.assertTrue(!player.mayBuild() && altar.charge() == BloodPoolRitual.REQUIRED && player.getMainHandItem().isEmpty(),
                "Adventure players can offer items to the real altar without construction permission");
        level.getBlockState(altarPos).use(level, player, InteractionHand.MAIN_HAND, altarHit);
        helper.assertTrue(level.getBlockState(altarPos).getValue(BloodOfferingAltarBlock.PHASE) == 2,
                "Adventure players can confirm the ritual with an empty hand");
        FillBucketEvent bucket = new FillBucketEvent(player, new ItemStack(Items.WATER_BUCKET), level, hit);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(bucket), "Fluid edits are denied");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND)), "Bucket use in air is denied");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND)), "Eating remains enabled");
        ArmorStand decoration = new ArmorStand(level, 2301, 65, 2301);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, decoration)), "Decorations cannot be destroyed");
        Mob monster = EntityType.VINDICATOR.create(level);
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, monster)), "Monster attacks remain enabled");
        EntityMobGriefingEvent griefing = new EntityMobGriefingEvent(monster);
        MinecraftForge.EVENT_BUS.post(griefing);
        helper.assertTrue(griefing.getResult() == Event.Result.DENY, "Monsters cannot grief dungeon buildings");
        Explosion explosion = new Explosion(level, monster, position.getX(), position.getY(), position.getZ(), 3, false, Explosion.BlockInteraction.DESTROY);
        explosion.getToBlow().add(position);
        List<Entity> damaged = new ArrayList<>(List.of(monster));
        ExplosionEvent.Detonate detonate = new ExplosionEvent.Detonate(level, explosion, damaged);
        MinecraftForge.EVENT_BUS.post(detonate);
        helper.assertTrue(explosion.getToBlow().isEmpty() && damaged.contains(monster), "Explosions retain combat damage, not block damage");
        FakePlayer ordinary = FakePlayerFactory.get(outside, new GameProfile(UUID.randomUUID(), "FTOutside"));
        BlockPos outsidePos = new BlockPos(2300, 64, 2300);
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(outside, outsidePos, Blocks.STONE.defaultBlockState(), ordinary)),
                "Other dimensions are unaffected");
        monster.discard();
        decoration.discard();
        System.out.println("DUNGEON_ADVENTURE_PASS: entry/exit all four modes; command denial; reconnect; death/clone; original-mode restoration; block/item/fluid protection; native altar offerings and activation; interactions/food/combat preserved; explosion and mob griefing protection; outside unaffected");
        helper.succeed();
    }

    private static ServerLevel dungeon(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
        helper.assertTrue(level != null && level.getServer() instanceof GameTestServer, "Only modify an isolated GameTest world");
        return level;
    }

    @GameTest(template = "empty", batch = "zzzzz_explosions", timeoutTicks = 800)
    public static void dimensionScopedBlastProtection(GameTestHelper helper) {
        ServerLevel protectedLevel = dungeon(helper);
        ServerLevel outside = protectedLevel.getServer().overworld();
        BlockPos origin = new BlockPos(3072, 64, 3072);
        List<net.minecraft.world.level.ChunkPos> forced = new ArrayList<>();
        for (int chunkX = (origin.getX() - 8) >> 4; chunkX <= (origin.getX() + 8) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - 8) >> 4; chunkZ <= (origin.getZ() + 8) >> 4; chunkZ++) {
                net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(chunkX, chunkZ);
                forced.add(chunk);
                protectedLevel.setChunkForced(chunkX, chunkZ, true);
                outside.setChunkForced(chunkX, chunkZ, true);
                protectedLevel.getChunk(chunkX, chunkZ);
                outside.getChunk(chunkX, chunkZ);
            }
        }
        helper.runAfterDelay(20, () -> {
            net.minecraft.world.level.GameRules rules = outside.getGameRules();
            boolean previousGriefing = rules.getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING);
            Difficulty previousDifficulty = protectedLevel.getServer().getWorldData().getDifficulty();
            Runnable cleanup = () -> {
                rules.getRule(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING).set(previousGriefing, outside.getServer());
                protectedLevel.getServer().setDifficulty(previousDifficulty, true);
                for (net.minecraft.world.level.ChunkPos chunk : forced) {
                    protectedLevel.setChunkForced(chunk.x, chunk.z, false);
                    outside.setChunkForced(chunk.x, chunk.z, false);
                }
            };
            try {
                protectedLevel.getServer().setDifficulty(Difficulty.NORMAL, true);
                rules.getRule(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING).set(true, outside.getServer());
                verifyNextBlast(helper, List.of(protectedLevel, outside), origin, 0, cleanup);
            } catch (RuntimeException | Error failure) {
                cleanup.run();
                throw failure;
            }
        });
    }

    private static void verifyNextBlast(GameTestHelper helper, List<ServerLevel> levels, BlockPos origin, int index, Runnable cleanup) {
        List<String> sources = List.of("creeper", "tnt", "fireball", "block");
        if (index == levels.size() * sources.size()) {
            try {
                helper.assertTrue(levels.get(1).getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING),
                        "Dungeon protection must not change the global mobGriefing gamerule");
                System.out.println("DUNGEON_BLAST_PROTECTION_PASS: real creeper/TNT/fireball/block-source explosions; "
                        + "dungeon blocks and chest inventory intact; no fire or block drops; combat damage preserved; "
                        + "outside explosions still destroy blocks; global gamerule unchanged");
                helper.succeed();
            } finally {
                cleanup.run();
            }
            return;
        }
        ServerLevel level = levels.get(index / sources.size());
        verifyBlast(helper, level, origin, sources.get(index % sources.size()), index < sources.size(),
                () -> verifyNextBlast(helper, levels, origin, index + 1, cleanup), cleanup);
    }

    private static void verifyBlast(GameTestHelper helper, ServerLevel level, BlockPos origin, String source, boolean protectedBlocks,
                                    Runnable next, Runnable cleanup) {
        net.minecraft.world.phys.AABB bounds = new net.minecraft.world.phys.AABB(origin.offset(-8, -1, -8), origin.offset(9, 9, 9));
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, bounds).forEach(Entity::discard);
        if (level.getBlockEntity(origin.offset(3, 1, 0)) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity previousChest) {
            previousChest.clearContent();
        }
        for (BlockPos position : BlockPos.betweenClosed(origin.offset(-7, 0, -7), origin.offset(7, 8, 7))) {
            level.setBlock(position, position.getY() == origin.getY() ? Blocks.GLASS.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos chestPos = origin.offset(3, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
        net.minecraft.world.level.block.entity.ChestBlockEntity chest =
                (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        Mob target = EntityType.RAVAGER.create(level);
        helper.assertTrue(target != null, "Native blast target must exist");
        target.moveTo(Vec3.atBottomCenterOf(origin.offset(0, 1, 3)));
        target.setNoAi(true);
        target.setPersistenceRequired();
        helper.assertTrue(level.addFreshEntity(target), "Native blast target must join the loaded fixture");
        float health = target.getHealth();
        helper.startSequence().thenWaitUntil(() ->
                helper.assertTrue(level.getEntitiesOfClass(Mob.class, bounds).contains(target),
                        "Native blast target must be visible to level entity queries: " + level.dimension().location()
                                + "; removed=" + target.getRemovalReason() + "; difficulty=" + level.getDifficulty()
                                + "; position=" + target.position() + "; box=" + target.getBoundingBox() + "; bounds=" + bounds
                                + "; lookup=" + level.getEntity(target.getId()) + "; chunk="
                                + level.getChunkAt(target.blockPosition()).getFullStatus()
                                + "; blockPos=" + target.blockPosition() + "; entityTicking=" + level.isPositionEntityTicking(target.blockPosition())))
                .thenExecute(() -> {
            Entity explosive = null;
            try {
                Vec3 center = Vec3.atCenterOf(origin.above(2));
                explosive = switch (source) {
                    case "creeper" -> EntityType.CREEPER.create(level);
                    case "tnt" -> new net.minecraft.world.entity.item.PrimedTnt(level, center.x, center.y, center.z, null);
                    case "fireball" -> new net.minecraft.world.entity.projectile.LargeFireball(level, EntityType.GHAST.create(level), 0, 0, 0, 4);
                    default -> null;
                };
                if (explosive != null) {
                    explosive.moveTo(center);
                }
                Level.ExplosionInteraction interaction = switch (source) {
                    case "creeper", "fireball" -> Level.ExplosionInteraction.MOB;
                    case "tnt" -> Level.ExplosionInteraction.TNT;
                    default -> Level.ExplosionInteraction.BLOCK;
                };
                Explosion blast = level.explode(explosive, center.x, center.y, center.z, 4, true, interaction);
                helper.assertTrue(target.getHealth() < health, "Block protection must retain real combat damage: " + source + "/" + level.dimension().location()
                        + "; health=" + target.getHealth() + "/" + health + "; exposure=" + Explosion.getSeenPercent(center, target));
                if (protectedBlocks) {
                    for (BlockPos position : BlockPos.betweenClosed(origin.offset(-7, 0, -7), origin.offset(7, 0, 7))) {
                        helper.assertTrue(level.getBlockState(position).is(Blocks.GLASS), "Dungeon floor survived: " + source + "/" + position);
                    }
                    helper.assertTrue(blast.getToBlow().isEmpty() && level.getBlockEntity(chestPos) == chest
                            && chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 3,
                            "Dungeon chest and inventory survived: " + source);
                    for (BlockPos position : BlockPos.betweenClosed(origin.offset(-7, 1, -7), origin.offset(7, 8, 7))) {
                        helper.assertTrue(!level.getBlockState(position).is(net.minecraft.tags.BlockTags.FIRE), "No explosion fire in dungeon: " + source);
                    }
                    helper.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, bounds).isEmpty(),
                            "Protected blocks create no explosion drops: " + source);
                } else {
                    boolean broken = false;
                    for (BlockPos position : BlockPos.betweenClosed(origin.offset(-7, 0, -7), origin.offset(7, 0, 7))) {
                        broken |= !level.getBlockState(position).is(Blocks.GLASS);
                    }
                    helper.assertTrue(broken, "Outside dimensions retain block destruction: " + source);
                }
            } catch (RuntimeException | Error failure) {
                cleanup.run();
                throw failure;
            } finally {
                target.discard();
                if (explosive != null) {
                    explosive.discard();
                }
            }
            next.run();
        });
    }

    private static net.minecraft.nbt.ListTag strings(String... values) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (String value : values) {
            list.add(net.minecraft.nbt.StringTag.valueOf(value));
        }
        return list;
    }

    private static List<Mob> owned(ServerLevel level, UUID round) {
        List<Mob> result = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob monster && !entity.isRemoved() && entity.getPersistentData().hasUUID(DungeonSpawnPoints.ROUND_TAG)
                    && entity.getPersistentData().getUUID(DungeonSpawnPoints.ROUND_TAG).equals(round)) {
                result.add(monster);
            }
        }
        return result;
    }
}
