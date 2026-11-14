package net.farmingtales.ftboss.transaction;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ExistingTransactionBoundary implements ForgeTransactionHost.Boundary {
    private final Set<ResourceLocation> verifiedItems;
    private final ExistingTransactionBoundary.ApprovedRules rules;

    public ExistingTransactionBoundary(Set<ResourceLocation> var1, ExistingTransactionBoundary.ApprovedRules var2) {
        this.verifiedItems = Set.copyOf(var1);
        this.rules = Objects.requireNonNull(var2);
    }

    @Override
    public boolean verifiedCustomItem(ItemStack var1) {
        return FourWeaponFoundationAdapter.read(var1).valid() || this.verifiedItems.contains(BuiltInRegistries.ITEM.getKey(var1.getItem()));
    }

    @Override
    public String statueIdentity(ServerPlayer var1, BlockPos var2) {
        BlockState var3 = var1.level().getBlockState(var2);
        if (var3.getBlock() instanceof CandyStatueBlock var4
            && var3.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
            && var3.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
            && var1.level().hasChunkAt(var2.above())) {
            BlockState var7 = var1.level().getBlockState(var2.above());
            if (var7.getBlock() == var3.getBlock()
                && var7.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && var7.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                ResourceLocation var6 = BuiltInRegistries.BLOCK.getKey(var3.getBlock());
                return var6 == null ? null : var6 + ":" + var4.ownTint();
            }

            return null;
        }

        return null;
    }

    private int tint(ServerPlayer var1, BlockPos var2) {
        return ((CandyStatueBlock)var1.level().getBlockState(var2).getBlock()).ownTint();
    }

    @Override
    public ForgeTransactionHost.Policy policy(ServerPlayer var1, ItemStack var2, BlockPos var3) {
        String var4 = this.rules.fingerprint(var1, var2);
        if (var4 != null && !var4.isBlank() && FoodWeaponEvolution.isEvolutionCandidate(var2)) {
            EnumSet<Operation> var5 = EnumSet.noneOf(Operation.class);
            int var6 = FoodWeaponEvolution.evolutionLevel(var2);
            if (var6 >= 0 && var6 <= 4) {
                if (FourWeaponFoundationAdapter.read(var2).valid()) {
                    var5.add(Operation.BUILD_CELL);
                    var5.add(Operation.REMOVE_CELL);
                    var5.add(Operation.OPEN_CONTAINER);

                    for (Operation var10 : Operation.values()) {
                        if (FourWeaponTransactions.menuOperation(var10)) {
                            var5.add(var10);
                        }
                    }
                }

                if (!FoodWeaponLifecycle.isBroken(var2)) {
                    if (!FoodWeaponSkillLoadout.skills(var2).isEmpty()) {
                        var5.add(Operation.SELECT_SKILL);
                    }

                    if (var6 < 4 && this.rules.progressionPreservesContainer(var2)) {
                        var5.add(Operation.EVOLVE);
                    }
                }

                FoodWeaponRepair.View var11 = FoodWeaponRepair.view(var2, this.tint(var1, var3));
                if (var11.eligible() && var11.correctStatue()) {
                    if (!var11.active()) {
                        var5.add(Operation.START_REPAIR);
                    } else if (!var11.ready()) {
                        var5.add(Operation.SUBMIT_REPAIR);
                    } else {
                        var5.add(Operation.CLAIM_REPAIR);
                    }
                }

                var5.removeIf(var2x -> !this.rules.allows(var2, var2x));
                if (var1.containerMenu instanceof SwordCavityMenu) {
                    var5.removeIf(var0 -> !FourWeaponTransactions.menuOperation(var0) && var0 != Operation.REMOVE_CELL);
                } else {
                    var5.removeIf(FourWeaponTransactions::menuOperation);
                }

                return new ForgeTransactionHost.Policy(
                    FourWeaponFoundationAdapter.applies(var2)
                        ? FourWeaponFoundationAdapter.revision(var2)
                        : (SwordCavityBuild.applies(var2) ? SwordCavityBuild.revision(var2) : 0),
                    FoodWeaponRepair.revision(var2),
                    var6,
                    var4,
                    var5
                );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean execute(ServerPlayer var1, ItemStack var2, BlockPos var3, Request var4) {
        ForgeTransactionHost.Policy var5 = this.policy(var1, var2, var3);
        if (var5 != null && var5.allowed().contains(var4.operation())) {
            int var6 = this.tint(var1, var3);

            return switch (var4.operation()) {
                case REPLACE_PART -> false;
                case SELECT_SKILL -> FoodWeaponSkillLoadout.select(var1, var2, var4.value(), false);
                case EVOLVE -> FoodWeaponProgression.evolve(var1, var2, var6, var4.value());
                case START_REPAIR -> FoodWeaponRepair.start(var1, var2, var6);
                case SUBMIT_REPAIR -> FoodWeaponRepair.submit(var1, var2, var6, var4.value());
                case CLAIM_REPAIR -> {
                    ItemStack var10 = var2.copy();
                    yield (
                            !FourWeaponFoundationAdapter.applies(var2)
                                || FourWeaponFoundationAdapter.resetWearAfterRepair(var10) && FourWeaponFoundationAdapter.read(var10).valid()
                        )
                        && FoodWeaponRepair.claim(var1, var2, var6);
                }
                case BUILD_CELL -> FourWeaponFoundationAdapter.replace(var1, var2, var4.part(), var4.value(), var4.expected().cavity());
                case REMOVE_CELL -> var1.containerMenu instanceof SwordCavityMenu var9
                ? var9.transactionRemoveReference(var1, var4)
                : FourWeaponFoundationAdapter.replace(var1, var2, var4.part(), -1, var4.expected().cavity());
                case OPEN_CONTAINER -> FourWeaponTransactionRuntime.openContainer(var1, var4);
                case MENU_PICKUP, MENU_QUICK_MOVE, MENU_SWAP, MENU_QUICK_CRAFT, MENU_PICKUP_ALL -> {
                    if (var1.containerMenu instanceof SwordCavityMenu var7 && var7.transactionClick(var1, var4)) {
                        yield true;
                    }

                    yield false;
                }
            };
        } else {
            return false;
        }
    }

    public interface ApprovedRules {
        String fingerprint(ServerPlayer var1, ItemStack var2);

        boolean allows(ItemStack var1, Operation var2);

        boolean progressionPreservesContainer(ItemStack var1);
    }
}
