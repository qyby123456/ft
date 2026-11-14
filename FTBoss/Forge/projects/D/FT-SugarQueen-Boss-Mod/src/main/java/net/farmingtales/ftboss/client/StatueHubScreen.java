package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodBlessingView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.SocketView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.StatueDailyView;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild.Stats;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.item.WeaponMaterialBuild.CombatStats;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules.Group;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** 石像交互界面；2026-10-24 按 Boss 主题重绘六个分页，保持槽位坐标与服务端确认。 */
public final class StatueHubScreen extends Screen implements StatueThemeSource {
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
    private static final int DETAILS_PER_PAGE = 3;
    private static final int TAB_SPACING = 30;
    private static final int MAX_BLESSINGS = 6;
    private static final int EVOLUTION_SOCKET_Y = 90;
    private static final int EVOLUTION_FOOD_Y = 164;
    private static final int MATERIAL_PART_Y = 101;
    private static final int MATERIAL_PART_HEIGHT = 24;
    private static final int MATERIAL_FILTER_Y = 127;
    private static final int MATERIAL_FILTER_HEIGHT = 15;
    private static final int MATERIAL_INVENTORY_Y = 145;
    private static final int MATERIAL_FOOD_Y = 158;
    private static final int MATERIAL_WARNING_Y = 195;

    private final BlockPos statuePos;
    private int tint;
    private boolean finaleUnlocked;
    private boolean finaleSelected;
    private Component statueName;
    private List<StatueDailyView> dailyTasks;
    private boolean white;
    private boolean gold;
    private Section section = Section.DAILY;
    private int left;
    private int top;
    private StatueHubViewport viewport;
    private int ingredientPage;
    private int dailyPage;
    private int dailyTaskPage;
    private int detailPage;
    private int materialPage;
    private int offeringSlot = -1;
    private int materialSlot = -1;
    private Part materialPart = Part.EDGE;
    private MaterialFilter materialFilter = MaterialFilter.ALL;
    private ItemStack selectionWeapon = ItemStack.EMPTY;
    private ItemStack selectionFood = ItemStack.EMPTY;
    private boolean actionPending;
    private Button evolveButton;
    private Button claimButton;
    private Button colorButton;
    private Button replaceButton;
    private Button bindButton;
    private final StatueWeaponRepairPanel repairPanel = new StatueWeaponRepairPanel(this::evolutionWeapon, () -> this.tint,
            this::sendRepair, this::updateActionButtons);
    private List<Component> hoverTooltip = List.of();

    @Override
    public StatueUiTheme statueTheme() {
        return StatueUiTheme.forTint(this.tint);
    }

    private enum Section {
        DAILY("screen.ftboss.statue_hub.tab.daily"),
        CHALLENGE("screen.ftboss.statue_hub.tab.challenge"),
        ENCHANT("screen.ftboss.statue_hub.tab.enchant"),
        EVOLUTION("screen.ftboss.statue_hub.tab.evolution"),
        MATERIAL("screen.ftboss.statue_hub.tab.material"),
        DETAILS("screen.ftboss.statue_hub.tab.details");

        final String key;
        Section(String key) { this.key = key; }
    }

    private enum MaterialFilter {
        ALL(null), BASE(Group.BASE), INGOT(Group.INGOT), BOSS(Group.BOSS);

        final Group group;
        MaterialFilter(Group group) { this.group = group; }
        Component label() { return Component.translatable("screen.ftboss.material.filter." + name().toLowerCase(java.util.Locale.ROOT)); }
        boolean accepts(Material material) { return this.group == null || WeaponMaterialCatalog.group(material) == this.group; }
    }

    private StatueHubScreen(StatueMenuNetwork.Open packet) {
        super(Component.translatable("screen.ftboss.statue_hub.title"));
        this.statuePos = packet.pos();
        apply(packet);
    }

    private void openSwordCavity() {
        if (this.actionPending) return;
        ItemStack weapon = evolutionWeapon();
        if (!FourWeaponCavityPresentation.usesCavity(weapon)) {
            this.section = Section.MATERIAL;
            this.updateActionButtons();
            return;
        }
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            if (!FourWeaponClientApi.openContainer(weapon, this.statuePos)) return;
        } else if (!StatueMenuNetwork.CavityOpenAction.send(this.statuePos, weapon)) return;
        this.section = Section.DAILY;
        this.materialSlot = -1;
        this.offeringSlot = -1;
    }

    private void sendRepair(int action, int value) {
        ItemStack weapon = evolutionWeapon();
        if (this.actionPending || FoodWeaponLifecycle.weaponId(weapon) == null) return;
        this.actionPending = true;
        this.updateActionButtons();
        StatueMenuNetwork.RepairAction.send(this.statuePos, weapon, action, value);
    }

    private void apply(StatueMenuNetwork.Open packet) {
        this.tint = packet.tint();
        this.white = packet.white();
        this.gold = packet.gold();
        this.finaleUnlocked = packet.finaleUnlocked();
        this.finaleSelected = packet.finaleSelected();
        this.statueName = packet.statueName();
        this.dailyTasks = List.copyOf(packet.dailyTasks());
        this.dailyTaskPage = Math.min(this.dailyTaskPage, dailyTaskPages() - 1);
        this.updateActionButtons();
    }

    public static void open(StatueMenuNetwork.Open packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof SwordCavityContainerScreen cavity && cavity.getMenu().matches(packet.pos())) return;
        if (minecraft.screen instanceof StatueSwordCavityScreen cavity && cavity.matches(packet.pos())) {
            cavity.parent().apply(packet);
        } else if (minecraft.screen instanceof StatueHubScreen hub && hub.statuePos.equals(packet.pos())) {
            hub.apply(packet);
        } else {
            minecraft.setScreen(new StatueHubScreen(packet));
        }
    }

    public static void updateAppearance(BlockPos pos, boolean white, boolean gold) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof StatueHubScreen hub && hub.statuePos.equals(pos)) {
            hub.white = white;
            hub.gold = gold;
            hub.updateColorButton();
        }
    }

    public static void acknowledgeAction(BlockPos pos) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof StatueSwordCavityScreen cavity && cavity.matches(pos)) {
            cavity.acknowledgeAction();
            return;
        }
        if (screen instanceof StatueHubScreen hub && hub.statuePos.equals(pos)) {
            hub.actionPending = false;
            hub.updateActionButtons();
            hub.updateColorButton();
        }
    }

    @Override
    public void onClose() {
        if (!this.actionPending) super.onClose();
    }

    @Override
    protected void init() {
        this.viewport = new StatueHubViewport(this.width, this.height, PANEL_WIDTH, PANEL_HEIGHT);
        this.left = (this.width - PANEL_WIDTH) / 2;
        this.top = (this.height - PANEL_HEIGHT) / 2;
        int tabX = this.left + 11;
        int tabY = this.top + HEADER_HEIGHT + 8;
        int index = 0;
        for (Section value : Section.values()) {
            final Section target = value;
            this.addRenderableWidget(StatueThemeButton.create(Component.translatable(value.key), button -> {
                if (target == Section.MATERIAL && usesCavity(evolutionWeapon())) {
                    if (!this.actionPending) openSwordCavity();
                    return;
                }
                this.section = target;
                this.updateActionButtons();
            }, tabX, tabY + index * TAB_SPACING, SIDEBAR_WIDTH - 22, 25, this::statueTheme));
            index++;
        }
        this.colorButton = this.addRenderableWidget(StatueThemeButton.create(colorLabel(), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_TOGGLE_COLOR);
            this.actionPending = true;
            this.updateActionButtons();
        }, this.left + PANEL_WIDTH - 104, this.top + PANEL_HEIGHT - 29, 88, 20, this::statueTheme));
        this.addRenderableWidget(StatueThemeButton.create(CommonComponents.GUI_DONE, button -> this.onClose(),
                this.left + 12, this.top + PANEL_HEIGHT - 29, 72, 20, this::statueTheme));
        this.claimButton = this.addRenderableWidget(StatueThemeButton.create(
                Component.translatable("screen.ftboss.statue_hub.evolution.claim"), button -> {
                    StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_CLAIM_EVOLUTION);
                    this.actionPending = true;
                    this.updateActionButtons();
                }, this.left + 145, this.top + PANEL_HEIGHT - 29, 96, 20, this::statueTheme));
        this.evolveButton = this.addRenderableWidget(StatueThemeButton.create(
                Component.translatable("screen.ftboss.statue_hub.evolution.evolve"), button -> {
                    StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_EVOLVE_WEAPON, this.offeringSlot);
                    this.offeringSlot = -1;
                    this.actionPending = true;
                    this.updateActionButtons();
                }, this.left + 250, this.top + PANEL_HEIGHT - 29, 120, 20, this::statueTheme));
        this.evolveButton.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.level_warning")));
        this.replaceButton = this.addRenderableWidget(StatueThemeButton.create(
                Component.translatable("screen.ftboss.material.confirm"), button -> {
                    if (usesCavity(evolutionWeapon())) return;
                    StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_REPLACE_MATERIAL,
                            this.materialPart.ordinal() * 36 + this.materialSlot);
                    this.materialSlot = -1;
                    this.actionPending = true;
                    this.updateActionButtons();
                }, this.left + 145, this.top + PANEL_HEIGHT - 29, 225, 20, this::statueTheme));
        this.bindButton = this.addRenderableWidget(StatueThemeButton.create(Component.translatable("screen.ftboss.evolution.bind"), button -> {
            StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_BIND_WEAPON);
            this.actionPending = true;
            this.updateActionButtons();
        }, contentX() + contentWidth() - 88, contentY() + 24, 82, 20, this::statueTheme));
        this.bindButton.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.evolution.bind_hint")));
        this.repairPanel.init(button -> this.addRenderableWidget(button), contentX(), contentY(), contentWidth(),
                this.top + PANEL_HEIGHT - 29);
        this.updateActionButtons();
        this.addBuildInspectorEntry();
        this.addWeaponRepairEntry();
    }

    private void addWeaponRepairEntry() {
        this.addRenderableWidget(StatueWeaponRepairNavigation.hubEntry(this, this.left + 11,
                this.top + HEADER_HEIGHT + 8 + Section.values().length * TAB_SPACING, SIDEBAR_WIDTH - 22));
        StatueWeaponRepairNavigation.openPending(this, this.statuePos, this.evolutionWeapon());
    }

    void openWeaponRepair() {
        this.section = Section.EVOLUTION;
        this.offeringSlot = -1;
        this.materialSlot = -1;
        this.repairPanel.open();
        this.updateActionButtons();
    }

    private void addBuildInspectorEntry() {
        if (this.tint != 0 && !FoodWeaponFamilyBuild.isEquipment(buildItem())) return;
        this.addRenderableWidget(StatueWeaponRepairNavigation.inspectorEntry(this, this.statuePos, this::buildItem,
                this::openBuildAdjustment, this::canOpenBuildInspector, this.left + 11,
                this.top + HEADER_HEIGHT + 8 + Section.values().length * TAB_SPACING, SIDEBAR_WIDTH - 22));
    }

    private boolean canOpenBuildInspector() { return !this.actionPending; }

    private void openBuildAdjustment() {
        this.openSwordCavity();
    }

    private void updateActionButtons() {
        if (this.evolveButton == null || this.claimButton == null) return;
        boolean evolutionPage = this.section == Section.EVOLUTION && !this.repairPanel.isShowing();
        ItemStack weapon = this.evolutionWeapon();
        boolean usable = !StatueWeaponRepairPanel.blocksChanges(weapon);
        this.repairPanel.update(this.section == Section.EVOLUTION, this.actionPending);
        if ((this.offeringSlot >= 0 || this.materialSlot >= 0)
                && (!ItemStack.isSameItemSameTags(weapon, this.selectionWeapon)
                || !ItemStack.isSameItemSameTags(inventoryItem(this.materialSlot >= 0 ? this.materialSlot : this.offeringSlot),
                this.selectionFood))) {
            this.offeringSlot = -1;
            this.materialSlot = -1;
        }
        boolean hasWeapon = !weapon.isEmpty();
        if (this.bindButton != null) {
            boolean bindable = FoodWeaponSkillLoadout.canBind(weapon);
            this.bindButton.visible = evolutionPage && hasWeapon;
            this.bindButton.active = this.bindButton.visible && bindable && !this.actionPending;
            this.bindButton.setMessage(Component.translatable(bindable ? "screen.ftboss.evolution.bind" : "screen.ftboss.evolution.bound"));
        }
        this.claimButton.visible = evolutionPage;
        this.claimButton.active = evolutionPage && !this.actionPending && hasWeapon && usable;
        this.evolveButton.visible = evolutionPage;
        this.evolveButton.active = evolutionPage && !this.actionPending && hasWeapon && usable
                && FoodWeaponEvolution.evolutionLevel(weapon) < FoodWeaponEvolution.maxEvolutionLevel(weapon)
                && FoodWeaponEvolution.evolutionExperience(weapon) >= FoodWeaponEvolution.nextEvolutionRequirement(weapon)
                && this.offeringSlot >= 0 && this.offeringSlot < 36
                && FoodWeaponProgression.isOffering(weapon, inventoryItem(this.offeringSlot), this.tint);
        if (this.replaceButton != null) {
            this.replaceButton.visible = this.section == Section.MATERIAL && !usesCavity(weapon)
                    && !net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(weapon);
            Material candidate = WeaponMaterialBuild.materialFor(inventoryItem(this.materialSlot));
            boolean boss = WeaponMaterialCatalog.boss(candidate);
            this.replaceButton.setMessage(Component.translatable(boss ? "screen.ftboss.material.confirm_boss" : "screen.ftboss.material.confirm"));
            this.replaceButton.setTooltip(Tooltip.create(Component.translatable(boss
                    ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning")));
            this.replaceButton.active = this.replaceButton.visible && !this.actionPending && hasWeapon
                    && usable
                    && this.materialSlot >= 0 && this.materialSlot < 36 && candidate != Material.ORIGINAL
                    && WeaponMaterialBuild.materials(weapon).get(this.materialPart.ordinal()) != candidate
                    && canReplaceMaterial(weapon, WeaponMaterialBuild.preview(weapon, this.materialPart, candidate));
        }
        if (this.colorButton != null) this.colorButton.active = !this.actionPending;
    }

    private Component colorLabel() {
        if (this.gold) return Component.translatable("screen.ftboss.statue_hub.color.restore");
        if (this.white && this.finaleUnlocked) return Component.translatable("screen.ftboss.statue_hub.color.gold");
        return Component.translatable(this.white
                ? "screen.ftboss.statue_hub.color.restore"
                : "screen.ftboss.statue_hub.color.white");
    }

    private void updateColorButton() {
        if (this.colorButton != null) {
            this.colorButton.setMessage(colorLabel());
            this.colorButton.active = !this.actionPending;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.section == Section.MATERIAL && !this.actionPending && usesCavity(evolutionWeapon())) {
            openSwordCavity();
            return;
        }
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) return;
        if (this.minecraft.player.distanceToSqr(this.statuePos.getX() + 0.5,
                this.statuePos.getY() + 1.0, this.statuePos.getZ() + 0.5) > 64.0
                || !(this.minecraft.level.getBlockState(this.statuePos).getBlock()
                instanceof net.farmingtales.ftboss.block.CandyStatueBlock)) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        GuiGraphics screenGraphics = graphics;
        int screenMouseX = mouseX;
        int screenMouseY = mouseY;
        graphics = this.viewport.graphics(graphics);
        mouseX = (int) Math.floor(this.viewport.localX(mouseX));
        mouseY = (int) Math.floor(this.viewport.localY(mouseY));
        this.hoverTooltip = List.of();
        this.updateActionButtons();
        StatueUiTheme theme = statueTheme();
        int accent = theme.accent;
        StatueThemeRenderer.frame(graphics, this.left, this.top, PANEL_WIDTH, PANEL_HEIGHT, theme);
        StatueThemeRenderer.card(graphics, this.left + 7, this.top + HEADER_HEIGHT + 4,
                SIDEBAR_WIDTH - 6, PANEL_HEIGHT - HEADER_HEIGHT - 40, theme, false);
        StatueThemeRenderer.crest(graphics, this.left + 12, this.top + 6, 30, theme);
        StatueThemeRenderer.line(graphics, this.left + 12, this.top + HEADER_HEIGHT - 3, PANEL_WIDTH - 24, theme);
        StatueThemeRenderer.line(graphics, this.left + 12, this.top + PANEL_HEIGHT - 35, PANEL_WIDTH - 24, theme);

        graphics.drawString(this.font, this.title, this.left + 49, this.top + 10, theme.text, false);
        String appearanceKey = this.gold ? "screen.ftboss.statue_hub.color.gold_state"
                : this.white ? "screen.ftboss.statue_hub.color.white_state"
                : "screen.ftboss.statue_hub.color.colored_state";
        Component subtitle = Component.translatable("screen.ftboss.statue_hub.subtitle", this.statueName,
                Component.translatable(appearanceKey));
        wrapped(graphics, subtitle, this.left + 49, this.top + 25, PANEL_WIDTH - 65, theme.muted, 10, 1);

        int selectedY = this.top + HEADER_HEIGHT + 8 + this.section.ordinal() * TAB_SPACING;
        graphics.fill(this.left + 8, selectedY + 3, this.left + 10, selectedY + 22, theme.accentBright);
        int contentX = contentX();
        int contentY = contentY();
        int contentW = contentWidth();
        graphics.fill(contentX - 5, contentY - 4, contentX + contentW + 5,
                this.top + PANEL_HEIGHT - 36, theme.panel);
        graphics.fill(contentX - 7, contentY - 4, contentX - 6, this.top + PANEL_HEIGHT - 36, theme.border);
        switch (this.section) {
            case DAILY -> renderDaily(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
            case CHALLENGE -> renderChallenge(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
            case ENCHANT -> renderEnchant(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
            case EVOLUTION -> renderEvolution(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
            case MATERIAL -> renderMaterials(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
            case DETAILS -> renderWeaponDetails(graphics, contentX, contentY, contentW, accent, mouseX, mouseY);
        }

        int swatchX = this.left + PANEL_WIDTH - 114;
        int swatchY = this.top + PANEL_HEIGHT - 25;
        graphics.fill(swatchX - 1, swatchY - 1, swatchX + 9, swatchY + 9, theme.border);
        graphics.fill(swatchX, swatchY, swatchX + 8, swatchY + 8,
                this.gold ? 0xFFFFCF4A : this.white ? 0xFFF2F0E8 : 0xFF000000 | accent);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.left + 15, selectedY + 23, this.left + SIDEBAR_WIDTH - 15, selectedY + 24, theme.accentBright);
        if (!this.hoverTooltip.isEmpty()) {
            screenGraphics.renderTooltip(this.font, this.hoverTooltip, Optional.empty(), screenMouseX, screenMouseY);
        }
    }

    private int contentX() { return this.left + SIDEBAR_WIDTH + 13; }
    private int contentY() { return this.top + HEADER_HEIGHT + 8; }
    private int contentWidth() { return PANEL_WIDTH - SIDEBAR_WIDTH - 26; }

    private void renderDaily(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        StatueUiTheme theme = statueTheme();
        heading(graphics, x, y, "screen.ftboss.statue_hub.daily.title", accent);
        int taskPages = dailyTaskPages();
        this.dailyTaskPage = Math.min(this.dailyTaskPage, taskPages - 1);
        int leftArrowX = x + width - 96;
        int rightArrowX = x + width - 18;
        StatueWeaponUiDraw.control(graphics, leftArrowX, y - 2, 18, 13, theme, false,
                this.dailyTaskPage > 0, inside(mouseX, mouseY, leftArrowX, y - 2, 18, 13));
        StatueWeaponUiDraw.control(graphics, rightArrowX, y - 2, 18, 13, theme, false,
                this.dailyTaskPage + 1 < taskPages, inside(mouseX, mouseY, rightArrowX, y - 2, 18, 13));
        graphics.drawCenteredString(this.font, "<", leftArrowX + 9, y, this.dailyTaskPage > 0 ? statueTheme().text : statueTheme().muted);
        graphics.drawCenteredString(this.font, ">", rightArrowX + 9, y,
                this.dailyTaskPage + 1 < taskPages ? statueTheme().text : statueTheme().muted);
        Component page = Component.translatable("screen.ftboss.statue_hub.daily.task_page",
                this.dailyTaskPage + 1, taskPages);
        graphics.drawCenteredString(this.font, page, x + width - 48, y, statueTheme().muted);

        int cardY = y + 20;
        if (this.dailyTasks.isEmpty()) {
            wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.daily.none"),
                    x + 4, cardY + 5, width - 8, statueTheme().muted, 11, 4);
        }
        int taskStart = this.dailyTaskPage * DAILY_TASKS_PER_PAGE;
        for (int visible = 0; visible < DAILY_TASKS_PER_PAGE; visible++) {
            int i = taskStart + visible;
            if (i >= this.dailyTasks.size()) break;
            StatueDailyView task = this.dailyTasks.get(i);
            int stateColor = task.state() == 2 ? 0xFF777777 : task.state() == 1 ? 0xFF65D38E : statueTheme().accentBright;
            StatueThemeRenderer.card(graphics, x, cardY, width, 26, theme, task.state() == 1);
            graphics.fill(x + 1, cardY + 3, x + 3, cardY + 23, stateColor);
            Component taskTitle = Component.translatable("screen.ftboss.statue_hub.daily.task", i + 1,
                    task.request());
            List<FormattedCharSequence> titleLine = this.font.split(taskTitle, width - 16);
            if (!titleLine.isEmpty()) graphics.drawString(this.font, titleLine.get(0), x + 8, cardY + 3, statueTheme().text, false);
            Component status = task.state() == 2
                    ? Component.translatable("screen.ftboss.statue_hub.daily.claimed")
                    : task.state() == 1
                    ? Component.translatable("screen.ftboss.statue_hub.daily.ready")
                    : Component.translatable("screen.ftboss.statue_hub.daily.progress",
                    task.progress(), task.required());
            Component details = status.copy().append(Component.literal(" · "))
                    .append(Component.translatable("screen.ftboss.statue_hub.daily.reward", task.reward()));
            List<FormattedCharSequence> clipped = this.font.split(details, width - 16);
            if (!clipped.isEmpty()) graphics.drawString(this.font, clipped.get(0), x + 8, cardY + 14, stateColor, false);
            cardY += 29;
        }

        List<Integer> foods = dailyFoodSlots();
        int pageSize = DAILY_COLUMNS * DAILY_ROWS;
        int pages = Math.max(1, (foods.size() + pageSize - 1) / pageSize);
        this.dailyPage = Math.min(this.dailyPage, pages - 1);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.daily.available",
                foods.size(), this.dailyPage + 1, pages), x + 1, y + 154, statueTheme().accentBright, false);
        int start = this.dailyPage * pageSize;
        int foodY = y + 170;
        for (int cell = 0; cell < pageSize; cell++) {
            int sx = x + (cell % DAILY_COLUMNS) * SLOT;
            int sy = foodY + (cell / DAILY_COLUMNS) * SLOT;
            int listIndex = start + cell;
            ItemStack stack = listIndex < foods.size() && this.minecraft != null && this.minecraft.player != null
                    ? this.minecraft.player.getInventory().getItem(foods.get(listIndex)) : ItemStack.EMPTY;
            drawSlot(graphics, sx, sy, stack, accent, false);
            if (inside(mouseX, mouseY, sx, sy, SLOT, SLOT) && !stack.isEmpty()) {
                this.hoverTooltip = dailyFoodTooltip(stack);
            }
        }
        Component submitHint = Component.translatable("screen.ftboss.statue_hub.daily.hint");
        Component pageHint = Component.translatable("screen.ftboss.statue_hub.daily.page_hint");
        wrapped(graphics, submitHint, x + 172, y + 183, width - 172, theme.muted, 11, 1);
        wrapped(graphics, pageHint, x + 172, y + 198, width - 172, theme.muted, 11, 1);
        if (inside(mouseX, mouseY, x + 172, y + 178, width - 172, 31)) this.hoverTooltip = List.of(submitHint, pageHint);
    }

    private int dailyTaskPages() {
        return Math.max(1, (this.dailyTasks.size() + DAILY_TASKS_PER_PAGE - 1) / DAILY_TASKS_PER_PAGE);
    }

    private List<Integer> dailyFoodSlots() {
        List<Integer> result = new ArrayList<>();
        if (this.minecraft == null || this.minecraft.player == null) return result;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = this.minecraft.player.getInventory().getItem(slot);
            if (FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, stack, this.tint)) result.add(slot);
        }
        ItemStack offhand = this.minecraft.player.getInventory().getItem(40);
        if (FoodWeaponEvolution.isDailySubmissionCandidate(this.dailyTasks, offhand, this.tint)) result.add(40);
        return result;
    }

    private List<Component> dailyFoodTooltip(ItemStack stack) {
        return List.of(stack.getHoverName(),
                Component.translatable("screen.ftboss.statue_hub.daily.submit_click"));
    }

    private void renderChallenge(GuiGraphics graphics, int x, int y, int width, int accent,
                                 int mouseX, int mouseY) {
        heading(graphics, x, y, "screen.ftboss.statue_hub.challenge.title", accent);
        boolean normalHover = inside(mouseX, mouseY, x, y + 23, width, 50);
        boolean finaleHover = inside(mouseX, mouseY, x, y + 79, width, 61);

        int normalBorder = !this.finaleSelected ? 0xFF70D998
                : normalHover ? 0xFF000000 | accent : 0xFF4A4650;
        StatueWeaponUiDraw.control(graphics, x, y + 23, width, 50, statueTheme(), !this.finaleSelected,
                !this.actionPending, normalHover);
        graphics.fill(x + 2, y + 27, x + 4, y + 69, normalBorder);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.challenge.normal"),
                x + 10, y + 31, 0xFF70D998, false);
        Component normalState = Component.translatable(this.finaleSelected
                ? "screen.ftboss.statue_hub.challenge.click_select"
                : "screen.ftboss.statue_hub.challenge.selected");
        graphics.drawString(this.font, normalState, x + width - 10 - this.font.width(normalState),
                y + 31, !this.finaleSelected && !normalHover ? 0xFF70D998 : statueTheme().text, false);
        wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.challenge.normal_desc", this.statueName),
                x + 10, y + 46, width - 20, statueTheme().muted, 11, 2);

        int finaleColor = this.finaleUnlocked ? 0xFFFF6B71 : 0xFF77727D;
        int finaleBorder = this.finaleSelected ? 0xFFFF6B71
                : finaleHover && this.finaleUnlocked ? 0xFF000000 | accent : 0xFF4A4650;
        StatueWeaponUiDraw.control(graphics, x, y + 79, width, 61, statueTheme(), this.finaleSelected,
                this.finaleUnlocked && !this.actionPending, finaleHover);
        graphics.fill(x + 2, y + 83, x + 4, y + 136, finaleBorder);
        graphics.drawString(this.font, Component.translatable(this.finaleUnlocked
                        ? "screen.ftboss.statue_hub.challenge.finale_unlocked"
                        : "screen.ftboss.statue_hub.challenge.finale_locked"),
                x + 10, y + 87, finaleColor, false);
        Component finaleState = Component.translatable(!this.finaleUnlocked
                ? "screen.ftboss.statue_hub.challenge.locked"
                : this.finaleSelected
                ? "screen.ftboss.statue_hub.challenge.selected"
                : "screen.ftboss.statue_hub.challenge.click_select");
        graphics.drawString(this.font, finaleState, x + width - 10 - this.font.width(finaleState),
                y + 87, this.finaleSelected ? 0xFFFF6B71 : statueTheme().muted, false);
        wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.challenge.finale_desc"),
                x + 10, y + 102, width - 20, statueTheme().muted, 11, 3);
        wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.challenge.hint"),
                x + 2, y + 149, width - 4, statueTheme().muted, 11, 4);
    }

    private void renderEnchant(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        heading(graphics, x, y, "screen.ftboss.statue_hub.enchant.title", accent);
        ItemStack weapon = evolutionWeapon();
        if (weapon.isEmpty()) {
            StatueThemeRenderer.card(graphics, x, y + 24, width, 72, statueTheme(), false);
            wrappedCentered(graphics, Component.translatable("screen.ftboss.statue_hub.enchant.no_weapon"),
                    x + 18, y + 45, width - 36, statueTheme().muted, 12, 4);
            return;
        }
        StatueThemeRenderer.card(graphics, x, y + 22, width, 50, statueTheme(), false);
        drawSlot(graphics, x + 8, y + 29, weapon, accent, false);
        wrapped(graphics, weapon.getHoverName(), x + 34, y + 27, width - 44, statueTheme().text, 11, 1);
        wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.enchant.rule"),
                x + 34, y + 40, width - 44, statueTheme().muted, 11, 3);

        List<FoodBlessingView> blessings = FoodWeaponEvolution.foodBlessingViews(weapon);
        StatueThemeRenderer.card(graphics, x, y + 90, width, 64, statueTheme(), false);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.enchant.current",
                blessings.size(), MAX_BLESSINGS), x + 1, y + 78, statueTheme().accent, false);
        if (blessings.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.enchant.none"),
                    x + 8, y + 92, statueTheme().muted, false);
        } else {
            for (int i = 0; i < Math.min(MAX_BLESSINGS, blessings.size()); i++) {
                FoodBlessingView view = blessings.get(i);
                Component line = Component.literal("• ").append(view.name())
                        .append(Component.literal(" " + view.rank() + " · ")).append(view.description());
                wrapped(graphics, line, x + 8, y + 92 + i * 10, width - 16,
                        i == blessings.size() - 1 ? statueTheme().accentBright : statueTheme().text, 11, 1);
                if (inside(mouseX, mouseY, x, y + 92 + i * 10, width, 10)) {
                    this.hoverTooltip = List.of(view.name(), view.description());
                }
            }
        }

        List<Integer> foods = blessingSlots(weapon);
        int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
        int pages = Math.max(1, (foods.size() + pageSize - 1) / pageSize);
        this.ingredientPage = Math.min(this.ingredientPage, pages - 1);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.enchant.foods",
                foods.size(), this.ingredientPage + 1, pages), x + 1, y + 157, statueTheme().accent, false);
        int gridY = y + 170;
        int start = this.ingredientPage * pageSize;
        for (int cell = 0; cell < pageSize; cell++) {
            int sx = x + (cell % INGREDIENT_COLUMNS) * SLOT;
            int sy = gridY + (cell / INGREDIENT_COLUMNS) * SLOT;
            int listIndex = start + cell;
            ItemStack stack = listIndex < foods.size() && this.minecraft != null && this.minecraft.player != null
                    ? this.minecraft.player.getInventory().getItem(foods.get(listIndex)) : ItemStack.EMPTY;
            drawSlot(graphics, sx, sy, stack, accent, false);
            if (inside(mouseX, mouseY, sx, sy, SLOT, SLOT) && !stack.isEmpty()) {
                this.hoverTooltip = blessingFoodTooltip(stack);
            }
        }
        String offeringHint = blessings.size() >= MAX_BLESSINGS
                ? "screen.ftboss.statue_hub.enchant.complete"
                : foods.isEmpty()
                ? "screen.ftboss.statue_hub.enchant.no_offerings"
                : "screen.ftboss.statue_hub.enchant.page_hint";
        wrapped(graphics, Component.translatable(offeringHint), x + 172, y + 177, width - 172,
                blessings.size() >= MAX_BLESSINGS ? 0xFF73D79A : foods.isEmpty() ? 0xFFFF9A70 : statueTheme().muted, 11, 3);
    }

    private void renderEvolution(GuiGraphics graphics, int x, int y, int width, int accent, int mouseX, int mouseY) {
        if (this.repairPanel.isShowing()) {
            this.hoverTooltip = this.repairPanel.render(graphics, this.font, x, y, width, accent, mouseX, mouseY, this.actionPending);
            return;
        }
        heading(graphics, x, y, "screen.ftboss.statue_hub.evolution.title", accent);
        ItemStack weapon = evolutionWeapon();
        if (weapon.isEmpty()) {
            StatueThemeRenderer.card(graphics, x, y + 24, width, 72, statueTheme(), false);
            wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"),
                    x + 10, y + 36, width - 20, statueTheme().muted, 12, 4);
            return;
        }

        StatueThemeRenderer.card(graphics, x, y + 21, width, 56, statueTheme(), false);
        drawSlot(graphics, x + 8, y + 30, weapon, accent, false);
        wrapped(graphics, weapon.getHoverName(), x + 34, y + 27, width - 130, statueTheme().text, 11, 1);
        int level = FoodWeaponEvolution.evolutionLevel(weapon);
        int max = FoodWeaponEvolution.maxEvolutionLevel(weapon);
        int experience = FoodWeaponEvolution.evolutionExperience(weapon);
        int required = FoodWeaponEvolution.nextEvolutionRequirement(weapon);
        int floor = FoodWeaponEvolution.currentEvolutionFloor(weapon);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.level", level, max),
                x + 34, y + 39, statueTheme().accentBright, false);
        drawExperienceBar(graphics, x + 34, y + 53, width - 44, experience, floor, required, level >= max, accent);
        Component xp = level >= max
                ? Component.translatable("screen.ftboss.statue_hub.evolution.maxed")
                : Component.translatable("screen.ftboss.statue_hub.evolution.xp", experience, required);
        graphics.drawCenteredString(this.font, xp, x + 34 + (width - 44) / 2, y + 64, statueTheme().text);

        List<SocketView> sockets = FoodWeaponEvolution.socketViews(weapon);
        int capacity = FoodWeaponEvolution.socketCapacity(weapon);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.sockets_full",
                sockets.size(), capacity), x + 1, y + 78, 0xFF73D79A, false);
        int socketY = y + EVOLUTION_SOCKET_Y;
        for (int index = 0; index < Math.min(capacity, SOCKET_COLUMNS * SOCKET_ROWS); index++) {
            int sx = x + (index % SOCKET_COLUMNS) * SLOT;
            int sy = socketY + (index / SOCKET_COLUMNS) * SLOT;
            SocketView view = index < sockets.size() ? sockets.get(index) : null;
            drawAccessorySlot(graphics, sx, sy, view, accent, index + 1);
            if (inside(mouseX, mouseY, sx, sy, SLOT, SLOT)) {
                this.hoverTooltip = socketTooltip(view, index + 1);
            }
        }

        int infoX = x + SOCKET_COLUMNS * SLOT + 10;
        StatueThemeRenderer.card(graphics, infoX, y + EVOLUTION_SOCKET_Y, width - (infoX - x), 54, statueTheme(), false);
        wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.evolution.socket_help"),
                infoX + 7, y + 96, width - (infoX - x) - 14, statueTheme().muted, 11, 4);
        if (inside(mouseX, mouseY, infoX, y + EVOLUTION_SOCKET_Y, width - (infoX - x), 54)) {
            this.hoverTooltip = List.of(Component.translatable("screen.ftboss.statue_hub.evolution.socket_help"));
        }

        List<Integer> ingredients = ingredientSlots(weapon);
        int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
        int pages = Math.max(1, (ingredients.size() + pageSize - 1) / pageSize);
        this.ingredientPage = Math.min(this.ingredientPage, pages - 1);
        graphics.drawString(this.font, Component.translatable("screen.ftboss.statue_hub.evolution.ingredients",
                ingredients.size(), this.ingredientPage + 1, pages), x + 1, y + 151, statueTheme().accentBright, false);
        int ingredientY = y + EVOLUTION_FOOD_Y;
        int start = this.ingredientPage * pageSize;
        for (int cell = 0; cell < pageSize; cell++) {
            int sx = x + (cell % INGREDIENT_COLUMNS) * SLOT;
            int sy = ingredientY + (cell / INGREDIENT_COLUMNS) * SLOT;
            int listIndex = start + cell;
            ItemStack stack = listIndex < ingredients.size() && this.minecraft != null && this.minecraft.player != null
                    ? this.minecraft.player.getInventory().getItem(ingredients.get(listIndex)) : ItemStack.EMPTY;
            drawSlot(graphics, sx, sy, stack, accent,
                    listIndex < ingredients.size() && ingredients.get(listIndex) == this.offeringSlot);
            if (inside(mouseX, mouseY, sx, sy, SLOT, SLOT) && !stack.isEmpty()) {
                this.hoverTooltip = ingredientTooltip(stack);
            }
        }
        ItemStack offering = inventoryItem(this.offeringSlot);
        Component selected = this.offeringSlot < 0
                ? Component.translatable("screen.ftboss.evolution.select_offering") : offering.getHoverName();
        wrapped(graphics, selected, x + 172, y + 158, width - 172, statueTheme().text, 11, 1);
        if (this.offeringSlot >= 0) {
            wrapped(graphics, Component.translatable("screen.ftboss.evolution.offering_chance",
                    FoodWeaponProgression.qualityName(offering), FoodWeaponProgression.chance(weapon, offering)),
                    x + 172, y + 170, width - 172, statueTheme().accentBright, 11, 1);
            wrapped(graphics, StatueWeaponDetailsPanel.offeringTooltip(offering).get(0),
                    x + 172, y + 181, width - 172, statueTheme().accentBright, 10, 1);
            if (inside(mouseX, mouseY, x + 172, y + 158, width - 172, 34)) this.hoverTooltip = ingredientTooltip(offering);
        }
        wrapped(graphics, Component.translatable("screen.ftboss.evolution.failure_warning"),
                x + 172, y + 193, width - 172, 0xFFFF9A70, 10, 2);
    }

    private void renderMaterials(GuiGraphics graphics, int originX, int originY, int width, int accent, int mouseX, int mouseY) {
        if (renderAttachmentExemption(graphics, originX, originY, width)) return;
        heading(graphics, originX, originY, "screen.ftboss.material.title", accent);
        ItemStack weapon = evolutionWeapon();
        if (usesCavity(weapon)) return;
        if (weapon.isEmpty()) {
            StatueThemeRenderer.card(graphics, originX, originY + 24, width, 72, statueTheme(), false);
            wrapped(graphics, Component.translatable("screen.ftboss.statue_hub.evolution.no_weapon"),
                    originX + 10, originY + 36, width - 20, statueTheme().muted, 12, 4);
            return;
        }
        drawSlot(graphics, originX, originY + 16, weapon, accent, false);
        wrapped(graphics, weapon.getHoverName(), originX + 24, originY + 21, (usesCavity(weapon) ? 150 : width) - 24, statueTheme().text, 11, 1);
        Material candidate = WeaponMaterialBuild.materialFor(inventoryItem(this.materialSlot));
        if (!usesCavity(weapon)) {
            Build preview = candidate == Material.ORIGINAL ? WeaponMaterialBuild.build(weapon)
                    : WeaponMaterialBuild.preview(weapon, this.materialPart, candidate);
            int half = width / 2;
            Stats familyCurrent = FoodWeaponFamilyBuild.stats(weapon);
            Stats familyPreview = candidate == Material.ORIGINAL ? familyCurrent
                    : FoodWeaponFamilyBuild.preview(weapon, this.materialPart, candidate);
            CombatStats currentStats = familyCurrent.kind() == Kind.MELEE ? WeaponMaterialBuild.combatStats(weapon) : null;
            CombatStats previewStats = familyCurrent.kind() != Kind.MELEE ? null : candidate == Material.ORIGINAL ? currentStats
                    : WeaponMaterialBuild.combatPreview(weapon, this.materialPart, candidate);
            List<Component> currentLines = StatueWeaponUiDraw.buildLines(familyCurrent, currentStats);
            List<Component> previewLines = StatueWeaponUiDraw.buildLines(familyPreview, previewStats);
            renderBuildStats(graphics, originX, originY + 37, half - 3, currentLines, "screen.ftboss.material.current");
            renderBuildStats(graphics, originX + half + 3, originY + 37, half - 3, previewLines, "screen.ftboss.material.preview");
            if (inside(mouseX, mouseY, originX, originY + 37, half - 3, 60)) this.hoverTooltip = buildTooltip(weapon, currentLines, familyCurrent);
            if (inside(mouseX, mouseY, originX + half + 3, originY + 37, half - 3, 60)) this.hoverTooltip = buildTooltip(weapon, previewLines, familyPreview);
            List<Material> materials = WeaponMaterialBuild.materials(weapon);
            int partWidth = width / Part.values().length;
            for (Part part : Part.values()) {
                int partX = originX + part.ordinal() * partWidth;
                Material installed = materials.get(part.ordinal());
                boolean selected = part == this.materialPart;
                StatueWeaponUiDraw.control(graphics, partX, originY + MATERIAL_PART_Y, partWidth - 3, MATERIAL_PART_HEIGHT,
                        statueTheme(), selected, !this.actionPending,
                        inside(mouseX, mouseY, partX, originY + MATERIAL_PART_Y, partWidth - 3, MATERIAL_PART_HEIGHT));
                wrapped(graphics, Component.translatable("screen.ftboss.material.part",
                        FoodWeaponFamilyBuild.partName(weapon, part), WeaponMaterialBuild.materialName(installed)),
                        partX + 5, originY + MATERIAL_PART_Y + 3, partWidth - 13, selected ? statueTheme().accentBright : statueTheme().text, 10, 2);
                if (inside(mouseX, mouseY, partX, originY + MATERIAL_PART_Y, partWidth - 3, MATERIAL_PART_HEIGHT)) {
                    this.hoverTooltip = materialTooltip(FoodWeaponFamilyBuild.partName(weapon, part), installed);
                }
            }
        }
        renderMaterialFilters(graphics, originX, originY, width, mouseX, mouseY);
        List<Integer> slots = materialSlots(weapon);
        int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
        int pages = Math.max(1, (slots.size() + pageSize - 1) / pageSize);
        wrapped(graphics, Component.translatable("screen.ftboss.material.inventory",
                slots.size(), this.materialPage + 1, pages), originX, originY + MATERIAL_INVENTORY_Y, width - 36, statueTheme().accentBright, 10, 1);
        StatueWeaponUiDraw.control(graphics, originX + width - 34, originY + MATERIAL_INVENTORY_Y - 1, 14, 12,
                statueTheme(), false, this.materialPage > 0,
                inside(mouseX, mouseY, originX + width - 34, originY + MATERIAL_INVENTORY_Y - 1, 14, 12));
        StatueWeaponUiDraw.control(graphics, originX + width - 14, originY + MATERIAL_INVENTORY_Y - 1, 14, 12,
                statueTheme(), false, this.materialPage + 1 < pages,
                inside(mouseX, mouseY, originX + width - 14, originY + MATERIAL_INVENTORY_Y - 1, 14, 12));
        graphics.drawCenteredString(this.font, "<", originX + width - 27, originY + MATERIAL_INVENTORY_Y,
                this.materialPage > 0 ? statueTheme().text : statueTheme().muted);
        graphics.drawCenteredString(this.font, ">", originX + width - 7, originY + MATERIAL_INVENTORY_Y,
                this.materialPage + 1 < pages ? statueTheme().text : statueTheme().muted);
        for (int cell = 0; cell < pageSize; cell++) {
            int listIndex = this.materialPage * pageSize + cell;
            int slot = listIndex < slots.size() ? slots.get(listIndex) : -1;
            ItemStack stack = inventoryItem(slot);
            int slotX = originX + cell % INGREDIENT_COLUMNS * SLOT;
            int slotY = originY + MATERIAL_FOOD_Y + cell / INGREDIENT_COLUMNS * SLOT;
            drawSlot(graphics, slotX, slotY, stack, accent, slot >= 0 && slot == this.materialSlot);
            Material material = WeaponMaterialBuild.materialFor(stack);
            if (WeaponMaterialCatalog.boss(material)) graphics.drawString(this.font, "!", slotX + 12, slotY + 1, 0xFFFFCF4A, true);
            if (slot >= 0 && inside(mouseX, mouseY, slotX, slotY, SLOT, SLOT)) {
                this.hoverTooltip = materialTooltip(stack.getHoverName(), material);
            }
        }
        Component passive = candidate == Material.ORIGINAL ? slots.isEmpty()
                ? Component.translatable("screen.ftboss.material.filter_empty", this.materialFilter.label())
                : Component.translatable("screen.ftboss.material.select_hint")
                : Component.translatable(WeaponMaterialCatalog.boss(candidate)
                ? "screen.ftboss.material.boss_candidate" : "screen.ftboss.material.candidate", WeaponMaterialBuild.materialName(candidate))
                .append(Component.literal("\n")).append(WeaponMaterialBuild.passive(candidate));
        wrapped(graphics, passive, originX + 172, originY + MATERIAL_FOOD_Y, width - 172,
                WeaponMaterialCatalog.boss(candidate) ? 0xFFFFCF4A : statueTheme().text, 11, 3);
        if (inside(mouseX, mouseY, originX + 172, originY + MATERIAL_FOOD_Y, width - 172, 36)) {
            this.hoverTooltip = candidate == Material.ORIGINAL ? List.of(passive)
                    : materialTooltip(inventoryItem(this.materialSlot).getHoverName(), candidate);
        }
        String costWarning = WeaponMaterialCatalog.boss(candidate) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning";
        String warning = candidate != Material.ORIGINAL
                        && !canReplaceMaterial(weapon, WeaponMaterialBuild.preview(weapon, this.materialPart, candidate))
                        ? "message.ftboss.material.repair_first"
                        : WeaponMaterialCatalog.boss(candidate) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning";
        wrapped(graphics, Component.translatable(warning),
                originX, originY + MATERIAL_WARNING_Y, width, warning.equals("message.ftboss.material.repair_first") ? 0xFFFF6B6B : 0xFFFF9A70, 10, 2);
        if (inside(mouseX, mouseY, originX, originY + MATERIAL_WARNING_Y, width, 19)) {
            this.hoverTooltip = List.of(Component.translatable(warning), Component.translatable(costWarning),
                    Component.translatable("tooltip.ftboss.material.unique"));
        }
    }

    private void renderMaterialFilters(GuiGraphics graphics, int originX, int originY, int width, int mouseX, int mouseY) {
        int filterWidth = width / MaterialFilter.values().length;
        for (MaterialFilter filter : MaterialFilter.values()) {
            int filterX = originX + filter.ordinal() * filterWidth;
            StatueWeaponUiDraw.control(graphics, filterX, originY + MATERIAL_FILTER_Y, filterWidth - 3, MATERIAL_FILTER_HEIGHT,
                    statueTheme(), this.materialFilter == filter, !this.actionPending,
                    inside(mouseX, mouseY, filterX, originY + MATERIAL_FILTER_Y, filterWidth - 3, MATERIAL_FILTER_HEIGHT));
            graphics.drawCenteredString(this.font, filter.label(), filterX + (filterWidth - 3) / 2, originY + MATERIAL_FILTER_Y + 3,
                    this.materialFilter == filter ? statueTheme().accentBright : statueTheme().text);
        }
    }

    private List<Component> materialTooltip(Component title, Material material) {
        List<Component> tooltip = new ArrayList<>(List.of(title, WeaponMaterialBuild.materialName(material), WeaponMaterialCatalog.source(material),
                WeaponMaterialBuild.passive(material),
                Component.translatable("tooltip.ftboss.material.unique"), Component.translatable("tooltip.ftboss.material.trigger_scope"),
                Component.translatable("screen.ftboss.material.select_hint"),
                Component.translatable(WeaponMaterialCatalog.boss(material) ? "screen.ftboss.material.boss_cost_warning" : "screen.ftboss.material.cost_warning")));
        if (WeaponMaterialCatalog.group(material) == Group.INGOT) tooltip.add(Component.translatable("screen.ftboss.material.ingot_rules"));
        return List.copyOf(tooltip);
    }

    private boolean canReplaceMaterial(ItemStack weapon, Build preview) {
        if (net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(weapon)) return false;
        int oldMax = weapon.getMaxDamage();
        int newMax = WeaponMaterialBuild.previewDurability(weapon, preview);
        return oldMax <= 0 || FoodWeaponBuildRules.preserveDamage(weapon.getDamageValue(), oldMax, newMax) < newMax;
    }

    private void renderBuildStats(GuiGraphics graphics, int originX, int originY, int width, List<Component> lines, String label) {
        StatueThemeRenderer.card(graphics, originX, originY, width, 60, statueTheme(), false);
        graphics.drawString(this.font, Component.translatable(label), originX + 6, originY + 4, statueTheme().accentBright, false);
        for (int index = 0; index < lines.size(); index++) {
            wrapped(graphics, lines.get(index), originX + 6, originY + 16 + index * 11, width - 12,
                    index == lines.size() - 1 ? statueTheme().muted : statueTheme().text, 10, 1);
        }
    }

    private List<Component> buildTooltip(ItemStack weapon, List<Component> lines, Stats stats) {
        List<Component> tooltip = new ArrayList<>(lines);
        tooltip.addAll(StatueWeaponUiDraw.familyTooltip(weapon, stats));
        return List.copyOf(tooltip);
    }

    private boolean renderAttachmentExemption(GuiGraphics graphics, int originX, int originY, int width) {
        if (!net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(evolutionWeapon())) return false;
        if (this.replaceButton != null) this.replaceButton.visible = false;
        heading(graphics, originX, originY, "screen.ftboss.material.title", statueTheme().accent);
        StatueThemeRenderer.card(graphics, originX, originY + 24, width, 72, statueTheme(), false);
        wrapped(graphics, Component.translatable("screen.ftboss.material.not_required"),
                originX + 10, originY + 36, width - 20, statueTheme().text, 12, 4);
        return true;
    }

    private ItemStack inventoryItem(int slot) {
        return this.minecraft == null || this.minecraft.player == null || slot < 0 || slot >= this.minecraft.player.getInventory().getContainerSize()
                ? ItemStack.EMPTY : this.minecraft.player.getInventory().getItem(slot);
    }

    private void selectInventorySlot(ItemStack weapon, int slot, boolean material) {
        this.selectionWeapon = weapon.copy();
        this.selectionFood = inventoryItem(slot).copy();
        this.offeringSlot = material ? -1 : slot;
        this.materialSlot = material ? slot : -1;
        this.updateActionButtons();
    }

    private List<Integer> materialSlots(ItemStack weapon) {
        List<Integer> result = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventoryItem(slot);
            Material material = WeaponMaterialBuild.materialFor(stack);
            if (stack != weapon && material != Material.ORIGINAL && this.materialFilter.accepts(material)) result.add(slot);
        }
        int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
        this.materialPage = Math.max(0, Math.min(this.materialPage, Math.max(0, (result.size() - 1) / pageSize)));
        return result;
    }

    private List<WeaponDetailView> weaponDetails(ItemStack weapon) {
        return StatueWeaponDetailsCache.frame(this, weapon).details();
    }

    private WeaponDetailView skillAtMouse(double mouseX, double mouseY) {
        List<WeaponDetailView> details = weaponDetails(evolutionWeapon());
        for (int row = 0; row < DETAILS_PER_PAGE; row++) {
            int index = this.detailPage * DETAILS_PER_PAGE + row;
            if (index < details.size() && inside(mouseX, mouseY, contentX(), contentY() + 96 + row * 38, contentWidth(), 34)) {
                return details.get(index);
            }
        }
        return null;
    }

    private void renderWeaponDetails(GuiGraphics graphics, int x, int y, int width,
                                     int accent, int mouseX, int mouseY) {
        ItemStack weapon = evolutionWeapon();
        var result = StatueWeaponDetailsPanel.render(graphics, this.font, x, y, width, accent, mouseX, mouseY,
                weapon, weaponDetails(weapon), this.detailPage);
        this.detailPage = result.page();
        this.hoverTooltip = result.tooltip();
    }

    private void drawExperienceBar(GuiGraphics graphics, int x, int y, int width,
                                   int experience, int floor, int required, boolean maxed, int accent) {
        double ratio = maxed ? 1.0D : required <= floor ? 0.0D
                : Math.max(0.0D, Math.min(1.0D, (experience - floor) / (double) (required - floor)));
        StatueWeaponUiDraw.progress(graphics, x, y, width, 9, ratio, statueTheme(), accent);
    }

    private void drawAccessorySlot(GuiGraphics graphics, int x, int y, SocketView view, int accent, int number) {
        drawSlot(graphics, x, y, view == null ? ItemStack.EMPTY : view.stack(),
                view == null ? accent : view.color(), view != null && view.matching());
        if (view == null) {
            String label = Integer.toString(number);
            graphics.drawString(this.font, label, x + 9 - this.font.width(label) / 2, y + 5, statueTheme().muted, false);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, ItemStack stack, int color, boolean matching) {
        StatueWeaponUiDraw.slot(graphics, x, y, stack, color, matching, statueTheme());
        if (!stack.isEmpty()) {
            if (stack.getCount() > 1) graphics.drawString(this.font, Integer.toString(stack.getCount()),
                    x + 16 - this.font.width(Integer.toString(stack.getCount())), y + 10, 0xFFFFFFFF, true);
        }
    }

    private List<Component> socketTooltip(SocketView view, int slot) {
        if (view == null) return List.of(Component.translatable("screen.ftboss.statue_hub.evolution.socket_empty_slot", slot));
        return List.of(
                Component.literal("#" + slot + " ").append(view.stack().getHoverName()),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", view.type(), view.tier()),
                view.effect(),
                Component.translatable("screen.ftboss.statue_hub.evolution.synergy", view.synergy()),
                Component.translatable("screen.ftboss.statue_hub.evolution.remove_click"));
    }

    private List<Component> blessingFoodTooltip(ItemStack stack) {
        SocketView view = FoodWeaponEvolution.ingredientView(stack);
        if (view == null) return List.of(stack.getHoverName());
        return List.of(stack.getHoverName(),
                Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", view.type(), view.tier()),
                Component.translatable("screen.ftboss.statue_hub.enchant.offer_click"));
    }

    private List<Component> ingredientTooltip(ItemStack stack) {
        SocketView view = FoodWeaponEvolution.ingredientView(stack);
        List<Component> result = new ArrayList<>();
        result.add(stack.getHoverName());
        if (stack != inventoryItem(40) && FoodWeaponProgression.isOffering(evolutionWeapon(), stack, this.tint)) {
            result.add(Component.translatable("screen.ftboss.evolution.offering_chance",
                    FoodWeaponProgression.qualityName(stack), FoodWeaponProgression.chance(evolutionWeapon(), stack)));
            result.add(Component.translatable("screen.ftboss.evolution.select_offering"));
            result.addAll(StatueWeaponDetailsPanel.offeringTooltip(stack));
            result.add(Component.translatable("screen.ftboss.evolution.failure_warning"));
        }
        if (view != null) {
            result.add(Component.translatable("screen.ftboss.statue_hub.evolution.socket_type", view.type(), view.tier()));
            result.add(view.effect());
            result.add(Component.translatable("screen.ftboss.statue_hub.evolution.synergy", view.synergy()));
            result.add(Component.translatable("screen.ftboss.statue_hub.evolution.insert_click"));
        }
        return List.copyOf(result);
    }

    private List<Integer> blessingSlots(ItemStack weapon) {
        return candidateSlots(weapon, true);
    }

    private List<Integer> ingredientSlots(ItemStack weapon) {
        return candidateSlots(weapon, false);
    }

    private List<Integer> candidateSlots(ItemStack weapon, boolean blessing) {
        List<Integer> result = new ArrayList<>();
        if (this.minecraft == null || this.minecraft.player == null) return result;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = this.minecraft.player.getInventory().getItem(slot);
            if (stack != weapon && (blessing
                    ? FoodWeaponEvolution.isBlessingCandidate(weapon, stack, this.tint)
                    : FoodWeaponEvolution.isSocketCandidate(weapon, stack, this.tint)
                    || FoodWeaponProgression.isOffering(weapon, stack, this.tint))) result.add(slot);
        }
        ItemStack offhand = this.minecraft.player.getInventory().getItem(40);
        if (offhand != weapon && (blessing
                ? FoodWeaponEvolution.isBlessingCandidate(weapon, offhand, this.tint)
                : FoodWeaponEvolution.isSocketCandidate(weapon, offhand, this.tint))) result.add(40);
        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = this.viewport.localX(mouseX);
        mouseY = this.viewport.localY(mouseY);
        this.updateActionButtons();
        if (this.section == Section.MATERIAL && usesCavity(evolutionWeapon())) {
            if (!this.actionPending) openSwordCavity();
            return true;
        }
        if (this.section == Section.EVOLUTION && (this.repairPanel.isShowing()
                || StatueWeaponRepairPanel.blocksChanges(evolutionWeapon()))) return super.mouseClicked(mouseX, mouseY, button);
        if (this.section == Section.DETAILS && !this.actionPending && (button == 1 || button == 0 && hasShiftDown())) {
            WeaponDetailView view = skillAtMouse(mouseX, mouseY);
            if (view != null && view.selectable()) {
                this.actionPending = true;
                this.updateActionButtons();
                StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_TOGGLE_CARRIED_SKILL, view.gourmetSkillIndex());
                return true;
            }
        }
        if (this.section == Section.EVOLUTION && !this.actionPending && (button == 1 || button == 0 && hasShiftDown())) {
            ItemStack weapon = evolutionWeapon();
            int cell = gridIndex(mouseX, mouseY, contentX(), contentY() + EVOLUTION_FOOD_Y, INGREDIENT_COLUMNS, INGREDIENT_ROWS);
            List<Integer> ingredients = ingredientSlots(weapon);
            int listIndex = this.ingredientPage * INGREDIENT_COLUMNS * INGREDIENT_ROWS + cell;
            if (!weapon.isEmpty() && cell >= 0 && listIndex < ingredients.size()
                    && FoodWeaponEvolution.isSocketCandidate(weapon, inventoryItem(ingredients.get(listIndex)), this.tint)) {
                this.actionPending = true;
                this.updateActionButtons();
                StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_INSERT_SOCKET, ingredients.get(listIndex));
                return true;
            }
        }
        if (button == 0 && !this.actionPending) {
            if (this.section == Section.MATERIAL) {
                ItemStack weapon = evolutionWeapon();
                int filterWidth = contentWidth() / MaterialFilter.values().length;
                for (MaterialFilter filter : MaterialFilter.values()) {
                    if (!weapon.isEmpty() && inside(mouseX, mouseY, contentX() + filter.ordinal() * filterWidth,
                            contentY() + MATERIAL_FILTER_Y, filterWidth - 3, MATERIAL_FILTER_HEIGHT)) {
                        this.materialFilter = filter;
                        this.materialPage = 0;
                        this.materialSlot = -1;
                        this.updateActionButtons();
                        return true;
                    }
                }
                List<Integer> slots = materialSlots(weapon);
                int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
                int pages = Math.max(1, (slots.size() + pageSize - 1) / pageSize);
                if (inside(mouseX, mouseY, contentX() + contentWidth() - 34, contentY() + MATERIAL_INVENTORY_Y - 1, 14, 12)
                        && this.materialPage > 0) { this.materialPage--; return true; }
                if (inside(mouseX, mouseY, contentX() + contentWidth() - 14, contentY() + MATERIAL_INVENTORY_Y - 1, 14, 12)
                        && this.materialPage + 1 < pages) { this.materialPage++; return true; }
                int partWidth = contentWidth() / Part.values().length;
                for (Part part : usesCavity(weapon) ? new Part[0] : Part.values()) {
                    if (inside(mouseX, mouseY, contentX() + part.ordinal() * partWidth, contentY() + MATERIAL_PART_Y,
                            partWidth - 3, MATERIAL_PART_HEIGHT)) {
                        this.materialPart = part;
                        this.updateActionButtons();
                        return true;
                    }
                }
                int cell = gridIndex(mouseX, mouseY, contentX(), contentY() + MATERIAL_FOOD_Y, INGREDIENT_COLUMNS, INGREDIENT_ROWS);
                int listIndex = this.materialPage * INGREDIENT_COLUMNS * INGREDIENT_ROWS + cell;
                if (!weapon.isEmpty() && cell >= 0 && listIndex < slots.size()) {
                    selectInventorySlot(weapon, slots.get(listIndex), true);
                    return true;
                }
            }
            if (this.section == Section.CHALLENGE) {
                int x = contentX();
                int y = contentY();
                int difficulty = inside(mouseX, mouseY, x, y + 23, contentWidth(), 50) ? 0
                        : inside(mouseX, mouseY, x, y + 79, contentWidth(), 61) ? 1 : -1;
                if (difficulty >= 0) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos,
                            StatueMenuNetwork.ACTION_SET_CHALLENGE_DIFFICULTY, difficulty);
                    return true;
                }
            }
            if (this.section == Section.DETAILS) {
                List<WeaponDetailView> details = weaponDetails(evolutionWeapon());
                int pages = Math.max(1, (details.size() + DETAILS_PER_PAGE - 1) / DETAILS_PER_PAGE);
                int x = contentX();
                int y = contentY() + 77;
                if (inside(mouseX, mouseY, x + contentWidth() - 96, y, 18, 13) && this.detailPage > 0) {
                    this.detailPage--;
                    return true;
                }
                if (inside(mouseX, mouseY, x + contentWidth() - 18, y, 18, 13)
                        && this.detailPage + 1 < pages) {
                    this.detailPage++;
                    return true;
                }
                int cardTop = contentY() + 96;
                if (inside(mouseX, mouseY, x, cardTop, contentWidth(), DETAILS_PER_PAGE * 38)) {
                    int row = ((int) mouseY - cardTop) / 38;
                    int detailIndex = this.detailPage * DETAILS_PER_PAGE + row;
                    if (detailIndex >= 0 && detailIndex < details.size()) {
                        WeaponDetailView view = details.get(detailIndex);
                        if (view.selectable()) {
                            if (!view.selected()) {
                                this.actionPending = true;
                                this.updateActionButtons();
                                StatueMenuNetwork.Action.send(this.statuePos,
                                        StatueMenuNetwork.ACTION_SELECT_GOURMET_SKILL, view.gourmetSkillIndex());
                            }
                            return true;
                        }
                    }
                }
            }
            if (this.section == Section.DAILY) {
                int x = contentX();
                int y = contentY();
                if (inside(mouseX, mouseY, x + contentWidth() - 96, y - 2, 18, 13)
                        && this.dailyTaskPage > 0) {
                    this.dailyTaskPage--;
                    return true;
                }
                if (inside(mouseX, mouseY, x + contentWidth() - 18, y - 2, 18, 13)
                        && this.dailyTaskPage + 1 < dailyTaskPages()) {
                    this.dailyTaskPage++;
                    return true;
                }
                List<Integer> foods = dailyFoodSlots();
                int cell = gridIndex(mouseX, mouseY, contentX(), contentY() + 170,
                        DAILY_COLUMNS, DAILY_ROWS);
                int listIndex = this.dailyPage * DAILY_COLUMNS * DAILY_ROWS + cell;
                if (cell >= 0 && listIndex >= 0 && listIndex < foods.size()) {
                    this.actionPending = true;
                    this.updateActionButtons();
                    StatueMenuNetwork.Action.send(this.statuePos,
                            StatueMenuNetwork.ACTION_SUBMIT_DAILY, foods.get(listIndex));
                    return true;
                }
            }
            if (this.section == Section.ENCHANT) {
                ItemStack weapon = evolutionWeapon();
                if (!weapon.isEmpty()) {
                    int cell = gridIndex(mouseX, mouseY, contentX(), contentY() + 170,
                            INGREDIENT_COLUMNS, INGREDIENT_ROWS);
                    List<Integer> foods = blessingSlots(weapon);
                    int listIndex = this.ingredientPage * INGREDIENT_COLUMNS * INGREDIENT_ROWS + cell;
                    if (cell >= 0 && listIndex >= 0 && listIndex < foods.size()) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        StatueMenuNetwork.Action.send(this.statuePos,
                                StatueMenuNetwork.ACTION_APPLY_FOOD_BLESSING, foods.get(listIndex));
                        return true;
                    }
                }
            }
            if (this.section == Section.EVOLUTION) {
                ItemStack weapon = evolutionWeapon();
                if (!weapon.isEmpty()) {
                    int x = contentX();
                    int y = contentY();
                    int socketIndex = gridIndex(mouseX, mouseY, x, y + EVOLUTION_SOCKET_Y, SOCKET_COLUMNS, SOCKET_ROWS);
                    int socketCount = FoodWeaponEvolution.socketCount(weapon);
                    if (socketIndex >= 0 && socketIndex < socketCount) {
                        this.actionPending = true;
                        this.updateActionButtons();
                        StatueMenuNetwork.Action.send(this.statuePos,
                                StatueMenuNetwork.ACTION_REMOVE_SOCKET, socketIndex);
                        return true;
                    }
                    int cell = gridIndex(mouseX, mouseY, x, y + EVOLUTION_FOOD_Y, INGREDIENT_COLUMNS, INGREDIENT_ROWS);
                    List<Integer> ingredients = ingredientSlots(weapon);
                    int listIndex = this.ingredientPage * INGREDIENT_COLUMNS * INGREDIENT_ROWS + cell;
                    if (cell >= 0 && listIndex >= 0 && listIndex < ingredients.size()) {
                        int slot = ingredients.get(listIndex);
                        if (slot < 36 && FoodWeaponProgression.isOffering(weapon, inventoryItem(slot), this.tint)) {
                            selectInventorySlot(weapon, slot, false);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        mouseX = this.viewport.localX(mouseX);
        mouseY = this.viewport.localY(mouseY);
        if (this.section == Section.EVOLUTION && this.repairPanel.isShowing()) return super.mouseScrolled(mouseX, mouseY, delta);
        if (this.section == Section.MATERIAL) {
            int pageSize = INGREDIENT_COLUMNS * INGREDIENT_ROWS;
            int pages = Math.max(1, (materialSlots(evolutionWeapon()).size() + pageSize - 1) / pageSize);
            this.materialPage = Math.max(0, Math.min(pages - 1, this.materialPage + (delta < 0 ? 1 : -1)));
            return true;
        }
        if (this.section == Section.DAILY) {
            int x = contentX();
            int y = contentY();
            int taskPages = dailyTaskPages();
            if (taskPages > 1 && inside(mouseX, mouseY, x, y + 15, contentWidth(), 125)) {
                this.dailyTaskPage = Math.max(0, Math.min(taskPages - 1,
                        this.dailyTaskPage + (delta < 0 ? 1 : -1)));
                return true;
            }
            int pages = Math.max(1, (dailyFoodSlots().size() + DAILY_COLUMNS * DAILY_ROWS - 1)
                    / (DAILY_COLUMNS * DAILY_ROWS));
            if (pages > 1) {
                this.dailyPage = Math.max(0, Math.min(pages - 1,
                        this.dailyPage + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        if (this.section == Section.ENCHANT) {
            List<Integer> ingredients = blessingSlots(evolutionWeapon());
            int pages = Math.max(1, (ingredients.size() + INGREDIENT_COLUMNS * INGREDIENT_ROWS - 1)
                    / (INGREDIENT_COLUMNS * INGREDIENT_ROWS));
            if (pages > 1) {
                this.ingredientPage = Math.max(0, Math.min(pages - 1,
                        this.ingredientPage + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        if (this.section == Section.EVOLUTION) {
            List<Integer> ingredients = ingredientSlots(evolutionWeapon());
            int pages = Math.max(1, (ingredients.size() + INGREDIENT_COLUMNS * INGREDIENT_ROWS - 1)
                    / (INGREDIENT_COLUMNS * INGREDIENT_ROWS));
            if (pages > 1) {
                this.ingredientPage = Math.max(0, Math.min(pages - 1,
                        this.ingredientPage + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        if (this.section == Section.DETAILS) {
            List<WeaponDetailView> details = weaponDetails(evolutionWeapon());
            int pages = Math.max(1, (details.size() + DETAILS_PER_PAGE - 1) / DETAILS_PER_PAGE);
            if (pages > 1) {
                this.detailPage = Math.max(0, Math.min(pages - 1,
                        this.detailPage + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(this.viewport.localX(mouseX), this.viewport.localY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(this.viewport.localX(mouseX), this.viewport.localY(mouseY), button,
                this.viewport.localDelta(dragX), this.viewport.localDelta(dragY));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(this.viewport.localX(mouseX), this.viewport.localY(mouseY));
    }

    @Override
    public void setTooltipForNextRenderPass(List<FormattedCharSequence> lines,
            net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner positioner, boolean override) {
        super.setTooltipForNextRenderPass(lines, this.viewport != null && this.viewport.scaled()
                ? net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE : positioner, override);
    }

    private static int gridIndex(double mouseX, double mouseY, int x, int y, int columns, int rows) {
        if (!inside(mouseX, mouseY, x, y, columns * SLOT, rows * SLOT)) return -1;
        return ((int) mouseY - y) / SLOT * columns + ((int) mouseX - x) / SLOT;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static boolean usesCavity(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) || SwordCavityBuild.applies(weapon);
    }

    private ItemStack evolutionWeapon() {
        if (this.minecraft == null || this.minecraft.player == null) return ItemStack.EMPTY;
        if (this.section == Section.ENCHANT) {
            ItemStack armor = net.farmingtales.ftboss.item.ArmorFoodBlessings.heldArmor(this.minecraft.player);
            if (!armor.isEmpty()) return armor;
        }
        ItemStack main = this.minecraft.player.getMainHandItem();
        if (FoodWeaponFamilyBuild.isBuildCandidate(main)) return main;
        ItemStack off = this.minecraft.player.getOffhandItem();
        return FoodWeaponFamilyBuild.isBuildCandidate(off) ? off : ItemStack.EMPTY;
    }

    private ItemStack buildItem() {
        if (this.minecraft == null || this.minecraft.player == null) return ItemStack.EMPTY;
        ItemStack main = this.minecraft.player.getMainHandItem();
        if (FoodWeaponFamilyBuild.isBuildCandidate(main)) return main;
        ItemStack off = this.minecraft.player.getOffhandItem();
        return FoodWeaponFamilyBuild.isBuildCandidate(off) ? off : ItemStack.EMPTY;
    }

    private void heading(GuiGraphics graphics, int x, int y, String key, int accent) {
        StatueWeaponUiDraw.heading(graphics, this.font, x, y, key, statueTheme());
    }

    private int wrapped(GuiGraphics graphics, Component text, int x, int y, int width,
                        int color, int lineHeight, int maxLines) {
        return StatueWeaponUiDraw.text(graphics, this.font, text, x, y, width, color, lineHeight, maxLines);
    }

    private void wrappedCentered(GuiGraphics graphics, Component text, int x, int y, int width,
                                 int color, int lineHeight, int maxLines) {
        StatueWeaponUiDraw.centered(graphics, this.font, text, x, y, width, color, lineHeight, maxLines);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
