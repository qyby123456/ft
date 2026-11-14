package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.RatBossEntity;
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

/** 鼠群诱饵(谷物): 右键召唤 鼠群(大老鼠+小老鼠) */
public class RatSwarmBaitItem extends Item {
    public RatSwarmBaitItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            /* 2026-09-13 v93: 全部 Boss 只能在主世界召唤 */
            if (!net.farmingtales.ftboss.boss.SummonGate.allow(level, player, "鼠群")) {
                return InteractionResultHolder.fail(stack);
            }
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position().add(look.x * 2.5, 0, look.z * 2.5);
            RatBossEntity rat = FTEntities.RAT_BOSS.get().create(level);
            if (rat == null) {
                player.displayClientMessage(Component.literal("§c此处无法招来鼠群"), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            if (!(level instanceof net.minecraft.server.level.ServerLevel server)
                    || !net.farmingtales.ftboss.util.BossCollision.place(server, rat,
                    new Vec3(pos.x, player.getY(), pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, player.getYRot())) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "鼠群");
                return InteractionResultHolder.fail(stack);
            }
            rat.setCustomName(Component.translatable("boss.ftboss.rat_swarm").withStyle(ChatFormatting.BOLD));
            rat.setCustomNameVisible(true);
            rat.setHealth(rat.getMaxHealth());
            rat.setPersistenceRequired();
            net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.prepare(rat);
            if (!level.addFreshEntity(rat)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(player, "鼠群");
                return InteractionResultHolder.fail(stack);
            }
            stack.shrink(1);
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 0.6f, 0.5f);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7鼠群召唤物：§f右键消耗1个引来 鼠群"));
        tip.add(Component.literal("§7击败副王破盾，再用奶酪或牛奶喂满限时装置"));
        tip.add(Component.literal("§8单次伤害达到主王最大生命可直接穿盾"));
        tip.add(Component.literal("§8合成：8种最高价奶酪环绕 1个金币锅"));
    }
}
