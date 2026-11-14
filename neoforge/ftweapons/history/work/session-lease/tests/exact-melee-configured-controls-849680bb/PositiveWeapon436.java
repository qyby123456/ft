package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance instanceof Class<?> supplied ? supplied : instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance instanceof Class<?> ? null : instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>(); report.put("cases", cases);
        try {
            var server = event.getServer();
            var storage = (net.minecraft.world.level.storage.PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
            var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
            Class<?> semantic = Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority");
            for (int sample = 0; sample < 19; sample++) {
                ServerPlayer player = new ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(new UUID(690, sample + 1), "ft_exact_" + sample), net.minecraft.server.level.ClientInformation.createDefault());
                if (storage.load(player).isEmpty()) throw new AssertionError("Original genuine post-combat load did not run");
                ItemStack actual = player.getInventory().getItem(0);
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();
                if (!List.of("minecraft:wooden_sword", "minecraft:wooden_axe", "minecraft:stone_sword", "minecraft:stone_axe", "minecraft:golden_sword", "minecraft:golden_axe", "minecraft:diamond_sword", "minecraft:diamond_axe", "minecraft:netherite_sword", "minecraft:netherite_axe", "cataclysm:khopesh", "cataclysm:black_steel_sword", "cataclysm:black_steel_axe", "ftboss:defect_claw", "ftboss:turkey_legs", "ftboss:apple_stem", "ftboss:blooming_thorns", "bakery:rolling_pin", "bakery:bread_knife").get(sample).equals(id)) throw new AssertionError("Original exact control registry identity drift");
                Object actualLease = lease.invoke(null, actual);
                Object actualGeneration = generation.invoke(null, actual);
                if (actualLease == null || actualGeneration == null || ((Number)read(actualLease, "generation")).longValue() != 2L || Boolean.TRUE.equals(read(actualGeneration, "retired"))) throw new AssertionError("Control lacks genuine live generation two");
                CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                RuntimeException originalFailure = new RuntimeException("foreign-original-item-callback");
                int[] calls = new int[]{0};
                try {
                    if (actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class) net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalCopy(player, actual, arguments -> { calls[0]++; throw originalFailure; });
                    else net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalPost(player, actual, target, player, arguments -> { calls[0]++; throw originalFailure; });
                    throw new AssertionError("Foreign callback exception swallowed");
                } catch (RuntimeException expected) {
                    if (expected != originalFailure || calls[0] != 1 || Boolean.TRUE.equals(read(actualGeneration, "retired")) || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))) throw new AssertionError("Foreign callback exception identity or unchanged authority differs");
                }
                ItemStack unrecognizedReplacement = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("meadow:cobbled_limestone")));
                if (unrecognizedReplacement.isEmpty() || net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(unrecognizedReplacement).isPresent()
                    || net.farmingtales.ftboss.item.SwordCavityBuild.materialFor(unrecognizedReplacement) != net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material.ORIGINAL)
                    throw new AssertionError("Original unknown nonnative replacement classification changed");
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values())
                    if (net.farmingtales.ftboss.util.SwordCavityRules.accepts(slot, net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material.ORIGINAL))
                        throw new AssertionError("Original nonnative unknown material became accepted by a slot");
                int wearBefore = actual.getDamageValue();
                calls[0] = 0;
                if (actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class) {
                    ItemStack copied = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalCopy(player, actual, arguments -> { calls[0]++; return actual.copy(); });
                    if (copied == actual || lease.invoke(null, copied) != null || generation.invoke(null, copied) != null || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(copied)) || !before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))) throw new AssertionError("Foreign copy acquired authority or changed the item image");
                } else {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeMeleeOriginalPost(player, actual, target, player, arguments -> { calls[0]++; actual.postHurtEnemy(target, player); return null; });
                    if (actual.getDamageValue() != wearBefore + 2) throw new AssertionError("Original foreign axe post callback was not preserved");
                }
                if (calls[0] != 1 || !Boolean.TRUE.equals(read(actualGeneration, "retired")) || lease.invoke(null, actual) != actualLease || generation.invoke(null, actual) != actualGeneration) throw new AssertionError("Foreign original item callback retained or reissued live authority");
                for (String name : List.of("NATIVE_REPAIR_OPERATION", "NATIVE_WEAR")) if (((ThreadLocal<?>)read(semantic, name)).get() != null) throw new AssertionError("Foreign callback created semantic permission");
                for (String name : List.of("PRIVATE_NATIVE_HURT", "PRIVATE_INGRESS")) if (((ThreadLocal<?>)read(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, name)).get() != null) throw new AssertionError("Foreign callback created hurt permission");
                cases.add(Map.of("id", id, "genuineGenerationTwoBeforeControl", true, "originalForeignCallbackExactlyOnce", true, "originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation", true, "foreignOriginalMutationRetiresGenuineAuthority", true, "noPrivatePermissionOrAuthorityReissued", true, "copyControl", actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class, "unknownNonNativeMaterialStillOriginalAndAllSlotRulesReject", true));
            }
            report.put("failures", 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
