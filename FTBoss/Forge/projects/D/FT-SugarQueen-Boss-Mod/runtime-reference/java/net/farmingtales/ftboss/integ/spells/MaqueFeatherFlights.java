/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.integ.spells;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorBossSpellProfile;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.FlywheelCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.MaqueRicochetRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="ftboss")
public final class MaqueFeatherFlights {
    private static final Map<ServerLevel, List<Flight>> ACTIVE = new WeakHashMap<ServerLevel, List<Flight>>();
    private static final Map<ServerLevel, Map<UUID, Long>> LAST_HIT = new WeakHashMap<ServerLevel, Map<UUID, Long>>();

    private MaqueFeatherFlights() {
    }

    public static boolean cast(ServerLevel serverLevel2, LivingEntity livingEntity, ManorSignatureSpell manorSignatureSpell, int n, Vec3 vec3) {
        EasterBossEntity easterBossEntity;
        if (manorSignatureSpell.kind != ManorSpellRules.Kind.FEATHER_FAN) {
            return false;
        }
        if (!livingEntity.m_6084_() || livingEntity.m_213877_() || livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).isMini() || !ManorSpellRules.validPoint(vec3.m_82557_(livingEntity.m_20182_())) || !serverLevel2.m_46805_(BlockPos.m_274446_((Position)vec3))) {
            return true;
        }
        float f = BossSpellTuning.playerPower(livingEntity, BossSpellTuning.manorBoss(livingEntity) ? ManorBossSpellProfile.power(manorSignatureSpell.kind) : manorSignatureSpell.getSpellPower(ManorSpellRules.level(n), (Entity)livingEntity));
        if (!Float.isFinite(f) || f < 0.0f) {
            return true;
        }
        List list = ACTIVE.computeIfAbsent(serverLevel2, serverLevel -> new ArrayList());
        if (list.size() + 5 > 320) {
            return true;
        }
        SpellVisuals.show(serverLevel2, livingEntity, manorSignatureSpell.kind.id, SpellVisualRules.Phase.RELEASE, vec3);
        Vec3 vec32 = vec3.m_82546_(livingEntity.m_146892_()).m_82541_();
        if (vec32.m_82556_() < 1.0E-8) {
            vec32 = livingEntity.m_20154_();
        }
        double d = BossSpellTuning.radius(livingEntity, 8.0);
        for (int i = -2; i <= 2; ++i) {
            Vec3 vec33 = vec32.m_82524_((float)i * 0.16f).m_82490_(1.1);
            Flight flight = new Flight(livingEntity.m_20148_(), vec3, d, f, serverLevel2.m_46467_(), livingEntity.m_146892_(), vec33);
            list.add(flight);
            MaqueFeatherFlights.send(serverLevel2, flight, false);
        }
        return true;
    }

    private static boolean valid(ServerLevel serverLevel, LivingEntity livingEntity, Flight flight, LivingEntity livingEntity2) {
        return livingEntity2.m_9236_() == serverLevel && BossSpellEffects.canTarget(livingEntity, livingEntity2) && (livingEntity instanceof Player || livingEntity2 instanceof Player) && livingEntity2.m_20238_(flight.center) <= flight.radius * flight.radius;
    }

    private static List<LivingEntity> targets(ServerLevel serverLevel, LivingEntity livingEntity, Flight flight) {
        return serverLevel.m_6443_(LivingEntity.class, new AABB(flight.center, flight.center).m_82400_(flight.radius), livingEntity2 -> MaqueFeatherFlights.valid(serverLevel, livingEntity, flight, livingEntity2) && MaqueFeatherFlights.visible(serverLevel, livingEntity, flight.position, livingEntity2.m_20191_().m_82399_()));
    }

    private static boolean visible(ServerLevel serverLevel, LivingEntity livingEntity, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)livingEntity)).m_6662_() == HitResult.Type.MISS;
    }

    private static void advance(ServerLevel serverLevel2, Flight flight, long l) {
        Object object;
        LivingEntity livingEntity;
        Object object2;
        LivingEntity livingEntity3;
        boolean bl;
        LivingEntity livingEntity4;
        Entity entity = serverLevel2.m_8791_(flight.caster);
        LivingEntity livingEntity5 = entity instanceof LivingEntity ? (livingEntity4 = (LivingEntity)entity) : null;
        boolean bl2 = bl = livingEntity5 != null && livingEntity5.m_6084_() && !livingEntity5.m_213877_() && !ManorFightData.get(serverLevel2.m_7654_()).retired(ManorFightMembers.fightId((Entity)livingEntity5));
        if (MaqueRicochetRules.finished(flight.hits, l - flight.born, bl) || !serverLevel2.m_46805_(BlockPos.m_274446_((Position)flight.position))) {
            flight.done = true;
            MaqueFeatherFlights.send(serverLevel2, flight, true);
            return;
        }
        LivingEntity livingEntity6 = flight.target == null ? null : (livingEntity3 = (object2 = serverLevel2.m_8791_(flight.target)) instanceof LivingEntity ? (livingEntity = (LivingEntity)object2) : null);
        if (livingEntity3 == null || !MaqueFeatherFlights.valid(serverLevel2, livingEntity5, flight, livingEntity3) || !MaqueFeatherFlights.visible(serverLevel2, livingEntity5, flight.position, livingEntity3.m_20191_().m_82399_())) {
            int n;
            livingEntity = MaqueFeatherFlights.targets(serverLevel2, livingEntity5, flight);
            int n2 = -1;
            for (n = 0; n < livingEntity.size(); ++n) {
                if (!((LivingEntity)livingEntity.get(n)).m_20148_().equals(flight.previous)) continue;
                n2 = n;
            }
            n = MaqueRicochetRules.choose(livingEntity.size(), n2, serverLevel2.f_46441_.m_188500_());
            livingEntity3 = n < 0 ? null : (LivingEntity)livingEntity.get(n);
            UUID uUID = flight.target = livingEntity3 == null ? null : livingEntity3.m_20148_();
        }
        if (livingEntity3 != null) {
            livingEntity = livingEntity3.m_20191_().m_82399_().m_82546_(flight.position);
            object2 = MaqueRicochetRules.steer(new double[]{flight.velocity.f_82479_, flight.velocity.f_82480_, flight.velocity.f_82481_}, new double[]{livingEntity.f_82479_, livingEntity.f_82480_, livingEntity.f_82481_});
            flight.velocity = new Vec3((double)object2[0], (double)object2[1], (double)object2[2]);
        }
        double d = (object2 = serverLevel2.m_45547_(new ClipContext(flight.position, (Vec3)(livingEntity = flight.position.m_82549_(flight.velocity)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)livingEntity5))).m_6662_() == HitResult.Type.MISS ? Double.POSITIVE_INFINITY : flight.position.m_82557_(object2.m_82450_());
        LivingEntity livingEntity7 = null;
        Vec3 vec3 = null;
        double d2 = d;
        for (LivingEntity livingEntity8 : serverLevel2.m_6443_(LivingEntity.class, new AABB(flight.position, (Vec3)livingEntity).m_82400_(1.28), livingEntity2 -> MaqueFeatherFlights.valid(serverLevel2, livingEntity5, flight, livingEntity2))) {
            double d3;
            object = livingEntity8.m_20191_().m_82400_(0.28);
            Vec3 vec32 = object.m_82390_(flight.position) ? flight.position : (Vec3)object.m_82371_(flight.position, (Vec3)livingEntity).orElse(null);
            if (vec32 == null || !((d3 = flight.position.m_82557_(vec32)) < d2)) continue;
            d2 = d3;
            livingEntity7 = livingEntity8;
            vec3 = vec32;
        }
        if (livingEntity7 != null && l >= flight.ready) {
            Map map = LAST_HIT.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
            int n = FlywheelCombatRules.hitCooldown(livingEntity5.m_20270_(livingEntity7), false);
            if (FlywheelCombatRules.hitReady(l, (Long)map.get(livingEntity7.m_20148_()), n)) {
                object = ManorIronSpells.spell(ManorSpellRules.Kind.FEATHER_FAN);
                flight.ready = l + (long)n;
                if (ManorSpellEffects.hurt(livingEntity5, livingEntity7, (ManorSignatureSpell)((Object)object), flight.damage)) {
                    map.put(livingEntity7.m_20148_(), l);
                    ++flight.hits;
                    flight.previous = livingEntity7.m_20148_();
                    flight.target = null;
                    ManorSpellEffects.effect(livingEntity5, livingEntity7, MobEffects.f_19619_, 80, 0);
                    if (flight.hits == 3) {
                        flight.position = vec3;
                        flight.done = true;
                        MaqueFeatherFlights.send(serverLevel2, flight, true);
                        return;
                    }
                }
            }
        }
        if (object2.m_6662_() != HitResult.Type.MISS) {
            flight.position = object2.m_82450_();
            flight.done = true;
            MaqueFeatherFlights.send(serverLevel2, flight, true);
            return;
        }
        flight.position = livingEntity;
        if ((l - flight.born) % 2L == 0L) {
            MaqueFeatherFlights.send(serverLevel2, flight, false);
        }
    }

    private static void send(ServerLevel serverLevel, Flight flight, boolean bl) {
        SpellVisuals.send(serverLevel, new SpellVisualPacket(serverLevel.m_46472_().m_135782_(), Integer.MAX_VALUE, flight.id, SpellVisualRules.find("feather_fan"), bl ? SpellVisualRules.Phase.PULSE : SpellVisualRules.Phase.BEAM, flight.position, bl ? flight.position : flight.position.m_82549_(flight.velocity)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent serverTickEvent) {
        if (serverTickEvent.phase != TickEvent.Phase.END || ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        for (Map.Entry<ServerLevel, List<Flight>> entry2 : List.copyOf(ACTIVE.entrySet())) {
            ServerLevel serverLevel = entry2.getKey();
            long l = serverLevel.m_46467_();
            for (Flight flight2 : List.copyOf((Collection)entry2.getValue())) {
                MaqueFeatherFlights.advance(serverLevel, flight2, l);
            }
            entry2.getValue().removeIf(flight -> flight.done);
            Map<UUID, Long> map = LAST_HIT.get(serverLevel);
            if (map == null || l % 200L != 0L) continue;
            map.entrySet().removeIf(entry -> l < (Long)entry.getValue() || l - (Long)entry.getValue() > 400L);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        ACTIVE.remove(unload.getLevel());
        LAST_HIT.remove(unload.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        ACTIVE.clear();
        LAST_HIT.clear();
    }

    private static final class Flight {
        final UUID id = UUID.randomUUID();
        final UUID caster;
        final Vec3 center;
        final double radius;
        final float damage;
        final long born;
        Vec3 position;
        Vec3 velocity;
        UUID target;
        UUID previous;
        int hits;
        long ready;
        boolean done;

        Flight(UUID uUID, Vec3 vec3, double d, float f, long l, Vec3 vec32, Vec3 vec33) {
            this.caster = uUID;
            this.center = vec3;
            this.radius = d;
            this.damage = f;
            this.born = l;
            this.position = vec32;
            this.velocity = vec33;
        }
    }
}

