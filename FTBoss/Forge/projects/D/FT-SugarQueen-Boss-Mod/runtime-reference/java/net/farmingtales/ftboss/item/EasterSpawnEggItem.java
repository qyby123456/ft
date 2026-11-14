/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.common.util.FakePlayer
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

public class EasterSpawnEggItem
extends Item {
    private final int variant;
    private final int baseColor;
    private final int spotColor;

    public EasterSpawnEggItem(int variant, int baseColor, int spotColor) {
        super(new Item.Properties());
        this.variant = variant;
        this.baseColor = baseColor;
        this.spotColor = spotColor;
    }

    public int variant() {
        return this.variant;
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.baseColor : this.spotColor;
    }

    public InteractionResult m_6225_(UseOnContext ctx) {
        Level level = ctx.m_43725_();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel sl = (ServerLevel)level;
        if (ctx.m_43723_() == null || ctx.m_43723_() instanceof FakePlayer || !SummonGate.allow(level, ctx.m_43723_(), "\u9690\u85cf Boss")) {
            return InteractionResult.FAIL;
        }
        if (ManorFight.hasActive(sl) || HiddenBossIntro.isActive()) {
            ctx.m_43723_().m_5661_((Component)Component.m_237113_((String)"\u00a7c\u672c\u7ef4\u5ea6\u5df2\u6709\u5e84\u56ed\u6311\u6218\u6216\u5165\u573a\u6f14\u51fa\uff0c\u8bf7\u5148\u7ed3\u675f\u3002"), true);
            return InteractionResult.FAIL;
        }
        BlockPos clicked = ctx.m_8083_();
        BlockState state = level.m_8055_(clicked);
        Direction face = ctx.m_43719_();
        BlockPos at = state.m_60812_((BlockGetter)level, clicked).m_83281_() ? clicked : clicked.m_121945_(face);
        EasterBossEntity boss = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)sl);
        if (boss == null) {
            return InteractionResult.PASS;
        }
        boss.m_7678_((double)at.m_123341_() + 0.5, at.m_123342_(), (double)at.m_123343_() + 0.5, ctx.m_43723_() != null ? ctx.m_43723_().m_146908_() : 0.0f, 0.0f);
        boss.setVariant(this.variant);
        boss.setManorSummonerId(ctx.m_43723_().m_20148_());
        boss.m_21530_();
        if (!sl.m_7967_((Entity)boss)) {
            return InteractionResult.PASS;
        }
        if (this.variant == 9) {
            EasterBossEntity.supportSparrow(sl, boss);
            ManorFight.start(sl, boss.m_20182_(), EasterBossEntity.MANOR_VARIANTS[0], ctx.m_43723_().m_20148_());
        } else {
            ManorFight.adopt(sl, boss);
        }
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123759_, (double)at.m_123341_() + 0.5, (double)at.m_123342_() + 0.5, (double)at.m_123343_() + 0.5, 24, 0.4, 0.4, 0.4, 0.06);
        sl.m_5594_(null, at, SoundEvents.f_11852_, SoundSource.PLAYERS, 1.0f, 1.2f);
        String name = EasterBossEntity.NAMES[Math.max(0, Math.min(EasterBossEntity.NAMES.length - 1, this.variant))];
        if (ctx.m_43723_() != null) {
            ctx.m_43723_().m_5661_((Component)Component.m_237113_((String)("\u00a7d\u5df2\u53ec\u5524\u9690\u85cf Boss\uff1a\u00a7f" + name + " \u00a77(variant " + this.variant + ")")), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][\u5237\u602a\u86cb] \u53ec\u5524\u9690\u85cf Boss {} (variant={}) @{},{},{}", new Object[]{name, this.variant, at.m_123341_(), at.m_123342_(), at.m_123343_()});
        ctx.m_43722_().m_41774_(1);
        return InteractionResult.CONSUME;
    }
}

