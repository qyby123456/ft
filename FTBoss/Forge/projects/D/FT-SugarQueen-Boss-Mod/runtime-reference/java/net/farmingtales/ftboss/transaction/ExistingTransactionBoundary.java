/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.registries.ForgeRegistries
 */
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
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

public final class ExistingTransactionBoundary
implements ForgeTransactionHost.Boundary {
    private final Set<ResourceLocation> verifiedItems;
    private final ApprovedRules rules;

    public ExistingTransactionBoundary(Set<ResourceLocation> set, ApprovedRules approvedRules) {
        this.verifiedItems = Set.copyOf(set);
        this.rules = Objects.requireNonNull(approvedRules);
    }

    @Override
    public boolean verifiedCustomItem(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.read(itemStack).valid() || this.verifiedItems.contains(ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_()));
    }

    @Override
    public String statueIdentity(ServerPlayer serverPlayer, BlockPos blockPos) {
        CandyStatueBlock candyStatueBlock;
        Block block;
        BlockState blockState;
        block5: {
            block4: {
                blockState = serverPlayer.m_9236_().m_8055_(blockPos);
                block = blockState.m_60734_();
                if (!(block instanceof CandyStatueBlock)) break block4;
                candyStatueBlock = (CandyStatueBlock)block;
                if (blockState.m_61138_((Property)BlockStateProperties.f_61401_) && blockState.m_61143_((Property)BlockStateProperties.f_61401_) == DoubleBlockHalf.LOWER && serverPlayer.m_9236_().m_46805_(blockPos.m_7494_())) break block5;
            }
            return null;
        }
        block = serverPlayer.m_9236_().m_8055_(blockPos.m_7494_());
        if (block.m_60734_() != blockState.m_60734_() || !block.m_61138_((Property)BlockStateProperties.f_61401_) || block.m_61143_((Property)BlockStateProperties.f_61401_) != DoubleBlockHalf.UPPER) {
            return null;
        }
        ResourceLocation resourceLocation = ForgeRegistries.BLOCKS.getKey((Object)blockState.m_60734_());
        return resourceLocation == null ? null : resourceLocation + ":" + candyStatueBlock.ownTint();
    }

    private int tint(ServerPlayer serverPlayer, BlockPos blockPos) {
        return ((CandyStatueBlock)serverPlayer.m_9236_().m_8055_(blockPos).m_60734_()).ownTint();
    }

    @Override
    public ForgeTransactionHost.Policy policy(ServerPlayer serverPlayer, ItemStack itemStack, BlockPos blockPos) {
        FourWeaponTransactions.Operation[] operationArray;
        String string = this.rules.fingerprint(serverPlayer, itemStack);
        if (string == null || string.isBlank() || !FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return null;
        }
        EnumSet<FourWeaponTransactions.Operation> enumSet = EnumSet.noneOf(FourWeaponTransactions.Operation.class);
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        if (n < 0 || n > 4) {
            return null;
        }
        if (FourWeaponFoundationAdapter.read(itemStack).valid()) {
            enumSet.add(FourWeaponTransactions.Operation.BUILD_CELL);
            enumSet.add(FourWeaponTransactions.Operation.REMOVE_CELL);
            enumSet.add(FourWeaponTransactions.Operation.OPEN_CONTAINER);
            operationArray = FourWeaponTransactions.Operation.values();
            int n2 = operationArray.length;
            for (int i = 0; i < n2; ++i) {
                FourWeaponTransactions.Operation operation2 = operationArray[i];
                if (!FourWeaponTransactions.menuOperation(operation2)) continue;
                enumSet.add(operation2);
            }
        }
        if (!FoodWeaponLifecycle.isBroken(itemStack)) {
            if (!FoodWeaponSkillLoadout.skills(itemStack).isEmpty()) {
                enumSet.add(FourWeaponTransactions.Operation.SELECT_SKILL);
            }
            if (n < 4 && this.rules.progressionPreservesContainer(itemStack)) {
                enumSet.add(FourWeaponTransactions.Operation.EVOLVE);
            }
        }
        if ((operationArray = FoodWeaponRepair.view(itemStack, this.tint(serverPlayer, blockPos))).eligible() && operationArray.correctStatue()) {
            if (!operationArray.active()) {
                enumSet.add(FourWeaponTransactions.Operation.START_REPAIR);
            } else if (!operationArray.ready()) {
                enumSet.add(FourWeaponTransactions.Operation.SUBMIT_REPAIR);
            } else {
                enumSet.add(FourWeaponTransactions.Operation.CLAIM_REPAIR);
            }
        }
        enumSet.removeIf(operation -> !this.rules.allows(itemStack, (FourWeaponTransactions.Operation)((Object)operation)));
        if (serverPlayer.f_36096_ instanceof SwordCavityMenu) {
            enumSet.removeIf(operation -> !FourWeaponTransactions.menuOperation(operation) && operation != FourWeaponTransactions.Operation.REMOVE_CELL);
        } else {
            enumSet.removeIf(FourWeaponTransactions::menuOperation);
        }
        return new ForgeTransactionHost.Policy(FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.revision(itemStack) : (SwordCavityBuild.applies(itemStack) ? SwordCavityBuild.revision(itemStack) : 0), FoodWeaponRepair.revision(itemStack), n, string, enumSet);
    }

    @Override
    public boolean execute(ServerPlayer serverPlayer, ItemStack itemStack, BlockPos blockPos, FourWeaponTransactions.Request request) {
        ForgeTransactionHost.Policy policy = this.policy(serverPlayer, itemStack, blockPos);
        if (policy == null || !policy.allowed().contains((Object)request.operation())) {
            return false;
        }
        int n = this.tint(serverPlayer, blockPos);
        return switch (request.operation()) {
            default -> throw new IncompatibleClassChangeError();
            case FourWeaponTransactions.Operation.REPLACE_PART -> false;
            case FourWeaponTransactions.Operation.SELECT_SKILL -> FoodWeaponSkillLoadout.select(serverPlayer, itemStack, request.value(), false);
            case FourWeaponTransactions.Operation.EVOLVE -> FoodWeaponProgression.evolve(serverPlayer, itemStack, n, request.value());
            case FourWeaponTransactions.Operation.START_REPAIR -> FoodWeaponRepair.start(serverPlayer, itemStack, n);
            case FourWeaponTransactions.Operation.SUBMIT_REPAIR -> FoodWeaponRepair.submit(serverPlayer, itemStack, n, request.value());
            case FourWeaponTransactions.Operation.CLAIM_REPAIR -> {
                ItemStack var7_7 = itemStack.m_41777_();
                if ((!FourWeaponFoundationAdapter.applies(itemStack) || FourWeaponFoundationAdapter.resetWearAfterRepair(var7_7) && FourWeaponFoundationAdapter.read(var7_7).valid()) && FoodWeaponRepair.claim(serverPlayer, itemStack, n)) {
                    yield true;
                }
                yield false;
            }
            case FourWeaponTransactions.Operation.BUILD_CELL -> FourWeaponFoundationAdapter.replace(serverPlayer, itemStack, request.part(), request.value(), request.expected().cavity());
            case FourWeaponTransactions.Operation.REMOVE_CELL -> {
                AbstractContainerMenu var8_10 = serverPlayer.f_36096_;
                if (var8_10 instanceof SwordCavityMenu) {
                    SwordCavityMenu var7_8 = (SwordCavityMenu)var8_10;
                    yield var7_8.transactionRemoveReference(serverPlayer, request);
                }
                yield FourWeaponFoundationAdapter.replace(serverPlayer, itemStack, request.part(), -1, request.expected().cavity());
            }
            case FourWeaponTransactions.Operation.OPEN_CONTAINER -> FourWeaponTransactionRuntime.openContainer(serverPlayer, request);
            case FourWeaponTransactions.Operation.MENU_PICKUP, FourWeaponTransactions.Operation.MENU_QUICK_MOVE, FourWeaponTransactions.Operation.MENU_SWAP, FourWeaponTransactions.Operation.MENU_QUICK_CRAFT, FourWeaponTransactions.Operation.MENU_PICKUP_ALL -> {
                SwordCavityMenu var7_9;
                AbstractContainerMenu var8_11 = serverPlayer.f_36096_;
                yield var8_11 instanceof SwordCavityMenu && (var7_9 = (SwordCavityMenu)var8_11).transactionClick(serverPlayer, request);
            }
        };
    }

    public static interface ApprovedRules {
        public String fingerprint(ServerPlayer var1, ItemStack var2);

        public boolean allows(ItemStack var1, FourWeaponTransactions.Operation var2);

        public boolean progressionPreservesContainer(ItemStack var1);
    }
}

