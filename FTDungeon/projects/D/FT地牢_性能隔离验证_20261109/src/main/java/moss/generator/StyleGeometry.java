package moss.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class StyleGeometry {
    private StyleGeometry() {}

    public static List<Catalog.Cell> process(Profiles.Style style, Catalog.Template template, List<Catalog.Cell> source) {
        if (style == null || template == null || source == null) throw new IllegalArgumentException("Missing style geometry input");
        if (style.decorations == null || style.decorations.isEmpty()) return List.copyOf(source);
        String role = role(template.key);
        if (role == null) return List.copyOf(source);
        View view = new View(template, source);
        Set<BlockPos> protectedCells = protectedCells(template, view);
        for (Profiles.Decoration decoration : style.decorations) {
            if (decoration == null || !"air".equals(decoration.mode)) throw new IllegalArgumentException("Only air decoration mode is supported");
            if (decoration.role == null || (!Set.of("all", "rooms", "room", "turn", "terminal", "promenade", "boss").contains(decoration.role)
                && !validPrefix(decoration.role))) {
                throw new IllegalArgumentException("Unknown geometry role: " + decoration.role);
            }
            if (!matches(decoration.role, role, template.key)) continue;
            validateBox(decoration, template);
            BlockState material = Profiles.state(decoration.block);
            if (material.isAir() || material.hasBlockEntity() || !material.getFluidState().isEmpty()
                || !material.isCollisionShapeFullBlock(view, BlockPos.ZERO)) {
                throw new IllegalArgumentException("Decoration requires a full solid block without BE/fluid: " + decoration.block);
            }
            for (int vertical = decoration.to[1]; vertical >= decoration.from[1]; vertical--) {
                for (int south = decoration.from[2]; south <= decoration.to[2]; south += decoration.spacing) {
                    for (int east = decoration.from[0]; east <= decoration.to[0]; east++) {
                        BlockPos position = new BlockPos(east, vertical, south);
                        Catalog.Cell previous = view.cells.get(position);
                        if (previous == null || previous.nbt() != null || !previous.state().isAir() || protectedCells.contains(position)) continue;
                        if (!clearBelow(view, position) || !attached(view, position)) continue;
                        view.cells.put(position, new Catalog.Cell(position, material, null));
                    }
                }
            }
        }
        List<Catalog.Cell> result = new ArrayList<>(view.cells.values());
        result.sort(Comparator.comparingInt((Catalog.Cell cell) -> cell.position().getY())
            .thenComparingInt(cell -> cell.position().getZ()).thenComparingInt(cell -> cell.position().getX()));
        return List.copyOf(result);
    }

    private static String role(String key) {
        if (key.startsWith("straight/room_")) return "room";
        if (key.startsWith("turn/")) return "turn";
        if (key.startsWith("terminal/")) return "terminal";
        if (key.equals("promenade")) return "promenade";
        if (key.startsWith("boss/")) return "boss";
        return null;
    }

    private static boolean validPrefix(String selection) {
        if (!selection.startsWith("prefix:")) return false;
        String prefix = selection.substring("prefix:".length());
        return !prefix.isEmpty() && prefix.matches("[a-z0-9_/-]+") && !prefix.contains("..");
    }

    private static boolean matches(String selection, String role, String key) {
        if ("all".equals(selection) || selection.equals(role)) return true;
        if ("rooms".equals(selection)) return !"boss".equals(role);
        return selection.startsWith("prefix:") && key.startsWith(selection.substring("prefix:".length()));
    }

    private static void validateBox(Profiles.Decoration decoration, Catalog.Template template) {
        if (decoration.from == null || decoration.to == null || decoration.from.length != 3 || decoration.to.length != 3
            || decoration.spacing < 1 || decoration.spacing > 64) throw new IllegalArgumentException("Invalid decoration bounds/spacing");
        for (int axis = 0; axis < 3; axis++) {
            if (decoration.from[axis] < 0 || decoration.to[axis] < decoration.from[axis] || decoration.to[axis] >= template.size[axis]) {
                throw new IllegalArgumentException("Decoration outside template " + template.key);
            }
        }
        if (decoration.from[1] < Math.max(8, template.minimum_y + 6)) throw new IllegalArgumentException("Decoration must stay above walking layer");
    }

    private static boolean clearBelow(View view, BlockPos position) {
        for (int below = 1; below <= 3; below++) {
            BlockState state = view.getBlockState(position.below(below));
            if (!state.isAir()) return false;
        }
        return true;
    }

    private static boolean attached(View view, BlockPos position) {
        for (Direction direction : List.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
            BlockPos neighbor = position.relative(direction);
            BlockState support = view.getBlockState(neighbor);
            if (support.getFluidState().isEmpty() && support.isCollisionShapeFullBlock(view, neighbor)) return true;
        }
        return false;
    }

    private static Set<BlockPos> protectedCells(Catalog.Template template, View view) {
        Set<BlockPos> result = new HashSet<>();
        for (Catalog.Cell cell : view.cells.values()) {
            BlockPos position = cell.position();
            if (cell.nbt() != null || cell.state().hasBlockEntity()) protect(result, position, 1, -1, 2);
            if (view.clear(position) && view.clear(position.above()) && view.getFluidState(position.below()).isEmpty()
                && view.getBlockState(position.below()).isCollisionShapeFullBlock(view, position.below())) {
                protect(result, position, 0, -1, 2);
            }
        }
        if (template.ports != null) for (Catalog.Port port : template.ports) {
            for (int[] point : port.cells) protect(result, Catalog.pos(point), 2, -1, 6);
        }
        if (template.sockets != null) for (Catalog.Socket socket : template.sockets) {
            protect(result, Catalog.pos(socket.position), 2, -1, 5);
            protect(result, Catalog.pos(socket.support), 1, 0, 1);
            protect(result, Catalog.pos(socket.spawn_position), 2, -1, 5);
        }
        if (template.portal != null) protect(result, Catalog.pos(template.portal), 4, -1, 6);
        if (template.portal_trigger != null) protect(result, Catalog.pos(template.portal_trigger), 3, -1, 5);
        if (template.key.startsWith("boss/")) {
            for (int[] point : new int[][]{{16,2,2},{16,2,13},{16,2,25},{16,2,22}}) protect(result, Catalog.pos(point), 4, -1, 6);
        }
        return result;
    }

    private static void protect(Set<BlockPos> cells, BlockPos center, int radius, int bottom, int top) {
        for (int east = -radius; east <= radius; east++) for (int south = -radius; south <= radius; south++) {
            for (int up = bottom; up <= top; up++) cells.add(center.offset(east, up, south));
        }
    }

    private static final class View implements BlockGetter {
        final Map<BlockPos, Catalog.Cell> cells = new HashMap<>();
        final int height;
        View(Catalog.Template template, List<Catalog.Cell> source) {
            height = template.size[1];
            for (Catalog.Cell cell : source) {
                if (cells.put(cell.position(), cell) != null) throw new IllegalArgumentException("Duplicate geometry cell");
            }
        }
        @Override public BlockEntity getBlockEntity(BlockPos position) { return null; }
        @Override public BlockState getBlockState(BlockPos position) {
            Catalog.Cell cell = cells.get(position);
            return cell == null ? Blocks.AIR.defaultBlockState() : cell.state();
        }
        @Override public FluidState getFluidState(BlockPos position) { return getBlockState(position).getFluidState(); }
        @Override public int getHeight() { return height; }
        @Override public int getMinBuildHeight() { return 0; }
        boolean clear(BlockPos position) {
            return getFluidState(position).isEmpty() && getBlockState(position).getCollisionShape(this, position).isEmpty();
        }
    }
}
