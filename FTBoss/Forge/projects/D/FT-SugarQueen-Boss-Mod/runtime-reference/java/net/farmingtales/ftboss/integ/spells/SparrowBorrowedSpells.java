/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorHealthBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSupportRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class SparrowBorrowedSpells {
    private static final Map<ServerLevel, Map<UUID, Cast>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Cast>>();
    private static final String NEXT = "ftboss_sparrow_borrow_next";
    private static final int MAX_ACTIVE = 64;

    private SparrowBorrowedSpells() {
    }

    private static boolean ready(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && !easterBossEntity.m_20147_() && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && !easterBossEntity.m_21525_();
    }

    private static boolean sameFight(EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2) {
        return Objects.equals(ManorFightMembers.fightId((Entity)easterBossEntity), ManorFightMembers.fightId((Entity)easterBossEntity2)) && Objects.equals(easterBossEntity.getManorSummonerId(), easterBossEntity2.getManorSummonerId());
    }

    public static boolean validField(ServerLevel serverLevel, EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, UUID uUID) {
        EasterBossEntity easterBossEntity2;
        Entity entity;
        return uUID != null && kind.variant != 9 && SparrowBorrowedSpells.ready(easterBossEntity) && ManorHealthBalance.support(easterBossEntity) && (entity = serverLevel.m_8791_(uUID)) instanceof EasterBossEntity && EasterBossEntity.isManorVariant((easterBossEntity2 = (EasterBossEntity)entity).getVariant()) && SparrowBorrowedSpells.ready(easterBossEntity2) && SparrowBorrowedSpells.sameFight(easterBossEntity2, easterBossEntity) && !easterBossEntity2.lemonSacrifice().active() && easterBossEntity2.m_20280_((Entity)easterBossEntity) <= 2304.0;
    }

    public static UUID ownerOf(LivingEntity livingEntity, ManorSpellRules.Kind kind) {
        Object object = livingEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ACTIVE.get(serverLevel);
        Cast cast = object == null ? null : (Cast)object.get(livingEntity.m_20148_());
        return cast != null && cast.kind() == kind ? cast.owner() : null;
    }

    public static boolean busy(LivingEntity livingEntity) {
        ServerLevel serverLevel;
        Level level = livingEntity.m_9236_();
        return level instanceof ServerLevel && ACTIVE.containsKey(serverLevel = (ServerLevel)level) && ACTIVE.get(serverLevel).containsKey(livingEntity.m_20148_());
    }

    private static boolean target(ServerLevel serverLevel, EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2, LivingEntity livingEntity) {
        return livingEntity instanceof ServerPlayer && livingEntity.m_9236_() == serverLevel && BossSpellEffects.canTarget((LivingEntity)easterBossEntity, livingEntity) && BossSpellEffects.canTarget((LivingEntity)easterBossEntity2, livingEntity) && easterBossEntity.m_20280_((Entity)livingEntity) <= 576.0 && easterBossEntity.m_142582_((Entity)livingEntity);
    }

    public static void tick(EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2) {
        LivingEntity livingEntity;
        ServerLevel serverLevel2;
        block10: {
            block9: {
                Level level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block9;
                serverLevel2 = (ServerLevel)level;
                if (SparrowBorrowedSpells.ready(easterBossEntity) && SparrowBorrowedSpells.ready(easterBossEntity2) && ManorHealthBalance.support(easterBossEntity) && SparrowBorrowedSpells.sameFight(easterBossEntity2, easterBossEntity) && !(easterBossEntity.m_20280_((Entity)easterBossEntity2) > 2304.0) && EasterBossEntity.isManorVariant(easterBossEntity2.getVariant()) && !easterBossEntity2.lemonSacrifice().active()) break block10;
            }
            return;
        }
        long l = serverLevel2.m_46467_();
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        long l2 = compoundTag.m_128454_(NEXT);
        if (!compoundTag.m_128441_(NEXT) || l2 - l > 200L) {
            compoundTag.m_128356_(NEXT, l + 200L);
            return;
        }
        if (l < l2 || easterBossEntity.isCastingLike() || easterBossEntity.isPerformingSkill()) {
            return;
        }
        Map map = ACTIVE.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
        map.entrySet().removeIf(entry -> serverLevel2.m_8791_((UUID)entry.getKey()) == null);
        if (map.containsKey(easterBossEntity.m_20148_()) || map.size() >= 64) {
            return;
        }
        LivingEntity livingEntity2 = livingEntity = SparrowBorrowedSpells.target(serverLevel2, easterBossEntity, easterBossEntity2, easterBossEntity2.m_5448_()) ? easterBossEntity2.m_5448_() : (LivingEntity)serverLevel2.m_6907_().stream().filter(serverPlayer -> SparrowBorrowedSpells.target(serverLevel2, easterBossEntity, easterBossEntity2, (LivingEntity)serverPlayer)).min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)easterBossEntity).m_20280_(arg_0))).orElse(null);
        if (livingEntity == null) {
            return;
        }
        List<ManorSpellRules.Kind> list = ManorSupportRules.pool(easterBossEntity.m_217043_().m_188503_(ManorSupportRules.variants())).stream().filter(kind -> ManorIronSpells.spell(kind).isEnabled()).toList();
        compoundTag.m_128356_(NEXT, l + 200L);
        if (list.isEmpty()) {
            return;
        }
        ManorSpellRules.Kind kind2 = list.get(easterBossEntity.m_217043_().m_188503_(list.size()));
        ManorSignatureSpell manorSignatureSpell = ManorIronSpells.spell(kind2);
        int n = manorSignatureSpell.getMinLevel();
        int n2 = Math.max(1, Math.min(200, manorSignatureSpell.getEffectiveCastTime(n, (LivingEntity)easterBossEntity)));
        MagicData magicData = new MagicData(true);
        magicData.setSyncedData(new SyncedSpellData((LivingEntity)easterBossEntity));
        if (!manorSignatureSpell.checkPreCastConditions((Level)serverLevel2, n, (LivingEntity)easterBossEntity, magicData)) {
            return;
        }
        Vec3 vec3 = kind2.support() ? easterBossEntity2.m_20182_() : livingEntity.m_20182_().m_82520_(0.0, 0.5, 0.0);
        easterBossEntity.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        magicData.initiateCast((AbstractSpell)manorSignatureSpell, n, n2, CastSource.MOB, "mainhand");
        manorSignatureSpell.onServerPreCast((Level)serverLevel2, n, (LivingEntity)easterBossEntity, magicData);
        magicData.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(vec3));
        map.put(easterBossEntity.m_20148_(), new Cast(easterBossEntity2.m_20148_(), livingEntity.m_20148_(), kind2, l, n2, n, magicData));
        easterBossEntity.playParallelSpell(n2);
        ManorSpellEffects.warning(serverLevel2, (LivingEntity)easterBossEntity, kind2, vec3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void advance(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Map<UUID, Cast> map, Cast cast) {
        Object object;
        LivingEntity livingEntity;
        long l = serverLevel.m_46467_() - cast.started();
        Object object2 = serverLevel.m_8791_(cast.target());
        LivingEntity livingEntity2 = object2 instanceof LivingEntity ? (livingEntity = (LivingEntity)object2) : null;
        Entity entity = serverLevel.m_8791_(cast.owner());
        if (entity instanceof EasterBossEntity) {
            object2 = (EasterBossEntity)entity;
            object = object2;
        } else {
            object = null;
        }
        livingEntity = object;
        object2 = ManorIronSpells.spell(cast.kind());
        if (!(SparrowBorrowedSpells.validField(serverLevel, easterBossEntity, cast.kind(), cast.owner()) && livingEntity != null && SparrowBorrowedSpells.target(serverLevel, easterBossEntity, (EasterBossEntity)livingEntity, livingEntity2) && object2.isEnabled() && l >= 0L && l <= (long)(cast.duration() + 20))) {
            SparrowBorrowedSpells.finish(serverLevel, easterBossEntity, map, cast, true);
            return;
        }
        easterBossEntity.m_21563_().m_24960_((Entity)livingEntity2, 60.0f, 60.0f);
        if (l < (long)cast.duration()) {
            cast.magic().handleCastDuration();
            ((ManorSignatureSpell)((Object)object2)).onServerCastTick((Level)serverLevel, cast.rank(), (LivingEntity)easterBossEntity, cast.magic());
            return;
        }
        try {
            ((ManorSignatureSpell)((Object)object2)).onCast((Level)serverLevel, cast.rank(), (LivingEntity)easterBossEntity, CastSource.MOB, cast.magic());
        }
        finally {
            SparrowBorrowedSpells.finish(serverLevel, easterBossEntity, map, cast, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void finish(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Map<UUID, Cast> map, Cast cast, boolean bl) {
        map.remove(easterBossEntity.m_20148_());
        try {
            ManorIronSpells.spell(cast.kind()).onServerCastComplete((Level)serverLevel, cast.rank(), (LivingEntity)easterBossEntity, cast.magic(), bl);
        }
        finally {
            easterBossEntity.finishParallelSpell();
        }
    }

    public static boolean allied(LivingEntity livingEntity, Entity entity) {
        LivingEntity livingEntity2;
        return livingEntity.m_7307_(entity) || entity instanceof LivingEntity && (SparrowBorrowedSpells.friendly((Entity)livingEntity, livingEntity2 = (LivingEntity)entity) || SparrowBorrowedSpells.friendly((Entity)livingEntity2, livingEntity));
    }

    public static boolean friendly(Entity entity, LivingEntity livingEntity) {
        if (livingEntity == null) {
            return false;
        }
        Object object = entity;
        for (int i = 0; object != null && i < 8; ++i) {
            EasterBossEntity easterBossEntity;
            if (object instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)object).getVariant() == 9) {
                ResourceLocation resourceLocation = BuiltInRegistries.f_256780_.m_7981_((Object)livingEntity.m_6095_());
                UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
                return livingEntity == easterBossEntity || easterBossEntity.m_7307_((Entity)livingEntity) || livingEntity.m_7307_((Entity)easterBossEntity) || resourceLocation != null && "ftboss".equals(resourceLocation.m_135827_()) || !(livingEntity instanceof Player) && uUID != null && uUID.equals(ManorFightMembers.fightId((Entity)livingEntity)) || ManorFightMembers.ownerId((Entity)livingEntity) != null && (ManorFightMembers.ownerId((Entity)livingEntity).equals(easterBossEntity.m_20148_()) || ManorFightMembers.ownerId((Entity)livingEntity).equals(ManorFightMembers.ownerId((Entity)easterBossEntity)));
            }
            easterBossEntity = ManorFightMembers.owner(object);
            if (easterBossEntity == object) break;
            object = easterBossEntity;
        }
        return false;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent livingAttackEvent) {
        if (SparrowBorrowedSpells.friendly(livingAttackEvent.getSource().m_7639_(), livingAttackEvent.getEntity()) || SparrowBorrowedSpells.friendly(livingAttackEvent.getSource().m_7640_(), livingAttackEvent.getEntity())) {
            livingAttackEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent levelTickEvent) {
        Object object;
        if (levelTickEvent.phase != TickEvent.Phase.END || !((object = levelTickEvent.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ACTIVE.get(serverLevel);
        if (object == null) {
            return;
        }
        for (UUID uUID : List.copyOf(object.keySet())) {
            Entity entity = serverLevel.m_8791_(uUID);
            if (entity instanceof EasterBossEntity) {
                EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
                SparrowBorrowedSpells.advance(serverLevel, easterBossEntity, (Map<UUID, Cast>)object, (Cast)object.get(uUID));
                continue;
            }
            object.remove(uUID);
        }
        if (object.isEmpty()) {
            ACTIVE.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        ACTIVE.remove(unload.getLevel());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        ACTIVE.clear();
    }

    private record Cast(UUID owner, UUID target, ManorSpellRules.Kind kind, long started, int duration, int rank, MagicData magic) {
    }
}

