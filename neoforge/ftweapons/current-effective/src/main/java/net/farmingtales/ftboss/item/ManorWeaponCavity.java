package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.Entry;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.GourmetBladeCavityRules;
import net.farmingtales.ftboss.util.ManorWeaponCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorWeaponCavity {
    private ManorWeaponCavity() {
    }

    public static boolean applies(ItemStack var0) {
        return !var0.isEmpty() && var0.getCount() == 1 && ManorWeaponCavityRules.WEAPONS.contains(FourWeaponFoundationAdapter.registryId(var0));
    }

    public static boolean pendingMigration(ItemStack var0) {
        return applies(var0) && (FoodWeaponData.read(var0, "FTBossFourWeaponFoundation") != null || !SwordCavityBuild.storedItem(var0, Slot.CORE).isEmpty());
    }

    public static boolean migrate(ServerPlayer var0, ItemStack var1) {
        if (!pendingMigration(var1)) {
            return true;
        } else {
            View var2 = FoodWeaponData.read(var1, "FTBossFourWeaponFoundation") == null ? null : FourWeaponFoundationAdapter.read(var1);
            if (var2 != null && !var2.valid()) {
                return false;
            } else {
                ArrayList<ItemStack> var3 = new ArrayList<>();

                for (int var4 = 0; var4 < 27; var4++) {
                    Entry var5 = var2 == null ? null : (Entry)var2.entries().get(var4);
                    var3.add(
                        var2 == null
                            ? SwordCavityBuild.storedItem(var1, Slot.byIndex(var4))
                            : (var5 != null && var5.paid() ? var5.storedItem() : ItemStack.EMPTY)
                    );
                }

                ItemStack var13 = var3.set(GourmetBladeCavityRules.CORE, ItemStack.EMPTY);
                if (!var13.isEmpty()) {
                    for (int var14 = 0; var14 < var3.size(); var14++) {
                        if (var14 != GourmetBladeCavityRules.CORE && ((ItemStack)var3.get(var14)).isEmpty()) {
                            var3.set(var14, var13);
                            var13 = ItemStack.EMPTY;
                            break;
                        }
                    }
                }

                CompoundTag var15 = new CompoundTag();
                var15.putInt("Version", 2);
                var15.putInt("Revision", (var2 == null ? SwordCavityBuild.revision(var1) : var2.revision()) + 1);
                var15.putBoolean("GridValues", true);
                var15.putBoolean("LinksEnabled", true);
                var15.putString("NativeItem", FourWeaponFoundationAdapter.registryId(var1));
                CompoundTag var6 = new CompoundTag();

                for (Slot var10 : Slot.values()) {
                    ItemStack var11 = (ItemStack)var3.get(var10.ordinal());
                    CompoundTag var12 = new CompoundTag();
                    var12.putString("State", var11.isEmpty() ? "empty" : "installed");
                    var12.putString("Material", var11.isEmpty() ? "original" : SwordCavityBuild.materialFor(var11).id());
                    var12.putBoolean("EverPresent", !var11.isEmpty());
                    if (!var11.isEmpty()) {
                        var12.put("Item", var11.saveOptional(var0.registryAccess()));
                    }

                    var6.put(var10.name(), var12);
                }

                var15.put("Cells", var6);
                CompoundTag var16 = var1.get(DataComponents.CUSTOM_DATA).copyTag();
                var16.put("FTBossSwordStructure", var15);
                if (!var16.contains("FTBossWeaponMaterials")) {
                    var16.put("FTBossWeaponMaterials", new CompoundTag());
                }

                var16.remove("FTBossFourWeaponFoundation");
                var1.set(DataComponents.CUSTOM_DATA, CustomData.of(var16));
                FoodWeaponLifecycle.ensureTracked(var1);
                if (!var13.isEmpty() && !var0.getInventory().add(var13)) {
                    var0.drop(var13, false);
                }

                var0.getInventory().setChanged();
                return true;
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(Post var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            for (int var4 = 0; var4 < var1.getInventory().getContainerSize(); var4++) {
                ItemStack var3 = var1.getInventory().getItem(var4);
                if (pendingMigration(var3)) {
                    migrate(var1, var3);
                }
            }
        }
    }
}
