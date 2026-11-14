package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.NonManorComponentMigrationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 服务端背包中的已知旧版 Boss 配件升级；不遍历存档或箱子，不消耗、返还或覆盖玩家实装物。 */
@Mod.EventBusSubscriber(modid = "ftboss")
public final class NonManorWeaponComponentMigration {
    private static final String ROOT = "FTBossFourWeaponFoundation";

    private NonManorWeaponComponentMigration() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack weapon = player.getInventory().getItem(slot);
            CompoundTag source = weapon.getTagElement(ROOT);
            if (weapon.isEmpty() || weapon.getCount() != 1 || source == null) continue;
            var id = ForgeRegistries.ITEMS.getKey(weapon.getItem());
            if (id == null || !(id.toString().equals("ftboss:frost_queen_staff") || id.toString().equals("ftboss:barrel_crossbow"))) continue;
            CompoundTag upgraded = NonManorComponentMigrationRules.reconcile(source, FourWeaponFoundationProfiles.examples().get(id.toString()));
            if (upgraded == null) continue;
            weapon.getOrCreateTag().put(ROOT, upgraded);
            changed = true;
        }
        if (changed) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        }
    }
}
