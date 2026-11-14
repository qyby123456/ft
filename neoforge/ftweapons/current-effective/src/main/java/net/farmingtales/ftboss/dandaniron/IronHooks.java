package net.farmingtales.ftboss.dandaniron;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell.Aim;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class IronHooks {
    private static final Map<ServerLevel, Map<IronHooks.Key, IronHooks.Cast>> STATES = new WeakHashMap<>();

    private IronHooks() {
    }

    private static boolean eligible(LivingEntity var0, String var1) {
        if (IronPacket.matches(var1) && var0 instanceof EasterBossEntity var2 && var2.getVariant() == 1 && !var2.isMini() && !var2.isGiant() && var2.isAlive()) {
            return true;
        }

        return false;
    }

    public static void begin(ManorSignatureSpell var0, int var1, LivingEntity var2, MagicData var3) {
        if (eligible(var2, var0.kind.id) && var2.level() instanceof ServerLevel var4) {
            int var14 = Math.max(1, var3 != null && var3.getCastDuration() > 0 ? var3.getCastDuration() : var0.getEffectiveCastTime(var1, var2));
            int var6 = var3 != null && var3.getCastDurationRemaining() > 0 ? Math.min(var14, var3.getCastDurationRemaining()) : var14;
            Map<IronHooks.Key, IronHooks.Cast> var7 = STATES.computeIfAbsent(var4, var0x -> new LinkedHashMap<>());
            var7.entrySet().removeIf(var1x -> var4.getGameTime() - ((IronHooks.Cast)var1x.getValue()).touched > 1200L);
            if (var7.size() >= 64) {
                var7.remove(var7.keySet().iterator().next());
            }

            IronHooks.Key var8 = new IronHooks.Key(var2.getUUID(), var0.kind.id);
            IronHooks.Cast var9 = (IronHooks.Cast)var7.get(var8);
            IronHooks.Cast var10 = new IronHooks.Cast(var4.getGameTime(), var14, var6);
            var10.fieldSequence = var9 == null ? var10.sequence : var9.fieldSequence;
            var7.put(var8, var10);
            Vec3 var11 = var3 != null && var3.getAdditionalCastData() instanceof Aim var12 ? var12.point() : var2.position();
            SpellVisuals.show(var4, var2, var0.kind.id, Phase.CHARGE, var11);
        }
    }

    public static void tick(ManorSignatureSpell var0, int var1, LivingEntity var2, MagicData var3) {
        if (eligible(var2, var0.kind.id) && var2.level() instanceof ServerLevel var4) {
            IronHooks.Cast var6 = STATES.getOrDefault(var4, Map.of()).get(new IronHooks.Key(var2.getUUID(), var0.kind.id));
            if (var6 == null) {
                begin(var0, var1, var2, var3);
            } else {
                if (var3 != null && var3.getCastDuration() > 0) {
                    var6.ticks = var3.getCastDuration();
                    var6.start = var4.getGameTime() - (long)Math.max(0, var6.ticks - var3.getCastDurationRemaining());
                }

                var6.touched = var4.getGameTime();
            }
        }
    }

    public static void release(ManorSignatureSpell var0, LivingEntity var1) {
        if (eligible(var1, var0.kind.id) && var1.level() instanceof ServerLevel var2) {
            IronHooks.Cast var4 = STATES.getOrDefault(var2, Map.of()).get(new IronHooks.Key(var1.getUUID(), var0.kind.id));
            if (var4 != null) {
                var4.released = true;
                var4.fieldSequence = var4.sequence;
                var4.touched = var2.getGameTime();
                cancelCharge(var2, var4);
            }
        }
    }

    public static void complete(ManorSignatureSpell var0, LivingEntity var1, boolean var2) {
        if (eligible(var1, var0.kind.id) && var1.level() instanceof ServerLevel var3) {
            IronHooks.Cast var5 = STATES.getOrDefault(var3, Map.of()).get(new IronHooks.Key(var1.getUUID(), var0.kind.id));
            if (var5 != null) {
                cancelCharge(var3, var5);
                var5.touched = var3.getGameTime();
            }
        }
    }

    private static void cancelCharge(ServerLevel var0, IronHooks.Cast var1) {
        if (var1.charge != null) {
            IronNetwork.send(var0, new IronPacket(var1.charge, var1.sequence, var1.start, var0.getGameTime(), var1.ticks, 1.0F, true));
        }
    }

    public static void observe(ServerLevel var0, SpellVisualPacket var1) {
        if (var1.valid() && IronPacket.matches(var1.spell().id)) {
            Entity var2 = var0.getEntity(var1.ownerId());
            if (var2 instanceof LivingEntity var3 && var2.getUUID().equals(var1.owner()) && eligible(var3, var1.spell().id)) {
                long var4 = var0.getGameTime();
                IronHooks.Cast var6 = STATES.getOrDefault(var0, Map.of()).get(new IronHooks.Key(var1.owner(), var1.spell().id));
                if (var6 != null) {
                    var6.touched = var4;
                }

                boolean var7 = var1.phase() == Phase.CHARGE;
                if (var7 && var6 != null && var6.released) {
                    return;
                }

                long var8 = var7 && var6 != null ? var6.start : var4;
                int var10 = var7 && var6 != null
                    ? var6.ticks
                    : IronClock.displayTicks(var1.spell().id, var1.phase().name(), var1.spell().lifetime(var1.phase()));
                if (var7 && var6 != null) {
                    var6.charge = var1;
                }

                long var11 = var6 == null ? var4 : (var1.phase() != Phase.PULSE && var1.phase() != Phase.BEAM ? var6.sequence : var6.fieldSequence);
                IronNetwork.send(var0, new IronPacket(var1, var11, var8, var4, var10, (float)SpellVisuals.radiusScale(var3, var1.spell().id), false));
                return;
            }
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        STATES.remove(var0.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent var0) {
        STATES.clear();
    }

    private static final class Cast {
        private long sequence;
        private long start;
        private long touched;
        private int ticks;
        private boolean released;
        private long fieldSequence;
        private SpellVisualPacket charge;

        private Cast(long var1, int var3, int var4) {
            this.sequence = var1;
            this.start = var1 - (long)Math.max(0, var3 - var4);
            this.touched = var1;
            this.ticks = var3;
        }
    }

    private static record Key(UUID owner, String spell) {
    }
}
