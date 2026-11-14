/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.FourWeaponCavityPresentation;
import net.farmingtales.ftboss.client.StatueSwordCavityPanel;
import net.farmingtales.ftboss.client.StatueSwordCavityScreen;
import net.farmingtales.ftboss.client.StatueWeaponDetailsPanel;
import net.farmingtales.ftboss.client.StatueWeaponRepairNavigation;
import net.farmingtales.ftboss.client.StatueWeaponRepairPanel;
import net.farmingtales.ftboss.client.StatueWeaponUiDraw;
import net.farmingtales.ftboss.client.SwordCavityContainerScreen;
import net.farmingtales.ftboss.item.FoodWeaponBossPassive;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class StatueHubScreen
extends Screen {
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
    private List<FoodWeaponEvolution.StatueDailyView> dailyTasks;
    private boolean white;
    private boolean gold;
    private Section section = Section.DAILY;
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
    private FoodWeaponBuildRules.Part materialPart = FoodWeaponBuildRules.Part.EDGE;
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
    private MaterialFilter materialFilter = MaterialFilter.ALL;
    private static final int DETAILS_PER_PAGE = 3;
    private int offeringSlot = -1;
    private ItemStack selectionFood;
    private static final int TAB_SPACING = 30;
    private int materialSlot = -1;
    private static final int MATERIAL_FOOD_Y = 158;
    private final StatueSwordCavityPanel swordCavityPanel;

    public static void updateAppearance(BlockPos pos, boolean white, boolean gold) {
        Screen screen = Minecraft.m_91087_().f_91080_;
        if (screen instanceof StatueHubScreen) {
            StatueHubScreen hub = (StatueHubScreen)screen;
            if (hub.statuePos.equals((Object)pos)) {
                hub.white = white;
                hub.gold = gold;
                hub.updateColorButton();
            }
        }
    }

    private Component colorLabel() {
        if (this.gold) {
            return Component.m_237115_((String)"screen.ftboss.statue_hub.color.restore");
        }
        if (this.white && this.finaleUnlocked) {
            return Component.m_237115_((String)"screen.ftboss.statue_hub.color.gold");
        }
        return Component.m_237115_((String)(this.white ? "screen.ftboss.statue_hub.color.restore" : "screen.ftboss.statue_hub.color.white"));
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
        int i;
        this.heading(graphics, x, y, "screen.ftboss.statue_hub.daily.title", accent);
        int taskPages = this.dailyTaskPages();
        this.dailyTaskPage = Math.min(this.dailyTaskPage, taskPages - 1);
        int leftArrowX = x + width - 96;
        int rightArrowX = x + width - 18;
        graphics.m_280509_(leftArrowX, y - 2, leftArrowX + 18, y + 11, this.dailyTaskPage > 0 ? -936301001 : -400285143);
        graphics.m_280509_(rightArrowX, y - 2, rightArrowX + 18, y + 11, this.dailyTaskPage + 1 < taskPages ? -936301001 : -400285143);
        graphics.m_280137_(this.f_96547_, "<", leftArrowX + 9, y, this.dailyTaskPage > 0 ? -1515055 : -6449784);
        graphics.m_280137_(this.f_96547_, ">", rightArrowX + 9, y, this.dailyTaskPage + 1 < taskPages ? -1515055 : -6449784);
        MutableComponent page = Component.m_237110_((String)"screen.ftboss.statue_hub.daily.task_page", (Object[])new Object[]{this.dailyTaskPage + 1, taskPages});
        graphics.m_280653_(this.f_96547_, (Component)page, x + width - 48, y, -6449784);
        int cardY = y + 20;
        if (this.dailyTasks.isEmpty()) {
            this.wrapped(graphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.daily.none"), x + 4, cardY + 5, width - 8, -6449784, 11, 4);
        }
        int taskStart = this.dailyTaskPage * 4;
        for (int visible = 0; visible < 4 && (i = taskStart + visible) < this.dailyTasks.size(); ++visible) {
            MutableComponent status;
            MutableComponent details;
            List clipped;
            FoodWeaponEvolution.StatueDailyView task = this.dailyTasks.get(i);
            int stateColor = task.state() == 2 ? -8947849 : (task.state() == 1 ? -10103922 : -1917592);
            graphics.m_280509_(x, cardY, x + width, cardY + 26, -936301001);
            graphics.m_280509_(x, cardY, x + 3, cardY + 26, stateColor);
            MutableComponent taskTitle = Component.m_237110_((String)"screen.ftboss.statue_hub.daily.task", (Object[])new Object[]{i + 1, task.request()});
            List titleLine = this.f_96547_.m_92923_((FormattedText)taskTitle, width - 16);
            if (!titleLine.isEmpty()) {
                graphics.m_280649_(this.f_96547_, (FormattedCharSequence)titleLine.get(0), x + 8, cardY + 3, -1515055, false);
            }
            if (!(clipped = this.f_96547_.m_92923_((FormattedText)(details = (status = task.state() == 2 ? Component.m_237115_((String)"screen.ftboss.statue_hub.daily.claimed") : (task.state() == 1 ? Component.m_237115_((String)"screen.ftboss.statue_hub.daily.ready") : Component.m_237110_((String)"screen.ftboss.statue_hub.daily.progress", (Object[])new Object[]{task.progress(), task.required()}))).m_6881_().m_7220_((Component)Component.m_237113_((String)" \u00b7 ")).m_7220_((Component)Component.m_237110_((String)"screen.ftboss.statue_hub.daily.reward", (Object[])new Object[]{task.reward()}))), width - 16)).isEmpty()) {
                graphics.m_280649_(this.f_96547_, (FormattedCharSequence)clipped.get(0), x + 8, cardY + 14, stateColor, false);
            }
            cardY += 29;
        }
        List<Integer> foods = this.dailyFoodSlots();
        int pageSize = 18;
        int pages = Math.max(1, (foods.size() + pageSize - 1) / pageSize);
        this.dailyPage = Math.min(this.dailyPage, pages - 1);
        graphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.daily.available", (Object[])new Object[]{foods.size(), this.dailyPage + 1, pages}), x + 1, y + 154, -1917592, false);
        int start = this.dailyPage * pageSize;
        int foodY = y + 170;
        for (int cell = 0; cell < pageSize; ++cell) {
            int sx = x + cell % 9 * 18;
            int sy = foodY + cell / 9 * 18;
            int listIndex = start + cell;
            ItemStack stack = listIndex < foods.size() && this.f_96541_ != null && this.f_96541_.f_91074_ != null ? this.f_96541_.f_91074_.m_150109_().m_8020_(foods.get(listIndex).intValue()) : ItemStack.f_41583_;
            this.drawSlot(graphics, sx, sy, stack, accent, false);
            if (!StatueHubScreen.inside(mouseX, mouseY, sx, sy, 18, 18) || stack.m_41619_()) continue;
            this.hoverTooltip = this.dailyFoodTooltip(stack);
        }
        graphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.daily.hint"), x + 172, y + 183, -6449784, false);
        graphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.daily.page_hint"), x + 172, y + 198, -6449784, false);
    }

    private int dailyTaskPages() {
        return Math.max(1, (this.dailyTasks.size() + 4 - 1) / 4);
    }

    private List<Integer> dailyFoodSlots() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null) {
            return result;
        }
        for (int slot = 0; slot < 36; ++slot) {
            ItemStack stack = this.f_96541_.f_91074_.m_150109_().m_8020_(slot);
            if (!FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, stack, this.tint)) continue;
            result.add(slot);
        }
        ItemStack offhand = this.f_96541_.f_91074_.m_150109_().m_8020_(40);
        if (FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, offhand, this.tint)) {
            result.add(40);
        }
        return result;
    }

    private List<Component> dailyFoodTooltip(ItemStack stack) {
        return List.of(stack.m_41786_(), Component.m_237115_((String)"screen.ftboss.statue_hub.daily.submit_click"));
    }

    private void renderChallenge(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        int finaleColor;
        this.heading(graphics, x, y, "screen.ftboss.statue_hub.challenge.title", accent);
        boolean normalHover = StatueHubScreen.inside(mouseX, mouseY, x, y + 23, width, 50);
        boolean finaleHover = StatueHubScreen.inside(mouseX, mouseY, x, y + 79, width, 61);
        int normalBorder = !this.finaleSelected ? -9381480 : (normalHover ? 0xFF000000 | accent : -11909552);
        graphics.m_280509_(x, y + 23, x + width, y + 73, normalBorder);
        graphics.m_280509_(x + 2, y + 25, x + width - 2, y + 71, -936301001);
        graphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.challenge.normal"), x + 10, y + 31, -9381480, false);
        MutableComponent normalState = Component.m_237115_((String)(this.finaleSelected ? "screen.ftboss.statue_hub.challenge.click_select" : "screen.ftboss.statue_hub.challenge.selected"));
        graphics.m_280614_(this.f_96547_, (Component)normalState, x + width - 10 - this.f_96547_.m_92852_((FormattedText)normalState), y + 31, !this.finaleSelected && !normalHover ? -9381480 : -1515055, false);
        this.wrapped(graphics, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.challenge.normal_desc", (Object[])new Object[]{this.statueName}), x + 10, y + 46, width - 20, -6449784, 11, 2);
        int n = finaleColor = this.finaleUnlocked ? -38031 : -8949123;
        int finaleBorder = this.finaleSelected ? -38031 : (finaleHover && this.finaleUnlocked ? 0xFF000000 | accent : -11909552);
        graphics.m_280509_(x, y + 79, x + width, y + 140, finaleBorder);
        graphics.m_280509_(x + 2, y + 81, x + width - 2, y + 138, -936301001);
        graphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)(this.finaleUnlocked ? "screen.ftboss.statue_hub.challenge.finale_unlocked" : "screen.ftboss.statue_hub.challenge.finale_locked")), x + 10, y + 87, finaleColor, false);
        MutableComponent finaleState = Component.m_237115_((String)(!this.finaleUnlocked ? "screen.ftboss.statue_hub.challenge.locked" : (this.finaleSelected ? "screen.ftboss.statue_hub.challenge.selected" : "screen.ftboss.statue_hub.challenge.click_select")));
        graphics.m_280614_(this.f_96547_, (Component)finaleState, x + width - 10 - this.f_96547_.m_92852_((FormattedText)finaleState), y + 87, this.finaleSelected ? -38031 : -6449784, false);
        this.wrapped(graphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.challenge.finale_desc"), x + 10, y + 102, width - 20, -6449784, 11, 3);
        this.wrapped(graphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.challenge.hint"), x + 2, y + 149, width - 4, -6449784, 11, 4);
    }

    private void drawExperienceBar(GuiGraphics graphics, int x, int y, int width, int experience, int floor, int required, boolean maxed, int accent) {
        graphics.m_280509_(x, y, x + width, y + 9, -16054002);
        graphics.m_280509_(x + 1, y + 1, x + width - 1, y + 8, -13357253);
        double ratio = maxed ? 1.0 : (required <= floor ? 0.0 : Math.max(0.0, Math.min(1.0, (double)(experience - floor) / (double)(required - floor))));
        int filled = (int)Math.round((double)(width - 2) * ratio);
        if (filled > 0) {
            graphics.m_280024_(x + 1, y + 1, x + 1 + filled, y + 8, 0xFF000000 | accent, -1656757);
            graphics.m_280509_(x + 1, y + 1, x + 1 + filled, y + 2, -1996488705);
        }
    }

    private void drawAccessorySlot(GuiGraphics graphics, int x, int y, FoodWeaponEvolution.SocketView view, int accent, int number) {
        this.drawSlot(graphics, x, y, view == null ? ItemStack.f_41583_ : view.stack(), view == null ? accent : view.color(), view != null && view.matching());
        if (view == null) {
            String label = Integer.toString(number);
            graphics.m_280056_(this.f_96547_, label, x + 9 - this.f_96547_.m_92895_(label) / 2, y + 5, -10659740, false);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, ItemStack stack, int color, boolean matching) {
        int border = matching ? -11173 : 0xFF000000 | color;
        graphics.m_280509_(x, y, x + 18, y + 18, -16185333);
        graphics.m_280509_(x + 1, y + 1, x + 18 - 1, y + 18 - 1, border);
        graphics.m_280509_(x + 2, y + 2, x + 18 - 2, y + 18 - 2, -13948880);
        if (!stack.m_41619_()) {
            graphics.m_280480_(stack, x + 1, y + 1);
            if (stack.m_41613_() > 1) {
                graphics.m_280056_(this.f_96547_, Integer.toString(stack.m_41613_()), x + 16 - this.f_96547_.m_92895_(Integer.toString(stack.m_41613_())), y + 10, -1, true);
            }
        }
    }

    private List<Component> socketTooltip(FoodWeaponEvolution.SocketView view, int slot) {
        if (view == null) {
            return List.of(Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.socket_empty_slot", (Object[])new Object[]{slot}));
        }
        return List.of(Component.m_237113_((String)("#" + slot + " ")).m_7220_(view.stack().m_41786_()), Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.socket_type", (Object[])new Object[]{view.type(), view.tier()}), view.effect(), Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.synergy", (Object[])new Object[]{view.synergy()}), Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.remove_click"));
    }

    private List<Component> blessingFoodTooltip(ItemStack stack) {
        FoodWeaponEvolution.SocketView view = FoodWeaponEvolution.ingredientView(stack);
        if (view == null) {
            return List.of(stack.m_41786_());
        }
        return List.of(stack.m_41786_(), Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.socket_type", (Object[])new Object[]{view.type(), view.tier()}), Component.m_237115_((String)"screen.ftboss.statue_hub.enchant.offer_click"));
    }

    private List<Integer> blessingSlots(ItemStack weapon) {
        return this.candidateSlots(weapon, true);
    }

    private List<Integer> ingredientSlots(ItemStack weapon) {
        return this.candidateSlots(weapon, false);
    }

    private static int gridIndex(double mouseX, double mouseY, int x, int y, int columns, int rows) {
        if (!StatueHubScreen.inside(mouseX, mouseY, x, y, columns * 18, rows * 18)) {
            return -1;
        }
        return ((int)mouseY - y) / 18 * columns + ((int)mouseX - x) / 18;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= (double)x && mouseX < (double)(x + width) && mouseY >= (double)y && mouseY < (double)(y + height);
    }

    private ItemStack evolutionWeapon() {
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null) {
            return ItemStack.f_41583_;
        }
        ItemStack main = this.f_96541_.f_91074_.m_21205_();
        if (FourWeaponCavityPresentation.selectableWeapon(main)) {
            return main;
        }
        ItemStack off = this.f_96541_.f_91074_.m_21206_();
        return FourWeaponCavityPresentation.selectableWeapon(off) ? off : ItemStack.f_41583_;
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

    public boolean m_7043_() {
        return false;
    }

    private void sendRepair(int n, int n2) {
        ItemStack itemStack = this.evolutionWeapon();
        if (this.actionPending || FoodWeaponLifecycle.weaponId(itemStack) == null) {
            return;
        }
        this.actionPending = true;
        this.updateActionButtons();
        StatueMenuNetwork.RepairAction.send(this.statuePos, itemStack, n, n2);
    }

    protected void m_7856_() {
        this.left = (this.f_96543_ - 490) / 2;
        this.top = (this.f_96544_ - 300) / 2;
        int n = this.left + 11;
        int n2 = this.top + 42 + 8;
        int n3 = 0;
        Section[] sectionArray = Section.values();
        int n4 = sectionArray.length;
        for (int i = 0; i < n4; ++i) {
            Section section;
            Section section2 = section = sectionArray[i];
            this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)section.key), button -> {
                if (section2 == Section.MATERIAL && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
                    if (!this.actionPending) {
                        this.openSwordCavity();
                    }
                    return;
                }
                this.section = section2;
                this.updateActionButtons();
            }).m_252987_(n, n2 + n3 * 30, 94, 25).m_253136_());
            ++n3;
        }
        this.colorButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)this.colorLabel(), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, 0);
            this.actionPending = true;
            this.updateActionButtons();
        }).m_252987_(this.left + 490 - 104, this.top + 300 - 29, 88, 20).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)CommonComponents.f_130655_, button -> this.m_7379_()).m_252987_(this.left + 12, this.top + 300 - 29, 72, 20).m_253136_());
        this.claimButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.claim"), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, 6);
            this.actionPending = true;
            this.updateActionButtons();
        }).m_252987_(this.left + 145, this.top + 300 - 29, 96, 20).m_253136_());
        this.evolveButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.evolve"), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, 5, this.offeringSlot);
            this.offeringSlot = -1;
            this.actionPending = true;
            this.updateActionButtons();
        }).m_252987_(this.left + 250, this.top + 300 - 29, 120, 20).m_253136_());
        this.evolveButton.m_257544_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.repair.level_warning")));
        this.replaceButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.material.confirm"), button -> {
            if (FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
                return;
            }
            StatueMenuNetwork.Action.send(this.statuePos, 10, this.materialPart.ordinal() * 36 + this.materialSlot);
            this.materialSlot = -1;
            this.actionPending = true;
            this.updateActionButtons();
        }).m_252987_(this.left + 145, this.top + 300 - 29, 225, 20).m_253136_());
        this.bindButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.evolution.bind"), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, 11);
            this.actionPending = true;
            this.updateActionButtons();
        }).m_252987_(this.contentX() + this.contentWidth() - 88, this.contentY() + 24, 82, 20).m_253136_());
        this.bindButton.m_257544_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.evolution.bind_hint")));
        this.repairPanel.init(button -> this.m_142416_((GuiEventListener)button), this.contentX(), this.contentY(), this.contentWidth(), this.top + 300 - 29);
        this.updateActionButtons();
        this.addBuildInspectorEntry();
        this.addWeaponRepairEntry();
    }

    private void updateColorButton() {
        if (this.colorButton != null) {
            this.colorButton.m_93666_(this.colorLabel());
            this.colorButton.f_93623_ = !this.actionPending;
        }
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.m_280273_(guiGraphics);
        this.hoverTooltip = List.of();
        this.updateActionButtons();
        int n3 = StatueHubScreen.accent(this.tint);
        guiGraphics.m_280509_(this.left - 2, this.top - 2, this.left + 490 + 2, this.top + 300 + 2, 0xD0000000 | n3);
        guiGraphics.m_280024_(this.left, this.top, this.left + 490, this.top + 300, -266856677, -267580400);
        guiGraphics.m_280509_(this.left, this.top, this.left + 490, this.top + 42, -534108368);
        guiGraphics.m_280509_(this.left, this.top, this.left + 5, this.top + 42, 0xFF000000 | n3);
        guiGraphics.m_280509_(this.left + 8, this.top + 42 + 4, this.left + 116, this.top + 300 - 36, -1978264294);
        guiGraphics.m_280614_(this.f_96547_, this.f_96539_, this.left + 17, this.top + 10, -1515055, false);
        String string = this.gold ? "screen.ftboss.statue_hub.color.gold_state" : (this.white ? "screen.ftboss.statue_hub.color.white_state" : "screen.ftboss.statue_hub.color.colored_state");
        MutableComponent mutableComponent = Component.m_237110_((String)"screen.ftboss.statue_hub.subtitle", (Object[])new Object[]{this.statueName, Component.m_237115_((String)string)});
        guiGraphics.m_280614_(this.f_96547_, (Component)mutableComponent, this.left + 17, this.top + 25, -6449784, false);
        int n4 = this.top + 42 + 8 + this.section.ordinal() * 30;
        guiGraphics.m_280509_(this.left + 8, n4, this.left + 11, n4 + 25, 0xFF000000 | n3);
        int n5 = this.contentX();
        int n6 = this.contentY();
        int n7 = this.contentWidth();
        guiGraphics.m_280509_(n5 - 5, n6 - 4, n5 + n7 + 5, this.top + 300 - 36, -400285143);
        switch (this.section) {
            case DAILY: {
                this.renderDaily(guiGraphics, n5, n6, n7, n3, n, n2);
                break;
            }
            case CHALLENGE: {
                this.renderChallenge(guiGraphics, n5, n6, n7, n3, n, n2);
                break;
            }
            case ENCHANT: {
                this.renderEnchant(guiGraphics, n5, n6, n7, n3, n, n2);
                break;
            }
            case EVOLUTION: {
                this.renderEvolution(guiGraphics, n5, n6, n7, n3, n, n2);
                break;
            }
            case MATERIAL: {
                this.renderMaterials(guiGraphics, n5, n6, n7, n3, n, n2);
                break;
            }
            case DETAILS: {
                this.renderWeaponDetails(guiGraphics, n5, n6, n7, n3, n, n2);
            }
        }
        int n8 = this.left + 490 - 114;
        int n9 = this.top + 300 - 25;
        guiGraphics.m_280509_(n8, n9, n8 + 8, n9 + 8, this.gold ? -12470 : (this.white ? -855832 : 0xFF000000 | n3));
        super.m_88315_(guiGraphics, n, n2, f);
        if (!this.hoverTooltip.isEmpty()) {
            guiGraphics.m_280677_(this.f_96547_, this.hoverTooltip, Optional.empty(), n, n2);
        }
    }

    private void renderEnchant(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        this.heading(guiGraphics, n, n2, "screen.ftboss.statue_hub.enchant.title", n4);
        ItemStack itemStack = this.evolutionWeapon();
        if (itemStack.m_41619_()) {
            guiGraphics.m_280509_(n, n2 + 24, n + n3, n2 + 96, -936301001);
            this.wrappedCentered(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.enchant.no_weapon"), n + 18, n2 + 45, n3 - 36, -6449784, 12, 4);
            return;
        }
        guiGraphics.m_280509_(n, n2 + 22, n + n3, n2 + 72, -936301001);
        this.drawSlot(guiGraphics, n + 8, n2 + 29, itemStack, n4, false);
        guiGraphics.m_280614_(this.f_96547_, itemStack.m_41786_(), n + 34, n2 + 27, -1515055, false);
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.enchant.rule"), n + 34, n2 + 40, n3 - 44, -6449784, 11, 3);
        List<FoodWeaponEvolution.FoodBlessingView> list = FoodWeaponEvolution.foodBlessingViews(itemStack);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.enchant.current", (Object[])new Object[]{list.size(), 6}), n + 1, n2 + 78, -4682497, false);
        if (list.isEmpty()) {
            guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.enchant.none"), n + 8, n2 + 92, -6449784, false);
        } else {
            for (int i = 0; i < Math.min(6, list.size()); ++i) {
                FoodWeaponEvolution.FoodBlessingView foodBlessingView = list.get(i);
                MutableComponent mutableComponent = Component.m_237113_((String)"\u2022 ").m_7220_(foodBlessingView.name()).m_7220_((Component)Component.m_237113_((String)(" " + foodBlessingView.rank() + " \u00b7 "))).m_7220_(foodBlessingView.description());
                this.wrapped(guiGraphics, (Component)mutableComponent, n + 8, n2 + 92 + i * 10, n3 - 16, i == list.size() - 1 ? -1588632 : -1515055, 11, 1);
                if (!StatueHubScreen.inside(n5, n6, n, n2 + 92 + i * 10, n3, 10)) continue;
                this.hoverTooltip = List.of(foodBlessingView.name(), foodBlessingView.description());
            }
        }
        List<Integer> list2 = this.blessingSlots(itemStack);
        int n7 = 18;
        int n8 = Math.max(1, (list2.size() + n7 - 1) / n7);
        this.ingredientPage = Math.min(this.ingredientPage, n8 - 1);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.enchant.foods", (Object[])new Object[]{list2.size(), this.ingredientPage + 1, n8}), n + 1, n2 + 157, -4682497, false);
        int n9 = n2 + 170;
        int n10 = this.ingredientPage * n7;
        for (int i = 0; i < n7; ++i) {
            int n11 = n + i % 9 * 18;
            int n12 = n9 + i / 9 * 18;
            int n13 = n10 + i;
            ItemStack itemStack2 = n13 < list2.size() && this.f_96541_ != null && this.f_96541_.f_91074_ != null ? this.f_96541_.f_91074_.m_150109_().m_8020_(list2.get(n13).intValue()) : ItemStack.f_41583_;
            this.drawSlot(guiGraphics, n11, n12, itemStack2, n4, false);
            if (!StatueHubScreen.inside(n5, n6, n11, n12, 18, 18) || itemStack2.m_41619_()) continue;
            this.hoverTooltip = this.blessingFoodTooltip(itemStack2);
        }
        String string = list.size() >= 6 ? "screen.ftboss.statue_hub.enchant.complete" : (list2.isEmpty() ? "screen.ftboss.statue_hub.enchant.no_offerings" : "screen.ftboss.statue_hub.enchant.page_hint");
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)string), n + 172, n2 + 177, n3 - 172, list.size() >= 6 ? -9185382 : (list2.isEmpty() ? -26000 : -6449784), 11, 3);
    }

    private void renderEvolution(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7;
        int n8;
        if (this.repairPanel.isShowing()) {
            this.hoverTooltip = this.repairPanel.render(guiGraphics, this.f_96547_, n, n2, n3, n4, n5, n6, this.actionPending);
            return;
        }
        this.heading(guiGraphics, n, n2, "screen.ftboss.statue_hub.evolution.title", n4);
        ItemStack itemStack = this.evolutionWeapon();
        if (itemStack.m_41619_()) {
            guiGraphics.m_280509_(n, n2 + 24, n + n3, n2 + 96, -936301001);
            this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.no_weapon"), n + 10, n2 + 36, n3 - 20, -6449784, 12, 4);
            return;
        }
        guiGraphics.m_280509_(n, n2 + 21, n + n3, n2 + 74, -936301001);
        this.drawSlot(guiGraphics, n + 8, n2 + 30, itemStack, n4, false);
        this.wrapped(guiGraphics, itemStack.m_41786_(), n + 34, n2 + 27, n3 - 130, -1515055, 11, 1);
        int n9 = FoodWeaponEvolution.evolutionLevel(itemStack);
        int n10 = FoodWeaponEvolution.maxEvolutionLevel(itemStack);
        int n11 = FoodWeaponEvolution.evolutionExperience(itemStack);
        int n12 = FoodWeaponEvolution.nextEvolutionRequirement(itemStack);
        int n13 = FoodWeaponEvolution.currentEvolutionFloor(itemStack);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.level", (Object[])new Object[]{n9, n10}), n + 34, n2 + 39, -1917592, false);
        this.drawExperienceBar(guiGraphics, n + 34, n2 + 53, n3 - 44, n11, n13, n12, n9 >= n10, n4);
        MutableComponent mutableComponent = n9 >= n10 ? Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.maxed") : Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.xp", (Object[])new Object[]{n11, n12});
        guiGraphics.m_280653_(this.f_96547_, (Component)mutableComponent, n + 34 + (n3 - 44) / 2, n2 + 65, -1515055);
        List<FoodWeaponEvolution.SocketView> list = FoodWeaponEvolution.socketViews(itemStack);
        int n14 = FoodWeaponEvolution.socketCapacity(itemStack);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.sockets_full", (Object[])new Object[]{list.size(), n14}), n + 1, n2 + 78, -9250406, false);
        int n15 = n2 + 90;
        for (n8 = 0; n8 < Math.min(n14, 30); ++n8) {
            int n16 = n + n8 % 10 * 18;
            n7 = n15 + n8 / 10 * 18;
            FoodWeaponEvolution.SocketView socketView = n8 < list.size() ? list.get(n8) : null;
            this.drawAccessorySlot(guiGraphics, n16, n7, socketView, n4, n8 + 1);
            if (!StatueHubScreen.inside(n5, n6, n16, n7, 18, 18)) continue;
            this.hoverTooltip = this.socketTooltip(socketView, n8 + 1);
        }
        n8 = n + 180 + 10;
        guiGraphics.m_280509_(n8, n2 + 90, n + n3, n2 + 144, -1725948635);
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.socket_help"), n8 + 7, n2 + 96, n3 - (n8 - n) - 14, -6449784, 11, 4);
        if (StatueHubScreen.inside(n5, n6, n8, n2 + 90, n3 - (n8 - n), 54)) {
            this.hoverTooltip = List.of(Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.socket_help"));
        }
        List<Integer> list2 = this.ingredientSlots(itemStack);
        n7 = 18;
        int n17 = Math.max(1, (list2.size() + n7 - 1) / n7);
        this.ingredientPage = Math.min(this.ingredientPage, n17 - 1);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.ingredients", (Object[])new Object[]{list2.size(), this.ingredientPage + 1, n17}), n + 1, n2 + 151, -1917592, false);
        int n18 = n2 + 164;
        int n19 = this.ingredientPage * n7;
        for (int i = 0; i < n7; ++i) {
            int n20 = n + i % 9 * 18;
            int n21 = n18 + i / 9 * 18;
            int n22 = n19 + i;
            ItemStack itemStack2 = n22 < list2.size() && this.f_96541_ != null && this.f_96541_.f_91074_ != null ? this.f_96541_.f_91074_.m_150109_().m_8020_(list2.get(n22).intValue()) : ItemStack.f_41583_;
            this.drawSlot(guiGraphics, n20, n21, itemStack2, n4, n22 < list2.size() && list2.get(n22) == this.offeringSlot);
            if (!StatueHubScreen.inside(n5, n6, n20, n21, 18, 18) || itemStack2.m_41619_()) continue;
            this.hoverTooltip = this.ingredientTooltip(itemStack2);
        }
        ItemStack itemStack3 = this.inventoryItem(this.offeringSlot);
        MutableComponent mutableComponent2 = this.offeringSlot < 0 ? Component.m_237115_((String)"screen.ftboss.evolution.select_offering") : itemStack3.m_41786_();
        this.wrapped(guiGraphics, (Component)mutableComponent2, n + 172, n2 + 158, n3 - 172, -1515055, 11, 1);
        if (this.offeringSlot >= 0) {
            this.wrapped(guiGraphics, (Component)Component.m_237110_((String)"screen.ftboss.evolution.offering_chance", (Object[])new Object[]{FoodWeaponProgression.qualityName(itemStack3), FoodWeaponProgression.chance(itemStack, itemStack3)}), n + 172, n2 + 170, n3 - 172, -1917592, 11, 1);
            this.wrapped(guiGraphics, StatueWeaponDetailsPanel.offeringTooltip(itemStack3).get(0), n + 172, n2 + 181, n3 - 172, -3622936, 10, 1);
            if (StatueHubScreen.inside(n5, n6, n + 172, n2 + 158, n3 - 172, 34)) {
                this.hoverTooltip = this.ingredientTooltip(itemStack3);
            }
        }
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.evolution.failure_warning"), n + 172, n2 + 193, n3 - 172, -26000, 10, 2);
    }

    private void renderMaterialFilters(GuiGraphics guiGraphics, int n, int n2, int n3) {
        int n4 = n3 / MaterialFilter.values().length;
        for (MaterialFilter materialFilter : MaterialFilter.values()) {
            int n5 = n + materialFilter.ordinal() * n4;
            guiGraphics.m_280509_(n5, n2 + 127, n5 + n4 - 3, n2 + 127 + 15, this.materialFilter == materialFilter ? -800309721 : -936301001);
            guiGraphics.m_280653_(this.f_96547_, materialFilter.label(), n5 + (n4 - 3) / 2, n2 + 127 + 3, this.materialFilter == materialFilter ? -10374 : -1515055);
        }
    }

    private List<Component> materialTooltip(Component component, FoodWeaponBuildRules.Material material) {
        ArrayList<MutableComponent> arrayList = new ArrayList<MutableComponent>(List.of(component, WeaponMaterialBuild.materialName(material), WeaponMaterialCatalog.source(material), WeaponMaterialBuild.passive(material), Component.m_237115_((String)"tooltip.ftboss.material.unique"), Component.m_237115_((String)"tooltip.ftboss.material.trigger_scope"), Component.m_237115_((String)"screen.ftboss.material.select_hint"), Component.m_237115_((String)(WeaponMaterialCatalog.boss(material) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning"))));
        if (WeaponMaterialCatalog.group(material) == WeaponMaterialCatalogRules.Group.INGOT) {
            arrayList.add(Component.m_237115_((String)"screen.ftboss.material.ingot_rules"));
        }
        return List.copyOf(arrayList);
    }

    private boolean canReplaceMaterial(ItemStack itemStack, FoodWeaponBuildRules.Build build) {
        if (WeaponAttachmentPolicy.excluded(itemStack)) {
            return false;
        }
        int n = itemStack.m_41776_();
        int n2 = WeaponMaterialBuild.previewDurability(itemStack, build);
        return n <= 0 || FoodWeaponBuildRules.preserveDamage(itemStack.m_41773_(), n, n2) < n2;
    }

    private void renderBuildStats(GuiGraphics guiGraphics, int n, int n2, int n3, WeaponMaterialBuild.CombatStats combatStats, String string) {
        guiGraphics.m_280509_(n, n2, n + n3, n2 + 60, -936301001);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)string), n + 6, n2 + 4, -1917592, false);
        List<Component> list = this.combatLines(combatStats);
        for (int i = 0; i < list.size(); ++i) {
            this.wrapped(guiGraphics, list.get(i), n + 6, n2 + 16 + i * 11, n3 - 12, i == list.size() - 1 ? -6449784 : -1515055, 10, 1);
        }
    }

    private List<Component> combatLines(WeaponMaterialBuild.CombatStats combatStats) {
        return List.of(Component.m_237110_((String)"screen.ftboss.material.combat", (Object[])new Object[]{String.format(Locale.ROOT, "%.1f", combatStats.damage()), String.format(Locale.ROOT, "%.2f", combatStats.speed())}), Component.m_237110_((String)"screen.ftboss.material.durability_wear", (Object[])new Object[]{combatStats.durability(), Math.round(combatStats.wear() * 100.0)}), Component.m_237110_((String)"screen.ftboss.material.reach", (Object[])new Object[]{String.format(Locale.ROOT, "%.2f", combatStats.reach())}), Component.m_237115_((String)"screen.ftboss.material.weapon_only"));
    }

    private ItemStack inventoryItem(int n) {
        return this.f_96541_ == null || this.f_96541_.f_91074_ == null || n < 0 || n >= this.f_96541_.f_91074_.m_150109_().m_6643_() ? ItemStack.f_41583_ : this.f_96541_.f_91074_.m_150109_().m_8020_(n);
    }

    private void selectInventorySlot(ItemStack itemStack, int n, boolean bl) {
        this.selectionWeapon = itemStack.m_41777_();
        this.selectionFood = this.inventoryItem(n).m_41777_();
        this.offeringSlot = bl ? -1 : n;
        this.materialSlot = bl ? n : -1;
        this.updateActionButtons();
    }

    private List<Integer> materialSlots(ItemStack itemStack) {
        int n;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (n = 0; n < 36; ++n) {
            ItemStack itemStack2 = this.inventoryItem(n);
            FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(itemStack2);
            if (itemStack2 == itemStack || material == FoodWeaponBuildRules.Material.ORIGINAL || !this.materialFilter.accepts(material)) continue;
            arrayList.add(n);
        }
        n = 18;
        this.materialPage = Math.max(0, Math.min(this.materialPage, Math.max(0, (arrayList.size() - 1) / n)));
        return arrayList;
    }

    private List<FoodWeaponEvolution.WeaponDetailView> weaponDetails(ItemStack itemStack) {
        ArrayList<FoodWeaponEvolution.WeaponDetailView> arrayList = new ArrayList<FoodWeaponEvolution.WeaponDetailView>(FoodWeaponEvolution.weaponDetailViews(itemStack));
        FoodWeaponEvolution.WeaponDetailView weaponDetailView = FoodWeaponBossPassive.detail(itemStack);
        if (weaponDetailView != null) {
            arrayList.add(weaponDetailView);
        }
        return List.copyOf(arrayList);
    }

    private FoodWeaponEvolution.WeaponDetailView skillAtMouse(double d, double d2) {
        List<FoodWeaponEvolution.WeaponDetailView> list = this.weaponDetails(this.evolutionWeapon());
        for (int i = 0; i < 3; ++i) {
            int n = this.detailPage * 3 + i;
            if (n >= list.size() || !StatueHubScreen.inside(d, d2, this.contentX(), this.contentY() + 96 + i * 38, this.contentWidth(), 34)) continue;
            return list.get(n);
        }
        return null;
    }

    private void renderWeaponDetails(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        ItemStack itemStack = this.evolutionWeapon();
        StatueWeaponDetailsPanel.Result result = StatueWeaponDetailsPanel.render(guiGraphics, this.f_96547_, n, n2, n3, n4, n5, n6, itemStack, this.weaponDetails(itemStack), this.detailPage);
        this.detailPage = result.page();
        this.hoverTooltip = result.tooltip();
    }

    private List<Component> ingredientTooltip(ItemStack itemStack) {
        FoodWeaponEvolution.SocketView socketView = FoodWeaponEvolution.ingredientView(itemStack);
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(itemStack.m_41786_());
        if (itemStack != this.inventoryItem(40) && FoodWeaponProgression.isOffering(this.evolutionWeapon(), itemStack, this.tint)) {
            arrayList.add(Component.m_237110_((String)"screen.ftboss.evolution.offering_chance", (Object[])new Object[]{FoodWeaponProgression.qualityName(itemStack), FoodWeaponProgression.chance(this.evolutionWeapon(), itemStack)}));
            arrayList.add(Component.m_237115_((String)"screen.ftboss.evolution.select_offering"));
            arrayList.addAll(StatueWeaponDetailsPanel.offeringTooltip(itemStack));
            arrayList.add(Component.m_237115_((String)"screen.ftboss.evolution.failure_warning"));
        }
        if (socketView != null) {
            arrayList.add(Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.socket_type", (Object[])new Object[]{socketView.type(), socketView.tier()}));
            arrayList.add(socketView.effect());
            arrayList.add(Component.m_237110_((String)"screen.ftboss.statue_hub.evolution.synergy", (Object[])new Object[]{socketView.synergy()}));
            arrayList.add(Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.insert_click"));
        }
        return List.copyOf(arrayList);
    }

    private List<Integer> candidateSlots(ItemStack itemStack, boolean bl) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null) {
            return arrayList;
        }
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack2 = this.f_96541_.f_91074_.m_150109_().m_8020_(i);
            if (itemStack2 == itemStack || !(bl ? FoodWeaponEvolution.isBlessingCandidate(itemStack, itemStack2, this.tint) : FoodWeaponEvolution.isSocketCandidate(itemStack, itemStack2, this.tint) || FoodWeaponProgression.isOffering(itemStack, itemStack2, this.tint))) continue;
            arrayList.add(i);
        }
        ItemStack itemStack3 = this.f_96541_.f_91074_.m_150109_().m_8020_(40);
        if (itemStack3 != itemStack && (bl ? FoodWeaponEvolution.isBlessingCandidate(itemStack, itemStack3, this.tint) : FoodWeaponEvolution.isSocketCandidate(itemStack, itemStack3, this.tint))) {
            arrayList.add(40);
        }
        return arrayList;
    }

    public boolean m_6050_(double d, double d2, double d3) {
        List<FoodWeaponEvolution.WeaponDetailView> list;
        List<Integer> list2;
        List<Integer> list3;
        int n;
        if (this.section == Section.EVOLUTION && this.repairPanel.isShowing()) {
            return super.m_6050_(d, d2, d3);
        }
        if (this.section == Section.MATERIAL) {
            int n2 = 18;
            int n3 = Math.max(1, (this.materialSlots(this.evolutionWeapon()).size() + n2 - 1) / n2);
            this.materialPage = Math.max(0, Math.min(n3 - 1, this.materialPage + (d3 < 0.0 ? 1 : -1)));
            return true;
        }
        if (this.section == Section.DAILY) {
            int n4 = this.contentX();
            n = this.contentY();
            int n5 = this.dailyTaskPages();
            if (n5 > 1 && StatueHubScreen.inside(d, d2, n4, n + 15, this.contentWidth(), 125)) {
                this.dailyTaskPage = Math.max(0, Math.min(n5 - 1, this.dailyTaskPage + (d3 < 0.0 ? 1 : -1)));
                return true;
            }
            int n6 = Math.max(1, (this.dailyFoodSlots().size() + 18 - 1) / 18);
            if (n6 > 1) {
                this.dailyPage = Math.max(0, Math.min(n6 - 1, this.dailyPage + (d3 < 0.0 ? 1 : -1)));
                return true;
            }
        }
        if (this.section == Section.ENCHANT && (n = Math.max(1, ((list3 = this.blessingSlots(this.evolutionWeapon())).size() + 18 - 1) / 18)) > 1) {
            this.ingredientPage = Math.max(0, Math.min(n - 1, this.ingredientPage + (d3 < 0.0 ? 1 : -1)));
            return true;
        }
        if (this.section == Section.EVOLUTION && (n = Math.max(1, ((list2 = this.ingredientSlots(this.evolutionWeapon())).size() + 18 - 1) / 18)) > 1) {
            this.ingredientPage = Math.max(0, Math.min(n - 1, this.ingredientPage + (d3 < 0.0 ? 1 : -1)));
            return true;
        }
        if (this.section == Section.DETAILS && (n = Math.max(1, ((list = this.weaponDetails(this.evolutionWeapon())).size() + 3 - 1) / 3)) > 1) {
            this.detailPage = Math.max(0, Math.min(n - 1, this.detailPage + (d3 < 0.0 ? 1 : -1)));
            return true;
        }
        return super.m_6050_(d, d2, d3);
    }

    private void heading(GuiGraphics guiGraphics, int n, int n2, String string, int n3) {
        StatueWeaponUiDraw.heading(guiGraphics, this.f_96547_, n, n2, string, n3);
    }

    private int wrapped(GuiGraphics guiGraphics, Component component, int n, int n2, int n3, int n4, int n5, int n6) {
        return StatueWeaponUiDraw.text(guiGraphics, this.f_96547_, component, n, n2, n3, n4, n5, n6);
    }

    private void wrappedCentered(GuiGraphics guiGraphics, Component component, int n, int n2, int n3, int n4, int n5, int n6) {
        StatueWeaponUiDraw.centered(guiGraphics, this.f_96547_, component, n, n2, n3, n4, n5, n6);
    }

    private StatueHubScreen(StatueMenuNetwork.Open open) {
        super((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.title"));
        this.selectionWeapon = ItemStack.f_41583_;
        this.selectionFood = ItemStack.f_41583_;
        this.repairPanel = new StatueWeaponRepairPanel(this::evolutionWeapon, () -> this.tint, this::sendRepair, this::updateActionButtons);
        this.hoverTooltip = List.of();
        this.statuePos = open.pos();
        this.apply(open);
    }

    private void apply(StatueMenuNetwork.Open open) {
        this.tint = open.tint();
        this.white = open.white();
        this.gold = open.gold();
        this.finaleUnlocked = open.finaleUnlocked();
        this.finaleSelected = open.finaleSelected();
        this.statueName = open.statueName();
        this.dailyTasks = List.copyOf(open.dailyTasks());
        this.dailyTaskPage = Math.min(this.dailyTaskPage, this.dailyTaskPages() - 1);
        this.updateActionButtons();
    }

    public static void acknowledgeAction(BlockPos blockPos) {
        Screen screen;
        Screen screen2 = Minecraft.m_91087_().f_91080_;
        if (screen2 instanceof StatueSwordCavityScreen && (screen = (StatueSwordCavityScreen)screen2).matches(blockPos)) {
            screen.acknowledgeAction();
            return;
        }
        if (screen2 instanceof StatueHubScreen) {
            screen = (StatueHubScreen)screen2;
            if (screen.statuePos.equals((Object)blockPos)) {
                screen.actionPending = false;
                screen.updateActionButtons();
                screen.updateColorButton();
            }
        }
    }

    public void m_7379_() {
        if (!this.actionPending) {
            super.m_7379_();
        }
    }

    private void updateActionButtons() {
        boolean bl;
        if (this.evolveButton == null || this.claimButton == null) {
            return;
        }
        boolean bl2 = this.section == Section.EVOLUTION && !this.repairPanel.isShowing();
        ItemStack itemStack = this.evolutionWeapon();
        boolean bl3 = !StatueWeaponRepairPanel.blocksChanges(itemStack);
        this.repairPanel.update(this.section == Section.EVOLUTION, this.actionPending);
        if (!(this.offeringSlot < 0 && this.materialSlot < 0 || ItemStack.m_150942_((ItemStack)itemStack, (ItemStack)this.selectionWeapon) && ItemStack.m_150942_((ItemStack)this.inventoryItem(this.materialSlot >= 0 ? this.materialSlot : this.offeringSlot), (ItemStack)this.selectionFood))) {
            this.offeringSlot = -1;
            this.materialSlot = -1;
        }
        boolean bl4 = bl = !itemStack.m_41619_();
        if (this.bindButton != null) {
            boolean bl5 = FoodWeaponSkillLoadout.canBind(itemStack);
            this.bindButton.f_93624_ = bl2 && bl;
            this.bindButton.f_93623_ = this.bindButton.f_93624_ && bl5 && !this.actionPending;
            this.bindButton.m_93666_((Component)Component.m_237115_((String)(bl5 ? "screen.ftboss.evolution.bind" : "screen.ftboss.evolution.bound")));
        }
        this.claimButton.f_93624_ = bl2;
        this.claimButton.f_93623_ = bl2 && !this.actionPending && bl && bl3;
        this.evolveButton.f_93624_ = bl2;
        boolean bl6 = this.evolveButton.f_93623_ = bl2 && !this.actionPending && bl && bl3 && FoodWeaponEvolution.evolutionLevel(itemStack) < FoodWeaponEvolution.maxEvolutionLevel(itemStack) && FoodWeaponEvolution.evolutionExperience(itemStack) >= FoodWeaponEvolution.nextEvolutionRequirement(itemStack) && this.offeringSlot >= 0 && this.offeringSlot < 36 && FoodWeaponProgression.isOffering(itemStack, this.inventoryItem(this.offeringSlot), this.tint);
        if (this.replaceButton != null) {
            this.replaceButton.f_93624_ = this.section == Section.MATERIAL && !FourWeaponCavityPresentation.usesCavity(itemStack);
            FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(this.inventoryItem(this.materialSlot));
            boolean bl7 = WeaponMaterialCatalog.boss(material);
            this.replaceButton.m_93666_((Component)Component.m_237115_((String)(bl7 ? "screen.ftboss.material.confirm_boss" : "screen.ftboss.material.confirm")));
            this.replaceButton.m_257544_(Tooltip.m_257550_((Component)Component.m_237115_((String)(bl7 ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning"))));
            boolean bl8 = this.replaceButton.f_93623_ = this.replaceButton.f_93624_ && !this.actionPending && bl && bl3 && this.materialSlot >= 0 && this.materialSlot < 36 && material != FoodWeaponBuildRules.Material.ORIGINAL && WeaponMaterialBuild.materials(itemStack).get(this.materialPart.ordinal()) != material && this.canReplaceMaterial(itemStack, WeaponMaterialBuild.preview(itemStack, this.materialPart, material));
        }
        if (this.colorButton != null) {
            this.colorButton.f_93623_ = !this.actionPending;
        }
    }

    public void m_86600_() {
        super.m_86600_();
        if (this.section == Section.MATERIAL && !this.actionPending && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
            this.openSwordCavity();
            return;
        }
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null || this.f_96541_.f_91073_ == null) {
            return;
        }
        if (this.f_96541_.f_91074_.m_20275_((double)this.statuePos.m_123341_() + 0.5, (double)this.statuePos.m_123342_() + 1.0, (double)this.statuePos.m_123343_() + 0.5) > 64.0 || !(this.f_96541_.f_91073_.m_8055_(this.statuePos).m_60734_() instanceof CandyStatueBlock)) {
            this.m_7379_();
        }
    }

    private void renderMaterials(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        String string;
        int n7;
        if (this.renderAttachmentExemption(guiGraphics, n, n2, n3)) {
            return;
        }
        this.heading(guiGraphics, n, n2, "screen.ftboss.material.title", n4);
        ItemStack itemStack = this.evolutionWeapon();
        if (FourWeaponCavityPresentation.usesCavity(itemStack)) {
            return;
        }
        if (itemStack.m_41619_()) {
            this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.evolution.no_weapon"), n + 10, n2 + 36, n3 - 20, -6449784, 12, 4);
            return;
        }
        this.drawSlot(guiGraphics, n, n2 + 16, itemStack, n4, false);
        this.wrapped(guiGraphics, itemStack.m_41786_(), n + 24, n2 + 21, n3 - 24, -1515055, 11, 1);
        FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(this.inventoryItem(this.materialSlot));
        FoodWeaponBuildRules.Build build = material == FoodWeaponBuildRules.Material.ORIGINAL ? WeaponMaterialBuild.build(itemStack) : WeaponMaterialBuild.preview(itemStack, this.materialPart, material);
        int n8 = n3 / 2;
        WeaponMaterialBuild.CombatStats combatStats = WeaponMaterialBuild.combatStats(itemStack);
        WeaponMaterialBuild.CombatStats combatStats2 = material == FoodWeaponBuildRules.Material.ORIGINAL ? combatStats : WeaponMaterialBuild.combatPreview(itemStack, this.materialPart, material);
        this.renderBuildStats(guiGraphics, n, n2 + 37, n8 - 3, combatStats, "screen.ftboss.material.current");
        this.renderBuildStats(guiGraphics, n + n8 + 3, n2 + 37, n8 - 3, combatStats2, "screen.ftboss.material.preview");
        if (StatueHubScreen.inside(n5, n6, n, n2 + 37, n8 - 3, 60)) {
            this.hoverTooltip = this.combatLines(combatStats);
        }
        if (StatueHubScreen.inside(n5, n6, n + n8 + 3, n2 + 37, n8 - 3, 60)) {
            this.hoverTooltip = this.combatLines(combatStats2);
        }
        List<FoodWeaponBuildRules.Material> list = WeaponMaterialBuild.materials(itemStack);
        int n9 = n3 / FoodWeaponBuildRules.Part.values().length;
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            n7 = n + part.ordinal() * n9;
            FoodWeaponBuildRules.Material material2 = list.get(part.ordinal());
            boolean bl = part == this.materialPart;
            guiGraphics.m_280509_(n7, n2 + 101, n7 + n9 - 3, n2 + 101 + 24, bl ? -800309721 : -936301001);
            this.wrapped(guiGraphics, (Component)Component.m_237110_((String)"screen.ftboss.material.part", (Object[])new Object[]{WeaponMaterialBuild.partName(part), WeaponMaterialBuild.materialName(material2)}), n7 + 5, n2 + 101 + 3, n9 - 13, bl ? -10374 : -1515055, 10, 2);
            if (!StatueHubScreen.inside(n5, n6, n7, n2 + 101, n9 - 3, 24)) continue;
            this.hoverTooltip = this.materialTooltip(WeaponMaterialBuild.partName(part), material2);
        }
        this.renderMaterialFilters(guiGraphics, n, n2, n3);
        List<Integer> list2 = this.materialSlots(itemStack);
        int n10 = 18;
        int n11 = Math.max(1, (list2.size() + n10 - 1) / n10);
        this.wrapped(guiGraphics, (Component)Component.m_237110_((String)"screen.ftboss.material.inventory", (Object[])new Object[]{list2.size(), this.materialPage + 1, n11}), n, n2 + 145, n3 - 36, -1917592, 10, 1);
        guiGraphics.m_280137_(this.f_96547_, "<", n + n3 - 27, n2 + 145, this.materialPage > 0 ? -1515055 : -6449784);
        guiGraphics.m_280137_(this.f_96547_, ">", n + n3 - 7, n2 + 145, this.materialPage + 1 < n11 ? -1515055 : -6449784);
        for (int i = 0; i < n10; ++i) {
            n7 = this.materialPage * n10 + i;
            int n12 = n7 < list2.size() ? (Integer)list2.get(n7) : -1;
            ItemStack itemStack2 = this.inventoryItem(n12);
            int n13 = n + i % 9 * 18;
            int n14 = n2 + 158 + i / 9 * 18;
            this.drawSlot(guiGraphics, n13, n14, itemStack2, n4, n12 >= 0 && n12 == this.materialSlot);
            FoodWeaponBuildRules.Material material3 = WeaponMaterialBuild.materialFor(itemStack2);
            if (WeaponMaterialCatalog.boss(material3)) {
                guiGraphics.m_280056_(this.f_96547_, "!", n13 + 12, n14 + 1, -12470, true);
            }
            if (n12 < 0 || !StatueHubScreen.inside(n5, n6, n13, n14, 18, 18)) continue;
            this.hoverTooltip = this.materialTooltip(itemStack2.m_41786_(), material3);
        }
        MutableComponent mutableComponent = material == FoodWeaponBuildRules.Material.ORIGINAL ? (list2.isEmpty() ? Component.m_237110_((String)"screen.ftboss.material.filter_empty", (Object[])new Object[]{this.materialFilter.label()}) : Component.m_237115_((String)"screen.ftboss.material.select_hint")) : Component.m_237110_((String)(WeaponMaterialCatalog.boss(material) ? "screen.ftboss.material.boss_candidate" : "screen.ftboss.material.candidate"), (Object[])new Object[]{WeaponMaterialBuild.materialName(material)}).m_7220_((Component)Component.m_237113_((String)"\n")).m_7220_(WeaponMaterialBuild.passive(material));
        this.wrapped(guiGraphics, (Component)mutableComponent, n + 172, n2 + 158, n3 - 172, WeaponMaterialCatalog.boss(material) ? -12470 : -1515055, 11, 3);
        if (StatueHubScreen.inside(n5, n6, n + 172, n2 + 158, n3 - 172, 36)) {
            this.hoverTooltip = material == FoodWeaponBuildRules.Material.ORIGINAL ? List.of(mutableComponent) : this.materialTooltip(this.inventoryItem(this.materialSlot).m_41786_(), material);
        }
        String string2 = string = WeaponMaterialCatalog.boss(material) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning";
        String string3 = material != FoodWeaponBuildRules.Material.ORIGINAL && !this.canReplaceMaterial(itemStack, WeaponMaterialBuild.preview(itemStack, this.materialPart, material)) ? "message.ftboss.material.repair_first" : (WeaponMaterialCatalog.boss(material) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning");
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)string3), n, n2 + 195, n3, string3.equals("message.ftboss.material.repair_first") ? -38037 : -26000, 10, 2);
        if (StatueHubScreen.inside(n5, n6, n, n2 + 195, n3, 19)) {
            this.hoverTooltip = List.of(Component.m_237115_((String)string3), Component.m_237115_((String)string), Component.m_237115_((String)"tooltip.ftboss.material.unique"));
        }
    }

    public boolean m_6375_(double d, double d2, int n) {
        int n2;
        Object object;
        int n3;
        FoodWeaponEvolution.WeaponDetailView weaponDetailView;
        this.updateActionButtons();
        if (this.section == Section.MATERIAL && FourWeaponCavityPresentation.usesCavity(this.evolutionWeapon())) {
            if (!this.actionPending) {
                this.openSwordCavity();
            }
            return true;
        }
        if (this.section == Section.EVOLUTION && (this.repairPanel.isShowing() || StatueWeaponRepairPanel.blocksChanges(this.evolutionWeapon()))) {
            return super.m_6375_(d, d2, n);
        }
        if (this.section == Section.DETAILS && !this.actionPending && (n == 1 || n == 0 && StatueHubScreen.m_96638_()) && (weaponDetailView = this.skillAtMouse(d, d2)) != null && weaponDetailView.selectable()) {
            this.actionPending = true;
            this.updateActionButtons();
            StatueMenuNetwork.Action.send(this.statuePos, 12, weaponDetailView.gourmetSkillIndex());
            return true;
        }
        if (this.section == Section.EVOLUTION && !this.actionPending && (n == 1 || n == 0 && StatueHubScreen.m_96638_())) {
            weaponDetailView = this.evolutionWeapon();
            n3 = StatueHubScreen.gridIndex(d, d2, this.contentX(), this.contentY() + 164, 9, 2);
            object = this.ingredientSlots((ItemStack)weaponDetailView);
            n2 = this.ingredientPage * 9 * 2 + n3;
            if (!weaponDetailView.m_41619_() && n3 >= 0 && n2 < object.size() && FoodWeaponEvolution.isSocketCandidate((ItemStack)weaponDetailView, this.inventoryItem((Integer)object.get(n2)), this.tint)) {
                this.actionPending = true;
                this.updateActionButtons();
                StatueMenuNetwork.Action.send(this.statuePos, 2, (Integer)object.get(n2));
                return true;
            }
        }
        if (n == 0 && !this.actionPending) {
            ItemStack itemStack;
            ItemStack itemStack2;
            int n4;
            int n5;
            int n6;
            if (this.section == Section.MATERIAL) {
                weaponDetailView = this.evolutionWeapon();
                n3 = this.contentWidth() / MaterialFilter.values().length;
                for (MaterialFilter materialFilter : MaterialFilter.values()) {
                    if (weaponDetailView.m_41619_() || !StatueHubScreen.inside(d, d2, this.contentX() + materialFilter.ordinal() * n3, this.contentY() + 127, n3 - 3, 15)) continue;
                    this.materialFilter = materialFilter;
                    this.materialPage = 0;
                    this.materialSlot = -1;
                    this.updateActionButtons();
                    return true;
                }
                object = this.materialSlots((ItemStack)weaponDetailView);
                n2 = 18;
                n6 = Math.max(1, (object.size() + n2 - 1) / n2);
                if (StatueHubScreen.inside(d, d2, this.contentX() + this.contentWidth() - 34, this.contentY() + 145 - 1, 14, 12) && this.materialPage > 0) {
                    --this.materialPage;
                    return true;
                }
                if (StatueHubScreen.inside(d, d2, this.contentX() + this.contentWidth() - 14, this.contentY() + 145 - 1, 14, 12) && this.materialPage + 1 < n6) {
                    ++this.materialPage;
                    return true;
                }
                n5 = this.contentWidth() / FoodWeaponBuildRules.Part.values().length;
                for (FoodWeaponBuildRules.Part part : FourWeaponCavityPresentation.usesCavity((ItemStack)weaponDetailView) ? new FoodWeaponBuildRules.Part[]{} : FoodWeaponBuildRules.Part.values()) {
                    if (!StatueHubScreen.inside(d, d2, this.contentX() + part.ordinal() * n5, this.contentY() + 101, n5 - 3, 24)) continue;
                    this.materialPart = part;
                    this.updateActionButtons();
                    return true;
                }
                n4 = StatueHubScreen.gridIndex(d, d2, this.contentX(), this.contentY() + 158, 9, 2);
                int n7 = this.materialPage * 9 * 2 + n4;
                if (!weaponDetailView.m_41619_() && n4 >= 0 && n7 < object.size()) {
                    this.selectInventorySlot((ItemStack)weaponDetailView, (Integer)object.get(n7), true);
                    return true;
                }
            }
            if (this.section == Section.CHALLENGE) {
                int n8;
                int n9 = this.contentX();
                int n10 = StatueHubScreen.inside(d, d2, n9, (n3 = this.contentY()) + 23, this.contentWidth(), 50) ? 0 : (n8 = StatueHubScreen.inside(d, d2, n9, n3 + 79, this.contentWidth(), 61) ? 1 : -1);
                if (n8 >= 0) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos, 8, n8);
                    return true;
                }
            }
            if (this.section == Section.DETAILS) {
                FoodWeaponEvolution.WeaponDetailView weaponDetailView2;
                List<FoodWeaponEvolution.WeaponDetailView> list = this.weaponDetails(this.evolutionWeapon());
                n3 = Math.max(1, (list.size() + 3 - 1) / 3);
                int n11 = this.contentX();
                n2 = this.contentY() + 77;
                if (StatueHubScreen.inside(d, d2, n11 + this.contentWidth() - 96, n2, 18, 13) && this.detailPage > 0) {
                    --this.detailPage;
                    return true;
                }
                if (StatueHubScreen.inside(d, d2, n11 + this.contentWidth() - 18, n2, 18, 13) && this.detailPage + 1 < n3) {
                    ++this.detailPage;
                    return true;
                }
                n6 = this.contentY() + 96;
                if (StatueHubScreen.inside(d, d2, n11, n6, this.contentWidth(), 114) && (n4 = this.detailPage * 3 + (n5 = ((int)d2 - n6) / 38)) >= 0 && n4 < list.size() && (weaponDetailView2 = list.get(n4)).selectable()) {
                    if (!weaponDetailView2.selected()) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        StatueMenuNetwork.Action.send(this.statuePos, 9, weaponDetailView2.gourmetSkillIndex());
                    }
                    return true;
                }
            }
            if (this.section == Section.DAILY) {
                int n12 = this.contentX();
                n3 = this.contentY();
                if (StatueHubScreen.inside(d, d2, n12 + this.contentWidth() - 96, n3 - 2, 18, 13) && this.dailyTaskPage > 0) {
                    --this.dailyTaskPage;
                    return true;
                }
                if (StatueHubScreen.inside(d, d2, n12 + this.contentWidth() - 18, n3 - 2, 18, 13) && this.dailyTaskPage + 1 < this.dailyTaskPages()) {
                    ++this.dailyTaskPage;
                    return true;
                }
                List<Integer> list = this.dailyFoodSlots();
                n2 = StatueHubScreen.gridIndex(d, d2, this.contentX(), this.contentY() + 170, 9, 2);
                n6 = this.dailyPage * 9 * 2 + n2;
                if (n2 >= 0 && n6 >= 0 && n6 < list.size()) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos, 4, list.get(n6));
                    return true;
                }
            }
            if (this.section == Section.ENCHANT && !(itemStack2 = this.evolutionWeapon()).m_41619_()) {
                n3 = StatueHubScreen.gridIndex(d, d2, this.contentX(), this.contentY() + 170, 9, 2);
                List<Integer> list = this.blessingSlots(itemStack2);
                n2 = this.ingredientPage * 9 * 2 + n3;
                if (n3 >= 0 && n2 >= 0 && n2 < list.size()) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos, 7, list.get(n2));
                    return true;
                }
            }
            if (this.section == Section.EVOLUTION && !(itemStack = this.evolutionWeapon()).m_41619_()) {
                n3 = this.contentX();
                int n13 = this.contentY();
                n2 = StatueHubScreen.gridIndex(d, d2, n3, n13 + 90, 10, 3);
                n6 = FoodWeaponEvolution.socketCount(itemStack);
                if (n2 >= 0 && n2 < n6) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos, 3, n2);
                    return true;
                }
                n5 = StatueHubScreen.gridIndex(d, d2, n3, n13 + 164, 9, 2);
                List<Integer> list = this.ingredientSlots(itemStack);
                int n14 = this.ingredientPage * 9 * 2 + n5;
                if (n5 >= 0 && n14 >= 0 && n14 < list.size()) {
                    int n15 = list.get(n14);
                    if (n15 < 36 && FoodWeaponProgression.isOffering(itemStack, this.inventoryItem(n15), this.tint)) {
                        this.selectInventorySlot(itemStack, n15, false);
                    }
                    return true;
                }
            }
        }
        return super.m_6375_(d, d2, n);
    }

    private void openSwordCavity() {
        if (this.actionPending) {
            return;
        }
        ItemStack itemStack = this.evolutionWeapon();
        if (!FourWeaponCavityPresentation.usesCavity(itemStack)) {
            this.section = Section.MATERIAL;
            this.updateActionButtons();
            return;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack) ? !FourWeaponClientApi.openContainer(itemStack, this.statuePos) : !StatueMenuNetwork.CavityOpenAction.send(this.statuePos, itemStack)) {
            return;
        }
        this.section = Section.DAILY;
        this.materialSlot = -1;
        this.offeringSlot = -1;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void open(StatueMenuNetwork.Open open) {
        Object object;
        Minecraft minecraft = Minecraft.m_91087_();
        Screen screen = minecraft.f_91080_;
        if (screen instanceof SwordCavityContainerScreen && ((SwordCavityMenu)(object = (SwordCavityContainerScreen)screen).m_6262_()).matches(open.pos())) {
            return;
        }
        Screen screen2 = minecraft.f_91080_;
        if (screen2 instanceof StatueSwordCavityScreen && ((StatueSwordCavityScreen)((Object)(object = (StatueSwordCavityScreen)screen2))).matches(open.pos())) {
            ((StatueSwordCavityScreen)((Object)object)).parent().apply(open);
            return;
        }
        screen2 = minecraft.f_91080_;
        if (screen2 instanceof StatueHubScreen) {
            screen = (StatueHubScreen)screen2;
            if (screen.statuePos.equals((Object)open.pos())) {
                screen.apply(open);
                return;
            }
        }
        minecraft.m_91152_((Screen)new StatueHubScreen(open));
    }

    private void addBuildInspectorEntry() {
        if (this.tint != 0) {
            return;
        }
        this.m_142416_((GuiEventListener)StatueWeaponRepairNavigation.inspectorEntry(this, this.statuePos, this::evolutionWeapon, this::openBuildAdjustment, this::canOpenBuildInspector, this.left + 11, this.top + 42 + 8 + Section.values().length * 30, 94));
    }

    private boolean canOpenBuildInspector() {
        return !this.actionPending;
    }

    private void openBuildAdjustment() {
        this.openSwordCavity();
    }

    private boolean renderAttachmentExemption(GuiGraphics guiGraphics, int n, int n2, int n3) {
        if (!WeaponAttachmentPolicy.excluded(this.evolutionWeapon())) {
            return false;
        }
        if (this.replaceButton != null) {
            this.replaceButton.f_93624_ = false;
        }
        this.wrapped(guiGraphics, (Component)Component.m_237115_((String)"screen.ftboss.material.not_required"), n + 10, n2 + 36, n3 - 20, -1515055, 12, 4);
        return true;
    }

    private void addWeaponRepairEntry() {
        this.m_142416_((GuiEventListener)StatueWeaponRepairNavigation.hubEntry(this, this.left + 11, this.top + 42 + 8 + Section.values().length * 30, 94));
        StatueWeaponRepairNavigation.openPending(this, this.statuePos, this.evolutionWeapon());
    }

    void openWeaponRepair() {
        this.section = Section.EVOLUTION;
        this.offeringSlot = -1;
        this.materialSlot = -1;
        this.repairPanel.open();
        this.updateActionButtons();
    }

    private static enum Section {
        DAILY("screen.ftboss.statue_hub.tab.daily"),
        CHALLENGE("screen.ftboss.statue_hub.tab.challenge"),
        ENCHANT("screen.ftboss.statue_hub.tab.enchant"),
        EVOLUTION("screen.ftboss.statue_hub.tab.evolution"),
        MATERIAL("screen.ftboss.statue_hub.tab.material"),
        DETAILS("screen.ftboss.statue_hub.tab.details");

        final String key;

        private Section(String key) {
            this.key = key;
        }
    }

    private static enum MaterialFilter {
        ALL(null),
        BASE(WeaponMaterialCatalogRules.Group.BASE),
        INGOT(WeaponMaterialCatalogRules.Group.INGOT),
        BOSS(WeaponMaterialCatalogRules.Group.BOSS);

        final WeaponMaterialCatalogRules.Group group;

        private MaterialFilter(WeaponMaterialCatalogRules.Group group) {
            this.group = group;
        }

        Component label() {
            return Component.m_237115_((String)("screen.ftboss.material.filter." + this.name().toLowerCase(Locale.ROOT)));
        }

        boolean accepts(FoodWeaponBuildRules.Material material) {
            return this.group == null || WeaponMaterialCatalog.group(material) == this.group;
        }
    }
}

