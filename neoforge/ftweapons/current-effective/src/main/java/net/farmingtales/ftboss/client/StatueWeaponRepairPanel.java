package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponRepair.TaskView;
import net.farmingtales.ftboss.item.FoodWeaponRepair.View;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

final class StatueWeaponRepairPanel {
    private static final int START_ACTION = 13;
    private static final int SUBMIT_ACTION = 14;
    private static final int CLAIM_ACTION = 15;
    private static final int TASK_COUNT = 3;
    private static final int TASK_Y = 92;
    private static final int TASK_SPACING = 34;
    private static final int TASK_HEIGHT = 32;
    private static final int WARNING_Y = 194;
    private final Supplier<ItemStack> weapon;
    private final IntSupplier tint;
    private final BiConsumer<Integer, Integer> send;
    private final Runnable changed;
    private final List<Button> submitButtons = new ArrayList<>();
    private Button toggle;
    private Button start;
    private Button claim;
    private boolean showing;

    StatueWeaponRepairPanel(Supplier<ItemStack> var1, IntSupplier var2, BiConsumer<Integer, Integer> var3, Runnable var4) {
        this.weapon = var1;
        this.tint = var2;
        this.send = var3;
        this.changed = var4;
    }

    void init(Consumer<Button> var1, int var2, int var3, int var4, int var5) {
        this.submitButtons.clear();
        this.toggle = Button.builder(Component.translatable("screen.ftboss.repair.open"), var1x -> {
            this.showing = !this.showing;
            this.changed.run();
        }).bounds(var2 + var4 - 112, var3 - 1, 112, 18).build();
        var1.accept(this.toggle);
        this.start = Button.builder(Component.translatable("screen.ftboss.repair.start"), var1x -> this.send.accept(13, 0))
            .bounds(var2 + 15, var5, 96, 20)
            .build();
        this.claim = Button.builder(Component.translatable("screen.ftboss.repair.claim"), var1x -> this.send.accept(15, 0))
            .bounds(var2 + 120, var5, 120, 20)
            .build();
        var1.accept(this.start);
        var1.accept(this.claim);

        for (int var6 = 0; var6 < 3; var6++) {
            int var7 = var6;
            Button var8 = Button.builder(Component.translatable("screen.ftboss.repair.submit"), var2x -> this.send.accept(14, var7))
                .bounds(var2 + var4 - 75, var3 + 92 + var6 * 34 + 6, 69, 20)
                .build();
            this.submitButtons.add(var8);
            var1.accept(var8);
        }
    }

    boolean isShowing() {
        return this.showing;
    }

    static boolean blocksChanges(ItemStack var0) {
        return !var0.isEmpty() && FoodWeaponLifecycle.isBroken(var0);
    }

    void update(boolean var1, boolean var2) {
        if (this.toggle != null) {
            ItemStack var3 = this.weapon.get();
            View var4 = FoodWeaponRepair.view(var3, this.tint.getAsInt());
            boolean var5 = var1 && this.showing;
            boolean var6 = var5 && !var2 && !var3.isEmpty() && FoodWeaponLifecycle.weaponId(var3) != null && var4.eligible() && var4.correctStatue();
            this.toggle.visible = var1;
            this.toggle.active = var1 && !var2;
            this.toggle
                .setMessage(
                    Component.translatable(
                        this.showing ? "screen.ftboss.repair.back" : (blocksChanges(var3) ? "screen.ftboss.repair.required" : "screen.ftboss.repair.open")
                    )
                );
            this.toggle.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")));
            this.start.visible = var5;
            this.start.active = var6 && !var4.active();
            this.claim.visible = var5;
            this.claim.active = var6 && var4.active() && var4.ready();

            for (int var7 = 0; var7 < this.submitButtons.size(); var7++) {
                TaskView var8 = task(var4, var7);
                Button var9 = this.submitButtons.get(var7);
                var9.visible = var5 && var4.active() && var8 != null;
                var9.active = var6 && var4.active() && var8 != null && !var8.complete();
                var9.setMessage(Component.translatable(var8 != null && var8.complete() ? "screen.ftboss.repair.complete" : "screen.ftboss.repair.submit"));
            }
        }
    }

    private static TaskView task(View var0, int var1) {
        return var0.tasks().stream().filter(var1x -> var1x.index() == var1).findFirst().orElse(null);
    }

    List<Component> render(GuiGraphics var1, Font var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9) {
        StatueWeaponUiDraw.heading(var1, var2, var3, var4, "screen.ftboss.repair.title", var6);
        ItemStack var10 = this.weapon.get();
        if (var10.isEmpty()) {
            StatueWeaponUiDraw.text(
                var1, var2, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"), var3 + 10, var4 + 36, var5 - 20, -6449784, 12, 4
            );
            return List.of();
        } else {
            View var11 = FoodWeaponRepair.view(var10, this.tint.getAsInt());
            List var12 = List.of();
            var1.fill(var3, var4 + 21, var3 + var5, var4 + 75, -936301001);
            StatueWeaponUiDraw.weapon(var1, var3 + 8, var4 + 29, var10, var6);
            StatueWeaponUiDraw.text(var1, var2, var10.getHoverName(), var3 + 34, var4 + 27, var5 - 44, -1515055, 10, 1);
            MutableComponent var13 = Component.translatable(
                FoodWeaponLifecycle.isBroken(var10)
                    ? "screen.ftboss.repair.broken"
                    : (var10.getDamageValue() > 0 ? "screen.ftboss.repair.worn" : "screen.ftboss.repair.healthy")
            );
            MutableComponent var14 = Component.translatable("screen.ftboss.repair.wear", FoodWeaponLifecycle.wearStep(var10));
            StatueWeaponUiDraw.text(var1, var2, var13, var3 + 34, var4 + 39, var5 - 44, -26000, 10, 1);
            StatueWeaponUiDraw.text(var1, var2, var14, var3 + 34, var4 + 51, var5 - 44, -6449784, 10, 1);
            String var15 = FoodWeaponLifecycle.repairProfile(var10);
            MutableComponent var16 = var15.isEmpty()
                ? Component.translatable("screen.ftboss.repair.unbound")
                : Component.translatable("screen.ftboss.repair.bound_statue", Component.translatable("profile.ftboss.evolution." + var15));
            StatueWeaponUiDraw.text(var1, var2, var16, var3 + 8, var4 + 64, var5 - 16, -6449784, 10, 1);
            if (StatueWeaponUiDraw.inside((double)var7, (double)var8, var3, var4 + 21, var5, 54)) {
                var12 = List.of(
                    var13,
                    var14,
                    var16,
                    Component.translatable("screen.ftboss.repair.binding_hint"),
                    Component.translatable(
                        FoodWeaponLifecycle.requiresStatueRepair(var10) ? "screen.ftboss.repair.statue_only" : "screen.ftboss.repair.normal_repair"
                    )
                );
            }

            MutableComponent var17 = Component.translatable(
                var9
                    ? "screen.ftboss.repair.pending"
                    : (
                        FoodWeaponLifecycle.weaponId(var10) == null
                            ? "screen.ftboss.repair.syncing"
                            : (
                                !var11.eligible()
                                    ? "screen.ftboss.repair.unavailable"
                                    : (
                                        !var11.correctStatue()
                                            ? "screen.ftboss.repair.wrong_statue"
                                            : (
                                                !var11.active()
                                                    ? "screen.ftboss.repair.not_started"
                                                    : (var11.ready() ? "screen.ftboss.repair.ready" : "screen.ftboss.repair.progress")
                                            )
                                    )
                            )
                    ),
                var11.completed(),
                var11.total()
            );
            StatueWeaponUiDraw.text(var1, var2, var17, var3 + 1, var4 + 79, var5 - 2, -1917592, 10, 1);
            if (StatueWeaponUiDraw.inside((double)var7, (double)var8, var3, var4 + 77, var5, 13)) {
                var12 = List.of(var17, var16);
            }

            for (int var18 = 0; var18 < 3; var18++) {
                TaskView var19 = task(var11, var18);
                int var20 = var4 + 92 + var18 * 34;
                var1.fill(var3, var20, var3 + var5, var20 + 32, -936301001);
                Object var21 = var19 == null
                    ? Component.translatable(var11.active() ? "screen.ftboss.repair.syncing" : "screen.ftboss.repair.task_placeholder", var18 + 1)
                    : var19.name();
                StatueWeaponUiDraw.text(var1, var2, (Component)var21, var3 + 8, var20 + 4, var5 - 91, -1515055, 10, 1);
                if (var19 != null) {
                    MutableComponent var22 = Component.translatable("screen.ftboss.repair.task_progress", var19.progress(), var19.required());
                    StatueWeaponUiDraw.text(var1, var2, var22, var3 + 8, var20 + 19, var5 - 91, var19.complete() ? -9250406 : -6449784, 10, 1);
                    if (StatueWeaponUiDraw.inside((double)var7, (double)var8, var3, var20, var5 - 80, 32)) {
                        var12 = List.of(var21, var22);
                    }
                }
            }

            MutableComponent var23 = Component.translatable("screen.ftboss.repair.losses");
            StatueWeaponUiDraw.text(var1, var2, var23, var3 + 1, var4 + 194, var5 - 2, -26000, 10, 2);
            if (StatueWeaponUiDraw.inside((double)var7, (double)var8, var3, var4 + 194, var5, 20)) {
                var12 = List.of(var23, Component.translatable("screen.ftboss.repair.level_warning"), Component.translatable("screen.ftboss.repair.cost_hint"));
            }

            return var12;
        }
    }

    void open() {
        this.showing = true;
    }
}
