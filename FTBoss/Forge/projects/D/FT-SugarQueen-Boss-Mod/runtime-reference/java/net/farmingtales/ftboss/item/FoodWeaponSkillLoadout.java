/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionAdmission;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponSkillLoadout {
    public static final String CARRIED = "FTBossCarriedEvolutionSkills";
    private static final int RECONCILE_RETRY_TICKS = 20;
    private static final Map<ServerPlayer, Integer> FAILED_RECONCILE = new WeakHashMap<ServerPlayer, Integer>();

    private FoodWeaponSkillLoadout() {
    }

    public static boolean canBind(ItemStack itemStack) {
        if (itemStack.m_41619_() || !FoodWeaponEvolution.isEvolutionCandidate(itemStack) || FoodWeaponEvolution.isNativeWeapon(itemStack)) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossFoodEvolution");
        return compoundTag == null || WeaponEvolutionSkillPool.Aspect.fromId(compoundTag.m_128461_("Profile")) == null;
    }

    public static boolean bindFromUi(ServerPlayer serverPlayer, int n) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        String string = FoodWeaponEvolution.profileId(n);
        if (string.isEmpty() || !FoodWeaponSkillLoadout.canBind(itemStack)) {
            return false;
        }
        itemStack.m_41698_("FTBossFoodEvolution").m_128359_("Profile", string);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.attuned", (Object[])new Object[]{itemStack.m_41786_(), Component.m_237115_((String)("profile.ftboss.evolution." + string))}));
        return true;
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> skills(ItemStack itemStack) {
        LinkedHashMap<Integer, FoodWeaponEvolution.WeaponDetailView> linkedHashMap = new LinkedHashMap<Integer, FoodWeaponEvolution.WeaponDetailView>();
        for (FoodWeaponEvolution.WeaponDetailView weaponDetailView : FoodWeaponEvolution.weaponDetailViews(itemStack)) {
            if (!weaponDetailView.selectable() || weaponDetailView.gourmetSkillIndex() >= 8) continue;
            linkedHashMap.putIfAbsent(weaponDetailView.gourmetSkillIndex(), weaponDetailView);
        }
        return List.copyOf(linkedHashMap.values());
    }

    public static List<Integer> carried(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41783_();
        int[] nArray = compoundTag != null && compoundTag.m_128425_(CARRIED, 11) ? compoundTag.m_128465_(CARRIED) : null;
        return WeaponSkillLoadoutRules.carried(FoodWeaponEvolution.evolutionLevel(itemStack), nArray);
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> carriedSkills(ItemStack itemStack) {
        List<Integer> list = FoodWeaponSkillLoadout.carried(itemStack);
        return FoodWeaponSkillLoadout.skills(itemStack).stream().filter(weaponDetailView -> list.contains(weaponDetailView.gourmetSkillIndex())).toList();
    }

    public static int activeIndex(ItemStack itemStack) {
        return FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? FoodWeaponEvolution.activeGourmetSkillIndex(itemStack) : FoodWeaponProgression.selectedStage(itemStack);
    }

    private static void save(ItemStack itemStack, List<Integer> list) {
        itemStack.m_41784_().m_128385_(CARRIED, list.stream().mapToInt(Integer::intValue).toArray());
    }

    private static boolean prune(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41783_();
        if (compoundTag == null || !compoundTag.m_128425_(CARRIED, 11) || !FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return false;
        }
        int n = Math.min(8, FoodWeaponEvolution.evolutionLevel(itemStack));
        int[] nArray = compoundTag.m_128465_(CARRIED);
        boolean bl = nArray.length > 0 || n == 0;
        int n2 = -1;
        for (int n3 : nArray) {
            if (n3 <= n2 || n3 < 0 || n3 >= n) {
                bl = false;
                break;
            }
            n2 = n3;
        }
        if (bl) {
            return false;
        }
        FoodWeaponSkillLoadout.save(itemStack, WeaponSkillLoadoutRules.carried(n, nArray));
        return true;
    }

    public static boolean select(ServerPlayer serverPlayer, int n, boolean bl) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (FoodWeaponSkillLoadout.skills(itemStack).stream().noneMatch(weaponDetailView -> weaponDetailView.gourmetSkillIndex() == n)) {
            return false;
        }
        ArrayList<Integer> arrayList = new ArrayList<Integer>(FoodWeaponSkillLoadout.carried(itemStack));
        if (bl && !arrayList.contains(n)) {
            return false;
        }
        if (!FoodWeaponEvolution.selectGourmetSkillFromUi(serverPlayer, n)) {
            return false;
        }
        if (!arrayList.contains(n)) {
            arrayList.add(n);
            arrayList.sort(Integer::compareTo);
            FoodWeaponSkillLoadout.save(itemStack, arrayList);
        }
        return true;
    }

    public static boolean toggleFromUi(ServerPlayer serverPlayer, int n) {
        int n2;
        List<Integer> list;
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (FoodWeaponSkillLoadout.skills(itemStack).stream().noneMatch(weaponDetailView -> weaponDetailView.gourmetSkillIndex() == n)) {
            return false;
        }
        List<Integer> list2 = FoodWeaponSkillLoadout.carried(itemStack);
        if (list2.equals(list = WeaponSkillLoadoutRules.toggle(FoodWeaponEvolution.evolutionLevel(itemStack), list2.stream().mapToInt(Integer::intValue).toArray(), n))) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.skills.keep_one"), true);
            return false;
        }
        int n3 = n2 = list.contains(FoodWeaponSkillLoadout.activeIndex(itemStack)) ? FoodWeaponSkillLoadout.activeIndex(itemStack) : list.get(0);
        if (!FoodWeaponEvolution.selectGourmetSkillFromUi(serverPlayer, n2)) {
            return false;
        }
        FoodWeaponSkillLoadout.save(itemStack, list);
        return true;
    }

    @SubscribeEvent
    public static void reconcile(TickEvent.PlayerTickEvent playerTickEvent) {
        ItemStack itemStack;
        ServerPlayer serverPlayer;
        Player player;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)player).m_6084_() || serverPlayer.m_5833_()) {
            return;
        }
        boolean bl = false;
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            bl |= FoodWeaponSkillLoadout.prune(serverPlayer.m_150109_().m_8020_(i));
        }
        if (bl) {
            serverPlayer.m_150109_().m_6596_();
            serverPlayer.f_36095_.m_38946_();
            serverPlayer.f_36096_.m_38946_();
        }
        if (!(itemStack = serverPlayer.m_21205_()).m_41782_() || !itemStack.m_41783_().m_128425_(CARRIED, 11)) {
            return;
        }
        List<Integer> list = FoodWeaponSkillLoadout.carried(itemStack);
        if (list.isEmpty() || list.contains(FoodWeaponSkillLoadout.activeIndex(itemStack))) {
            return;
        }
        Integer n = FAILED_RECONCILE.get(serverPlayer);
        if (n != null && serverPlayer.f_19797_ - n < 20) {
            return;
        }
        if (FoodWeaponEvolution.selectGourmetSkillFromUi(serverPlayer, list.get(0))) {
            FAILED_RECONCILE.remove(serverPlayer);
            serverPlayer.m_150109_().m_6596_();
            serverPlayer.f_36095_.m_38946_();
            serverPlayer.f_36096_.m_38946_();
        } else {
            FAILED_RECONCILE.put(serverPlayer, serverPlayer.f_19797_);
        }
    }

    public static boolean select(ServerPlayer serverPlayer, ItemStack itemStack, int n, boolean bl) {
        if (FourWeaponFoundationAdapter.applies(itemStack) && !TransactionAdmission.permits(serverPlayer, itemStack, FourWeaponTransactions.Operation.SELECT_SKILL)) {
            return false;
        }
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_()) {
            return false;
        }
        if (FoodWeaponSkillLoadout.skills(itemStack).stream().noneMatch(weaponDetailView -> weaponDetailView.gourmetSkillIndex() == n)) {
            return false;
        }
        ArrayList<Integer> arrayList = new ArrayList<Integer>(FoodWeaponSkillLoadout.carried(itemStack));
        if (bl && !arrayList.contains(n)) {
            return false;
        }
        if (!FoodWeaponEvolution.selectGourmetSkillFromUi(serverPlayer, itemStack, n)) {
            return false;
        }
        if (!arrayList.contains(n)) {
            arrayList.add(n);
            arrayList.sort(Integer::compareTo);
            FoodWeaponSkillLoadout.save(itemStack, arrayList);
        }
        return true;
    }
}

