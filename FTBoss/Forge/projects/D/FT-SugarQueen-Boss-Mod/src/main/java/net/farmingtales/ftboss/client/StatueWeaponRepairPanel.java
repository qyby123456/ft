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
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** 食武损耗/修复子页；2026-10-24 重绘主题卡片和任务进度，保留动作13/14/15与ACK等待。 */
final class StatueWeaponRepairPanel {
    private static final int START_ACTION = StatueMenuNetwork.ACTION_START_REPAIR;
    private static final int SUBMIT_ACTION = StatueMenuNetwork.ACTION_SUBMIT_REPAIR;
    private static final int CLAIM_ACTION = StatueMenuNetwork.ACTION_CLAIM_REPAIR;
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

    StatueWeaponRepairPanel(Supplier<ItemStack> weapon, IntSupplier tint, BiConsumer<Integer, Integer> send, Runnable changed) {
        this.weapon = weapon;
        this.tint = tint;
        this.send = send;
        this.changed = changed;
    }

    void init(Consumer<Button> add, int x, int y, int width, int footerY) {
        this.submitButtons.clear();
        this.toggle = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.open"), button -> {
            this.showing = !this.showing;
            this.changed.run();
        }, x + width - 112, y - 1, 112, 18, this::theme);
        add.accept(this.toggle);
        this.start = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.start"),
                button -> this.send.accept(START_ACTION, 0), x + 15, footerY, 96, 20, this::theme);
        this.claim = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.claim"),
                button -> this.send.accept(CLAIM_ACTION, 0), x + 120, footerY, 120, 20, this::theme);
        add.accept(this.start);
        add.accept(this.claim);
        for (int index = 0; index < TASK_COUNT; index++) {
            final int taskIndex = index;
            Button submit = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.submit"),
                    button -> this.send.accept(SUBMIT_ACTION, taskIndex),
                    x + width - 75, y + TASK_Y + index * TASK_SPACING + 6, 69, 20, this::theme);
            this.submitButtons.add(submit);
            add.accept(submit);
        }
    }

    boolean isShowing() { return this.showing; }

    private StatueUiTheme theme() { return StatueUiTheme.forTint(this.tint.getAsInt()); }

    void open() { this.showing = true; }

    static boolean blocksChanges(ItemStack weapon) {
        return !weapon.isEmpty() && FoodWeaponLifecycle.isBroken(weapon);
    }

    void update(boolean evolutionPage, boolean pending) {
        if (this.toggle == null) return;
        ItemStack stack = this.weapon.get();
        View view = FoodWeaponRepair.view(stack, this.tint.getAsInt());
        boolean visible = evolutionPage && this.showing;
        boolean allowed = visible && !pending && !stack.isEmpty() && FoodWeaponLifecycle.weaponId(stack) != null
                && view.eligible() && view.correctStatue();
        this.toggle.visible = evolutionPage;
        this.toggle.active = evolutionPage && !pending;
        this.toggle.setMessage(Component.translatable(this.showing ? "screen.ftboss.repair.back"
                : blocksChanges(stack) ? "screen.ftboss.repair.required" : "screen.ftboss.repair.open"));
        this.toggle.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")));
        this.start.visible = visible;
        this.start.active = allowed && !view.active();
        this.claim.visible = visible;
        this.claim.active = allowed && view.active() && view.ready();
        for (int index = 0; index < this.submitButtons.size(); index++) {
            TaskView task = task(view, index);
            Button button = this.submitButtons.get(index);
            button.visible = visible && view.active() && task != null;
            button.active = allowed && view.active() && task != null && !task.complete();
            button.setMessage(Component.translatable(task != null && task.complete()
                    ? "screen.ftboss.repair.complete" : "screen.ftboss.repair.submit"));
        }
    }

    private static TaskView task(View view, int index) {
        return view.tasks().stream().filter(task -> task.index() == index).findFirst().orElse(null);
    }

    List<Component> render(GuiGraphics graphics, Font font, int x, int y, int width, int accent,
                           int mouseX, int mouseY, boolean pending) {
        StatueUiTheme theme = theme();
        StatueWeaponUiDraw.heading(graphics, font, x, y, "screen.ftboss.repair.title", theme);
        ItemStack stack = this.weapon.get();
        if (stack.isEmpty()) {
            StatueThemeRenderer.card(graphics, x, y + 24, width, 72, theme, false);
            StatueWeaponUiDraw.text(graphics, font, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"),
                    x + 10, y + 36, width - 20, theme.muted, 12, 4);
            return List.of();
        }
        View view = FoodWeaponRepair.view(stack, this.tint.getAsInt());
        List<Component> hover = List.of();
        StatueThemeRenderer.card(graphics, x, y + 21, width, 54, theme, false);
        StatueWeaponUiDraw.weapon(graphics, x + 8, y + 29, stack, accent);
        StatueWeaponUiDraw.text(graphics, font, stack.getHoverName(), x + 34, y + 27, width - 44, theme.text, 10, 1);
        Component state = Component.translatable(FoodWeaponLifecycle.isBroken(stack) ? "screen.ftboss.repair.broken"
                : stack.getDamageValue() > 0 ? "screen.ftboss.repair.worn" : "screen.ftboss.repair.healthy");
        Component wear = Component.translatable("screen.ftboss.repair.wear", FoodWeaponLifecycle.wearStep(stack));
        StatueWeaponUiDraw.text(graphics, font, state, x + 34, y + 39, width - 44, 0xFFFF9A70, 10, 1);
        StatueWeaponUiDraw.text(graphics, font, wear, x + 34, y + 51, width - 44, theme.muted, 10, 1);
        String profile = FoodWeaponLifecycle.repairProfile(stack);
        Component binding = profile.isEmpty() ? Component.translatable("screen.ftboss.repair.unbound")
                : Component.translatable("screen.ftboss.repair.bound_statue", Component.translatable("profile.ftboss.evolution." + profile));
        StatueWeaponUiDraw.text(graphics, font, binding, x + 8, y + 63, width - 16, theme.muted, 10, 1);
        if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, y + 21, width, 54)) {
            hover = List.of(state, wear, binding, Component.translatable("screen.ftboss.repair.binding_hint"),
                    Component.translatable(FoodWeaponLifecycle.requiresStatueRepair(stack)
                            ? "screen.ftboss.repair.statue_only" : "screen.ftboss.repair.normal_repair"));
        }
        Component status = Component.translatable(pending ? "screen.ftboss.repair.pending"
                : FoodWeaponLifecycle.weaponId(stack) == null ? "screen.ftboss.repair.syncing" : !view.eligible()
                ? "screen.ftboss.repair.unavailable" : !view.correctStatue() ? "screen.ftboss.repair.wrong_statue"
                : !view.active() ? "screen.ftboss.repair.not_started" : view.ready() ? "screen.ftboss.repair.ready"
                : "screen.ftboss.repair.progress", view.completed(), view.total());
        StatueWeaponUiDraw.text(graphics, font, status, x + 1, y + 79, width - 2, theme.accentBright, 10, 1);
        if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, y + 77, width, 13)) hover = List.of(status, binding);
        for (int index = 0; index < TASK_COUNT; index++) {
            TaskView task = task(view, index);
            int cardY = y + TASK_Y + index * TASK_SPACING;
            StatueThemeRenderer.card(graphics, x, cardY, width, TASK_HEIGHT, theme, task != null && task.complete());
            Component name = task == null ? Component.translatable(view.active()
                    ? "screen.ftboss.repair.syncing" : "screen.ftboss.repair.task_placeholder", index + 1) : task.name();
            StatueWeaponUiDraw.text(graphics, font, name, x + 8, cardY + 4, width - 91, theme.text, 10, 1);
            if (task != null) {
                Component progress = Component.translatable("screen.ftboss.repair.task_progress", task.progress(), task.required());
                StatueWeaponUiDraw.text(graphics, font, progress, x + 8, cardY + 18, width - 91,
                        task.complete() ? 0xFF72D99A : theme.muted, 10, 1);
                double ratio = task.required() <= 0 ? 0.0D : task.progress() / (double) task.required();
                StatueWeaponUiDraw.progress(graphics, x + 8, cardY + 28, width - 91, 3, ratio, theme,
                        task.complete() ? 0xFF72D99A : theme.accent);
                if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, cardY, width - 80, TASK_HEIGHT)) hover = List.of(name, progress);
            }
        }
        Component losses = Component.translatable("screen.ftboss.repair.losses");
        StatueWeaponUiDraw.text(graphics, font, losses, x + 1, y + WARNING_Y, width - 2, 0xFFFF9A70, 10, 2);
        if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, y + WARNING_Y, width, 20)) {
            hover = List.of(losses, Component.translatable("screen.ftboss.repair.level_warning"),
                    Component.translatable("screen.ftboss.repair.cost_hint"));
        }
        return hover;
    }
}
