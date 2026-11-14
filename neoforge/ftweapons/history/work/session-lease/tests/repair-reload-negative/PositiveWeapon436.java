package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
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

    private static void mutate(CompoundTag player, String mutation) {
        ListTag inventory = player.getList("Inventory", 10);
        CompoundTag entry = inventory.getCompound(0);
        CompoundTag ticket = entry.getCompound("FTBossRepairContinuation");
        switch (mutation) {
            case "save" -> ticket.putUUID("Save", UUID.randomUUID());
            case "owner" -> ticket.putUUID("Owner", UUID.randomUUID());
            case "weapon" -> ticket.putUUID("Weapon", UUID.randomUUID());
            case "identity" -> ticket.putUUID("NativeIdentity", UUID.randomUUID());
            case "epoch" -> ticket.putUUID("LeaseEpoch", UUID.randomUUID());
            case "generation" -> ticket.putLong("LeaseGeneration", ticket.getLong("LeaseGeneration") + 1L);
            case "slot" -> ticket.putInt("Slot", 1);
            case "shape" -> ticket.putBoolean("Unissued", true);
            case "missing" -> entry.remove("FTBossRepairContinuation");
            case "wrong_type" -> entry.putInt("FTBossRepairContinuation", 1);
            case "whole_item" -> entry.getCompound("components").putInt("minecraft:damage", 1);
            case "duplicate_weapon" -> {
                CompoundTag duplicate = entry.copy();
                duplicate.putByte("Slot", (byte)1);
                duplicate.getCompound("FTBossRepairContinuation").putInt("Slot", 1);
                inventory.add(duplicate);
            }
            default -> throw new AssertionError("Unknown mutation " + mutation);
        }
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Object> cases = new ArrayList<>();
        Path playerFile = null;
        byte[] originalBytes = null;
        try {
            var server = event.getServer();
            UUID owner = new UUID(334, 1);
            GameProfile profile = new GameProfile(owner, "ft317_0");
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat");
            originalBytes = Files.readAllBytes(playerFile);
            CompoundTag original = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            CompoundTag originalTicket = original.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            CompoundTag before = (CompoundTag)readDocument.invoke(store, documentOwner, false);
            if (!before.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")) throw new AssertionError("Missing original committed save");
            Map<Path, byte[]> authorityBytes = new LinkedHashMap<>();
            try (var paths = Files.walk(server.getWorldPath(LevelResource.ROOT).resolve("ftboss-authority"))) {
                for (Path path : paths.filter(path -> path.toString().endsWith(".bin")).toList()) authorityBytes.put(path, Files.readAllBytes(path));
            }
            for (String mutation : List.of("save", "owner", "weapon", "identity", "epoch", "generation", "slot", "shape", "missing", "wrong_type", "whole_item", "duplicate_weapon")) {
                CompoundTag changed = original.copy();
                mutate(changed, mutation);
                NbtIo.writeCompressed(changed, playerFile);
                ServerPlayer rejected = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                if (storage.load(rejected).isEmpty()) throw new AssertionError("Original negative load did not run " + mutation);
                ItemStack actual = rejected.getInventory().getItem(0);
                if (actual.isEmpty() || lease.invoke(null, actual) != null || generation.invoke(null, actual) != null)
                    throw new AssertionError("Modified original save granted authority " + mutation);
                if (!before.equals(readDocument.invoke(store, documentOwner, false))) throw new AssertionError("Negative consumed valid ticket " + mutation);
                for (var entry : authorityBytes.entrySet()) {
                    if (!Arrays.equals(entry.getValue(), Files.readAllBytes(entry.getKey()))) throw new AssertionError("Negative changed signed authority bytes " + mutation);
                }
                Files.write(playerFile, originalBytes);
                cases.add(Map.of("mutation", mutation, "originalLoadRan", true, "noLeaseOrGeneration", true,
                    "committedDocumentAndAuthorityBytesUnchanged", true));
            }
            ServerPlayer restored = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
            if (storage.load(restored).isEmpty()) throw new AssertionError("Original control load missing");
            ItemStack actual = restored.getInventory().getItem(0);
            Object genuine = lease.invoke(null, actual);
            if (genuine == null || generation.invoke(null, actual) == null
                || ((Number)read(genuine, "generation")).longValue() != originalTicket.getLong("LeaseGeneration") + 1L)
                throw new AssertionError("Valid original control lost its unused successor");
            Class<?> curioItem = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            Object changedProvider = java.lang.reflect.Proxy.newProxyInstance(curioItem.getClassLoader(), new Class<?>[]{curioItem},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == arguments[0];
                    if (method.getName().equals("toString")) return "Reloaded provider mutation input";
                    if (method.getReturnType() == boolean.class) return false;
                    return null;
                });
            Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks");
            var registryField = hooks.getDeclaredField("REGISTRY"); registryField.setAccessible(true);
            var registry = (Map<Object, Object>)registryField.get(null);
            if (registry.containsKey(actual.getItem())) throw new AssertionError("Control registry not originally inert");
            registry.put(actual.getItem(), changedProvider);
            registry.remove(actual.getItem(), changedProvider);
            var locallyLive = storeType.getDeclaredMethod("locallyLive", genuine.getClass()); locallyLive.setAccessible(true);
            Object input = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
            if (Boolean.TRUE.equals(locallyLive.invoke(null, genuine)) || (input != null && !Boolean.TRUE.equals(read(input, "retired"))))
                throw new AssertionError("Raw provider ABA kept reloaded genuine authority live");
            if (!Arrays.equals(originalBytes, Files.readAllBytes(playerFile))) throw new AssertionError("Fixture changed original saved player checkpoint");
            report.put("cases", cases);
            report.put("failures", 0);
            report.put("negativeOriginalLoadCount", cases.size());
            report.put("validControlRetainedUnusedSuccessor", true);
            report.put("rawProviderAbaRetiredActualReloadedLeaseAndInput", true);
            report.put("playerFilePreservedAcrossNegativesAndRevocation", true);
            report.put("originalTicketPredecessorGeneration", originalTicket.getLong("LeaseGeneration"));
        } catch (Throwable failure) {
            report.put("cases", cases); report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            if (playerFile != null && originalBytes != null) {
                try { Files.write(playerFile, originalBytes); }
                catch (Exception failure) { report.put("failures", 1); report.put("restoreFailure", failure.toString()); }
            }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
