package net.farmingtales.ftboss.transaction;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.NativeStackComponents;
import net.farmingtales.ftboss.item.FoodWeaponData;
import net.minecraft.core.HolderLookup;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Host;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Mutation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.State;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ForgeTransactionHost implements Host {
    private final Function<UUID, ServerPlayer> players;
    private final ForgeTransactionHost.Boundary boundary;
    private final Map<UUID, Long> lastRequest = new HashMap<>();

    public ForgeTransactionHost(Function<UUID, ServerPlayer> var1, ForgeTransactionHost.Boundary var2) {
        this.players = Objects.requireNonNull(var1);
        this.boundary = Objects.requireNonNull(var2);
    }

    @Override
    public State read(UUID var1, Binding var2) {
        ServerPlayer var3 = this.players.apply(var1);
        if (var3 == null
            || var3.isRemoved()
            || !var3.isAlive()
            || var3.isSpectator()
            || var3.isSleeping()
            || var3.isUsingItem()
            || var3.containerMenu != var3.inventoryMenu && (!(var3.containerMenu instanceof SwordCavityMenu var4) || !var4.transactionMatches(var2))
            || !var3.getUUID().equals(var1)
            || !var2.owner().equals(var1)
            || !var3.level().dimension().location().toString().equals(var2.dimension())) {
            return null;
        } else if (var2.hand() == 0 && var3.getInventory().selected != var2.inventorySlot()) {
            return null;
        } else {
            ItemStack var10 = var3.getItemInHand(var2.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (!var10.isEmpty() && var10.getCount() == 1 && this.verified(var10)) {
                ResourceLocation var11 = BuiltInRegistries.ITEM.getKey(var10.getItem());
                CompoundTag var6 = FoodWeaponData.read(var10, "FTBossWeaponLifecycle");
                if (var11 != null
                    && var11.toString().equals(var2.item())
                    && var6 != null
                    && var6.hasUUID("Weapon")
                    && var2.weapon().equals(var6.getUUID("Weapon"))) {
                    for (int var7 = 0; var7 < var3.getInventory().getContainerSize(); var7++) {
                        ItemStack var8 = var3.getInventory().getItem(var7);
                        CompoundTag var9 = FoodWeaponData.read(var8, "FTBossWeaponLifecycle");
                        if (var8 != var10 && var9 != null && var9.hasUUID("Weapon") && var2.weapon().equals(var9.getUUID("Weapon"))) {
                            return null;
                        }
                    }

                    BlockPos var12 = BlockPos.of(var2.statue());
                    if (var3.level().hasChunkAt(var12)
                        && var3.level().getWorldBorder().isWithinBounds(var12)
                        && !(var3.distanceToSqr((double)var12.getX() + 0.5, (double)var12.getY() + 1.0, (double)var12.getZ() + 0.5) > 64.0)) {
                        String var13 = this.boundary.statueIdentity(var3, var12);
                        if (var13 != null && var13.equals(var2.statueIdentity())) {
                            ForgeTransactionHost.Policy var14 = this.boundary.policy(var3, var10, var12);
                            return var14 == null
                                ? null
                                : new State(
                                    var2,
                                    var3,
                                    var10,
                                    var14.cavityRevision(),
                                    var14.repairRevision(),
                                    var14.evolutionLevel(),
                                    fingerprint(var10, var3.registryAccess()),
                                    var14.rulesFingerprint(),
                                    var14.allowed()
                                );
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private boolean verified(ItemStack var1) {
        return var1.getItem() instanceof BowItem
            || var1.getItem() instanceof CrossbowItem
            || var1.getItem() instanceof AxeItem
            || var1.getItem() instanceof TridentItem
            || this.boundary.verifiedCustomItem(var1);
    }

    @Override
    public Mutation prepare(UUID var1, final Request var2, State var3) {
        final ServerPlayer var4 = this.players.apply(var1);
        State var5 = this.read(var1, var2.binding());
        if (var4 != null
            && var5 != null
            && var5.source() == var3.source()
            && var5.principal() == var3.principal()
            && var5.fingerprint().equals(var3.fingerprint())) {
            long var6 = var4.serverLevel().getGameTime();
            Long var8 = this.lastRequest.get(var1);
            if (FourWeaponTransactions.menuOperation(var2.operation()) || var8 == null || var6 >= var8 && var6 - var8 >= 4L) {
                this.lastRequest.put(var1, var6);
                final ItemStack var9 = (ItemStack)var5.source();
                if (var2.operation() == Operation.REPLACE_PART || var2.operation() == Operation.EVOLVE || var2.operation() == Operation.BUILD_CELL) {
                    ItemStack var10 = var4.getInventory().getItem(var2.value());
                    if (var10.isEmpty() || var10 == var9 || !fingerprint(var10, var4.registryAccess()).equals(var2.offeringFingerprint())) {
                        return null;
                    }
                }

                final ArrayList var16 = new ArrayList();

                for (int var11 = 0; var11 < var4.getInventory().getContainerSize(); var11++) {
                    var16.add(var4.getInventory().getItem(var11).copy());
                }

                final ItemStack var17 = var4.containerMenu.getCarried().copy();
                final NativeStackComponents.Snapshot var12 = NativeStackComponents.capture(var9);
                final AbstractContainerMenu var13 = var4.containerMenu;
                final List var14 = var13 instanceof SwordCavityMenu var15 ? var15.transactionContents() : null;
                final BlockPos var18 = BlockPos.of(var2.binding().statue());
                return StatueRepairMutation.wrap(new Mutation() {
                    @Override
                    public boolean commit() {
                        return ForgeTransactionHost.this.boundary.execute(var4, var9, var18, var2);
                    }

                    @Override
                    public void rollback() {
                        var12.restore(var9);

                        for (int var1 = 0; var1 < var16.size(); var1++) {
                            if (var1 != var2.binding().inventorySlot()) {
                                var4.getInventory().setItem(var1, ((ItemStack)var16.get(var1)).copy());
                            }
                        }

                        var13.setCarried(var17.copy());
                        if (var14 != null && var13 instanceof SwordCavityMenu var2x) {
                            var2x.transactionRestore(var14);
                        }

                        var4.getInventory().setChanged();
                    }
                }, var4, var9, var2.operation(), var2.value(), var2.binding().inventorySlot(), var18, var2);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String fingerprint(ItemStack stack, HolderLookup.Provider registries) {
        return NativeStackComponents.fingerprint(stack, registries);
    }

    public static String fingerprint(Tag tag) {
        return NativeStackComponents.fingerprint(tag);
    }

    public interface Boundary {
        boolean verifiedCustomItem(ItemStack var1);

        String statueIdentity(ServerPlayer var1, BlockPos var2);

        ForgeTransactionHost.Policy policy(ServerPlayer var1, ItemStack var2, BlockPos var3);

        boolean execute(ServerPlayer var1, ItemStack var2, BlockPos var3, Request var4);
    }

    public static record Policy(int cavityRevision, int repairRevision, int evolutionLevel, String rulesFingerprint, Set<Operation> allowed) {
        public Policy(int cavityRevision, int repairRevision, int evolutionLevel, String rulesFingerprint, Set<Operation> allowed) {
            allowed = Set.copyOf(allowed);
            this.cavityRevision = cavityRevision;
            this.repairRevision = repairRevision;
            this.evolutionLevel = evolutionLevel;
            this.rulesFingerprint = rulesFingerprint;
            this.allowed = allowed;
        }
    }
}
