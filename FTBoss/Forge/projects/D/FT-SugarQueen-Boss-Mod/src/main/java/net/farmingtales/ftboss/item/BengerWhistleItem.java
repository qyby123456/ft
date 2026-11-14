package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.BengerEntity;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 【牧羊哨】—— 召唤边牧犬王 <b>benger</b>（2026-09-14 v127 新 Boss 的召唤物）。
 *
 * <p>与其它 Boss 的召唤物一个套路：只能**主世界**召唤（{@link net.farmingtales.ftboss.boss.SummonGate}），
 * 在玩家面前 4 格生成、吹哨音效、消耗 1 个（创造模式不消耗）。
 * 配方见 {@code data/ftboss/recipes/benger_whistle.json}（八角 = 八件与"犬/骨"有关的高级材料，中心金桶）。
 */
public class BengerWhistleItem extends Item {

    public BengerWhistleItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 —— 不满足就中止，且**不消耗**召唤物 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "边牧犬王 benger")) {
                return InteractionResultHolder.fail(stack);
            }
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 4);
            BengerEntity boss = FTEntities.BENGER.get().create(sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] benger create() returned null");
                return InteractionResultHolder.fail(stack);
            }
            if (!net.farmingtales.ftboss.util.BossCollision.place(sl, boss,
                    new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot() + 180.0f)
                    || !sl.addFreshEntity(boss)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "边牧犬王 benger");
                return InteractionResultHolder.fail(stack);
            }
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.WOLF_HOWL, SoundSource.HOSTILE, 1.8f, 0.7f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            FTBossMod.LOGGER.info("[FTBoss] benger 召唤于 {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<net.minecraft.network.chat.Component> tip, TooltipFlag flag) {
        tip.add(net.minecraft.network.chat.Component.literal("§7边牧犬王的召唤物：§f右键在主世界召唤 benger"));
        tip.add(net.minecraft.network.chat.Component.literal("§8它嘴里叼着一根打狗棒 —— 击败它就能拿走"));
    }
}
