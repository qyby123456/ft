package net.farmingtales.ftboss.item;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.*;

public final class RangedFallback431 {
    private static final String KEY = "FTBossRangedProcTimers";

    public static void main(String[] arguments) throws Exception {
        List<Tag> variants = new ArrayList<>();
        variants.add(null);
        variants.add(ByteTag.valueOf((byte) 1));
        variants.add(ShortTag.valueOf((short) 2));
        variants.add(IntTag.valueOf(3));
        variants.add(LongTag.valueOf(4));
        variants.add(FloatTag.valueOf(5.5F));
        variants.add(DoubleTag.valueOf(6.5D));
        variants.add(new ByteArrayTag(new byte[]{1, 2}));
        variants.add(StringTag.valueOf("旧计时器"));
        variants.add(new ListTag());
        variants.add(new IntArrayTag(new int[]{3, 4}));
        variants.add(new LongArrayTag(new long[]{5, 6}));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Tag original : variants) {
            CompoundTag root = new CompoundTag();
            if (original != null) root.put(KEY, original);
            CompoundTag before = root.copy();
            CompoundTag first = root.getCompound(KEY);
            CompoundTag second = root.getCompound(KEY);
            require(first != second && first.isEmpty() && second.isEmpty(), "Native fallback is not a fresh empty compound");
            require(root.equals(before) && root.get(KEY) == original, "Native fallback attached or replaced original data");
            first.putLong("LastSkillProc.blood_feast", 80L);
            require(root.equals(before) && second.isEmpty(), "Detached timer write changed root or another fallback");
            FoodWeaponExpectedTrace.Value player = original == null ? FoodWeaponExpectedTrace.emptyCompound()
                : FoodWeaponExpectedTrace.put(FoodWeaponExpectedTrace.emptyCompound(), KEY, immutable(original));
            FoodWeaponExpectedTrace.Builder builder = new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(
                FoodWeaponExpectedTrace.emptyCompound(), player, FoodWeaponExpectedTrace.emptyCompound()));
            boolean rejected = false;
            try { builder.rangedAcquire(root, first); }
            catch (IllegalArgumentException expected) {
                require(expected.getMessage().equals("Unplanned original ranged allocation/root identity is unsupported off-cut"), "Unexpected planning failure");
                rejected = true;
            }
            require(rejected, "Current plan unexpectedly supports detached native fallback");
            FoodWeaponExpectedTrace.Operation acquireFresh = builder.rangedAcquireDetached431(root, first);
            require(acquireFresh.after.player == player && !acquireFresh.after.rangedAttached
                && acquireFresh.after.rangedBufferIdentity == first && acquireFresh.after.rangedPlayerIdentity == root
                && acquireFresh.after.rangedAcquired && FoodWeaponExpectedTrace.same(acquireFresh.after.rangedBuffer, FoodWeaponExpectedTrace.emptyCompound()),
                "Detached plan adopted observed buffer contents or changed root before put");
            FoodWeaponExpectedTrace.Operation detachedTimer = builder.rangedLong("LastSkillProc.blood_feast", 80L);
            require(detachedTimer.after.player == player && !detachedTimer.after.rangedAttached
                && FoodWeaponExpectedTrace.field(detachedTimer.after.rangedBuffer, "LastSkillProc.blood_feast").bits == 80L,
                "Detached timer plan changed original player image");
            FoodWeaponExpectedTrace.Operation attach = builder.rangedRoot();
            require(attach.after.rangedAttached && FoodWeaponExpectedTrace.same(
                FoodWeaponExpectedTrace.field(attach.after.player, KEY), detachedTimer.after.rangedBuffer), "Detached publication differs");

            root.put(KEY, first);
            require(root.getCompound(KEY) == first && root.getCompound(KEY).getLong("LastSkillProc.blood_feast") == 80L, "Native root publication failed");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("initialType", original == null ? "missing" : original.getType().getPrettyName());
            row.put("freshDetachedNativeFallback", true);
            row.put("currentIndependentPlannerRejects", true);
            row.put("nativeSaveSucceeds", true);
            rows.add(row);
        }
        CompoundTag root = new CompoundTag();
        CompoundTag stored = new CompoundTag();
        stored.putLong("LastSkillProc.blood_feast", 95L);
        root.put(KEY, stored);
        require(root.getCompound(KEY) == stored && root.getCompound(KEY) == root.getCompound(KEY), "Stored compound identity changed");
        FoodWeaponExpectedTrace.Value buffer = FoodWeaponExpectedTrace.put(FoodWeaponExpectedTrace.emptyCompound(), "LastSkillProc.blood_feast", FoodWeaponExpectedTrace.number(4, 95L));
        FoodWeaponExpectedTrace.Value player = FoodWeaponExpectedTrace.put(FoodWeaponExpectedTrace.emptyCompound(), KEY, buffer);
        FoodWeaponExpectedTrace.Builder builder = new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(FoodWeaponExpectedTrace.emptyCompound(), player, FoodWeaponExpectedTrace.emptyCompound()));
        FoodWeaponExpectedTrace.Operation acquire = builder.rangedAcquire(root, stored);
        require(acquire.after.rangedPlayerIdentity == root && acquire.after.rangedBufferIdentity == stored && acquire.after.rangedAttached, "Attached plan identities changed");
        FoodWeaponExpectedTrace.Operation timer = builder.rangedLong("LastSkillProc.blood_feast", 80L);
        require(timer.argument.bits == 95L, "Existing max timer value changed");
        builder.rangedRoot();
        int foreignRejections = 0;
        try { NativeExpectedFieldObservation.rangedFreshAllocation431(new CompoundTag(), new java.util.HashMap<>()); }
        catch (IllegalStateException expected) { foreignRejections++; }
        try { NativeExpectedFieldObservation.plannedRangedFresh431(null, null, null, null); }
        catch (IllegalStateException expected) { foreignRejections++; }
        for (String methodName : List.of("rangedFreshAllocation431", "plannedRangedFresh431")) {
            java.lang.reflect.Method method = java.util.Arrays.stream(NativeExpectedFieldObservation.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName)).findFirst().orElseThrow();
            method.setAccessible(true);
            try { method.invoke(null, new Object[method.getParameterCount()]); }
            catch (java.lang.reflect.InvocationTargetException expected) {
                require(expected.getCause() instanceof IllegalStateException, "Wrong foreign caller rejection");
                foreignRejections++;
            }
        }
        require(foreignRejections == 4, "Foreign acquisition helper caller admitted");
        int invalidIntentRejections = 0;
        FoodWeaponExpectedTrace.State attachedState = new FoodWeaponExpectedTrace.State(FoodWeaponExpectedTrace.emptyCompound(), player, FoodWeaponExpectedTrace.emptyCompound());
        try { new FoodWeaponExpectedTrace.Builder(attachedState).rangedAcquireDetached431(root, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        FoodWeaponExpectedTrace.State emptyState = new FoodWeaponExpectedTrace.State(FoodWeaponExpectedTrace.emptyCompound(), FoodWeaponExpectedTrace.emptyCompound(), FoodWeaponExpectedTrace.emptyCompound());
        try { new FoodWeaponExpectedTrace.Builder(emptyState).rangedAcquireDetached431(null, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        try { new FoodWeaponExpectedTrace.Builder(emptyState).rangedAcquireDetached431(root, null); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        FoodWeaponExpectedTrace.Builder once = new FoodWeaponExpectedTrace.Builder(emptyState);
        once.rangedAcquireDetached431(root, stored);
        try { once.rangedAcquireDetached431(root, stored); }
        catch (IllegalArgumentException expected) { invalidIntentRejections++; }
        require(invalidIntentRejections == 4, "Invalid allocation intent admitted");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("failures", 0);
        report.put("newDetachedPlanCases", 12);
        report.put("foreignCallerRejections", foreignRejections);
        report.put("invalidIntentRejections", invalidIntentRejections);

        report.put("nativeFallbackCases", rows);
        report.put("fallbackCases", rows.size());
        report.put("storedCompoundIdentityAndMaxPreserved", true);
        report.put("oldAcquireMethodSupportsDetachedFallback", false);
        report.put("productionChanged", false);
        report.put("actualMixinRuntimeExecuted", false);
        report.put("installable", false);
        Files.writeString(Path.of(arguments[0]), new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }

    private static FoodWeaponExpectedTrace.Value immutable(Tag tag) {
        return switch (tag.getId()) {
            case 1 -> FoodWeaponExpectedTrace.number(1, ((ByteTag) tag).getAsByte());
            case 2 -> FoodWeaponExpectedTrace.number(2, ((ShortTag) tag).getAsShort());
            case 3 -> FoodWeaponExpectedTrace.number(3, ((IntTag) tag).getAsInt());
            case 4 -> FoodWeaponExpectedTrace.number(4, ((LongTag) tag).getAsLong());
            case 5 -> FoodWeaponExpectedTrace.number(5, Float.floatToRawIntBits(((FloatTag) tag).getAsFloat()));
            case 6 -> FoodWeaponExpectedTrace.number(6, Double.doubleToRawLongBits(((DoubleTag) tag).getAsDouble()));
            case 7 -> FoodWeaponExpectedTrace.array(7, new long[]{1, 2});
            case 8 -> FoodWeaponExpectedTrace.string(tag.getAsString());
            case 9 -> FoodWeaponExpectedTrace.list(0, new FoodWeaponExpectedTrace.Value[0]);
            case 11 -> FoodWeaponExpectedTrace.array(11, new long[]{3, 4});
            case 12 -> FoodWeaponExpectedTrace.array(12, new long[]{5, 6});
            default -> throw new AssertionError("Unsupported fixture type");
        };
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
