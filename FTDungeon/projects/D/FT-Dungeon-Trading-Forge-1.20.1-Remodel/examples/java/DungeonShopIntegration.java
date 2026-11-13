package example;

import java.util.List;
import java.util.function.Predicate;
import net.farmingtales.ftdungeontrading.api.DungeonTradingApi;
import net.farmingtales.ftdungeontrading.api.TradeContext;
import net.farmingtales.ftdungeontrading.api.TradeCost;
import net.farmingtales.ftdungeontrading.api.TradeEvent;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;

public final class DungeonShopIntegration {
    private static final ResourceLocation OFFER_ID = new ResourceLocation("myaddon", "dungeon_supplies");

    private DungeonShopIntegration() {}

    public static void registerOffers() {
        TradeOffer supplies = new TradeOffer(OFFER_ID, Component.literal("冒险者补给"),
                List.of(TradeCost.item(new ItemStack(Items.EMERALD), 3)),
                List.of(new ItemStack(Items.BREAD, 6), new ItemStack(Items.TORCH, 8)));
        DungeonTradingApi.registerOffer(supplies);
    }

    public static void bindLoadedTable(ServerLevel level, BlockPos position) {
        DungeonTradingApi.bindTable(level, position, OFFER_ID, 20);
    }

    public static void installAccessRule(Predicate<TradeContext> accessRule) {
        MinecraftForge.EVENT_BUS.<TradeEvent.BeforePurchase>addListener(event -> {
            if (event.offer().id().equals(OFFER_ID) && !accessRule.test(event.context())) {
                event.setCanceled(true);
            }
        });
    }
}
