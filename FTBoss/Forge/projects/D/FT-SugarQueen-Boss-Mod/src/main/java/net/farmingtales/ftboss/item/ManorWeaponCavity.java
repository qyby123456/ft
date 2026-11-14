package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.GourmetBladeCavityRules;
import net.farmingtales.ftboss.util.ManorWeaponCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorWeaponCavity {
    private ManorWeaponCavity() {}

    public static boolean applies(ItemStack weapon) {
        return !weapon.isEmpty() && weapon.getCount() == 1
                && ManorWeaponCavityRules.WEAPONS.contains(FourWeaponFoundationAdapter.registryId(weapon));
    }

    public static boolean pendingMigration(ItemStack weapon) {
        return applies(weapon) && (weapon.getTagElement(FourWeaponFoundationAdapter.ROOT) != null
                || !SwordCavityBuild.storedItem(weapon, SwordCavityRules.Slot.CORE).isEmpty());
    }

    public static boolean migrate(ServerPlayer player, ItemStack weapon) {
        if (!pendingMigration(weapon)) return true;
        var legacy = weapon.getTagElement(FourWeaponFoundationAdapter.ROOT) == null ? null : FourWeaponFoundationAdapter.read(weapon);
        if (legacy != null && !legacy.valid()) return false;
        List<ItemStack> materials = new ArrayList<>();
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            var entry = legacy == null ? null : legacy.entries().get(index);
            materials.add(legacy == null ? SwordCavityBuild.storedItem(weapon, SwordCavityRules.Slot.byIndex(index))
                    : entry != null && entry.paid() ? entry.storedItem() : ItemStack.EMPTY);
        }
        ItemStack displaced = materials.set(GourmetBladeCavityRules.CORE, ItemStack.EMPTY);
        if (!displaced.isEmpty()) {
            for (int index = 0; index < materials.size(); index++) {
                if (index == GourmetBladeCavityRules.CORE || !materials.get(index).isEmpty()) continue;
                materials.set(index, displaced);
                displaced = ItemStack.EMPTY;
                break;
            }
        }
        CompoundTag structure = new CompoundTag();
        structure.putInt("Version", SwordCavityRules.VERSION);
        structure.putInt("Revision", (legacy == null ? SwordCavityBuild.revision(weapon) : legacy.revision()) + 1);
        structure.putBoolean("GridValues", true);
        structure.putBoolean("LinksEnabled", true);
        structure.putString("NativeItem", FourWeaponFoundationAdapter.registryId(weapon));
        CompoundTag cells = new CompoundTag();
        for (var slot : SwordCavityRules.Slot.values()) {
            ItemStack material = materials.get(slot.ordinal());
            CompoundTag cell = new CompoundTag();
            cell.putString("State", material.isEmpty() ? "empty" : "installed");
            cell.putString("Material", material.isEmpty() ? "original" : SwordCavityBuild.materialFor(material).id());
            cell.putBoolean("EverPresent", !material.isEmpty());
            if (!material.isEmpty()) cell.put("Item", material.save(new CompoundTag()));
            cells.put(slot.name(), cell);
        }
        structure.put("Cells", cells);
        CompoundTag updated = weapon.getTag().copy();
        updated.put(SwordCavityBuild.ROOT, structure);
        if (!updated.contains(WeaponMaterialBuild.ROOT)) updated.put(WeaponMaterialBuild.ROOT, new CompoundTag());
        updated.remove(FourWeaponFoundationAdapter.ROOT);
        weapon.setTag(updated);
        FoodWeaponLifecycle.ensureTracked(weapon);
        if (!displaced.isEmpty() && !player.getInventory().add(displaced)) player.drop(displaced, false);
        player.getInventory().setChanged();
        return true;
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            ItemStack weapon = player.getInventory().getItem(index);
            if (pendingMigration(weapon)) migrate(player, weapon);
        }
    }
}
