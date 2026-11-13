package net.farmingtales.ftdungeontrading;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftdungeontrading.api.TradeCost;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TradeTableBlockEntity extends BlockEntity {
    private final UUID instanceId = UUID.randomUUID();
    private ResourceLocation offerId = new ResourceLocation(FTDungeonTrading.ID, "unbound");
    private int remainingStock;
    private long changeVersion;
    private long observedCatalog = -1;
    private Component displayTitle = Component.empty();
    private List<ItemStack> displayCosts = List.of();
    private List<ItemStack> displayRewards = List.of();

    public TradeTableBlockEntity(BlockPos position, BlockState state) {
        super(FTDungeonTrading.TABLE_ENTITY.get(), position, state);
    }

    public UUID instanceId() { return instanceId; }
    public ResourceLocation offerId() { return offerId; }
    public int remainingStock() { return remainingStock; }
    public long changeVersion() { return changeVersion; }
    public Component displayTitle() { return displayTitle.copy(); }
    public List<ItemStack> displayCosts() { return displayCosts.stream().map(ItemStack::copy).toList(); }
    public List<ItemStack> displayRewards() { return displayRewards.stream().map(ItemStack::copy).toList(); }

    void configure(ResourceLocation offer, int stock) {
        requireServerThread();
        if (stock < -1 || stock > 1000000) throw new IllegalArgumentException("Stock must be -1..1000000");
        offerId = offer;
        remainingStock = stock;
        changeVersion++;
        observedCatalog = -1;
        refreshDisplay();
    }

    void consumeStock() {
        requireServerThread();
        if (remainingStock == 0) throw new IllegalStateException("Cannot consume sold-out stock");
        if (remainingStock > 0) remainingStock--;
        changeVersion++;
        synchronize();
    }

    public void refreshDisplay() {
        requireServerThread();
        if (observedCatalog == TradeOfferRepository.revision()) return;
        observedCatalog = TradeOfferRepository.revision();
        var offer = TradeOfferRepository.find(offerId);
        displayTitle = offer.map(value -> value.title()).orElse(Component.translatable("message.ftdungeontrading.missing_offer"));
        displayCosts = offer.map(value -> value.costs().stream().limit(3).map(TradeCost::displayStack).toList()).orElse(List.of());
        displayRewards = offer.map(value -> value.rewards().stream().limit(3).map(stack -> stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()))).toList()).orElse(List.of());
        synchronize();
    }

    private void synchronize() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void requireServerThread() {
        if (level == null || level.isClientSide || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Trading table mutation requires its server thread");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("OfferId", offerId.toString());
        tag.putInt("RemainingStock", remainingStock);
        tag.putLong("ChangeVersion", changeVersion);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("OfferId"));
        if (parsed != null) offerId = parsed;
        remainingStock = tag.contains("RemainingStock", Tag.TAG_INT) ? Math.max(-1, Math.min(1000000, tag.getInt("RemainingStock"))) : 0;
        changeVersion = tag.getLong("ChangeVersion");
        observedCatalog = -1;
        if (tag.contains("DisplayTitle")) {
            try {
                Component title = Component.Serializer.fromJson(tag.getString("DisplayTitle"));
                displayTitle = title == null ? Component.empty() : title;
            } catch (RuntimeException exception) { displayTitle = Component.empty(); }
            displayCosts = readStacks(tag.getList("DisplayCosts", Tag.TAG_COMPOUND));
            displayRewards = readStacks(tag.getList("DisplayRewards", Tag.TAG_COMPOUND));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putString("DisplayTitle", Component.Serializer.toJson(displayTitle));
        tag.put("DisplayCosts", writeStacks(displayCosts));
        tag.put("DisplayRewards", writeStacks(displayRewards));
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public boolean onlyOpCanSetNbt() { return true; }

    private static ListTag writeStacks(List<ItemStack> stacks) {
        ListTag result = new ListTag();
        stacks.stream().limit(3).forEach(stack -> result.add(stack.save(new CompoundTag())));
        return result;
    }

    private static List<ItemStack> readStacks(ListTag tag) {
        return tag.stream().limit(3).map(value -> ItemStack.of((CompoundTag) value)).toList();
    }
}
