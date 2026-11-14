package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.transaction.TransactionAdmission;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponSkillLoadout {
    public static final String CARRIED = "FTBossCarriedEvolutionSkills";
    private static final int RECONCILE_RETRY_TICKS = 20;
    private static final Map<ServerPlayer, Integer> FAILED_RECONCILE = new WeakHashMap<>();

    private FoodWeaponSkillLoadout() {
    }

    public static boolean canBind(ItemStack var0) {
        if (!var0.isEmpty() && FoodWeaponEvolution.isEvolutionCandidate(var0) && !FoodWeaponEvolution.isNativeWeapon(var0)) {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
            return var1 == null || Aspect.fromId(var1.getString("Profile")) == null;
        } else {
            return false;
        }
    }

    public static boolean bindFromUi(ServerPlayer var0, int var1) {
        if (var0.isAlive() && !var0.isSpectator()) {
            ItemStack var2 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            String var3 = FoodWeaponEvolution.profileId(var1);
            if (!var3.isEmpty() && canBind(var2)) {
                FoodWeaponData.edit(var2, "FTBossFoodEvolution", data -> data.putString("Profile", var3));
                FoodWeaponLifecycle.ensureTracked(var2);
                NativeFoodWeaponWear.manage(var0, var2);
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.attuned", var2.getHoverName(), Component.translatable("profile.ftboss.evolution." + var3))
                );
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static List<WeaponDetailView> skills(ItemStack var0) {
        LinkedHashMap<Integer, WeaponDetailView> var1 = new LinkedHashMap<>();

        for (WeaponDetailView var3 : FoodWeaponEvolution.weaponDetailViews(var0)) {
            if (var3.selectable() && var3.gourmetSkillIndex() < 8) {
                var1.putIfAbsent(Integer.valueOf(var3.gourmetSkillIndex()), var3);
            }
        }

        return List.copyOf(var1.values());
    }

    public static List<Integer> carried(ItemStack var0) {
        CustomData customData = var0.get(DataComponents.CUSTOM_DATA);
        CompoundTag var1 = customData == null ? null : customData.copyTag();
        int[] var2 = var1 != null && var1.contains("FTBossCarriedEvolutionSkills", 11) ? var1.getIntArray("FTBossCarriedEvolutionSkills") : null;
        return WeaponSkillLoadoutRules.carried(FoodWeaponEvolution.evolutionLevel(var0), var2);
    }

    public static List<WeaponDetailView> carriedSkills(ItemStack var0) {
        List var1 = carried(var0);
        return skills(var0).stream().filter(var1x -> var1.contains(var1x.gourmetSkillIndex())).toList();
    }

    public static int activeIndex(ItemStack var0) {
        return FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)
            ? FoodWeaponEvolution.activeGourmetSkillIndex(var0)
            : FoodWeaponProgression.selectedStage(var0);
    }

    private static void save(ItemStack var0, List<Integer> var1) {
        CustomData.update(DataComponents.CUSTOM_DATA, var0, data -> data.putIntArray("FTBossCarriedEvolutionSkills", var1.stream().mapToInt(Integer::intValue).toArray()));
    }

    private static boolean prune(ItemStack var0) {
        CustomData customData = var0.get(DataComponents.CUSTOM_DATA);
        CompoundTag var1 = customData == null ? null : customData.copyTag();
        if (var1 != null && var1.contains("FTBossCarriedEvolutionSkills", 11) && FoodWeaponEvolution.isEvolutionCandidate(var0)) {
            int var2 = Math.min(8, FoodWeaponEvolution.evolutionLevel(var0));
            int[] var3 = var1.getIntArray("FTBossCarriedEvolutionSkills");
            boolean var4 = var3.length > 0 || var2 == 0;
            int var5 = -1;

            for (int var9 : var3) {
                if (var9 <= var5 || var9 < 0 || var9 >= var2) {
                    var4 = false;
                    break;
                }

                var5 = var9;
            }

            if (var4) {
                return false;
            } else {
                save(var0, WeaponSkillLoadoutRules.carried(var2, var3));
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean select(ServerPlayer var0, int var1, boolean var2) {
        if (var0.isAlive() && !var0.isSpectator()) {
            ItemStack var3 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            if (skills(var3).stream().noneMatch(var1x -> var1x.gourmetSkillIndex() == var1)) {
                return false;
            } else {
                ArrayList<Integer> var4 = new ArrayList<>(carried(var3));
                if (var2 && !var4.contains(var1)) {
                    return false;
                } else if (!FoodWeaponEvolution.selectGourmetSkillFromUi(var0, var1)) {
                    return false;
                } else {
                    if (!var4.contains(var1)) {
                        var4.add(var1);
                        var4.sort(Integer::compareTo);
                        save(var3, var4);
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean toggleFromUi(ServerPlayer var0, int var1) {
        if (var0.isAlive() && !var0.isSpectator()) {
            ItemStack var2 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            if (skills(var2).stream().noneMatch(var1x -> var1x.gourmetSkillIndex() == var1)) {
                return false;
            } else {
                List<Integer> var3 = carried(var2);
                List<Integer> var4 = WeaponSkillLoadoutRules.toggle(FoodWeaponEvolution.evolutionLevel(var2), var3.stream().mapToInt(Integer::intValue).toArray(), var1);
                if (var3.equals(var4)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.skills.keep_one"), true);
                    return false;
                } else {
                    int var5 = var4.contains(activeIndex(var2)) ? activeIndex(var2) : (Integer)var4.get(0);
                    if (!FoodWeaponEvolution.selectGourmetSkillFromUi(var0, var5)) {
                        return false;
                    } else {
                        save(var2, var4);
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public static void reconcile(Post var0) {
        if (var0.getEntity() instanceof ServerPlayer var1 && var1.isAlive() && !var1.isSpectator()) {
            boolean var6 = false;

            for (int var3 = 0; var3 < var1.getInventory().getContainerSize(); var3++) {
                var6 |= prune(var1.getInventory().getItem(var3));
            }

            if (var6) {
                var1.getInventory().setChanged();
                var1.inventoryMenu.broadcastChanges();
                var1.containerMenu.broadcastChanges();
            }

            ItemStack var7 = var1.getMainHandItem();
            CustomData reconcileData = var7.get(DataComponents.CUSTOM_DATA);
            if (reconcileData != null && reconcileData.copyTag().contains("FTBossCarriedEvolutionSkills", 11)) {
                List var4 = carried(var7);
                if (!var4.isEmpty() && !var4.contains(activeIndex(var7))) {
                    Integer var5 = FAILED_RECONCILE.get(var1);
                    if (var5 == null || var1.tickCount - var5 >= 20) {
                        if (FoodWeaponEvolution.selectGourmetSkillFromUi(var1, (Integer)var4.get(0))) {
                            FAILED_RECONCILE.remove(var1);
                            var1.getInventory().setChanged();
                            var1.inventoryMenu.broadcastChanges();
                            var1.containerMenu.broadcastChanges();
                        } else {
                            FAILED_RECONCILE.put(var1, var1.tickCount);
                        }
                    }
                }
            }
        }
    }

    public static boolean select(ServerPlayer var0, ItemStack var1, int var2, boolean var3) {
        if (FourWeaponFoundationAdapter.applies(var1) && !TransactionAdmission.permits(var0, var1, Operation.SELECT_SKILL)) {
            return false;
        } else if (var0.isAlive() && !var0.isSpectator() && (var1 == var0.getMainHandItem() || var1 == var0.getOffhandItem())) {
            if (skills(var1).stream().noneMatch(var1x -> var1x.gourmetSkillIndex() == var2)) {
                return false;
            } else {
                ArrayList<Integer> var4 = new ArrayList<>(carried(var1));
                if (var3 && !var4.contains(var2)) {
                    return false;
                } else if (!FoodWeaponEvolution.selectGourmetSkillFromUi(var0, var1, var2)) {
                    return false;
                } else {
                    if (!var4.contains(var2)) {
                        var4.add(var2);
                        var4.sort(Integer::compareTo);
                        save(var1, var4);
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }
}
