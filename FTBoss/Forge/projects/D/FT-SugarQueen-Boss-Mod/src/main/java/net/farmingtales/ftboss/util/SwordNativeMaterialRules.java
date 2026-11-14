package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** 原生剑体材料纯规则；2026-10-13 保留配方/主题/共享账本，并提供每格单件的九乘三剑形代表布局，不计锻造模板。 */
public final class SwordNativeMaterialRules {
    public static final int MAX_PIECES = 27;
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    private static final int REQUIRED_PIECES = 4;
    private static final int STICK_POSITION = 2;
    private static final int NUGGETS_PER_INGOT = 9;
    private static final String STICK = "minecraft:stick";
    private static final String AIR = "minecraft:air";
    private static final Piece EMPTY = new Piece(AIR, 0, Origin.SHARED);
    private static final int SWORD_SHAPE_SIZE = 19;
    private static final int[] OPTIONAL_LAYOUT_ORDER = {13, 14, 15, 16, 17, 6, 7, 8, 9, 10, 20, 21, 22, 23, 24, 4, 5, 11, 12, 18, 19, 25, 26};
    private static final String UPGRADE_TEMPLATE = "minecraft:netherite_upgrade_smithing_template";
    private static final List<String> NETHER_THEME = List.of("minecraft:nether_brick", "minecraft:obsidian", "minecraft:blaze_rod");
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final Comparator<Key> KEY_ORDER = Comparator.comparing(Key::itemId).thenComparingInt(key -> key.origin().ordinal());

    public enum Origin { RECIPE, THEME, SHARED }

    public record Piece(String itemId, int count, Origin origin) {}

    private record Key(String itemId, Origin origin) {}

    private SwordNativeMaterialRules() {}

    public static List<Piece> ironPrototype() {
        return List.of(ingredient("minecraft:iron_ingot", 1), ingredient("minecraft:iron_ingot", 1), ingredient(STICK, 1),
                new Piece("minecraft:string", 1, Origin.THEME), new Piece("minecraft:cobblestone", 1, Origin.THEME),
                new Piece("minecraft:flint", 1, Origin.THEME));
    }

    public static boolean ironRecipe(List<Piece> pieces) {
        if (pieces == null || pieces.isEmpty()) return false;
        Map<String, Integer> recipe = new TreeMap<>();
        Map<String, Integer> theme = new TreeMap<>();
        for (Piece piece : pieces) {
            if (!valid(piece) || piece.origin() == Origin.SHARED) return false;
            Map<String, Integer> target = piece.origin() == Origin.RECIPE ? recipe : theme;
            long total = (long) target.getOrDefault(piece.itemId(), 0) + piece.count();
            if (total > 18) return false;
            target.put(piece.itemId(), (int) total);
        }
        boolean original = recipe.equals(Map.of("minecraft:iron_nugget", 18, STICK, 1))
                || recipe.equals(Map.of("minecraft:iron_ingot", 2, STICK, 1));
        return original && (theme.isEmpty() || theme.equals(Map.of("minecraft:string", 1, "minecraft:cobblestone", 1, "minecraft:flint", 1)));
    }

    public static List<Piece> ironPrototypeLayout(List<Piece> pieces) {
        if (!ironRecipe(pieces)) return layout(pieces);
        List<Piece> result = new ArrayList<>();
        for (int index = 0; index < MAX_PIECES; index++) result.add(EMPTY);
        List<Piece> source = ironPrototype();
        int[] positions = {14, 15, 2, 3, 13, 16};
        for (int index = 0; index < positions.length; index++) result.set(positions[index], source.get(index));
        return List.copyOf(result);
    }

    public static List<Piece> vanilla(String weaponId) {
        if (weaponId == null) return List.of();
        return switch (weaponId) {
            case "minecraft:iron_sword" -> List.of(
                    ingredient("minecraft:iron_nugget", 9), ingredient("minecraft:iron_nugget", 8),
                    ingredient(STICK, 1), ingredient("minecraft:iron_nugget", 1));
            case "minecraft:golden_sword" -> List.of(
                    ingredient("minecraft:gold_nugget", 9), ingredient("minecraft:gold_nugget", 8),
                    ingredient(STICK, 1), ingredient("minecraft:gold_nugget", 1));
            case "minecraft:netherite_sword" -> List.of(
                    ingredient("minecraft:diamond", 2), ingredient("minecraft:netherite_scrap", 4),
                    ingredient(STICK, 1), ingredient("minecraft:gold_nugget", 36));
            case "minecraft:diamond_sword" -> basic("minecraft:diamond");
            case "minecraft:wooden_sword" -> basic("minecraft:oak_planks");
            case "minecraft:stone_sword" -> basic("minecraft:cobblestone");
            default -> List.of();
        };
    }

    public static List<Piece> recipe(List<Piece> ingredients, boolean netherTheme) {
        if (ingredients == null) return List.of();
        Map<Key, Integer> amounts = new TreeMap<>(KEY_ORDER);
        Map<String, Long> totals = new TreeMap<>();
        Set<String> shared = new TreeSet<>();
        for (Piece piece : ingredients) {
            if (!valid(piece)) return List.of();
            String itemId = normalizedItem(piece.itemId());
            if (piece.origin() == Origin.SHARED) {
                shared.add(itemId);
                continue;
            }
            if (UPGRADE_TEMPLATE.equals(itemId)) return List.of();
            long count = (long) piece.count() * (itemId.equals(piece.itemId()) ? 1 : NUGGETS_PER_INGOT);
            Key key = new Key(itemId, piece.origin());
            long sum = count + amounts.getOrDefault(key, 0);
            long total = count + totals.getOrDefault(itemId, 0L);
            if (sum > Integer.MAX_VALUE || total > Integer.MAX_VALUE) return List.of();
            amounts.put(key, (int) sum);
            totals.put(itemId, total);
        }
        if (netherTheme) for (String itemId : NETHER_THEME) {
            if (!totals.containsKey(itemId)) {
                amounts.put(new Key(itemId, Origin.THEME), 1);
                totals.put(itemId, 1L);
            }
        }
        if (amounts.isEmpty() || amounts.size() > MAX_PIECES) return List.of();
        List<Piece> remaining = new ArrayList<>();
        Piece stick = null;
        for (Map.Entry<Key, Integer> entry : amounts.entrySet()) {
            Piece piece = new Piece(entry.getKey().itemId(), entry.getValue(), entry.getKey().origin());
            if (STICK.equals(piece.itemId()) && stick == null) stick = piece;
            else remaining.add(piece);
        }
        List<Piece> result = new ArrayList<>();
        for (int index = 0; index < REQUIRED_PIECES; index++) result.add(null);
        if (stick != null) result.set(STICK_POSITION, stick);
        int next = 0;
        for (int index = 0; index < REQUIRED_PIECES && next < remaining.size(); index++) {
            if (result.get(index) == null) result.set(index, remaining.get(next++));
        }
        while (next < remaining.size()) result.add(remaining.get(next++));
        String support = stick != null || shared.contains(STICK) ? STICK
                : !shared.isEmpty() ? shared.iterator().next() : amounts.keySet().iterator().next().itemId();
        for (int index = 0; index < REQUIRED_PIECES; index++) {
            if (result.get(index) != null) continue;
            int donor = donor(result);
            if (donor >= 0) {
                Piece source = result.get(donor);
                result.set(donor, new Piece(source.itemId(), source.count() - 1, source.origin()));
                result.set(index, new Piece(source.itemId(), 1, source.origin()));
            } else result.set(index, new Piece(support, 0, Origin.SHARED));
        }
        return result.size() <= MAX_PIECES ? List.copyOf(result) : List.of();
    }

    private static boolean valid(Piece piece) {
        if (piece == null || piece.itemId() == null || piece.origin() == null || !ITEM_ID.matcher(piece.itemId()).matches()) return false;
        return piece.origin() == Origin.SHARED ? piece.count() == 0 : piece.count() > 0;
    }

    public static List<Piece> layout(List<Piece> budget) {
        if (budget == null) return List.of();
        Map<Key, Integer> available = new TreeMap<>(KEY_ORDER);
        Map<String, Long> totals = new TreeMap<>();
        Set<String> shared = new TreeSet<>();
        for (Piece piece : budget) {
            if (!valid(piece)) return List.of();
            if (AIR.equals(piece.itemId())) {
                if (piece.origin() != Origin.SHARED) return List.of();
                continue;
            }
            if (piece.origin() == Origin.SHARED) {
                shared.add(piece.itemId());
                continue;
            }
            if (UPGRADE_TEMPLATE.equals(piece.itemId())) return List.of();
            Key key = new Key(piece.itemId(), piece.origin());
            long quantity = (long) available.getOrDefault(key, 0) + piece.count();
            long total = totals.getOrDefault(piece.itemId(), 0L) + piece.count();
            if (quantity > Integer.MAX_VALUE || total > Integer.MAX_VALUE) return List.of();
            available.put(key, (int) quantity);
            totals.put(piece.itemId(), total);
        }
        if (totals.size() > MAX_PIECES) return List.of();
        List<Piece> result = new ArrayList<>();
        for (int index = 0; index < MAX_PIECES; index++) result.add(EMPTY);
        if (totals.isEmpty() && shared.isEmpty()) return List.copyOf(result);
        Key grip = available.keySet().stream().filter(key -> STICK.equals(key.itemId())).findFirst().orElse(null);
        String support = grip != null || shared.contains(STICK) ? STICK
                : !shared.isEmpty() ? shared.iterator().next() : totals.keySet().iterator().next();
        Set<String> represented = new TreeSet<>();
        for (int index = 0; index < REQUIRED_PIECES; index++) {
            Piece reference = index < budget.size() ? budget.get(index) : null;
            Key preferred = index == STICK_POSITION && grip != null ? grip
                    : reference != null && reference.origin() != Origin.SHARED && !AIR.equals(reference.itemId())
                            ? new Key(reference.itemId(), reference.origin()) : null;
            boolean gripPending = index < STICK_POSITION && grip != null;
            Key selected = layoutUnit(available, represented, preferred, gripPending ? grip : null,
                    totals.size(), MAX_PIECES - index - 1, false);
            if (selected != null) placeUnit(result, index, available, represented, selected);
            else {
                String itemId = reference != null && reference.origin() == Origin.SHARED && !AIR.equals(reference.itemId())
                        ? reference.itemId() : support;
                result.set(index, new Piece(itemId, 0, Origin.SHARED));
            }
        }
        int next = 0;
        while (represented.size() < totals.size() && next < OPTIONAL_LAYOUT_ORDER.length) {
            Key selected = layoutUnit(available, represented, null, null, totals.size(), MAX_PIECES, true);
            if (selected == null) return List.of();
            placeUnit(result, OPTIONAL_LAYOUT_ORDER[next++], available, represented, selected);
        }
        if (represented.size() != totals.size()) return List.of();
        int target = Math.max(SWORD_SHAPE_SIZE, totals.size());
        while (REQUIRED_PIECES + next < target && next < OPTIONAL_LAYOUT_ORDER.length) {
            Key selected = layoutUnit(available, represented, null, null, totals.size(), MAX_PIECES, false);
            if (selected == null) break;
            placeUnit(result, OPTIONAL_LAYOUT_ORDER[next++], available, represented, selected);
        }
        return List.copyOf(result);
    }

    public static boolean hasPiece(List<Piece> pieces, int logicalSlot) {
        if (pieces == null || pieces.size() != MAX_PIECES || logicalSlot < 0 || logicalSlot >= MAX_PIECES) return false;
        Piece piece = pieces.get(logicalSlot);
        return valid(piece) && !AIR.equals(piece.itemId()) && (piece.count() == 1 || piece.origin() == Origin.SHARED)
                && (piece.origin() == Origin.SHARED || !UPGRADE_TEMPLATE.equals(piece.itemId()));
    }

    public static int visualColumn(int logicalSlot) {
        int position = visualPosition(logicalSlot);
        return position < 0 ? -1 : position % COLUMNS;
    }

    public static int visualRow(int logicalSlot) {
        int position = visualPosition(logicalSlot);
        return position < 0 ? -1 : position / COLUMNS;
    }

    public static int slotAt(int column, int row) {
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) return -1;
        if (row == 0) return column + REQUIRED_PIECES;
        if (row == 2) return row * COLUMNS + column;
        return switch (column) {
            case 0 -> 2;
            case 1 -> 3;
            case 7 -> 1;
            case 8 -> 0;
            default -> column + 11;
        };
    }

    private static int visualPosition(int logicalSlot) {
        if (logicalSlot < 0 || logicalSlot >= MAX_PIECES) return -1;
        return switch (logicalSlot) {
            case 0 -> 17;
            case 1 -> 16;
            case 2 -> 9;
            case 3 -> 10;
            default -> logicalSlot <= 12 ? logicalSlot - REQUIRED_PIECES : logicalSlot <= 17 ? logicalSlot - 2 : logicalSlot;
        };
    }

    private static Key layoutUnit(Map<Key, Integer> available, Set<String> represented, Key preferred, Key grip,
            int types, int spacesAfter, boolean unseenOnly) {
        Key selected = null;
        int largest = 0;
        for (Map.Entry<Key, Integer> entry : available.entrySet()) {
            Key key = entry.getKey();
            int quantity = entry.getValue() - (key.equals(grip) ? 1 : 0);
            boolean unseen = !represented.contains(key.itemId());
            int unseenAfter = types - represented.size() - (unseen ? 1 : 0);
            int duplicateGrip = grip != null && (represented.contains(STICK) || STICK.equals(key.itemId())) ? 1 : 0;
            if (quantity <= 0 || unseenOnly && !unseen || unseenAfter > spacesAfter - duplicateGrip) continue;
            if (key.equals(preferred)) return key;
            if (quantity > largest) {
                selected = key;
                largest = quantity;
            }
        }
        return selected;
    }

    private static void placeUnit(List<Piece> layout, int slot, Map<Key, Integer> available, Set<String> represented, Key key) {
        layout.set(slot, new Piece(key.itemId(), 1, key.origin()));
        available.put(key, available.get(key) - 1);
        represented.add(key.itemId());
    }

    private static String normalizedItem(String itemId) {
        return switch (itemId) {
            case "minecraft:iron_ingot" -> "minecraft:iron_nugget";
            case "minecraft:gold_ingot" -> "minecraft:gold_nugget";
            default -> itemId;
        };
    }

    private static int donor(List<Piece> pieces) {
        int result = -1;
        for (int index = 0; index < pieces.size(); index++) {
            Piece piece = pieces.get(index);
            if (piece != null && piece.count() > 1 && (result < 0 || piece.count() > pieces.get(result).count())) result = index;
        }
        return result;
    }

    private static Piece ingredient(String itemId, int count) { return new Piece(itemId, count, Origin.RECIPE); }

    private static List<Piece> basic(String material) {
        return List.of(ingredient(material, 1), ingredient(material, 1), ingredient(STICK, 1), new Piece(STICK, 0, Origin.SHARED));
    }
}
