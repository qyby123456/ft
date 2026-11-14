package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.boss.MeatBoss;
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

/** 屠夫之心：右键召唤 屠夫(肉类Boss)（消耗1个）；机制同 糖霜水晶 */
public class MeatHeartItem extends Item {
    public MeatHeartItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            // 失败原因由 MeatBoss.tryStart 自己提示(已存在/实体创建失败/无法生成), 这里不再统一甩一句"已在战斗中"
            if (MeatBoss.tryStart((net.minecraft.server.level.ServerPlayer) player)) {
                stack.shrink(1);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.0f, 0.8f);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7肉类Boss召唤物：§f右键消耗1个召唤 屠夫"));
        tip.add(Component.literal("§8合成：8种最高价肉料理环绕 1个金币锅"));
    }
}
