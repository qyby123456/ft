package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started);
    }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static Map<String, Object> capture(Class<?> actualClass) throws Exception {
        Path root = Path.of(System.getProperty("ftboss.observe.definition.dir")).toRealPath();
        List<Map<String, Object>> matching = new ArrayList<>();
        try (var paths = Files.list(root)) {
            for (Path metadata : paths.filter(path -> path.getFileName().toString().matches("capture-[0-9]+\\.xml")).toList()) {
                Properties values = new Properties();
                try (var input = Files.newInputStream(metadata)) { values.loadFromXML(input); }
                if (!actualClass.getName().replace('.', '/').equals(values.getProperty("internalName"))) continue;
                if (!Integer.toString(System.identityHashCode(actualClass.getClassLoader())).equals(values.getProperty("loaderIdentity"))) continue;
                if (!"true".equals(values.getProperty("soleApplicableJavaTransformer")) || !"false".equals(values.getProperty("redefinition"))
                    || !"false".equals(values.getProperty("returnedReplacementBytes"))
                    || !actualClass.getClassLoader().getClass().getName().equals(values.getProperty("loaderClass"))
                    || !String.valueOf(actualClass.getModule().getName()).equals(values.getProperty("module")))
                    throw new AssertionError("JVM definition identity/transformer inventory mismatch " + actualClass.getName());
                Path bytes = Path.of(values.getProperty("classFile")).toRealPath();
                if (!bytes.startsWith(root) || !values.getProperty("sha256").equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(bytes)))))
                    throw new AssertionError("JVM definition byte snapshot mismatch " + actualClass.getName());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("class", actualClass.getName()); row.put("metadata", metadata.toString()); row.put("bytes", bytes.toString());
                row.put("sha256", values.getProperty("sha256")); row.put("loaderIdentity", values.getProperty("loaderIdentity"));
                row.put("loaderClass", values.getProperty("loaderClass")); row.put("module", values.getProperty("module"));
                row.put("codeSource", values.getProperty("codeSource"));
                matching.add(row);
            }
        }
        if (matching.size() != 1) throw new AssertionError("Expected exactly one actual definition capture " + actualClass.getName() + " found " + matching.size());
        return matching.get(0);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        try {
            var server = event.getServer();
            ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(new UUID(334, 1), "ft317_0"), ClientInformation.createDefault());
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            if (storage.load(player).isEmpty()) throw new AssertionError("Original paid weapon reload did not run");
            var actual = player.getInventory().getItem(0);
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            Object lease = leaseMethod.invoke(null, actual);
            Object generation = generationMethod.invoke(null, actual);
            if (lease == null || generation == null) throw new AssertionError("Actual factory provenance test has no genuine paid lease/generation");
            Object observation = read(generation, "nativeCapabilities");
            Class<?> observationType = observation.getClass();
            Class<?> curios = Class.forName("top.theillusivec4.curios.Curios");
            Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks");
            Class<?> curioItem = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            Object capability = Class.forName("top.theillusivec4.curios.api.CuriosCapability").getField("ITEM").get(null);
            Object capabilityImage = ((Map<?, ?>)read(observation, "capabilities")).get(capability);
            if (capabilityImage == null) throw new AssertionError("Actual original Curios item capability is absent");
            Object itemProviders = ((Map<?, ?>)read(capabilityImage, "entries")).get(actual.getItem());
            List<?> providers = (List<?>)read(itemProviders, "members");
            if (providers.size() != 1) throw new AssertionError("Expected exactly one actual original Curios provider");
            Object provider = providers.get(0);
            Field[] capturedFields = provider.getClass().getDeclaredFields();
            if (!provider.getClass().isHidden() || provider.getClass().getNestHost() != curios || capturedFields.length != 1
                || capturedFields[0].getType() != net.minecraft.world.item.Item.class || !Modifier.isFinal(capturedFields[0].getModifiers())
                || Modifier.isStatic(capturedFields[0].getModifiers())) throw new AssertionError("Actual Curios hidden factory shape differs from original provenance");
            capturedFields[0].setAccessible(true);
            if (capturedFields[0].get(provider) != actual.getItem() || readStatic(observationType, "originalCurioCapability") != capability
                || ((Map<?, ?>)readStatic(observationType, "ORIGINAL_CURIO_PROVIDERS")).get(provider) != actual.getItem())
                throw new AssertionError("Original registration provenance does not bind this actual item/provider identity");
            Object registry = readStatic(hooks, "REGISTRY");
            if (registry.getClass() != Class.forName("net.farmingtales.ftboss.item.NativeCuriosProviderFields260")
                || ((Map<?, ?>)registry).containsKey(actual.getItem()) || curioItem.isInstance(actual.getItem()))
                throw new AssertionError("Paid item is not the original inert Curios registry case");
            long versionBefore = ((java.util.concurrent.atomic.AtomicLong)read(registry, "rawMutationVersion")).get();
            Object result = ((net.neoforged.neoforge.capabilities.ICapabilityProvider)provider).getCapability(actual, null);
            boolean live = (boolean)observationType.getMethod("live").invoke(observation);
            Object input = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
            if (result != null || !live || leaseMethod.invoke(null, actual) != lease || generationMethod.invoke(null, actual) != generation
                || input == null || Boolean.TRUE.equals(read(input, "retired"))
                || versionBefore != ((java.util.concurrent.atomic.AtomicLong)read(registry, "rawMutationVersion")).get())
                throw new AssertionError("Actual original inert factory call changed authority, registry or returned a custom capability");
            Map<String, Object> captures = new LinkedHashMap<>();
            for (Class<?> type : List.of(curios, hooks, net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class,
                PlayerDataStorage.class, net.minecraft.world.entity.player.Inventory.class, observationType,
                registry.getClass(), generation.getClass().getEnclosingClass())) captures.put(type.getName(), capture(type));
            report.put("failures", 0); report.put("actualOriginalLoadHasNonNullPaidLeaseAndGeneration", true);
            report.put("actualOriginalRegistrationProvenanceBindsProviderItemCapability", true);
            report.put("actualHiddenFactoryCapturesExactItemAndOriginalCuriosNest", true);
            report.put("actualAbsentRegistryAndNonCurioItemFactoryReturnedNull", true);
            report.put("actualFactoryCallPreservedLiveLeaseGenerationInputAndRegistryVersion", true);
            report.put("actualProviderClass", provider.getClass().getName()); report.put("jvmDefinitionCaptures", captures);
            report.put("postMixinExportUsedAsDefinitionBytes", false); report.put("rawJarUsedAsDefinitionBytes", false);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
