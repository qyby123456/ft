package net.farmingtales.ftboss.util.foundations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.item.ItemStack;

/** 作者规则快照；2026-10-20 按完整动态规则值缓存静态目录校验，不缓存武器、权限或交易结果。 */
public final class FoundationAuthorRuleSnapshots {
    private static final int MAX_AUTHOR_SNAPSHOTS = 32;
    private static final int MAX_RULE_FINGERPRINTS = 32;
    private static final StackWalker CALLER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Map<String, FrozenRules> AUTHORS = new LinkedHashMap<>(16, 0.75f, true);
    private static MaterialSnapshot materialSnapshot;
    private static Map<String, Object> materialRules;

    private FoundationAuthorRuleSnapshots() {}

    public static final class MaterialSnapshot {
        private final String key;
        private final FrozenRules rules;

        private MaterialSnapshot(String key, CompoundTag rules) {
            this.key = key;
            this.rules = new FrozenRules(rules);
        }

        public CompoundTag copy() {
            return rules.copy();
        }
    }

    private static final class FrozenRules {
        private final CompoundTag rules;
        private final byte[] encoded;
        private final Map<String, String> fingerprints = new LinkedHashMap<>(16, 0.75f, true);

        private FrozenRules(CompoundTag source) {
            rules = source.copy();
            byte[] candidate = null;
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                NbtIo.write(rules, new DataOutputStream(bytes));
                byte[] serialized = bytes.toByteArray();
                if (rules.equals(NbtIo.read(new DataInputStream(new ByteArrayInputStream(serialized))))) candidate = serialized;
            } catch (IOException rejected) {
                candidate = null;
            }
            encoded = candidate;
        }

        private CompoundTag copy() {
            if (encoded != null) {
                try {
                    return NbtIo.read(new DataInputStream(new ByteArrayInputStream(encoded)));
                } catch (IOException rejected) {
                    throw new IllegalStateException("foundation-cached-rule-copy", rejected);
                }
            }
            return rules.copy();
        }
    }

    public static MaterialSnapshot material(Map<String, Object> dynamicRules, Supplier<CompoundTag> catalogue) {
        if (CALLER.getCallerClass() != net.farmingtales.ftboss.api.SwordMaterialExtensions.class) {
            throw new IllegalArgumentException("untrusted-author-rule-source");
        }
        synchronized (FoundationAuthorRuleSnapshots.class) {
            if (materialSnapshot != null && dynamicRules.equals(materialRules)) return materialSnapshot;
            String key = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(FoundationCanonicalDigest.object(dynamicRules)));
            Map<String, Object> complete = new java.util.TreeMap<>(dynamicRules);
            complete.put("FoodCatalogueResources", catalogue.get());
            CompoundTag rules = FoundationCanonicalDigest.object(complete);
            materialSnapshot = new MaterialSnapshot(key, rules);
            materialRules = Map.copyOf(dynamicRules);
            return materialSnapshot;
        }
    }

    public static void validate(ItemStack weapon) {
        capture(weapon);
    }

    public static CompoundTag snapshot(ItemStack weapon) {
        return capture(weapon).copy();
    }

    public static String fingerprint(ItemStack weapon, String profile, CompoundTag paidDefinitions) {
        FrozenRules author = capture(weapon);
        CompoundTag dynamic = new CompoundTag();
        dynamic.putString("Profile", profile);
        dynamic.put("PaidMaterialDefinitions", paidDefinitions);
        String key = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(dynamic));
        synchronized (author.fingerprints) {
            String cached = author.fingerprints.get(key);
            if (cached != null) return cached;
        }
        CompoundTag rules = author.copy();
        rules.putString("Profile", profile);
        rules.put("PaidMaterialDefinitions", paidDefinitions);
        String fingerprint = FoundationCanonicalDigest.fingerprint("author-rules", rules);
        synchronized (author.fingerprints) {
            author.fingerprints.put(key, fingerprint);
            if (author.fingerprints.size() > MAX_RULE_FINGERPRINTS) author.fingerprints.remove(author.fingerprints.keySet().iterator().next());
        }
        return fingerprint;
    }

    private static FrozenRules capture(ItemStack weapon) {
        try {
            Class<?> cavityApi = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
            Class<?> materialApi = Class.forName("net.farmingtales.ftboss.api.SwordMaterialExtensions");
            Object cavityValue = cavityApi.getMethod("foundationExtensionsSnapshot", ItemStack.class).invoke(null, weapon);
            Object materialValue = materialApi.getMethod("foundationRuleSnapshot").invoke(null);
            if (!(cavityValue instanceof CompoundTag cavity) || !(materialValue instanceof MaterialSnapshot material)) {
                throw new IllegalArgumentException("bad-author-rule-snapshot");
            }
            String key = material.key + ":" + Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical(cavity));
            synchronized (AUTHORS) {
                FrozenRules cached = AUTHORS.get(key);
                if (cached != null) return cached;
            }
            CompoundTag rules = new CompoundTag();
            rules.put("Cavity", cavity);
            rules.put("Material", material.copy());
            FoundationCanonicalDigest.canonical(rules);
            FrozenRules created = new FrozenRules(rules);
            synchronized (AUTHORS) {
                FrozenRules cached = AUTHORS.get(key);
                if (cached != null) return cached;
                AUTHORS.put(key, created);
                if (AUTHORS.size() > MAX_AUTHOR_SNAPSHOTS) AUTHORS.remove(AUTHORS.keySet().iterator().next());
                return created;
            }
        } catch (ReflectiveOperationException | LinkageError rejected) {
            Throwable cause = rejected instanceof InvocationTargetException invocation ? invocation.getCause() : rejected;
            throw new IllegalArgumentException("unavailable-or-invalid-author-rules", cause);
        }
    }
}
