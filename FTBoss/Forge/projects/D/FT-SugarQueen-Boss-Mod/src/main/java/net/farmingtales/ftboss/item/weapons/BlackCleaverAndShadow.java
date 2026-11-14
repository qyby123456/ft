package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 【黑色切割者 / 暗影剑】(2026-09-14 v143 按用户要求)。
 *
 * <p><b>黑色切割者</b>：玩家拿到手的伤害砍到 **8 点**（原来 26）；作为补偿，命中敌人时叠
 * {@link BloodRageFakeEffect 血怒·伪}：满 5 层 +100% 攻击力 / +10% 移速 / 1% 真伤，每 10 秒衰减一层。
 *
 * <p><b>暗影剑</b>：基础 **10 点**伤害；右键**投掷末影珍珠**，珍珠落地 3 格内的生物
 * 失明 + 5 点魔法伤害（落点由 {@link ShadowPearlWatcher} 监视，因为原版珍珠实体没有我们的回调）。
 */
public final class BlackCleaverAndShadow {

    private BlackCleaverAndShadow() {}

    /** 【黑色切割者】玩家版：12 点伤害 + 命中叠血怒·伪。
     *  ★ 2026-09-16 按用户要求「黑切是斧子，所以使用斧子的攻击动画」：基类由 {@link SwordItem}
     *  改成 {@link AxeItem}。原版里剑和斧的**手臂挥砍动作是同一套**，真正的差别是：
     *    · 剑：命中会触发**横扫（sweep）**—— 横扫粒子弧 + 挥扫动作，所以拿它当剑用看着就是剑 ✗；
     *    · 斧：不横扫 ✓，并且可以**破盾**（{@code canDisableShield}）✓，挖掘类型为 AXE（砍木头快）✓。
     *  另外本整合包没有 Better Combat / Epic Fight 这类按武器类型换动作的 mod（只有动画库/翻滚/更多动画），
     *  所以"斧子的攻击动画"在原版语境下就等于"按斧子判定"。
     *  伤害不变：1(玩家) + 7 + 4(下界合金) = 12 点。 */
    public static class BlackCleaver extends AxeItem {
        public BlackCleaver() {
            super(Tiers.NETHERITE, 7, -2.6F, new net.minecraft.world.item.Item.Properties());   // v152: 1 + (4 + 7) = 12 点 (彩蛋武器 x1.5)
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.hurtEnemy(stack, target, attacker);
            /* v151: 特效统一由 WeaponHitEffects(伤害事件) 处理, 这里不再重复结算 */
            if (false && hit && !attacker.level().isClientSide) {
                BloodRageFakeEffect.onHit(attacker);
                /* 真伤：按当前层数把一部分伤害转成绕护甲的魔法伤害 */
                /* v144: 削弱敌人护甲 10 点（可叠 2 层 = -20，8 秒） */
                ArmorShredEffect.onHit(target);
                /* v144: 无视 50% 护甲 —— 把"被护甲吃掉的那部分伤害"的一半，用魔法伤害补回来
                   （魔法伤害不吃护甲，等价于把护甲减免减半） */
                try {
                    float raw = (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    double armor = target.getArmorValue();
                    double tough = target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
                    double reduction = Math.min(20.0, Math.max(armor / 5.0, armor - raw / (2.0 + tough / 4.0))) / 25.0;
                    float pierced = (float) (raw * reduction * 0.5);
                    if (pierced > 0.01f) {
                        target.invulnerableTime = 0;
                        target.hurt(attacker.damageSources().magic(), pierced);
                    }
                } catch (Throwable ignored) {}
                var eff = FTEffects.BLOOD_RAGE_FAKE.get();
                var inst = (eff == null) ? null : attacker.getEffect(eff);
                if (inst != null) {
                    float base = (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    float extra = base * BloodRageFakeEffect.trueDamageRatio(inst.getAmplifier());
                    if (extra > 0.01f) {
                        target.invulnerableTime = 0;
                        target.hurt(attacker.damageSources().magic(), extra);
                    }
                }
            }
            return hit;
        }
    }

    /** 【暗影剑】10 点伤害 + 右键投掷末影珍珠（落地致盲 + 5 点魔法伤害） */
    public static class ShadowSword extends SwordItem {
        public static final int COOLDOWN = 40;      // 2 秒
        public static final double AOE_RADIUS = 3.0;
        public static final float AOE_DAMAGE = 10.0f;
        public static final int BLIND_TICKS = 6 * 20;

        public ShadowSword() {
            super(Tiers.NETHERITE, 10, -2.4F, new net.minecraft.world.item.Item.Properties());  // v152: 1 + (4 + 10) = 15 点 (彩蛋武器 x1.5)
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                /* ★ 2026-09-18 补：蛋蛋的末影封锁只管"手里拿珍珠"的情况，而这里手里是**剑**，
                   所以 DandanPearlBan.onRightClickItem 的 is(Items.ENDER_PEARL) 永远不成立，
                   玩家可以靠暗影剑在封锁里随便传送。这里显式问一次 ——
                   被封锁时既不扔珍珠、也不吃冷却（和"按下没反应"一致）。 */
                if (player instanceof net.minecraft.server.level.ServerPlayer sp
                        && net.farmingtales.ftboss.boss.ai.DandanPearlBan.blockThrow(sp, "暗影剑 · 末影珍珠投掷")) {
                    return InteractionResultHolder.fail(stack);
                }
                ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);
                pearl.setItem(new ItemStack(Items.ENDER_PEARL));
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                Vec3 eye = player.getEyePosition();
                pearl.setPos(eye.x, eye.y - 0.1, eye.z);
                level.addFreshEntity(pearl);
                ShadowPearlWatcher.watch(pearl, player);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            player.getCooldowns().addCooldown(this, COOLDOWN);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    /** 珍珠落点监视：原版珍珠一落地就被 discard，所以在这里补我们的 AOE */
    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
    public static final class ShadowPearlWatcher {
        private record Tracked(ThrownEnderpearl pearl, Player owner, int born, Vec3 last) {}
        private static final List<Tracked> LIVE = new ArrayList<>();
        private static final List<Tracked> DONE = new ArrayList<>();

        private ShadowPearlWatcher() {}

        public static void watch(ThrownEnderpearl pearl, Player owner) {
            LIVE.add(new Tracked(pearl, owner, owner.tickCount, pearl.position()));
        }

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || LIVE.isEmpty()) return;
            DONE.clear();
            for (Tracked t : LIVE) {
                if (t.pearl().isRemoved() || t.owner().tickCount - t.born() > 200) {
                    if (t.owner().level() instanceof ServerLevel sl) impact(sl, t.owner(), t.pearl().position());
                    DONE.add(t);
                } else {
                    LIVE.set(LIVE.indexOf(t), new Tracked(t.pearl(), t.owner(), t.born(), t.pearl().position()));
                }
            }
            LIVE.removeAll(DONE);
        }

        private static void impact(ServerLevel sl, Player owner, Vec3 at) {
            /* ★ 2026-09-18 补：这颗是**真的原版 ThrownEnderpearl**，落地时原版会走
               EntityTeleportEvent.EnderPearl，所以 DandanPearlBan 的第 ② 条已经能拦住传送；
               但落点 AOE 是本类在珍珠被 discard 之后自己补算的，不归那条钩子管 ——
               在封锁里就整段跳过（AOE 也不结算），与"珍珠被取消"的表现一致。 */
            if (owner instanceof net.minecraft.server.level.ServerPlayer sp
                    && net.farmingtales.ftboss.boss.ai.DandanPearlBan.blockLanding(sp, "暗影剑 · 末影珍珠落点")) {
                return;
            }
            sl.sendParticles(ParticleTypes.SQUID_INK, at.x, at.y + 0.4, at.z, 40, 1.2, 0.6, 1.2, 0.05);
            sl.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 0.6, at.z, 30, 1.5, 0.8, 1.5, 0.2);
            sl.playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4f, 0.6f);
            int n = 0;
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(ShadowSword.AOE_RADIUS))) {
                if (le == owner || !le.isAlive()) continue;
                le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ShadowSword.BLIND_TICKS, 0, false, true, true));
                le.invulnerableTime = 0;
                /* ★ 2026-09-21 同 WeaponHitEffects：这里的魔法伤害以前也是**无实体**伤害源，
                   庄园 Boss 的闸门（isRealPlayerDamage）认不出玩家就会整下丢弃。
                   改成 indirectMagic(owner, owner)：伤害类型不变（仍是魔法/穿甲），只是带上归因。 */
                le.hurt(owner.damageSources().indirectMagic(owner, owner), ShadowSword.AOE_DAMAGE);
                n++;
            }
            FTBossMod.LOGGER.info("[FTBoss][暗影剑] 末影珍珠落地: 3 格内 {} 个生物失明 + {} 点魔法伤害", n, (int) ShadowSword.AOE_DAMAGE);
        }
    }
}
