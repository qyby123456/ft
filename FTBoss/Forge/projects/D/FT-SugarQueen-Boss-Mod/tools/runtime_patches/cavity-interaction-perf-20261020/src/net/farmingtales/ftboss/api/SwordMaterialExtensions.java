package net.farmingtales.ftboss.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** 运行时方法移植模板；2026-10-20 只替换目录快照入口并增加动态规则捕获入口。 */
public final class SwordMaterialExtensions {
    private static final Map<ResourceLocation, Object> MATERIALS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Object> BONDS = new ConcurrentHashMap<>();

    private static native CompoundTag foodCatalogueSnapshot();

    public static CompoundTag foundationExtensionsSnapshot() {
        return foundationRuleSnapshot().copy();
    }

    public static FoundationAuthorRuleSnapshots.MaterialSnapshot foundationRuleSnapshot() {
        Map<String, Object> dynamicRules = new java.util.TreeMap<>();
        dynamicRules.put("Bonds", Map.copyOf(BONDS));
        dynamicRules.put("Materials", Map.copyOf(MATERIALS));
        dynamicRules.put("FoodProviderRevision", 1);
        return FoundationAuthorRuleSnapshots.material(dynamicRules, SwordMaterialExtensions::foodCatalogueSnapshot);
    }
}
