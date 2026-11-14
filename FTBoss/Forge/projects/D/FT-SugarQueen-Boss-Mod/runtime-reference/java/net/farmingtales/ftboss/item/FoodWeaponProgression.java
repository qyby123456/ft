/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionAdmission;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponProgression {
    public static final String ROOT = "FTBossFoodEvolution";
    private static final String VERSION = "ProgressionVersion";
    private static final String HISTORY = "StageHistory";
    private static final String SELECTED = "SelectedStage";
    private static final List<String> PROGRESSION_KEYS = List.of("Level", "EvolutionSkill", "SecondProfile", "ActiveProfile", "ActiveSkillIndex", "EvolutionSkills", "EvolutionStats", "BlessingWashed", "SelectedStage");

    private FoodWeaponProgression() {
    }

    public static int experience(ItemStack itemStack, int n) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag == null || compoundTag.m_128451_(VERSION) >= 2) {
            return Math.max(0, n);
        }
        int n2 = FoodWeaponEvolution.evolutionLevel(itemStack);
        int n3 = n2 <= 0 ? 0 : FoodWeaponBuildRules.evolutionCost(n2 - 1);
        return Math.max(0, n - n3);
    }

    static void normalizeExperience(ItemStack itemStack) {
        int n = FoodWeaponEvolution.evolutionExperience(itemStack);
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        compoundTag.m_128405_("Experience", n);
        compoundTag.m_128405_(VERSION, 2);
    }

    public static int quality(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41783_();
        if (compoundTag != null && compoundTag.m_128425_("quality_food", 10) && compoundTag.m_128469_("quality_food").m_128425_("quality", 99)) {
            return Math.max(0, Math.min(3, compoundTag.m_128469_("quality_food").m_128451_("quality")));
        }
        return Math.max(0, FoodWeaponEvolution.foodQualityTier(itemStack) - 1);
    }

    public static Component qualityName(ItemStack itemStack) {
        return Component.m_237115_((String)("quality.ftboss.evolution." + FoodWeaponProgression.quality(itemStack)));
    }

    public static boolean isOffering(ItemStack itemStack, ItemStack itemStack2, int n) {
        return !itemStack2.m_41619_() && itemStack2 != itemStack && FoodWeaponEvolution.isEvolutionCandidate(itemStack) && !FoodWeaponEvolution.isEvolutionCandidate(itemStack2) && FoodWeaponEvolution.matchesStatueFood(itemStack2, n);
    }

    public static int chance(ItemStack itemStack, ItemStack itemStack2) {
        return FoodWeaponBuildRules.successPercent(FoodWeaponEvolution.evolutionLevel(itemStack), FoodWeaponProgression.quality(itemStack2), FoodWeaponEvolution.isGourmetBlankBlade(itemStack));
    }

    public static boolean evolve(ServerPlayer serverPlayer, int n, int n2) {
        int n3;
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || n2 < 0 || n2 >= 36) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodWeaponEvolution.Profile profile = FoodWeaponEvolution.Profile.byTint(n);
        if (profile == null || itemStack.m_41619_() || FoodWeaponLifecycle.isBroken(itemStack)) {
            return false;
        }
        int n4 = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (n4 >= FoodWeaponEvolution.maxEvolutionLevel(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.maxed"), true);
            return false;
        }
        if (!FoodWeaponProgression.isOffering(itemStack, itemStack2, n)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.offering_required"), true);
            return false;
        }
        int n5 = FoodWeaponEvolution.evolutionExperience(itemStack);
        if (n5 < (n3 = FoodWeaponEvolution.nextEvolutionRequirement(itemStack))) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.need_xp", (Object[])new Object[]{n5, n3}), true);
            return false;
        }
        if (!FoodWeaponProgression.validHistory(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        int n6 = FoodWeaponProgression.chance(itemStack, itemStack2);
        int n7 = FoodWeaponProgression.quality(itemStack2);
        int n8 = serverPlayer.m_217043_().m_188503_(100);
        boolean bl = FoodWeaponBuildRules.succeeds(n8, n6);
        FoodWeaponProgression.normalizeExperience(itemStack);
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        if (!compoundTag.m_128425_("Profile", 8)) {
            compoundTag.m_128359_("Profile", FoodWeaponEvolution.profileId(n));
        }
        FoodWeaponProgression.ensureHistory(itemStack);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag2 = FoodWeaponProgression.snapshot(compoundTag);
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        if (bl) {
            FoodWeaponEvolution.completeEvolution(serverPlayer, profile, itemStack);
            ListTag listTag = compoundTag.m_128437_(HISTORY, 10);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128359_("Statue", FoodWeaponEvolution.profileId(n));
            compoundTag3.m_128365_("Previous", (Tag)compoundTag2);
            ListTag listTag2 = new ListTag();
            if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
                FoodWeaponEvolution.EvolutionSkill evolutionSkill;
                LinkedHashSet<FoodWeaponEvolution.EvolutionSkill> linkedHashSet = new LinkedHashSet<FoodWeaponEvolution.EvolutionSkill>(FoodWeaponProgression.passiveSkills(itemStack));
                for (int i = 0; i < 2 && (evolutionSkill = FoodWeaponEvolution.drawStageSkill(profile, linkedHashSet, serverPlayer.m_217043_())) != null; ++i) {
                    linkedHashSet.add(evolutionSkill);
                    listTag2.add((Object)StringTag.m_129297_((String)FoodWeaponEvolution.skillId(evolutionSkill)));
                }
            }
            compoundTag3.m_128365_("Learned", (Tag)listTag2);
            FoodWeaponSkillRarity.recordSuccess(itemStack, compoundTag3, n7, serverPlayer.m_217043_());
            listTag.add((Object)compoundTag3);
            compoundTag.m_128365_(HISTORY, (Tag)listTag);
            compoundTag.m_128405_(SELECTED, n4);
            FoodWeaponLifecycle.markEvolved(itemStack);
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.success_chance", (Object[])new Object[]{n6}).m_130940_(ChatFormatting.GREEN));
        } else {
            FoodWeaponProgression.rollback(itemStack);
            serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11668_, SoundSource.PLAYERS, 0.6f, 0.85f);
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.failed", (Object[])new Object[]{n6, n4, FoodWeaponEvolution.evolutionLevel(itemStack)}).m_130940_(ChatFormatting.RED));
        }
        compoundTag.m_128405_("Experience", 0);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        FTBossMod.LOGGER.info("[FTBoss][\u98df\u6b66\u8fdb\u5316\u732e\u796d] {} level={} result={} roll={} chance={} newLevel={}", new Object[]{serverPlayer.m_7755_().getString(), n4, bl, n8, n6, FoodWeaponEvolution.evolutionLevel(itemStack)});
        return true;
    }

    private static CompoundTag snapshot(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        for (String string : PROGRESSION_KEYS) {
            if (!compoundTag.m_128441_(string)) continue;
            compoundTag2.m_128365_(string, compoundTag.m_128423_(string).m_6426_());
        }
        return compoundTag2;
    }

    private static boolean validHistory(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag == null || !compoundTag.m_128441_(HISTORY)) {
            return true;
        }
        Tag tag = compoundTag.m_128423_(HISTORY);
        if (!(tag instanceof ListTag)) {
            return false;
        }
        ListTag listTag = (ListTag)tag;
        if (!listTag.isEmpty() && listTag.m_7264_() != 10) {
            return false;
        }
        ListTag listTag2 = compoundTag.m_128437_(HISTORY, 10);
        if (listTag2.isEmpty()) {
            return true;
        }
        if (listTag2.size() != FoodWeaponEvolution.evolutionLevel(itemStack)) {
            return false;
        }
        for (int i = 0; i < listTag2.size(); ++i) {
            CompoundTag compoundTag2 = listTag2.m_128728_(i);
            if (compoundTag2.m_128425_("Previous", 10) && compoundTag2.m_128469_("Previous").m_128451_("Level") == i && FoodWeaponEvolution.Profile.byId(compoundTag2.m_128461_("Statue")) != null) continue;
            return false;
        }
        return true;
    }

    static void ensureHistory(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        ListTag listTag = compoundTag.m_128437_(HISTORY, 10);
        if (listTag.size() == n) {
            return;
        }
        if (!listTag.isEmpty()) {
            throw new IllegalStateException("evolution history does not match level");
        }
        ListTag listTag2 = compoundTag.m_128437_("EvolutionSkills", 10);
        for (int i = 0; i < n; ++i) {
            String string;
            CompoundTag compoundTag2 = FoodWeaponProgression.snapshot(compoundTag);
            compoundTag2.m_128405_("Level", i);
            if (i < 2) {
                compoundTag2.m_128473_("SecondProfile");
            }
            if (i == 0) {
                compoundTag2.m_128473_("EvolutionSkill");
                compoundTag2.m_128473_("ActiveProfile");
                compoundTag2.m_128473_("ActiveSkillIndex");
            }
            ListTag listTag3 = new ListTag();
            for (int j = 0; j < Math.min(i, listTag2.size()); ++j) {
                listTag3.add((Object)listTag2.get(j).m_6426_());
            }
            compoundTag2.m_128365_("EvolutionSkills", (Tag)listTag3);
            CompoundTag compoundTag3 = compoundTag.m_128469_("EvolutionStats").m_6426_();
            Object object = compoundTag3.m_128431_().iterator();
            while (object.hasNext()) {
                string = (String)object.next();
                compoundTag3.m_128347_(string, compoundTag3.m_128459_(string) * (double)i / (double)Math.max(1, n));
            }
            compoundTag2.m_128365_("EvolutionStats", (Tag)compoundTag3);
            compoundTag2.m_128405_("ActiveSkillIndex", Math.max(0, i - 1));
            compoundTag2.m_128405_(SELECTED, Math.max(0, i - 1));
            Object object2 = i < listTag2.size() ? listTag2.m_128728_(i).m_128461_("Profile") : (object = i == 0 ? compoundTag.m_128461_("Profile") : compoundTag.m_128461_("SecondProfile"));
            if (FoodWeaponEvolution.Profile.byId((String)object) == null) {
                object = FoodWeaponEvolution.firstProfileId(itemStack);
            }
            string = new CompoundTag();
            string.m_128359_("Statue", (String)object);
            string.m_128379_("Legacy", true);
            string.m_128365_("Previous", (Tag)compoundTag2);
            listTag.add((Object)string);
        }
        compoundTag.m_128365_(HISTORY, (Tag)listTag);
    }

    static void rollback(ItemStack itemStack) {
        CompoundTag compoundTag;
        ListTag listTag;
        if (FoodWeaponEvolution.evolutionLevel(itemStack) > 0) {
            FoodWeaponRepair.invalidateForProgression(itemStack);
        }
        if ((listTag = (compoundTag = itemStack.m_41698_(ROOT)).m_128437_(HISTORY, 10)).isEmpty()) {
            compoundTag.m_128405_("Level", 0);
            return;
        }
        CompoundTag compoundTag2 = listTag.m_128728_(listTag.size() - 1).m_128469_("Previous");
        listTag.remove(listTag.size() - 1);
        for (String string : PROGRESSION_KEYS) {
            compoundTag.m_128473_(string);
            if (!compoundTag2.m_128441_(string)) continue;
            compoundTag.m_128365_(string, compoundTag2.m_128423_(string).m_6426_());
        }
        compoundTag.m_128365_(HISTORY, (Tag)listTag);
    }

    public static int selectedStage(ItemStack itemStack) {
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (n == 0) {
            return -1;
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        int n2 = compoundTag != null && compoundTag.m_128441_(SELECTED) ? compoundTag.m_128451_(SELECTED) : n - 1;
        return Math.max(0, Math.min(n - 1, n2));
    }

    public static int activeStage(ItemStack itemStack) {
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (n <= 0) {
            return 0;
        }
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return 1;
        }
        return FoodWeaponProgression.selectedStage(itemStack) == 0 ? 1 : 2;
    }

    public static String secondProfile(ItemStack itemStack, String string) {
        if (FoodWeaponProgression.selectedStage(itemStack) <= 0) {
            return "";
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        ListTag listTag = compoundTag == null ? new ListTag() : compoundTag.m_128437_(HISTORY, 10);
        int n = FoodWeaponProgression.selectedStage(itemStack);
        return n < listTag.size() ? listTag.m_128728_(n).m_128461_("Statue") : string;
    }

    public static boolean select(ServerPlayer serverPlayer, int n) {
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || itemStack.m_41619_() || n < 0 || n >= FoodWeaponEvolution.evolutionLevel(itemStack)) {
            return false;
        }
        if (!FoodWeaponProgression.validHistory(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        FoodWeaponProgression.ensureHistory(itemStack);
        itemStack.m_41698_(ROOT).m_128405_(SELECTED, n);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        return true;
    }

    public static List<FoodWeaponEvolution.EvolutionSkill> passiveSkills(ItemStack itemStack) {
        LinkedHashSet<FoodWeaponEvolution.EvolutionSkill> linkedHashSet = new LinkedHashSet<FoodWeaponEvolution.EvolutionSkill>();
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag == null || FoodWeaponEvolution.evolutionLevel(itemStack) == 0) {
            return List.of();
        }
        FoodWeaponEvolution.EvolutionSkill evolutionSkill = FoodWeaponEvolution.EvolutionSkill.byId(compoundTag.m_128461_("EvolutionSkill"));
        if (evolutionSkill != null) {
            linkedHashSet.add(evolutionSkill);
        }
        ListTag listTag = compoundTag.m_128437_(HISTORY, 10);
        for (int i = 0; i < Math.min(FoodWeaponEvolution.evolutionLevel(itemStack), listTag.size()); ++i) {
            ListTag listTag2 = listTag.m_128728_(i).m_128437_("Learned", 8);
            for (int j = 0; j < Math.min(2, listTag2.size()); ++j) {
                FoodWeaponEvolution.EvolutionSkill evolutionSkill2 = FoodWeaponEvolution.EvolutionSkill.byId(listTag2.m_128778_(j));
                if (evolutionSkill2 == null) continue;
                linkedHashSet.add(evolutionSkill2);
            }
        }
        return List.copyOf(linkedHashSet);
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> details(ItemStack itemStack) {
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return List.of();
        }
        ArrayList<FoodWeaponEvolution.WeaponDetailView> arrayList = new ArrayList<FoodWeaponEvolution.WeaponDetailView>();
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        ListTag listTag = compoundTag == null ? new ListTag() : compoundTag.m_128437_(HISTORY, 10);
        WeaponEvolutionSkillPool.Aspect aspect = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.firstProfileId(itemStack));
        if (aspect == null) {
            return List.of();
        }
        for (int i = 0; i < n; ++i) {
            String object = i < listTag.size() ? listTag.m_128728_(i).m_128461_("Statue") : (compoundTag == null ? "" : compoundTag.m_128461_("SecondProfile"));
            WeaponEvolutionSkillPool.Aspect aspect2 = WeaponEvolutionSkillPool.Aspect.fromId(object);
            if (aspect2 == null) {
                aspect2 = aspect;
            }
            WeaponEvolutionSkillPool.Fusion fusion = WeaponEvolutionSkillPool.fusion(aspect, aspect2);
            MutableComponent mutableComponent = i == 0 ? Component.m_237115_((String)aspect.translationKey()) : Component.m_237115_((String)fusion.primaryTranslationKey());
            MutableComponent mutableComponent2 = i == 0 ? Component.m_237115_((String)aspect.descriptionKey()) : Component.m_237110_((String)"screen.ftboss.evolution.fusion_pair", (Object[])new Object[]{Component.m_237115_((String)fusion.primaryTranslationKey()), Component.m_237115_((String)fusion.secondaryTranslationKey())});
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"screen.ftboss.evolution.stage_skill", (Object[])new Object[]{i + 1, mutableComponent}), (Component)mutableComponent2, i, FoodWeaponProgression.selectedStage(itemStack) == i));
        }
        for (FoodWeaponEvolution.EvolutionSkill evolutionSkill : FoodWeaponProgression.passiveSkills(itemStack)) {
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.evolution.passive"), evolutionSkill.nameComponent(), FoodWeaponSkillRarity.passiveDescription(itemStack, FoodWeaponEvolution.skillId(evolutionSkill), FoodWeaponEvolution.skillDescription(evolutionSkill))));
        }
        return List.copyOf(arrayList);
    }

    static void clear(ItemStack itemStack) {
        FoodWeaponRepair.invalidateForProgression(itemStack);
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null) {
            compoundTag.m_128473_(HISTORY);
            compoundTag.m_128473_(SELECTED);
            compoundTag.m_128405_(VERSION, 2);
        }
    }

    public static boolean evolve(ServerPlayer serverPlayer, ItemStack itemStack, int n, int n2) {
        int n3;
        if (serverPlayer.m_21205_() != itemStack && serverPlayer.m_21206_() != itemStack) {
            return false;
        }
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || n2 < 0 || n2 >= 36) {
            return false;
        }
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodWeaponEvolution.Profile profile = FoodWeaponEvolution.Profile.byTint(n);
        if (profile == null || itemStack.m_41619_() || FoodWeaponLifecycle.isBroken(itemStack)) {
            return false;
        }
        int n4 = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (n4 >= FoodWeaponEvolution.maxEvolutionLevel(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.maxed"), true);
            return false;
        }
        if (!FoodWeaponProgression.isOffering(itemStack, itemStack2, n)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.offering_required"), true);
            return false;
        }
        int n5 = FoodWeaponEvolution.evolutionExperience(itemStack);
        if (n5 < (n3 = FoodWeaponEvolution.nextEvolutionRequirement(itemStack))) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.need_xp", (Object[])new Object[]{n5, n3}), true);
            return false;
        }
        ItemStack itemStack3 = itemStack.m_41777_();
        if (!(!FourWeaponFoundationAdapter.applies(itemStack) || FourWeaponFoundationAdapter.canEvolve(itemStack) && FourWeaponFoundationAdapter.initializeForServerEdit(itemStack3))) {
            return false;
        }
        if (!FoodWeaponProgression.validHistory(itemStack3)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        int n6 = FoodWeaponProgression.chance(itemStack, itemStack2);
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            itemStack.m_41751_(itemStack3.m_41783_().m_6426_());
        }
        int n7 = FoodWeaponProgression.quality(itemStack2);
        int n8 = serverPlayer.m_217043_().m_188503_(100);
        boolean bl = FoodWeaponBuildRules.succeeds(n8, n6);
        FoodWeaponProgression.normalizeExperience(itemStack);
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        if (!compoundTag.m_128425_("Profile", 8)) {
            compoundTag.m_128359_("Profile", FoodWeaponEvolution.profileId(n));
        }
        FoodWeaponProgression.ensureHistory(itemStack);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag2 = FoodWeaponProgression.snapshot(compoundTag);
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        if (bl) {
            FoodWeaponEvolution.completeEvolution(serverPlayer, profile, itemStack);
            ListTag listTag = compoundTag.m_128437_(HISTORY, 10);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128359_("Statue", FoodWeaponEvolution.profileId(n));
            compoundTag3.m_128365_("Previous", (Tag)compoundTag2);
            ListTag listTag2 = new ListTag();
            if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
                FoodWeaponEvolution.EvolutionSkill evolutionSkill;
                LinkedHashSet<FoodWeaponEvolution.EvolutionSkill> linkedHashSet = new LinkedHashSet<FoodWeaponEvolution.EvolutionSkill>(FoodWeaponProgression.passiveSkills(itemStack));
                for (int i = 0; i < 2 && (evolutionSkill = FoodWeaponEvolution.drawStageSkill(profile, linkedHashSet, serverPlayer.m_217043_())) != null; ++i) {
                    linkedHashSet.add(evolutionSkill);
                    listTag2.add((Object)StringTag.m_129297_((String)FoodWeaponEvolution.skillId(evolutionSkill)));
                }
            }
            compoundTag3.m_128365_("Learned", (Tag)listTag2);
            FoodWeaponSkillRarity.recordSuccess(itemStack, compoundTag3, n7, serverPlayer.m_217043_());
            listTag.add((Object)compoundTag3);
            compoundTag.m_128365_(HISTORY, (Tag)listTag);
            compoundTag.m_128405_(SELECTED, n4);
            FoodWeaponLifecycle.markEvolved(itemStack);
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.success_chance", (Object[])new Object[]{n6}).m_130940_(ChatFormatting.GREEN));
        } else {
            FoodWeaponProgression.rollback(itemStack);
            serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11668_, SoundSource.PLAYERS, 0.6f, 0.85f);
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.failed", (Object[])new Object[]{n6, n4, FoodWeaponEvolution.evolutionLevel(itemStack)}).m_130940_(ChatFormatting.RED));
        }
        compoundTag.m_128405_("Experience", 0);
        if (FourWeaponFoundationAdapter.applies(itemStack) && !FourWeaponFoundationAdapter.reconcileLevel(itemStack, compoundTag.m_128451_("Level"), 0)) {
            throw new IllegalStateException("Resolved progression failed foundation reconciliation");
        }
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        FTBossMod.LOGGER.info("[FTBoss][\u98df\u6b66\u8fdb\u5316\u732e\u796d] {} level={} result={} roll={} chance={} newLevel={}", new Object[]{serverPlayer.m_7755_().getString(), n4, bl, n8, n6, FoodWeaponEvolution.evolutionLevel(itemStack)});
        return true;
    }

    public static boolean select(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        if (FourWeaponFoundationAdapter.applies(itemStack) && !TransactionAdmission.permits(serverPlayer, itemStack, FourWeaponTransactions.Operation.SELECT_SKILL)) {
            return false;
        }
        if (itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_()) {
            return false;
        }
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || itemStack.m_41619_() || n < 0 || n >= FoodWeaponEvolution.evolutionLevel(itemStack)) {
            return false;
        }
        if (!FoodWeaponProgression.validHistory(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        FoodWeaponProgression.ensureHistory(itemStack);
        itemStack.m_41698_(ROOT).m_128405_(SELECTED, n);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        return true;
    }
}

