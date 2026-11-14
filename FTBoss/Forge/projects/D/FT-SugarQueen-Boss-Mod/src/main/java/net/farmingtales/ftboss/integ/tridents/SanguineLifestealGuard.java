package net.farmingtales.ftboss.integ.tridents;

import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 血腥契约（{@code tridents_n_stuff:sanguine_bond}）**只在对敌对生物造成伤害时吸血**
 * —— 2026-09-24 按用户要求（先说「去除血腥契约攻击假人上时给玩家的回复」，
 * 随后把口径明确成「血腥契约只在攻击敌对生物产生吸血」）。
 *
 * <p><b>为什么要在这一层拦：</b>吸血来自两处，都不在本工程的可改源码里 ——
 *
 * <ol>
 *   <li>内嵌的 TnS 自己（Kotlin {@code TridentModifier.applySanguine}）：
 *       读目标身上的 {@code tridents_n_stuff:bleeding} 等级 {@code n}，给目标叠流血，
 *       然后 <b>{@code user.heal(2f * 2^min(n+1, 4))}</b> —— 也就是 <b>2 / 4 / 8 / 16 / 32</b> 点。
 *       那个包是原样 Jar-in-Jar 内嵌的，没有源码，改不了它。</li>
 *   <li>本模组那个「没有源码的合并类」{@code SanguineBondHooks.execute}
 *       （右键技能二的处决）：命中成立后 <b>{@code player.heal(min(8f, damage * 0.25f))}</b>，
 *       {@code damage = 10 + 3 * min(5, 层数)} —— 也就是
 *       <b>2.5 / 3.25 / 4 / 4.75 / 5.5 / 6.25</b> 点。</li>
 * </ol>
 *
 * <p>所以这里用"打的是不是敌对生物 + 回复量对不对得上"两道判据拦，
 * 而不是把那一小段时间里的所有回复一刀切 —— 免得把同一 tick 里再生效果、
 * 别的模组的吸血一起误伤：
 *
 * <ol>
 *   <li>{@link AttackEntityEvent}（平砍，主手或副手拿着血腥契约）或
 *       {@link LivingHurtEvent}（投出去的三叉戟实体命中）时，把该玩家的
 *       「命中时刻 + 目标是不是敌对生物」写进他自己的 {@code persistentData}；</li>
 *   <li>{@link LivingHealEvent}：同一个 tick（+{@link #WINDOW_TICKS} 容差）内，
 *       回复量正好是上面那两套档位之一，且**上一次命中不是敌对生物** → 取消。</li>
 * </ol>
 *
 * <p><b>「敌对生物」怎么判</b>（{@link #isHostile}）：原版敌对标记 {@link Enemy}
 * （Monster / Slime / Vindicator 这类）**或**本模组 Boss {@link EasterBossEntity}
 * **或**生成类别为 {@link MobCategory#MONSTER}。最后一条是必需的兜底：
 * 糖霜女皇 / 茶壶树 / 酒桶王继承的是铁魔法的 {@code AbstractSpellCastingMob}
 * （它 extends {@code PathfinderMob}，**不是** {@code Monster}），但本模组注册时给的就是 MONSTER。
 * 反过来，试验假人（Dummmmmmy，{@code dummmmmmy:target_dummy}）是 {@code Mob} +
 * {@code MobCategory.MISC}，两条都不满足 —— 所以打假人不再回血。
 * 另外**玩家**（PvP）与**盔甲架**一律不算敌对生物，也不吸血。
 */
public final class SanguineLifestealGuard {
    /** 假人模组 Dummmmmmy 的命名空间：显式排除，免得它哪天改了注册类别又漏回来。 */
    private static final String DUMMY_NAMESPACE = "dummmmmmy";
    /** 三叉戟模组命名空间：投出去的三叉戟实体（含血腥契约）都在这个命名空间下。 */
    private static final String TRIDENT_NAMESPACE = "tridents_n_stuff";

    private static final ResourceLocation SANGUINE_BOND = new ResourceLocation("tridents_n_stuff", "sanguine_bond");
    /** 显示名兜底（与 BossHandler 的契约判定同款口径：精确「血腥契约」）。 */
    private static final String SANGUINE_NAME = "血腥契约";

    /** 时间戳挂在玩家自己的 persistentData 上，玩家下线/卸载自动带走，不用自己清表。 */
    private static final String STAMP_KEY = "ftboss:SanguineHitTick";
    /** 上一次命中是不是敌对生物（false = 这次回血要拦）。 */
    private static final String HOSTILE_KEY = "ftboss:SanguineHitHostile";
    /** 非敌对目标的显示名，仅用于日志。 */
    private static final String TARGET_KEY = "ftboss:SanguineHitTarget";
    /** 同 tick 结算，留 1 tick 容差。 */
    private static final long WINDOW_TICKS = 1L;

    /**
     * 两套吸血档位（见类注释）。这些数在二进制里都是精确值
     * （2 的幂；以及 {@code damage * 0.25f}，damage 全是整数），所以可以精确比对。
     */
    private static final Set<Float> SANGUINE_HEALS = Set.of(
            2.0F, 4.0F, 8.0F, 16.0F, 32.0F,                       // TnS applySanguine
            2.5F, 3.25F, 4.75F, 5.5F, 6.25F);                     // SanguineBondHooks.execute（4.0 已在上方）

    /** 日志节流：每个玩家最多每 40 tick 打一条，免得刷屏。 */
    private static final String LOG_KEY = "ftboss:SanguineHealLog";
    private static final long LOG_THROTTLE_TICKS = 40L;

    private SanguineLifestealGuard() {}

    /** 由 {@link TridentIntegration#register} 调用（那边是合并版 FTBossMod 一定会走到的地方）。 */
    public static void register() {
        MinecraftForge.EVENT_BUS.register(SanguineLifestealGuard.class);
    }

    /* ① 平砍：主手/副手拿着血腥契约 → 记下"命中时刻 + 目标是不是敌对生物" */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            if (player.level().isClientSide) return;
            if (!(event.getTarget() instanceof LivingEntity target)) return;
            if (!isSanguine(player.getMainHandItem()) && !isSanguine(player.getOffhandItem())) return;
            stamp(player, target);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][血腥契约] 吸血判定异常(平砍): {}", t.toString());
        }
    }

    /* ② 投出去的三叉戟实体命中 → 同样记一笔（投掷后手上已经没有那把戟了，不能查手持） */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        try {
            LivingEntity target = event.getEntity();
            if (target.level().isClientSide) return;
            Entity direct = event.getSource().getDirectEntity();
            if (direct == null || direct == event.getSource().getEntity()) return;
            if (!TRIDENT_NAMESPACE.equals(namespaceOf(direct))) return;
            if (event.getSource().getEntity() instanceof ServerPlayer player) stamp(player, target);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][血腥契约] 吸血判定异常(投掷): {}", t.toString());
        }
    }

    /* ③ 吸血：刚打完 + 数量正好是那两套档位之一 + 目标不是敌对生物 → 取消 */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            if (player.level().isClientSide) return;
            var data = player.getPersistentData();
            long stamp = data.getLong(STAMP_KEY);
            if (stamp == 0L) return;
            long now = player.level().getGameTime();
            if (now < stamp || now - stamp > WINDOW_TICKS) return;
            if (data.getBoolean(HOSTILE_KEY)) return;      // 打的是敌对生物 → 正常吸血，不干预
            float amount = event.getAmount();
            if (!SANGUINE_HEALS.contains(amount)) return;  // 不是血腥契约那两套档位（可能是再生等）→ 放行
            event.setCanceled(true);
            if (now - data.getLong(LOG_KEY) >= LOG_THROTTLE_TICKS) {
                data.putLong(LOG_KEY, now);
                String name = data.contains(TARGET_KEY) ? data.getString(TARGET_KEY) : "非敌对目标";
                FTBossMod.LOGGER.info("[FTBoss][血腥契约] 非敌对目标不吸血: {} 打 {} 取消回复 {} 点",
                        player.getName().getString(), name, amount);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][血腥契约] 吸血判定异常(回血): {}", t.toString());
        }
    }

    private static void stamp(ServerPlayer player, LivingEntity target) {
        var data = player.getPersistentData();
        data.putLong(STAMP_KEY, player.level().getGameTime());
        boolean hostile = isHostile(target);
        data.putBoolean(HOSTILE_KEY, hostile);
        if (hostile) data.remove(TARGET_KEY);
        else data.putString(TARGET_KEY, target.getName().getString());
    }

    /** 只对敌对生物吸血。见类注释里对每一条判据的说明。 */
    public static boolean isHostile(LivingEntity target) {
        if (target instanceof Player) return false;                 // 玩家不算敌对生物（PvP 不吸血）
        if (target instanceof ArmorStand) return false;             // 盔甲架是摆设
        if (DUMMY_NAMESPACE.equals(namespaceOf(target))) return false; // 试验假人（显式排除）
        if (target instanceof Enemy) return true;                   // 原版敌对标记
        if (target instanceof EasterBossEntity) return true;         // 本模组 Boss
        return target.getType().getCategory() == MobCategory.MONSTER; // 生成类别兜底（含铁魔法系 Boss）
    }

    private static String namespaceOf(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id == null ? "" : id.getNamespace();
    }

    /** 血腥契约的判定与 BossHandler 同款：精确 id，或显示名精确等于「血腥契约」。 */
    private static boolean isSanguine(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (SANGUINE_BOND.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) return true;
        return SANGUINE_NAME.equals(stack.getHoverName().getString().replace(" ", ""));
    }
}
