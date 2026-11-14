package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.util.SparrowAppearanceRules;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class ManorFightData extends SavedData {
    private ListTag active = new ListTag();
    private ListTag deaths = new ListTag();
    private final Set<UUID> retired = new HashSet<>();
    private final Set<UUID> sparrowVisits = new HashSet<>();
    private final Map<UUID, Integer> sparrowPlans = new HashMap<>();
    private final Map<UUID, Integer> sparrowStages = new HashMap<>();
    private static final Factory<ManorFightData> FACTORY = new Factory<>(ManorFightData::new, ManorFightData::load, DataFixTypes.LEVEL);

    public static ManorFightData get(MinecraftServer var0) {
        return var0.overworld().getDataStorage().computeIfAbsent(FACTORY, "ftboss_manor_fights");
    }

    private static ManorFightData load(CompoundTag var0, Provider var1) {
        ManorFightData var2 = new ManorFightData();
        var2.active = var0.getList("Active", 10).copy();
        var2.deaths = var0.getList("Deaths", 10).copy();

        for (Tag var4 : var0.getList("Retired", 10)) {
            CompoundTag var5 = (CompoundTag)var4;
            if (var5.hasUUID("Id")) {
                var2.retired.add(var5.getUUID("Id"));
            }
        }

        for (Tag var11 : var0.getList("SparrowVisits", 10)) {
            CompoundTag var13 = (CompoundTag)var11;
            if (var13.hasUUID("Id") && !var2.retired(var13.getUUID("Id"))) {
                var2.sparrowVisits.add(var13.getUUID("Id"));
            }
        }

        for (Tag var12 : var0.getList("SparrowRounds", 10)) {
            CompoundTag var14 = (CompoundTag)var12;
            if (var14.hasUUID("Id") && !var2.retired(var14.getUUID("Id"))) {
                UUID var6 = var14.getUUID("Id");
                int var7 = var14.getInt("Visited") & 3583;
                int var8 = var14.getInt("Planned") & 3583;
                if (Integer.bitCount(var8) < 4) {
                    var8 = SparrowAppearanceRules.plan(var6);
                }

                var2.sparrowPlans.put(var6, var8 | var7);
                var2.sparrowStages.put(var6, var7);
            }
        }

        return var2;
    }

    public ListTag active() {
        return this.active.copy();
    }

    public ListTag deaths() {
        return this.deaths.copy();
    }

    public void storeDeaths(ListTag var1) {
        this.deaths = var1.copy();
        this.setDirty();
    }

    public void store(ListTag var1) {
        this.active = var1.copy();
        this.setDirty();
    }

    public boolean retired(UUID var1) {
        return var1 != null && this.retired.contains(var1);
    }

    public boolean sparrowVisited(UUID var1) {
        return var1 != null && this.sparrowVisits.contains(var1);
    }

    public void visitSparrow(UUID var1) {
        if (var1 != null && !this.retired(var1) && this.sparrowVisits.add(var1)) {
            this.setDirty();
        }
    }

    public int sparrowPlan(UUID var1) {
        if (var1 != null && !this.retired(var1)) {
            if (!this.sparrowPlans.containsKey(var1)) {
                this.sparrowPlans.put(var1, SparrowAppearanceRules.plan(var1));
                this.setDirty();
            }

            return this.sparrowPlans.get(var1);
        } else {
            return 0;
        }
    }

    public boolean sparrowPlanned(UUID var1, int var2) {
        return (this.sparrowPlan(var1) & SparrowAppearanceRules.bit(var2)) != 0;
    }

    public boolean sparrowVisited(UUID var1, int var2) {
        return (this.sparrowStages.getOrDefault(var1, 0) & SparrowAppearanceRules.bit(var2)) != 0;
    }

    public void visitSparrow(UUID var1, int var2) {
        int var3 = SparrowAppearanceRules.bit(var2);
        if (var1 != null && !this.retired(var1) && var3 != 0 && !this.sparrowVisited(var1, var2)) {
            this.sparrowPlans.put(var1, this.sparrowPlan(var1) | var3);
            this.sparrowStages.merge(var1, var3, (var0, var1x) -> var0 | var1x);
            this.visitSparrow(var1);
            this.setDirty();
        }
    }

    public void retire(UUID var1) {
        this.retired.add(var1);
        this.sparrowVisits.remove(var1);
        this.sparrowPlans.remove(var1);
        this.sparrowStages.remove(var1);
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag var1, Provider var2) {
        var1.put("Active", this.active.copy());
        var1.put("Deaths", this.deaths.copy());
        ListTag var3 = new ListTag();

        for (UUID var5 : this.retired) {
            CompoundTag var6 = new CompoundTag();
            var6.putUUID("Id", var5);
            var3.add(var6);
        }

        var1.put("Retired", var3);
        ListTag var9 = new ListTag();

        for (UUID var12 : this.sparrowVisits) {
            CompoundTag var7 = new CompoundTag();
            var7.putUUID("Id", var12);
            var9.add(var7);
        }

        var1.put("SparrowVisits", var9);
        ListTag var11 = new ListTag();

        for (Entry var14 : this.sparrowPlans.entrySet()) {
            CompoundTag var8 = new CompoundTag();
            var8.putUUID("Id", (UUID)var14.getKey());
            var8.putInt("Planned", (Integer)var14.getValue());
            var8.putInt("Visited", this.sparrowStages.getOrDefault(var14.getKey(), 0));
            var11.add(var8);
        }

        var1.put("SparrowRounds", var11);
        return var1;
    }
}
