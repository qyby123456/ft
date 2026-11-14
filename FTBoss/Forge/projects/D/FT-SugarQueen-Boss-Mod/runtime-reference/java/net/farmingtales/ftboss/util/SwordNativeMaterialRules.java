/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class SwordNativeMaterialRules {
    public static final int MAX_PIECES = 27;
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    private static final int REQUIRED_PIECES = 4;
    private static final int STICK_POSITION = 2;
    private static final int NUGGETS_PER_INGOT = 9;
    private static final String STICK = "minecraft:stick";
    private static final String AIR = "minecraft:air";
    private static final Piece EMPTY = new Piece("minecraft:air", 0, Origin.SHARED);
    private static final int SWORD_SHAPE_SIZE = 19;
    private static final int[] OPTIONAL_LAYOUT_ORDER = new int[]{13, 14, 15, 16, 17, 6, 7, 8, 9, 10, 20, 21, 22, 23, 24, 4, 5, 11, 12, 18, 19, 25, 26};
    private static final String UPGRADE_TEMPLATE = "minecraft:netherite_upgrade_smithing_template";
    private static final List<String> NETHER_THEME = List.of("minecraft:nether_brick", "minecraft:obsidian", "minecraft:blaze_rod");
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final Comparator<Key> KEY_ORDER = Comparator.comparing(Key::itemId).thenComparingInt(key -> key.origin().ordinal());

    private SwordNativeMaterialRules() {
    }

    public static List<Piece> vanilla(String string) {
        if (string == null) {
            return List.of();
        }
        return switch (string) {
            case "minecraft:iron_sword" -> List.of(SwordNativeMaterialRules.ingredient("minecraft:iron_nugget", 9), SwordNativeMaterialRules.ingredient("minecraft:iron_nugget", 8), SwordNativeMaterialRules.ingredient(STICK, 1), SwordNativeMaterialRules.ingredient("minecraft:iron_nugget", 1));
            case "minecraft:golden_sword" -> List.of(SwordNativeMaterialRules.ingredient("minecraft:gold_nugget", 9), SwordNativeMaterialRules.ingredient("minecraft:gold_nugget", 8), SwordNativeMaterialRules.ingredient(STICK, 1), SwordNativeMaterialRules.ingredient("minecraft:gold_nugget", 1));
            case "minecraft:netherite_sword" -> List.of(SwordNativeMaterialRules.ingredient("minecraft:diamond", 2), SwordNativeMaterialRules.ingredient("minecraft:netherite_scrap", 4), SwordNativeMaterialRules.ingredient(STICK, 1), SwordNativeMaterialRules.ingredient("minecraft:gold_nugget", 36));
            case "minecraft:diamond_sword" -> SwordNativeMaterialRules.basic("minecraft:diamond");
            case "minecraft:wooden_sword" -> SwordNativeMaterialRules.basic("minecraft:oak_planks");
            case "minecraft:stone_sword" -> SwordNativeMaterialRules.basic("minecraft:cobblestone");
            default -> List.of();
        };
    }

    /*
     * WARNING - void declaration
     */
    public static List<Piece> recipe(List<Piece> list, boolean bl) {
        void var8_19;
        void var8_18;
        void var8_16;
        void var6_10;
        ArrayList<Piece> arrayList;
        if (list == null) {
            return List.of();
        }
        TreeMap<Key, Integer> treeMap = new TreeMap<Key, Integer>(KEY_ORDER);
        TreeMap<Object, Long> treeMap2 = new TreeMap<Object, Long>();
        TreeSet<String> treeSet = new TreeSet<String>();
        for (Piece object2 : list) {
            if (!SwordNativeMaterialRules.valid(object2)) {
                return List.of();
            }
            arrayList = SwordNativeMaterialRules.normalizedItem(object2.itemId());
            if (object2.origin() == Origin.SHARED) {
                treeSet.add((String)((Object)arrayList));
                continue;
            }
            if (UPGRADE_TEMPLATE.equals(arrayList)) {
                return List.of();
            }
            long l = (long)object2.count() * (long)(((String)((Object)arrayList)).equals(object2.itemId()) ? 1 : 9);
            Key key = new Key((String)((Object)arrayList), object2.origin());
            long l2 = l + (long)treeMap.getOrDefault(key, 0).intValue();
            long l3 = l + treeMap2.getOrDefault(arrayList, 0L);
            if (l2 > Integer.MAX_VALUE || l3 > Integer.MAX_VALUE) {
                return List.of();
            }
            treeMap.put(key, (int)l2);
            treeMap2.put(arrayList, l3);
        }
        if (bl) {
            for (String string : NETHER_THEME) {
                if (treeMap2.containsKey(string)) continue;
                treeMap.put(new Key(string, Origin.THEME), 1);
                treeMap2.put(string, 1L);
            }
        }
        if (treeMap.isEmpty() || treeMap.size() > 27) {
            return List.of();
        }
        ArrayList arrayList2 = new ArrayList();
        Object var6_9 = null;
        for (Map.Entry entry : treeMap.entrySet()) {
            Piece piece = new Piece(((Key)entry.getKey()).itemId(), (Integer)entry.getValue(), ((Key)entry.getKey()).origin());
            if (STICK.equals(piece.itemId()) && var6_10 == null) {
                Piece piece2 = piece;
                continue;
            }
            arrayList2.add(piece);
        }
        arrayList = new ArrayList<Piece>();
        boolean bl2 = false;
        while (var8_16 < 4) {
            arrayList.add(null);
            ++var8_16;
        }
        if (var6_10 != null) {
            arrayList.set(2, (Piece)var6_10);
        }
        boolean bl3 = false;
        for (int i = 0; i < 4 && var8_18 < arrayList2.size(); ++i) {
            if (arrayList.get(i) != null) continue;
            arrayList.set(i, (Piece)arrayList2.get((int)(++var8_18)));
        }
        while (var8_19 < arrayList2.size()) {
            arrayList.add((Piece)arrayList2.get((int)(++var8_19)));
        }
        String string = var6_10 != null || treeSet.contains(STICK) ? STICK : (!treeSet.isEmpty() ? (String)treeSet.iterator().next() : ((Key)treeMap.keySet().iterator().next()).itemId());
        for (int i = 0; i < 4; ++i) {
            if (arrayList.get(i) != null) continue;
            int n = SwordNativeMaterialRules.donor(arrayList);
            if (n >= 0) {
                Piece piece = (Piece)arrayList.get(n);
                arrayList.set(n, new Piece(piece.itemId(), piece.count() - 1, piece.origin()));
                arrayList.set(i, new Piece(piece.itemId(), 1, piece.origin()));
                continue;
            }
            arrayList.set(i, new Piece(string, 0, Origin.SHARED));
        }
        return arrayList.size() <= 27 ? List.copyOf(arrayList) : List.of();
    }

    private static boolean valid(Piece piece) {
        if (piece == null || piece.itemId() == null || piece.origin() == null || !ITEM_ID.matcher(piece.itemId()).matches()) {
            return false;
        }
        return piece.origin() == Origin.SHARED ? piece.count() == 0 : piece.count() > 0;
    }

    public static List<Piece> layout(List<Piece> list) {
        Key key2;
        int n;
        Object object;
        if (list == null) {
            return List.of();
        }
        TreeMap<Key, Integer> treeMap = new TreeMap<Key, Integer>(KEY_ORDER);
        TreeMap<String, Long> treeMap2 = new TreeMap<String, Long>();
        TreeSet<String> treeSet = new TreeSet<String>();
        for (Piece piece : list) {
            if (!SwordNativeMaterialRules.valid(piece)) {
                return List.of();
            }
            if (AIR.equals(piece.itemId())) {
                if (piece.origin() == Origin.SHARED) continue;
                return List.of();
            }
            if (piece.origin() == Origin.SHARED) {
                treeSet.add(piece.itemId());
                continue;
            }
            if (UPGRADE_TEMPLATE.equals(piece.itemId())) {
                return List.of();
            }
            object = new Key(piece.itemId(), piece.origin());
            long l = (long)treeMap.getOrDefault(object, 0).intValue() + (long)piece.count();
            long l2 = treeMap2.getOrDefault(piece.itemId(), 0L) + (long)piece.count();
            if (l > Integer.MAX_VALUE || l2 > Integer.MAX_VALUE) {
                return List.of();
            }
            treeMap.put((Key)object, (int)l);
            treeMap2.put(piece.itemId(), l2);
        }
        if (treeMap2.size() > 27) {
            return List.of();
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 27; ++i) {
            arrayList.add(EMPTY);
        }
        if (treeMap2.isEmpty() && treeSet.isEmpty()) {
            return List.copyOf(arrayList);
        }
        Key key3 = treeMap.keySet().stream().filter(key -> STICK.equals(key.itemId())).findFirst().orElse(null);
        object = key3 != null || treeSet.contains(STICK) ? STICK : (!treeSet.isEmpty() ? (String)treeSet.iterator().next() : (String)treeMap2.keySet().iterator().next());
        TreeSet<String> treeSet2 = new TreeSet<String>();
        for (n = 0; n < 4; ++n) {
            boolean bl;
            Piece piece;
            Piece piece2 = piece = n < list.size() ? list.get(n) : null;
            key2 = n == 2 && key3 != null ? key3 : (piece != null && piece.origin() != Origin.SHARED && !AIR.equals(piece.itemId()) ? new Key(piece.itemId(), piece.origin()) : null);
            Key key4 = SwordNativeMaterialRules.layoutUnit(treeMap, treeSet2, key2, (bl = n < 2 && key3 != null) ? key3 : null, treeMap2.size(), 27 - n - 1, false);
            if (key4 != null) {
                SwordNativeMaterialRules.placeUnit(arrayList, n, treeMap, treeSet2, key4);
                continue;
            }
            Object object2 = piece != null && piece.origin() == Origin.SHARED && !AIR.equals(piece.itemId()) ? piece.itemId() : object;
            arrayList.set(n, new Piece((String)object2, 0, Origin.SHARED));
        }
        n = 0;
        while (treeSet2.size() < treeMap2.size() && n < OPTIONAL_LAYOUT_ORDER.length) {
            Key key5 = SwordNativeMaterialRules.layoutUnit(treeMap, treeSet2, null, null, treeMap2.size(), 27, true);
            if (key5 == null) {
                return List.of();
            }
            SwordNativeMaterialRules.placeUnit(arrayList, OPTIONAL_LAYOUT_ORDER[n++], treeMap, treeSet2, key5);
        }
        if (treeSet2.size() != treeMap2.size()) {
            return List.of();
        }
        int n2 = Math.max(19, treeMap2.size());
        while (4 + n < n2 && n < OPTIONAL_LAYOUT_ORDER.length && (key2 = SwordNativeMaterialRules.layoutUnit(treeMap, treeSet2, null, null, treeMap2.size(), 27, false)) != null) {
            SwordNativeMaterialRules.placeUnit(arrayList, OPTIONAL_LAYOUT_ORDER[n++], treeMap, treeSet2, key2);
        }
        return List.copyOf(arrayList);
    }

    public static boolean hasPiece(List<Piece> list, int n) {
        if (list == null || list.size() != 27 || n < 0 || n >= 27) {
            return false;
        }
        Piece piece = list.get(n);
        return !(!SwordNativeMaterialRules.valid(piece) || AIR.equals(piece.itemId()) || piece.count() != 1 && piece.origin() != Origin.SHARED || piece.origin() != Origin.SHARED && UPGRADE_TEMPLATE.equals(piece.itemId()));
    }

    public static int visualColumn(int n) {
        int n2 = SwordNativeMaterialRules.visualPosition(n);
        return n2 < 0 ? -1 : n2 % 9;
    }

    public static int visualRow(int n) {
        int n2 = SwordNativeMaterialRules.visualPosition(n);
        return n2 < 0 ? -1 : n2 / 9;
    }

    public static int slotAt(int n, int n2) {
        if (n < 0 || n >= 9 || n2 < 0 || n2 >= 3) {
            return -1;
        }
        if (n2 == 0) {
            return n + 4;
        }
        if (n2 == 2) {
            return n2 * 9 + n;
        }
        return switch (n) {
            case 0 -> 2;
            case 1 -> 3;
            case 7 -> 1;
            case 8 -> 0;
            default -> n + 11;
        };
    }

    private static int visualPosition(int n) {
        if (n < 0 || n >= 27) {
            return -1;
        }
        return switch (n) {
            case 0 -> 17;
            case 1 -> 16;
            case 2 -> 9;
            case 3 -> 10;
            default -> n <= 12 ? n - 4 : (n <= 17 ? n - 2 : n);
        };
    }

    private static Key layoutUnit(Map<Key, Integer> map, Set<String> set, Key key, Key key2, int n, int n2, boolean bl) {
        Key key3 = null;
        int n3 = 0;
        for (Map.Entry<Key, Integer> entry : map.entrySet()) {
            int n4;
            Key key4 = entry.getKey();
            int n5 = entry.getValue() - (key4.equals(key2) ? 1 : 0);
            boolean bl2 = !set.contains(key4.itemId());
            int n6 = n - set.size() - (bl2 ? 1 : 0);
            int n7 = n4 = key2 != null && (set.contains(STICK) || STICK.equals(key4.itemId())) ? 1 : 0;
            if (n5 <= 0 || bl && !bl2 || n6 > n2 - n4) continue;
            if (key4.equals(key)) {
                return key4;
            }
            if (n5 <= n3) continue;
            key3 = key4;
            n3 = n5;
        }
        return key3;
    }

    private static void placeUnit(List<Piece> list, int n, Map<Key, Integer> map, Set<String> set, Key key) {
        list.set(n, new Piece(key.itemId(), 1, key.origin()));
        map.put(key, map.get(key) - 1);
        set.add(key.itemId());
    }

    private static String normalizedItem(String string) {
        return switch (string) {
            case "minecraft:iron_ingot" -> "minecraft:iron_nugget";
            case "minecraft:gold_ingot" -> "minecraft:gold_nugget";
            default -> string;
        };
    }

    private static int donor(List<Piece> list) {
        int n = -1;
        for (int i = 0; i < list.size(); ++i) {
            Piece piece = list.get(i);
            if (piece == null || piece.count() <= 1 || n >= 0 && piece.count() <= list.get(n).count()) continue;
            n = i;
        }
        return n;
    }

    private static Piece ingredient(String string, int n) {
        return new Piece(string, n, Origin.RECIPE);
    }

    private static List<Piece> basic(String string) {
        return List.of(SwordNativeMaterialRules.ingredient(string, 1), SwordNativeMaterialRules.ingredient(string, 1), SwordNativeMaterialRules.ingredient(STICK, 1), new Piece(STICK, 0, Origin.SHARED));
    }

    public static List<Piece> ironPrototype() {
        return List.of(SwordNativeMaterialRules.ingredient("minecraft:iron_ingot", 1), SwordNativeMaterialRules.ingredient("minecraft:iron_ingot", 1), SwordNativeMaterialRules.ingredient(STICK, 1), new Piece("minecraft:string", 1, Origin.THEME), new Piece("minecraft:cobblestone", 1, Origin.THEME), new Piece("minecraft:flint", 1, Origin.THEME));
    }

    public static boolean ironRecipe(List<Piece> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
        TreeMap treeMap2 = new TreeMap();
        for (Piece piece : list) {
            if (!SwordNativeMaterialRules.valid(piece) || piece.origin() == Origin.SHARED) {
                return false;
            }
            TreeMap<String, Integer> treeMap3 = piece.origin() == Origin.RECIPE ? treeMap : treeMap2;
            long l = (long)treeMap3.getOrDefault(piece.itemId(), 0).intValue() + (long)piece.count();
            if (l > 18L) {
                return false;
            }
            treeMap3.put(piece.itemId(), (int)l);
        }
        boolean bl = treeMap.equals(Map.of("minecraft:iron_nugget", 18, STICK, 1)) || treeMap.equals(Map.of("minecraft:iron_ingot", 2, STICK, 1));
        return bl && (treeMap2.isEmpty() || treeMap2.equals(Map.of("minecraft:string", 1, "minecraft:cobblestone", 1, "minecraft:flint", 1)));
    }

    public static List<Piece> ironPrototypeLayout(List<Piece> list) {
        if (!SwordNativeMaterialRules.ironRecipe(list)) {
            return SwordNativeMaterialRules.layout(list);
        }
        ArrayList<Piece> arrayList = new ArrayList<Piece>();
        for (int i = 0; i < 27; ++i) {
            arrayList.add(EMPTY);
        }
        List<Piece> list2 = SwordNativeMaterialRules.ironPrototype();
        int[] nArray = new int[]{14, 15, 2, 3, 13, 16};
        for (int i = 0; i < nArray.length; ++i) {
            arrayList.set(nArray[i], list2.get(i));
        }
        return List.copyOf(arrayList);
    }

    public record Piece(String itemId, int count, Origin origin) {
    }

    public static enum Origin {
        RECIPE,
        THEME,
        SHARED;

    }

    private record Key(String itemId, Origin origin) {
    }
}

