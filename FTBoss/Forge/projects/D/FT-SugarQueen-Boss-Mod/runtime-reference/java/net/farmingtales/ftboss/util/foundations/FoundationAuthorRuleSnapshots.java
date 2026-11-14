/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.util.foundations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class FoundationAuthorRuleSnapshots {
    private static final int MAX_AUTHOR_SNAPSHOTS = 32;
    private static final int MAX_RULE_FINGERPRINTS = 32;
    private static final StackWalker CALLER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Map<String, FrozenRules> AUTHORS = new LinkedHashMap<String, FrozenRules>(16, 0.75f, true);
    private static MaterialSnapshot materialSnapshot;

    private FoundationAuthorRuleSnapshots() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static MaterialSnapshot material(Map<String, Object> map, Supplier<CompoundTag> supplier) {
        if (CALLER.getCallerClass() != SwordMaterialExtensions.class) {
            throw new IllegalArgumentException("untrusted-author-rule-source");
        }
        String string = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical((Tag)FoundationCanonicalDigest.object(map)));
        Class<FoundationAuthorRuleSnapshots> clazz = FoundationAuthorRuleSnapshots.class;
        synchronized (FoundationAuthorRuleSnapshots.class) {
            if (materialSnapshot != null && FoundationAuthorRuleSnapshots.materialSnapshot.key.equals(string)) {
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return materialSnapshot;
            }
            TreeMap<String, Object> treeMap = new TreeMap<String, Object>(map);
            treeMap.put("FoodCatalogueResources", supplier.get());
            CompoundTag compoundTag = FoundationCanonicalDigest.object(treeMap);
            materialSnapshot = new MaterialSnapshot(string, compoundTag);
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return materialSnapshot;
        }
    }

    public static void validate(ItemStack itemStack) {
        FoundationAuthorRuleSnapshots.capture(itemStack);
    }

    public static CompoundTag snapshot(ItemStack itemStack) {
        return FoundationAuthorRuleSnapshots.capture(itemStack).copy();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String fingerprint(ItemStack itemStack, String string, CompoundTag compoundTag) {
        String string2;
        FrozenRules frozenRules = FoundationAuthorRuleSnapshots.capture(itemStack);
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_("Profile", string);
        compoundTag2.m_128365_("PaidMaterialDefinitions", (Tag)compoundTag);
        String string3 = Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical((Tag)compoundTag2));
        CompoundTag compoundTag3 = frozenRules.fingerprints;
        synchronized (compoundTag3) {
            string2 = frozenRules.fingerprints.get(string3);
            if (string2 != null) {
                return string2;
            }
        }
        compoundTag3 = frozenRules.copy();
        compoundTag3.m_128359_("Profile", string);
        compoundTag3.m_128365_("PaidMaterialDefinitions", (Tag)compoundTag);
        string2 = FoundationCanonicalDigest.fingerprint("author-rules", (Tag)compoundTag3);
        Map<String, String> map = frozenRules.fingerprints;
        synchronized (map) {
            frozenRules.fingerprints.put(string3, string2);
            if (frozenRules.fingerprints.size() > 32) {
                frozenRules.fingerprints.remove(frozenRules.fingerprints.keySet().iterator().next());
            }
        }
        return string2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static FrozenRules capture(ItemStack itemStack) {
        try {
            FrozenRules frozenRules;
            CompoundTag compoundTag;
            Object object;
            block17: {
                block16: {
                    Class<?> clazz = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
                    Class<?> clazz2 = Class.forName("net.farmingtales.ftboss.api.SwordMaterialExtensions");
                    Object object2 = clazz.getMethod("foundationExtensionsSnapshot", ItemStack.class).invoke(null, itemStack);
                    object = clazz2.getMethod("foundationRuleSnapshot", new Class[0]).invoke(null, new Object[0]);
                    if (!(object2 instanceof CompoundTag)) break block16;
                    compoundTag = (CompoundTag)object2;
                    if (object instanceof MaterialSnapshot) break block17;
                }
                throw new IllegalArgumentException("bad-author-rule-snapshot");
            }
            MaterialSnapshot materialSnapshot = (MaterialSnapshot)object;
            String string = materialSnapshot.key + ":" + Base64.getEncoder().encodeToString(FoundationCanonicalDigest.canonical((Tag)compoundTag));
            CompoundTag compoundTag2 = AUTHORS;
            synchronized (compoundTag2) {
                frozenRules = AUTHORS.get(string);
                if (frozenRules != null) {
                    return frozenRules;
                }
            }
            compoundTag2 = new CompoundTag();
            compoundTag2.m_128365_("Cavity", (Tag)compoundTag);
            compoundTag2.m_128365_("Material", (Tag)materialSnapshot.copy());
            FoundationCanonicalDigest.canonical((Tag)compoundTag2);
            frozenRules = new FrozenRules(compoundTag2);
            Map<String, FrozenRules> map = AUTHORS;
            synchronized (map) {
                FrozenRules frozenRules2 = AUTHORS.get(string);
                if (frozenRules2 != null) {
                    return frozenRules2;
                }
                AUTHORS.put(string, frozenRules);
                if (AUTHORS.size() > 32) {
                    AUTHORS.remove(AUTHORS.keySet().iterator().next());
                }
                return frozenRules;
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            Throwable throwable2;
            if (throwable instanceof InvocationTargetException) {
                InvocationTargetException invocationTargetException = (InvocationTargetException)throwable;
                throwable2 = invocationTargetException.getCause();
            } else {
                throwable2 = throwable;
            }
            Throwable throwable3 = throwable2;
            throw new IllegalArgumentException("unavailable-or-invalid-author-rules", throwable3);
        }
    }

    public static final class MaterialSnapshot {
        private final String key;
        private final FrozenRules rules;

        private MaterialSnapshot(String string, CompoundTag compoundTag) {
            this.key = string;
            this.rules = new FrozenRules(compoundTag);
        }

        public CompoundTag copy() {
            return this.rules.copy();
        }
    }

    private static final class FrozenRules {
        private final CompoundTag rules;
        private final byte[] encoded;
        private final Map<String, String> fingerprints = new LinkedHashMap<String, String>(16, 0.75f, true);

        private FrozenRules(CompoundTag compoundTag) {
            this.rules = compoundTag.m_6426_();
            byte[] byArray = null;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                NbtIo.m_128941_((CompoundTag)this.rules, (DataOutput)new DataOutputStream(byteArrayOutputStream));
                byte[] byArray2 = byteArrayOutputStream.toByteArray();
                if (this.rules.equals((Object)NbtIo.m_128928_((DataInput)new DataInputStream(new ByteArrayInputStream(byArray2))))) {
                    byArray = byArray2;
                }
            }
            catch (IOException iOException) {
                byArray = null;
            }
            this.encoded = byArray;
        }

        private CompoundTag copy() {
            if (this.encoded != null) {
                try {
                    return NbtIo.m_128928_((DataInput)new DataInputStream(new ByteArrayInputStream(this.encoded)));
                }
                catch (IOException iOException) {
                    throw new IllegalStateException("foundation-cached-rule-copy", iOException);
                }
            }
            return this.rules.m_6426_();
        }
    }
}

