package cn.farmingtales.qysevent.client;

public final class WishingWellShopLayout {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 236;
    public static final String MENU_TITLE = "许愿井 Lv.3 · 左键购买";
    public static final int HEADER_SLOT = 4;
    public static final int WEAPON_SLOT = 30;
    public static final int SPELL_SLOT = 32;
    private static final Position HIDDEN = new Position(-10000, -10000);
    private static final Position[] POSITIONS = createPositions();
    private static final Bounds[] PRODUCTS = createProducts();

    private WishingWellShopLayout() {}

    public static Position position(int slot) {
        return slot >= 0 && slot < POSITIONS.length ? POSITIONS[slot] : HIDDEN;
    }

    private static Position[] createPositions() {
        Position[] positions = new Position[72];
        for (int slot = 0; slot < positions.length; slot++) positions[slot] = createPosition(slot);
        return positions;
    }

    private static Position createPosition(int slot) {
        if (slot == HEADER_SLOT) return new Position(24, 163);
        if (slot >= 9 && slot < 20) {
            int product = slot - 9;
            return new Position(126 + product % 6 * 26, 57 + product / 6 * 24);
        }
        if (slot == WEAPON_SLOT) return new Position(130, 120);
        if (slot == SPELL_SLOT) return new Position(214, 120);
        if (slot >= 36 && slot < 63) {
            int inventory = slot - 36;
            return new Position(126 + inventory % 9 * 18, 153 + inventory / 9 * 18);
        }
        if (slot >= 63 && slot < 72) return new Position(126 + (slot - 63) * 18, 211);
        return HIDDEN;
    }

    public static Bounds productBounds(int slot) {
        return slot >= 0 && slot < PRODUCTS.length ? PRODUCTS[slot] : null;
    }

    private static Bounds[] createProducts() {
        Bounds[] products = new Bounds[36];
        for (int slot = 0; slot < products.length; slot++) products[slot] = createProductBounds(slot);
        return products;
    }

    private static Bounds createProductBounds(int slot) {
        if (slot >= 9 && slot < 20) {
            Position position = position(slot);
            return new Bounds(position.x() - 3, position.y() - 3, 22, 22);
        }
        if (slot == WEAPON_SLOT) return new Bounds(124, 115, 78, 26);
        if (slot == SPELL_SLOT) return new Bounds(208, 115, 78, 26);
        return null;
    }

    public static int frameEdge(int edge, int extent) {
        return switch (edge) {
            case 0 -> 0;
            case 1 -> 3;
            case 2 -> extent - 3;
            case 3 -> extent;
            default -> throw new IllegalArgumentException("Invalid frame edge: " + edge);
        };
    }

    public record Position(int x, int y) {}

    public record Bounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
