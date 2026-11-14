package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
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

/** 【禁果】(2026-09-12): 苹果爵士的召唤物 —— 右键在面前 4 格召唤 Sir Honeycrisp。 */
public class ForbiddenAppleItem extends Item {

    public ForbiddenAppleItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 —— 不满足就直接中止, 且**不消耗**禁果 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "苹果爵士")) {
                return InteractionResultHolder.fail(stack);
            }
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 4);
            AppleBossEntity boss = FTEntities.APPLE_BOSS.get().create(sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] Apple Boss create() returned null");
                return InteractionResultHolder.fail(stack);
            }
            if (!net.farmingtales.ftboss.util.BossCollision.place(sl, boss,
                    new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot() + 180.0f)
                    || !sl.addFreshEntity(boss)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "苹果爵士");
                return InteractionResultHolder.fail(stack);
            }
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.6f, 0.75f);
            /* 2026-09-12 按用户要求"禁果要消耗": 生存模式必定消耗, 并打日志便于核对
               (创造模式按原版惯例不消耗; 若你在创造下测, 看到 creative=true 就是正常的) */
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!stack.isEmpty()) stack.setCount(0);   // 兜底: 极端情况下 shrink 没生效
                player.getInventory().setChanged();
            }
            FTBossMod.LOGGER.info("[FTBoss] forbidden apple used: creative={} remaining={}",
                    player.getAbilities().instabuild, stack.getCount());
            FTBossMod.LOGGER.info("[FTBoss] Apple Boss summoned at {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
