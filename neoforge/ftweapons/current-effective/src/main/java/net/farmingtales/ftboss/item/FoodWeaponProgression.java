package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.EvolutionSkill;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.transaction.TransactionAdmission;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Fusion;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
    private static final List<String> PROGRESSION_KEYS = List.of(
        "Level", "EvolutionSkill", "SecondProfile", "ActiveProfile", "ActiveSkillIndex", "EvolutionSkills", "EvolutionStats", "BlessingWashed", "SelectedStage"
    );

    private FoodWeaponProgression() {
    }

    public static int experience(ItemStack var0, int var1) {
        CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        if (var2 != null && var2.getInt("ProgressionVersion") < 2) {
            int var3 = FoodWeaponEvolution.evolutionLevel(var0);
            int var4 = var3 <= 0 ? 0 : FoodWeaponBuildRules.evolutionCost(var3 - 1);
            return Math.max(0, var1 - var4);
        } else {
            return Math.max(0, var1);
        }
    }

    static void normalizeExperience(ItemStack var0) {
        int var1 = FoodWeaponEvolution.evolutionExperience(var0);
        NativeProgressionItemData.normalizeExperience(var0, var1);
    }

    public static int quality(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.customData(var0);
        return var1 != null && var1.contains("quality_food", 10) && var1.getCompound("quality_food").contains("quality", 99)
            ? Math.max(0, Math.min(3, var1.getCompound("quality_food").getInt("quality")))
            : Math.max(0, FoodWeaponEvolution.foodQualityTier(var0) - 1);
    }

    public static Component qualityName(ItemStack var0) {
        return Component.translatable("quality.ftboss.evolution." + quality(var0));
    }

    public static boolean isOffering(ItemStack var0, ItemStack var1, int var2) {
        return !var1.isEmpty()
            && var1 != var0
            && FoodWeaponEvolution.isEvolutionCandidate(var0)
            && !FoodWeaponEvolution.isEvolutionCandidate(var1)
            && FoodWeaponEvolution.matchesStatueFood(var1, var2);
    }

    public static int chance(ItemStack var0, ItemStack var1) {
        return FoodWeaponBuildRules.successPercent(FoodWeaponEvolution.evolutionLevel(var0), quality(var1), FoodWeaponEvolution.isGourmetBlankBlade(var0));
    }

    public static boolean evolve(ServerPlayer var0, int var1, int var2) {
        if (!var0.isSpectator() && var0.isAlive() && var2 >= 0 && var2 < 36) {
            ItemStack var3 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            ItemStack var4 = var0.getInventory().getItem(var2);
            Profile var5 = Profile.byTint(var1);
            if (var5 != null && !var3.isEmpty() && !FoodWeaponLifecycle.isBroken(var3)) {
                int var6 = FoodWeaponEvolution.evolutionLevel(var3);
                if (var6 >= FoodWeaponEvolution.maxEvolutionLevel(var3)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                    return false;
                } else if (!isOffering(var3, var4, var1)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
                    return false;
                } else {
                    int var7 = FoodWeaponEvolution.evolutionExperience(var3);
                    int var8 = FoodWeaponEvolution.nextEvolutionRequirement(var3);
                    if (var7 < var8) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", var7, var8), true);
                        return false;
                    } else if (!validHistory(var3)) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
                        return false;
                    } else {
                        int var9 = chance(var3, var4);
                        int var10 = quality(var4);
                        int var11 = var0.getRandom().nextInt(100);
                        boolean var12 = FoodWeaponBuildRules.succeeds(var11, var9);
                        normalizeExperience(var3);
                        CompoundTag var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                        if (!var13.contains("Profile", 8)) {
                            var13.putString("Profile", FoodWeaponEvolution.profileId(var1));
                        }

                        FoodWeaponData.write(var3, "FTBossFoodEvolution", var13);
                        ensureHistory(var3);
                        FoodWeaponLifecycle.ensureTracked(var3);
                        var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                        CompoundTag var14 = snapshot(var13);
                        if (!var0.getAbilities().instabuild) {
                            var4.shrink(1);
                        }

                        if (var12) {
                            FoodWeaponEvolution.completeEvolution(var0, var5, var3);
                            var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                            ListTag var15 = var13.getList("StageHistory", 10);
                            CompoundTag var16 = new CompoundTag();
                            var16.putString("Statue", FoodWeaponEvolution.profileId(var1));
                            var16.put("Previous", var14);
                            ListTag var17 = new ListTag();
                            if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(var3)) {
                                LinkedHashSet var18 = new LinkedHashSet<>(passiveSkills(var3));

                                for (int var19 = 0; var19 < 2; var19++) {
                                    EvolutionSkill var20 = FoodWeaponEvolution.drawStageSkill(var5, var18, var0.getRandom());
                                    if (var20 == null) {
                                        break;
                                    }

                                    var18.add(var20);
                                    var17.add(StringTag.valueOf(FoodWeaponEvolution.skillId(var20)));
                                }
                            }

                            var16.put("Learned", var17);
                            FoodWeaponSkillRarity.recordSuccess(var3, var16, var10, var0.getRandom());
                            NativeProgressionItemData.appendStage(var3, var15, var16, var6);
                            FoodWeaponLifecycle.markEvolved(var3);
                            var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.success_chance", var9).withStyle(ChatFormatting.GREEN));
                        } else {
                            rollback(var3);
                            var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.85F);
                            var0.sendSystemMessage(
                                Component.translatable("message.ftboss.evolution.failed", var9, var6, FoodWeaponEvolution.evolutionLevel(var3))
                                    .withStyle(ChatFormatting.RED)
                            );
                        }

                        NativeProgressionItemData.finishExperience(var3);
                        var0.getInventory().setChanged();
                        var0.containerMenu.broadcastChanges();
                        FTBossMod.LOGGER
                            .info(
                                "[FTBoss][食武进化献祭] {} level={} result={} roll={} chance={} newLevel={}",
                                new Object[]{var0.getName().getString(), var6, var12, var11, var9, FoodWeaponEvolution.evolutionLevel(var3)}
                            );
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static CompoundTag snapshot(CompoundTag var0) {
        return NativeProgressionItemData.snapshot(var0, PROGRESSION_KEYS);
    }

    private static boolean validHistory(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        if (var1 != null && var1.contains("StageHistory")) {
            if (!(var1.get("StageHistory") instanceof ListTag var3)) {
                return false;
            } else if (!var3.isEmpty() && var3.getElementType() != 10) {
                return false;
            } else {
                ListTag var4 = var1.getList("StageHistory", 10);
                if (var4.isEmpty()) {
                    return true;
                } else if (var4.size() != FoodWeaponEvolution.evolutionLevel(var0)) {
                    return false;
                } else {
                    for (int var5 = 0; var5 < var4.size(); var5++) {
                        CompoundTag var6 = var4.getCompound(var5);
                        if (!var6.contains("Previous", 10)
                            || var6.getCompound("Previous").getInt("Level") != var5
                            || Profile.byId(var6.getString("Statue")) == null) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        } else {
            return true;
        }
    }

    static void ensureHistory(ItemStack var0) {
        FoodWeaponData.edit(var0, "FTBossFoodEvolution", var1 -> {
            int var2 = FoodWeaponEvolution.evolutionLevel(var0);
            ListTag var3 = var1.getList("StageHistory", 10);
            if (var3.size() != var2) {
                if (!var3.isEmpty()) {
                    throw new IllegalStateException("evolution history does not match level");
                } else {
                    ListTag var4 = var1.getList("EvolutionSkills", 10);

                    for (int var5 = 0; var5 < var2; var5++) {
                        CompoundTag var6 = snapshot(var1);
                        var6.putInt("Level", var5);
                        if (var5 < 2) {
                            var6.remove("SecondProfile");
                        }

                        if (var5 == 0) {
                            var6.remove("EvolutionSkill");
                            var6.remove("ActiveProfile");
                            var6.remove("ActiveSkillIndex");
                        }

                        ListTag var7 = new ListTag();

                        for (int var8 = 0; var8 < Math.min(var5, var4.size()); var8++) {
                            var7.add(var4.get(var8).copy());
                        }

                        var6.put("EvolutionSkills", var7);
                        CompoundTag var11 = var1.getCompound("EvolutionStats").copy();

                        for (String var10 : var11.getAllKeys()) {
                            var11.putDouble(var10, var11.getDouble(var10) * (double)var5 / (double)Math.max(1, var2));
                        }

                        var6.put("EvolutionStats", var11);
                        var6.putInt("ActiveSkillIndex", Math.max(0, var5 - 1));
                        var6.putInt("SelectedStage", Math.max(0, var5 - 1));
                        String var12 = var5 < var4.size()
                            ? var4.getCompound(var5).getString("Profile")
                            : (var5 == 0 ? var1.getString("Profile") : var1.getString("SecondProfile"));
                        if (Profile.byId(var12) == null) {
                            var12 = FoodWeaponEvolution.firstProfileId(var0);
                        }

                        CompoundTag var13 = new CompoundTag();
                        var13.putString("Statue", var12);
                        var13.putBoolean("Legacy", true);
                        var13.put("Previous", var6);
                        var3.add(var13);
                    }

                    var1.put("StageHistory", var3);
                }
            }
        });
    }

    static void rollback(ItemStack var0) {
        if (FoodWeaponEvolution.evolutionLevel(var0) > 0) {
            FoodWeaponRepair.invalidateForProgression(var0);
        }

        FoodWeaponData.edit(var0, "FTBossFoodEvolution", var1 -> {
            ListTag var2 = var1.getList("StageHistory", 10);
            if (var2.isEmpty()) {
                var1.putInt("Level", 0);
            } else {
                CompoundTag var3 = var2.getCompound(var2.size() - 1).getCompound("Previous");
                var2.remove(var2.size() - 1);

                for (String var5 : PROGRESSION_KEYS) {
                    var1.remove(var5);
                    if (var3.contains(var5)) {
                        var1.put(var5, var3.get(var5).copy());
                    }
                }

                var1.put("StageHistory", var2);
            }
        });
    }

    public static int selectedStage(ItemStack var0) {
        int var1 = FoodWeaponEvolution.evolutionLevel(var0);
        if (var1 == 0) {
            return -1;
        } else {
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            int var3 = var2 != null && var2.contains("SelectedStage") ? var2.getInt("SelectedStage") : var1 - 1;
            return Math.max(0, Math.min(var1 - 1, var3));
        }
    }

    public static int activeStage(ItemStack var0) {
        int var1 = FoodWeaponEvolution.evolutionLevel(var0);
        if (var1 <= 0) {
            return 0;
        } else if (FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)) {
            return 1;
        } else {
            return selectedStage(var0) == 0 ? 1 : 2;
        }
    }

    public static String secondProfile(ItemStack var0, String var1) {
        if (selectedStage(var0) <= 0) {
            return "";
        } else {
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            ListTag var3 = var2 == null ? new ListTag() : var2.getList("StageHistory", 10);
            int var4 = selectedStage(var0);
            return var4 < var3.size() ? var3.getCompound(var4).getString("Statue") : var1;
        }
    }

    public static boolean select(ServerPlayer var0, int var1) {
        ItemStack var2 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
        if (!var0.isAlive() || var0.isSpectator() || var2.isEmpty() || var1 < 0 || var1 >= FoodWeaponEvolution.evolutionLevel(var2)) {
            return false;
        } else if (!validHistory(var2)) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
            return false;
        } else {
            ensureHistory(var2);
            FoodWeaponData.edit(var2, "FTBossFoodEvolution", evolution -> evolution.putInt("SelectedStage", var1));
            var0.getInventory().setChanged();
            var0.containerMenu.broadcastChanges();
            return true;
        }
    }

    public static List<EvolutionSkill> passiveSkills(ItemStack var0) {
        LinkedHashSet var1 = new LinkedHashSet();
        CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        if (var2 != null && FoodWeaponEvolution.evolutionLevel(var0) != 0) {
            EvolutionSkill var3 = EvolutionSkill.byId(var2.getString("EvolutionSkill"));
            if (var3 != null) {
                var1.add(var3);
            }

            ListTag var4 = var2.getList("StageHistory", 10);

            for (int var5 = 0; var5 < Math.min(FoodWeaponEvolution.evolutionLevel(var0), var4.size()); var5++) {
                ListTag var6 = var4.getCompound(var5).getList("Learned", 8);

                for (int var7 = 0; var7 < Math.min(2, var6.size()); var7++) {
                    EvolutionSkill var8 = EvolutionSkill.byId(var6.getString(var7));
                    if (var8 != null) {
                        var1.add(var8);
                    }
                }
            }

            return List.copyOf(var1);
        } else {
            return List.of();
        }
    }

    public static List<WeaponDetailView> details(ItemStack var0) {
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)) {
            return List.of();
        } else {
            ArrayList var1 = new ArrayList();
            int var2 = FoodWeaponEvolution.evolutionLevel(var0);
            CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            ListTag var4 = var3 == null ? new ListTag() : var3.getList("StageHistory", 10);
            Aspect var5 = Aspect.fromId(FoodWeaponEvolution.firstProfileId(var0));
            if (var5 == null) {
                return List.of();
            } else {
                for (int var6 = 0; var6 < var2; var6++) {
                    String var7 = var6 < var4.size() ? var4.getCompound(var6).getString("Statue") : (var3 == null ? "" : var3.getString("SecondProfile"));
                    Aspect var8 = Aspect.fromId(var7);
                    if (var8 == null) {
                        var8 = var5;
                    }

                    Fusion var9 = WeaponEvolutionSkillPool.fusion(var5, var8);
                    MutableComponent var10 = var6 == 0 ? Component.translatable(var5.translationKey()) : Component.translatable(var9.primaryTranslationKey());
                    MutableComponent var11 = var6 == 0
                        ? Component.translatable(var5.descriptionKey())
                        : Component.translatable(
                            "screen.ftboss.evolution.fusion_pair",
                            Component.translatable(var9.primaryTranslationKey()),
                            Component.translatable(var9.secondaryTranslationKey())
                        );
                    var1.add(
                        new WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("screen.ftboss.evolution.stage_skill", var6 + 1, var10),
                            var11,
                            var6,
                            selectedStage(var0) == var6
                        )
                    );
                }

                for (EvolutionSkill var13 : passiveSkills(var0)) {
                    var1.add(
                        new WeaponDetailView(
                            Component.translatable("screen.ftboss.evolution.passive"),
                            var13.nameComponent(),
                            FoodWeaponSkillRarity.passiveDescription(var0, FoodWeaponEvolution.skillId(var13), FoodWeaponEvolution.skillDescription(var13))
                        )
                    );
                }

                return List.copyOf(var1);
            }
        }
    }

    static void clear(ItemStack var0) {
        FoodWeaponRepair.invalidateForProgression(var0);
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        if (var1 != null) {
            var1.remove("StageHistory");
            var1.remove("SelectedStage");
            var1.putInt("ProgressionVersion", 2);
            FoodWeaponData.write(var0, "FTBossFoodEvolution", var1);
        }
    }

    public static boolean evolve(ServerPlayer var0, ItemStack var1, int var2, int var3) {
        if (var0.getMainHandItem() != var1 && var0.getOffhandItem() != var1) {
            return false;
        } else if (!var0.isSpectator() && var0.isAlive() && var3 >= 0 && var3 < 36) {
            ItemStack var4 = var0.getInventory().getItem(var3);
            Profile var5 = Profile.byTint(var2);
            if (var5 != null && !var1.isEmpty() && !FoodWeaponLifecycle.isBroken(var1)) {
                int var6 = FoodWeaponEvolution.evolutionLevel(var1);
                if (var6 >= FoodWeaponEvolution.maxEvolutionLevel(var1)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                    return false;
                } else if (!isOffering(var1, var4, var2)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
                    return false;
                } else {
                    int var7 = FoodWeaponEvolution.evolutionExperience(var1);
                    int var8 = FoodWeaponEvolution.nextEvolutionRequirement(var1);
                    if (var7 < var8) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", var7, var8), true);
                        return false;
                    } else {
                        ItemStack var9 = var1.copy();
                        if (!FourWeaponFoundationAdapter.applies(var1)
                            || FourWeaponFoundationAdapter.canEvolve(var1) && FourWeaponFoundationAdapter.initializeForServerEdit(var9)) {
                            if (!validHistory(var9)) {
                                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
                                return false;
                            } else {
                                int var10 = chance(var1, var4);
                                if (FourWeaponFoundationAdapter.applies(var1)) {
                                    NativeFoundationItemData.copyComponents(var1, var9);
                                }

                                int var11 = quality(var4);
                                int var12 = var0.getRandom().nextInt(100);
                                boolean var13 = FoodWeaponBuildRules.succeeds(var12, var10);
                                normalizeExperience(var1);
                                CompoundTag var14 = FoodWeaponData.read(var1, "FTBossFoodEvolution");
                                if (!var14.contains("Profile", 8)) {
                                    var14.putString("Profile", FoodWeaponEvolution.profileId(var2));
                                }

                                FoodWeaponData.write(var1, "FTBossFoodEvolution", var14);
                                ensureHistory(var1);
                                FoodWeaponLifecycle.ensureTracked(var1);
                                var14 = FoodWeaponData.read(var1, "FTBossFoodEvolution");
                                CompoundTag var15 = snapshot(var14);
                                if (!var0.getAbilities().instabuild) {
                                    var4.shrink(1);
                                }

                                if (var13) {
                                    FoodWeaponEvolution.completeEvolution(var0, var5, var1);
                                    var14 = FoodWeaponData.read(var1, "FTBossFoodEvolution");
                                    ListTag var16 = var14.getList("StageHistory", 10);
                                    CompoundTag var17 = new CompoundTag();
                                    var17.putString("Statue", FoodWeaponEvolution.profileId(var2));
                                    var17.put("Previous", var15);
                                    ListTag var18 = new ListTag();
                                    if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(var1)) {
                                        LinkedHashSet var19 = new LinkedHashSet<>(passiveSkills(var1));

                                        for (int var20 = 0; var20 < 2; var20++) {
                                            EvolutionSkill var21 = FoodWeaponEvolution.drawStageSkill(var5, var19, var0.getRandom());
                                            if (var21 == null) {
                                                break;
                                            }

                                            var19.add(var21);
                                            var18.add(StringTag.valueOf(FoodWeaponEvolution.skillId(var21)));
                                        }
                                    }

                                    var17.put("Learned", var18);
                                    FoodWeaponSkillRarity.recordSuccess(var1, var17, var11, var0.getRandom());
                                    NativeProgressionItemData.appendStage(var1, var16, var17, var6);
                                    FoodWeaponLifecycle.markEvolved(var1);
                                    var0.sendSystemMessage(
                                        Component.translatable("message.ftboss.evolution.success_chance", var10).withStyle(ChatFormatting.GREEN)
                                    );
                                } else {
                                    rollback(var1);
                                    var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.85F);
                                    var0.sendSystemMessage(
                                        Component.translatable("message.ftboss.evolution.failed", var10, var6, FoodWeaponEvolution.evolutionLevel(var1))
                                            .withStyle(ChatFormatting.RED)
                                    );
                                }

                                NativeProgressionItemData.finishExperience(var1);
                                var14 = FoodWeaponData.read(var1, "FTBossFoodEvolution");
                                if (FourWeaponFoundationAdapter.applies(var1) && !FourWeaponFoundationAdapter.reconcileLevel(var1, var14.getInt("Level"), 0)) {
                                    throw new IllegalStateException("Resolved progression failed foundation reconciliation");
                                } else {
                                    var0.getInventory().setChanged();
                                    var0.containerMenu.broadcastChanges();
                                    FTBossMod.LOGGER
                                        .info(
                                            "[FTBoss][食武进化献祭] {} level={} result={} roll={} chance={} newLevel={}",
                                            new Object[]{var0.getName().getString(), var6, var13, var12, var10, FoodWeaponEvolution.evolutionLevel(var1)}
                                        );
                                    return true;
                                }
                            }
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean select(ServerPlayer var0, ItemStack var1, int var2) {
        if (FourWeaponFoundationAdapter.applies(var1) && !TransactionAdmission.permits(var0, var1, Operation.SELECT_SKILL)) {
            return false;
        } else if (var1 != var0.getMainHandItem() && var1 != var0.getOffhandItem()) {
            return false;
        } else if (!var0.isAlive() || var0.isSpectator() || var1.isEmpty() || var2 < 0 || var2 >= FoodWeaponEvolution.evolutionLevel(var1)) {
            return false;
        } else if (!validHistory(var1)) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
            return false;
        } else {
            ensureHistory(var1);
            FoodWeaponData.edit(var1, "FTBossFoodEvolution", evolution -> evolution.putInt("SelectedStage", var2));
            var0.getInventory().setChanged();
            var0.containerMenu.broadcastChanges();
            return true;
        }
    }

    public static int ftboss$legacyOrdinary$chance(ItemStack var0, ItemStack var1) {
            return FoodWeaponBuildRules.successPercent(FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0), ftboss$legacyOrdinary$quality(var1), FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetBlankBlade(var0));
        }

    static void ftboss$legacyOrdinary$ensureHistory(ItemStack var0) {
            FoodWeaponData.edit(var0, "FTBossFoodEvolution", var1 -> {
                int var2 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0);
                ListTag var3 = var1.getList("StageHistory", 10);
                if (var3.size() != var2) {
                    if (!var3.isEmpty()) {
                        throw new IllegalStateException("evolution history does not match level");
                    } else {
                        ListTag var4 = var1.getList("EvolutionSkills", 10);
    
                        for (int var5 = 0; var5 < var2; var5++) {
                            CompoundTag var6 = ftboss$legacyOrdinary$snapshot(var1);
                            var6.putInt("Level", var5);
                            if (var5 < 2) {
                                var6.remove("SecondProfile");
                            }
    
                            if (var5 == 0) {
                                var6.remove("EvolutionSkill");
                                var6.remove("ActiveProfile");
                                var6.remove("ActiveSkillIndex");
                            }
    
                            ListTag var7 = new ListTag();
    
                            for (int var8 = 0; var8 < Math.min(var5, var4.size()); var8++) {
                                var7.add(var4.get(var8).copy());
                            }
    
                            var6.put("EvolutionSkills", var7);
                            CompoundTag var11 = var1.getCompound("EvolutionStats").copy();
    
                            for (String var10 : var11.getAllKeys()) {
                                var11.putDouble(var10, var11.getDouble(var10) * (double)var5 / (double)Math.max(1, var2));
                            }
    
                            var6.put("EvolutionStats", var11);
                            var6.putInt("ActiveSkillIndex", Math.max(0, var5 - 1));
                            var6.putInt("SelectedStage", Math.max(0, var5 - 1));
                            String var12 = var5 < var4.size()
                                ? var4.getCompound(var5).getString("Profile")
                                : (var5 == 0 ? var1.getString("Profile") : var1.getString("SecondProfile"));
                            if (Profile.byId(var12) == null) {
                                var12 = FoodWeaponEvolution.ftboss$legacyOrdinary$firstProfileId(var0);
                            }
    
                            CompoundTag var13 = new CompoundTag();
                            var13.putString("Statue", var12);
                            var13.putBoolean("Legacy", true);
                            var13.put("Previous", var6);
                            var3.add(var13);
                        }
    
                        var1.put("StageHistory", var3);
                    }
                }
            });
        }

    public static boolean ftboss$legacyOrdinary$evolve(ServerPlayer var0, int var1, int var2) {
            if (!var0.isSpectator() && var0.isAlive() && var2 >= 0 && var2 < 36) {
                ItemStack var3 = FoodWeaponEvolution.ftboss$legacyOrdinary$heldEvolutionWeapon(var0);
                ItemStack var4 = var0.getInventory().getItem(var2);
                Profile var5 = Profile.byTint(var1);
                if (var5 != null && !var3.isEmpty() && !FoodWeaponLifecycle.isBroken(var3)) {
                    int var6 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var3);
                    if (var6 >= FoodWeaponEvolution.ftboss$legacyOrdinary$maxEvolutionLevel(var3)) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                        return false;
                    } else if (!ftboss$legacyOrdinary$isOffering(var3, var4, var1)) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
                        return false;
                    } else {
                        int var7 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionExperience(var3);
                        int var8 = FoodWeaponEvolution.ftboss$legacyOrdinary$nextEvolutionRequirement(var3);
                        if (var7 < var8) {
                            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", var7, var8), true);
                            return false;
                        } else if (!ftboss$legacyOrdinary$validHistory(var3)) {
                            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
                            return false;
                        } else {
                            int var9 = ftboss$legacyOrdinary$chance(var3, var4);
                            int var10 = ftboss$legacyOrdinary$quality(var4);
                            int var11 = var0.getRandom().nextInt(100);
                            boolean var12 = FoodWeaponBuildRules.succeeds(var11, var9);
                            ftboss$legacyOrdinary$normalizeExperience(var3);
                            CompoundTag var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                            if (!var13.contains("Profile", 8)) {
                                var13.putString("Profile", FoodWeaponEvolution.ftboss$legacyOrdinary$profileId(var1));
                            }
    
                            FoodWeaponData.write(var3, "FTBossFoodEvolution", var13);
                            ftboss$legacyOrdinary$ensureHistory(var3);
                            FoodWeaponLifecycle.ensureTracked(var3);
                            var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                            CompoundTag var14 = ftboss$legacyOrdinary$snapshot(var13);
                            if (!var0.getAbilities().instabuild) {
                                var4.shrink(1);
                            }
    
                            if (var12) {
                                FoodWeaponEvolution.ftboss$legacyOrdinary$completeEvolution(var0, var5, var3);
                                var13 = FoodWeaponData.read(var3, "FTBossFoodEvolution");
                                ListTag var15 = var13.getList("StageHistory", 10);
                                CompoundTag var16 = new CompoundTag();
                                var16.putString("Statue", FoodWeaponEvolution.ftboss$legacyOrdinary$profileId(var1));
                                var16.put("Previous", var14);
                                ListTag var17 = new ListTag();
                                if (!FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var3)) {
                                    LinkedHashSet var18 = new LinkedHashSet<>(ftboss$legacyOrdinary$passiveSkills(var3));
    
                                    for (int var19 = 0; var19 < 2; var19++) {
                                        EvolutionSkill var20 = FoodWeaponEvolution.ftboss$legacyOrdinary$drawStageSkill(var5, var18, var0.getRandom());
                                        if (var20 == null) {
                                            break;
                                        }
    
                                        var18.add(var20);
                                        var17.add(StringTag.valueOf(FoodWeaponEvolution.ftboss$legacyOrdinary$skillId(var20)));
                                    }
                                }
    
                                var16.put("Learned", var17);
                                FoodWeaponSkillRarity.ftboss$legacyOrdinary$recordSuccess(var3, var16, var10, var0.getRandom());
                                NativeProgressionItemData.appendStage(var3, var15, var16, var6);
                                FoodWeaponLifecycle.markEvolved(var3);
                                var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.success_chance", var9).withStyle(ChatFormatting.GREEN));
                            } else {
                                ftboss$legacyOrdinary$rollback(var3);
                                var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.85F);
                                var0.sendSystemMessage(
                                    Component.translatable("message.ftboss.evolution.failed", var9, var6, FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var3))
                                        .withStyle(ChatFormatting.RED)
                                );
                            }
    
                            NativeProgressionItemData.finishExperience(var3);
                            var0.getInventory().setChanged();
                            var0.containerMenu.broadcastChanges();
                            FTBossMod.LOGGER
                                .info(
                                    "[FTBoss][食武进化献祭] {} level={} result={} roll={} chance={} newLevel={}",
                                    new Object[]{var0.getName().getString(), var6, var12, var11, var9, FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var3)}
                                );
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

    public static int ftboss$legacyOrdinary$experience(ItemStack var0, int var1) {
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            if (var2 != null && var2.getInt("ProgressionVersion") < 2) {
                int var3 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0);
                int var4 = var3 <= 0 ? 0 : FoodWeaponBuildRules.evolutionCost(var3 - 1);
                return Math.max(0, var1 - var4);
            } else {
                return Math.max(0, var1);
            }
        }

    public static boolean ftboss$legacyOrdinary$isOffering(ItemStack var0, ItemStack var1, int var2) {
            return !var1.isEmpty()
                && var1 != var0
                && FoodWeaponEvolution.ftboss$legacyOrdinary$isEvolutionCandidate(var0)
                && !FoodWeaponEvolution.ftboss$legacyOrdinary$isEvolutionCandidate(var1)
                && FoodWeaponEvolution.ftboss$legacyOrdinary$matchesStatueFood(var1, var2);
        }

    static void ftboss$legacyOrdinary$normalizeExperience(ItemStack var0) {
            int var1 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionExperience(var0);
            NativeProgressionItemData.normalizeExperience(var0, var1);
        }

    public static List<EvolutionSkill> ftboss$legacyOrdinary$passiveSkills(ItemStack var0) {
            LinkedHashSet var1 = new LinkedHashSet();
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            if (var2 != null && FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0) != 0) {
                EvolutionSkill var3 = EvolutionSkill.byId(var2.getString("EvolutionSkill"));
                if (var3 != null) {
                    var1.add(var3);
                }
    
                ListTag var4 = var2.getList("StageHistory", 10);
    
                for (int var5 = 0; var5 < Math.min(FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0), var4.size()); var5++) {
                    ListTag var6 = var4.getCompound(var5).getList("Learned", 8);
    
                    for (int var7 = 0; var7 < Math.min(2, var6.size()); var7++) {
                        EvolutionSkill var8 = EvolutionSkill.byId(var6.getString(var7));
                        if (var8 != null) {
                            var1.add(var8);
                        }
                    }
                }
    
                return List.copyOf(var1);
            } else {
                return List.of();
            }
        }

    public static int ftboss$legacyOrdinary$quality(ItemStack var0) {
            CompoundTag var1 = NativeFoundationItemData.customData(var0);
            return var1 != null && var1.contains("quality_food", 10) && var1.getCompound("quality_food").contains("quality", 99)
                ? Math.max(0, Math.min(3, var1.getCompound("quality_food").getInt("quality")))
                : Math.max(0, FoodWeaponEvolution.ftboss$legacyOrdinary$foodQualityTier(var0) - 1);
        }

    static void ftboss$legacyOrdinary$rollback(ItemStack var0) {
            if (FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0) > 0) {
                FoodWeaponRepair.invalidateForProgression(var0);
            }
    
            FoodWeaponData.edit(var0, "FTBossFoodEvolution", var1 -> {
                ListTag var2 = var1.getList("StageHistory", 10);
                if (var2.isEmpty()) {
                    var1.putInt("Level", 0);
                } else {
                    CompoundTag var3 = var2.getCompound(var2.size() - 1).getCompound("Previous");
                    var2.remove(var2.size() - 1);
    
                    for (String var5 : PROGRESSION_KEYS) {
                        var1.remove(var5);
                        if (var3.contains(var5)) {
                            var1.put(var5, var3.get(var5).copy());
                        }
                    }
    
                    var1.put("StageHistory", var2);
                }
            });
        }

    private static CompoundTag ftboss$legacyOrdinary$snapshot(CompoundTag var0) {
            return NativeProgressionItemData.snapshot(var0, PROGRESSION_KEYS);
        }

    private static boolean ftboss$legacyOrdinary$validHistory(ItemStack var0) {
            CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
            if (var1 != null && var1.contains("StageHistory")) {
                if (!(var1.get("StageHistory") instanceof ListTag var3)) {
                    return false;
                } else if (!var3.isEmpty() && var3.getElementType() != 10) {
                    return false;
                } else {
                    ListTag var4 = var1.getList("StageHistory", 10);
                    if (var4.isEmpty()) {
                        return true;
                    } else if (var4.size() != FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0)) {
                        return false;
                    } else {
                        for (int var5 = 0; var5 < var4.size(); var5++) {
                            CompoundTag var6 = var4.getCompound(var5);
                            if (!var6.contains("Previous", 10)
                                || var6.getCompound("Previous").getInt("Level") != var5
                                || Profile.byId(var6.getString("Statue")) == null) {
                                return false;
                            }
                        }
    
                        return true;
                    }
                }
            } else {
                return true;
            }
        }

    public static int ftboss$legacyOrdinary$selectedStage(ItemStack var0) {
            int var1 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0);
            if (var1 == 0) {
                return -1;
            } else {
                CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
                int var3 = var2 != null && var2.contains("SelectedStage") ? var2.getInt("SelectedStage") : var1 - 1;
                return Math.max(0, Math.min(var1 - 1, var3));
            }
        }

    public static int ftboss$legacyOrdinary$activeStage(ItemStack var0) {
            int var1 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0);
            if (var1 <= 0) {
                return 0;
            } else if (FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                return 1;
            } else {
                return ftboss$legacyOrdinary$selectedStage(var0) == 0 ? 1 : 2;
            }
        }

    public static String ftboss$legacyOrdinary$secondProfile(ItemStack var0, String var1) {
            if (ftboss$legacyOrdinary$selectedStage(var0) <= 0) {
                return "";
            } else {
                CompoundTag var2 = NativeLegacyEvolutionItemData.element(var0, "FTBossFoodEvolution");
                ListTag var3 = var2 == null ? new ListTag() : var2.getList("StageHistory", 10);
                int var4 = ftboss$legacyOrdinary$selectedStage(var0);
                return var4 < var3.size() ? var3.getCompound(var4).getString("Statue") : var1;
            }
        }

    public static List<WeaponDetailView> ftboss$legacyOrdinary$details(ItemStack var0) {
            if (FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                return List.of();
            } else {
                ArrayList var1 = new ArrayList();
                int var2 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0);
                CompoundTag var3 = NativeLegacyEvolutionItemData.element(var0, "FTBossFoodEvolution");
                ListTag var4 = var3 == null ? new ListTag() : var3.getList("StageHistory", 10);
                Aspect var5 = Aspect.fromId(FoodWeaponEvolution.ftboss$legacyOrdinary$firstProfileId(var0));
                if (var5 == null) {
                    return List.of();
                } else {
                    for (int var6 = 0; var6 < var2; var6++) {
                        String var7 = var6 < var4.size() ? var4.getCompound(var6).getString("Statue") : (var3 == null ? "" : var3.getString("SecondProfile"));
                        Aspect var8 = Aspect.fromId(var7);
                        if (var8 == null) {
                            var8 = var5;
                        }
    
                        Fusion var9 = WeaponEvolutionSkillPool.fusion(var5, var8);
                        MutableComponent var10 = var6 == 0 ? Component.translatable(var5.translationKey()) : Component.translatable(var9.primaryTranslationKey());
                        MutableComponent var11 = var6 == 0
                            ? Component.translatable(var5.descriptionKey())
                            : Component.translatable(
                                "screen.ftboss.evolution.fusion_pair",
                                Component.translatable(var9.primaryTranslationKey()),
                                Component.translatable(var9.secondaryTranslationKey())
                            );
                        var1.add(
                            new WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable("screen.ftboss.evolution.stage_skill", var6 + 1, var10),
                                var11,
                                var6,
                                ftboss$legacyOrdinary$selectedStage(var0) == var6
                            )
                        );
                    }
    
                    for (EvolutionSkill var13 : ftboss$legacyOrdinary$passiveSkills(var0)) {
                        var1.add(
                            new WeaponDetailView(
                                Component.translatable("screen.ftboss.evolution.passive"),
                                var13.nameComponent(),
                                FoodWeaponSkillRarity.ftboss$legacyOrdinary$passiveDescription(var0, FoodWeaponEvolution.ftboss$legacyOrdinary$skillId(var13), FoodWeaponEvolution.ftboss$legacyOrdinary$skillDescription(var13))
                            )
                        );
                    }
    
                    return List.copyOf(var1);
                }
            }
        }
}
