/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.trade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public final class TradeHandler {
    private static final List<Trade> TRADES = new ArrayList<Trade>();
    private static final String DEFAULT_JSON = "{\n  \"\u8bf4\u660e\": \"\u77f3\u50cf\u4ea4\u6613\u914d\u7f6e\uff1a\u73a9\u5bb6\u62ff cost \u6750\u6599\u53f3\u952e\u77f3\u50cf\uff0c\u83b7\u5f97 result\u3002item \u4e3a\u7269\u54c1id(\u652f\u6301mod\u7269\u54c1)\uff0ccount \u4e3a\u6570\u91cf\u3002\u6539\u5b8c\u91cd\u542f\u751f\u6548\u3002\",\n  \"trades\": [\n    { \"cost\": {\"item\": \"minecraft:sugar\", \"count\": 8},   \"result\": {\"item\": \"ftboss:sugar_queen_crystal\", \"count\": 1} },\n    { \"cost\": {\"item\": \"minecraft:sugar\", \"count\": 16},  \"result\": {\"item\": \"minecraft:emerald\", \"count\": 1} }\n  ]\n}\n";

    private TradeHandler() {
    }

    public static void load(Path configDir) {
        TRADES.clear();
        Path f = configDir.resolve("ftboss_trades.json");
        try {
            JsonObject root;
            if (!Files.exists(f, new LinkOption[0])) {
                Files.writeString(f, (CharSequence)DEFAULT_JSON, StandardCharsets.UTF_8, new OpenOption[0]);
            }
            if ((root = JsonParser.parseString((String)Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject()).has("trades")) {
                for (JsonElement el : root.getAsJsonArray("trades")) {
                    if (!el.isJsonObject()) continue;
                    try {
                        JsonObject o = el.getAsJsonObject();
                        JsonObject cost = o.getAsJsonObject("cost");
                        JsonObject res = o.getAsJsonObject("result");
                        Item c = TradeHandler.parseItem(cost.get("item").getAsString());
                        Item r = TradeHandler.parseItem(res.get("item").getAsString());
                        if (c == null || r == null) continue;
                        int cc = cost.has("count") ? cost.get("count").getAsInt() : 1;
                        int rc = res.has("count") ? res.get("count").getAsInt() : 1;
                        TRADES.add(new Trade(c, Math.max(1, cc), r, Math.max(1, rc)));
                    }
                    catch (Exception ex) {
                        FTBossMod.LOGGER.warn("[FTBoss][\u4ea4\u6613] \u8df3\u8fc7\u65e0\u6cd5\u89e3\u6790\u7684\u4ea4\u6613\u6761\u76ee: {}", (Object)ex.getMessage());
                    }
                }
            }
            FTBossMod.LOGGER.info("[FTBoss][\u4ea4\u6613] \u77f3\u50cf\u4ea4\u6613\u914d\u7f6e\u52a0\u8f7d\u5b8c\u6210: {} \u6761\u4ea4\u6613", (Object)TRADES.size());
        }
        catch (JsonParseException | IOException ex) {
            FTBossMod.LOGGER.error("[FTBoss][\u4ea4\u6613] \u8bfb\u53d6\u914d\u7f6e\u5931\u8d25, \u4f7f\u7528\u5185\u7f6e\u9ed8\u8ba4\u4ea4\u6613", ex);
            TRADES.clear();
            TRADES.add(new Trade(TradeHandler.parseItem("minecraft:sugar"), 8, TradeHandler.parseItem("ftboss:sugar_queen_crystal"), 1));
        }
    }

    private static Item parseItem(String id) {
        return (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    }

    public static void tryTrade(ServerPlayer p, ItemStack hand, double bx, double by, double bz) {
        if (hand.m_41619_()) {
            TradeHandler.listTrades(p);
            return;
        }
        for (Trade t : TRADES) {
            if (!hand.m_150930_(t.costItem) || hand.m_41613_() < t.costCount) continue;
            hand.m_41774_(t.costCount);
            ItemStack out = new ItemStack((ItemLike)t.resultItem, t.resultCount);
            if (!p.m_150109_().m_36054_(out)) {
                p.m_9236_().m_7967_((Entity)new ItemEntity(p.m_9236_(), bx, by + 0.5, bz, out));
            }
            p.m_9236_().m_6263_(null, bx, by, bz, SoundEvents.f_12509_, SoundSource.BLOCKS, 1.0f, 1.2f);
            p.m_5661_((Component)Component.m_237113_((String)("\u00a76[\u77f3\u50cf] \u00a7a\u4ea4\u6613\u6210\u529f \u00a77\u2192 " + t.resultItem.m_7626_(out).getString() + " x" + t.resultCount)), true);
            return;
        }
        p.m_5661_((Component)Component.m_237113_((String)"\u00a76[\u77f3\u50cf] \u00a7c\u5b83\u6447\u4e86\u6447\u5934\uff1a\u4e0d\u9700\u8981\u8fd9\u6837\u4e1c\u897f\uff08\u7a7a\u624b\u53f3\u952e\u53ef\u67e5\u770b\u53ef\u4ea4\u6613\u6e05\u5355\uff09"), true);
    }

    private static void listTrades(ServerPlayer p) {
        p.m_213846_((Component)Component.m_237113_((String)"\u00a76[\u77f3\u50cf\u00b7\u4ea4\u6613\u6e05\u5355] \u00a77\u62ff\u6750\u6599\u53f3\u952e\u77f3\u50cf\u5373\u53ef\u5151\u6362"));
        if (TRADES.isEmpty()) {
            p.m_213846_((Component)Component.m_237113_((String)"\u00a77  (\u6682\u65e0\u53ef\u7528\u4ea4\u6613\uff0c\u53ef\u7f16\u8f91 config/ftboss_trades.json \u6dfb\u52a0)"));
            return;
        }
        int i = 1;
        for (Trade t : TRADES) {
            ItemStack c = new ItemStack((ItemLike)t.costItem, t.costCount);
            ItemStack r = new ItemStack((ItemLike)t.resultItem, t.resultCount);
            p.m_213846_((Component)Component.m_237113_((String)("\u00a77" + i + ". \u00a7f" + c.m_41786_().getString() + " x" + t.costCount + " \u00a78\u2192 \u00a7f" + r.m_41786_().getString() + " x" + t.resultCount)));
            ++i;
        }
        p.m_213846_((Component)Component.m_237113_((String)"\u00a78\u7f16\u8f91 config/ftboss_trades.json \u53ef\u81ea\u5b9a\u4e49\u4ea4\u6613\uff08\u91cd\u542f\u751f\u6548\uff09"));
    }

    public static final class Trade {
        public final Item costItem;
        public final int costCount;
        public final Item resultItem;
        public final int resultCount;

        Trade(Item costItem, int costCount, Item resultItem, int resultCount) {
            this.costItem = costItem;
            this.costCount = costCount;
            this.resultItem = resultItem;
            this.resultCount = resultCount;
        }
    }
}

