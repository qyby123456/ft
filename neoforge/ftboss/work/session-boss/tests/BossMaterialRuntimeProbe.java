package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.item.BossMaterialDrops;
import net.farmingtales.ftboss.skill.BossRelicDrops;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_bossprobe")
public final class BossMaterialRuntimeProbe {
    private static final List<String> cases = new ArrayList<>();

    public BossMaterialRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossMaterialRuntimeProbe::started);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("fullBossAcceptance", false);
        report.put("authenticatedPlayerKillRewardAccepted", false);
        report.put("actualNativeBossDeathPathTested", false);
        report.put("noPlayerNegativeScope", "Direct original event-handler call with actual registered Boss entity and generic no-player damage source; no death or player attribution fabricated");
        try {
            var world = event.getServer().overworld();
            Map<Kind, String> ids = Map.of(Kind.FROST, "frost_material", Kind.BUTCHER, "butcher_material", Kind.TEA, "tea_material", Kind.WINE, "wine_material", Kind.RAT, "cheese_material", Kind.DEFECT, "capacitor_material", Kind.COMPOST, "spore_material", Kind.APPLE, "apple_material", Kind.BENGER, "shepherd_material", Kind.TIDE, "tide_material");
            List<Map<String, Object>> outputs = new ArrayList<>();
            report.put("actualRewards", outputs);
            for (Kind kind : Kind.values()) {
                String expected = "ftboss:" + ids.get(kind);
                Item registered = BossMaterialDrops.get(kind);
                require(BuiltInRegistries.ITEM.getKey(registered).toString().equals(expected), "originalRegisteredMaterialId:" + kind);
                List<ItemStack> reward = new ArrayList<>();
                BossMaterialDrops.reward(kind, reward::add);
                require(reward.size() == 1 && reward.get(0).getItem() == registered && reward.get(0).getCount() == 1, "originalOneItemReward:" + kind);
                ItemStack stack = reward.get(0);
                require(stack.getMaxStackSize() == 64 && !stack.isDamageableItem(), "originalBasicItemStackProperties:" + kind);
                require(stack.getRarity() == Rarity.RARE && stack.has(DataComponents.FIRE_RESISTANT), "originalRarityAndFireResistance:" + kind);
                var saved = stack.save(world.registryAccess());
                ItemStack restored = ItemStack.parseOptional(world.registryAccess(), (net.minecraft.nbt.CompoundTag) saved);
                require(restored.getItem() == registered && restored.getCount() == 1, "nativeRegistrySerializationRoundTrip:" + kind);
                List<net.minecraft.network.chat.Component> tooltip = new ArrayList<>();
                registered.appendHoverText(stack, Item.TooltipContext.of(world), tooltip, TooltipFlag.NORMAL);
                require(tooltip.size() == 3, "materialTooltipWithoutWeaponGrowth:" + kind);
                outputs.add(Map.of("bossKind", kind.name(), "id", expected, "count", stack.getCount(), "tooltip", tooltip.stream().map(Object::toString).toList()));
            }
            List<ItemStack> nullReward = new ArrayList<>();
            BossMaterialDrops.reward(null, nullReward::add);
            require(nullReward.isEmpty(), "originalNullBossHasNoMaterialReward");
            List<ItemStack> creative = new ArrayList<>();
            BossMaterialDrops.addTo((stack, visibility) -> creative.add(stack));
            require(creative.size() == 10 && creative.stream().map(ItemStack::getItem).distinct().count() == 10, "allTenMaterialsInCreativeOutput");
            for (String absent : List.of("net.farmingtales.ftboss.item.FoodWeaponEvolution", "net.farmingtales.ftboss.util.FoodWeaponBuildRules", "net.farmingtales.ftboss.item.WeaponBossMaterialItems", "net.farmingtales.ftboss.menu.SwordCavityMenu")) {
                boolean missing = false;
                try { Class.forName(absent, false, BossMaterialDrops.class.getClassLoader()); }
                catch (ClassNotFoundException expected) { missing = true; }
                require(missing, "pausedWeaponClassAbsent:" + absent);
            }
            Entity actualBoss = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("ftboss", "sugar_queen")).create(world);
            require(actualBoss instanceof LivingEntity, "actualBossConstructedForOriginalNoPlayerRejection");
            LivingEntity living = (LivingEntity) actualBoss;
            var before = living.getPersistentData().copy();
            BossRelicDrops.scriptedDeath(new LivingDeathEvent(living, world.damageSources().generic()));
            require(before.equals(living.getPersistentData()) && !living.getPersistentData().getBoolean("ftboss:boss_relic_rewarded"), "originalNoPlayerHandlerDoesNotConsumeRewardEligibility");
            report.put("actualItemRegistryCount", BuiltInRegistries.ITEM.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).count());
            report.put("actualEntityRegistryCount", BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).count());
            report.put("actualCoreClassResource", BossMaterialDrops.class.getResource("BossMaterialDrops.class").toString());
            report.put("players", event.getServer().getPlayerList().getPlayerCount());
        } catch (Throwable failure) {
            report.put("failures", 1);
            report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(trace));
            report.put("trace", trace.toString());
        }
        try {
            Files.writeString(Path.of("actual-boss-material-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }
}
