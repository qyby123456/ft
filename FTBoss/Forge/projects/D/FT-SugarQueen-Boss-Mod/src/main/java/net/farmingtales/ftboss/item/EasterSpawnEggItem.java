package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 【隐藏 Boss 刷怪蛋】(2026-09-14 v109 按用户要求"隐藏 Boss 的 10 个刷怪蛋随便做一下, 能召唤测试就行")。
 *
 * <p>庄园十人(彩蛋 Boss)都是**同一个实体** {@code ftboss:easter_boss} 的不同 {@code variant},
 * 所以刷怪蛋不能直接用原版 {@link net.minecraft.world.item.SpawnEggItem}
 * (它只会生成默认变体) —— 这里自己实现 {@link #useOn}, 在生成时把 {@link #variant} 写进去。
 *
 * <p>外观**零新增贴图**: 模型指向原版 {@code minecraft:item/template_spawn_egg}
 * (两层: {@code item/spawn_egg} 蛋壳 + {@code item/spawn_egg_overlay} 斑点),
 * 颜色由客户端 {@code FTClientSetup} 里注册的 {@code ItemColors} 提供 —— 见 {@link #getColor(int)}。
 *
 * <p>测试用细节: 生成后 {@code setPersistenceRequired()}(跑远也不会被刷掉) + 聊天栏提示变体名。
 */
public class EasterSpawnEggItem extends Item {

    /** 变体下标(见 {@link EasterBossEntity#MANOR_VARIANTS}) */
    private final int variant;
    /** 蛋壳底色 / 斑点色(tintIndex 0 / 1) */
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

    /** 原版刷怪蛋模型的两层染色: 0 = 蛋壳, 1 = 斑点(客户端注册见 FTClientSetup) */
    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.baseColor : this.spotColor;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;   // 客户端只回成功, 真正生成在服务端
        if (ctx.getPlayer() == null || ctx.getPlayer() instanceof net.minecraftforge.common.util.FakePlayer
                || !net.farmingtales.ftboss.boss.SummonGate.allow(level, ctx.getPlayer(), "隐藏 Boss")) return InteractionResult.FAIL;
        if (net.farmingtales.ftboss.boss.ManorFight.hasActive(sl) || net.farmingtales.ftboss.boss.HiddenBossIntro.isActive()) {
            ctx.getPlayer().displayClientMessage(Component.literal("§c本维度已有庄园挑战或入场演出，请先结束。"), true);
            return InteractionResult.FAIL;
        }

        BlockPos clicked = ctx.getClickedPos();
        BlockState state = level.getBlockState(clicked);
        Direction face = ctx.getClickedFace();
        BlockPos at = state.getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(face);

        EasterBossEntity boss = FTEntities.EASTER_BOSS.get().create(sl);
        if (boss == null) return InteractionResult.PASS;
        boss.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5,
                ctx.getPlayer() != null ? ctx.getPlayer().getYRot() : 0.0f, 0.0f);
        boss.setVariant(this.variant);
        boss.setManorSummonerId(ctx.getPlayer().getUUID());
        boss.setPersistenceRequired();                    // 测试用: 别因为玩家跑远就被刷掉
        if (!sl.addFreshEntity(boss)) return InteractionResult.PASS;
        if (this.variant == EasterBossEntity.SPARROW_VARIANT) {
            EasterBossEntity.supportSparrow(sl, boss);
            net.farmingtales.ftboss.boss.ManorFight.start(sl, boss.position(), EasterBossEntity.MANOR_VARIANTS[0], ctx.getPlayer().getUUID());
        } else {
            net.farmingtales.ftboss.boss.ManorFight.adopt(sl, boss);
        }

        sl.sendParticles(ParticleTypes.POOF, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5,
                24, 0.4, 0.4, 0.4, 0.06);
        sl.playSound(null, at, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);
        String name = EasterBossEntity.NAMES[Math.max(0, Math.min(EasterBossEntity.NAMES.length - 1, this.variant))];
        if (ctx.getPlayer() != null) {
            ctx.getPlayer().displayClientMessage(
                    Component.literal("§d已召唤隐藏 Boss：§f" + name + " §7(variant " + this.variant + ")"), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][刷怪蛋] 召唤隐藏 Boss {} (variant={}) @{},{},{}",
                name, this.variant, at.getX(), at.getY(), at.getZ());

        ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
