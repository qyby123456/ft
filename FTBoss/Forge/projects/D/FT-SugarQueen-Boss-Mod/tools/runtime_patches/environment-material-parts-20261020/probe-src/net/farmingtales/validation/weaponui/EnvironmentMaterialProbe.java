package net.farmingtales.validation.weaponui;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.*;
import net.farmingtales.ftboss.menu.WeaponCavityMenuRules;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.registries.ForgeRegistries;

/** 环境辅料真实注册表、配件存取和重载回归；只在隔离测试世界使用。 */
public final class EnvironmentMaterialProbe {
    private static final List<String> MATERIALS = List.of("irons_spellbooks:arcane_essence", "irons_spellbooks:arcane_ingot", "irons_spellbooks:weapon_parts", "irons_spellbooks:ice_venom_vial", "irons_spellbooks:pyrium_ingot", "irons_spellbooks:frosted_helve", "irons_spellbooks:permafrost_shard", "irons_spellbooks:magic_cloth", "armamentarium:greatalloy_ingot", "armamentarium:greatalloy_nugget", "legendary_monsters:chorus_crystal", "legendary_monsters:infected_chorus_fruit", "legendary_monsters:void_gem", "legendary_monsters:enderitium_ingot", "legendary_monsters:knights_sword_blade_half", "legendary_monsters:metal_debris", "legendary_monsters:withered_bone", "legendary_monsters:withered_horn", "legendary_monsters:air_rune", "legendary_monsters:cloud_rod", "minecraft:grass", "minecraft:oak_leaves", "minecraft:kelp", "minecraft:slime_ball", "minecraft:chain", "minecraft:tripwire_hook", "minecraft:iron_nugget", "minecraft:feather", "minecraft:phantom_membrane", "minecraft:ghast_tear", "minecraft:ice", "minecraft:redstone_block", "tide:shooting_starfish");
    private static final List<String> WEAPONS = List.of("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:bow", "minecraft:crossbow", "minecraft:trident", "irons_spellbooks:graybeard_staff", "irons_spellbooks:artificer_cane", "ftboss:qizai_prayer_staff", "ftboss:frost_queen_staff");
    private static int checks;
    private static void require(boolean condition, String label) { checks++; if (!condition) throw new IllegalStateException(label); }
    private static ItemStack item(String id) { return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id))); }
    public static void run(Path instance, ServerPlayer player, BlockPos statue) {
        var result = new LinkedHashMap<String, Object>();
        var registered = new ArrayList<String>();
        var absent = new ArrayList<String>();
        var matrix = new ArrayList<Map<String, Object>>();
        try {
            for (String id : MATERIALS) {
                if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id))) { absent.add(id); continue; }
                ItemStack stack = item(id);
                require(!stack.isDamageableItem(), "Raw material is damageable " + id);
                require(SwordMaterialExtensions.resolve(stack).isPresent(), "Material not resolved " + id);
                registered.add(id);
            }
            require(registered.contains("irons_spellbooks:arcane_essence"), "Arcane essence missing");
            player.setGameMode(GameType.CREATIVE);
            player.getInventory().clearContent();
            player.getInventory().selected = 0;
            for (String weaponId : WEAPONS) {
                player.closeContainer();
                player.setItemInHand(InteractionHand.MAIN_HAND, item(weaponId));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                if (FoodWeaponSkillLoadout.canBind(player.getMainHandItem())) require(FoodWeaponSkillLoadout.bindFromUi(player, 1), "Bind " + weaponId);
                StatueMenuNetwork.open(player, statue);
                ItemStack seed = player.getMainHandItem().copy();
                if (FourWeaponFoundationAdapter.applies(seed)) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, seed);
                    require(FoundationEntryPoints.seedFromStatue(player, seed, statue, true), "Seed " + weaponId);
                }
                int tested = 0;
                for (int materialCase = 0; materialCase < registered.size() * 2; materialCase++) {
                    String materialId = registered.get(materialCase % registered.size());
                    ItemStack weapon = seed.copy();
                    player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
                    ItemStack offering = item(materialId);
                    if (materialCase < registered.size()) offering.getOrCreateTag().putString("EnvironmentProbe", materialId);
                    var parts = new ArrayList<>(WeaponCavityMenuRules.containerItems(weapon));
                    int cell = -1;
                    for (int index = 0; index < 27; index++) {
                        if (!WeaponCavityMenuRules.cellUnlocked(weapon, index)) continue;
                        var candidate = new ArrayList<>(parts);
                        candidate.set(index, offering);
                        if (WeaponCavityMenuRules.canSetContainerItems(weapon, candidate)) { cell = index; break; }
                    }
                    require(cell >= 0, "No material cell " + weaponId + " " + materialId);
                    parts.set(cell, offering);
                    int revision = WeaponCavityMenuRules.revision(weapon);
                    require(WeaponCavityMenuRules.setContainerItems(player, weapon, parts, revision), "Install " + weaponId + " " + materialId);
                    require(WeaponCavityMenuRules.revision(weapon) > revision, "Install revision");
                    if (FourWeaponFoundationAdapter.applies(weapon)) require(new net.farmingtales.ftboss.transaction.FoundationApprovedRules().fingerprint(player, weapon) != null, "Post-install authority invalid " + weaponId + " " + materialId + " " + FourWeaponFoundationAdapter.read(weapon).failure());
                    ItemStack loaded = ItemStack.of(weapon.save(new CompoundTag()));
                    require(ItemStack.matches(WeaponCavityMenuRules.containerItems(loaded).get(cell), offering), "Stored NBT " + weaponId + " " + materialId);
                    var invalid = new ArrayList<>(WeaponCavityMenuRules.containerItems(loaded));
                    ItemStack oversized = offering.copy(); oversized.setCount(2); invalid.set(cell, oversized);
                    require(!WeaponCavityMenuRules.canSetContainerItems(loaded, invalid), "Count guard");
                    invalid.set(cell, new ItemStack(Items.BARRIER));
                    require(!WeaponCavityMenuRules.canSetContainerItems(loaded, invalid), "Unknown guard");
                    invalid.set(cell, new ItemStack(Items.IRON_SWORD));
                    require(!WeaponCavityMenuRules.canSetContainerItems(loaded, invalid), "Weapon guard");
                    player.setItemInHand(InteractionHand.MAIN_HAND, loaded);
                    var removal = new ArrayList<>(WeaponCavityMenuRules.containerItems(loaded)); removal.set(cell, ItemStack.EMPTY);
                    require(WeaponCavityMenuRules.setContainerItems(player, loaded, removal, WeaponCavityMenuRules.revision(loaded)), "Remove");
                    require(WeaponCavityMenuRules.containerItems(ItemStack.of(loaded.save(new CompoundTag()))).get(cell).isEmpty(), "Removal NBT");
                    tested++;
                }
                matrix.add(Map.of("weapon", weaponId, "materials", tested, "status", "PASS"));
            }
            require(SwordMaterialExtensions.resolve(new ItemStack(Items.IRON_SWORD)).isEmpty(), "Damageable resolver guard");
            require(SwordMaterialExtensions.resolve(new ItemStack(Items.BARRIER)).isEmpty(), "Unknown resolver guard");
            result.put("status", "PASS_REGISTERED_MATERIAL_INSTALL_REMOVE_NBT_AND_GUARDS");
        } catch (Throwable problem) {
            result.put("status", "FAIL"); result.put("failure", problem.toString());
            throw new IllegalStateException("Environment material matrix failed", problem);
        } finally {
            result.put("checks", checks); result.put("registeredMaterials", registered); result.put("absentOptionalMaterials", absent); result.put("matrix", matrix);
            try { Files.writeString(instance.resolve("evidence/ENVIRONMENT_MATERIAL_RESULT.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result)); }
            catch (Exception problem) { throw new IllegalStateException(problem); }
        }
    }
}
