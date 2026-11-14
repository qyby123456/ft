/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss.ai;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BleedLink;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="ftboss")
public final class DogPack {
    public static final String TAG = "ftboss_pack";
    public static final int CAP = 12;
    public static final int LIFE = 1200;
    private static final String[][] FALLBACK = new String[][]{{"minecraft", "wolf"}, {"quark", "foxhound"}};
    private static final String[] KEYS = new String[]{"wolf", "dog", "hound", "corgi", "husky", "akita", "poodle", "beagle", "terrier", "retriever", "collie", "dachshund", "chihuahua", "samoyed", "malamute"};
    private static final String[] EXCLUDE = new String[]{"shiba"};
    private static List<EntityType<?>> CACHE = null;
    private static int roundRobin = 0;
    private static final String BORN_TAG = "ftboss_pack_born";
    private static final Map<UUID, Mob> LOADED = new ConcurrentHashMap<UUID, Mob>();
    private static final Map<Long, UUID> PENDING_LEAP = new ConcurrentHashMap<Long, UUID>();

    private DogPack() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        ServerLevel level;
        block5: {
            block4: {
                Level level2 = event.getLevel();
                if (!(level2 instanceof ServerLevel)) break block4;
                level = (ServerLevel)level2;
                if (DogPack.isPack(event.getEntity())) break block5;
            }
            return;
        }
        Mob mob = (Mob)event.getEntity();
        if (!mob.getPersistentData().m_128425_(BORN_TAG, 4)) {
            mob.getPersistentData().m_128356_(BORN_TAG, level.m_7654_().m_129783_().m_46467_());
        }
        LOADED.put(mob.m_20148_(), mob);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel) {
            LOADED.remove(event.getEntity().m_20148_(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) {
            LOADED.values().removeIf(mob -> mob.m_9236_() == event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOADED.clear();
        PENDING_LEAP.clear();
        CACHE = null;
        roundRobin = 0;
    }

    /*
     * WARNING - void declaration
     */
    public static synchronized List<EntityType<?>> dogs() {
        if (CACHE != null) {
            return CACHE;
        }
        ArrayList list = new ArrayList();
        for (Map.Entry entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            EntityType t;
            ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).m_135782_();
            if ("ftboss".equals(resourceLocation.m_135827_())) continue;
            String path = resourceLocation.m_135815_();
            boolean hit = false;
            for (String k : KEYS) {
                if (!path.contains(k)) continue;
                hit = true;
                break;
            }
            for (String ex : EXCLUDE) {
                if (!path.contains(ex)) continue;
                hit = false;
                break;
            }
            if (!hit || (t = (EntityType)entry.getValue()).m_20674_() != MobCategory.CREATURE && t.m_20674_() != MobCategory.MONSTER || t.m_20680_().f_20377_ > 2.0f || t.m_20680_().f_20378_ > 2.5f) continue;
            list.add(t);
        }
        if (list.isEmpty()) {
            void var3_7;
            String[][] stringArray = FALLBACK;
            int n = stringArray.length;
            boolean bl = false;
            while (var3_7 < n) {
                String[] id = stringArray[var3_7];
                EntityType t = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id[0], id[1]));
                if (t != null) {
                    list.add(t);
                }
                ++var3_7;
            }
        }
        CACHE = list;
        StringBuilder sb = new StringBuilder();
        for (EntityType entityType : list) {
            sb.append(ForgeRegistries.ENTITY_TYPES.getKey((Object)entityType)).append(' ');
        }
        FTBossMod.LOGGER.info("[FTBoss][DogPack] \u6574\u5408\u5305\u91cc\u7684\u72d7\u7c7b\u540d\u5355\uff08{} \u79cd\uff09: {}", (Object)list.size(), (Object)sb.toString().trim());
        return CACHE;
    }

    public static int summon(ServerLevel sl, LivingEntity owner, double x, double y, double z, int n) {
        List<EntityType<?>> pool = DogPack.dogs();
        if (pool.isEmpty()) {
            return 0;
        }
        int made = 0;
        int remaining = 12 - DogPack.count(sl);
        for (int i = 0; i < n && remaining > 0; ++i) {
            EntityType<?> type;
            Entity e;
            if (!((e = (type = pool.get(Math.floorMod(roundRobin++, pool.size()))).m_20615_((Level)sl)) instanceof Mob)) continue;
            Mob mob = (Mob)e;
            double ang = sl.f_46441_.m_188500_() * Math.PI * 2.0;
            double px = x + Math.cos(ang) * 2.6;
            double pz = z + Math.sin(ang) * 2.6;
            mob.m_7678_(px, y, pz, sl.f_46441_.m_188501_() * 360.0f, 0.0f);
            mob.m_21530_();
            mob.getPersistentData().m_128379_(TAG, true);
            mob.getPersistentData().m_128356_(BORN_TAG, sl.m_7654_().m_129783_().m_46467_());
            ServerPlayer nearest = DogPack.nearestPlayer(sl, px, y, pz);
            if (nearest != null) {
                mob.m_6710_((LivingEntity)nearest);
                DogPack.angry(mob, nearest);
            }
            if (!sl.m_7967_((Entity)mob)) continue;
            LOADED.put(mob.m_20148_(), mob);
            --remaining;
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123759_, px, y + 0.6, pz, 14, 0.3, 0.4, 0.3, 0.05);
            sl.m_6263_(null, px, y + 0.8, pz, SoundEvents.f_144243_, SoundSource.HOSTILE, 1.3f, 1.7f);
            ++made;
        }
        return made;
    }

    private static void angry(Mob mob, ServerPlayer p) {
        try {
            Method m = mob.getClass().getMethod("setPersistentAngerTarget", UUID.class);
            m.invoke(mob, p.m_20148_());
            Method m2 = mob.getClass().getMethod("startPersistentAngerTimer", new Class[0]);
            m2.invoke(mob, new Object[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static ServerPlayer nearestPlayer(ServerLevel sl, double x, double y, double z) {
        ServerPlayer best = null;
        double bd = 4096.0;
        for (ServerPlayer p : sl.m_6907_()) {
            double d;
            if (!p.m_6084_() || p.m_5833_() || !((d = p.m_20275_(x, y, z)) < bd)) continue;
            bd = d;
            best = p;
        }
        return best;
    }

    public static int count(ServerLevel sl) {
        int n = 0;
        for (Mob mob : LOADED.values()) {
            if (mob.m_9236_() != sl || mob.m_213877_() || !mob.m_6084_() || !DogPack.isPack((Entity)mob) || sl.m_8791_(mob.m_20148_()) != mob) continue;
            ++n;
        }
        return n;
    }

    private static boolean isPack(Entity e) {
        return e instanceof Mob && e.getPersistentData().m_128471_(TAG);
    }

    public static void launchAll(ServerLevel sl, ServerPlayer target, int delay) {
        PENDING_LEAP.put(sl.m_46467_() + (long)delay, target.m_20148_());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        if (LOADED.isEmpty() && PENDING_LEAP.isEmpty()) {
            return;
        }
        long now = server.m_129783_().m_46467_();
        LOADED.values().removeIf(mob -> {
            ServerLevel level;
            Level patt10222$temp;
            return mob.m_213877_() || !mob.m_6084_() || !DogPack.isPack((Entity)mob) || !((patt10222$temp = mob.m_9236_()) instanceof ServerLevel) || (level = (ServerLevel)patt10222$temp).m_7654_() != server || level.m_8791_(mob.m_20148_()) != mob;
        });
        if (!PENDING_LEAP.isEmpty()) {
            PENDING_LEAP.entrySet().removeIf(en -> {
                if ((Long)en.getKey() > now) {
                    return false;
                }
                for (ServerLevel sl : server.m_129785_()) {
                    ServerPlayer t = sl.m_7654_().m_6846_().m_11259_((UUID)en.getValue());
                    if (t == null || t.m_9236_() != sl) continue;
                    int n = 0;
                    for (Mob m : LOADED.values()) {
                        if (m.m_9236_() != sl || m.m_20280_((Entity)t) > 2304.0) continue;
                        Vec3 d = new Vec3(t.m_20185_() - m.m_20185_(), 0.0, t.m_20189_() - m.m_20189_());
                        if (d.m_82556_() < 1.0E-4) {
                            d = new Vec3(0.0, 0.0, 1.0);
                        }
                        d = d.m_82541_();
                        m.m_20334_(d.f_82479_ * 1.0, 0.5, d.f_82481_ * 1.0);
                        m.f_19812_ = true;
                        ++n;
                    }
                    if (n <= 0) continue;
                    sl.m_6263_(null, t.m_20185_(), t.m_20186_(), t.m_20189_(), SoundEvents.f_12619_, SoundSource.HOSTILE, 1.6f, 0.75f);
                    FTBossMod.LOGGER.info("[FTBoss][DogPack] \u72fc\u7fa4\u9f50\u6251\uff1a{} \u53ea\u72d7\u540c\u65f6\u6251\u5411 {}", (Object)n, (Object)t.m_7755_().getString());
                }
                return true;
            });
        }
        if (now % 20L != 0L) {
            return;
        }
        for (Mob mob2 : LOADED.values()) {
            ServerPlayer player;
            Level level = mob2.m_9236_();
            if (!(level instanceof ServerLevel)) continue;
            ServerLevel level2 = (ServerLevel)level;
            long born = mob2.getPersistentData().m_128454_(BORN_TAG);
            if (now - born > 1200L) {
                mob2.m_146870_();
                LOADED.remove(mob2.m_20148_(), mob2);
                continue;
            }
            if (mob2.m_5448_() != null && mob2.m_5448_().m_6084_() && now % 40L != 0L || (player = DogPack.nearestPlayer(level2, mob2.m_20185_(), mob2.m_20186_(), mob2.m_20189_())) == null) continue;
            mob2.m_6710_((LivingEntity)player);
            DogPack.angry(mob2, player);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            Mob m;
            Projectile pr;
            Entity src = e.getSource().m_7639_();
            if (src == null) {
                src = e.getSource().m_7640_();
            }
            if (src instanceof Projectile && (pr = (Projectile)src).m_19749_() != null) {
                src = pr.m_19749_();
            }
            if (!(src instanceof Mob) || !DogPack.isPack((Entity)(m = (Mob)src))) {
                return;
            }
            LivingEntity livingEntity = e.getEntity();
            if (!(livingEntity instanceof Player)) {
                return;
            }
            Player p = (Player)livingEntity;
            ExtraDebuffs.bleed((LivingEntity)p, 100, 0);
            if (p instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)p;
                BleedLink.link((Player)sp, (LivingEntity)m);
            }
            DogPack.debuff((LivingEntity)p, "vulnerable", 200, 0);
            DogPack.debuff((LivingEntity)p, "clipped", 200, 0);
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 30, 0, false, false, true));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void debuff(LivingEntity target, String path, int ticks, int amp) {
        try {
            MobEffect eff = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", path));
            if (eff == null) {
                return;
            }
            if (target.m_21023_(eff)) {
                target.m_21195_(eff);
            }
            target.m_7292_(new MobEffectInstance(eff, ticks, amp, false, false, true));
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][DogPack] \u51cf\u76ca {} \u65bd\u52a0\u5931\u8d25: {}", (Object)path, (Object)t.toString());
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract e) {
        Mob m;
        Entity entity = e.getTarget();
        if (entity instanceof Mob && DogPack.isPack((Entity)(m = (Mob)entity))) {
            e.setCanceled(true);
            Player player = e.getEntity();
            if (player instanceof ServerPlayer) {
                ServerPlayer p = (ServerPlayer)player;
                p.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u8fd9\u6279\u91ce\u72d7\u662f benger \u5524\u6765\u7684 \u2014\u2014 \u4e0d\u80fd\u88ab\u9a6f\u670d\u3002"), true);
            }
        }
    }
}

