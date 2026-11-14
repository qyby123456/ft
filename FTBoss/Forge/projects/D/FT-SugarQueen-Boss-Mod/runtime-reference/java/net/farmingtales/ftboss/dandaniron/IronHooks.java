/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.dandaniron;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.dandaniron.IronClock;
import net.farmingtales.ftboss.dandaniron.IronNetwork;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class IronHooks {
    private static final Map<ServerLevel, Map<Key, Cast>> STATES = new WeakHashMap<ServerLevel, Map<Key, Cast>>();

    private IronHooks() {
    }

    private static boolean eligible(LivingEntity livingEntity, String string) {
        EasterBossEntity easterBossEntity;
        return IronPacket.matches(string) && livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 1 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_();
    }

    public static void begin(ManorSignatureSpell manorSignatureSpell, int n, LivingEntity livingEntity, MagicData magicData) {
        Vec3 vec3;
        ICastData iCastData;
        Level level;
        if (!IronHooks.eligible(livingEntity, manorSignatureSpell.kind.id) || !((level = livingEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel2 = (ServerLevel)level;
        int n2 = Math.max(1, magicData != null && magicData.getCastDuration() > 0 ? magicData.getCastDuration() : manorSignatureSpell.getEffectiveCastTime(n, livingEntity));
        int n3 = magicData != null && magicData.getCastDurationRemaining() > 0 ? Math.min(n2, magicData.getCastDurationRemaining()) : n2;
        Map map = STATES.computeIfAbsent(serverLevel2, serverLevel -> new LinkedHashMap());
        map.entrySet().removeIf(entry -> serverLevel2.m_46467_() - ((Cast)entry.getValue()).touched > 1200L);
        if (map.size() >= 64) {
            map.remove(map.keySet().iterator().next());
        }
        Key key = new Key(livingEntity.m_20148_(), manorSignatureSpell.kind.id);
        Cast cast = (Cast)map.get(key);
        Cast cast2 = new Cast(serverLevel2.m_46467_(), n2, n3);
        cast2.fieldSequence = cast == null ? cast2.sequence : cast.fieldSequence;
        map.put(key, cast2);
        if (magicData != null && (iCastData = magicData.getAdditionalCastData()) instanceof BossSignatureSpell.Aim) {
            BossSignatureSpell.Aim aim = (BossSignatureSpell.Aim)iCastData;
            vec3 = aim.point();
        } else {
            vec3 = livingEntity.m_20182_();
        }
        Vec3 vec32 = vec3;
        SpellVisuals.show(serverLevel2, livingEntity, manorSignatureSpell.kind.id, SpellVisualRules.Phase.CHARGE, vec32);
    }

    public static void tick(ManorSignatureSpell manorSignatureSpell, int n, LivingEntity livingEntity, MagicData magicData) {
        Object object;
        if (!IronHooks.eligible(livingEntity, manorSignatureSpell.kind.id) || !((object = livingEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Cast)STATES.getOrDefault(serverLevel, Map.of()).get(new Key(livingEntity.m_20148_(), manorSignatureSpell.kind.id));
        if (object == null) {
            IronHooks.begin(manorSignatureSpell, n, livingEntity, magicData);
            return;
        }
        if (magicData != null && magicData.getCastDuration() > 0) {
            object.ticks = magicData.getCastDuration();
            object.start = serverLevel.m_46467_() - (long)Math.max(0, object.ticks - magicData.getCastDurationRemaining());
        }
        object.touched = serverLevel.m_46467_();
    }

    public static void release(ManorSignatureSpell manorSignatureSpell, LivingEntity livingEntity) {
        Object object;
        if (!IronHooks.eligible(livingEntity, manorSignatureSpell.kind.id) || !((object = livingEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Cast)STATES.getOrDefault(serverLevel, Map.of()).get(new Key(livingEntity.m_20148_(), manorSignatureSpell.kind.id));
        if (object != null) {
            object.released = true;
            object.fieldSequence = object.sequence;
            object.touched = serverLevel.m_46467_();
            IronHooks.cancelCharge(serverLevel, (Cast)object);
        }
    }

    public static void complete(ManorSignatureSpell manorSignatureSpell, LivingEntity livingEntity, boolean bl) {
        Object object;
        if (!IronHooks.eligible(livingEntity, manorSignatureSpell.kind.id) || !((object = livingEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Cast)STATES.getOrDefault(serverLevel, Map.of()).get(new Key(livingEntity.m_20148_(), manorSignatureSpell.kind.id));
        if (object != null) {
            IronHooks.cancelCharge(serverLevel, (Cast)object);
            object.touched = serverLevel.m_46467_();
        }
    }

    private static void cancelCharge(ServerLevel serverLevel, Cast cast) {
        if (cast.charge != null) {
            IronNetwork.send(serverLevel, new IronPacket(cast.charge, cast.sequence, cast.start, serverLevel.m_46467_(), cast.ticks, 1.0f, true));
        }
    }

    public static void observe(ServerLevel serverLevel, SpellVisualPacket spellVisualPacket) {
        int n;
        boolean bl;
        LivingEntity livingEntity;
        block8: {
            block7: {
                if (!spellVisualPacket.valid() || !IronPacket.matches(spellVisualPacket.spell().id)) {
                    return;
                }
                Entity entity = serverLevel.m_6815_(spellVisualPacket.ownerId());
                if (!(entity instanceof LivingEntity)) break block7;
                livingEntity = (LivingEntity)entity;
                if (entity.m_20148_().equals(spellVisualPacket.owner()) && IronHooks.eligible(livingEntity, spellVisualPacket.spell().id)) break block8;
            }
            return;
        }
        long l = serverLevel.m_46467_();
        Cast cast = (Cast)STATES.getOrDefault(serverLevel, Map.of()).get(new Key(spellVisualPacket.owner(), spellVisualPacket.spell().id));
        if (cast != null) {
            cast.touched = l;
        }
        boolean bl2 = bl = spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE;
        if (bl && cast != null && cast.released) {
            return;
        }
        long l2 = bl && cast != null ? cast.start : l;
        int n2 = n = bl && cast != null ? cast.ticks : IronClock.displayTicks(spellVisualPacket.spell().id, spellVisualPacket.phase().name(), spellVisualPacket.spell().lifetime(spellVisualPacket.phase()));
        if (bl && cast != null) {
            cast.charge = spellVisualPacket;
        }
        long l3 = cast == null ? l : (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE || spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM ? cast.fieldSequence : cast.sequence);
        IronNetwork.send(serverLevel, new IronPacket(spellVisualPacket, l3, l2, l, n, (float)SpellVisuals.radiusScale((Entity)livingEntity, spellVisualPacket.spell().id), false));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        STATES.remove(unload.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        STATES.clear();
    }

    private record Key(UUID owner, String spell) {
    }

    private static final class Cast {
        private long sequence;
        private long start;
        private long touched;
        private int ticks;
        private boolean released;
        private long fieldSequence;
        private SpellVisualPacket charge;

        private Cast(long l, int n, int n2) {
            this.sequence = l;
            this.start = l - (long)Math.max(0, n - n2);
            this.touched = l;
            this.ticks = n;
        }
    }
}

