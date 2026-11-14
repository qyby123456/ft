package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 【酒桶弩】—— 酒桶王(酒魔)的武器, 2026-09-14 v108 先作为外观导入, **v123 按用户要求做成能真的用的武器**。
 *
 * <p>用户原话: 「完善一下酒桶弩的攻击效果，装填任意啤酒后发射爆炸酒桶造成爆炸」。
 *
 * <p>手感完全照原版弩:
 * <ol>
 *   <li><b>装填</b>: 背包里带一瓶**任意啤酒**(见 {@link BeerAmmo} 的词元判定 + 数据包标签
 *       {@code ftboss:beer_ammo}) → 长按右键拉满 {@value #CHARGE_TICKS} tick, 松手就把那瓶酒
 *       塞进桶里(消耗一瓶, 创造模式不消耗), 弩的外观换成"装填完成"那一版; 没酒就拉不动弦。</li>
 *   <li><b>发射</b>: 装填状态下右键 → 打出一颗**爆炸酒桶**
 *       ({@link BarrelCannonballEntity}, 爆炸威力 = 闪电苦力怕 {@code 3.0}, 命中/落地即爆,
 *       四系之外**不破坏地形**), 然后重新需要装填。</li>
 * </ol>
 *
 * <p>附魔支持 **快速装填**(每级 -5 tick)/ **多重射击**(一次 3 颗, 左右各偏 9°)/ 耐久与经验修补
 * —— 用铁砧或附魔书加, 附魔台也只会给这几个。
 *
 * <p>继承原版弩以接入第一/第三人称持弩动作；装填与发射仍由本类管理，不调用原版箭矢逻辑。
 */
public class BarrelCrossbowItem extends CrossbowItem {

    /** 拉满所需的 tick(与原版弩一致) */
    public static final int CHARGE_TICKS = 25;
    /** NBT: 桶里装的是哪一瓶酒 */
    private static final String KEY_LOADED = "ftboss_loaded_beer";
    /** 出膛速度 */
    private static final float VELOCITY = 1.65F;
    /** 发射后的短冷却 */
    private static final int SHOOT_COOLDOWN = 8;

    public BarrelCrossbowItem(Properties props) {
        super(props);
    }

    /* ==================== 装填状态(NBT) ====================
       做成 static 是为了让客户端模型谓词({@code ftboss:loaded})能直接读 ——
       模型谓词拿到的只有 ItemStack, 拿不到 Item 实例。 */

    /** 桶里有没有酒(客户端模型谓词也调这个, 必须 null 安全) */
    public static boolean isLoaded(ItemStack stack) {
        CompoundTag t = stack.getTag();
        return t != null && t.contains(KEY_LOADED);
    }

    /** 桶里那瓶酒(仅用于工具提示/日志) */
    public static ItemStack loadedBeer(ItemStack stack) {
        CompoundTag t = stack.getTag();
        if (t == null || !t.contains(KEY_LOADED)) return ItemStack.EMPTY;
        return ItemStack.of(t.getCompound(KEY_LOADED));
    }

    private static void setLoaded(ItemStack stack, @Nullable ItemStack beer) {
        if (beer == null || beer.isEmpty()) {
            CompoundTag t = stack.getTag();
            if (t != null) {
                t.remove(KEY_LOADED);
                t.remove("Charged");
                if (t.isEmpty()) stack.setTag(null);
            }
            return;
        }
        stack.getOrCreateTag().put(KEY_LOADED, beer.copyWithCount(1).save(new CompoundTag()));
        CrossbowItem.setCharged(stack, true);
    }

    /** 拉满要多久 —— 快速装填每级 -5 tick, 最低 5(和原版弩一样) */
    public static int chargeTicks(ItemStack stack) {
        int quickCharge = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        return FoodWeaponFamilyBuild.chargeTicks(stack, Math.max(5, CHARGE_TICKS - 5 * quickCharge));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && CrossbowItem.isCharged(stack) != isLoaded(stack)) {
            CrossbowItem.setCharged(stack, isLoaded(stack));
        }
    }

    /* ==================== 原版弩手感 ==================== */

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
        return ench == Enchantments.QUICK_CHARGE || ench == Enchantments.MULTISHOT
                || ench == Enchantments.UNBREAKING || ench == Enchantments.MENDING
                || ench == Enchantments.VANISHING_CURSE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (FoodWeaponLifecycle.isBroken(stack)) return InteractionResultHolder.fail(stack);
        /* ① 装填完了 -> 发射 */
        if (isLoaded(stack)) {
            if (!level.isClientSide) shoot(level, player, stack, hand);
            setLoaded(stack, null);
            player.getCooldowns().addCooldown(this, SHOOT_COOLDOWN);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        /* ② 没装填: 身上得有酒才拉得动弦 */
        if (BeerAmmo.findBeer(player).isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal(
                        "§c酒桶弩是空弦 —— 背包里带一瓶啤酒(任意啤酒/麦酒/葡萄酒都行)再拉弦装填"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /** 拉弦音效(和原版弩一样: 起手一声 + 中途一声) */
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level.isClientSide) return;
        int used = this.getUseDuration(stack) - remaining;
        int full = chargeTicks(stack);
        if (used == 4) {
            play(level, user, SoundEvents.CROSSBOW_LOADING_START);
        } else if (used == Math.max(5, full / 2)) {
            play(level, user, SoundEvents.CROSSBOW_LOADING_MIDDLE);
        }
    }

    /** 松手: 拉满了就把背包里那瓶酒塞进桶里 */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.isClientSide || FoodWeaponLifecycle.isBroken(stack)) return;
        if (!(user instanceof Player player)) return;
        if (isLoaded(stack)) return;
        if (this.getUseDuration(stack) - timeLeft < chargeTicks(stack)) return;   // 没拉满 = 没装填
        ItemStack beer = BeerAmmo.findBeer(player);
        if (beer.isEmpty()) return;
        boolean creative = player.getAbilities().instabuild;
        ItemStack loaded = beer.copyWithCount(1);        // 先留一份再扣, 免得只剩 1 瓶时拿到空栈
        if (!creative) beer.shrink(1);
        setLoaded(stack, loaded);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!level.isClientSide) {
            FTBossMod.LOGGER.info("[FTBoss][酒桶弩] {} 装填 {} -> 右键发射爆炸酒桶(威力 {})",
                    player.getName().getString(), loaded.getHoverName().getString(),
                    BarrelCannonballEntity.EXPLOSION_POWER);
        }
    }

    /* ==================== 发射 ==================== */

    private void shoot(Level level, Player player, ItemStack stack, InteractionHand hand) {
        int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack);
        int count = multishot > 0 ? 3 : 1;
        String beerName = loadedBeer(stack).getHoverName().getString();
        if (level instanceof ServerLevel sl) {
            CompoundTag volley = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    ? FoodWeaponRanged.capture(serverPlayer, stack) : new CompoundTag();
            Vec3 look = player.getLookAngle();
            for (int i = 0; i < count; i++) {
                double spread = count > 1 ? (i - 1) * 9.0 : 0.0;    // 多重射击: 左中右各偏 9°
                Vec3 dir = rotateY(look, spread);
                BarrelCannonballEntity shell = new BarrelCannonballEntity(sl, player);
                shell.setPos(player.getX() + dir.x * 0.7, player.getEyeY() - 0.2, player.getZ() + dir.z * 0.7);
                shell.setOwner(player);
                shell.shoot(dir.x, dir.y + 0.03, dir.z, VELOCITY, count > 1 ? 1.0F : 0.35F);
                FoodWeaponRanged.launch(shell, volley);
                sl.addFreshEntity(shell);
            }
            FTBossMod.LOGGER.info("[FTBoss][酒桶弩] {} 发射 {} 颗爆炸酒桶 (装填={}, 威力={})",
                    player.getName().getString(), count, beerName, BarrelCannonballEntity.EXPLOSION_POWER);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
    }

    /** 绕 Y 轴转一个角度(多重射击的散射) —— 手写三角, 不依赖 Vec3#yRot 的版本差异 */
    private static Vec3 rotateY(Vec3 v, double degrees) {
        if (degrees == 0.0) return v;
        double r = Math.toRadians(degrees);
        double c = Math.cos(r), s = Math.sin(r);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    private static void play(Level level, LivingEntity user, net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /* ==================== 工具提示 ==================== */

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        ItemStack beer = loadedBeer(stack);
        if (!beer.isEmpty()) {
            tip.add(Component.literal("§6已装填：§f" + beer.getHoverName().getString()));
        } else {
            tip.add(Component.literal("§7空弦 —— 背包里带一瓶啤酒, 长按右键拉弦装填"));
        }
        tip.add(Component.literal("§8装填后右键: 打出一颗爆炸酒桶(爆炸威力 = 闪电苦力怕)"));
        tip.add(Component.literal("§8爆炸不破坏方块, 也不会炸掉地上的掉落物"));
        tip.add(Component.literal("§8可附魔: 快速装填 / 多重射击 / 耐久 / 经验修补"));
    }
}
