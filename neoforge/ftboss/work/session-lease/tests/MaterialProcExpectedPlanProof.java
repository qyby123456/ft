package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;

public final class MaterialProcExpectedPlanProof {
    private static int cases;
    private static int writes;
    private static int rejected;

    private static void require(boolean accepted,String name) {
        if (!accepted) throw new AssertionError(name);
    }

    private static Value image(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            List<String> keys=new ArrayList<>(compound.getAllKeys());
            keys.sort(String::compareTo);
            return FoodWeaponExpectedTrace.compound(keys.toArray(String[]::new),keys.stream().map(key->image(compound.get(key))).toArray(Value[]::new));
        }
        if (tag.getId()==8) return FoodWeaponExpectedTrace.string(tag.getAsString());
        if (tag instanceof NumericTag number) return FoodWeaponExpectedTrace.number(tag.getId(),number.getAsLong());
        throw new AssertionError("Unquoted test tag kind "+tag.getId());
    }

    private static CompoundTag fixture(Material material,int shape) {
        CompoundTag player=new CompoundTag();player.putString("unrelated-owner","preserve");
        if (shape==0) return player;
        if (shape==1) {player.putString("FTBossMaterialPowers","wrong-root-type");return player;}
        CompoundTag root=new CompoundTag();root.putString("unrelated-material","preserve");player.put("FTBossMaterialPowers",root);
        if (shape==2) return player;
        if (shape==3) {root.putString(material.id(),"wrong-timer-type");return player;}
        CompoundTag timer=new CompoundTag();timer.putString("unrelated-field","preserve");root.put(material.id(),timer);
        if (shape==4) return player;
        timer.putLong("Stamp",shape==9?-1L:1000L);
        if (shape==5) timer.putInt("Delay",0);
        if (shape==6) timer.putInt("Delay",Integer.MAX_VALUE);
        if (shape==7) timer.putLong("Delay",0L);
        if (shape==8) {timer.putInt("Stamp",1000);timer.putInt("Delay",Integer.MIN_VALUE);}
        return player;
    }

    private static boolean originalProc(CompoundTag persistent, long tick, Material var1, int var2, List<Value> observed) {
        long var3 = tick;
        CompoundTag var5 = persistent.getCompound("FTBossMaterialPowers");
        CompoundTag var6 = var5.getCompound(var1.id());
        int var7 = WeaponMaterialPowerRules.baseCooldown(var1);
        int var8 = var6.contains("Delay", 3) ? Math.max(var7 * 4 / 5, Math.min(var7, var6.getInt("Delay"))) : var7;
        if (!WeaponMaterialPowerRules.ready(var3, var6.contains("Stamp", 4), var6.getLong("Stamp"), var8)) {
            return false;
        } else {
            var6.putLong("Stamp", var3);
            observed.add(image(persistent));
            var6.putInt("Delay", WeaponMaterialPowerRules.cooldown(var1, var2));
            observed.add(image(persistent));
            var5.put(var1.id(), var6);
            observed.add(image(persistent));
            persistent.put("FTBossMaterialPowers", var5);
            observed.add(image(persistent));
            return true;
        }
    }


    public static void main(String[] arguments) {
        for (Material material:Material.values()) for (int shape=0;shape<10;shape++)
            for (int foodCount:new int[]{-1,0,1,2,30,31}) for (long tick:new long[]{-1L,0L,999L,1000L,1159L,1400L,Long.MAX_VALUE}) {
                CompoundTag actual=fixture(material,shape);
                Value before=image(actual);
                MaterialProcExpectedPlan.Plan plan=MaterialProcExpectedPlan.quote(before,material,tick,foodCount);
                require(FoodWeaponExpectedTrace.same(before,image(actual)),"Pure planner mutated original before image");
                List<Value> observed=new ArrayList<>();boolean returned=originalProc(actual,tick,material,foodCount,observed);
                require(returned==plan.fired(),"Original ready branch mismatch "+material+" shape="+shape+" tick="+tick);
                require(observed.size()==plan.writes().size(),"Exact four original writes or no original writes required");
                for (int index=0;index<observed.size();index++) {
                    require(FoodWeaponExpectedTrace.same(observed.get(index),plan.writes().get(index).playerAfter()),"Exact intermediate player image differs at write "+index+" shape="+shape);
                    writes++;
                }
                require(FoodWeaponExpectedTrace.same(image(actual),plan.playerAfter()),"Complete original player image mismatch");
                require(plan.playerBefore()==before,"Planner replaced immutable before identity");
                if (!returned) {rejected++;require(plan.playerAfter()==before,"No-write branch must retain exact before identity");}
                cases++;
            }
        try {MaterialProcExpectedPlan.quote(FoodWeaponExpectedTrace.string("wrong"),Material.values()[0],0L,0);throw new AssertionError("Malformed root accepted");}
        catch(IllegalArgumentException expected) {cases++;}
        System.out.println("{\"failures\":0,\"oracleCases\":"+cases+",\"exactIntermediateWrites\":"+writes+",\"noWriteBranches\":"+rejected+",\"productGraphChanged\":false,\"nativeSessionIntegrationAccepted\":false}");
    }
}
