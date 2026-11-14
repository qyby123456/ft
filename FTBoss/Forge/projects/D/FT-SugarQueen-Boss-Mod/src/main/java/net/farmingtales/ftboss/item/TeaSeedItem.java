package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 茶树种子(占位召唤物): 右键在面前长出一棵 茶树古树(临时, 待专属模型接入) */
public class TeaSeedItem extends Item {
    public TeaSeedItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "沙耶")) {
                return InteractionResultHolder.fail(stack);
            }
            net.minecraft.server.level.ServerPlayer serverPlayer = player instanceof
                    net.minecraft.server.level.ServerPlayer sp ? sp : null;
            boolean finale = player.isShiftKeyDown() || serverPlayer != null
                    && net.farmingtales.ftboss.boss.BossChallengePreference.selected(serverPlayer)
                    == net.farmingtales.ftboss.boss.ManorDifficulty.IMPOSSIBLE;
            if (finale && (serverPlayer == null
                    || !net.farmingtales.ftboss.boss.ManorDifficulty.unlocked(serverPlayer))) {
                player.displayClientMessage(Component.literal("§c终结难度尚未解锁：请先完整通关普通庄园挑战。"), true);
                return InteractionResultHolder.fail(stack);
            }
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position().add(look.x * 3, 0, look.z * 3);
            TeaTreeBossEntity tree = FTEntities.TEA_TREE.get().create(level);
            if (tree == null) {
                player.displayClientMessage(Component.literal("§c此处无法长出茶树"), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)
                    || !net.farmingtales.ftboss.util.BossCollision.place(server, tree,
                    new Vec3(pos.x, player.getY(), pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot())) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, finale ? "终结难度沙耶" : "沙耶");
                return InteractionResultHolder.fail(stack);
            }
            net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.prepare(tree, finale);
            tree.setCustomName(Component.translatable("boss.ftboss.saya").withStyle(ChatFormatting.BOLD)
                    .append(finale ? Component.literal(" §4[终结]") : Component.empty()));
            tree.setCustomNameVisible(true);
            tree.setHealth(tree.getMaxHealth());
            tree.setPersistenceRequired();
            if (!level.addFreshEntity(tree)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, finale ? "终结难度沙耶" : "沙耶");
                return InteractionResultHolder.fail(stack);
            }
            stack.shrink(1);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.CROP_PLANTED,
                    SoundSource.HOSTILE, 1.0f, 0.8f);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7茶系Boss召唤物：§f右键长出 沙耶"));
        tip.add(Component.literal("§4石像预设为终结难度，或潜行右键：召唤终结难度沙耶"));
        tip.add(Component.literal("§8终结难度首次战利品包含饰品【银之钥】"));
        tip.add(Component.literal("§8合成：8种最高价茶环绕 1个金币锅"));
    }
}
