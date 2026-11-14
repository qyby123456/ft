package net.farmingtales.validation.weaponui;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 作者材料规则快照回归；检查规范化、隔离拷贝、失效以及非法武器状态保护。 */
public final class RuleCacheProbe {
    private static int checks;

    private static Object field(String name) throws Exception {
        var field = SwordMaterialExtensions.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static CompoundTag originalMaterial() throws Exception {
        Map<String, Object> rules = new TreeMap<>();
        rules.put("Bonds", Map.copyOf((Map<?, ?>) field("BONDS")));
        rules.put("Materials", Map.copyOf((Map<?, ?>) field("MATERIALS")));
        rules.put("FoodProviderRevision", 1);
        var catalogue = SwordMaterialExtensions.class.getDeclaredMethod("foodCatalogueSnapshot");
        catalogue.setAccessible(true);
        rules.put("FoodCatalogueResources", catalogue.invoke(null));
        return FoundationCanonicalDigest.object(rules);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
        checks++;
    }

    private static void parity(ItemStack weapon) throws Exception {
        CompoundTag material = originalMaterial();
        require(material.equals(SwordMaterialExtensions.foundationExtensionsSnapshot()), "Material author snapshot changed");
        CompoundTag original = new CompoundTag();
        original.put("Cavity", SwordCavityExtensions.foundationExtensionsSnapshot(weapon));
        original.put("Material", material);
        byte[] before = FoundationCanonicalDigest.canonical(original);
        CompoundTag current = FoundationAuthorRuleSnapshots.snapshot(weapon);
        require(Arrays.equals(before, FoundationCanonicalDigest.canonical(current)), "Complete author rules canonical bytes changed");
        current.getCompound("Material").putString("probe", "mutation");
        require(original.equals(FoundationAuthorRuleSnapshots.snapshot(weapon)), "Caller mutation poisoned author cache");
        CompoundTag paid = FoundationCanonicalDigest.object(List.of(Map.of("Slot", 1, "Definition", "probe-dynamic-value")));
        CompoundTag fullRules = original.copy();
        fullRules.putString("Profile", "probe-full-profile");
        fullRules.put("PaidMaterialDefinitions", paid);
        String expected = FoundationCanonicalDigest.fingerprint("author-rules", fullRules);
        require(expected.equals(FoundationAuthorRuleSnapshots.fingerprint(weapon, "probe-full-profile", paid)), "Author fingerprint bytes changed");
        require(expected.equals(FoundationAuthorRuleSnapshots.fingerprint(weapon, "probe-full-profile", paid.copy())), "Repeated fingerprint changed");
        CompoundTag changedPaid = FoundationCanonicalDigest.object(List.of(Map.of("Slot", 2, "Definition", "probe-dynamic-value")));
        require(!expected.equals(FoundationAuthorRuleSnapshots.fingerprint(weapon, "probe-full-profile", changedPaid)), "Paid definition change reused old fingerprint");
        require(!expected.equals(FoundationAuthorRuleSnapshots.fingerprint(weapon, "changed-profile", paid)), "Profile change reused old fingerprint");
        CompoundTag copy = SwordMaterialExtensions.foundationExtensionsSnapshot();
        copy.putString("probe", "mutation");
        require(material.equals(SwordMaterialExtensions.foundationExtensionsSnapshot()), "Caller mutation poisoned material cache");
        FoundationAuthorRuleSnapshots.validate(weapon);
    }

    public static void run(Path instance) {
        try {
            ItemStack bow = new ItemStack(Items.BOW);
            parity(bow);
            boolean untrustedRejected = false;
            try {
                FoundationAuthorRuleSnapshots.material(Map.of(), () -> { throw new AssertionError("Untrusted catalogue evaluated"); });
            } catch (IllegalArgumentException expected) {
                untrustedRejected = expected.getMessage().equals("untrusted-author-rule-source");
            }
            require(untrustedRejected, "Untrusted material cache source accepted");
            String original = FourWeaponFoundationAdapter.rulesFingerprint(bow);
            ResourceLocation id = new ResourceLocation("ftboss", "probe_rule_cache_material");
            require(!SwordMaterialExtensions.registered(id), "Probe material already registered");
            try {
                SwordMaterialDefinition definition = new SwordMaterialDefinition(id, Material.IRON, SwordFunctionRules.ZERO,
                        Set.of(SwordMaterialDefinition.Category.METAL), 0, 0, OptionalDouble.empty(), OptionalInt.empty(),
                        Map.of(new ResourceLocation("ftboss", "probe_attribute"), 3.0), Component.literal("缓存完整值测试"));
                require(SwordMaterialExtensions.register(definition), "Material registration failed");
                parity(bow);
                require(!original.equals(FourWeaponFoundationAdapter.rulesFingerprint(bow)), "Material change did not invalidate rules fingerprint");
            } finally {
                SwordMaterialExtensions.unregister(id);
            }
            parity(bow);
            require(original.equals(FourWeaponFoundationAdapter.rulesFingerprint(bow)), "Material unregister did not restore rules fingerprint");
            ItemStack stick = new ItemStack(Items.STICK);
            CompoundTag beforeLayout = FoundationAuthorRuleSnapshots.snapshot(stick);
            int[] positions = new int[27];
            Arrays.setAll(positions, index -> 26 - index);
            try {
                require(SwordCavityExtensions.registerLayout(new ResourceLocation("minecraft", "stick"), positions), "Probe layout already registered");
                parity(stick);
                require(!beforeLayout.equals(FoundationAuthorRuleSnapshots.snapshot(stick)), "Layout change did not invalidate author cache");
            } finally {
                SwordCavityExtensions.unregisterLayout(new ResourceLocation("minecraft", "stick"));
            }
            require(beforeLayout.equals(FoundationAuthorRuleSnapshots.snapshot(stick)), "Layout unregister did not restore author cache");
            for (var item : List.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.IRON_AXE)) {
                ItemStack weapon = new ItemStack(item);
                parity(weapon);
                require(FourWeaponFoundationAdapter.read(weapon).valid(), "Fresh foundation invalid");
                weapon.getOrCreateTagElement(FourWeaponFoundationAdapter.ROOT).putInt("Schema", -1);
                require(!FourWeaponFoundationAdapter.read(weapon).valid(), "Cache bypassed malformed weapon validation");
                weapon.removeTagKey(FourWeaponFoundationAdapter.ROOT);
                require(FourWeaponFoundationAdapter.read(weapon).valid(), "Invalid weapon poisoned cached rules");
                weapon.setCount(2);
                require(!FourWeaponFoundationAdapter.read(weapon).valid(), "Cache bypassed stack count validation");
            }
            long start = System.nanoTime();
            for (int index = 0; index < 200; index++) require(FourWeaponFoundationAdapter.read(bow).valid(), "Repeated foundation read invalid");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "PASS_AUTHOR_RULE_CANONICAL_PARITY_MUTATION_ISOLATION_INVALIDATION_AND_WEAPON_GUARDS");
            result.put("checks", checks);
            result.put("warmBowReadMeanMs", (System.nanoTime() - start) / 200000000.0);
            Files.writeString(instance.resolve("evidence/RULE_CACHE_RESULT.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
            System.out.println("[RuleCacheProbe] PASS checks=" + checks);
        } catch (Exception failure) {
            throw new IllegalStateException("Rule cache acceptance failed", failure);
        }
    }
}
