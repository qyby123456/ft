package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.StatueWeaponDetailsPanel.Result;
import net.farmingtales.ftboss.item.FoodWeaponBossPassive;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodBlessingView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.SocketView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.StatueDailyView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.WeaponMaterialBuild.CombatStats;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryAction;
import net.farmingtales.ftboss.network.NativeStatueCavityNetwork.CavityOpenAction;
import net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryOpen;
import net.farmingtales.ftboss.network.StatueMenuNetwork.RepairAction;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules.Group;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class OrdinaryStatueHubScreen extends Screen {
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
    private OrdinaryStatueHubScreen.Section section = OrdinaryStatueHubScreen.Section.DAILY;
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
    private List<Component> hoverTooltip;
    private Button replaceButton;
    private Button bindButton;
    private static final int EVOLUTION_FOOD_Y = 164;
    private Part materialPart;
    private static final int MATERIAL_PART_HEIGHT = 24;
    private static final int MATERIAL_INVENTORY_Y = 145;
    private static final int MATERIAL_WARNING_Y = 195;
    private int materialPage;
    private static final int MATERIAL_PART_Y = 101;
    private static final int MATERIAL_FILTER_Y = 127;
    private static final int MAX_BLESSINGS = 6;
    private static final int EVOLUTION_SOCKET_Y = 90;
    private final StatueWeaponRepairPanel repairPanel;
    private static final int MATERIAL_FILTER_HEIGHT = 15;
    private ItemStack selectionWeapon;
    private OrdinaryStatueHubScreen.MaterialFilter materialFilter;
    private static final int DETAILS_PER_PAGE = 3;
    private int offeringSlot = -1;
    private ItemStack selectionFood;
    private static final int TAB_SPACING = 30;
    private int materialSlot = -1;
    private static final int MATERIAL_FOOD_Y = 158;
    private final StatueSwordCavityPanel swordCavityPanel = null;

    public static void updateAppearance(BlockPos pos, boolean white, boolean gold) {
        if (Minecraft.getInstance().screen instanceof OrdinaryStatueHubScreen hub && hub.statuePos.equals(pos)) {
            hub.white = white;
            hub.gold = gold;
            hub.updateColorButton();
        }
    }

    private Component colorLabel() {
        if (this.gold) {
            return Component.translatable("screen.ftboss.statue_hub.color.restore");
        } else {
            return this.white && this.finaleUnlocked
                ? Component.translatable("screen.ftboss.statue_hub.color.gold")
                : Component.translatable(this.white ? "screen.ftboss.statue_hub.color.restore" : "screen.ftboss.statue_hub.color.white");
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

    private void renderDaily(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        this.heading(graphics, x, y, "screen.ftboss.statue_hub.daily.title", accent);
        int taskPages = this.dailyTaskPages();
        this.dailyTaskPage = Math.min(this.dailyTaskPage, taskPages - 1);
        int leftArrowX = x + width - 96;
        int rightArrowX = x + width - 18;
        graphics.fill(leftArrowX, y - 2, leftArrowX + 18, y + 11, this.dailyTaskPage > 0 ? -936301001 : -400285143);
        graphics.fill(rightArrowX, y - 2, rightArrowX + 18, y + 11, this.dailyTaskPage + 1 < taskPages ? -936301001 : -400285143);
        graphics.drawCenteredString(this.font, "<", leftArrowX + 9, y, this.dailyTaskPage > 0 ? -1515055 : -6449784);
        graphics.drawCenteredString(this.font, ">", rightArrowX + 9, y, this.dailyTaskPage + 1 < taskPages ? -1515055 : -6449784);
        Component page = Component.translatable("screen.ftboss.statue_hub.daily.task_page", this.dailyTaskPage + 1, taskPages);
        graphics.drawCenteredString(this.font, page, x + width - 48, y, -6449784);
        int cardY = y + 20;
        if (this.dailyTasks.isEmpty()) {
            this.wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.daily.none"), x + 4, cardY + 5, width - 8, -6449784, 11, 4);
        }

        int taskStart = this.dailyTaskPage * 4;

        for (int visible = 0; visible < 4; visible++) {
            int i = taskStart + visible;
            if (i >= this.dailyTasks.size()) {
                break;
            }

            StatueDailyView task = this.dailyTasks.get(i);
            int stateColor = task.state() == 2 ? -8947849 : (task.state() == 1 ? -10103922 : -1917592);
            graphics.fill(x, cardY, x + width, cardY + 26, -936301001);
            graphics.fill(x, cardY, x + 3, cardY + 26, stateColor);
            Component taskTitle = Component.translatable("screen.ftboss.statue_hub.daily.task", i + 1, task.request());
            List<FormattedCharSequence> titleLine = this.font.split(taskTitle, width - 16);
            if (!titleLine.isEmpty()) {
                graphics.drawString(this.font, titleLine.get(0), x + 8, cardY + 3, -1515055, false);
            }

            Component status = task.state() == 2
                ? Component.translatable("screen.ftboss.statue_hub.daily.claimed")
                : (
                    task.state() == 1
                        ? Component.translatable("screen.ftboss.statue_hub.daily.ready")
                        : Component.translatable("screen.ftboss.statue_hub.daily.progress", task.progress(), task.required())
                );
            Component details = status.copy()
                .append(Component.literal(" · "))
                .append(Component.translatable("screen.ftboss.statue_hub.daily.reward", task.reward()));
            List<FormattedCharSequence> clipped = this.font.split(details, width - 16);
            if (!clipped.isEmpty()) {
                graphics.drawString(this.font, clipped.get(0), x + 8, cardY + 14, stateColor, false);
            }

            cardY += 29;
        }

        List<Integer> foods = this.dailyFoodSlots();
        int pageSize = 18;
        int pages = Math.max(1, (foods.size() + pageSize - 1) / pageSize);
        this.dailyPage = Math.min(this.dailyPage, pages - 1);
        graphics.drawString(
            this.font,
            Component.translatable("screen.ftboss.statue_hub.daily.available", foods.size(), this.dailyPage + 1, pages),
            x + 1,
            y + 154,
            -1917592,
            false
        );
        int start = this.dailyPage * pageSize;
        int foodY = y + 170;

        for (int cell = 0; cell < pageSize; cell++) {
            int sx = x + cell % 9 * 18;
            int sy = foodY + cell / 9 * 18;
            int listIndex = start + cell;
            ItemStack stack = listIndex < foods.size() && this.minecraft != null && this.minecraft.player != null
                ? this.minecraft.player.getInventory().getItem(foods.get(listIndex))
                : ItemStack.EMPTY;
            this.drawSlot(graphics, sx, sy, stack, accent, false);
            if (inside((double)mouseX, (double)mouseY, sx, sy, 18, 18) && !stack.isEmpty()) {
                this.hoverTooltip = this.dailyFoodTooltip(stack);
            }
        }

        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.daily.hint"), x + 172, y + 183, -6449784, false);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.daily.page_hint"), x + 172, y + 198, -6449784, false);
    }

    private int dailyTaskPages() {
        return Math.max(1, (this.dailyTasks.size() + 4 - 1) / 4);
    }

    private List<Integer> dailyFoodSlots() {
        List<Integer> result = new ArrayList<>();
        if (this.minecraft != null && this.minecraft.player != null) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = this.minecraft.player.getInventory().getItem(slot);
                if (FoodWeaponEvolution.ftboss$legacyOrdinary$isDailySubmissionCandidate(this.dailyTasks, stack, this.tint)) {
                    result.add(slot);
                }
            }

            ItemStack offhand = this.minecraft.player.getInventory().getItem(40);
            if (FoodWeaponEvolution.ftboss$legacyOrdinary$isDailySubmissionCandidate(this.dailyTasks, offhand, this.tint)) {
                result.add(40);
            }

            return result;
        } else {
            return result;
        }
    }

    private List<Component> dailyFoodTooltip(ItemStack stack) {
        return List.of(stack.getHoverName(), Component.translatable("screen.ftboss.statue_hub.daily.submit_click"));
    }

    private void renderChallenge(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        this.heading(graphics, x, y, "screen.ftboss.statue_hub.challenge.title", accent);
        boolean normalHover = inside((double)mouseX, (double)mouseY, x, y + 23, width, 50);
        boolean finaleHover = inside((double)mouseX, (double)mouseY, x, y + 79, width, 61);
        int normalBorder = !this.finaleSelected ? -9381480 : (normalHover ? 0xFF000000 | accent : -11909552);
        graphics.fill(x, y + 23, x + width, y + 73, normalBorder);
        graphics.fill(x + 2, y + 25, x + width - 2, y + 71, -936301001);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.challenge.normal"), x + 10, y + 31, -9381480, false);
        Component normalState = Component.translatable(
            this.finaleSelected ? "screen.ftboss.statue_hub.challenge.click_select" : "screen.ftboss.statue_hub.challenge.selected"
        );
        graphics.drawString(
            this.font, normalState, x + width - 10 - this.font.width(normalState), y + 31, !this.finaleSelected && !normalHover ? -9381480 : -1515055, false
        );
        this.wrapped(
            graphics, Component.translatable("screen.ftboss.statue_hub.challenge.normal_desc", this.statueName), x + 10, y + 46, width - 20, -6449784, 11, 2
        );
        int finaleColor = this.finaleUnlocked ? -38031 : -8949123;
        int finaleBorder = this.finaleSelected ? -38031 : (finaleHover && this.finaleUnlocked ? 0xFF000000 | accent : -11909552);
        graphics.fill(x, y + 79, x + width, y + 140, finaleBorder);
        graphics.fill(x + 2, y + 81, x + width - 2, y + 138, -936301001);
        graphics.drawString(
            this.font,
            Component.translatable(
                this.finaleUnlocked ? "screen.ftboss.statue_hub.challenge.finale_unlocked" : "screen.ftboss.statue_hub.challenge.finale_locked"
            ),
            x + 10,
            y + 87,
            finaleColor,
            false
        );
        Component finaleState = Component.translatable(
            !this.finaleUnlocked
                ? "screen.ftboss.statue_hub.challenge.locked"
                : (this.finaleSelected ? "screen.ftboss.statue_hub.challenge.selected" : "screen.ftboss.statue_hub.challenge.click_select")
        );
        graphics.drawString(this.font, finaleState, x + width - 10 - this.font.width(finaleState), y + 87, this.finaleSelected ? -38031 : -6449784, false);
        this.wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.challenge.finale_desc"), x + 10, y + 102, width - 20, -6449784, 11, 3);
        this.wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.challenge.hint"), x + 2, y + 149, width - 4, -6449784, 11, 4);
    }

    private void drawExperienceBar(GuiGraphics graphics, int x, int y, int width, int experience, int floor, int required, boolean maxed, int accent) {
        graphics.fill(x, y, x + width, y + 9, -16054002);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 8, -13357253);
        double ratio = maxed ? 1.0 : (required <= floor ? 0.0 : Math.max(0.0, Math.min(1.0, (double)(experience - floor) / (double)(required - floor))));
        int filled = (int)Math.round((double)(width - 2) * ratio);
        if (filled > 0) {
            graphics.fillGradient(x + 1, y + 1, x + 1 + filled, y + 8, 0xFF000000 | accent, -1656757);
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + 2, -1996488705);
        }
    }

    private void drawAccessorySlot(GuiGraphics graphics, int x, int y, SocketView view, int accent, int number) {
        this.drawSlot(graphics, x, y, view == null ? ItemStack.EMPTY : view.stack(), view == null ? accent : view.color(), view != null && view.matching());
        if (view == null) {
            String label = Integer.toString(number);
            graphics.drawString(this.font, label, x + 9 - this.font.width(label) / 2, y + 5, -10659740, false);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, ItemStack stack, int color, boolean matching) {
        int border = matching ? -11173 : 0xFF000000 | color;
        graphics.fill(x, y, x + 18, y + 18, -16185333);
        graphics.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, border);
        graphics.fill(x + 2, y + 2, x + 18 - 2, y + 18 - 2, -13948880);
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
            if (stack.getCount() > 1) {
                graphics.drawString(
                    this.font, Integer.toString(stack.getCount()), x + 16 - this.font.width(Integer.toString(stack.getCount())), y + 10, -1, true
                );
            }
        }
    }

    private List<Component> socketTooltip(SocketView view, int slot) {
        return view == null
            ? List.of(Component.translatable("screen.ftboss.statue_hub.evolution.socket_empty_slot", slot))
            : List.of(
                Component.literal("#" + slot + " ").append(view.stack().getHoverName()),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", view.type(), view.tier()),
                view.effect(),
                Component.translatable("screen.ftboss.statue_hub.evolution.synergy", view.synergy()),
                Component.translatable("screen.ftboss.statue_hub.evolution.remove_click")
            );
    }

    private List<Component> blessingFoodTooltip(ItemStack stack) {
        SocketView view = FoodWeaponEvolution.ftboss$legacyOrdinary$ingredientView(stack);
        return view == null
            ? List.of(stack.getHoverName())
            : List.of(
                stack.getHoverName(),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", view.type(), view.tier()),
                Component.translatable("screen.ftboss.statue_hub.enchant.offer_click")
            );
    }



    private List<Integer> ingredientSlots(ItemStack weapon) {
        return this.candidateSlots(weapon, false);
    }

    private static int gridIndex(double mouseX, double mouseY, int x, int y, int columns, int rows) {
        return !inside(mouseX, mouseY, x, y, columns * 18, rows * 18) ? -1 : ((int)mouseY - y) / 18 * columns + ((int)mouseX - x) / 18;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= (double)x && mouseX < (double)(x + width) && mouseY >= (double)y && mouseY < (double)(y + height);
    }

    private ItemStack evolutionWeapon() {
        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack main = this.minecraft.player.getMainHandItem();
            if (FourWeaponCavityPresentation.selectableWeapon(main)) {
                return main;
            } else {
                ItemStack off = this.minecraft.player.getOffhandItem();
                return FourWeaponCavityPresentation.selectableWeapon(off) ? off : ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static int accent(int tint) {
        return switch (tint) {
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

    private void sendRepair(int var1, int var2) {
        ItemStack var3 = this.evolutionWeapon();
        if (!this.actionPending && FoodWeaponLifecycle.weaponId(var3) != null) {
            this.actionPending = true;
            this.updateActionButtons();
            net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryRepairAction.send(this.statuePos, var3, var1, var2);
        }
    }

    @Override
    protected void init() {
        this.left = (this.width - 490) / 2;
        this.top = (this.height - 300) / 2;
        int var1 = this.left + 11;
        int var2 = this.top + 42 + 8;
        int var3 = 0;

        for (OrdinaryStatueHubScreen.Section var7 : OrdinaryStatueHubScreen.Section.values()) {
            this.addRenderableWidget(Button.builder(Component.translatable(var7.key), var2x -> {
                if (var7 == OrdinaryStatueHubScreen.Section.MATERIAL && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
                    if (!this.actionPending) {
                        this.openSwordCavity();
                    }
                } else {
                    this.section = var7;
                    this.updateActionButtons();
                }
            }).bounds(var1, var2 + var3 * 30, 94, 25).build());
            var3++;
        }

        this.colorButton = this.addRenderableWidget(Button.builder(this.colorLabel(), var1x -> {
            OrdinaryAction.send(this.statuePos, 0);
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.left + 490 - 104, this.top + 300 - 29, 88, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, var1x -> this.onClose()).bounds(this.left + 12, this.top + 300 - 29, 72, 20).build());
        this.claimButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.statue_hub.evolution.claim"), var1x -> {
            OrdinaryAction.send(this.statuePos, 6);
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.left + 145, this.top + 300 - 29, 96, 20).build());
        this.evolveButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.statue_hub.evolution.evolve"), var1x -> {
            OrdinaryAction.send(this.statuePos, 5, this.offeringSlot);
            this.offeringSlot = -1;
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.left + 250, this.top + 300 - 29, 120, 20).build());
        this.evolveButton.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.level_warning")));
        this.replaceButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.material.confirm"), var1x -> {
            if (!FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
                OrdinaryAction.send(this.statuePos, 10, this.materialPart.ordinal() * 36 + this.materialSlot);
                this.materialSlot = -1;
                this.actionPending = true;
                this.updateActionButtons();
            }
        }).bounds(this.left + 145, this.top + 300 - 29, 225, 20).build());
        this.bindButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.evolution.bind"), var1x -> {
            OrdinaryAction.send(this.statuePos, 11);
            this.actionPending = true;
            this.updateActionButtons();
        }).bounds(this.contentX() + this.contentWidth() - 88, this.contentY() + 24, 82, 20).build());
        this.bindButton.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.evolution.bind_hint")));
        this.repairPanel.init(var1x -> this.addRenderableWidget(var1x), this.contentX(), this.contentY(), this.contentWidth(), this.top + 300 - 29);
        this.updateActionButtons();
        this.addBuildInspectorEntry();
        this.addWeaponRepairEntry();
    }

    private void updateColorButton() {
        if (this.colorButton != null) {
            this.colorButton.setMessage(this.colorLabel());
            this.colorButton.active = !this.actionPending;
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
        int var8 = this.top + 42 + 8 + this.section.ordinal() * 30;
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
            case MATERIAL:
                this.renderMaterials(var1, var9, var10, var11, var5, var2, var3);
                break;
            case DETAILS:
                this.renderWeaponDetails(var1, var9, var10, var11, var5, var2, var3);
        }

        int var12 = this.left + 490 - 114;
        int var13 = this.top + 300 - 25;
        var1.fill(var12, var13, var12 + 8, var13 + 8, this.gold ? -12470 : (this.white ? -855832 : 0xFF000000 | var5));
        super.render(var1, var2, var3, var4);
        if (!this.hoverTooltip.isEmpty()) {
            var1.renderTooltip(this.font, this.hoverTooltip, Optional.empty(), var2, var3);
        }
    }



    private void renderEvolution(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        if (this.repairPanel.isShowing()) {
            this.hoverTooltip = this.repairPanel.render(var1, this.font, var2, var3, var4, var5, var6, var7, this.actionPending);
        } else {
            this.heading(var1, var2, var3, "screen.ftboss.statue_hub.evolution.title", var5);
            ItemStack var8 = this.evolutionWeapon();
            if (var8.isEmpty()) {
                var1.fill(var2, var3 + 24, var2 + var4, var3 + 96, -936301001);
                this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"), var2 + 10, var3 + 36, var4 - 20, -6449784, 12, 4);
            } else {
                var1.fill(var2, var3 + 21, var2 + var4, var3 + 74, -936301001);
                this.drawSlot(var1, var2 + 8, var3 + 30, var8, var5, false);
                this.wrapped(var1, var8.getHoverName(), var2 + 34, var3 + 27, var4 - 130, -1515055, 11, 1);
                int var9 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var8);
                int var10 = FoodWeaponEvolution.ftboss$legacyOrdinary$maxEvolutionLevel(var8);
                int var11 = FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionExperience(var8);
                int var12 = FoodWeaponEvolution.ftboss$legacyOrdinary$nextEvolutionRequirement(var8);
                int var13 = FoodWeaponEvolution.currentEvolutionFloor(var8);
                var1.drawString(
                    this.font, Component.translatable("screen.ftboss.statue_hub.evolution.level", var9, var10), var2 + 34, var3 + 39, -1917592, false
                );
                this.drawExperienceBar(var1, var2 + 34, var3 + 53, var4 - 44, var11, var13, var12, var9 >= var10, var5);
                MutableComponent var14 = var9 >= var10
                    ? Component.translatable("screen.ftboss.statue_hub.evolution.maxed")
                    : Component.translatable("screen.ftboss.statue_hub.evolution.xp", var11, var12);
                var1.drawCenteredString(this.font, var14, var2 + 34 + (var4 - 44) / 2, var3 + 65, -1515055);
                List var15 = FoodWeaponEvolution.ftboss$legacyOrdinary$socketViews(var8);
                int var16 = FoodWeaponEvolution.socketCapacity(var8);
                var1.drawString(
                    this.font,
                    Component.translatable("screen.ftboss.statue_hub.evolution.sockets_full", var15.size(), var16),
                    var2 + 1,
                    var3 + 78,
                    -9250406,
                    false
                );
                int var17 = var3 + 90;

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
                var1.fill(var29, var3 + 90, var2 + var4, var3 + 144, -1725948635);
                this.wrapped(
                    var1,
                    Component.translatable("screen.ftboss.statue_hub.evolution.socket_help"),
                    var29 + 7,
                    var3 + 96,
                    var4 - (var29 - var2) - 14,
                    -6449784,
                    11,
                    4
                );
                if (inside((double)var6, (double)var7, var29, var3 + 90, var4 - (var29 - var2), 54)) {
                    this.hoverTooltip = List.of(Component.translatable("screen.ftboss.statue_hub.evolution.socket_help"));
                }

                List var30 = this.ingredientSlots(var8);
                byte var31 = 18;
                int var32 = Math.max(1, (var30.size() + var31 - 1) / var31);
                this.ingredientPage = Math.min(this.ingredientPage, var32 - 1);
                var1.drawString(
                    this.font,
                    Component.translatable("screen.ftboss.statue_hub.evolution.ingredients", var30.size(), this.ingredientPage + 1, var32),
                    var2 + 1,
                    var3 + 151,
                    -1917592,
                    false
                );
                int var22 = var3 + 164;
                int var23 = this.ingredientPage * var31;

                for (int var24 = 0; var24 < var31; var24++) {
                    int var25 = var2 + var24 % 9 * 18;
                    int var26 = var22 + var24 / 9 * 18;
                    int var27 = var23 + var24;
                    ItemStack var28 = var27 < var30.size() && this.minecraft != null && this.minecraft.player != null
                        ? this.minecraft.player.getInventory().getItem((Integer)var30.get(var27))
                        : ItemStack.EMPTY;
                    this.drawSlot(var1, var25, var26, var28, var5, var27 < var30.size() && (Integer)var30.get(var27) == this.offeringSlot);
                    if (inside((double)var6, (double)var7, var25, var26, 18, 18) && !var28.isEmpty()) {
                        this.hoverTooltip = this.ingredientTooltip(var28);
                    }
                }

                ItemStack var33 = this.inventoryItem(this.offeringSlot);
                Object var34 = this.offeringSlot < 0 ? Component.translatable("screen.ftboss.evolution.select_offering") : var33.getHoverName();
                this.wrapped(var1, (Component)var34, var2 + 172, var3 + 158, var4 - 172, -1515055, 11, 1);
                if (this.offeringSlot >= 0) {
                    this.wrapped(
                        var1,
                        Component.translatable(
                            "screen.ftboss.evolution.offering_chance", ordinaryQualityName(var33), FoodWeaponProgression.ftboss$legacyOrdinary$chance(var8, var33)
                        ),
                        var2 + 172,
                        var3 + 170,
                        var4 - 172,
                        -1917592,
                        11,
                        1
                    );
                    this.wrapped(var1, (Component)StatueWeaponDetailsPanel.offeringTooltip(var33).get(0), var2 + 172, var3 + 181, var4 - 172, -3622936, 10, 1);
                    if (inside((double)var6, (double)var7, var2 + 172, var3 + 158, var4 - 172, 34)) {
                        this.hoverTooltip = this.ingredientTooltip(var33);
                    }
                }

                this.wrapped(var1, Component.translatable("screen.ftboss.evolution.failure_warning"), var2 + 172, var3 + 193, var4 - 172, -26000, 10, 2);
            }
        }
    }

    private void renderMaterialFilters(GuiGraphics var1, int var2, int var3, int var4) {
        int var5 = var4 / OrdinaryStatueHubScreen.MaterialFilter.values().length;

        for (OrdinaryStatueHubScreen.MaterialFilter var9 : OrdinaryStatueHubScreen.MaterialFilter.values()) {
            int var10 = var2 + var9.ordinal() * var5;
            var1.fill(var10, var3 + 127, var10 + var5 - 3, var3 + 127 + 15, this.materialFilter == var9 ? -800309721 : -936301001);
            var1.drawCenteredString(this.font, var9.label(), var10 + (var5 - 3) / 2, var3 + 127 + 3, this.materialFilter == var9 ? -10374 : -1515055);
        }
    }

    private List<Component> materialTooltip(Component var1, Material var2) {
        List var3 = new ArrayList<>(
            List.of(
                var1,
                WeaponMaterialBuild.materialName(var2),
                WeaponMaterialCatalog.source(var2),
                WeaponMaterialBuild.passive(var2),
                Component.translatable("tooltip.ftboss.material.unique"),
                Component.translatable("tooltip.ftboss.material.trigger_scope"),
                Component.translatable("screen.ftboss.material.select_hint"),
                Component.translatable(WeaponMaterialCatalog.boss(var2) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning")
            )
        );
        if (WeaponMaterialCatalog.group(var2) == Group.INGOT) {
            var3.add(Component.translatable("screen.ftboss.material.ingot_rules"));
        }

        return List.copyOf(var3);
    }

    private boolean canReplaceMaterial(ItemStack var1, Build var2) {
        if (WeaponAttachmentPolicy.excluded(var1)) {
            return false;
        } else {
            int var3 = var1.getMaxDamage();
            int var4 = WeaponMaterialBuild.previewDurability(var1, var2);
            return var3 <= 0 || FoodWeaponBuildRules.preserveDamage(var1.getDamageValue(), var3, var4) < var4;
        }
    }

    private void renderBuildStats(GuiGraphics var1, int var2, int var3, int var4, CombatStats var5, String var6) {
        var1.fill(var2, var3, var2 + var4, var3 + 60, -936301001);
        var1.drawString(this.font, Component.translatable(var6), var2 + 6, var3 + 4, -1917592, false);
        List var7 = this.combatLines(var5);

        for (int var8 = 0; var8 < var7.size(); var8++) {
            this.wrapped(var1, (Component)var7.get(var8), var2 + 6, var3 + 16 + var8 * 11, var4 - 12, var8 == var7.size() - 1 ? -6449784 : -1515055, 10, 1);
        }
    }

    private List<Component> combatLines(CombatStats var1) {
        return List.of(
            Component.translatable(
                "screen.ftboss.material.combat", String.format(Locale.ROOT, "%.1f", var1.damage()), String.format(Locale.ROOT, "%.2f", var1.speed())
            ),
            Component.translatable("screen.ftboss.material.durability_wear", var1.durability(), Math.round(var1.wear() * 100.0)),
            Component.translatable("screen.ftboss.material.reach", String.format(Locale.ROOT, "%.2f", var1.reach())),
            Component.translatable("screen.ftboss.material.weapon_only")
        );
    }

    private ItemStack inventoryItem(int var1) {
        return this.minecraft != null && this.minecraft.player != null && var1 >= 0 && var1 < this.minecraft.player.getInventory().getContainerSize()
            ? this.minecraft.player.getInventory().getItem(var1)
            : ItemStack.EMPTY;
    }

    private void selectInventorySlot(ItemStack var1, int var2, boolean var3) {
        this.selectionWeapon = var1.copy();
        this.selectionFood = this.inventoryItem(var2).copy();
        this.offeringSlot = var3 ? -1 : var2;
        this.materialSlot = var3 ? var2 : -1;
        this.updateActionButtons();
    }

    private List<Integer> materialSlots(ItemStack var1) {
        List var2 = new ArrayList();

        for (int var3 = 0; var3 < 36; var3++) {
            ItemStack var4 = this.inventoryItem(var3);
            Material var5 = WeaponMaterialBuild.materialFor(var4);
            if (var4 != var1 && var5 != Material.ORIGINAL && this.materialFilter.accepts(var5)) {
                var2.add(var3);
            }
        }

        byte var6 = 18;
        this.materialPage = Math.max(0, Math.min(this.materialPage, Math.max(0, (var2.size() - 1) / var6)));
        return var2;
    }

    private List<WeaponDetailView> weaponDetails(ItemStack var1) {
        List var2 = new ArrayList<>(FoodWeaponEvolution.ftboss$legacyOrdinary$weaponDetailViews(var1));
        WeaponDetailView var3 = FoodWeaponBossPassive.detail(var1);
        if (var3 != null) {
            var2.add(var3);
        }

        return List.copyOf(var2);
    }

    private WeaponDetailView skillAtMouse(double var1, double var3) {
        List var5 = this.weaponDetails(this.evolutionWeapon());

        for (int var6 = 0; var6 < 3; var6++) {
            int var7 = this.detailPage * 3 + var6;
            if (var7 < var5.size() && inside(var1, var3, this.contentX(), this.contentY() + 96 + var6 * 38, this.contentWidth(), 34)) {
                return (WeaponDetailView)var5.get(var7);
            }
        }

        return null;
    }

    private void renderWeaponDetails(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        ItemStack var8 = this.evolutionWeapon();
        Result var9 = StatueWeaponDetailsPanel.render(var1, this.font, var2, var3, var4, var5, var6, var7, var8, this.weaponDetails(var8), this.detailPage);
        this.detailPage = var9.page();
        this.hoverTooltip = var9.tooltip();
    }

    private List<Component> ingredientTooltip(ItemStack var1) {
        SocketView var2 = FoodWeaponEvolution.ftboss$legacyOrdinary$ingredientView(var1);
        List var3 = new ArrayList();
        var3.add(var1.getHoverName());
        if (var1 != this.inventoryItem(40) && FoodWeaponProgression.ftboss$legacyOrdinary$isOffering(this.evolutionWeapon(), var1, this.tint)) {
            var3.add(
                Component.translatable(
                    "screen.ftboss.evolution.offering_chance",
                    ordinaryQualityName(var1),
                    FoodWeaponProgression.ftboss$legacyOrdinary$chance(this.evolutionWeapon(), var1)
                )
            );
            var3.add(Component.translatable("screen.ftboss.evolution.select_offering"));
            var3.addAll(StatueWeaponDetailsPanel.offeringTooltip(var1));
            var3.add(Component.translatable("screen.ftboss.evolution.failure_warning"));
        }

        if (var2 != null) {
            var3.add(Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", var2.type(), var2.tier()));
            var3.add(var2.effect());
            var3.add(Component.translatable("screen.ftboss.statue_hub.evolution.synergy", var2.synergy()));
            var3.add(Component.translatable("screen.ftboss.statue_hub.evolution.insert_click"));
        }

        return List.copyOf(var3);
    }

    private List<Integer> candidateSlots(ItemStack var1, boolean var2) {
        List var3 = new ArrayList();
        if (this.minecraft != null && this.minecraft.player != null) {
            for (int var4 = 0; var4 < 36; var4++) {
                ItemStack var5 = this.minecraft.player.getInventory().getItem(var4);
                if (var5 != var1
                    && (
                        var2
                            ? FoodWeaponEvolution.ftboss$legacyOrdinary$isBlessingCandidate(var1, var5, this.tint)
                            : FoodWeaponEvolution.ftboss$legacyOrdinary$isSocketCandidate(var1, var5, this.tint) || FoodWeaponProgression.ftboss$legacyOrdinary$isOffering(var1, var5, this.tint)
                    )) {
                    var3.add(var4);
                }
            }

            ItemStack var6 = this.minecraft.player.getInventory().getItem(40);
            if (var6 != var1
                && (var2 ? FoodWeaponEvolution.ftboss$legacyOrdinary$isBlessingCandidate(var1, var6, this.tint) : FoodWeaponEvolution.ftboss$legacyOrdinary$isSocketCandidate(var1, var6, this.tint))) {
                var3.add(40);
            }

            return var3;
        } else {
            return var3;
        }
    }

    public boolean mouseScrolled(double var1, double var3, double horizontalDelta, double var5) {
        if (this.section == OrdinaryStatueHubScreen.Section.EVOLUTION && this.repairPanel.isShowing()) {
            return super.mouseScrolled(var1, var3, horizontalDelta, var5);
        } else if (this.section == OrdinaryStatueHubScreen.Section.MATERIAL) {
            byte var14 = 18;
            int var18 = Math.max(1, (this.materialSlots(this.evolutionWeapon()).size() + var14 - 1) / var14);
            this.materialPage = Math.max(0, Math.min(var18 - 1, this.materialPage + (var5 < 0.0 ? 1 : -1)));
            return true;
        } else {
            if (this.section == OrdinaryStatueHubScreen.Section.DAILY) {
                int var7 = this.contentX();
                int var8 = this.contentY();
                int var9 = this.dailyTaskPages();
                if (var9 > 1 && inside(var1, var3, var7, var8 + 15, this.contentWidth(), 125)) {
                    this.dailyTaskPage = Math.max(0, Math.min(var9 - 1, this.dailyTaskPage + (var5 < 0.0 ? 1 : -1)));
                    return true;
                }

                int var10 = Math.max(1, (this.dailyFoodSlots().size() + 18 - 1) / 18);
                if (var10 > 1) {
                    this.dailyPage = Math.max(0, Math.min(var10 - 1, this.dailyPage + (var5 < 0.0 ? 1 : -1)));
                    return true;
                }
            }

            

            if (this.section == OrdinaryStatueHubScreen.Section.EVOLUTION) {
                List var12 = this.ingredientSlots(this.evolutionWeapon());
                int var16 = Math.max(1, (var12.size() + 18 - 1) / 18);
                if (var16 > 1) {
                    this.ingredientPage = Math.max(0, Math.min(var16 - 1, this.ingredientPage + (var5 < 0.0 ? 1 : -1)));
                    return true;
                }
            }

            if (this.section == OrdinaryStatueHubScreen.Section.DETAILS) {
                List var13 = this.weaponDetails(this.evolutionWeapon());
                int var17 = Math.max(1, (var13.size() + 3 - 1) / 3);
                if (var17 > 1) {
                    this.detailPage = Math.max(0, Math.min(var17 - 1, this.detailPage + (var5 < 0.0 ? 1 : -1)));
                    return true;
                }
            }

            return super.mouseScrolled(var1, var3, horizontalDelta, var5);
        }
    }

    private void heading(GuiGraphics var1, int var2, int var3, String var4, int var5) {
        StatueWeaponUiDraw.heading(var1, this.font, var2, var3, var4, var5);
    }

    private int wrapped(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        return StatueWeaponUiDraw.text(var1, this.font, var2, var3, var4, var5, var6, var7, var8);
    }

    private void wrappedCentered(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        StatueWeaponUiDraw.centered(var1, this.font, var2, var3, var4, var5, var6, var7, var8);
    }

    private OrdinaryStatueHubScreen(OrdinaryOpen var1) {
        super(Component.translatable("screen.ftboss.statue_hub.title"));
        this.materialPart = Part.EDGE;
        this.materialFilter = OrdinaryStatueHubScreen.MaterialFilter.ALL;
        this.selectionWeapon = ItemStack.EMPTY;
        this.selectionFood = ItemStack.EMPTY;
        this.repairPanel = new StatueWeaponRepairPanel(this::evolutionWeapon, () -> this.tint, this::sendRepair, this::updateActionButtons);
        this.hoverTooltip = List.of();
        this.statuePos = var1.pos();
        this.apply(var1);
    }

    private void apply(OrdinaryOpen var1) {
        this.tint = var1.tint();
        this.white = var1.white();
        this.gold = var1.gold();
        this.finaleUnlocked = var1.finaleUnlocked();
        this.finaleSelected = var1.finaleSelected();
        this.statueName = var1.statueName();
        this.dailyTasks = List.copyOf(var1.dailyTasks());
        this.dailyTaskPage = Math.min(this.dailyTaskPage, this.dailyTaskPages() - 1);
        this.updateActionButtons();
    }

    public static void acknowledgeAction(BlockPos var0) {
        Screen var1 = Minecraft.getInstance().screen;
        if (var1 instanceof OrdinaryStatueSwordCavityScreen var2 && var2.matches(var0)) {
            var2.acknowledgeAction();
            return;
        }

        if (var1 instanceof OrdinaryStatueHubScreen var3 && var3.statuePos.equals(var0)) {
            var3.actionPending = false;
            var3.updateActionButtons();
            var3.updateColorButton();
        }
    }

    @Override
    public void onClose() {
        if (!this.actionPending) {
            super.onClose();
        }
    }

    private void updateActionButtons() {
        if (this.evolveButton != null && this.claimButton != null) {
            boolean var1 = this.section == OrdinaryStatueHubScreen.Section.EVOLUTION && !this.repairPanel.isShowing();
            ItemStack var2 = this.evolutionWeapon();
            boolean var3 = !StatueWeaponRepairPanel.blocksChanges(var2);
            this.repairPanel.update(this.section == OrdinaryStatueHubScreen.Section.EVOLUTION, this.actionPending);
            if ((this.offeringSlot >= 0 || this.materialSlot >= 0)
                && (
                    !ItemStack.isSameItemSameComponents(var2, this.selectionWeapon)
                        || !ItemStack.isSameItemSameComponents(this.inventoryItem(this.materialSlot >= 0 ? this.materialSlot : this.offeringSlot), this.selectionFood)
                )) {
                this.offeringSlot = -1;
                this.materialSlot = -1;
            }

            boolean var4 = !var2.isEmpty();
            if (this.bindButton != null) {
                boolean var5 = FoodWeaponSkillLoadout.canBind(var2);
                this.bindButton.visible = var1 && var4;
                this.bindButton.active = this.bindButton.visible && var5 && !this.actionPending;
                this.bindButton.setMessage(Component.translatable(var5 ? "screen.ftboss.evolution.bind" : "screen.ftboss.evolution.bound"));
            }

            this.claimButton.visible = var1;
            this.claimButton.active = var1 && !this.actionPending && var4 && var3;
            this.evolveButton.visible = var1;
            this.evolveButton.active = var1
                && !this.actionPending
                && var4
                && var3
                && FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var2) < FoodWeaponEvolution.ftboss$legacyOrdinary$maxEvolutionLevel(var2)
                && FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionExperience(var2) >= FoodWeaponEvolution.ftboss$legacyOrdinary$nextEvolutionRequirement(var2)
                && this.offeringSlot >= 0
                && this.offeringSlot < 36
                && FoodWeaponProgression.ftboss$legacyOrdinary$isOffering(var2, this.inventoryItem(this.offeringSlot), this.tint);
            if (this.replaceButton != null) {
                this.replaceButton.visible = this.section == OrdinaryStatueHubScreen.Section.MATERIAL && !FourWeaponCavityPresentation.usesCavity(var2);
                Material var7 = WeaponMaterialBuild.materialFor(this.inventoryItem(this.materialSlot));
                boolean var6 = WeaponMaterialCatalog.boss(var7);
                this.replaceButton.setMessage(Component.translatable(var6 ? "screen.ftboss.material.confirm_boss" : "screen.ftboss.material.confirm"));
                this.replaceButton
                    .setTooltip(
                        Tooltip.create(Component.translatable(var6 ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning"))
                    );
                this.replaceButton.active = this.replaceButton.visible
                    && !this.actionPending
                    && var4
                    && var3
                    && this.materialSlot >= 0
                    && this.materialSlot < 36
                    && var7 != Material.ORIGINAL
                    && WeaponMaterialBuild.materials(var2).get(this.materialPart.ordinal()) != var7
                    && this.canReplaceMaterial(var2, WeaponMaterialBuild.preview(var2, this.materialPart, var7));
            }

            if (this.colorButton != null) {
                this.colorButton.active = !this.actionPending;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.section == OrdinaryStatueHubScreen.Section.MATERIAL && !this.actionPending && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
            this.openSwordCavity();
        } else if (this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null) {
            if (this.minecraft
                        .player
                        .distanceToSqr((double)this.statuePos.getX() + 0.5, (double)this.statuePos.getY() + 1.0, (double)this.statuePos.getZ() + 0.5)
                    > 64.0
                || !(this.minecraft.level.getBlockState(this.statuePos).getBlock() instanceof CandyStatueBlock)) {
                this.onClose();
            }
        }
    }

    private void renderMaterials(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        if (this.renderAttachmentExemption(var1, var2, var3, var4)) {
            return;
        }
        this.heading(var1, var2, var3, "screen.ftboss.material.title", var5);
        ItemStack var8 = this.evolutionWeapon();
        if (FourWeaponCavityPresentation.usesCavity(var8)) {
            return;
        }
        if (var8.isEmpty()) {
            this.wrapped(var1, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"), var2 + 10, var3 + 36, var4 - 20, -6449784, 12, 4);
            return;
        }
        this.drawSlot(var1, var2, var3 + 16, var8, var5, false);
        this.wrapped(var1, var8.getHoverName(), var2 + 24, var3 + 21, var4 - 24, -1515055, 11, 1);
        Material var9 = WeaponMaterialBuild.materialFor(this.inventoryItem(this.materialSlot));
        Build var10 = var9 == Material.ORIGINAL ? WeaponMaterialBuild.build(var8) : WeaponMaterialBuild.preview(var8, this.materialPart, var9);

        int var11 = var4 / 2;
        CombatStats var12 = WeaponMaterialBuild.combatStats(var8);
        CombatStats var13 = var9 == Material.ORIGINAL ? var12 : WeaponMaterialBuild.combatPreview(var8, this.materialPart, var9);
        this.renderBuildStats(var1, var2, var3 + 37, var11 - 3, var12, "screen.ftboss.material.current");
        this.renderBuildStats(var1, var2 + var11 + 3, var3 + 37, var11 - 3, var13, "screen.ftboss.material.preview");
        if (inside((double)var6, (double)var7, var2, var3 + 37, var11 - 3, 60)) {
            this.hoverTooltip = this.combatLines(var12);
        }

        if (inside((double)var6, (double)var7, var2 + var11 + 3, var3 + 37, var11 - 3, 60)) {
            this.hoverTooltip = this.combatLines(var13);
        }

        List var14 = WeaponMaterialBuild.materials(var8);
        int var15 = var4 / Part.values().length;

        for (Part var19 : Part.values()) {
            int var20 = var2 + var19.ordinal() * var15;
            Material var21 = (Material)var14.get(var19.ordinal());
            boolean var22 = var19 == this.materialPart;
            var1.fill(var20, var3 + 101, var20 + var15 - 3, var3 + 101 + 24, var22 ? -800309721 : -936301001);
            this.wrapped(
                var1,
                Component.translatable("screen.ftboss.material.part", WeaponMaterialBuild.partName(var19), WeaponMaterialBuild.materialName(var21)),
                var20 + 5,
                var3 + 101 + 3,
                var15 - 13,
                var22 ? -10374 : -1515055,
                10,
                2
            );
            if (inside((double)var6, (double)var7, var20, var3 + 101, var15 - 3, 24)) {
                this.hoverTooltip = this.materialTooltip(WeaponMaterialBuild.partName(var19), var21);
            }
        }

        this.renderMaterialFilters(var1, var2, var3, var4);
        List var26 = this.materialSlots(var8);
        byte var27 = 18;
        int var28 = Math.max(1, (var26.size() + var27 - 1) / var27);
        this.wrapped(
            var1,
            Component.translatable("screen.ftboss.material.inventory", var26.size(), this.materialPage + 1, var28),
            var2,
            var3 + 145,
            var4 - 36,
            -1917592,
            10,
            1
        );
        var1.drawCenteredString(this.font, "<", var2 + var4 - 27, var3 + 145, this.materialPage > 0 ? -1515055 : -6449784);
        var1.drawCenteredString(this.font, ">", var2 + var4 - 7, var3 + 145, this.materialPage + 1 < var28 ? -1515055 : -6449784);

        for (int var29 = 0; var29 < var27; var29++) {
            int var31 = this.materialPage * var27 + var29;
            int var33 = var31 < var26.size() ? (Integer)var26.get(var31) : -1;
            ItemStack var35 = this.inventoryItem(var33);
            int var23 = var2 + var29 % 9 * 18;
            int var24 = var3 + 158 + var29 / 9 * 18;
            this.drawSlot(var1, var23, var24, var35, var5, var33 >= 0 && var33 == this.materialSlot);
            Material var25 = WeaponMaterialBuild.materialFor(var35);
            if (WeaponMaterialCatalog.boss(var25)) {
                var1.drawString(this.font, "!", var23 + 12, var24 + 1, -12470, true);
            }

            if (var33 >= 0 && inside((double)var6, (double)var7, var23, var24, 18, 18)) {
                this.hoverTooltip = this.materialTooltip(var35.getHoverName(), var25);
            }
        }

        MutableComponent var30 = var9 == Material.ORIGINAL
            ? (
                var26.isEmpty()
                    ? Component.translatable("screen.ftboss.material.filter_empty", this.materialFilter.label())
                    : Component.translatable("screen.ftboss.material.select_hint")
            )
            : Component.translatable(
                    WeaponMaterialCatalog.boss(var9) ? "screen.ftboss.material.boss_candidate" : "screen.ftboss.material.candidate",
                    WeaponMaterialBuild.materialName(var9)
                )
                .append(Component.literal("\n"))
                .append(WeaponMaterialBuild.passive(var9));
        this.wrapped(var1, var30, var2 + 172, var3 + 158, var4 - 172, WeaponMaterialCatalog.boss(var9) ? -12470 : -1515055, 11, 3);
        if (inside((double)var6, (double)var7, var2 + 172, var3 + 158, var4 - 172, 36)) {
            this.hoverTooltip = var9 == Material.ORIGINAL
                ? List.of(var30)
                : this.materialTooltip(this.inventoryItem(this.materialSlot).getHoverName(), var9);
        }

        String var32 = WeaponMaterialCatalog.boss(var9) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning";
        String var34 = var9 != Material.ORIGINAL && !this.canReplaceMaterial(var8, WeaponMaterialBuild.preview(var8, this.materialPart, var9))
            ? "message.ftboss.material.repair_first"
            : (WeaponMaterialCatalog.boss(var9) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning");
        this.wrapped(
            var1,
            Component.translatable(var34),
            var2,
            var3 + 195,
            var4,
            var34.equals("message.ftboss.material.repair_first") ? -38037 : -26000,
            10,
            2
        );
        if (inside((double)var6, (double)var7, var2, var3 + 195, var4, 19)) {
            this.hoverTooltip = List.of(
                Component.translatable(var34), Component.translatable(var32), Component.translatable("tooltip.ftboss.material.unique")
            );
        }
    }

    @Override
    public boolean mouseClicked(double var1, double var3, int var5) {
        this.updateActionButtons();
        if (this.section == OrdinaryStatueHubScreen.Section.MATERIAL && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
            if (!this.actionPending) {
                this.openSwordCavity();
            }

            return true;
        } else if (this.section != OrdinaryStatueHubScreen.Section.EVOLUTION
            || !this.repairPanel.isShowing() && !StatueWeaponRepairPanel.blocksChanges(this.evolutionWeapon())) {
            if (this.section == OrdinaryStatueHubScreen.Section.DETAILS && !this.actionPending && (var5 == 1 || var5 == 0 && hasShiftDown())) {
                WeaponDetailView var6 = this.skillAtMouse(var1, var3);
                if (var6 != null && var6.selectable()) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    OrdinaryAction.send(this.statuePos, 12, var6.gourmetSkillIndex());
                    return true;
                }
            }

            if (this.section == OrdinaryStatueHubScreen.Section.EVOLUTION && !this.actionPending && (var5 == 1 || var5 == 0 && hasShiftDown())) {
                ItemStack var16 = this.evolutionWeapon();
                int var7 = gridIndex(var1, var3, this.contentX(), this.contentY() + 164, 9, 2);
                List var8 = this.ingredientSlots(var16);
                int var9 = this.ingredientPage * 9 * 2 + var7;
                if (!var16.isEmpty()
                    && var7 >= 0
                    && var9 < var8.size()
                    && FoodWeaponEvolution.ftboss$legacyOrdinary$isSocketCandidate(var16, this.inventoryItem((Integer)var8.get(var9)), this.tint)) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    OrdinaryAction.send(this.statuePos, 2, (Integer)var8.get(var9));
                    return true;
                }
            }

            if (var5 == 0 && !this.actionPending) {
                if (this.section == OrdinaryStatueHubScreen.Section.MATERIAL) {
                    ItemStack var17 = this.evolutionWeapon();
                    int var23 = this.contentWidth() / OrdinaryStatueHubScreen.MaterialFilter.values().length;

                    for (OrdinaryStatueHubScreen.MaterialFilter var11 : OrdinaryStatueHubScreen.MaterialFilter.values()) {
                        if (!var17.isEmpty() && inside(var1, var3, this.contentX() + var11.ordinal() * var23, this.contentY() + 127, var23 - 3, 15)) {
                            this.materialFilter = var11;
                            this.materialPage = 0;
                            this.materialSlot = -1;
                            this.updateActionButtons();
                            return true;
                        }
                    }

                    List var30 = this.materialSlots(var17);
                    byte var37 = 18;
                    int var42 = Math.max(1, (var30.size() + var37 - 1) / var37);
                    if (inside(var1, var3, this.contentX() + this.contentWidth() - 34, this.contentY() + 145 - 1, 14, 12) && this.materialPage > 0) {
                        this.materialPage--;
                        return true;
                    }

                    if (inside(var1, var3, this.contentX() + this.contentWidth() - 14, this.contentY() + 145 - 1, 14, 12) && this.materialPage + 1 < var42) {
                        this.materialPage++;
                        return true;
                    }

                    int var46 = this.contentWidth() / Part.values().length;

                    for (Part var15 : FourWeaponCavityPresentation.usesCavity(var17) ? new Part[0] : Part.values()) {
                        if (inside(var1, var3, this.contentX() + var15.ordinal() * var46, this.contentY() + 101, var46 - 3, 24)) {
                            this.materialPart = var15;
                            this.updateActionButtons();
                            return true;
                        }
                    }

                    int var49 = gridIndex(var1, var3, this.contentX(), this.contentY() + 158, 9, 2);
                    int var52 = this.materialPage * 9 * 2 + var49;
                    if (!var17.isEmpty() && var49 >= 0 && var52 < var30.size()) {
                        this.selectInventorySlot(var17, (Integer)var30.get(var52), true);
                        return true;
                    }
                }

                if (this.section == OrdinaryStatueHubScreen.Section.CHALLENGE) {
                    int var18 = this.contentX();
                    int var24 = this.contentY();
                    int var31 = inside(var1, var3, var18, var24 + 23, this.contentWidth(), 50)
                        ? 0
                        : (inside(var1, var3, var18, var24 + 79, this.contentWidth(), 61) ? 1 : -1);
                    if (var31 >= 0) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        OrdinaryAction.send(this.statuePos, 8, var31);
                        return true;
                    }
                }

                if (this.section == OrdinaryStatueHubScreen.Section.DETAILS) {
                    List var19 = this.weaponDetails(this.evolutionWeapon());
                    int var25 = Math.max(1, (var19.size() + 3 - 1) / 3);
                    int var32 = this.contentX();
                    int var38 = this.contentY() + 77;
                    if (inside(var1, var3, var32 + this.contentWidth() - 96, var38, 18, 13) && this.detailPage > 0) {
                        this.detailPage--;
                        return true;
                    }

                    if (inside(var1, var3, var32 + this.contentWidth() - 18, var38, 18, 13) && this.detailPage + 1 < var25) {
                        this.detailPage++;
                        return true;
                    }

                    int var43 = this.contentY() + 96;
                    if (inside(var1, var3, var32, var43, this.contentWidth(), 114)) {
                        int var47 = ((int)var3 - var43) / 38;
                        int var50 = this.detailPage * 3 + var47;
                        if (var50 >= 0 && var50 < var19.size()) {
                            WeaponDetailView var53 = (WeaponDetailView)var19.get(var50);
                            if (var53.selectable()) {
                                if (!var53.selected()) {
                                    this.actionPending = true;
                                    this.updateActionButtons();
                                    OrdinaryAction.send(this.statuePos, 9, var53.gourmetSkillIndex());
                                }

                                return true;
                            }
                        }
                    }
                }

                if (this.section == OrdinaryStatueHubScreen.Section.DAILY) {
                    int var20 = this.contentX();
                    int var26 = this.contentY();
                    if (inside(var1, var3, var20 + this.contentWidth() - 96, var26 - 2, 18, 13) && this.dailyTaskPage > 0) {
                        this.dailyTaskPage--;
                        return true;
                    }

                    if (inside(var1, var3, var20 + this.contentWidth() - 18, var26 - 2, 18, 13) && this.dailyTaskPage + 1 < this.dailyTaskPages()) {
                        this.dailyTaskPage++;
                        return true;
                    }

                    List var33 = this.dailyFoodSlots();
                    int var39 = gridIndex(var1, var3, this.contentX(), this.contentY() + 170, 9, 2);
                    int var44 = this.dailyPage * 9 * 2 + var39;
                    if (var39 >= 0 && var44 >= 0 && var44 < var33.size()) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        OrdinaryAction.send(this.statuePos, 4, (Integer)var33.get(var44));
                        return true;
                    }
                }

                

                if (this.section == OrdinaryStatueHubScreen.Section.EVOLUTION) {
                    ItemStack var22 = this.evolutionWeapon();
                    if (!var22.isEmpty()) {
                        int var28 = this.contentX();
                        int var35 = this.contentY();
                        int var41 = gridIndex(var1, var3, var28, var35 + 90, 10, 3);
                        int var45 = FoodWeaponEvolution.ftboss$legacyOrdinary$socketCount(var22);
                        if (var41 >= 0 && var41 < var45) {
                            this.actionPending = true;
                            this.updateActionButtons();
                            OrdinaryAction.send(this.statuePos, 3, var41);
                            return true;
                        }

                        int var48 = gridIndex(var1, var3, var28, var35 + 164, 9, 2);
                        List var51 = this.ingredientSlots(var22);
                        int var54 = this.ingredientPage * 9 * 2 + var48;
                        if (var48 >= 0 && var54 >= 0 && var54 < var51.size()) {
                            int var55 = (Integer)var51.get(var54);
                            if (var55 < 36 && FoodWeaponProgression.ftboss$legacyOrdinary$isOffering(var22, this.inventoryItem(var55), this.tint)) {
                                this.selectInventorySlot(var22, var55, false);
                            }

                            return true;
                        }
                    }
                }
            }

            return super.mouseClicked(var1, var3, var5);
        } else {
            return super.mouseClicked(var1, var3, var5);
        }
    }

    private void openSwordCavity() {
        if (!this.actionPending) {
            ItemStack var1 = this.evolutionWeapon();
            if (!FourWeaponCavityPresentation.usesCavity(var1)) {
                this.section = OrdinaryStatueHubScreen.Section.MATERIAL;
                this.updateActionButtons();
            } else {
                if (FourWeaponFoundationAdapter.applies(var1)) {
                    if (!FourWeaponClientApi.openContainer(var1, this.statuePos)) {
                        return;
                    }
                } else if (!CavityOpenAction.send(this.statuePos, var1)) {
                    return;
                }

                this.section = OrdinaryStatueHubScreen.Section.DAILY;
                this.materialSlot = -1;
                this.offeringSlot = -1;
            }
        }
    }

    public static void open(OrdinaryOpen var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.screen instanceof SwordCavityContainerScreen var2 && ((SwordCavityMenu)var2.getMenu()).matches(var0.pos())) {
            return;
        }

        if (var1.screen instanceof OrdinaryStatueSwordCavityScreen var5 && var5.matches(var0.pos())) {
            var5.parent().apply(var0);
            return;
        }

        if (var1.screen instanceof OrdinaryStatueHubScreen var6 && var6.statuePos.equals(var0.pos())) {
            var6.apply(var0);
            return;
        }

        var1.setScreen(new OrdinaryStatueHubScreen(var0));
    }

    private void addBuildInspectorEntry() {
        if (this.tint == 0) {
            this.addRenderableWidget(
                StatueWeaponRepairNavigation.inspectorEntry(
                    this,
                    this.statuePos,
                    this::evolutionWeapon,
                    this::openBuildAdjustment,
                    this::canOpenBuildInspector,
                    this.left + 11,
                    this.top + 42 + 8 + OrdinaryStatueHubScreen.Section.values().length * 30,
                    94
                )
            );
        }
    }

    private boolean canOpenBuildInspector() {
        return !this.actionPending;
    }

    private void openBuildAdjustment() {
        this.openSwordCavity();
    }

    private boolean renderAttachmentExemption(GuiGraphics var1, int var2, int var3, int var4) {
        if (!WeaponAttachmentPolicy.excluded(this.evolutionWeapon())) {
            return false;
        } else {
            if (this.replaceButton != null) {
                this.replaceButton.visible = false;
            }

            this.wrapped(var1, Component.translatable("screen.ftboss.material.not_required"), var2 + 10, var3 + 36, var4 - 20, -1515055, 12, 4);
            return true;
        }
    }

    private void addWeaponRepairEntry() {
        this.addRenderableWidget(
            StatueWeaponRepairNavigation.hubEntry(this, this.left + 11, this.top + 42 + 8 + OrdinaryStatueHubScreen.Section.values().length * 30, 94)
        );
        StatueWeaponRepairNavigation.openPending(this, this.statuePos, this.evolutionWeapon());
    }

    void openWeaponRepair() {
        this.section = OrdinaryStatueHubScreen.Section.EVOLUTION;
        this.offeringSlot = -1;
        this.materialSlot = -1;
        this.repairPanel.open();
        this.updateActionButtons();
    }

    private static enum MaterialFilter {
        ALL(null),
        BASE(Group.BASE),
        INGOT(Group.INGOT),
        BOSS(Group.BOSS);

        final Group group;

        private MaterialFilter(Group var3) {
            this.group = var3;
        }

        Component label() {
            return Component.translatable("screen.ftboss.material.filter." + this.name().toLowerCase(Locale.ROOT));
        }

        boolean accepts(Material var1) {
            return this.group == null || WeaponMaterialCatalog.group(var1) == this.group;
        }
    }

    private static enum Section {
        DAILY("screen.ftboss.statue_hub.tab.daily"),
        CHALLENGE("screen.ftboss.statue_hub.tab.challenge"),

        EVOLUTION("screen.ftboss.statue_hub.tab.evolution"),
        DETAILS("screen.ftboss.statue_hub.tab.details"),
        MATERIAL("screen.ftboss.statue_hub.tab.material");
        final String key;

        private Section(String key) {
            this.key = key;
        }
    }
    private static Component ordinaryQualityName(ItemStack stack) {
        return Component.translatable("quality.ftboss.evolution." + FoodWeaponProgression.ftboss$legacyOrdinary$quality(stack));
    }

    public boolean matchesMenu(net.farmingtales.ftboss.menu.SwordCavityMenu menu) {
        return menu != null && menu.matches(this.statuePos);
    }

}
