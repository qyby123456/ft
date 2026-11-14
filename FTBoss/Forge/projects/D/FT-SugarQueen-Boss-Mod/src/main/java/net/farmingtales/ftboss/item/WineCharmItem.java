package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
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

/** 酒引(红白葡萄酿造): 右键召唤 酒魔·酒桶王(固定原地) */
public class WineCharmItem extends Item {
    public WineCharmItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "酒魔·酒桶王")) {
                return InteractionResultHolder.fail(stack);
            }
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position().add(look.x * 2.5, 0, look.z * 2.5);
            WineBarrelBossEntity barrel = FTEntities.WINE_BARREL.get().create(level);
            if (barrel == null) {
                player.displayClientMessage(Component.literal("§c此处放不下酒桶"), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)
                    || !net.farmingtales.ftboss.util.BossCollision.place(server, barrel,
                    new Vec3(pos.x, player.getY(), pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot())) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "酒魔·酒桶王");
                return InteractionResultHolder.fail(stack);
            }
            barrel.setCustomName(Component.translatable("boss.ftboss.wine_barrel").withStyle(ChatFormatting.BOLD));
            barrel.setCustomNameVisible(true);
            barrel.setHealth(barrel.getMaxHealth());
            barrel.setPersistenceRequired();
            if (!level.addFreshEntity(barrel)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "酒魔·酒桶王");
                return InteractionResultHolder.fail(stack);
            }
            stack.shrink(1);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.HOSTILE, 1.0f, 0.9f);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7酒系Boss召唤物：§f右键召唤 酒魔·酒桶王"));
        tip.add(Component.literal("§8合成：8种最高价酒环绕 1个金币锅"));
    }
}
