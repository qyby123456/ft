package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;

import net.farmingtales.ftboss.client.render.ShadowSwordGeoRenderer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.ShadowCastManager;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 【噬影之刃 · 裂瞳】GeckoLib 骨骼物品 +【裂瞳投珠】+【裂瞳回响】。
 *
 * ★ 裂瞳回响的 ready 状态（2026-09-15 第二版，已修掉"跟随本地玩家"的缺陷）：
 *   服务端在 inventoryTick 里根据**这一把剑持有者的冷却**写标记到 **ItemStack 的 NBT**
 *   （`ftboss_ready`）→ 随背包同步下发到客户端 → 渲染器与动画控制器都读这个标记。
 *   因此：**每把剑各自独立** ✓ 展示框/掉落物/别人手里的剑不会再跟随本地玩家 ✗→✓
 *
 * 边沿触发（只播一次）：客户端记录"这把剑上一次是否已可用"（按玩家 UUID），
 *   只在 不可用→可用 的那一个 tick 置 echoPulse 并撒一次粒子 ✓ 不会每 tick 重复 ✓
 *
 * 其余：长按右键蓄力 → 服务器状态机（第 7 tick 发射 / 第 18 tick 结束 / 取消清理）→ 自定义暗影珍珠。
 */
public class ShadowSwordItem extends BlackCleaverAndShadow.ShadowSword implements GeoItem {

    /** ItemStack NBT 键：true = 技能已就绪（冷却完成）。 */
    public static final String TAG_READY = "ftboss_ready";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.ftboss.shadow_sword.idle");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cast");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.release");
    private static final RawAnimation RECOVER = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.recover");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cancel");
    private static final RawAnimation COOLDOWN_READY = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cooldown_ready");
    private static final RawAnimation READY_IDLE = RawAnimation.begin().thenLoop("animation.ftboss.shadow_sword.ready_idle");
    /** 武器光效: 三处种子一瞬间爆亮（新片段, 不改现有 8 段） */
    private static final RawAnimation GLOW_FLASH = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.glow_flash");

    private static final String CTRL = "skill";

    private static volatile boolean echoPulse;
    private static volatile boolean echoReady;
    private static final Map<UUID, Boolean> WAS_READY = new HashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShadowSwordItem() {
        super();
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "weapon_controller", 5, state -> {
            if (echoPulse) {
                echoPulse = false;                       // 消费一次, 保证不每 tick 重复
                return state.setAndContinue(COOLDOWN_READY);
            }
            return state.setAndContinue(echoReady ? READY_IDLE : IDLE);
        }));
        controllers.add(new AnimationController<>(this, CTRL, 0, state -> PlayState.STOP)
                .triggerableAnim("cast", CAST)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("release", RELEASE)
                .triggerableAnim("recover", RECOVER)
                .triggerableAnim("cancel", CANCEL)
                .triggerableAnim("glow", GLOW_FLASH));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void triggerSkillAnim(Entity holder, ItemStack stack, String name) {
        if (holder.level() instanceof ServerLevel sl) {
            triggerAnim(holder, GeoItem.getOrAssignId(stack, sl), CTRL, name);
        }
    }

    /** 服务器写标记（每把剑各自独立）; 客户端读标记做边沿触发与粒子。 */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!level.isClientSide) {
            if (player instanceof ServerPlayer sp) {
                boolean cooling = sp.getCooldowns().isOnCooldown(this);
                boolean ready = !cooling;
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.getBoolean(TAG_READY) != ready) {
                    tag.putBoolean(TAG_READY, ready);     // 变更才写, 避免每 tick 触发同步
                }
            }
            return;
        }

        boolean ready = stack.hasTag() && stack.getTag().getBoolean(TAG_READY);
        UUID id = player.getUUID();
        boolean was = Boolean.TRUE.equals(WAS_READY.get(id));
        if (ready && !was) {
            echoPulse = true;                             // 只在 不可用->可用 的这一 tick
            spawnEchoParticles(player, level);
        }
        echoReady = ready;
        WAS_READY.put(id, ready);
    }

    /* ============ 暗影传送（v2: 去掉下坠，改为"传送到落点并结算"） ============ */

    /** 落点范围魔法伤害的基础值（半径 3 格，**无视护甲**）；实际结算走 {@link #skyDamage()}。 */
    public static final float SKY_DAMAGE = 10.0F;
    public static final double SKY_RADIUS = 3.0D;
    /** 竖向判定半高：落点是"柱子"不是"球"，高处的敌人也该吃到。 */
    public static final double SKY_HALF_HEIGHT = 2.5D;
    public static final int SKY_BLIND_TICKS = 120;
    /** 落点把人往外推的力量（贴脸时不至于零击退）。 */
    private static final double SKY_KNOCKBACK = 1.4D;
    /** 传送落点最多向下找几格落脚点；都找不到就**不传送**、只结算伤害。 */
    private static final int LANDING_SEARCH_DOWN = 3;

    /** 落点范围魔法伤害的实际值（庄园技能倍率已计入）。 */
    public static float skyDamage() {
        return 30.0F;
    }

    /** 找一个能站人的落点（脚下实心、身位两格空），找不到返回 NaN。 */
    private static double findLandingY(ServerLevel sl, double x, double y, double z) {
        net.minecraft.core.BlockPos base = net.minecraft.core.BlockPos.containing(x, y, z);
        for (int dy = 0; dy <= LANDING_SEARCH_DOWN; dy++) {
            net.minecraft.core.BlockPos feet = base.below(dy);
            if (sl.isEmptyBlock(feet) && sl.isEmptyBlock(feet.above()) && !sl.isEmptyBlock(feet.below())) {
                return feet.getY();
            }
        }
        return Double.NaN;
    }

    /**
     * 【暗影传送】把使用者**直接传送到珍珠落点**，并在落点结算范围魔法伤害。
     *
     * <p>v2（2026-09-16 按用户要求）：原来是把人瞬移到落点上方 14 格再"砸下来"，
     * 下坠期间要每 tick 压坠落伤害，还要处理卡住 / 登出 / 换维度的残留状态。
     * 现在**整段下坠都删掉了** —— 传送与结算是同一个瞬间，没有滞空状态，那些边角问题一并消失。
     *
     * <p>伤害走 {@code indirectMagic}（无视护甲，但受魔法抗性减免），与本模组其它"技能魔法伤害"一致。
     * PvP 保护：同一个队伍/友军不吃伤害（只吃击退与致盲）。
     */
    public static void teleportStrike(ServerPlayer sp, double x, double y, double z) {
        Level lv = sp.level();
        ServerLevel sl = sp.serverLevel();

        /* 0) 先找落脚点：落点本身能站就站，否则往下找几格；都找不到就不传送（伤害照常结算） */
        double landY = findLandingY(sl, x, y, z);
        if (!Double.isNaN(landY)) {
            sp.teleportTo(sl, x, landY, z, sp.getYRot(), sp.getXRot());
            sp.resetFallDistance();                    // 传送前若正在下落，别把这笔坠落伤害带过来
            sp.hurtMarked = true;
            Fx.sound(lv, x, landY, z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.9F);
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§5【暗影传送】§7已抵达落点 —— 周围造成范围魔法伤害"), true);
        }

        /* 0.5) 【暗影镇印】地面魔法阵 + 能量柱（2026-09-16 补上 —— 之前**根本没实现**，
               shadowsigil / shadow_pillar 两张贴图从 09-15 生成后一直没有 Java 引用）。
               贴花不随粒子/音效自动同步，所以走本模组第一个自定义 S2C 包；只发给附近玩家。
               y 用落点最终站立的那个高度（找不到落脚点就退回结算点），阵才贴在地面上而不是悬空。 */
        net.farmingtales.ftboss.skill.FTNetwork.sendSigilNear(sp, x,
                Double.isNaN(landY) ? y : landY, z, true);

        /* 1) 范围伤害：横向用半径，竖向用柱子半高（高处的敌人也吃到） */
        AABB box = new AABB(x - SKY_RADIUS, y - SKY_HALF_HEIGHT, z - SKY_RADIUS,
                x + SKY_RADIUS, y + SKY_HALF_HEIGHT, z + SKY_RADIUS);
        DamageSource src = lv.damageSources().indirectMagic(sp, sp);
        int hit = 0;

        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == sp) {
                continue;                                   // 不伤自己
            }
            // 横向超出半径的（AABB 是方的）跳过 -> 实际判定是圆柱
            double dx = e.getX() - x;
            double dz = e.getZ() - z;
            if (dx * dx + dz * dz > SKY_RADIUS * SKY_RADIUS) {
                continue;
            }
            // PvP / 友军保护：不伤害，但照样吃击退与致盲
            boolean hurtable = !(e instanceof Player victim && !sp.canHarmPlayer(victim));
            if (hurtable && e.hurt(src, skyDamage())) {
                hit++;
            }
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, SKY_BLIND_TICKS, 0));
            // 2) 击退：从落点向外 + 略微抬起（贴脸时给一个保底力度）
            Vec3 away = new Vec3(e.getX() - x, 0.0D, e.getZ() - z);
            away = away.lengthSqr() < 1.0E-4D
                    ? new Vec3(sp.getRandom().nextDouble() - 0.5D, 0.0D, sp.getRandom().nextDouble() - 0.5D)
                    : away.normalize();
            e.push(away.x * SKY_KNOCKBACK, 0.35D, away.z * SKY_KNOCKBACK);
            e.hurtMarked = true;
        }

        /* 3) 冲击波：三圈错开半径的地面环 + 上冲暗影柱 + 碎屑（复用 Fx 收口的工具） */
        WeaponFx.burst(sl, new Vec3(x, y + 0.1, z), SKY_RADIUS, Theme.SHADOW);
        Fx.particles(lv, x, y + 0.5D, z, 10, ParticleTypes.SQUID_INK, 0.7D, 0.08D);
        Fx.particles(lv, x, y + 1.2D, z, 12, ParticleTypes.REVERSE_PORTAL, 0.6D, 0.12D);
        Fx.particles(lv, x, y + 0.2D, z, 6, ParticleTypes.CLOUD, 1.0D, 0.06D);
        lv.addParticle(ParticleTypes.FLASH, x, y + 0.6D, z, 0.0D, 0.0D, 0.0D);

        /* 4) 音效：铁砧落地（主）+ 低频轰击 + 末影传送音做"暗影"味 */
        Fx.sound(lv, x, y, z, net.minecraft.sounds.SoundEvents.ANVIL_LAND, 1.1F, 0.55F);
        Fx.sound(lv, x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 0.7F, 1.35F);

        /* 5) 传送抵达瞬间武器闪光一次 */
        flashGlow(sp);

        FTBossMod.LOGGER.info("[FTBoss][暗影传送] {} 落点结算: 命中 {} 个目标, 半径 {} 格 / {} 点魔法伤害",
                sp.getGameProfile().getName(), hit, SKY_RADIUS, SKY_DAMAGE);
    }

    /** 手上（主手或副手）拿着暗影剑 -> 触发一次武器光效闪光。 */
    public static void flashGlow(ServerPlayer sp) {
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack st = sp.getItemInHand(hand);
            if (st.getItem() instanceof ShadowSwordItem sword) {
                sword.triggerSkillAnim(sp, st, "glow");
            }
        }
    }

    /** 冷却完成瞬间: 魔眼与柄首附近 8 个暗紫/浅紫粒子, 向外扩散, 只撒一次。 */
    private static void spawnEchoParticles(Player player, Level level) {
        double x = player.getX(), y = player.getY() + 1.1D, z = player.getZ();
        for (int i = 0; i < 8; i++) {
            double a = (i / 8.0D) * Math.PI * 2.0D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                    x + Math.cos(a) * 0.28D, y, z + Math.sin(a) * 0.28D,
                    Math.cos(a) * 0.03D, 0.02D, Math.sin(a) * 0.03D);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isUsingItem() || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            player.startUsingItem(hand);                  // 维持使用中(松手才会发 release 包); pass 不摆臂
            return InteractionResultHolder.pass(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!ShadowCastManager.begin(serverPlayer, hand)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ShadowCastManager.release(player, stack);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !player.level().isClientSide) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), CTRL, "release");
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ShadowSwordGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ShadowSwordGeoRenderer();
                }
                return this.renderer;
            }
        });
    }
}
