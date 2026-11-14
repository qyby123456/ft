/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.StatueWeaponUiDraw;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
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
    private final List<Button> submitButtons = new ArrayList<Button>();
    private Button toggle;
    private Button start;
    private Button claim;
    private boolean showing;

    StatueWeaponRepairPanel(Supplier<ItemStack> supplier, IntSupplier intSupplier, BiConsumer<Integer, Integer> biConsumer, Runnable runnable) {
        this.weapon = supplier;
        this.tint = intSupplier;
        this.send = biConsumer;
        this.changed = runnable;
    }

    void init(Consumer<Button> consumer, int n, int n2, int n3, int n4) {
        this.submitButtons.clear();
        this.toggle = Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.open"), button -> {
            this.showing = !this.showing;
            this.changed.run();
        }).m_252987_(n + n3 - 112, n2 - 1, 112, 18).m_253136_();
        consumer.accept(this.toggle);
        this.start = Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.start"), button -> this.send.accept(13, 0)).m_252987_(n + 15, n4, 96, 20).m_253136_();
        this.claim = Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.claim"), button -> this.send.accept(15, 0)).m_252987_(n + 120, n4, 120, 20).m_253136_();
        consumer.accept(this.start);
        consumer.accept(this.claim);
        for (int i = 0; i < 3; ++i) {
            int n5 = i;
            Button button2 = Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.submit"), button -> this.send.accept(14, n5)).m_252987_(n + n3 - 75, n2 + 92 + i * 34 + 6, 69, 20).m_253136_();
            this.submitButtons.add(button2);
            consumer.accept(button2);
        }
    }

    boolean isShowing() {
        return this.showing;
    }

    static boolean blocksChanges(ItemStack itemStack) {
        return !itemStack.m_41619_() && FoodWeaponLifecycle.isBroken(itemStack);
    }

    void update(boolean bl, boolean bl2) {
        if (this.toggle == null) {
            return;
        }
        ItemStack itemStack = this.weapon.get();
        FoodWeaponRepair.View view = FoodWeaponRepair.view(itemStack, this.tint.getAsInt());
        boolean bl3 = bl && this.showing;
        boolean bl4 = bl3 && !bl2 && !itemStack.m_41619_() && FoodWeaponLifecycle.weaponId(itemStack) != null && view.eligible() && view.correctStatue();
        this.toggle.f_93624_ = bl;
        boolean bl5 = this.toggle.f_93623_ = bl && !bl2;
        this.toggle.m_93666_((Component)Component.m_237115_((String)(this.showing ? "screen.ftboss.repair.back" : (StatueWeaponRepairPanel.blocksChanges(itemStack) ? "screen.ftboss.repair.required" : "screen.ftboss.repair.open"))));
        this.toggle.m_257544_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.repair.binding_hint")));
        this.start.f_93624_ = bl3;
        this.start.f_93623_ = bl4 && !view.active();
        this.claim.f_93624_ = bl3;
        this.claim.f_93623_ = bl4 && view.active() && view.ready();
        for (int i = 0; i < this.submitButtons.size(); ++i) {
            FoodWeaponRepair.TaskView taskView = StatueWeaponRepairPanel.task(view, i);
            Button button = this.submitButtons.get(i);
            button.f_93624_ = bl3 && view.active() && taskView != null;
            button.f_93623_ = bl4 && view.active() && taskView != null && !taskView.complete();
            button.m_93666_((Component)Component.m_237115_((String)(taskView != null && taskView.complete() ? "screen.ftboss.repair.complete" : "screen.ftboss.repair.submit")));
        }
    }

    private static FoodWeaponRepair.TaskView task(FoodWeaponRepair.View view, int n) {
        return view.tasks().stream().filter(taskView -> taskView.index() == n).findFirst().orElse(null);
    }

    List<Component> render(GuiGraphics guiGraphics, Font font, int n, int n2, int n3, int n4, int n5, int n6, boolean bl) {
        StatueWeaponUiDraw.heading(guiGraphics, font, n, n2, "screen.ftboss.repair.title", n4);
        ItemStack itemStack = this.weapon.get();
        if (itemStack.m_41619_()) {
            StatueWeaponUiDraw.text(guiGraphics, font, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.no_weapon"), n + 10, n2 + 36, n3 - 20, -6449784, 12, 4);
            return List.of();
        }
        FoodWeaponRepair.View view = FoodWeaponRepair.view(itemStack, this.tint.getAsInt());
        List<Component> list = List.of();
        guiGraphics.m_280509_(n, n2 + 21, n + n3, n2 + 75, -936301001);
        StatueWeaponUiDraw.weapon(guiGraphics, n + 8, n2 + 29, itemStack, n4);
        StatueWeaponUiDraw.text(guiGraphics, font, itemStack.m_41786_(), n + 34, n2 + 27, n3 - 44, -1515055, 10, 1);
        MutableComponent mutableComponent = Component.m_237115_((String)(FoodWeaponLifecycle.isBroken(itemStack) ? "screen.ftboss.repair.broken" : (itemStack.m_41773_() > 0 ? "screen.ftboss.repair.worn" : "screen.ftboss.repair.healthy")));
        MutableComponent mutableComponent2 = Component.m_237110_((String)"screen.ftboss.repair.wear", (Object[])new Object[]{FoodWeaponLifecycle.wearStep(itemStack)});
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent, n + 34, n2 + 39, n3 - 44, -26000, 10, 1);
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent2, n + 34, n2 + 51, n3 - 44, -6449784, 10, 1);
        String string = FoodWeaponLifecycle.repairProfile(itemStack);
        MutableComponent mutableComponent3 = string.isEmpty() ? Component.m_237115_((String)"screen.ftboss.repair.unbound") : Component.m_237110_((String)"screen.ftboss.repair.bound_statue", (Object[])new Object[]{Component.m_237115_((String)("profile.ftboss.evolution." + string))});
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent3, n + 8, n2 + 64, n3 - 16, -6449784, 10, 1);
        if (StatueWeaponUiDraw.inside(n5, n6, n, n2 + 21, n3, 54)) {
            list = List.of(mutableComponent, mutableComponent2, mutableComponent3, Component.m_237115_((String)"screen.ftboss.repair.binding_hint"), Component.m_237115_((String)(FoodWeaponLifecycle.requiresStatueRepair(itemStack) ? "screen.ftboss.repair.statue_only" : "screen.ftboss.repair.normal_repair")));
        }
        MutableComponent mutableComponent4 = Component.m_237110_((String)(bl ? "screen.ftboss.repair.pending" : (FoodWeaponLifecycle.weaponId(itemStack) == null ? "screen.ftboss.repair.syncing" : (!view.eligible() ? "screen.ftboss.repair.unavailable" : (!view.correctStatue() ? "screen.ftboss.repair.wrong_statue" : (!view.active() ? "screen.ftboss.repair.not_started" : (view.ready() ? "screen.ftboss.repair.ready" : "screen.ftboss.repair.progress")))))), (Object[])new Object[]{view.completed(), view.total()});
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent4, n + 1, n2 + 79, n3 - 2, -1917592, 10, 1);
        if (StatueWeaponUiDraw.inside(n5, n6, n, n2 + 77, n3, 13)) {
            list = List.of(mutableComponent4, mutableComponent3);
        }
        for (int i = 0; i < 3; ++i) {
            FoodWeaponRepair.TaskView taskView = StatueWeaponRepairPanel.task(view, i);
            int n7 = n2 + 92 + i * 34;
            guiGraphics.m_280509_(n, n7, n + n3, n7 + 32, -936301001);
            Component component = taskView == null ? Component.m_237110_((String)(view.active() ? "screen.ftboss.repair.syncing" : "screen.ftboss.repair.task_placeholder"), (Object[])new Object[]{i + 1}) : taskView.name();
            StatueWeaponUiDraw.text(guiGraphics, font, component, n + 8, n7 + 4, n3 - 91, -1515055, 10, 1);
            if (taskView == null) continue;
            MutableComponent mutableComponent5 = Component.m_237110_((String)"screen.ftboss.repair.task_progress", (Object[])new Object[]{taskView.progress(), taskView.required()});
            StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent5, n + 8, n7 + 19, n3 - 91, taskView.complete() ? -9250406 : -6449784, 10, 1);
            if (!StatueWeaponUiDraw.inside(n5, n6, n, n7, n3 - 80, 32)) continue;
            list = List.of(component, mutableComponent5);
        }
        MutableComponent mutableComponent6 = Component.m_237115_((String)"screen.ftboss.repair.losses");
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent6, n + 1, n2 + 194, n3 - 2, -26000, 10, 2);
        if (StatueWeaponUiDraw.inside(n5, n6, n, n2 + 194, n3, 20)) {
            list = List.of(mutableComponent6, Component.m_237115_((String)"screen.ftboss.repair.level_warning"), Component.m_237115_((String)"screen.ftboss.repair.cost_hint"));
        }
        return list;
    }

    void open() {
        this.showing = true;
    }
}

