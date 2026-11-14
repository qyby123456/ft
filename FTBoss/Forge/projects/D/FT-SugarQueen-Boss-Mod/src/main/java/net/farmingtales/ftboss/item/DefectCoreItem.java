package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 故障机器人(召唤物, 2026-09-11 新增): 右键在视线前方召唤故障机器人。
 * <p>按用户要求"允许召唤同种多个 Boss" —— 这里**没有任何单例闸门**, 可以直接连续召唤多只。
 */
public class DefectCoreItem extends Item {

    public DefectCoreItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "故障机器人")) {
                return InteractionResultHolder.fail(stack);
            }
            try {
                Vec3 look = player.getLookAngle();
                Vec3 pos = player.position().add(look.x * 4.0, 0, look.z * 4.0);
                Entity raw = FTEntities.DEFECT_CORE.get().create(level);
                if (raw instanceof DefectCoreEntity core) {
                    if (level instanceof net.minecraft.server.level.ServerLevel server
                            && net.farmingtales.ftboss.util.BossCollision.place(server, core,
                            new Vec3(pos.x, player.getY() + 2.4, pos.z),
                            net.farmingtales.ftboss.util.BossCollision.SpawnMode.AIR, player.getYRot())
                            && level.addFreshEntity(core)) {
                        stack.shrink(1);
                        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_ACTIVATE,
                                SoundSource.HOSTILE, 1.4f, 0.8f);
                        player.displayClientMessage(Component.literal("§5故障机器人 §7已启动…"), true);
                        FTBossMod.LOGGER.info("[FTBoss] 故障机器人被召唤 @ {} {} {}",
                                String.format("%.1f", core.getX()), String.format("%.1f", core.getY()), String.format("%.1f", core.getZ()));
                    } else {
                        net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "故障机器人");
                    }
                } else {
                    player.displayClientMessage(Component.literal("§c故障机器人创建失败"), true);
                }
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss] 故障机器人召唤异常: {}", t.toString());
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7右键召唤 §d故障机器人§7(可同时召唤多只)"));
        tip.add(Component.literal("§8两阶段阈值: 70% / 35% · 死亡时核心超载爆炸"));
    }
}
