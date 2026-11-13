package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;

final class MaterialProcExpectedPlan {
    static final String ROOT="FTBossMaterialPowers";
    record Write(String receiver,String key,Value argument,Value playerBefore,Value playerAfter) {}
    record Plan(Material material,long tick,int foodCount,Value playerBefore,List<Write> writes) {
        Plan { writes=List.copyOf(writes); }
        boolean fired() { return !writes.isEmpty(); }
        Value playerAfter() { return writes.isEmpty()?playerBefore:writes.getLast().playerAfter(); }
    }

    private MaterialProcExpectedPlan() {}

    private static Value compound(Value value) {
        return value!=null && value.kind==10?value:FoodWeaponExpectedTrace.emptyCompound();
    }

    static Plan quote(Value player,Material material,long tick,int foodCount) {
        if (player==null || player.kind!=10 || material==null) throw new IllegalArgumentException("Immutable original player image and selected material required");
        Value storedRoot=FoodWeaponExpectedTrace.field(player,ROOT);
        Value root=compound(storedRoot);
        Value storedTimer=FoodWeaponExpectedTrace.field(root,material.id());
        Value timer=compound(storedTimer);
        Value delay=FoodWeaponExpectedTrace.field(timer,"Delay");
        Value stamp=FoodWeaponExpectedTrace.field(timer,"Stamp");
        int base=WeaponMaterialPowerRules.baseCooldown(material);
        int cooldown=delay!=null && delay.kind==3?Math.max(base*4/5,Math.min(base,(int)delay.bits)):base;
        boolean ready=WeaponMaterialPowerRules.ready(tick,stamp!=null && stamp.kind==4,stamp!=null && stamp.kind==4?stamp.bits:0L,cooldown);
        if (!ready) return new Plan(material,tick,foodCount,player,List.of());
        Value stampValue=FoodWeaponExpectedTrace.number(4,tick);
        Value delayValue=FoodWeaponExpectedTrace.number(3,WeaponMaterialPowerRules.cooldown(material,foodCount));
        Value stamped=FoodWeaponExpectedTrace.put(timer,"Stamp",stampValue);
        Value completed=FoodWeaponExpectedTrace.put(stamped,"Delay",delayValue);
        boolean rootAttached=storedRoot!=null && storedRoot.kind==10;
        boolean timerAttached=rootAttached && storedTimer!=null && storedTimer.kind==10;
        Value afterStamp=timerAttached?FoodWeaponExpectedTrace.put(player,ROOT,FoodWeaponExpectedTrace.put(root,material.id(),stamped)):player;
        Value afterDelay=timerAttached?FoodWeaponExpectedTrace.put(player,ROOT,FoodWeaponExpectedTrace.put(root,material.id(),completed)):afterStamp;
        Value insertedRoot=FoodWeaponExpectedTrace.put(root,material.id(),completed);
        Value afterInsert=rootAttached?FoodWeaponExpectedTrace.put(player,ROOT,insertedRoot):afterDelay;
        Value afterRoot=FoodWeaponExpectedTrace.put(player,ROOT,insertedRoot);
        return new Plan(material,tick,foodCount,player,List.of(
            new Write("material-buffer","Stamp",stampValue,player,afterStamp),
            new Write("material-buffer","Delay",delayValue,afterStamp,afterDelay),
            new Write("root-buffer",material.id(),completed,afterDelay,afterInsert),
            new Write("player-root",ROOT,insertedRoot,afterInsert,afterRoot)));
    }
}
