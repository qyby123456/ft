package net.farmingtales.ftdungeonloot;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class LootStandEntity extends BlockEntity {
    private final UUID instance = UUID.randomUUID();
    private ResourceLocation profileId = new ResourceLocation(FTDungeonLoot.ID, "unbound");
    private ResourceLocation currencyId = new ResourceLocation("minecraft", "air");
    private UUID run;
    private ItemStack reward = ItemStack.EMPTY;
    private int price;
    private boolean sold;
    private long revision;
    private long seed = UUID.randomUUID().getLeastSignificantBits();

    public LootStandEntity(BlockPos position, BlockState state) { super(FTDungeonLoot.STAND_ENTITY.get(), position, state); }
    public ResourceLocation profileId() { return profileId; }
    public ResourceLocation currencyId() { return currencyId; }
    public UUID instance() { return instance; }
    public UUID run() { return run; }
    public long revision() { return revision; }
    public ItemStack reward() { return reward.copy(); }
    public int price() { return price; }
    public boolean sold() { return sold; }

    void configure(ResourceLocation profile) {
        requireServer();
        profileId = profile;
        run = null;
        reward = ItemStack.EMPTY;
        price = 0;
        sold = false;
        revision++;
        ensureOffer();
        sync();
    }

    boolean ensureOffer() {
        requireServer();
        ServerLevel serverLevel = (ServerLevel) level;
        LootProfile profile = LootProfiles.find(profileId).orElse(null);
        if (profile == null || !profile.dimension().equals(level.dimension().location()) || profile.currencyItem() == null) return false;
        UUID current = DungeonRounds.current(serverLevel, profile);
        if (current == null) return false;
        if (!current.equals(run)) {
            var candidates = LootRoller.roll(serverLevel, worldPosition, profile, seed ^ current.getMostSignificantBits() ^ current.getLeastSignificantBits())
                    .stream().filter(stack -> profile.price(stack) > 0).toList();
            var random = net.minecraft.util.RandomSource.create(seed ^ current.getLeastSignificantBits());
            reward = candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(random.nextInt(candidates.size())).copy();
            price = profile.price(reward);
            currencyId = profile.currency();
            run = current;
            sold = false;
            revision++;
            sync();
        }
        return profile.currency().equals(currencyId) && !reward.isEmpty() && price > 0;
    }

    void purchase() {
        requireServer();
        if (sold || reward.isEmpty()) throw new IllegalStateException("No remaining loot stock");
        sold = true;
        revision++;
        sync();
    }

    private void requireServer() {
        if (!(level instanceof ServerLevel serverLevel) || !serverLevel.getServer().isSameThread()) {
            throw new IllegalStateException("Loot stand mutation requires its server thread");
        }
    }

    private void sync() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) ensureOffer();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Profile", profileId.toString());
        tag.putString("Currency", currencyId.toString());
        tag.put("Reward", reward.save(new CompoundTag()));
        tag.putInt("Price", price);
        tag.putBoolean("Sold", sold);
        tag.putLong("Revision", revision);
        tag.putLong("Seed", seed);
        if (run != null) tag.putUUID("Run", run);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("Profile"));
        profileId = parsed == null ? new ResourceLocation(FTDungeonLoot.ID, "unbound") : parsed;
        parsed = ResourceLocation.tryParse(tag.getString("Currency"));
        currencyId = parsed == null ? new ResourceLocation("minecraft", "air") : parsed;
        reward = ItemStack.of(tag.getCompound("Reward"));
        price = Math.max(0, Math.min(1000000, tag.getInt("Price")));
        sold = tag.getBoolean("Sold");
        revision = tag.getLong("Revision");
        if (tag.contains("Seed")) seed = tag.getLong("Seed");
        run = tag.hasUUID("Run") ? tag.getUUID("Run") : null;
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public boolean onlyOpCanSetNbt() { return true; }
    @Override
    public AABB getRenderBoundingBox() { return new AABB(worldPosition).inflate(.15, .8, .15); }
}
