/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.item.BeerAmmo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BarrelCrossbowItem
extends CrossbowItem {
    public static final int CHARGE_TICKS = 25;
    private static final String KEY_LOADED = "ftboss_loaded_beer";
    private static final float VELOCITY = 1.65f;
    private static final int SHOOT_COOLDOWN = 8;

    public BarrelCrossbowItem(Item.Properties props) {
        super(props);
    }

    public static boolean isLoaded(ItemStack stack) {
        CompoundTag t = stack.m_41783_();
        return t != null && t.m_128441_(KEY_LOADED);
    }

    public static ItemStack loadedBeer(ItemStack stack) {
        CompoundTag t = stack.m_41783_();
        if (t == null || !t.m_128441_(KEY_LOADED)) {
            return ItemStack.f_41583_;
        }
        return ItemStack.m_41712_((CompoundTag)t.m_128469_(KEY_LOADED));
    }

    private static void setLoaded(ItemStack stack, @Nullable ItemStack beer) {
        if (beer == null || beer.m_41619_()) {
            CompoundTag t = stack.m_41783_();
            if (t != null) {
                t.m_128473_(KEY_LOADED);
                t.m_128473_("Charged");
                if (t.m_128456_()) {
                    stack.m_41751_(null);
                }
            }
            return;
        }
        stack.m_41784_().m_128365_(KEY_LOADED, (Tag)beer.m_255036_(1).m_41739_(new CompoundTag()));
        CrossbowItem.m_40884_((ItemStack)stack, (boolean)true);
    }

    public static int chargeTicks(ItemStack stack) {
        int quickCharge = EnchantmentHelper.m_44843_((Enchantment)Enchantments.f_44960_, (ItemStack)stack);
        return Math.max(5, 25 - 5 * quickCharge);
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.m_6883_(stack, level, entity, slot, selected);
        if (!level.f_46443_ && CrossbowItem.m_40932_((ItemStack)stack) != BarrelCrossbowItem.isLoaded(stack)) {
            CrossbowItem.m_40884_((ItemStack)stack, (boolean)BarrelCrossbowItem.isLoaded(stack));
        }
    }

    public int m_8105_(ItemStack stack) {
        return 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
        return ench == Enchantments.f_44960_ || ench == Enchantments.f_44959_ || ench == Enchantments.f_44986_ || ench == Enchantments.f_44962_ || ench == Enchantments.f_44963_;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (BarrelCrossbowItem.isLoaded(stack)) {
            if (!level.f_46443_) {
                this.shoot(level, player, stack, hand);
            }
            BarrelCrossbowItem.setLoaded(stack, null);
            player.m_36335_().m_41524_((Item)this, 8);
            player.m_36246_(Stats.f_12982_.m_12902_((Object)this));
            return InteractionResultHolder.m_19096_((Object)stack);
        }
        if (BeerAmmo.findBeer(player).m_41619_()) {
            if (!level.f_46443_) {
                player.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u9152\u6876\u5f29\u662f\u7a7a\u5f26 \u2014\u2014 \u80cc\u5305\u91cc\u5e26\u4e00\u74f6\u5564\u9152(\u4efb\u610f\u5564\u9152/\u9ea6\u9152/\u8461\u8404\u9152\u90fd\u884c)\u518d\u62c9\u5f26\u88c5\u586b"), true);
            }
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        player.m_6672_(hand);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public void m_5929_(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level.f_46443_) {
            return;
        }
        int used = this.m_8105_(stack) - remaining;
        int full = BarrelCrossbowItem.chargeTicks(stack);
        if (used == 4) {
            BarrelCrossbowItem.play(level, user, SoundEvents.f_11843_);
        } else if (used == Math.max(5, full / 2)) {
            BarrelCrossbowItem.play(level, user, SoundEvents.f_11842_);
        }
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.f_46443_) {
            return;
        }
        if (!(user instanceof Player)) {
            return;
        }
        Player player = (Player)user;
        if (BarrelCrossbowItem.isLoaded(stack)) {
            return;
        }
        if (this.m_8105_(stack) - timeLeft < BarrelCrossbowItem.chargeTicks(stack)) {
            return;
        }
        ItemStack beer = BeerAmmo.findBeer(player);
        if (beer.m_41619_()) {
            return;
        }
        boolean creative = player.m_150110_().f_35937_;
        ItemStack loaded = beer.m_255036_(1);
        if (!creative) {
            beer.m_41774_(1);
        }
        BarrelCrossbowItem.setLoaded(stack, loaded);
        level.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11841_, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!level.f_46443_) {
            FTBossMod.LOGGER.info("[FTBoss][\u9152\u6876\u5f29] {} \u88c5\u586b {} -> \u53f3\u952e\u53d1\u5c04\u7206\u70b8\u9152\u6876(\u5a01\u529b {})", new Object[]{player.m_7755_().getString(), loaded.m_41786_().getString(), Float.valueOf(3.0f)});
        }
    }

    private void shoot(Level level, Player player, ItemStack stack, InteractionHand hand) {
        int multishot = EnchantmentHelper.m_44843_((Enchantment)Enchantments.f_44959_, (ItemStack)stack);
        int count = multishot > 0 ? 3 : 1;
        String beerName = BarrelCrossbowItem.loadedBeer(stack).m_41786_().getString();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Vec3 look = player.m_20154_();
            for (int i = 0; i < count; ++i) {
                double spread = count > 1 ? (double)(i - 1) * 9.0 : 0.0;
                Vec3 dir = BarrelCrossbowItem.rotateY(look, spread);
                BarrelCannonballEntity shell = new BarrelCannonballEntity((Level)sl, (LivingEntity)player);
                shell.m_6034_(player.m_20185_() + dir.f_82479_ * 0.7, player.m_20188_() - 0.2, player.m_20189_() + dir.f_82481_ * 0.7);
                shell.m_5602_((Entity)player);
                shell.m_6686_(dir.f_82479_, dir.f_82480_ + 0.03, dir.f_82481_, 1.65f, count > 1 ? 1.0f : 0.35f);
                sl.m_7967_((Entity)shell);
            }
            FTBossMod.LOGGER.info("[FTBoss][\u9152\u6876\u5f29] {} \u53d1\u5c04 {} \u9897\u7206\u70b8\u9152\u6876 (\u88c5\u586b={}, \u5a01\u529b={})", new Object[]{player.m_7755_().getString(), count, beerName, Float.valueOf(3.0f)});
        }
        level.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11847_, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!player.m_150110_().f_35937_) {
            stack.m_41622_(1, (LivingEntity)player, p -> p.m_21190_(hand));
        }
    }

    private static Vec3 rotateY(Vec3 v, double degrees) {
        if (degrees == 0.0) {
            return v;
        }
        double r = Math.toRadians(degrees);
        double c = Math.cos(r);
        double s = Math.sin(r);
        return new Vec3(v.f_82479_ * c - v.f_82481_ * s, v.f_82480_, v.f_82479_ * s + v.f_82481_ * c);
    }

    private static void play(Level level, LivingEntity user, SoundEvent sound) {
        level.m_6263_(null, user.m_20185_(), user.m_20186_(), user.m_20189_(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        ItemStack beer = BarrelCrossbowItem.loadedBeer(stack);
        if (!beer.m_41619_()) {
            tip.add((Component)Component.m_237113_((String)("\u00a76\u5df2\u88c5\u586b\uff1a\u00a7f" + beer.m_41786_().getString())));
        } else {
            tip.add((Component)Component.m_237113_((String)"\u00a77\u7a7a\u5f26 \u2014\u2014 \u80cc\u5305\u91cc\u5e26\u4e00\u74f6\u5564\u9152, \u957f\u6309\u53f3\u952e\u62c9\u5f26\u88c5\u586b"));
        }
        tip.add((Component)Component.m_237113_((String)"\u00a78\u88c5\u586b\u540e\u53f3\u952e: \u6253\u51fa\u4e00\u9897\u7206\u70b8\u9152\u6876(\u7206\u70b8\u5a01\u529b = \u95ea\u7535\u82e6\u529b\u6015)"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u7206\u70b8\u4e0d\u7834\u574f\u65b9\u5757, \u4e5f\u4e0d\u4f1a\u70b8\u6389\u5730\u4e0a\u7684\u6389\u843d\u7269"));
        tip.add((Component)Component.m_237113_((String)"\u00a78\u53ef\u9644\u9b54: \u5feb\u901f\u88c5\u586b / \u591a\u91cd\u5c04\u51fb / \u8010\u4e45 / \u7ecf\u9a8c\u4fee\u8865"));
    }
}

