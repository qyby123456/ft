package moss.generator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class Gameplay {
    public static final long ROOM_COOLDOWN_TICKS = 3600L;
    public static final int INSTANCE_CAP = 64;
    public static final int GLOBAL_CAP = 256;
    public static final int BOSS_ADDS = 4;
    private static final String INSTANCE_TAG = "ftmossgen.instance";
    private static final String ROLE_TAG = "ftmossgen.role";

    public static final class State {
        public int version = 1;
        public String instanceId;
        public String style;
        public String bossState = "READY";
        public String bossUuid;
        public int bossPiece;
        public int[] bossSpawn;
        public int[] bossEntry;
        public int[] bossReturn;
        public List<SpawnPoint> bossPoints = new ArrayList<>();
        public List<Room> rooms = new ArrayList<>();
        public Map<String, Intent> intents = new LinkedHashMap<>();
        public boolean rewardGranted;
        public String victorUuid;
    }

    public static final class Room {
        public int piece;
        public long nextWaveAt;
        public long wave;
        public List<SpawnPoint> points = new ArrayList<>();
        public List<String> active = new ArrayList<>();
    }

    public static final class SpawnPoint {
        public int[] rune;
        public int[] feet;
        public String kind;
    }

    public static final class Intent {
        public String uuid;
        public String role;
        public String type;
        public int piece;
        public int[] feet;
        public String status = "PREPARED";
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(MossMod.DIMENSION)) {
            return;
        }
        for (Engine.Job job : Store.get(level).jobs.values()) {
            if (!"COMPLETE".equals(job.stage) || job.gameplay == null) {
                continue;
            }
            validateState(job);
            materialize(level, job);
            updateGate(level, job);
            for (Room room : job.gameplay.rooms) {
                if (level.getGameTime() >= room.nextWaveAt && !waveOutstanding(job.gameplay, room)
                        && hasPlayer(level, piece(job.plan, room.piece))) {
                    startWave(level, job, room);
                }
            }

        }
    }

    public static boolean startWave(ServerLevel level, Engine.Job job, Room room) {
        requireDimension(level);
        validateState(job);
        if (!"COMPLETE".equals(job.stage) || !job.gameplay.rooms.contains(room)
                || level.getGameTime() < room.nextWaveAt || waveOutstanding(job.gameplay, room)) {
            return false;
        }
        int count = room.points.size();
        if (count == 0 || livingReservations(job.gameplay) + count > Profiles.get(job.plan.style).instanceCap - BOSS_ADDS - 1
                || globalReservations(level) + count > GLOBAL_CAP - BOSS_ADDS - 1) {
            return false;
        }
        for (SpawnPoint point : room.points) {
            if (!safeFeet(level, Catalog.pos(point.feet))) {
                return false;
            }
        }
        room.wave++;
        room.active.clear();
        for (int index = 0; index < count; index++) {
            SpawnPoint point = room.points.get(index);
            Intent intent = prepare(job.gameplay, "room/" + room.piece + "/" + room.wave + "/" + index,
                    "room", "ranged".equals(point.kind) ? Profiles.get(job.plan.style).ranged : Profiles.get(job.plan.style).melee, room.piece, point.feet);
            room.active.add(intent.uuid);
        }
        room.nextWaveAt = level.getGameTime() + Profiles.get(job.plan.style).roomCooldownTicks;
        Store.flush(level);
        materialize(level, job);
        return true;
    }

    public static boolean startBoss(ServerLevel level, Engine.Job job) {
        requireDimension(level);
        validateState(job);
        State state = job.gameplay;
        if (!"COMPLETE".equals(job.stage) || !"READY".equals(state.bossState)
                || livingReservations(state) + BOSS_ADDS + 1 > Profiles.get(job.plan.style).instanceCap
                || globalReservations(level) + BOSS_ADDS + 1 > GLOBAL_CAP) {
            return false;
        }
        if (!safeFeet(level, Catalog.pos(state.bossSpawn))) {
            return false;
        }
        for (int index = 0; index < BOSS_ADDS; index++) {
            if (!safeFeet(level, Catalog.pos(state.bossPoints.get(index).feet))) {
                return false;
            }
        }
        Intent boss = prepare(state, "boss", "boss", Profiles.get(job.plan.style).bossType, state.bossPiece, state.bossSpawn);
        state.bossUuid = boss.uuid;
        for (int index = 0; index < BOSS_ADDS; index++) {
            SpawnPoint point = state.bossPoints.get(index);
            prepare(state, "boss/add/" + index, "boss_add", "ranged".equals(point.kind) ? Profiles.get(job.plan.style).ranged : Profiles.get(job.plan.style).melee,
                    state.bossPiece, point.feet);
        }
        state.bossState = "FIGHTING";
        Store.flush(level);
        materialize(level, job);
        return true;
    }

    public static void death(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity().level() instanceof ServerLevel level)
                || !level.dimension().equals(MossMod.DIMENSION)) {
            return;
        }
        Entity dead = event.getEntity();
        Engine.Job job = Store.get(level).jobs.get(dead.getPersistentData().getString(INSTANCE_TAG));
        if (job == null || job.gameplay == null || !"COMPLETE".equals(job.stage)) {
            return;
        }
        validateState(job);
        State state = job.gameplay;
        Intent intent = state.intents.get(dead.getUUID().toString());
        if (intent == null || !matches(dead, state, intent) || "DEAD".equals(intent.status)) {
            return;
        }
        intent.status = "DEAD";
        if (!"boss".equals(intent.role) || !dead.getUUID().toString().equals(state.bossUuid)
                || !"FIGHTING".equals(state.bossState)) {
            Store.flush(level);
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !realPlayer(player)
                || player.serverLevel() != level || !entityType(intent.type).equals(dead.getType())) {
            state.bossState = "LOCKED";
            Store.flush(level);
            updateGate(level, job);
            return;
        }
        state.bossState = "VICTORY";
        state.victorUuid = player.getUUID().toString();
        boolean giveReward = !state.rewardGranted;
        state.rewardGranted = true;
        for (Intent reinforcement : state.intents.values()) {
            if ("boss_add".equals(reinforcement.role)) {
                reinforcement.status = "DEAD";
            }
        }
        Store.flush(level);
        updateGate(level, job);
        materialize(level, job);
        if (giveReward) {
            ItemStack reward = new ItemStack(Items.EMERALD, 24);
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
            player.sendSystemMessage(Component.literal(Profiles.get(job.plan.style).bossName + "已被击败，本实例归途石门正在开启。"));
        }
    }

    private static void materialize(ServerLevel level, Engine.Job job) {
        State state = job.gameplay;
        boolean changed = false;
        for (Intent intent : state.intents.values()) {
            UUID uuid = UUID.fromString(intent.uuid);
            Entity existing = level.getEntity(uuid);
            if ("DEAD".equals(intent.status)) {
                if (existing != null && existing.isAlive() && matches(existing, state, intent)) {
                    existing.discard();
                }
                continue;
            }
            if (existing != null) {
                if (!matches(existing, state, intent)) {
                    throw new IllegalStateException("Entity UUID ownership collision: " + uuid);
                }
                if (!"SPAWNED".equals(intent.status)) {
                    intent.status = "SPAWNED";
                    changed = true;
                }
                continue;
            }
            BlockPos feet = Catalog.pos(intent.feet);
            level.getChunk(feet.getX() >> 4, feet.getZ() >> 4);
            if (!level.areEntitiesLoaded(ChunkPos.asLong(feet.getX() >> 4, feet.getZ() >> 4))) {
                continue;
            }
            existing = level.getEntity(uuid);
            if (existing != null) {
                if (!matches(existing, state, intent)) {
                    throw new IllegalStateException("Entity UUID ownership collision: " + uuid);
                }
                intent.status = "SPAWNED";
                changed = true;
                continue;
            }
            if (!"PREPARED".equals(intent.status) || !safeFeet(level, feet)) {
                continue;
            }
            Mob mob = createMob(level, state, intent);
            if (!level.noCollision(mob, mob.getBoundingBox())
                    || !level.getEntities(mob, mob.getBoundingBox(), Entity::isAlive).isEmpty()) {
                continue;
            }
            intent.status = "ATTEMPTED";
            Store.flush(level);
            if (level.addFreshEntity(mob)) {
                intent.status = "SPAWNED";
            }
            changed = true;
        }
        if (changed) {
            Store.flush(level);
        }
    }

    private static Mob createMob(ServerLevel level, State state, Intent intent) {
        Profiles.Style profile = Profiles.get(state.style);
        Entity created = "merchant".equals(intent.role)
                ? net.farmingtales.ftdungeonmobs.ImportedCreatures.SKELETON_MERCHANT.get().create(level)
                : entityType(intent.type).create(level);
        if (!(created instanceof Mob)) {
            throw new IllegalStateException("Encounter entity must be a Mob: " + intent.type);
        }
        Mob mob = (Mob) created;
        if (mob == null) {
            throw new IllegalStateException("Entity factory failed: " + intent.type);
        }
        BlockPos feet = Catalog.pos(intent.feet);
        mob.setUUID(UUID.fromString(intent.uuid));
        mob.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 180.0F, 0.0F);
        mob.setPersistenceRequired();
        mob.addTag("ftmossgen." + intent.role);
        mob.addTag("ftmossgen.instance." + state.instanceId);
        mob.getPersistentData().putString(ROLE_TAG, intent.role);
        mob.getPersistentData().putString(INSTANCE_TAG, state.instanceId);
        if (mob instanceof Villager merchant) {
            merchant.setNoAi(true);
            merchant.setInvulnerable(true);
            merchant.setSilent(true);
            merchant.setCustomName(Component.literal("林间补给商"));
            merchant.setVillagerData(merchant.getVillagerData().setProfession(VillagerProfession.TOOLSMITH).setLevel(5));
            MerchantOffers offers = new MerchantOffers();
            offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.BREAD, 16), 999999, 0, 0.0F));
            offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.ARROW, 32), 999999, 0, 0.0F));
            offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.GOLDEN_CARROT, 8), 999999, 0, 0.0F));
            merchant.setOffers(offers);
        } else {
            if (mob instanceof AbstractSkeleton) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            if ("boss".equals(intent.role)) {
                mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(profile.bossHealth);
                mob.setHealth((float) profile.bossHealth);
                if (mob.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
                    throw new IllegalStateException("Boss Mob has no attack damage attribute: " + intent.type);
                }
                mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(profile.bossDamage);
                if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                    mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25);
                }
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                mob.setCustomName(Component.literal(profile.bossName));
                mob.setCustomNameVisible(true);
            }
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            mob.setCanPickUpLoot(false);
        }
        return mob;
    }

    public static EntityType<?> entityType(String id) {
        String qualified = id.indexOf(':') < 0 ? "minecraft:" + id : id;
        ResourceLocation key = ResourceLocation.tryParse(qualified);
        if (key == null || !ForgeRegistries.ENTITY_TYPES.containsKey(key)) {
            throw new IllegalStateException("Unknown encounter entity: " + id);
        }
        return ForgeRegistries.ENTITY_TYPES.getValue(key);
    }

    public static void validateProfile(ServerLevel level, Profiles.Style profile) {
        for (String type : List.of(profile.melee, profile.ranged, profile.bossType)) {
            Entity probe = entityType(type).create(level);
            if (!(probe instanceof Mob mob)) {
                throw new IllegalStateException("Configured encounter entity is not a Mob: " + type);
            }
            if (type.equals(profile.bossType) && (mob.getAttribute(Attributes.MAX_HEALTH) == null
                    || mob.getAttribute(Attributes.ATTACK_DAMAGE) == null)) {
                throw new IllegalStateException("Configured Boss needs health and attack damage attributes: " + type);
            }
        }
    }

    private static Intent prepare(State state, String key, String role, String type, int piece, int[] feet) {
        String uuid = UUID.nameUUIDFromBytes(("ftmossgen/" + state.instanceId + "/" + key)
                .getBytes(StandardCharsets.UTF_8)).toString();
        Intent existing = state.intents.get(uuid);
        if (existing != null) {
            return existing;
        }
        Intent intent = new Intent();
        intent.uuid = uuid;
        intent.role = role;
        intent.type = type;
        intent.piece = piece;
        intent.feet = feet.clone();
        state.intents.put(uuid, intent);
        return intent;
    }

    private static boolean matches(Entity entity, State state, Intent intent) {
        return state.instanceId.equals(entity.getPersistentData().getString(INSTANCE_TAG))
                && intent.role.equals(entity.getPersistentData().getString(ROLE_TAG))
                && entity.getUUID().toString().equals(intent.uuid);
    }

    public static List<BlockPos> gateCells(Planner.Plan plan) {
        Planner.Piece boss = bossPiece(plan);
        List<BlockPos> cells = new ArrayList<>();
        for (int height = 2; height <= 7; height++) {
            for (int width = 15; width <= 18; width++) {
                cells.add(local(boss, width, height, 22));
            }
        }
        return cells;
    }

    public static boolean safeFeet(ServerLevel level, BlockPos feet) {
        if (feet.getY() <= level.getMinBuildHeight() || feet.getY() + 2 >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(feet)) {
            return false;
        }
        level.getChunk(feet.getX() >> 4, feet.getZ() >> 4);
        return level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                && level.getBlockState(feet.above(2)).isAir() && level.getFluidState(feet).isEmpty()
                && level.getFluidState(feet.above()).isEmpty() && fullFloor(level, feet.below())
                && level.noCollision(new AABB(feet.getX() + 0.1, feet.getY(), feet.getZ() + 0.1,
                        feet.getX() + 0.9, feet.getY() + 2.4, feet.getZ() + 0.9));
    }

    private static boolean waveOutstanding(State state, Room room) {
        for (String uuid : room.active) {
            Intent intent = state.intents.get(uuid);
            if (intent == null || !"DEAD".equals(intent.status)) {
                return true;
            }
        }
        return false;
    }

    public static int livingReservations(State state) {
        int count = 0;
        for (Intent intent : state.intents.values()) {
            if (!"merchant".equals(intent.role) && !"DEAD".equals(intent.status)) {
                count++;
            }
        }
        return count;
    }

    private static int globalReservations(ServerLevel level) {
        int count = 0;
        for (Engine.Job job : Store.get(level).jobs.values()) {
            if (job.gameplay != null) {
                count += livingReservations(job.gameplay);
            }
        }
        return count;
    }

    private static void updateGate(ServerLevel level, Engine.Job job) {
        MossBossPortals.refresh(level, job);
        boolean victory = "VICTORY".equals(job.gameplay.bossState);
        for (BlockPos cell : gateCells(job.plan)) {
            if (victory) {
                if (!level.getBlockState(cell).isAir()) {
                    level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
                }
            } else if (!level.getBlockState(cell).equals(Profiles.state(Profiles.get(job.plan.style).barrierBlock))) {
                level.setBlock(cell, Profiles.state(Profiles.get(job.plan.style).barrierBlock), 3);
            }
        }
    }

    private static boolean fullFloor(ServerLevel level, BlockPos floor) {
        return level.getFluidState(floor).isEmpty()
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(floor).isCollisionShapeFullBlock(level, floor);
    }

    private static BlockPos findFloor(ServerLevel level, Planner.Piece piece, BlockPos desired, Set<BlockPos> reserved) {
        AABB bounds = Planner.bounds(piece);
        for (int distance = 0; distance <= 24; distance++) {
            for (int offsetX = -12; offsetX <= 12; offsetX++) {
                for (int offsetZ = -12; offsetZ <= 12; offsetZ++) {
                    if (Math.abs(offsetX) + Math.abs(offsetZ) != distance) {
                        continue;
                    }
                    BlockPos candidate = desired.offset(offsetX, 0, offsetZ);
                    if (bounds.contains(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5)
                            && bounds.contains(candidate.getX() + 0.5, candidate.getY() + 2.4, candidate.getZ() + 0.5)
                            && !reserved.contains(candidate) && safeFeet(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        throw new IllegalStateException("No independent safe floor near " + desired + " in piece " + piece.index);
    }

    private static boolean hasPlayer(ServerLevel level, Planner.Piece piece) {
        AABB bounds = Planner.bounds(piece);
        for (ServerPlayer player : level.players()) {
            if (realPlayer(player) && player.isAlive() && bounds.contains(player.position())) {
                return true;
            }
        }
        return false;
    }

    private static boolean realPlayer(ServerPlayer player) {
        return !(player instanceof FakePlayer) && !player.isSpectator();
    }

    private static Planner.Piece bossPiece(Planner.Plan plan) {
        for (Planner.Piece piece : plan.pieces) {
            if ("boss/eclipse_keeper".equals(piece.template)) {
                return piece;
            }
        }
        throw new IllegalStateException("Missing Boss piece for " + plan.id);
    }

    private static Planner.Piece piece(Planner.Plan plan, int index) {
        for (Planner.Piece piece : plan.pieces) {
            if (piece.index == index) {
                return piece;
            }
        }
        throw new IllegalStateException("Unknown piece " + index);
    }

    private static BlockPos local(Planner.Piece piece, int localX, int localY, int localZ) {
        return Planner.transform(new int[] {localX, localY, localZ}, piece.size, piece.rotation, piece.origin);
    }

    private static void validateState(Engine.Job job) {
        if (job.gameplay == null || job.gameplay.version != 1
                || !job.plan.id.equals(job.gameplay.instanceId)
                || !Profiles.get(job.plan.style).id.equals(job.gameplay.style)
                || job.gameplay.rooms == null || job.gameplay.intents == null
                || job.gameplay.bossPoints == null || job.gameplay.bossPoints.size() < BOSS_ADDS
                || !Set.of("READY", "FIGHTING", "LOCKED", "VICTORY").contains(job.gameplay.bossState)) {
            throw new IllegalStateException("Invalid gameplay ownership or schema for " + job.plan.id);
        }
    }

    private static void requireDimension(ServerLevel level) {
        if (!level.dimension().equals(MossMod.DIMENSION)) {
            throw new IllegalStateException("Gameplay only runs in the generator dimension");
        }
    }

    private Gameplay() {
    }

    public static void initialize(ServerLevel level, Engine.Job job) {
        requireDimension(level);
        if (!"INITIALIZING".equals(job.stage) && !"COMPLETE".equals(job.stage)) {
            throw new IllegalStateException("Gameplay requires completed geometry");
        }
        if (job.gameplay != null) {
            validateState(job);
            MossBossPortals.initialize(level, job);
            materialize(level, job);
            return;
        }
        State state = new State();
        state.instanceId = job.plan.id;
        state.style = Profiles.get(job.plan.style).id;
        validateProfile(level, Profiles.get(job.plan.style));
        Set<BlockPos> reserved = new HashSet<>();
        Planner.Piece boss = bossPiece(job.plan);
        state.bossPiece = boss.index;
        state.bossEntry = Catalog.vec(local(boss, 16, 2, 2));
        state.bossReturn = Catalog.vec(local(boss, 16, 2, 25));
        state.bossSpawn = Catalog.vec(findFloor(level, boss, local(boss, 16, 2, 13), reserved));
        reserved.add(Catalog.pos(state.bossSpawn));
        for (Planner.Piece piece : job.plan.pieces) {
            List<SpawnPoint> points = new ArrayList<>();
            for (Catalog.Socket socket : piece.sockets) {
                BlockPos rune = Catalog.pos(socket.position);
                BlockPos support = Catalog.pos(socket.support);
                if (!support.equals(rune.below()) || !fullFloor(level, support)
                        || !level.getBlockState(rune).is(MossMod.RUNE.get())) {
                    throw new IllegalStateException("Invalid supported rune at " + rune);
                }
                BlockPos feet = findFloor(level, piece, Catalog.pos(socket.spawn_position), reserved);
                SpawnPoint point = new SpawnPoint();
                point.rune = Catalog.vec(rune);
                point.feet = Catalog.vec(feet);
                point.kind = socket.kind;
                points.add(point);
                reserved.add(feet);
            }
            if (piece.index == boss.index) {
                state.bossPoints = points;
            } else if (!points.isEmpty()) {
                Room room = new Room();
                room.piece = piece.index;
                room.points = points;
                room.nextWaveAt = level.getGameTime();
                state.rooms.add(room);
            }
            if ("terminal/canteen".equals(piece.template)) {
                if (!points.isEmpty()) {
                    throw new IllegalStateException("A shop must not contain hostile sockets");
                }
                BlockPos feet = findFloor(level, piece, local(piece, 4, 2, 10), reserved);
                reserved.add(feet);
                prepare(state, "shop/" + piece.index, "merchant", "villager", piece.index, Catalog.vec(feet));
            }
        }
        if (state.bossPoints.size() < BOSS_ADDS || !safeFeet(level, Catalog.pos(state.bossEntry))) {
            throw new IllegalStateException("Unsafe Boss entry, return or reinforcements");
        }
        job.gameplay = state;
        Store.flush(level);
        MossBossPortals.initialize(level, job);
        updateGate(level, job);
        materialize(level, job);
    }
}
