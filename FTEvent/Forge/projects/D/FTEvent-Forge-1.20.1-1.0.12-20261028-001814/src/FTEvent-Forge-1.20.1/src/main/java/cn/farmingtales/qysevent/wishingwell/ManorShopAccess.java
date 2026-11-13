package cn.farmingtales.qysevent.wishingwell;

import cn.farmingtales.qysevent.QysEventMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = QysEventMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ManorShopAccess {
    public static final ResourceLocation CRIMSON_CONTRACT = ResourceLocation.fromNamespaceAndPath(
            QysEventMod.MOD_ID, "crimson_contract");
    private static final ResourceLocation LEGACY_CONTRACT = ResourceLocation.fromNamespaceAndPath("ftboss", "blood_pact");
    private static final ResourceLocation WEAPON = ResourceLocation.fromNamespaceAndPath("tridents_n_stuff", "sanguine_bond");

    private ManorShopAccess() {}

    public static boolean hasCrimsonContract(ServerPlayer player) {
        return completed(player, CRIMSON_CONTRACT) || completed(player, LEGACY_CONTRACT) || ownsContract(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Advancement advancement = player.server.getAdvancements().getAdvancement(CRIMSON_CONTRACT);
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()
                || !hasCrimsonContract(player)) return;
        for (String criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static boolean completed(ServerPlayer player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static boolean ownsContract(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && WEAPON.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) return true;
        }
        return false;
    }
}
