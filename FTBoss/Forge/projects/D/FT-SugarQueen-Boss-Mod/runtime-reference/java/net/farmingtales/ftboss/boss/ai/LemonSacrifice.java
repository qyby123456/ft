/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.LemonInheritedAbilities;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.lemonmotion.LemonMotionServer;
import net.farmingtales.ftboss.lemonparty.LemonPartyRules;
import net.farmingtales.ftboss.skill.LemonCatNetwork;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class LemonSacrifice {
    public static final int DURATION = 36000;
    public static final boolean INFINITE_UNTIL_DEATH = true;
    private static final long INFINITE_UNTIL = 0x3FFFFFFFFFFFFFFFL;
    public static final String DATA = "ftboss:lemon_sacrifice";
    private static final String CHILD_EXPIRY = "ftboss:lemon_inherited_until";
    private final EasterBossEntity owner;
    private final LemonInheritedAbilities abilities;
    private boolean granting;
    private boolean applyingEffect;
    private List<BossSpellRules.Kind> cachedSpells;
    public static final int MIN_SUMMONED_BOSSES = 2;
    public static final int POST_SACRIFICE_HELPER_DEFEAT_LIMIT = 3;
    private static final String DEFEATED_HELPERS = "defeated_helpers";
    public static final float HALF_HEALTH_TRIGGER_RATIO = 0.5f;

    public static long untilFrom(long gameTime) {
        return 0x3FFFFFFFFFFFFFFFL;
    }

    public static int grantTicks() {
        return Integer.MAX_VALUE;
    }

    public LemonSacrifice(EasterBossEntity owner) {
        this.owner = owner;
        this.abilities = new LemonInheritedAbilities(owner, this);
    }

    public CompoundTag state() {
        CompoundTag data = this.owner.getPersistentData();
        if (!data.m_128425_(DATA, 10)) {
            data.m_128365_(DATA, (Tag)new CompoundTag());
        }
        return data.m_128469_(DATA);
    }

    public boolean used() {
        return this.state().m_128471_("used");
    }

    public boolean active() {
        return this.owner.getVariant() == 4 && !this.owner.isMini() && !this.owner.isGiant() && this.owner.m_6084_() && !this.owner.m_213877_() && this.used() && this.owner.m_9236_().m_46467_() < this.state().m_128454_("until");
    }

    public boolean has(String profile) {
        return this.active() && this.state().m_128469_("profiles").m_128471_(profile);
    }

    public int inheritedBossCount() {
        if (!this.active()) {
            return 0;
        }
        if (this.state().m_128425_("bosses", 99)) {
            return Math.max(0, this.state().m_128451_("bosses"));
        }
        return (int)this.state().m_128469_("profiles").m_128431_().stream().filter(LemonSacrifice::bossProfile).filter(this::has).count();
    }

    public int spellsPerCast() {
        return 2 + Math.min(3, this.inheritedBossCount());
    }

    private static boolean bossProfile(String profile) {
        return switch (profile) {
            case "sugar_queen", "meat_mountain", "tea_tree", "wine_barrel", "rat_boss" -> true;
            default -> false;
        };
    }

    public int defeatedPostSacrificeHelpers() {
        return this.used() ? Math.max(0, this.state().m_128451_(DEFEATED_HELPERS)) : 0;
    }

    public boolean canSummonHelpers() {
        return !this.used() || this.defeatedPostSacrificeHelpers() < LemonPartyRules.helperLimit(this.owner);
    }

    public int summonCount() {
        if (!this.canSummonHelpers()) {
            return 0;
        }
        return this.used() ? LemonPartyRules.summonCount(this.owner, 1) : LemonPartyRules.summonCount(this.owner, 2);
    }

    public boolean recordPostSacrificeHelperDefeat() {
        if (!this.used() || this.owner.getVariant() != 4 || this.owner.isMini() || this.owner.isGiant() || !this.owner.m_6084_()) {
            return false;
        }
        int defeated = this.defeatedPostSacrificeHelpers();
        if (defeated >= LemonPartyRules.helperLimit(this.owner)) {
            return false;
        }
        this.state().m_128405_(DEFEATED_HELPERS, ++defeated);
        if (defeated >= LemonPartyRules.helperLimit(this.owner)) {
            this.announce("\u00a7e\u52a9\u9635 Boss \u5df2\u8fbe\u5230\u51fb\u8d25\u4e0a\u9650\uff0c\u67e0\u6aac\u732b\u4e0d\u518d\u53ec\u5524 Boss\uff01");
            FTBossMod.LOGGER.info("[FTBoss][Lemon] \u732e\u796d\u540e\u52a9\u6218 Boss \u5df2\u88ab\u51fb\u8d25 {}/{}\uff0c\u6c38\u4e45\u505c\u6b62\u7ee7\u7eed\u53ec\u5524", (Object)defeated, (Object)LemonPartyRules.helperLimit(this.owner));
        } else {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] \u732e\u796d\u540e\u52a9\u6218 Boss \u88ab\u51fb\u8d25: {}/{}", (Object)defeated, (Object)LemonPartyRules.helperLimit(this.owner));
        }
        return true;
    }

    public boolean halfHealthFallbackReady() {
        return !this.used() && this.owner.getVariant() == 4 && !this.owner.isMini() && !this.owner.isGiant() && this.owner.m_6084_() && !this.owner.m_213877_() && !this.owner.m_21525_() && !this.owner.m_20147_() && this.owner.m_21233_() > 0.0f && this.owner.m_21223_() > 0.0f && this.owner.m_21223_() <= this.owner.m_21233_() * 0.5f;
    }

    public boolean owns(Entity entity) {
        return entity != this.owner && (this.owner.m_20148_().equals(ManorFightMembers.ownerId(entity)) || this.owner.m_20148_().equals(MinionBlessing.ownerId(entity)));
    }

    public boolean inheritedChild(Entity entity) {
        return this.owns(entity) && entity.getPersistentData().m_128454_(CHILD_EXPIRY) == this.state().m_128454_("until") && entity.getPersistentData().m_128441_(CHILD_EXPIRY);
    }

    public int summonBossCount(ServerLevel level) {
        return this.owner.aliveSummonBosses();
    }

    public boolean countsAsSummonedBoss(Entity entity) {
        Mob mob;
        return entity instanceof Mob && (mob = (Mob)entity).m_6084_() && this.owns((Entity)mob) && LemonSacrifice.isSummonedBoss(mob);
    }

    private static boolean isSummonedBoss(Mob mob) {
        if (mob instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)mob;
            return boss.getVariant() != 9 && !boss.isMini() && !boss.isGiant();
        }
        return LemonSacrifice.bossProfile(LemonSacrifice.profile(mob));
    }

    private static boolean isSacrificeable(Mob mob) {
        return LemonSacrifice.isSummonedBoss(mob);
    }

    public static boolean isInheritedSummon(Entity entity) {
        return entity.getPersistentData().m_128441_(CHILD_EXPIRY);
    }

    public boolean trySacrifice(Collection<UUID> directSummons) {
        boolean found;
        Mob mob;
        ServerLevel level;
        block12: {
            block11: {
                Level level2 = this.owner.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block11;
                level = (ServerLevel)level2;
                if (!this.used() && this.owner.m_6084_() && !this.owner.m_21525_() && !this.owner.m_20147_() && !this.owner.isMini() && !this.owner.isGiant() && this.owner.getVariant() == 4) break block12;
            }
            return false;
        }
        int summonedBosses = this.summonBossCount(level);
        if (summonedBosses < LemonPartyRules.absorbMinimum(this.owner)) {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] \u732e\u796d\u6761\u4ef6\u672a\u6ee1\u8db3: \u573a\u4e0a\u52a9\u6218 Boss {}/{} \u2014\u2014 \u672c\u6b21\u4e0d\u732e\u796d", (Object)summonedBosses, (Object)LemonPartyRules.absorbMinimum(this.owner));
            return false;
        }
        ArrayList<Mob> victims = new ArrayList<Mob>();
        ArrayList<Mob> candidates = new ArrayList<Mob>();
        HashSet<UUID> family = new HashSet<UUID>(directSummons);
        family.add(this.owner.m_20148_());
        for (Entity entity : level.m_8583_()) {
            if (!(entity instanceof Mob) || !(mob = (Mob)entity).m_6084_() || mob == this.owner || !LemonSacrifice.isSacrificeable(mob)) continue;
            candidates.add(mob);
        }
        do {
            found = false;
            Iterator iterator = candidates.iterator();
            while (iterator.hasNext()) {
                mob = (Mob)iterator.next();
                Entity parent = ManorFightMembers.owner((Entity)mob);
                if (!family.contains(mob.m_20148_()) && !this.owns((Entity)mob) && !family.contains(MinionBlessing.ownerId((Entity)mob)) && (parent == null || !family.contains(parent.m_20148_()))) continue;
                family.add(mob.m_20148_());
                victims.add(mob);
                iterator.remove();
                found = true;
            }
        } while (found && !candidates.isEmpty());
        if (victims.isEmpty()) {
            return false;
        }
        CompoundTag state = this.state();
        CompoundTag profiles = new CompoundTag();
        state.m_128379_("used", true);
        state.m_128356_("until", LemonSacrifice.untilFrom(level.m_46467_()));
        state.m_128405_(DEFEATED_HELPERS, 0);
        state.m_128405_("sacrificed", victims.size());
        state.m_128405_("bosses", (int)victims.stream().filter(victim -> LemonSacrifice.bossProfile(LemonSacrifice.profile(victim))).count());
        state.m_128365_("profiles", (Tag)profiles);
        state.m_128365_("effects", (Tag)new CompoundTag());
        for (Mob victim2 : victims) {
            String profile = LemonSacrifice.profile(victim2);
            if (profile.isEmpty()) continue;
            profiles.m_128379_(profile, true);
        }
        LemonCatNetwork.sacrifice((LivingEntity)this.owner, victims);
        for (Mob victim2 : victims) {
            Vec3 origin = victim2.m_20182_().m_82520_(0.0, (double)victim2.m_20206_() * 0.5, 0.0);
            Vec3 destination = this.owner.m_20182_().m_82520_(0.0, 1.1, 0.0);
            for (int step = 0; step < 12; ++step) {
                Vec3 point = origin.m_165921_(destination, (double)step / 11.0);
                level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.04, 0.04, 0.04, 0.0);
            }
            victim2.m_146870_();
        }
        LemonMotionServer.absorb(this.owner, 14, 3);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123767_, this.owner.m_20185_(), this.owner.m_20186_() + 1.0, this.owner.m_20189_(), 48, 0.7, 1.0, 0.7, 0.06);
        level.m_5594_(null, this.owner.m_20183_(), SoundEvents.f_12513_, SoundSource.HOSTILE, 1.1f, 0.7f);
        this.announce("\u00a7e\u67e0\u6aac\u732b\u732e\u796d\u4e86 " + victims.size() + " \u4e2a\u53ec\u5524\u7269\uff0c\u7ee7\u627f\u5176\u80fd\u529b\u2014\u2014\u00a7c\u76f4\u5230\u5b83\u6b7b\u4ea1\u00a7e\uff01\u4e4b\u540e\u53ec\u5524\u6570\u91cf\u548c\u505c\u6b62\u6761\u4ef6\u6309\u5f53\u524d\u5355\u4eba/\u591a\u4eba\u89c4\u5219\u6267\u884c\u3002");
        FTBossMod.LOGGER.info("[FTBoss][Lemon] sacrificed {} summons, profiles={}, until={}", new Object[]{victims.size(), profiles, state.m_128454_("until")});
        return true;
    }

    private static String profile(Mob mob) {
        RatBossEntity rat;
        if (mob instanceof RatBossEntity && (rat = (RatBossEntity)mob).isMinion()) {
            return "rat_minion";
        }
        return switch (EntityType.m_20613_((EntityType)mob.m_6095_()).toString()) {
            case "ftboss:sugar_queen" -> "sugar_queen";
            case "ftboss:meat_mountain" -> "meat_mountain";
            case "ftboss:tea_tree" -> "tea_tree";
            case "ftboss:tea_minion" -> "tea_minion";
            case "ftboss:wine_barrel" -> "wine_barrel";
            case "ftboss:barrel_minion" -> "barrel_minion";
            case "ftboss:rat_boss" -> "rat_boss";
            default -> "";
        };
    }

    public List<BossSpellRules.Kind> spells() {
        if (!this.active()) {
            return List.of();
        }
        if (this.cachedSpells == null) {
            this.cachedSpells = this.state().m_128469_("profiles").m_128431_().stream().sorted().flatMap(profile -> BossSpellRules.forBoss("ftboss:" + profile).stream()).distinct().toList();
        }
        return this.cachedSpells;
    }

    public void tick() {
        Level level;
        if (this.owner.m_9236_().f_46443_ || !this.used()) {
            return;
        }
        if (!this.active()) {
            this.expire();
            return;
        }
        if (this.owner.m_21525_() || this.owner.m_20147_()) {
            this.abilities.pause();
            return;
        }
        this.runInherited(this.abilities::tick);
        if (this.owner.f_19797_ % 40 == 0 && (level = this.owner.m_9236_()) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.m_8767_((ParticleOptions)ParticleTypes.f_123809_, this.owner.m_20185_(), this.owner.m_20186_() + 1.1, this.owner.m_20189_(), 5, 0.6, 0.6, 0.6, 0.04);
        }
    }

    public void runInherited(Runnable action) {
        boolean previous = this.granting;
        this.granting = true;
        try {
            action.run();
        }
        finally {
            this.granting = previous;
        }
    }

    public boolean capturesEffects() {
        return this.granting && !this.applyingEffect && this.active();
    }

    public static boolean allowsSpellEffect(LivingEntity caster) {
        EasterBossEntity lemon;
        return !(caster instanceof EasterBossEntity) || !(lemon = (EasterBossEntity)caster).lemonSacrifice().used() || lemon.lemonSacrifice().active();
    }

    public static void spellEffect(LivingEntity caster, Runnable action) {
        EasterBossEntity lemon;
        if (caster instanceof EasterBossEntity && (lemon = (EasterBossEntity)caster).lemonSacrifice().active()) {
            lemon.lemonSacrifice().runInherited(action);
        } else {
            action.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean grant(MobEffectInstance effect) {
        boolean applied;
        if (!this.active() || !effect.m_19544_().m_19486_()) {
            return false;
        }
        int remaining = (int)Math.min((long)LemonSacrifice.grantTicks(), this.state().m_128454_("until") - this.owner.m_9236_().m_46467_());
        MobEffectInstance current = this.owner.m_21124_(effect.m_19544_());
        int duration = Math.min(remaining, effect.m_267577_() ? remaining : effect.m_19557_());
        if (current != null && (current.m_19564_() > effect.m_19564_() || current.m_19564_() == effect.m_19564_() && (current.m_267577_() || current.m_19557_() >= duration))) {
            return false;
        }
        CompoundTag effects = this.state().m_128469_("effects");
        String key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect.m_19544_()).toString();
        CompoundTag lease = effects.m_128469_(key);
        if (!effects.m_128441_(key) && current != null) {
            lease.m_128365_("previous", (Tag)current.m_19555_(new CompoundTag()));
            lease.m_128356_("previousUntil", current.m_267577_() ? Long.MAX_VALUE : this.owner.m_9236_().m_46467_() + (long)current.m_19557_());
        }
        MobEffectInstance granted = new MobEffectInstance(effect.m_19544_(), duration, effect.m_19564_(), true, true, true);
        this.applyingEffect = true;
        try {
            applied = this.owner.m_7292_(granted);
        }
        finally {
            this.applyingEffect = false;
        }
        if (applied) {
            lease.m_128405_("amplifier", granted.m_19564_());
            lease.m_128405_("duration", duration);
            effects.m_128365_(key, (Tag)lease);
        }
        return applied;
    }

    private void expire() {
        String key2;
        if (this.state().m_128471_("expired")) {
            return;
        }
        this.abilities.reset();
        CompoundTag effects = this.state().m_128469_("effects");
        for (String key2 : effects.m_128431_()) {
            MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(key2));
            if (effect == null) continue;
            MobEffectInstance current = this.owner.m_21124_(effect);
            CompoundTag lease = effects.m_128469_(key2);
            if (current == null || current.m_19564_() != lease.m_128451_("amplifier") || current.m_267577_() || current.m_19557_() > lease.m_128451_("duration")) continue;
            this.owner.m_21195_(effect);
            MobEffectInstance previous = MobEffectInstance.m_19560_((CompoundTag)lease.m_128469_("previous"));
            long left = lease.m_128454_("previousUntil") - this.owner.m_9236_().m_46467_();
            if (previous == null || left <= 0L) continue;
            this.owner.m_7292_(new MobEffectInstance(effect, previous.m_267577_() ? -1 : (int)Math.min(Integer.MAX_VALUE, left), previous.m_19564_()));
        }
        this.owner.getPersistentData().m_128473_("ftboss:signature_cast");
        key2 = this.owner.m_9236_();
        if (key2 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)key2;
            ArrayList<Entity> children = new ArrayList<Entity>();
            for (Entity entity : level.m_8583_()) {
                if (!this.owns(entity) || !entity.getPersistentData().m_128441_(CHILD_EXPIRY)) continue;
                children.add(entity);
            }
            children.forEach(Entity::m_146870_);
        }
        this.state().m_128379_("expired", true);
        this.state().m_128473_("effects");
        if (this.owner.m_6084_()) {
            this.announce("\u00a77\u67e0\u6aac\u732b\u7684\u732e\u796d\u7ee7\u627f\u5df2\u7ed3\u675f\uff1b\u732e\u796d\u4e0e\u52a9\u6218\u51fb\u8d25\u8ba1\u6570\u4e0d\u4f1a\u91cd\u7f6e\u3002");
        }
    }

    private void announce(String message) {
        Level level = this.owner.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            for (ServerPlayer player : level2.m_6907_()) {
                if (!(player.m_20280_((Entity)this.owner) <= 4096.0)) continue;
                Component.m_237113_((String)message);
            }
        }
    }

    public boolean shielded(DamageSource source) {
        return false;
    }

    public void onMeleeHit(LivingEntity target, float dealt) {
        if (this.active()) {
            this.runInherited(() -> this.abilities.onMeleeHit(target, dealt));
        }
    }

    public void onRemoval(Entity.RemovalReason reason) {
        if (!this.owner.m_9236_().f_46443_ && this.used()) {
            this.abilities.pause();
            if (reason.m_146965_()) {
                this.expire();
            }
        }
    }

    public static void inheritLifetime(Entity parent, Entity child) {
        EasterBossEntity boss;
        long until = parent.getPersistentData().m_128454_(CHILD_EXPIRY);
        if (parent instanceof EasterBossEntity && (boss = (EasterBossEntity)parent).lemonSacrifice().active() && boss.lemonSacrifice().granting) {
            until = boss.lemonSacrifice().state().m_128454_("until");
        }
        if (until > 0L) {
            child.getPersistentData().m_128356_(CHILD_EXPIRY, until);
        }
    }

    public static EasterBossEntity ratLeader(Entity rat) {
        EasterBossEntity boss;
        Entity entity;
        ServerLevel level;
        block3: {
            block2: {
                Level level2 = rat.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block2;
                level = (ServerLevel)level2;
                if (rat.getPersistentData().m_128441_(CHILD_EXPIRY)) break block3;
            }
            return null;
        }
        UUID owner = ManorFightMembers.ownerId(rat);
        return owner != null && (entity = level.m_8791_(owner)) instanceof EasterBossEntity && (boss = (EasterBossEntity)entity).lemonSacrifice().has("rat_boss") ? boss : null;
    }

    @SubscribeEvent
    public static void fireRecovery(LivingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity lemon = (EasterBossEntity)livingEntity;
            if (!lemon.m_9236_().f_46443_ && lemon.lemonSacrifice().has("wine_barrel") && event.getAmount() > 0.0f && event.getSource().m_269533_(DamageTypeTags.f_268745_)) {
                lemon.m_5634_(event.getAmount() * 0.75f);
            }
        }
    }

    @SubscribeEvent
    public static void expireSummon(LivingEvent.LivingTickEvent event) {
        block4: {
            LivingEntity entity;
            block5: {
                EasterBossEntity boss;
                Level level;
                entity = event.getEntity();
                if (entity.m_9236_().f_46443_) {
                    return;
                }
                long until = entity.getPersistentData().m_128454_(CHILD_EXPIRY);
                if (until <= 0L) break block4;
                if (entity.m_9236_().m_46467_() >= until) break block5;
                if (entity.f_19797_ % 20 != 0 || !((level = entity.m_9236_()) instanceof ServerLevel)) break block4;
                ServerLevel level2 = (ServerLevel)level;
                if (ManorFightMembers.ownerId((Entity)entity) == null || !((level = level2.m_8791_(ManorFightMembers.ownerId((Entity)entity))) instanceof EasterBossEntity) || (boss = (EasterBossEntity)level).lemonSacrifice().active()) break block4;
            }
            entity.m_146870_();
        }
    }

    @SubscribeEvent
    public static void preventExpiredJoin(EntityJoinLevelEvent event) {
        long until = event.getEntity().getPersistentData().m_128454_(CHILD_EXPIRY);
        if (!event.getLevel().f_46443_ && until > 0L && event.getLevel().m_46467_() >= until) {
            event.setCanceled(true);
        }
    }
}

