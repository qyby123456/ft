package moss.generator;

import com.google.gson.Gson;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerators;

@GameTestHolder("ftstylesqa")
@PrefixGameTestTemplate(false)
public final class StylePlannerGameTests {
    static Map<Integer, Integer> distances(GameTestHelper helper, Planner.Plan plan) {
        Map<BlockPos, Integer> owners = new HashMap<>();
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        for (var piece : plan.pieces) {
            for (var port : piece.ports) {
                for (int[] cell : port.cells) {
                    helper.assertTrue(owners.put(Catalog.pos(cell), piece.index) == null, "Port cells have unique owners");
                }
            }
        }
        for (var entry : owners.entrySet()) {
            for (BlockPos adjacent : List.of(entry.getKey().north(), entry.getKey().south(), entry.getKey().east(), entry.getKey().west())) {
                Integer neighbor = owners.get(adjacent);
                if (neighbor != null && !neighbor.equals(entry.getValue())) {
                    graph.computeIfAbsent(entry.getValue(), ignored -> new HashSet<>()).add(neighbor);
                }
            }
        }
        Map<Integer, Integer> distance = new HashMap<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int entrance = plan.pieces.stream().filter(piece -> "spawn/spawn".equals(piece.template)).findFirst().orElseThrow().index;
        queue.add(entrance);
        distance.put(entrance, 0);
        while (!queue.isEmpty()) {
            int current = queue.remove();
            for (int neighbor : graph.getOrDefault(current, Set.of())) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, distance.get(current) + 1);
                    queue.add(neighbor);
                }
            }
        }
        long playable = plan.pieces.stream().filter(piece -> !piece.ports.isEmpty()).count();
        helper.assertTrue(distance.size() == playable, "Every playable room is reachable using actual adjacent world port cells");
        int edges = graph.values().stream().mapToInt(Set::size).sum() / 2;
        helper.assertTrue(edges == plan.edges && edges == playable - 1, "Independent port graph agrees with connected tree edge count");
        Set<String> expectedEdges = new HashSet<>();
        graph.forEach((source, targets) -> targets.forEach(target -> expectedEdges.add(Math.min(source, target) + ":" + Math.max(source, target))));
        Set<String> persistedEdges = new HashSet<>();
        for (int[] edge : plan.graphEdges) {
            helper.assertTrue(edge.length == 2, "Persisted graph edge has two owners");
            helper.assertTrue(persistedEdges.add(Math.min(edge[0], edge[1]) + ":" + Math.max(edge[0], edge[1])), "No duplicate persisted graph edge");
        }
        helper.assertTrue(expectedEdges.equals(persistedEdges), "Frozen graphEdges match independent port adjacency, not trusted metadata");
        return distance;
    }

    private record WalkSurface(Set<BlockPos> feet, Set<BlockPos> obstacles) {}
    private static final Map<String, WalkSurface> SURFACES = new HashMap<>();

    private static final class TemplateView implements BlockGetter {
        final Map<BlockPos, BlockState> states = new HashMap<>();
        final int height;

        TemplateView(String style, Catalog.Template template) {
            height = template.size[1];
            for (var cell : Catalog.blocks(style, template.key)) states.put(cell.position(), cell.state());
        }

        @Override public BlockEntity getBlockEntity(BlockPos position) { return null; }
        @Override public BlockState getBlockState(BlockPos position) { return states.getOrDefault(position, Blocks.AIR.defaultBlockState()); }
        @Override public FluidState getFluidState(BlockPos position) { return getBlockState(position).getFluidState(); }
        @Override public int getHeight() { return height; }
        @Override public int getMinBuildHeight() { return 0; }

        boolean clear(BlockPos position) {
            return getFluidState(position).isEmpty() && getBlockState(position).getCollisionShape(this, position).isEmpty();
        }
    }

    private static WalkSurface surface(String style, Catalog.Template template) {
        return SURFACES.computeIfAbsent(style + ":" + template.key, ignored -> {
            TemplateView view = new TemplateView(style, template);
            Set<BlockPos> feet = new HashSet<>();
            Set<BlockPos> obstacles = new HashSet<>();
            for (var entry : view.states.entrySet()) {
                BlockPos local = entry.getKey();
                if (!view.clear(local)) obstacles.add(local);
                if (local.getY() > template.minimum_y && local.getY() + 1 < template.size[1]
                    && view.clear(local) && view.clear(local.above()) && view.getFluidState(local.below()).isEmpty()
                    && view.getBlockState(local.below()).isCollisionShapeFullBlock(view, local.below())) {
                    feet.add(local);
                }
            }
            return new WalkSurface(Set.copyOf(feet), Set.copyOf(obstacles));
        });
    }

    static void portalChoice(GameTestHelper helper, Planner.Plan plan) {
        distances(helper, plan);
        Set<BlockPos> feet = new HashSet<>();
        Set<BlockPos> obstacles = new HashSet<>();
        Map<BlockPos, Integer> owners = new HashMap<>();
        Map<Integer, BlockPos> eligible = new HashMap<>();
        for (var piece : plan.pieces) {
            if (piece.ports.isEmpty()) continue;
            Catalog.Template template = Catalog.get(plan.style).templates.get(piece.key);
            WalkSurface local = surface(plan.style, template);
            for (BlockPos point : local.feet()) {
                BlockPos world = Planner.transform(Catalog.vec(point), piece.size, piece.rotation, piece.origin);
                helper.assertTrue(owners.put(world, piece.index) == null, "Native walking surfaces have unique room ownership");
                feet.add(world);
            }
            for (BlockPos point : local.obstacles()) obstacles.add(Planner.transform(Catalog.vec(point), piece.size, piece.rotation, piece.origin));
            if (template.portal != null) {
                helper.assertTrue(piece.template.startsWith("straight/room") || piece.template.startsWith("turn/")
                    || piece.template.startsWith("terminal/") || piece.template.equals("promenade"), "Eligible portal metadata belongs to a room, never a corridor/scenery/stair");
                helper.assertTrue(template.portal_trigger != null, "Eligible room has a declared native walking trigger");
                BlockPos trigger = Planner.transform(template.portal_trigger, piece.size, piece.rotation, piece.origin);
                helper.assertTrue(trigger.equals(Catalog.pos(piece.portalTrigger)), "Piece trigger matches actual rotated final template metadata");
                helper.assertTrue(java.util.Arrays.equals(piece.portal, Catalog.vec(Planner.transform(template.portal, piece.size, piece.rotation, piece.origin))),
                    "Eligible room center-anchor matches actual rotated final template metadata");
                Direction nativeFacing = Direction.from2DDataValue(template.portal_facing);
                for (int turn = 0; turn < piece.rotation; turn++) nativeFacing = nativeFacing.getClockWise();
                helper.assertTrue(piece.portalFacing == nativeFacing.get2DDataValue(), "Eligible portal facing matches native clockwise template rotation");
                eligible.put(piece.index, trigger);
            }
        }
        for (int[] cap : plan.caps) {
            BlockPos bottom = Catalog.pos(cap);
            for (int rise = 0; rise < 5; rise++) {
                obstacles.add(bottom.above(rise));
                feet.remove(bottom.above(rise));
                feet.remove(bottom.above(rise).below());
            }
        }
        BlockPos entry = Catalog.pos(plan.entry);
        helper.assertTrue(feet.contains(entry), "Actual entry has collision-free feet/head and full support");
        Map<BlockPos, Integer> walking = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        walking.put(entry, 0);
        queue.add(entry);
        Set<Integer> reached = new HashSet<>();
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            reached.add(owners.get(current));
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos horizontal = current.relative(facing);
                for (int elevation = -1; elevation <= 1; elevation++) {
                    BlockPos target = horizontal.offset(0, elevation, 0);
                    if (!feet.contains(target) || walking.containsKey(target)) continue;
                    if (elevation > 0 && obstacles.contains(current.above(2))) continue;
                    if (elevation < 0 && obstacles.contains(target.above(2))) continue;
                    walking.put(target, walking.get(current) + 1);
                    queue.add(target);
                }
            }
        }
        for (var piece : plan.pieces) if (!piece.ports.isEmpty()) helper.assertTrue(reached.contains(piece.index), "Native block-step walk graph reaches every playable room: " + piece.index);
        Map<Integer, Integer> independentDistances = new java.util.TreeMap<>();
        eligible.forEach((index, trigger) -> {
            helper.assertTrue(walking.containsKey(trigger), "Eligible non-corridor trigger is actually reachable: " + index);
            independentDistances.put(index, walking.get(trigger));
        });
        helper.assertTrue(independentDistances.equals(plan.roomDistances), "roomDistances equal independently measured native collision/block-step BFS shortest paths");
        int lowest = eligible.values().stream().mapToInt(BlockPos::getY).min().orElseThrow();
        int farthest = eligible.entrySet().stream().filter(candidate -> candidate.getValue().getY() == lowest)
            .mapToInt(candidate -> independentDistances.get(candidate.getKey())).max().orElseThrow();
        int selected = eligible.entrySet().stream().filter(candidate -> candidate.getValue().getY() == lowest
            && independentDistances.get(candidate.getKey()) == farthest).mapToInt(Map.Entry::getKey).min().orElseThrow();
        var room = plan.pieces.stream().filter(piece -> piece.index == selected).findFirst().orElseThrow();
        helper.assertTrue(plan.bossPortalPiece == selected && plan.portalDistance == farthest,
            "Boss door is the eligible lowest-floor room with farthest native walking trigger, with stable index tie-break;seed=" + plan.seed + ";expectedRoom=" + selected + ";expectedSteps=" + farthest);
        helper.assertTrue(java.util.Arrays.equals(plan.bossPortal, room.portal) && java.util.Arrays.equals(plan.exit, room.portalTrigger), "Frozen Boss anchor and trigger reference selected eligible room");
        helper.assertTrue(plan.bossPortalFacing == room.portalFacing, "Frozen Boss gate facing belongs to selected eligible room");
        helper.assertTrue(plan.bossPortal != null && plan.bossPortal.length == 3 && plan.bossPortalFacing >= 0 && plan.bossPortalFacing < 4,
            "Boss portal has a frozen world anchor and legal horizontal facing");
        var box = Planner.bounds(room);
        BlockPos anchor = Catalog.pos(plan.bossPortal);
        helper.assertTrue(box.contains(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5), "Door belongs to selected farthest room");
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void sameSeedStylesAndNativeWalkingGeometry(GameTestHelper helper) {
        Gson gson = new Gson();
        Set<String> ids = new HashSet<>();
        Map<String, Set<String>> geometry = new HashMap<>();
        for (String style : List.of("ftmossgen:forest", "ftmossgen:desert", "ftmossgen:frost")) {
            Profiles.get(style);
            for (long seed : new long[] {20261107L, -1L, 42L}) {
                var plan = Planner.create(style, seed, new BlockPos(0, 100, 0));
                helper.assertTrue(style.equals(plan.style), "Frozen plan owns explicit profile");
                helper.assertTrue(gson.toJson(plan).equals(gson.toJson(Planner.create(style, seed, new BlockPos(0, 100, 0)))), "Same seed and profile are deterministic");
                helper.assertTrue(ids.add(plan.id), "Profile identity participates in unique instance ID");
                Set<Integer> floors = new HashSet<>();
                for (var piece : plan.pieces) {
                    helper.assertTrue(style.equals(piece.style), "Every piece preserves profile");
                    if (!piece.ports.isEmpty() && piece.grid != null) floors.add(piece.grid[1]);
                    for (var other : plan.pieces) if (piece.index < other.index) helper.assertTrue(!Planner.bounds(piece).intersects(Planner.bounds(other)), "No style room envelope overlap");
                }
                helper.assertTrue(floors.size() == 4, "Each profile preserves four connected floors");
                portalChoice(helper, plan);
            }
            Set<String> occupied = new HashSet<>();
            for (var template : Catalog.get(style).templates.values()) {
                for (var cell : Catalog.blocks(style, template.key)) {
                    if (!cell.state().getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, cell.position()).isEmpty()) {
                        occupied.add(template.key + ":" + cell.position().asLong());
                    }
                }
            }
            helper.assertTrue(occupied.size() > 10000, "Substantial real generated style geometry");
            geometry.put(style, occupied);
        }
        helper.assertTrue(!geometry.get("ftmossgen:forest").equals(geometry.get("ftmossgen:desert")) && !geometry.get("ftmossgen:forest").equals(geometry.get("ftmossgen:frost"))
            && !geometry.get("ftmossgen:desert").equals(geometry.get("ftmossgen:frost")), "Styles change occupied collision geometry, not only palette names");
        System.out.println("STYLE_PLANNER_PASS:9 plans;three profiles;unique IDs;determinism;independent collision BFS;different occupied geometry");
        helper.succeed();
    }
}
