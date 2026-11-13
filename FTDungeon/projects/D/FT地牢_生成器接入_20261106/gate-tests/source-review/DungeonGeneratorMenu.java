package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public final class DungeonGeneratorMenu extends AbstractContainerMenu {
    public record Entry(ResourceLocation id, Component name, Component description) {}
    private final BlockPos anchor;
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
    private final List<Entry> entries;
    private final DataSlot selected = DataSlot.standalone();
    private final DataSlot locked = DataSlot.standalone();

    private DungeonGeneratorMenu(int containerId, Inventory inventory, BlockPos anchor, List<Entry> entries) {
        super(FTDungeonMobs.DUNGEON_GENERATOR_MENU.get(), containerId);
        this.anchor = anchor.immutable();
        this.dimension = inventory.player.level().dimension();
        this.entries = List.copyOf(entries);
        addDataSlot(selected);
        addDataSlot(locked);
        if (inventory.player instanceof ServerPlayer player) {
            ResourceLocation current = ((DungeonGateBlockEntity) player.level().getBlockEntity(anchor)).generatorId();
            selected.set(-1);
            for (int index = 0; index < entries.size(); index++) {
                if (entries.get(index).id().equals(current)) { selected.set(index); }
            }
            locked.set(DungeonGateData.get(player.server).roundActive() ? 1 : 0);
        }
    }

    public DungeonGeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), readEntries(buffer));
    }

    private static List<Entry> readEntries(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 1 || count > 256) { throw new IllegalArgumentException("Invalid generator menu size"); }
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            entries.add(new Entry(buffer.readResourceLocation(), buffer.readComponent(), buffer.readComponent()));
        }
        return entries;
    }

    public static void open(ServerPlayer player, BlockPos anchor) {
        if (!(player.level().getBlockEntity(anchor) instanceof DungeonGateBlockEntity)) { return; }
        List<Entry> entries = DungeonGenerators.entries().stream()
                .map(provider -> new Entry(provider.id(), provider.name(), provider.description())).toList();
        NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, ignored) ->
                new DungeonGeneratorMenu(containerId, inventory, anchor, entries),
                Component.translatable("menu.ftdungeonmobs.generator.title")), buffer -> {
                    buffer.writeBlockPos(anchor);
                    buffer.writeVarInt(entries.size());
                    for (Entry entry : entries) {
                        buffer.writeResourceLocation(entry.id());
                        buffer.writeComponent(entry.name());
                        buffer.writeComponent(entry.description());
                    }
                });
    }

    public List<Entry> entries() { return entries; }
    public int selected() { return selected.get(); }
    public boolean locked() { return locked.get() != 0; }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)
                || index < 0 || index >= entries.size()) { return false; }
        DungeonGateData round = DungeonGateData.get(serverPlayer.server);
        locked.set(round.roundActive() ? 1 : 0);
        if (locked()) {
            DungeonGateInteraction.showLocked(serverPlayer);
            return false;
        }
        if (DungeonGenerators.get(entries.get(index).id()) == null) { return false; }
        DungeonGateBlockEntity gate = (DungeonGateBlockEntity) player.level().getBlockEntity(anchor);
        gate.setGeneratorId(entries.get(index).id());
        selected.set(index);
        serverPlayer.displayClientMessage(Component.translatable("message.ftdungeonmobs.gate.generator_selected",
                entries.get(index).name()), true);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.isSpectator() || !player.level().dimension().equals(dimension)
                || player.distanceToSqr(anchor.getX() + 0.5, anchor.getY() + 0.5, anchor.getZ() + 0.5) > 64
                || !player.level().mayInteract(player, anchor)) { return false; }
        var state = player.level().getBlockState(anchor);
        return state.getBlock() instanceof DungeonGateBlock gate && !gate.returning()
                && state.getValue(DungeonGateBlock.PART) == DungeonGateBlock.ANCHOR_PART
                && DungeonGateBlock.complete(player.level(), anchor, gate, state.getValue(DungeonGateBlock.FACING))
                && player.level().getBlockEntity(anchor) instanceof DungeonGateBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
