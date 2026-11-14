package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class LemonSacrifice {
    /** 旧口径：献祭继承窗口 30 分钟（{@link #INFINITE_UNTIL_DEATH} 打开时不再使用，只作为效果单次时长上限的兜底）。 */
    public static final int DURATION = 30 * 60 * 20;

    /** ★ 2026-09-19 按用户要求「柠檬的能力获取时间改成无限时间直到死亡」：
        继承窗口不再按 30 分钟过期，而是**直到柠檬猫死亡/被移除**才结束
        （{@code active()} 仍要求 isAlive / !isRemoved，{@link #onRemoval} 里也会 pause + expire，
        所以死亡那一刻继承来的技能、法术与召唤物一起收掉）。 */
    public static final boolean INFINITE_UNTIL_DEATH = true;

    /** 无限窗口用的时间戳：大到永远追不上，又不会在后续减法里溢出。 */
    private static final long INFINITE_UNTIL = Long.MAX_VALUE / 2;

    /** 继承窗口结束的游戏刻（无限模式下给一个极大的常量）。 */
    public static long untilFrom(long gameTime) {
        return INFINITE_UNTIL_DEATH ? INFINITE_UNTIL : gameTime + DURATION;
    }

    /** 单次授予效果的时长：无限模式下≈永久（随死亡一起消失），否则就是 30 分钟。 */
    public static int grantTicks() {
        return INFINITE_UNTIL_DEATH ? Integer.MAX_VALUE : DURATION;
    }
    public static final String DATA = "ftboss:lemon_sacrifice";
    private static final String CHILD_EXPIRY = "ftboss:lemon_inherited_until";
    private final EasterBossEntity owner;
    private final LemonInheritedAbilities abilities;
    private boolean granting;
    private boolean applyingEffect;
    private List<BossSpellRules.Kind> cachedSpells;

    public LemonSacrifice(EasterBossEntity owner) {
        this.owner = owner;
        this.abilities = new LemonInheritedAbilities(owner, this);
    }

    public CompoundTag state() {
        CompoundTag data = this.owner.getPersistentData();
        if (!data.contains(DATA, 10)) data.put(DATA, new CompoundTag());
        return data.getCompound(DATA);
    }

    public boolean used() {
        return state().getBoolean("used");
    }

    public boolean active() {
        return this.owner.getVariant() == EasterBossEntity.LEMONCAT_VARIANT && !this.owner.isMini()
                && !this.owner.isGiant() && this.owner.isAlive() && !this.owner.isRemoved()
                && used() && this.owner.level().getGameTime() < state().getLong("until");
    }

    public boolean has(String profile) {
        return active() && state().getCompound("profiles").getBoolean(profile);
    }

    public int inheritedBossCount() {
        if (!active()) return 0;
        if (state().contains("bosses", 99)) return Math.max(0, state().getInt("bosses"));
        return (int) state().getCompound("profiles").getAllKeys().stream()
                .filter(LemonSacrifice::bossProfile).filter(this::has).count();
    }

    public int spellsPerCast() {
        return 2 + Math.min(3, inheritedBossCount());
    }

    private static boolean bossProfile(String profile) {
        return switch (profile) {
            case "sugar_queen", "meat_mountain", "tea_tree", "wine_barrel", "rat_boss" -> true;
            default -> false;
        };
    }

    public static final int MIN_SUMMONED_BOSSES = 3;

    public int requiredBosses() {
        return LemonCombatBalance.requiredBosses(this.owner);
    }
    /** 献祭后真正被击败的助战 Boss 达到这个数量，就永久停止继续召唤。 */
    public static final int POST_SACRIFICE_HELPER_DEFEAT_LIMIT = 3;
    private static final String DEFEATED_HELPERS = "defeated_helpers";

    public int defeatedPostSacrificeHelpers() {
        return used() ? Math.max(0, state().getInt(DEFEATED_HELPERS)) : 0;
    }

    public boolean canSummonHelpers() {
        return !used() || defeatedPostSacrificeHelpers() < POST_SACRIFICE_HELPER_DEFEAT_LIMIT;
    }

    public int summonCount() {
        if (!canSummonHelpers()) return 0;
        return used() ? net.farmingtales.ftboss.lemonparty.LemonPartyRules.summonCount(this.owner, 1) : requiredBosses();
    }

    /**
     * 只由“献祭之后直接召出的助战 Boss 真正死亡”调用。超时、换批、转场与随主清理都是
     * {@code discard()}，不会走这里，也就不会冒充“被玩家击败”。
     */
    public boolean recordPostSacrificeHelperDefeat() {
        if (!used() || this.owner.getVariant() != EasterBossEntity.LEMONCAT_VARIANT
                || this.owner.isMini() || this.owner.isGiant() || !this.owner.isAlive()) return false;
        int defeated = defeatedPostSacrificeHelpers();
        if (defeated >= POST_SACRIFICE_HELPER_DEFEAT_LIMIT) return false;
        defeated++;
        state().putInt(DEFEATED_HELPERS, defeated);
        if (defeated >= POST_SACRIFICE_HELPER_DEFEAT_LIMIT) {
            announce("§e三位助阵 Boss 已被击败，柠檬猫不再召唤 Boss！");
            FTBossMod.LOGGER.info("[FTBoss][Lemon] 献祭后助战 Boss 已被击败 {}/{}，永久停止继续召唤",
                    defeated, POST_SACRIFICE_HELPER_DEFEAT_LIMIT);
        } else {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] 献祭后助战 Boss 被击败: {}/{}",
                    defeated, POST_SACRIFICE_HELPER_DEFEAT_LIMIT);
        }
        return true;
    }

    /** 未在前半血完成献祭时的保底触发线。 */
    public static final float HALF_HEALTH_TRIGGER_RATIO = 0.5F;

    /**
     * 半血保底只在真正可战斗的柠檬猫身上生效；演出冻结、无敌、迷你/巨型与死亡状态
     * 都等到恢复为合法战斗状态后再触发，避免在庄园转场里凭空生成祭品。
     */
    public boolean halfHealthFallbackReady() {
        return !used() && this.owner.getVariant() == EasterBossEntity.LEMONCAT_VARIANT
                && !this.owner.isMini() && !this.owner.isGiant() && this.owner.isAlive() && !this.owner.isRemoved()
                && !this.owner.isNoAi() && !this.owner.isInvulnerable() && this.owner.getMaxHealth() > 0.0F
                && this.owner.getHealth() > 0.0F
                && this.owner.getHealth() <= this.owner.getMaxHealth() * HALF_HEALTH_TRIGGER_RATIO;
    }

    public boolean owns(Entity entity) {
        return entity != this.owner && (this.owner.getUUID().equals(ManorFightMembers.ownerId(entity))
                || this.owner.getUUID().equals(MinionBlessing.ownerId(entity)));
    }

    public boolean inheritedChild(Entity entity) {
        return owns(entity) && entity.getPersistentData().getLong(CHILD_EXPIRY) == state().getLong("until")
                && entity.getPersistentData().contains(CHILD_EXPIRY);
    }

    /**
     * 场上**助战 Boss** 的数量 —— 走 {@link EasterBossEntity#aliveSummonBosses()}，
     * 也就是柠檬猫「喊帮手」真正登记下来的那几位（最多 {@link #summonCount()} 位）。
     */
    public int summonBossCount(ServerLevel level) {
        return this.owner.aliveSummonBosses();
    }

    public boolean countsAsSummonedBoss(Entity entity) {
        return entity instanceof Mob mob && mob.isAlive() && owns(mob) && isSacrificeable(mob);
    }

    /** 这一只算不算"场上的助战 Boss"（麻雀/迷你/终章巨人/小怪都不算）。 */
    private static boolean isSummonedBoss(Mob mob) {
        if (mob instanceof EasterBossEntity boss) {
            return boss.getVariant() != EasterBossEntity.SPARROW_VARIANT && !boss.isMini() && !boss.isGiant();
        }
        return bossProfile(profile(mob));
    }

    /** 这一只能不能被献祭：是助战 Boss 才行 —— **麻雀永远不行**（用户要求）。 */
    private static boolean isSacrificeable(Mob mob) {
        return isSummonedBoss(mob) && bossProfile(profile(mob));
    }

    public static boolean isInheritedSummon(Entity entity) {
        return entity.getPersistentData().contains(CHILD_EXPIRY);
    }

    public boolean trySacrifice(Collection<UUID> directSummons) {
        if (!(this.owner.level() instanceof ServerLevel level) || used() || !this.owner.isAlive()
                || this.owner.isNoAi() || this.owner.isInvulnerable() || this.owner.isMini() || this.owner.isGiant()
                || this.owner.getVariant() != EasterBossEntity.LEMONCAT_VARIANT) return false;
        /* ★ v171 闸门（按用户要求）：场上助战 Boss 不足 MIN_SUMMONED_BOSSES 位就不献祭。
           以前是"有召唤物就献"，日志实证献祭了 6 个里只有 2 个是真 Boss、
           15 个里 13 个是茶芽/小老鼠 —— 现在只认真 Boss，且数量要够。
           麻雀既不算数也不会被献祭（见 isSacrificeable / summonBossCount）。 */
        int summonedBosses = summonBossCount(level);
        if (summonedBosses < requiredBosses()) {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] 献祭条件未满足: 场上助战 Boss {}/{} —— 本次不献祭",
                    summonedBosses, requiredBosses());
            return false;
        }
        List<Mob> victims = new ArrayList<>();
        List<Mob> candidates = new ArrayList<>();
        Set<UUID> family = new HashSet<>(directSummons);
        family.add(this.owner.getUUID());
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob mob && mob.isAlive() && mob != this.owner
                    && isSacrificeable(mob)) candidates.add(mob);   // v171: 麻雀与小怪不进候选
        }
        boolean found;
        do {
            found = false;
            for (var iterator = candidates.iterator(); iterator.hasNext();) {
                Mob mob = iterator.next();
                Entity parent = ManorFightMembers.owner(mob);
                if (family.contains(mob.getUUID()) || owns(mob) || family.contains(MinionBlessing.ownerId(mob))
                        || parent != null && family.contains(parent.getUUID())) {
                    family.add(mob.getUUID());
                    victims.add(mob);
                    iterator.remove();
                    found = true;
                }
            }
        } while (found && !candidates.isEmpty());
        if (victims.size() < requiredBosses()) return false;
        victims = new ArrayList<>(victims.subList(0, requiredBosses()));
        CompoundTag state = state();
        CompoundTag profiles = new CompoundTag();
        state.putBoolean("used", true);
        state.putLong("until", untilFrom(level.getGameTime()));
        state.putInt(DEFEATED_HELPERS, 0);
        state.putInt("sacrificed", victims.size());
        state.putInt("bosses", (int) victims.stream().filter(victim -> bossProfile(profile(victim))).count());
        state.put("profiles", profiles);
        state.put("effects", new CompoundTag());
        LemonCombatBalance.absorbMaximumHealth(this.owner, victims);
        this.cachedSpells = null;
        for (Mob victim : victims) {
            String profile = profile(victim);
            if (!profile.isEmpty()) profiles.putBoolean(profile, true);
            /* ★ v171 按用户要求「柠檬不会获得 boss 的 buff，但是会得到 boss 的技能」:
               原来这里会把被献祭者身上的**所有有益效果**按 amplified 等级复制到柠檬猫身上
               （`grant(new MobEffectInstance(effect..., grantTicks(), effect.getAmplifier(), ...))`）——
               那正是"献祭几只就叠出一身抗性/吸收、变成打不死"的主要来源之一。
               现在**只继承 profile（=技能线）**，不复制任何增益。
               技能继承走 profiles → {@link #spells()} → LemonSpellBarrage，那一线没动。 */
        }
        net.farmingtales.ftboss.skill.LemonCatNetwork.sacrifice(this.owner, victims);
        for (Mob victim : victims) {
            Vec3 origin = victim.position().add(0, victim.getBbHeight() * 0.5, 0);
            Vec3 destination = this.owner.position().add(0, 1.1, 0);
            for (int step = 0; step < 12; step++) {
                Vec3 point = origin.lerp(destination, step / 11.0);
                level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.04, 0.04, 0.04, 0);
            }
            victim.discard();
        }
        net.farmingtales.ftboss.lemonmotion.LemonMotionServer.absorb(this.owner, 14, 3);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.owner.getX(), this.owner.getY() + 1,
                this.owner.getZ(), 48, 0.7, 1, 0.7, 0.06);
        level.playSound(null, this.owner.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 1.1F, 0.7F);
        announce("§e柠檬猫献祭了 " + victims.size() + " 个召唤物，继承其能力——§c直到它死亡§e！之后召唤数量和停止条件按当前单人/多人规则执行。");
        FTBossMod.LOGGER.info("[FTBoss][Lemon] sacrificed {} summons, profiles={}, until={}",
                victims.size(), profiles, state.getLong("until"));
        return true;
    }

    private static String profile(Mob mob) {
        if (mob instanceof net.farmingtales.ftboss.entity.RatBossEntity rat && rat.isMinion()) return "rat_minion";
        return switch (EntityType.getKey(mob.getType()).toString()) {
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
        if (!active()) return List.of();
        if (this.cachedSpells == null) this.cachedSpells = state().getCompound("profiles").getAllKeys().stream().sorted()
                .flatMap(profile -> BossSpellRules.forBoss("ftboss:" + profile).stream()).distinct().toList();
        return this.cachedSpells;
    }

    public void tick() {
        if (this.owner.level().isClientSide || !used()) return;
        if (!active()) {
            expire();
            return;
        }
        if (this.owner.isNoAi() || this.owner.isInvulnerable()) {
            this.abilities.pause();
            return;
        }
        runInherited(this.abilities::tick);
        if (this.owner.tickCount % 40 == 0 && this.owner.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT, this.owner.getX(), this.owner.getY() + 1.1,
                    this.owner.getZ(), 5, 0.6, 0.6, 0.6, 0.04);
        }
    }

    public void runInherited(Runnable action) {
        boolean previous = this.granting;
        this.granting = true;
        try {
            action.run();
        } finally {
            this.granting = previous;
        }
    }

    public boolean capturesEffects() {
        return this.granting && !this.applyingEffect && active();
    }

    public static boolean allowsSpellEffect(LivingEntity caster) {
        return !(caster instanceof EasterBossEntity lemon) || !lemon.lemonSacrifice().used() || lemon.lemonSacrifice().active();
    }

    public static void spellEffect(LivingEntity caster, Runnable action) {
        if (caster instanceof EasterBossEntity lemon && lemon.lemonSacrifice().active()) lemon.lemonSacrifice().runInherited(action);
        else action.run();
    }

    public boolean grant(MobEffectInstance effect) {
        if (!active() || !effect.getEffect().isBeneficial()) return false;
        int remaining = (int) Math.min(grantTicks(), state().getLong("until") - this.owner.level().getGameTime());
        MobEffectInstance current = this.owner.getEffect(effect.getEffect());
        int duration = Math.min(remaining, effect.isInfiniteDuration() ? remaining : effect.getDuration());
        if (current != null && (current.getAmplifier() > effect.getAmplifier()
                || current.getAmplifier() == effect.getAmplifier()
                && (current.isInfiniteDuration() || current.getDuration() >= duration))) return false;
        CompoundTag effects = state().getCompound("effects");
        String key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString();
        CompoundTag lease = effects.getCompound(key);
        if (!effects.contains(key) && current != null) {
            lease.put("previous", current.save(new CompoundTag()));
            lease.putLong("previousUntil", current.isInfiniteDuration() ? Long.MAX_VALUE
                    : this.owner.level().getGameTime() + current.getDuration());
        }
        MobEffectInstance granted = new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), true, true, true);
        boolean applied;
        this.applyingEffect = true;
        try {
            applied = this.owner.addEffect(granted);
        } finally {
            this.applyingEffect = false;
        }
        if (applied) {
            lease.putInt("amplifier", granted.getAmplifier());
            lease.putInt("duration", duration);
            effects.put(key, lease);
        }
        return applied;
    }

    private void expire() {
        if (state().getBoolean("expired")) return;
        this.abilities.reset();
        CompoundTag effects = state().getCompound("effects");
        for (String key : effects.getAllKeys()) {
            var effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation(key));
            if (effect == null) continue;
            MobEffectInstance current = this.owner.getEffect(effect);
            CompoundTag lease = effects.getCompound(key);
            if (current != null && current.getAmplifier() == lease.getInt("amplifier")
                    && !current.isInfiniteDuration() && current.getDuration() <= lease.getInt("duration")) {
                this.owner.removeEffect(effect);
                MobEffectInstance previous = MobEffectInstance.load(lease.getCompound("previous"));
                long left = lease.getLong("previousUntil") - this.owner.level().getGameTime();
                if (previous != null && left > 0) this.owner.addEffect(new MobEffectInstance(effect,
                        previous.isInfiniteDuration() ? -1 : (int) Math.min(Integer.MAX_VALUE, left), previous.getAmplifier()));
            }
        }
        this.owner.getPersistentData().remove("ftboss:signature_cast");
        if (this.owner.level() instanceof ServerLevel level) {
            List<Entity> children = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (owns(entity) && entity.getPersistentData().contains(CHILD_EXPIRY)) children.add(entity);
            }
            children.forEach(Entity::discard);
        }
        state().putBoolean("expired", true);
        state().remove("effects");
        if (this.owner.isAlive()) announce("§7柠檬猫的献祭继承已结束；献祭与助战击败计数不会重置。");
    }

    private void announce(String message) {
        if (this.owner.level() instanceof ServerLevel level) {
            for (var player : level.players()) {
                if (player.distanceToSqr(this.owner) <= 64 * 64) player.displayClientMessage(Component.literal(message), false);
            }
        }
    }

    /**
     * 柠檬猫只继承沙耶的进攻/回复技能，不继承“守护茶芽期间完全免伤”。
     * 保留这个查询入口用于兼容旧校验与外部调用，但它必须永远为 false。
     */
    public boolean shielded(net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    public void onMeleeHit(LivingEntity target, float dealt) {
        if (active()) runInherited(() -> this.abilities.onMeleeHit(target, dealt));
    }

    public void onRemoval(Entity.RemovalReason reason) {
        if (!this.owner.level().isClientSide && used()) {
            this.abilities.pause();
            if (reason.shouldDestroy()) expire();
        }
    }

    public static void inheritLifetime(Entity parent, Entity child) {
        long until = parent.getPersistentData().getLong(CHILD_EXPIRY);
        if (parent instanceof EasterBossEntity boss && boss.lemonSacrifice().active() && boss.lemonSacrifice().granting) {
            until = boss.lemonSacrifice().state().getLong("until");
        }
        if (until > 0) child.getPersistentData().putLong(CHILD_EXPIRY, until);
    }

    public static EasterBossEntity ratLeader(Entity rat) {
        if (!(rat.level() instanceof ServerLevel level) || !rat.getPersistentData().contains(CHILD_EXPIRY)) return null;
        UUID owner = ManorFightMembers.ownerId(rat);
        return owner != null && level.getEntity(owner) instanceof EasterBossEntity boss
                && boss.lemonSacrifice().has("rat_boss") ? boss : null;
    }

    @SubscribeEvent
    public static void fireRecovery(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (event.getEntity() instanceof EasterBossEntity lemon && !lemon.level().isClientSide
                && lemon.lemonSacrifice().has("wine_barrel") && event.getAmount() > 0
                && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            lemon.heal(event.getAmount() * 0.75F);
        }
    }

    @SubscribeEvent
    public static void expireSummon(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        long until = entity.getPersistentData().getLong(CHILD_EXPIRY);
        if (until > 0 && (entity.level().getGameTime() >= until || entity.tickCount % 20 == 0
                && entity.level() instanceof ServerLevel level && ManorFightMembers.ownerId(entity) != null
                && level.getEntity(ManorFightMembers.ownerId(entity)) instanceof EasterBossEntity boss
                && !boss.lemonSacrifice().active())) entity.discard();
    }

    @SubscribeEvent
    public static void preventExpiredJoin(EntityJoinLevelEvent event) {
        long until = event.getEntity().getPersistentData().getLong(CHILD_EXPIRY);
        if (!event.getLevel().isClientSide && until > 0 && event.getLevel().getGameTime() >= until) event.setCanceled(true);
    }
}
