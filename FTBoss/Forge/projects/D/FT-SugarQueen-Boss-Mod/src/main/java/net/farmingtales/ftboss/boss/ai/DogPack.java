package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【狗群】—— benger 的召唤物改成**整合包里真实存在的狗类生物**（2026-09-14 v136 按用户要求）。
 *
 * <p>用户原话：「把整合包里面的全部狗类的生物召唤一次，替换召唤小狗。攻击是一样的但是不允许驯服」。
 *
 * <p>整合包里能用的狗类（扫过所有模组 jar 的 lang 键 + 注册表）：
 * <ul>
 *   <li>{@code minecraft:wolf} 原版狼</li>
 *   <li>{@code quark:shiba} 夸克·柴犬</li>
 *   <li>{@code quark:foxhound} 夸克·猎狐犬</li>
 * </ul>
 * 名单以"**按注册名关键词扫注册表**"为准（dog/wolf/hound/shiba/…），所以以后整合包加了狗类模组会自动带上；
 * 上面三个只是兜底/日志用。
 *
 * <p>三条与"原版狗"的差异（都是用户要求）：
 * <ol>
 *   <li><b>攻击一样</b>：这些狗打中玩家时，由 {@link #onHurt} 挂上和原来的小狗完全相同的减益
 *       ——【流血】5 秒 + {@code armamentarium:vulnerable}(护甲削弱) + {@code clipped}(肌腱断裂) + 缓慢；</li>
 *   <li><b>不允许驯服</b>：召唤出来的每一只都打上 {@code ftboss_pack} 标记，{@link #onInteract}
 *       直接取消玩家对它的右键（骨头驯服、喂食、上鞍/项圈之类一律无效）；</li>
 *   <li>会**一直盯着最近的玩家**（每 20 tick 重新锁定）并在 {@value #LIFE} tick 后退场，不会赖在世界上。</li>
 * </ol>
 *
 * <p>「狼群扑击」里"全体一起扑"由 {@link #launchAll} 负责：到点把每只狗朝玩家方向
 * {@code setDeltaMovement(方向 × 1.0, 0.5, …)} 抛出去 —— 因为它们不是我们的实体类，没法给它们加字段，
 * 所以调度放在这张静态表里。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class DogPack {

    /** 召唤物标记（持久化数据，读档后依然认得出） */
    public static final String TAG = "ftboss_pack";
    /** 场上最多留多少只 */
    public static final int CAP = 12;
    /** 每只存活多久（tick）——到点自己退场 */
    public static final int LIFE = 60 * 20;

    /** 兜底名单（扫注册表拿到的主要就是这三只） */
    private static final String[][] FALLBACK = {
            {"minecraft", "wolf"}, {"quark", "foxhound"},
    };
    /** 关键词（按注册名 path 判） */
    private static final String[] KEYS = {
            "wolf", "dog", "hound", "corgi", "husky", "akita", "poodle",
            "beagle", "terrier", "retriever", "collie", "dachshund", "chihuahua", "samoyed", "malamute",
    };
    /** 黑名单：名字像狗但**不打玩家**的（用户 2026-09-14 反馈「柴犬不打人，把他去除掉」） */
    private static final String[] EXCLUDE = { "shiba" };

    private static List<EntityType<?>> CACHE = null;
    private static int roundRobin = 0;
    private static final String BORN_TAG = "ftboss_pack_born";
    private static final Map<UUID, Mob> LOADED = new ConcurrentHashMap<>();
    /** 待执行的集体飞扑：起飞刻 -> 目标 */
    private static final Map<Long, UUID> PENDING_LEAP = new ConcurrentHashMap<>();

    private DogPack() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isPack(event.getEntity())) return;
        Mob mob = (Mob) event.getEntity();
        if (!mob.getPersistentData().contains(BORN_TAG, Tag.TAG_LONG)) {
            mob.getPersistentData().putLong(BORN_TAG, level.getServer().overworld().getGameTime());
        }
        LOADED.put(mob.getUUID(), mob);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel) LOADED.remove(event.getEntity().getUUID(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) LOADED.values().removeIf(mob -> mob.level() == event.getLevel());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOADED.clear();
        PENDING_LEAP.clear();
        CACHE = null;
        roundRobin = 0;
    }

    /* ==================== 名单 ==================== */

    /** 整合包里的狗类实体（懒扫注册表 + 关键词判定；找不到就退回兜底名单） */
    public static synchronized List<EntityType<?>> dogs() {
        if (CACHE != null) return CACHE;
        List<EntityType<?>> list = new ArrayList<>();
        for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (FTBossMod.MOD_ID.equals(id.getNamespace())) continue;      // 不含我们自己的
            String path = id.getPath();
            boolean hit = false;
            for (String k : KEYS) if (path.contains(k)) { hit = true; break; }
            for (String ex : EXCLUDE) if (path.contains(ex)) { hit = false; break; }   // 黑名单优先
            if (!hit) continue;
            EntityType<?> t = entry.getValue();
            /* 只收"能当怪打"的生物，并且体型别太离谱（免得召出巨型 boss） */
            if (t.getCategory() != net.minecraft.world.entity.MobCategory.CREATURE
                    && t.getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) continue;
            if (t.getDimensions().width > 2.0f || t.getDimensions().height > 2.5f) continue;
            list.add(t);
        }
        if (list.isEmpty()) {
            for (String[] id : FALLBACK) {
                EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id[0], id[1]));
                if (t != null) list.add(t);
            }
        }
        CACHE = list;
        StringBuilder sb = new StringBuilder();
        for (EntityType<?> t : list) sb.append(ForgeRegistries.ENTITY_TYPES.getKey(t)).append(' ');
        FTBossMod.LOGGER.info("[FTBoss][DogPack] 整合包里的狗类名单（{} 种）: {}", list.size(), sb.toString().trim());
        return CACHE;
    }

    /* ==================== 召唤 ==================== */

    /** 在 (x,y,z) 附近召唤 n 只**不同的**狗（轮着来，尽量一次一支新面孔） */
    public static int summon(ServerLevel sl, LivingEntity owner, double x, double y, double z, int n) {
        List<EntityType<?>> pool = dogs();
        if (pool.isEmpty()) return 0;
        int made = 0;
        int remaining = CAP - count(sl);
        for (int i = 0; i < n; i++) {
            if (remaining <= 0) break;
            EntityType<?> type = pool.get(Math.floorMod(roundRobin++, pool.size()));
            Entity e = type.create(sl);
            if (!(e instanceof Mob mob)) continue;
            double ang = sl.random.nextDouble() * Math.PI * 2.0;
            double px = x + Math.cos(ang) * 2.6, pz = z + Math.sin(ang) * 2.6;
            mob.moveTo(px, y, pz, sl.random.nextFloat() * 360.0f, 0.0f);
            mob.setPersistenceRequired();
            mob.getPersistentData().putBoolean(TAG, true);
            mob.getPersistentData().putLong(BORN_TAG, sl.getServer().overworld().getGameTime());
            /* 一直盯着玩家：这里先锁最近的，之后由 tick() 每 20 tick 重锁 */
            ServerPlayer nearest = nearestPlayer(sl, px, y, pz);
            if (nearest != null) { mob.setTarget(nearest); angry(mob, nearest); }
            if (!sl.addFreshEntity(mob)) continue;
            LOADED.put(mob.getUUID(), mob);
            remaining--;
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, px, y + 0.6, pz, 14, 0.3, 0.4, 0.3, 0.05);
            sl.playSound(null, px, y + 0.8, pz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.3f, 1.7f);
            made++;
        }
        return made;
    }

    /** 原版狼要靠"被激怒"才会打玩家 */
    private static void angry(Mob mob, ServerPlayer p) {
        try {
            var m = mob.getClass().getMethod("setPersistentAngerTarget", UUID.class);
            m.invoke(mob, p.getUUID());
            var m2 = mob.getClass().getMethod("startPersistentAngerTimer");
            m2.invoke(mob);
        } catch (Throwable ignored) {
            /* 非狼类没有怒气系统：setTarget 就够了 */
        }
    }

    private static ServerPlayer nearestPlayer(ServerLevel sl, double x, double y, double z) {
        ServerPlayer best = null;
        double bd = 64.0 * 64.0;
        for (ServerPlayer p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            double d = p.distanceToSqr(x, y, z);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    /** 场上还有几只（带标记且活着） */
    public static int count(ServerLevel sl) {
        int n = 0;
        for (Mob mob : LOADED.values()) {
            if (mob.level() == sl && !mob.isRemoved() && mob.isAlive() && isPack(mob) && sl.getEntity(mob.getUUID()) == mob) n++;
        }
        return n;
    }

    private static boolean isPack(Entity e) {
        return e instanceof Mob && e.getPersistentData().getBoolean(TAG);
    }

    /* ==================== 集体飞扑 ==================== */

    /** 约定 delay tick 后让全体狗一起扑向玩家 */
    public static void launchAll(ServerLevel sl, ServerPlayer target, int delay) {
        PENDING_LEAP.put(sl.getGameTime() + delay, target.getUUID());
    }

    /* ==================== 维护：锁定 / 退场 / 待执行的飞扑 ==================== */

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (LOADED.isEmpty() && PENDING_LEAP.isEmpty()) return;
        long now = server.overworld().getGameTime();
        LOADED.values().removeIf(mob -> mob.isRemoved() || !mob.isAlive() || !isPack(mob)
                || !(mob.level() instanceof ServerLevel level) || level.getServer() != server
                || level.getEntity(mob.getUUID()) != mob);
        /* 到点的集体飞扑 */
        if (!PENDING_LEAP.isEmpty()) {
            PENDING_LEAP.entrySet().removeIf(en -> {
                if (en.getKey() > now) return false;
                for (ServerLevel sl : server.getAllLevels()) {
                    ServerPlayer t = sl.getServer().getPlayerList().getPlayer(en.getValue());
                    if (t == null || t.level() != sl) continue;
                    int n = 0;
                    for (Mob m : LOADED.values()) {
                        if (m.level() != sl) continue;
                        if (m.distanceToSqr(t) > 48.0 * 48.0) continue;
                        Vec3 d = new Vec3(t.getX() - m.getX(), 0.0, t.getZ() - m.getZ());
                        if (d.lengthSqr() < 1.0E-4) d = new Vec3(0, 0, 1);
                        d = d.normalize();
                        m.setDeltaMovement(d.x * 1.0, 0.5, d.z * 1.0);
                        m.hasImpulse = true;
                        n++;
                    }
                    if (n > 0) {
                        sl.playSound(null, t.getX(), t.getY(), t.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 1.6f, 0.75f);
                        FTBossMod.LOGGER.info("[FTBoss][DogPack] 狼群齐扑：{} 只狗同时扑向 {}", n, t.getName().getString());
                    }
                }
                return true;
            });
        }
        /* 每 20 tick：重锁玩家 + 到寿退场 */
        if (now % 20L != 0L) return;
        for (Mob mob : LOADED.values()) {
            if (!(mob.level() instanceof ServerLevel level)) continue;
            long born = mob.getPersistentData().getLong(BORN_TAG);
            if (now - born > LIFE) {
                mob.discard();
                LOADED.remove(mob.getUUID(), mob);
                continue;
            }
            if (mob.getTarget() == null || !mob.getTarget().isAlive() || now % 40L == 0L) {
                ServerPlayer player = nearestPlayer(level, mob.getX(), mob.getY(), mob.getZ());
                if (player != null) { mob.setTarget(player); angry(mob, player); }
            }
        }
    }

    /* ==================== 攻击效果（与原小狗一致） ==================== */

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            Entity src = e.getSource().getEntity();
            if (src == null) src = e.getSource().getDirectEntity();
            if (src instanceof Projectile pr && pr.getOwner() != null) src = pr.getOwner();
            if (!(src instanceof Mob m) || !isPack(m)) return;
            if (!(e.getEntity() instanceof Player p)) return;
            ExtraDebuffs.bleed(p, 5 * 20, 0);                    // 流血（本模组效果）
            if (p instanceof ServerPlayer sp) BleedLink.link(sp, m);
            debuff(p, "vulnerable", 200, 0);                     // 护甲削弱
            debuff(p, "clipped", 200, 0);                        // 肌腱断裂
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0, false, false, true));
        } catch (Throwable ignored) {
            /* 效果出错不能影响伤害结算 */
        }
    }

    private static void debuff(LivingEntity target, String path, int ticks, int amp) {
        try {
            var eff = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", path));
            if (eff == null) return;
            if (target.hasEffect(eff)) target.removeEffect(eff);   // 先摘旧实例（固定 UUID 修饰符的老坑）
            target.addEffect(new MobEffectInstance(eff, ticks, amp, false, false, true));
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][DogPack] 减益 {} 施加失败: {}", path, t.toString());
        }
    }

    /* ==================== 不允许驯服 ==================== */

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract e) {
        if (e.getTarget() instanceof Mob m && isPack(m)) {
            e.setCanceled(true);      // 骨头驯服 / 喂食 / 一切右键交互一律无效
            if (e.getEntity() instanceof ServerPlayer p) {
                p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§c这批野狗是 benger 唤来的 —— 不能被驯服。"), true);
            }
        }
    }
}
