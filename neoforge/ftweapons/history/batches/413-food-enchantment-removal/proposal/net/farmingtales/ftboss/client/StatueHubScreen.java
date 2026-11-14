package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodBlessingView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.SocketView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.StatueDailyView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.network.StatueMenuNetwork.Action;
import net.farmingtales.ftboss.network.StatueMenuNetwork.Open;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class StatueHubScreen extends Screen {
    private static final int PANEL_WIDTH = 490;
    private static final int PANEL_HEIGHT = 300;
    private static final int SIDEBAR_WIDTH = 116;
    private static final int HEADER_HEIGHT = 42;
    private static final int SLOT = 18;
    private static final int SOCKET_COLUMNS = 10;
    private static final int SOCKET_ROWS = 3;
    private static final int INGREDIENT_COLUMNS = 9;
    private static final int INGREDIENT_ROWS = 2;
    private static final int DAILY_COLUMNS = 9;
    private static final int DAILY_ROWS = 2;
    private static final int DAILY_TASKS_PER_PAGE = 4;
    private static final int DETAILS_PER_PAGE = 4;
    private static final int TEXT = -1515055;
    private static final int MUTED = -6449784;
    private static final int PANEL = -266856677;
    private static final int PANEL_INNER = -400285143;
    private static final int CARD = -936301001;
    private final BlockPos statuePos;
    private int tint;
    private boolean finaleUnlocked;
    private boolean finaleSelected;
    private Component statueName;
    private List<StatueDailyView> dailyTasks;
    private boolean white;
    private boolean gold;
    private boolean goldAvailable;
    private StatueHubScreen.Section section = StatueHubScreen.Section.DAILY;
    private int left;
    private int top;
    private int ingredientPage;
    private int dailyPage;
    private int dailyTaskPage;
    private int detailPage;
    private boolean actionPending;
    private Button evolveButton;
    private Button claimButton;
    private Button colorButton;
    private List<Component> hoverTooltip = List.of();
    private final StatueWeaponRepairPanel repairPanel = new StatueWeaponRepairPanel(this::evolutionWeapon, () -> this.tint, this::sendRepair, this::updateActionButtons);

    private StatueHubScreen(Open var1) {
        super(Component.translatable("screen.ftboss.statue_hub.title"));
        this.statuePos = var1.pos();
        this.apply(var1);
    }

    private void apply(Open var1) {
        this.tint = var1.tint();
        this.white = var1.white();
        this.gold = var1.gold();
        this.goldAvailable = var1.goldAvailable();
        this.finaleUnlocked = var1.finaleUnlocked();
        this.finaleSelected = var1.finaleSelected();
        this.statueName = var1.statueName();
        this.dailyTasks = List.copyOf(var1.dailyTasks());
        this.dailyTaskPage = Math.min(this.dailyTaskPage, this.dailyTaskPages() - 1);
        this.actionPending = false;
        this.updateActionButtons();
    }

    public static void acknowledgeAction(BlockPos var0) {
        Screen var1 = Minecraft.getInstance().screen;
        if (var1 instanceof StatueSwordCavityScreen var2 && var2.matches(var0)) {
            var2.acknowledgeAction();
            return;
        }

        if (var1 instanceof StatueHubScreen var3 && var3.statuePos.equals(var0)) {
            var3.actionPending = false;
            var3.updateActionButtons();
            var3.updateColorButton();
        }
    }

    public static void open(Open var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.screen instanceof SwordCavityContainerScreen cavity && cavity.getMenu().matches(var0.pos())) return;
        if (var1.screen instanceof StatueSwordCavityScreen cavity && cavity.matches(var0.pos())) {
            cavity.parent().apply(var0);
            return;
        }
        if (var1.screen instanceof StatueHubScreen var2 && var2.statuePos.equals(var0.pos())) {
            var2.apply(var0);
            return;
        }

        var1.setScreen(new StatueHubScreen(var0));
        StatueWeaponRepairNavigation.bootstrapHub(var0.pos());
    }

    public static void updateAppearance(BlockPos var0, boolean var1, boolean var2) {
        if (Minecraft.getInstance().screen instanceof StatueHubScreen var4 && var4.statuePos.equals(var0)) {
            var4.white = var1;
            var4.gold = var2;
            var4.updateColorButton();
        }
    }

    @Override
    protected void init() {
        this.left = (this.width - 490) / 2;
        this.top = (this.height - 300) / 2;
        int var1 = this.left + 11;
        int var2 = this.top + 42 + 8;
        int var3 = 0;

        for (StatueHubScreen.Section var7 : StatueHubScreen.Section.values()) {
            this.addRenderableWidget(Button.builder(Component.translatable(var7.key), var2x -> {
                this.section = var7;
                this.actionPending = false;
                this.updateActionButtons();
            }).bounds(var1, var2 + var3 * 34, 94, 25).build());
            var3++;
        }

        this.colorButton = this.addRenderableWidget(Button.builder(this.colorLabel(), var1x -> {
            Action.send(this.statuePos, 0);
            var1x.active = false;
        }).bounds(this.left + 490 - 104, this.top + 300 - 29, 88, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, var1x -> this.onClose()).bounds(this.left + 12, this.top + 300 - 29, 72, 20).build());
        this.claimButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.statue_hub.evolution.claim"), var1x -> {
            Action.send(this.statuePos, 6);
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.left + 145, this.top + 300 - 29, 96, 20).build());
        this.evolveButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.statue_hub.evolution.evolve"), var1x -> {
            Action.send(this.statuePos, 5);
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.left + 250, this.top + 300 - 29, 120, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.statue_hub.tab.material"), button -> this.openSwordCavity()).bounds(this.left + 11, this.top + 42 + 8 + StatueHubScreen.Section.values().length * 34, 94, 25).build());
        this.repairPanel.init(button -> this.addRenderableWidget(button), this.contentX(), this.contentY(), this.contentWidth(), this.top + 300 - 29);
        StatueWeaponRepairNavigation.openPending(this, this.statuePos, this.evolutionWeapon());
        this.updateActionButtons();
        this.addOrdinaryHubEntry();
    }

    private void updateActionButtons() {
        if (this.evolveButton != null && this.claimButton != null) {
            boolean var1 = this.section == StatueHubScreen.Section.EVOLUTION && !this.repairPanel.isShowing();
            this.repairPanel.update(this.section == StatueHubScreen.Section.EVOLUTION, this.actionPending);
            boolean var2 = !this.evolutionWeapon().isEmpty();
            this.claimButton.visible = var1;
            this.claimButton.active = var1 && !this.actionPending && var2;
            this.evolveButton.visible = var1;
            this.evolveButton.active = var1 && !this.actionPending && var2;
        }
    }

    private Component colorLabel() {
        if (this.gold) {
            return Component.translatable("screen.ftboss.statue_hub.color.restore");
        } else {
            return this.white && this.goldAvailable
                ? Component.translatable("screen.ftboss.statue_hub.color.gold")
                : Component.translatable(this.white ? "screen.ftboss.statue_hub.color.restore" : "screen.ftboss.statue_hub.color.white");
        }
    }

    private void updateColorButton() {
        if (this.colorButton != null) {
            this.colorButton.setMessage(this.colorLabel());
            this.colorButton.active = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null) {
            if (this.minecraft
                        .player
                        .distanceToSqr((double)this.statuePos.getX() + 0.5, (double)this.statuePos.getY() + 1.0, (double)this.statuePos.getZ() + 0.5)
                    > 100.0
                || !(this.minecraft.level.getBlockState(this.statuePos).getBlock() instanceof CandyStatueBlock)) {
                this.onClose();
            }
        }
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.renderBackground(var1, var2, var3, var4);
        this.hoverTooltip = List.of();
        this.updateActionButtons();
        int var5 = accent(this.tint);
        var1.fill(this.left - 2, this.top - 2, this.left + 490 + 2, this.top + 300 + 2, -805306368 | var5);
        var1.fillGradient(this.left, this.top, this.left + 490, this.top + 300, -266856677, -267580400);
        var1.fill(this.left, this.top, this.left + 490, this.top + 42, -534108368);
        var1.fill(this.left, this.top, this.left + 5, this.top + 42, 0xFF000000 | var5);
        var1.fill(this.left + 8, this.top + 42 + 4, this.left + 116, this.top + 300 - 36, -1978264294);
        var1.drawString(this.font, this.title, this.left + 17, this.top + 10, -1515055, false);
        String var6 = this.gold
            ? "screen.ftboss.statue_hub.color.gold_state"
            : (this.white ? "screen.ftboss.statue_hub.color.white_state" : "screen.ftboss.statue_hub.color.colored_state");
        MutableComponent var7 = Component.translatable("screen.ftboss.statue_hub.subtitle", this.statueName, Component.translatable(var6));
        var1.drawString(this.font, var7, this.left + 17, this.top + 25, -6449784, false);
        int var8 = this.top + 42 + 8 + this.section.ordinal() * 34;
        var1.fill(this.left + 8, var8, this.left + 11, var8 + 25, 0xFF000000 | var5);
        int var9 = this.contentX();
        int var10 = this.contentY();
        int var11 = this.contentWidth();
        var1.fill(var9 - 5, var10 - 4, var9 + var11 + 5, this.top + 300 - 36, -400285143);
        switch (this.section) {
            case DAILY:
                this.renderDaily(var1, var9, var10, var11, var5, var2, var3);
                break;
            case CHALLENGE:
                this.renderChallenge(var1, var9, var10, var11, var5, var2, var3);
                break;

            case EVOLUTION:
                this.renderEvolution(var1, var9, var10, var11, var5, var2, var3);
                break;
            case DETAILS:
                this.renderWeaponDetails(var1, var9, var10, var11, var5, var2, var3);
        }

        int var12 = this.left + 490 - 114;
        int var13 = this.top + 300 - 25;
        var1.fill(var12, var13, var12 + 8, var13 + 8, this.gold ? -12470 : (this.white ? -855832 : 0xFF000000 | var5));

        for (GuiEventListener var15 : this.children()) {
            if (var15 instanceof Renderable var16) {
                var16.render(var1, var2, var3, var4);
            }
        }

        if (!this.hoverTooltip.isEmpty()) {
            var1.renderTooltip(this.font, this.hoverTooltip, Optional.empty(), var2, var3);
        }
    }

    private int contentX() {
        return this.left + 116 + 13;
    }

    private int contentY() {
        return this.top + 42 + 8;
    }

    private int contentWidth() {
        return 348;
    }

    private void renderDaily(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        this.heading(var1, var2, var3, "screen.ftboss.statue_hub.daily.title", var5);
        int var8 = this.dailyTaskPages();
        this.dailyTaskPage = Math.min(this.dailyTaskPage, var8 - 1);
        int var9 = var2 + var4 - 96;
        int var10 = var2 + var4 - 18;
        var1.fill(var9, var3 - 2, var9 + 18, var3 + 11, this.dailyTaskPage > 0 ? -936301001 : -400285143);
        var1.fill(var10, var3 - 2, var10 + 18, var3 + 11, this.dailyTaskPage + 1 < var8 ? -936301001 : -400285143);
        var1.drawCenteredString(this.font, "<", var9 + 9, var3, this.dailyTaskPage > 0 ? -1515055 : -6449784);
        var1.drawCenteredString(this.font, ">", var10 + 9, var3, this.dailyTaskPage + 1 < var8 ? -1515055 : -6449784);
        MutableComponent var11 = Component.translatable("screen.ftboss.statue_hub.daily.task_page", this.dailyTaskPage + 1, var8);
        var1.drawCenteredString(this.font, var11, var2 + var4 - 48, var3, -6449784);
        int var12 = var3 + 20;
        if (this.dailyTasks.isEmpty()) {
            this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.daily.none"), var2 + 4, var12 + 5, var4 - 8, -6449784, 11, 4);
        }

        int var13 = this.dailyTaskPage * 4;

        for (int var14 = 0; var14 < 4; var14++) {
            int var15 = var13 + var14;
            if (var15 >= this.dailyTasks.size()) {
                break;
            }

            StatueDailyView var16 = this.dailyTasks.get(var15);
            int var17 = var16.state() == 2 ? -8947849 : (var16.state() == 1 ? -10103922 : -1917592);
            var1.fill(var2, var12, var2 + var4, var12 + 26, -936301001);
            var1.fill(var2, var12, var2 + 3, var12 + 26, var17);
            MutableComponent var18 = Component.translatable("screen.ftboss.statue_hub.daily.task", var15 + 1, var16.request());
            List var19 = this.font.split(var18, var4 - 16);
            if (!var19.isEmpty()) {
                var1.drawString(this.font, (FormattedCharSequence)var19.get(0), var2 + 8, var12 + 3, -1515055, false);
            }

            MutableComponent var20 = var16.state() == 2
                ? Component.translatable("screen.ftboss.statue_hub.daily.claimed")
                : (
                    var16.state() == 1
                        ? Component.translatable("screen.ftboss.statue_hub.daily.ready")
                        : Component.translatable("screen.ftboss.statue_hub.daily.progress", var16.progress(), var16.required())
                );
            MutableComponent var21 = var20.copy()
                .append(Component.literal(" · "))
                .append(Component.translatable("screen.ftboss.statue_hub.daily.reward", var16.reward()));
            List var22 = this.font.split(var21, var4 - 16);
            if (!var22.isEmpty()) {
                var1.drawString(this.font, (FormattedCharSequence)var22.get(0), var2 + 8, var12 + 14, var17, false);
            }

            var12 += 29;
        }

        List var24 = this.dailyFoodSlots();
        byte var25 = 18;
        int var26 = Math.max(1, (var24.size() + var25 - 1) / var25);
        this.dailyPage = Math.min(this.dailyPage, var26 - 1);
        var1.drawString(
            this.font,
            Component.translatable("screen.ftboss.statue_hub.daily.available", var24.size(), this.dailyPage + 1, var26),
            var2 + 1,
            var3 + 154,
            -1917592,
            false
        );
        int var27 = this.dailyPage * var25;
        int var28 = var3 + 170;

        for (int var29 = 0; var29 < var25; var29++) {
            int var30 = var2 + var29 % 9 * 18;
            int var31 = var28 + var29 / 9 * 18;
            int var32 = var27 + var29;
            ItemStack var23 = var32 < var24.size() && this.minecraft != null && this.minecraft.player != null
                ? this.minecraft.player.getInventory().getItem((Integer)var24.get(var32))
                : ItemStack.EMPTY;
            this.drawSlot(var1, var30, var31, var23, var5, false);
            if (inside((double)var6, (double)var7, var30, var31, 18, 18) && !var23.isEmpty()) {
                this.hoverTooltip = this.dailyFoodTooltip(var23);
            }
        }

        var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.daily.hint"), var2 + 172, var3 + 183, -6449784, false);
        var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.daily.page_hint"), var2 + 172, var3 + 198, -6449784, false);
    }

    private int dailyTaskPages() {
        return Math.max(1, (this.dailyTasks.size() + 4 - 1) / 4);
    }

    private List<Integer> dailyFoodSlots() {
        ArrayList var1 = new ArrayList();
        if (this.minecraft != null && this.minecraft.player != null) {
            for (int var2 = 0; var2 < 36; var2++) {
                ItemStack var3 = this.minecraft.player.getInventory().getItem(var2);
                if (FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, var3, this.tint)) {
                    var1.add(var2);
                }
            }

            ItemStack var4 = this.minecraft.player.getInventory().getItem(40);
            if (FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, var4, this.tint)) {
                var1.add(40);
            }

            return var1;
        } else {
            return var1;
        }
    }

    private List<Component> dailyFoodTooltip(ItemStack var1) {
        return List.of(var1.getHoverName(), Component.translatable("screen.ftboss.statue_hub.daily.submit_click"));
    }

    private void renderChallenge(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        this.heading(var1, var2, var3, "screen.ftboss.statue_hub.challenge.title", var5);
        boolean var8 = inside((double)var6, (double)var7, var2, var3 + 23, var4, 50);
        boolean var9 = inside((double)var6, (double)var7, var2, var3 + 79, var4, 61);
        int var10 = !this.finaleSelected ? -9381480 : (var8 ? 0xFF000000 | var5 : -11909552);
        var1.fill(var2, var3 + 23, var2 + var4, var3 + 73, var10);
        var1.fill(var2 + 2, var3 + 25, var2 + var4 - 2, var3 + 71, -936301001);
        var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.challenge.normal"), var2 + 10, var3 + 31, -9381480, false);
        MutableComponent var11 = Component.translatable(
            this.finaleSelected ? "screen.ftboss.statue_hub.challenge.click_select" : "screen.ftboss.statue_hub.challenge.selected"
        );
        var1.drawString(this.font, var11, var2 + var4 - 10 - this.font.width(var11), var3 + 31, !this.finaleSelected && !var8 ? -9381480 : -1515055, false);
        this.wrapped(
            var1, Component.translatable("screen.ftboss.statue_hub.challenge.normal_desc", this.statueName), var2 + 10, var3 + 46, var4 - 20, -6449784, 11, 2
        );
        int var12 = this.finaleUnlocked ? -38031 : -8949123;
        int var13 = this.finaleSelected ? -38031 : (var9 && this.finaleUnlocked ? 0xFF000000 | var5 : -11909552);
        var1.fill(var2, var3 + 79, var2 + var4, var3 + 140, var13);
        var1.fill(var2 + 2, var3 + 81, var2 + var4 - 2, var3 + 138, -936301001);
        var1.drawString(
            this.font,
            Component.translatable(
                this.finaleUnlocked ? "screen.ftboss.statue_hub.challenge.finale_unlocked" : "screen.ftboss.statue_hub.challenge.finale_locked"
            ),
            var2 + 10,
            var3 + 87,
            var12,
            false
        );
        MutableComponent var14 = Component.translatable(
            !this.finaleUnlocked
                ? "screen.ftboss.statue_hub.challenge.locked"
                : (this.finaleSelected ? "screen.ftboss.statue_hub.challenge.selected" : "screen.ftboss.statue_hub.challenge.click_select")
        );
        var1.drawString(this.font, var14, var2 + var4 - 10 - this.font.width(var14), var3 + 87, this.finaleSelected ? -38031 : -6449784, false);
        this.wrapped(
            var1, Component.translatable("screen.ftboss.statue_hub.challenge.finale_desc", this.statueName), var2 + 10, var3 + 102, var4 - 20, -6449784, 11, 3
        );
        this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.challenge.hint"), var2 + 2, var3 + 149, var4 - 4, -6449784, 11, 4);
    }



    private void renderEvolution(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        if (this.repairPanel.isShowing()) {
            this.hoverTooltip = this.repairPanel.render(var1, this.font, var2, var3, var4, var5, var6, var7, this.actionPending);
            return;
        }
        this.heading(var1, var2, var3, "screen.ftboss.statue_hub.evolution.title", var5);
        ItemStack var8 = this.evolutionWeapon();
        if (var8.isEmpty()) {
            var1.fill(var2, var3 + 24, var2 + var4, var3 + 96, -936301001);
            this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"), var2 + 10, var3 + 36, var4 - 20, -6449784, 12, 4);
        } else {
            var1.fill(var2, var3 + 21, var2 + var4, var3 + 82, -936301001);
            this.drawSlot(var1, var2 + 8, var3 + 30, var8, var5, false);
            var1.drawString(this.font, var8.getHoverName(), var2 + 34, var3 + 27, -1515055, false);
            int var9 = FoodWeaponEvolution.evolutionLevel(var8);
            int var10 = FoodWeaponEvolution.maxEvolutionLevel(var8);
            int var11 = FoodWeaponEvolution.evolutionExperience(var8);
            int var12 = FoodWeaponEvolution.nextEvolutionRequirement(var8);
            int var13 = FoodWeaponEvolution.currentEvolutionFloor(var8);
            var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.level", var9, var10), var2 + 34, var3 + 39, -1917592, false);
            this.drawExperienceBar(var1, var2 + 34, var3 + 53, var4 - 44, var11, var13, var12, var9 >= var10, var5);
            MutableComponent var14 = var9 >= var10
                ? Component.translatable("screen.ftboss.statue_hub.evolution.maxed")
                : Component.translatable("screen.ftboss.statue_hub.evolution.xp", var11, var12);
            var1.drawCenteredString(this.font, var14, var2 + 34 + (var4 - 44) / 2, var3 + 65, -1515055);
            List var15 = FoodWeaponEvolution.socketViews(var8);
            int var16 = FoodWeaponEvolution.socketCapacity(var8);
            var1.drawString(
                this.font, Component.translatable("screen.ftboss.statue_hub.evolution.sockets_full", var15.size(), var16), var2 + 1, var3 + 88, -9250406, false
            );
            int var17 = var3 + 101;

            for (int var18 = 0; var18 < Math.min(var16, 30); var18++) {
                int var19 = var2 + var18 % 10 * 18;
                int var20 = var17 + var18 / 10 * 18;
                SocketView var21 = var18 < var15.size() ? (SocketView)var15.get(var18) : null;
                this.drawAccessorySlot(var1, var19, var20, var21, var5, var18 + 1);
                if (inside((double)var6, (double)var7, var19, var20, 18, 18)) {
                    this.hoverTooltip = this.socketTooltip(var21, var18 + 1);
                }
            }

            int var29 = var2 + 180 + 10;
            var1.fill(var29, var3 + 101, var2 + var4, var3 + 155, -1725948635);
            this.wrapped(
                var1,
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_help"),
                var29 + 7,
                var3 + 108,
                var4 - (var29 - var2) - 14,
                -6449784,
                11,
                4
            );
            List var30 = this.ingredientSlots(var8);
            byte var31 = 18;
            int var32 = Math.max(1, (var30.size() + var31 - 1) / var31);
            this.ingredientPage = Math.min(this.ingredientPage, var32 - 1);
            var1.drawString(
                this.font,
                Component.translatable("screen.ftboss.statue_hub.evolution.ingredients", var30.size(), this.ingredientPage + 1, var32),
                var2 + 1,
                var3 + 164,
                -1917592,
                false
            );
            int var22 = var3 + 176;
            int var23 = this.ingredientPage * var31;

            for (int var24 = 0; var24 < var31; var24++) {
                int var25 = var2 + var24 % 9 * 18;
                int var26 = var22 + var24 / 9 * 18;
                int var27 = var23 + var24;
                ItemStack var28 = var27 < var30.size() && this.minecraft != null && this.minecraft.player != null
                    ? this.minecraft.player.getInventory().getItem((Integer)var30.get(var27))
                    : ItemStack.EMPTY;
                this.drawSlot(var1, var25, var26, var28, var5, false);
                if (inside((double)var6, (double)var7, var25, var26, 18, 18) && !var28.isEmpty()) {
                    this.hoverTooltip = this.ingredientTooltip(var28);
                }
            }

            var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.help"), var2 + 172, var3 + 183, -6449784, false);
            var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.page_hint"), var2 + 172, var3 + 198, -6449784, false);
        }
    }

    private void renderWeaponDetails(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        this.heading(var1, var2, var3, "screen.ftboss.statue_hub.details.title", var5);
        ItemStack var8 = this.evolutionWeapon();
        if (var8.isEmpty()) {
            var1.fill(var2, var3 + 24, var2 + var4, var3 + 96, -936301001);
            this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.details.no_weapon"), var2 + 10, var3 + 36, var4 - 20, -6449784, 12, 4);
        } else {
            var1.fill(var2, var3 + 21, var2 + var4, var3 + 70, -936301001);
            this.drawSlot(var1, var2 + 8, var3 + 29, var8, var5, false);
            var1.drawString(this.font, var8.getHoverName(), var2 + 34, var3 + 27, -1515055, false);
            int var9 = FoodWeaponEvolution.evolutionLevel(var8);
            int var10 = FoodWeaponEvolution.maxEvolutionLevel(var8);
            int var11 = FoodWeaponEvolution.evolutionExperience(var8);
            int var12 = FoodWeaponEvolution.nextEvolutionRequirement(var8);
            var1.drawString(
                this.font,
                Component.translatable(
                    "screen.ftboss.statue_hub.details.summary",
                    var9,
                    var10,
                    var11,
                    var9 >= var10 ? var11 : var12,
                    FoodWeaponEvolution.socketCount(var8),
                    FoodWeaponEvolution.socketCapacity(var8)
                ),
                var2 + 34,
                var3 + 41,
                -6449784,
                false
            );
            var1.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.details.grindstone_hint"), var2 + 34, var3 + 55, -5858644, false);
            List var13 = FoodWeaponEvolution.weaponDetailViews(var8);
            int var14 = Math.max(1, (var13.size() + 4 - 1) / 4);
            this.detailPage = Math.max(0, Math.min(this.detailPage, var14 - 1));
            int var15 = var2 + var4 - 96;
            int var16 = var2 + var4 - 18;
            int var17 = var3 + 77;
            var1.fill(var15, var17, var15 + 18, var17 + 13, this.detailPage > 0 ? -936301001 : -400285143);
            var1.fill(var16, var17, var16 + 18, var17 + 13, this.detailPage + 1 < var14 ? -936301001 : -400285143);
            var1.drawCenteredString(this.font, "<", var15 + 9, var17 + 2, this.detailPage > 0 ? -1515055 : -6449784);
            var1.drawCenteredString(this.font, ">", var16 + 9, var17 + 2, this.detailPage + 1 < var14 ? -1515055 : -6449784);
            MutableComponent var18 = Component.translatable("screen.ftboss.statue_hub.details.page", var13.size(), this.detailPage + 1, var14);
            var1.drawString(this.font, var18, var2 + 1, var17 + 2, -6449784, false);
            if (var13.isEmpty()) {
                this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.details.empty"), var2 + 8, var3 + 109, var4 - 16, -6449784, 12, 4);
            } else {
                int var19 = this.detailPage * 4;
                int var20 = var3 + 96;

                for (int var21 = 0; var21 < 4; var21++) {
                    int var22 = var19 + var21;
                    if (var22 >= var13.size()) {
                        break;
                    }

                    WeaponDetailView var23 = (WeaponDetailView)var13.get(var22);
                    int var24 = var20 + var21 * 38;
                    int var25 = var23.selected() ? -14249 : 0xFF000000 | var5;
                    var1.fill(var2, var24, var2 + var4, var24 + 34, var23.selected() ? -800309721 : -936301001);
                    var1.fill(var2, var24, var2 + (var23.selected() ? 5 : 3), var24 + 34, var25);
                    this.wrappedCentered(var1, var23.category(), var2 + 7, var24 + 7, 58, var25, 10, 2);
                    var1.drawString(this.font, var23.title(), var2 + 72, var24 + 5, var23.selected() ? -10374 : -1515055, false);
                    this.wrapped(
                        var1,
                        var23.description(),
                        var2 + 72,
                        var24 + 17,
                        var4 - 80,
                        var23.selectable() ? (var23.selected() ? -10374 : -3622936) : -6449784,
                        10,
                        2
                    );
                    if (var23.selectable() && inside((double)var6, (double)var7, var2, var24, var4, 34)) {
                        this.hoverTooltip = List.of(
                            var23.title(),
                            Component.translatable(
                                var23.selected()
                                    ? "screen.ftboss.statue_hub.details.gourmet_skill_current_hint"
                                    : "screen.ftboss.statue_hub.details.gourmet_skill_click_hint"
                            )
                        );
                    }
                }
            }
        }
    }

    private void drawExperienceBar(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, int var9) {
        var1.fill(var2, var3, var2 + var4, var3 + 9, -16054002);
        var1.fill(var2 + 1, var3 + 1, var2 + var4 - 1, var3 + 8, -13357253);
        double var10 = var8 ? 1.0 : (var7 <= var6 ? 0.0 : Math.max(0.0, Math.min(1.0, (double)(var5 - var6) / (double)(var7 - var6))));
        int var12 = (int)Math.round((double)(var4 - 2) * var10);
        if (var12 > 0) {
            var1.fillGradient(var2 + 1, var3 + 1, var2 + 1 + var12, var3 + 8, 0xFF000000 | var9, -1656757);
            var1.fill(var2 + 1, var3 + 1, var2 + 1 + var12, var3 + 2, -1996488705);
        }
    }

    private void drawAccessorySlot(GuiGraphics var1, int var2, int var3, SocketView var4, int var5, int var6) {
        this.drawSlot(var1, var2, var3, var4 == null ? ItemStack.EMPTY : var4.stack(), var4 == null ? var5 : var4.color(), var4 != null && var4.matching());
        if (var4 == null) {
            String var7 = Integer.toString(var6);
            var1.drawString(this.font, var7, var2 + 9 - this.font.width(var7) / 2, var3 + 5, -10659740, false);
        }
    }

    private void drawSlot(GuiGraphics var1, int var2, int var3, ItemStack var4, int var5, boolean var6) {
        int var7 = var6 ? -11173 : 0xFF000000 | var5;
        var1.fill(var2, var3, var2 + 18, var3 + 18, -16185333);
        var1.fill(var2 + 1, var3 + 1, var2 + 18 - 1, var3 + 18 - 1, var7);
        var1.fill(var2 + 2, var3 + 2, var2 + 18 - 2, var3 + 18 - 2, -13948880);
        if (!var4.isEmpty()) {
            var1.renderItem(var4, var2 + 1, var3 + 1);
            if (var4.getCount() > 1) {
                var1.drawString(
                    this.font, Integer.toString(var4.getCount()), var2 + 16 - this.font.width(Integer.toString(var4.getCount())), var3 + 10, -1, true
                );
            }
        }
    }

    private List<Component> socketTooltip(SocketView var1, int var2) {
        return var1 == null
            ? List.of(Component.translatable("screen.ftboss.statue_hub.evolution.socket_empty_slot", var2))
            : List.of(
                Component.literal("#" + var2 + " ").append(var1.stack().getHoverName()),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", var1.type(), var1.tier()),
                var1.effect(),
                Component.translatable("screen.ftboss.statue_hub.evolution.synergy", var1.synergy()),
                Component.translatable("screen.ftboss.statue_hub.evolution.remove_click")
            );
    }

    private List<Component> blessingFoodTooltip(ItemStack var1) {
        SocketView var2 = FoodWeaponEvolution.ingredientView(var1);
        return var2 == null
            ? List.of(var1.getHoverName())
            : List.of(
                var1.getHoverName(),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", var2.type(), var2.tier()),
                Component.translatable("screen.ftboss.statue_hub.enchant.offer_click")
            );
    }

    private List<Component> ingredientTooltip(ItemStack var1) {
        SocketView var2 = FoodWeaponEvolution.ingredientView(var1);
        return var2 == null
            ? List.of(var1.getHoverName())
            : List.of(
                var1.getHoverName(),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", var2.type(), var2.tier()),
                var2.effect(),
                Component.translatable("screen.ftboss.statue_hub.evolution.synergy", var2.synergy()),
                Component.translatable("screen.ftboss.statue_hub.evolution.insert_click")
            );
    }



    private List<Integer> ingredientSlots(ItemStack var1) {
        return this.candidateSlots(var1, false);
    }

    private List<Integer> candidateSlots(ItemStack var1, boolean var2) {
        ArrayList var3 = new ArrayList();
        if (this.minecraft != null && this.minecraft.player != null) {
            for (int var4 = 0; var4 < 36; var4++) {
                ItemStack var5 = this.minecraft.player.getInventory().getItem(var4);
                if (var5 != var1
                    && (var2 ? FoodWeaponEvolution.isBlessingCandidate(var1, var5, this.tint) : FoodWeaponEvolution.isSocketCandidate(var1, var5, this.tint))) {
                    var3.add(var4);
                }
            }

            ItemStack var6 = this.minecraft.player.getInventory().getItem(40);
            if (var6 != var1
                && (var2 ? FoodWeaponEvolution.isBlessingCandidate(var1, var6, this.tint) : FoodWeaponEvolution.isSocketCandidate(var1, var6, this.tint))) {
                var3.add(40);
            }

            return var3;
        } else {
            return var3;
        }
    }

    @Override
    public boolean mouseClicked(double var1, double var3, int var5) {
        if (this.section == StatueHubScreen.Section.EVOLUTION && this.repairPanel.isShowing()) return super.mouseClicked(var1, var3, var5);
        if (var5 == 0 && !this.actionPending) {
            if (this.section == StatueHubScreen.Section.CHALLENGE) {
                int var6 = this.contentX();
                int var7 = this.contentY();
                int var8 = inside(var1, var3, var6, var7 + 23, this.contentWidth(), 50)
                    ? 0
                    : (inside(var1, var3, var6, var7 + 79, this.contentWidth(), 61) ? 1 : -1);
                if (var8 >= 0) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    Action.send(this.statuePos, 8, var8);
                    return true;
                }
            }

            if (this.section == StatueHubScreen.Section.DETAILS) {
                List var14 = FoodWeaponEvolution.weaponDetailViews(this.evolutionWeapon());
                int var18 = Math.max(1, (var14.size() + 4 - 1) / 4);
                int var22 = this.contentX();
                int var9 = this.contentY() + 77;
                if (inside(var1, var3, var22 + this.contentWidth() - 96, var9, 18, 13) && this.detailPage > 0) {
                    this.detailPage--;
                    return true;
                }

                if (inside(var1, var3, var22 + this.contentWidth() - 18, var9, 18, 13) && this.detailPage + 1 < var18) {
                    this.detailPage++;
                    return true;
                }

                int var10 = this.contentY() + 96;
                if (inside(var1, var3, var22, var10, this.contentWidth(), 152)) {
                    int var11 = ((int)var3 - var10) / 38;
                    int var12 = this.detailPage * 4 + var11;
                    if (var12 >= 0 && var12 < var14.size()) {
                        WeaponDetailView var13 = (WeaponDetailView)var14.get(var12);
                        if (var13.selectable()) {
                            if (!var13.selected()) {
                                this.actionPending = true;
                                this.updateActionButtons();
                                Action.send(this.statuePos, 9, var13.gourmetSkillIndex());
                            }

                            return true;
                        }
                    }
                }
            }

            if (this.section == StatueHubScreen.Section.DAILY) {
                int var15 = this.contentX();
                int var19 = this.contentY();
                if (inside(var1, var3, var15 + this.contentWidth() - 96, var19 - 2, 18, 13) && this.dailyTaskPage > 0) {
                    this.dailyTaskPage--;
                    return true;
                }

                if (inside(var1, var3, var15 + this.contentWidth() - 18, var19 - 2, 18, 13) && this.dailyTaskPage + 1 < this.dailyTaskPages()) {
                    this.dailyTaskPage++;
                    return true;
                }

                List var23 = this.dailyFoodSlots();
                int var26 = gridIndex(var1, var3, this.contentX(), this.contentY() + 170, 9, 2);
                int var29 = this.dailyPage * 9 * 2 + var26;
                if (var26 >= 0 && var29 >= 0 && var29 < var23.size()) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    Action.send(this.statuePos, 4, (Integer)var23.get(var29));
                    return true;
                }
            }

            

            if (this.section == StatueHubScreen.Section.EVOLUTION) {
                ItemStack var17 = this.evolutionWeapon();
                if (!var17.isEmpty()) {
                    int var21 = this.contentX();
                    int var25 = this.contentY();
                    int var28 = gridIndex(var1, var3, var21, var25 + 101, 10, 3);
                    int var30 = FoodWeaponEvolution.socketCount(var17);
                    if (var28 >= 0 && var28 < var30) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        Action.send(this.statuePos, 3, var28);
                        return true;
                    }

                    int var31 = gridIndex(var1, var3, var21, var25 + 176, 9, 2);
                    List var32 = this.ingredientSlots(var17);
                    int var33 = this.ingredientPage * 9 * 2 + var31;
                    if (var31 >= 0 && var33 >= 0 && var33 < var32.size()) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        Action.send(this.statuePos, 2, (Integer)var32.get(var33));
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(var1, var3, var5);
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        if (this.section == StatueHubScreen.Section.DAILY) {
            int var11 = this.contentX();
            int var12 = this.contentY();
            int var13 = this.dailyTaskPages();
            if (var13 > 1 && inside(var1, var3, var11, var12 + 15, this.contentWidth(), 125)) {
                this.dailyTaskPage = Math.max(0, Math.min(var13 - 1, this.dailyTaskPage + (var7 < 0.0 ? 1 : -1)));
                return true;
            }

            int var14 = Math.max(1, (this.dailyFoodSlots().size() + 18 - 1) / 18);
            if (var14 > 1) {
                this.dailyPage = Math.max(0, Math.min(var14 - 1, this.dailyPage + (var7 < 0.0 ? 1 : -1)));
                return true;
            }
        }

        

        if (this.section == StatueHubScreen.Section.EVOLUTION) {
            List var16 = this.ingredientSlots(this.evolutionWeapon());
            int var19 = Math.max(1, (var16.size() + 18 - 1) / 18);
            if (var19 > 1) {
                this.ingredientPage = Math.max(0, Math.min(var19 - 1, this.ingredientPage + (var7 < 0.0 ? 1 : -1)));
                return true;
            }
        }

        if (this.section == StatueHubScreen.Section.DETAILS) {
            List var17 = FoodWeaponEvolution.weaponDetailViews(this.evolutionWeapon());
            int var20 = Math.max(1, (var17.size() + 4 - 1) / 4);
            if (var20 > 1) {
                this.detailPage = Math.max(0, Math.min(var20 - 1, this.detailPage + (var7 < 0.0 ? 1 : -1)));
                return true;
            }
        }

        return super.mouseScrolled(var1, var3, var5, var7);
    }

    private static int gridIndex(double var0, double var2, int var4, int var5, int var6, int var7) {
        return !inside(var0, var2, var4, var5, var6 * 18, var7 * 18) ? -1 : ((int)var2 - var5) / 18 * var6 + ((int)var0 - var4) / 18;
    }

    private static boolean inside(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= (double)var4 && var0 < (double)(var4 + var6) && var2 >= (double)var5 && var2 < (double)(var5 + var7);
    }

    private ItemStack evolutionWeapon() {
        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack var1 = this.minecraft.player.getMainHandItem();
            if (FourWeaponCavityPresentation.selectableWeapon(var1)) {
                return var1;
            } else {
                ItemStack var2 = this.minecraft.player.getOffhandItem();
                return FourWeaponCavityPresentation.selectableWeapon(var2) ? var2 : ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void heading(GuiGraphics var1, int var2, int var3, String var4, int var5) {
        var1.drawString(this.font, Component.translatable(var4), var2 + 1, var3 + 2, 0xFF000000 | var5, false);
        var1.fill(var2, var3 + 15, var2 + 46, var3 + 17, 0xFF000000 | var5);
    }

    private int wrapped(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        List var9 = this.font.split(var2, var5);
        int var10 = Math.min(var8, var9.size());

        for (int var11 = 0; var11 < var10; var11++) {
            var1.drawString(this.font, (FormattedCharSequence)var9.get(var11), var3, var4 + var11 * var7, var6, false);
        }

        return var10;
    }

    private void wrappedCentered(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        List var9 = this.font.split(var2, var5);
        int var10 = Math.min(var8, var9.size());

        for (int var11 = 0; var11 < var10; var11++) {
            FormattedCharSequence var12 = (FormattedCharSequence)var9.get(var11);
            var1.drawString(this.font, var12, var3 + (var5 - this.font.width(var12)) / 2, var4 + var11 * var7, var6, false);
        }
    }

    private static int accent(int var0) {
        return switch (var0) {
            case 1 -> 15509720;
            case 2 -> 13851736;
            case 3 -> 6801802;
            case 4 -> 11041236;
            case 5 -> 14202215;
            case 6 -> 6671066;
            case 7 -> 9285983;
            case 8 -> 14052209;
            case 9 -> 6270680;
            case 10 -> 7046871;
            default -> 12958110;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static enum Section {
        DAILY("screen.ftboss.statue_hub.tab.daily"),
        CHALLENGE("screen.ftboss.statue_hub.tab.challenge"),

        EVOLUTION("screen.ftboss.statue_hub.tab.evolution"),
        DETAILS("screen.ftboss.statue_hub.tab.details");

        final String key;

        private Section(String nullxx) {
            this.key = nullxx;
        }
    }

    private void sendRepair(int var1, int var2) {
        ItemStack var3 = this.evolutionWeapon();
        if (!this.actionPending && FoodWeaponLifecycle.weaponId(var3) != null) {
            this.actionPending = true;
            this.updateActionButtons();
            net.farmingtales.ftboss.network.StatueMenuNetwork.RepairAction.send(this.statuePos, var3, var1, var2);
        }
    }

    void openWeaponRepair() {
        this.section = StatueHubScreen.Section.EVOLUTION;
        this.repairPanel.open();
        this.updateActionButtons();
    }

    private void openSwordCavity() {
        if (!this.actionPending) {
            ItemStack stack = this.evolutionWeapon();
            if (FourWeaponCavityPresentation.usesCavity(stack)) {
                if (!FourWeaponCavityPresentation.openContainer(this.statuePos, stack)) return;
                this.section = StatueHubScreen.Section.DAILY;
                this.updateActionButtons();
            }
        }
    }

    private void addOrdinaryHubEntry() {
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.ftboss.statue_hub.tab.evolution").append(" / ").append(Component.translatable("screen.ftboss.statue_hub.tab.material")),
            this::openOrdinaryHub
        ).bounds(this.left + 11, this.top + 249, 94, 18).build());
    }

    private void openOrdinaryHub(Button button) {
        if (!this.actionPending) {
            this.actionPending = true;
            this.updateActionButtons();
            net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryAction.send(this.statuePos, 1);
        }
    }

}
