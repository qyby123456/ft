package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftboss.item.NativeItemCapabilityObservation;
import net.farmingtales.ftboss.item.WeaponSystemScope410;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }

    private static List<String> hierarchy(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) result.add(current.getName());
        return result;
    }

    private static String family(Item item) {
        if (item instanceof SwordItem) return "sword";
        if (item instanceof BowItem) return "bow";
        if (item instanceof CrossbowItem) return "crossbow";
        if (item instanceof AxeItem) return "axe";
        if (item instanceof TridentItem) return "trident";
        if (hierarchy(item.getClass()).contains("io.redspace.ironsspellbooks.item.CastingItem")) return "staff";
        ItemStack stack = new ItemStack(item);
        for (String family : List.of("bow", "crossbow", "axe", "trident", "staff"))
            if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.matchesType(stack, family)) return family;
        return "unclassified";
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        try {
            Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks");
            Map<?, ?> registry = (Map<?, ?>)readStatic(hooks, "REGISTRY");
            long beforeVersion = ((AtomicLong)read(registry, "rawMutationVersion")).get();
            Object originalCapability = readStatic(NativeItemCapabilityObservation.class, "originalCurioCapability");
            Map<?, ?> originals = (Map<?, ?>)readStatic(NativeItemCapabilityObservation.class, "ORIGINAL_CURIO_PROVIDERS");
            Object observation = NativeItemCapabilityObservation.capture();
            Map<?, ?> capabilities = (Map<?, ?>)read(observation, "capabilities");
            Class<?> curioItem = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Integer> familyCounts = new LinkedHashMap<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                if (!WeaponSystemScope410.supports(stack)) continue;
                String family = family(item);
                familyCounts.merge(family, 1, Integer::sum);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", BuiltInRegistries.ITEM.getKey(item).toString()); row.put("family", family);
                row.put("class", item.getClass().getName()); row.put("hierarchy", hierarchy(item.getClass()));
                var domain = item.getClass().getProtectionDomain();
                row.put("codeSource", domain == null || domain.getCodeSource() == null ? null : domain.getCodeSource().getLocation().toExternalForm());
                row.put("nativeDamageable", net.farmingtales.ftboss.item.NativeLifecycleItemData.nativeDamageable(stack));
                row.put("evolutionCandidate", net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(stack));
                row.put("maxDamage", stack.getMaxDamage()); row.put("curiosRegistryContainsItem", registry.containsKey(item));
                row.put("implementsCurioItem", curioItem.isInstance(item));
                row.put("nativeRepairExactClassPolicyCurrentlyAllowsClass", item.getClass() == SwordItem.class || item.getClass() == BowItem.class
                    || item.getClass() == CrossbowItem.class || item.getClass() == TridentItem.class);
                List<Map<String, Object>> providers = new ArrayList<>();
                for (var entry : capabilities.entrySet()) {
                    Object providerSet = ((Map<?, ?>)read(entry.getValue(), "entries")).get(item);
                    if (providerSet == null) continue;
                    for (Object provider : (List<?>)read(providerSet, "members")) {
                        Map<String, Object> providerRow = new LinkedHashMap<>();
                        providerRow.put("capability", entry.getKey().toString()); providerRow.put("providerClass", provider.getClass().getName());
                        providerRow.put("originalCuriosRegistrationIdentity", entry.getKey() == originalCapability && originals.get(provider) == item);
                        providerRow.put("providerNest", provider.getClass().getNestHost().getName());
                        providerRow.put("hidden", provider.getClass().isHidden());
                        List<String> fields = new ArrayList<>();
                        for (Field field : provider.getClass().getDeclaredFields())
                            fields.add(Modifier.toString(field.getModifiers()) + " " + field.getType().getName());
                        providerRow.put("fields", fields); providers.add(providerRow);
                    }
                }
                row.put("providers", providers); rows.add(row);
            }
            if (!((NativeItemCapabilityObservation)observation).live() || beforeVersion != ((AtomicLong)read(registry, "rawMutationVersion")).get())
                throw new AssertionError("Read-only census changed provider observation or registry version");
            for (String expected : List.of("sword", "bow", "crossbow", "axe", "trident", "staff"))
                if (!familyCounts.containsKey(expected)) throw new AssertionError("Retained family absent from actual registry: " + expected);
            report.put("failures", 0); report.put("familyCounts", familyCounts); report.put("items", rows);
            report.put("actualRegisteredItemsAndOriginalProviderIdentitiesRead", true);
            report.put("actualProviderGetCapabilityInvokedByCensus", false); report.put("leaseOrRepairTaskIssued", false);
            report.put("providerObservationAndRawRegistryVersionUnchanged", true);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("sixFamilyRepairAccepted", false); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
