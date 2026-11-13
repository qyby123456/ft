package net.farmingtales.ftdungeonmobs;

import java.util.List;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerator;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerators;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;

public final class DungeonGeneratorMenu extends ChestMenu {
    private final ServerLevel source;
    private final BlockPos anchor;
    private final SimpleContainer display;
    private final List<DungeonGenerator> providers = DungeonGenerators.all();
    private int page;
    private long nextRefresh;
    private long regenerateConfirmationUntil;
    private String regenerateConfirmationInstance = "";

    public DungeonGeneratorMenu(int containerId, Inventory inventory, ServerLevel source, BlockPos anchor) {
        this(containerId, inventory, source, anchor, new SimpleContainer(27));
    }

    private DungeonGeneratorMenu(int containerId, Inventory inventory, ServerLevel source, BlockPos anchor, SimpleContainer display) {
        super(MenuType.GENERIC_9x3, containerId, inventory, display, 3);
        this.source = source;
        this.anchor = anchor.immutable();
        this.display = display;
        refresh();
    }

    private static ItemStack button(ItemStack stack, String name, String... descriptions) {
        stack.setHoverName(Component.literal(name));
        ListTag lore = new ListTag();
        for (String description : descriptions) lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(description))));
        stack.getOrCreateTagElement("display").put("Lore", lore);
        return stack;
    }

    private void refresh() {
        display.clearContent();
        if (!(source.getBlockEntity(anchor) instanceof DungeonGateBlockEntity gate)) return;
        for (int index = 0; index < 20; index++) {
            int providerIndex = page * 20 + index;
            if (providerIndex >= providers.size()) break;
            DungeonGenerator provider = providers.get(providerIndex);
            display.setItem(index, button(provider.icon().copy(), provider.title().getString()
                    + (provider.id().equals(gate.generatorId()) ? " ✓ 已选择" : ""),
                    "点击选择此生成类型（管理员/创造模式）", "生成后固定绑定，避免入口错连"));
        }
        display.setItem(21, button(new ItemStack(Items.ARROW), "上一页"));
        DungeonGenerator.Status status = DungeonGateGenerators.status(source, gate);
        String selected = DungeonGenerators.find(gate.generatorId()).map(provider -> provider.title().getString())
                .orElse("不可用生成器 " + gate.generatorId());
        display.setItem(22, button(new ItemStack(Items.PAPER), selected, status.detail().getString(),
                "实例：" + (gate.generatorInstance().isEmpty() ? "尚未绑定" : gate.generatorInstance()),
                "生成后不可换类型或种子；请另放一座入口门"));
        display.setItem(23, button(new ItemStack(Items.ARROW), "下一页"));
        display.setItem(24, button(new ItemStack(Items.WHEAT_SEEDS), "种子："
                + (gate.hasGeneratorSeed() ? gate.generatorSeed() : "生成时随机"), "点击重抽；仅在生成前允许"));
        String action = status.ready() ? "进入地牢" :
                status.phase() == DungeonGenerator.Phase.PAUSED || status.phase() == DungeonGenerator.Phase.PLANNED ? "继续生成" : "生成 / 查看进度";
        display.setItem(25, button(new ItemStack(status.ready() ? Items.ENDER_PEARL : Items.IRON_PICKAXE), action,
                status.detail().getString(), "施工需要管理员/创造模式，完成后普通玩家可进入", "门旁祭炉需点亮；月蚀战斗沿用生成器自身规则"));
        display.setItem(26, button(new ItemStack(Items.BARRIER), "关闭菜单"));
        if (!gate.generatorInstance().isEmpty() && DungeonGenerators.find(gate.generatorId())
                .map(DungeonGenerator::supportsRegeneration).orElse(false)) {
            boolean confirming = source.getGameTime() < regenerateConfirmationUntil
                    && gate.generatorInstance().equals(regenerateConfirmationInstance);
            display.setItem(20, button(new ItemStack(confirming ? Items.TNT : Items.NETHER_STAR),
                    confirming ? "再次点击：确认重新生成" : "重新生成混沌地牢",
                    "清空旧地牢与战利品，重新抽取布局、登记入口旁队员", "地牢内包含离线玩家时拒绝；管理员/创造模式操作"));
        }
    }

    @Override public boolean stillValid(Player player) {
        return player instanceof ServerPlayer serverPlayer && DungeonGateGenerators.valid(serverPlayer, source, anchor);
    }

    @Override public void clicked(int slotId, int mouseButton, ClickType type, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) return;
        if (type != ClickType.PICKUP || mouseButton != 0) {
            sendAllDataToRemote();
            return;
        }
        if (!(source.getBlockEntity(anchor) instanceof DungeonGateBlockEntity gate)) return;
        try {
            if (slotId == 20 && !gate.generatorInstance().isEmpty() && DungeonGenerators.find(gate.generatorId())
                    .map(DungeonGenerator::supportsRegeneration).orElse(false)) {
                if (!DungeonGateGenerators.canConfigure(serverPlayer)) throw new IllegalStateException("重新生成需要管理员或创造模式");
                if (source.getGameTime() < regenerateConfirmationUntil
                        && gate.generatorInstance().equals(regenerateConfirmationInstance)) {
                    regenerateConfirmationUntil = 0;
                    DungeonGateGenerators.regenerate(serverPlayer, gate);
                } else {
                    regenerateConfirmationInstance = gate.generatorInstance();
                    regenerateConfirmationUntil = source.getGameTime() + 200;
                    serverPlayer.sendSystemMessage(Component.literal("重新生成会清空旧混沌地牢和战利品；10秒内再次点击确认。"));
                }
            } else if (slotId >= 0 && slotId < 20) {
                int index = page * 20 + slotId;
                if (index < providers.size()) DungeonGateGenerators.choose(serverPlayer, gate, providers.get(index).id());
            } else if (slotId == 21 && page > 0) {
                page--;
            } else if (slotId == 23 && (page + 1) * 20 < providers.size()) {
                page++;
            } else if (slotId == 24) {
                DungeonGateGenerators.reroll(serverPlayer, gate);
            } else if (slotId == 25) {
                if (DungeonGateGenerators.status(source, gate).ready()) {
                    serverPlayer.closeContainer();
                    DungeonGateTravel.use(serverPlayer, anchor, (DungeonGateBlock) gate.getBlockState().getBlock(), false);
                    return;
                }
                DungeonGenerator.Phase phase = DungeonGateGenerators.status(source, gate).phase();
                if (phase == DungeonGenerator.Phase.UNPLANNED || phase == DungeonGenerator.Phase.PLANNED
                        || phase == DungeonGenerator.Phase.PAUSED) DungeonGateGenerators.prepare(serverPlayer, gate);
                else serverPlayer.sendSystemMessage(DungeonGateGenerators.status(source, gate).detail());
            } else if (slotId == 26) {
                serverPlayer.closeContainer();
                return;
            }
        } catch (RuntimeException failure) {
            serverPlayer.sendSystemMessage(Component.literal(failure.getMessage() == null ? "门操作失败" : failure.getMessage()));
        }
        refresh();
        broadcastChanges();
        sendAllDataToRemote();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return false; }
    @Override public boolean canDragTo(Slot slot) { return false; }
    @Override public void broadcastChanges() {
        if (source != null && source.getGameTime() >= nextRefresh) {
            nextRefresh = source.getGameTime() + 20;
            refresh();
        }
        super.broadcastChanges();
    }
}
