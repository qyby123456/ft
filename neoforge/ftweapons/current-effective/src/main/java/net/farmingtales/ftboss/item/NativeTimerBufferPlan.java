package net.farmingtales.ftboss.item;

import java.util.IdentityHashMap;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class NativeTimerBufferPlan {
    private final Thread thread = Thread.currentThread();
    private final ItemStack source;
    private final NativeItemComponentObservation physicalBefore;
    private final CompoundTag physicalRoot;
    private final Value expectedReturned;
    private final IdentityHashMap<Object, Boolean> physicalNodes;

    private NativeTimerBufferPlan(ItemStack source, FoodWeaponEvolution.Profile profile,
            NativeItemComponentObservation physicalBefore, CompoundTag physicalRoot, IdentityHashMap<Object, Boolean> physicalNodes) {
        this.source = source;
        this.physicalBefore = physicalBefore;
        this.physicalRoot = physicalRoot;
        CustomData data = source.get(DataComponents.CUSTOM_DATA);
        Value publicRoot = data == null ? FoodWeaponExpectedTrace.emptyCompound() : NativeExpectedRawFields.observe(data.getUnsafe());
        Value child = FoodWeaponExpectedTrace.field(publicRoot, "FTBossFoodEvolution");
        Value result = child != null && child.kind == FoodWeaponExpectedTrace.COMPOUND ? child : FoodWeaponExpectedTrace.emptyCompound();
        String profileId = FoodWeaponEvolution.planHitTimerProfile(profile);
        Value existingProfile = FoodWeaponExpectedTrace.field(result, "Profile");
        if (profileId != null && (existingProfile == null || existingProfile.kind != FoodWeaponExpectedTrace.STRING)) {
            result = FoodWeaponExpectedTrace.put(result, "Profile", FoodWeaponExpectedTrace.string(profileId));
        }
        this.expectedReturned = result;
        this.physicalNodes = new IdentityHashMap<>(physicalNodes);
        if (!physicalBefore.matches(source)) throw new IllegalStateException("Native timer source changed while planning writableData");
    }

    static NativeTimerBufferPlan prepare(ItemStack source, FoodWeaponEvolution.Profile profile,
            NativeItemComponentObservation physicalBefore, CompoundTag physicalRoot, IdentityHashMap<Object, Boolean> physicalNodes) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeExpectedFieldObservation.class) {
            throw new IllegalStateException("Native timer buffer plans require the actual field observer");
        }
        if (source == null || physicalBefore == null) throw new IllegalArgumentException("Actual full native source BEFORE required");
        return new NativeTimerBufferPlan(source, profile, physicalBefore, physicalRoot, physicalNodes);
    }

    boolean matches(CompoundTag returned, NativeNbtGraphObservation returnedGraph, CompoundTag physicalRoot, IdentityHashMap<Object, Boolean> detached) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != NativeExpectedFieldObservation.class) {
            throw new IllegalStateException("Native timer buffer comparison requires the actual field observer");
        }
        if (this.thread != Thread.currentThread() || this.physicalRoot != physicalRoot || !this.physicalBefore.matches(this.source)
                || returned == null || !returnedGraph.matches(returned)
                || !FoodWeaponExpectedTrace.same(this.expectedReturned, NativeExpectedRawFields.observe(returned))) return false;
        if (detached == null) return false;
        for (Object node : detached.keySet()) if (this.physicalNodes.containsKey(node)) return false;
        return this.physicalBefore.matches(this.source) && returnedGraph.matches(returned);
    }
}
