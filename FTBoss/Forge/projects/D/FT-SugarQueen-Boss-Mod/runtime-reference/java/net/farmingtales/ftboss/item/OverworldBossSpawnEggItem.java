/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.dispenser.DispenseItemBehavior
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.common.ForgeSpawnEggItem
 */
package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.farmingtales.ftboss.boss.SummonGate;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

public final class OverworldBossSpawnEggItem
extends ForgeSpawnEggItem {
    public OverworldBossSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primary, int secondary, Item.Properties properties) {
        super(type, primary, secondary, properties);
    }

    protected DispenseItemBehavior createDispenseBehavior() {
        DispenseItemBehavior original = super.createDispenseBehavior();
        return (source, stack) -> SummonGate.allow((Level)source.m_7727_(), null, "Boss \u5237\u602a\u86cb") ? original.m_6115_(source, stack) : stack;
    }

    public InteractionResult m_6225_(UseOnContext context) {
        if (!SummonGate.allow(context.m_43725_(), context.m_43723_(), "Boss \u5237\u602a\u86cb")) {
            return InteractionResult.FAIL;
        }
        return super.m_6225_(context);
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (!SummonGate.allow(level, player, "Boss \u5237\u602a\u86cb")) {
            return InteractionResultHolder.m_19100_((Object)player.m_21120_(hand));
        }
        return super.m_7203_(level, player, hand);
    }
}

