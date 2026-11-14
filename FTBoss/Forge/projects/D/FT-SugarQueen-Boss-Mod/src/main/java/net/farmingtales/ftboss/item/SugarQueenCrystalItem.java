package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.boss.SugarQueenBoss;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 糖霜水晶：右键召唤糖霜女皇（消耗1个） */
public class SugarQueenCrystalItem extends Item {
    public SugarQueenCrystalItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            if (SugarQueenBoss.tryStart((net.minecraft.server.level.ServerPlayer) player)) {
                stack.shrink(1);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 1.2f);
            } else {
                player.displayClientMessage(Component.literal("§c糖霜女皇已在战斗中，或此处无法召唤"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7甜点系Boss召唤物：§f右键消耗1个召唤 姜饼国王"));
        tip.add(Component.literal("§8合成：8种最高价甜点环绕 1个金币锅"));
    }
}
