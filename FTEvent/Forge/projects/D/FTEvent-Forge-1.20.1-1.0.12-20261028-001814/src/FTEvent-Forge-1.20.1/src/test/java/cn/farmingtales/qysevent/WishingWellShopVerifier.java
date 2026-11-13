package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.client.WishingWellShopLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class WishingWellShopVerifier {
    private WishingWellShopVerifier() {}

    public static void verify() {
        require(WishingWellShopLayout.WIDTH <= 320 && WishingWellShopLayout.HEIGHT <= 240,
                "shop must fit the minimum GUI-scaled window");
        List<WishingWellShopLayout.Bounds> products = new ArrayList<>();
        int visible = 0;
        for (int slot = 0; slot < 72; slot++) {
            WishingWellShopLayout.Position position = WishingWellShopLayout.position(slot);
            require(position == WishingWellShopLayout.position(slot), "immutable slot layout must be reused");
            if (position.x() >= 0) {
                visible++;
                require(position.x() + 16 <= WishingWellShopLayout.WIDTH
                                && position.y() + 16 <= WishingWellShopLayout.HEIGHT,
                        "visible slot is clipped: " + slot);
            }
            WishingWellShopLayout.Bounds bounds = WishingWellShopLayout.productBounds(slot);
            if (bounds == null) continue;
            require(bounds == WishingWellShopLayout.productBounds(slot), "immutable product bounds must be reused");
            require(bounds.contains(position.x(), position.y())
                            && bounds.contains(position.x() + 15, position.y() + 15),
                    "product item and click area disagree: " + slot);
            require(!bounds.contains(bounds.x() + bounds.width(), bounds.y()), "right edge must be exclusive");
            require(!bounds.contains(bounds.x(), bounds.y() + bounds.height()), "bottom edge must be exclusive");
            for (WishingWellShopLayout.Bounds other : products) {
                require(bounds.x() >= other.x() + other.width() || other.x() >= bounds.x() + bounds.width()
                                || bounds.y() >= other.y() + other.height() || other.y() >= bounds.y() + bounds.height(),
                        "product click areas overlap");
            }
            products.add(bounds);
        }
        require(products.size() == 13, "expected eleven summons and two manor products");
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 72, Integer.MAX_VALUE}) {
            require(WishingWellShopLayout.position(invalid).x() == -10000, "invalid slot became visible");
            require(WishingWellShopLayout.productBounds(invalid) == null, "invalid slot became clickable");
        }
        for (int extent = 6; extent <= 512; extent++) {
            int[] expected = {0, 3, extent - 3, extent};
            for (int edge = 0; edge < 4; edge++) {
                require(WishingWellShopLayout.frameEdge(edge, extent) == expected[edge], "nine-slice edge changed");
            }
        }
        require(visible == 50, "expected header, thirteen products and thirty-six backpack slots");
        require(WishingWellShopLayout.position(36).x() == WishingWellShopLayout.position(63).x(),
                "backpack and hotbar columns must align");
        try (InputStream resource = WishingWellShopVerifier.class.getResourceAsStream(
                "/assets/qys_event/textures/gui/wishing_well_shop.png")) {
            require(resource != null, "redrawn shop texture is missing");
            BufferedImage texture = ImageIO.read(resource);
            require(texture != null && texture.getWidth() == WishingWellShopLayout.WIDTH
                            && texture.getHeight() == WishingWellShopLayout.HEIGHT,
                    "shop texture does not match its rendering dimensions");
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read wishing-well shop texture", error);
        }
        verifyContractAdvancement();
        System.out.println("Wishing-well UI verified: 13 products, 36 backpack slots, click bounds, pixel-art texture and red contract advancement.");
    }

    private static void verifyContractAdvancement() {
        JsonObject advancement = resourceJson("/data/qys_event/advancements/crimson_contract.json");
        require(advancement.get("parent").getAsString().equals("minecraft:adventure/root"),
                "contract advancement must not depend on the optional Boss advancement tree");
        JsonObject display = advancement.getAsJsonObject("display");
        JsonObject title = display.getAsJsonObject("title");
        require(title.get("text").getAsString().equals("×") && title.get("color").getAsString().equals("red")
                        && title.get("bold").getAsBoolean(), "contract title must be a bold red cross");
        require(display.get("show_toast").getAsBoolean() && display.get("announce_to_chat").getAsBoolean(),
                "obtaining the contract must announce its advancement");
        JsonObject criterion = advancement.getAsJsonObject("criteria").getAsJsonObject("obtained_crimson_contract");
        require(criterion.get("trigger").getAsString().equals("minecraft:inventory_changed"),
                "acquisition must use the native persistent inventory advancement");
        require(criterion.getAsJsonObject("conditions").getAsJsonArray("items").get(0).getAsJsonObject()
                .get("tag").getAsString().equals("qys_event:crimson_contract"), "wrong acquisition item tag");
        JsonObject weapon = resourceJson("/data/qys_event/tags/items/crimson_contract.json")
                .getAsJsonArray("values").get(0).getAsJsonObject();
        require(weapon.get("id").getAsString().equals("tridents_n_stuff:sanguine_bond")
                && !weapon.get("required").getAsBoolean(), "contract weapon must remain optional for standalone Event");
    }

    private static JsonObject resourceJson(String path) {
        try (InputStream resource = WishingWellShopVerifier.class.getResourceAsStream(path)) {
            require(resource != null, "missing resource: " + path);
            return JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read " + path, error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
