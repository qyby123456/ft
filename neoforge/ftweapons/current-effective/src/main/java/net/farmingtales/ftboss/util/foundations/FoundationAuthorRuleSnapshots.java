package net.farmingtales.ftboss.util.foundations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.item.ItemStack;

public final class FoundationAuthorRuleSnapshots {
    private static final int MAX_AUTHOR_SNAPSHOTS = 32;
    private static final int MAX_RULE_FINGERPRINTS = 32;
    private static final StackWalker CALLER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    private static final Map<String, FoundationAuthorRuleSnapshots.FrozenRules> AUTHORS = new LinkedHashMap<>(16, 0.75F, true);
    private static FoundationAuthorRuleSnapshots.MaterialSnapshot materialSnapshot;

    private FoundationAuthorRuleSnapshots() {
    }

    public static FoundationAuthorRuleSnapshots.MaterialSnapshot material(Map<String, Object> var0, Supplier<CompoundTag> var1) {
        if (CALLER.getCallerClass() != SwordMaterialExtensions.class) {
            throw new IllegalArgumentException("untrusted-author-rule-source");
        } else {
            String var2 = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(FoundationCanonicalDigest.object(var0)));
            synchronized (FoundationAuthorRuleSnapshots.class) {
                if (materialSnapshot != null && materialSnapshot.key.equals(var2)) {
                    return materialSnapshot;
                } else {
                    TreeMap var4 = new TreeMap(var0);
                    var4.put("FoodCatalogueResources", var1.get());
                    CompoundTag var5 = FoundationCanonicalDigest.object(var4);
                    materialSnapshot = new FoundationAuthorRuleSnapshots.MaterialSnapshot(var2, var5);
                    return materialSnapshot;
                }
            }
        }
    }

    public static void validate(ItemStack var0) {
        capture(var0);
    }

    public static CompoundTag snapshot(ItemStack var0) {
        return capture(var0).copy();
    }

    public static String fingerprint(ItemStack var0, String var1, CompoundTag var2) {
        FoundationAuthorRuleSnapshots.FrozenRules var3 = capture(var0);
        PrivateFingerprint416 var4 = new PrivateFingerprint416();
        var4.putString("Profile", var1);
        var4.put("PaidMaterialDefinitions", var2);
        String var5 = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(var4.finish()));
        synchronized (var3.fingerprints) {
            String var7 = var3.fingerprints.get(var5);
            if (var7 != null) {
                return var7;
            }
        }

        PrivateFingerprint416 var12 = new PrivateFingerprint416(var3.copy());
        var12.putString("Profile", var1);
        var12.put("PaidMaterialDefinitions", var2);
        String var13 = FoundationCanonicalDigest.fingerprint("author-rules", var12.finish());
        synchronized (var3.fingerprints) {
            var3.fingerprints.put(var5, var13);
            if (var3.fingerprints.size() > 32) {
                var3.fingerprints.remove(var3.fingerprints.keySet().iterator().next());
            }

            return var13;
        }
    }

    private static FoundationAuthorRuleSnapshots.FrozenRules capture(ItemStack var0) {
        try {
            Class var1 = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
            Class var16 = Class.forName("net.farmingtales.ftboss.api.SwordMaterialExtensions");
            Object var17 = var1.getMethod("foundationExtensionsSnapshot", ItemStack.class).invoke(null, var0);
            Object var4 = var16.getMethod("foundationRuleSnapshot").invoke(null);
            if (var17 instanceof CompoundTag var5 && var4 instanceof FoundationAuthorRuleSnapshots.MaterialSnapshot var6) {
                String var7 = var6.key + ":" + Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(var5));
                synchronized (AUTHORS) {
                    FoundationAuthorRuleSnapshots.FrozenRules var9 = AUTHORS.get(var7);
                    if (var9 != null) {
                        return var9;
                    }
                }

                PrivateFingerprint416 var18 = new PrivateFingerprint416();
                var18.put("Cavity", var5);
                var18.put("Material", var6.copy());
                CompoundTag snapshot416 = var18.finish();
                FoundationCanonicalDigest.canonical(snapshot416);
                FoundationAuthorRuleSnapshots.FrozenRules var19 = new FoundationAuthorRuleSnapshots.FrozenRules(snapshot416);
                synchronized (AUTHORS) {
                    FoundationAuthorRuleSnapshots.FrozenRules var11 = AUTHORS.get(var7);
                    if (var11 != null) {
                        return var11;
                    }

                    AUTHORS.put(var7, var19);
                    if (AUTHORS.size() > 32) {
                        AUTHORS.remove(AUTHORS.keySet().iterator().next());
                    }

                    return var19;
                }
            }

            throw new IllegalArgumentException("bad-author-rule-snapshot");
        } catch (LinkageError | ReflectiveOperationException var15) {
            Object var2 = var15 instanceof InvocationTargetException var3 ? var3.getCause() : var15;
            throw new IllegalArgumentException("unavailable-or-invalid-author-rules", (Throwable)var2);
        }
    }

    private static final class FrozenRules {
        private final CompoundTag rules;
        private final byte[] encoded;
        private final Map<String, String> fingerprints = new LinkedHashMap<>(16, 0.75F, true);

        private FrozenRules(CompoundTag var1) {
            this.rules = var1.copy();
            byte[] var2 = null;

            try {
                ByteArrayOutputStream var3 = new ByteArrayOutputStream();
                NbtIo.write(this.rules, new DataOutputStream(var3));
                byte[] var4 = var3.toByteArray();
                if (this.rules.equals(NbtIo.read(new DataInputStream(new ByteArrayInputStream(var4))))) {
                    var2 = var4;
                }
            } catch (IOException var5) {
                var2 = null;
            }

            this.encoded = var2;
        }

        private CompoundTag copy() {
            if (this.encoded != null) {
                try {
                    return NbtIo.read(new DataInputStream(new ByteArrayInputStream(this.encoded)));
                } catch (IOException var2) {
                    throw new IllegalStateException("foundation-cached-rule-copy", var2);
                }
            } else {
                return this.rules.copy();
            }
        }
    }

    public static final class MaterialSnapshot {
        private final String key;
        private final FoundationAuthorRuleSnapshots.FrozenRules rules;

        private MaterialSnapshot(String var1, CompoundTag var2) {
            this.key = var1;
            this.rules = new FoundationAuthorRuleSnapshots.FrozenRules(var2);
        }

        public CompoundTag copy() {
            return this.rules.copy();
        }
    }

    private static final class PrivateFingerprint416 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private final CompoundTag seed;
        private PrivateFingerprint416() { this.seed = null; }
        private PrivateFingerprint416(CompoundTag seed) { this.seed = seed; }
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.fingerprintCompoundForRead416(seed, fields);
        }
    }
}
