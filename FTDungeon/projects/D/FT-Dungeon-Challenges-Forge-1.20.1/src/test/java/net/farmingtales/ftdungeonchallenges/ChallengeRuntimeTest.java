package net.farmingtales.ftdungeonchallenges;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftdungeonmobs.ArenaDifficultyApi;
import net.farmingtales.ftdungeonmobs.ArenaTorchBlock;
import net.farmingtales.ftdungeonmobs.DifficultyProfile;
import net.farmingtales.ftdungeonmobs.DungeonDifficultyApi;
import net.farmingtales.ftdungeonmobs.DungeonDifficultyConfig;
import net.farmingtales.ftdungeonmobs.DungeonSkeletonEntity;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.farmingtales.ftdungeonmobs.SkeletonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(FTDungeonChallenges.ID)
@PrefixGameTestTemplate(false)
public final class ChallengeRuntimeTest {
    private static final Map<UUID, ArenaChallengeApi.EndReason> RESULTS = new HashMap<>();
    private static int assertions;

    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void challengeLifecycle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        ArenaChallengeConfig.Settings original = ArenaChallengeConfig.settings();
        for (int tier = 1; tier <= 5; tier++) {
            check(helper, original.tier(tier).waves() == tier + 2, "default wave gradient tier " + tier);
            check(helper, original.tier(tier).baseMobs() == tier + 3, "default initial enemy gradient tier " + tier);
            check(helper, original.tier(tier).finalBoss() == (tier > 1), "default boss threshold tier " + tier);
            check(helper, original.tier(tier).mobCount(20, 16, original) <= original.maxMobs(), "hard wave cap tier " + tier);
        }
        configure(fastSettings());
        clearTorchCooldowns(level);
        MinecraftForge.EVENT_BUS.addListener((ArenaChallengeEvent.Finished event) -> RESULTS.put(event.challenge().id(), event.reason()));
        BlockPos base = helper.absolutePos(new BlockPos(2, 3, 2));
        for (int offsetX = -24; offsetX <= 24; offsetX++) {
            for (int offsetZ = -24; offsetZ <= 24; offsetZ++) {
                level.setBlock(base.offset(offsetX, -1, offsetZ), Blocks.STONE.defaultBlockState(), 3);
                for (int height = 0; height < 5; height++) {
                    level.setBlock(base.offset(offsetX, height, offsetZ), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        place(level, base, 4);
        ServerPlayer owner = player(helper, base);
        ServerPlayer teammate = player(helper, base);
        ServerPlayer late = player(helper, base);
        click(owner, base, false, InteractionHand.MAIN_HAND);
        check(helper, level.getBlockState(base).getValue(ArenaTorchBlock.TIER) == 5, "normal right click still cycles difficulty");
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        click(owner, base, true, InteractionHand.MAIN_HAND);
        check(helper, ArenaChallengeApi.find(level, base).isEmpty(), "held-item sneak click cannot accidentally start");
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        click(owner, base, true, InteractionHand.OFF_HAND);
        check(helper, ArenaChallengeApi.find(level, base).isEmpty(), "offhand cannot double-start");
        click(owner, base.above(), true, InteractionHand.MAIN_HAND);
        ArenaChallengeApi.Snapshot initial = ArenaChallengeApi.find(level, base).orElseThrow();
        check(helper, initial.tier() == 5 && initial.wave() == 0 && initial.totalWaves() == 2, "upper-half interaction starts tier-five countdown");
        check(helper, ArenaChallengeApi.join(teammate, base).participants().size() == 2, "teammate joins despite torch reservation cooldown");
        check(helper, ArenaChallengeApi.start(owner, base).id().equals(initial.id()), "repeated start is idempotent");
        if (!late.hasPermissions(2)) { expectError(helper, () -> ArenaChallengeApi.stop(late, base), "not_owner"); }
        expectError(helper, () -> ArenaChallengeApi.join(late, base), "full");
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ArenaChallengeCommands.register(dispatcher);
        try {
            check(helper, dispatcher.execute("ftchallenge status " + base.getX() + " " + base.getY() + " " + base.getZ(),
                    owner.createCommandSourceStack().withPermission(0)) == 1, "non-operators can inspect their challenge");
            try {
                dispatcher.execute("ftchallenge reload", owner.createCommandSourceStack().withPermission(0));
                throw new IllegalStateException("Non-operator accessed configuration reload");
            } catch (CommandSyntaxException expected) { check(helper, true, "administrative command requires permission two"); }
        } catch (CommandSyntaxException exception) { throw new IllegalStateException(exception); }
        AtomicBoolean threadRejected = new AtomicBoolean();
        Thread background = new Thread(() -> {
            try { ArenaChallengeApi.active(level.getServer()); }
            catch (IllegalStateException expected) { threadRejected.set(true); }
        });
        background.start();
        try { background.join(1000); }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new IllegalStateException(exception); }
        check(helper, threadRejected.get(), "public API rejects off-thread world access");
        click(owner, base, false, InteractionHand.MAIN_HAND);
        check(helper, level.getBlockState(base).getValue(ArenaTorchBlock.TIER) == 5, "normal clicks are locked during challenge");
        BlockPos other = base.offset(6, 0, 0);
        place(level, other, 2);
        expectError(helper, () -> ArenaChallengeApi.start(late, other), "overlap");
        level.destroyBlock(other, false);
        ArenaChallengeRun run = run(initial);
        check(helper, run.enemies.isEmpty(), "preparation does not spawn immediately");
        helper.runAtTickTime(55, () -> {
            try {
                check(helper, run.wave == 1 && !run.enemies.isEmpty(), "real server tick advances countdown and spawns first wave");
                expectError(helper, () -> ArenaChallengeApi.join(late, base), "join_closed");
                check(helper, run.enemies.size() + run.snapshot().pendingSpawns() == 3, "team size scales enemy count once");
                run.tick();
                inspectEnemies(helper, run);
                killAll(run);
                run.tick();
                check(helper, run.snapshot().phase() == ArenaChallengeApi.Phase.INTERMISSION, "all confirmed kills start intermission");
                expire(run, "phaseUntil");
                run.tick();
                run.tick();
                check(helper, run.wave == 2 && run.enemies.size() == 4, "later wave adds configured enemies");
                check(helper, run.enemies.stream().map(level::getEntity).anyMatch(entity -> entity instanceof DungeonSkeletonEntity skeleton
                        && skeleton.variant() == SkeletonVariant.KING), "last wave guarantees exactly one king slot");
                int ownerExperience = owner.totalExperience;
                int teammateExperience = teammate.totalExperience;
                killAll(run);
                run.tick();
                check(helper, RESULTS.get(run.id) == ArenaChallengeApi.EndReason.VICTORY, "last wave grants victory");
                check(helper, owner.totalExperience == ownerExperience + 35 && teammate.totalExperience == teammateExperience + 35,
                        "eligible participants receive frozen-tier experience once");
                check(helper, owner.getInventory().countItem(Items.DIAMOND) >= 2 && owner.getInventory().countItem(Items.EMERALD) >= 8,
                        "real tier-five gift loot table resolves and rewards inventory");
                run.tick();
                check(helper, owner.totalExperience == ownerExperience + 35, "completed run cannot pay twice");
                expectError(helper, () -> ArenaChallengeApi.start(owner, base), "cooldown");
                place(level, other, 2);
                expectError(helper, () -> ArenaChallengeApi.start(owner, other), "cooldown");
                level.destroyBlock(other, false);
                CompoundTag saved = ArenaChallengeData.get(level).save(new CompoundTag());
                check(helper, ArenaChallengeData.load(saved).remaining(level, base, owner.getUUID()) > 0, "player and torch cooldown survive NBT roundtrip");
                failureCases(helper, base);
                configure(original);
                System.out.println("FT_CHALLENGE_RUNTIME_VERIFIED assertions=" + assertions);
                helper.succeed();
            } catch (Throwable failure) {
                configure(original);
                failure.printStackTrace();
                throw failure;
            }
        });
    }

    private static void inspectEnemies(GameTestHelper helper, ArenaChallengeRun run) {
        for (UUID identifier : run.enemies) {
            DungeonSkeletonEntity skeleton = (DungeonSkeletonEntity) run.level.getEntity(identifier);
            var inspected = DungeonDifficultyApi.inspect(skeleton);
            DifficultyProfile profile = DungeonDifficultyConfig.settings().profile(skeleton.variant(), 5);
            check(helper, inspected.tier() == 5 && !inspected.automatic(), "challenge tier is pinned through the existing difficulty API");
            near(helper, skeleton.getMaxHealth(), skeleton.variant().health() * profile.healthMultiplier(), "existing maximum-health tier scaling");
            near(helper, skeleton.getAttributeValue(Attributes.ATTACK_DAMAGE), skeleton.variant().damage() * profile.attackMultiplier(),
                    "native attack including weapon modifier is multiplied once");
            near(helper, skeleton.getAttributeValue(Attributes.ARMOR), (skeleton.variant() == SkeletonVariant.KING ? 6 : 2) + profile.armorBonus(),
                    "existing physical armor tier scaling");
            near(helper, skeleton.getAttributeValue(AttributeRegistry.SPELL_RESIST.get()), 1 + profile.magicArmorBonus(), "native Iron spell resistance");
            near(helper, skeleton.getAttributeValue(AttributeRegistry.SPELL_POWER.get()), profile.spellPowerMultiplier(), "native Iron spell power");
            skeleton.setHealth(skeleton.getMaxHealth() / 2);
            DungeonDifficultyApi.refresh(skeleton);
            DungeonDifficultyApi.refresh(skeleton);
            near(helper, skeleton.getHealth() / skeleton.getMaxHealth(), 0.5, "refresh neither heals nor stacks tier modifiers");
        }
    }

    private static void failureCases(GameTestHelper helper, BlockPos base) {
        ServerLevel level = helper.getLevel();
        ArenaChallengeRun missing = startWave(helper, base);
        Entity disappeared = level.getEntity(missing.enemies.iterator().next());
        disappeared.discard();
        missing.tick();
        ended(helper, missing, ArenaChallengeApi.EndReason.ENEMY_LOST);

        ArenaChallengeRun cancelled = startWave(helper, base);
        ArenaChallengeApi.cancel(level, base.above());
        ended(helper, cancelled, ArenaChallengeApi.EndReason.CANCELLED);

        ArenaChallengeRun cancelledDeath = startWave(helper, base);
        DungeonSkeletonEntity living = (DungeonSkeletonEntity) level.getEntity(cancelledDeath.enemies.iterator().next());
        LivingDeathEvent cancelledEvent = new LivingDeathEvent(living, level.damageSources().genericKill());
        ArenaChallengeApi.onDeath(cancelledEvent);
        cancelledEvent.setCanceled(true);
        cancelledDeath.tick();
        check(helper, cancelledDeath.enemies.contains(living.getUUID()) && ArenaChallengeApi.find(level, base).isPresent(),
                "late-cancelled death event cannot clear a living enemy");
        ArenaChallengeApi.cancel(level, base);
        ended(helper, cancelledDeath, ArenaChallengeApi.EndReason.CANCELLED);

        ArenaChallengeRun timedOut = startWave(helper, base);
        expire(timedOut, "phaseUntil");
        timedOut.tick();
        ended(helper, timedOut, ArenaChallengeApi.EndReason.WAVE_TIMEOUT);

        ArenaChallengeRun changed = startWave(helper, base);
        ArenaDifficultyApi.setTier(level, base, 1);
        changed.tick();
        ended(helper, changed, ArenaChallengeApi.EndReason.TORCH_REMOVED);

        ArenaChallengeRun removedTorch = startWave(helper, base);
        level.destroyBlock(base.above(), false);
        removedTorch.tick();
        ended(helper, removedTorch, ArenaChallengeApi.EndReason.TORCH_REMOVED);
        place(level, base, 5);

        ArenaChallengeRun escaped = startWave(helper, base);
        level.getEntity(escaped.enemies.iterator().next()).moveTo(base.getX() + escaped.radius + 12, base.getY(), base.getZ(), 0, 0);
        escaped.tick();
        ended(helper, escaped, ArenaChallengeApi.EndReason.OUT_OF_BOUNDS);

        ArenaChallengeRun disabled = startWave(helper, base);
        ArenaChallengeConfig.Settings fast = fastSettings();
        configure(new ArenaChallengeConfig.Settings(false, 1, 1, 30, 1, 1, 0, 3, 2, 8, 1, 1, 4, 7, fast.tiers()));
        disabled.tick();
        ended(helper, disabled, ArenaChallengeApi.EndReason.DISABLED);

        ArenaChallengeRun absent = startWave(helper, base);
        ServerPlayer absentPlayer = level.getServer().getPlayerList().getPlayer(absent.owner);
        absentPlayer.moveTo(base.getX() + 80, base.getY(), base.getZ(), 0, 0);
        absent.tick();
        set(absent, "absentSince", level.getGameTime() - 40);
        absent.tick();
        ended(helper, absent, ArenaChallengeApi.EndReason.ABSENT);

        ArenaChallengeRun wiped = startWave(helper, base);
        ServerPlayer victim = level.getServer().getPlayerList().getPlayer(wiped.owner);
        victim.setHealth(0);
        victim.die(level.damageSources().genericKill());
        wiped.tick();
        ended(helper, wiped, ArenaChallengeApi.EndReason.DEFEAT);

        ArenaChallengeRun blocked = fresh(helper, base);
        expire(blocked, "phaseUntil");
        double borderSize = level.getWorldBorder().getSize();
        double borderX = level.getWorldBorder().getCenterX();
        double borderZ = level.getWorldBorder().getCenterZ();
        level.getWorldBorder().setCenter(base.getX() + 0.5, base.getZ() + 0.5);
        level.getWorldBorder().setSize(2);
        try {
            blocked.tick();
            check(helper, blocked.enemies.isEmpty() && blocked.snapshot().pendingSpawns() > 0, "blocked spawns remain pending instead of counting as kills");
            expire(blocked, "spawnUntil");
            blocked.tick();
            ended(helper, blocked, ArenaChallengeApi.EndReason.SPAWN_BLOCKED);
        } finally {
            level.getWorldBorder().setCenter(borderX, borderZ);
            level.getWorldBorder().setSize(borderSize);
        }

        DungeonSkeletonEntity orphan = FTDungeonMobs.WARRIOR.get().create(level);
        orphan.moveTo(Vec3.atCenterOf(base));
        orphan.getPersistentData().putUUID(ArenaChallengeApi.ENEMY_TAG, UUID.randomUUID());
        level.addFreshEntity(orphan);
        check(helper, orphan.isRemoved(), "orphaned challenge mobs are discarded on load after restart");
        DungeonSkeletonEntity ordinary = FTDungeonMobs.WARRIOR.get().create(level);
        ordinary.moveTo(Vec3.atCenterOf(base));
        level.addFreshEntity(ordinary);
        check(helper, !ordinary.isRemoved(), "unrelated FT mobs are not cleaned up");
        ordinary.discard();

        ArenaChallengeRun stopping = startWave(helper, base);
        ArenaChallengeApi.onStopping(new ServerStoppingEvent(level.getServer()));
        ended(helper, stopping, ArenaChallengeApi.EndReason.SERVER_STOPPED);
        check(helper, ArenaChallengeApi.active(level.getServer()).isEmpty(), "shutdown leaves no active sessions");
        level.getServer().setDifficulty(Difficulty.PEACEFUL, true);
        ServerPlayer peaceful = player(helper, base);
        expectError(helper, () -> ArenaChallengeApi.start(peaceful, base), "peaceful");
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
    }

    private static ArenaChallengeRun fresh(GameTestHelper helper, BlockPos base) {
        ArenaChallengeConfig.Settings settings = fastSettings();
        configure(new ArenaChallengeConfig.Settings(true, 1, 1, 30, 1, 1, 0, 3, 2, 8, 1, 1, 4, 7, settings.tiers()));
        clearTorchCooldowns(helper.getLevel());
        ArenaDifficultyApi.setTier(helper.getLevel(), base, 5);
        return run(ArenaChallengeApi.start(player(helper, base), base));
    }

    private static void clearTorchCooldowns(ServerLevel level) {
        ArenaChallengeData data = ArenaChallengeData.get(level);
        try {
            Field locations = ArenaChallengeData.class.getDeclaredField("locations");
            locations.setAccessible(true);
            ((Map<?, ?>) locations.get(data)).clear();
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    private static ArenaChallengeRun startWave(GameTestHelper helper, BlockPos base) {
        ArenaChallengeRun run = fresh(helper, base);
        expire(run, "phaseUntil");
        run.tick();
        run.tick();
        check(helper, !run.enemies.isEmpty(), "failure scenario spawns real enemies");
        return run;
    }

    private static void ended(GameTestHelper helper, ArenaChallengeRun run, ArenaChallengeApi.EndReason expected) {
        check(helper, RESULTS.get(run.id) == expected, "expected end reason " + expected);
        check(helper, ArenaChallengeApi.find(run.level, run.base).isEmpty() && run.enemies.isEmpty(), "failure cleans session and enemy tracking");
        ServerPlayer owner = run.level.getServer().getPlayerList().getPlayer(run.owner);
        check(helper, owner.totalExperience == 0 && owner.getInventory().countItem(Items.DIAMOND) == 0, "failure cannot grant victory rewards");
    }

    private static ArenaChallengeConfig.Settings fastSettings() {
        List<ArenaChallengeConfig.Tier> tiers = new ArrayList<>();
        for (int tier = 1; tier <= 5; tier++) { tiers.add(new ArenaChallengeConfig.Tier(2, 2, 4, 2, tier - 1, tier > 1)); }
        return new ArenaChallengeConfig.Settings(true, 1, 1, 30, 1, 1, 120, 3, 2, 8, 1, 1, 4, 7, List.copyOf(tiers));
    }

    private static void configure(ArenaChallengeConfig.Settings settings) {
        try {
            Field field = ArenaChallengeConfig.class.getDeclaredField("settings");
            field.setAccessible(true);
            field.set(null, settings);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    @SuppressWarnings("unchecked")
    private static ArenaChallengeRun run(ArenaChallengeApi.Snapshot snapshot) {
        try {
            Field field = ArenaChallengeApi.class.getDeclaredField("ACTIVE");
            field.setAccessible(true);
            return ((Map<UUID, ArenaChallengeRun>) field.get(null)).get(snapshot.id());
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    private static void expire(ArenaChallengeRun run, String field) { set(run, field, run.level.getGameTime() - 1); }

    private static void set(ArenaChallengeRun run, String field, long value) {
        try {
            Field target = ArenaChallengeRun.class.getDeclaredField(field);
            target.setAccessible(true);
            target.setLong(run, value);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    @SuppressWarnings("unchecked")
    private static ServerPlayer player(GameTestHelper helper, BlockPos base) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "FT-probe"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override
            public void send(Packet<?> packet) {}
            @Override
            public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        try {
            Field field = PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            ((Map<UUID, ServerPlayer>) field.get(level.getServer().getPlayerList())).put(player.getUUID(), player);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
        player.moveTo(base.getX() + 1.5, base.getY(), base.getZ() + 1.5, 0, 0);
        player.getAbilities().invulnerable = true;
        return player;
    }

    private static void place(ServerLevel level, BlockPos base, int tier) {
        var block = FTDungeonMobs.ARENA_TORCH.get();
        level.setBlock(base, block.defaultBlockState().setValue(ArenaTorchBlock.HALF, DoubleBlockHalf.LOWER).setValue(ArenaTorchBlock.TIER, tier), 3);
        level.setBlock(base.above(), block.defaultBlockState().setValue(ArenaTorchBlock.HALF, DoubleBlockHalf.UPPER).setValue(ArenaTorchBlock.TIER, tier), 3);
    }

    private static void click(ServerPlayer player, BlockPos position, boolean sneaking, InteractionHand hand) {
        player.setShiftKeyDown(sneaking);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.SOUTH, position, false);
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, position, hit);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled() && !sneaking) {
            player.level().getBlockState(position).use(player.level(), player, hand, hit);
        }
        player.setShiftKeyDown(false);
    }

    private static void killAll(ArenaChallengeRun run) {
        for (UUID enemy : new ArrayList<>(run.enemies)) { run.level.getEntity(enemy).kill(); }
    }

    private static void expectError(GameTestHelper helper, Runnable action, String key) {
        try { action.run(); throw new IllegalStateException("Expected challenge error " + key); }
        catch (ArenaChallengeApi.ChallengeException expected) {
            check(helper, expected.component().getContents().toString().contains("error." + key), "expected user-facing error " + key);
        }
    }

    private static void near(GameTestHelper helper, double actual, double expected, String message) {
        check(helper, Math.abs(actual - expected) < 0.001, message + ": " + actual + " vs " + expected);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        assertions++;
        helper.assertTrue(condition, message);
    }
}
