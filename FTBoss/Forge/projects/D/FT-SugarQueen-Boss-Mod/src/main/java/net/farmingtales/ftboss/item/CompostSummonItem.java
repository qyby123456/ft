package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Rainbow Compost Bucket (2026-09-12): summons the Compost Superman in front of the player. */
public class CompostSummonItem extends Item {

    public CompostSummonItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 —— 不满足就中止, 且**不消耗**召唤物 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "堆肥超人")) {
                return InteractionResultHolder.fail(stack);
            }
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 4);
            CompostBossEntity boss = FTEntities.COMPOST_BOSS.get().create(sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] Compost Superman create() returned null");
                return InteractionResultHolder.fail(stack);
            }
            if (!net.farmingtales.ftboss.util.BossCollision.place(sl, boss,
                    new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot() + 180.0f)
                    || !sl.addFreshEntity(boss)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "堆肥超人");
                return InteractionResultHolder.fail(stack);
            }
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.6f, 0.6f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            FTBossMod.LOGGER.info("[FTBoss] Compost Superman summoned at {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
