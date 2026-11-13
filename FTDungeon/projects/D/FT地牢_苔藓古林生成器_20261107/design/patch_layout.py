from pathlib import Path
from build_forest import ROOT, patch

catalog=ROOT/'src/main/java/moss/generator/Catalog.java'
if 'bossPortalPiece' in (ROOT/'src/main/java/moss/generator/Planner.java').read_text(encoding='utf-8'):
    raise SystemExit('Layout extension already applied; Java files are authoritative.')
text=catalog.read_text(encoding='utf-8')
text=text.replace('public int minimum_y;', 'public int minimum_y;\n        public int[] portal, portal_trigger;\n        public int portal_facing;')
patch(catalog,text)
planner=ROOT/'src/main/java/moss/generator/Planner.java'
text=planner.read_text(encoding='utf-8')
text=text.replace('import net.minecraft.core.BlockPos;', 'import net.minecraft.core.BlockPos;\nimport net.minecraft.core.Direction;\nimport net.minecraft.world.level.EmptyBlockGetter;\nimport net.minecraft.world.level.block.state.BlockState;')
text=text.replace('public int edges;', '''public int edges;
        public int bossPortalPiece = -1, bossPortalFacing, portalDistance;
        public int[] bossPortal;
        public List<int[]> graphEdges = new ArrayList<>();
        public Map<Integer, Integer> roomDistances = new java.util.TreeMap<>();''')
text=text.replace('public long forkSeed;', 'public long forkSeed;\n        public int[] portal, portalTrigger;\n        public int portalFacing;')
text=text.replace('"v1:"', '"forest-v1:"')
text=text.replace('(record.pos.getX() - 32) * 16', '(record.pos.getX() - 32) * 48')
text=text.replace('(record.pos.getY() - 15) * 16', '(record.pos.getY() - 15) * 32')
text=text.replace('(record.pos.getZ() - 32) * 16', '(record.pos.getZ() - 32) * 48')
text=text.replace('spawnOrigin.getY() - 48','spawnOrigin.getY() - 96')
text=text.replace('plan.entry = Catalog.vec(spawnOrigin.offset(7, 4, 8));','plan.entry = Catalog.vec(spawnOrigin.offset(23, 4, 24));')
text=text.replace('        validate(plan);\n        return plan;', '        validate(plan);\n        choosePortal(plan);\n        return plan;')
text=text.replace('        for (Catalog.Socket local : definition.sockets)', '''        if (definition.portal != null) {
            piece.portal = Catalog.vec(transform(definition.portal, piece.size, rotation, piece.origin));
            piece.portalTrigger = Catalog.vec(transform(definition.portal_trigger, piece.size, rotation, piece.origin));
            Direction facing = Direction.from2DDataValue(definition.portal_facing);
            for (int turn = 0; turn < rotation; turn++) facing = facing.getClockWise();
            piece.portalFacing = facing.get2DDataValue();
        }
        for (Catalog.Socket local : definition.sockets)''')
text=text.replace('        plan.edges = edges.size(); plan.caps.clear();', '''        plan.edges = edges.size(); plan.caps.clear();
        plan.graphEdges.clear();
        edges.stream().sorted().forEach(edge -> {
            String[] pair = edge.split(":");
            plan.graphEdges.add(new int[]{Integer.parseInt(pair[0]), Integer.parseInt(pair[1])});
        });''')
extra='''
    private static final Map<String, List<BlockPos>> WALKABLE = new HashMap<>();

    private static List<BlockPos> walkable(String key) {
        return WALKABLE.computeIfAbsent(key, unused -> {
            Map<BlockPos, BlockState> states = new HashMap<>();
            for (Catalog.Cell cell : Catalog.blocks(key)) states.put(cell.position(), cell.state());
            List<BlockPos> result = new ArrayList<>();
            for (Catalog.Cell cell : Catalog.blocks(key)) {
                BlockPos position = cell.position();
                BlockState above = states.get(position.above());
                BlockState below = states.get(position.below());
                if (cell.state().isAir() && above != null && above.isAir() && below != null
                    && below.getFluidState().isEmpty()
                    && below.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, position.below())) result.add(position);
            }
            return List.copyOf(result);
        });
    }

    private static void choosePortal(Plan plan) {
        Set<BlockPos> feet = new HashSet<>();
        Map<BlockPos, Integer> owners = new HashMap<>();
        for (Piece piece : plan.pieces) {
            if (piece.ports.isEmpty()) continue;
            for (BlockPos local : walkable(piece.key)) {
                BlockPos world = transform(Catalog.vec(local), piece.size, piece.rotation, piece.origin);
                feet.add(world);
                owners.put(world, piece.index);
            }
        }
        Set<BlockPos> caps = new HashSet<>();
        for (int[] cap : plan.caps) caps.add(Catalog.pos(cap));
        feet.removeAll(caps);
        BlockPos entry = Catalog.pos(plan.entry);
        Catalog.require(feet.contains(entry), "Forest entry is not walkable");
        Map<BlockPos, Integer> distances = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        distances.put(entry, 0); queue.add(entry);
        Set<Integer> reachedPieces = new HashSet<>();
        while (!queue.isEmpty()) {
            BlockPos position = queue.remove();
            reachedPieces.add(owners.get(position));
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                for (int rise : new int[]{0, 1, -1}) {
                    BlockPos destination = next.offset(0, rise, 0);
                    if (!feet.contains(destination) || distances.containsKey(destination)) continue;
                    if (rise == 1 && !airAt(plan, position.above(2))) continue;
                    if (rise == -1 && !airAt(plan, destination.above(2))) continue;
                    distances.put(destination, distances.get(position) + 1);
                    queue.add(destination);
                }
            }
        }
        for (Piece piece : plan.pieces) if (!piece.ports.isEmpty()) {
            Catalog.require(reachedPieces.contains(piece.index), "Forest walking graph disconnected at " + piece.index);
        }
        int lowest = plan.pieces.stream().filter(piece -> piece.portal != null)
            .mapToInt(piece -> piece.portalTrigger[1]).min().orElseThrow();
        Piece selected = null;
        int farthest = -1;
        plan.roomDistances.clear();
        for (Piece piece : plan.pieces) {
            if (piece.portal == null) continue;
            Integer distance = distances.get(Catalog.pos(piece.portalTrigger));
            Catalog.require(distance != null, "Forest candidate portal unreachable: " + piece.index);
            plan.roomDistances.put(piece.index, distance);
            if (piece.portalTrigger[1] == lowest && (distance > farthest
                || distance == farthest && (selected == null || piece.index < selected.index))) {
                selected = piece; farthest = distance;
            }
        }
        Catalog.require(selected != null, "No lowest-floor portal room");
        plan.bossPortalPiece = selected.index;
        plan.bossPortal = selected.portal.clone();
        plan.bossPortalFacing = selected.portalFacing;
        plan.exit = selected.portalTrigger.clone();
        plan.portalDistance = farthest;
    }

    private static boolean airAt(Plan plan, BlockPos world) {
        for (Piece piece : plan.pieces) {
            if (piece.ports.isEmpty() || !bounds(piece).contains(world.getX()+0.5,world.getY()+0.5,world.getZ()+0.5)) continue;
            for (Catalog.Cell cell : Catalog.blocks(piece.key)) {
                if (transform(Catalog.vec(cell.position()), piece.size, piece.rotation, piece.origin).equals(world)) return cell.state().isAir();
            }
        }
        return false;
    }
'''
text=text.rstrip()[:-1]+extra+'}\n'
patch(planner,text)
