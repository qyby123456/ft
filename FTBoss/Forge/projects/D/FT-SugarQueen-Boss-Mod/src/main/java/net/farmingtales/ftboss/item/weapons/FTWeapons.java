package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Boss 主题武器的"专属机制"实现(2026-09-10 追加)。
 *
 * <p>全部集中在本文件里(每把武器一个嵌套类 + 共用小工具), 便于对照调整:
 * <ul>
 *   <li><b>酒瓶锤</b> —— 低攻速; 手持时给予醉酒(tipsy) + **3 个随机酒类 buff 轮替**</li>
 *   <li><b>奶酪锤</b> —— 同酒瓶锤但只有 **2 个**轮替 buff, 基础伤害更高</li>
 * </ul>
 *
 * <p>手持类 buff 的做法: 用短持续时间(100t)刷新(每 40t 一次), 换手后约 3 秒自然消失,
 * 不需要在换手时做清理 —— 避免"卸下装备还在吃药"的残留。
 */
public final class FTWeapons {

    private FTWeapons() {}

    // =====================================================================================
    //  共用小工具
    // =====================================================================================

    /** 按命名空间+路径取效果(对应 mod 缺失时返回 null, 调用处自动跳过) */
    static MobEffect fx(String ns, String path) {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(ns, path));
    }

    /** 挂效果(效果不存在则静默跳过) */
    static void apply(LivingEntity target, String ns, String path, int ticks, int amp) {
        MobEffect e = fx(ns, path);
        if (e != null) target.addEffect(new MobEffectInstance(e, ticks, amp, false, false, true));
    }

    /** 手持 buff 的刷新节奏 */
    static final int REFRESH = 40;
    static final int DURATION = 100;

    // =====================================================================================
    //  酒瓶锤与奶酪锤: 醉酒 + 酒类 buff 轮替
    // =====================================================================================

    /**
     * 酒瓶锤 / 奶酪锤的公共底座。
     * <p>手持时: 常驻 <b>醉酒</b>({@code brewinandchewin:tipsy}, 画面摇晃) + 从酒类效果池里随机取 N 个**轮替**
     * (每 {@link #ROTATE_TICKS} 刻换一批; 用玩家 UUID + 批次号做种子, 同一玩家同一批稳定、不同玩家不同)。
     * <p>池子 = 醉酒/醉意(intoxication) / 狂怒(raging) / 甜蜜之心(sweet_heart) + 原版"酒壮胆"三件套(力量/饱和/幸运)。
     * 其中 intoxicated 与反胃是负面, 属于"喝多了"的风味, 可用 {@link #INCLUDE_NEGATIVE} 关掉。
     */
    public abstract static class DrinkingMace extends SwordItem {

        /** 每 8 秒换一批 buff */
        public static final int ROTATE_TICKS = 160;
        /** 是否把"喝多了"的负面效果也算进池子 */
        public static final boolean INCLUDE_NEGATIVE = true;

        private static final String[] POOL_NS = {
                "brewinandchewin", "brewinandchewin", "brewinandchewin",
                "minecraft", "minecraft", "minecraft"
        };
        private static final String[] POOL_PATH = {
                "raging", "sweet_heart", "intoxication",
                "strength", "saturation", "luck"
        };

        protected DrinkingMace(int damageBonus, float speed, Item.Properties props) {
            super(FTWeaponTier.INSTANCE, damageBonus, speed, props);
        }

        /** 本武器轮替的 buff 个数(酒瓶锤 3 / 奶酪锤 2) */
        protected abstract int rotatingCount();

        /** 轮替 buff 的等级(2026-09-10 加强: 统一为 II 级) */
        protected int buffAmplifier() { return 1; }

        /** 命中: 醉拳 —— 有几率让目标也醉(反胃+迟缓), 两把锤共用 */
        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.hurtEnemy(stack, target, attacker);
            if (hit && !target.level().isClientSide && target.getRandom().nextFloat() < 0.35F) {
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, false, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0, false, false, true));
            }
            return hit;
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, level, entity, slot, selected);
            if (level.isClientSide || !selected || !(entity instanceof LivingEntity living)) return;
            if (living.tickCount % REFRESH != 0) return;

            // 醉酒(常驻) —— 2026-09-10 按用户要求"酒桶 boss 的微醺效果提高到 5 级": amplifier 0 → 4
            apply(living, "brewinandchewin", "tipsy", DURATION, 4);

            // 随机轮替
            long phase = level.getGameTime() / ROTATE_TICKS;
            Random rnd = new Random(living.getUUID().getLeastSignificantBits() * 31L + phase * 0x9E3779B9L);
            List<Integer> idx = new ArrayList<>();
            for (int i = 0; i < POOL_PATH.length; i++) idx.add(i);
            Collections.shuffle(idx, rnd);
            int n = Math.min(rotatingCount(), idx.size());
            for (int k = 0; k < n; k++) {
                int i = idx.get(k);
                boolean negative = POOL_PATH[i].equals("intoxication");
                if (negative && !INCLUDE_NEGATIVE) continue;
                MobEffect e = fx(POOL_NS[i], POOL_PATH[i]);
                if (e != null) living.addEffect(new MobEffectInstance(e, ROTATE_TICKS + 40, buffAmplifier(), false, false, true));
            }
        }
    }

    /** 酒瓶锤: 低攻速, 手持醉酒 + 3 个随机酒类 buff 轮替(II 级) */
    public static class WineBottleMace extends DrinkingMace {
        public WineBottleMace() {
            super(5, -3.2F, new Item.Properties());
        }

        @Override protected int rotatingCount() { return 3; }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add(Component.literal("§6低攻速重锤（-3.2）").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("§d手持：醉酒 + §f3 §d个随机酒类增益轮替（II 级，每 8 秒换一批）"));
            tip.add(Component.literal("§d命中：35% 几率让目标醉酒（反胃+迟缓）"));
        }
    }

    /** 奶酪锤: 与酒瓶锤同源, 但轮替 buff 更少(2 个)、基础伤害更高 */
    public static class CheeseWheelMace extends DrinkingMace {
        public CheeseWheelMace() {
            super(8, -3.4F, new Item.Properties());
        }

        @Override protected int rotatingCount() { return 2; }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add(Component.literal("§6低攻速重锤（-3.4），本模组最高伤害").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("§d手持：醉酒 + §f2 §d个随机酒类增益轮替（II 级）"));
            tip.add(Component.literal("§d命中：35% 几率让目标醉酒（反胃+迟缓）"));
        }
    }

    // =====================================================================================
    //  6) 【打狗棒】—— 边牧犬王 benger 的掉落武器(2026-09-14 v127)
    // =====================================================================================

    /**
     * 【打狗棒】(边牧犬王 benger 的掉落, 2026-09-14 v127)。
     *
     * <p>主题: 打狗棒专治恶犬 —— 命中挂本模组的【流血】({@code ftboss:bleed}) + 缓慢,
     * 对**犬类**(注册名里带 wolf/dog/hound/fox/benger 的生物)额外 +6 点真实伤害(穿甲);
     * 右键是一次**横扫**: 身周 3.5 格内所有非友方生物被掀飞 + 受伤 + 缓慢, 冷却 3 秒。
     */
    public static class DogBeatingStick extends SwordItem {

        /** 对犬类的额外真实伤害(穿甲) */
        public static final float BONUS_VS_DOGS = 6.0F;
        /** 横扫半径 / 伤害 / 冷却(tick) */
        public static final double SWEEP_RADIUS = 3.5;
        public static final float SWEEP_DAMAGE = 7.0F;
        public static final int SWEEP_COOLDOWN = 60;

        public DogBeatingStick() {
            super(FTWeaponTier.INSTANCE, 5, -2.2F, new Item.Properties());
        }

        /** 名字里带这些词就算"犬类"(不引编译期依赖, 按注册名判) */
        private static boolean isCanine(LivingEntity e) {
            var key = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
            if (key == null) return false;
            String p = key.getPath();
            return p.contains("wolf") || p.contains("dog") || p.contains("hound")
                    || p.contains("fox") || p.contains("benger") || p.contains("jackal");
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.hurtEnemy(stack, target, attacker);
            if (hit && !target.level().isClientSide) {
                net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(target, 8 * 20, 1);   // 本模组自带流血 DoT
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 20, 0, false, false, true));
                if (isCanine(target)) {
                    target.invulnerableTime = 0;
                    target.hurt(target.damageSources().genericKill(), BONUS_VS_DOGS);
                    if (attacker instanceof Player p) {
                        p.displayClientMessage(Component.literal("§6打狗棒 §7专治恶犬：额外 " + (int) BONUS_VS_DOGS + " 点真实伤害"), true);
                    }
                }
            }
            return hit;
        }

        @Override
        public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return net.minecraft.world.InteractionResultHolder.fail(stack);
            player.getCooldowns().addCooldown(this, SWEEP_COOLDOWN);
            if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                int hits = 0;
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(SWEEP_RADIUS))) {
                    if (e == player || !e.isAlive() || e instanceof Player other && other.isCreative()) continue;
                    e.invulnerableTime = 0;
                    e.hurt(player.damageSources().playerAttack(player), SWEEP_DAMAGE);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2 * 20, 0, false, false, true));
                    e.knockback(1.15, e.getX() - player.getX(), e.getZ() - player.getZ());
                    hits++;
                }
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                        player.getX(), player.getY() + 1.0, player.getZ(), 8, 1.6, 0.3, 1.6, 0.02);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.2f, 0.9f);
                net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][打狗棒] {} 横扫命中 {} 个目标", player.getName().getString(), hits);
            }
            player.swing(hand, true);
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add(Component.literal("§6命中：流血 II + 缓慢 I 3 秒").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("§d对犬类（狼/狗/狐…）额外 " + (int) BONUS_VS_DOGS + " 点真实伤害"));
            tip.add(Component.literal("§7右键横扫：身周 " + SWEEP_RADIUS + " 格击飞 + " + (int) SWEEP_DAMAGE + " 伤害（冷却 3 秒）"));
        }
    }
}
